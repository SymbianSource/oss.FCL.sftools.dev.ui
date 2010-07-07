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
package com.nokia.tools.theme.s60.ui.animation.presets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import com.nokia.tools.theme.s60.ui.Messages;

/**
 * Canvas capable of showing PolyLine, and allow manipulating it by mouse.
 */
public class AnimationPresetControl extends Composite implements PaintListener, MouseMoveListener, MouseListener {
	
	public static final String PROPERTY_POINTS = "Points";
	
	/** data for interpolation */
	protected double data_y[] = {0, 0.2, 0.4, 0.6, 0.8, 1};
	protected double data_x[] = {0, 0.2, 0.4, 0.6, 0.8, 1};
	
	/** handle coords relative to component top-left */
	protected int[][] handleCoordinates;
	
	protected Color BACKGROUND = ColorConstants.white; 
	
	protected Color FOREGROUND = ColorConstants.black;
	protected Color GRIDS = ColorConstants.lightGray;
	
	protected Color MAIN_LINE = ColorConstants.blue;
	protected Color HANDLES = ColorConstants.blue;
	protected Color MAIN_AXES = ColorConstants.red;
	
	protected int AREA_WIDTH = 420;
	protected int AREA_HEIGHT = 200;
	
	protected int MARGIN_W = 30;
	protected int MARGIN_H = 25;
	
	protected int WIDTH = AREA_WIDTH + 2 * MARGIN_W;
	protected int HEIGHT = AREA_HEIGHT + 2 * MARGIN_H;

	protected int dragindex;
	protected boolean dragging;
	protected int mouseX;
	
	protected PropertyChangeListener canvasListener;
	
	protected String minLabel = "min", maxLabel = "max", startLabel = "start", endLabel = "end";
	
	protected long animationDuration;	
	
	/**
	 * returns data from which we will paint
	 * @return
	 */
	private double[][] getDataTransformed() {				
		double d[][] = new double[data_y.length][2]; //[no][x,y]
		for (int i=0;i<data_y.length;i++) {
			double xCoord = data_x[i] * AREA_WIDTH;
			double yCoord = data_y[i] * AREA_HEIGHT;
			d[i][0] = xCoord;
			d[i][1] = yCoord;
		}
		return d;
	}
	
	public double[][] getDataTransformed(int width, int height) {				
		double d[][] = new double[data_y.length][2]; //[no][x,y]
		for (int i=0;i<data_y.length;i++) {
			double xCoord = data_x[i] * width;
			double yCoord = data_y[i] * height;
			d[i][0] = xCoord;
			d[i][1] = yCoord;
		}
		return d;		
	}
	
	private void paintGrahics(GC gc) {
		
		Image image = new Image(null, getBounds().width, getBounds().height);
		GC imageGc = new GC(image);
		try {
			imageGc.setBackground(BACKGROUND);
			imageGc.fillRectangle(0, 0, WIDTH, HEIGHT);
			
			paintAxes(imageGc);		
			paintGrid(imageGc);
			paintTexts(imageGc);
			paintLine(imageGc);
			paintHandles(imageGc);
			
			gc.drawImage(image, 0, 0);
		} finally {
			imageGc.dispose();
			image.dispose();
		}
	}
	
	private void paintTexts(GC gc) {
		gc.setForeground(FOREGROUND);
		gc.drawString(startLabel, MARGIN_W - 10, MARGIN_H + AREA_HEIGHT + 5, true);
		gc.drawString(endLabel, MARGIN_W + AREA_WIDTH - 10, MARGIN_H + AREA_HEIGHT + 5, true);
		
		int baseY = MARGIN_H + AREA_HEIGHT - 1;
		String labels[] = {minLabel, "", "", "", "", maxLabel};
		int index = 0;
		int step = AREA_HEIGHT / (labels.length - 1);			
		for (String label: labels) {			
			gc.drawString(label, 5, baseY - (index * step) - 7, true);
			index++;
		}
	}

	private void paintHandles(GC gc) {
		gc.setForeground(HANDLES);
		gc.setBackground(HANDLES);
		handleCoordinates = new int[data_y.length][2];
		int baseY = AREA_HEIGHT - 1;
		
		double data[][] = getDataTransformed();		
		for (int c = 0; c < data.length; c++) {
			int x = (int) data[c][0];
			int y = (int) data[c][1];
			gc.fillRectangle(MARGIN_W +  x - 2,MARGIN_H + baseY - y - 2, 5, 5);
			handleCoordinates[c][0] = x + MARGIN_W;
			handleCoordinates[c][1] = baseY - y + MARGIN_H;
		}		
	}

	private void paintLine(GC gc) {
		gc.setForeground(MAIN_LINE);
		
		int baseX = MARGIN_W;
		int baseY = MARGIN_H + AREA_HEIGHT - 1;
		
		double data[][] = getDataTransformed();
		int lastX = (int) data[0][0], lastY = (int) data[0][1];
		for (int c = 1; c < data.length; c++) {
			int x = (int) data[c][0];
			int y = (int) data[c][1];
			gc.drawLine(baseX + lastX, baseY - lastY, baseX + x, baseY - y);
			lastX = x; lastY = y;
		}
		
	}

