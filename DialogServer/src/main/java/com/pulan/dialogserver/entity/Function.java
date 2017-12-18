package com.pulan.dialogserver.entity;

public class Function {
    private int fun_id;
    private String function;
	public Function(int fun_id, String function) {
		this.fun_id = fun_id;
		this.function = function;
	}
	public int getFun_id() {
		return fun_id;
	}
	public void setFun_id(int fun_id) {
		this.fun_id = fun_id;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
    
}
