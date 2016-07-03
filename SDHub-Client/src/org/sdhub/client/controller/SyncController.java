package org.sdhub.client.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jface.preference.IPreferenceStore;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.sdhub.client.Activator;
import org.sdhub.client.interfaces.IDataFetcher;
import org.sdhub.client.interfaces.IDataPorter;
import org.sdhub.client.model.JsonTableModel;
import org.sdhub.client.model.MainIndexRecordModel;
import org.sdhub.client.model.ScheduleModel;
import org.sdhub.client.model.TableIndexRecordModel;
import org.sdhub.client.preference.PreferenceConstants;
import org.sdhub.client.trackers.FetcherPlugins;
import org.sdhub.client.trackers.PorterPlugins;

public  class  SyncController {

	public final static int RUNNING = 1;
	public final static int STOPED = 2;
	public final static int SUSPEND = 3;
	
	private static Scheduler quartzScheduler;
	
	public static HashMap<String, ScheduleModel> ScheduleMap = new HashMap<String, ScheduleModel>();
	
	public static List<ScheduleModel> ScheduleList = new ArrayList<ScheduleModel>();
	
	public static synchronized int getStatus()
	{
		try {
			if(quartzScheduler.isShutdown())
			{
				return STOPED;
			}
			if(quartzScheduler.isInStandbyMode())
			{
				return SUSPEND;
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return RUNNING;
	}
	
	public static synchronized void init()
	{
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(!store.contains(PreferenceConstants.FETCHER_NAME))
		{
			return;
		}
		
		if(!store.contains(PreferenceConstants.PORTER_NAME))
		{
			return;
		}
		
		IDataFetcher dataFetcher = FetcherPlugins.dataFetchersHashMap.get(store.getString(PreferenceConstants.FETCHER_NAME));
		IDataPorter dataPorter = PorterPlugins.dataPortersHashMap.get(store.getString(PreferenceConstants.PORTER_NAME));
		
		if(null == dataFetcher)
		{
			return;
		}
		
		if(null == dataPorter)
		{
			return;
		}
		
		List<MainIndexRecordModel> mainIndexRecordList = dataFetcher.fetchSchedule();
		
		ScheduleMap.clear();
		
		for(MainIndexRecordModel mirmTemp : mainIndexRecordList)
		{
			ScheduleModel smTemp = new ScheduleModel();
			smTemp.setTableName(mirmTemp.getTableName());
			smTemp.setStatus(ScheduleModel.UNHANDLED);
			int hour = 0;
			int minute = 0;
			
			String hourString = mirmTemp.getSchedule().substring(0, 2);
			String minuteString = mirmTemp.getSchedule().substring(3, 5);
			//System.out.println(hourString + minuteString);
			
			hour = Integer.valueOf(hourString);
			minute = Integer.valueOf(minuteString);
			
			smTemp.setHour(hour);
			smTemp.setMinute(minute);
			
			if(store.contains(smTemp.getTableName()))
			{
				smTemp.setEnable(store.getBoolean(smTemp.getTableName()));
			}else{
				smTemp.setEnable(true);
			}
			
			ScheduleMap.put(smTemp.getTableName(), smTemp);
			
		}
		ScheduleList.clear();
		ScheduleList.addAll(ScheduleMap.values());
	}
	
	public static synchronized void start()
	{
		try {

			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			
			if(null != quartzScheduler)
			{
				if(!quartzScheduler.isShutdown())
				{
					quartzScheduler.shutdown();
				}
			}
		
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			
			quartzScheduler = schedulerFactory.getScheduler();
			quartzScheduler.start();

			for(ScheduleModel smTemp : ScheduleList)
			{
				JobDetail jobDetailTemp = new JobDetail(smTemp.getTableName(), null, SyncJob.class);
				
				jobDetailTemp.setName(smTemp.getTableName());
				jobDetailTemp.getJobDataMap().put("TableName", smTemp.getTableName());
				jobDetailTemp.getJobDataMap().put("FetcherName", store.getString(PreferenceConstants.FETCHER_NAME));
				jobDetailTemp.getJobDataMap().put("PorterName", store.getString(PreferenceConstants.PORTER_NAME));
				
				Trigger triggerTemp = TriggerUtils.makeDailyTrigger(smTemp.getTableName(), smTemp.getHour(), smTemp.getMinute());

				quartzScheduler.scheduleJob(jobDetailTemp, triggerTemp);
				System.out.println("job added in : " + smTemp.getTableName() + " time: " + smTemp.getHour() + smTemp.getMinute());
				
			}
			
			quartzScheduler.start();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void stop()
	{
		try{
			if(null != quartzScheduler)
			{
				if(!quartzScheduler.isShutdown())
				{
					quartzScheduler.shutdown();
				}
			}
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void restart()
	{
		stop();
		start();
	}
	
	public static void sync(String tableName)
	{
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		if(!store.contains(PreferenceConstants.FETCHER_NAME))
		{
			return;
		}
		
		if(!store.contains(PreferenceConstants.PORTER_NAME))
		{
			return;
		}
		
		IDataFetcher dataFetcher = FetcherPlugins.dataFetchersHashMap.get(store.getString(PreferenceConstants.FETCHER_NAME));
		IDataPorter dataPorter = PorterPlugins.dataPortersHashMap.get(store.getString(PreferenceConstants.PORTER_NAME));
		
		if(null == dataFetcher)
		{
			return;
		}
		
		if(null == dataPorter)
		{
			return;
		}
		
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
		
		dataFetcher.fetchUpdateData(tableName);
		
		List<JsonTableModel> dataList = dataFetcher.loadData(tableName, lastSeqNoInDB);
		
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
