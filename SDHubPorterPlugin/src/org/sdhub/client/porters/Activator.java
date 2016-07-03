package org.sdhub.client.porters;

import java.util.Dictionary;
import java.util.Properties;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.sdhub.client.interfaces.IDataPorter;
import org.sdhub.client.porters.impl.SDHubPorter;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.sdhub.client.sdhubPorter"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public static BundleContext context=null;
	
	public static SDHubPorter sdhubPorter;
	
	private ServiceRegistration serviceRegistration=null;
	
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
		System.out.println("SDHub Porter Plugin Start");
		
        this.context=context;
        sdhubPorter = new SDHubPorter();

        Dictionary properties = new Properties();
        properties.put("PorterName", "SDHubPorter");

        serviceRegistration=this.context.registerService(IDataPorter.class.getName(), sdhubPorter, properties);  
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		serviceRegistration.unregister();
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