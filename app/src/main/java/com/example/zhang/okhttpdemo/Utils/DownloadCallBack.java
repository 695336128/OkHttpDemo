package com.example.zhang.okhttpdemo.Utils;

/**
 * 断点下载回调 Created by zhang on 2017/2/24.
 */

public interface DownloadCallBack {

    void download(String current_progress, String total_progress);

    void downloadprogress(int progress);
}
