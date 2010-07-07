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
package com.nokia.tools.s60.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 */
public class GalleryLayout extends Layout {
	public static final int MAXIMUM_STRETCH = 0;
	public static final int FIT_PAGE = 1;

	private static final int MIN_WIDTH = 100;
	private static final double SWITCHING_RATIO = 2.0;

	private static final int MARGIN = 5;

	private int type = MAXIMUM_STRETCH;
	private int maxWidth = 20;
	private int maxHeight = 20;
	private int borderWidth = 0;
	private int borderHeight = 0;
	private boolean autoMarginWidth;
	private boolean autoMarginHeight;

	private int cols;
	private int rows;
	private int childWidth;
	private int childHeight;
	private int marginWidth = MARGIN;
	private int marginHeight = MARGIN;

	private boolean isUpdate;

	public GalleryLayout() {
	}

	public GalleryLayout(int type) {
		this.type = type;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		if (type != MAXIMUM_STRETCH && type != FIT_PAGE) {
			throw new IllegalArgumentException("Invalid type: " + type);
		}
		this.type = type;
	}

	/**
	 * @return Returns the maxHeight.
	 */
	public int getMaxHeight() {
		return maxHeight;
	}

	/**
	 * @param maxHeight The maxHeight to set.
	 */
	public void setMaxHeight(int maxHeight) {
		if (maxHeight <= 0) {
			throw new IllegalArgumentException("Invalid max height: "
					+ maxHeight);
		}
		this.maxHeight = maxHeight;
	}

	/**
	 * @return Returns the maxWidth.
	 */
	public int getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @param maxWidth The maxWidth to set.
	 */
	public void setMaxWidth(int maxWidth) {
		if (maxWidth <= 0) {
			throw new IllegalArgumentException("Invalid max width: " + maxWidth);
		}
		this.maxWidth = maxWidth;
	}

	/**
	 * @return Returns the isUpdate.
	 */
	public boolean isUpdate() {
		return isUpdate;
	}

	/**
	 * @param isUpdate The isUpdate to set.
	 */
	public void setUpdate(boolean update) {
		this.isUpdate = update;
	}

	/**
	 * @return Returns the borderHeight.
	 */
	public int getBorderHeight() {
		return borderHeight;
	}

	/**
	 * @param borderHeight The borderHeight to set.
	 */
	public void setBorderHeight(int borderHeight) {
		this.borderHeight = borderHeight;
	}

	/**
	 * @return Returns the borderWidth.
	 */
	public int getBorderWidth() {
		return borderWidth;
	}

	/**
	 * @param borderWidth The borderWidth to set.
	 */
	public void setBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	/**
	 * @return Returns the autoMarginHeight.
	 */
	public boolean isAutoMarginHeight() {
		return autoMarginHeight;
	}

	/**
	 * @param autoMarginHeight The autoMarginHeight to set.
	 */
	public void setAutoMarginHeight(boolean autoMarginHeight) {
		this.autoMarginHeight = autoMarginHeight;
	}

	/**
	 * @return Returns the autoMarginWidth.
	 */
	public boolean isAutoMarginWidth() {
		return autoMarginWidth;
	}

	/**
	 * @param autoMarginWidth The autoMarginWidth to set.
	 */
	public void setAutoMarginWidth(boolean autoMarginWidth) {
		this.autoMarginWidth = autoMarginWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 *      int, int, boolean)
	 */
	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		cols = 0;
		rows = 0;
		childWidth = 0;
		childHeight = 0;
		marginWidth = MARGIN;
		marginHeight = MARGIN;

		if (wHint == SWT.DEFAULT) {
			wHint = isUpdate ? composite.getParent().getClientArea().width
					: composite.getParent().getSize().x;
		}
		if (hHint == SWT.DEFAULT) {
			hHint = isUpdate ? composite.getParent().getClientArea().height
					: composite.getParent().getSize().y;
		}

		boolean fillHorizontal = wHint / (double) hHint > SWITCHING_RATIO;
		Control[] children = composite.getChildren();
		double ratio = maxWidth / (double) maxHeight;

		if (type == MAXIMUM_STRETCH) {
			if (fillHorizontal) {
				rows = Math.min(children.length, hHint
						/ (maxHeight + borderHeight));
			} else {
				cols = Math.min(children.length, wHint
						/ (maxWidth + borderWidth));
			}
		} else if (type == FIT_PAGE) {
			int[] hvs = computeHorizontal(children, wHint, hHint, ratio);
			int[] vvs = computeVertical(children, wHint, hHint, ratio);
			int ha = hvs[2] * hvs[3];
			int va = vvs[2] * vvs[3];
			int[] vs;
			if (ha > va) {
				vs = hvs;
			} else {
				vs = vvs;
			}
			rows = vs[0];
			cols = vs[1];
			childWidth = vs[2];
			childHeight = vs[3];
		}

