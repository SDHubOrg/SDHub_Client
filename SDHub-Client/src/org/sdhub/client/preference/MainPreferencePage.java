package org.sdhub.client.preference;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.sdhub.client.Activator;
import org.sdhub.client.trackers.FetcherPlugins;
import org.sdhub.client.trackers.PorterPlugins;
import org.eclipse.ui.IWorkbench;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class MainPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public MainPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Main Configuration Page");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {

		String fetchers[][] = new String[FetcherPlugins.dataFetchersHashMap.size()][2];
		int index = 0;
		for(String fetcherName : FetcherPlugins.dataFetchersHashMap.keySet())
		{
			fetchers[index][0] = fetcherName;
			fetchers[index][1] = FetcherPlugins.dataFetchersHashMap.get(fetcherName).getName();
			index++;
		}
		
		String porters[][] = new String[PorterPlugins.dataPortersHashMap.size()][2];
		index = 0;
		for(String porterName : PorterPlugins.dataPortersHashMap.keySet())
		{
			porters[index][0] = porterName;
			porters[index][1] = PorterPlugins.dataPortersHashMap.get(porterName).getName();
			index++;
		}
		
		addField(new ComboFieldEditor(PreferenceConstants.FETCHER_NAME, "Select Fetcher Plugin", fetchers, getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.PORTER_NAME, "Select Porter Plugin", porters, getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}