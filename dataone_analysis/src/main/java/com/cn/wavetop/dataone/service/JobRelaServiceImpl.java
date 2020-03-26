package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.destCreateTable.SuperCreateTable;
import com.cn.wavetop.dataone.destCreateTable.impl.MysqlCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.OracleCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.SqlserverCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.DMCreateSql;

import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.util.DBConns;
import com.netflix.discovery.converters.Auto;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * 1.findSourcesDbinfoById:根据jobId查询源端数据源信息
 * 2.findDestDbinfoById:根据jobId查询目标端数据源信息
 * 3.findById:根据jobId查询任务信息
 * 4.findFileTableByJobId:根据jobId查询过滤的表
 * 5.findTableById:根据jobId查询映射的表名称
 * 6.findFilterFiledByJobId:根据jobId和表名查询过滤的字段名称
 * 7.findSourceFiled:查看源端映射字段的类型，长度，精度，是否为null
 * 8.findFiledByJobId:据jobId和表名查询映射的字段名称
 * 9.findPrimaryKey:根据jobId和表名查询源端表主键
 * 10.destTableName：根据jobId和表名查询目标端表名
 * 11.VerifyDb：验证目标端是否存在表 0不存在 1存在
 * 12.BlobOrClob：根据任务id和源端表名查询映射的字段中是否含有大字段 返回大字段的list集合
 * 13.createTable：目的端的建表语句
 * 14.excuteSql：执行目的端建表sql返回sql
 * 15.getDestTable：根据源端表名返回目的端表名
 * 16.findMapField：根据jobId和tableName查询源端对应的目标端表 返回map，key为源端表名 value为目标端
 * 17.findFiledNoBlob：根据jobId和tableName查询同步的字段不包含大字段的字段集合
 */
@Service
public class JobRelaServiceImpl {
    //    private SysJobrelaRespository sysJobrelaRespository = (SysJobrelaRespository) SpringContextUtil.getBean("sysJobrelaRespository");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private SysFilterTableRepository sysFilterTableRepository = (SysFilterTableRepository) SpringContextUtil.getBean("sysFilterTableRepository");
//    private SysDbinfoRespository sysDbinfoRespository = (SysDbinfoRespository) SpringContextUtil.getBean("sysDbinfoRespository");
//    private SysTableruleRepository sysTableruleRepository = (SysTableruleRepository) SpringContextUtil.getBean("sysTableruleRepository");

    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private SysFilterTableRepository sysFilterTableRepository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Autowired
    private SysTableruleRepository sysTableruleRepository;
    @Autowired
    private SysFieldruleRepository sysFieldruleRepository;
    @Autowired
    private ErrorLogRespository errorLogRespository;
    @Autowired
    private UserLogRepository userLogRepository;
    // 注入restTemplate
    @Autowired
    private RestTemplate restTemplate ;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    /**
     * 根据jobId查询源端数据源信息
     */
    public SysDbinfo findSourcesDbinfoById(Long jobId) {
        return sysJobrelaRespository.findSourcesDbinfoById(jobId.longValue());
    }

    /**
     * 根据jobId查询目标端数据源信息
     */
    public SysDbinfo findDestDbinfoById(Long jobId) {
        return sysJobrelaRespository.findDbinfoById(jobId.longValue());
    }

    /**
     * 根据jobId查询任务信息
     */
    public SysJobrela findById(Long jobId) {
        return sysJobrelaRespository.findById(jobId.intValue());
    }

    /**
     * 根据jobId查询过滤的表
     */
    public List findFileTableByJobId(Long jobId) {
        List<SysFilterTable> sysFilterTable = sysFilterTableRepository.findJobId(jobId.longValue());
        List<String> filterTable = new ArrayList<>();
        for (SysFilterTable sysFilterTable1 : sysFilterTable) {
            filterTable.add(sysFilterTable1.getFilterTable());
        }
        return filterTable;
    }


