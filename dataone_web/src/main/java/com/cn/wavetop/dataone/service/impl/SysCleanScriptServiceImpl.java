package com.cn.wavetop.dataone.service.impl;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;
import com.cn.wavetop.dataone.dao.SysCleanScriptRepository;
import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.entity.vo.ScriptMessage;
import com.cn.wavetop.dataone.entity.vo.ToData;
import com.cn.wavetop.dataone.entity.vo.ToDataMessage;
import com.cn.wavetop.dataone.service.SysCleanScriptService;
import com.cn.wavetop.dataone.util.StringUtils;
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

    @Override
    public ScriptMessage executeScript(String scriptContent, Map payload) {
        Class cls = null;
        Object o = null;
        Method test = null;
        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(scriptContent);
        if (compiler.compiler()) {

            // 获取class
            try {
                cls = compiler.getScriptClass();
            } catch (Exception e) {
                System.out.println("获取class失败！");
                return ScriptMessage.builder()
                        .errorMessage(e.toString())
                        .message("获取class失败！请检查代码格式，工具类是否导包等问题。")
                        .status(2).build();
            }

            // 构造实例对象
            try {
                o = cls.newInstance();
            } catch (Exception e) {
                return ScriptMessage.builder()
                        .errorMessage(e.toString())
                        .message("构造实例对象失败！请检查代码格式规范，工具类是否导包等问题。")
                        .status(3).build();
            }

            // 获取方法
            try {
                test = cls.getMethod("process",   Map.class);
            } catch (Exception e) {
                System.out.println("获取脚本方法失败！请检查方法名和参数类型");

                return ScriptMessage.builder()
                        .errorMessage(e.toString())
                        .message("获取脚本方法失败！请检查方法名和参数类型")
                        .status(4).build();
            }
            test.setAccessible(true);// 暴力反射

            // 执行方法
            try {

                Map result = (Map) test.invoke(o,  payload);

                return ScriptMessage.builder()
//                        .errorMessage(compiler.getCompilerMessage())
                        .message("代码执行成功！请确认返回值是否符合要求")
                        .status(0)
                        .result(result).build();

            } catch (Exception e) {
                return ScriptMessage.builder()
                        .errorMessage(e.toString())
                        .message("执行脚本方法失败！请检查代码对样本数据的操作。")
                        .status(5).build();

            }
        } else {
            return ScriptMessage.builder()
                    .errorMessage(compiler.getCompilerMessage())
                    .message("脚本编译失败！请检查代码格式规范，工具类是否导包等问题。")
                    .status(1).build();
        }
    }
}
