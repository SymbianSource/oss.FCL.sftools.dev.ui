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

package com.nokia.tools.media.utils.layers;

import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.io.File;

public interface IImageHolder extends Cloneable, IPasteTargetAdapter {
	public RenderedImage getImage();

	public RenderedImage getRAWImage(int width, int height, boolean applyMask);

	public RenderedImage getRAWImage(boolean applyMask);

	public boolean supportsMask();

	public boolean hasMask();

	public RenderedImage getMask();

	public void clearMask();

	public void pasteMask(Object data) throws Exception;

	public File getImageFile();

	public File getMaskFile();

	public int getWidth();

	public int getHeight();

	public boolean isBitmap();

	public boolean supportsBitmap();

	public boolean isSvg();

	public boolean supportsSvg();

	public void copyImageToClipboard(Clipboard clip);

	public void setImageFile(File imageFile);

	public void setMaskFile(File maskFile);

	public IImageHolder cloneImageHolder();

	public File getWorkDir();

	public void refresh();
}
