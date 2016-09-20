package com.freeload.jason.toolbox;

import com.freeload.jason.core.DownloadRequest;
import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;
import com.freeload.jason.core.IReceipt;

import java.util.ArrayList;

public class DownloadManager {
    private int mThreadCount = 0;
    private int mSuccessCount = 0;

    private RequestQueue mRequestQueue = null;

    private EssentialInfo mEssentialInfo = null;

    private ArrayList<DownloadRequest> mDownloadRequestList = null;

    private EscapeReceipt escapeReceipt = new EscapeReceipt();

    public static DownloadManager create() {
        return new DownloadManager();
    }

    protected DownloadManager() {
        mEssentialInfo = new EssentialInfo();
        mDownloadRequestList = new ArrayList<DownloadRequest>();
    }

    public DownloadManager setListener(Response.Listener<IReceipt> listener) {
        this.mEssentialInfo.mListener = listener;
        return this;
    }

    public DownloadManager setPepareListener(Response.PepareListener<IReceipt> listener) {
        this.mEssentialInfo.mPepareListener = listener;
        return this;
    }

    public DownloadManager setDownloadId(int id) {
        this.mEssentialInfo.mId = id;
        return this;
    }

    public DownloadManager setDownloadUrl(String Url) {
        this.mEssentialInfo.mUrl = Url;
        return this;
    }

    public DownloadManager setDownloadedFileFolder(String downloadedFileFolder) {
        this.mEssentialInfo.mDownloadedFileFolder = downloadedFileFolder;
        return this;
    }

    public DownloadManager setFileName(String fileName) {
        this.mEssentialInfo.mFileName = fileName;
        return this;
    }

    public DownloadManager setFileSize(long fileSize) {
        this.mEssentialInfo.mFileSize = fileSize;
        return this;
    }

    public DownloadManager setEscapeReceipt(String receipt) {
        this.mEssentialInfo.mCustomerReceipt.setCustomerReceipt(receipt);
        return this;
    }

    public void cancel() {
        for (DownloadRequest downloadRequest : mDownloadRequestList) {
            downloadRequest.cancel();
        }
    }

    public DownloadManager addRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;

        int ThreadSize = 1;
        switch (this.mEssentialInfo.mThreadType) {
            case DownloadThreadType.NORMAL:
                ThreadSize = 1;
                break;
            case DownloadThreadType.DOUBLETHREAD:
                ThreadSize = 2;
                break;
            case DownloadThreadType.TRIPLETHREAD:
                ThreadSize = 3;
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
            mDownloadRequestList.add(createMulitDownloadRequest(position, this.mEssentialInfo.mThreadType));
        }
    }

    public DownloadManager setDownloadThreadType(int type) {
        this.mEssentialInfo.mThreadType = type;
        return this;
    }

    private DownloadRequest createMulitDownloadRequest(final int position, int threadType) {
        return DownloadRequest.create()
                .setDownloadId(this.mEssentialInfo.mId)
                .setDownloadUrl(this.mEssentialInfo.mUrl)
                .setDownloadedFileFolder(this.mEssentialInfo.mDownloadedFileFolder)
                .setThreadPositon(position)
                .setReceipt(mEssentialInfo.mCustomerReceipt.getDownloadReceipt(position))
                .setDownloadFileName(this.mEssentialInfo.mFileName)
                .setDownloadFileSize(this.mEssentialInfo.mFileSize)
                .setDownloadThreadType(threadType)
                .setPepareListener(new Response.PepareListener<DownloadReceipt>() {
                    @Override
                    public void onProgressPepare(DownloadReceipt response) {
                        EscapeReceipt escapeReceipt = new EscapeReceipt();
                        escapeReceipt.setDownloadReceipt(response, position - 1);
                        if (mEssentialInfo.mPepareListener != null) {
                            mEssentialInfo.mPepareListener.onProgressPepare(escapeReceipt);
                        }
                    }
                })
                .setListener(new Response.Listener<DownloadReceipt>() {
                    @Override
                    public void onProgressChange(DownloadReceipt response) {
                        if (response.getDownloadState() == DownloadReceipt.STATE.SUCCESS_DOWNLOAD) {
                            ++mSuccessCount;
                        }

                        escapeReceipt.setDownloadReceipt(response, position - 1);

                        if (mEssentialInfo.mListener != null) {
                            mEssentialInfo.mListener.onProgressChange(escapeReceipt);
                        }

                        if (mThreadCount == mSuccessCount) {
                            addEndingRequestQueue();
                        }
                    }
                });
    }

    private void addEndingRequestQueue() {
        mRequestQueue.addEnding(createFileRequest(mEssentialInfo.mThreadType));
    }

    private DownloadRequest createFileRequest(int threadType) {
        DownloadRequest downloadRequest = mDownloadRequestList.get(0);
        return DownloadRequest.create()
                .setDownloadedFileFolder(this.mEssentialInfo.mDownloadedFileFolder)
                .setDownloadFileName(downloadRequest.getFileName())
                .setDownloadThreadType(threadType)
                .setListener(new Response.Listener<DownloadReceipt>() {
                    @Override
                    public void onProgressChange(DownloadReceipt response) {
                        EscapeReceipt escapeReceipt = new EscapeReceipt();
                        escapeReceipt.setDownloadReceipt(response, 0);
                        mEssentialInfo.mListener.onProgressChange(escapeReceipt);
                    }
                });
    }
}
