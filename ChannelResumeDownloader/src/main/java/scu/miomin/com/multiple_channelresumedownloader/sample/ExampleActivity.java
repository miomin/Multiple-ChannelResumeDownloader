package scu.miomin.com.multiple_channelresumedownloader.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import scu.miomin.com.multiple_channelresumedownloader.listener.MioDownLoadStateListener;
import scu.miomin.com.multiple_channelresumedownloader.task.MioMultiResumeDownTask;
import scu.miomin.com.multiple_channelresumedownloader.task.MioRequestManager;
import scu.miomin.com.multiple_channelresumedownloader.R;

/**
 * Created by 莫绪旻 on 16/2/20.
 */
public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = "miomin";

    private Button btnDown;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;

    MioMultiResumeDownTask multiResumeDownTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // 申请Runtime Permission
        requestRuntimePermissions();

        btnDown = (Button) findViewById(R.id.btnDown);

        // 下面的url是需要下载的文件在服务器上的url
        multiResumeDownTask =
                new MioMultiResumeDownTask(ExampleActivity.this,
                        "http://192.168.253.1:8080/test.pdf", toString(),
                        new MioDownLoadStateListener() {
                            // 这是监听MultiResumeDownloader下载过程的回调
                            @Override
                            public void OnDownLoadProcessChange(int process) {
                                Log.i(TAG, "process:" + process);
                            }

                            @Override
                            public void OnDownLoadStart(int fileLength) {
                                btnDown.setText("开始下载,文件长度：" + fileLength);
                            }

                            @Override
                            public void OnDownLoadResume(int process) {
                                btnDown.setText("暂停下载,下载到：" + process);
                            }

                            @Override
                            public void OnDownLoadFinished(File file) {
                                btnDown.setText("下载完成");
                                btnDown.setEnabled(false);
                            }

                            @Override
                            public void OnDownLoadFailed(String error) {
                                Log.i(TAG, "下载失败:" + error);
                            }
                        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!multiResumeDownTask.isDownloading()) {
                    // 开始下载
                    multiResumeDownTask.startDownload();
                } else {
                    multiResumeDownTask.resumeDownload();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 实现request与Activity生命周期绑定
        MioRequestManager.getInstance().cancelRequest(toString());
    }

    // 申请需要的运行时权限
    private void requestRuntimePermissions() {

        // 如果版本低于Android6.0，不需要申请运行时权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    // 对运行时权限做相应处理
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
                finish();
            }
        }
    }
}
