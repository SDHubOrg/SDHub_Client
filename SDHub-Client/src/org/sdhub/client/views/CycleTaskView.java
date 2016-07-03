package org.sdhub.client.views;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.part.ViewPart;
import org.sdhub.client.controller.SyncController;
import org.sdhub.client.model.ScheduleModel;
import org.sdhub.client.Activator;
import org.sdhub.client.trackers.FetcherPluginTracker;
import org.sdhub.client.trackers.FetcherPlugins;
import org.sdhub.client.trackers.PorterPlugins;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wb.swt.ResourceManager;

import org.slf4j.*;

public class CycleTaskView extends ViewPart {

	public static final String ID = "org.sdhub.client.views.CycleTaskView"; //$NON-NLS-1$
	
	public static Display display;
	
	private Table table;

	private Button startButton;
	private Button stopButton;

	private Button refreshButton;
	private TableColumn tblclmnTableName;
	
	private TableColumn tblclmnSchedule;
	private TableColumn tblclmnStatus;
	
	public CycleTaskView() {
	}

	/**
	 * Create contents of the view part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		display = parent.getDisplay();
		
		Composite container = new Composite(parent, SWT.BORDER);
		container.setLayout(new GridLayout(3, false));

		{
			startButton = new Button(container, SWT.NONE);
			startButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			startButton.setText("Start");
			startButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					startTask();
					triggerButtonStat();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}
		{
			stopButton = new Button(container, SWT.NONE);
			stopButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			stopButton.setText("Stop");
			stopButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					stopTask();
					triggerButtonStat();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		{
			refreshButton = new Button(container, SWT.NONE);
			refreshButton.setImage(ResourceManager.getPluginImage("SDHub-Client", "icons/alt_window_16.gif"));
			refreshButton.setToolTipText("Download Schedule From Data Source Site");
			refreshButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
			refreshButton.setText("Refresh");
			refreshButton.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					CycleTaskView.this.refreshSchedule();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					
					
				}
			});
		}
		
		{
			table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.VIRTUAL);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);
			{
				tblclmnTableName = new TableColumn(table, SWT.NONE);
				tblclmnTableName.setWidth(130);
				tblclmnTableName.setText("TableName");
			}
			{
				tblclmnSchedule = new TableColumn(table, SWT.NONE);
				tblclmnSchedule.setWidth(70);
				tblclmnSchedule.setText("Schedule");
			}
			{
				tblclmnStatus = new TableColumn(table, SWT.NONE);
				tblclmnStatus.setWidth(80);
				tblclmnStatus.setText("Status");
			}
			table.setItemCount(500);
			table.addListener(SWT.SetData, new Listener(){
		        public void handleEvent(Event event) {
		            TableItem item = (TableItem)event.item;
		            int index = event.index;
		            if(index < SyncController.ScheduleList.size())
		            {
		                ScheduleModel smItem = SyncController.ScheduleList.get(index);
		                String timeString = String.format("%02d", smItem.getHour()) + ":" + String.format("%02d", smItem.getMinute());
		                String isEnable = "";
		                if(smItem.isEnable() == true)
		                {
		                	isEnable = "Enable";
		                }else{
		                	isEnable = "Disable";
		                }
		                item.setText(new String[] {smItem.getTableName(), timeString, isEnable});
		                
		                if(smItem.getStatus() == ScheduleModel.PROCESSING)
		                {
		                	item.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
		                }
		                
		                if(smItem.getStatus() == ScheduleModel.UNHANDLED)
		                {
		                	item.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		                }

		            }
		        }
		    });
			
			table.addListener(SWT.MouseDown,  new Listener() {
				
            	private Table tableTemp;
            	private TableItem tableItem;
				
            	@Override
                public void handleEvent(Event event) {

                	if(event.button != 3)
                	{
                		return;
                	}
                	
                	tableTemp = (Table)event.widget;

                	tableItem = tableTemp.getItem(new Point(event.x, event.y));
                	
                	if(null == tableItem)
                	{
                		return;
                	}
                	
                	if(tableItem.getText(0).isEmpty())
                	{
                		return;
                	}
                	
                    Menu menu = new Menu(table);  
                    table.setMenu(menu);  
                    MenuItem menuItem1 = new MenuItem(menu, SWT.POP_UP);  
                    menuItem1.setText("Update");
                    
                    menuItem1.addListener(SWT.Selection, new Listener() {  
                        public void handleEvent(Event event) {
                        	SyncController.sync(tableItem.getText(0));
                        }
                    }); 
                    

                    MenuItem menuItem2 = new MenuItem(menu, SWT.POP_UP);  
                	if("Enable".equals(tableItem.getText(2).toString()))
                	{
                		menuItem2.setText("Disable");
                	}
                	if("Disable".equals(tableItem.getText(2).toString()))
                	{
                		menuItem2.setText("Enable");
                	}
                    menuItem2.addListener(SWT.Selection, new Listener() {  
                        public void handleEvent(Event event) {
                        	System.out.println("menuItem2.addListener");
                        	IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                           	if("Enable".equals(tableItem.getText(2).toString()))
                        	{
                           		store.setValue(tableItem.getText(0).toString(), false);
                           		
            		            for(ScheduleModel smTemp1 : SyncController.ScheduleList)
            		            {
            		            	if(smTemp1.getTableName().equals(tableItem.getText(0).toString()))
            		            	{
            		                	smTemp1.setEnable(false);
            		            	}
            		            }
            		            CycleTaskView.this.display.asyncExec(new Runnable() {
            		                @Override
            		                public void run() {
            		                	if(null != CycleTaskView.this)
            		                	{
            		                		if(null != tableItem){
            		                			tableItem.setText(2, "Disable");
            		                		}
            		                	}
            		                }
            		            });
                           		
                        	}
                           	
                        	if("Disable".equals(tableItem.getText(2).toString()))
                        	{
                        		store.setValue(tableItem.getText(0).toString(), true);
                        		
            		            for(ScheduleModel smTemp1 : SyncController.ScheduleList)
            		            {
            		            	if(smTemp1.getTableName().equals(tableItem.getText(0).toString()))
            		            	{
            		                	smTemp1.setEnable(true);
            		            	}
            		            }
            		            CycleTaskView.this.display.asyncExec(new Runnable() {
            		                @Override
            		                public void run() {
            		                	if(null != CycleTaskView.this)
            		                	{
            		                		if(null != tableItem){
            		                			tableItem.setText(2, "Enable");
            		                		}
            		                	}
            		                }
            		            });
                        		
                        	}
                        }
                    }); 
                }  
            });

			
		}

		Timer viewRefreshTimer = new Timer();
		
		viewRefreshTimer.schedule(new RefreshStatusTask(), 0, 5 * 1000);

		createActions();
		initializeToolBar();
		initializeMenu();
		refreshSchedule();
	}

	private void refreshSchedule()
	{
		SyncController.init();
		refreshTaskTable();
	}
	private void startTask()
	{
		SyncController.start();
	}
	
	private void stopTask()
	{
		SyncController.stop();
	}

	
	private void triggerButtonStat()
	{
		if(SyncController.getStatus() == SyncController.RUNNING)
		{
			this.startButton.setEnabled(false);
			this.stopButton.setEnabled(true);
		}else{
			this.startButton.setEnabled(true);
			this.stopButton.setEnabled(false);
		}
	}
	
	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	public void refreshTaskTable()
	{
		if(null == table)
		{
			return;
		}
		
		tidyTable();
		table.clearAll();
	}
	
	public void tidyTable()
	{
		if(null != table)
		{
			int width = table.getSize().x;
			tblclmnTableName.setWidth(width / 2 - 1);
			tblclmnSchedule.setWidth(width / 4 - 1);
			tblclmnStatus.setWidth(width / 4 - 1);
		}
	}
	
	
	public class RefreshStatusTask extends TimerTask
	{

		public RefreshStatusTask() {
			
		}
		
		@Override
		public void run() {
			if(null == CycleTaskView.this)
			{
				return;
			}
			if(null == CycleTaskView.this.display)
			{
				return;
			}
			CycleTaskView.this.display.asyncExec(new Runnable() {
                @Override
                public void run() {
                	if(null != CycleTaskView.this)
                	{
                		CycleTaskView.this.refreshTaskTable();
                	}
                }
            });
			
			
		}
		
	}
}
