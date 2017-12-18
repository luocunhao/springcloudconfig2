package com.pulan.dialogserver.config;

import com.pulan.dialogserver.shiro.CredentialsMatcher;
import com.pulan.dialogserver.shiro.LdapAuthRealm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
@EnableAutoConfiguration
public class ShiroConfiguration {
	private static final Logger logger = LogManager.getLogger(ShiroConfiguration.class);
	@Bean(name = "shiroFilter")
	public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManager") SecurityManager manager){
		ShiroFilterFactoryBean bean=new ShiroFilterFactoryBean();
		bean.setSecurityManager(manager);
		//配置登录的url和登录成功的url
		bean.setLoginUrl("/message/app/login");
		//配置访问权限  filterChainDefinitionMap必须是LinkedHashMap因为它必须保证有序
		LinkedHashMap<String, String> filterChainDefinitionMap=new LinkedHashMap<>();
		filterChainDefinitionMap.put("/message/manisdplservertext","anon");
        filterChainDefinitionMap.put("/message/plttsservice", "authc");
//		filterChainDefinitionMap.put("/message/loginUser", "anon"); //表示可以匿名访问
//		filterChainDefinitionMap.put("/message/logout", "anon");
//		filterChainDefinitionMap.put("/message/app/isRegisterIMEI", "anon");
//		filterChainDefinitionMap.put("/message/app/isImeiValid", "anon");
//        filterChainDefinitionMap.put("/message/verbal/select*","user");
//        filterChainDefinitionMap.put("/message/verbal/insert*","authc,perms[admin:add]");
//        filterChainDefinitionMap.put("/message/verbal/update*","authc,perms[admin:update]");
//        filterChainDefinitionMap.put("/message/verbal/delete*","authc,perms[admin:delete]");
//        filterChainDefinitionMap.put("/message/*", "authc");//表示需要认证才可以访问
//        filterChainDefinitionMap.put("/message/**", "authc");//表示需要认证才可以访问
        bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return bean;
	}
	//配置核心安全事务管理器
	@Bean(name="securityManager")
	public SecurityManager securityManager(@Qualifier("authRealm") LdapAuthRealm authRealm){
		logger.info("--------------shiro已经加载----------------");
		DefaultWebSecurityManager manager=new DefaultWebSecurityManager();
		//用户授权/认证信息Cache, 采用EhCache缓存
	    manager.setCacheManager(ehCacheManager());
	    //注入记住我管理器
	    manager.setRememberMeManager(rememberMeManager());
        manager.setRealm(authRealm);
        return manager;
	}
	/**
     * shiro缓存管理器;
     * 需要注入对应的其它的实体类中：
     * 1、安全管理器：securityManager
     * 可见securityManager是整个shiro的核心；
     * @return 返回管理器
     */
    @Bean
    public EhCacheManager ehCacheManager(){
        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManagerConfigFile("classpath:config/ehcache-shiro.xml");
        logger.info("缓存配置文件路径"+cacheManager.getCacheManagerConfigFile());
        return cacheManager;
    }
//	//配置自定义的权限登录器
//    @Bean(name="authRealm")
//    public AuthRealm authRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
//        AuthRealm authRealm=new AuthRealm();
//        authRealm.setCredentialsMatcher(matcher);
//        return authRealm;
//    }
  @Bean(name="authRealm")
  public LdapAuthRealm authRealm(@Qualifier("credentialsMatcher") CredentialsMatcher matcher) {
	  LdapAuthRealm authRealm=new LdapAuthRealm();
	  authRealm.setCredentialsMatcher(matcher);
      return authRealm;
  }
    
  //配置自定义的密码比较器
    @Bean(name="credentialsMatcher")
    public CredentialsMatcher credentialsMatcher() {
        return new CredentialsMatcher();
    }
    /**
     * cookie对象;
     * rememberMeCookie()方法是设置Cookie的生成模版，比如cookie的name，cookie的有效时间等等。
     * @return
    */
    @Bean
    public SimpleCookie rememberMeCookie(){
          //System.out.println("ShiroConfiguration.rememberMeCookie()");
          //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
          SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
          //<!-- 记住我cookie生效时间30天 ,单位秒;-->
          simpleCookie.setMaxAge(12*60*60);
          return simpleCookie;
    }
    @Bean
    public CookieRememberMeManager rememberMeManager(){
          //System.out.println("ShiroConfiguration.rememberMeManager()");
          CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
          cookieRememberMeManager.setCookie(rememberMeCookie());
          //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
          cookieRememberMeManager.setCipherKey(Base64.decode("2AvVhdsgUs0FSA3SDFAdag=="));
          return cookieRememberMeManager;
    }
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator creator=new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(@Qualifier("securityManager") SecurityManager manager) {
        AuthorizationAttributeSourceAdvisor advisor=new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(manager);
        return advisor;
    }
}
