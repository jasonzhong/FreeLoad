package com.freeload.jason.core;

import android.text.TextUtils;

import com.freeload.jason.core.response.ResponseDelivery;
import com.freeload.jason.core.response.ResponseUtil;
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
    private final static int READ_TIMEOUT = 10 * 1000;

    @Override
    public boolean preparePerform(Request<?> request, ResponseDelivery delivery) {
        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.GETSIZE);
        long downloadFileSize = getFileSize(request);
        if (downloadFileSize <= 0) {
            ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_GETSIZE);
            return false;
        }
        ResponseUtil.postPepareResponse(request, delivery, DownloadReceipt.STATE.GETSIZE, downloadFileSize);
        request.setFileSize(downloadFileSize);

        int nPos = request.getThreadPosition();
        File file = new File(request.getFolderName(), request.getFileName() + "" + nPos);
        if (!file.exists()) {
            request.setDownloadReceipt(null);
        }

        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.QUEST_PREPARE);
        boolean bParse = parseStoragePages(request);
        if (!bParse) {
            ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_QUEST_PREPARE);
        }

        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.CREATEFILE);
        boolean bCreate = createFile(request);
        if (!bCreate) {
            ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.FAILED_CREATEFILE);
            return false;
        }
        ResponseUtil.postDownloadResponse(request, delivery, DownloadReceipt.STATE.PEPARE_FINISH);

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
                bRes = setSingleQuestDownloadSizeInfo(request, downloadFileSize);
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

    private boolean setSingleQuestDownloadSizeInfo(Request<?> request, long downloadFileSize) {
        String fileName = request.getFileName();
        fileName += "" + 1;
        request.setFileName(fileName);

        request.setDownloadStart(0);
        request.setWriteFileStart(0);

        request.setDownloadEnd(downloadFileSize);
        request.setWriteFileEnd(downloadFileSize);

        request.setDownloadFilePerSize(downloadFileSize);

        return true;
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
        long fileSize = 0;
        DownloadReceipt downloadReceipt = request.getDownloadReceipt();
        if (downloadReceipt != null) {
            fileSize = downloadReceipt.getDownloadTotalSize();
            if (fileSize > 0) {
                return fileSize;
            }
        }

        long downloadFileSize = 0;
        if (request.getDownloadFileSize() > 0) {
            fileSize = request.getDownloadFileSize();
            if (fileSize > 0) {
                return fileSize;
            }
        }

        //HTTP HEAD
        long contentLength = connectAndGetFileSizeByHead(request);
        if (contentLength > 0) {
            return contentLength;
        }

        downloadFileSize = connectAndGetFileSize(request);
        return downloadFileSize;
    }

    private long connectAndGetFileSize(Request<?> request) {
        long lenght = 0;

        HttpURLConnection conn = null;
        try {
            URL mUrl = new URL(request.getUrl());
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
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
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return lenght;
    }

    private long connectAndGetFileSizeByHead(Request<?> request) {
        long lenght = 0;

        HttpURLConnection conn = null;
        try {
            URL mUrl = new URL(request.getUrl());
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestMethod("HEAD");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                lenght = conn.getContentLength();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return lenght;
    }
}
