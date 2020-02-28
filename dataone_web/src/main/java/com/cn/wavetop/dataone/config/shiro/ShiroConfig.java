package com.cn.wavetop.dataone.config.shiro;


import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ShiroConfig {

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") SecurityManager securityManager){
        ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
        bean.setLoginUrl("/login");
        //自定义拦截器
        Map<String, Filter> customFilterMap = new LinkedHashMap<>();
       // customFilterMap.put("corsAuthenticationFilter", new CORSAuthenticationFilter());
       //不能同时登录一个账号
        customFilterMap.put("kickout", kickoutSessionFilter());
       //登录拦截判断
        customFilterMap.put("authc", new ShiroFormAuthenticationFilter());
        //权限拦截判断
//        customFilterMap.put("authorization", new ShiroRoleAuthorizationFilter());
        bean.setFilters(customFilterMap);
         //过滤器
        LinkedHashMap<String,String> filterChainDefinitionMap=new LinkedHashMap<>();
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/csrf", "anon");
        filterChainDefinitionMap.put("/v2/**", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/login**", "anon");
        filterChainDefinitionMap.put("/login/**", "anon");
        filterChainDefinitionMap.put("/sys_user/login**", "anon");
        filterChainDefinitionMap.put("/sys_user/login/", "anon");
        filterChainDefinitionMap.put("/sys_jobrela/findById","anon");

        //导出
        filterChainDefinitionMap.put("/sys_loginlog/OutPutLoginExcel**", "anon");
        filterChainDefinitionMap.put("/sys_log/OutPutExcel**", "anon");
        filterChainDefinitionMap.put("/sys_userlog/OutPutUserExcel**", "anon");
        filterChainDefinitionMap.put("/errorlog/outErrorlog**", "anon");
        filterChainDefinitionMap.put("/userlog/OutputError**", "anon");
        filterChainDefinitionMap.put("/sysError/**", "anon");

//        filterChainDefinitionMap.put("/sys_user/login_out/", "anon");
        //后台kafka调用的接口
        filterChainDefinitionMap.put("/toback/**", "anon");
        filterChainDefinitionMap.put("/sys_jobrela/findById","anon");
        //前端js，css，图片
        filterChainDefinitionMap.put("/icons/**", "anon");
        filterChainDefinitionMap.put("/asset-manifest.json/**", "anon");
        filterChainDefinitionMap.put("/index.html", "anon");
        filterChainDefinitionMap.put("/**.js", "anon");
        filterChainDefinitionMap.put("/**.css", "anon");
        filterChainDefinitionMap.put("/sys_user/login/**", "anon");
        filterChainDefinitionMap.put("/static/**.js", "anon");
        filterChainDefinitionMap.put("/static/**.css", "anon");
        filterChainDefinitionMap.put("/static/**.jpg", "anon");
        filterChainDefinitionMap.put("/api/**", "anon");
        filterChainDefinitionMap.put("/favicon.**", "anon");
        filterChainDefinitionMap.put("**.png", "anon");
        filterChainDefinitionMap.put("**.jpg", "anon");

//        前端react的路由
        filterChainDefinitionMap.put("**.js", "anon");
        filterChainDefinitionMap.put("//at.alicdn.com/t/font_1537367_enrqpjp2dab.js", "anon");
        filterChainDefinitionMap.put("/register*", "anon");

        filterChainDefinitionMap.put("/busList**", "anon");
        filterChainDefinitionMap.put("/busList/**", "anon");
        filterChainDefinitionMap.put("/sys_user/login/", "anon");
        filterChainDefinitionMap.put("/user**", "anon");
        filterChainDefinitionMap.put("/user/**", "anon");
        filterChainDefinitionMap.put("/personal**", "anon");
        filterChainDefinitionMap.put("/personal/**", "anon");
        filterChainDefinitionMap.put("/busCreat**", "anon");
        filterChainDefinitionMap.put("/busCreat/**", "anon");
        filterChainDefinitionMap.put("/watch**", "anon");
        filterChainDefinitionMap.put("/watch/**", "anon");
        filterChainDefinitionMap.put("/log**", "anon");
        filterChainDefinitionMap.put("/log/**", "anon");
        filterChainDefinitionMap.put("/usermanage**", "anon");
        filterChainDefinitionMap.put("/usermanage/**", "anon");
        filterChainDefinitionMap.put("/bussiness**", "anon");
        filterChainDefinitionMap.put("/bussiness/**", "anon");
        //忘记密码时的接口
        filterChainDefinitionMap.put("/sys_user/sendEmail/**", "anon");
        filterChainDefinitionMap.put("/sys_user/codeEquals/**", "anon");
        filterChainDefinitionMap.put("/sys_user/editPasswordByEmail/**", "anon");

//        filterChainDefinitionMap.put("/sys_monitoring/**", "anon");


        filterChainDefinitionMap.put("/**", "authc");


//      filterChainDefinitionMap.put("/**", "authc,kickout");
              bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
    }


    @Bean("securityManager")
    public SecurityManager securityManager(@Qualifier("myShiroRelam") MyShiroRelam myShiroRelam,@Qualifier("sessionManager")SessionManager sessionManager){
        DefaultWebSecurityManager manager=new DefaultWebSecurityManager();
        //manager.setCacheManager(cacheManager());
        manager.setSessionManager(sessionManager);
        manager.setCacheManager(ehCacheManager());
        manager.setRealm(myShiroRelam);
        return manager;
    }

    @Bean("myShiroRelam")
    public MyShiroRelam  myShiroRelam(@Qualifier("credentialsMatcher") CredentialMatcher matcher){
      MyShiroRelam myShiroRelam=new MyShiroRelam();
      myShiroRelam.setCredentialsMatcher(matcher);

     // myShiroRelam.setCacheManager(ehCacheManager());
        return myShiroRelam;
    }

    @Bean("credentialsMatcher")
    public CredentialMatcher credentialsMatcher(){
        return new CredentialMatcher();
    }
    //开启shiro aop注解支持，不开启的话权限验证就会失效

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager( securityManager);
        return authorizationAttributeSourceAdvisor;
    }
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator=new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
    /**
     * Session Manager：会话管理
     * 即用户登录后就是一次会话，在没有退出之前，它的所有信息都在会话中；
     * 会话可以是普通JavaSE环境的，也可以是如Web环境的；
     */
    @Bean("sessionManager")
    public SessionManager sessionManager(){
        ShiroSessionManager sessionManager = new ShiroSessionManager();
        sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());
        // 删除过期的session
        sessionManager.setDeleteInvalidSessions(true);

