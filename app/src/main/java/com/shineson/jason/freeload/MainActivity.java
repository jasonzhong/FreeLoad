package com.shineson.jason.freeload;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.freeload.jason.core.DownloadThreadType;
import com.freeload.jason.core.RequestQueue;
import com.freeload.jason.core.Response;
import com.freeload.jason.toolbox.DownloadRequestManager;
import com.freeload.jason.toolbox.EscapeReceipt;
import com.freeload.jason.toolbox.Freeload;

public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue = null;
    private DownloadRequestManager request = null;

    private String downloadUrl = "http://sw.bos.baidu.com/sw-search-sp/software/f726db3f1f943/QQ_8.4.18380.0_setup.exe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestQueue = Freeload.newRequestQueue(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

                request = DownloadRequestManager.create()
                        .setDownloadId(1)
                        .setDownloadUrl(downloadUrl)
                        .setDownloadThreadType(DownloadThreadType.DOUBLETHREAD)
                        .setListener(new Response.Listener<EscapeReceipt>() {
                            @Override
                            public void onProgressChange(EscapeReceipt s) {
                                System.out.println(s.toString());
                            }
                        })
                        .addRequestQueue(requestQueue);
            }
        });
    }

}
