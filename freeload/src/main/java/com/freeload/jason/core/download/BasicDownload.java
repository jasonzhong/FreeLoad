package com.freeload.jason.core.download;

import com.freeload.jason.core.Request;
import com.freeload.jason.core.response.ResponseDelivery;
import com.freeload.jason.core.response.ResponseUtil;
import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BasicDownload {

    private static long mRangeStart = 0;
    private static long mRangeEnd = 0;
    private static int mRangeLength = 0;

    public static void setDownloadRange(long start, long end) {
        if (start >= end) {
            return;
        }

        mRangeStart = start;
        mRangeEnd = end;

        mRangeLength = (int) (mRangeEnd - mRangeStart);
    }

    public static boolean performRequest(Request<?> request, ResponseDelivery delivery) {
        byte[] downloadByte = downloadCore(request, delivery, false);
        if (downloadByte != null && downloadByte.length == mRangeLength) {
            return true;
        }

        boolean downloadRetry = retryPerform(request, delivery);
        if (!downloadRetry) {
            return false;
        }
        return true;
    }

    private static boolean retryPerform(Request<?> request, ResponseDelivery delivery) {
        while (request.getRetryCount() < request.getRetryLimiteCount()) {
            if (request.isCanceled()) {
                request.finish();
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            request.addRetryCount();

            boolean finalDownload = false;
            if (request.getRetryCount() == request.getRetryLimiteCount()) {
                finalDownload = true;
            }

            byte[] downloadByte = downloadCore(request, delivery, finalDownload);
            if (downloadByte != null && downloadByte.length == mRangeLength) {
                return true;
            }
        }
        return false;
    }

    private static URL createUrl(Request<?> request) {
        URL downloadUrl = null;
        try {
            downloadUrl = new URL(request.getUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return downloadUrl;
    }

    private static long getRangeStart(Request<?> request) {
        long rangeStart = mRangeStart;
        if (mRangeStart <= 0) {
            rangeStart = request.getWriteFileStart();
        }
        return rangeStart;
    }

    private static long getRangeEnd(Request<?> request) {
        long rangeEnd = mRangeEnd;
        if (mRangeEnd <= 0) {
            rangeEnd = request.getWriteFileEnd();
        }
        return rangeEnd;
    }

    private static byte[] downloadCore(Request<?> request, ResponseDelivery delivery, boolean finalDownload) {
        if (request == null) {
            return null;
        }

        URL downloadUrl = createUrl(request);
        if (downloadUrl == null) {
            return null;
        }

        byte[] downloadByte = null;

        HttpURLConnection http = null;
        try {
            long startRangePos = getRangeStart(request);
            long endRangePos = getRangeEnd(request);

            http = (HttpURLConnection) downloadUrl.openConnection();
            http.setConnectTimeout(request.getConnectTimeOut());
            http.setReadTimeout(request.getReadTimeOut());
            http.setRequestProperty("Range", "bytes=" + startRangePos + "-"+ endRangePos);//设置获取实体数据的范围

            int exception = http.getResponseCode();
            switch (exception) {
                case HttpURLConnection.HTTP_OK :
                case HttpURLConnection.HTTP_PARTIAL :
                    downloadByte = transferData(request, delivery, http, finalDownload);
                    break;
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_SEE_OTHER:
                case 307:
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
                    }
                    break;
                case 416:
                case 500:
                case 503:
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.TIMEOUT);
                    }
                    break;
                default:
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
                    }
                    break;
            }

        } catch (Exception e) {
            if (finalDownload) {
                ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
            }
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }

        return downloadByte;
    }

    private static byte[] append(byte[] org, byte[] to) {
        byte[] newByte = new byte[org.length + to.length];
        System.arraycopy(org, 0, newByte, 0, org.length);
        System.arraycopy(to, 0, newByte, org.length, to.length);
        return newByte;
    }

    private static byte[] transferData(Request<?> request, ResponseDelivery delivery, HttpURLConnection http,
                                        boolean finalDownload) throws IOException {
        InputStream inStream = null;

        byte[] downloadByte = new byte[0];
        byte[] downloadBuffer = new byte[request.getContainerSize()];

        int offset = 0;
        long fileStartPos = request.getWriteFileStart();
        long fileEndPos = request.getWriteFileEnd();

        long writeFileLength = fileStartPos;

        RandomAccessFile threadfile = new RandomAccessFile(request.getSaveFile(), "rwd");
        threadfile.seek(fileStartPos);

        try {
            inStream = http.getInputStream();

            while (true) {
                try {
                    offset = inStream.read(downloadBuffer, 0, request.getContainerSize());
                } catch (IOException io) {
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM);
                    }
                    break;
                }

                if (request.isCanceled()) {
                    ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CANCEL);
                    break;
                }

                if (offset <= -1) {
                    if (writeFileLength < fileEndPos) {
                        if (finalDownload) {
                            ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM);
                        }
                    } else {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.SUCCESS_DOWNLOAD);
                    }
                    break;
                }

                finalDownload = false;
                request.setRetryCount(0);
                downloadByte = append(downloadByte, downloadBuffer);

                if (request.isCanceled()) {
                    break;
                }
            }
        } catch (Exception e) {
            if (finalDownload) {
                ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
            }
        } finally {
            if (threadfile != null) {
                threadfile.close();
                threadfile = null;
            }
            if (inStream != null) {
                inStream.close();
                inStream = null;
            }
        }

        return downloadByte;
    }
}
