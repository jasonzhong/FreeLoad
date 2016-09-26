package com.freeload.jason.core.download;

import com.freeload.jason.core.Request;
import com.freeload.jason.core.response.ResponseDelivery;
import com.freeload.jason.core.response.ResponseUtil;
import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SingleDownload implements INetwork {
    @Override
    public boolean performRequest(Request<?> request, ResponseDelivery delivery) {
        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.START);

        boolean checkOut = checkOutData(request);
        if (!checkOut) {
            return false;
        }

        boolean downloadPerform = downloadCore(request, delivery, false);
        if (downloadPerform) {
            return true;
        }

        boolean downloadRetry = retryPerform(request, delivery);
        if (!downloadRetry && !downloadPerform) {
            ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CANCEL);
            return false;
        }
        return true;
    }

    private boolean retryPerform(Request<?> request, ResponseDelivery delivery) {
        boolean downloadRetry = false;
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

            downloadRetry = retryPerformRequest(request, delivery, finalDownload);
            if (downloadRetry) {
                break;
            }
        }
        return downloadRetry;
    }

    @Override
    public boolean retryPerformRequest(Request<?> request, ResponseDelivery delivery, boolean finalDownload) {
        boolean checkOut = checkOutData(request);
        if (!checkOut) {
            return false;
        }

        boolean transferCore = downloadCore(request, delivery, finalDownload);
        if (!transferCore) {
            return false;
        }
        return true;
    }

    private boolean checkOutData(Request<?> request) {
        if (request == null) {
            return false;
        }

        long startFilePos = request.getWriteFileStart();
        long endFilePos = request.getWriteFileEnd();

        if (endFilePos <= 0 || startFilePos > endFilePos) {
            return false;
        }
        return true;
    }

    private URL createUrl(Request<?> request) {
        URL downloadUrl = null;
        try {
            downloadUrl = new URL(request.getUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return downloadUrl;
    }

    private boolean downloadCore(Request<?> request, ResponseDelivery delivery, boolean finalDownload) {
        if (request == null) {
            return false;
        }

        URL downloadUrl = createUrl(request);
        if (downloadUrl == null) {
            return false;
        }

        boolean transferSucc = false;

        HttpURLConnection http = null;
        try {
            long startFilePos = request.getWriteFileStart();
            long endFilePos = request.getWriteFileEnd();

            http = (HttpURLConnection) downloadUrl.openConnection();
            http.setConnectTimeout(request.getConnectTimeOut());
            http.setReadTimeout(request.getReadTimeOut());
            http.setRequestProperty("Range", "bytes=" + startFilePos + "-"+ endFilePos);//设置获取实体数据的范围

            int exception = http.getResponseCode();
            switch (exception) {
                case HttpURLConnection.HTTP_OK :
                case HttpURLConnection.HTTP_PARTIAL :
                    transferSucc = transferData(request, delivery, http, finalDownload);
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

        return transferSucc;
    }

    private boolean transferData(Request<?> request, ResponseDelivery delivery, HttpURLConnection http
            , boolean finalDownload) throws IOException {
        boolean result = true;

        InputStream inStream = null;

        byte[] buffer = new byte[request.getContainerSize()];

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
                    offset = inStream.read(buffer, 0, request.getContainerSize());
                } catch (IOException io) {
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM);
                    }
                    result = false;
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
                        result = false;
                    } else {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.SUCCESS_DOWNLOAD);
                    }
                    break;
                }

                threadfile.write(buffer, 0, offset);
                writeFileLength += offset;

                finalDownload = false;
                request.setRetryCount(0);
                ResponseUtil.postDownloadProgress(request, delivery, DownloadReceipt.STATE.DOWNLOAD);

                request.setWriteFileStart(writeFileLength);
                request.setWriteFileEnd(fileEndPos);

                if (request.isCanceled()) {
                    ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CANCEL);
                    break;
                }
            }
        } catch (Exception e) {
            if (finalDownload) {
                ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG);
            }
            result = false;
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

        return result;
    }
}
