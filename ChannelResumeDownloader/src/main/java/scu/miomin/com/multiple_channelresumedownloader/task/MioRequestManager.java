package scu.miomin.com.multiple_channelresumedownloader.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by miomin on 16/3/16.
 */
public class MioRequestManager {

    private static MioRequestManager instance;
    private HashMap<String, ArrayList<MioMultiResumeDownTask>> downtaskMap;

    public static MioRequestManager getInstance() {
        if (instance == null) {
            instance = new MioRequestManager();
        }
        return instance;
    }

    private MioRequestManager() {
        downtaskMap = new HashMap<>();
    }

    // 执行文件下载任务
    public void excuteDownTask(MioMultiResumeDownTask task) {
        if (!downtaskMap.containsKey(task.getTag())) {
            ArrayList<MioMultiResumeDownTask> downTasks = new ArrayList<>();
            downtaskMap.put(task.getTag(), downTasks);
        }
        downtaskMap.get(task.getTag()).add(task);
    }

    // 取消与tag的Activity相关的所有任务
    public void cancelRequest(String tag) {

        if (tag == null || "".equals(tag.trim())) {
            return;
        }

        // 暂停与该activity关联的所有下载任务
        if (downtaskMap.containsKey(tag)) {
            ArrayList<MioMultiResumeDownTask> downTasks = downtaskMap.remove(tag);
            for (MioMultiResumeDownTask downTask : downTasks) {
                if (!downTask.isDownloading() && downTask.getTag().equals(tag)) {
                    downTask.resumeDownload();
                }
            }
        }
    }

    // 取消进程中的所有下载和访问任务
    public void cancleAll() {

        for (Map.Entry<String, ArrayList<MioMultiResumeDownTask>> entry : downtaskMap.entrySet()) {
            ArrayList<MioMultiResumeDownTask> downTasks = entry.getValue();
            for (MioMultiResumeDownTask downTask : downTasks) {
                if (!downTask.isDownloading()) {
                    downTask.resumeDownload();
                }
            }
        }
    }
}
