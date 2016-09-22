package com.freeload.jason.core;

public interface INetwork {

    public void performRequest(Request<?> request);

    public boolean performRequest(Request<?> request, ResponseDelivery delivery);

    public boolean retryPerformRequest(Request<?> request, ResponseDelivery delivery, boolean finalDownload);
}
