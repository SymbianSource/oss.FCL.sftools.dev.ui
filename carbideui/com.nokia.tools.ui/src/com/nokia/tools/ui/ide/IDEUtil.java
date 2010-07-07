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

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class IDEUtil {
	private IDEUtil() {
	}

	/**
	 * During startup IWorkbench.getActiveWorkbenchWindow returns null and then
	 * method returns any - first workbenchwindow The main reason for this is to
	 * get safe Shell to display
	 * 
	 * @return IWorkbench.getActiveWorkbenchWindow and if this one is null
	 *         returns a
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		
		IWorkbenchWindow activeWorkbenchWindow=null;
		if(!workbench.isClosing())
		{
			activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		}
		if (activeWorkbenchWindow == null) 
		{
			// return;
			int cnt = workbench.getWorkbenchWindowCount();
			if (cnt > 0)
				return workbench.getWorkbenchWindows()[0];
		}
		
		return activeWorkbenchWindow;			
	}
}
