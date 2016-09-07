package com.shineson.jason.freeload;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.IReceipt;
import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;
import com.freeload.jason.toolbox.DownloadManager;
import com.freeload.jason.toolbox.DownloadReceipt;
import com.freeload.jason.toolbox.EscapeReceipt;
import com.freeload.jason.toolbox.Freeload;

import java.io.File;

public class MainActivity extends Activity {

    private RequestQueue requestQueue = null;
    private DownloadManager requestDoublie = null;
    private DownloadManager requestNormal = null;

    private Button mStart = null;
    private Button mEnd = null;
    private Button mResume = null;

    private Button mStart1 = null;
    private Button mEnd1 = null;
    private Button mResume1 = null;

    private String receipt = "";

    private String downloadUrl = "http://dl.cm.ksmobile.com/static/res/4d/5a/cm_security_cn.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Freeload.newRequestQueue(this);

        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDoublie = DownloadManager.create()
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
                requestDoublie = DownloadManager.create()
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

                String mDownloadedFileFolder = getCacheDir() + File.separator + "kuaichuanshou" + File.separator + "download";
                requestNormal = DownloadManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
                        .setDownloadedFileFolder(mDownloadedFileFolder)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                System.out.println("xxxx: state:" + i + " path:" + path);
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
                requestNormal = DownloadManager.create()
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
