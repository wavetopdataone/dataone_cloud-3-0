package com.cn.wavetop.dataone.controller;


import com.cn.wavetop.dataone.entity.SysJobrela;
import com.cn.wavetop.dataone.service.SysJobinfoService;
import com.cn.wavetop.dataone.service.SysJobrelaService;
import com.cn.wavetop.dataone.service.SysUserJobrelaService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author yongz
 * @Date 2019/10/10、11:45
 */
@RestController
@RequestMapping("/sys_jobrela")
public class SysJobrelaController {

    @Autowired
    private SysJobrelaService service;
    @Autowired
    private SysUserJobrelaService sysUserJobrelaService;

    @ApiOperation(value = "根据用户权限不同显示不同任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询用户信息")
    @PostMapping("/jobrela_all")
    public Object jobrela_all(Integer current,Integer size) {
        return service.getJobrelaAll( current, size);
    }

    @ApiImplicitParam
    @PostMapping("/check_jobinfo")
    public Object check_jobinfo(long id) {
        return service.checkJobinfoById(id);
    }


    @ApiOperation(value = "添加任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "添加任务")
    @PostMapping("/add_jobrela")
    public Object add_jobrela(@RequestBody  SysJobrela sysJobrela) {

        return service.addJobrela(sysJobrela);
    }

    @ApiImplicitParam
    @PostMapping("/edit_jobrela")
    public Object edit_jobrela(@RequestBody SysJobrela sysJobrela) {

        return service.editJobrela(sysJobrela);
    }

    @ApiImplicitParam
    @PostMapping("/delete_jobrela")
    public Object delete_jobrela(Long id) {

        return service.deleteJobrela(id);
    }

    // 查询个任务状态接口
    @ApiImplicitParam
    @PostMapping("/jobrela_count")
    public Object jobrela_count(Long deptId) {

        return service.jobrelaCount(deptId);
    }

    //模糊查询
    @ApiImplicitParam
    @PostMapping("/query_jobrela")
    public Object query_jobrela(String job_name,Integer current,Integer size ) {

        return service.queryJobrela(job_name,current,size);
    }

    //
    @ApiImplicitParam
    @PostMapping("/some_jobrela")
    public Object some_jobrela(String job_status,Long deptId,Integer current,Integer size) {
        return service.someJobrela(job_status,deptId,current,size);
    }

    @ApiImplicitParam
    @PostMapping("/start")
    public Object start(Long id) {
        return service.start(id);
    }


    @ApiImplicitParam
    @PostMapping("/pause")
    public Object pause(Long id) {
        return service.pause(id);
    }


    @ApiImplicitParam
    @PostMapping("/end")
    public Object end(Long id) {
        return service.end(id);
    }


    //@MyLog(value = "为用户选任务")
    @ApiOperation(value = "为用户选任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "为用户选任务")
    @PostMapping("/addUserJob")
    public Object addUserJobRela(Long userId,String jobrela_id) {
        return sysUserJobrelaService.addUserJobRela(userId,jobrela_id);
    }
    @ApiOperation(value = "根据用户名或任务名查询任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "为用户选任务")
    @PostMapping("/selJobrelaUser")
    public Object selJobrelaUser(String status ,String name ) {
        return service.selJobrelaUser(status,name);
    }
    @ApiOperation(value = "根据用户查询任务默认被勾选", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据用户查询任务默认被勾选")
    @PostMapping("/selJobrelaChecked")
    public Object selJobrelaChecked(Long userId) {
        return service.seleJobrelaByUser(userId);
    }
    @ApiOperation(value = "根据任务id获取参与人", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "根据任务id获取参与人")
    @PostMapping("/selUserByJobId")
    public Object selUserByJobId(Long jobId) {
        return service.selUserByJobId(jobId);
    }

    @ApiOperation(value = "为任务选择参与人", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "为任务选择参与人")
    @PostMapping("/addUserByJobId")
    public Object addUserByJobId(Long jobId,String userId) {

        return service.addUserByJobId(jobId,userId);
    }


    @ApiOperation(value = "后台查询", httpMethod = "GET", protocols = "HTTP", produces = "application/json", notes = "后台查询")
    @GetMapping("/findById")
    public Object findDbinfoById(Long jobId) {

        return service.findDbinfoById(jobId);
    }
    @ApiOperation(value = "删除该用户绑定的任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "删除该用户绑定的任务")
    @PostMapping("/deleteUserJobRela")
    public Object deleteUserJobRela(Long userId,String jobrela_id) {
        return sysUserJobrelaService.deleteUserJobRela(userId,jobrela_id);
    }
    @ApiOperation(value = "查询用户参与的任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询用户没有参与的任务")
    @PostMapping("/findUserJob")
    public Object findUserJob(Long userId) {

        return service.findUserJob(userId);
    }
    @ApiOperation(value = "查询用户没有参与的任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "查询用户参与的任务")
    @PostMapping("/findUserJobNo")
    public Object findUserJobNo(Long userId) {

        return service.findUserJobNo(userId);
    }


    @ApiOperation(value = "首页根据部门或者用户查询任务分页", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "首页根据部门查询任务分页")
    @PostMapping("/selJobrelaByDeptIdPage")
    public Object selJobrelaByDeptIdPage(Long deptId,Integer current,Integer size) {

        return service.selJobrelaByDeptIdPage(deptId,current,size);
    }
    @ApiOperation(value = "复制任务", httpMethod = "POST", protocols = "HTTP", produces = "application/json", notes = "复制任务")
    @PostMapping("/copyJob")
    public Object copyJob(Long jobId) {
        return service.copyJob(jobId);
    }

}
