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

import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;
import com.nokia.tools.media.utils.timeline.impl.TimeLineNode;

/**
 * 
 * TimeLineNode implementation with effect-specific features
 */
public class BaseTimeLineNode extends TimeLineNode {

	public BaseTimeLineNode(ITimeLineRow row) {
		super(row, 0, 0);
	}
	
	@Override
	public void mouseDoubleClick(int button, int x, int y, long time) {
		BaseTimeLineRow row = (BaseTimeLineRow) getRow();
		ILayerEffect e = (ILayerEffect) row.getSource();
		
		if (e.isAnimated())
			super.mouseDoubleClick(button, x, y, time);
	}
	
	@Override
	public IControlPointModel getControlPointModel() {
		BaseTimeLineRow row = (BaseTimeLineRow) getRow();
		ILayerEffect e = (ILayerEffect) row.getSource();
		
		if (e.isAnimated())
			return super.getControlPointModel();
		return null;
	}
	
	@Override
	protected Action getCreateControlPointAction() {
		return ((BaseTimeLineRow) getRow()).getCreateControlPointAction();
	}
	
}
