package com.freeload.jason.core;

import com.freeload.jason.core.response.ResponseDelivery;

public interface IEnding {
    public boolean endingPerform(Request<?> request, ResponseDelivery delivery);
}
