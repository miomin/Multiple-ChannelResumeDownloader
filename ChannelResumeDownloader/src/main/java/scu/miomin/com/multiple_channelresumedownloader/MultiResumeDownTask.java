package scu.miomin.com.multiple_channelresumedownloader;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 莫绪旻 on 16/2/20.
 * 实现多通道断点续传下载功能
 */
public class MultiResumeDownTask {

    // 下载完成
    private static final int DOWNLOADFINISHED = 1;

    /**
     * 下载过程变化的回调
     */
    private OnDownLoadStateListener onDownLoadStateListener;

    public interface OnDownLoadStateListener {
        /**
         * 下载进度变化的回调
         *
         * @param process
         */
        void OnDownLoadProcessChange(int process);

        /**
         * 下载开始的回调
         *
         * @param process
         */
        void OnDownLoadStart(int process);

        /**
         * 暂停下载的回调
         *
         * @param process
         */
        void OnDownLoadResume(int process);

        /**
         * 下载完成的回调
         *
         * @param process
         */
        void OnDownLoadFinished(int process);
    }

    // 接受下载过程数据的Handler
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOADFINISHED:
                    // 下载完成的回调
                    onDownLoadStateListener.OnDownLoadFinished(process);
                    break;
            }
        }
    };

    private static final String TAG = "miomin";

    // 记录文件下载了多少
    private int process = 0;

    // 表示文件分成BLOCKCOUNT块，使用BLOCKCOUNT个单独的线程并行下载,必须大于0
    private final int BLOCKCOUNT = 3;

    // 表示是否正在下载
    private boolean downloading = false;

    private Context context;
    // file在服务端的url字符串表示
    private String fileUrl;
    // file在手机本地的引用
    private File file;
    // file在服务端的url
    private URL url;

    // 用于断点续传时保存每个线程的上下文
    private List<HashMap<String, Integer>> threadList;

    // 需要下载的文件长度
    private int fileLength = -1;

    // 构造器
    public MultiResumeDownTask(Context context, String fileUrl, OnDownLoadStateListener onDownLoadStateListener) {
        try {
            this.fileUrl = fileUrl;
            this.context = context;
            this.file = new File(Environment.getExternalStorageDirectory(),
                    getFileNameFromUrl(fileUrl));
            this.url = new URL(fileUrl);
            this.threadList = new ArrayList<>();
            this.onDownLoadStateListener = onDownLoadStateListener;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始下载
     */
    public void startDownload() {
        downloading = true;
        // 开始下载的回调
        onDownLoadStateListener.OnDownLoadStart(process);

        if (threadList.size() == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        fileLength = MultiResumeDownHelper.getFileLength(fileUrl, context);

                        if (fileLength < 0) {
                            Log.i(TAG, "文件不存在");
                            return;
                        }

                        if (!SDCardTool.ExistSDCard()) {
                            return;
                        }

                        //读写模式为rw
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        randomAccessFile.setLength(fileLength);

                        // 文件分成三个线程下载
                        int blockSize = fileLength / BLOCKCOUNT;

                        for (int i = 0; i < BLOCKCOUNT; i++) {
                            int begin = i * blockSize;
                            int end = (i + 1) * blockSize;
                            // 整除BLOCKCOUNT的误差处理
                            if (i == BLOCKCOUNT - 1) {
                                end = fileLength;
                            }

                            // 初始化上下文
                            HashMap<String, Integer> map = new HashMap<String, Integer>();
                            map.put("begin", begin);
                            map.put("end", end);
                            map.put("finished", 0);
                            threadList.add(map);

                            //创建新的线程，下载文件
                            Thread thread = new Thread(new DownloadRunable(i, begin, end));
                            thread.start();
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            // 断点续传恢复上下文，恢复下载
            for (int i = 0; i < BLOCKCOUNT; i++) {
                HashMap<String, Integer> map = threadList.get(i);
                int begin = map.get("begin");
                int end = map.get("end");
                int finished = map.get("finished");
                Thread thread = new Thread(new DownloadRunable(i, begin + finished, end));
                thread.start();
            }
        }
    }

    /**
     * 暂停下载
     */
    public void resumeDownload() {
        downloading = false;
        // 暂停下载的回调
        onDownLoadStateListener.OnDownLoadResume(process);
    }

    private String getFileNameFromUrl(String url) {
        int index = url.lastIndexOf("/") + 1;
        return url.substring(index);
    }

    /**
     * 下载通道
     */
    private class DownloadRunable implements Runnable {

        // 线程id
        private int id;
        // 该block在文件中的起始位置
        private int begin;
        // 该block在文件中的结束为止
        private int end;

        public DownloadRunable(int id, int begin, int end) {
            this.id = id;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            Log.i(TAG, "下载中：" + id);
            InputStream is = null;
            // 不同的线程要用不同的randomAccessFile对象，不能直接把主线程的randomAccessFile对象传进来，不然访问时会有
            // 冲突，可以用不同的randomAccessFile对象来操作同一个文件
            RandomAccessFile randomAccessFile = null;
            try {
                if (begin > end) {
                    return;
                }
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                // 伪装成浏览器
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                connection.setRequestProperty("Range", "bytes=" + begin + "-" + end);

                is = connection.getInputStream();
                byte[] buf = new byte[1024 * 1024];
                randomAccessFile = new RandomAccessFile(file, "rw");
                // 定位
                randomAccessFile.seek(begin);

                int len = 0;

                while (((len = is.read(buf)) != -1) && downloading) {
                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);
                    // 保存断点续传的上下文
                    threadList.get(id).put("finished", threadList.get(id).get("finished") + len);

                    // 下载进度变化的回调
                    onDownLoadStateListener.OnDownLoadProcessChange(process);
                }

                if (is != null) {
                    is.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 跟新文件的下载进度total
    synchronized private void updateProgress(int add) {
        process += add;
        if (process >= fileLength) {
            // 工作线程中不回调,通过handler通知
            Message message;
            message = Message.obtain();
            message.what = DOWNLOADFINISHED;
            mHandler.sendMessage(message);
        }
    }

    public boolean isDownloading() {
        return downloading;
    }
}
