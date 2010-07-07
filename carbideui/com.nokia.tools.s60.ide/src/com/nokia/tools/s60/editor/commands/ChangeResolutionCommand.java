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
package com.nokia.tools.s60.editor.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.s60.views.ColorsView;
import com.nokia.tools.s60.views.SearchView;
import com.nokia.tools.s60.views.SearchViewPage;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 */
public class ChangeResolutionCommand extends Command {
	private IContentAdapter adapter;
	private Display oldDisplay;
	private Display newDisplay;
	public static final String ID = "com.nokia.tools.s60.views.SearchView";

	public ChangeResolutionCommand(IContentAdapter adapter, Display newDisplay) {
		setLabel(Messages.ChangeResolution_Label);

		this.adapter = adapter;
		this.newDisplay = newDisplay;
		this.oldDisplay = (Display) getTheme().getAttribute(
				ContentAttribute.DISPLAY.name());
	}

	private IContent getTheme() {
		return ScreenUtil.getPrimaryContent(adapter.getContents());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		return getTheme() != null && newDisplay != null
				&& !newDisplay.equals(oldDisplay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		oldDisplay = (Display) getTheme().getAttribute(
				ContentAttribute.DISPLAY.name());
		getTheme().setAttribute(ContentAttribute.DISPLAY.name(), newDisplay);
		adapter.updateContent(ContentAttribute.DISPLAY);
		
		//refreshing search view once resolution changes happens
		try {
			if (isNotNull(PlatformUI.getWorkbench())
					&& isNotNull(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow())
					&& isNotNull(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage())) {
				IViewPart view = (IViewPart) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.findView(ID);
				if (view != null){
					SearchViewPage page=(SearchViewPage)((SearchView)view).getCurrentPage();
					page.refresh();
				}			
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo() {
		newDisplay = oldDisplay;
		execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		newDisplay = oldDisplay;
		execute();
	}

	@Override
	public boolean canUndo() {
		return oldDisplay != null;
	}
	
	/**
	 * utility function for checking if object under consideration is null or
	 * not
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isNotNull(Object obj) {
		return obj != null;
	}
}
