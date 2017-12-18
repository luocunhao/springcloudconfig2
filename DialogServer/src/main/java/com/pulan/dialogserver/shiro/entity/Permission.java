package com.pulan.dialogserver.shiro.entity;

public class Permission {
	private int id;
	private String permission_name;
	private int role_id;
	public Permission(int id, String permission_name, int role_id) {
		this.id = id;
		this.permission_name = permission_name;
		this.role_id = role_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPermission_name() {
		return permission_name;
	}
	public void setPermission_name(String permission_name) {
		this.permission_name = permission_name;
	}
	public int getRole_id() {
		return role_id;
	}
	public void setRole_id(int role_id) {
		this.role_id = role_id;
	}
	@Override
	public String toString() {
		return "permission [id=" + id + ", permission_name=" + permission_name + ", role_id=" + role_id + "]";
	}
	
}
