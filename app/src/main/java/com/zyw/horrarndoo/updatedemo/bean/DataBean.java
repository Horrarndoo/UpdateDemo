package com.zyw.horrarndoo.updatedemo.bean;

/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public class DataBean {
    private String content;
    private int id;
    private String api_key;
    private int version_code;
    private String version_name;

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setApiKey(String api_key) {
        this.api_key = api_key;
    }

    public String getApiKey() {
        return api_key;
    }

    public void setVersionCode(int version_code) {
        this.version_code = version_code;
    }

    public int getVersionCode() {
        return version_code;
    }

    public String getVersionName() {
        return version_name;
    }

    public void setVersionName(String version_name) {
        this.version_name = version_name;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "content='" + content + '\'' +
                ", id=" + id +
                ", api_key='" + api_key + '\'' +
                ", version_code=" + version_code +
                ", version_name='" + version_name + '\'' +
                '}';
    }
}
