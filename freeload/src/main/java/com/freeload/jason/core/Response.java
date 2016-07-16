package com.freeload.jason.core;

public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called a download response progress. */
        public void onProgressChange(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        public void onErrorResponse();
    }

    /** Returns a successful response containing the parsed result. */
    public static <T> Response<T> success(T result) {
        return new Response<T>(result);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> Response<T> error() {
        return new Response<T>();
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    /** True if this response was a soft-expired one and a second one MAY be coming. */
    public boolean intermediate = false;


    private Response(T result) {
        this.result = result;
    }

    private Response() {
        this.result = null;
    }
}
