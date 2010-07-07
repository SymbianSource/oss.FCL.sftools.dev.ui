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

package com.nokia.tools.s60.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipUIContribution;
import com.nokia.tools.s60.editor.actions.SetColorAction;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColorPickerComposite;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

public class ColorPickerTooltipUIContributor extends IconTooltipUIContributor
		implements IDynamicTooltipUIContribution {

	@Override
	public void createControls(Composite parent, boolean focusState) {
		final IContentData data = (IContentData) selection;

		IColorAdapter colorAdapter = (IColorAdapter) data
				.getAdapter(IColorAdapter.class);
		if (colorAdapter != null) {

			final ColorChangedLabelWrapper colorChangedLabelWrapper = new ColorChangedLabelWrapper();
			colorChangedLabelWrapper.setColorString(ColorUtil
					.asHashString(ColorUtil.toRGB(colorAdapter.getColor())));

			IColorPickerListener colorPickerListener = new IColorPickerListener() {
				public void selectionChanged() {
					okCloseDialog();
				}

				public void okCloseDialog() {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							tooltip.hide(true);
						}
					});

					
					data.getAdapter(EditObject.class);

					SetColorAction action = new SetColorAction(
							new SimpleSelectionProvider(data), stack);
					action.setSelectedColor(colorChangedLabelWrapper
							.getColorDescriptor().getRGB());
					if (action.isEnabled()) {
						action.run();
					}
				}
			};

			Composite cPickerComposite = new ColorPickerComposite(parent,
					SWT.NONE, colorChangedLabelWrapper, colorPickerListener);

			parent.setBackground(cPickerComposite.getBackground());

			cPickerComposite.setLayoutData(new GridData(SWT.CENTER, SWT.NONE,
					true, false));
		}
	}

}
