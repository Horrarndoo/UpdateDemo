package com.zyw.horrarndoo.updatedemo.update;

/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public interface OnUpdateListener {
    /**
     * 开始更新
     */
    void onStartUpdate();

    /**
     * 更新进度变化
     *
     * @param progress 当前更新进度
     */
    void onProgress(int progress);

    /**
     * 新的Apk下载完成
     *
     * @param apkPath apk 全路径
     */
    void onApkDownloadFinish(String apkPath);

    /**
     * 更新失败
     */
    void onUpdateFailed();

    /**
     * 更新已取消
     */
    void onUpdateCanceled();
}
