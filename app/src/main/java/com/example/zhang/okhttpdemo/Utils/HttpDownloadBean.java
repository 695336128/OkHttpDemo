package com.example.zhang.okhttpdemo.Utils;

/**
 * Created by zhang on 2017/2/24.
 */

public class HttpDownloadBean {

    /** 请求地址*/
    private String url = null;

    /** 存储路径*/
    private String storagepath = null;

    /** 文件名*/
    private String filepath = null;

    /** 当前下载长度*/
    private long current_length = 0L;


    private long total_length = 0L;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStoragepath() {
        return storagepath;
    }

    public void setStoragepath(String storagepath) {
        this.storagepath = storagepath;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public long getCurrent_length() {
        return current_length;
    }

    public void setCurrent_length(long current_length) {
        this.current_length = current_length;
    }

    public long getTotal_length() {
        return total_length;
    }

    public void setTotal_length(long total_length) {
        this.total_length = total_length;
    }
}
