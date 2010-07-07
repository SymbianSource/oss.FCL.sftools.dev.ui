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
package com.nokia.tools.theme.s60.editing.providers.realtime;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.IInputValidator;

import com.nokia.tools.media.utils.editor.Messages;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.impl.TimeLine;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineRow;
import com.nokia.tools.theme.s60.effects.EffectObject;

/**
 * 
 * Timeline row implementation with effect-specific features
 */
public class RealtimeTimeLineRow extends BaseTimeLineRow {
	
	public static final String CREATE_CP_ACTION_ID = "CreateCPRealtime";

	public RealtimeTimeLineRow(Object source) {
		super(source);

		if (((EffectObject) source).isAnimatedFor(TimingModel.RealTime)) {
			RealtimeTimeLineNode node = new RealtimeTimeLineNode(this);
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

	public class CreateControlPointAction extends BaseCreateControlPointAction {		

		public CreateControlPointAction(RealtimeTimeLineRow row) {
			super(Messages.CreateCP_label, row);			
			setId(CREATE_CP_ACTION_ID);
			validator = new IInputValidator() {
				public String isValid(String newText) {
					try {
						if (timeLine.getTimeLabelProvider() != null) {
							timeLine.getTimeLabelProvider().parse(newText);
						} else {
							Long.parseLong(newText);
						}
					} catch (NumberFormatException nfe) {
						return Messages.CreateCPAction_msg_NAN;
					} catch (Exception ex) {
						return ex.getMessage();
					}
					return null;
				};
			};
		}

		@Override
		public boolean isEnabled() {
			return row.getNodes().length == 1
					&& row.getNodes()[0].getControlPointModel() != null;
		}
	}
}
