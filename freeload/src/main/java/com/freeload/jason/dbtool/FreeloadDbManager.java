package com.freeload.jason.dbtool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class FreeloadDbManager {

    private SQLiteHelper mHelper = null;
    private SQLiteDatabase mDbBase = null;

    private Context mContext = null;
    private String mDbName;

    public FreeloadDbManager(Context context, String dbName, int version, Class<?> clazz){
        mHelper = new SQLiteHelper(context, dbName, version, clazz);
        mDbBase = mHelper.getWritableDatabase();

        this.mContext = context;
        this.mDbName = dbName;
    }

    public void closeDataBase() {
        mDbBase.close();
        mHelper = null;
        mDbBase = null;
    }

    public boolean deleteDataBase() {
        return mContext.deleteDatabase(mDbName);
    }

    public long insert(Object obj) {
        Class<?> modeClass = obj.getClass();
        Field[] fields = modeClass.getDeclaredFields();
        ContentValues values = new ContentValues();

        for (Field fd : fields) {
            fd.setAccessible(true);
            String fieldName = fd.getName();
            //剔除主键id值得保存，由于框架默认设置id为主键自动增长
            if (fieldName.equalsIgnoreCase("id") || fieldName.equalsIgnoreCase("_id")) {
                continue;
            }
            putValues(values, fd, obj);
        }
        return mDbBase.insert(DBUtils.getTableName(modeClass), null, values);
    }

    private void putValues(ContentValues values, Field fd, Object obj) {
        Class<?> clazz = values.getClass();
        try {
            Object[] parameters = new Object[]{fd.getName(), fd.get(obj)};
            Class<?>[] parameterTypes = getParameterTypes(fd, fd.get(obj), parameters);
            Method method = clazz.getDeclaredMethod("put", parameterTypes);
            method.setAccessible(true);
            method.invoke(values, parameters);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
        Class<?>[] parameterTypes;
        if (isCharType(field)) {
            parameters[1] = String.valueOf(fieldValue);
            parameterTypes = new Class[]{String.class, String.class};
        } else {
            if (field.getType().isPrimitive()) {
                parameterTypes = new Class[]{String.class, getObjectType(field.getType())};
            } else if ("java.util.Date".equals(field.getType().getName())) {
                parameterTypes = new Class[]{String.class, Long.class};
            } else {
                parameterTypes = new Class[]{String.class, field.getType()};
            }
        }
        return parameterTypes;
    }

    private boolean isCharType(Field field) {
        String type = field.getType().getName();
        return type.equals("char") || type.endsWith("Character");
    }

    private Class<?> getObjectType(Class<?> primitiveType) {
        if (primitiveType == null) {
            return null;
        }

        if (!primitiveType.isPrimitive()) {
            return null;
        }

        String basicTypeName = primitiveType.getName();
        if ("int".equals(basicTypeName)) {
            return Integer.class;
        } else if ("short".equals(basicTypeName)) {
            return Short.class;
        } else if ("long".equals(basicTypeName)) {
            return Long.class;
        } else if ("float".equals(basicTypeName)) {
            return Float.class;
        } else if ("double".equals(basicTypeName)) {
            return Double.class;
        } else if ("boolean".equals(basicTypeName)) {
            return Boolean.class;
        } else if ("char".equals(basicTypeName)) {
            return Character.class;
        }

        return null;
    }

    public <T> List<T> findAll(Class<T> clazz) {
        Cursor cursor = mDbBase.query(clazz.getSimpleName(), null, null, null, null, null, null);
        return getEntity(cursor, clazz);
    }

    public <T> T findById(Class<T> clazz, int id) {
        Cursor cursor = mDbBase.query(clazz.getSimpleName(), null, "id=" + id, null, null, null, null);
        List<T> list = getEntity(cursor, clazz);
        return list.get(0);
    }

    public void deleteById(Class<?> clazz, long id) {
        mDbBase.delete(DBUtils.getTableName(clazz), "id=" + id, null);
    }

    public void deleteTable(Class<?> clazz) {
        mDbBase.execSQL("DROP TABLE IF EXISTS" + DBUtils.getTableName(clazz));
    }

    public void updateById(Class<?> clazz, ContentValues values, long id) {
        mDbBase.update(clazz.getSimpleName(), values, "id=" + id, null);
    }

    private <T> List<T> getEntity(Cursor cursor, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Field[] fields = clazz.getDeclaredFields();
                    T modeClass = clazz.newInstance();
                    for (Field field : fields) {
                        Class<?> cursorClass = cursor.getClass();
                        String columnMethodName = getColumnMethodName(field.getType());
                        Method cursorMethod = cursorClass.getMethod(columnMethodName, int.class);

                        Object value = cursorMethod.invoke(cursor, cursor.getColumnIndex(field.getName()));

                        if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                            if ("0".equals(String.valueOf(value))) {
                                value = false;
                            } else if ("1".equals(String.valueOf(value))) {
                                value = true;
                            }
                        } else if (field.getType() == char.class || field.getType() == Character.class) {
                            value = ((String) value).charAt(0);
                        } else if (field.getType() == Date.class) {
                            long date = (Long) value;
                            if (date <= 0) {
                                value = null;
                            } else {
                                value = new Date(date);
                            }
                        }
                        String methodName = makeSetterMethodName(field);
                        Method method = clazz.getDeclaredMethod(methodName, field.getType());
                        method.invoke(modeClass, value);
                    }
                    list.add(modeClass);
                } while (cursor.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    private String getColumnMethodName(Class<?> fieldType) {
        String typeName;
        if (fieldType.isPrimitive()) {
            typeName = DBUtils.capitalize(fieldType.getName());
        } else {
            typeName = fieldType.getSimpleName();
        }
        String methodName = "get" + typeName;
        if ("getBoolean".equals(methodName)) {
            methodName = "getInt";
        } else if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            methodName = "getString";
        } else if ("getDate".equals(methodName)) {
            methodName = "getLong";
        } else if ("getInteger".equals(methodName)) {
            methodName = "getInt";
        }
        return methodName;
    }


    private boolean isPrimitiveBooleanType(Field field) {
        Class<?> fieldType = field.getType();
        if ("boolean".equals(fieldType.getName())) {
            return true;
        }
        return false;
    }

    private String makeSetterMethodName(Field field) {
        String setterMethodName;
        String setterMethodPrefix = "set";
        if (isPrimitiveBooleanType(field) && field.getName().matches("^is[A-Z]{1}.*$")) {
            setterMethodName = setterMethodPrefix + field.getName().substring(2);
        } else if (field.getName().matches("^[a-z]{1}[A-Z]{1}.*")) {
            setterMethodName = setterMethodPrefix + field.getName();
        } else {
            setterMethodName = setterMethodPrefix + DBUtils.capitalize(field.getName());
        }
        return setterMethodName;
    }

}
