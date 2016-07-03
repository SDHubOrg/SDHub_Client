package org.sdhub.client.model;

import com.alibaba.fastjson.annotation.JSONField;

public class TableIndexRecordModel {
	
	public static final int NONE_SEQNO = -1;
	public static final int FIRST_SEQNO = 0;

	@JSONField(ordinal = 0)
	private String name;
	
	@JSONField(ordinal = 1)
	private String timestamp;
	
	@JSONField(ordinal = 2)
	private int firstSeqNo;

	@JSONField(ordinal = 3)
	private int lastSeqNo;

	public TableIndexRecordModel()
	{
		lastSeqNo = -1;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	public int getFirstSeqNo() {
		return firstSeqNo;
	}

	public void setFirstSeqNo(int firstSeqNo) {
		this.firstSeqNo = firstSeqNo;
	}

	public int getLastSeqNo() {
		return lastSeqNo;
	}
	
	public void setLastSeqNo(int lastSeqNo) {
		this.lastSeqNo = lastSeqNo;
	}
	
}

