package com.example.zhang.okhttpdemo.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.zhang.okhttpdemo.R;
import com.socks.library.KLog;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 使用downloadManager下载
 */
public class DownManagerActivity extends AppCompatActivity {

    private Context context;

    private String mimeType = "application/vnd.android.package-archive";

    private Button downloadManagerBtn;

    private ProgressBar progressBar;

    private String url = "http://gdown.baidu.com/data/wisegame/c5ada0a2f33be088/baidushoujizhushouyuan91zhu_16792523.apk";

    private DownloadManager mDownloadManager;

    private DownloadManager.Request mRequest;

    private DownloadManager.Query mQuery;

    private Long downId;

    private Timer mTimer;

    private String filePath;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1001) {
                progressBar.setProgress((Integer) msg.obj);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_manager);
        context = this;
        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("使用DownloadManager下载文件");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        downloadManagerBtn = (Button) findViewById(R.id.downloadManagerBtn);
        downloadManagerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        int Percentage = getQuery();
                        if (Percentage >= 100) {
                            cancel();
                            install(filePath);
                        }
                        Message msg = new Message();
                        msg.what = 1001;
                        msg.obj = Percentage;
                        handler.sendMessage(msg);

                    }
                }, 0, 100);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    /**
     * 下载
     */
    private void download() {
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        mRequest = new DownloadManager.Request(Uri.parse(url));
        mRequest.setDestinationInExternalPublicDir("/zhang_download/", url.substring(url.lastIndexOf("/")))// 指定下载路径
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE) // 指定可以在移动网络下下载
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI) // 可以再wifi下下载
                .setMimeType(mimeType) // 设定下载类型为apk
                // .addRequestHeader("header", "value") //网络连接的http头
                .setTitle("downloadManager下载...") // notification标题
                .setDescription("<文件描述>") // notification标题描述
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // notification可见方式

        // 将文件保存在应用所在文件夹下的Download文件夹下，下载的文件会随着应用的卸载而删除
        mRequest.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "file.apk");
        // 将文件保存在SD卡"zhang_download"文件夹下，这个文件夹如果不存在会自动创建
        mRequest.setDestinationInExternalPublicDir("/zhang_download/", "file.apk");

        // 指定可以在移动网络下下载
        mRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
        // 可以在wifi下下载
        mRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        // 设置下载的文件类型为.APK安装文件
        mRequest.setMimeType("application/vnd.android.package-archive");

        downId = mDownloadManager.enqueue(mRequest);
    }

    /**
     * 获取下载信息
     */
    private int getQuery() {
        int Percentage;
        mQuery = new DownloadManager.Query();
        Cursor cursor = mDownloadManager.query(mQuery.setFilterById(downId));
        if (cursor != null && cursor.moveToFirst()) {
            String address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            Percentage = (int) ((bytes_downloaded * 1f) / bytes_total * 100f);
            filePath = address;
            KLog.v(filePath);
            cursor.close();
            return Percentage;
        }
        return 0;

    }

    /**
     * 安装app
     * 
     * @param path
     */
    private void install(String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(path), mimeType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
