package com.freeload.jason.toolbox;

import com.freeload.jason.core.Request;
import com.freeload.jason.core.Response;

public class DownloadRequest extends Request<DownloadReceipt> {

    private Response.Listener<DownloadReceipt> mListener;

    public static DownloadRequest create(int id, String Url) {
        return create(id, Url, null);
    }

    public static DownloadRequest create(int id, String Url, String fileFolder) {
        return new DownloadRequest(id, Url, fileFolder);
    }

    protected DownloadRequest(int id, String Url, String fileFolder) {
        super(id, Url, fileFolder);
    }

    public DownloadRequest setDownloadFileName(String fileName) {
        if (!"".equals(fileName)) {
            setFileName(fileName);
        }
        return this;
    }

    public DownloadRequest setListener(Response.Listener<DownloadReceipt> listener) {
        this.mListener = listener;
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
}
