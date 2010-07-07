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
package com.nokia.tools.screen.ui.propertysheet.color;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;

/**
 * As extension of swt button is not allowed, utility performs action of setting
 * the color, color image is set and unused images disposed
 */
public class ColoredButtonUtility {
	private static final DisposeListener DISPOSE_LISTENER = new DisposeListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
		 */
		public void widgetDisposed(DisposeEvent e) {
			if (e.widget instanceof Button) {
				Image image = ((Button) e.widget).getImage();
				if (image != null) {
					image.dispose();
				}
			}
		}

	};

	/**
	 * @param button
	 * @param color
	 */
	public static void setButtonColor(Button button, int width, int height,
			RGB color) {
		button.removeDisposeListener(DISPOSE_LISTENER);
		button.addDisposeListener(DISPOSE_LISTENER);

		Image colorImage = new Image(button.getDisplay(), createColorRectangle(
				width, height, color));
		Image oldImage = button.getImage();
		button.setImage(colorImage);
		if (oldImage != null)
			oldImage.dispose();
	}

	protected static ImageData createColorRectangle(int width, int height,
			RGB rgb) {
		ImageData imageData = null;
		PaletteData paletteData = new PaletteData(new RGB[] { rgb,
				new RGB(0, 0, 0) });
		imageData = new ImageData(width, height, 1, paletteData);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				imageData.setPixel(x, y, 0);
			}
		}
		return imageData;
	}
}
