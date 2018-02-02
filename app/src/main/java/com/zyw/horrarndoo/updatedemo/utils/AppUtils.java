package com.zyw.horrarndoo.updatedemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;

import com.zyw.horrarndoo.updatedemo.MyApplication;

import static android.R.attr.version;

/**
 * Created by Horrarndoo on 2017/4/5.
 * 系统界面工具类
 */
public class AppUtils {
    /**
     * 获取上下文对象
     *
     * @return
     */
    public static Context getContext() {
        return MyApplication.getContext();
    }

    /**
     * 获取全局handler
     *
     * @return
     */
    public static Handler getHandler() {
        return MyApplication.getHandler();
    }

    /**
     * 获取主线程id
     *
     * @return
     */
    public static int getMainThreadId() {
        return MyApplication.getMainThreadId();
    }

    ///////////////////加载资源文件 /////////////////////

    /**
     * 获取strings.xml资源文件字符串
     *
     * @param id 资源文件id
     * @return 资源文件对应字符串
     */
    public static String getString(int id) {
        return getContext().getResources().getString(id);
    }

    /**
     * 获取strings.xml资源文件字符串数组
     *
     * @param id 资源文件id
     * @return 资源文件对应字符串数组
     */
    public static String[] getStringArray(int id) {
        return getContext().getResources().getStringArray(id);
    }

    /**
     * 获取drawable资源文件图片
     *
     * @param id 资源文件id
     * @return 资源文件对应图片
     */
    public static Drawable getDrawable(@DrawableRes int id) {
        return getContext().getResources().getDrawable(id);
    }

    /**
     * 获取drawable资源文件图片bitmap
     *
     * @param id 资源文件id
     * @return 资源文件对应图片bitmap
     */
    public static Bitmap getBitmap(@DrawableRes int id) {
        return BitmapFactory.decodeResource(getContext().getResources(), id);
    }

    /**
     * 获取colors.xml资源文件颜色
     *
     * @param id 资源文件id
     * @return 资源文件对应颜色值
     */
    public static int getColor(@ColorRes int id) {
        return getContext().getResources().getColor(id);
    }

    /**
     * 获取颜色的状态选择器
     *
     * @param id 资源文件id
     * @return 资源文件对应颜色状态
     */
    public static ColorStateList getColorStateList(int id) {
        return getContext().getResources().getColorStateList(id);
    }

    /**
     * 获取dimens资源文件中具体像素值
     *
     * @param id 资源文件id
     * @return 资源文件对应像素值
     */
    public static int getDimen(int id) {
        return getContext().getResources().getDimensionPixelSize(id);// 返回具体像素值
    }

    /**
     * 加载布局文件
     *
     * @param id 布局文件id
     * @return 布局view
     */
    public static View inflate(int id) {
        return View.inflate(getContext(), id, null);
    }

    /**
     * 判断是否运行在主线程
     *
     * @return true：当前线程运行在主线程
     * fasle：当前线程没有运行在主线程
     */
    public static boolean isRunOnUIThread() {
        // 获取当前线程id, 如果当前线程id和主线程id相同, 那么当前就是主线程
        int myTid = android.os.Process.myTid();
        if (myTid == getMainThreadId()) {
            return true;
        }
        return false;
    }

    /**
     * 运行在主线程
     *
     * @param r 运行的Runnable对象
     */
    public static void runOnUIThread(Runnable r) {
        if (isRunOnUIThread()) {
            // 已经是主线程, 直接运行
            r.run();
        } else {
            // 如果是子线程, 借助handler让其运行在主线程
            getHandler().post(r);
        }
    }

    /**
     * 获取APP版本名
     */
    public static String getAppVersionName() {
        String version = null;
        try {
            PackageInfo info = getContext().getPackageManager().getPackageInfo
                    (getContext().getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取APP版本号
     */
    public static int getAppVersionCode() {
        int versionCode = 0;
        try {
            PackageInfo info = getContext().getPackageManager().getPackageInfo
                    (getContext().getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}
