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
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;

public class SetStretchModeAction extends AbstractAction {

	public static final String ID = "SetStretchMode"; 

	public static final String ID_Stretch = ID
			+ "." + IMediaConstants.STRETCHMODE_STRETCH; 
	public static final String ID_Aspect = ID
			+ "." + IMediaConstants.STRETCHMODE_ASPECT; 	

	private String stretchMode;

	public static String getId(String stretchMode) {
		return ID + "." + stretchMode;
	}

	public SetStretchModeAction(IWorkbenchPart part, String stretchMode) {
		this(part, stretchMode, null);
	}

	public SetStretchModeAction(ISelectionProvider provider,
			CommandStack stack, String stretchMode) {
		this(provider, stack, stretchMode, null);
	}

	public SetStretchModeAction(IWorkbenchPart part, String stretchMode,
			String label) {
		super(part, AS_CHECK_BOX);
		this.stretchMode = stretchMode;
		setId(ID + "." + stretchMode);
		setText(label == null ? UtilsPlugin.getStretchModeLabel(stretchMode)
				: label); 
	}

	public SetStretchModeAction(ISelectionProvider provider,
			CommandStack stack, String stretchMode, String label) {
		super(null, AS_CHECK_BOX);
		this.stack = stack;
		this.stretchMode = stretchMode;
		setId(ID + "." + stretchMode);
		setText(label == null ? UtilsPlugin.getStretchModeLabel(stretchMode)
				: label); 
		setSelectionProvider(provider);
	}

	@Override
	public void doRun(Object sel) {

		if (isChecked())
			return;

		ILayer element = null;
		if (sel instanceof ILayer)
			element = (ILayer) sel;
		else
			element = getLayer(false, true, sel);

		if (element instanceof ILayer) {
			final ILayer layer = (ILayer) element;

			// called from view
			try {
				layer.setStretchMode(stretchMode);
			} catch (Exception e) {
				e.printStackTrace();
			}

			IContentData data = getContentData(sel);
			if (data != null) {

				ForwardUndoCompoundCommand cc = new ForwardUndoCompoundCommand(
						getText());
				ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				cc.add(adapter.getApplyStretchModeCommand(stretchMode));
				Command updateCommand = getUpdateLayerCommand(layer, sel);
				cc.add(updateCommand);
				execute(cc, null);
			}
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {

		if (selection == null) {
			setChecked(false);
			return false;
		}

		IContentData data = getContentData(sel);

		if (data == null) {
			/* invoked from animation editor */
			if (sel instanceof ILayer) {
				ILayer layer = (ILayer) sel;
								
				if (layer.hasImage() && !layer.isBackground()) {
					String sMode = layer.getStretchMode();
					setChecked(stretchMode.equals(sMode));
					return true;
				}
			}
		} else {
			IToolBoxAdapter tba = (IToolBoxAdapter) data
					.getAdapter(IToolBoxAdapter.class);
			if (tba != null && tba.isMultipleLayersSupport()) {
				// compute enablement & check state
				ILayer element = getLayer(false, true, sel);
				if (element != null) {
					if (element.hasImage() && !element.isBackground()) {
						setChecked(stretchMode.equalsIgnoreCase(element
								.getStretchMode()));
						return true;
					}
				}
			}
		}

		setChecked(false);
		return false;
	}
}
