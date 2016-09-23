package com.freeload.jason.core;

import android.os.Process;

import com.freeload.jason.toolbox.DownloadReceipt;

import java.util.concurrent.BlockingQueue;

public class DownloadDispatcher extends Thread {

    private boolean finish = false;

    /** The queue of requests to service. */
    private final BlockingQueue<Request<?>> mQueue;

    /** The download interface for processing requests. */
    private final BasicDownload mDownload;

    /** The prepare interface for prepare download. */
    private final PrepareDownload mPrepare;

    /** For posting responses and errors. */
    private final ResponseDelivery mDelivery;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    public DownloadDispatcher(BlockingQueue<Request<?>> queue, BasicDownload basicDownload,
                              PrepareDownload prepareDownload, ResponseDelivery delivery) {
        this.mQueue = queue;
        this.mDownload = basicDownload;
        this.mPrepare = prepareDownload;
        this.mDelivery = delivery;
    }

    public boolean isFinish() {
        return finish;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            Request<?> request;
            try {
                request = mQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                }
                continue;
            }

            // If the request was cancelled already, do not perform the download request.
            if (request.isCanceled()) {
                request.finish();
                continue;
            }

            boolean prepare = mPrepare.preparePerform(request, mDelivery);
            if (!prepare) {
                continue;
            }

            boolean downloadRetry = false;
            boolean download = mDownload.performRequest(request, mDelivery);
            if (!download) {
                while (request.getDownloadRetryCount() < request.getDownloadRetryLimiteCount()) {
                    if (request.isCanceled()) {
                        request.finish();
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    request.addDownloadRetryCount();

                    boolean finalDownload = false;
                    if (request.getDownloadRetryCount() == request.getDownloadRetryLimiteCount()) {
                        finalDownload = true;
                    }

                    downloadRetry = mDownload.retryPerformRequest(request, mDelivery, finalDownload);
                    if (downloadRetry) {
                        break;
                    }
                }
            }

            if (!downloadRetry && !download) {
                postResponse(request, mDelivery, DownloadReceipt.STATE.CANCEL,
                        0, 0, 0, 0);
            }
            request.finish();
        }
    }

    private void postResponse(Request<?> request, ResponseDelivery delivery, DownloadReceipt.STATE state,
                              long downLoadFileSize, long downloadLength,
                              long writeFileSize, long writeFileLength) {
        if (delivery == null) {
            return;
        }
        DownloadReceipt downloadReceipt = getDownloadReceipt(request, state,
                downLoadFileSize, downloadLength, writeFileSize, writeFileLength);
        delivery.postDownloadProgress(request, Response.success(downloadReceipt));
    }

    private DownloadReceipt getDownloadReceipt(Request<?> request, DownloadReceipt.STATE state,
                                               long downLoadFileSize, long downloadLength,
                                               long writeFileSize, long writeFileLength) {
        DownloadReceipt downloadReceipt = new DownloadReceipt();
        downloadReceipt.setDownloadPosition(request.getThreadPosition());
        downloadReceipt.setDownloadState(state);
        downloadReceipt.setDownloadedSize(downLoadFileSize);
        downloadReceipt.setTotalDownloadSize(downloadLength);
        downloadReceipt.setWriteFileSize(writeFileSize);
        downloadReceipt.setWriteFileTotalSize(writeFileLength);
        return downloadReceipt;
    }

    /**
     * Forces this dispatcher to quit immediately.  If any requests are still in
     * the queue, they are not guaranteed to be processed.
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }
}
