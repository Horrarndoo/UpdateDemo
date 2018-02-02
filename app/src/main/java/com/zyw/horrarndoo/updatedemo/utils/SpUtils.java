package com.zyw.horrarndoo.updatedemo.utils;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences工具类封装
 */
public class SpUtils {
    private static final String SHARE_KEY_UPDATE_DEMO = "share_key_update_demo";
    private static SharedPreferences sp;

    /**
     * 写入boolean变量至sp中
     *
     * @param key   存储节点名称
     * @param value 存储节点的值
     */
    public static void putBoolean(String key, boolean value) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        sp.edit().putBoolean(key, value).apply();
    }

    /**
     * 读取boolean标示从sp中
     *
     * @param key      存储节点名称
     * @param defValue 没有此节点默认值
     * @return 默认值或者此节点读取到的结果
     */
    public static boolean getBoolean(String key, boolean defValue) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        return sp.getBoolean(key, defValue);
    }

    /**
     * 写入String变量至sp中
     *
     * @param key   存储节点名称
     * @param value 存储节点的值
     */
    public static void putString(String key, String value) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        sp.edit().putString(key, value).apply();
    }

    /**
     * 读取String标示从sp中
     *
     * @param key      存储节点名称
     * @param defValue 没有此节点默认值
     * @return 默认值或者此节点读取到的结果
     */
    public static String getString(String key, String defValue) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        return sp.getString(key, defValue);
    }


    /**
     * 写入int变量至sp中
     *
     * @param key   存储节点名称
     * @param value 存储节点的值
     */
    public static void putInt(String key, int value) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        sp.edit().putInt(key, value).apply();
    }

    /**
     * 读取long标示从sp中
     *
     * @param key      存储节点名称
     * @param defValue 没有此节点默认值
     * @return 默认值或者此节点读取到的结果
     */
    public static long getLong(String key, long defValue) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        return sp.getLong(key, defValue);
    }


    /**
     * 写入long变量至sp中
     *
     * @param key   存储节点名称
     * @param value 存储节点的值
     */
    public static void putLong(String key, long value) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        sp.edit().putLong(key, value).apply();
    }

    /**
     * 读取int标示从sp中
     *
     * @param key      存储节点名称
     * @param defValue 没有此节点默认值
     * @return 默认值或者此节点读取到的结果
     */
    public static int getInt(String key, int defValue) {
        //(存储节点文件名称,读写方式)
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        return sp.getInt(key, defValue);
    }

    /**
     * 从sp中移除指定节点
     *
     * @param key 需要移除节点的名称
     */
    public static void remove(String key) {
        if (sp == null) {
            sp = AppUtils.getContext().getSharedPreferences(SHARE_KEY_UPDATE_DEMO, Context
                    .MODE_PRIVATE);
        }
        sp.edit().remove(key).apply();
    }
}
