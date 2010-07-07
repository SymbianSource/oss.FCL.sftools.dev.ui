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
package com.nokia.tools.s60.editor.actions;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.s60.editor.commands.ChangeThemeModelCommand;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 */
public class ChangeThemeModelAction extends CommandStackAction {
	private String modelId;
	private IAction parentAction;

	public ChangeThemeModelAction(IWorkbenchPart part, IAction parentAction,
			String modelId) {
		super(part);
		this.modelId = modelId;
		this.parentAction = parentAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.CommandStackAction#run()
	 */
	@Override
	public void run() {
		super.run();
		parentAction.run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.CommandStackAction#createCommand(com.nokia.tools.content.core.IContentAdapter)
	 */
	@Override
	protected Command createCommand(IContentAdapter adapter) {
		IContent content = ScreenUtil.getPrimaryContent(adapter.getContents());
		if (content != null) {
			String currentModelId = (String) content
					.getAttribute(ContentAttribute.MODEL.name());
			if (modelId != null && !modelId.equalsIgnoreCase(currentModelId)) {
				return new ChangeThemeModelCommand(adapter, modelId);
			}
		}
		return null;
	}
}