	private void paintGrid(GC gc) {
		gc.setForeground(GRIDS);
		int xx = MARGIN_W;
		int yy = MARGIN_H;
		for (int x = 60; x <= AREA_WIDTH; x+=60) {
			gc.drawLine(xx + x, yy, xx + x, yy + AREA_HEIGHT - 2);
		}
		for (int y = 0; y < AREA_HEIGHT; y+=40) {
			gc.drawLine(xx + 1, yy + y, xx + AREA_WIDTH - 1, yy + y);
		}
	}

	private void paintAxes(GC gc) {
		gc.setForeground(MAIN_AXES);
		gc.drawLine(MARGIN_W, MARGIN_H + AREA_HEIGHT-1,MARGIN_W + AREA_WIDTH-1,MARGIN_H + AREA_HEIGHT-1);
		gc.drawLine(MARGIN_W, MARGIN_H +AREA_HEIGHT-1, MARGIN_W, MARGIN_H);
	}

	public AnimationPresetControl(Composite parent, int i) {
		super(parent, i | SWT.NO_BACKGROUND);
		addPaintListener(this);
		addMouseListener(this);
		addMouseMoveListener(this);
	}	
	
	public void paintControl(PaintEvent e) {
		paintGrahics(e.gc);
	}
	
	public void setPresetData(double[] x, double[] y) {
		data_x = x;
		data_y = y;
	}
	
	public double[] getDataX() {
		return data_x;
	}
	
	public double[] getDataY() {
		return data_y;
	}
	
	private Cursor cursor;
	boolean handCursor = false;

	private boolean lastPointMovableX;

	public void mouseMove(MouseEvent e) {
		
		double snap = 5/(double)AREA_WIDTH;
		
		if (dragging) {
			//alter point 			
			data_y[dragindex] = (MARGIN_H + AREA_HEIGHT-1-e.y) / (double) AREA_HEIGHT;
			if (data_y[dragindex] < 0)
				data_y[dragindex] = 0;
			if (data_y[dragindex] > 1)
				data_y[dragindex] = 1;
			
			int lastIndex = lastPointMovableX ? data_x.length : data_x.length - 1;
			
			if (dragindex > 0 && dragindex < lastIndex) {
				data_x[dragindex] = (e.x - MARGIN_W) / (double) AREA_WIDTH;
				if (data_x[dragindex] < 0)
					data_x[dragindex] = 0;
				if (data_x[dragindex] > 1)
					data_x[dragindex] = 1;
				
				if (dragindex > 0) {
					if (data_x[dragindex] < data_x[dragindex - 1] + snap)
						data_x[dragindex] = data_x[dragindex - 1] + snap;
				}
				if (dragindex < data_x.length - 1) {
					if (data_x[dragindex] > data_x[dragindex + 1] - snap)
						data_x[dragindex] = data_x[dragindex + 1] - snap;
				}
			}
			setToolTipText(computeTooltip(
					data_x[dragindex], data_y[dragindex]));
			AnimationPresetControl.this.redraw();
			
			//notify listener that points was dragged by user
			if (canvasListener != null) 
				try {
					canvasListener.propertyChange(new PropertyChangeEvent(this, PROPERTY_POINTS, null, null));
				} finally {					
				}
				
		} else {

			int pointIndex = 0;
			for (int coords[]: handleCoordinates) {
				int dx = Math.abs(e.x - coords[0]);
				int dy = Math.abs(e.y - coords[1]);
				if (dx < 5 && dy < 5) {
					if (handCursor)
						return;
					if (cursor != null)
						cursor.dispose();
					//holding point
					cursor = new Cursor(getDisplay(), SWT.CURSOR_HAND);
					handCursor = true;
					setCursor(cursor);
					
					setToolTipText(computeTooltip(
							data_x[pointIndex], data_y[pointIndex]));
					
					return;
				}
				pointIndex++;
			}
			if (handCursor) {
				if (cursor != null)
					cursor.dispose();
				cursor = null;
				handCursor = false;
				setCursor(cursor);
				setToolTipText("");
				return;
			}
		}
	}
	
	protected String computeTooltip(double xValue, double yValue) {
		try {						
			int min = Integer.parseInt(minLabel);
			int max = Integer.parseInt(maxLabel);								
			int value = (int) (min * (1-yValue) + max * yValue);
			long time = (long) (xValue * animationDuration);
			return value + " / " + time + "ms";
		} catch (Exception ex) {
		}
		return "";
	}

	public void mouseDoubleClick(MouseEvent e) {}

