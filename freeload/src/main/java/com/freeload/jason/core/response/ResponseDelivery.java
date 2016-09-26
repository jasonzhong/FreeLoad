package com.freeload.jason.core.response;

import com.freeload.jason.core.Request;

public interface ResponseDelivery {
    /**
     *  Posts file download progress stat. And parses a response from the network or cache and delivers it.
     *  @hide
     */
    public void postDownloadProgress(Request<?> request, Response<?> response);

    /**
     *  Posts file pepare stat. And parses a response from the network or cache and delivers it.
     *  @hide
     */
    public void postDownloadPepare(Request<?> request, Response<?> response);
}
