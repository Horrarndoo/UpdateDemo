# UpdateDemo
应用内更新demo

对于Android app来说，应用内更新几乎成了一个标配的功能了。原理其实不难，今天我们就从零开始撸一个自己的应用内更新的demo出来。

先看看最终实现的效果：

![效果图](https://raw.githubusercontent.com/Horrarndoo/imageAssets/master/updateDemo/demo.gif)

上图的效果，稍微将功能拆分一下，可以总结为以下几点。

1. 检查更新；
2. 最新apk下载；
3. apk下载成功后应用内跳转安装；
		
###1.检查更新

为了检验检查更新的效果，我们需要一个tomcat服务器。至于tomcat怎么搭建，这里就不花篇幅去讲了，网上资料还是很多的。
Tomcat部署完成后，在Tomcat ROOT目录上新建一个本次demo的目录，并且将新版的apk文件和一个保存了新版apk相关信息的json文件放在demo目录下。如下图所示：

![tomcat目录](https://raw.githubusercontent.com/Horrarndoo/imageAssets/master/updateDemo/tomcat.png)

各位应该已经想到检查更新的原理了，其实就是解析保存了新版apk信息的json文件，然后根据json中新版apk的版本信息来判断当前apk是否有可以更新。
我们这里模拟一个新版apk相关信息的json文件内容。

```
{"data":{"content":"更新内容如下：\n   1.xxxxxx;\n   2.xxxxxx;\n   3.xxxxxx;\n","id":"1","api_key":"android","version_code":"2","version_name":"1.0.2"},"msg":"获取成功","status":1}
```

可以看到，对于检查更新来说，最重要的几个信息都包含在data字段中，包括了更新内容，新版apk版本号，新版apk版本名称等。当然，根据实际需求，这个json可能会有所不同，具体项目中可以做一些修改，届时解析的时候稍作改动就好了。

接下来完成检查更新相关代码：

```
    /**
     * 检查更新
     */
    public void checkUpdate(OnCheckUpdateListener onCheckUpdateListener) {
        mOnCheckUpdateListener = onCheckUpdateListener;
        HttpUtils.sendOkHttpRequest(mUpdateHelper.getNewestApkVersionInfoUrl(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ToastUtils.showToast("check update failed.");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strJson = response.body().string();
                Log.e("onResponse", "response.body().string() = " + strJson);
                if (parseJson(strJson).getVersionCode() > AppUtils.getAppVersionCode()) {
                    sendMessage(MSG_ON_FIND_NEW_VERSION, parseJson(strJson));
                } else {
                    sendMessage(MSG_ON_NEWEST, null);
                }
            }
        });
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
```

###2.最新apk文件下载

如何检查更新的代码搞定了，接下来是稍微麻烦一点的，就是下载apk文件。
由于下载文件可以作为一个单独的功能，所以将下载文件这一块单独独立出来作为一个子模块来编写。其实谷歌官方有提供专门的DownloadManager来下载文件，但是很多国内的rom把DownloadManager阉割了。所以这里我们自己来撸一个DownloadManager，具体代码如下，注释还是比较清晰的，就不多做解释了。

DownloadManager.java：

```
public class DownloadManager {
    private static volatile DownloadManager manager = null;

    private DownloadTask downloadTask;

    private String mFileName;

    private String mFileParentPath;

    private DownloadManager() {
        mFileParentPath = DownloadHelper.getDownloadParentFilePath();
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

                @Override
                public void onCanceled() {
                    //下载任务取消，删除已下载的文件
                    clearCacheFile(getDownloadFilePath());
                }

                @Override
                public void onException() {
                    //下载任务异常，删除已下载的文件
                    clearCacheFile(getDownloadFilePath());
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
        }
    }

    /**
     * 清除下载的全部cache文件
     */
    public void clearAllCacheFile() {
        if (StringUtils.isEmpty(mFileParentPath))
            return;

        FileUtils.delAllFile(mFileParentPath);
    }

    /**
     * 清除下载的cache文件
     *
     * @param filePath 要删除文件的绝对路径
     */
    public void clearCacheFile(String filePath) {
        if (StringUtils.isEmpty(filePath))
            return;

        FileUtils.delFile(filePath);
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
```

DownloadTask.java

```
public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;

    private OnDownloadListener mOnDownloadListener;
    private OnDownloadTaskFinshedListener mOnDownloadTaskFinshedListener;

    private boolean isCanceled = false;

    private boolean isPaused = false;

    private int lastProgress;

    private File mDownloadFile = null;

    private long mContentLength; // 记录url下载文件的长度

    public DownloadTask(OnDownloadListener onDownloadListener) {
        mOnDownloadListener = onDownloadListener;
    }

    public void setOnDownloadTaskFinshedListener(OnDownloadTaskFinshedListener
                                                         onDownloadTaskFinshedListener) {
        mOnDownloadTaskFinshedListener = onDownloadTaskFinshedListener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        try {

            long downloadLength = 0; // 记录已下载的文件长度
            String downloadUrl = params[0];
            String fileParentPath = params[1];
            String fileName = params[2];
            mDownloadFile = new File(fileParentPath, fileName);
            if (mDownloadFile.exists()) {
                downloadLength = mDownloadFile.length();
            }

            mContentLength = getContentLength(downloadUrl);
            if (mContentLength == 0) {
                return TYPE_FAILED;
            } else if (mContentLength == downloadLength) {
                // 已下载字节和文件总字节相等，说明已经下载完成了
                return TYPE_SUCCESS;
            }
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    // 断点下载，指定从哪个字节开始下载
                    .addHeader("RANGE", "bytes=" + downloadLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(mDownloadFile, "rw");
                savedFile.seek(downloadLength); // 跳过已下载的字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);
                        // 计算已下载的百分比
                        int progress = (int) ((total + downloadLength) * 100 / mContentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && mDownloadFile != null) {
                    mDownloadFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            mOnDownloadListener.onProgress(progress);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                if (mContentLength != mDownloadFile.length()) {
                    if (mOnDownloadListener != null)
                        mOnDownloadListener.onException();

                    //下载数据异常，告知downManager下载任务已失败
                    if (mOnDownloadTaskFinshedListener != null)
                        mOnDownloadTaskFinshedListener.onException();
                } else {
                    if (mOnDownloadListener != null)
                        mOnDownloadListener.onSuccess();
                }
                break;
            case TYPE_FAILED:
                if (mOnDownloadListener != null)
                    mOnDownloadListener.onFailed();
                break;
            case TYPE_PAUSED:
                if (mOnDownloadListener != null)
                    mOnDownloadListener.onPaused();
                break;
            case TYPE_CANCELED:
                if (mOnDownloadListener != null)
                    mOnDownloadListener.onCanceled();

                if (mOnDownloadTaskFinshedListener != null)
                    mOnDownloadTaskFinshedListener.onCanceled();
            default:
                break;
        }

        if (mOnDownloadTaskFinshedListener != null)
            mOnDownloadTaskFinshedListener.onFinished();
    }

    /**
     * 暂停下载任务
     */
    public void pauseDownload() {
        isPaused = true;
    }

    /**
     * 取消下载任务
     */
    public void cancelDownload() {
        isCanceled = true;
    }

    /**
     * 获取下载文件长度
     * @param downloadUrl 下载文件url
     * @return 下载文件长度
     * @throws IOException IOException
     */
    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

    public interface OnDownloadTaskFinshedListener {
        /**
         * 下载任务已结束
         */
        void onFinished();

        /**
         * 下载任务已取消
         */
        void onCanceled();

        /**
         * 下载文件异常，不是完整的文件或者文件包异常
         */
        void onException();
    }
}
```

###3.apk下载成功后应用内跳转安装
在7.0以前，通过apk的uri跳转安装就可以了。7.0以后由于StrictMode API 政策，需要通过FileProvider来安装apk文件，使用步骤也比较简单。
		
#####第一步：在AndroidManifest.xml清单文件中注册provider

-  exported:必须为false，true会报安全异常。
-  grantUriPermissions:true，授予 URI 临时访问权限。
-  authorities 组件标识

```
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="${applicationId}.fileProvider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths"/>
</provider>
```

#####第二步：指定共享的目录
在res资源目录下新建一个xml目录，并新建一个名字和上一步指定的resource文件相同名字的xml文件，这里是：file_paths。 file_path.xml的内容如下：

```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path path="" name="updateDemo" />
</paths>
```

< files-path />代表根目录： Context.getFilesDir()
< external-path />代表根目录: Environment.getExternalStorageDirectory()
< cache-path />代表根目录: getCacheDir()
external-path path=""代表根目录，也就是说你可以向其它的应用共享根目录及其子目录下任何一个文件了。

#####第三步：使用FileProvider

```
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
                    .APPLICATION_ID + ".fileProvider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        AppUtils.getContext().startActivity(intent);
    }
```

接下来完成UpdateManager的全部代码：

```
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

    /**
     * 获取updateManager实例
     *
     * @return updateManager实例
     */
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
     *
     * @param apkInfoUrl            服务器端保存新版apk相关信息json的url
     * @param onCheckUpdateListener onCheckUpdateListener
     */
    public void checkUpdate(String apkInfoUrl, OnCheckUpdateListener onCheckUpdateListener) {
        mOnCheckUpdateListener = onCheckUpdateListener;
        HttpUtils.sendOkHttpRequest(apkInfoUrl, new Callback() {
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
     *
     * @param apkUrl           服务端最新apk文件url
     * @param onUpdateListener onUpdateListener
     */
    public void startToUpdate(String apkUrl, OnUpdateListener onUpdateListener) {
        mOnUpdateListener = onUpdateListener;

        if (StringUtils.isEmpty(mNewestVersionName) || mNewestVersionCode == 0)
            return;

        downloadNewestApkFile(apkUrl, mNewestVersionCode, mNewestVersionName);
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
        //保留下载已完成的部分apk cache文件，cache文件最多保留7天
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
     * @param url               服务端最新apk文件url
     * @param newestVersionCode 最新版本APK版本号
     * @param newestVersionName 最新版本APK版本名称
     */
    private void downloadNewestApkFile(String url, int newestVersionCode, String
            newestVersionName) {
        String apkFileName = getApkNameWithVersionName(DownloadHelper.getUrlFileName(url),
                newestVersionName);

        sendMessage(MSG_ON_START, null);

        mDownloadManager.startDownload(url, apkFileName, new
                OnDownloadListener() {
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
     * @return dataBean
     */
    private DataBean parseJson(String jsonData) {
        ...
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
        ...
    }
}
```

最后在Activity中调用UpdateManager就可以简单的检查更新我们的apk啦，不过有一点稍微要注意下，就是运行时权限的获取，如果6.0以上的手机没有获取sd卡权限的话，我们的程序是无法正常运行的，所以如果用户没有给予权限的话，就退出程序并给出提示。

```
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private UpdateManager mUpdateManager;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        ...

        initPermission();

        mUpdateManager = UpdateManager.getInstance();
    }

    private void initProgressDialog() {
        ...
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check_update:
                mUpdateManager.checkUpdate(Constant.VERSION_INFO_URL, new OnCheckUpdateListener() {
                    @Override
                    public void onFindNewVersion(String versionName, String newVersionContent) {
                        String content = "最新版: V" + versionName + "\n" + newVersionContent;
                        buildNewVersionDialog(content);
                    }

                    @Override
                    public void onNewest() {
                        showToast("app is newest version.");
                        dismissProgressDialog();
                    }
                });

                break;
            case R.id.btn_clear_apk:
                mUpdateManager.clearCacheApkFile();
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager
                        .PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private void dismissProgressDialog() {
        mProgressDialog.setProgress(0);
        if (mProgressDialog.isShowing())
            mProgressDialog.dismiss();
    }

    /**
     * 创建发现新版本apk alert dialog
     *
     * @param message dialog显示消息
     */
    private void buildNewVersionDialog(String message) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("发现新版本")
                .setIcon(R.mipmap.ic_launcher)
                .setMessage(message)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mUpdateManager.startToUpdate(Constant.APK_URL, mOnUpdateListener);
                    }
                })
                .setNegativeButton("取消", null)
                .create();
        dialog.show();
    }

    private OnUpdateListener mOnUpdateListener = new OnUpdateListener() {
        @Override
        public void onStartUpdate() {
            mProgressDialog.show();
        }

        @Override
        public void onProgress(int progress) {
            mProgressDialog.setProgress(progress);
        }

        @Override
        public void onApkDownloadFinish(String apkPath) {
            showToast("newest apk download finish. apkPath: " + apkPath);
            Log.e("tag", "newest apk download finish. apkPath: " + apkPath);
            dismissProgressDialog();
            //所有的更新全部在updateManager中完成，Activity在这里只是做一些界面上的处理
        }

        @Override
        public void onUpdateFailed() {
            showToast("update failed.");
            dismissProgressDialog();
        }

        @Override
        public void onUpdateCanceled() {
            showToast("update cancled.");
            dismissProgressDialog();
        }

        @Override
        public void onUpdateException() {
            showToast("update exception.");
            dismissProgressDialog();
        }
    };
}
```

至此已经基本完成此次的应用内更新模块了，用起来还是很简单的，UpdateManager只做更新相关判断处理，DownloadManager则处理下载文件、缓存文件续传及相关状态处理，并且UpdateManager和DownloadManager都是单独可以作为一个独立模块在实际项目中使用的。

最后附上源码：[https://github.com/Horrarndoo/UpdateDemo](https://github.com/Horrarndoo/UpdateDemo)
