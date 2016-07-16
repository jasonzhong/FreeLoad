package com.freeload.jason.core;

import android.os.Handler;

import java.util.concurrent.Executor;

public class ExecutorDelivery implements ResponseDelivery {

    /** Used for posting responses, typically to the main thread. */
    private final Executor mResponsePoster;

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public ExecutorDelivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void postResponse(Request<?> request) {
        postResponse(request, null);
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        postDownloadProgress(request, response, 0, 0);
    }

    @Override
    public void postDownloadProgress(Request<?> request, Response<?> response, long fileSize, long downloadedSize) {
        mResponsePoster.execute(new ResponseProgressDeliveryRunnable(request, response, fileSize, downloadedSize));
    }

    private class ResponseProgressDeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;
        private final long mFileSize;
        private final long mDownloadedSize;

        private ResponseProgressDeliveryRunnable(Request request, Response response, long mFileSize, long mDownloadedSize) {
            this.mRequest = request;
            this.mResponse = response;
            this.mFileSize = mFileSize;
            this.mDownloadedSize = mDownloadedSize;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            if (mRequest == null || mResponse == null) {
                return;
            }

            mRequest.deliverDownloadProgress(mResponse.result);
        }
    }
}
