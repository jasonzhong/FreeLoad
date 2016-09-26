package com.freeload.jason.core.download;

import com.freeload.jason.core.Request;
import com.freeload.jason.core.response.ResponseDelivery;

public interface INetwork {

    public boolean performRequest(Request<?> request, ResponseDelivery delivery);

    public boolean retryPerformRequest(Request<?> request, ResponseDelivery delivery, boolean finalDownload);
}
