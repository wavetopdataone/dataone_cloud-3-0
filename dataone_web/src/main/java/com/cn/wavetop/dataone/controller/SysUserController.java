package com.cn.wavetop.dataone.controller;


import com.cn.wavetop.dataone.entity.SysUser;
import com.cn.wavetop.dataone.service.SysUserService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys_user")
public class  SysUserController {
    @Autowired
    private SysUserService sysUserService;

    @ApiImplicitParam
    @PostMapping("/addUser")
    public Object regist(@RequestBody SysUser sysUser,String id) {

        return sysUserService.addSysUser(sysUser,id);
    }
//    @RequiresGuest
    @ApiOperation(value = "登录",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "用户登录")
    @PostMapping("/login")
    public Object login(String name,String password){

        return sysUserService.login(name,password);
    }

    @ApiOperation(value = "根据用户权限查询用户",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "查询用户")
    @PostMapping("/alluser")
    public Object findAll()
    {
        return sysUserService.findAll();
    }
    @ApiOperation(value = "删除用户",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "删除用户")
    @PostMapping("/delete")
    public Object delete(String name){

        return sysUserService.delete(name);
    }
    @ApiOperation(value = "给用户添加分组",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "给用户添加分组")
    @ApiImplicitParam
    @PostMapping("/updateUser")
    public Object updateUser(Long DeptId,Long id){

        return sysUserService.updateUser(id,DeptId);
    }
    @ApiOperation(value = "修改用户",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "修改用户")
    @PostMapping("/update")
    public Object update(@RequestBody SysUser sysUser){

        return sysUserService.update(sysUser);
    }
    @ApiOperation(value = "退出登录",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "退出登录")
    @PostMapping("/login_out")
    public Object loginOut(){
        return sysUserService.loginOut();
    }
    @ApiOperation(value = "根据用户名查找角色权限",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "根据用户名查找角色权限")
    @PostMapping("/findRolePerms")
    public Object findRolePerms(String loginName){
        return sysUserService.findRolePerms(loginName);
    }
    //跟findUserByDept借口效果差不多？这个不适用与超级管理员
//    @ApiOperation(value = "查询管理员组下的全部人员",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "查询管理员组下的全部人员")
//    @PostMapping("/findAllUser")
//    public Object findAllUser(){
//
//        return sysUserService.findById();
//    }


    @ApiOperation(value = "用户模糊查询",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "用户模糊查询")
    @PostMapping("/findByName")
    public Object findByUserName(String userName){

        return sysUserService.findByUserName(userName);
    }
    @ApiOperation(value = "根据部门ID查询该部门下的用户",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "根据部门ID查询该部门下的用户")
    @PostMapping("/findUserByDept")
    public Object findUserByDept(Long deptId){
        return sysUserService.findUserByDept(deptId);
    }

    @ApiOperation(value = "冻结用户",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "冻结用户")
    @PostMapping("/updateStatus")
    public Object updateStatus(Long id ,String status){
        return sysUserService.updateStatus(id,status);
    }

    @ApiOperation(value = "根据用户id查询用户详细信息",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "根据用户id查询用户详细信息")
    @PostMapping("/selSysUser")
    public Object selSysUser(Long userId){

        return sysUserService.selSysUser(userId);
    }
    @ApiOperation(value = "移交团队id是管理员userid是编辑者",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "移交团队id是管理员userid是编辑者")
    @PostMapping("/HandedTeam")
    public Object HandedTeam(Long id,Long userId ){

        return sysUserService.HandedTeam(id,userId);
    }

    @ApiOperation(value = "全部成员任务名",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "全部成员人物名")
    @PostMapping("/seleUserBystatus")
    public Object seleUserBystatus(String status ){

        return sysUserService.seleUserBystatus(status);
    }
    @ApiOperation(value = "超级管理员移交权限根据管理员id查询出编辑者",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "超级管理员移交权限根据管理员id查询出编辑者")
    @PostMapping("/selectUser")
    public Object selectUserByParentId(Long userId ){

        return sysUserService.selectUserByParentId(userId);
    }
    @ApiOperation(value = "发送验证码",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "发送验证码")
    @PostMapping("/sendEmail")
    public Object sendEmail(String email){

        return sysUserService.sendEmail(email);
    }
    @ApiOperation(value = "验证码是否正确",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "验证码是否正确")
    @PostMapping("/codeEquals")
    public Object codeEquals(String email,String authCode){

        return sysUserService.codeEquals(email,authCode);
    }
    @ApiOperation(value = "忘记密码时修改密码",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "修改密码")
    @PostMapping("/editPasswordByEmail")
    public Object editPasswordByEmail(String email, String password){
//        String ip=session.getHost();
//        System.out.println(ip);
        return sysUserService.editPasswordByEmail(email,password);
    }

    @ApiOperation(value = "初始化绑定超管邮箱和密码",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "绑定超管邮箱和密码")
    @PostMapping("/bindEmail")
    public Object  bindEmail(String email, String emailPassword){

        return sysUserService.bindEmail(email,emailPassword);
    }
    @ApiOperation(value = "个人设置信息详情",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "个人设置信息详情")
    @PostMapping("/personal")
    public Object  Personal(){
        return sysUserService.Personal();
    }

    @ApiOperation(value = "查询分组和该分组下面的人",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "查询分组和该分组下面的人")
    @PostMapping("/findDeptAndUser")
    public Object  findDeptAndUser(){

        return sysUserService.findDeptAndUser();
    }
    ;
    @ApiOperation(value = "修改个人密码",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "修改个人密码")
    @PostMapping("/updPassword")
    public  Object updPassword(Long userId,String password,String newPassword){
        return sysUserService.updPassword(userId,password,newPassword);
    }
    @ApiOperation(value = "修改超级管理员邮箱",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "修改超级管理员邮箱")
    @PostMapping("/updSuperEmail")
    public  Object updSuperEmail(Long userId,String password,String newEmail,String emailPassword){
        return sysUserService.updSuperEmail(userId,password,newEmail,emailPassword);
    }
    @ApiOperation(value = "修改用户邮箱",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "修改用户邮箱")
    @PostMapping("/updUserEmail")
    public  Object updUserEmail(Long userId, String password, String newEmail){
        return sysUserService.updUserEmail(userId,password,newEmail);
    }

    @ApiOperation(value = "修改技术支持的邮箱需要发送的验证码",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "修改技术支持的邮箱需要发送的验证码")
    @PostMapping("/sendCode")
    public Object sendCode(String email){
        return sysUserService.sendCode(email);
    }
    @ApiOperation(value = "验证修改技术支持邮箱验证码是否正确",httpMethod = "POST",protocols = "HTTP", produces ="application/json", notes = "验证修改技术支持邮箱验证码是否正确")
    @PostMapping("/skillCodeEquals")
    public Object SkillCodeEquals(String email,String authCode){
        return sysUserService.SkillCodeEquals( email, authCode);
    }
}
