package com.zyw.horrarndoo.updatedemo.download;

import android.support.annotation.NonNull;

import com.zyw.horrarndoo.updatedemo.utils.StringUtils;

import java.io.File;

/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public class DownloadManager {
    private static volatile DownloadManager manager = null;

    private DownloadTask downloadTask;

    private String mFileName;

    private String mFileParentPath;

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (manager == null) {
            synchronized (DownloadManager.class) {
                if (manager == null) {
                    manager = new DownloadManager();
                }
            }
        }
        return manager;
    }

    /**
     * 开启下载任务
     *
     * @param url                下载链接
     * @param onDownloadListener onDownloadListener
     */
    public void startDownload(String url, OnDownloadListener onDownloadListener) {
        startDownload(url, DownloadHelper.getDownloadParentFilePath(), DownloadHelper
                        .getUrlFileName(url)
                , onDownloadListener);
    }

    /**
     * 开启下载任务
     *
     * @param url                下载链接
     * @param fileName           指定下载文件名
     * @param onDownloadListener onDownloadListener
     */
    public void startDownload(String url, @NonNull String fileName, OnDownloadListener
            onDownloadListener) {
        startDownload(url, DownloadHelper.getDownloadParentFilePath(), fileName,
                onDownloadListener);
    }

    /**
     * 开启下载任务
     *
     * @param url                下载链接
     * @param fileParentPath     指定下载文件目录
     * @param fileName           指定下载文件名
     * @param onDownloadListener onDownloadListener
     */
    public void startDownload(String url, @NonNull String fileParentPath, @NonNull String fileName,
                              OnDownloadListener onDownloadListener) {
        if (StringUtils.isEmpty(fileParentPath))
            fileParentPath = DownloadHelper.getDownloadParentFilePath();

        if (StringUtils.isEmpty(fileName))
            fileName = DownloadHelper.getUrlFileName(url);

        mFileParentPath = fileParentPath;
        mFileName = fileName;

        if (downloadTask == null) {
            downloadTask = new DownloadTask(onDownloadListener);
            downloadTask.execute(url, fileParentPath, fileName);
            downloadTask.setOnDownloadTaskFinshedListener(new DownloadTask
                    .OnDownloadTaskFinshedListener() {
                @Override
                public void onFinished() {
                    downloadTask = null;
                }
            });
        }
    }

    /**
     * 暂停下载任务
     */
    public void pauseDownload() {
        if (downloadTask != null) {
            downloadTask.pauseDownload();
        }
    }

    /**
     * 取消下载任务
     */
    public void cancelDownload() {
        if (downloadTask != null) {
            downloadTask.cancelDownload();
        } else {
            // 取消下载时将文件删除
            clearCacheFile();
        }
    }

    /**
     * 清除下载的cache文件
     */
    public void clearCacheFile() {
        if (StringUtils.isEmpty(mFileParentPath) || StringUtils.isEmpty(mFileName))
            return;

        File file = new File(mFileParentPath, mFileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取下载文件的全路径
     *
     * @return 下载文件的全路径
     */
    public String getDownloadFilePath() {
        if (StringUtils.isEmpty(mFileParentPath) || StringUtils.isEmpty(mFileName))
            return null;

        return mFileParentPath + "/" + mFileName;
    }
}
