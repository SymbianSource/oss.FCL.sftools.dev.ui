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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip;
import com.nokia.tools.media.utils.tooltip.ExtendedCompositeInformationControl;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipUIContribution;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;

public class IconTooltip extends DynamicTooltip {

	public static IconTooltip ACTIVE;

	protected IContentData data;

	protected CommandStack stack;

	public IconTooltip(IContentData data, Object uiContainer, CommandStack stack) {
		super(data, uiContainer, "component",
				DynamicTooltip.EStyle.HORIZONTAL_HORIZONTAL);
		this.data = data;
		this.stack = stack;
	}

	@Override
	protected void keyPressed(org.eclipse.swt.events.KeyEvent e) {
	};

	@Override
	protected CompositeInformationControl createFocusedControl() {
		return createUnfocusedControl();
	}

	@Override
	protected CompositeInformationControl createUnfocusedControl() {
		CompositeInformationControl cic = super.createUnfocusedControl();
		if (cic instanceof ExtendedCompositeInformationControl) {
			final ExtendedCompositeInformationControl exCic = (ExtendedCompositeInformationControl) cic;
			exCic.setBackground(ColorConstants.white);
			exCic.setMoveable(false);
			exCic.addTitleAction(exCic.new CloseAction());
			exCic.getTitleMenu().addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					manager.add(exCic.new CloseAction());
				}
			});
		}
		return cic;
	}

	@Override
	protected void configureUIContribution(IDynamicTooltipUIContribution ui) {
		super.configureUIContribution(ui);
		if (ui instanceof IconTooltipUIContributor) {
			((IconTooltipUIContributor) ui).setStack(stack);
		}
	}
}
