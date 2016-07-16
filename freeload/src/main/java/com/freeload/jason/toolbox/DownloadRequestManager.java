package com.freeload.jason.toolbox;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;

import java.util.ArrayList;

public class DownloadRequestManager {
    /** Unique id for download. */
    private final int mId;

    /** URL of this request. */
    private final String mUrl;

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

    private Response.Listener<EscapeReceipt> mListener;
    private ArrayList<DownloadRequest> mDownloadRequestList;

    public static DownloadRequestManager create(int id, String Url) {
        return new DownloadRequestManager(id, Url);
    }

    protected DownloadRequestManager(int id, String Url) {
        this.mId = id;
        this.mUrl = Url;

        mEscapeReceipt = new EscapeReceipt();
        mCustomerReceipt = new EscapeReceipt();
        mDownloadRequestList = new ArrayList<DownloadRequest>();
    }

    public DownloadRequestManager setListener(Response.Listener<EscapeReceipt> listener) {
        this.mListener = listener;
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
                addRequestQueue(1);
                ThreadSize = 1;
                break;
            case DownloadThreadType.DOUBLETHREAD:
                addRequestQueue(2);
                ThreadSize = 2;
                break;
            default:
                ThreadSize = 1;
                addRequestQueue(1);
                break;
        }

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
        return DownloadRequest.create(this.mId, this.mUrl)
                .setThreadPositon(position)
                .setReceipt(mCustomerReceipt.getDownloadReceipt(position))
                .setDownloadFileName(mFileName)
                .setDownloadThreadType(threadType)
                .setListener(new Response.Listener<DownloadReceipt>() {
                    @Override
                    public void onProgressChange(DownloadReceipt response) {
                        if (response.getDownloadState() == DownloadReceipt.STATE.SUCCESS_DOWNLOAD) {
                            ++mSuccessCount;
                        }
                        if (mThreadCount == mSuccessCount) {
                            addEndingRequestQueue();
                        }
                        mEscapeReceipt.setDownloadReceipt(response);
                        mListener.onProgressChange(mEscapeReceipt);
                    }
                });
    }

    private void addEndingRequestQueue() {
        mRequestQueue.addEnding(createFileRequest(mThreadType));
    }

    private DownloadRequest createFileRequest(int threadType) {
        DownloadRequest downloadRequest = mDownloadRequestList.get(0);
        return DownloadRequest.create(0, "")
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
