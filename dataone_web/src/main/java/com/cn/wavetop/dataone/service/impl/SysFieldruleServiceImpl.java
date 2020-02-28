package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysFieldruleService;
import com.cn.wavetop.dataone.util.DBConns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


/**
 * @Author yongz
 * @Date 2019/10/11、15:27
 */
@Service
public class SysFieldruleServiceImpl implements SysFieldruleService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SysFieldruleRespository repository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private SysTableruleRespository sysTableruleRespository;
    @Autowired
    private SysJobrelaRepository sysJobrelaRepository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Autowired
    private SysFieldruleRepository sysFieldruleRepository;
    @Autowired
    private SysFilterTableRepository sysFilterTableRepository;
    @Autowired
    private SysJobrelaRelatedRespository sysJobrelaRelatedRespository;
    @Autowired
    private SysDesensitizationRepository sysDesensitizationRepository;
    @Autowired
    private SysFiledTypeRepository sysFiledTypeRepository;
    @Autowired
    private KafkaDestFieldRepository kafkaDestFieldRepository;
    @Autowired
    private KafkaDestTableRepository kafkaDestTableRepository;

    @Override
    public Object getFieldruleAll() {
        return ToData.builder().status("1").data(repository.findAll()).build();
    }

    @Override
    public Object checkFieldruleByJobId(long job_id) {
        if (repository.existsByJobId(job_id)) {
            List<SysFieldrule> sysFieldrule = repository.findByJobId(job_id);
            Map<Object, Object> map = new HashMap();
            map.put("status", 1);
            map.put("data", sysFieldrule);
            return map;
        } else {
            return ToData.builder().status("0").message("任务不存在").build();

        }
    }

    @Override
    public Object addFieldrule(SysFieldrule sysFieldrule) {
        return "该接口已弃用";
    }

    //判断字段是否发生改变
    public boolean fieldChange(String[] split) {
        boolean flag = false;
        for (String s : split) {
            String[] ziduan = s.split(",");
            //若字段改变则存入字段规则表 todo
//                if (!ziduan[0].equals(ziduan[1]) || !ziduan[3].equals(ziduan[7]) || !ziduan[5].equals(ziduan[8])) {
            if (!ziduan[0].equals(ziduan[1])) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    //验证源端目标端是否存在表
    public Object VerifyDb(Long job_id, String source_name, String dest_name) {
        SysDbinfo sysDbinfo = new SysDbinfo();//源端
        SysDbinfo sysDbinfo2 = new SysDbinfo();//目标端
        //查詢关联的数据库连接表jobrela
        List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(job_id.longValue());
        //查询到数据库连接
        if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
            sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
            sysDbinfo2 = sysDbinfoRespository.findById(sysJobrelaList.get(0).getDestId().longValue());
        } else {
            return ToDataMessage.builder().status("0").message("该任务没有连接").build();
        }
        //若目标端存在此表则提示用户
        String sqlss = "";
        if (sysDbinfo2.getType() == 1) {
            //oracle
            sqlss = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo2.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
        } else if (sysDbinfo2.getType() == 2) {
            //mysql
            sqlss = "show tables";
        } else if (sysDbinfo2.getType() == 3) {
            //sqlserver
            sqlss = "select name from sysobjects where xtype='u'";
        } else if (sysDbinfo2.getType() == 4) {
            //dameng select distinct object_name TABLE_SCHEMA from all_objects where object_type = 'SCH'
            sqlss = "SELECT TABLE_NAME FROM USER_TABLES";
        }
        //查询目标端是否出现此表
        List<String> tablename = DBConns.existsTableName(sysDbinfo2, sqlss, source_name, dest_name);
        String sssql = "";
        if (sysDbinfo.getType() == 1) {
            //oracle
            sssql = "SELECT TABLE_NAME FROM DBA_ALL_TABLES WHERE OWNER='" + sysDbinfo.getSchema() + "'AND TEMPORARY='N' AND NESTED='NO'";
        } else if (sysDbinfo.getType() == 2) {
            //mysql
            sssql = "show tables";
        }
        //查詢源端是否存在此表名
        List<String> sourcetablename = DBConns.existsTableName(sysDbinfo, sssql, source_name, dest_name);
        //查看目的端是否存在表名
        if (tablename != null && tablename.size() > 0) {
            return ToDataMessage.builder().status("0").message("目的端表名" + dest_name + "已经存在").build();
        }
        //查看源端是否存在表名
        if (sourcetablename != null && sourcetablename.size() > 0) {
            return ToDataMessage.builder().status("0").message("源端表名" + dest_name + "已经存在").build();
        }
        return ToDataMessage.builder().status("1").build();
    }


    @Transactional
    @Override
//     设置主键就穿字段名称，没有就穿空就行
// 新增日期字段，没有就穿空就行
    public Object editFieldrule(String list_data, String source_name, String dest_name, Long job_id, String primaryKey, String addFile) {
        System.out.println(list_data + "-----------");
        Map<Object, Object> map = new HashMap();
        Integer key = 0;
        SysFieldrule sysFieldrule1 = new SysFieldrule();
        List<SysFieldrule> sysFieldrules = new ArrayList<>();
        List<SysFieldrule> list = new ArrayList<>();
        String sql = "";
        String[] split;
        boolean flag = false;//判断字段是否发生改变
        List<SysDesensitization> sysDesensitizationss = null;//脱敏的表名是否改变
        List<SysFieldrule> sysFieldruleList = null;//配置表的表名是否改变
        List<SysJobrela> sysJobrelaList = null;

        if (list_data != null && source_name != null && dest_name != null && !"".equals(list_data) && !"undefined".equals(dest_name)) {
            split = list_data.replace("$", "@").split(",@,");
            SysTablerule byJobIdAndSourceTable = new SysTablerule();
            SysTablerule byJobIdAndSourceTable2 = null;
            SysDbinfo sysDbinfo = new SysDbinfo();//源端
            SysDbinfo sysDbinfo2 = new SysDbinfo();//目标端
            SysDbinfo sysDbinfo1 = new SysDbinfo();//主任务目标端
            //查詢关联的数据库连接表jobrela
            sysJobrelaList = sysJobrelaRepository.findById(job_id.longValue());
            //查询到数据库连接
            if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
                sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
                sysDbinfo1 = sysDbinfoRespository.findById(sysJobrelaList.get(0).getDestId().longValue());
            } else {
                return ToDataMessage.builder().status("0").message("该任务没有连接").build();
            }
            //查询是否关联的有子任务
            List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(job_id);
            //删除主任务的表规则
            int a = sysTableruleRespository.deleteByJobIdAndSourceTable(job_id, source_name);


            //如果保存过后脱敏规则再修改表名字 也要修改脱敏表的表
            sysDesensitizationss = sysDesensitizationRepository.findByJobIdAndSourceTable(job_id, source_name);
            if (sysDesensitizationss != null && sysDesensitizationss.size() > 0) {
                //只有目的段表名和添加脱敏表名不一致的情况下
                if (!sysDesensitizationss.get(0).getDestTable().equals(dest_name)) {
                    for (SysDesensitization sysDesensitization : sysDesensitizationss) {
                        sysDesensitization.setDestTable(dest_name);
                        sysDesensitizationRepository.save(sysDesensitization);
                    }
                }
            }
            //如果保存后删除规则在修改表名字，也要修改配置表的目的段表名
            sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndVarFlag(job_id, source_name, 1L);

            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                //只有目的段表名和添加过滤字段表名不一致的情况下
                if (!sysFieldruleList.get(0).getDestName().equals(dest_name)) {
                    for (SysFieldrule sysFieldrule : sysFieldruleList) {
                        sysFieldrule.setDestName(dest_name);
                        sysFieldruleRepository.save(sysFieldrule);
                    }
                }
            }
            //查询该任务有没有关联的子任务
            if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                    //先删除表规则字段規則過濾规则
                    sysTableruleRespository.deleteByJobIdAndSourceTable(sysJobrelaRelated.getSlaveJobId(), source_name);
                    sysFieldruleRepository.deleteByJobIdAndSourceName(sysJobrelaRelated.getSlaveJobId(), source_name);
                    sysFilterTableRepository.deleteByJobIdAndFilterTable(sysJobrelaRelated.getSlaveJobId(), source_name);
                    //子任务脱敏修改表名
                    sysDesensitizationss = sysDesensitizationRepository.findByJobIdAndSourceTable(sysJobrelaRelated.getSlaveJobId(), source_name);
                    if (sysDesensitizationss != null && sysDesensitizationss.size() > 0) {
                        if (!sysDesensitizationss.get(0).getDestTable().equals(dest_name)) {
                            for (SysDesensitization sysDesensitization : sysDesensitizationss) {
                                sysDesensitization.setDestTable(dest_name);
                                sysDesensitizationRepository.save(sysDesensitization);
                            }
                        }
                    }
                    //子任务如果保存后删除规则在修改表名字，也要修改配置表的目的段表名
                    sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndVarFlag(sysJobrelaRelated.getSlaveJobId(), source_name, 1L);
                    if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                        //只有目的段表名和添加过滤字段表名不一致的情况下
                        if (!sysFieldruleList.get(0).getDestName().equals(dest_name)) {
                            for (SysFieldrule sysFieldrule : sysFieldruleList) {
                                sysFieldrule.setDestName(dest_name);
                                sysFieldruleRepository.save(sysFieldrule);
                            }
                        }
                    }
                }
            }
            //若源端表和目标端不一致则添加表规则
            if (!source_name.equals(dest_name)) {
                byJobIdAndSourceTable.setDestTable(dest_name);
                byJobIdAndSourceTable.setJobId(job_id);
                byJobIdAndSourceTable.setSourceTable(source_name);
                byJobIdAndSourceTable.setVarFlag(Long.valueOf(2));//2代表映射
                sysTableruleRespository.save(byJobIdAndSourceTable);
                //如果先删除字段后再修改表的名字也要修改字段表的表名
                //若有子任务为子任务添加表规则
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        byJobIdAndSourceTable2 = new SysTablerule();
                        byJobIdAndSourceTable2.setDestTable(dest_name);
                        byJobIdAndSourceTable2.setJobId(sysJobrelaRelated.getSlaveJobId());
                        byJobIdAndSourceTable2.setSourceTable(source_name);
                        byJobIdAndSourceTable2.setVarFlag(Long.valueOf(2));
                        sysTableruleRespository.save(byJobIdAndSourceTable2);
                    }
                }
            }
            //删除主任务同步字段规则
            sysFieldruleRepository.deleteByJobIdAndSourceName(job_id, source_name);
