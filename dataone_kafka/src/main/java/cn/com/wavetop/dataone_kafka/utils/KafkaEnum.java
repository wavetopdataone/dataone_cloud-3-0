package cn.com.wavetop.dataone_kafka.utils;

public enum KafkaEnum {

    KAFKA_DEFAULTTERM("connectors"),
    KAFKA_TASKS("tasks"),
    KAFKA_STATUS("status"),
    KAFKA_CONFIG("config"),
    KAFKA_PAUSE("pause"),
    KAFKA_RESUME("resume"),
    KAFKA_RESTART("restart");
    private String value;

    KafkaEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}