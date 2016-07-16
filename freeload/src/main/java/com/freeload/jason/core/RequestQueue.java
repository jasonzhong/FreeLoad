package com.freeload.jason.core;

import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestQueue {

    /** The network dispatchers. */
    private DownloadDispatcher[] mDownloadDispatchers;

    /** The file dispatchers. */
    private FileDispatcher[] mFileDispatchers;

    /** Number of download request dispatcher threads to start. */
    private static final int DEFAULT_DOWNLOAD_THREAD_POOL_SIZE = 4;

    /** Number of file request dispatcher threads to start. */
    private static final int DEFAULT_FILE_THREAD_POOL_SIZE = 2;

    /** The queue of requests that are actually going out to the network. */
    private final PriorityBlockingQueue<Request<?>> mDownloadQueue =
            new PriorityBlockingQueue<Request<?>>();

    /** The queue of requests that are actually going out to the network. */
    private final PriorityBlockingQueue<Request<?>> mFileQueue =
            new PriorityBlockingQueue<Request<?>>();

    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    /** Download interface for performing requests. */
    private final BasicDownload mDownload;

    /** The prepare interface for prepare download. */
    private final PrepareDownload mPrepare;

    /** Response delivery mechanism. */
    private final ResponseDelivery mDelivery;

    /** Ending download perform */
    private final EndingDownload mEnding;

    public RequestQueue(BasicDownload basicDownload, PrepareDownload prepareDownload, EndingDownload endingDownload) {
        this(basicDownload, prepareDownload, endingDownload, DEFAULT_DOWNLOAD_THREAD_POOL_SIZE, DEFAULT_FILE_THREAD_POOL_SIZE);
    }

    public RequestQueue(BasicDownload basicDownload, PrepareDownload prepareDownload, EndingDownload endingDownload, int threadPoolSize, int filePoolSize) {
        this(basicDownload, prepareDownload, endingDownload, threadPoolSize, filePoolSize, new ExecutorDelivery(new Handler(Looper.getMainLooper())));
    }

    public RequestQueue(BasicDownload basicDownload, PrepareDownload prepareDownload, EndingDownload endingDownload, int threadPoolSize, int filePoolSize, ResponseDelivery delivery) {
        this.mDownload = basicDownload;
        this.mPrepare = prepareDownload;
        this.mEnding = endingDownload;
        this.mDownloadDispatchers = new DownloadDispatcher[threadPoolSize];
        this.mFileDispatchers = new FileDispatcher[filePoolSize];
        this.mDelivery = delivery;
    }

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
    private final Set<Request<?>> mCurrentRequests = new HashSet<Request<?>>();

    /**
     * Starts the dispatchers in this queue.
     */
    public void start() {
        stop();

        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < mDownloadDispatchers.length; ++i) {
            DownloadDispatcher downloadDispatcher =
                    new DownloadDispatcher(mDownloadQueue, mDownload,
                            mPrepare, mDelivery);
            mDownloadDispatchers[i] = downloadDispatcher;
            downloadDispatcher.start();
        }

        // Create file dispatchers up to the pool size.
        for (int n = 0; n < mFileDispatchers.length; ++n) {
            FileDispatcher fileDispatcher =
                    new FileDispatcher(mFileQueue, mEnding, mDelivery);
            mFileDispatchers[n] = fileDispatcher;
            fileDispatcher.start();
        }
    }

    /**
     * Stops download dispatchers.
     */
    public void stop() {
        for (int i = 0; i < mDownloadDispatchers.length; i++) {
            if (mDownloadDispatchers[i] != null) {
                mDownloadDispatchers[i].quit();
            }
        }
    }

    /**
     * Adds a Request to the dispatch queue.
     * @param request The request to service
     * @return The passed-in request
     */
    public <T> Request<T> addOpening(Request<T> request) {
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }

        request.setSequence(getSequenceNumber());

        mDownloadQueue.add(request);

        return request;
    }

    /**
     * Adds a Request to the dispatch queue.
     * @param request The request to service
     * @return The passed-in request
     */
    public <T> Request<T> addEnding(Request<T> request) {
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }

        request.setSequence(getSequenceNumber());

        mFileQueue.add(request);

        return request;
    }

    /**
     * Gets a sequence number.
     */
    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    /**
     * Called from {@link Request#finish()}, indicating that processing of the given request
     * has finished.
     */
    <T> void finish(Request<T> request) {
        // Remove from the set of requests currently being processed.
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }
    }
}
