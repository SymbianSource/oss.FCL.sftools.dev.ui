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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic fileTransferable, implements also dummy ClipboardOwner
 * for convenience.
 */
public class FileTransferable implements Transferable, ClipboardOwner {
	
		private List<File> files;

		public FileTransferable(List<File>  files) {
			this.files = files;
		}
		
		public FileTransferable(File...  _files) {
			files = new ArrayList<File>();
			if (_files != null)
				for (File x: _files)
					files.add(x);
		}		

		public DataFlavor[] getTransferDataFlavors() {			
			return new DataFlavor[] {DataFlavor.javaFileListFlavor};
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor.equals(DataFlavor.javaFileListFlavor));
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.javaFileListFlavor)) {
				return files;
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
