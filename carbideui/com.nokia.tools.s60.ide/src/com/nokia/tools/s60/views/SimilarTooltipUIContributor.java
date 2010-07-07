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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipUIContribution;

public class SimilarTooltipUIContributor extends IconTooltipUIContributor implements
		IDynamicTooltipUIContribution {

	@Override
	public void createControls(final Composite parent, boolean focusState) {
		final IContentData data = (IContentData) selection;
		final Label pleaseWaitLabel = new Label(parent, SWT.NONE);
		parent.setBackground(ColorConstants.white);
		pleaseWaitLabel.setBackground(ColorConstants.white);
		pleaseWaitLabel.setText("Parsing themes data, please wait...");
		
	}
}
