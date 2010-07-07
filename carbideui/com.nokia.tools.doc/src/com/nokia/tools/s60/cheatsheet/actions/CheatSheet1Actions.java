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

import java.awt.Color;
import java.util.List;
import java.util.Stack;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.ICheatSheetAction;
import org.eclipse.ui.cheatsheets.ICheatSheetManager;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ISeries60Command;
import com.nokia.tools.theme.s60.S60ThemeProvider;

/**
 * perform action for cheatsheet 13 - edit layer animation
 * 
 */
public class CheatSheet1Actions extends BaseAction implements ICheatSheetAction {

	static class UpdateCommand extends Command implements ISeries60Command {

		private IContentData data;
		private Runnable worker;

		public UpdateCommand(IContentData d, Runnable w) {
			data = d;
			worker = w;
		}

		public IContentData getData() {
			return data;
		}

		public String getElementId() {
			return data.getId();
		}

		@Override
		public void execute() {
			worker.run();
		}

		@Override
		public boolean canUndo() {
			return false;
		}
	}

	public void run(String[] params, ICheatSheetManager manager) {
		if ("changeColor".equals(params[0])) {
			String color = params[1];
			String elementId = params[2];
			try {
				IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
				EditPartViewer viewer = getEditPartViewer();
				IContentAdapter adapter = (IContentAdapter) activeEd
						.getAdapter(IContentAdapter.class);
				if (viewer != null && adapter != null) {
					IContent[] cnt = adapter
							.getContents(S60ThemeProvider.CONTENT_TYPE);
					if (cnt.length == 0) {
						return;
					}
					IContent root = cnt[0].getRoot();
					List childrens = viewer.getRootEditPart().getChildren();
					EditPart rootScreen = (EditPart) ((EditPart) childrens
							.get(0)).getChildren().get(0);

					// iterate through all editparts on screen until we find
					// desired element
					Stack<EditPart> stack = new Stack<EditPart>();
					stack.push(rootScreen);
					while (stack.isEmpty() == false) {
						EditPart ep = stack.pop();
						if (isModelElement(ep, elementId)) {
							viewer.setSelection(new StructuredSelection(ep));
							// activate editor part
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.activate(activeEd);

							final IScreenElement screenEl = JEMUtil
									.getScreenElement(ep);
							IContentData data = screenEl.getData();
							ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) data
									.getAdapter(ISkinnableEntityAdapter.class);
							BitmapProperties bitmap = new BitmapProperties(ska
									.getAttributes());
							bitmap.setColorize(true);
							bitmap.setColor(new Color(Integer.parseInt(color
									.substring(1, 3), 16), Integer.parseInt(
									color.substring(3, 5), 16), Integer
									.parseInt(color.substring(5), 16)));
							CommandStack cs = (CommandStack) activeEd
									.getAdapter(CommandStack.class);
							Command command = ska
									.getApplyBitmapPropertiesCommand(bitmap);
							if (cs != null) {
								cs.execute(command);
							} else {
								command.execute();
							}

							if (activeEd instanceof ISetSelectionTarget) {
								((ISetSelectionTarget) activeEd)
										.selectReveal(new StructuredSelection(
												data));
								return;
							}
						}
						for (Object _p : ep.getChildren()) {
							stack.push((EditPart) _p);
						}
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		notifyResult(false);
	}

}
