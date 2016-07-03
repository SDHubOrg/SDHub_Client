package org.sdhub.client.model;

import com.alibaba.fastjson.annotation.JSONField;

public class MainIndexRecordModel {

	@JSONField(ordinal = 0)
	private String tableName;

	@JSONField(ordinal = 1)
	private String schedule;
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getSchedule() {
		return schedule;
	}
	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}
}
