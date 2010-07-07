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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Perspective;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

public class ActionSetCustomizer {
	private Set<String> idsToRem = new HashSet<String>();

	public void addId(String id) {
		idsToRem.add(id);
	}

	public void turnOffActionSets() {
		WorkbenchWindow window = (WorkbenchWindow) PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		if (page == null) {
			return;
		}
		Perspective perspective = page.getActivePerspective();
		if (perspective == null) {
			return;
		}

		if (WorkbenchPlugin.getDefault() == null) {
			return;
		}

		List<IActionSetDescriptor> descs = new ArrayList<IActionSetDescriptor>();
		ActionSetRegistry reg = WorkbenchPlugin.getDefault()
		    .getActionSetRegistry();
		IActionSetDescriptor[] sets = reg.getActionSets();
		for (IActionSetDescriptor desc : perspective.getAlwaysOnActionSets()) {
			if (idsToRem.contains(desc.getId())) {
				descs.add(desc);
			}
		}
		perspective.turnOffActionSets(descs
		    .toArray(new IActionSetDescriptor[descs.size()]));

		window.updateActionSets();
	}
}
