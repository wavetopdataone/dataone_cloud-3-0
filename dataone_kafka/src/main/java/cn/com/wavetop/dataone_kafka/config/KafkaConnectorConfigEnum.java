package cn.com.wavetop.dataone_kafka.config;

public enum KafkaConnectorConfigEnum {
    JDBCSOURCECONNECTOR("io.confluent.connect.jdbc.JdbcSourceConnector"),
    JDBCSINKCONNECTOR("io.confluent.connect.jdbc.JdbcSinkConnector"),
    ORACLESINKCONNECTOR(""),
    ORACLESOURCECONNECTOR("");
    private String name;

    KafkaConnectorConfigEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

