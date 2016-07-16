package com.freeload.jason.toolbox;

import android.content.Context;

import com.freeload.jason.core.BasicDownload;
import com.freeload.jason.core.EndingDownload;
import com.freeload.jason.core.PrepareDownload;
import com.freeload.jason.core.RequestQueue;

public class Freeload {

    public static RequestQueue newRequestQueue(Context context) {
        BasicDownload basicDownload = new BasicDownload();
        PrepareDownload prepareDownload = new PrepareDownload();
        EndingDownload endingDownload = new EndingDownload();

        RequestQueue queue = new RequestQueue(basicDownload, prepareDownload, endingDownload);
        queue.start();

        return queue;
    }
}
