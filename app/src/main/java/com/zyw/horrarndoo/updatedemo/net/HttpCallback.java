package com.zyw.horrarndoo.updatedemo.net;

public interface HttpCallback {

    /**
     * 请求完成
     * @param response 请求
     */
    void onFinish(String response);

    /**
     * 请求失败
     * @param e
     */
    void onError(Exception e);

}