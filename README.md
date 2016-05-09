# Multiple-ChannelResumeDownloader
 - 多线程断点续传下载
 - 多线程下载的好处：当同时有很多客户机从服务端下载文件时，CPU的时间片会平分给下载线程，多线程下载可以分到更多的CPU时间片，在一定程度上提高下载效率。
 - 断点续传：中途暂停下载或者退出Activity，恢复后可以接着上次下载的位置下载
 - 下载过程中可以监听文件下载进度，并在回调函数中做相应处理
 - 文件默认被放到SDCard的根目录

## Usage

### 新建下载任务

```Java
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
```

### 启动下载任务
```Java
MioRequestManager.getInstance().excuteDownTask(multiResumeDownTask);
```

### 暂停下载任务
```Java
MioRequestManager.getInstance().resumeDownTask(multiResumeDownTask);
```

### 绑定生命周期 
//实现下载任务与Activity生命周期绑定，当Activity销毁时，所有的下载任务自动终止heng
```Java
@Override
protected void onDestroy() {
    super.onDestroy();
    // 实现request与Activity生命周期绑定
    MioRequestManager.getInstance().cancelRequest(toString());
}
```
