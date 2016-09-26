package com.freeload.jason.core.response;

public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called a download response progress. */
        public void onProgressChange(T response);
    }

    /** Callback interface for delivering parsed pepare responses. */
    public interface PepareListener<T> {
        /** Called a download response pepare. */
        public void onProgressPepare(T response);
    }

    /** Returns a successful response containing the parsed result. */
    public static <T> Response<T> success(T result) {
        return new Response<T>(result);
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    private Response(T result) {
        this.result = result;
    }
}
