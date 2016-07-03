package org.sdhub.client.trackers;

import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.sdhub.client.interfaces.IDataPorter;

public class PorterPluginTracker implements ServiceTrackerCustomizer {

	private BundleContext bc;

	public PorterPluginTracker(BundleContext bc)
	{
		this.bc = bc;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		IDataPorter dataPorter = (IDataPorter) bc.getService(reference);

		String dataPorterName = dataPorter.getName();
		System.out.print("Adding Plugin: ");
		System.out.println(dataPorterName);

		if (PorterPlugins.dataPortersHashMap.isEmpty()) {
			//PlatformUI.getPreferenceStore().setValue("PorterPluginName", dataPorterName);
		}

		PorterPlugins.dataPortersHashMap.put(dataPorterName, dataPorter);
		return dataPorter;
	}

	@Override
	public void modifiedService(ServiceReference reference, Object arg1) {
		IDataPorter dataPorter = (IDataPorter) bc.getService(reference);
		String dataPorterName = dataPorter.getName();
		
		System.out.print("Modified Plugin");
		System.out.println(dataPorterName);
		
		PorterPlugins.dataPortersHashMap.put(dataPorterName, dataPorter);
		return;

	}

	@Override
	public void removedService(ServiceReference reference, Object arg1) {
		IDataPorter dataPorter = (IDataPorter) bc.getService(reference);
		String dataPorterName = dataPorter.getName();
		
		System.out.println("removedService");
		System.out.println(dataPorterName);
		
		if (PorterPlugins.dataPortersHashMap.containsKey(dataPorterName)) {
			PorterPlugins.dataPortersHashMap.remove(dataPorterName);
		}

	}

}
