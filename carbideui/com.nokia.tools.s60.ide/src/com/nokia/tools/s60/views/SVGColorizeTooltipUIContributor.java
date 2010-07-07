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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipUIContribution;
import com.nokia.tools.s60.editor.actions.ColorizeSvgAction;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.ColorPickerComposite;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

public class SVGColorizeTooltipUIContributor extends IconTooltipUIContributor
		implements IDynamicTooltipUIContribution {

	@Override
	public void createControls(Composite parent, boolean focusState) {
		final IContentData data = (IContentData) selection;
		ISkinnableEntityAdapter helper = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
		if (helper != null && helper.isSVG()) {

			final ColorChangedLabelWrapper colorChangedLabelWrapper = new ColorChangedLabelWrapper();
			colorChangedLabelWrapper.setColorString(ColorUtil
					.asHashString(ColorUtil.getRGB("#ffffff")));

				IColorPickerListener colorPickerListener = new IColorPickerListener() {
					public void selectionChanged() {
						okCloseDialog();
					}

					public void okCloseDialog() {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								tooltip.hide(true);
							}
						});
					
						// adapt screen element to screen context if needed
						data.getAdapter(EditObject.class);

						ColorizeSvgAction action = new ColorizeSvgAction(
								new SimpleSelectionProvider(data), stack);
						action.setColor(colorChangedLabelWrapper
								.getColorDescriptor().getRGB(), 96);
						if (action.isEnabled()) {
							action.run();
						}
					}
				};

				Composite cPickerComposite = new ColorPickerComposite(parent,
						SWT.NONE, colorChangedLabelWrapper, colorPickerListener);
		}
	}
}
