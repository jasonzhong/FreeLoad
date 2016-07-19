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
        if (mListReceipt.size() <= 0 || mListReceipt.size() < (pos - 1)) {
            return null;
        }
        return mListReceipt.get(pos - 1);
    }

    public void setDownloadReceipt(DownloadReceipt receipt) {
        if (null == receipt) {
            return;
        }

        long size = 0;
        long totalSize = 0;
        int pos = 0;
        for (; pos < mListReceipt.size(); ++pos) {
            DownloadReceipt localReceipt = mListReceipt.get(pos);
            if (localReceipt.getDownloadPosition() == receipt.getDownloadPosition()) {
                size = localReceipt.getDownloadedSize();
                totalSize = localReceipt.getDownloadTotalSize();

                if (receipt.getDownloadTotalSize() == 0) {
                    receipt.setDownloadedSize(size);
                    receipt.setDownloadTotalSize(totalSize);
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
            String downloadTotalSize = receiptSplitInfo[1].substring(receiptSplitInfo[1].indexOf(":") + 1, receiptSplitInfo[1].length());

            DownloadReceipt downloadReceipt = new DownloadReceipt();
            downloadReceipt.setDownloadedSize(Long.parseLong(downloadSize));
            downloadReceipt.setDownloadTotalSize(Long.parseLong(downloadTotalSize));
            mListReceipt.add(downloadReceipt);
        }
    }

    @Override
    public String toString() {
        String str = "";

        for (int i = 0; i < mListReceipt.size(); ++i) {
            str += "downloadSize" + mListReceipt.get(i).getDownloadPosition() + ":" + mListReceipt.get(i).getDownloadedSize() +
                    ",downloadTotalSize" + mListReceipt.get(i).getDownloadPosition() + ":" + mListReceipt.get(i).getDownloadTotalSize() +
                    ",downloadState:" + mListReceipt.get(i).getDownloadState() + ";";
        }
        return str;
    }

    @Override
    public String getReceipt() {
        return toString();
    }
}
