package com.pulan.dialogserver.shiro;

import com.pulan.dialogserver.utils.LdapHelper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

public class CredentialsMatcher extends SimpleCredentialsMatcher {
	private Logger logger = LogManager.getLogger(CredentialsMatcher.class.getName());
	@Override
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
	// TODO Auto-generated method stub
		UsernamePasswordToken utoken = (UsernamePasswordToken) token;
		return LdapHelper.authenticate(utoken.getUsername(),String.valueOf(utoken.getPassword()));

	}

}
