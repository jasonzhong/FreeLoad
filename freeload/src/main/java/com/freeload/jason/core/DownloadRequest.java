package com.freeload.jason.core;

import android.text.TextUtils;

import com.freeload.jason.toolbox.DownloadReceipt;

public class DownloadRequest extends Request<DownloadReceipt> {

    private Response.Listener<DownloadReceipt> mListener;
    private Response.PepareListener<DownloadReceipt> mPepareListener;

    public static DownloadRequest create() {
        return new DownloadRequest();
    }

    public DownloadRequest setDownloadFileName(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            setFileName(fileName);
        }
        return this;
    }

    public DownloadRequest setDownloadFileSize(long fileSize) {
        if (fileSize != 0) {
            setFileSize(fileSize);
        }
        return this;
    }

    public DownloadRequest setListener(Response.Listener<DownloadReceipt> listener) {
        this.mListener = listener;
        return this;
    }

    public DownloadRequest setPepareListener(Response.PepareListener<DownloadReceipt> listener) {
        this.mPepareListener = listener;
        return this;
    }

    public DownloadRequest setDownloadId(int id) {
        setId(id);
        return this;
    }

    public DownloadRequest setDownloadUrl(String url) {
        setUrl(url);
        return this;
    }

    public DownloadRequest setDownloadedFileFolder(String downloadedFileFolder) {
        setFileFolder(downloadedFileFolder);
        return this;
    }

    public DownloadRequest setReceipt(DownloadReceipt downloadReceipt) {
        if (downloadReceipt != null) {
            setDownloadReceipt(downloadReceipt);
        }
        return this;
    }

    public void cancel() {
        super.cancel();
    }

    public DownloadRequest setThreadPositon(int position) {
        super.setThreadPosition(position);
        return this;
    }

    public DownloadRequest setDownloadThreadType(int type) {
        setThreadType(type);
        return this;
    }

    @Override
    public void deliverDownloadProgress(DownloadReceipt response) {
        if (this.mListener != null) {
            this.mListener.onProgressChange(response);
        }
    }

    @Override
    public void deliverDownloadPepare(DownloadReceipt response) {
        if (this.mPepareListener != null) {
            this.mPepareListener.onProgressPepare(response);
        }
    }
}
