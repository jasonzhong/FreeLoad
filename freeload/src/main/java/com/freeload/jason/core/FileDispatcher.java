package com.freeload.jason.core;

import android.os.Process;

import java.util.concurrent.BlockingQueue;

public class FileDispatcher  extends Thread {

    private boolean finish = false;

    /** Ending download perform */
    private final EndingDownload mEnding;

    /** The queue of requests to service. */
    private final BlockingQueue<Request<?>> mQueue;

    /** For posting responses and errors. */
    private final ResponseDelivery mDelivery;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    public FileDispatcher(BlockingQueue<Request<?>> queue, EndingDownload endingDownload,
                          ResponseDelivery delivery) {
        this.mQueue = queue;
        this.mEnding = endingDownload;
        this.mDelivery = delivery;
    }

    public boolean isFinish() {
        return finish;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
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

            mEnding.endingPerform(request, mDelivery);

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
