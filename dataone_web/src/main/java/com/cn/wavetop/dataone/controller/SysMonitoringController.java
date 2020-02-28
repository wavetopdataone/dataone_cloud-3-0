package com.cn.wavetop.dataone.controller;

import com.cn.wavetop.dataone.dao.ErrorLogRespository;
import com.cn.wavetop.dataone.dao.SysDataChangeRepository;
import com.cn.wavetop.dataone.dao.SysMonitoringRepository;
import com.cn.wavetop.dataone.dao.SysRealTimeMonitoringRepository;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.entity.vo.SysMonitorRateVo;
import com.cn.wavetop.dataone.service.SysMonitoringService;
import com.cn.wavetop.dataone.service.SysRelaService;
import com.cn.wavetop.dataone.util.DateUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.transform.Result;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/sys_monitoring")
public class SysMonitoringController {
    @Autowired
    private SysMonitoringService sysMonitoringService;
    @Autowired
    private SysMonitoringRepository sysMonitoringRepository;
    @Autowired
    private SysDataChangeRepository sysDataChangeRepository;
    @Autowired
    private  SysRealTimeMonitoringRepository sysRealTimeMonitoringRepository;
    @Autowired
    private ErrorLogRespository errorLogRespository;

    @ApiOperation(value = "查看全部", protocols = "HTTP", produces = "application/json", notes = "查看全部")
    @RequestMapping("/monitoring_all")
    public Object userAll(){
        return sysMonitoringService.findAll();
    }
    @ApiOperation(value = "单一查询", protocols = "HTTP", produces = "application/json", notes = "单一查询")
    @RequestMapping("/check_monitoring")
    public Object checkUser(long job_id){
        return sysMonitoringService.findByJobId(job_id);
    }
    @ApiOperation(value = "添加", protocols = "HTTP", produces = "application/json", notes = "添加")
    @PostMapping("/add_monitoring")
    public Object addUser(@RequestBody SysMonitoring sysMonitoring){
        return sysMonitoringService.addSysMonitoring(sysMonitoring);
    }
    @ApiOperation(value = "修改", protocols = "HTTP", produces = "application/json", notes = "修改")
    @PostMapping("/edit_monitoring")
    public Object editUser(@RequestBody SysMonitoring sysMonitoring){
        return sysMonitoringService.update(sysMonitoring);
    }
    @ApiOperation(value = "删除", protocols = "HTTP", produces = "application/json", notes = "删除")
    @RequestMapping("/delete_monitoring")
    public Object deleteUser(long job_id){
        return sysMonitoringService.delete(job_id);
    }

    @ApiOperation(value = "模糊查询", protocols = "HTTP", produces = "application/json", notes = "模糊查询")
    @RequestMapping("/query_monitoring")
    public Object queryMonitoring(String source_table,long job_id){
        return sysMonitoringService.findLike(source_table,job_id);
    }
    @ApiOperation(value = "根据jobid查询条数和时间", protocols = "HTTP", produces = "application/json", notes = "根据jobid查询条数和时间")
    @RequestMapping("/data_rate")
    public Object dataRate(long job_id){
        return sysMonitoringService.dataRate(job_id);
    }
    @ApiOperation(value = "根据jobId查询读取写入错误行", protocols = "HTTP", produces = "application/json", notes = "根据jobId查询读取写入错误行")
    @RequestMapping("/show_monitoring")
    public Object showMonitoring(long job_id){
        return sysMonitoringService.showMonitoring(job_id);
    }
    @ApiOperation(value = "插入目标表名,显示该记录", protocols = "HTTP", produces = "application/json", notes = "插入目标表名,显示该记录")
    @RequestMapping("/table_monitoring")
    public Object tableMonitoring(long job_id,Integer current,Integer size){
        return sysMonitoringService.tableMonitoring(job_id, current, size);
    }
    @ApiOperation(value = "速率折线图数据", protocols = "POST", produces = "application/json", notes = "速率折线图数据")
    @PostMapping("/syncMonitoring")
    public  Object SyncMonitoring(Long jobId,String date){

        return sysMonitoringService.SyncMonitoring(jobId,date);
    }
    /**
     * 折线图数据
     * @param job_id
     * @return
     */
    @ApiOperation(value = "折线图数据", protocols = "POST", produces = "application/json", notes = "折线图数据")
    @PostMapping("/dataChangeView")
    public Object dataChangeView(@RequestParam long job_id,@RequestParam Integer date){
        return sysMonitoringService.dataChangeView(job_id,date);
    }

