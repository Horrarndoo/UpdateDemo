package com.zyw.horrarndoo.updatedemo.download;

import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public class DownloadHelper {
    private static String DIR_DOWNLOAD = "UpdateDemo";

    /**
     * 获取下载文件url中的文件名
     *
     * @param url 下载文件的url
     * @return 下载文件url中的文件名
     * 和path组成完整的file path
     */
    @NonNull
    public static String getUrlFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 获取文件下载父目录
     *
     * @return 文件下载父目录
     */
    public static File getDownloadParentFile() {
        File appDir = new File(Environment.getExternalStorageDirectory(), DIR_DOWNLOAD);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        return appDir;
    }

    /**
     * 获取下载文件父目录路径
     *
     * @return 下载文件父目录路径
     */
    @NonNull
    public static String getDownloadParentFilePath() {
        return getDownloadParentFile().getPath();
    }
}
