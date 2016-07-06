package org.sdhub.client.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.sdhub.client.views.CycleTaskView;

public class MainPerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {

		//String editorArea = layout.getEditorArea();
		//addFastViews(layout);
		//addViewShortcuts(layout);
		//addPerspectiveShortcuts(layout);
		layout.setEditorAreaVisible(false);
		//layout.setFixed(true);
		//layout.addView(CycleTaskView.ID, IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		//layout.addView("org.sdhub.client.views.ResourceListView", IPageLayout.RIGHT, 0.19f, CycleTaskView.ID);
		//layout.addView("org.sdhub.client.views.WebView", IPageLayout.RIGHT, 0.62f, "org.sdhub.client.views.ResourceListView");
		//layout.addView("org.sdhub.client.views.LogView", IPageLayout.BOTTOM, 0.74f, "org.sdhub.client.views.ResourceListView");
		//layout.getViewLayout(CycleTaskView.ID).setCloseable(false);
		//layout.getViewLayout(CycleTaskView.ID).setMoveable(false);
		
/*		layout.addView("org.sdhub.client.views.CycleTaskView", IPageLayout.LEFT, 0.5f, IPageLayout.ID_EDITOR_AREA);
		layout.addView("org.sdhub.client.views.ResourceListView", IPageLayout.RIGHT, 0.39f, "org.sdhub.client.views.CycleTaskView");
		layout.addView("org.sdhub.client.views.WebView", IPageLayout.RIGHT, 0.49f, "org.sdhub.client.views.ResourceListView");
		layout.addView("org.sdhub.client.views.LogView", IPageLayout.BOTTOM, 0.5f, "org.sdhub.client.views.ResourceListView");*/
	}


}
