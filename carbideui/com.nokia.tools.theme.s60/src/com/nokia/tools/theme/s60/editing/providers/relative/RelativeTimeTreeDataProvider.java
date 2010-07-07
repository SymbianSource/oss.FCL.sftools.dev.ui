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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.IDisplaySettings;
import com.nokia.tools.media.utils.timeline.IGridSettings;
import com.nokia.tools.media.utils.timeline.ITimeLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineGridLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeLineTreeContentProvider;
import com.nokia.tools.media.utils.timeline.IZoomProvider;
import com.nokia.tools.media.utils.timeline.impl.TimeLineRow;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.editing.providers.BaseTimeLineProvider;
import com.nokia.tools.theme.s60.editing.providers.LayerTimeLineNode;
import com.nokia.tools.theme.s60.editing.providers.TimeLabelProvider;
import com.nokia.tools.theme.s60.editing.providers.TimeLineTreeContentProvider;
import com.nokia.tools.theme.s60.effects.EffectObject;
import com.nokia.tools.theme.s60.effects.EffectParameter;

public class RelativeTimeTreeDataProvider extends BaseTimeLineProvider {

	private EditableEntityImage img;
	private TimeSpan timeSpan;
	private IDisplaySettings display;

	public RelativeTimeTreeDataProvider(EditableEntityImage img, TimeSpan span) {
		super((long) (EffectParameter.getDurationFor(span)));
		this.img = img;		
		this.timeSpan = span;
	}

	public ITimeLineTreeContentProvider getTreeContentProvider() {
		return new TimeLineTreeContentProvider(TimingModel.Relative, timeSpan,
				showAnimatedOnly);
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

	/**
	 * default display width
	 */
	public long getDisplayWidth() {
		return (long) EffectParameter.getDurationFor(timeSpan);
	}

	public int getMajorGridInterval() {
		
		if (display != null) {			
			return (int) (display.getDisplayWidthInTime() / getMajorIntervalCount(timeSpan));
		} else {
			return (int) (getDisplayWidth() / getMajorIntervalCount(timeSpan));
		}
	}

	public int getMinorGridInterval() {
		return getMajorGridInterval() / 4;
	}

	public int getClockIncrement() {
		stepSize = (int) (Math.max(1000l, img.getAnimationDuration(timeSpan)) / DEF_ANIM_STEPS);
		return (int) stepSize;
	}
	
	@Override
	public int getClockTimePerIncrement() {
		return ANIMATION_STEP_DURATION;
	}

	public Object getInput() {
		return img;
	}

	TimeZone gmt = TimeZone.getTimeZone("GMT");

	public ITimeLabelProvider getTimeLabelProvider() {
		return new TimeLabelProvider(TimingModel.Relative, timeSpan);
	}
	
	private int getMajorIntervalCount(TimeSpan span) {
		if (timeSpan == TimeSpan.EHour) {
			return 6;
		}
		if (timeSpan == TimeSpan.EDay) {
			return 6;
		}
		if (timeSpan == TimeSpan.EWeek) {
			return 7; //seven days
		}
		//if (timeSpan == TimeSpan.EMonth)
		return 4; //four weeks	
	}

	public ITimeLineGridLabelProvider getGridLabelProvider() {
		
		return new ITimeLineGridLabelProvider() {

			public String getLabel(long time, IGridSettings gridData) {
				
				display = gridData.getDisplayData();												
				int gridInterval = getMajorGridInterval();
				
				if (gridInterval != gridData.getMajorGridInterval()) {
					gridData.setGridInterval(getMajorGridInterval(), getMinorGridInterval());
				}
				
				while (gridData.getDisplayData().getDisplayWidthInTime() / gridInterval > 8)
					gridInterval *= 2;
				
				while (gridData.getDisplayData().getDisplayWidthInTime() / gridInterval < 6) {
					gridInterval = gridInterval / 2;
					if (gridInterval < 1) {
						gridInterval = 1;
						break;
					}
				}				
				
				if (time % gridInterval == 0) {
					if (timeSpan == TimeSpan.EHour) {
						DateFormat df = new SimpleDateFormat("HH:mm");
						df.setTimeZone(gmt);
						return df.format(new Date(time));
					}
					if (timeSpan == TimeSpan.EDay) {
						DateFormat df = new SimpleDateFormat("HH:mm");
						df.setTimeZone(gmt);
						return df.format(new Date(time));
					}
					if (timeSpan == TimeSpan.EWeek) {
						String format = "E";
						if (gridData.getDisplayData().getDisplayWidthInTime() < EffectParameter.getDurationFor(timeSpan))
							format = "E HH:mm";
						DateFormat df = new SimpleDateFormat(format);
						df.setTimeZone(gmt);
						return df.format(new Date(time));
					}
					if (timeSpan == TimeSpan.EMonth) {
						DateFormat df = new SimpleDateFormat("dd.MM");
						df.setTimeZone(gmt);
						return df.format(new Date(time));
					}
				}
				return null;
			}

			public org.eclipse.swt.graphics.Color getBackground() {
				return ColorConstants.white;
			}
		};
	}

	public ITimeLineRow getRowForTreeElement(Object object) {
		if (object instanceof ILayer) {
			EditableImageLayer iml = (EditableImageLayer) object;
			if (iml.getTimeLineRow() == null) {
				TimeLineRow row = new TimeLineRow(iml);
				row.setLabel(iml.getName());
				row.setVisible(true);
				row.removeAllNodes();
				row.setCenterLineColor(ColorConstants.gray);
				// add node
				LayerTimeLineNode node = new LayerTimeLineNode(row, iml,
						timeSpan);
				row.addNode(node);
				iml.setTimeLineRow(row);
			}
			return iml.getTimeLineRow();
		}
		if (object instanceof ILayerEffect) {
			EffectObject obj = (EffectObject) object;

			if (obj.isAnimatedFor(timeSpan)) {
				if (obj.getTimeLineRow() == null) {
					obj
							.setTimeLineRow(new com.nokia.tools.theme.s60.editing.providers.relative.RelativeTimeLineRow(
									obj, timeSpan));
				}
				return obj.getTimeLineRow();
			} else {
				// add dummy row
				if (obj.getTimeLineRow() == null) {
					TimeLineRow dummy = new TimeLineRow(obj);
					dummy.setCenterLineColor(ColorConstants.lightGray);
					dummy.setShowCenterLine(false);
					obj.setTimeLineRow(dummy);
				}
				return obj.getTimeLineRow();
			}
		}
		return null;
	}

	protected TimeSpan getTimeSpan() {
		return timeSpan;
	}

	protected void setTimeSpan(TimeSpan timeSpan) {
		this.timeSpan = timeSpan;
	}

	public IZoomProvider getZoomProvider() {
		return new IZoomProvider() {
			
			public void zoomIn(IDisplaySettings data) {		
				long limit = EffectParameter.getDurationFor(timeSpan) / 10;
				long width = data.getDisplayWidthInTime();	
				width /= 2; 
				data.setDisplayWidthInTime(width > limit ? width : limit);
			}

			public void zoomOut(IDisplaySettings data) {
				long limit = EffectParameter.getDurationFor(timeSpan) * 2;
				long width = data.getDisplayWidthInTime();	
				width *= 2; 
				data.setDisplayWidthInTime(width < limit ? width : limit);
			}
		};
	}
}