package com.freeload.jason.core.download;

import com.freeload.jason.core.Request;
import com.freeload.jason.core.response.ResponseDelivery;
import com.freeload.jason.core.response.ResponseUtil;
import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class BasicDownload implements INetwork {

    @Override
    public boolean performRequest(Request<?> request, ResponseDelivery delivery) {
        boolean transferSucc = false;

        long startDownloadPos = request.getDownloadStart();
        long endDownloadPos = request.getDownloadEnd();
        long fileStartPos = request.getWriteFileStart();
        long fileEndPos = request.getWriteFileEnd();

        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.START,
                startDownloadPos, endDownloadPos,
                fileStartPos, fileEndPos);

        if (endDownloadPos <= 0 || startDownloadPos > endDownloadPos) {
            return transferSucc;
        }

        transferSucc = downloadCore(request, delivery, startDownloadPos, endDownloadPos, fileStartPos, fileEndPos, false);
        return transferSucc;
    }

    private boolean downloadCore(Request<?> request, ResponseDelivery delivery,
                                 long startDownloadPos, long endDownloadPos,
                                 long fileStartPos, long fileEndPos,
                                 boolean finalDownload) {

        boolean transferSucc = false;
        HttpURLConnection http = null;
        try {
            URL downloadUrl = new URL(request.getUrl());

            http = (HttpURLConnection) downloadUrl.openConnection();
            http.setConnectTimeout(request.getConnectTimeOut());
            http.setReadTimeout(request.getReadTimeOut());
            http.setRequestProperty("Range", "bytes=" + startDownloadPos + "-"+ endDownloadPos);//设置获取实体数据的范围

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
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
                                startDownloadPos, endDownloadPos,
                                fileStartPos, fileEndPos);
                    }
                    break;
                case 416:
                case 500:
                case 503:
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.TIMEOUT,
                                startDownloadPos, endDownloadPos,
                                fileStartPos, fileEndPos);
                    }
                    break;
                default:
                    if (finalDownload) {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
                                startDownloadPos, endDownloadPos,
                                fileStartPos, fileEndPos);
                    }
                    break;
            }

        } catch (Exception e) {
            if (finalDownload) {
                ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
                        startDownloadPos, endDownloadPos,
                        fileStartPos, fileEndPos);
            }
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }

        return transferSucc;
    }

    @Override
    public boolean retryPerformRequest(Request<?> request, ResponseDelivery delivery, boolean finalDownload) {
        boolean transferSucc = false;

        long startDownloadPos = request.getDownloadStart();
        long endDownloadPos = request.getDownloadEnd();
        long fileStartPos = request.getWriteFileStart();
        long fileEndPos = request.getWriteFileEnd();

        if (endDownloadPos <= 0 ||
                startDownloadPos > endDownloadPos) {
            return transferSucc;
        }

        transferSucc = downloadCore(request, delivery, startDownloadPos, endDownloadPos, fileStartPos, fileEndPos, finalDownload);

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

        long startDownloadPos = request.getDownloadStart();
        long endDownloadPos = request.getDownloadEnd();

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
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM,
                                startDownloadPos, endDownloadPos,
                                writeFileLength, fileEndPos);
                    }
                    result = false;
                    break;
                }

                if (request.isCanceled()) {
                    ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CANCEL,
                            startDownloadPos, endDownloadPos,
                            writeFileLength, fileEndPos);
                    break;
                }

                if (offset <= -1) {
                    if (writeFileLength < fileEndPos) {
                        if (finalDownload) {
                            ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM,
                                    startDownloadPos, endDownloadPos,
                                    writeFileLength, fileEndPos);
                        }
                        result = false;
                    } else {
                        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.SUCCESS_DOWNLOAD,
                                startDownloadPos, endDownloadPos,
                                writeFileLength, fileEndPos);
                    }
                    break;
                }

                threadfile.write(buffer, 0, offset);
                writeFileLength += offset;
                startDownloadPos += offset;

                finalDownload = false;
                request.setRetryCount(0);
                ResponseUtil.postDownloadProgress(request, delivery, DownloadReceipt.STATE.DOWNLOAD,
                        startDownloadPos, endDownloadPos,
                        writeFileLength, fileEndPos);

                request.setDownloadStart(startDownloadPos + writeFileLength);
                request.setDownloadEnd(endDownloadPos);

                request.setWriteFileStart(writeFileLength);
                request.setWriteFileEnd(fileEndPos);

                if (request.isCanceled()) {
                    ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CANCEL,
                            startDownloadPos, endDownloadPos,
                            writeFileLength, fileEndPos);
                    break;
                }
            }
        } catch (Exception e) {
            if (finalDownload) {
                ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
                        startDownloadPos, endDownloadPos,
                        fileStartPos, fileEndPos);
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