	public void mouseDown(MouseEvent e) {
		int index = 0;
		mouseX = e.x;		
		for (int coords[]: handleCoordinates) {
			int dx = Math.abs(e.x - coords[0]);
			int dy = Math.abs(e.y - coords[1]);
			if (dx < 5 && dy < 5) {
				//holding point
				dragging = true;
				dragindex = index;				
				if (e.button == 3) {
					showContextMenu();
				}
				return;
			}
			index++;
		}
		dragging = false;			
		if (e.button == 3) {
			showContextMenu();
		}
	}

	private void showContextMenu() {
		MenuManager manager = new MenuManager();
		Action addPoint = new Action() {
			@Override
			public void run() {
				addPoint();
				redraw();
				dragging = false;
			}
		};
		addPoint.setText(Messages.presetsDlg_menu_addPoint);
		
		Action removePoint = new Action() {
			@Override
			public void run() {
				removePoint();
				redraw();
				dragging = false;				
			}
		};
		
		removePoint.setText(Messages.presetsDlg_menu_removePoint);
		if (dragging) {
			if (getPointsCount() > 2) {
				if (dragindex > 0 && dragindex < getPointsCount() - 1)
					manager.add(removePoint);
			}
		} else {
			double snap = 5/(double)AREA_WIDTH;
			
			//test if we can add
			int xOffset = mouseX - MARGIN_W;			
			double newX = xOffset / (double)AREA_WIDTH;			
			int addIndex = -1;
			for (int i = 0; i < data_x.length - 1; i++) {
				if (data_x[i] < newX - snap && data_x[i + 1] > newX + snap) {
					addIndex = i + 1;
					break;
				}
			}
			if (addIndex > 0)
				manager.add(addPoint);
		}
		
		Menu menu = manager.createContextMenu(this);		
		setMenu(menu);			
		menu.setVisible(true);			
	}
	
	protected void removePoint() {
		double[] x = new double[data_x.length - 1];
		double[] y = new double[data_y.length - 1];
		for (int i = 0; i < dragindex; i++) {
			x[i] = data_x[i];
			y[i] = data_y[i];
		}
		for (int i = dragindex + 1; i < data_x.length; i++) {
			x[i - 1] = data_x[i];
			y[i - 1] = data_y[i];
		}
		data_x = x;
		data_y = y;
	}

	protected void addPoint() {
		
		int xOffset = mouseX - MARGIN_W;		
		double newX = xOffset / (double)AREA_WIDTH;
		
		double snap = 5/(double)AREA_WIDTH;
		
		//find where to add
		int addIndex = -1;
		for (int i = 0; i < data_x.length - 1; i++) {
			if (data_x[i] < newX - snap && data_x[i + 1] > newX + snap) {
				addIndex = i + 1;
				break;
			}
		}
		if (addIndex < 0)
			return;
		
		double yLow, yHigh, xLow, xHigh;
		yLow = data_y[addIndex - 1];
		xLow = data_x[addIndex - 1];
		yHigh = data_y[addIndex];
		xHigh = data_x[addIndex];
		double ratio = (newX - xLow) / (xHigh - xLow);
		double newY = yHigh * ratio + yLow * (1-ratio);
		
		
		double[] x = new double[data_x.length + 1];
		double[] y = new double[data_y.length + 1];
		for (int i = 0; i < addIndex; i++) {
			x[i] = data_x[i];
			y[i] = data_y[i];
		}
		for (int i = addIndex; i < data_x.length; i++) {
			x[i + 1] = data_x[i];
			y[i + 1] = data_y[i];
		}
		x[addIndex] = newX;
		y[addIndex] = newY;
		data_x = x;
		data_y = y;
	}

	public int getPointsCount() {
		return data_x.length;
	}

	public void mouseUp(MouseEvent e) {
		dragging = false;
	}
	
	@Override
	public Point getSize() {
		return new Point(WIDTH, HEIGHT);
	}	

	public void setLastPointMovableX(boolean b) {
		lastPointMovableX = b;
	}

	public void setPresetData(double[][] animationPreset) {
		if (animationPreset != null) {
			data_x = new double[animationPreset.length];
			data_y = new double[animationPreset.length];
			for (int i=0; i < data_x.length;i++) {
				data_x[i]  = animationPreset[i][0];
				data_y[i]  = animationPreset[i][1];
			}
		}
	}

	public void setEndLabel(String endLabel) {
		this.endLabel = endLabel;
	}

	public void setMaxLabel(String maxLabel) {
		if (maxLabel != null)			
			this.maxLabel = maxLabel;		
	}

	public void setMinLabel(String minLabel) {
		if (minLabel != null)
			this.minLabel = minLabel;
	}

	public void setStartLabel(String startLabel) {
		this.startLabel = startLabel;
	}

	public void setCanvasListener(PropertyChangeListener canvasListener) {
		this.canvasListener = canvasListener;
	}
	
	@Override
	public void dispose() {
		try {
			if (cursor != null)
				cursor.dispose();
		} finally {}
		
		super.dispose();
	}

	public void setAnimationDuration(long animationDuration) {
		this.animationDuration = animationDuration;
	}
		

}
