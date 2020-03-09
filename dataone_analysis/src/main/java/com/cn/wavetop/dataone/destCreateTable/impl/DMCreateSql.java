package com.cn.wavetop.dataone.destCreateTable.impl;

import com.cn.wavetop.dataone.config.SpringContextUtil;
import com.cn.wavetop.dataone.dao.SysFieldruleRepository;
import com.cn.wavetop.dataone.dao.SysFiledTypeRepository;
import com.cn.wavetop.dataone.dao.SysTableruleRepository;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.destCreateTable.SuperCreateTable;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.entity.SysFieldrule;
import com.cn.wavetop.dataone.entity.SysFiledType;
import com.cn.wavetop.dataone.service.JobRelaServiceImpl;
import com.cn.wavetop.dataone.util.DBConns;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DMCreateSql implements SuperCreateTable {
    private final SysTableruleRepository sysTableruleRepository = (SysTableruleRepository) SpringContextUtil.getBean("sysTableruleRepository");
    private final SysFieldruleRepository sysFieldruleRepository = (SysFieldruleRepository) SpringContextUtil.getBean("sysFieldruleRepository");
    private final SysFiledTypeRepository sysFiledTypeRepository = (SysFiledTypeRepository) SpringContextUtil.getBean("sysFiledTypeRepository");
    private JobRelaServiceImpl jobRelaServiceImpl = (JobRelaServiceImpl) SpringContextUtil.getBean("jobRelaServiceImpl");;
    /**
     * DM不能携带长度的类型
     */
    public static String[]Types={
            "DATE","TIMESTAMP"
    };
    /**
     * DM需要携带精度的类型
     */
    public static String[]AccuracyTypes={
            "NUMBER"
    };

    /**
     * 判断是否包含不能携带长度的类型
     * 包含返回true，否则false
     * @param type
     * @return
     */
    public static boolean equalsType(String type){
        for (int i = 0; i <Types.length ; i++) {
            if(Types[i].equals(type)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * 判断是否包含能携带精度的类型
     * 包含返回true，否则false
     * @param type
     * @return
     */
    public static boolean equalsAccuracyTypes(String type){
        for (int i = 0; i <AccuracyTypes.length ; i++) {
            if(AccuracyTypes[i].equals(type)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String createTable(Long jobId, String tableName) {
        SysDbinfo sysDbinfo = jobRelaServiceImpl.findDestDbinfoById(jobId);//目标端数据库
        SysDbinfo sourceSysDbinfo = jobRelaServiceImpl.findSourcesDbinfoById(jobId);//源端数据库
        ResultMap list =jobRelaServiceImpl.findSourceFiled(jobId, tableName);//源端的字段信息
        List<SysFieldrule> sysFieldruleList = new ArrayList<>();//目标端的字段信息
        List<SysFiledType> sysFiledTypeList = new ArrayList<>();//目标端的字段类型
        List primaryKey = null;//源端表的主键
        StringBuffer stringBuffer = new StringBuffer("CREATE TABLE");
        stringBuffer.append(" " + sysDbinfo.getSchema() + "." + jobRelaServiceImpl.destTableName(jobId, tableName) + "(");
        System.out.println("该表一共多少个映射字段" + list.size());
        for (int i = 0; i < list.size(); i++) {

            //查询映射目标端的字段信息
            sysFieldruleList = sysFieldruleRepository.findByJobIdAndSourceNameAndFieldName(jobId, tableName, (String) list.get(i).get("COLUMN_NAME"));
            if (sysFieldruleList != null && sysFieldruleList.size() > 0) {
                //如果长度为0或者类型为DATA等类型 不能拼接长度
                if ("0".equals(sysFieldruleList.get(0).getScale()) || equalsType(sysFieldruleList.get(0).getType())) {
                    stringBuffer.append(sysFieldruleList.get(0).getDestFieldName() + " " + sysFieldruleList.get(0).getType());
                } else {
                    //如果精度不为空，类型也为number等类型 拼接精度
                    if (!"0".equals(sysFieldruleList.get(0).getAccuracy()) && equalsType(sysFieldruleList.get(0).getType())) {
                        stringBuffer.append(sysFieldruleList.get(0).getDestFieldName() + " " + sysFieldruleList.get(0).getType() + "(" + sysFieldruleList.get(0).getScale() + ","+sysFieldruleList.get(0).getAccuracy()+")");
                    } else {
                        stringBuffer.append(sysFieldruleList.get(0).getDestFieldName() + " " + sysFieldruleList.get(0).getType() + "(" + sysFieldruleList.get(0).getScale() + ")");

                    }
                }
                if (sysFieldruleList.get(0).getNotNull() == 1) {
                    stringBuffer.append(" NOT NULL");
                }
            } else {
                //目标端的字段类型
                sysFiledTypeList = sysFiledTypeRepository.findBySourceTypeAndDestTypeAndSourceFiledType(String.valueOf(sourceSysDbinfo.getType()), String.valueOf(sysDbinfo.getType()), (String) list.get(i).get("DATA_TYPE"));
                //如果长度为0或者类型为DATA等类型 不能拼接长度
                if ("0".equals(list.get(i).get("DATA_LENGTH").toString()) || equalsType(sysFiledTypeList.get(0).getDestFiledType())) {
                    stringBuffer.append(list.get(i).get("COLUMN_NAME") + " " + sysFiledTypeList.get(0).getDestFiledType());
                } else {
                    //如果精度不为空，类型也为number等类型 拼接精度
                    if (!"0".equals(list.get(i).get("DATA_SCALE").toString()) && equalsAccuracyTypes(sysFiledTypeList.get(0).getDestFiledType())) {
                        stringBuffer.append(list.get(i).get("COLUMN_NAME") + " " + sysFiledTypeList.get(0).getDestFiledType() + "(" + list.get(i).get("DATA_LENGTH") + "," + list.get(i).get("DATA_SCALE") + ")");
                    }else{
                        stringBuffer.append(list.get(i).get("COLUMN_NAME") + " " + sysFiledTypeList.get(0).getDestFiledType() + "(" + list.get(i).get("DATA_LENGTH")+ ")");
                    }
                }
                if (!"Y".equals(list.get(i).get("NULLABLE"))) {
                    stringBuffer.append(" NOT NULL");
                }
            }
            if (i == list.size() - 1) {
                continue;
            }
            stringBuffer.append(",");
        }
        //新增字段
        List<SysFieldrule> addField = sysFieldruleRepository.findByJobIdAndSourceNameAndAddFlag(jobId, tableName, 1);
        if (addField != null && addField.size() > 0) {
            stringBuffer.append("," + sysFieldruleList.get(0).getDestFieldName() + " " + sysFieldruleList.get(0).getType() + "(" + sysFieldruleList.get(0).getScale() + ")");
            if (addField.get(0).getNotNull() == 1) {
                stringBuffer.append(" NOT NULL");
            }
        }
        //目标端的主键
        List<SysFieldrule> primary = sysFieldruleRepository.findPremaryKey(jobId, tableName);
        if (primary != null && primary.size() > 0) {
            stringBuffer.append(",CONSTRAINT PK_" + primary.get(0).getDestFieldName().toUpperCase() + " PRIMARY KEY ('" + primary.get(0).getDestFieldName() + "')");
        } else {
            primaryKey = jobRelaServiceImpl.findPrimaryKey(jobId, tableName);
            if (primaryKey != null && primaryKey.size() > 0) {
                stringBuffer.append(",CONSTRAINT PK_" + primaryKey.get(0).toString().toUpperCase() + " PRIMARY KEY (");
                for(int i=0;i<primaryKey.size();i++){
                    stringBuffer.append(primaryKey.get(i));
                    if(i<primaryKey.size()-1){
                        stringBuffer.append(",");
                    }
                }
                stringBuffer.append(")");
            }
        }
        stringBuffer.append(");");
        return String.valueOf(stringBuffer);

    }

    @Override
    public String excuteSql(Long jobId, String tableName) {
        JobRelaServiceImpl jobRelaServiceImpl=new JobRelaServiceImpl();
        SysDbinfo sysDbinfo = jobRelaServiceImpl.findDestDbinfoById(jobId);//目标端数据库
        Connection conn=null;
        String sql=createTable( jobId,  tableName);
        try {
             conn= DBConns.getDaMengConn(sysDbinfo);
            DBUtil.update(sql,conn);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                DBConns.close(conn);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return sql;
    }


}
