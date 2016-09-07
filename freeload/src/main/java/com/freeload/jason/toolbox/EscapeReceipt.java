package com.freeload.jason.toolbox;

import com.freeload.jason.core.IReceipt;

import java.io.Serializable;
import java.lang.Override;
import java.util.ArrayList;

public class EscapeReceipt implements Serializable, IReceipt {

    private ArrayList<DownloadReceipt> mListReceipt;

    public EscapeReceipt() {
        mListReceipt = new ArrayList<DownloadReceipt>();
    }

    public DownloadReceipt getDownloadReceipt(int pos) {
        if (mListReceipt.size() <= 0 || mListReceipt.size() < pos) {
            return null;
        }

        for (int n = 0; n < mListReceipt.size(); ++n) {
            DownloadReceipt downloadReceipt = mListReceipt.get(n);
            if (downloadReceipt.getDownloadPosition() == pos) {
                return downloadReceipt;
            }
        }
        return null;
    }

    public void setDownloadReceipt(DownloadReceipt receipt) {
        if (null == receipt) {
            return;
        }

        long size = 0;
        long totalSize = 0;

        long fileSize = 0;
        long fileTotalSize = 0;

        String path = "";
        int pos = 0;
        for (; pos < mListReceipt.size(); ++pos) {
            DownloadReceipt localReceipt = mListReceipt.get(pos);
            if (localReceipt.getDownloadPosition() == receipt.getDownloadPosition()) {
                size = localReceipt.getDownloadedSize();
                totalSize = localReceipt.getDownloadTotalSize();
                path = localReceipt.getDownloadFilePath();

                fileSize = localReceipt.getWriteFileSize();
                fileTotalSize = localReceipt.getDownloadTotalSize();

                if (receipt.getDownloadTotalSize() == 0) {
                    receipt.setDownloadedSize(size);
                    receipt.setTotalDownloadSize(totalSize);
                    receipt.setWriteFileSize(fileSize);
                    receipt.setWriteFileTotalSize(fileTotalSize);
                    receipt.setDownloadFilePath(path);
                }
                mListReceipt.remove(pos);
                mListReceipt.add(receipt);
                return;
            }
        }
        mListReceipt.add(receipt);
    }

    public void setCustomerReceipt(String receipt) {
        if ("".equals(receipt)) {
            return;
        }

        parseReceipt(receipt);
    }

    private void parseReceipt(String receipt) {
        String[] receiptSplitThread = receipt.split(";");
        for (String receiptInfo : receiptSplitThread) {
            String[] receiptSplitInfo = receiptInfo.split(",");

            if (receiptSplitInfo.length < 3) {
                continue;
            }

            String downloadSize = receiptSplitInfo[0].substring(receiptSplitInfo[0].indexOf(":") + 1, receiptSplitInfo[0].length());
            String downloadPosition = receiptSplitInfo[0].substring(receiptSplitInfo[0].indexOf("[") + 1, receiptSplitInfo[0].indexOf("]"));
            String downloadTotalSize = receiptSplitInfo[1].substring(receiptSplitInfo[1].indexOf(":") + 1, receiptSplitInfo[1].length());
            String writeFileSize = receiptSplitInfo[2].substring(receiptSplitInfo[2].indexOf(":") + 1, receiptSplitInfo[2].length());
            String writeFileTotalSize = receiptSplitInfo[3].substring(receiptSplitInfo[3].indexOf(":") + 1, receiptSplitInfo[3].length());
            String downloadState = receiptSplitInfo[4].substring(receiptSplitInfo[4].indexOf(":") + 1, receiptSplitInfo[4].length());

            DownloadReceipt downloadReceipt = new DownloadReceipt();
            downloadReceipt.setDownloadedSize(Long.parseLong(downloadSize));
            downloadReceipt.setDownloadPosition(Integer.parseInt(downloadPosition));
            downloadReceipt.setTotalDownloadSize(Long.parseLong(downloadTotalSize));
            downloadReceipt.setWriteFileSize(Long.parseLong(writeFileSize));
            downloadReceipt.setWriteFileTotalSize(Long.parseLong(writeFileTotalSize));
            downloadReceipt.setStartDownloadPosition(Long.parseLong(downloadPosition));
            parseDownloadState(downloadState, downloadReceipt);

            mListReceipt.add(downloadReceipt);
        }
    }

    private void parseDownloadState(String downloadState, DownloadReceipt downloadReceipt) {
//                NONE,                   // 0
//                GETSIZE,                // 1
//                FAILED_GETSIZE,         // 2
//                CREATEFILE,             // 3
//                FAILED_CREATEFILE,      // 4
//                QUEST_PREPARE,          // 5
//                FAILED_QUEST_PREPARE,   // 6
//                START,                  // 7
//                DOWNLOAD,               // 8
//                CANCEL,                 // 9
//                SUCCESS_DOWNLOAD,       // 10
//                START_COMBIN_FILE,      // 11
//                SUCCESS_COMBIN_FILE,    // 12
//                FAILED,                 // 13
//                TIMEOUT,                // 14
//                CONNWRONG               // 15

        if (downloadState.toUpperCase().equals("NONE")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.NONE);
        } else if (downloadState.toUpperCase().equals("GETSIZE")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.GETSIZE);
        } else if (downloadState.toUpperCase().equals("START")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.START);
        } else if (downloadState.toUpperCase().equals("DOWNLOAD")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.DOWNLOAD);
        } else if (downloadState.toUpperCase().equals("SUCCESS_DOWNLOAD")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.SUCCESS_DOWNLOAD);
        } else if (downloadState.toUpperCase().equals("CANCEL")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.CANCEL);
        } else if (downloadState.toUpperCase().equals("START_COMBIN_FILE")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.START_COMBIN_FILE);
        } else if (downloadState.toUpperCase().equals("SUCCESS_COMBIN_FILE")) {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.SUCCESS_COMBIN_FILE);
        } else {
            downloadReceipt.setDownloadState(DownloadReceipt.STATE.NONE);
        }
    }

    @Override
    public String toString() {
        String str = "";

        for (int i = 0; i < mListReceipt.size(); ++i) {
            str += "[" + mListReceipt.get(i).getDownloadPosition() + "]" +
                    "downloadSize:" + mListReceipt.get(i).getDownloadedSize() +
                    ",downloadTotalSize:" + mListReceipt.get(i).getDownloadTotalSize() +
                    ",mWriteFileSize:" + mListReceipt.get(i).getWriteFileSize() +
                    ",mWriteFileTotalSize:" + mListReceipt.get(i).getWriteFileTotalSize() +
                    ",downloadState:" + mListReceipt.get(i).getDownloadState() +
                    ",path:" + mListReceipt.get(i).getDownloadFilePath() + ";";
        }
        return str;
    }

    @Override
    public String getReceipt() {
        return toString();
    }

    @Override
    public DownloadReceipt.STATE getReceiptState() {
        if (mListReceipt.size() <= 0) {
            return DownloadReceipt.STATE.NONE;
        }
        return mListReceipt.get(0).getDownloadState();
    }

    @Override
    public long getDownloadedSize() {
        if (mListReceipt.size() <= 0) {
            return 0;
        }
        return mListReceipt.get(0).getDownloadedSize();
    }

    @Override
    public long getDownloadTotalSize() {
        if (mListReceipt.size() <= 0) {
            return 0;
        }
        return mListReceipt.get(0).getDownloadTotalSize();
    }

    @Override
    public String getDownloadFilePath() {
        if (mListReceipt.size() <= 0) {
            return "";
        }
        return mListReceipt.get(0).getDownloadFilePath();
    }
}
