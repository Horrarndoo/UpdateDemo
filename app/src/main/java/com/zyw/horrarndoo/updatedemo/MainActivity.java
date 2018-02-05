package com.zyw.horrarndoo.updatedemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zyw.horrarndoo.updatedemo.constant.Constant;
import com.zyw.horrarndoo.updatedemo.update.IUpdateHelper;
import com.zyw.horrarndoo.updatedemo.update.OnCheckUpdateListener;
import com.zyw.horrarndoo.updatedemo.update.OnUpdateListener;
import com.zyw.horrarndoo.updatedemo.update.UpdateManager;
import com.zyw.horrarndoo.updatedemo.utils.AppUtils;

import static com.zyw.horrarndoo.updatedemo.utils.ToastUtils.showToast;

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
        TextView tvVersionName = (TextView) findViewById(R.id.tv_version_name);
        Button btnCheckUpdate = (Button) findViewById(R.id.btn_check_update);
        Button btnClearApk = (Button) findViewById(R.id.btn_clear_apk);

        btnCheckUpdate.setOnClickListener(this);
        btnClearApk.setOnClickListener(this);
        tvVersionName.setText(AppUtils.getAppVersionName());

        initProgressDialog();

        initPermission();

        mUpdateManager = UpdateManager.getInstance(new IUpdateHelper() {
            @NonNull
            @Override
            public String getNewestApkVersionInfoUrl() {
                return Constant.VERSION_INFO_URL;
            }

            @NonNull
            @Override
            public String getNewestApkUrl() {
                return Constant.APK_URL;
            }
        });
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this, R.style.DialogTheme);
        mProgressDialog.setTitle("UpdateDemo");
        mProgressDialog.setMessage("Downloading, Please wait...");
        mProgressDialog.setMax(100);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mUpdateManager.cancleUpdate();
            }
        });
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
                mUpdateManager.checkUpdate(new OnCheckUpdateListener() {
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
                        mUpdateManager.startToUpdate(mOnUpdateListener);
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
