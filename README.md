# Freeload 

标签：Freeload

---

## 概括
> Freeload 是一款下载引擎。通过它你可以很方便的添加下载任务，并且实时获取下载过程。该引擎轻巧易于维护，并且拥有很好的扩展性。在新版的引擎中添加了多线程下载单资源的支持，能够支持双线程、三线程甚至四线程同步下载单资源。使用上支持链式编程，让代码编辑更加合理。

## 使用说明
### 1、创建下载请求队列
```java
private RequestQueue requestQueue = null;
requestQueue = Freeload.newRequestQueue(context);
```

### 2、创建下载请求并将下载请求添加到下载队列里
```java
private DownloadRequestManager request = null;

// 创建监听回调
request = DownloadRequestManager.create(id, Url)
    .setDownloadThreadType(DownloadThreadType.DOUBLETHREAD)
    .setListener(new Response.Listener<IReceipt>() {
        @Override
        public void onProgressChange(IReceipt s) {
            System.out.println(s.getReceipt());
        }
    })
    .addRequestQueue(requestQueue);
```

1. `DownloadRequestManager.create(id, Url)`创建一个`DownloadRequestManager`对象，参数为下载的id和下载的url。
2. `setDownloadThreadType(DownloadThreadType.DOUBLETHREAD)`设置下载的线程数，Freeload下载库支持多线程同步下载单资源与多资源。常见的有NORMAL`DownloadThreadType.`单线程和`DownloadThreadType.DOUBLETHREAD`双线程两个。
3. `setListener(new Response.Listener<IReceipt>()`设置监听对象，实时将下载信息同步传回。
4. `addRequestQueue(requestQueue)`将任务添加到下载队列里。

### 3、反馈系统使用
```java
//反馈凭条
private String receipt = "";

@Override
public void onProgressChange(IReceipt s) {
    receipt = s.getReceipt();
    System.out.println(s.getReceipt());
}

requestDoublie = DownloadManager.create()
        .setEscapeReceipt(receipt)
        .setDownloadThreadType(DownloadThreadType.DOUBLETHREAD)
        .setListener(new Response.Listener<IReceipt>() {
            @Override
            public void onProgressChange(IReceipt s) {
                receipt = s.getReceipt();
                System.out.println(s.getReceipt());
            }
        })
        .addRequestQueue(requestQueue);
```
1. 在监听函数里保存反馈的凭条，随意找个String内存保存即可。
2. 在resume操作的时候使用`setEscapeReceipt(receipt)`函数将凭条输入。
3. 系统会根据凭条的内容自行继续下载。

### 4、在AndroidManifest里添加权限请求
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_INTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
```
