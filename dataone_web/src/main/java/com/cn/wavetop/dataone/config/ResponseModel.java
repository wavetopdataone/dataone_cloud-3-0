package com.cn.wavetop.dataone.config;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseModel implements Serializable {
    private boolean status;
    private String msg;
    private Object data;

    public ResponseModel() {
    }

    public static ResponseModel error() {
        return new ResponseModel(false, null, null);
    }

    public static ResponseModel error(String msg) {
        return new ResponseModel(false, msg, null);
    }

    public static ResponseModel success() {
        return new ResponseModel(true, null, null);
    }

    public static ResponseModel success(String msg) {
        return new ResponseModel(true, msg, null);
    }

    public static ResponseModel success(Object data) {
        return new ResponseModel(true, null, JSON.toJSONString(data));
    }

    public static ResponseModel success(String msg, Object data) {
        return new ResponseModel(true, msg, JSON.toJSONString(data));
    }

    public ResponseModel(boolean status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

}
