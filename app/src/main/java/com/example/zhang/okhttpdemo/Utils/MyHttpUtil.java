package com.example.zhang.okhttpdemo.Utils;

import android.content.Context;

import com.socks.library.KLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OK-Http 工具 Created by zhang on 2017/2/24.
 */

public class MyHttpUtil {

    private static volatile MyHttpUtil instance = null;

    private Context context;

    private String TAG = "lilith";

    /**
     * 请求的tag，cancel中断请求的时候使用
     */
    private String REQUEST_TAG = "fate";

    private int CONNECT_TIMEOUT = 10;

    private int WRITE_TIMEOUT = 30;

    private int READ_TIMEOUT = 30;

    /**
     * mdiatype json,和服务器要一致
     */
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * mdiatype 文件,和服务器要一致
     */
    private static final MediaType MEDIA_OBJECT_STREAM = MediaType.parse("application/octet-stream");

    /**
     * OkHttpClient 对象
     */
    private OkHttpClient mOkHttpClient;

    private MyHttpUtil(Context context) {
        this.context = context.getApplicationContext();
        initOkHttpClient();
    }

    /**
     * 初始化OkHttpClient
     */
    private void initOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);// 连接超时
        builder.connectTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS); // 写超时
        builder.connectTimeout(READ_TIMEOUT, TimeUnit.SECONDS); // 读超时
        builder.addNetworkInterceptor(DownLoadInterceptor());
        mOkHttpClient = builder.build();
    }

    public static MyHttpUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (MyHttpUtil.class) {
                if (instance == null) {
                    instance = new MyHttpUtil(context);
                }
            }
        }
        return instance;
    }

    private Call newCall(HttpDownloadBean httpDownloadBean) {
        Request request = new Request.Builder().tag(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()))
                .url(httpDownloadBean.getUrl())
                .addHeader("Accept-Encoding", "identity")
                .header("RANGE",
                        "bytes=" + SharePreferenceUtils
                                .getParam(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()), (long) 0) + "-")
                .build();
        return mOkHttpClient.newCall(request);
    }

    /**
     * 断点下载的拦截器
     */
    private Interceptor DownLoadInterceptor() {
        Interceptor downloadInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                Response newResponse = originalResponse.newBuilder().body(originalResponse.body()).build();
                return newResponse;
            }
        };
        return downloadInterceptor;
    }

    /**
     * 断点下载方法
     *
     * @param httpDownloadBean
     *            下载信息
     * @param callBack
     *            回调
     */
    public void download(final HttpDownloadBean httpDownloadBean, final DownloadCallBack callBack) {
        if (!SharePreferenceUtils.contains(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()))) {
            SharePreferenceUtils.setParam(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()), (long) 0);
        }
        if (!SharePreferenceUtils.contains(EncryptUtils.encryptMD2ToString(httpDownloadBean.getUrl()))) {
            SharePreferenceUtils.setParam(EncryptUtils.encryptMD2ToString(httpDownloadBean.getUrl()), (long) 0);
        }

        Call call = newCall(httpDownloadBean);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                KLog.v("下载失败了啊。");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                writeToSDCard(response, httpDownloadBean, callBack);
            }
        });
    }

    /**
     * 暂停下载
     * 
     * @param httpDownloadBean
     */
    public void pause(HttpDownloadBean httpDownloadBean) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()))) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (call.request().tag().equals(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()))) {
                call.cancel();
            }
        }
    }

    /**
     * 写入sd卡
     * 
     * @param response
     * @param httpDownloadBean
     * @param callBack
     */
    private void writeToSDCard(Response response, HttpDownloadBean httpDownloadBean, DownloadCallBack callBack) {
        ResponseBody body = response.body();
        InputStream is = body.byteStream();
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null; // 随机访问文件，可以指定断点续传的起始位置
        long current = 0;
        long total = 0;
        current = (long) SharePreferenceUtils.getParam(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()),
                (long) 0);
        total = (long) SharePreferenceUtils.getParam(EncryptUtils.encryptMD2ToString(httpDownloadBean.getUrl()),
                (long) 0);
        if (total == 0) {
            total = body.contentLength();
            httpDownloadBean.setTotal_length(body.contentLength());
            SharePreferenceUtils.setParam(EncryptUtils.encryptMD2ToString(httpDownloadBean.getUrl()),
                    httpDownloadBean.getTotal_length());
        }
        try {
            randomAccessFile = new RandomAccessFile(
                    createStorgeFile(httpDownloadBean.getStoragepath(), httpDownloadBean.getFilepath()), "rwd");

            // Chanel NIO 中的用法，由于RandomAccessFile 没有使用缓存策略，直接使用会使下载速度变得慢
            channelOut = randomAccessFile.getChannel();
            KLog.v("lilith", "randomAccessFile.length()=" + randomAccessFile.length());
            KLog.v("lilith", "current=" + current);
            KLog.v("lilith", "total=" + total);
            // 内存映射，直接使用RandomAccessFil，是用其seek方法指定其下载位置。这里使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, current, total - current);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                current += len;
                if (callBack != null) {
                    callBack.download(String.valueOf(current), String.valueOf(total));
                    callBack.downloadprogress((int) (current * 1.0f / total * 100f));
                }
                httpDownloadBean.setCurrent_length(current);
                if (current >= total) {
                    SharePreferenceUtils.remove(context, EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()));
                    SharePreferenceUtils.remove(context, EncryptUtils.encryptMD2ToString(httpDownloadBean.getUrl()));
                } else {
                    SharePreferenceUtils.setParam(EncryptUtils.encryptMD5ToString(httpDownloadBean.getUrl()),
                            httpDownloadBean.getCurrent_length());
                }
                mappedBuffer.put(buffer, 0, len);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建file
     */
    private File createStorgeFile(String destFileDir, String name) {
        File fileDir = new File(destFileDir);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        File file = new File(destFileDir, name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public interface DownloadCallBack {
        void download(String current_progress, String total_progress);

        void downloadprogress(int progress);
    }

}
