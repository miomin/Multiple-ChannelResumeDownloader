package scu.miomin.com.multiple_channelresumedownloader;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by 莫绪旻 on 16/2/20.
 */
public class DownHelper {

    // 获取需要下载的文件的长度
    public static int getFileLength(String fileUrl, Context context) {
        int length = -1;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            // 伪装成浏览器
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
            length = connection.getContentLength();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(context, "URL不正确", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }
}
