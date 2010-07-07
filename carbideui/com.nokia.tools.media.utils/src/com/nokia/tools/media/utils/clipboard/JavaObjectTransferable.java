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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class JavaObjectTransferable implements Transferable {

	private Object content;
	private DataFlavor[] flavor;
	
	public JavaObjectTransferable(Object content) {
		this.flavor = new DataFlavor[]{new JavaObjectDataFlavor(content.getClass(), content.getClass().getName())};
		this.content = content;
	}
	
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor instanceof JavaObjectDataFlavor) {
			JavaObjectDataFlavor fl = (JavaObjectDataFlavor) flavor;
			if (fl.getRepresentationClass().isAssignableFrom(content.getClass()))
				return content;
		}
		return null;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavor;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (flavor instanceof JavaObjectDataFlavor) {
			JavaObjectDataFlavor fl = (JavaObjectDataFlavor) flavor;
			if (fl.getRepresentationClass().isAssignableFrom(content.getClass()))
				return true;
		}
		return false;
	}

	public Object getTransferData() {
		return content;
	}
	

}
