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

package com.nokia.tools.ui.dnd;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;

/**
 * 
 * Drag and Drop adapter class
 *
 */
public abstract class FileDropListener
    implements TransferDropTargetListener {
	private DropTargetEvent currentEvent;

	private boolean showingFeedback;

	private EditPart target;

	private Transfer transfer;

	protected EditPartViewer viewer;

	private SelectionRequest request = new SelectionRequest();

	/**
	 * Extension for svg.
	 */
	protected final static String FILE_EXT_SVG = "svg";

	public FileDropListener(EditPartViewer viewer, Transfer transfer) {
		super();
		this.transfer = transfer;
		this.viewer = viewer;
	}

	protected EditPart calculateTargetEditPart() {
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

	protected void updateTargetRequest() {
		request.setLocation(getDropLocation());
	}

	/**
	 * Handles drop of given image to target EP through Command & Command Stack
	 */
	protected abstract void handleDrop(); 


	protected void handleEnteredEditPart() {
	}

	protected void handleExitingEditPart() {
		eraseTargetFeedback();
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
}