//            //todo 修改了新增的的日期字段
            if (!"".equals(addFile) && addFile != null && !"undefined".equals(addFile)) {
//                List<SysFieldrule> addFileUpate = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(job_id, source_name, 1);
//                if (addFileUpate != null && addFileUpate.size() > 0) {
//                    addFileUpate.get(0).setDestFieldName(addFile);
//                }
//                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
//                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
//                        addFileUpate = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(sysJobrelaRelated.getSlaveJobId(), source_name, 1);
//                        if (addFileUpate != null && addFileUpate.size() > 0) {
//                            addFileUpate.get(0).setDestFieldName(addFile);
//                        }
//                    }
//                }

                SysFieldrule build1 = new SysFieldrule();
                build1.setDestFieldName(addFile);
                build1.setJobId(job_id);
                build1.setType("DATE");
                build1.setNotNull(0L);
                build1.setAccuracy(String.valueOf(0));
                build1.setScale(String.valueOf(0L));
                build1.setSourceName(source_name);
                build1.setDestName(dest_name);
                build1.setVarFlag(Long.valueOf(2));
                build1.setAddFlag(1);
                if (primaryKey.equals(addFile)) {
                    build1.setPrimaryKey(1L);
                } else {
                    build1.setPrimaryKey(0L);
                }
                sysFieldrules.add(repository.save(build1));
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        SysFieldrule build2 = new SysFieldrule();
                        build2.setDestFieldName(addFile);
                        build2.setJobId(sysJobrelaRelated.getSlaveJobId());
                        build2.setType("DATE");
                        build1.setNotNull(0L);
                        build1.setAccuracy(String.valueOf(0));
                        build1.setScale(String.valueOf(0L));
                        build2.setSourceName(source_name);
                        build2.setDestName(dest_name);
                        build2.setVarFlag(Long.valueOf(2));
                        build2.setAddFlag(1);
                        if (primaryKey.equals(addFile)) {
                            build2.setPrimaryKey(1L);
                        } else {
                            build2.setPrimaryKey(0L);
                        }
                        sysFieldrules.add(repository.save(build2));
                    }
                }
            }


            List<SysDesensitization> desensitizationList = null;//同步的字段脱敏修改
            String[] ziduan = null;
            List<SysFiledType> sysFiledTypeList = null;//查询映射规则表
            SysFiledType sysFiledType = null;//映射表的记录
            for (String s : split) {
                ziduan = s.split(",");
                if (ziduan[5] == null || "".equals(ziduan[5])) {
                    ziduan[5] = "0";
                }
                //todo 同步的字段已有脱敏规则后，又修改的字段的名称，所以在这里要更新下脱敏表
                desensitizationList = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(job_id, source_name, ziduan[0]);
                if (desensitizationList != null && desensitizationList.size() > 0) {
                    if (!ziduan[1].equals(desensitizationList.get(0).getDestField())) {
                        desensitizationList.get(0).setDestField(ziduan[1]);
                    }
                }
                //若有子任务也修改规则
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        desensitizationList = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(sysJobrelaRelated.getSlaveJobId(), source_name, ziduan[0]);
                        if (desensitizationList != null && desensitizationList.size() > 0) {
                            if (!ziduan[1].equals(desensitizationList.get(0).getDestField())) {
                                desensitizationList.get(0).setDestField(ziduan[1]);
                            }
                        }
                    }
                }


                //todo  字段类型在数据库查不到，就默认和源端一致
                //若字段改变则存入字段规则表
                sysFiledTypeList = sysFiledTypeRepository.findBySourceTypeAndDestTypeAndSourceFiledType(String.valueOf(sysDbinfo.getType()), String.valueOf(sysDbinfo1.getType()), ziduan[6].toUpperCase());

                if (sysFiledTypeList == null || sysFiledTypeList.size() <= 0) {
                    sysFiledType = new SysFiledType();
                    sysFiledType.setDestFiledType(ziduan[6]);
                    sysFiledTypeList.add(sysFiledType);
                }

                if (!ziduan[0].equals(ziduan[1]) || !ziduan[3].equals(ziduan[7]) || !ziduan[4].equals(ziduan[8]) || !ziduan[5].equals(ziduan[9]) || !sysFiledTypeList.get(0).getDestFiledType().toUpperCase().equals(ziduan[2].toUpperCase()) || (!"".equals(primaryKey) && primaryKey != null && !"undefined".equals(primaryKey) && primaryKey.equals(ziduan[1]))) {
//                if (!ziduan[0].equals(ziduan[1])) {
                    SysFieldrule build = new SysFieldrule();
                    build.setFieldName(ziduan[0]);
                    build.setDestFieldName(ziduan[1]);
                    build.setJobId(job_id);
                    build.setType(ziduan[2]);
                    build.setScale(ziduan[3]);
                    build.setNotNull(Long.valueOf(ziduan[4]));
                    build.setAccuracy(ziduan[5]);
                    build.setSourceName(source_name);
                    build.setDestName(dest_name);
                    build.setVarFlag(Long.valueOf(2));
                    //todo 主键策略
                    if (primaryKey.equals(ziduan[1])) {
                        build.setPrimaryKey(1L);
                    } else {
                        build.setPrimaryKey(0L);
                    }

                    sysFieldrules.add(repository.save(build));
                    //若有子任务也保存规则
                    if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                        for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                            //查询子任务的目标端是什么类型
                            sysJobrelaList = sysJobrelaRepository.findById(sysJobrelaRelated.getSlaveJobId().longValue());
                            sysDbinfo2 = sysDbinfoRespository.findById(sysJobrelaList.get(0).getDestId().longValue());
                            sysFiledTypeList = sysFiledTypeRepository.findBySourceTypeAndDestTypeAndSourceFiledType(String.valueOf(sysDbinfo.getType()), String.valueOf(sysDbinfo2.getType()), ziduan[6].toUpperCase());
                            SysFieldrule builds = new SysFieldrule();
                            builds.setFieldName(ziduan[0]);
                            builds.setDestFieldName(ziduan[1]);
                            builds.setJobId(sysJobrelaRelated.getSlaveJobId());
                            if (sysFiledTypeList != null && sysFiledTypeList.size() > 0) {
                                if(sysDbinfo2.getType()==2||sysDbinfo2.getType()==3){
                                    builds.setType(sysFiledTypeList.get(0).getDestFiledType().toLowerCase());
                                }else {
                                    builds.setType(sysFiledTypeList.get(0).getDestFiledType());
                                }
                            } else {
                                if(sysDbinfo2.getType()==2||sysDbinfo2.getType()==3) {
                                    builds.setType(ziduan[6].toLowerCase());
                                }else{
                                    builds.setType(ziduan[6]);
                                }
                            }
                            builds.setScale(ziduan[3]);
                            builds.setNotNull(Long.valueOf(ziduan[4]));
                            builds.setAccuracy(ziduan[5]);
                            builds.setSourceName(source_name);
                            builds.setDestName(dest_name);
                            builds.setVarFlag(Long.valueOf(2));
                            //todo 主键策略
                            if (primaryKey.equals(ziduan[1])) {
                                builds.setPrimaryKey(1L);
                            } else {
                                builds.setPrimaryKey(0L);
                            }

                            sysFieldrules.add(repository.save(builds));
                        }
                    }
                    flag = true;
                }
            }
            //表相同但是字段发生映射改变，表也要存入tablerule
            if (source_name.equals(dest_name)&&flag) {
                byJobIdAndSourceTable = new SysTablerule();
                byJobIdAndSourceTable.setDestTable(dest_name);
                byJobIdAndSourceTable.setJobId(job_id);
                byJobIdAndSourceTable.setSourceTable(source_name);
                byJobIdAndSourceTable.setVarFlag(Long.valueOf(2));//2代表映射
                sysTableruleRespository.save(byJobIdAndSourceTable);
                //若有子任务为子任务添加表规则
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        byJobIdAndSourceTable2 = new SysTablerule();
                        byJobIdAndSourceTable2.setDestTable(dest_name);
                        byJobIdAndSourceTable2.setJobId(sysJobrelaRelated.getSlaveJobId());
                        byJobIdAndSourceTable2.setSourceTable(source_name);
                        byJobIdAndSourceTable2.setVarFlag(Long.valueOf(2));
                        sysTableruleRespository.save(byJobIdAndSourceTable2);
                    }
                }
            }
