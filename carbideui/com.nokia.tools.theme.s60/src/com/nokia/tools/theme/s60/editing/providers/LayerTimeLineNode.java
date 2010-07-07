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

import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.effects.EffectObject;

/**
 * Layer - specific TimeLineNode implementation with 
 * special behaviour - length of node is determined as maximum animation length of 
 * underlying effects
 */
public class LayerTimeLineNode extends
		com.nokia.tools.media.utils.timeline.impl.TimeLineNode {

	private EditableImageLayer layer;
	private TimeSpan span;

	/**
	 * LayerTimeLineNode for relative time span
	 * @param timeLineRow
	 * @param layer
	 * @param sp
	 */
	public LayerTimeLineNode(ITimeLineRow timeLineRow, EditableImageLayer layer, TimeSpan sp) {
		super(timeLineRow, 0, 0);
		this.layer = layer;
		this.span = sp;
	}
	
	/**
	 * Create node for realtime timespan
	 * @param timeLineRow
	 * @param layer
	 */
	public LayerTimeLineNode(ITimeLineRow timeLineRow, EditableImageLayer layer) {
		super(timeLineRow, 0, 0);
		this.layer = layer;
	}
	
	@Override
	public long getStartTime() {
		return 0;
	}
	
	@Override
	public long getEndTime() {
		//return maximum duration of underl. effects, only for realTime
		long max = 0;
		java.util.List<ILayerEffect> effects = layer.getLayerEffects();
		for (int i = 0; i < effects.size(); i++) {
			EffectObject eo = (EffectObject) effects.get(i);
			if (eo.isSelected()) {
				if (span != null) {
				if (eo.isAnimatedFor(TimingModel.Relative)) {
					long m = eo.getAnimationDuration(span);
					if (m > max)
						max = m;
				}
				} else {
					if (eo.isAnimatedFor(TimingModel.RealTime)) {
						long m = eo.getAnimationDuration();
						if (m > max)
							max = m;
					}
				}
			}
		}
		return max;
	}	
	
}
