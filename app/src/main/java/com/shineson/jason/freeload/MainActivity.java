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
    private DownloadManager requestNormal1 = null;
    private DownloadManager requestNormal2 = null;

    private Button mStart = null;
    private Button mEnd = null;
    private Button mResume = null;

    private Button mStart1 = null;
    private Button mEnd1 = null;
    private Button mResume1 = null;

    private Button mStart2 = null;
    private Button mEnd2 = null;
    private Button mResume2 = null;

    private Button mStart3 = null;
    private Button mEnd3 = null;
    private Button mResume3 = null;

    private Button mStart4 = null;
    private Button mEnd4 = null;
    private Button mResume4 = null;

    private String receipt = "";

    private String downloadUrl = "http://admin.doyo.cn/mobile/apk/5e/11d4289a4e2e03f78fd57f4509f219.apk";
    private String downloadUrl1 = "http://a5.pc6.com/xyb3/yamuyingyu.apk";
    private String downloadUrl2 = "http://admin.doyo.cn/mobile/apk/e1/fd44006e9d4fe48948313d7d0be6d9.apk";
    private String downloadUrl3 = "http://admin.doyo.cn/mobile/apk/f9/a4224b48cdd3f50f47b2f445f48f5f.apk";

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

        mStart1 = (Button) findViewById(R.id.start2);
        mStart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String mDownloadedFileFolder = getCacheDir() + File.separator + "kuaichuanshou" + File.separator + "fdownload";
                requestNormal = DownloadManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
//                        .setDownloadedFileFolder(mDownloadedFileFolder)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
                        .setPepareListener(new Response.PepareListener<IReceipt>() {
                            @Override
                            public void onProgressPepare(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                long size = s.getDownloadedSize();
                                System.out.println(receipt);
                                System.out.println("xxxx: state:" + i + " path:" + path + " size:" + size);
                            }
                        })
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                if (i == DownloadReceipt.STATE.SUCCESS_COMBIN_FILE) {
                                    System.out.println("xxxx: state:" + i + " path:" + path + " size:" + s.getDownloadedSize());
                                }
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });

        mStart2 = (Button) findViewById(R.id.start3);
        mStart2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String mDownloadedFileFolder = getCacheDir() + File.separator + "kuaichuanshou" + File.separator + "fdownload";
                requestNormal = DownloadManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl1)
//                        .setDownloadedFileFolder(mDownloadedFileFolder)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
                        .setPepareListener(new Response.PepareListener<IReceipt>() {
                            @Override
                            public void onProgressPepare(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                long size = s.getDownloadedSize();
                                System.out.println(receipt);
                                System.out.println("xxxx 1: state:" + i + " path:" + path + " size:" + size);
                            }
                        })
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                if (i == DownloadReceipt.STATE.SUCCESS_COMBIN_FILE) {
                                    System.out.println("xxxx 1: state:" + i + " path:" + path + " size:" + s.getDownloadedSize());
                                }
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });

        mStart3 = (Button) findViewById(R.id.start4);
        mStart3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String mDownloadedFileFolder = getCacheDir() + File.separator + "kuaichuanshou" + File.separator + "fdownload";
                requestNormal = DownloadManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl2)
//                        .setDownloadedFileFolder(mDownloadedFileFolder)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
                        .setPepareListener(new Response.PepareListener<IReceipt>() {
                            @Override
                            public void onProgressPepare(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                long size = s.getDownloadedSize();
                                System.out.println(receipt);
                                System.out.println("xxxx 2: state:" + i + " path:" + path + " size:" + size);
                            }
                        })
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                if (i == DownloadReceipt.STATE.SUCCESS_COMBIN_FILE) {
                                    System.out.println("xxxx 2: state:" + i + " path:" + path + " size:" + s.getDownloadedSize());
                                }
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });

        mStart4 = (Button) findViewById(R.id.start5);
        mStart4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                String mDownloadedFileFolder = getCacheDir() + File.separator + "kuaichuanshou" + File.separator + "fdownload";
                requestNormal = DownloadManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl3)
//                        .setDownloadedFileFolder(mDownloadedFileFolder)
                        .setDownloadThreadType(DownloadThreadType.NORMAL)
                        .setPepareListener(new Response.PepareListener<IReceipt>() {
                            @Override
                            public void onProgressPepare(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                long size = s.getDownloadedSize();
                                System.out.println(receipt);
                                System.out.println("xxxx 2: state:" + i + " path:" + path + " size:" + size);
                            }
                        })
                        .setListener(new Response.Listener<IReceipt>() {
                            @Override
                            public void onProgressChange(IReceipt s) {
                                receipt = s.getReceipt();

                                DownloadReceipt.STATE i = s.getReceiptState();
                                String path = s.getDownloadFilePath();
                                if (i == DownloadReceipt.STATE.SUCCESS_COMBIN_FILE) {
                                    System.out.println("xxxx 2: state:" + i + " path:" + path + " size:" + s.getDownloadedSize());
                                }
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });

        mEnd1 = (Button) findViewById(R.id.stop2);
        mEnd1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestNormal.cancel();
            }
        });

        mResume1 = (Button) findViewById(R.id.resume2);
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
