package com.freeload.jason.dbtool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Field;

public class SQLiteHelper extends SQLiteOpenHelper {

    private DBUtils dbUtils;

    private Class mClazz;

    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, Class clazz) {
        super(context, name, factory, version);
        this.mClazz = clazz;
    }

    public SQLiteHelper(Context context, String name, int version, Class mClazz) {
        this(context, name, null, version, mClazz);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL(getCreateTableSql(mClazz));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + DBUtils.getTableName(mClazz));
        createTable(db);
    }

    private String getCreateTableSql(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        //将类名作为表名
        String tabName = dbUtils.getTableName(clazz);
        sb.append("create table ").append(tabName).append(" (id  INTEGER PRIMARY KEY AUTOINCREMENT, ");
        //得到类中所有属性对象数组
        Field[] fields = clazz.getDeclaredFields();
        for (Field fd : fields) {
            String fieldName = fd.getName();
            String fieldType = fd.getType().getName();
            if (fieldName.equalsIgnoreCase("_id") || fieldName.equalsIgnoreCase("id")) {
                continue;
            } else {
                sb.append(fieldName).append(dbUtils.getColumnType(fieldType)).append(", ");
            }
        }
        int len = sb.length();
        sb.replace(len - 2, len, ")");
        return sb.toString();
    }
}
