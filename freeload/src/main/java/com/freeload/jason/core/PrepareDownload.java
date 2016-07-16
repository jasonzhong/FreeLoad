package com.freeload.jason.core;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.Prepare;
import com.freeload.jason.core.Request;
import com.freeload.jason.core.Response;
import com.freeload.jason.core.ResponseDelivery;
import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class PrepareDownload implements Prepare {

    /** connect to get downloadfile head timeout count. */
    private final static int CONNECT_TIMEOUT = 5 * 1000;

    @Override
    public boolean preparePerform(Request<?> request, ResponseDelivery delivery) {
        postResponse(delivery, request, DownloadReceipt.STATE.GETSIZE);
        long downloadFileSize = getFileSize(request);
        if (downloadFileSize <= 0) {
            postResponse(delivery, request, DownloadReceipt.STATE.FAILED_GETSIZE);
            return false;
        }
        request.setDownloadFileSize(downloadFileSize);

        postResponse(delivery, request, DownloadReceipt.STATE.QUEST_PREPARE);
        boolean bParse = parseReceipt(request, downloadFileSize);
        if (!bParse) {
            postResponse(delivery, request, DownloadReceipt.STATE.FAILED_QUEST_PREPARE);
        }

        postResponse(delivery, request, DownloadReceipt.STATE.CREATEFILE);
        File saveFile = createFile(request);
        if (saveFile == null) {
            postResponse(delivery, request, DownloadReceipt.STATE.FAILED_CREATEFILE);
            return false;
        }
        request.setDownloadFile(saveFile);

        return true;
    }

    private boolean parseReceipt(Request<?> request, long downloadFileSize) {
        boolean bRes = true;
        int threadType = request.getThreadType();
        switch (threadType) {
            case DownloadThreadType.NORMAL:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 1);
                break;
            case DownloadThreadType.DOUBLETHREAD:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 2);
                break;
            default:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 1);
                break;
        }
        return bRes;
    }

    private boolean setQuestDownloadSizeInfo(Request<?> request, long downloadFileSize, int division) {
        long perSize = downloadFileSize / division;
        int nPos = request.getThreadPosition();

        String fileName = request.getFileName();
        fileName += "" + nPos;
        request.setFileName(fileName);

        DownloadReceipt downloadReceipt = request.getDownloadReceipt();
        int pos = nPos - 1;
        pos = ( pos >= 0 ? pos : 0 );
        if (downloadReceipt == null) {
            request.setDownloadStart(perSize * pos);
        } else {
            long lSize = downloadReceipt.getDownloadedSize(nPos);
            request.setDownloadStart(lSize == 0 ? (perSize * pos) : lSize);
        }

        if (downloadReceipt == null) {
            request.setWriteFileStart(0);
        } else {
            long lSize = downloadReceipt.getDownloadedSize(nPos);
            request.setWriteFileStart(lSize - (perSize * pos));
        }

        if (division == 1 || nPos == division) {
            request.setDownloadEnd(downloadFileSize);
            request.setWriteFileEnd(downloadFileSize - (perSize * pos));
        } else {
            request.setDownloadEnd(perSize * nPos - 1);
            request.setWriteFileEnd(perSize * nPos - 1);
        }

        if (division == 1) {
            request.setDownloadFilePerSize(downloadFileSize);
        } else {
            request.setDownloadFilePerSize(perSize);
        }

        return true;
    }

    private void postResponse(ResponseDelivery delivery, Request<?> request, DownloadReceipt.STATE state) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadState(state);

        delivery.postResponse(request, Response.success(downloadReceipt));
    }

    private File createFile(Request<?> request) {
        if (!createFolder(request.getFolderName())) {
            return null;
        }

        File saveFile = new File(request.getFolderName(), request.getFileName());

        try {
            RandomAccessFile randOut = new RandomAccessFile(saveFile, "rw");
            switch (request.getThreadType()) {
                case DownloadThreadType.NORMAL:
                    if(request.getDownloadFileSize() > 0) {
                        randOut.setLength(request.getDownloadFileSize());
                    }
                    break;
                case DownloadThreadType.DOUBLETHREAD:
                    if (request.getThreadPosition() == 2) {
                        randOut.setLength(request.getDownloadFileSize() - request.getDownloadFilePerSize());
                    } else {
                        randOut.setLength(request.getDownloadFilePerSize());
                    }
                    break;
                default:
                    if(request.getDownloadFileSize() > 0) {
                        randOut.setLength(request.getDownloadFileSize());
                    }
                    break;
            }
            randOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return saveFile;
    }

    private boolean createFolder(String fileFolder) {
        File folder = new File(fileFolder);
        if (!createAndCheckFolder(folder)) {
            return false;
        }

        if (!folder.isDirectory()) {
            if (!folder.delete()) {
                return false;
            }
        }

        if (!createAndCheckFolder(folder)) {
            return false;
        }

        return true;
    }

    private boolean createAndCheckFolder(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        } else {
            return true;
        }

        if (!folder.exists()) {
            return false;
        }
        return true;
    }

    private long getFileSize(Request<?> request) {
        if (request.getDownloadFileSize() > 0) {
            return 0;
        }

        long downloadFileSize = 0;
        try {
            downloadFileSize = connectAndGetFileSize(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return downloadFileSize;
    }

    private long connectAndGetFileSize(Request<?> request) throws Exception {
        URL mUrl = new URL(request.getUrl());
        HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Encoding", "identity");
        conn.setRequestProperty("Referer", request.getUrl());
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.connect();

        long lenght = 0;
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            lenght = conn.getContentLength();
        }
        conn.disconnect();

        return lenght;
    }
}
