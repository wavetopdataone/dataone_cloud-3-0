package com.cn.wavetop.dataone.aop;

import com.alibaba.fastjson.JSON;
import com.cn.wavetop.dataone.dao.*;
import com.cn.wavetop.dataone.entity.*;
import com.cn.wavetop.dataone.util.PermissionUtils;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.mapstruct.BeforeMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 系统日志：切面处理类
 */
@Aspect
@Component
public class SysLogAspect {


    @Autowired
   private SysLogRepository sysLogRepository;
    @Autowired
    private SysUserRepository sysUserRepository;
    @Autowired
    private SysDeptRepository sysDeptRepository;
    @Autowired
    private SysErrorRepository sysErrorRepository;
    @Autowired
    private SysUserlogRepository sysUserlogRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //定义切点 @Pointcut
    //在注解的位置切入代码
//    @Pointcut("@annotation(com.cn.wavetop.dataone.aop.MyLog)")
//    public void logPoinCut() {
//    }
// 异常增强
//    @AfterThrowing(throwing="ex",pointcut="execution(* com.cn.wavetop.dataone.service.impl..*.*(..))")
    @AfterThrowing(throwing="ex",pointcut="execution(* com.cn.wavetop.dataone..*.*(..))")
    public void afterThrowing(JoinPoint joinPoint,Throwable ex) {
//        logger.error("*"+ex);
        //保存日志
        SysError sysLog = new SysError();
        if (PermissionUtils.getSysUser() != null) {
            //获取用户名
            sysLog.setUsername(PermissionUtils.getSysUser().getLoginName());
            //获取角色信息
            List<SysRole> sysRoles = sysUserRepository.findUserById(PermissionUtils.getSysUser().getId());
            String roleName = "";
            if (sysRoles != null && sysRoles.size() > 0) {
                roleName = sysRoles.get(0).getRoleName();

                sysLog.setRoleName(roleName);
            }
            if (PermissionUtils.getSysUser().getDeptId() != 0 && PermissionUtils.getSysUser().getDeptId() != null) {
                //获取部门信息
                Optional<SysDept> sysDepts = sysDeptRepository.findById(PermissionUtils.getSysUser().getDeptId());

                String deptName = "";
                if (sysDepts != null) {
                    deptName = sysDepts.get().getDeptName();

                    sysLog.setDeptName(deptName);
                }
            }
        }
            //从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();

//        //获取操作
//        MyLog myLog = method.getAnnotation(MyLog.class);
//        if (myLog != null) {
//            String value = myLog.value();
//            //String jobId=myLog.jobId();
//            sysLog.setOperation(value);//保存获取的操作
//            //sysLog.setJobId(jobId);
//        }

            //获取请求的类名
            String className = joinPoint.getTarget().getClass().getName();
            //获取请求的方法名
            String methodName = method.getName();
            sysLog.setMethod(className + "." + methodName);
        //请求的参数
        Object[] args = joinPoint.getArgs();
        Class<?> targetClass = null;
        try {
            targetClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Method[] methods = targetClass.getMethods();
        StringBuilder requestBuilder = new StringBuilder(0);

/**
 * 遍历方法 获取能与方法名相同且请求参数个数也相同的方法
 */
        for (Method method1 : methods)
        {
            if (!method1.getName().equals(methodName))
            {
                continue;
            }

            Class<?>[] classes = method1.getParameterTypes();

            if (classes.length != args.length)
            {
                continue;
            }

            for (int index = 0; index < classes.length; index++)
            {
                // 如果参数类型是请求和响应的http，则不需要拼接【这两个参数，使用JSON.toJSONString()转换会抛异常】
                if (args[index] instanceof HttpServletRequest
                        || args[index] instanceof HttpServletResponse)
                {
                    continue;
                }
                requestBuilder.append(args[index] == null ? ""
                        : JSON.toJSONString(args[index]));
            }
            sysLog.setParams(requestBuilder.toString());
        }







            //将参数所在的数组转换成json
//          String params = JSON.toJSONString(args);
//
//            SysUser sysUser= JSON.parseObject(params, SysUser.class);
            sysLog.setCreateDate(new Date());


            //获取用户ip地址
            //HttpServletRequest request = HttpContextUtils.getHttpServletRequest();
            sysLog.setIp(SecurityUtils.getSubject().getSession().getHost());
            sysLog.setLineNumber(ex.getStackTrace()[0].getLineNumber());
            sysLog.setErrorName(ex.toString());
            sysLog.setErrorType("ERROR");
            String DeclaringType = String.valueOf(joinPoint.getSignature().getDeclaringType());
            sysLog.setName(joinPoint.getSignature().getName());
            sysLog.setModifiers(joinPoint.getSignature().getModifiers());

            sysErrorRepository.save(sysLog);
            System.out.println("【" + className + "】:" + methodName + "执行时出现异常：" + ex + "。");

    }
}
