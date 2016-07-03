package org.sdhub.client.fetchers.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.sdhub.client.fetchers.Activator;
import org.sdhub.client.fetchers.preferences.PreferenceConstants;
import org.sdhub.client.interfaces.IDataFetcher;
import org.sdhub.client.model.JsonTableModel;
import org.sdhub.client.model.MainIndexRecordModel;
import org.sdhub.client.model.TableIndexRecordModel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONReader;
import com.alibaba.fastjson.JSONWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.eclipse.jface.preference.IPreferenceStore;

public class SDHubFetcher implements IDataFetcher {

	@Override
	public String getName() {
		return "SDHubFetcher";
	}

	@Override
	public String getLastError() {
		// TODO Auto-generated method stub
		return null;
	}

/*	public void fetchUpdateData(String dataSource, String localStore, String tableName);
	public boolean checkLocalData(String localStore, String tableName);
	public List<TableIndexRecordModel> fetchIndex(String dataSource, String tableName);
	public List<MainIndexRecordModel> fetchSchedule(String dataSource);
	public List<JsonTableModel> loadData(String localStore, String tableName);
	public List<JsonTableModel> loadData(String localStore, String tableName, int lastSeqNo);*/

	public List<MainIndexRecordModel> fetchSchedule(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_DATA_SOURCE))
		{
			if(!store.getString(PreferenceConstants.SD_DATA_SOURCE).isEmpty())
			{
				return fetchSchedule(store.getString(PreferenceConstants.SD_DATA_SOURCE));
			}
		}
		
		return new ArrayList<MainIndexRecordModel>();
		
	}
	
	public List<MainIndexRecordModel> fetchSchedule(String dataSource) {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  

        HttpGet httpget = new HttpGet(dataSource+ "/index.json");

        HttpResponse response;
        
        List<MainIndexRecordModel> scheduleList = new ArrayList<MainIndexRecordModel>();
		try {
			response = closeableHttpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String scheduleString = EntityUtils.toString(entity, Charset.forName("UTF-8"));
			scheduleList = JSON.parseArray(scheduleString, MainIndexRecordModel.class);
			//TODO
			//JSONReader jReader = new JSONReader(new InputStreamReader(response.getEntity().getContent()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
			try {

				closeableHttpClient.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return scheduleList;
	}
	
	public List<TableIndexRecordModel> fetchIndex(String tableName) {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_DATA_SOURCE))
		{
			if(!store.getString(PreferenceConstants.SD_DATA_SOURCE).isEmpty())
			{
				return fetchIndex(store.getString(PreferenceConstants.SD_DATA_SOURCE), tableName);
			}
		}
		
		return new ArrayList<TableIndexRecordModel>();
	}

	public List<TableIndexRecordModel> fetchIndex(String dataSource, String tableName) {

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  
        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  

        HttpGet httpget = new HttpGet(dataSource + "/" + tableName + "/index.json");
        
        HttpResponse response;
        
        List<TableIndexRecordModel> tirmList = new ArrayList<TableIndexRecordModel>();

        try {
			response = closeableHttpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String recordString = EntityUtils.toString(entity, Charset.forName("UTF-8"));
			tirmList = JSON.parseArray(recordString, TableIndexRecordModel.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
			try {

				closeableHttpClient.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return tirmList;
	}
	
	public void fetchUpdateData(String tableName) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_DATA_SOURCE) && store.contains(PreferenceConstants.SD_LOCAL_STORE))
		{
			if((!store.getString(PreferenceConstants.SD_DATA_SOURCE).isEmpty()) && (!store.getString(PreferenceConstants.SD_LOCAL_STORE).isEmpty()))
			{
				fetchUpdateData(store.getString(PreferenceConstants.SD_DATA_SOURCE), store.getString(PreferenceConstants.SD_LOCAL_STORE), tableName);
			}
		}
		
		return;
	}

	public void fetchUpdateData(String dataSource, String localStore, String tableName) {
		//TODO
		boolean isLocalStoreEmpty = false;
		
		int lastSeqNoInLocal = -1;
		
		List<TableIndexRecordModel> remoteList = fetchIndex(dataSource, tableName);
		List<TableIndexRecordModel> localList = loadIndex(localStore, tableName);
		
		if(null == localList)
		{
			isLocalStoreEmpty = true;
		}
		
		if(localList.isEmpty())
		{
			isLocalStoreEmpty = true;
		}
		
		if(isLocalStoreEmpty)
		{
			if(remoteList.get(0).getFirstSeqNo() > 0)
			{
				System.out.println("need history data package!!");
				return;
			}
			lastSeqNoInLocal = -1;

		}else{
			lastSeqNoInLocal = localList.get(localList.size() - 1).getLastSeqNo();
		}
		
		List<JsonTableModel> remoteDatas = fetchData(dataSource, tableName, lastSeqNoInLocal);
		int index = 0;

		for(JsonTableModel jsonTableModel : remoteDatas)
		{
			for(TableIndexRecordModel tirmTemp : remoteList)
			{
				if(tirmTemp.getLastSeqNo() == jsonTableModel.getRecords().get(jsonTableModel.getRecords().size() - 1).getSeqNo())
				{
					addFile(localStore, tableName, tirmTemp, jsonTableModel);
				}
			}
			
		}
		
		return;
		
	}
	
	public List<JsonTableModel> fetchData(String tableName) {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_DATA_SOURCE))
		{
			if(!store.getString(PreferenceConstants.SD_DATA_SOURCE).isEmpty())
			{
				return fetchData(store.getString(PreferenceConstants.SD_DATA_SOURCE), tableName, -1);
			}
		}
		
		return new ArrayList<JsonTableModel>();
	}
	
	public List<JsonTableModel> fetchData(String tableName, int lastSeqNo) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_DATA_SOURCE))
		{
			if(!store.getString(PreferenceConstants.SD_DATA_SOURCE).isEmpty())
			{
				return fetchData(store.getString(PreferenceConstants.SD_DATA_SOURCE), tableName, lastSeqNo);
			}
		}
		
		return new ArrayList<JsonTableModel>();
	}
	
	public List<JsonTableModel> fetchData(String dataSource, String tableName) {
		return fetchData(dataSource, tableName, -1);
	}
	
	public List<JsonTableModel> fetchData(String dataSource, String tableName, int lastSeqNo) {
		
		List<JsonTableModel> recordList = new ArrayList<JsonTableModel>();
		
		List<TableIndexRecordModel> tirmList = fetchIndex(dataSource, tableName);
		
		for(TableIndexRecordModel tirmTemp : tirmList)
		{
			if(tirmTemp.getLastSeqNo() > lastSeqNo)
			{
				System.out.println(tirmTemp.getName());
				JsonTableModel jtmTemp = fetchDataByFileName(dataSource, tableName, tirmTemp.getName());
				recordList.add(jtmTemp);
			}
		}
		
		return recordList;
	}
	
	private JsonTableModel fetchDataByFileName(String dataSource, String tableName, String fileName)
	{

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();  

        CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  

        HttpGet httpget = new HttpGet(dataSource + "/" + tableName + "/" + fileName);
        
        HttpResponse response;
        
        JsonTableModel data = new JsonTableModel();
        
        try {
			response = closeableHttpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			String recordString = EntityUtils.toString(entity, Charset.forName("UTF-8"));
			System.out.println(System.currentTimeMillis());
			data = JSON.parseObject(recordString, JsonTableModel.class);
			System.out.println(System.currentTimeMillis());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			
			try {

				closeableHttpClient.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return data;
	}
	
	public List<JsonTableModel> loadData(String tableName) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_LOCAL_STORE))
		{
			if(!store.getString(PreferenceConstants.SD_LOCAL_STORE).isEmpty())
			{
				return loadData(store.getString(PreferenceConstants.SD_LOCAL_STORE), tableName);
			}
		}
		
		return new ArrayList<JsonTableModel>();
	}

	public List<JsonTableModel> loadData(String localStore, String tableName) {
		return loadData(localStore, tableName, -1);
	}

	public List<JsonTableModel> loadData(String tableName, int lastSeqNo) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_LOCAL_STORE))
		{
			if(!store.getString(PreferenceConstants.SD_LOCAL_STORE).isEmpty())
			{
				return loadData(store.getString(PreferenceConstants.SD_LOCAL_STORE), tableName, lastSeqNo);
			}
		}
		
		return new ArrayList<JsonTableModel>();
	}

	public List<JsonTableModel> loadData(String localStore, String tableName, int lastSeqNo) {
		
		List<JsonTableModel> recordList = new ArrayList<JsonTableModel>();
		
		List<TableIndexRecordModel> tirmList = loadIndex(localStore, tableName);
		
		for(TableIndexRecordModel tirmTemp : tirmList)
		{
			if(tirmTemp.getLastSeqNo() > lastSeqNo)
			{
				System.out.println(tirmTemp.getName());
				JsonTableModel jtmTemp = loadDataByFileName(localStore, tableName, tirmTemp.getName());
				if(null != jtmTemp)
				{
					recordList.add(jtmTemp);
				}
			}
		}
		
		return recordList;
	}
	
	private JsonTableModel loadDataByFileName(String localStore, String tableName, String fileName)
	{

		String filePath = localStore + "/" + tableName + "/" + fileName;
		
		File localFile = new File(filePath);
		
		if(!localFile.exists())
		{
			return null;
		}
		
        JsonTableModel data = null;
        
        JSONReader reader = null;
		try{
			reader = new JSONReader(new FileReader(filePath));

			data = reader.readObject(JsonTableModel.class);

		}catch(JSONException e)
		{
			//logger.error("read :" + tableName + "failure!", e);
			if(null != reader)
			{
				reader.close();
			}
		}catch(IOException ioException)
		{
			//logger.error("read :" + tableName + "failure!", e);
			if(null != reader)
			{
				reader.close();
			}
		}
		
		return data;
	}
	
	public List<TableIndexRecordModel> loadIndex(String tableName) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_LOCAL_STORE))
		{
			if(!store.getString(PreferenceConstants.SD_LOCAL_STORE).isEmpty())
			{
				return loadIndex(store.getString(PreferenceConstants.SD_LOCAL_STORE), tableName);
			}
		}
		
		return new ArrayList<TableIndexRecordModel>();
	}

	public List<TableIndexRecordModel> loadIndex(String localStore, String tableName) {
		
		List<TableIndexRecordModel> tirmList = new ArrayList<TableIndexRecordModel>();
		
		String filePath = "";
		String dirPath = "";

		filePath = localStore + "/" + tableName + "/" + "index.json";
		dirPath = localStore + "/" + tableName + "/";

		File tableIndex = new File(filePath);
		File tableDir = new File(dirPath);
		
		if(!tableDir.exists())
		{
			return tirmList;
		}
		
		if(!tableIndex.exists())
		{	
			return tirmList;
		}

		JSONReader reader = null;
		try{
			reader = new JSONReader(new FileReader(tableIndex));
			reader.startArray();
			while(reader.hasNext()) {
				TableIndexRecordModel tirmTemp = reader.readObject(TableIndexRecordModel.class);
				tirmList.add(tirmTemp);
			}
			reader.endArray();
		}catch(JSONException e)
		{
			//logger.error("read :" + tableName + "failure!", e);
			if(null != reader)
			{
				reader.close();
			}
		}catch(IOException ioException)
		{
			//logger.error("read :" + tableName + "failure!", e);
			if(null != reader)
			{
				reader.close();
			}
		}
		
		if(null != reader)
		{
			reader.close();
		}
		
		return tirmList;
	}

	public boolean checkLocalData(String tableName) {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(store.contains(PreferenceConstants.SD_LOCAL_STORE))
		{
			if(!store.getString(PreferenceConstants.SD_LOCAL_STORE).isEmpty())
			{
				return checkLocalData(store.getString(PreferenceConstants.SD_LOCAL_STORE), tableName);
			}
		}
		
		return false;
	}

	public boolean checkLocalData(String localStore, String tableName) {
		
		List<TableIndexRecordModel> tirmList = loadIndex(localStore, tableName);
		
		int lastSeqNo = -1;
		
		for(TableIndexRecordModel tirmTemp : tirmList)
		{
			if(tirmTemp.getFirstSeqNo() == tirmTemp.getLastSeqNo())
			{
				continue;
			}
			
			if((tirmTemp.getFirstSeqNo() - lastSeqNo) != 1)
			{
				return false;
			}
			
			lastSeqNo = tirmTemp.getLastSeqNo();
		}
		
		return true;
	}

	private int addFile(String localStore, String tableName, TableIndexRecordModel newTirm, JsonTableModel jtm)
	{
		String filePath = "";
		String dirPath = "";
		
		filePath = localStore + "/" + tableName + "/" + "index.json";
		dirPath = localStore + "/" + tableName + "/";
		
		File tableIndex = new File(filePath);
		
		int lastSeqNo = -1;
		
		List<TableIndexRecordModel> tirmList = new ArrayList<TableIndexRecordModel>();
		
		TableIndexRecordModel lastTirm = new TableIndexRecordModel();
		
		lastTirm.setLastSeqNo(-1);
		
		try {
			
			File tableDir = new File(dirPath);
			if(!tableDir.exists())
			{
				tableDir.mkdirs();
			}
			
			if(!tableIndex.exists())
			{	
				tableIndex.createNewFile();
				FileWriter fileWriter = new FileWriter(tableIndex);
				fileWriter.append("[]");
				fileWriter.flush();
				fileWriter.close();
			}
			
			JSONReader reader = null;
			try{
				reader = new JSONReader(new FileReader(tableIndex));
				reader.startArray();
				while(reader.hasNext()) {
					lastTirm = reader.readObject(TableIndexRecordModel.class);
					tirmList.add(lastTirm);
				}
				reader.endArray();
			}catch(JSONException e)
			{
				//logger.error("read :" + tableName + "failure!", e);
				if(null != reader)
				{
					reader.close();
				}
			}
			if(null != reader)
			{
				reader.close();
			}

			String fileName = newTirm.getName();
			//Start Write Json Table Model File
			String jtmFileName = dirPath + fileName;
			File newjtmFile = new File(jtmFileName);
			newjtmFile.createNewFile();
			JSONWriter jtmWriter = new JSONWriter(new FileWriter(newjtmFile));
			//jtmWriter.startObject();
			jtmWriter.writeValue(jtm);
			//jtmWriter.endObject();
			jtmWriter.close();
			
			
			tirmList.add(newTirm);
			JSONWriter writer = new JSONWriter(new FileWriter(filePath));
			writer.startArray();
			for (TableIndexRecordModel tirmTemp : tirmList) {
				writer.writeValue(tirmTemp);
			}
			writer.endArray();
			writer.close();
			
			return 0;
		}catch (Exception e) {
			//logger.error("write json file fail!", e);
			return -1;
		}

	}

	public void syncData(String localStore, String dataSource, String tableName )
	
	
}
