package cn.com.wavetop.dataone_kafka.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

/**
 * @Author yongz
 * @Date 2019/10/27、19:36
 * kafka connect 通用工具类
 */
public class HttpClientKafkaUtil {
    private static CloseableHttpClient httpClient = null;

    public static void main(String[] args) {
        getConnectRestart("192.168.1.156",8083,"connect-sink-225-file2_148");
    }
    static {
        httpClient = HttpClients.createDefault();
    }


    /**
     * 更新 connectors
     * 格式
     * {"connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
     * "tasks.max": "1",
     * "connection.url": "jdbc:mysql://192.168.0.119:3306/testo?user=root&password=**",
     * "topics": "mysql-kafka-person",
     * "auto.create": "false",
     * "insert.mode" : "upsert",
     * "pk.mode":"record_value",
     * "pk.fields":"pid",
     * "table.name.format":"kafkaperson"}
     * 更新connector配置
     * data 参考 createConnector
     */
    public static String updateConnector(String ip, int port, String updateData, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_CONFIG.getValue();
        Header header = new BasicHeader("Content-Type", "application/json");
        StringEntity stringEntity = new StringEntity(updateData, Charset.forName("UTF-8"));
        String detail = httpclientGetExecute1(uri, stringEntity, header);
        return detail;
    }


    /**
     * 重启connectors ..
     */
    public static String getConnectRestart(String ip, int port, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_RESTART.getValue();
        System.out.println(uri);
        return httpclientGetExecute(uri, HttpEnum.HTTP_POST.getValue(), null);
    }