    /**
     * 根据jobId查询映射的表名称
     */
    public List findTableById(Long jobId, Connection conn) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        List tableNameList = new ArrayList();
        List<String> tableNames = new ArrayList<>();
        //oracle
        if (sysDbinfo.getType() == 1) {
            //根据jobID查询任务有多少张表sum代替//标的名字是什么
            //过滤的直接去掉，字段就按照原表的先解析
            String sql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
            try {
                tableNameList = DBUtil.query2(sql, conn).getList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("所有的表有：" + tableNameList);
            Map map = null;
            for (int i = 0; i < tableNameList.size(); i++) {
                map = (Map) tableNameList.get(i);
                tableNames.add((String) map.get("TABLE_NAME"));
            }
            Set<String> set = new HashSet<>();
            List<String> sysFilterTable = findFileTableByJobId(jobId);
            for (int i = 0; i < sysFilterTable.size(); i++) {
                set.add(sysFilterTable.get(i));
            }
            Iterator<String> iterator = tableNames.iterator();
            while (iterator.hasNext()) {
                String num = iterator.next();
                if (set.contains(num)) {
                    iterator.remove();
                }
            }
            return tableNames;
        } else if (sysDbinfo.getType() == 2) {

        }
        return null;
    }

    /**
     * 根据jobId和表名查询过滤的字段名称
     */
    public List findFilterFiledByJobId(Long jobId, String tableName) {
        List<String> filterFiled = sysFilterTableRepository.findFiledJobId(jobId, tableName);
        return filterFiled;
    }

