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

package com.nokia.tools.media.image;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.swt.graphics.Image;

/**
 * Model Obect for a program
 * 
 * 
 */
public class ProgramDescriptor {

	private String fullPath;

	private Image icon;

	private String name;

	public ProgramDescriptor(final String name, final String fullPath,
	    final Image icon) {
		super();
		this.name = name;
		this.fullPath = fullPath;
		this.icon = icon;
		if (icon == null) {
			computeIcon();

		}
	}

	private void computeIcon() {
		final Icon iconImage = FileSystemView.getFileSystemView()
		    .getSystemIcon(new File(fullPath));

		if (iconImage instanceof ImageIcon) {
			try {
				final java.awt.Image awtImage = ((ImageIcon) iconImage)
				    .getImage();
				if (awtImage instanceof BufferedImage) {
					final CoreImage coreImage = CoreImage.create();
					coreImage.setAwt((BufferedImage) awtImage);
					icon = coreImage.getSwt();

				}
			} catch (final Exception e) {

			}

		}

	}

	public void dispose() {
		if (icon != null) {
			icon.dispose();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		boolean equal = super.equals(obj);
		if (!equal) {
			if ((obj != null) && (obj instanceof ProgramDescriptor)) {
				final ProgramDescriptor des = (ProgramDescriptor) obj;
				if ((des.getFullPath() != null)
				    && des.getFullPath().equals(this.fullPath)) {
					if ((des.getName() != null)
					    && des.getName().equals(this.name)) {
						equal = true;
					}
				}
			}
		}
		return equal;
	}

	public String getFullPath() {
		return fullPath;
	}

	public Image getIcon() {
		return icon;
	}

	public String getName() {
		return name;
	}

	public void setFullPath(final String fullPath) {
		this.fullPath = fullPath;
	}

	public void setIcon(final Image icon) {
		this.icon = icon;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return (getName() + " : " + getFullPath());
	}
}
