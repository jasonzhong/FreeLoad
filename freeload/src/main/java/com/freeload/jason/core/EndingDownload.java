package com.freeload.jason.core;

import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EndingDownload implements IEnding {

    @Override
    public boolean endingPerform(Request<?> request, ResponseDelivery delivery) {
        postResponse(delivery, request, DownloadReceipt.STATE.START_COMBIN_FILE, null);

        int size = 0;
        switch (request.getThreadType()) {
            case DownloadThreadType.NORMAL:
                size = 1;
                break;
            case DownloadThreadType.DOUBLETHREAD:
                size = 2;
                break;
            default:
                size = 1;
                break;
        }

        String fileName = request.getFileName();
        int last = fileName.indexOf(".tmp");
        fileName = fileName.substring(0, last);
        File saveFile = new File(request.getFolderName(), fileName);
        deleteCurrentSameFile(saveFile);

        for (int n = 1; n <= size; ++n) {
            copyNewFile(request, saveFile, n);
            deleteTempFile(request, saveFile, n);
        }

        if (!saveFile.exists()) {
            postResponse(delivery, request, DownloadReceipt.STATE.FAILED_COMBIN_FILE, saveFile.getPath());
            return false;
        }

        postResponse(delivery, request, DownloadReceipt.STATE.SUCCESS_COMBIN_FILE, saveFile.getPath());
        return true;
    }

    private void deleteCurrentSameFile(File file) {
        if (file == null) {
            return;
        }

        if (file.exists() && file.isFile()) {
            boolean delete = file.delete();
            if (!delete) {
                file.getAbsoluteFile().delete();
            }
        }
    }

    private void deleteTempFile(Request<?> request, File saveFile, int index) {
        File file = new File(request.getFolderName() + "/" + saveFile.getName() + ".tmp" + index);
        if (file.exists() && file.isFile()) {
            boolean delete = file.delete();
            if (!delete) {
                file.getAbsoluteFile().delete();
            }
        }
    }

    private void copyNewFile(Request<?> request, File saveFile, int pos) {
        RandomAccessFile fileSrc = null;
        RandomAccessFile fileCopy = null;

        try {
            fileCopy = new RandomAccessFile(saveFile, "rwd");
            fileSrc = new RandomAccessFile(request.getFolderName() + "/" + saveFile.getName() + ".tmp" + pos, "rwd");
            fileSrc.seek(0);

            int len = (int) fileSrc.length();
            byte[] b = new byte[len];
            fileSrc.readFully(b);
            fileCopy.seek(fileCopy.length());
            fileCopy.write(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileCopy != null) {
                    fileCopy.close();
                }

                if (fileSrc != null) {
                    fileSrc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void postResponse(ResponseDelivery delivery, Request<?> request, DownloadReceipt.STATE state, String filePath) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadFilePath(filePath);
        downloadReceipt.setDownloadState(state);

        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }
}
