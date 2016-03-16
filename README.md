# Multiple-ChannelResumeDownloader
文件默认被放到SDCard的根目录

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
multiResumeDownTask.startDownload();
```
### 暂停下载任务
```Java
multiResumeDownTask.resumeDownload();
```
### 判断是否正在加载
```Java
if (multiResumeDownTask.isDownloading()) {}
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
