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
package com.nokia.tools.screen.ui.propertysheet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.media.utils.ImageAdapter;

public class ColorLabelProvider extends LabelProvider {
	private List<Image> imagesToDispose = new ArrayList<Image>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		Color color = (Color) element;
		return color.getRed() + "," + color.getGreen() + "," + color.getBlue();
	}

	@Override
	public Image getImage(Object element) {
		Color color = (Color) element;
		// create image colored with that color
		BufferedImage img = new BufferedImage(17, 17,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		g.setColor(color);
		g.fillRect(2, 2, img.getWidth() - 4, img.getHeight() - 4);

		ImageAdapter vpAdaptor = new ImageAdapter(img.getWidth(), img
				.getHeight());
		Graphics vpG = vpAdaptor.getGraphics();
		vpG.drawImage(img, 0, 0, java.awt.Color.WHITE, null);
		vpG.dispose();
		Image image = vpAdaptor.toSwtImage();
		imagesToDispose.add(image);
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		for (Image image : imagesToDispose) {
			image.dispose();
		}
	}
}
