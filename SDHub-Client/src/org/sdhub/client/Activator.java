package org.sdhub.client;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.sdhub.client.interfaces.IDataFetcher;
import org.sdhub.client.interfaces.IDataPorter;
import org.sdhub.client.trackers.FetcherPluginTracker;
import org.sdhub.client.trackers.PorterPluginTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "SDHub-Client"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public static BundleContext context;
	
	private ServiceTracker porterPluginTracker = null;
	
	private ServiceTracker fetcherPluginTracker = null;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		//init porter plugin tracker
		porterPluginTracker = new ServiceTracker(context, IDataPorter.class.getName(), new PorterPluginTracker(context));
		porterPluginTracker.open();
		
		//init fetcher plugin tracker
		fetcherPluginTracker = new ServiceTracker(context, IDataFetcher.class.getName(), new FetcherPluginTracker(context));
		fetcherPluginTracker.open();
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		
		porterPluginTracker.close();
		fetcherPluginTracker.close();
		
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
