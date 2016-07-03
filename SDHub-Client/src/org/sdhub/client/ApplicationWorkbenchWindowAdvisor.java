package org.sdhub.client;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();

		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setTitle("SDHub Client");
		//configurer.setShowPerspectiveBar(true);
		//configurer.setShellStyle(SWT.MIN | SWT.CLOSE | SWT.PRIMARY_MODAL);
		//BORDER, CLOSE, MIN, MAX, NO_TRIM, RESIZE, TITLE, ON_TOP, TOOL, SHEET 
		//APPLICATION_MODAL, MODELESS, PRIMARY_MODAL, SYSTEM_MODAL 
	}
	
	public void postWindowOpen() {
		super.postWindowOpen();
		this.getWindowConfigurer().getWindow().getShell().setMaximized(true);

	}


}
