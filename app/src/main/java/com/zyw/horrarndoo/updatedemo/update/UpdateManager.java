package com.zyw.horrarndoo.updatedemo.update;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.zyw.horrarndoo.updatedemo.BuildConfig;
import com.zyw.horrarndoo.updatedemo.bean.DataBean;
import com.zyw.horrarndoo.updatedemo.constant.Constant;
import com.zyw.horrarndoo.updatedemo.download.DownloadHelper;
import com.zyw.horrarndoo.updatedemo.download.DownloadManager;
import com.zyw.horrarndoo.updatedemo.download.OnDownloadListener;
import com.zyw.horrarndoo.updatedemo.net.HttpUtils;
import com.zyw.horrarndoo.updatedemo.utils.AppUtils;
import com.zyw.horrarndoo.updatedemo.utils.SpUtils;
import com.zyw.horrarndoo.updatedemo.utils.StringUtils;
import com.zyw.horrarndoo.updatedemo.utils.ToastUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zyw.horrarndoo.updatedemo.constant.Constant.SP_KEY_CACHE_APK_VERSION_CODE;
import static com.zyw.horrarndoo.updatedemo.constant.Constant.SP_KEY_CACHE_VALID_TIME;


/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public class UpdateManager {
    private static volatile UpdateManager manager = null;

    private static final int MSG_ON_START = 1;
    private static final int MSG_ON_PROGRESS = 2;
    private static final int MSG_ON_DOWNLOAD_FINISH = 3;
    private static final int MSG_ON_FAILED = 4;
    private static final int MSG_ON_CANCLE = 5;
    private static final int MSG_ON_FIND_NEW_VERSION = 6;
    private static final int MSG_ON_NEWEST = 7;
    private static final int MSG_ON_UPDATE_EXCEPTION = 8;

    private DownloadManager mDownloadManager;

    private OnUpdateListener mOnUpdateListener;
    private OnCheckUpdateListener mOnCheckUpdateListener;

    private int mNewestVersionCode;
    private String mNewestVersionName;
    private String mNewVersionContent;

    /**
     * 最后一次保存cache的时间
     */
    private long mLastCacheSaveTime = 0;

    private UpdateManager() {
        mDownloadManager = DownloadManager.getInstance();
    }

    public static UpdateManager getInstance() {
        if (manager == null) {
            synchronized (UpdateManager.class) {
                if (manager == null) {
                    manager = new UpdateManager();
                }
            }
        }
        return manager;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ON_START:
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onStartUpdate();
                    break;

                case MSG_ON_PROGRESS:
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onProgress((Integer) msg.obj);
                    break;

                case MSG_ON_DOWNLOAD_FINISH:
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onApkDownloadFinish((String) msg.obj);
                    installApk((String) msg.obj);
                    break;

                case MSG_ON_FAILED:
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onUpdateFailed();
                    break;

                case MSG_ON_CANCLE:
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onUpdateCanceled();
                    break;

                case MSG_ON_UPDATE_EXCEPTION:
                    if (mOnUpdateListener != null)
                        mOnUpdateListener.onUpdateException();
                    break;

                case MSG_ON_FIND_NEW_VERSION:
                    DataBean dataBean = (DataBean) msg.obj;

                    mNewestVersionCode = dataBean.getVersionCode();
                    mNewestVersionName = dataBean.getVersionName();
                    mNewVersionContent = dataBean.getContent();

                    if (mOnCheckUpdateListener != null)
                        mOnCheckUpdateListener.onFindNewVersion(mNewestVersionName,
                                mNewVersionContent);
                    break;

                case MSG_ON_NEWEST:
                    if (mOnCheckUpdateListener != null)
                        mOnCheckUpdateListener.onNewest();
                    break;
            }
        }
    };

    /**
     * 检查更新
     */
    public void checkUpdate(OnCheckUpdateListener onCheckUpdateListener) {
        mOnCheckUpdateListener = onCheckUpdateListener;
        HttpUtils.sendOkHttpRequest(Constant.VERSION_INFO_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showToast("check update failed.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strJson = response.body().string();
                Log.e("onResponse", "response.body().string() = " + strJson);
                if (parseJson(strJson).getVersionCode() > AppUtils.getAppVersionCode()) {
                    //最后一次缓存的时间超过缓存文件有效期，或者最后一次缓存的apk不是最新版本的apk，删除缓存apk
                    if ((System.currentTimeMillis() - mLastCacheSaveTime > getCacheSaveValidTime())
                            || (getCacheApkVersionCode() != parseJson(strJson).getVersionCode())) {
                        clearCacheApkFile();
                        setCacheApkVersionCode(parseJson(strJson).getVersionCode());
                    }
                    sendMessage(MSG_ON_FIND_NEW_VERSION, parseJson(strJson));
                } else {
                    sendMessage(MSG_ON_NEWEST, null);
                    //当前已经是最新版本APK，清除本地已经缓存的apk安装包
                    clearCacheApkFile();
                }
            }
        });
    }

    /**
     * 开始更新App
     * <p>
     * 此时开始正式下载更新Apk
     */
    public void startToUpdate(OnUpdateListener onUpdateListener) {
        mOnUpdateListener = onUpdateListener;

        if (StringUtils.isEmpty(mNewestVersionName) || mNewestVersionCode == 0)
            return;

        downloadNewestApkFile(mNewestVersionCode, mNewestVersionName);
    }

    /**
     * 设置缓存文件有效时间，单位：秒
     * <p>
     * 默认缓存有效期为7天
     *
     * @param cacheValidTime 缓存文件有效时间
     */
    public void setCacheSaveValidTime(long cacheValidTime) {
        SpUtils.putLong(SP_KEY_CACHE_VALID_TIME, cacheValidTime);
    }

    /**
     * 获取缓存文件有效时间，单位：秒
     * <p>
     * 默认缓存有效期为7天
     *
     * @return 缓存文件有效时间
     */
    public long getCacheSaveValidTime() {
        return SpUtils.getLong(SP_KEY_CACHE_VALID_TIME, 60 * 60 * 24 * 7);
    }

    /**
     * 设置缓存文件版本号
     *
     * @param versionCode cacheApk版本号
     */
    public void setCacheApkVersionCode(int versionCode) {
        SpUtils.putInt(SP_KEY_CACHE_APK_VERSION_CODE, versionCode);
    }

    /**
     * 获取缓存文件版本号
     *
     * @return 缓存文件版本号
     */
    public int getCacheApkVersionCode() {
        return SpUtils.getInt(SP_KEY_CACHE_APK_VERSION_CODE, 0);
    }

    /**
     * 取消更新
     */
    public void cancleUpdate() {
        //保留下载已完成的部分apk cache文件，cache文件最多保留1天
        mDownloadManager.pauseDownload();
    }

    /**
     * 清除已下载的APK缓存
     */
    public void clearCacheApkFile() {
        Log.e("tag", "清除所有的apk文件");
        mDownloadManager.clearAllCacheFile();
    }

    /**
     * 下载最新版本的APK文件
     *
     * @param newestVersionCode 最新版本APK版本号
     * @param newestVersionName 最新版本APK版本名称
     */
    private void downloadNewestApkFile(int newestVersionCode, String newestVersionName) {
        String apkFileName = getApkNameWithVersionName(DownloadHelper.getUrlFileName(Constant
                .APK_URL), newestVersionName);

        sendMessage(MSG_ON_START, null);

        mDownloadManager.startDownload(Constant.APK_URL, apkFileName, new OnDownloadListener() {
            @Override
            public void onException() {
                sendMessage(MSG_ON_UPDATE_EXCEPTION, null);
            }

            @Override
            public void onProgress(int progress) {
                sendMessage(MSG_ON_PROGRESS, progress);
            }

            @Override
            public void onSuccess() {
                mLastCacheSaveTime = System.currentTimeMillis();
                sendMessage(MSG_ON_DOWNLOAD_FINISH, mDownloadManager.getDownloadFilePath());
            }

            @Override
            public void onFailed() {
                mLastCacheSaveTime = System.currentTimeMillis();
                sendMessage(MSG_ON_FAILED, null);
            }

            @Override
            public void onPaused() {
                mLastCacheSaveTime = System.currentTimeMillis();
                //取消升级时，调用download pause，保留已下载的部分apk文件
                sendMessage(MSG_ON_CANCLE, null);
            }

            @Override
            public void onCanceled() {
                //为了保证断点续传，升级时，调用download pause，不使用cancle，onCancle不会被调用
                mLastCacheSaveTime = System.currentTimeMillis();
                sendMessage(MSG_ON_CANCLE, null);
            }
        });
    }

    private void sendMessage(int msgWhat, Object o) {
        Message msg = Message.obtain();
        msg.what = msgWhat;
        msg.obj = o;
        mHandler.sendMessage(msg);
    }

    /**
     * 解析json数据
     *
     * @param jsonData json数据
     *                 {
     *                 "data": {
     *                 "content": "更新内容如下：1.xxxxxx;/n 2.xxxxxx;/n 3.xxxxxx;/n",
     *                 "id": "1",
     *                 "api_key": "update test",
     *                 "version_code": "2"
     *                 },
     *                 "msg": "获取成功",
     *                 "status": 1
     *                 }
     * @return dataBean
     */
    private DataBean parseJson(String jsonData) {
        DataBean dataBean = new DataBean();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject dataObject = jsonObject.getJSONObject("data");
            dataBean.setContent(dataObject.getString("content"));
            dataBean.setId(dataObject.getInt("id"));
            dataBean.setApiKey(dataObject.getString("api_key"));
            dataBean.setVersionCode(dataObject.getInt("version_code"));
            dataBean.setVersionName(dataObject.getString("version_name"));
            //            Log.e("parseJson", "content " + dataObject.getString("content"));
            //            Log.e("parseJson", "id " + dataObject.getInt("id"));
            //            Log.e("parseJson", "api_key " + dataObject.getString("api_key"));
            //            Log.e("parseJson", "version_code " + dataObject.getInt("version_code"));
            //            Log.e("parseJson", "version_name " + dataObject.getString
            // ("version_name"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataBean;
    }

    /**
     * 获取带版本名称的apk文件名
     *
     * @param apkName apk原名
     * @return 带版本名称的apk文件名
     */
    private String getApkNameWithVersionName(String apkName, String versionName) {
        if (StringUtils.isEmpty(apkName))
            return apkName;

        apkName = apkName.substring(apkName.lastIndexOf("/") + 1, apkName.indexOf("" +
                ".apk"));
        Log.e("tag", "newApkName = " + apkName + "_v" + versionName + ".apk");
        return apkName + "_v" + versionName + ".apk";
    }

    /**
     * 安装 apk
     *
     * @param apkPath apk全路径
     */
    public void installApk(String apkPath) {
        if (StringUtils.isEmpty(apkPath)) {
            Log.e("tag", "apkPath is null.");
            return;
        }

        File file = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) { //判断版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致  参数3 共享的文件
            Uri apkUri = FileProvider.getUriForFile(AppUtils.getContext(), BuildConfig
                    .APPLICATION_ID +
                    ".fileProvider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        AppUtils.getContext().startActivity(intent);
    }
}
