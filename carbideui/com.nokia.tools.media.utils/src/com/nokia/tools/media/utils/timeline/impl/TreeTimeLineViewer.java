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

package com.nokia.tools.media.utils.timeline.impl;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.media.utils.timeline.ITimeLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineGridLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineTreeContentProvider;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;

public class TreeTimeLineViewer {

	protected Composite parent;

	protected TreeTimeLine timeLine;

	public TreeTimeLineViewer(Composite parent, int style,
			ITreeTimeLineDataProvider dataProvider) {
		this.parent = parent;
		this.timeLine = new TreeTimeLine(parent, style, dataProvider);
	}

	public TreeTimeLineViewer(Composite parent, int style) {
		this.parent = parent;
		this.timeLine = new TreeTimeLine(parent, style);
	}

	public TreeTimeLineViewer(Composite parent, int style, long startTime,
			long endTime, long displayWidth) {
		this.parent = parent;
		this.timeLine = new TreeTimeLine(parent, style, startTime, endTime,
				displayWidth);
	}

	public TreeTimeLineViewer(Composite parent, int style, long startTime,
			long endTime, long displayWidth,
			ITimeLabelProvider timeLabelProvider,
			ITimeLineGridLabelProvider gridLabelProvider) {
		this.parent = parent;
		this.timeLine = new TreeTimeLine(parent, style, startTime, endTime,
				displayWidth, timeLabelProvider, gridLabelProvider);
	}

	public void setInput(Object input) {
		timeLine.setInput(input);
		
		Runnable runnanble = new Runnable() {
			public void run() {
				timeLine.redraw();
			};
		};
		
		if (Display.getCurrent() != null) {
			runnanble.run();
		} else {
			Display.getDefault().asyncExec(runnanble);
		}
	}

	public void setTreeContentProvider(ITimeLineTreeContentProvider provider) {
		timeLine.setTreeContentProvider(provider);
	}

	public void setTreeLabelProvider(ILabelProvider provider) {
		timeLine.setTreeLabelProvider(provider);
	}

	public TreeTimeLine getTimeLine() {
		return timeLine;
	}

	public void collapseAll() {
		timeLine.getTreeViewer().collapseAll();
		timeLine.synchronizeRowsWithTree();
	}

	public void expandAll() {
		timeLine.getTreeViewer().expandAll();
		timeLine.synchronizeRowsWithTree();
	}

	public void expandToLevel(int level) {
		timeLine.getTreeViewer().expandToLevel(level);
		timeLine.synchronizeRowsWithTree();
	}

	public void expandToLevel(Object element, int level) {
		timeLine.getTreeViewer().expandToLevel(element, level);
		timeLine.synchronizeRowsWithTree();
	}

	public void collapseToLevel(Object element, int level) {
		timeLine.getTreeViewer().collapseToLevel(element, level);
		timeLine.synchronizeRowsWithTree();
	}

}
