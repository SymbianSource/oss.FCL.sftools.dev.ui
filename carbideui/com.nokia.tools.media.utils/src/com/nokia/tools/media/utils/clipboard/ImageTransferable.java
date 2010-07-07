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
package com.nokia.tools.media.utils.clipboard;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes this as static class instead of inner one because the clipboard
 * will keep reference the the enclosing class until something overrides it
 */
public class ImageTransferable implements Transferable {
	private Object imageObject;

	public ImageTransferable(Object image) {
		this.imageObject = image;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.javaFileListFlavor,
				DataFlavor.imageFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (DataFlavor.javaFileListFlavor.equals(flavor) && (imageObject instanceof File
				|| imageObject instanceof List || imageObject instanceof String))
				|| (DataFlavor.imageFlavor.equals(flavor) && (imageObject instanceof RenderedImage || imageObject instanceof Image));
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor.equals(DataFlavor.javaFileListFlavor)) {
			if (imageObject instanceof File) {
				List<File> files = new ArrayList<File>(1);
				files.add((File) imageObject);
				return files;
			}

			if (imageObject instanceof List) {
				return imageObject;
			}

			if (imageObject instanceof String) {
				List<File> files = new ArrayList<File>(1);
				files.add(new File((String) imageObject));
				return files;
			}

			return null;
		}

		if (flavor.equals(DataFlavor.imageFlavor)) {
			if (imageObject instanceof RenderedImage) {
				return imageObject;
			}

			if (imageObject instanceof Image) {
				return imageObject;
			}

			return null;
		}
		return null;
	}
}

