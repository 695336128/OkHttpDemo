package com.example.zhang.okhttpdemo.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by zhang on 2016/12/19. 使用OkHttp的工具类 默认全部加了时间戳作为请求头
 */

public class HttpUtils {

    private Context context;

    private String TAG = "fate";

    /**
     * 请求的tag，cancel中断请求的时候使用
     */
    private String REQUEST_TAG = "fate";

    private int CONNECT_TIMEOUT = 10;

    private int WRITE_TIMEOUT = 10;

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
     * 全局处理子线程和主线程通信
     */
    private Handler okHttpHandler;

    /**
     * OkHttpClient 对象
     */
    public OkHttpClient mOkHttpClient;

    /**
     * 单例对象
     */
    private static volatile HttpUtils instance = null;

    /**
     * 请求头
     */
    private Map<String, String> headerMap = new HashMap<>();

    /**
     * 是否使用缓存
     */
    private boolean useCache = false;

    /**
     * 缓存区大小 10Mib
     */
    private int cacheSize = 10 * 1024 * 1024;

    /**
     * 拦截器
     */
    private Interceptor interceptor;

    /**
     * 构造方法
     *
     * @param context
     */
    public HttpUtils(Context context) {
        this.context = context.getApplicationContext();
        // 初始化OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        Cache cache = new Cache(context.getCacheDir(), cacheSize);// 初始化缓存
        builder.cache(cache);
        builder.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);// 连接超时
        builder.connectTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS); // 写超时
        builder.connectTimeout(READ_TIMEOUT, TimeUnit.SECONDS); // 读超时
        mOkHttpClient = builder.build();
        builder.interceptors().add(initInterceptor());// 设置拦截器
        // 初始化Handler
        okHttpHandler = new Handler(context.getMainLooper());
    }

    /**
     * 获取单例引用
     *
     * @param context
     * @return
     */
    public static HttpUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (HttpUtils.class) {
                if (instance == null) {
                    instance = new HttpUtils(context);
                }
            }
        }
        return instance;
    }


    /**
     * get请求
     *
     * @param url
     *            请求地址
     * @param handler
     * @param what
     */
    public void getHttp(String url, final Handler handler, final int what) {

        // 使用缓存
        final CacheControl.Builder ccBuilder = new CacheControl.Builder();
        ccBuilder.maxAge(10, TimeUnit.SECONDS);// 设置超时时间为10s
        ccBuilder.maxStale(5, TimeUnit.SECONDS);// 超时之外的超时时间为5s
        CacheControl cacheControl = ccBuilder.build();

        // 创建一个Request
        final Request request = new Request.Builder()
                .url(url)
                .tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey())
                .cacheControl(cacheControl)
                .build();
        // new call
        Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(9999);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                Message message = new Message();
                message.what = what;
                message.obj = responseString;
                handler.sendMessage(message);
            }

        });

    }

    /**
     * post方法，参数为json
     *
     * @param url
     *            请求地址
     * @param jsonParams
     *            参数
     * @param handler
     * @param what
     */
    public void post(String url, String jsonParams, final Handler handler, final int what) {
        RequestBody body = RequestBody.create(JSON, jsonParams);
        Request request = new Request.Builder()
                .url(url)
                .tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey())
                .post(body)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(9999);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                Message message = new Message();
                message.what = what;
                message.obj = responseString;
                handler.sendMessage(message);
            }

        });
    }

    /**
     * 以键值对的方式post请求
     *
     * @param url
     *            请求地址
     * @param mapParams
     *            请求参数
     * @param handler
     * @param what
     */
    public void post(String url, Map<String, String> mapParams, final Handler handler, final int what) {

        FormBody.Builder builder = new FormBody.Builder();

        for (Map.Entry<String, String> entry : mapParams.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = builder.build();
        Request request = new Request.Builder().url(url).post(requestBody).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                handler.sendEmptyMessage(9999);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                Message message = new Message();
                message.what = what;
                message.obj = responseString;
                handler.sendMessage(message);
            }

        });

    }

    /*
     * * * * * * * * * * * * * * * * /* * /* 以上为使用handler处理返回信息 * /* * /*
     * 以下为使用callbak处理返回信息 * /* * /* * * * * * * * * * * * * * *
     */

    /**
     * get请求
     *
     * @param url
     *            请求地址
     */
    public <T> void getHttp(String url, final ReqCallBack<T> callBack) {
        // 创建一个Request
        final Request request = new Request.Builder().url(url).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).build();
        // new call
        final Call call = mOkHttpClient.newCall(request);
        // 请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Request failed", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                successCallBack((T) result, callBack);
            }

        });
    }

    /**
     * 以键值对的方式post请求
     *
     * @param url
     *            请求地址
     * @param mapParams
     *            请求参数
     */
    public <T> void post(String url, Map<String, String> mapParams, final ReqCallBack<T> callBack) {

        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : mapParams.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder().url(url).tag(REQUEST_TAG).post(requestBody)
                .addHeader("serviceOapiKey", getHeaderKey()).build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Request failed", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                successCallBack((T) result, callBack);
            }

        });

    }

    /**
     * 不带参数的文件上传
     *
     * @param actionUrl
     *            接口地址
     * @param filePath
     *            本地文件地址
     * @param callback
     */
    public <T> void upLoadFile(String actionUrl, String filePath, final ReqCallBack<T> callback) {
        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context, "文件不存在，请修改文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        // 创建requestBody
        RequestBody requestBody = RequestBody.create(MEDIA_OBJECT_STREAM, file);
        // 创建request
        Request request = new Request.Builder().url(actionUrl).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).post(requestBody).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Upload failed", callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    Log.d(TAG, string);
                    successCallBack((T) string, callback);
                } else {
                    failedCallBack("Upload failed", callback);
                }
            }

        });

    }

    /**
     * 带参数上传文件
     *
     * @param actionUrl
     *            接口地址
     * @param paramsMap
     *            Map格式参数
     * @param callback
     *            回调
     * @param <T>
     */
    public <T> void upLoadFile(String actionUrl, Map<String, Object> paramsMap, final ReqCallBack<T> callback) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 设置类型
        builder.setType(MultipartBody.FORM);
        // 追加参数
        for (String key : paramsMap.keySet()) {
            Object object = paramsMap.get(key);
            if (!(object instanceof File)) {
                // 如果参数不是文件
                builder.addFormDataPart(key, object.toString());
            } else {
                File file = (File) object;
                builder.addFormDataPart(key, file.getName(), RequestBody.create(null, file));
            }
        }
        // 创建RequestBody
        RequestBody requestBody = builder.build();
        // 创建Request
        Request request = new Request.Builder().url(actionUrl).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).post(requestBody).build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Upload failed", callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    Log.d(TAG, string);
                    successCallBack((T) string, callback);
                } else {
                    failedCallBack("Upload failed", callback);
                }
            }

        });
    }

    /**
     * 带参数带进度上传文件
     *
     * @param actionUrl
     *            接口地址
     * @param paramsMap
     *            参数
     * @param callBack
     *            回调
     * @param <T>
     */
    public <T> void upLoadFile(String actionUrl, Map<String, Object> paramsMap, final ReqProgressCallBack<T> callBack) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        // 设置类型
        builder.setType(MultipartBody.FORM);
        // 追加参数
        for (String key : paramsMap.keySet()) {
            Object object = paramsMap.get(key);
            if (!(object instanceof File)) {
                builder.addFormDataPart(key, object.toString());
            } else {
                File file = (File) object;
                builder.addFormDataPart(key, file.getName(),
                        createProgressRequestBody(MEDIA_OBJECT_STREAM, file, callBack));
            }
        }
        // 创建RequestBody
        RequestBody requestBody = builder.build();
        // 创建Requset
        Request request = new Request.Builder().url(actionUrl).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).post(requestBody).build();
        // 创建Call
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Upload failed", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String string = response.body().string();
                    Log.d(TAG, string);
                    successCallBack((T) string, callBack);
                } else {
                    failedCallBack("Upload failed", callBack);
                }
            }
        });

    }

    /**
     * 不带进度的文件下载方法
     *
     * @param fileUrl
     *            文件地址
     * @param destFileDir
     *            本地保存文件地址
     * @param callBack
     *            回调
     * @param <T>
     */
    public <T> void downLoadFile(String fileUrl, final String destFileDir, final ReqCallBack<T> callBack) {
        final String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        final File file = new File(destFileDir, fileName);
        if (file.exists()) {
            // 文件已经存在
            successCallBack((T) file, callBack);
            return;
        }
        Request request = new Request.Builder().url(fileUrl).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).build();

        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Download failed", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    int len = 0;
                    FileOutputStream fos = null;

                    try {
                        long total = response.body().contentLength();
                        long current = 0;
                        is = response.body().byteStream();
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            current += len;
                            fos.write(buf, 0, len);
                        }
                        fos.flush();
                        successCallBack((T) file, callBack);
                    } catch (IOException e) {
                        e.printStackTrace();
                        failedCallBack("Download failed", callBack);
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });

    }

    /**
     * 带进度的文件下载方法
     *
     * @param fileUrl
     *            文件地址
     * @param destFileDir
     *            本地保存文件地址
     * @param callBack
     *            回调
     * @param <T>
     */
    public <T> void downLoadFile(String fileUrl, final String destFileDir, final ReqProgressCallBack<T> callBack) {
        final String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        final File file = new File(destFileDir, fileName);
        if (file.exists()) {
            // 文件已经存在
            successCallBack((T) file, callBack);
        }
        Request request = new Request.Builder().url(fileUrl).tag(REQUEST_TAG)
                .addHeader("serviceOapiKey", getHeaderKey()).build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                failedCallBack("Download failed", callBack);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                {
                    InputStream is = null;
                    byte[] buf = new byte[2048];
                    FileOutputStream fos = null;
                    int len = 0;
                    try {
                        long total = response.body().contentLength();
                        long current = 0;
                        is = response.body().byteStream();
                        fos = new FileOutputStream(file);
                        while ((len = is.read(buf)) != -1) {
                            current += len;
                            fos.write(buf, 0, len);
                            progressCallBack(total, current, callBack);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        failedCallBack("Download failed", callBack);
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                            if (fos != null) {
                                fos.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
    }

    /**
     * 创建带进度的RequestBody
     *
     * @param ontentType
     *            MediaType
     * @param file
     *            准备上传的文件
     * @param callBack
     *            回调
     * @param <T>
     * @return RequestBody
     */
    private <T> RequestBody createProgressRequestBody(final MediaType ontentType, final File file,
            final ReqProgressCallBack<T> callBack) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return ontentType;
            }

            @Override
            public long contentLength() throws IOException {
                return file.length();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source;
                try {
                    source = Okio.source(file);
                    Buffer buf = new Buffer();
                    long remaining = contentLength();
                    long current = 0;
                    for (long readcount; (readcount = source.read(buf, 2048)) != -1;) {
                        sink.write(buf, readcount);
                        current += readcount;
                        Log.d(TAG, "current------>" + current);
                        progressCallBack(remaining, current, callBack);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

    }

    /**
     * 取消所有Tag为REQUEST_TAG的请求。
     *
     * @throws Exception
     */
    public void cancelRequest() throws Exception {
        // Call call = mOkHttpClient.newCall(request);
    }

    /*
     * * * * * * * * * * * * * * * * /* * /* 以下为接口和回调方法的定义 * /* * /* * * * * * *
     * * * * * * * * *
     */

    /**
     * 统一处理成功信息
     *
     * @param result
     *            成功返回信息
     * @param callback
     *            回调
     * @param <T>
     */
    private <T> void successCallBack(final T result, final ReqCallBack<T> callback) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onReqSuccess(result);
                }
            }
        });

    }

    /**
     * 统一处理失败信息
     *
     * @param errorMsg
     *            失败提示
     * @param callback
     *            回调
     * @param <T>
     */
    private <T> void failedCallBack(final String errorMsg, final ReqCallBack<T> callback) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onReqFailed(errorMsg);
                }
            }
        });
    }

    /**
     * 统一处理进度信息
     *
     * @param total
     *            总进度
     * @param current
     *            当前进度
     * @param callBack
     *            回调
     * @param <T>
     */
    private <T> void progressCallBack(final long total, final long current, final ReqProgressCallBack<T> callBack) {
        okHttpHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callBack != null) {
                    callBack.onProgress(total, current);
                }
            }
        });
    }

    public interface ReqCallBack<T> {
        void onReqSuccess(T result);

        void onReqFailed(String errorMsg);
    }

    public interface ReqProgressCallBack<T> extends ReqCallBack<T> {
        void onProgress(long total, long current);
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳
     */
    public static String getHeaderKey() {
        String value = Long.toString(System.currentTimeMillis());
        String time = "";
        try {
            time = encryptHeader.getEncryptedText("servicePotralKey", value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * 初始化拦截器 无网状态强制使用缓存
     */
    private Interceptor initInterceptor() {
        interceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                boolean connected = NetUtils.isConnected(context);
                // 如果没网，强制读取缓存数据
                if (!connected) {
                    request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
                }
                Response response = chain.proceed(request);
                // 如果服务器没有缓存头，重写"Cache-Control"字段
                Response response1 = response.newBuilder().removeHeader("Pragma").removeHeader("Cache-Control")
                        // cache for 30 days
                        .header("Cache-Control", "max-age=" + 3600 * 24 * 30).build();

                return response1;
            }
        };
        return interceptor;
    }

}
