package com.zyw.horrarndoo.updatedemo.bean;

/**
 * Created by Horrarndoo on 2018/2/1.
 * <p>
 */

public class UpdateBean {
    private DataBean data;
    private String msg;
    private int status;

    public void setData(DataBean data) {
        this.data = data;
    }

    public DataBean getData() {
        return data;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "UpdateBean{" +
                "data=" + data +
                ", msg='" + msg + '\'' +
                ", status=" + status +
                '}';
    }
}
