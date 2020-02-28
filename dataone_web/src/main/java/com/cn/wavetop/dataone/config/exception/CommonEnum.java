package com.cn.wavetop.dataone.config.exception;

public enum  CommonEnum implements BaseErrorInfoInterface {


    // 数据操作错误定义
    SUCCESS("200", "成功!"),
    BODY_NOT_MATCH("400", "请求的数据格式不符!"),
    NULL_NOT_MATCH("500","参数不匹配或为空"),
    SIGNATURE_NOT_MATCH("4001", "请求的数字签名不匹配!"),
    NOT_FOUND("404", "未找到该资源!"),
    INTERNAL_SERVER_ERROR("500", "服务器内部错误!"),
    SERVER_BUSY("503", "服务器正忙，请稍后再试!"),
    REQUEST_METHOD_SUPPORT_ERROR("40001","当前请求方法不支持"),
    TYPE_NOT_ERROR("500","类型转换错误"),
    NOT_FOUND_FILE("404","系统找不到指定文件"),
    NUMBER_FROMAT_ERROR("500","接口参数转化异常"),
    PARAMS_NOT_ERROR("500","入参和形参不匹配");



    /**
     * 错误码
     */
    private String resultCode;
    /**
     * 错误描述
     */
    private String resultMsg;

    CommonEnum(String resultCode, String resultMsg) {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    @Override
    public String getResultCode() {
        return resultCode;
    }

    @Override
    public String getResultMsg() {
        return resultMsg;
    }

}
