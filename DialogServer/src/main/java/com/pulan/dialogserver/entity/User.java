package com.pulan.dialogserver.entity;

import java.io.Serializable;

public class User implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7531430443748391139L;
	private String id;
	private String mail_name;
	private String cn_name;
	private String ldap_dn;
	private String department;
	private String mobile;
	private String email;
	private String pinyin;
	private String imei;
	private String role;
	private String create_time;
	private String password;
	public User(){}
	public User(String mail_name, String cn_name, String ldap_dn, String department, String mobile,
                String email, String pinyin, String imei, String role, String create_time, String password) {
		this.mail_name = mail_name;
		this.cn_name = cn_name;
		this.ldap_dn = ldap_dn;
		this.department = department;
		this.mobile = mobile;
		this.email = email;
		this.pinyin = pinyin;
		this.imei = imei;
		this.role = role;
		this.create_time = create_time;
		this.password = password;
	}
	public User(String id, String mail_name, String cn_name, String ldap_dn, String department, String mobile,
                String email, String pinyin, String imei, String role, String create_time, String password) {
		this.id = id;
		this.mail_name = mail_name;
		this.cn_name = cn_name;
		this.ldap_dn = ldap_dn;
		this.department = department;
		this.mobile = mobile;
		this.email = email;
		this.pinyin = pinyin;
		this.imei = imei;
		this.role = role;
		this.create_time = create_time;
		this.password = password;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMail_name() {
		return mail_name;
	}
	public void setMail_name(String mail_name) {
		this.mail_name = mail_name;
	}
	public String getCn_name() {
		return cn_name;
	}
	public void setCn_name(String cn_name) {
		this.cn_name = cn_name;
	}
	public String getLdap_dn() {
		return ldap_dn;
	}
	public void setLdap_dn(String ldap_dn) {
		this.ldap_dn = ldap_dn;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", mail_name=" + mail_name + ", cn_name=" + cn_name + ", ldap_dn=" + ldap_dn
				+ ", department=" + department + ", mobile=" + mobile + ", email=" + email + ", pinyin=" + pinyin
				+ ", imei=" + imei + ", role=" + role + ", create_time=" + create_time + ", password=" + password + "]";
	}
	
}
