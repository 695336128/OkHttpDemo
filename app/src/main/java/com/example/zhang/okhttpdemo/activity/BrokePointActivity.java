package com.example.zhang.okhttpdemo.activity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.zhang.okhttpdemo.R;
import com.example.zhang.okhttpdemo.Utils.HttpDownloadBean;
import com.example.zhang.okhttpdemo.Utils.MyHttpUtil;
import com.example.zhang.okhttpdemo.Utils.SharePreferenceUtils;
import com.socks.library.KLog;

/**
 * 断点下载
 */
public class BrokePointActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "fate";

    private Context context;

    private Button start_1_btn;

    private TextView progress_1_tv;

    private Button pause_1_btn;

    private Button start_2_btn;

    private TextView progress_2_tv;

    private Button pause_2_btn;

    private String url = "http://gdown.baidu.com/data/wisegame/c5ada0a2f33be088/baidushoujizhushouyuan91zhu_16792523.apk";

    private MyHttpUtil myHttpUtil;

    private HttpDownloadBean mHttpDownloadBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broke_point);
        context = this;
        SharePreferenceUtils.getAppConfig(context);
        initView();
        myHttpUtil = MyHttpUtil.getInstance(context);
        mHttpDownloadBean = new HttpDownloadBean();
        mHttpDownloadBean.setUrl(url);
        mHttpDownloadBean.setFilepath(url.substring(url.lastIndexOf("/")));
        mHttpDownloadBean.setStoragepath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/zhang");
    }

    private void initView() {
        start_1_btn = (Button) findViewById(R.id.start_1_btn);
        progress_1_tv = (TextView) findViewById(R.id.progress_1_tv);
        pause_1_btn = (Button) findViewById(R.id.pause_1_btn);
        start_2_btn = (Button) findViewById(R.id.start_2_btn);
        progress_2_tv = (TextView) findViewById(R.id.progress_2_tv);
        pause_2_btn = (Button) findViewById(R.id.pause_2_btn);

        start_1_btn.setOnClickListener(this);
        pause_1_btn.setOnClickListener(this);
        start_2_btn.setOnClickListener(this);
        pause_2_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start_1_btn:
            myHttpUtil.download(mHttpDownloadBean, new MyHttpUtil.DownloadCallBack() {
                @Override
                public void download(String current_progress, String total_progress) {
//                    KLog.i(TAG, "current_progress :" + current_progress + "\n" + "total_progress:" + total_progress);
                }

                @Override
                public void downloadprogress(final int progress) {
                    KLog.i(TAG, "progress:" + progress + "%");
                    new AsyncTask<Void, Void, String>() {

                        @Override
                        protected String doInBackground(Void... voids) {
                            return String.valueOf(progress);
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            progress_1_tv.setText(s + "%");
                            super.onPostExecute(s);
                        }
                    }.execute();
                }
            });
            break;
        case R.id.pause_1_btn:
            myHttpUtil.pause(mHttpDownloadBean);
            break;
        case R.id.start_2_btn:

            break;
        case R.id.pause_2_btn:

            break;
        }
    }
}
