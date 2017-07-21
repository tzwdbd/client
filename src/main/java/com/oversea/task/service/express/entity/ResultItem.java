package com.oversea.task.service.express.entity;

import java.util.Date;

public class ResultItem {
	
	private Date time;
	private String context;
	private Date occurTime;//发生时间
	
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public Date getOccurTime() {
		return occurTime;
	}
	public void setOccurTime(Date occurTime) {
		this.occurTime = occurTime;
	}
	
}
