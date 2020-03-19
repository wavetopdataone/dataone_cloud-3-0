package com.cn.wavetop.dataone.compilerutil.testComplier;

import com.cn.wavetop.dataone.compilerutil.CustomStringJavaCompiler;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * Create by andy on 2018-12-07 13:10
 */
public class CustomCallable implements Callable<RunInfo> {
    private String sourceCode;

    public CustomCallable(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    //方案1
    //@Override
    //public RunInfo call() throws Exception {
    //    System.out.println("开始执行call" + LocalTime.now());
    //    RunInfo runInfo = new RunInfo();
    //    CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(sourceCode);
    //    if (compiler.compiler()) {
    //        runInfo.setCompilerSuccess(true);
    //        try {
    //            compiler.runMainMethod();
    //            runInfo.setRunSuccess(true);
    //            runInfo.setRunTakeTime(compiler.getRunTakeTime());
    //            runInfo.setRunMessage(compiler.getRunResult()); //获取运行的时候输出内容
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            runInfo.setRunSuccess(false);
    //            runInfo.setRunMessage(e.getMessage());
    //        }
    //    } else {
    //        //编译失败
    //        runInfo.setCompilerSuccess(false);
    //    }
    //    runInfo.setCompilerTakeTime(compiler.getCompilerTakeTime());
    //    runInfo.setCompilerMessage(compiler.getCompilerMessage());
    //    System.out.println("call over" + LocalTime.now());
    //    return runInfo;
    //}


    //方案2
    @Override
    public RunInfo call() throws Exception {
        RunInfo runInfo = new RunInfo();
        Thread t1 = new Thread(() -> realCall(runInfo));
        t1.start();
        try {
            t1.join(3000); //等待3秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //不管有没有正常执行完成，强制停止t1
        t1.stop();
        return runInfo;
    }

    private void realCall(RunInfo runInfo) {
        CustomStringJavaCompiler compiler = new CustomStringJavaCompiler(sourceCode);
        if (compiler.compiler()) {
            runInfo.setCompilerSuccess(true);
            try {
                compiler.runMainMethod();
                runInfo.setRunSuccess(true);
                runInfo.setRunTakeTime(compiler.getRunTakeTime());
                runInfo.setRunMessage(compiler.getRunResult()); //获取运行的时候输出内容
            } catch (InvocationTargetException e) {
                //反射调用异常了,是因为超时的线程被强制stop了
                if ("java.lang.ThreadDeath".equalsIgnoreCase(e.getCause().toString())) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                runInfo.setRunSuccess(false);
                runInfo.setRunMessage(e.getMessage());
            }
        } else {
            //编译失败
            runInfo.setCompilerSuccess(false);
        }
        runInfo.setCompilerTakeTime(compiler.getCompilerTakeTime());
        runInfo.setCompilerMessage(compiler.getCompilerMessage());
        runInfo.setTimeOut(false); //走到这一步代表没有超时
    }
}