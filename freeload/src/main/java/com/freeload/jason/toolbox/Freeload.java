package com.freeload.jason.toolbox;

import com.freeload.jason.core.EndingDownload;
import com.freeload.jason.core.PrepareDownload;
import com.freeload.jason.core.RequestQueue;

public class Freeload {
    public static RequestQueue newRequestQueue() {
        PrepareDownload prepareDownload = new PrepareDownload();
        EndingDownload endingDownload = new EndingDownload();

        RequestQueue queue = new RequestQueue(prepareDownload, endingDownload);
        queue.start();

        return queue;
    }
}
