package com.pulan.dialogserver.shiro;

import com.pulan.dialogserver.shiro.entity.User;
import com.pulan.dialogserver.utils.ShiroDao;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

public class LdapAuthRealm extends AuthorizingRealm {
	private Logger logger = Logger.getLogger(LdapAuthRealm.class.getName());
	@Autowired
	private ShiroDao shiroDao;
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// TODO Auto-generated method stub
		logger.info("身份认证登录");
		UsernamePasswordToken authcToken = (UsernamePasswordToken) token;
		String userName = authcToken.getUsername();
		String password = String.valueOf(authcToken.getPassword());
		logger.info("用户名  :"+userName+", "+"密码 : "+password);

		User user = shiroDao.findByUserName(userName);
		logger.info("UserLdap:"+user.toString());
        return new SimpleAuthenticationInfo(user, password, getName());
		}
	}


