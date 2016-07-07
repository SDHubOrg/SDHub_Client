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
import org.sdhub.client.util.LogUtil;

public  class  SyncController {

	public final static int RUNNING = 1;
	public final static int STOPED = 2;
	public final static int SUSPEND = 3;
	
	private static Scheduler quartzScheduler;
	
	private static Scheduler temporaryScheduler;
	
	public static HashMap<String, ScheduleModel> ScheduleMap = new HashMap<String, ScheduleModel>();
	
	public static List<ScheduleModel> ScheduleList = new ArrayList<ScheduleModel>();
	
	public static synchronized int getStatus()
	{
		try {
			
			if(null == quartzScheduler)
			{
				return STOPED;
			}
			
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
			
			store.setDefault(mirmTemp.getTableName(), true);
			
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
		
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
		try {
			temporaryScheduler = schedulerFactory.getScheduler();
			temporaryScheduler.start();
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized void start()
	{
		
		LogUtil.showLogWithTime("Start Scheduler ...");
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
		LogUtil.showLogWithTime("Stop Scheduler ...");
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
		LogUtil.showLogWithTime("Restart Scheduler ...");
		stop();
		start();
	}
	
	public static void sync(String tableName)
	{
		LogUtil.showLogWithTime("Manually Update : " + tableName);
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		JobDetail jobDetailTemp = new JobDetail(tableName, null, SyncJob.class);
		
		jobDetailTemp.setName(tableName);
		jobDetailTemp.getJobDataMap().put("TableName", tableName);
		jobDetailTemp.getJobDataMap().put("FetcherName", store.getString(PreferenceConstants.FETCHER_NAME));
		jobDetailTemp.getJobDataMap().put("PorterName", store.getString(PreferenceConstants.PORTER_NAME));

		Trigger triggerTemp = TriggerUtils.makeImmediateTrigger(tableName + "_temp",0, 1);
		
		try {
			temporaryScheduler.scheduleJob(jobDetailTemp, triggerTemp);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