    /**
     * 根据表的状态查询
     */
    @ApiOperation(value = "根据表的状态查询", protocols = "POST", produces = "application/json", notes = "根据表的状态查询")
    @PostMapping("/statusMonitoring")
    public  Object statusMonitoring(Long job_id,Integer jobStatus){
        return sysMonitoringService.statusMonitoring(job_id,jobStatus);
    }
    /**
     * 监控页写入设置查询同步表
     */
    @ApiOperation(value = "监控页写入设置查询同步表", protocols = "POST", produces = "application/json", notes = "监控页写入设置查询同步表")
    @PostMapping("/selTable")
   public Object selTable(Long jobId){
       return sysMonitoringService.selTable(jobId);
   }
    /**
     * 根据表名模糊和状态查询
     */
    @ApiOperation(value = "根据表名模糊和状态查询", protocols = "POST", produces = "application/json", notes = "根据表名模糊和状态查询")
    @PostMapping("/findTableAndStatus")
   public Object findTableAndStatus(String source_table,Integer jobStatus,Long job_id,Integer current,Integer size){
        return sysMonitoringService.findTableAndStatus(source_table,jobStatus,job_id,current,size);
   }


    /**
     * 抽取速率读写量
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void saveDataChange() {
        SysDataChange dataChange = null;
        HashMap<Object, Double> map = new HashMap<>();
        List<SysRealTimeMonitoring> list=new ArrayList<>();
        List<ErrorLog> errorLogs=new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
        SimpleDateFormat dfs = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        Pageable page=null;//读取速率分页
        Pageable page2=null;//写入速率分页
        Integer result=0;//读取速率中间数
        Integer result2=0;//写入速率中间数
        Page<SysRealTimeMonitoring> sysRealTimeMonitoring1=null;
        Page<SysRealTimeMonitoring> sysRealTimeMonitoring2=null;
        String nowDate = dfs.format(new Date());//几号
        //todo 因为现在是凌晨抽取，那凌晨抽就要昨天的数据，所以要用yesterday
        String yesterDay = DateUtil.dateAdd(nowDate, -1);//昨天
        String weekDay = DateUtil.todate(yesterDay);//星期几
        List<Long> jobIdList = sysRealTimeMonitoringRepository.selJobId(yesterDay);//查询当天的所有jobid
        if (jobIdList != null && jobIdList.size() > 0) {
            for (Long jobId : jobIdList) {
                long errorData = 0;//错误量
                long readData = 0;//读取量
                long writeData = 0;//写入量
                double readRate = 0;//读取速率
                double disposeRate = 0;//处理速率
                //根据jobid查询读写错误，处理写入值
                list=sysRealTimeMonitoringRepository.findByJobId(jobId, DateUtil.StringToDate(yesterDay), DateUtil.StringToDate(nowDate));
                if(list!=null&&list.size()>0) {
                    for (SysRealTimeMonitoring sysRealTimeMonitoring:list) {
                        if (sysRealTimeMonitoring.getReadAmount() != null) {
                            readData += sysRealTimeMonitoring.getReadAmount();

                        }
                        if (sysRealTimeMonitoring.getWriteAmount() != null) {
                            writeData += sysRealTimeMonitoring.getWriteAmount();
                        }
                    }
                }
                errorLogs=errorLogRespository.findByJobIdAndOptTime(jobId,DateUtil.StringToDate(yesterDay), DateUtil.StringToDate(nowDate));
                if(errorLogs!=null&&errorLogs.size()>0) {
                    errorData = errorLogs.size();
                }else{
                    errorData=0;
                }
                //todo 读取速率数，然取值是不为空的中间后分页查询1条
                 result= sysRealTimeMonitoringRepository.findByJobIdAndTimeRead(jobId,DateUtil.StringToDate(yesterDay), DateUtil.StringToDate(nowDate));
                if(result>0) {
                     //读取速率的分页取不为空中间
                     page = PageRequest.of(result - 1, 1, Sort.Direction.ASC, "readRate");
                     //读取速率
                     Specification<SysRealTimeMonitoring> querySpecifi = new Specification<SysRealTimeMonitoring>() {
                         @Override
                         public Predicate toPredicate(Root<SysRealTimeMonitoring> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                             List<Predicate> predicates = new ArrayList<>();
                             predicates.add(cb.equal(root.get("jobId").as(Long.class), jobId));
                             predicates.add(cb.like(root.get("optTime").as(String.class), yesterDay+"%"));
                             predicates.add(cb.isNotNull(root.get("readRate").as(Double.class)));
                             criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//
                             // and到一起的话所有条件就是且关系，or就是或关系
                             return criteriaQuery.getRestriction();
                         }
                     };
                     sysRealTimeMonitoring1 = sysRealTimeMonitoringRepository.findAll(querySpecifi,page);
                     if(sysRealTimeMonitoring1.getContent().get(0).getReadRate()!=null){
                         readRate=sysRealTimeMonitoring1.getContent().get(0).getReadRate();
                     }
                 }
                 //写入速率不为空的中间值
                result2= sysRealTimeMonitoringRepository.findByJobIdAndTimeWrite(jobId,DateUtil.StringToDate(yesterDay), DateUtil.StringToDate(nowDate));
                if(result2>0){
                    //写入速率分页取不为空的中间
                    page2 = PageRequest.of(result2 - 1, 1, Sort.Direction.ASC, "writeRate");
                    //写入速率
                    Specification<SysRealTimeMonitoring> querySpecifi = new Specification<SysRealTimeMonitoring>() {
                        @Override
                        public Predicate toPredicate(Root<SysRealTimeMonitoring> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(cb.equal(root.get("jobId").as(Long.class), jobId));
                            predicates.add(cb.like(root.get("optTime").as(String.class), yesterDay+"%"));
                            predicates.add(cb.isNotNull(root.get("writeRate").as(Double.class)));

                            criteriaQuery.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//                criteriaQuery.orderBy(cb.desc(root.get("createDate")));
                            // and到一起的话所有条件就是且关系，or就是或关系
                            return criteriaQuery.getRestriction();
                        }
                    };
                    sysRealTimeMonitoring2 = sysRealTimeMonitoringRepository.findAll(querySpecifi,page2);
                    if(sysRealTimeMonitoring2.getContent().get(0).getWriteRate()!=null){
                        disposeRate=sysRealTimeMonitoring2.getContent().get(0).getWriteRate();
                    }
                }
                SysDataChange dataChange2 = new SysDataChange();
                dataChange2.setCreateTime(DateUtil.StringToDate(yesterDay));
                dataChange2.setDisposeRate(disposeRate);
                dataChange2.setJobId(jobId);
                dataChange2.setWeekDay(weekDay);
                dataChange2.setErrorData(errorData);
                dataChange2.setReadData(readData);
                dataChange2.setWriteData(writeData);
                dataChange2.setReadRate(readRate);
                sysDataChangeRepository.save(dataChange2);
//                //删除
//                sysRealTimeMonitoringRepository.deleteByJobId(jobId,DateUtil.StringToDate(yesterDay), DateUtil.StringToDate(nowDate));
            }
            //删除表结构
            sysRealTimeMonitoringRepository.delete();
        }

    }


//    }
}
