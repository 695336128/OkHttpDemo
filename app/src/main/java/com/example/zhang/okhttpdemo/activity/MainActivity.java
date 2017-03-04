package com.example.zhang.okhttpdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.zhang.okhttpdemo.R;
import com.example.zhang.okhttpdemo.Utils.HttpUtils;
import com.example.zhang.okhttpdemo.event.TestEvent;
import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    @BindView(R.id.getHttpBtn)
    Button getHttpBtn;

    private HttpUtils httpUtils;

    @BindView(R.id.postMapBtn)
    Button postMapBtn;

    @BindView(R.id.postMap2Btn)
    Button postMap2Btn;

    @BindView(R.id.downLoadBtn)
    Button downLoadBtn;

    @BindView(R.id.brokePointBtn)
    Button brokePointBtn;

    @BindView(R.id.eventBtn)
    Button eventBtn;

    @BindView(R.id.progressBar)
    ProgressBar progress;

    @BindView(R.id.downManagerBtn)
    Button downManagerBtn;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case 1001:
                KLog.i(msg.obj.toString());
                break;
            case 1002:
                KLog.json(msg.obj.toString());
                break;
            case 9999:
                // 请求失败
                KLog.i("数据请求失败了啊");
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        ButterKnife.bind(this);
        initView();
        httpUtils = new HttpUtils(context);
        EventBus.getDefault().register(this);

    }

    private void initView() {

        progress.setMax(100);
        progress.setProgress(0);

        getHttpBtn.setOnClickListener(this);
        postMapBtn.setOnClickListener(this);
        postMap2Btn.setOnClickListener(this);
        downLoadBtn.setOnClickListener(this);
        eventBtn.setOnClickListener(this);
        brokePointBtn.setOnClickListener(this);
        downManagerBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.getHttpBtn:
            httpUtils.getHttp(
                    "http://api.k780.com:88/?app=life.time&appkey=22270&sign=f21231ac898537114ae6a300c8d2b175&format=json",
                    handler, 1001);
            break;
        case R.id.postMapBtn:
            Map<String, String> map = new HashMap<>();
            map.put("userId", "zhangteng");
            map.put("password", "123");
            map.put("osType", "OT1");
            map.put("deviceType", "DT2");
            httpUtils.post("http://101.201.76.73:8888/semp/thirdparty/oapi/mportal/login?_type=json", map, handler,
                    1002);
            break;
        case R.id.postMap2Btn:
            Map<String, String> map2 = new HashMap<>();
            map2.put("userId", "zhangteng");
            map2.put("password", "123");
            map2.put("osType", "OT1");
            map2.put("deviceType", "DT2");
            httpUtils.post("http://101.201.76.73:8888/semp/thirdparty/oapi/mportal/login?_type=json", map2,
                    new HttpUtils.ReqCallBack<String>() {

                        @Override
                        public void onReqSuccess(String result) {
                            KLog.json(result);
                        }

                        @Override
                        public void onReqFailed(String errorMsg) {
                            KLog.json(errorMsg);
                        }
                    });
            break;
        case R.id.downLoadBtn:
            httpUtils.downLoadFile(
                    "http://shouji.360tpcdn.com/161209/419c49e40ed768588244be6ea06dcff9/com.estrongs.android.pop_552.apk",
                    Environment.getExternalStorageDirectory().getAbsolutePath(),
                    new HttpUtils.ReqProgressCallBack<File>() {
                        @Override
                        public void onReqSuccess(File result) {
                            KLog.d("文件下载地址是" + Environment.getExternalStorageDirectory().getAbsolutePath()
                                    + result.getName());
                        }

                        @Override
                        public void onReqFailed(String errorMsg) {
                            KLog.d(errorMsg);
                        }

                        @Override
                        public void onProgress(long total, long current) {
                            int p = (int) (current * 100 / total);
                            progress.setProgress(p);
                        }
                    });
            break;
        case R.id.eventBtn:
            Intent intent = new Intent(context, EventBusActivity.class);
            startActivity(intent);
            break;
        case R.id.brokePointBtn:
            Intent broke_point_intent = new Intent(context, BrokePointActivity.class);
            startActivity(broke_point_intent);
        case R.id.downManagerBtn:
            startActivity(new Intent(context,DownManagerActivity.class));
            break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTestEvent(TestEvent event) {
        // 处理eventbus接收到的消息
        eventBtn.setText(event.getEventName());
    }

}
