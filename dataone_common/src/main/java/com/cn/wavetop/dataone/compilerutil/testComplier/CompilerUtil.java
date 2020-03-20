package com.cn.wavetop.dataone.compilerutil.testComplier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @Author yongz
 * @Date 2020/03/20、15:10
 */
public class CompilerUtil {
    //这里用一个线程是因为防止System.out输出内容错乱
    private static ExecutorService pool = Executors.newFixedThreadPool(1);

    public static RunInfo getRunInfo(String javaSourceCode) {
        RunInfo runInfo;
        CustomCallable compilerAndRun = new CustomCallable(javaSourceCode);
        Future<RunInfo> future = pool.submit(compilerAndRun);
        //方案1
        try {
            runInfo = future.get();
        } catch (Exception e) {
            e.printStackTrace();
            //代码编译或者运行超时
            runInfo = new RunInfo();
            runInfo.setTimeOut(true);
        }

        //方案2：不可行的原因：future.get超时会有问题，由于线程池只有1个线程，同时提交10个任务， 当前面几个任务执行时间很长，后面调用get就会立马失败，也就是说get的超时时间是从调用get开始算的，并不是线程真正执行时间开始计算的
        //try {
        //    runInfo = future.get(5, TimeUnit.SECONDS);
        //    return runInfo;
        //} catch (InterruptedException e) {
        //    System.out.println("future在睡着时被打断");
        //    e.printStackTrace();
        //} catch (ExecutionException e) {
        //    System.out.println("future在尝试取得任务结果时出错");
        //    e.printStackTrace();
        //} catch (TimeoutException e) {
        //    System.out.println("future时间超时");
        //    e.printStackTrace();
        //    future.cancel(true);
        //}
        //runInfo = new RunInfo();
        //runInfo.setTimeOut(true);
        return runInfo;

    }
}