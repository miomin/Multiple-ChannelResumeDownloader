# Multiple-ChannelResumeDownloader
多通道断点续传下载库，默认把文件放到SDCard的根目录

// 下面的url是需要下载的文件在服务器上的url
```Java
MultiResumeDownTask multiResumeDownTask =
    new MultiResumeDownTask(ExampleActivity.this,"http://192.168.23.1:8080/test.pdf",
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
```
        
// 开始下载
```Java
multiResumeDownTask.startDownload();
```
// 暂停下载
```Java
multiResumeDownTask.resumeDownload();
```
// 判断是否正在加载
```Java
if (multiResumeDownTask.isDownloading()) {}
```
