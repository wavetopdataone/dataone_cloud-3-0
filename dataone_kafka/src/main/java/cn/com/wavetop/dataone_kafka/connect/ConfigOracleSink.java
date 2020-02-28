package cn.com.wavetop.dataone_kafka.connect;

import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import cn.com.wavetop.dataone_kafka.utils.JSONUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2019/12/10、16:53
 */
public class ConfigOracleSink {
    private String name; // sink名
    private String connector_class = "io.confluent.connect.jdbc.JdbcSinkConnector";
    private String topics;
    private String tasks_max = "1";
    private String auto_create = "true";
    private String auto_evolve = "false";
    private String connection_url = "jdbc:oracle:thin:@192.168.103.238:1521:orcl";
    private String connection_user = "test2";
    private String connection_password = "test2";
    private String insert_mode = "insert";
    private String table_name_format = "TEST1.EMPLOYEES";
    private String errors_tolerance = "all";
    private String errors_deadletterqueue_topic_name = "error-logs";
    private int errors_deadletterqueue_topic_replication_factor = 1;
    private boolean errors_deadletterqueue_context_headers_enable = true;
    private boolean errors_log_enable = true;
    private boolean errors_log_include_messages = true;

    public ConfigOracleSink(long jobId, String destTable, SysDbinfo sysDbinfo) {
        this.name = "oracle-sink-" + jobId + "-" + destTable;
        this.topics = "task-" + jobId + "-" + destTable;
        this.table_name_format = destTable;
        this.errors_deadletterqueue_topic_name = "error-logs-" + jobId + "-" + destTable;
        this.connection_url = "jdbc:oracle:thin:@" + sysDbinfo.getHost() + ":" + sysDbinfo.getPort() + ":orcl";
        this.connection_user = sysDbinfo.getUser();
        this.connection_password = sysDbinfo.getPassword();

    }

    public String toJsonConfig() {
        Map<String, Object> name = new HashMap<>();
        Map<String, Object> config = new HashMap<>();
        name.put("name", this.name);
        name.put("config", config);
        config.put("connector.class", this.connector_class);
        config.put("tasks.max", this.tasks_max);
        config.put("topics", this.topics);
        config.put("auto.create", this.auto_create);
        config.put("auto.evolve", this.auto_evolve);
        config.put("connection.url", this.connection_url);
        config.put("connection.user", this.connection_user);
        config.put("connection.password", this.connection_password);
        config.put("insert.mode", this.insert_mode);
        config.put("table.name.format", this.table_name_format);
        config.put("errors.tolerance", this.errors_tolerance);
        config.put("errors.deadletterqueue.topic.name", this.errors_deadletterqueue_topic_name);
        config.put("errors.deadletterqueue.topic.replication.factor", this.errors_deadletterqueue_topic_replication_factor);
        config.put("errors.deadletterqueue.context.headers.enable", this.errors_deadletterqueue_context_headers_enable);
        config.put("errors.log.enable", this.errors_log_enable);
        config.put("errors.log.include.messages", this.errors_log_include_messages);


        String data = JSONUtil.toJSONString(name);
        config.clear();
        name.clear();
        name = null;
        config = null;
        return data;
    }
}
