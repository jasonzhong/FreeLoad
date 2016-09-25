package com.freeload.jason.core;

import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EndingDownload implements IEnding {

    @Override
    public boolean endingPerform(Request<?> request, ResponseDelivery delivery) {
        ResponseUtil.postFileResponse(request, delivery, DownloadReceipt.STATE.START_COMBIN_FILE, null);

        int size = 0;
        switch (request.getThreadType()) {
            case DownloadThreadType.NORMAL:
                size = 1;
                break;
            case DownloadThreadType.DOUBLETHREAD:
                size = 2;
                break;
            case DownloadThreadType.TRIPLETHREAD:
                size = 3;
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
            ResponseUtil.postFileResponse(request, delivery, DownloadReceipt.STATE.FAILED_COMBIN_FILE, saveFile.getPath());
            return false;
        }

        ResponseUtil.postFileResponse(request, delivery, DownloadReceipt.STATE.SUCCESS_COMBIN_FILE, saveFile.getPath());
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

    private boolean copyNewFile(Request<?> request, File saveFile, int pos) {
        if (saveFile == null) {
            return false;
        }

        boolean result = false;

        File oldfile = new File(request.getFolderName() + "/" + saveFile.getName() + ".tmp" + pos);

        InputStream fInStream = null;
        FileOutputStream fOutStream = null;
        try {
            fInStream = new FileInputStream(oldfile);
            fOutStream = new FileOutputStream(saveFile, true);
            if (!oldfile.exists()) {
                return false;
            }

            int byteread = 0;
            byte[] buffer = new byte[2048];
            while ((byteread = fInStream.read(buffer)) != -1) {
                fOutStream.write(buffer, 0, byteread);
            }

            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fInStream != null) {
                    fInStream.close();
                    fInStream = null;
                }

                if (fOutStream != null) {
                    fOutStream.close();
                    fOutStream = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
