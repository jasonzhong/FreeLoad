package com.freeload.jason.core;

public interface INetwork {

    public void performRequest(Request<?> request);

    public void performRequest(Request<?> request, ResponseDelivery delivery);
}
