package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;
import com.cn.wavetop.dataone.dao.SysCleanScriptRepository;
import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.service.SysCleanScriptService;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Service
public class SysCleanScriptServiceImpl implements SysCleanScriptService {

    /**
     * 保存加执行
     *
     * @param sysCleanScript
     * @return
     */
    @Override
    public Object save(SysCleanScript sysCleanScript,String content, Map map) {
        System.out.println(map+"-------传参");
        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(content);
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return invoke;
    }

    @Override
    public Object findByIdAndTable(Long jobId, String sourceTable) {
        return null;
    }
}
