package com.freeload.jason.core;

import android.text.TextUtils;

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
        postPepareResponse(delivery, request, DownloadReceipt.STATE.GETSIZE, downloadFileSize);

        request.setFileSize(downloadFileSize);
        postResponse(delivery, request, DownloadReceipt.STATE.QUEST_PREPARE);
        boolean bParse = parseStoragePages(request);
        if (!bParse) {
            postResponse(delivery, request, DownloadReceipt.STATE.FAILED_QUEST_PREPARE);
        }

        postResponse(delivery, request, DownloadReceipt.STATE.CREATEFILE);
        boolean bCreate = createFile(request);
        if (!bCreate) {
            postResponse(delivery, request, DownloadReceipt.STATE.FAILED_CREATEFILE);
            return false;
        }
        postResponse(delivery, request, DownloadReceipt.STATE.PEPARE_FINISH);

        return true;
    }

    private boolean parseStoragePages(Request<?> request) {
        if (request == null) {
            return false;
        }

        DownloadReceipt downloadReceipt = request.getDownloadReceipt();

        boolean receiptResult = false;
        if (downloadReceipt != null) {
            receiptResult = parseStoragePagesFromReceipt(request, downloadReceipt);
        }

        if (receiptResult) {
            return true;
        }

        return parseStoragePagesFromNew(request, request.getDownloadFileSize());
    }

    private boolean parseStoragePagesFromReceipt(Request<?> request, DownloadReceipt downloadReceipt) {
        if (request == null || downloadReceipt == null) {
            return false;
        }

        // 设置文件名
        String fileName = request.getFileName() + downloadReceipt.getDownloadPosition();
        File file = new File(request.getFolderName() + File.separator + fileName);
        if (!file.exists()) {
            return false;
        }

        request.setFileName(fileName);

        long lDownloadSize = downloadReceipt.getDownloadedSize();
        request.setDownloadStart(lDownloadSize);
        long lWriteFileSize = downloadReceipt.getWriteFileSize();
        request.setWriteFileStart(lWriteFileSize);

        long lDownloadTotalSize = downloadReceipt.getDownloadTotalSize();
        request.setDownloadEnd(lDownloadTotalSize);
        long lWriteFileTotalSize = downloadReceipt.getWriteFileTotalSize();
        request.setWriteFileEnd(lWriteFileTotalSize);

        return true;
    }

    private boolean parseStoragePagesFromNew(Request<?> request, long downloadFileSize) {
        boolean bRes = true;
        int threadType = request.getThreadType();
        switch (threadType) {
            case DownloadThreadType.NORMAL:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 1);
                break;
            case DownloadThreadType.DOUBLETHREAD:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 2);
                break;
            case DownloadThreadType.TRIPLETHREAD:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 3);
                break;
            default:
                bRes = setQuestDownloadSizeInfo(request, downloadFileSize, 1);
                break;
        }
        return bRes;
    }

    private boolean setQuestDownloadSizeInfo(Request<?> request, long downloadFileSize, int division) {
        long sizePart = downloadFileSize / 1024;
        long halfSize = sizePart / division * 1024;

        long perSize = downloadFileSize - halfSize;
        int nPos = request.getThreadPosition();

        String fileName = request.getFileName();
        fileName += "" + nPos;
        request.setFileName(fileName);

        int pos = nPos - 1;
        pos = ( pos >= 0 ? pos : 0 );

        request.setDownloadStart(perSize * pos);
        request.setWriteFileStart(0);

        if (division == 1 || nPos == division) {
            request.setDownloadEnd(downloadFileSize);
            request.setWriteFileEnd(downloadFileSize - (perSize * pos));
        } else {
            request.setDownloadEnd(perSize * nPos -1);
            request.setWriteFileEnd(perSize * nPos -1);
        }

        if (division == 1) {
            request.setDownloadFilePerSize(downloadFileSize);
        } else {
            request.setDownloadFilePerSize(perSize * (division - 1));
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

        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    private void postPepareResponse(ResponseDelivery delivery, Request<?> request, DownloadReceipt.STATE state, long fileSize) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadState(state);
        downloadReceipt.setDownloadedSize(fileSize);

        delivery.postDownloadPepare(request, Response.success(downloadReceipt));
    }

    private boolean createFile(Request<?> request) {
        if (request == null) {
            return false;
        }

        if (!createFolder(request.getFolderName())) {
            return false;
        }

        DownloadReceipt downloadReceipt = request.getDownloadReceipt();
        if (downloadReceipt != null) {
            return createFileFromReceipt(request, downloadReceipt);
        }

        File saveFile = createFileFromNew(request);
        if (saveFile == null) {
            return false;
        }
        request.setDownloadFile(saveFile);

        return true;
    }

    private boolean createFileFromReceipt(Request<?> request, DownloadReceipt downloadReceipt) {
        if (request == null || downloadReceipt == null) {
            return false;
        }

        File saveFile = new File(request.getFolderName(), request.getFileName());
        request.setDownloadFile(saveFile);

        return true;
    }

    private File createFileFromNew(Request<?> request) {
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
                case DownloadThreadType.TRIPLETHREAD:
                    if (request.getThreadPosition() == 3) {
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
        if (TextUtils.isEmpty(fileFolder)) {
            return false;
        }

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
        DownloadReceipt downloadReceipt = request.getDownloadReceipt();
        if (downloadReceipt != null) {
            return downloadReceipt.getDownloadTotalSize();
        }

        long downloadFileSize = 0;
        if (request.getDownloadFileSize() > 0) {
            return request.getDownloadFileSize();
        }

        try {
            downloadFileSize = connectAndGetFileSize(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return downloadFileSize;
    }

    private long connectAndGetFileSize(Request<?> request) throws Exception {
        long lenght = 0;

        HttpURLConnection conn = null;
        try {
            URL mUrl = new URL(request.getUrl());
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("Referer", request.getUrl());
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                lenght = conn.getContentLength();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return lenght;
    }
}
