/*
* Copyright (c) 2006-2010 Nokia Corporation and/or its subsidiary(-ies). 
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description:
*
*/

package com.nokia.tools.carbide.ui.productsupport.actions;

import java.util.Properties;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

public class NewProjectIntroAction extends NewProjectAction implements IIntroAction {
	
	public void run(IIntroSite site, Properties params) {
		 IWorkbench workbench = PlatformUI.getWorkbench();
		 IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		 IWorkbenchPage page = window.getActivePage();
		 IViewReference[] viewRefs = page.getViewReferences();	
		 for(int i=0;i<viewRefs.length;i++){		 
			 if (viewRefs[i].getId().equals("org.eclipse.ui.internal.introview")) {
		        IViewPart part = page.findView("org.eclipse.ui.internal.introview");
		        page.hideView(part);	       
		 }
		}
		super.run();		
	}

}
