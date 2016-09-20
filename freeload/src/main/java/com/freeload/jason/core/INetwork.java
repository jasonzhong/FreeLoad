package com.freeload.jason.core;

public interface INetwork {

    public void performRequest(Request<?> request);

    public boolean performRequest(Request<?> request, ResponseDelivery delivery);

    public boolean tryPerformRequest(Request<?> request, ResponseDelivery delivery);
}
