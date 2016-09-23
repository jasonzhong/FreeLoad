package com.freeload.jason.core;

import com.freeload.jason.toolbox.DownloadReceipt;

public interface IReceipt {

    public String getReceipt();

    public DownloadReceipt.STATE getReceiptState();

    public long getDownloadedSize();

    public long getDownloadedFileSize();

    public long getDownloadedSize(int pos);

    public long getDownloadTotalSize();

    public String getDownloadFilePath();
}