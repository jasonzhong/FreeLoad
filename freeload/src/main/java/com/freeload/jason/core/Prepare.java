package com.freeload.jason.core;

/**
 * Created by Jzcloud on 16/1/23.
 * Action prepare before download.
 */
public interface Prepare {
    public boolean preparePerform(Request<?> request, ResponseDelivery delivery);
}
