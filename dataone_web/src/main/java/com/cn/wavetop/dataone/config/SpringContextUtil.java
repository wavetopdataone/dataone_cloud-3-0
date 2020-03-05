package com.cn.wavetop.dataone.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Locale;

/**
 * @author yongz
 * @Description:
 * @date 2019/11/14 16:57
 */
//spring 中通过ApplicationContext getBean获取注入对象, 必须在需要注入之前（被依赖）
public class SpringContextUtil implements ApplicationContextAware {
 
    private static ApplicationContext applicationContext;
 
    /**
     * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextUtil.applicationContext = context;
    }
 
    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
 
    /**
     * 获取对象
     * 这里重写了bean方法，起主要作用
     * @param beanName
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    public static Object getBean(String beanName) throws BeansException{
        return applicationContext.getBean(beanName);
    }
 
    public static Object getMessage(String key) {
        return applicationContext.getMessage(key, null, Locale.getDefault());
    }
 
 
    /**
     * 清除applicationContext静态变量.
     */
    public static void cleanApplicationContext() {
        applicationContext = null;
    }
 
    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext未注入");
        }
    }
    /**
     * 用类来获取bean
     * @param c
     * @return
     */
    public static <T> T getBean(Class<T> c){

        return (T) applicationContext.getBean(c);
    }

}

