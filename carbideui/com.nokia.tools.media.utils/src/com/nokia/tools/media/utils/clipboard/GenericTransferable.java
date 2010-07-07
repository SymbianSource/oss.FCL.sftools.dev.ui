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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Basic fileTransferable, implements also dummy ClipboardOwner
 * for convenience.
 */
public class GenericTransferable implements Transferable, ClipboardOwner {
	
		private Object data;
		private DataFlavor flavor;

		public GenericTransferable(Object data, DataFlavor flavor) {
			this.data = data;
			this.flavor = flavor;
		}
		
		public DataFlavor[] getTransferDataFlavors() {			
			return new DataFlavor[] {flavor};
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return this.flavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (this.flavor.equals(flavor)) {
				return data;
			}			
			return null;
		}

		/**
		 * dummy method
		 * @param clipboard
		 * @param contents
		 */
		public void lostOwnership(Clipboard clipboard, Transferable contents) {}
	
}
