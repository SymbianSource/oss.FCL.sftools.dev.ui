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

import com.nokia.tools.media.utils.timeline.ITimeLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineGridLabelProvider;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.media.utils.timeline.IZoomProvider;

public abstract class BaseTimeLineProvider implements ITreeTimeLineDataProvider {
	
	protected static final int ANIMATION_STEP_DURATION = 125;
	protected static final int DEF_ANIM_STEPS = 40;

	protected long animTime;
	protected int stepSize;
	protected boolean showAnimatedOnly;
	
	public BaseTimeLineProvider(long animTime) {		
		this.animTime = animTime;
		stepSize = (int) (animTime / DEF_ANIM_STEPS);
	}

	public long getEndTime() {
		return 0;
	}

	public long getStartTime() {
		return 0;
	}

	public long getDisplayStart() {
		return getStartTime();
	}

	public long getDisplayWidth() {
		return animTime;
	}

	public long getInitialTime() {
		return getStartTime();
	}

	public boolean getShowGrid() {
		return true;
	}

	public boolean getShowGridHeader() {
		return true;
	}

	public int getMajorGridInterval() {
		return (int) (stepSize * 2);
	}

	public int getMinorGridInterval() {
		return (int) (stepSize / 2);
	}

	public int getClockIncrement() {		
		return (int) stepSize;		
	}

	public int getClockTimePerIncrement() {
		return (int) stepSize;
	}

	public ITimeLabelProvider getTimeLabelProvider() {
		return TimeLabelProvider.instanceRealtime;
	}

	public ITimeLineGridLabelProvider getGridLabelProvider() {
		return TimeLabelProvider.instanceRealtime;
	}

	public boolean getClockAutorepeat() {
		return false;
	}

	public IZoomProvider getZoomProvider() {
		return new SimpleZoomProvider();
	}
	
	public void setShowAnimatedOnly(boolean b) {
		showAnimatedOnly = b;
		
	}

}