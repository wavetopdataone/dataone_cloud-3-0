package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.dao.SysJobrelaRepository;
import com.cn.wavetop.dataone.dao.SysJobrelaRespository;
import com.cn.wavetop.dataone.db.DBUtil;
import com.cn.wavetop.dataone.db.ResultMap;
import com.cn.wavetop.dataone.entity.SysDbinfo;
import com.cn.wavetop.dataone.service.CleanOutService;
import com.cn.wavetop.dataone.util.DBConn;
import com.cn.wavetop.dataone.util.DBConns;
import com.cn.wavetop.dataone.util.JSONUtil;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class CleanOutServiceImpl implements CleanOutService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;

    @Override
    public Object selData(Long jobId, String tableName) {
        SysDbinfo sysDbinfo = sysJobrelaRespository.findSourcesDbinfoById(jobId);
        Connection conn = null;
        while (conn == null) {
            try {
                conn = DBConns.getConn(sysDbinfo);
                Thread.sleep(4000);
            } catch (Exception e) {
                logger.error("数据库连接失效"+e.getMessage());
            }
        }

        ResultMap resultMap = null;
        Map map = null;
        Map map2=new HashMap();
        try {
            if (sysDbinfo.getType() == 1) {
                String sql = "select * from ( select * from "+tableName+" order by dbms_random.value) where rownum=1";
                resultMap = DBUtil.query2(sql, conn);
            } else if (sysDbinfo.getType() == 2) {
                String sql="select * from "+tableName+" order by rand() LIMIT 1";
                resultMap = DBUtil.query2(sql, conn);
            } else {
               logger.error("源端不支持数据类型");
            }
            if (resultMap != null && resultMap.size() > 0) {
                map = resultMap.get(0);
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

        return map;
    }
}