//        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
//        sessionManager.setSessionDAO(sessionDAO());
//        //设置session过期时间
//        sessionManager.setGlobalSessionTimeout(60 * 60 * 1000);
//        sessionManager.setSessionValidationSchedulerEnabled(true);
//        // 去掉shiro登录时url里的JSESSIONID
//        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }
    @Bean
    public CacheManager cacheManager() {

        return new MemoryConstrainedCacheManager();
    }

    @Bean
    public EhCacheManager ehCacheManager() {
        EhCacheManager cacheManager = new EhCacheManager();
        //ry的意思跟我這個一樣的吧
//        net.sf.ehcache.CacheManager cacheManagers = net.sf.ehcache.CacheManager.getCacheManager("es");
//        if(cacheManagers==null) {
//            cacheManager.setCacheManager(new net.sf.ehcache.CacheManager());
//        }else{
//            cacheManager.setCacheManager(cacheManagers);
//        }
        cacheManager.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return cacheManager;
    }
    @Bean
    public SessionDAO sessionDAO() {
        return new MemorySessionDAO();//使用默认的MemorySessionDAO
    }

    /**
     * 同一个用户多设备登录限制
     */
    public KickoutSessionFilter kickoutSessionFilter()
    {
        KickoutSessionFilter kickoutSessionFilter = new KickoutSessionFilter();
        kickoutSessionFilter.setCacheManager(ehCacheManager());
        kickoutSessionFilter.setSessionManager(sessionManager());
        // 同一个用户最大的会话数，默认-1无限制；比如2的意思是同一个用户允许最多同时两个人登录
        kickoutSessionFilter.setMaxSession(1);
        // 是否踢出后来登录的，默认是false；即后者登录的用户踢出前者登录的用户；踢出顺序
        kickoutSessionFilter.setKickoutAfter(false);
        // 被踢出后重定向到的地址；
        kickoutSessionFilter.setKickoutUrl("/login");
        return kickoutSessionFilter;
    }

    //配置异常处理，不配置的话没有权限后台报错，前台不会跳转到403页面
    @Bean(name="simpleMappingExceptionResolver")
    public SimpleMappingExceptionResolver
    createSimpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();
        Properties mappings = new Properties();
        mappings.setProperty("DatabaseException", "databaseError");//数据库异常处理
        mappings.setProperty("UnauthorizedException","403");
        simpleMappingExceptionResolver.setExceptionMappings(mappings);  // None by default
        simpleMappingExceptionResolver.setDefaultErrorView("error");    // No default
        simpleMappingExceptionResolver.setExceptionAttribute("ex");     // Default is "exception"
        return simpleMappingExceptionResolver;
    }
    /**
     *
     * @return MethodInvokingFactoryBean 实例
     */
    @Bean
    public MethodInvokingFactoryBean methodInvokingFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        bean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
        bean.setArguments(securityManager);
        return bean;
    }

}
