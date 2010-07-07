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

import java.awt.Color;
import java.awt.datatransfer.Clipboard;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AddToGroupAction;
import com.nokia.tools.s60.editor.actions.CopyGraphicsAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.PasteGraphicsAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.DraggedColorObject;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;

public class FileDropAdapter implements TransferDropTargetListener {

	private DropTargetEvent currentEvent;

	private boolean showingFeedback;

	private EditPart target;

	private Transfer transfer;

	private EditPartViewer viewer;

	private SelectionRequest request = new SelectionRequest();

	public FileDropAdapter(EditPartViewer viewer, Transfer t) {
		this.viewer = viewer;
		transfer = t;
	}

	private EditPart calculateTargetEditPart() {
		return viewer.findObjectAt(getDropLocation());
	}

	public void dragEnter(DropTargetEvent event) {
		setCurrentEvent(event);
	}

	public void dragLeave(DropTargetEvent event) {
		setCurrentEvent(event);
		unload();
	}

	public void dragOperationChanged(DropTargetEvent event) {
		setCurrentEvent(event);
		handleDragOperationChanged();
	}

	public void dragOver(DropTargetEvent event) {
		setCurrentEvent(event);
		request.setLocation(getDropLocation());
		handleDragOver();
	}

	public void drop(DropTargetEvent event) {
		setCurrentEvent(event);
		eraseTargetFeedback();
		handleDrop();
		unload();
	}

	public void dropAccept(DropTargetEvent event) {
		setCurrentEvent(event);
	}

	protected void eraseTargetFeedback() {
		if (getTargetEditPart() != null && showingFeedback) {
			showingFeedback = false;
			getTargetEditPart().eraseTargetFeedback(request);
		}
	}

	public DropTargetEvent getCurrentEvent() {
		return currentEvent;
	}

	protected Point getDropLocation() {
		org.eclipse.swt.graphics.Point swt;
		swt = new org.eclipse.swt.graphics.Point(getCurrentEvent().x,
				getCurrentEvent().y);
		DropTarget target = (DropTarget) getCurrentEvent().widget;
		swt = target.getControl().toControl(swt);
		return new Point(swt.x, swt.y);
	}

	protected EditPart getTargetEditPart() {
		return target;
	}

	public Transfer getTransfer() {
		return transfer;
	}

	protected void handleDragOperationChanged() {
		eraseTargetFeedback();
		updateTargetRequest();
		setTargetEditPart(calculateTargetEditPart());
	}

	protected void handleDragOver() {
		updateTargetRequest();
		setTargetEditPart(calculateTargetEditPart());
		showTargetFeedback();
	}

	private void updateTargetRequest() {
		request.setLocation(getDropLocation());
	}

