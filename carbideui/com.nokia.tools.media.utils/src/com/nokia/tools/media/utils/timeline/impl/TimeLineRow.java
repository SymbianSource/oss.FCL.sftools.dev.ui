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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.timeline.ITimeLine;
import com.nokia.tools.media.utils.timeline.ITimeLineDoubleClickListener;
import com.nokia.tools.media.utils.timeline.ITimeLineNode;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeListener;

public class TimeLineRow implements ITimeLineRow, KeyListener {
	private static final Pattern NODE_LINE_PATTERN = new Pattern(null, 0, 0, 2,
			2, IMediaConstants.NODE_LINE_COLOR,
			IMediaConstants.BACKGROUND_COLOR2);

	protected Color backgroundColor = IMediaConstants.BACKGROUND_COLOR;

	protected Color selectedBackgroundColor = IMediaConstants.SELECTED_BACKGROUND_COLOR;

	protected Color centerLineColor = IMediaConstants.CENTER_LINE_COLOR;

	protected Color nodeLineColor = IMediaConstants.NODE_LINE_COLOR;

	protected int nodeLineWidth = 5;

	protected boolean showCenterLine = true;

	protected boolean visible = true;

	protected String text;

	protected SortedSet<ITimeLineNode> nodes = new TreeSet<ITimeLineNode>(
			new TimeLineNodeComparator());

	protected Set<ITimeListener> timeListeners = new HashSet<ITimeListener>();

	protected Set<ITimeLineDoubleClickListener> doubleClickListeners = new HashSet<ITimeLineDoubleClickListener>();

	protected ITimeLine timeLine;

	protected Map<ITimeLineNode, Rectangle> nodesLocations = new HashMap<ITimeLineNode, Rectangle>();

	protected TimeLineNode mouseDownNode;

	public TimeLineRow() {
	}

	public TimeLineRow(ITimeLine timeLine) {
		this.timeLine = timeLine;
		this.timeLine.addRow(this);
	}

	public ITimeLineNode[] getNodes() {
		return nodes.toArray(new TimeLineNode[nodes.size()]);
	}

	public void setNodes(ITimeLineNode[] nodes) {
		this.nodes.clear();

		for (ITimeLineNode node : nodes) {
			this.nodes.add(node);
		}
	}

	public void addNode(ITimeLineNode node) {
		this.nodes.add(node);
	}

	public void removeNode(ITimeLineNode node) {
		this.nodes.remove(node);
	}

	public String getLabel() {
		return text;
	}

