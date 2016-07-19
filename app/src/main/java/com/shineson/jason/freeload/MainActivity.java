package com.shineson.jason.freeload;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.IReceipt;
import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;
import com.freeload.jason.toolbox.DownloadRequestManager;
import com.freeload.jason.toolbox.EscapeReceipt;
import com.freeload.jason.toolbox.Freeload;

public class MainActivity extends Activity {

    private RequestQueue requestQueue = null;
    private DownloadRequestManager request = null;

    private Button mStart = null;
    private Button mEnd = null;
    private Button mResume = null;

    private String receipt = "";

    private String downloadUrl = "http://sw.bos.baidu.com/sw-search-sp/software/f726db3f1f943/QQ_8.4.18380.0_setup.exe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Freeload.newRequestQueue(this);

        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request = DownloadRequestManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
                        .setDownloadThreadType(DownloadThreadType.DOUBLETHREAD)
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();
                                System.out.println(s.getReceipt());
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });

        mEnd = (Button) findViewById(R.id.stop);
        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.cancel();
            }
        });

        mResume = (Button) findViewById(R.id.resume);
        mResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request = DownloadRequestManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
                        .setEscapeReceipt(receipt)
                        .setDownloadThreadType(DownloadThreadType.DOUBLETHREAD)
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();
                                System.out.println(s.getReceipt());
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });
    }

}
