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
package com.nokia.tools.theme.s60.editing.providers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;

import com.nokia.tools.media.utils.editor.Messages;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.impl.TimeLine;
import com.nokia.tools.media.utils.timeline.impl.TimeLineNode;
import com.nokia.tools.media.utils.timeline.impl.TimeLineRow;

/**
 * 
 * Timeline row implementation with effect-specific features
 */
public abstract class BaseTimeLineRow extends TimeLineRow {

	protected long lastMouseDownTime;
	
	protected class BaseCreateControlPointAction extends Action {
		
		protected BaseTimeLineRow row;	
		protected TimeLine timeLine;
		protected IInputValidator validator;
	
		public BaseCreateControlPointAction(String label, BaseTimeLineRow row) {
			super(label);
			this.row = row;
			this.timeLine = (TimeLine) row.getTimeLine();		
		}
	
		@Override
		public void run() {
			final TimeLineNode node = (TimeLineNode) row.getNodes()[0];
					
			String mouseDownTime;
	
			if (timeLine.getTimeLabelProvider() != null) {
				mouseDownTime = timeLine.getTimeLabelProvider().getLabel(
						lastMouseDownTime);
			} else {
				mouseDownTime = Long.toString(lastMouseDownTime);
			}
	
			InputDialog dialog = new InputDialog(timeLine.getShell(),
					Messages.CreateCPAction_msg_createCP, Messages.CreateCPAction_msg_enterTime, mouseDownTime,
					validator);
	
			if (dialog.open() == Dialog.OK) {
				try {
					long time = 0;
					if (timeLine.getTimeLabelProvider() != null) {
						time = timeLine.getTimeLabelProvider().parse(
								dialog.getValue());
					} else {
						time = Long.parseLong(dialog.getValue());
					}
	
					IControlPoint newControlPoint = node.getControlPointModel()
							.createControlPoint(time);
					node.selectControlPoint(newControlPoint);
					timeLine.notifySelectionListeners();
					timeLine.repaint();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public BaseTimeLineRow(Object source) {
		super(source);
	}

	@Override
	public void mouseDoubleClick(int button, int x, int y, long time) {
		notifyDoubleClickListeners(time);
		BaseTimeLineNode node = (BaseTimeLineNode) nodes.first();
		node.mouseDoubleClick(button, x, y, time);
	}

	@Override
	public void mouseDown(int button, int x, int y, long time) {
		super.mouseDown(button, x, y, time);
		lastMouseDownTime = time;
	}

	@Override
	public void mouseMove(int x, int y, long time) {
		BaseTimeLineNode node = (BaseTimeLineNode) nodes.first();
		node.mouseMove(x, y, time);
	}

	@Override
	public void mouseUp(int button, int x, int y, long time) {
		super.mouseUp(button, x, y, time);
	}

	protected abstract Action getCreateControlPointAction();

}
