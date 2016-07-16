package com.freeload.jason.core;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.Ending;
import com.freeload.jason.core.Request;
import com.freeload.jason.core.Response;
import com.freeload.jason.core.ResponseDelivery;
import com.freeload.jason.toolbox.DownloadReceipt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EndingDownload implements Ending {

    @Override
    public boolean endingPerform(Request<?> request, ResponseDelivery delivery) {
        postResponse(delivery, request, DownloadReceipt.STATE.START_COMBIN_FILE);

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

        for (int n = 1; n <= size; ++n) {
            copyNewFile(request, saveFile, n);
        }

        postResponse(delivery, request, DownloadReceipt.STATE.SUCCESS_COMBIN_FILE);

        return true;
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

            fileSrc.close();
            fileCopy.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
