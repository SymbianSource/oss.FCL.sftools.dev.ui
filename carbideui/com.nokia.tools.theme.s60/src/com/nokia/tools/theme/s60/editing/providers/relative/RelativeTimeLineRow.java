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
package com.nokia.tools.theme.s60.editing.providers.relative;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IInputValidator;

import com.nokia.tools.media.utils.editor.Messages;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.timeline.ITimeLineNode;
import com.nokia.tools.media.utils.timeline.impl.TimeLine;
import com.nokia.tools.media.utils.timeline.impl.TimeLineNode;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineRow;
import com.nokia.tools.theme.s60.effects.EffectObject;

/**
 * 
 * Timeline row implementation with effect-specific features
 */
public class RelativeTimeLineRow extends BaseTimeLineRow {
	
	public static final String CREATE_CP_ACTION_ID = "CreateCPRelative";
	private TimeSpan tSpan;
	
	public RelativeTimeLineRow(Object source, TimeSpan timespan) {
		super(source);
		this.tSpan = timespan;
		if (((EffectObject) source).isAnimatedFor(timespan)) {
			RelativeTimeLineNode node = new RelativeTimeLineNode(this,
					(EffectObject) source, timespan);
			node.setControlPointModel(((EffectObject) source)
					.getControlPointModelWrapper());
			addNode(node);
		}
	}

	@Override
	public void contributeToContextMenu(IMenuManager menuManager) {
		super.contributeToContextMenu(menuManager);

		if (menuManager.find(CREATE_CP_ACTION_ID) == null) {
			Action createAction = getCreateControlPointAction();
			if (createAction != null) {
				menuManager.add(createAction);
			}
		}
	}

	protected Action getCreateControlPointAction() {
		TimeLine timeLine = (TimeLine) getTimeLine();
		if (timeLine != null) {
			return new CreateControlPointAction(this);
		}
		return null;
	}

	class CreateControlPointAction extends BaseCreateControlPointAction {
		
		public CreateControlPointAction(RelativeTimeLineRow row) {
			super(Messages.CreateCP_label, row);
			setId(CREATE_CP_ACTION_ID);		
		}

		@Override
		public void run() {
			final TimeLineNode node = (TimeLineNode) row.getNodes()[0];
			validator = new IInputValidator() {
				public String isValid(String newText) {
					try {
						long time = 0;
						String startTime, endTime;
						if (timeLine.getTimeLabelProvider() != null) {
							time = timeLine.getTimeLabelProvider().parse(
									newText);
							startTime = timeLine.getTimeLabelProvider()
									.getLabel(node.getStartTime());
							endTime = timeLine.getTimeLabelProvider().getLabel(
									node.getEndTime());
						} else {
							time = Long.parseLong(newText);
							startTime = "" + node.getStartTime();
							endTime = "" + node.getEndTime();
						}

						if (time > node.getEndTime()) {
							throw new Exception(Messages.bind(Messages.CreateCPAction_msg_invalidTime, startTime, endTime));
						}
					} catch (NumberFormatException nfe) {
						return Messages.CreateCPAction_msg_NAN;
					} catch (Exception ex) {
						return ex.getMessage();
					}
					return null;
				};
			};
			super.run();
		}

		@Override
		public boolean isEnabled() {
			if (row.getNodes().length == 0) {
				return false;
			}

			ITimeLineNode node = row.getNodes()[0];

			return node.getStartTime() <= lastMouseDownTime
					&& node.getEndTime() >= lastMouseDownTime
					&& row.getNodes()[0].getControlPointModel() != null;
		}
	}

}