    /**
     * 查看源端映射字段的类型，长度，精度，是否为null
     *
     * @param jobId
     * @param tableName
     * @return list里面套的map
     */
    public ResultMap findSourceFiled(Long jobId, String tableName, Connection conn) {
//            sql =
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        ResultMap filedNameList = null;
        List<String> filedNames = new ArrayList<>();
        try {
            if (sysDbinfo.getType() == 1) {
                filedNameList = DBUtil.query2("SELECT COLUMN_NAME, DATA_TYPE,(Case When DATA_TYPE='NUMBER' Then NVL(DATA_PRECISION,0) Else NVL(DATA_LENGTH,0) End ) as DATA_LENGTH , NVL(DATA_PRECISION,0) as DATA_PRECISION , NVL(DATA_SCALE,0) as DATA_SCALE, NULLABLE, COLUMN_ID ,DATA_TYPE_OWNER FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + tableName + "' AND OWNER='" + sysDbinfo.getSchema() + "'", conn);
                filedNames = findFilterFiledByJobId(jobId, tableName);//过滤的字段
                for (int i = 0; i < filedNames.size(); i++) {
                    for (int j = 0; j < filedNameList.size(); j++) {
                        if (filedNames.get(i).toUpperCase().equals(filedNameList.get(j).get("COLUMN_NAME").toString().toUpperCase())) {
                            filedNameList.remove(j);
                        }
                    }
                }
            } else if (sysDbinfo.getType() == 2) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filedNameList;
    }

    /**
     * 根据jobId和表名查询映射的字段名称
     */
    public List findFiledByJobId(Long jobId, String tableName, Connection conn) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        List filedNameList = new ArrayList();
        List<String> filedNames = new ArrayList<>();
        if (sysDbinfo.getType() == 1) {
            String sql = "SELECT COLUMN_NAME FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + tableName + "' AND OWNER='" + sysDbinfo.getSchema() + "'";
            try {
                filedNameList = DBUtil.query2(sql, conn).getList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Map map = null;
            for (int i = 0; i < filedNameList.size(); i++) {
                map = (Map) filedNameList.get(i);
                filedNames.add((String) map.get("COLUMN_NAME"));
            }
            Set<String> set = new HashSet<>();
            set.addAll(findFilterFiledByJobId(jobId, tableName));
//             List<String> sysFiledName = findFileTableByJobId(jobId);
//             for (int i = 0; i < sysFilterTable.size(); i++) {
//                 set.add(sysFilterTable.get(i));
//             }
            Iterator<String> iterator = filedNames.iterator();
            while (iterator.hasNext()) {
                String num = iterator.next();
                if (set.contains(num)) {
                    iterator.remove();
                }
            }
            return filedNames;
        } else if (sysDbinfo.getType() == 2) {
//             sql = "select Column_Name as ColumnName,data_type as TypeName, (case when data_type = 'float' or data_type = 'int' or data_type = 'datetime' or data_type = 'bigint' or data_type = 'double' or data_type = 'decimal' then NUMERIC_PRECISION else CHARACTER_MAXIMUM_LENGTH end ) as length, NUMERIC_SCALE as Scale,(case when IS_NULLABLE = 'YES' then 0 else 1 end) as CanNull  from information_schema.columns where table_schema ='" + sysDbinfo.getDbname() + "' and table_name='" + tablename + "'";

            return null;
        } else {
            return null;
        }
    }

    /**
     * 表名查询表主键
     */
    public List findPrimaryKey(Long jobId, String tableName, Connection conn) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        List PrimaryKeyList = null;
        List<String> PrimaryKeys = new ArrayList<>();
        Map map = null;
        if (sysDbinfo.getType() == 1) {
            String sql = "select COLUMN_NAME from user_cons_columns where table_name='" + tableName + "' and constraint_name in (select constraint_name from user_constraints where table_name='" + tableName + "' and constraint_type='P')";
            try {
                PrimaryKeyList = DBUtil.query2(sql, conn).getList();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("该表的主键有：" + PrimaryKeyList);
            for (int i = 0; i < PrimaryKeyList.size(); i++) {
                map = (Map) PrimaryKeyList.get(i);
                PrimaryKeys.add((String) map.get("COLUMN_NAME"));
            }
            return PrimaryKeys;
        } else if (sysDbinfo.getType() == 2) {
//            SELECT distinct column_name FROM INFORMATION_SCHEMA.`KEY_COLUMN_USAGE` WHERE table_name='error_log' AND constraint_name='PRIMARY'
            return null;
        } else {
            return null;
        }
    }


    /**
     * 目标端表名为
     */
    public String destTableName(Long jobId, String sourceTable) {
        String table = null;
        List<SysTablerule> sysTableruleList = sysTableruleRepository.findBySourceTableAndJobId(sourceTable, jobId);
        if (sysTableruleList != null && sysTableruleList.size() > 0) {
            table = sysTableruleList.get(0).getDestTable();
        } else {
            table = sourceTable;
        }
        return table;
    }

    /**
     * 验证目标端是否存在表
     *
     * @param job_id
     * @param source_name//表名
     * @return 0是不存在, 1是存在
     */
    public Integer VerifyDb(Long job_id, String source_name, Connection conn) {
        String sqlss = "";
        String destTable = "";
        Integer flag = 1;//0是不存在,1是存在
        SysDbinfo sysDbinfo = findSourcesDbinfoById(job_id);
        //若目标端存在此表则提示用户
        if (sysDbinfo.getType() == 1) {
            //oracle
            sqlss = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
        } else if (sysDbinfo.getType() == 2) {
            //mysql
            sqlss = "show tables";
        } else if (sysDbinfo.getType() == 3) {
            //sqlserver
            sqlss = "select name from sysobjects where xtype='u'";
        } else if (sysDbinfo.getType() == 4) {
            //dameng select distinct object_name TABLE_SCHEMA from all_objects where object_type = 'SCH'
            sqlss = "SELECT TABLE_NAME FROM USER_TABLES";
        }
        destTable = destTableName(job_id, source_name);//目标端表名字
        //查询目标端是否出现此表
        List<String> tablename = DBConns.existsTableName(sysDbinfo, sqlss, source_name, destTable, conn);
        //查看目的端是否存在表名
        if (tablename != null && tablename.size() > 0) {
            flag = 1;
            return flag;
        } else {
            flag = 0;
            return flag;
        }
    }

    /**
     * 根据任务id和源端表名查询映射的字段中是否含有大字段
     * 返回值是list数组，内容为大字段名称
     */
    public List<String> BlobOrClob(Long jobId, String sourceTable, Connection conn) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        ResultMap list = findSourceFiled(jobId, sourceTable, conn);
        List<String> blobOrClob = new ArrayList<>();
        Map map = null;
        if (sysDbinfo.getType() == 1) {
            for (int i = 0; i < list.size(); i++) {
                map = list.get(i);
                if (map.get("DATA_TYPE").toString().toUpperCase().equals("blob".toUpperCase()) || map.get("DATA_TYPE").toString().toUpperCase().equals("clob".toUpperCase())) {
                    blobOrClob.add((String) map.get("COLUMN_NAME"));
                }
            }
            return blobOrClob;
        } else if (sysDbinfo.getType() == 2) {
            return null;
        }
        return null;
    }


    /**
     * 数据库的建表语句
     * <p>
     * COLUMN_NAME, DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE, NULLABLE, COLUMN_ID ,DATA_TYPE_OWNER
     */
    public String createTable(Long jobId, String sourceTable, Connection conn) {
        SysDbinfo sysDbinfo = findDestDbinfoById(jobId);//目标端数据库
        SuperCreateTable createSql = null;
        switch (sysDbinfo.getType().intValue()) {
            case 1:
                //oracle
                createSql = new OracleCreateSql();
                break;
            case 2:
                //mysql
                createSql = new MysqlCreateSql();
                break;
            case 3:
                //sqlserver
                createSql = new SqlserverCreateSql();
                break;
            case 4:
                //DM
                createSql = new DMCreateSql();
                break;
            default:
                logger.error("不存在目标端类型");
        }
        String sql = createSql.createTable(jobId, sourceTable, conn);
        return sql;
    }


    public String excuteSql(Long jobId, String tableName, String sql, Connection connection) {

        try {
            DBUtil.update(sql, connection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sql;
    }

    /**
     * todo
     * 根据源端表名获取目的端表名
     *
     * @param jobId
     * @param tableName
     * @return
     */
    public String getDestTable(Long jobId, String tableName) {
        List<SysTablerule> SysTablerules = sysTableruleRepository.findBySourceTableAndJobId(tableName, jobId);
        if (SysTablerules.size() < 1) {
            return tableName;
        }
        String destTable = SysTablerules.get(0).getDestTable();
        if (destTable == null || "".equals(destTable)) {
            return SysTablerules.get(0).getSourceTable();
        } else {
            return destTable;
        }
    }

    /**
     * 参数：jobid和tableName
     * return map
     * key为源端表字段，对应的value为目的端表
     */
    public Map findMapField(Long jobId, String sourceTable, Connection conn) {
        //源端同步的所有字段
        List<String> sourceFiledList = findFiledByJobId(jobId, sourceTable, conn);
        List<SysFieldrule> sysFieldruleList = null;
        Map map = new HashMap();
        for (String sourceFiled : sourceFiledList) {
            sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndFieldName(jobId, sourceTable, sourceFiled);
            //中台数据库是否能查到映射的目标端字段
            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                map.put(sourceFiled, sysFieldruleList.get(0).getDestFieldName());
            } else {
                map.put(sourceFiled, sourceFiled);
            }
        }
        return map;
    }

    /**
     * 参数：jobid和tableName
     * return List
     * 要求查源端需要同步的字段（不包含blob、clob。。。）
     */
    public List findFiledNoBlob(Long jobId, String sourceTable, Connection conn) {
        //源端同步的所有字段
        List<String> sourceFiled = findFiledByJobId(jobId, sourceTable, conn);
        //源端同步的大字段
        List<String> BlobOrClob = BlobOrClob(jobId, sourceTable, conn);
        if (BlobOrClob != null && BlobOrClob.size() > 0) {
            Iterator<String> iterator = sourceFiled.iterator();
            while (iterator.hasNext()) {
                String num = iterator.next();
                if (BlobOrClob.contains(num)) {
                    iterator.remove();
                }
            }
        }
        return sourceFiled;
    }


    /**
     * 解析create语句取出日期类型 返回值放入的都是日期类型的列名
     */
    public List<String> analyCreate(Map dataMap) {
        Map message = (Map) dataMap.get("message");
        String createTable = message.get("creatTable").toString();
        String[] strings = createTable.substring(createTable.indexOf("(") + 1, createTable.lastIndexOf(")")).split(",");
        List list = null;
        String filedType;
        String field; // 字段
        for (int i = 0; i < strings.length; i++) {

            if (strings[i].contains("PRIMARY KEY")) continue; //这个不是字段的调过
            if (strings[i].split(" ").length < 2) continue; // 精度
            if (strings[i].split(" ")[1].toUpperCase().equals("date".toUpperCase()) || strings[i].split(" ")[1].equals("TIMESTAMP".toUpperCase())) {
                list.add(strings[i].split(" ")[0]);
            }
        }
        return list;
    }

    /**
     * 判断日期类型的值长度为多少
     */
    public Object dateLength(String value) {
        StringBuffer stringBuffer = new StringBuffer("to_date('");
        if (value.length() > 10) {
            stringBuffer.append(value + "','yyyy-MM-dd hh24:mi:ss')");
        } else {
            stringBuffer.append(value + "','yyyy-MM-dd')");
        }
        return stringBuffer;
    }

    /**
     * 判断同步的列是否包含日期字段
     *
     * @param value
     * @param fields
     * @return
     */
    public boolean equalsDate(String value, List<String> fields) {
        for (int i = 0; i < fields.size(); i++) {
            if (value.equals(fields.get(i))) {
                return true;
            }
        }
        return false;
    }


    /**
     * 插入错误信息
     */
    @Transactional
    public void insertError(ErrorLog errorLog) {

        /*Optional<SysJobrela> sysJobrela = sysJobrelaRespository.findById(errorLog.getJobId());
        String jobName = sysJobrela.get().getJobName();

        long count = errorLogRespository.count();
        if (count >= 100000) {
            Userlog build2 = Userlog.builder().time(new Date()).jobName(jobName).operate("错误队列" + jobName + "已达上限，请处理后重启").jobId(errorLog.getJobId()).build();
            String jobStatus = sysJobrela.get().getJobStatus();
            Boolean forObject = restTemplate.getForObject("http://dataone-analysis/job/stop/" + errorLog.getJobId(), Boolean.class);
            //0是待激活,1是运行,2是暂停,3是终止,4是异常,5是待完善,11运行状态
            if (forObject && !"2".equals(jobStatus) && !"4".equals(jobStatus)) {
                sysJobrela.get().setJobStatus("2");//改为暂停
                sysJobrelaRespository.save(sysJobrela.get());
                userLogRepository.save(build2);
            }
        } else if (count >= 90000 && count < 100000) {
            Userlog build2 = Userlog.builder().time(new Date()).jobName(jobName).operate("错误队列" + jobName + "已接近上限").jobId(errorLog.getJobId()).build();
            userLogRepository.save(build2);
        }*/
        errorLogRespository.save(errorLog);
        //更新错误信息
        sysMonitoringRepository.updateErrorData(errorLog.getJobId(),errorLog.getSourceName());
    }



    private static List<String> doLogAddress(Long jobId) {
//        SysDbinfo sysDbinfo=findSourcesDbinfoById(jobId);
        //数据库连接
        SysDbinfo sysDbinfo = SysDbinfo.builder().dbname("ORCL").host("192.168.1.25").user("system").password("oracle").port(1521L).type(1l).schema("SCOTT").build();
        String sql = " select group#, member from v$logfile order by group#";
        Connection conn = null;
        ResultMap resultMap = null;
        List<String> logList = new ArrayList<>();
        try {
            conn = DBConns.getConn(sysDbinfo);
            resultMap = DBUtil.query2(sql, conn);
            for (int i = 0; i < resultMap.size(); i++) {
                logList.add(resultMap.get(i, "member").replaceAll("\\\\", "\\\\\\\\"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                DBConns.close(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return logList;
    }

    public static void main(String[] args) {
        List<String> list = doLogAddress(16L);
        for (String a : list) {
            System.out.println(a);
        }
    }
}