    /**
     * 新建一个connector
     *
     * @param ip
     * @param port
     * @param data 数据 这里指connect 配置
     *             demo
     *             {
     *             "name": "mysql-a-source-person",                      connect的名字
     *             "config": {                                           conncet的配置
     *             "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
     *             "tasks.max": "1",
     *             "connection.url": "jdbc:mysql://192.168.0.119:3306/test?user=root&password=***",
     *             "mode": "incrementing",
     *             "incrementing.column.name": "pid",
     *             "table.whitelist" : "person",
     *             "topic.prefix":"mysql-kafka-"}}'
     * @return
     */
    public static String createConnector(String ip, int port, String data) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue();
        System.out.println(uri);
        Header header = new BasicHeader("Content-Type", "application/json");
        StringEntity stringEntity = new StringEntity(data, Charset.forName("UTF-8"));
        String detail = httpclientGetExecute(uri, stringEntity, header);
        return detail;
    }


    /**
     * 重启 Connector 未测
     *
     * @param ip
     * @param port
     * @param connectorName
     * @return
     */
    public static String getConnectResume(String ip, int port, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_RESUME.getValue();
        return httpclientGetExecute(uri, HttpEnum.HTTP_PUT.getValue(), null);
    }

    /**
     * 暂停 Connector   未测
     *
     * @param ip
     * @param port
     * @param connectorName
     * @return
     */
    public static String getConnectPause(String ip, int port, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_PAUSE.getValue();
        System.out.println(uri);
        return httpclientGetExecute(uri, HttpEnum.HTTP_PUT.getValue(), null);
    }

    /**
     * 获取 Connector 配置信息 ..
     *
     * @param ip
     * @param port
     * @param connectorName
     * @return
     */
    public static String getConnectConfig(String ip, int port, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_CONFIG.getValue();
        System.out.println(uri);
        return httpclientGetExecute(uri, HttpEnum.HTTP_GET.getValue(), null);
    }


    /**
     * 获取 Connector 状态信息 ..
     *
     * @param ip
     * @param port
     * @param connectorName
     * @return
     */
    public static String getConnectStatus(String ip, int port, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_STATUS.getValue();
        System.out.println(uri);
        return httpclientGetExecute(uri, HttpEnum.HTTP_GET.getValue(), null);
    }


    /**
     * 获取 Connector 上 Task 以及相关配置的信息 ..
     *
     * @param ip
     * @param port
     * @param connectorName
     * @return
     */
    public static String getConnectTask(String ip, int port, String connectorName) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_TASKS.getValue();
        return httpclientGetExecute(uri, HttpEnum.HTTP_GET.getValue(), null);
    }


    /**
     * 获取 Connect Worker 信息 ..
     *
     * @param ip
     * @param port
     * @return
     */
    public static String getConnectWorkerDetails(String ip, int port) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port;
        System.out.println(uri);
        // 创建http GET请求
        return httpclientGetExecute(uri, HttpEnum.HTTP_GET.getValue(), null);
    }


    /**
     * 列出 Connect Worker 上所有 Connector  ..
     *
     * @param ip
     * @param port
     * @return
     */
    public static String getConnectorsDetails(String ip, int port) {
        String uri = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue();
        System.out.println(uri);
        //  Header header = new BasicHeader("Content-Type","application/json");
        // Header header1 = new BasicHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
      /*
       Header header2 = new BasicHeader("Accept-Encoding","gzip, deflate");
        Header header3 = new BasicHeader("Accept-Language","zh-CN,zh;q=0.9");
        Header header4 = new BasicHeader("Cache-Control","max-age=0");
        Header header5 = new BasicHeader("Connection","keep-alive");
        Header header6 = new BasicHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        Header header7 = new BasicHeader("Upgrade-Insecure-Requests","1");
        */
        return httpclientGetExecute(uri, HttpEnum.HTTP_GET.getValue(), null);
    }


    /**
     * 删除运行中的connectors  ..
     *
     * @param ip
     * @param port
     * @param connectorName connectors name
     * @return
     */
    public static String deleteConnectors(String ip, int port, String connectorName) {
        String url = HttpEnum.HTTP.getValue() + ip + HttpEnum.COLON.getValue() + port + HttpEnum.BACKSLASH.getValue() + KafkaEnum.KAFKA_DEFAULTTERM.getValue() + HttpEnum.BACKSLASH.getValue() + connectorName;
        // 创建Http Post请求
        HttpDelete httpDelete = new HttpDelete(url);
        // 执行http请求
        try (CloseableHttpResponse response = httpClient.execute(httpDelete)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return "";
            }
            return EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 关闭response
     *
     * @param response
     */
    public static void closeCloseableHttpResponse(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param uri
     * @param type   请求类型 get post put .....
     * @param header 请求头
     * @return
     */
    public static String httpclientGetExecute(String uri, String type, Header... header) {
        HttpUriRequest http = new HttpRequestBase() {
            @Override
            public String getMethod() {
                return type.toUpperCase();
            }
        };
        ((HttpRequestBase) http).setURI(URI.create(uri));
        if (header != null) {
            for (Header h : header) {
                http.addHeader(h);
            }
        }


        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(http)) {
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return "";
            }
            return EntityUtils.toString(entity, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 新建 connectors
     *
     * @param uri
     * @param header 请求头 只支持post
     * @return
     */
    public static String httpclientGetExecute(String uri, StringEntity stringEntity, Header... header) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.setURI(URI.create(uri));
        httpPost.setEntity(stringEntity);
        //HttpRequestBase httpRequestBase = ((HttpRequestBase) http);
        //((HttpEntityEnclosingRequestBase)http).setEntity(stringEntity);

        if (header != null) {
            for (Header h : header) {
                httpPost.addHeader(h);
            }
        }
        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return EntityUtils.toString(response.getEntity(), "utf-8");
        }catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 更新 connectors
     * 格式
     *
     * @param uri
     * @param header 请求头 只支持put
     * @return
     */
    public static String httpclientGetExecute1(String uri, StringEntity stringEntity, Header... header) {
        HttpPut httpPut = new HttpPut(uri);
        System.out.println(uri);
        httpPut.setURI(URI.create(uri));
        httpPut.setEntity(stringEntity);
        //HttpRequestBase httpRequestBase = ((HttpRequestBase) http);
        //((HttpEntityEnclosingRequestBase)http).setEntity(stringEntity);

        if (header != null) {
            for (Header h : header) {
                httpPut.addHeader(h);
            }
        }
        // 执行请求
        try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
            return EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}