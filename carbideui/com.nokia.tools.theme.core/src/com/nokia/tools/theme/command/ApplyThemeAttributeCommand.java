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
package com.nokia.tools.theme.command;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.core.Activator;
import com.nokia.tools.theme.content.ThemeData;


public abstract class ApplyThemeAttributeCommand extends
		ApplyThemeGraphicCommand {

	public ApplyThemeAttributeCommand(ThemeData data) {
		setTarget(data.getResource());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.ApplyFeatureCommand#canExecute()
	 */
	@Override
	public boolean canExecute() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.ApplyFeatureCommand#canUndo()
	 */
	@Override
	public boolean canUndo() {
		return true;
	}

	protected abstract boolean doExecute();

	protected abstract boolean doUndo();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		preExecute();
		if (doExecute()) {
			updateThemeGraphic();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo() {
		execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		if (doUndo()) {
			updateThemeGraphic();
			postUndo();
		}
	}

	protected void updateThemeGraphic() {
		try {
			SkinnableEntity entity = getData().getSkinnableEntity();
			if (entity.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
				AnimatedThemeGraphic graphic = entity.getAnimatedThemeGraphic();
				EditingUtil.setFeatureValue(getData().getResource(),
						"themeGraphic", graphic);
			} else {
				ThemeGraphic tg = entity.getThemeGraphic();
				tg.setAttribute(ThemeTag.ATTR_STATUS,
						ThemeTag.ATTR_VALUE_ACTUAL);

				EditingUtil.setFeatureValue(getData().getResource(),
						"themeGraphic", tg);
			}
		} catch (Exception e) {
			Activator.error(e);
		}
	}
}
