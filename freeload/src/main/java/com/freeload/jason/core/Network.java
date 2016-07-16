package com.freeload.jason.core;

public interface Network {

    public void performRequest(Request<?> request);

    public void performRequest(Request<?> request, ResponseDelivery delivery);
}
