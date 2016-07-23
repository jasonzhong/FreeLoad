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
    private DownloadRequestManager requestDoublie = null;
    private DownloadRequestManager requestNormal = null;

    private Button mStart = null;
    private Button mEnd = null;
    private Button mResume = null;

    private Button mStart1 = null;
    private Button mEnd1 = null;
    private Button mResume1 = null;

    private String receipt = "";

    private String downloadUrl = "http://bcs.91.com/wisedown/data/wisegame/4c6e7cc01e81f333/hetaoduobao_33.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Freeload.newRequestQueue(this);

        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDoublie = DownloadRequestManager.create()
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
                requestDoublie.cancel();
            }
        });

        mResume = (Button) findViewById(R.id.resume);
        mResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDoublie = DownloadRequestManager.create()
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

        mStart1 = (Button) findViewById(R.id.start1);
        mStart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNormal = DownloadRequestManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
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

        mEnd1 = (Button) findViewById(R.id.stop1);
        mEnd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNormal.cancel();
            }
        });

        mResume1 = (Button) findViewById(R.id.resume1);
        mResume1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNormal = DownloadRequestManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
                        .setEscapeReceipt(receipt)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
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
