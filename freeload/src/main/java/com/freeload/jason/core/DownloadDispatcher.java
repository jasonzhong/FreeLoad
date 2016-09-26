package com.freeload.jason.core;

import android.os.Process;

import com.freeload.jason.core.download.BasicDownload;
import com.freeload.jason.core.download.INetwork;
import com.freeload.jason.core.download.MulitDownload;
import com.freeload.jason.core.download.SingleDownload;
import com.freeload.jason.core.response.ResponseDelivery;

import java.util.concurrent.BlockingQueue;

public class DownloadDispatcher extends Thread {

    private boolean finish = false;

    /** The queue of requests to service. */
    private final BlockingQueue<Request<?>> mQueue;

    /** The prepare interface for prepare download. */
    private final PrepareDownload mPrepare;

    /** For posting responses and errors. */
    private final ResponseDelivery mDelivery;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    public DownloadDispatcher(BlockingQueue<Request<?>> queue, PrepareDownload prepareDownload,
                              ResponseDelivery delivery) {
        this.mQueue = queue;
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

            INetwork network = null;
            switch (request.getThreadType()) {
                case DownloadThreadType.NORMAL:
                    network = new SingleDownload();
                    break;
                case DownloadThreadType.DOUBLETHREAD:
                    network = new MulitDownload();
                default:
                    network = new SingleDownload();
                    break;
            }

            network.performRequest(request, mDelivery);
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
