package com.cn.wavetop.dataone.service;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;
import com.cn.wavetop.dataone.dao.SysCleanScriptRepository;
import com.cn.wavetop.dataone.entity.SysCleanScript;
import com.cn.wavetop.dataone.entity.vo.ScriptMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @Author yongz
 * @Date 2020/3/30、11:19
 */
@Service
public class SysCleanScriptImpl {

    @Autowired
    private SysCleanScriptRepository sysCleanScriptRepository;

    public Map executeScript(Long jobId, String tableName, Map payload) {
        List<SysCleanScript> SysCleanScript = sysCleanScriptRepository.findByJobIdAndSourceTableAndFlag(jobId, tableName,1);
       // System.out.println("SysCleanScript:" + SysCleanScript);
        if (SysCleanScript != null && SysCleanScript.size() > 0) {
            return executeScript(SysCleanScript.get(0).getScriptContent(), payload).getResult();
        } else {
            return null;
        }
    }

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
                test = cls.getMethod("process", Map.class);
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

                Map result = (Map) test.invoke(o, payload);

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

    public Class getScriptCls(String scriptContent) {
        Class cls = null;
        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(scriptContent);
        if (compiler.compiler()) {
            // 获取class
            try {
                cls = compiler.getScriptClass();
            } catch (Exception e) {
                return null;
            }
            return cls;
        } else {
            return null;
        }
    }

    public Class getScriptCls(Long jobId, String tableName) {
        List<SysCleanScript> SysCleanScript = sysCleanScriptRepository.findByJobIdAndSourceTableAndFlag(jobId, tableName,1);
        if (SysCleanScript != null && SysCleanScript.size() > 0) {
            return getScriptCls(SysCleanScript.get(0).getScriptContent());
        } else {
            return null;
        }
    }

}
