package com.freeload.jason.core;

import android.os.Process;

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

            boolean download = mDownload.performRequest(request, mDelivery);
            if (!download) {
                for (int count = 0; count < request.getDownloadRetryTime(); ++count) {
                    if (request.isCanceled()) {
                        request.finish();
                        break;
                    }

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    boolean finalDownload = false;
                    if (count == 2) {
                        finalDownload = true;
                    }
                    boolean downloadRetry = mDownload.retryPerformRequest(request, mDelivery, finalDownload);
                    if (downloadRetry) {
                        break;
                    }
                }
            }

            request.finish();
        }
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
