package com.freeload.jason;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.widget.Toast;

import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;
import com.freeload.jason.toolbox.DownloadRequest;
import com.freeload.jason.toolbox.Freeload;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    private RequestQueue requestQueue = null;
    private DownloadRequest request = null;

    private String Url = "http://gdown.baidu.com/data/wisegame/f70d2a17410e25a8/shoujiyingyongshichang_1.apk";
    private int mDownloadSize = 0;

    public ApplicationTest() {
        super(Application.class);
    }
}