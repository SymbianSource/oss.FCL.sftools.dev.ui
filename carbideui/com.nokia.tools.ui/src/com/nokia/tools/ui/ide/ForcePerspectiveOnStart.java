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

package com.nokia.tools.ui.ide;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.nokia.tools.ui.Activator;

/**
 * When in other product mode performs non political forcing of mobile
 * perspective to be default
 * 
 */
public abstract class ForcePerspectiveOnStart
    implements IStartup {

	protected abstract String getPerspectiveId();

	protected abstract PerspectiveHackManager getPerspectiveHackManager();

	protected void openPerspective() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {

					PlatformUI.getWorkbench().showPerspective(
					    getPerspectiveId(),
					    PlatformUI.getWorkbench().getActiveWorkbenchWindow()); //$NON-NLS-1$
				} catch (WorkbenchException e) {
					Activator.error(e);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public synchronized void earlyStartup() {
		String perspectiveId = getPerspectiveId();
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
		    && PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		        .getActivePage() != null
		    && !PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		        .getActivePage().getPerspective().getId().startsWith(
		            perspectiveId)) {
			openPerspective();
		}

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				getPerspectiveHackManager().getMenuCustomizer()
				    .initializeMenus();
				getPerspectiveHackManager().initializePerspective();
			}
		});
	}
}
