package org.sdhub.client.controller;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.core.SchedulingContext;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.sdhub.client.Activator;
import org.sdhub.client.interfaces.IDataFetcher;
import org.sdhub.client.interfaces.IDataPorter;
import org.sdhub.client.model.JsonTableModel;
import org.sdhub.client.model.ScheduleModel;
import org.sdhub.client.model.TableIndexRecordModel;
import org.sdhub.client.preference.PreferenceConstants;
import org.sdhub.client.trackers.FetcherPlugins;
import org.sdhub.client.trackers.PorterPlugins;
import org.sdhub.client.util.LogUtil;

public class SyncJob implements Job, JobStore, JobListener{

	private String tableName;
	private IDataFetcher dataFetcher;
	private IDataPorter dataPorter;
	
	public SyncJob(){
		
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		ScheduleModel sm = null;

		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		{
			this.tableName = (String)context.getJobDetail().getJobDataMap().get("TableName");

			LogUtil.showLogWithTime("Start syncro table : " + this.tableName);
			
			String fetcherName = (String)context.getJobDetail().getJobDataMap().get("FetcherName");
			String porterName = (String)context.getJobDetail().getJobDataMap().get("PorterName");

			this.dataFetcher = FetcherPlugins.dataFetchersHashMap.get(fetcherName);
			this.dataPorter = PorterPlugins.dataPortersHashMap.get(porterName);
		}
		
		if(store.contains(this.tableName))
		{
			if(store.getBoolean(this.tableName) == false)
			{
				return;
			}
		}
		
		for(ScheduleModel scheduleModel : SyncController.ScheduleList)
		{
			if(scheduleModel.getTableName().endsWith(this.tableName))
			{
				sm = scheduleModel;
				break;
			}
		}
		
		sm.setStatus(ScheduleModel.PROCESSING);
		
		if(null == this.dataFetcher)
		{
			sm.setStatus(ScheduleModel.UNHANDLED);
			LogUtil.showLogWithTime("WARN no useable Data Fetcher Plugin for " + this.tableName);
			return;
		}
		
		if(null == this.dataPorter)
		{
			sm.setStatus(ScheduleModel.UNHANDLED);
			LogUtil.showLogWithTime("WARN no useable Data Porter Plugin for " + this.tableName);
			return;
		}

		List<TableIndexRecordModel> tableIndexRecordList = dataFetcher.fetchIndex(tableName);
		if(null == tableIndexRecordList)
		{
			LogUtil.showLogWithTime("ERROR can not get index.json of " + this.tableName);
			sm.setStatus(ScheduleModel.UNHANDLED);
			return;
		}

		if(tableIndexRecordList.isEmpty())
		{
			LogUtil.showLogWithTime("ERROR index.json of " + this.tableName + " is Empty");
			sm.setStatus(ScheduleModel.UNHANDLED);
			return;
		}

		int lastSeqNoInDB = dataPorter.getLastSeqNo(tableName);
		
		dataFetcher.fetchUpdateData(tableName);
		List<JsonTableModel> dataList = dataFetcher.loadData(tableName, lastSeqNoInDB);
		if(dataList.isEmpty())
		{
			LogUtil.showLogWithTime("No updates for " + this.tableName);
			sm.setStatus(ScheduleModel.UNHANDLED);
			return;
		}

		for(JsonTableModel jsonTableModel : dataList)
		{
			dataPorter.initTable(jsonTableModel);
			dataPorter.processTableModel(jsonTableModel);
		}
		
		sm.setStatus(ScheduleModel.UNHANDLED);
	}


	@Override
	public Trigger acquireNextTrigger(SchedulingContext arg0, long arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String[] getCalendarNames(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public long getEstimatedTimeToReleaseAndAcquireTrigger() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public String[] getJobGroupNames(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String[] getJobNames(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getNumberOfCalendars(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getNumberOfJobs(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getNumberOfTriggers(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Set getPausedTriggerGroups(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String[] getTriggerGroupNames(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String[] getTriggerNames(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int getTriggerState(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public Trigger[] getTriggersForJob(SchedulingContext arg0, String arg1, String arg2)
			throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void initialize(ClassLoadHelper arg0, SchedulerSignaler arg1) throws SchedulerConfigException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isClustered() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void pauseAll(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pauseJob(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pauseJobGroup(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pauseTrigger(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void pauseTriggerGroup(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void releaseAcquiredTrigger(SchedulingContext arg0, Trigger arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean removeCalendar(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean removeJob(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean removeTrigger(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean replaceTrigger(SchedulingContext arg0, String arg1, String arg2, Trigger arg3)
			throws JobPersistenceException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void resumeAll(SchedulingContext arg0) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resumeJob(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resumeJobGroup(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resumeTrigger(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void resumeTriggerGroup(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public Calendar retrieveCalendar(SchedulingContext arg0, String arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JobDetail retrieveJob(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Trigger retrieveTrigger(SchedulingContext arg0, String arg1, String arg2) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void schedulerStarted() throws SchedulerException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setInstanceId(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setInstanceName(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void storeCalendar(SchedulingContext arg0, String arg1, Calendar arg2, boolean arg3, boolean arg4)
			throws ObjectAlreadyExistsException, JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void storeJob(SchedulingContext arg0, JobDetail arg1, boolean arg2)
			throws ObjectAlreadyExistsException, JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void storeJobAndTrigger(SchedulingContext arg0, JobDetail arg1, Trigger arg2)
			throws ObjectAlreadyExistsException, JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void storeTrigger(SchedulingContext arg0, Trigger arg1, boolean arg2)
			throws ObjectAlreadyExistsException, JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean supportsPersistence() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public TriggerFiredBundle triggerFired(SchedulingContext arg0, Trigger arg1) throws JobPersistenceException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void triggeredJobComplete(SchedulingContext arg0, Trigger arg1, JobDetail arg2, int arg3)
			throws JobPersistenceException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void jobExecutionVetoed(JobExecutionContext arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		
		String tableName = context.getJobDetail().getName();
		
		SyncController.ScheduleMap.get(tableName).setStatus(ScheduleModel.PROCESSING);
		
		for(ScheduleModel smTemp : SyncController.ScheduleList)
		{
			if(smTemp.getTableName().equals(tableName))
			{
				smTemp.setStatus(ScheduleModel.PROCESSING);
				break;
			}
		}
	}


	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		
		String tableName = context.getJobDetail().getName();
		
		SyncController.ScheduleMap.get(tableName).setStatus(ScheduleModel.UNHANDLED);
		
		for(ScheduleModel smTemp : SyncController.ScheduleList)
		{
			if(smTemp.getTableName().equals(tableName))
			{
				smTemp.setStatus(ScheduleModel.UNHANDLED);
				break;
			}
		}
		
	}

}
