package com.cn.wavetop.dataone;

/**
 * @Author yongz
 * @Date 2020/3/23、9:46
 */
public class TestThread  extends  Thread{

    @Override
    public void run() {
        int i =0;
        while (true){
            System.out.println(i++);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *      * IllegalMonitorStateException 非法监控异常
     *      注意
     */
//    public void suspendMe() {
//        try {
//            wait();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    public void stopMe() {
        // 资源释放
        stop();
    }

//    public void resumeMe() {
//        notify();
//    }
}
