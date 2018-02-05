package com.zyw.horrarndoo.updatedemo.update;

import android.support.annotation.NonNull;

/**
 * Created by Horrarndoo on 2018/2/5.
 * <p>
 * update url接口
 */

public interface IUpdateHelper {
    /**
     * 获取新版Apk json info url
     *
     * @return 新版Apk info url
     */
    @NonNull
    String getNewestApkVersionInfoUrl();

    /**
     * 获取新版Apk下载url
     *
     * @return 新版Apk下载url
     */
    @NonNull
    String getNewestApkUrl();
}
