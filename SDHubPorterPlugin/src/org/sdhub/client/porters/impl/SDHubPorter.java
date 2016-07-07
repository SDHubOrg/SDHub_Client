package org.sdhub.client.porters.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.sdhub.client.interfaces.IDataPorter;
import org.sdhub.client.model.JsonRecordModel;
import org.sdhub.client.model.JsonTableModel;
import org.sdhub.client.porters.constants.OptNumber;
import org.sdhub.client.porters.database.MariaDBManager;
import org.sdhub.client.porters.util.SQLStringUtil;

import com.mysql.jdbc.DatabaseMetaData;

public class SDHubPorter implements IDataPorter {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SDHubPorter";
	}

	@Override
	public String getLastError() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isTableExists(String tableName) {
		
		boolean isExists = false;
		
		MariaDBManager.init();
		if(null == MariaDBManager.mariaDS)
		{
			return false;
		}
		Connection conn = null;
		PreparedStatement  stmt = null;
		ResultSet rs = null;
		try{

			conn = MariaDBManager.mariaDS.getConnection();
			//MariaDBManager.printInnerDSStats();
			conn.setAutoCommit(false);
			
			String sqlString = "SELECT COUNT(*) FROM " + tableName.toLowerCase();
			
			stmt = conn.prepareStatement(sqlString);

			rs = stmt.executeQuery();
			conn.commit();
			if(rs.next())
			{
				isExists = true;
			}

		}catch(SQLException sqlE)
		{
			if(sqlE.getErrorCode() == 1146)
			{
				isExists = false;
			}

		}finally {
            try {  
                if (rs != null){
                	if(!rs.isClosed())
                	{
                		rs.close(); 
                	}
                }
                if (stmt != null){
                    stmt.close(); 
                }
                if (conn != null){
                    conn.close();   
                }
            } catch (Exception e) {  
            	e.printStackTrace();
            }  
		}
		
		return isExists;

	}
	
	@Override
	public int getLastSeqNo(String tableName) {
		
		int maxSeqNo = -1;
		
		if(!isTableExists(tableName))
		{
			System.out.println("table does not exists :" + tableName);
			return -1;
		}
		
		MariaDBManager.init();
		if(null == MariaDBManager.mariaDS)
		{
			return -1;
		}
		Connection conn = null;
		PreparedStatement  stmt = null;
		ResultSet rs = null;
		try{
			conn = MariaDBManager.mariaDS.getConnection();
			//MariaDBManager.printInnerDSStats();
			conn.setAutoCommit(false);

			stmt = conn.prepareStatement("select max(seqNo) from " + tableName);

			rs = stmt.executeQuery();
			conn.commit();
			if(rs.next())
			{
				maxSeqNo = rs.getInt(1);
			}

		}catch(Exception e)
		{
			e.printStackTrace();

		}finally {
            try {  
                if (rs != null){
                	if(!rs.isClosed())
                	{
                		rs.close(); 
                	}
                }
                if (stmt != null){
                    stmt.close(); 
                }
                if (conn != null){
                    conn.close();   
                }
            } catch (Exception e) {  
            	e.printStackTrace();
            }  
		}
		
		return maxSeqNo;
	}

	@Override
	public int processTableModel(JsonTableModel jsonTableModel) {
		
		
		
		if(null == jsonTableModel.getRecords())
		{
			return -1;
		}
		
		if(jsonTableModel.getRecords().isEmpty())
		{
			return 0;
		}
		
		MariaDBManager.init();
		
		if(null == MariaDBManager.mariaDS)
		{
			return -1;
		}
		
		Connection conn = null;
		Statement  stmt = null;
		
		
		try{
			conn = MariaDBManager.mariaDS.getConnection();
			//MariaDBManager.printInnerDSStats();
			conn.setAutoCommit(false);
			stmt =  conn.createStatement();
			
			//TODO  
			for(JsonRecordModel jrmItem : jsonTableModel.getRecords())
			{
				String sqlString = "";
				if(jrmItem.getOptNum() == OptNumber.NEW)
				{
					jrmItem.getData().put("seqNO", String.valueOf(jrmItem.getSeqNo()));
					jrmItem.getData().put("lastOptNum", String.valueOf(jrmItem.getOptNum()));
					jrmItem.getData().put("UID", jrmItem.getUid());
					
					sqlString = SQLStringUtil.buildInsertSQL(jsonTableModel.getTable(), jrmItem.getData());
				}
				
				if(jrmItem.getOptNum() == OptNumber.UPDATE)
				{
					jrmItem.getData().put("seqNO", String.valueOf(jrmItem.getSeqNo()));
					jrmItem.getData().put("lastOptNum", String.valueOf(jrmItem.getOptNum()));
					
					sqlString = SQLStringUtil.buildUpdateSQLByUID(jsonTableModel.getTable(), jrmItem.getData(), jrmItem.getUid());
				}
				
/*				if(jrmItem.getOptNum() == OptNumber.DELETE)
				{
					jrmItem.getData().put("seqNO", String.valueOf(jrmItem.getSeqNo()));
					jrmItem.getData().put("lastOptNum", String.valueOf(jrmItem.getOptNum()));
					
					sqlString = SQLStringUtil.buildDeleteSQLByUID(jsonTableModel.getTable(), jrmItem.getUid());
				}*/
				
				//Just change the optNum, do not delete the record
				if(jrmItem.getOptNum() == OptNumber.DELETE)
				{
					jrmItem.getData().clear();
					jrmItem.getData().put("lastOptNum", String.valueOf(OptNumber.DELETE));
					
					sqlString = SQLStringUtil.buildUpdateSQLByUID(jsonTableModel.getTable(), jrmItem.getData(), jrmItem.getUid());
				}
				
				stmt.addBatch(sqlString);
			}
			
			stmt.executeBatch();
			conn.commit();

		}catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}finally {
            try {  

                if (stmt != null){
                    stmt.close(); 
                }
                if (conn != null){
                    conn.close();   
                }
            } catch (Exception e) {  
            	e.printStackTrace();
            }  
		}
		
		return 0;
	}

	@Override
	public int initTable(JsonTableModel jsonTableModel) {
		
		HashMap<String, String> fieldsInfo = jsonTableModel.getFields();
		
		if(fieldsInfo == null)
		{
			return -1;
		}
		
		if(fieldsInfo.isEmpty())
		{
			return -1;
		}
		
		for(String fieldName : fieldsInfo.keySet())
		{
			String fieldFormat = fieldsInfo.get(fieldName);
			String fieldFormatArray[] = fieldFormat.split("\\-");
			
			
			if("Char".equals(fieldFormatArray[0]))
			{
				//VARCHAR(size)
				StringBuilder formatTemp = new StringBuilder("VARCHAR");
				formatTemp.append("(");
				formatTemp.append(fieldFormatArray[1]);
				formatTemp.append(")");
				fieldsInfo.put(fieldName, formatTemp.toString());
				continue;
			}
			if("Int".equals(fieldFormatArray[0]))
			{
				//BIGINT(size)
				StringBuilder formatTemp = new StringBuilder("BIGINT");
				formatTemp.append("(");
				formatTemp.append(fieldFormatArray[1]);
				formatTemp.append(")");
				fieldsInfo.put(fieldName, formatTemp.toString());
				continue;
			}
			if("Float".equals(fieldFormatArray[0]))
			{
				//DOUBLE(size,d)
				StringBuilder formatTemp = new StringBuilder("DOUBLE");
				formatTemp.append("(");
				
				if("32".equals(fieldFormatArray[1]))
				{
					formatTemp.append("16");
					formatTemp.append(",");
					formatTemp.append("4");
				}else if("64".equals(fieldFormatArray[1]))
				{
					formatTemp.append("24");
					formatTemp.append(",");
					formatTemp.append("8");
				}
				
				formatTemp.append(")");
				fieldsInfo.put(fieldName, formatTemp.toString());
				continue;
			}
			if("Text".equals(fieldFormatArray[0]))
			{
				//TEXT
				fieldsInfo.put(fieldName, "TEXT");
				continue;
			}
		}
		
		fieldsInfo.put("lastOptNum", "INT(1)");
		fieldsInfo.put("seqNo", "INT(16)");
		fieldsInfo.put("UID", "VARCHAR(36)");
		
		String createTableString = SQLStringUtil.buildCreateTableIfNotExistsSQL(jsonTableModel.getTable(), fieldsInfo);
		
		MariaDBManager.init();
		if(null == MariaDBManager.mariaDS)
		{
			return -1;
		}
		Connection conn = null;
		PreparedStatement  stmt = null;

		try{
			conn = MariaDBManager.mariaDS.getConnection();
			//MariaDBManager.printInnerDSStats();
			conn.setAutoCommit(false);
			stmt = conn.prepareStatement(createTableString);

			stmt.execute();
			conn.commit();

		}catch(Exception e)
		{
			e.printStackTrace();
			return -1;
		}finally {
            try {  

                if (stmt != null){
                    stmt.close(); 
                }
                if (conn != null){
                    conn.close();   
                }
            } catch (Exception e) {  
            	e.printStackTrace();
            }  
		}

		return 0;
	}

}
