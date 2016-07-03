package org.sdhub.client.model;

import java.util.HashMap;

import com.alibaba.fastjson.annotation.JSONField;

public class JsonRecordModel {
	
	@JSONField(ordinal = 0)
	private String uid;
	
	@JSONField(ordinal = 1)
	private int optNum;

	@JSONField(ordinal = 2)
	private int seqNo;
	
	@JSONField(ordinal = 3)
	private HashMap<String, String> data;
	

	public String getUid() {
		return uid;
	}
	
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public int getOptNum() {
		return optNum;
	}
	
	public void setOptNum(int optNum) {
		this.optNum = optNum;
	}
	
	public int getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public HashMap<String, String> getData() {
		return data;
	}
	public void setData(HashMap<String, String> data) {
		this.data = data;
	}
}
