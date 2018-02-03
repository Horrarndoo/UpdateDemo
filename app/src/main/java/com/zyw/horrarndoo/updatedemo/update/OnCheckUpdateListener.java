package com.zyw.horrarndoo.updatedemo.update;

/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public interface OnCheckUpdateListener {
    /**
     * 发现新版本
     *
     * @param versionName       新版Apk版本名称
     * @param newVersionContent 新版Apk更新内容
     */
    void onFindNewVersion(String versionName, String newVersionContent);

    /**
     * 当前版本已是最新版本
     */
    void onNewest();
}
