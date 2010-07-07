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
package com.nokia.tools.media.utils.editor.frameanimation;

import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IAnimationFrame;

public class AnimationImageDragSource implements DragSourceListener {
	private Button dragSourceButton = null;

	private int operations = DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_MOVE;

	private DragSource source = null;

	Transfer[] types = new Transfer[] { LocalSelectionTransfer.getInstance(), FileTransfer.getInstance()};

	private String[] eventData;

	public AnimationImageDragSource(Button b) {
		dragSourceButton = b;
		source = new DragSource(b, operations);
		source.setTransfer(types);
		source.addDragListener(this);
	}

	public void dragStart(DragSourceEvent event) {
		List<Button> selectionList = new ArrayList<Button>();
		selectionList.add(dragSourceButton);
		ISelection selection = new StructuredSelection(selectionList);
		LocalSelectionTransfer.getInstance().setSelection(selection);
		LocalSelectionTransfer.getInstance().setSelectionSetTime(
				event.time & 0xFFFF);
		event.doit = true;
	
		try {
			IAnimationFrame sourceImg = (IAnimationFrame) dragSourceButton
					.getData(IAnimationFrame.class.getName());
			Clipboard clip = new Clipboard("");
			sourceImg.copyImageToClipboard(clip);
			Object possibleFile = ClipboardHelper.getClipboardContent(ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK, clip);
			if (possibleFile instanceof List) {						
				List files = (List) possibleFile;
				List<String> result = new ArrayList<String>();
				for (Object item: files) {
					if (item instanceof File)
						result.add(((File)item).getAbsolutePath());
				}
				if (result.size() > 0) {
					eventData = result.toArray(new String[result.size()]);				
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	public void dragSetData(DragSourceEvent event) {
		try {
			if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
				event.data = eventData;
				event.detail = DND.DROP_COPY;
				FileTransfer.getInstance().javaToNative(event.data, FileTransfer.getInstance().getSupportedTypes()[0]);
			} else
				event.data = LocalSelectionTransfer.getInstance().getSelection();		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public void dragFinished(DragSourceEvent event) {
	}
}
