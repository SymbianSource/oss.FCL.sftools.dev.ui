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

import org.eclipse.emf.ecore.EObject;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.content.ContentAdapter;
import com.nokia.tools.theme.content.ThemeData;

/**
 * Check this class usage, better to modify the domain models.
 * 
 */
public class ApplyThemeGraphicCommand extends ApplyFeatureCommand {
	private String id;
	private IContent content;
	private boolean isOriginalSkinned;

	/**
	 * @return the data
	 */
	public ThemeData getData() {
		EObject target = getTarget();
		if (target instanceof EditObject) {
			return ContentAdapter.getContentData((EditObject) target);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.ApplyFeatureCommand#setTarget(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public void setTarget(EObject target) {
		super.setTarget(target);
		if (target instanceof EditObject) {
			ThemeData data = getData();
			id = data.getId();
			content = data.getRoot();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.ApplyFeatureCommand#getTarget()
	 */
	@Override
	public EObject getTarget() {
		EObject target = super.getTarget();
		if (target instanceof EditObject) {
			ThemeData data = ContentAdapter.getContentData((EditObject) target);
			if (data == null && content != null) {
				// resource changed, rely on id
				data = (ThemeData) content.findById(id);
				if (data == null) {
					return null;
				}
				return data.getResource();
			}
		}
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.ApplyFeatureCommand#execute()
	 */
	@Override
	public void execute() {
		preExecute();
		super.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.editing.ui.command.ApplyFeatureCommand#undo()
	 */
	@Override
	public void undo() {
		super.undo();
		postUndo();
	}

	protected void preExecute() {
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) getData()
				.getAdapter(ISkinnableEntityAdapter.class);
		if (adapter != null) {
			//isOriginalSkinned = adapter.isSkinned() || adapter.isNinePiece();
			isOriginalSkinned = adapter.isSkinned() || adapter.isMultiPiece();
		}
	}

	protected void postUndo() {
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) getData()
				.getAdapter(ISkinnableEntityAdapter.class);
		if (adapter != null) {
			if (getValue() instanceof ThemeGraphic) {
				setValue(((ThemeGraphic) getValue()).clone());
			}
			if (!isOriginalSkinned && adapter.isSkinned()) {
				adapter.clearThemeGraphics();
				if (getData().getAdapter(IColorAdapter.class) != null) {
					EditingUtil.setFeatureValue(getTarget(), getFeature(),
							adapter.getThemeGraphics());
				}
			}
		}
	}
}
