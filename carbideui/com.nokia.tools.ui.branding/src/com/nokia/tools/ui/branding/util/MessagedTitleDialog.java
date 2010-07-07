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
package com.nokia.tools.ui.branding.util;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract titled dialog with banner message
 * 
 */
public abstract class MessagedTitleDialog extends BrandedTitleAreaDialog {

	public MessagedTitleDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (getBannerMessage() != null) {
			setMessage(getBannerMessage());
		}
		if (getBannerTitle() != null) {
			setTitle(getBannerTitle());
		}
		return super.createDialogArea(parent);
	}

	/**
	 * Gets the banner message null if no message needed
	 * 
	 * @return - the banner message required or <code> null </code>
	 */
	protected abstract String getBannerMessage();

	/**
	 * Gets the banner title null if no message needed
	 * 
	 * @return - the banner title required or <code> null </code>
	 */
	protected abstract String getBannerTitle();

}
