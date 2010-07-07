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

package com.nokia.tools.screen.ui.dialogs;

import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.ui.dialog.IResourceSelectionPage;
import com.nokia.tools.ui.dialog.ResourceSelectionDialog;

public class ThemeResourceSelectionDialog<T>
    extends ResourceSelectionDialog
    implements IThemeResourcePageManager<T> {

	private ResourceResult<T> resourceResult;

	public ThemeResourceSelectionDialog(Shell parentShell,
	    IResourceSelectionPage[] pages, String bannerMessage) {
		this(parentShell, pages, bannerMessage, null, true);
	}

	public ThemeResourceSelectionDialog(Shell parentShell,
	    IResourceSelectionPage[] pages, String bannerMessage,
	    String titleMessage, boolean shouldPreviewImageCheckBoxShowUp) {
		super(parentShell, pages, bannerMessage, titleMessage,
		    shouldPreviewImageCheckBoxShowUp);
	}

	/**
	 * @return
	 */
	public ResourceResult<T> getResult() {
		return this.resourceResult;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourcePageManager#setResult(com.nokia.tools.screen.ui.dialogs.ResourceResult)
	 */
	public void setResult(ResourceResult resourceResult) {
		this.resourceResult = resourceResult;
	}
}