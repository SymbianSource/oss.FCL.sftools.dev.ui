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
package com.nokia.tools.ui.branding.defaultimpl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.Activator;

/**
 * Default implementation of <code>IBrandingManager</code> interface
 * 
 * 
 */
public class BrandingManager implements IBrandingManager {
	
	/**
	 * Default background color for all text areas/labels
	 * 
	 */
	private  static Color BACKGROUND_ALL_RGB = new Color(null, 209, 209, 211);

	/**
	 * Default foreground color and font for banner title
	 */
	private static Color FOREGROUND_TITLE_RGB = new Color(null, 254, 254, 254);

	/**
	 * Default foreground color and font for banner message
	 * 
	 */
	private static Color FOREGROUND_MESSAGE_RGB = new Color(null, 26, 26, 26);

	/**
	 * 
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#brandTitleArea(org.eclipse.swt.widgets.Shell)
	 */
	public void brandTitleArea(Shell shell) {
		Control[] ctrl = shell.getChildren();

		// Same background color for all text areas/labels
		ctrl[0].getParent().setBackground(getBackgroundColor());
		for (int i = 1; i < ctrl.length; i++) {
			ctrl[i].setBackground(getBackgroundColor());
		}

		// Foreground color and font for banner title
		ctrl[2].setForeground(getForegroundTitleColor());
		FontData fd = ctrl[2].getFont().getFontData()[0];
		final Font font = new Font(Display.getDefault(), fd.getName(),
				(int) (fd.height + 2), SWT.BOLD);
		ctrl[2].setFont(font);

		// Foreground color and font for banner message
		ctrl[4].setForeground(getForegroundMessageColor());
		fd = ctrl[4].getFont().getFontData()[0];
		final Font font2 = new Font(Display.getDefault(), fd.getName(),
				(int) (fd.height), SWT.BOLD);
		ctrl[4].setFont(font2);

		shell.addDisposeListener(new DisposeListener() {

			/**
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				font.dispose();
				font2.dispose();
			}
		});
	}
	
	/**
	 *
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#getBannerImageDescriptor()
	 */
	public ImageDescriptor getBannerImageDescriptor() {
		return getBannerImageDescriptor(null);
	}

	/**
	 * 
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#getBannerImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public ImageDescriptor getBannerImageDescriptor(ImageDescriptor icon) {
		ImageDescriptor desc = Activator
				.getImageDescriptor("icons/dialog_banner_150x66.bmp");

		if (icon != null) {
			Image bannerImage = null;
			Image iconImage = null;
			GC imageGC = null;

			try {
				bannerImage = desc.createImage();
				iconImage = icon.createImage();
				imageGC = new GC(bannerImage);
				imageGC.drawImage(iconImage,
						(int) ((bannerImage.getBounds().width - 17) - iconImage
								.getBounds().width), (int) (33 - (iconImage
								.getBounds().height / 2)));
				return ImageDescriptor.createFromImageData(bannerImage
						.getImageData());
			} finally {
				if (imageGC != null) {
					imageGC.dispose();
				}
				if (bannerImage != null) {
					bannerImage.dispose();
				}
				if (iconImage != null) {
					iconImage.dispose();
				}
			}
		}
		return desc;
	}

	/**
	 * 
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#getIconImageDescriptor()
	 */
	public ImageDescriptor getIconImageDescriptor() {
		return Activator
				.getImageDescriptor("icons/carbide_ui_icon_16x16.png");
	}

	/**
	 * Gets the default background <code>Color</code> with RGB value 209, 209, 211
	 * 
	 * @return - background  <code>Color</code> with RGB value 209, 209, 211
	 * 
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#getBackgroundColor()
	 */
	public Color getBackgroundColor() {
		return BACKGROUND_ALL_RGB;
	}
	
	/**
	 *
	 * Gets the default message foreground <code>Color</code> with RGB value  26, 26, 26
	 * 
	 * @return - message foreground <code>Color</code> with RGB value  26, 26, 26
	 * 
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#getForegroundMessageColor()
	 */
	public Color getForegroundMessageColor() {
		return FOREGROUND_MESSAGE_RGB ;
	}

	/**
	 *  Gets the default title foreground <code>Color</code> with RGB value 254, 254, 254
	 * 
	 * @return - title foreground <code>Color</code> with RGB value 254, 254, 254
	 * 
	 * @see com.nokia.tools.screen.ui.branding.IBrandingManager#getForegroundTitleColor()
	 */
	public Color getForegroundTitleColor() {
		return FOREGROUND_TITLE_RGB;
	}
	
	
}