	/**
	 * Handles drop of given image to target EP through Command & Command Stack
	 */
	protected void handleDrop() {

		EditPart ep = getTargetEditPart();
		if (ep == null)
			ep = calculateTargetEditPart();
		Object data = getCurrentEvent().data;

		IScreenElement scrEl = JEMUtil.getScreenElement(ep);

		if (scrEl != null
				&& scrEl.getData().getAdapter(ISkinnableEntityAdapter.class) != null) {

			if (data instanceof IStructuredSelection)
				data = ((IStructuredSelection) data).getFirstElement();

			/* clipboard content descriptor support */
			if (data instanceof ClipboardContentDescriptor) {
				ClipboardContentDescriptor cDesc = (ClipboardContentDescriptor) data;
				if (cDesc.getType() == ContentType.CONTENT_ELEMENT) {

					Object source = cDesc.getContent();

					Clipboard clip = new Clipboard("");
					ISelectionProvider provider = new SimpleSelectionProvider(
							source);

					if (source instanceof IContentData) {
						// content data - from component store, component view
						AbstractAction copy = new CopyGraphicsAction(provider,
								clip);
						if (copy.isEnabled()) {
							copy.run();
							PasteGraphicsAction paste = new PasteGraphicsAction(
									new SimpleSelectionProvider(ep), ep
											.getViewer().getEditDomain()
											.getCommandStack(), clip);
							if (paste.isEnabled()) {
								paste.run();
								return;
							}
						}
					}

					
					// selection from layers view
					CopyImageAction copy = new CopyImageAction(provider, clip);
					if (copy.isEnabled()) {
						copy.run();
						PasteImageAction paste = new PasteImageAction(
								new SimpleSelectionProvider(ep), ep.getViewer()
										.getEditDomain().getCommandStack(),
								clip);
						if (paste.isEnabled()) {
							paste.run();
							return;
						}
					}

				}
			} else if (data instanceof DraggedColorObject) {

				DraggedColorObject obj = (DraggedColorObject) data;

				if (scrEl.getData().getAdapter(ISkinnableEntityAdapter.class) != null) {
					ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) scrEl
							.getData()
							.getAdapter(ISkinnableEntityAdapter.class);

					ISelectionProvider provider = null;

					provider = new SimpleSelectionProvider(adapter
							.getContentData());
					IColorAdapter colorAdapter = (IColorAdapter) adapter
							.getContentData().getAdapter(IColorAdapter.class);
					if (colorAdapter != null) {
						Color oldColor = colorAdapter.getColor();
						AddToGroupAction action = new AddToGroupAction(
								new StructuredSelection(adapter
										.getContentData()), provider, null);

						
						action.setColor(ColorUtil.getRGB(ColorUtil
								.asHashString(oldColor)));
						action.setGroupName(obj.getName());
						action.setColorGroups(obj.getGrps());
						action.run();

					}
				}
			}

			Object dropDataCandidate = null;
			dropDataCandidate = S60BaseDropListener.unifyDropData(data);

			if (dropDataCandidate instanceof Clipboard) {

				PasteImageAction paste = new PasteImageAction(
						new SimpleSelectionProvider(scrEl.getData()), ep
								.getViewer().getEditDomain().getCommandStack(),
						(Clipboard) dropDataCandidate);
				try {
					if (paste.isEnabled())
						paste.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void handleEnteredEditPart() {
	}

	protected void handleExitingEditPart() {
		eraseTargetFeedback();
	}

	public boolean isEnabled(DropTargetEvent event) {
		boolean result = false;

		for (int i = 0; i < event.dataTypes.length; i++) {
			if (getTransfer().isSupportedType(event.dataTypes[i])) {
				setCurrentEvent(event);
				event.currentDataType = event.dataTypes[i];
				updateTargetRequest();
				setTargetEditPart(calculateTargetEditPart());
				if (getTargetEditPart() != null) {

					EditPart ep = getTargetEditPart();

					IScreenElement scrEl = JEMUtil.getScreenElement(ep);
					if (scrEl != null
							&& scrEl.getData().getAdapter(
									ISkinnableEntityAdapter.class) != null) {
						ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) scrEl
								.getData().getAdapter(
										ISkinnableEntityAdapter.class);
						IMediaFileAdapter mediaAdapter = (IMediaFileAdapter) scrEl
								.getData().getAdapter(IMediaFileAdapter.class);
						if ((mediaAdapter != null && !mediaAdapter.isSound())
								|| (adapter != null && !adapter.isColour()
										&& !adapter.isMultiPiece() && !objectIsDraggedColorObject())) {
							// from all views and editor
							// layer is added automatically during operation
							result = true;
							break;
						} else if (adapter.isColour()
								&& objectIsDraggedColorObject()) {
							result = true;
							break;
						}
					}
				}
			}
		}
		if (result) {
			getCurrentEvent().detail = DND.DROP_COPY;
		} else {
			getCurrentEvent().detail = DND.DROP_NONE;
		}
		return result;
	}

	public void setCurrentEvent(DropTargetEvent currentEvent) {
		this.currentEvent = currentEvent;
	}

	protected void setTargetEditPart(EditPart ep) {
		if (ep != target) {
			if (target != null)
				handleExitingEditPart();
			target = ep;
			if (target != null)
				;
			handleEnteredEditPart();
		}
	}

	protected void showTargetFeedback() {
		if (getTargetEditPart() != null) {
			showingFeedback = true;
			// mark as dnd command request
			// see SComponentGraphicalEditPart.createEditPolicies()
			request.setType("DropSelectionRequest");
			getTargetEditPart().showTargetFeedback(request);
		}
	}

	protected void unload() {
		eraseTargetFeedback();
		setTargetEditPart(null);
		setCurrentEvent(null);
	}

	private boolean objectIsDraggedColorObject() {
		if (transfer instanceof LocalSelectionTransfer) {
			LocalSelectionTransfer trans = (LocalSelectionTransfer) transfer;
			ISelection selection = trans.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection strSelection = (IStructuredSelection) selection;
				Object data = strSelection.getFirstElement();

				if (data instanceof DraggedColorObject) {
					return true;
				}
			}
		}
		return false;

	}

}
