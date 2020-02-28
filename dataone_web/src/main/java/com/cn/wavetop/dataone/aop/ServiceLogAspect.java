package com.cn.wavetop.dataone.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ServiceLogAspect {
    // slf4j日志记录器
     private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class.getName());

    /**
     * 定义切入点
     */
    @Pointcut("execution(* org.yong.hellospringjpa.service.*.*(..))")
    public void pointCut(){}

    /**
     * 前置增强
     * @param jp
     */
    @Before(value="pointCut()")
    public void before(JoinPoint jp){
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<该方法进行前置增强>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        logger.info("调用"+jp.getTarget()+"的"+jp.getSignature()+"方法，方法参数："+ Arrays.toString(jp.getArgs()));
    }

    /**
     * 后置
     * @param jp
     * @param result
     */
    @AfterReturning(value="pointCut()",returning = "result")
    public void after(JoinPoint jp, Object result){
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<该方法进行后置增强>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        logger.info("调用"+jp.getTarget()+"的"+jp.getSignature()+"方法，方法返回值："+ result);
    }


//    /**
//     *异常增强
//     */
//    @AfterThrowing(value="execution(* org.yong.hellospringjpa.service.*.*(..))")
//    public void afterThrowing(JoinPoint point){
//        logger.info("<<<<<<<<<<<<<<<<<<<<<<<我是异常增强>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        logger.info(point.getSignature().getName());
//    }
//    /**
//     * 环绕增强
//     */
//    @Around(value="execution(* org.yong.hellospringjpa.service.*.*(..))")
//    public Object myAround(ProceedingJoinPoint point){
//        Object result=null;
//
//        try {
//            logger.info("<<<<<<<<<<<<<<<<<<<<<<<环绕增强开始了>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//            logger.info(point.getKind()+point.getArgs());
//            point.proceed();
//            logger.info("<<<<<<<<<<<<<<<<<<<<<<<环绕增强后置增强了>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//            logger.info(point.getTarget()+""+point.getClass());
//        } catch (Throwable e) {
//            logger.info("<<<<<<<<<<<<<<<<<<<<<<<环绕增强,异常增强处理>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//            e.printStackTrace();
//        }finally{
//            logger.info("<<<<<<<<<<<<<<<<<<<<<<<环绕增强最终增强>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        }
//
//        return result;
//
//    }

}