		if (cols <= 0) {
			cols = 1;
		}
		if (rows <= 0) {
			rows = 1;
		}

		if (type == MAXIMUM_STRETCH) {
			if (fillHorizontal) {
				cols = children.length / rows;
				if (children.length % rows != 0) {
					cols++;
				}
			} else {
				rows = children.length / cols;
				if (children.length % cols != 0) {
					rows++;
				}
			}
		}

		if (cols <= 0) {
			cols = 1;
		}
		if (rows <= 0) {
			rows = 1;
		}

		if (type != FIT_PAGE) {
			if (fillHorizontal) {
				childHeight = Math
						.min(maxHeight, (hHint - marginHeight) / rows)
						- marginHeight;
				childWidth = (int) ((childHeight - borderHeight) * ratio);

				if (type == FIT_PAGE) {
					int width = Math.min(maxWidth + borderWidth,
							(wHint - marginWidth) / cols)
							- marginWidth;
					if (childWidth > width) {
						childWidth = width;
						childHeight = (int) (((childWidth - borderWidth) / ratio) + borderHeight);
					}
				}
			} else {
				childWidth = Math.min(maxWidth + borderWidth,
						(wHint - marginWidth) / cols - marginWidth);
				childHeight = (int) ((childWidth / ratio) + borderHeight);
			}
		}

		if (children.length > 0) {
			if (autoMarginWidth) {
				marginWidth = Math.max(marginWidth, (wHint - cols * childWidth)
						/ (cols + 1));
			}
			if (autoMarginHeight) {
				marginHeight = Math.max(marginHeight, (hHint - rows
						* childHeight)
						/ (rows + 1));
			}
		}

		if (composite.getParent().getVerticalBar() != null) {
			composite.getParent().getVerticalBar().setIncrement(
					childHeight + marginHeight);
			composite.getParent().getVerticalBar().setPageIncrement(
					2 * (childHeight + marginHeight));
		}
		if (composite.getParent().getHorizontalBar() != null) {
			composite.getParent().getHorizontalBar().setIncrement(
					childWidth + marginWidth);
			composite.getParent().getHorizontalBar().setPageIncrement(
					2 * (childWidth + marginWidth));
		}

		Point size = new Point(Math.max(cols * (childWidth + marginWidth)
				+ marginWidth, MIN_WIDTH), rows * (childHeight + marginHeight));
		if (type == FIT_PAGE) {
			size = new Point(Math.min(size.x, wHint), Math.min(size.y, hHint));
		}
		return size;
	}

	private int[] computeHorizontal(Control[] children, int wHint, int hHint,
			double ratio) {
		int rows = 1;
		int cols = 1;
		int tw = 0;
		int th = 0;
		while (true) {
			rows = children.length / cols;
			if (rows == 0) {
				rows = 1;
				cols--;
				th = Math.min(maxHeight + borderHeight, hHint - marginHeight)
						- marginHeight;
				tw = (int) ((th - borderHeight) * ratio) + borderWidth;
				break;
			}
			if (children.length % cols != 0) {
				rows++;
			}

			tw = Math.min(maxWidth + borderWidth, (wHint - marginWidth) / cols)
					- marginWidth;
			th = (int) (tw / ratio) + borderHeight;

			if (rows * (th + marginHeight) + marginHeight <= hHint
					&& cols * (tw + marginWidth) + marginWidth <= wHint) {
				break;
			}

			cols++;
		}
		return new int[] { rows, cols, tw, th };
	}

	private int[] computeVertical(Control[] children, int wHint, int hHint,
			double ratio) {
		int rows = 1;
		int cols = 1;
		int tw = 0;
		int th = 0;
		while (true) {
			cols = children.length / rows;
			if (cols == 0) {
				cols = 1;
				rows--;
				tw = Math.min(maxWidth + borderWidth, wHint - marginWidth)
						- marginWidth;
				th = (int) (tw / ratio) + borderHeight;
				break;
			}
			if (children.length % rows != 0) {
				cols++;
			}

			th = Math.min(maxHeight + borderHeight, (hHint - marginHeight)
					/ rows)
					- marginHeight;
			tw = (int) ((th - borderHeight) * ratio) + borderWidth;
			if (rows * (th + marginHeight) + marginHeight <= hHint
					&& cols * (tw + marginWidth) + marginWidth <= wHint) {
				break;
			}

			rows++;
		}
		return new int[] { rows, cols, tw, th };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	@Override
	protected void layout(Composite composite, boolean flushCache) {
		Control[] children = composite.getChildren();
		int index = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols && index < children.length; j++) {
				int x = marginWidth + j * (childWidth + marginWidth);
				int y = marginHeight + i * (childHeight + marginHeight);
				children[index++].setBounds(x, y, childWidth, childHeight);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#flushCache(org.eclipse.swt.widgets.Control)
	 */
	@Override
	protected boolean flushCache(Control control) {
		return true;
	}
}
