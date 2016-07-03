package org.sdhub.client.interfaces;

import org.sdhub.client.model.JsonTableModel;

public interface IDataPorter {

	public String getName();
	public String getLastError();
	
	public int getLastSeqNo(String tableName);
	
	public int processTableModel(JsonTableModel jsonTableModel);
	
	public int initTable(JsonTableModel jsonTableModel);
	
}
