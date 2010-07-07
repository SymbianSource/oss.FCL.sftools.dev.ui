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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.IGridSettings;
import com.nokia.tools.media.utils.timeline.ITimeLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineGridLabelProvider;

/**
 * common label and grid provider - simply display number = miliseconds.
 *
 */
public class TimeLabelProvider implements ITimeLabelProvider, ITimeLineGridLabelProvider {
	
	public static TimeLabelProvider instanceRealtime = new TimeLabelProvider(TimingModel.RealTime, null);
	
	private SimpleDateFormat df = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat week_df = new SimpleDateFormat("E HH:mm");
	private SimpleDateFormat month_df = new SimpleDateFormat("dd.MM HH:mm");
	
	private static TimeZone gmt = TimeZone.getTimeZone("GMT");
	
	private TimingModel mode;
	private TimeSpan span;

	private boolean addUnitPostfix;
	
	public TimeLabelProvider(TimingModel m, TimeSpan span) {
		this.mode = m;
		this.span = span;
	}
	
	public String getLabel(long time) {
		if (mode == TimingModel.RealTime)
			return Long.toString(time) + (addUnitPostfix ? " ms" : "");
		else if (mode == TimingModel.Relative) {
			if (df.getTimeZone() != gmt) {
				df.setTimeZone(gmt);
				week_df.setTimeZone(gmt);
				month_df.setTimeZone(gmt);
			}
			if (span == TimeSpan.EWeek)
				return week_df.format(new Date(time));
			if (span == TimeSpan.EMonth)
				return month_df.format(new Date(time));
			return df.format(new Date(time));
		}
		return null;
	}

	public long parse(String label) {
		if (mode == TimingModel.RealTime) {
			try {
				return new Long(label);
			} catch (NumberFormatException e) {
				throw new NumberFormatException("Not a number");
			}
		} else if (mode == TimingModel.Relative) {
			if (df.getTimeZone() != gmt) {
				df.setTimeZone(gmt);
				week_df.setTimeZone(gmt);
				month_df.setTimeZone(gmt);
			}
			try {
				if (span == TimeSpan.EWeek) {
					return week_df.parse(label).getTime();
				} else if (span == TimeSpan.EMonth) {
					return month_df.parse(label).getTime();
				} else {
					return df.parse(label).getTime();
				}
			} catch (ParseException e) {
				if (span == TimeSpan.EWeek) {
					throw new NumberFormatException(
							"Not a valid date/time (d hh:mm)");
				} else if (span == TimeSpan.EMonth) {
					throw new NumberFormatException(
							"Not a valid date/time (dd.mm hh:mm)");
				} else {
					throw new NumberFormatException(
							"Not a valid date/time (hh:mm)");
				}
			}
		}
		return 0;
	}

	public Color getBackground() {
		return ColorConstants.white;
	}

	public String getLabel(long time, IGridSettings gridData) {
		gridData.setGridInterval(gridData.getDisplayData()
				.getDisplayWidthInTime() / 10, gridData
				.getDisplayData().getDisplayWidthInTime() / 50);
		if (time % gridData.getMajorGridInterval() == 0)
			return time + "";
		return null;
	}

	public void setAddUnitPostfix(boolean b) {
		addUnitPostfix = b;
	}
}
