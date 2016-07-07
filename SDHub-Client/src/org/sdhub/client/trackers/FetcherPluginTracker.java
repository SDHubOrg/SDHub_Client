package org.sdhub.client.trackers;

import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.sdhub.client.interfaces.IDataFetcher;
import org.sdhub.client.interfaces.IDataPorter;

public class FetcherPluginTracker implements ServiceTrackerCustomizer {

	private BundleContext bc;

	public FetcherPluginTracker(BundleContext bc)
	{
		this.bc = bc;
	}
	
	@Override
	public Object addingService(ServiceReference reference) {
		
		IDataFetcher dataFetcher = (IDataFetcher) bc.getService(reference);

		String dataFetcherName = dataFetcher.getName();
		//System.out.print("Adding Plugin: ");
		//System.out.println(dataFetcherName);

		if (FetcherPlugins.dataFetchersHashMap.isEmpty()) {
			//PlatformUI.getPreferenceStore().setValue("FetcherPluginName", dataFetcherName);
		}

		FetcherPlugins.dataFetchersHashMap.put(dataFetcherName, dataFetcher);
		return dataFetcher;
	}

	@Override
	public void modifiedService(ServiceReference reference, Object service) {
		IDataFetcher dataFetcher = (IDataFetcher) bc.getService(reference);

		String dataFetcherName = dataFetcher.getName();
		//System.out.print("Modify Plugin: ");
		//System.out.println(dataFetcherName);
		
		FetcherPlugins.dataFetchersHashMap.put(dataFetcherName, dataFetcher);
		return;
		
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		IDataFetcher dataFetcher = (IDataFetcher) bc.getService(reference);

		String dataFetcherName = dataFetcher.getName();
		//System.out.print("Remove Plugin: ");
		//System.out.println(dataFetcherName);
		
		if (FetcherPlugins.dataFetchersHashMap.containsKey(dataFetcherName)) {
			FetcherPlugins.dataFetchersHashMap.remove(dataFetcherName);
		}
	}

}
