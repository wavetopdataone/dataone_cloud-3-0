package cn.com.wavetop.dataone_kafka.utils;

public enum HttpEnum {
    HTTP("http://"),
    HTTP_GET("get"),
    HTTP_POST("post"),
    HTTP_PUT("put"),
    HTTP_DELETE("delete"),
    HTTP_HEAD("head"),
    HTTP_TRACE("trace"),
    HTTP_OPINIONS("opinions"),
    COLON(":"),
    BACKSLASH("/");

    HttpEnum(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}