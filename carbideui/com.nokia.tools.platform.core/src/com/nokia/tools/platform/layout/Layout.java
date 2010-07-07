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


package com.nokia.tools.platform.layout;

import java.awt.Rectangle;
import java.io.Serializable;

/**
 *	Holds layout data. The layout object is used to return
 *  the layout information to other modules.
 */
public class Layout implements Serializable {
	private static final long serialVersionUID = 1L;

	int[][] left;
	int[][] right;
	int[][] top;
	int[][] bottom;
	int[][] width;
	int[][] height;

	public Layout() {
		this(1, 1);
	}

	/**
	 * Constructor for multiline / mutlicolumn layouts
	 * 
	 * @param rowCount
	 * @param columnCount
	 */
	public Layout(int rowCount, int columnCount) {
		left = new int[rowCount][columnCount];
		right = new int[rowCount][columnCount];
		top = new int[rowCount][columnCount];
		bottom = new int[rowCount][columnCount];
		width = new int[rowCount][columnCount];
		height = new int[rowCount][columnCount];
	}

	/**
	 * Sets layout values
	 * 
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @param width
	 * @param height
	 * @param justification
	 * @param colour
	 * @param type
	 */
	
	public void setLayout(int left, int right, int top, int bottom, int width,
			int height) {
		this.left[0][0] = left;
		this.right[0][0] = right;
		this.top[0][0] = top;
		this.bottom[0][0] = bottom;
		this.width[0][0] = width;
		this.height[0][0] = height;
	}

	public void setW(int width) {

		this.width[0][0] = width;
	}

	public void setH(int height) {

		this.height[0][0] = height;
	}

	public void setL(int left) {

		this.left[0][0] = left;
	}

	public void setB(int bottom) {

		this.bottom[0][0] = bottom;
	}

	public void setR(int right) {

		this.right[0][0] = right;
	}

	public void setT(int top) {

		this.top[0][0] = top;
	}

	/**
	 * Sets the data of all the rows and columns for the layout
	 * 
	 * @param left The left value matrix
	 * @param right The right value matrix
	 * @param top The top value matrix
	 * @param bottom The bottom value matrix
	 * @param width The width value matrix
	 * @param height The height value matrix
	 */
	
	public void setLayout(int[][] left, int[][] right, int[][] top,
			int[][] bottom, int[][] width, int[][] height) {
		for (int r = 0; r < this.left.length; r++) {
			for (int c = 0; c < this.left[r].length; c++) {
				this.left[r][c] = left[r][c];
				this.right[r][c] = right[r][c];
				this.top[r][c] = top[r][c];
				this.bottom[r][c] = bottom[r][c];
				this.width[r][c] = width[r][c];
				this.height[r][c] = height[r][c];
			}
		}
	}

	/**
	 * @return Returns the bottom value.
	 */
	public int B() {
		return bottom[0][0];
	}

	/**
	 * @param rowNo (any value less than 1 is treated as 1)
	 * @param columnNo (any value less than 1 is treated as 1)
	 * @return Returns the bottom value for the given row and column
	 */
	public int B(int rowNo, int columnNo) {
		return bottom[rowNo][columnNo];
	}

	/**
	 * @return Returns the height.
	 */
	public int H() {
		return height[0][0];
	}

	/**
	 * @return Returns the left.
	 */
	public int L() {
		return left[0][0];
	}

	/**
	 * @return Returns the left.
	 * @param rowNo (any value less than 1 is treated as 1)
	 * @param columnNo (any value less than 1 is treated as 1)
	 */
	public int L(int rowNo, int columnNo) {
		return left[rowNo][columnNo];
	}

	/**
	 * @return Returns the right.
	 */
	public int R() {
		return right[0][0];
	}

	/**
	 * @return Returns the right.
	 * @param rowNo (any value less than 1 is treated as 1)
	 * @param columnNo (any value less than 1 is treated as 1)
	 */
	public int R(int rowNo, int columnNo) {
		return right[rowNo][columnNo];
	}

	/**
	 * @return Returns the top.
	 */
	public int T() {
		return top[0][0];
	}

	/**
	 * @return Returns the top.
	 * @param rowNo (any value less than 1 is treated as 1)
	 * @param columnNo (any value less than 1 is treated as 1)
	 */
	public int T(int rowNo, int columnNo) {
		return top[rowNo][columnNo];
	}

	/**
	 * @return Returns the width.
	 */
	public int W() {
		return width[0][0];
	}

	public Rectangle getBounds() {
		return new Rectangle(L(), T(), W(), H());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Layout:\n");
		for (int i = 0; i < left.length; i++) {
			for (int j = 0; j < left[i].length; j++) {
				String rowCol = "[" + i + "]" + "[" + j + "]:";
				sb.append(" L").append(rowCol).append(left[i][j]).append(" T")
						.append(rowCol).append(top[i][j]).append(" R").append(
								rowCol).append(right[i][j]).append(" B")
						.append(rowCol).append(bottom[i][j]).append(" W")
						.append(rowCol).append(width[i][j]).append(" H")
						.append(rowCol).append(height[i][j]);
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
