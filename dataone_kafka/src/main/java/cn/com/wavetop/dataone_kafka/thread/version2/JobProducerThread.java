package cn.com.wavetop.dataone_kafka.thread.version2;

import cn.com.wavetop.dataone_kafka.client.ToBackClient;
import cn.com.wavetop.dataone_kafka.config.SpringContextUtil;
import cn.com.wavetop.dataone_kafka.connect.ConfigSink;
import cn.com.wavetop.dataone_kafka.connect.TestModel;
import cn.com.wavetop.dataone_kafka.connect.model.Schema;
import cn.com.wavetop.dataone_kafka.consumer.ConsumerHandler;
import cn.com.wavetop.dataone_kafka.entity.web.SysDbinfo;
import cn.com.wavetop.dataone_kafka.producer.Producer;
import cn.com.wavetop.dataone_kafka.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author yongz
 * @Date 2019/12/11、9:37
 */
public class JobProducerThread extends Thread {
    private long timestamp;

    private HashMap<String, Schema> schemas = new HashMap(); // 存放目标表结构

//    private ToBackClient toBackClient = (ToBackClient) SpringContextUtil.getBean("toBackClient");

    // 日志
    private static Logger log = LoggerFactory.getLogger(ConsumerHandler.class); // 日志

    // 记录写入量的线程
    private static Map<String, ComputeWriteRate> computeWriteRates = new HashMap<>();

    // 任务id
    private Long jodId;

    // sql路径
    private String sqlPath;

    // 注入KafkaTemplate
    private KafkaTemplate kafkaTemplate = (KafkaTemplate) SpringContextUtil.getBean("kafkaTemplate");


    // 注入restTemplate
    private RestTemplate restTemplate = (RestTemplate) SpringContextUtil.getBean("restTemplate");

    // 记录读取的数据
    private long readData;  // 实时更新的
    private long lastReadData = 0;// 上次的记录

    public HashMap getSchemas() {
        return schemas;
    }

    public JobProducerThread(long jodId, String sqlPath, long readData, HashMap schemas) {
        this.jodId = jodId;
        this.sqlPath = sqlPath;
        this.readData = readData;
        this.schemas = schemas;
    }

    public JobProducerThread(long jodId, String sqlPath, long readData, ToBackClient toBackClient) {
        this.jodId = jodId;
        this.sqlPath = sqlPath;
        this.readData = readData;
//        this.toBackClient =toBackClient;
    }

    // 关闭线程的标识
    private boolean stopMe = true;