	public void setLabel(String label) {
		this.text = label;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getSelectedBackgroundColor() {
		return selectedBackgroundColor;
	}

	public void setSelectedBackgroundColor(Color backgroundColor) {
		this.selectedBackgroundColor = backgroundColor;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isShowCenterLine() {
		return showCenterLine;
	}

	public void setShowCenterLine(boolean showCenterLine) {
		this.showCenterLine = showCenterLine;
	}

	public Color getCenterLineColor() {
		return centerLineColor;
	}

	public void setCenterLineColor(Color centerLineColor) {
		this.centerLineColor = centerLineColor;
	}

	public Color getNodeLineColor() {
		return nodeLineColor;
	}

	public void setNodeLineColor(Color nodeLineColor) {
		this.nodeLineColor = nodeLineColor;
	}

	public void setNodeLineWidth(int nodeLineWidth) {
		this.nodeLineWidth = nodeLineWidth;
	}

	public int getNodeLineWidth() {
		return nodeLineWidth;
	}

	public void removeAllNodes() {
		this.nodes.clear();
	}

	public void addTimeListener(ITimeListener listener) {
		synchronized (timeListeners) {
			timeListeners.add(listener);
		}
	}

	public void removeTimeListener(ITimeListener listener) {
		synchronized (timeListeners) {
			timeListeners.remove(listener);
		}
	}

	void notifyTimeListeners(final long currentTime) {
		if (timeListeners.size() == 0) {
			return;
		}

		ExecutionThread.INSTANCE.execute(new Runnable() {
			public void run() {
				synchronized (timeListeners) {
					for (ITimeListener listener : timeListeners) {
						listener.timeChanged(currentTime);
					}
				}
			};
		});
	}

	public void addDoubleClickListener(ITimeLineDoubleClickListener listener) {
		synchronized (doubleClickListeners) {
			doubleClickListeners.add(listener);
		}
	}

	public void removeDoubleClickListener(ITimeLineDoubleClickListener listener) {
		synchronized (doubleClickListeners) {
			doubleClickListeners.remove(listener);
		}
	}

	protected void notifyDoubleClickListeners(final long currentTime) {
		if (doubleClickListeners.size() == 0) {
			return;
		}

		ExecutionThread.INSTANCE.execute(new Runnable() {
			public void run() {
				synchronized (doubleClickListeners) {
					for (ITimeLineDoubleClickListener listener : doubleClickListeners) {
						listener.doubleClick(currentTime);
					}
				}
			};
		});
	}

	public ITimeLine getTimeLine() {
		return timeLine;
	}

	public void setTimeLine(ITimeLine timeLine) {
		this.timeLine = timeLine;
	}

	protected void paintBackground(GC gc, Rectangle bounds) {
		if (((TimeLine) timeLine).getSelectedRow() == this) {
			if (getSelectedBackgroundColor() != null) {
				gc.setBackground(getSelectedBackgroundColor());
				gc.fillRectangle(bounds);
			}
		} else {
			if (getBackgroundColor() != null) {
				gc.setBackground(getBackgroundColor());
				gc.fillRectangle(bounds);
			}
		}
	}

	protected void paint(GC gc, Rectangle bounds) {

		synchronized (nodes) {
			nodesLocations.clear();

			if (isShowCenterLine()) {
				paintCenterLine(gc, bounds, getCenterLineColor());
			}

			for (ITimeLineNode node : nodes) {
				paintNode(gc, node, bounds);
			}
		}
	}

	protected void paintCenterLine(GC gc, Rectangle bounds, Color color) {
		int x1 = bounds.x;
		int y1 = bounds.y + bounds.height / 2;
		int x2 = bounds.x + bounds.width - 1;
		int y2 = y1;

		gc.setForeground(color);
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.drawLine(x1, y1, x2, y2);
	}

	protected void paintNode(GC gc, ITimeLineNode node, Rectangle bounds) {
		Point startPoint = ((TimeLine) timeLine).getPointForTime(bounds, node
				.getStartTime());

		Point endPoint = ((TimeLine) timeLine).getPointForTime(bounds, node
				.getEndTime());

		if (startPoint == null
				&& node.getStartTime() <= timeLine.getDisplayData()
						.getDisplayStartTime()) {
			startPoint = new Point(bounds.x, bounds.y + bounds.height / 2);
		}

		if (endPoint == null
				&& node.getEndTime() >= timeLine.getDisplayData()
						.getDisplayEndTime()) {
			endPoint = new Point(bounds.x + bounds.width - 1, bounds.y
					+ bounds.height / 2);
		}

		if (startPoint == null || endPoint == null) {
			// not visible on current display area
			return;
		}

		// draw the node line
		gc.setBackground(((TimeLineRow) node.getRow()).getNodeLineColor());
		gc.setBackgroundPattern(NODE_LINE_PATTERN);

		Rectangle nodeBounds = new Rectangle(startPoint.x, startPoint.y
				- ((TimeLineRow) node.getRow()).getNodeLineWidth() / 2,
				endPoint.x - startPoint.x + 1, ((TimeLineRow) node.getRow())
						.getNodeLineWidth());

		gc.fillRectangle(nodeBounds);
		gc.setBackgroundPattern(null);

		nodesLocations.put(node, nodeBounds);

		// paint control points
		((TimeLineNode) node).paintControlPoints(gc, bounds);
	}

	protected TimeLineNode findNodeAtPoint(int x, int y) {
		synchronized (nodes) {

			for (ITimeLineNode node : nodes) {
				Rectangle bounds = nodesLocations.get(node);

				if (bounds == null) {
					continue;
				}

				if (bounds.contains(x, y)) {
					return (TimeLineNode) node;
				}

				if (((TimeLineNode) node).findControlPointAtPoint(x, y) != null) {
					return (TimeLineNode) node;
				}
			}
			return null;
		}
	}

	public void mouseDown(int button, int x, int y, long time) {
		((TimeLine) timeLine).setSelectedRow(this);

		mouseDownNode = findNodeAtPoint(x, y);
		if (mouseDownNode != null) {
			((TimeLine) timeLine).setSelectedNode(mouseDownNode);
			mouseDownNode.mouseDown(button, x, y, time);
		}
	}

	public void mouseDoubleClick(int button, int x, int y, long time) {
		notifyDoubleClickListeners(time);

		TimeLineNode node = findNodeAtPoint(x, y);
		if (node != null) {
			node.mouseDoubleClick(button, x, y, time);
		}
	}

	public void mouseUp(int button, int x, int y, long time) {
		if (mouseDownNode != null) {
			mouseDownNode.mouseUp(button, x, y, time);
			mouseDownNode = null;
		}
	}

	public void mouseMove(int x, int y, long time) {
		TimeLineNode node = findNodeAtPoint(x, y);
		if (node != null) {
			node.mouseMove(x, y, Math.min(Math.max(time, node.getStartTime()),
					node.getEndTime()));
		}
	}

	public void contributeToContextMenu(IMenuManager menuManager) {
	}

	public TimeLineRow(Object source) {
		this.source = source;
	}

	private Object source;

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void keyPressed(KeyEvent e) {
		ITimeLineNode node = ((TimeLine) timeLine).getSelectedNode();
		if (node == null) {
			return;
		}

		((TimeLineNode) node).keyPressed(e);
	}

	public void keyReleased(KeyEvent e) {
		ITimeLineNode node = ((TimeLine) timeLine).getSelectedNode();
		if (node == null) {
			return;
		}

		((TimeLineNode) node).keyReleased(e);
	}
}
