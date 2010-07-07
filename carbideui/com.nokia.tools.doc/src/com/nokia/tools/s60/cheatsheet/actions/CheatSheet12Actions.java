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
package com.nokia.tools.s60.cheatsheet.actions;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.editor.actions.DistributeAnimateTimeAction;
import com.nokia.tools.s60.editor.actions.SetAnimateTimeAction;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 * perform action for cheatsheet - edit animation time action
 * 
 */
public class CheatSheet12Actions extends BaseAction implements
		ICheatSheetAction {

	public void run(String[] params, ICheatSheetManager manager) {
		if ("setAnimTime".equals(params[0])) {
			doSetAnimTime(params);
		} else if ("runAnimation".equals(params[0])) {
			doRunAnimation();
		} else if ("doSave".equals(params[0])) {
			try {
				IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
				if (activeEd instanceof GraphicsEditorPart) {
					GraphicsEditorPart sep = (GraphicsEditorPart) activeEd;
					sep.doSave(new NullProgressMonitor());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void doSetAnimTime(String[] params) {
		int time1 = Integer.parseInt(params[1]);
		int time2 = Integer.parseInt(params[2]);
		// create selection object and send it to icon view
		try {
			IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
			if (activeEd instanceof GraphicsEditorPart) {
				GraphicsEditorPart sep = (GraphicsEditorPart) activeEd;
				IImage current = sep.getActiveImage();

				DistributeAnimateTimeAction da = new DistributeAnimateTimeAction(
						new SimpleSelectionProvider(current), sep
								.getCommandStack());
				da.setSelectedTime(time1);
				da.run();

				IAnimatedImage aimg = (IAnimatedImage) current;
				IAnimationFrame[] frmaes = aimg.getAnimationFrames();
				IAnimationFrame frame = frmaes.length < 3 ? frmaes[frmaes.length - 1]
						: frmaes[2];
				SetAnimateTimeAction saa = new SetAnimateTimeAction(
						new SimpleSelectionProvider(frame), sep
								.getCommandStack());
				saa.setSelectedTime(time2);
				saa.run();

				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		notifyResult(false);
	}

}