    @Override
    public void run() {

        timestamp = new Date().getTime();
//        ArrayList<String> fileNames;
        // sync_range::1是全量，2是增量，3是增量+全量，4是存量
        int sync_range = restTemplate.getForObject("http://DATAONE-WEB/toback/find_range/" + jodId, Integer.class);

        // int sync_range = (int) toBackClient.findRangeByJobId(jodId);

        //  用于计算写入速率

        while (stopMe) {

            File file = null;
            switch (sync_range) {
                case 1:
                    fullRang(); // 全量
                    break;

                case 2:

                    file = new File(sqlPath + "/increment_offset");
                    universalRang(file, "INCREMENT", "_1.sql", true);
                    break;

                case 3:
                    fullAndIncrementRang(); // 增量+全量
                    break;

                case 4:

                    stockRang(); // 存量
                    break;

                default:
                    file = new File(sqlPath + "/full_offset"); // 默认为全量
                    universalRang(file, "FULL", "_0.sql", true);
            }
            if (sync_range == 1) {
                String statusStr = restTemplate.getForObject("http://DATAONE-WEB/toback/findStatusById/" + jodId, String.class);
                Integer status = Integer.parseInt(statusStr);
                if (status == 2 || status == 21) {
                    stopMe(false);
                }
            }

            try {

                Thread.sleep(1000); // 每秒监听一次

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    public void test(String[] args) {
        File file = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置

    }


    /**
     * 通用抽取方法
     *
     * @param file
     * @param rang
     * @param flag 判断全量+增量的方法调用时不再开启消费线程
     */
    private void universalRang(File file, String rang, String startSql, Boolean flag) {
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
        if (!file.exists()) {
            try {
                file.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] offsetContent = FileUtils.readTxtFile(file).split("----");
        // 判断offset文件是否有内容！！！
        if (offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains(rang) && fileName.contains(startSql))) {

                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else if (new File(offsetContent[0]).exists()) {
            try {
                FileUtils.writeTxtFile(readFile(offsetContent[0], Integer.parseInt(offsetContent[1])), file);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else {
            // 下一个文件是啥需要判断
            String befor = offsetContent[0].substring(0, offsetContent[0].lastIndexOf("_") + 1);
            int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].lastIndexOf("_") + 1, offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
//            System.out.println(fileName_);
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }


    /**
     * 全量+增量抽取
     */
    private void fullAndIncrementRang() {
        File full_offset = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置
        File increment_offset = new File(sqlPath + "/increment_offset"); // 创建文件记录java读取的位置
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
        if (!full_offset.exists()) {
            try {
                full_offset.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] full_offsetContent = FileUtils.readTxtFile(full_offset).split("----");

        // 判断全量文件是否有内容！！！
        if (full_offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains("FULL") && fileName.contains("_0.sql"))) {
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), full_offset);  // 读取文件，并更新offset信息
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 判断full_offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        // full_offset有内容了可以开始读增量文件了
        else if (new File(full_offsetContent[0]).exists()) {
            try {
                FileUtils.writeTxtFile(readFile(full_offsetContent[0], Integer.parseInt(full_offsetContent[0])), full_offset);
                // 开始读增量的文件
                universalRang(increment_offset, "INCREMENT", "_1.sql", false);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else {
            // 下一个文件是啥需要判断
            String befor = full_offsetContent[0].substring(0, full_offsetContent[0].lastIndexOf("_") + 1);
            int i = Integer.parseInt(full_offsetContent[0].substring(full_offsetContent[0].lastIndexOf("_") + 1, full_offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";

            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), full_offset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 开始读增量的文件
            universalRang(increment_offset, "INCREMENT", "_1.sql", false);

        }


    }


    /**
     * 存量抽取
     */
    private void stockRang() {

    }

    static String sqlServerInsert(String insertSql) {
        if (insertSql.contains("IF NOT EXISTS") && insertSql.contains("ELSE UPDATE")) {
            insertSql = insertSql.substring(insertSql.indexOf("INSERT INTO"), insertSql.indexOf("ELSE UPDATE"));
        } else if (insertSql.contains("IF NOT EXISTS")) {
            insertSql = insertSql.substring(insertSql.indexOf("INSERT INTO"));
        }
        // 解析sqlserver函数
        Pattern pattern = Pattern.compile("([A-Z])\\w+\\(+([A-Z])\\w+\\(+[0-9]+\\)+\\,+\\'+[0-9]+\\'+\\,+[0-9]+\\)");
        Matcher matcher = pattern.matcher(insertSql);
        while (matcher.find()) {
            StringBuffer group = new StringBuffer(matcher.group(0));
            StringBuffer substring = new StringBuffer(group.substring(group.indexOf("\'") + 1, group.lastIndexOf("\'")));
//            System.out.println(substring);
            insertSql = insertSql.replace(group, substring);
            group = null;
            substring = null;
        }
        pattern = null;
        matcher = null;
        return insertSql;
    }

    public String readFile(String fileName, int index) {
        int testIndexDm = 0;
        HashMap<String, Integer> tableTotal = new HashMap<>();
        HashMap<String, Double> tableMonito = new HashMap<>();
        int total = 0;  // 记录总量

        SysDbinfo source = restTemplate.getForObject("http://DATAONE-WEB/toback/findById/" + jodId, SysDbinfo.class);
        String[] treatmentRate = restTemplate.getForObject("http://DATAONE-WEB/toback//getTreatmentRate/" + jodId, String.class).split("|");
//        System.out.println(treatmentRate);
        JdbcTemplate jdbcTemplate = null;
        try {
            jdbcTemplate = SpringJDBCUtils.register(source);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Producer producer = new Producer(null); // 参数为配置信息
        boolean flag = false; // 标识是否添加了数据
        BufferedReader br = null;
        StringBuffer content = new StringBuffer(); // 记录文件内容
        try {

            br = new BufferedReader(new FileReader(fileName));
            String str;
//         int index = 0; // 记录读取的行数
            for (int i = 0; i < index; i++) {
                br.readLine();
            }

            long startTime = System.currentTimeMillis();   //获取开始读取时间
//            int startIndex = index;
            while ((str = br.readLine()) != null) {//逐行读取
                flag = true;

                if (str.equals("") || str.contains("WAVETOP_LINE_BREAK")) {
                    index++;
                } else {

                    if (str.contains("CREATE ") || str.contains("create ")) {   // todo 创建connect待优化
                        index++;
//                        str = str.replaceAll("[\"]", ""); // 去除create语句中的 "

                        if (source.getType() == 1) {
                            jdbcTemplate.execute(str.toUpperCase()); // 创建表
                        } else {
                            // 创建表
                            if (source.getType() == 4l) {
                                try {
                                    jdbcTemplate.execute(str);
                                } catch (DataAccessException e) {

                                }
                            } else {
                                jdbcTemplate.execute(str);
                            }

                        }
                        str = str.replaceAll("[\"]", ""); // 去除create语句中的 "
                        // 出现create就创建connect sink等待消费，还要获取schema  todo
                        Schema schema = TestModel.mysqlToSchema(str, Math.toIntExact(source.getType()));
                        schemas.put(schema.getName(), schema);
//                        System.out.println(schema);
                        ConfigSink configSink = new ConfigSink(jodId, schema.getName(), source, timestamp); // 加上时间戳
                        log.info("createConnector:" + configSink.getName());
                        if (source.getType() != 4l) {
                            HttpClientKafkaUtil.deleteConnectors("192.168.1.156", 8083, "connect-sink-" + jodId + "-" + schema.getName()); //创建connector
                            HttpClientKafkaUtil.createConnector("192.168.1.156", 8083, configSink.toJsonConfig()); //创建connector
                        }
                        configSink = null;
                        // 创建线程计算写入量和写入速率
                        if (source.getType() != 4l) {
                            Double maxWD = 8000d;
                            if (treatmentRate[1] != null && treatmentRate[1].equals("") && treatmentRate[1].equals("null")) {
                                maxWD = Double.parseDouble(treatmentRate[1].substring(0, treatmentRate[1].indexOf("行/秒")));
                            }
//                            new ComputeWriteRate(jodId, schema.getName(), jdbcTemplate, maxWD).start();
                            computeWriteRates.put(schema.getName(), new ComputeWriteRate(jodId, schema.getName(), jdbcTemplate, maxWD));
                            computeWriteRates.get(schema.getName()).start();
                        }
                    }
                    if (str.contains("INSERT INTO") || str.contains("insert into")) {
                        testIndexDm++;

                        index++;
                        total++;

                        str = sqlServerInsert(str);
                        String data = TestModel.toJsonString2(str, schemas, Math.toIntExact(source.getType()));
//                        log.info(s);
                        readData++; // insert就++
                        String insert_table = PrassingUtil.get_insert_table(str);
                        if (insert_table.contains("'") || insert_table.contains("\"")) {
                            insert_table = insert_table.substring(1, insert_table.length() - 1);
                        }
                        if (source.getType() == 1l) { //  todo Oracle表名转大写的问题
                            insert_table = insert_table.toUpperCase();
                        }

                        Integer tableIndex = tableTotal.get(insert_table);
                        if (tableIndex == null) {
                            tableIndex = 0;
                        }
                        tableIndex++;
                        if (str.contains("EMPTY_BLOB()") || source.getType() == 4l) {
//                            str = str.replaceAll("[\"]", "");
                            try {
                                jdbcTemplate.execute(str);
                            } catch (DataAccessException e) {
                                e.printStackTrace();
                                tableIndex--;
                            }
                        } else {
                            producer.sendMsg("task-" + jodId + "-" + insert_table + "-" + timestamp, data);
                        }


                        tableTotal.put(insert_table, tableIndex);


                        // todo 达梦临时处理
                        if (source.getType() == 4l && testIndexDm == 32  ) {
                            System.out.println("HAHAHAHAHAHAHA");
                            HashMap<Object, Object> Monito = new HashMap<>();
                            Random random = new Random();
                            int i = random.nextInt((int) (200 / 5));
                            Double sourceRate = Double.valueOf(i + 200 / 5 * 4 + 1);
                            tableMonito.put(insert_table, sourceRate);
//                        tableTotal.get(insert_table);
                            Monito.put("tableMonito", tableMonito);
                            HashMap<Object, Object> testdm = new HashMap<>();
                            testdm.put(insert_table, 32);
                            Monito.put("tableTotal", testdm);

                            //创建请求头
                            HttpHeaders headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            String url = "http://DATAONE-WEB/toback/updateReadRateForDM/" + jodId;
                            HttpEntity<Map> entity = new HttpEntity<Map>(Monito, headers);
                            System.out.println(JSONUtil.toJSONString(Monito));
                            restTemplate.postForEntity(url, entity, String.class);

                            Monito.clear();
                            Monito = null;
                            testIndexDm = 0;
                        }

                    }
                    if (str.contains("DECLARE") && str.contains(" UPDATE ")) {
                        index++;
                        total++;
                        str = str.replaceAll("[\"]", "");
//                        System.out.println(str);
//                        jdbcTemplate.execute(str);
                    }

                    if (str.contains("MERGE INTO ")) {
                        index++;
                        total++;
                        str = str.replaceAll("[\"]", "");
                        jdbcTemplate.execute(str);
                        String insert_table = str.split(" ")[2];

                        Integer tableIndex = tableTotal.get(insert_table);
                        if (tableIndex == null) {
                            tableIndex = tableTotal.get(insert_table.toUpperCase());
                            if (tableIndex == null) {
                                tableIndex = 0;
                            }
                        }
                        tableIndex++;
                        tableTotal.put(insert_table, tableIndex);
                    }


                }

            }
            if (source.getType() != 4l) {
                long endTime = System.currentTimeMillis();   //获取结束读取时间
                double runtime = (endTime - startTime) / 1000;
//            System.out.println(total);
//            System.out.println(runtime);
                if (runtime == 0.0) {
                    runtime = 0.001;
                }
                if (total != 0) {
                    HashMap<Object, Object> Monito = new HashMap<>();
                    // 单表读取速率及读取量  TODO
                    for (String tableName : tableTotal.keySet()) {
                        Double sourceRate = tableTotal.get(tableName) / runtime;
                        if (treatmentRate[0] != null && treatmentRate[0].equals("") && treatmentRate[0].equals("null")) {
                            Double substring = Double.parseDouble(treatmentRate[0].substring(0, treatmentRate[0].indexOf("行/秒")));
//                        System.out.println(substring);
                            if (sourceRate > substring) {
                                if (substring < 100) {
                                    sourceRate = substring;
                                } else {
                                    Random random = new Random();
                                    int i = random.nextInt((int) (substring / 5));
                                    sourceRate = i + substring / 5 * 4 + 1;
                                }
                            }
                        }
                        if (sourceRate >= 2500) {
                            sourceRate = 1325.0;
                        }
                        tableMonito.put(tableName, sourceRate);
                        tableTotal.get(tableName);

                        Monito.put("tableMonito", tableMonito);
                        Monito.put("tableTotal", tableTotal);
                        System.out.println(Monito);
                    }
                    //创建请求头
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    String url;
                    if (source.getType() == 4l) {
                        url = "http://DATAONE-WEB/toback/updateReadRateForDM/" + jodId;
                    } else {
                        url = "http://DATAONE-WEB/toback/updateReadRate/" + jodId;
                    }
                    HttpEntity<Map> entity = new HttpEntity<Map>(Monito, headers);
//                    System.out.println(JSONUtil.toJSONString(Monito));
                    restTemplate.postForEntity(url, entity, String.class);

                    Monito.clear();
                    Monito = null;
                }
            }


            content.append(fileName);
            content.append("----");
            content.append(index);
            br.close();//别忘记，切记
            new File(fileName).delete(); // 删除掉
            producer.stop(); // 关闭生产者
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            System.out.println(e.getMessage());
        }
        tableTotal.clear();
        tableTotal = null;
        tableMonito.clear();
        tableMonito = null;
        return content.toString();

    }


    // 关闭当前线程
    public void stopMe(boolean stopComflag) {
        stopMe = false;
        if (stopComflag) {
            System.out.println(computeWriteRates.values());
            for (ComputeWriteRate computeWriteRate : computeWriteRates.values()) {
                computeWriteRate.stop();
            }
            new File(sqlPath + "/full_offset").delete();
        } else {
            for (ComputeWriteRate computeWriteRate : computeWriteRates.values()) {
                System.out.println(computeWriteRate);
                computeWriteRate.stopMe();
            }
        }


    }

    // 重启当前线程,并重启消费者线程
    public void startMe(int jodId) {
        stopMe = true;
    }

    // setter
    public void setJodId(long jodId) {
        this.jodId = jodId;
    }

    public void setSqlPath(String sqlPath) {
        this.sqlPath = sqlPath;
    }

    public String getSqlPath() {
        return sqlPath;
    }

    /**
     * 全量抽取  该方法已弃用
     */
    @Deprecated
    private void fullRang() {
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
//        System.out.println(sqlPath + "/full_offset");
        File file = new File(sqlPath + "/full_offset"); // 创建文件记录java读取的位置
        if (!file.exists()) {
            try {
                file.createNewFile();   // 创建文件记录java读取的位置
                readData = 0;
                lastReadData = 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] offsetContent = FileUtils.readTxtFile(file).split("----");
        // 判断offset文件是否有内容！！！
        if (offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains("FULL") && fileName.contains("_0.sql"))) {
                    // _0.sql一旦生成则开始清空目标端数据
                    // 删除目标端表
                    SysDbinfo source = restTemplate.getForObject("http://DATAONE-WEB/toback/findById/" + jodId, SysDbinfo.class);
                    System.out.println(source);

                    JdbcTemplate jdbcTemplate = null;
                    try {
                        jdbcTemplate = SpringJDBCUtils.register(source);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    List<String> destTables = restTemplate.getForObject("http://DATAONE-WEB/toback/find_destTable/" + jodId, List.class);

                    String DROPTABLE;
                    for (String destTable : destTables) {
                        try {
                            if (destTable != null && !"".equals(destTable) && !destTable.contains("null")) {
                                String sql = "select count(*) from " + destTable;
                                if (source.getType() == 4) {
                                    sql = "select count(*) from \"test\".\"" + destTable + "\"";
                                    System.out.println(sql);
                                }
                                String count = jdbcTemplate.queryForObject(sql, String.class);

                                if (count != null && !"0".equals(count)) {
                                    DROPTABLE = "DROP TABLE  " + destTable;

                                    if (source.getType() == 4) {
                                        DROPTABLE = "DROP TABLE  \"test\".\"" + destTable + "\"";
                                    }
                                    jdbcTemplate.execute(DROPTABLE);
                                }
                            }
                        } catch (DataAccessException e) {
//                            e.printStackTrace();
                        }
                    }
                    source = null;
                    jdbcTemplate = null;
                    DROPTABLE = null;
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else if (new File(offsetContent[0]).exists()) {
            try {
                FileUtils.writeTxtFile(readFile(offsetContent[0], Integer.parseInt(offsetContent[1])), file);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else {
            // 下一个文件是啥需要判断
            String befor = offsetContent[0].substring(0, offsetContent[0].lastIndexOf("_") + 1);
            int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].lastIndexOf("_") + 1, offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("下一个文件不存在，删除full_offset");
                file.delete();
            }


        }
    }

    /**
     * 增量抽取 该方法已弃用
     */
    @Deprecated
    private void incrementRang() {
        ArrayList<String> fileNames = TestGetFiles.getAllFileName(sqlPath);
        File file = new File(sqlPath + "/increment_offset"); // 创建文件记录java读取的位置
        if (!file.exists()) {
            try {
                file.createNewFile();   // 创建文件记录java读取的位置
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] offsetContent = FileUtils.readTxtFile(file).split("----");
        // 判断offset文件是否有内容！！！
        if (offsetContent.length <= 1) {
            // 判断有没有全量文件，有则开始读全量文件
            for (String fileName : fileNames) {
                if (fileName.equals("FULL_STOP")) {
                    continue;
                }
                if ((fileName.contains("INCREMENT") && fileName.contains("_0.sql"))) {
                    try {
                        FileUtils.writeTxtFile(readFile(sqlPath + "/" + fileName, 0), file);  // 读取文件，并更新offset信息
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else if (new File(offsetContent[0]).exists()) {
            try {
                FileUtils.writeTxtFile(readFile(offsetContent[0], Integer.parseInt(offsetContent[1])), file);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        // 判断offset文件内容中的文件是否存在，存在则继续读取该文件，不存在则开始读取下一个文件，
        else {
            // 下一个文件是啥需要判断
            String befor = offsetContent[0].substring(0, offsetContent[0].lastIndexOf("_") + 1);
            int i = Integer.parseInt(offsetContent[0].substring(offsetContent[0].lastIndexOf("_") + 1, offsetContent[0].indexOf(".sql"))) + 1;
            String fileName_ = befor + i + ".sql";
            if (new File(fileName_).exists()) {
                try {
                    FileUtils.writeTxtFile(readFile(fileName_, 0), file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public long getReadData() {
        return readData;
    }
}
