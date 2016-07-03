package org.sdhub.client.controller;

import java.util.ArrayList;
import java.util.List;

import org.sdhub.client.interfaces.IDataFetcher;
import org.sdhub.client.interfaces.IDataPorter;
import org.sdhub.client.model.JsonTableModel;
import org.sdhub.client.model.TableIndexRecordModel;

public class TaskThread extends Thread{

	private String dataSource;
	private String tableName;
	private IDataFetcher dataFetcher;
	private IDataPorter dataPorter;
	
	public TaskThread(String dataSource, String tableName, IDataFetcher dataFetcher, IDataPorter dataPorter) {
		this.dataSource = dataSource;
		this.tableName = tableName;
		this.dataFetcher = dataFetcher;
		this.dataPorter = dataPorter;
	}
	
	@Override
	public void run() {
		
		List<TableIndexRecordModel> tableIndexRecordList = dataFetcher.fetchIndex(tableName);
		
		if(null == tableIndexRecordList)
		{
			return;
		}
		if(tableIndexRecordList.isEmpty())
		{
			return;
		}
		
		int lastSeqNoInDB = dataPorter.getLastSeqNo(tableName);
		
		List<JsonTableModel> dataList = dataFetcher.fetchData(tableName, lastSeqNoInDB);
		
		if(dataList.isEmpty())
		{
			return;
		}
		
		
		
		for(JsonTableModel jsonTableModel : dataList)
		{
			dataPorter.initTable(jsonTableModel);
			dataPorter.processTableModel(jsonTableModel);
		}

	}

}
