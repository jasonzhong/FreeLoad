package com.freeload.jason.core;

import android.os.Handler;

import com.freeload.jason.core.response.Response;
import com.freeload.jason.core.response.ResponseDelivery;

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
    public void postDownloadProgress(Request<?> request, Response<?> response) {
        mResponsePoster.execute(new ResponseProgressDeliveryRunnable(request, response));
    }

    @Override
    public void postDownloadPepare(Request<?> request, Response<?> response) {
        mResponsePoster.execute(new ResponsePepareDeliveryRunnable(request, response));
    }

    private class ResponseProgressDeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;

        private ResponseProgressDeliveryRunnable(Request request, Response response) {
            this.mRequest = request;
            this.mResponse = response;
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

    private class ResponsePepareDeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;

        private ResponsePepareDeliveryRunnable(Request request, Response response) {
            this.mRequest = request;
            this.mResponse = response;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            if (mRequest == null || mResponse == null) {
                return;
            }

            mRequest.deliverDownloadPepare(mResponse.result);
        }
    }
}
