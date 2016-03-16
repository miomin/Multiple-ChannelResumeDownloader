package scu.miomin.com.multiple_channelresumedownloader.listener;

import java.io.File;

/**
 * Created by 莫绪旻 on 16/3/15.
 */
public interface MioDownLoadStateListener {

    /**
     * 下载进度变化的回调
     *
     * @param process
     */
    void OnDownLoadProcessChange(int process);

    /**
     * 下载开始的回调
     */
    void OnDownLoadStart(int fileLength);

    /**
     * 暂停下载的回调
     *
     * @param process
     */
    void OnDownLoadResume(int process);

    /**
     * 下载完成的回调
     */
    void OnDownLoadFinished(File file);

    /**
     * 下载失败的回调
     */
    void OnDownLoadFailed(String error);
}
