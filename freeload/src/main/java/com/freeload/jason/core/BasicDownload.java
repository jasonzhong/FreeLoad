package com.freeload.jason.core;

import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class BasicDownload implements INetwork {

    /** connect to get downloadfile head timeout count. */
    private final static int CONNECT_TIMEOUT = 5 * 1000;
    private final static int CONTAINER_SIZE = 8 * 1024;

    @Override
    public void performRequest(Request<?> request) {
        performRequest(request, null);
    }

    @Override
    public boolean performRequest(Request<?> request, ResponseDelivery delivery) {
        boolean transferSucc = false;

        long startDownloadPos = request.getDownloadStart();
        long endDownloadPos = request.getDownloadEnd();
        long fileStartPos = request.getWriteFileStart();
        long fileEndPos = request.getWriteFileEnd();

        postResponse(request, delivery, DownloadReceipt.STATE.START,
                startDownloadPos, endDownloadPos,
                fileStartPos, fileEndPos);

        if (endDownloadPos <= 0 ||
                startDownloadPos > endDownloadPos) {
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
            http.setConnectTimeout(CONNECT_TIMEOUT);
            http.setReadTimeout(1000);
            http.setRequestProperty("Range", "bytes=" + startDownloadPos + "-"+ endDownloadPos);//设置获取实体数据的范围

            int exception = http.getResponseCode();
            switch (exception) {
                case HttpURLConnection.HTTP_OK :
                case HttpURLConnection.HTTP_PARTIAL :
                    transferSucc = transferData(request, delivery, http, finalDownload);
                    break;
                case 301:
                case 302:
                case 303:
                case 307:
                    if (finalDownload) {
                        postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
                                startDownloadPos, endDownloadPos,
                                fileStartPos, fileEndPos);
                    }
                    break;
                case 416:
                case 500:
                case 503:
                    if (finalDownload) {
                        postResponse(request, delivery, DownloadReceipt.STATE.TIMEOUT,
                                startDownloadPos, endDownloadPos,
                                fileStartPos, fileEndPos);
                    }
                    break;
                default:
                    if (finalDownload) {
                        postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
                                startDownloadPos, endDownloadPos,
                                fileStartPos, fileEndPos);
                    }
                    break;
            }

        } catch (Exception e) {
            if (finalDownload) {
                postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
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

        byte[] buffer = new byte[CONTAINER_SIZE];

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
                    offset = inStream.read(buffer, 0, CONTAINER_SIZE);
                } catch (IOException io) {
                    if (finalDownload) {
                        postResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM,
                                startDownloadPos, endDownloadPos,
                                writeFileLength, fileEndPos);
                    }
                    result = false;
                    break;
                }

                if (request.isCanceled()) {
                    postResponse(request, delivery, DownloadReceipt.STATE.CANCEL,
                            startDownloadPos, endDownloadPos,
                            writeFileLength, fileEndPos);
                    break;
                }

                if (offset <= -1) {
                    if (writeFileLength < fileEndPos) {
                        if (finalDownload) {
                            postResponse(request, delivery, DownloadReceipt.STATE.FAILED_GET_STREAM,
                                    startDownloadPos, endDownloadPos,
                                    writeFileLength, fileEndPos);
                        }
                        result = false;
                    } else {
                        postResponse(request, delivery, DownloadReceipt.STATE.SUCCESS_DOWNLOAD,
                                startDownloadPos, endDownloadPos,
                                writeFileLength, fileEndPos);
                    }
                    break;
                }

                threadfile.write(buffer, 0, offset);
                writeFileLength += offset;
                startDownloadPos += offset;

                finalDownload = false;
                request.setDownloadRetryCount(0);
                postProgress(request, delivery, DownloadReceipt.STATE.DOWNLOAD,
                        startDownloadPos, endDownloadPos,
                        writeFileLength, fileEndPos);

                request.setDownloadStart(startDownloadPos + writeFileLength);
                request.setDownloadEnd(endDownloadPos);

                request.setWriteFileStart(writeFileLength);
                request.setWriteFileEnd(fileEndPos);

                if (request.isCanceled()) {
                    postResponse(request, delivery, DownloadReceipt.STATE.CANCEL,
                            startDownloadPos, endDownloadPos,
                            writeFileLength, fileEndPos);
                    break;
                }
            }
        } catch (Exception e) {
            if (finalDownload) {
                postResponse(request, delivery, DownloadReceipt.STATE.CONNWRONG,
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

    private void postResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                              long downLoadFileSize, long downloadLength,
                              long writeFileSize, long writeFileLength) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                downLoadFileSize, downloadLength, writeFileSize, writeFileLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    private void postProgress(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                              long downLoadFileSize, long downloadLength,
                              long writeFileSize, long writeFileLength) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                downLoadFileSize, downloadLength, writeFileSize, writeFileLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    private DownloadReceipt getDownloadReceipt(Request<?> request, DownloadReceipt.STATE state,
                                               long downLoadFileSize, long downloadLength,
                                               long writeFileSize, long writeFileLength) {
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadState(state);
        downloadReceipt.setDownloadedSize(downLoadFileSize);
        downloadReceipt.setTotalDownloadSize(downloadLength);
        downloadReceipt.setWriteFileSize(writeFileSize);
        downloadReceipt.setWriteFileTotalSize(writeFileLength);
        return downloadReceipt;
    }
}
