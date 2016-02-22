package scu.miomin.com.multiple_channelresumedownloader;

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

/**
 * Created by 莫绪旻 on 16/2/20.
 */
public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = "miomin";

    private Button btnDown;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;

    MultiResumeDownTask multiResumeDownTask;

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
                new MultiResumeDownTask(ExampleActivity.this,
                        "http://192.168.23.1:8080/test.pdf",
                        new MultiResumeDownTask.OnDownLoadStateListener() { // 这是监听MultiResumeDownloader下载过程的回调
                            @Override
                            public void OnDownLoadProcessChange(int process) {
                                Log.i(TAG, "process:" + process);
                            }

                            @Override
                            public void OnDownLoadStart(int process) {
                                btnDown.setText("暂停下载");
                            }

                            @Override
                            public void OnDownLoadResume(int process) {
                                btnDown.setText("开始下载");
                            }

                            @Override
                            public void OnDownLoadFinished(int process) {
                                btnDown.setText("下载完成");
                                btnDown.setEnabled(false);
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
