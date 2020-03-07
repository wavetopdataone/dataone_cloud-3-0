package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.config.SpringJDBCUtils;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysFilterTable;
import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.entity.SysTablerule;
import com.cn.wavetop.dataone.util.DBConns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

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


    /**
     * 根据jobId查询源端数据源信息
     */
    public SysDbinfo findSourcesDbinfoById(Long jobId) {
        return sysJobrelaRespository.findSourcesDbinfoById(jobId.longValue());
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
     * 查看源端表字段，类型，长度，精度，是否为null
     *
     * @param jobId
     * @param tableName
     * @return list里面套的map
     */
    public List findSourceFiled(Long jobId, String tableName) {
//            sql =
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        JdbcTemplate jdbcTemplate = SpringJDBCUtils.register(sysDbinfo);
        List filedNameList = new ArrayList();
        List<String> filedNames = new ArrayList<>();
        if (sysDbinfo.getType() == 1) {
            filedNameList = jdbcTemplate.queryForList("SELECT COLUMN_NAME, DATA_TYPE,(Case When DATA_TYPE='NUMBER' Then DATA_PRECISION Else DATA_LENGTH End ) as DATA_LENGTH , NVL(DATA_PRECISION,0), NVL(DATA_SCALE,0), NULLABLE, COLUMN_ID ,DATA_TYPE_OWNER FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + tableName + "' AND OWNER='" + sysDbinfo.getSchema() + "'");
            logger.info("所有的表有：" + filedNameList);
        } else if (sysDbinfo.getType() == 2) {
        }
        return filedNameList;
    }

    /**
     * 根据jobId和表名查询映射的字段名称
     */
    public List findFiledByJobId(Long jobId, String tableName) {
        SysDbinfo sysDbinfo = findSourcesDbinfoById(jobId);
        JdbcTemplate jdbcTemplate = SpringJDBCUtils.register(sysDbinfo);
        List filedNameList = new ArrayList();
        List<String> filedNames = new ArrayList<>();
        if (sysDbinfo.getType() == 1) {
            filedNameList = jdbcTemplate.queryForList("SELECT COLUMN_NAME FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + tableName + "' AND OWNER='" + sysDbinfo.getSchema() + "'");
            logger.info("所有的表有：" + filedNameList);
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
     * 验证目标端是否存在表
     *
     * @param job_id
     * @param source_name//表名
     * @return
     */
    public Integer VerifyDb(Long job_id, String source_name) {
        String sqlss = "";
        String table = "";
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
        List<SysTablerule> sysTableruleList = sysTableruleRepository.findBySourceTableAndJobId(source_name, job_id);
        if (sysTableruleList != null && sysTableruleList.size() > 0) {
            table = sysTableruleList.get(0).getDestTable();
        } else {
            table = source_name;
        }
        //查询目标端是否出现此表
        List<String> tablename = DBConns.existsTableName(sysDbinfo, sqlss, source_name, table);
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
     * <p>
     * <p>
     * 上面查询映射字段时候应该吧类型长度精度不为null也要查出来为拼接建表做准备
     */
    public List<String> BlobOrClob(Long jobId, String sourceTable) {

        return null;
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
}