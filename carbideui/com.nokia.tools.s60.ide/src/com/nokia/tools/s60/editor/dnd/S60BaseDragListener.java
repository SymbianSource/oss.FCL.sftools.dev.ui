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
package com.nokia.tools.s60.editor.dnd;

import java.awt.datatransfer.Clipboard;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

/**
 * Capable of working with filetransfer & localselection trasnfer.
 */
public class S60BaseDragListener implements
		org.eclipse.jface.util.TransferDragSourceListener {

	private Transfer transfer;

	private ISelectionProvider provider;
	
	private Object eventData;
	private int eventDetail;
	
	private Object eventDataLocal;

	public S60BaseDragListener(Transfer transfer, ISelectionProvider provider) {
		this.transfer = transfer;
		this.provider = provider;
	}

	protected Object getSelectedElement(DragSourceEvent evt) {
		DragSource ds = (DragSource) evt.widget;
		if (ds.getControl() instanceof Tree) {
			Tree tree = (Tree) ds.getControl();
			TreeItem treeItem = tree.getSelection()[0];
			if (treeItem == null) {
				return null;
			} else {
				return treeItem.getData();
			}
		} else {
			IStructuredSelection selected = (IStructuredSelection) provider
					.getSelection();
			return selected.getFirstElement();
		}
	}

	public Transfer getTransfer() {
		return transfer;
	}

	public void dragFinished(DragSourceEvent event) {
		
	}	

	public void dragStart(DragSourceEvent event) {
		
		event.detail = DND.DROP_NONE;
		if (transfer == LocalSelectionTransfer.getInstance()) {
			LocalSelectionTransfer.getInstance().setSelection(null);
		}

		Object selectedData = getSelectedElement(event);
		List<ClipboardContentDescriptor> extendedSelection = null;
		if (selectedData != null) {
			if (DNDUtil.isSelectionDraggeable(selectedData)) {									
				
				IStructuredSelection selection = (IStructuredSelection) provider.getSelection();
				//items from selection				
				if (selection.size() > 1) {
					Object[] test = selection.toArray();						
					Object matchedElement = null;
					for (Object item: test) {																	
						if (equals(selectedData,item)) {
							matchedElement = item;
							break;
						}
					}						
					if (matchedElement != null) {
						
						extendedSelection = new ArrayList<ClipboardContentDescriptor>();
						extendedSelection.add(new ClipboardContentDescriptor(matchedElement, ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT));
						for (int i = 0; i < test.length; i++) {
							Object selContent = test[i];
							if (selContent instanceof IContentData && selContent != matchedElement) {
								ClipboardContentDescriptor ccd = new ClipboardContentDescriptor(selContent, ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT);
								extendedSelection.add(ccd);
							}
						}
					}
				}
			} else
				selectedData = null;
		}					
		
		if (selectedData != null) {
					
				if (transfer == LocalSelectionTransfer.getInstance()) {
				
					List<ClipboardContentDescriptor> dragContent = null; 
					
					if (extendedSelection == null) {					
						dragContent = new ArrayList<ClipboardContentDescriptor>();
						ClipboardContentDescriptor ccd = new ClipboardContentDescriptor(selectedData, ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT);				
						dragContent.add(ccd);
					} else {
						dragContent = extendedSelection;
					}
														
					eventDataLocal = new StructuredSelection(dragContent);
					eventDetail = DND.DROP_COPY;
					LocalSelectionTransfer.getInstance().setSelection((ISelection) eventDataLocal);
					LocalSelectionTransfer.getInstance().setSelectionSetTime(event.time & 0xFFFF);
										
				} else if (transfer instanceof FileTransfer) {
					
					if (extendedSelection == null) {
						extendedSelection = new ArrayList<ClipboardContentDescriptor>();
						extendedSelection.add(new ClipboardContentDescriptor(selectedData, ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT));
					}
					
					Clipboard clip = new Clipboard("");
					List<String> result = new ArrayList<String>();
					for (ClipboardContentDescriptor cc: extendedSelection) {
						selectedData = cc.getContent();						
						CopyImageAction copyAction = new CopyImageAction(new SimpleSelectionProvider(selectedData), clip);
						copyAction.setSilent(true);
						if (copyAction.isEnabled())
							copyAction.run();						
						Object possibleFile = ClipboardHelper.getSupportedClipboardContent(clip);
						if (possibleFile instanceof List) {						
							List files = (List) possibleFile;							
							for (Object item: files) {
								if (item instanceof File)
									result.add(((File)item).getAbsolutePath());
							}							
						}
					}
					if (result.size() > 0) {
						eventData = result.toArray(new String[result.size()]);							
						eventDetail = DND.DROP_COPY;							
					}
				}				
		}
	}

	public void dragSetData(DragSourceEvent event) {			
		if (transfer instanceof FileTransfer && eventData != null) {
			event.data = eventData;
			event.detail = eventDetail;
			((FileTransfer)transfer).javaToNative(event.data, transfer.getSupportedTypes()[0]);
		}	
		if (transfer instanceof LocalSelectionTransfer && eventDataLocal != null) {
			event.data = eventDataLocal;
			event.detail = eventDetail;
		}			
	}
	
	private boolean equals(Object a, Object b) {
		if (a instanceof IContentData && b instanceof IContentData) {
			IContentData aData = (IContentData) a;
			IContentData bData = (IContentData) b;
			return (aData.getId().equals(bData.getId()));
		}
		return false;
	}
}