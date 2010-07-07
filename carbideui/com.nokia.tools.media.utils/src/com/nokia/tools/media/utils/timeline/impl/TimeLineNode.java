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
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.timeline.ITimeLineNode;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.cp.IControlPoint;
import com.nokia.tools.media.utils.timeline.cp.IControlPointModel;
import com.nokia.tools.media.utils.timeline.cp.IControlPointMovingListener;

public class TimeLineNode implements ITimeLineNode, KeyListener {
	protected ITimeLineRow row;

	protected long startTime;

	protected long endTime;

	protected IControlPointModel controlPointModel = null;

	protected Map<IControlPoint, Point> controlPointLocations = new HashMap<IControlPoint, Point>();

	private IControlPoint movingControlPoint;

	private boolean moving;

	private long lastMouseDownTime;

	public TimeLineNode(ITimeLineRow row, long startTime, long endTime) {
		this.row = row;
		this.startTime = startTime;
		this.endTime = endTime;

		this.row.addNode(this);
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public ITimeLineRow getRow() {
		return row;
	}

	public void setRow(ITimeLineRow row) {
		this.row = row;
		this.row.addNode(this);
	}

	public IControlPointModel getControlPointModel() {
		return controlPointModel;
	}

	void paintControlPoints(GC gc, Rectangle bounds) {
		if (getControlPointModel() == null) {
			return;
		}

		List<IControlPoint> controlPoints = getControlPointModel()
				.getControlPoints();

		synchronized (controlPointLocations) {
			controlPointLocations.clear();

			for (IControlPoint controlPoint : controlPoints) {
				Point point = ((TimeLine) ((TimeLineRow) row).getTimeLine())
						.getPointForTime(bounds, controlPoint.getTime());
				paintControlPoint(gc, controlPoint, point);

				controlPointLocations.put(controlPoint, point);
			}
		}
	}

	void paintControlPoint(GC gc, IControlPoint controlPoint, Point point) {
		if (point == null) {
			return;
		}

		if (((TimeLine) ((TimeLineRow) row).getTimeLine())
				.getCurrentControlPoint() == controlPoint) {
			gc.setBackground(IMediaConstants.BG_COLOR1);
		} else {
			gc.setBackground(IMediaConstants.BG_COLOR2);
		}

		Rectangle bounds = getControlPointBounds(point);
		gc.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
	}

	Rectangle getControlPointBounds(Point point) {
		return new Rectangle(point.x - 2, point.y - 5, 5, 11);
	}

	IControlPoint findControlPointAtPoint(int x, int y) {
		if (getControlPointModel() == null) {
			return null;
		}

		synchronized (controlPointLocations) {
			List<IControlPoint> controlPoints = getControlPointModel()
					.getControlPoints();

			IControlPoint controlPoint = null;
			int xdiff = 0;

			for (IControlPoint cp : controlPoints) {
				Point point = controlPointLocations.get(cp);

				if (point == null) {
					continue;
				}

				Rectangle bounds = getControlPointBounds(point);

				if (bounds.contains(x, y)) {
					if (controlPoint == null) {
						controlPoint = cp;
						xdiff = Math.abs(x - point.x);
					} else {
						int diff = Math.abs(x - point.x);
						if (diff < xdiff) {
							controlPoint = cp;
							xdiff = diff;
						}
					}

					if (xdiff == 0) {
						return controlPoint;
					}
				}
			}
			return controlPoint;
		}
	}

	public void selectControlPoint(IControlPoint cp) {
		((TimeLine) ((TimeLineRow) row).getTimeLine())
				.setSelectedControlPoint(cp);

		if (getControlPointModel() != null) {
			getControlPointModel().controlPointSelected(cp);
		}
	}

	public void mouseDown(int button, int x, int y, long time) {
		lastMouseDownTime = time;

		IControlPoint controlPoint = findControlPointAtPoint(x, y);

		if (controlPoint != null) {
			if (button == 1 && controlPoint.canBeMoved()) {
				movingControlPoint = controlPoint;
				moving = true;
				if (getControlPointModel() != null
						&& getControlPointModel() instanceof IControlPointMovingListener) {
					((IControlPointMovingListener) getControlPointModel())
							.moveStarted(movingControlPoint);
				}
			}
			selectControlPoint(controlPoint);
		} else {
			selectControlPoint(null);
		}
	}

	public void mouseDoubleClick(int button, int x, int y, long time) {
		IControlPoint controlPoint = findControlPointAtPoint(x, y);

		if (controlPoint == null && getControlPointModel() != null) {
			// create new control point
			IControlPoint newControlPoint = getControlPointModel()
					.createControlPoint(time);
			selectControlPoint(newControlPoint);
		} else {
			if (controlPoint != null) {
				((TimeLine) ((TimeLineRow) row).getTimeLine())
						.setCurrentTime(controlPoint.getTime());
			}
		}

		((TimeLine) ((TimeLineRow) row).getTimeLine()).repaint();
	}

	public void mouseUp(int button, int x, int y, long time) {
		if (button == 1) {
			if (moving) {
				moving = false;
				if (movingControlPoint != null) {
					if (getControlPointModel() != null
							&& getControlPointModel() instanceof IControlPointMovingListener) {
						((IControlPointMovingListener) getControlPointModel())
								.moveFinished(movingControlPoint);
					}
					movingControlPoint = null;
				}
			}
		}
	}

	public void mouseMove(int x, int y, long time) {
		if (moving) {
			if (movingControlPoint != null && getControlPointModel() != null) {
				getControlPointModel().moveControlPoint(movingControlPoint,
						time);
				if (getControlPointModel() instanceof IControlPointMovingListener) {
					((IControlPointMovingListener) getControlPointModel())
							.moveInProgress(movingControlPoint);
				}
			}
			((TimeLine) ((TimeLineRow) row).getTimeLine()).repaint();
		}
	}

	public void setControlPointModel(IControlPointModel model) {
		this.controlPointModel = model;
	}

	protected void contributeToContextMenu(IMenuManager menuManager) {
		TimeLine timeLine = ((TimeLine) ((TimeLineRow) row).getTimeLine());

		if (timeLine != null) {
			Action createCPMenuItem = getCreateControlPointAction();
			menuManager.add(createCPMenuItem);

			Action removeCPMenuItem = getRemoveControlPointAction();
			menuManager.add(removeCPMenuItem);
		}
	}

	public IControlPoint getSelectedControlPoint() {
		return row.getTimeLine().getCurrentControlPoint();
	}

	public static class CreateControlPointAction extends Action {
		public static final String ID = CreateControlPointAction.class
				.getName();

		TimeLine timeLine;

		public CreateControlPointAction(TimeLine timeLine) {
			super(Messages
					.getString("TimeLineNode.MenuItem_CreateControlPoint")); //$NON-NLS-1$
			this.timeLine = timeLine;
			setId(ID);
		}

		public CreateControlPointAction(String text, TimeLine timeLine) {
			super(text);
			this.timeLine = timeLine;
			setId(ID);
		}

		@Override
		public void run() {
			IInputValidator validator = new IInputValidator() {
				public String isValid(String newText) {
					try {
						timeLine.timeLabelProvider.parse(newText);
					} catch (Exception ex) {
						return ex.getMessage();
					}
					return null;
				};
			};

			String mouseDownTime;

			if (timeLine.timeLabelProvider != null) {
				mouseDownTime = timeLine.timeLabelProvider
						.getLabel(((TimeLineNode) timeLine.getSelectedNode()).lastMouseDownTime);
			} else {
				mouseDownTime = ((Long) ((TimeLineNode) timeLine
						.getSelectedNode()).lastMouseDownTime).toString();
			}

			InputDialog dialog = new InputDialog(
					timeLine.getShell(),
					Messages
							.getString("TimeLineNode.DialogTitle_CreateControlPoint"), Messages.getString("TimeLine.TextInput_EnterTime"), mouseDownTime, //$NON-NLS-1$ //$NON-NLS-2$
					validator);

			if (dialog.open() == Dialog.OK) {
				try {
					long time = 0;
					if (timeLine.timeLabelProvider != null) {
						time = timeLine.timeLabelProvider.parse(dialog
								.getValue());
					} else {
						time = Long.parseLong(dialog.getValue());
					}
					IControlPoint newControlPoint = timeLine.getSelectedNode()
							.getControlPointModel().createControlPoint(time);
					timeLine.setSelectedControlPoint(newControlPoint);
					timeLine.notifySelectionListeners();
					timeLine.repaint();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}

		@Override
		public boolean isEnabled() {
			return timeLine.getCurrentControlPoint() == null
					&& timeLine.getSelectedNode() != null
					&& timeLine.getSelectedNode().getControlPointModel() != null;
		}
	}
	
	protected Action getCreateControlPointAction() {
		TimeLine timeLine = ((TimeLine) ((TimeLineRow) row).getTimeLine());
		return new CreateControlPointAction(timeLine);
	}
	
	protected Action getRemoveControlPointAction() {
		TimeLine timeLine = ((TimeLine) ((TimeLineRow) row).getTimeLine());
		return new RemoveControlPointAction(timeLine);
	}

	public static class RemoveControlPointAction extends Action {
		public static final String ID = RemoveControlPointAction.class
				.getName();

		TimeLine timeLine;

		public RemoveControlPointAction(TimeLine timeLine) {
			super(Messages
					.getString("TimeLineNode.MenuItem_RemoveControlPoint")); //$NON-NLS-1$
			this.timeLine = timeLine;
			setId(ID);
		}

		public RemoveControlPointAction(String text, TimeLine timeLine) {
			super(text);
			this.timeLine = timeLine;
			setId(ID);
		}

		@Override
		public void run() {
			IControlPointModel model = timeLine.getSelectedNode()
					.getControlPointModel();

			if (timeLine.getCurrentControlPoint().canBeDeleted()) {
				model.removeControlPoint(timeLine.getCurrentControlPoint());
				timeLine.setSelectedControlPoint(null);
				timeLine.notifySelectionListeners();
				timeLine.repaint();
			}
		}

		@Override
		public boolean isEnabled() {
			return timeLine.getCurrentControlPoint() != null
					&& timeLine.getCurrentControlPoint().canBeDeleted();
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.keyCode == SWT.DEL && e.stateMask == 0) {
			Action removeAction = getRemoveControlPointAction();
			if (removeAction != null) {
				removeAction.run();
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		
		
	}
}
