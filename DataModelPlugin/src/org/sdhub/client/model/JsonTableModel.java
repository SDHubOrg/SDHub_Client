package org.sdhub.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;

public class JsonTableModel {

	@JSONField(ordinal = 0)
	private String table;
	
	@JSONField(ordinal = 1)
	private HashMap<String, String> fields;
	
	@JSONField(ordinal = 2)
	private List<JsonRecordModel> records;

	public String getTable() {
		return table;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
	
	public HashMap<String, String> getFields() {
		return fields;
	}
	
	public void setFields(HashMap<String, String> fields) {
		this.fields = fields;
	}
	
	public List<JsonRecordModel> getRecords() {
		return records;
	}

	public void setRecords(List<JsonRecordModel> records) {
		this.records = records;
	}
	
	public void init(String tableName, HashMap<String, String> fields)
	{
		this.setTable(tableName);
		this.setFields(fields);
		this.setRecords(new ArrayList<JsonRecordModel>());
	}
	
	public void addRecord(HashMap<String, String> data, String uid, Integer optNum, Integer seqNo)
	{
		JsonRecordModel jrm = new JsonRecordModel();
		
		jrm.setData(data);
		
		jrm.setUid(uid);
		jrm.setOptNum(optNum);
		jrm.setSeqNo(seqNo);
		
		this.getRecords().add(jrm);

	}
	
	public void resetSeqNo(Integer lastSeqNo)
	{
		int i = 1;
		for(JsonRecordModel jrm: records)
		{
			jrm.setSeqNo(lastSeqNo + i);
			i++;
		}
	}
	
	public Integer recordSize()
	{

		if(null == records)
		{
			return 0;
		}
		
		if(records.isEmpty())
		{
			return 0;
		}
		
		return records.size();

	}
	
}