//            if (sysDbinfo.getType() == 2) {
//                sql = "select Column_Name as ColumnName,data_type as TypeName, (case when data_type = 'float' or data_type = 'int' or data_type = 'datetime' or data_type = 'bigint' or data_type = 'double' or data_type = 'decimal' then NUMERIC_PRECISION else CHARACTER_MAXIMUM_LENGTH end ) as length, NUMERIC_SCALE as Scale,(case when IS_NULLABLE = 'YES' then 0 else 1 end) as CanNull  from information_schema.columns where table_schema ='" + sysDbinfo.getDbname() + "' and table_name='" + source_name + "'";
//            } else if (sysDbinfo.getType() == 1) {
//                sql = "SELECT COLUMN_NAME, DATA_TYPE, NVL(DATA_LENGTH,0), NVL(DATA_PRECISION,0), NVL(DATA_SCALE,0), NULLABLE, COLUMN_ID ,DATA_TYPE_OWNER FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + source_name
//                        + "' AND OWNER='" + sysDbinfo.getSchema() + "'";
//            }
//            //不同步的字段
//            list = DBConns.getResult(sysDbinfo, sql, list_data);
//            SysFilterTable sysFilterTable = null;
//            sysFilterTableRepository.deleteByJobIdAndFilterTable(job_id, source_name);
//            List<SysDesensitization> sysDesensitizations = null;//查询不同步字段是否有脱敏规则
//            for (SysFieldrule sysFieldrule : list) {
//                //todo 不同步的字段之前添加过脱敏规则要删除
//                sysDesensitizations = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(job_id, source_name, sysFieldrule.getFieldName());
//                if (sysDesensitizations != null && sysDesensitizations.size() > 0) {
//                    sysDesensitizationRepository.delete(sysDesensitizations.get(0));
//                }
//                sysFilterTable = new SysFilterTable();
//                sysFilterTable.setFilterTable(source_name);
//                sysFilterTable.setJobId(job_id);
//                sysFilterTable.setFilterField(sysFieldrule.getFieldName());
//                sysFilterTableRepository.save(sysFilterTable);
//                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
//                    SysFilterTable sysFilterTable2 = null;
//                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
//                        //todo 子任务不同步的字段之前添加过脱敏规则要删除
//                        sysDesensitizations = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(sysJobrelaRelated.getSlaveJobId(), source_name, sysFieldrule.getFieldName());
//                        if (sysDesensitizations != null && sysDesensitizations.size() > 0) {
//                            sysDesensitizationRepository.delete(sysDesensitizations.get(0));
//                        }
//
//                        sysFilterTable2 = new SysFilterTable();
//                        sysFilterTable2.setFilterTable(source_name);
//                        sysFilterTable2.setJobId(sysJobrelaRelated.getSlaveJobId());
//                        sysFilterTable2.setFilterField(sysFieldrule.getFieldName());
//                        sysFilterTableRepository.save(sysFilterTable2);
//                    }
//                }
//            }

            //修改任务的状态为未激活
            long job_id1 = job_id;
            SysJobrela sysJobrelaById = sysJobrelaRespository.findById(job_id1);
            sysJobrelaById.setJobStatus("0");
            //修改子任务的状态未激活
            List<SysJobrelaRelated> lists = sysJobrelaRelatedRespository.findByMasterJobId(job_id);
            if (lists != null && lists.size() > 0) {
                for (SysJobrelaRelated sysJobrelaRelated : lists) {
                    Optional<SysJobrela> sysJobrelaByIds = sysJobrelaRespository.findById(sysJobrelaRelated.getSlaveJobId());
                    sysJobrelaByIds.get().setJobStatus("0");
                }
            }

            map.put("status", 1);
            map.put("message", "保存成功");
            map.put("data", sysFieldrules);
        } else {
            map.put("status", 1);
            map.put("message", "保存成功");
        }
        return map;
    }

    @Transactional
    @Override
    public Object deleteFieldrule(String source_name) {
        Map<Object, Object> map = new HashMap();
        if (repository.deleteBySourceName(source_name)) {
            map.put("status", 1);
            map.put("message", "删除成功");
        } else {
            map.put("status", 0);
            map.put("message", "没有找到删除目标");
        }
        return map;
    }

    //查看源端表字段
    @Override
    public Object linkTableDetails(SysDbinfo sysDbinfo, String tablename, Long job_id) {
        HashMap<Object, Object> map = new HashMap<>();

        if (sysDbinfo == null) {
            //查詢关联的数据库连接表jobrela
            List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(job_id.longValue());
            //查询到数据库连接
            if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
                sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
            } else {
                map.put("status", "0");
                map.put("message", "该任务没有连接");
                return map;
            }
        }
        Long type = sysDbinfo.getType();
        String sql = "";
        ArrayList<Object> data = new ArrayList<>();
        List<SysFieldrule> list = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        SysFieldrule sysFieldrule = null;
        if (type == 2) {
            sql = "select Column_Name as ColumnName,data_type as TypeName, (case when data_type = 'float' or data_type = 'int' or data_type = 'datetime' or data_type = 'bigint' or data_type = 'double' or data_type = 'decimal' then NUMERIC_PRECISION else CHARACTER_MAXIMUM_LENGTH end ) as length, NUMERIC_SCALE as Scale,(case when IS_NULLABLE = 'YES' then 0 else 1 end) as CanNull  from information_schema.columns where table_schema ='" + sysDbinfo.getDbname() + "' and table_name='" + tablename + "'";
            try {
                conn = DBConns.getMySQLConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    sysFieldrule = new SysFieldrule();
                    sysFieldrule.setFieldName(rs.getString("ColumnName"));
                    sysFieldrule.setType(rs.getString("TypeName"));
                    if (rs.getString("length") != null && !"".equals(rs.getString("length"))) {
                        sysFieldrule.setScale(rs.getString("length"));
                    } else {
                        sysFieldrule.setScale("0");
                    }
                    sysFieldrule.setNotNull(Long.valueOf(rs.getString("CanNull")));
                    if (rs.getString("Scale") != null && !"".equals(rs.getString("Scale"))) {
                        sysFieldrule.setAccuracy(rs.getString("Scale"));
                    } else {
                        sysFieldrule.setAccuracy("0");
                    }

                    list.add(sysFieldrule);
                }
            } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*连接异常" + stackTraceElement.getLineNumber() + e);
                e.printStackTrace();
                return map;
            }
        } else if (type == 1) {
            sql = "SELECT COLUMN_NAME, DATA_TYPE, NVL(DATA_LENGTH,0), NVL(DATA_PRECISION,0), NVL(DATA_SCALE,0), NULLABLE, COLUMN_ID ,DATA_TYPE_OWNER FROM DBA_TAB_COLUMNS WHERE TABLE_NAME='" + tablename + "' AND OWNER='" + sysDbinfo.getSchema() + "'";

            try {
                conn = DBConns.getOracleConn(sysDbinfo);
                stmt = conn.prepareStatement(sql);
                rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    sysFieldrule = new SysFieldrule();
                    sysFieldrule.setFieldName(rs.getString("COLUMN_NAME"));
                    sysFieldrule.setType(rs.getString("DATA_TYPE"));
                    if (rs.getString("NVL(DATA_LENGTH,0)") != null && !"".equals(rs.getString("NVL(DATA_LENGTH,0)"))) {
                        sysFieldrule.setScale(rs.getString("NVL(DATA_LENGTH,0)"));
                    } else {
                        sysFieldrule.setScale("0");
                    }
                    if (rs.getString("NULLABLE").equals("Y")) {
                        sysFieldrule.setNotNull(Long.valueOf(0));
                    } else {
                        sysFieldrule.setNotNull(Long.valueOf(1));
                    }
                    if (rs.getString("NVL(DATA_SCALE,0)") != null && !"".equals(rs.getString("NVL(DATA_SCALE,0)"))) {
                        sysFieldrule.setAccuracy(rs.getString("NVL(DATA_SCALE,0)"));
                    } else {
                        sysFieldrule.setAccuracy("0");
                    }
                    list.add(sysFieldrule);
                }
            } catch (SQLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                StackTraceElement stackTraceElement = e.getStackTrace()[0];
                logger.error("*连接异常" + stackTraceElement.getLineNumber() + e);
                e.printStackTrace();
                return map;
            }
        }
        try {
            DBConns.close(stmt, conn, rs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // List<SysFieldrule> sysFieldruleList= sysFieldruleRepository.findByJobIdAndSourceName(job_id,tablename);
        map.put("status", "1");
        map.put("data", list);
        return map;
    }

    //查看目标端表字段
    @Override
    public Object DestlinkTableDetails(SysDbinfo sysDbinfo, String tablename, Long job_id) {
        HashMap<Object, Object> map = new HashMap<>();
        HashMap<Object, Object> haspmap = new HashMap<>();
        ArrayList<SysFieldrule> data = new ArrayList<>();

        List<SysFieldrule> sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceName(job_id, tablename, 1);
//        List<SysFieldrule> list = sysFieldruleRepository.findByJobIdAndSourceNameAndVarFlag(job_id, tablename, Long.valueOf(2));
        List<SysDesensitization> sysDesensitizations = sysDesensitizationRepository.findByJobIdAndSourceTable(job_id, tablename);
        try {
            if (sysDbinfo == null) {
                //查詢关联的数据库连接表jobrela
                List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(job_id.longValue());
                //查询到数据库连接
                if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
                    sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
                } else {
                    return ToDataMessage.builder().status("0").message("该任务没有连接").build();
                }
            }
            List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(job_id.longValue());
            //查询目的数据库连接

            SysDbinfo sysDbinfo2 = sysDbinfoRespository.findById(sysJobrelaList.get(0).getDestId().longValue());
            List<SysFiledType> sysFiledTypeList = null;
            //拿到源端的字段集合
            haspmap = (HashMap<Object, Object>) linkTableDetails(sysDbinfo, tablename, job_id);
            data = (ArrayList<SysFieldrule>) haspmap.get("data");
            //先把类型转换为目标端的类型
            for (int i = 0; i < data.size(); i++) {
                sysFiledTypeList = new ArrayList<>();
                //若果目的表字段有值就不换，无值就把源端的
                if (data.get(i).getDestFieldName() == null || "".equals(data.get(i).getDestFieldName())) {
                    data.get(i).setDestFieldName(data.get(i).getFieldName());
                }
                //oracle转化大写
//                if(sysDbinfo2.getType()==1){
//                    data.get(i).setDestFieldName(data.get(i).getDestFieldName().toUpperCase());
//                }else{
//                    data.get(i).setDestFieldName(data.get(i).getDestFieldName());
//                }
                if (sysDbinfo.getType() != sysDbinfo2.getType()) {
                    //oracle到达梦和oracle到oracle一样
                    if (sysDbinfo.getType() == 1 && sysDbinfo2.getType() == 4) {
                        System.out.println("oracle到达梦和oracle到oracle一样");
                    } else {
                        //去找到映射的字段类型
                        sysFiledTypeList = sysFiledTypeRepository.findBySourceTypeAndDestTypeAndSourceFiledType(String.valueOf(sysDbinfo.getType()), String.valueOf(sysDbinfo2.getType()), data.get(i).getType().toUpperCase());
                        if (sysFiledTypeList != null && sysFiledTypeList.size() > 0) {
                            //todo 如果是mysql 和sqlserver 换成小写
                            if(sysDbinfo2.getType()==2||sysDbinfo2.getType()==3){
                                data.get(i).setType(sysFiledTypeList.get(0).getDestFiledType().toLowerCase());
                            }else {
                                data.get(i).setType(sysFiledTypeList.get(0).getDestFiledType());
                            }
                        }else{
                            //todo 如果是mysql 和sqlserver 换成小写
                            if(sysDbinfo2.getType()==2||sysDbinfo2.getType()==3){
                                data.get(i).setType(data.get(i).getType().toLowerCase());
                            }else {
                                data.get(i).setType(data.get(i).getType());
                            }
                        }
                    }
                }
            }
            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                //做过修改的替换进来，其余不变
                for (SysFieldrule sysFieldrule : sysFieldruleList) {
                    for (int i = 0; i < data.size(); i++) {
                        if (sysFieldrule.getFieldName().equals(data.get(i).getFieldName())) {
                            data.set(i, sysFieldrule);
                        }
                    }
                }
            }

            //判断是否有新增的字段
            List<SysFieldrule> sysFieldrule = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(job_id, tablename,1);
            if (sysFieldrule != null && sysFieldrule.size() > 0) {
                for (SysFieldrule sysFieldrule1 : sysFieldrule) {
                    sysFieldrule1.setFieldName(sysFieldrule1.getDestFieldName());
                    data.add(sysFieldrule1);
                }
            }
            map.put("status", "1");
            map.put("data", data);
            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                map.put("destName", sysFieldruleList.get(0).getDestName());
            } else {
                List<SysTablerule> sysTablerules = sysTableruleRespository.findByJobIdAndSourceTableAndVarFlag(job_id, tablename, 2L);
                if (sysTablerules != null && sysTablerules.size() > 0) {
                    map.put("destName", sysTablerules.get(0).getDestTable());
                } else {
                    map.put("destName", tablename);
                }
            }
            //todo tuomin
            map.put("data2", sysDesensitizations);
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*" + stackTraceElement.getLineNumber() + e);
            e.printStackTrace();
        }
        return map;
    }

    //批量删除字段 假删除
    @Transactional
    public Object deleteAll(String list_data, String source_name, String dest_name, Long job_id) {
        SysFilterTable sysFilterTable = null;
        SysFieldrule sysFieldrule = null;
        String[] filedNames = null;
        HashMap<Object, Object> map = new HashMap<>();
        List<SysDesensitization> sysDesensitizations = null;
        List<SysFieldrule> sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(job_id, source_name, 1);
        if (list_data != null && !"".equals(list_data) && !"undefined".equals(list_data)) {
            List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(job_id);
            filedNames = list_data.split(",");
            String fieldName = null;
            //todo  该表有新增的字段
            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                //把新增的字段名称赋值
                fieldName = sysFieldruleList.get(0).getDestFieldName();
            }
            //比较是不是删除新增的字段
            if (filedNames[0].equals(fieldName)) {
                //这是删除新增的字段
                sysFieldruleList.get(0).setVarFlag(Long.valueOf(1));
                //子任务
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(sysJobrelaRelated.getSlaveJobId(), source_name, 1);
                       if(sysFieldruleList!=null&&sysFieldruleList.size()>0) {
                           sysFieldruleList.get(0).setVarFlag(Long.valueOf(1));
                       }
                    }
                }
            } else {
                //todo 这是没有新增字段的删除
                //fieldrule表  为了前端展示
              List<SysFieldrule> sysFieldruleList1=  sysFieldruleRepository.findByJobIdAndSourceNameAndFieldName(job_id,source_name,filedNames[0]);
                if(sysFieldruleList1!=null&&sysFieldruleList1.size()>0){
                    sysFieldruleRepository.deleteByJobIdAndSourceNameAndFieldName(job_id,source_name,filedNames[0]);
                }
                sysFieldrule = new SysFieldrule();
                sysFieldrule.setDestFieldName(filedNames[1]);
                sysFieldrule.setFieldName(filedNames[0]);
                sysFieldrule.setJobId(job_id);
                sysFieldrule.setType(filedNames[2]);
                sysFieldrule.setNotNull(Long.valueOf(filedNames[4]));
                sysFieldrule.setAccuracy(filedNames[5]);
                sysFieldrule.setScale(filedNames[3]);
                sysFieldrule.setSourceName(source_name);
                sysFieldrule.setDestName(dest_name);
                sysFieldrule.setVarFlag(Long.valueOf(1));
                sysFieldruleRepository.save(sysFieldrule);
                //过滤表 这个是为春哥用的
                sysFilterTable = new SysFilterTable();
                sysFilterTable.setFilterTable(source_name);
                sysFilterTable.setJobId(job_id);
                sysFilterTable.setFilterField(filedNames[0]);
                sysFilterTableRepository.save(sysFilterTable);
                //查询要删除字段的脱敏
                sysDesensitizations = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(job_id, source_name, filedNames[0]);
                if (sysDesensitizations != null && sysDesensitizations.size() > 0) {
                    sysDesensitizationRepository.delete(sysDesensitizations.get(0));
                }
                //如果存在子任务，那么子任务也要加上
                if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                    for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                        //todo 子任务不同步的字段之前添加过脱敏规则要删除
                        sysDesensitizations = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(sysJobrelaRelated.getSlaveJobId(), source_name, filedNames[0]);
                        if (sysDesensitizations != null && sysDesensitizations.size() > 0) {
                            sysDesensitizationRepository.delete(sysDesensitizations.get(0));
                        }
                        sysFieldruleList1=  sysFieldruleRepository.findByJobIdAndSourceNameAndFieldName(sysJobrelaRelated.getSlaveJobId(),source_name,filedNames[0]);
                        if(sysFieldruleList1!=null&&sysFieldruleList1.size()>0){
                            sysFieldruleRepository.deleteByJobIdAndSourceNameAndFieldName(sysJobrelaRelated.getSlaveJobId(),source_name,filedNames[0]);
                        }
                        sysFieldrule = new SysFieldrule();
                        sysFieldrule.setDestFieldName(filedNames[1]);
                        sysFieldrule.setFieldName(filedNames[0]);
                        sysFieldrule.setJobId(sysJobrelaRelated.getSlaveJobId());
                        sysFieldrule.setType(filedNames[2]);
                        sysFieldrule.setNotNull(Long.valueOf(filedNames[4]));
                        sysFieldrule.setAccuracy(filedNames[5]);
                        sysFieldrule.setScale(filedNames[3]);
                        sysFieldrule.setSourceName(source_name);
                        sysFieldrule.setDestName(dest_name);
                        sysFieldrule.setVarFlag(Long.valueOf(1));
                        sysFieldruleRepository.save(sysFieldrule);
                        //过滤表 这个是为春哥用的
                        sysFilterTable = new SysFilterTable();
                        sysFilterTable.setFilterTable(source_name);
                        sysFilterTable.setJobId(sysJobrelaRelated.getSlaveJobId());
                        sysFilterTable.setFilterField(filedNames[0]);
                        sysFilterTableRepository.save(sysFilterTable);
                    }
                }
            }
            map.put("status", "1");
            map.put("message", "删除成功");
        } else {
            map.put("status", "1");
            map.put("message", "请先选择删除的字段");
        }
        return map;
    }

    //恢复字段list_data 需要源端字段和目标端字段
    @Transactional
    public Object recover(String sourceField, String destField, String source_name, String dest_name, Long job_id) {
        SysFieldrule sysFieldrule = null;
        HashMap<Object, Object> map = new HashMap<>();
        Integer result = null;
        String[] fieldNames = null;
        List<SysFieldrule> sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(job_id, source_name, 1);
        List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(job_id);
        String fieldName = null;
        //todo  该表有新增的字段
        if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
            //把新增的字段名称赋值
            fieldName = sysFieldruleList.get(0).getDestFieldName();
        }
        //比较是不是删除新增的字段
        if (sourceField.equals(fieldName)) {
            //这是删除新增的字段
            sysFieldruleList.get(0).setVarFlag(Long.valueOf(2));
            //子任务
            if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                    sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(sysJobrelaRelated.getSlaveJobId(), source_name, 1);
                   if(sysFieldruleList!=null&&sysFieldruleList.size()>0) {
                       sysFieldruleList.get(0).setVarFlag(Long.valueOf(2));
                   }
                }
            }
            result=1;
        } else {
            result = sysFieldruleRepository.deleteByJobIdAndSourceNameAndDestNameAndFieldName(job_id, source_name, dest_name, sourceField);
            //删除过滤表
            sysFilterTableRepository.deleteByJobIdAndFilterTableAndFilterField(job_id, source_name, sourceField);
            //如果存在子任务，那么子任务也要加上
            if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
                for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                    //删除过滤表
                    sysFilterTableRepository.deleteByJobIdAndFilterTableAndFilterField(sysJobrelaRelated.getSlaveJobId(), source_name, sourceField);
                    sysFieldruleRepository.deleteByJobIdAndSourceNameAndDestNameAndFieldName(sysJobrelaRelated.getSlaveJobId(), source_name, dest_name, sourceField);
                }
            }
        }
        if (result != null) {
            map.put("status", "1");
            map.put("message", "恢复成功");
        } else {
            map.put("status", "0");
            map.put("message", "恢复失败");
        }
        return map;
    }


    //批量脱敏之前的在弹窗内显示的数据
    public Object showFieldrule(SysDbinfo sysDbinfo, String tablename, Long job_id) {
        HashMap<Object, Object> map = new HashMap<>();
        HashMap<Object, Object> haspmap = new HashMap<>();
        ArrayList<SysFieldrule> data = new ArrayList<>();
        List<SysDesensitization> sysDesensitizationList = new ArrayList<>();//返回前端的集合
        SysDesensitization sysDesensitization = null;
        String destTable = null;
        List<SysFieldrule> sysFieldruleList = null;
        List<SysTablerule> sysTableruleList = null;
        //查询字段规则表中是否有数据
        try {
            sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceName(job_id, tablename);
            //查询表规则
            sysTableruleList = sysTableruleRespository.findByJobIdAndSourceTableAndVarFlag(job_id, tablename, 2L);

            if (sysDbinfo == null) {
                //查詢关联的数据库连接表jobrela
                List<SysJobrela> sysJobrelaList = sysJobrelaRepository.findById(job_id.longValue());
                //查询到数据库连接
                if (sysJobrelaList != null && sysJobrelaList.size() > 0) {
                    sysDbinfo = sysDbinfoRespository.findById(sysJobrelaList.get(0).getSourceId().longValue());
                } else {
                    return ToDataMessage.builder().status("0").message("该任务没有连接").build();
                }
            }
            haspmap = (HashMap<Object, Object>) linkTableDetails(sysDbinfo, tablename, job_id);
            data = (ArrayList<SysFieldrule>) haspmap.get("data");//源端的字段集合
            //如果字段配置表查不到 那就说说明用户还未修改字段规则，则源端字段和目标端一致
            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                //先把类型转换为目标端的类型
                for (int i = 0; i < data.size(); i++) {
                    for (int j = 0; j < sysFieldruleList.size(); j++) {
                        if (sysFieldruleList.get(j).getVarFlag() == 1) {
                            //如果是过滤（删除）的字段那么在这里也删掉
                            if (data.get(i).getFieldName().equals(sysFieldruleList.get(j).getFieldName())) {
                                data.remove(i);
                                //因为list是动态的 删除后 下标会前移 所以i--
                                if (i == 0) {
                                    i = 0;
                                } else {
                                    i--;
                                }
                            }
                        } else if (sysFieldruleList.get(j).getVarFlag() == 2) {
                            //如果是要同步的字段，那就把目的段字段给他
                            if (data.get(i).getFieldName().equals(sysFieldruleList.get(j).getFieldName())) {
                                data.get(i).setDestFieldName(sysFieldruleList.get(j).getDestFieldName());
                            }
                        }
                    }
                }
            }

            //如果表规则里面有表 那就用 没有就把远端的给目的段
            if (sysTableruleList != null && sysTableruleList.size() > 0) {
                destTable = sysTableruleList.get(0).getDestTable(); //目的段表名
            } else {
                destTable = tablename;
            }
            List<SysDesensitization> sysDesensitizations1 = null;
            for (SysFieldrule sysFieldrule : data) {
                //查询那张表那个字段的脱敏规则
                sysDesensitizations1 = sysDesensitizationRepository.findByJobIdAndSourceTableAndSourceField(job_id, tablename, sysFieldrule.getFieldName());
                sysDesensitization = new SysDesensitization();
                sysDesensitization.setDestTable(destTable);//目的段表名
                sysDesensitization.setSourceTable(tablename);//源端表名
                sysDesensitization.setSourceField(sysFieldrule.getFieldName());//源端字段
                if (sysFieldrule.getDestFieldName() != null) {
                    sysDesensitization.setDestField(sysFieldrule.getDestFieldName());//目的段字段
                } else {
                    sysDesensitization.setDestField(sysFieldrule.getFieldName());//目的段字段
                }
                if (sysDesensitizations1 != null && sysDesensitizations1.size() > 0) {
                    //该字段的脱敏方式
                    sysDesensitization.setDesensitizationWay(sysDesensitizations1.get(0).getDesensitizationWay());
                    if (sysDesensitizations1.get(0).getRemark() != null) {
                        //该字段的脱敏规则
                        sysDesensitization.setRemark(sysDesensitizations1.get(0).getRemark());
                    }
                }
                sysDesensitizationList.add(sysDesensitization);
            }
            map.put("status", "1");
            map.put("data", sysDesensitizationList);
        } catch (Exception e) {
            StackTraceElement stackTraceElement = e.getStackTrace()[0];
            logger.error("*" + stackTraceElement.getLineNumber() + e);
            e.printStackTrace();
        }
        return map;
    }

    public Object addField(Long jobId, String sourceTable, String destTable) {
        HashMap<Object, Object> map = new HashMap<>();
        List<SysJobrelaRelated> sysJobrelaRelateds = sysJobrelaRelatedRespository.findByMasterJobId(jobId);
        SysFieldrule sysFieldrule = new SysFieldrule();
        sysFieldrule.setDestFieldName("add_Time");
        sysFieldrule.setJobId(jobId);
        sysFieldrule.setType("DATE");
        sysFieldrule.setNotNull(0L);
        sysFieldrule.setAccuracy(String.valueOf(0));
        sysFieldrule.setScale(String.valueOf(0L));
        sysFieldrule.setSourceName(sourceTable);
        sysFieldrule.setDestName(destTable);
        sysFieldrule.setVarFlag(Long.valueOf(2));
        sysFieldrule.setAddFlag(1);
        sysFieldrule.setPrimaryKey(0L);
        SysFieldrule sysFieldrule1 = sysFieldruleRepository.save(sysFieldrule);
        if (sysJobrelaRelateds != null && sysJobrelaRelateds.size() > 0) {
            for (SysJobrelaRelated sysJobrelaRelated : sysJobrelaRelateds) {
                sysFieldrule = new SysFieldrule();
                sysFieldrule.setDestFieldName("add_Time");
                sysFieldrule.setJobId(sysJobrelaRelated.getSlaveJobId());
                sysFieldrule.setType("DATE");
                sysFieldrule.setNotNull(0L);
                sysFieldrule.setAccuracy(String.valueOf(0));
                sysFieldrule.setScale(String.valueOf(0L));
                sysFieldrule.setSourceName(sourceTable);
                sysFieldrule.setDestName(destTable);
                sysFieldrule.setVarFlag(Long.valueOf(2));
                sysFieldrule.setAddFlag(1);
                sysFieldrule.setPrimaryKey(0L);
                sysFieldruleRepository.save(sysFieldrule);
            }
        }
        sysFieldrule1.setFieldName("add_Time");
        map.put("status", "1");
        map.put("data", sysFieldrule1);
        return map;
    }

}
