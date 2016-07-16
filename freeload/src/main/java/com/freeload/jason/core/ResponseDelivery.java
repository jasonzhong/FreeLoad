package com.freeload.jason.core;

public interface ResponseDelivery {

    /** Parses a response from the network or cache and delivers it.*/
    public void postResponse(Request<?> request);

    /** Parses a response from the network or cache and delivers it.*/
    public void postResponse(Request<?> request, Response<?> response);

    /**
     *  Posts file download progress stat. And parses a response from the network or cache and delivers it.
     *  @hide
     */
    public void postDownloadProgress(Request<?> request, Response<?> response, long fileSize, long downloadedSize);
}
