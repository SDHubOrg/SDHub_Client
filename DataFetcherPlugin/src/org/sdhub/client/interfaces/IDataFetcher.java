package org.sdhub.client.interfaces;

import java.util.List;

import org.sdhub.client.model.JsonTableModel;
import org.sdhub.client.model.MainIndexRecordModel;
import org.sdhub.client.model.TableIndexRecordModel;

public interface IDataFetcher {

	public String getName();
	public String getLastError();
	
	public void fetchUpdateData(String tableName);
	
	public List<TableIndexRecordModel> fetchIndex(String tableName);
	
	public List<MainIndexRecordModel> fetchSchedule();
	
	public List<JsonTableModel> fetchData(String tableName, int lastSeqNo);
	
	public List<JsonTableModel> fetchData(String tableName);
	
	public List<TableIndexRecordModel> loadIndex(String tableName);
	
	public List<JsonTableModel> loadData(String tableName);
	
	public List<JsonTableModel> loadData(String tableName, int lastSeqNo);
	
	public boolean checkLocalData(String tableName);

}
