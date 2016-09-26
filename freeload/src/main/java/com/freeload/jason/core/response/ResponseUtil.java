package com.freeload.jason.core.response;

import com.freeload.jason.core.Request;
import com.freeload.jason.toolbox.DownloadReceipt;

public class ResponseUtil {

    public static void postFileResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                                        String filePath) {
        if (delivery == null) {
            return;
        }

        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadFilePath(filePath);
        downloadReceipt.setDownloadState(state);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    public static void postDownloadResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state) {
        if (delivery == null || request == null) {
            return;
        }

        long startPos = request.getDownloadStart();
        long totalLength = request.getDownloadEnd();
        long startFilePos = request.getWriteFileStart();
        long totalFileLength = request.getWriteFileEnd();

        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                startPos, totalLength, startFilePos, totalFileLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    public static void postDownloadResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                                            long startPos, long totalLength) {
        if (delivery == null) {
            return;
        }

        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                startPos, totalLength, startPos, totalLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    public static void postDownloadResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                                            long startPos, long totalLength,
                                            long startFilePos, long totalFileLength) {
        if (delivery == null) {
            return;
        }

        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                startPos, totalLength, startFilePos, totalFileLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    public static void postDownloadProgress(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state) {
        if (delivery == null) {
            return;
        }

        long startDownloadPos = request.getDownloadStart();
        long endDownloadPos = request.getDownloadEnd();
        long startFilePos = request.getWriteFileStart();
        long endFilePos = request.getWriteFileEnd();

        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                startDownloadPos, endDownloadPos, startFilePos, endFilePos);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    public static void postDownloadProgress(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                                            long downLoadFileSize, long downloadLength,
                                            long writeFileSize, long writeFileLength) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                downLoadFileSize, downloadLength, writeFileSize, writeFileLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    public static void postPepareResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state, long fileSize) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadState(state);
        downloadReceipt.setDownloadedSize(fileSize);

        delivery.postDownloadPepare(request, Response.success(downloadReceipt));
    }

    private static DownloadReceipt getDownloadReceipt(Request<?> request, DownloadReceipt.STATE state,
                                                      long downLoadFileSize, long downloadLength,
                                                      long writeFileSize, long writeFileLength) {
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request != null ? request.getThreadPosition() : 0);
        downloadReceipt.setDownloadState(state);
        downloadReceipt.setDownloadedSize(downLoadFileSize);
        downloadReceipt.setTotalDownloadSize(downloadLength);
        downloadReceipt.setWriteFileSize(writeFileSize);
        downloadReceipt.setWriteFileTotalSize(writeFileLength);
        return downloadReceipt;
    }
}
