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

import java.util.Iterator;
import java.util.Map;

import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineNode;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectParameter;


/**
 * 
 * TimeLineNode for Relative timing model, 
 * length of node is fixed, based on time span and cannot be changed
 */
public class RelativeTimeLineNode extends BaseTimeLineNode {
	
	private EffectObject effect;
	private TimeSpan timespan;

	public RelativeTimeLineNode(ITimeLineRow row, EffectObject effect, TimeSpan span) {
		super(row);
		this.effect = effect;
		timespan = span;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public long getEndTime() {					
		//if at least one parameter has relative t.m. and timespan, return
		Map pars = effect.getParameterList();
		Iterator x = pars.values().iterator();
		long maxDur = 0;
		while (x.hasNext()) {
			EffectParameter p = (EffectParameter) x.next();
			if (timespan == p.getTimeSpan()) {
				long dur = p.getAnimationDuration(TimingModel.Relative);
				maxDur = dur > maxDur ? dur : maxDur;
			}
		}
		return maxDur;
	}
	
	@Override
	public void mouseDoubleClick(int button, int x, int y, long time) {
		if (time > getEndTime())
			return;
		super.mouseDoubleClick(button, x, y, time);
	}
	
	@Override
	public IControlPointModel getControlPointModel() {
		if (effect.isAnimated())
			return super.getControlPointModel();
		return null;
	}	
	
	@Override
	public void mouseMove(int x, int y, long time) {
		if (time > getEndTime())
			super.mouseMove(x,y,getEndTime());
		else 
			super.mouseMove(x,y,time);
	}
	
}
