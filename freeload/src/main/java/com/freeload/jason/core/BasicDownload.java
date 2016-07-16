package com.freeload.jason.core;

import com.freeload.jason.core.Network;
import com.freeload.jason.core.Request;
import com.freeload.jason.core.Response;
import com.freeload.jason.core.ResponseDelivery;
import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class BasicDownload implements Network {

    /** connect to get downloadfile head timeout count. */
    private final static int CONNECT_TIMEOUT = 5 * 1000;

    @Override
    public void performRequest(Request<?> request) {
        performRequest(request, null);
    }

    @Override
    public void performRequest(Request<?> request, ResponseDelivery delivery) {
        long downloadLength = 0;

        HttpURLConnection http = null;
        try {
            URL downloadUrl = new URL(request.getUrl());
            postResponse(request, delivery, DownloadReceipt.STATE.START);

            //使用Get方式下载

            http = (HttpURLConnection) downloadUrl.openConnection();
            http.setConnectTimeout(CONNECT_TIMEOUT);
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            http.setRequestProperty("Accept-Language", "zh-CN");
            http.setRequestProperty("Referer", downloadUrl.toString());
            http.setRequestProperty("Charset", "UTF-8");

            long startPos = request.getDownloadStart();
            long endPos = request.getDownloadEnd();

            http.setRequestProperty("Range", "bytes=" + startPos + "-"+ endPos);//设置获取实体数据的范围
            http.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            http.setRequestProperty("Connection", "Keep-Alive");

            int exception = http.getResponseCode();
            switch (exception) {
                case 200 :
                case 206 :
                    transferData(request, delivery, downloadLength, http);
                    break;
                case 301:
                case 302:
                case 303:
                case 307:
                    postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
                    break;
                case 416:
                case 500:
                case 503:
                    postResponse(request, delivery, DownloadReceipt.STATE.TIMEOUT);
                    break;
                default:
                    postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
                    break;
            }

        } catch (Exception e) {
            postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    private void transferData(Request<?> request, ResponseDelivery delivery, long downloadLength, HttpURLConnection http) throws IOException {
        InputStream inStream = null;
        byte[] buffer = new byte[1024];

        int offset = 0;
        long startPos = request.getWriteFileStart();
        long endPos = request.getWriteFileEnd();

        RandomAccessFile threadfile = new RandomAccessFile(request.getSaveFile(), "rwd");
        threadfile.seek(startPos);

        inStream = http.getInputStream();

        while (true) {
            offset = inStream.read(buffer, 0, 1024);
            if (offset == -1) {
                postResponse(request, delivery, DownloadReceipt.STATE.SUCCESS_DOWNLOAD);
                break;
            }

            threadfile.write(buffer, 0, offset);
            downloadLength += offset;

            if (request.isCanceled()) {
                postResponse(request, delivery, DownloadReceipt.STATE.CANCEL);
                break;
            }

            postProgress(request, delivery, DownloadReceipt.STATE.DOWNLOAD, startPos + downloadLength, endPos);
        }

        threadfile.close();
        inStream.close();
    }

    private void postResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state, 0, 0);
        delivery.postResponse(request, Response.success(downloadReceipt));
    }

    private void postProgress(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state, long downLoadFileSize, long downloadLength) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state, downLoadFileSize, downloadLength);
        delivery.postResponse(request, Response.success(downloadReceipt));
    }

    private DownloadReceipt getDownloadReceipt(Request<?> request, DownloadReceipt.STATE state, long downLoadFileSize, long downloadLength) {
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadState(state);
        downloadReceipt.setDownloadedSize(downLoadFileSize);
        downloadReceipt.setDownloadTotalSize(downloadLength);
        return downloadReceipt;
    }
}
