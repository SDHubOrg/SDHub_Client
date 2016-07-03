package org.sdhub.client.porters.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;

public class SQLStringUtil {
	
	public static String buildInsertSQL(String table, HashMap<String, String> values)
	{
		
		
		StringBuilder sb = new StringBuilder();
		
		StringBuilder sb1 = new StringBuilder();
		
		StringBuilder sb2 = new StringBuilder();
		
		int length = values.size();
		int count = 0;
		
		sb.append("INSERT INTO ");
		sb.append(table);
		sb.append(" ");

		Set<String> keys = values.keySet();
		sb1.append("( ");
		sb2.append("( ");
		for(String key : keys)
		{
			count ++;
			sb1.append(key);
			
			sb2.append("'");
			sb2.append(StringEscapeUtils.escapeSql(values.get(key)));
			sb2.append("'");
			
			if(count < length)
			{
				sb1.append(", ");
				sb2.append(", ");
			}
		}
		
		sb1.append(") ");
		sb2.append(") ");
		
		sb.append(sb1.toString());
		sb.append(" VALUES ");
		sb.append(sb2.toString());
		
		return sb.toString();
	}
	
	//UPDATE 琛ㄥ悕绉� SET 鍒楀悕绉� = 鏂板�� WHERE 鍒楀悕绉� = 鏌愬��
	//UPDATE Person SET Address = 'Zhongshan 23', City = 'Nanjing' WHERE LastName = 'Wilson'
	public static String buildUpdateSQL(String table, HashMap<String, String> values, String conditions)
	{
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		int length = values.size();
		int count = 0;
		
		sb.append("UPDATE ");
		sb.append(table);
		sb.append(" SET ");

		for(String key : values.keySet())
		{
			count ++;
			String temp = key + " = " + "'" + StringEscapeUtils.escapeSql(values.get(key)) + "'";
			sb1.append(temp);
			if(count < length)
			{
				sb1.append(", ");
			}
		}
		
		sb.append(sb1.toString());
		
		sb.append(" ");
		sb.append(conditions);
		
		return sb.toString();
	}
	
	public static String buildUpdateSQLByUID(String table, HashMap<String, String> values, String uid)
	{
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		int length = values.size();
		int count = 0;
		
		sb.append("UPDATE ");
		sb.append(table);
		sb.append(" SET ");

		for(String key : values.keySet())
		{
			count ++;
			String temp = key + " = " + "'" + StringEscapeUtils.escapeSql(values.get(key)) + "'";
			sb1.append(temp);
			if(count < length)
			{
				sb1.append(", ");
			}
		}
		
		sb.append(sb1.toString());
		
		sb.append(" WHERE UID ='");
		sb.append(uid);
		sb.append("'");
		
		return sb.toString();
	}
	
	public static String buildRetriveSQL()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT ");
		
		return sb.toString();
	}
	
	public static String buildDeleteSQLByUID(String table, String uid)
	{
		//DELETE FROM Person WHERE LastName = 'Wilson' 
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ");
		sb.append(table);
		sb.append(" WHERE UID=");
		sb.append("'");
		sb.append(uid);
		sb.append("'");
		
		return sb.toString();
	}
	//create table if not exists tablename
	public static String buildCreateTableIfNotExistsSQL(String tableName, HashMap<String, String> fieldsInfo)
	{
/*		CREATE TABLE IF NOT EXISTS Persons
		(
		Id_P int,
		LastName varchar(255),
		FirstName varchar(255),
		Address varchar(255),
		City varchar(255)
		)*/
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(tableName);
		sb.append(" ");
		sb.append("(");
		
		int length = fieldsInfo.size();
		int count = 0;
		for(String fieldName: fieldsInfo.keySet())
		{
			count ++;
			String temp = fieldName + " " + fieldsInfo.get(fieldName);
			sb.append(StringEscapeUtils.escapeSql(temp));
			if(count < length)
			{
				sb.append(", ");
			}
		}
		
		sb.append(")");
		return sb.toString();
	}
	
	public static String buildCreateTableSQL(String tableName, HashMap<String, String> fieldsInfo)
	{
/*		CREATE TABLE Persons
		(
		Id_P int,
		LastName varchar(255),
		FirstName varchar(255),
		Address varchar(255),
		City varchar(255)
		)*/
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append(tableName);
		sb.append(" ");
		sb.append("(");
		
		int length = fieldsInfo.size();
		int count = 0;
		for(String fieldName: fieldsInfo.keySet())
		{
			count ++;
			String temp = fieldName + " " + fieldsInfo.get(fieldName);
			sb.append(StringEscapeUtils.escapeSql(temp));
			if(count < length)
			{
				sb.append(", ");
			}
		}
		
		sb.append(")");
		return sb.toString();
	}

}
