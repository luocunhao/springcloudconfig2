package com.pulan.dialogserver.utils;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;


public class LdapHelper {
	private static DirContext ctx;
	@SuppressWarnings("unchecked")
	public static boolean authenticate(String usr,String password) {
      String root = "OU=A花样年集团（中国）有限公司,OU=花样年控股集团有限公司,DC=hyn,DC=com"; // root
      Hashtable env = new Hashtable();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
      env.put(Context.PROVIDER_URL, "ldap://192.168.0.6:389/" + root);
      env.put(Context.SECURITY_AUTHENTICATION, "simple");
      env.put(Context.SECURITY_PRINCIPAL, "hyn\\"+usr );
      env.put(Context.SECURITY_CREDENTIALS, password);
      try {
          // 链接ldap
          ctx = new InitialDirContext(env);
          System.out.println("认证成功");
          return true;
      } catch (javax.naming.AuthenticationException e) {
          System.out.println("认证失败");
          return false;
      } catch (Exception e) {
          System.out.println("认证出错：");
          return false;
      }
  }
}
