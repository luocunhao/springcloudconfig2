package com.pulan.dialogserver.entity;

public class Schedual {
    private String open_id;
    private String service;
    private String event_date;
    private String content;
    private String text;
    
	public Schedual(String open_id, String service, String event_date, String content, String text) {
		this.open_id = open_id;
		this.service = service;
		this.event_date = event_date;
		this.content = content;
		this.text = text;
	}
	public String getOpen_id() {
		return open_id;
	}
	public void setOpen_id(String open_id) {
		this.open_id = open_id;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getEvent_date() {
		return event_date;
	}
	public void setEvent_date(String event_date) {
		this.event_date = event_date;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	@Override
	public String toString() {
		return "Schedual [open_id=" + open_id + ", service=" + service + ", event_date=" + event_date + ", content="
				+ content + ", text=" + text + "]";
	}
    
}
