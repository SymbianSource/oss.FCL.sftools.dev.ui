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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * 
 */
public abstract class BrandedTitleAreaDialog extends TitleAreaDialog {
	private Image titleAreaImage, windowImage;

	public BrandedTitleAreaDialog(Shell parentShell) {
		super(parentShell);
	}

	protected abstract ImageDescriptor getBannerIconDescriptor();

	protected abstract String getTitle();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		IBrandingManager manager = BrandingExtensionManager.getBrandingManager();
		if (manager != null) {
			ImageDescriptor bannerIcon = getBannerIconDescriptor();
			titleAreaImage = manager.getBannerImageDescriptor(bannerIcon)
					.createImage();
			setTitleImage(titleAreaImage);

			windowImage = manager.getIconImageDescriptor().createImage();
			newShell.setImage(windowImage);
		}

		newShell.setText(getTitle());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.TrayDialog#close()
	 */
	@Override
	public boolean close() {
		if (titleAreaImage != null) {
			titleAreaImage.dispose();
		}
		if (windowImage != null) {
			windowImage.dispose();
		}
		return super.close();
	}
}
