package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;
import com.cn.wavetop.dataone.dao.SysCleanScriptRepository;
import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysCleanScriptService;
import org.apache.poi.ss.formula.functions.Today;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysCleanScriptServiceImpl implements SysCleanScriptService {
    @Autowired
    private SysCleanScriptRepository sysCleanScriptRepository;

    /**
     * 保存加执行
     *
     * @param sysCleanScript
     * @return
     */
    @Override
    public Object saveAndExcues(SysCleanScript sysCleanScript, Map map) {
        System.out.println(map+"-------传参");
        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(sysCleanScript.getScriptContent());
        boolean compiler1 = compiler.compiler();
        Map invoke=null;
        Class cls = null;
        try {
            cls = compiler.getScriptClass();
            String fullClassName = compiler.getFullClassName();
            // 反射的基础
            Object o = cls.newInstance();
            Method test = cls.getMethod("test", Map.class);
            test.setAccessible(true);// 暴力反射
             invoke = (Map) test.invoke(o,  map);
            System.out.println("返回的map-------------"+invoke);
            sysCleanScriptRepository.save(sysCleanScript);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return invoke;
    }

    @Override
    public Object save(SysCleanScript sysCleanScript) {
        List<SysCleanScript> list = sysCleanScriptRepository.findByJobIdAndSourceTable(sysCleanScript.getJobId(), sysCleanScript.getSourceTable());
        SysCleanScript sysCleanScript1=null;
        if (list != null && list.size() > 0) {
            list.get(0).setScriptContent(sysCleanScript.getScriptContent());
             sysCleanScript1 = sysCleanScriptRepository.save(list.get(0));
        }
            if (sysCleanScript1 != null) {
                return ToDataMessage.builder().status("1").message("保存成功").build();
            } else {
                return ToDataMessage.builder().status("0").message("保存失败").build();
            }
        }


    @Override
    public Object findByIdAndTable(Long jobId, String sourceTable) {
        List<SysCleanScript> list= sysCleanScriptRepository.findByJobIdAndSourceTable(jobId,sourceTable);
        return ToData.builder().status("1").data(list).build();
    }
}
