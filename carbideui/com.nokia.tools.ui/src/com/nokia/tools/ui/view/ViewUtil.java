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

package com.nokia.tools.ui.view;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ContainerPlaceholder;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.internal.WorkbenchPage;

public class ViewUtil {
	private ViewUtil() {
	}

	public static void minimizeView(String viewId) {
		ViewStack stack = getViewStack(viewId);
		if (stack != null) {
			stack.setMinimized(true);
		} else {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			IViewReference ref = page.findViewReference(viewId);
			page.setPartState(ref, IWorkbenchPage.STATE_MINIMIZED);
		}
	}

	public static void restoreView(String viewId) {
		ViewStack stack = getViewStack(viewId);
		if (stack != null) {
			stack.setMinimized(false);
		} else {
			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			IViewReference ref = page.findViewReference(viewId);
			page.setPartState(ref, IWorkbenchPage.STATE_RESTORED);
		}
	}

	private static ViewStack getViewStack(String viewId) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		Perspective persp = ((WorkbenchPage) page).getActivePerspective();
		LayoutPart layoutPart = persp.getPresentation().findPart(viewId, null);
		ViewStack stack = null;
		if (layoutPart.getContainer() instanceof ViewStack) {
			return (ViewStack) layoutPart.getContainer();
		}
		if (layoutPart instanceof ContainerPlaceholder) {
			return (ViewStack) ((ContainerPlaceholder) layoutPart)
					.getRealContainer();
		}
		if (layoutPart.getContainer() instanceof ContainerPlaceholder) {
			return (ViewStack) ((ContainerPlaceholder) layoutPart
					.getContainer()).getRealContainer();
		}
		return null;
	}
}
