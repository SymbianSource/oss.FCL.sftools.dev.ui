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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeLineTreeContentProvider;
import com.nokia.tools.media.utils.timeline.impl.TimeLineRow;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineProvider;
import com.nokia.tools.theme.s60.editing.providers.LayerTimeLineNode;
import com.nokia.tools.theme.s60.editing.providers.TimeLineTreeContentProvider;
import com.nokia.tools.theme.s60.effects.EffectObject;

public class RealTimeTreeDataProvider extends BaseTimeLineProvider {

	EditableEntityImage img;
	
	public RealTimeTreeDataProvider(EditableEntityImage img) {
		super(Math.max(1000l, img.getAnimationDuration(TimingModel.RealTime)));
		this.img = img;		
	}

	public ITimeLineTreeContentProvider getTreeContentProvider() {
		return new TimeLineTreeContentProvider(TimingModel.RealTime, null, showAnimatedOnly);
	}

	public ILabelProvider getTreeLabelProvider() {
		return new LabelProvider() {

			public String getText(Object element) {
				if (element instanceof ILayer) {
					return ((ILayer) element).getName();
				}
				if (element instanceof ILayerEffect) {
					return ((ILayerEffect) element).getName();
				}
				return "BAD!";
			}
		};
	}
	
	@Override
	public int getClockTimePerIncrement() {
		if (img.getAnimationDuration() >= 1000) {
			return 50;
		} else {
			return (int) ((1000f / img.getAnimationDuration()) * getClockIncrement());
		}
	}

	public int getClockIncrement() {
		if (img.getAnimationDuration() < 1000) {
			stepSize = (int) (Math.max(1000l, img.getAnimationDuration()) / 20);
			return (int) Math.max(1, img.getAnimationDuration() / 20);
		} else {
			return 50;
		}
	}

	public Object getInput() {
		return img;
	}

	public ITimeLineRow getRowForTreeElement(Object object) {
		if (object instanceof ILayer) {
			EditableImageLayer iml = (EditableImageLayer) object;
			if (iml.getTimeLineRow() == null)
			{
				TimeLineRow row = new TimeLineRow(iml);
				row.setLabel(iml.getName());
				row.setVisible(true);
				row.removeAllNodes();
				row.setShowCenterLine(true);
				row.setCenterLineColor(ColorConstants.gray);
				//add node
				LayerTimeLineNode node = new LayerTimeLineNode(row, iml);
				row.addNode(node);
				iml.setTimeLineRow(row);
			}
			return iml.getTimeLineRow();
		}
		if (object instanceof ILayerEffect) {
			EffectObject obj = (EffectObject) object;
			if (obj.isAnimatedFor(TimingModel.RealTime)) 
			{
				if (obj.getTimeLineRow() == null)
				{
					obj.setTimeLineRow(new RealtimeTimeLineRow(obj));
					((RealtimeTimeLineRow)obj.getTimeLineRow()).setCenterLineColor(ColorConstants.black);
				}
			} else {
				//add dummy row - cannot be animated
				if (obj.getTimeLineRow() == null)
				{
					TimeLineRow dummy = new TimeLineRow(obj);
					dummy.setCenterLineColor(ColorConstants.lightGray);
					dummy.setShowCenterLine(false);					
					obj.setTimeLineRow(dummy);
				}
			}
			return obj.getTimeLineRow();
		}
		return null;
	}

}