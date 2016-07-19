package com.freeload.jason.toolbox;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;
import com.freeload.jason.core.IReceipt;

import java.util.ArrayList;

public class DownloadRequestManager {
    /** Unique id for download. */
    private int mId;

    /** URL of this request. */
    private String mUrl;

    /** download file receipt */
    private EscapeReceipt mCustomerReceipt = null;

    /** download file receipt */
    private EscapeReceipt mEscapeReceipt = null;

    /** download file thread setting */
    private int mThreadType = DownloadThreadType.NORMAL;

    private String mFileName = "";

    private int mThreadCount = 0;
    private int mSuccessCount = 0;
    private RequestQueue mRequestQueue = null;

    private Response.Listener<IReceipt> mListener = null;
    private ArrayList<DownloadRequest> mDownloadRequestList = null;

    public static DownloadRequestManager create() {
        return new DownloadRequestManager();
    }

    protected DownloadRequestManager() {
        mEscapeReceipt = new EscapeReceipt();
        mCustomerReceipt = new EscapeReceipt();
        mDownloadRequestList = new ArrayList<DownloadRequest>();
    }

    public DownloadRequestManager setListener(Response.Listener<IReceipt> listener) {
        this.mListener = listener;
        return this;
    }

    public DownloadRequestManager setDownloadId(int id) {
        this.mId = id;
        return this;
    }

    public DownloadRequestManager setDownloadUrl(String Url) {
        this.mUrl = Url;
        return this;
    }

    public DownloadRequestManager setFileName(String fileName) {
        this.mFileName = fileName;
        return this;
    }

    public DownloadRequestManager setEscapeReceipt(String receipt) {
        this.mCustomerReceipt.setCustomerReceipt(receipt);
        return this;
    }

    public void cancel() {
        for (DownloadRequest downloadRequest : mDownloadRequestList) {
            downloadRequest.cancel();
        }
    }

    public DownloadRequestManager addRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;

        int ThreadSize = 1;
        switch (this.mThreadType) {
            case DownloadThreadType.NORMAL:
                ThreadSize = 1;
                break;
            case DownloadThreadType.DOUBLETHREAD:
                ThreadSize = 2;
                break;
            default:
                ThreadSize = 1;
                break;
        }
        addRequestQueue(ThreadSize);

        for (DownloadRequest downloadRequest : mDownloadRequestList) {
            mRequestQueue.addOpening(downloadRequest);
        }

        return this;
    }

    private void addRequestQueue(int pos) {
        mThreadCount = pos;
        for (int position = 1; position <= pos; ++position) {
            mDownloadRequestList.add(createMulitDownloadRequest(position, this.mThreadType));
        }
    }

    public DownloadRequestManager setDownloadThreadType(int type) {
        this.mThreadType = type;
        return this;
    }

    private DownloadRequest createMulitDownloadRequest(int position, int threadType) {
        return DownloadRequest.create()
                .setDownloadId(this.mId)
                .setDownloadUrl(this.mUrl)
                .setThreadPositon(position)
                .setReceipt(mCustomerReceipt.getDownloadReceipt(position))
                .setDownloadFileName(this.mFileName)
                .setDownloadThreadType(threadType)
                .setListener(new Response.Listener<DownloadReceipt>() {
                    @Override
                    public void onProgressChange(DownloadReceipt response) {
                        if (response.getDownloadState() == DownloadReceipt.STATE.SUCCESS_DOWNLOAD) {
                            ++mSuccessCount;
                        }
                        mEscapeReceipt.setDownloadReceipt(response);
                        mListener.onProgressChange((IReceipt) mEscapeReceipt);

                        if (mThreadCount == mSuccessCount) {
                            addEndingRequestQueue();
                        }
                    }
                });
    }

    private void addEndingRequestQueue() {
        mRequestQueue.addEnding(createFileRequest(mThreadType));
    }

    private DownloadRequest createFileRequest(int threadType) {
        DownloadRequest downloadRequest = mDownloadRequestList.get(0);
        return DownloadRequest.create()
                .setDownloadFileName(downloadRequest.getFileName())
                .setThreadPositon(threadType + 2)
                .setDownloadThreadType(threadType)
                .setListener(new Response.Listener<DownloadReceipt>() {
                    @Override
                    public void onProgressChange(DownloadReceipt response) {
                        mEscapeReceipt.setDownloadReceipt(response);
                        mListener.onProgressChange(mEscapeReceipt);
                    }
                });
    }
}
