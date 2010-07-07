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
package com.nokia.tools.theme.s60;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.command.ApplyThemeAttributeCommand;
import com.nokia.tools.theme.content.ThemeData;


public class ApplyStretchModeCommand extends ApplyThemeAttributeCommand {
	private String stretchMode;
	private String oldStretchMode;
	private boolean isOriginalSkinned;

	public ApplyStretchModeCommand(ThemeData data, String stretchMode) {
		super(data);
		this.stretchMode = stretchMode;
		setLabel(com.nokia.tools.theme.command.Messages.Command_ApplyStretchMode_Label);
		S60SkinnableEntityAdapter adapter = getAdapter();
		if (adapter != null) {
			isOriginalSkinned = adapter.isSkinned();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		S60SkinnableEntityAdapter adapter = getAdapter();
		return adapter != null && adapter.getGraphicsLayer() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	@Override
	public boolean canUndo() {
		S60SkinnableEntityAdapter adapter = getAdapter();
		return adapter != null && adapter.getGraphicsLayer() != null;
	}

	protected S60SkinnableEntityAdapter getAdapter() {
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) getData()
				.getAdapter(ISkinnableEntityAdapter.class);
		if (adapter instanceof S60SkinnableEntityAdapter) {
			return (S60SkinnableEntityAdapter) adapter;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ApplyThemeAttributeCommand#doExecute()
	 */
	@Override
	protected boolean doExecute() {
		S60SkinnableEntityAdapter adapter = getAdapter();
		if (adapter != null) {
			ILayer layer = adapter.getGraphicsLayer();
			if (layer != null) {
				oldStretchMode = layer.getStretchMode();
				layer.setStretchMode(stretchMode);
				updateThemeGraphic(adapter, layer);
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ApplyThemeAttributeCommand#doUndo()
	 */
	@Override
	protected boolean doUndo() {
		if (!isOriginalSkinned) {
			// not skinned and no need to set graphic, may cause problem to
			// create two graphics later
			return false;
		}
		S60SkinnableEntityAdapter adapter = getAdapter();
		if (adapter != null) {
			ILayer layer = adapter.getGraphicsLayer();
			if (layer != null) {
				layer.setStretchMode(oldStretchMode);
				updateThemeGraphic(adapter, layer);
			}
		}
		return false;
	}

	protected void updateThemeGraphic(S60SkinnableEntityAdapter adapter,
			ILayer layer) {
		if (getData().getSkinnableEntity().isEntityType().equals(
				ThemeTag.ELEMENT_BMPANIM)) {
			updateThemeGraphic();
		} else {
			try {
				ThemeGraphic tg = (ThemeGraphic) adapter
						.getEditedThemeGraphics(layer.getParent());
				EditingUtil.setFeatureValue(getData().getResource(),
						"themeGraphic", tg);
			} catch (Exception e) {
				S60ThemePlugin.error(e);
			}
		}
	}
}
