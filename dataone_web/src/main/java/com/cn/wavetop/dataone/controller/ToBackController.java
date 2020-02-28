package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.service.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/toback")
public class ToBackController {
    @Autowired
    private ErrorLogService errorLogService;
    @Autowired
    private SysJobrelaService sysJobrelaService;
    @Autowired
    private SysTableruleService sysTableruleService;
    @Autowired
    private SysJobinfoService sysJobinfoService;
    @Autowired
    private SysJobinfoRespository sysJobinfoRespository;
    @Autowired
    private SysMonitoringService sysMonitoringService;
    @Autowired
    private SysErrorRepository sysErrorRepository;
    @Autowired
    private SysJobrelaRespository sysJobrelaRespository;
    @Autowired
    private SysDbinfoRespository sysDbinfoRespository;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    @Autowired
    private ErrorLogRespository errorLogRespository;

    @Autowired
    private KafkaDestFieldRepository kafkaDestFieldRepository;
    @Autowired
    private KafkaDestTableRepository kafkaDestTableRepository;
    @Autowired
    private SysRealTimeMonitoringRepository sysRealTimeMonitoringRepository;

    /**
     * 根据jobid查询数据信息
     *
     * @param jobId
     * @return
     */
    @ApiOperation(value = "后台查询", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "后台查询")
    @GetMapping("/findById/{jobId}")
    public Object findDbinfoById(@PathVariable Long jobId) {

        return sysJobrelaService.findDbinfoById(jobId);
    }

    /**
     * 查询同步数据类型
     *
     * @param Id
     * @return
     */
    @ApiOperation(value = "查看同步方式", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查看同步方式")
    @GetMapping("/find_range/{Id}")
    public Object findRangeByJobId(@PathVariable long Id) {
        return sysJobrelaService.findRangeByJobId(Id);
    }

    /**
     * 更新读监听数据
     *
     * @param Id
     * @param readData
     */
    @ApiOperation(value = "更新监听数据", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "更新监听数据")
    @GetMapping("/readmonitoring/{Id}")
    public void updateReadMonitoring(@PathVariable long Id, @RequestParam Long readData, String table) {
        sysMonitoringService.updateReadMonitoring(Id, readData, table);
    }


    /**
     * 更新写监听数据
     *
     * @param Id
     * @param writeData
     */
    @ApiOperation(value = "更新监听数据", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "更新监听数据")
    @GetMapping("/writemonitoring/{Id}")
    public void updateWriteMonitoring(@PathVariable long Id, @RequestParam Long writeData, String table) {
        sysMonitoringService.updateWriteMonitoring(Id, writeData, table);
    }

    /**
     * 更新写监听数据
     *
     * @param Id
     * @param
     */
    @ApiOperation(value = "查询监控目的表", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "查询监控目的表")
    @GetMapping("/find_destTable/{Id}")
    public Object monitoringTable(@PathVariable long Id) {
        String str = "";
        String dbName = "";
        List<String> list = new ArrayList<>();
        SysJobrela sysJobrela = sysJobrelaRespository.findById(Id);
        Optional<SysDbinfo> sysDbinfo = sysDbinfoRespository.findById(sysJobrela.getDestId());
        if (sysDbinfo.get().getType() == 1) {
            dbName = sysDbinfo.get().getSchema();
        } else if (sysDbinfo.get().getType() == 2) {
            dbName = sysDbinfo.get().getDbname();
        }
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(Id);
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList) {
                if (!"".equals(dbName) && dbName != null) {
                    str = dbName + "." + sysMonitoring.getDestTable();
                    list.add(str);
                } else {
                    list.add(sysMonitoring.getDestTable());
                }
            }
        }
        return list;
    }

    /**
     * 更新读监听数据
     *
     * @param
     * @para
     */
