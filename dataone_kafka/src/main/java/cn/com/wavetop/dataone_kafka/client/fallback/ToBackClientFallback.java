//package cn.com.wavetop.dataone_kafka.client.fallback;
//
//import cn.com.wavetop.dataone_kafka.client.ToBackClient;
//import org.springframework.stereotype.Component;
//
///**
// * @Author yongz
// * @Date 2019/11/13、15:54
// * 熔断类
// */
//@Component
//public class ToBackClientFallback implements ToBackClient {
//    @Override
//    public Object findDbinfoById(Long jobId) {
//        return null;
//    }
//
//    @Override
//    public Object findRangeByJobId(long Id) {
//        return null;
//    }
//
//    @Override
//    public void updateReadMonitoring(long Id, Long readData, String table) {
//
//    }
//
//    @Override
//    public void updateWriteMonitoring(long Id, Long writeData, String table) {
//
//    }
//
//    @Override
//    public Object monitoringTable(long Id) {
//        return null;
//    }
//
//    @Override
//    public void monitoringTable(Long readRate, Long jobId) {
//
//    }
//
//    @Override
//    public void updateDisposeRateAndError(int jobId, double disposeRate, int errorData) {
//
//    }
//
//    @Override
//    public void InsertLogError(int jobId, Object optContext, Object content) {
//
//    }
//
//
//    @Override
//    public Object kafkaFiled(Long jobId) {
//        return null;
//    }
//}
