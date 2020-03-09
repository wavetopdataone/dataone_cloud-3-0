package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.destCreateTable.SuperCreateTable;
import com.cn.wavetop.dataone.destCreateTable.impl.DMCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.MysqlCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.OracleCreateSql;
import com.cn.wavetop.dataone.destCreateTable.impl.SqlserverCreateSql;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.util.DBConns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * JobRelaServiceImpl.findSourcesDbinfoById(Long jobId):
 *
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
    public List findTableById(Long jobId) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        JdbcTemplate jdbcTemplate = SpringJDBCUtils.register(sysDbinfo);
        List tableNameList = new ArrayList();
        List<String> tableNames = new ArrayList<>();
        //oracle
        if (sysDbinfo.getType() == 1) {
            //根据jobID查询任务有多少张表sum代替//标的名字是什么
            //过滤的直接去掉，字段就按照原表的先解析
            tableNameList = jdbcTemplate.queryForList("SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'");
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
    public ResultMap findSourceFiled(Long jobId, String tableName) {
//            sql =
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        Connection conn = null;
        ResultMap filedNameList = null;
        List<String> filedNames = new ArrayList<>();
        try {
            if (sysDbinfo.getType() == 1) {
                conn = DBConns.getOracleConn(sysDbinfo);
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
        } finally {
            try {
                DBConns.close(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return filedNameList;
    }

    /**
     * 根据jobId和表名查询映射的字段名称
     * 要求查源端需要同步的字段（包含blod、clob。。。）
     */
    public List findFiledByJobId(Long jobId, String tableName) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        JdbcTemplate jdbcTemplate = SpringJDBCUtils.register(sysDbinfo);
        List filedNameList = new ArrayList();
        List<String> filedNames = new ArrayList<>();
        if (sysDbinfo.getType() == 1) {
            filedNameList = jdbcTemplate.queryForList("SELECT COLUMN_NAME FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + tableName + "' AND OWNER='" + sysDbinfo.getSchema() + "'");
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
    public List findPrimaryKey(Long jobId, String tableName) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        JdbcTemplate jdbcTemplate = SpringJDBCUtils.register(sysDbinfo);
        List PrimaryKeyList = new ArrayList();
        List<String> PrimaryKeys = new ArrayList<>();
        Map map = null;
        if (sysDbinfo.getType() == 1) {
            PrimaryKeyList = jdbcTemplate.queryForList("select COLUMN_NAME from user_cons_columns where table_name='" + tableName + "' and constraint_name in (select constraint_name from user_constraints where table_name='" + tableName + "' and constraint_type='P')");
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
    public Integer VerifyDb(Long job_id, String source_name) {
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
        List<String> tablename = DBConns.existsTableName(sysDbinfo, sqlss, source_name, destTable);
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
    public List<String> BlobOrClob(Long jobId, String sourceTable) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        ResultMap list = findSourceFiled(jobId, sourceTable);
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
    public String createTable(Long jobId, String sourceTable) {
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
        String sql = createSql.createTable(jobId, sourceTable);
        return sql;
    }

    /**
     * 执行sql返回sql
     *
     * @param jobId
     * @param sourceTable
     * @return
     */
    public String excuteSql(Long jobId, String sourceTable) {
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
        String sql = createSql.excuteSql(jobId, sourceTable);
        System.out.println("sql执行成功");
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
    public Map findMapField(Long jobId, String sourceTable) {
        //源端同步的所有字段
        List<String> sourceFiledList = findFiledByJobId(jobId, sourceTable);
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
    public List findFiledNoBlob(Long jobId, String sourceTable) {
        //源端同步的所有字段
        List<String> sourceFiled = findFiledByJobId(jobId, sourceTable);
        //源端同步的大字段
        List<String> BlobOrClob =  BlobOrClob(jobId, sourceTable);
        if(BlobOrClob!=null&&BlobOrClob.size()>0) {
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
}