//    @ApiOperation(value = "读取速率", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "插入读取速率")
//    @GetMapping("/updateReadRate/{readRate}")
//    public void monitoringTable(@PathVariable Long readRate, Long jobId) {
//        //todo 后面要分表
//
//        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(jobId);
//        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
//            for (SysMonitoring sysMonitoring : sysMonitoringList) {
//                sysMonitoring.setReadRate(readRate);
//                sysMonitoringRepository.save(sysMonitoring);
//            }
//        }
//
//    }

    /**
     * 重置读写速率
     *
     * @param jobId
     * @Author yongz
     */
    @PostMapping("/resetMonitoring/{jobId}")
    public void resetMonitoring(@PathVariable Long jobId) {
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(jobId);
        for (SysMonitoring sysMonitoring : sysMonitoringList) {
            sysMonitoring.setReadData(0l);
            sysMonitoring.setWriteData(0l);
            sysMonitoring.setReadRate(0l);
            sysMonitoring.setDisposeRate(0l);
            sysMonitoring.setErrorData(0l);
            sysMonitoringRepository.save(sysMonitoring);
        }
    }

    /**
     * 分表更新读监听数据
     *
     * @param
     * @athor yongz
     * @para
     */
    @ApiOperation(value = "new读取速率", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "插入读取速率")
    @PostMapping("/updateReadRate/{jobId}")
    public void monitoringTable(@PathVariable Long jobId, @RequestBody Map<String, Map> Monito) {

        //todo 后面要分表
        Map<String, Object> tableMonito = Monito.get("tableMonito");
        Map<String, Object> tableTotal = Monito.get("tableTotal");
        System.out.println(Monito);
        System.out.println(tableTotal);
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(jobId);

        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (String table : tableTotal.keySet()) {
                for (SysMonitoring sysMonitoring : sysMonitoringList) {

                    String destTable = sysMonitoring.getDestTable();
                    if (destTable == null || "".equals(destTable)) {
                        destTable = sysMonitoring.getSourceTable();
                        sysMonitoring.setDestTable(destTable);
                    }
                    if (table.equalsIgnoreCase(destTable)) {
                        Double readRate = Double.parseDouble(tableMonito.get(table).toString());
//                        System.out.println(tableTotal.get(table));
                        long readData = Long.parseLong(tableTotal.get(table).toString());
                        sysMonitoringRepository.updateReadMonitoring2(sysMonitoring.getId(),sysMonitoring.getReadData() + readData,new Date(),new Double(readRate).longValue(), sysMonitoring.getDestTable());
                        // 更新实时表
                        SysRealTimeMonitoring sysRealTimeMonitoring = SysRealTimeMonitoring.builder().jobId(jobId).optTime(new Date()).destTable(table).readRate(readRate).readAmount((int) readData).build();
                        sysRealTimeMonitoringRepository.save(sysRealTimeMonitoring);
                    }

                }

            }


//            for (SysMonitoring sysMonitoring : sysMonitoringList) {
//
//                String destTable = sysMonitoring.getDestTable();
//
//                if (destTable == null || "".equals(destTable)) {
//                    destTable = sysMonitoring.getSourceTable();
//                }
//
//                Double readRate = tableMonito.get(destTable);
//                System.out.println(destTable);
//                long readData = tableTotal.get(destTable);
//                if ((readData == 0L) ) {
//                    readRate = tableMonito.get(destTable.toUpperCase());
//                    readData = tableTotal.get(destTable);
//                    if (readData == 0L){
//                        readRate = tableMonito.get(destTable.toLowerCase());
//                        readData = tableTotal.get(destTable);
//                    }
//                }
//
//            }
        }

    }

    /**
     * 分表更新读监听数据
     *
     * @param
     * @athor yongz
     * @para
     */
    @ApiOperation(value = "new读取速率", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "插入读取速率")
    @PostMapping("/getTreatmentRate/{jobId}")
    public String getTreatmentRate(@PathVariable Long jobId) {
        SysJobinfo sysJobinfo = sysJobinfoRespository.findByJobId(jobId);
        return sysJobinfo.getMaxSourceReadTo()+"|"+sysJobinfo.getMaxDestWriteTo();
    }

    /**
     * 分表更新读监听数据
     *
     * @param
     * @athor yongz
     * @para
     */
    @ApiOperation(value = "new写入速率", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "插入读取速率")
    @GetMapping("/updateWriteRate/{jobId}")
    @Transactional
    public void monitoringWriteRate(@PathVariable Long jobId, String destTable, Long writeAmount, Double writeRate, Long realWriteAmount) {

        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(jobId);
        System.out.println(sysMonitoringList);
        for (SysMonitoring sysMonitoring : sysMonitoringList) {
            if (sysMonitoring.getDestTable() == null || "".equals(sysMonitoring.getDestTable())) {
                if (destTable.equalsIgnoreCase(sysMonitoring.getSourceTable())) {
//                    sysMonitoring.setWriteData(writeAmount);
//                    sysMonitoring.setDisposeRate(new Double(writeRate).longValue());
//                    sysMonitoringRepository.save(sysMonitoring);
                    sysMonitoringRepository.updateWriteMonitoring2(sysMonitoring.getId(),writeAmount,new Double(writeRate).longValue());

                    SysRealTimeMonitoring sysRealTimeMonitoring = SysRealTimeMonitoring.builder().jobId(jobId).optTime(new Date()).destTable(destTable).readRate(writeRate).readAmount(Math.toIntExact(realWriteAmount)).build();
                    if (sysRealTimeMonitoring.getWriteAmount() != 0 && sysRealTimeMonitoring.getWriteAmount() != null)
                        sysRealTimeMonitoringRepository.save(sysRealTimeMonitoring);
                }
            } else {
                if (destTable.equalsIgnoreCase(sysMonitoring.getDestTable())) {
//                    sysMonitoring.setWriteData(writeAmount);
//                    sysMonitoring.setDisposeRate(new Double(writeRate).longValue());
//                    sysMonitoringRepository.save(sysMonitoring);
                    sysMonitoringRepository.updateWriteMonitoring2(sysMonitoring.getId(),writeAmount,new Double(writeRate).longValue());

                    SysRealTimeMonitoring sysRealTimeMonitoring = SysRealTimeMonitoring.builder().jobId(jobId).optTime(new Date()).destTable(destTable).writeRate(writeRate).writeAmount(Math.toIntExact(realWriteAmount)).build();
                    if (sysRealTimeMonitoring.getWriteAmount() != 0 && sysRealTimeMonitoring.getWriteAmount() != null)
                        sysRealTimeMonitoringRepository.save(sysRealTimeMonitoring);
                }
            }

        }


    }

    /**
     * 更新写监听数据
     *
     * @param
     * @para
     */
    @ApiOperation(value = "写入速率", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "插入读取速率")
    @GetMapping("/updateDisposeRateAndError/{jobId}")
    public void updateDisposeRateAndError(@PathVariable Long jobId, Long disposeRate, Long errorData) {
        //todo 后面要分表
        List<SysMonitoring> sysMonitoringList = sysMonitoringRepository.findByJobId(jobId);
        if (sysMonitoringList != null && sysMonitoringList.size() > 0) {
            for (SysMonitoring sysMonitoring : sysMonitoringList) {
                sysMonitoring.setDisposeRate(disposeRate);
                sysMonitoring.setErrorData(errorData);
                sysMonitoringRepository.save(sysMonitoring);
            }
        }
    }

    /**
     * 错误队列
     *
     * @param
     * @para
     */
    @ApiOperation(value = "错误队列", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "插入读取速率")
    @GetMapping("/InsertLogError/{jobId}")
    public void InsertLogError(@PathVariable Long jobId, String optContext, String content) {
        //todo 后面要分表
        ErrorLog errorLog = new ErrorLog();
        errorLog.setContent(content);
        errorLog.setOptContext(optContext);
        errorLog.setJobId(jobId);
        errorLog.setOptTime(new Date());
        errorLogRespository.save(errorLog);

    }

    /**
     * kafka需要的同步数据
     */
    @ApiOperation(value = "kafka需要的同步数据", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "kafka需要的同步数据")
    @GetMapping("/kafkaFiled/{jobId}")
    public Object kafkaFiled(@PathVariable Long jobId) {
        List<KafkaDestTable> kafkaDestTableList = kafkaDestTableRepository.findByJobId(jobId);
        List<Object> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        for (KafkaDestTable kafkaDestTable : kafkaDestTableList) {
            List<KafkaDestField> kafkaDestFieldList = kafkaDestFieldRepository.findByKafkaDestId(kafkaDestTable.getId());
            list = new ArrayList<>();
            list.add(kafkaDestFieldList);
            map.put(kafkaDestTable.getDestTable(), list);
        }
        return map;
    }

    /**
     * 插入错误信息
     */
    @PostMapping("/insertErrorLog")
    public void insertError(@RequestParam Long jobId,@RequestParam String sourceTable,@RequestParam String destTable,@RequestParam String time,@RequestParam String errortype,
                            @RequestParam String message){

        errorLogService.insertError(jobId,sourceTable,destTable,time,errortype,message);
    }

    /**
     * 查询源端表名
     * 将错误状态4填入到monitoring表中
     * 将错误信息填入到userlog中
     * @param jobId
     * @param destTable
     * @param time
     * @return
     */
    @GetMapping("/selecttable")
    public String selectTable(@RequestParam Long jobId,@RequestParam String destTable,@RequestParam String time,@RequestParam Integer errorflag) {
        return sysTableruleService.selectTable(jobId,destTable,time,errorflag);
    }


    /**
     * 将系统错误信息插入到系统日志表
     * @param syserror
     */
    @PostMapping("/insertsyslog")
    void inserSyslog(@RequestParam String syserror,@RequestParam String method,@RequestParam String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date format = null;
        try {
            format = simpleDateFormat.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SysError sysError = new SysError();
        String errortype = "ConsumerError";
        sysError.setCreateDate(format);
        sysError.setErrorType(errortype);
        sysError.setMethod(method);
        sysError.setErrorName(syserror);
        sysErrorRepository.save(sysError);
    }
}
