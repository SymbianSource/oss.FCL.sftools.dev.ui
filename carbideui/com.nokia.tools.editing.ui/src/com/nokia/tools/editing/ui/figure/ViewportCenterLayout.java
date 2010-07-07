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
package com.nokia.tools.editing.ui.figure;

import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class ViewportCenterLayout extends XYLayout {
	private double zoom = 1.0;
	private Point origin = Point.SINGLETON;

	/**
	 * @return Returns the zoom.
	 */
	public double getZoom() {
		return zoom;
	}

	/**
	 * @param zoom The zoom to set.
	 */
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.XYLayout#layout(org.eclipse.draw2d.IFigure)
	 */
	@Override
	public void layout(IFigure parent) {
		IFigure viewport = parent;
		while (!(viewport instanceof FreeformViewport)) {
			viewport = viewport.getParent();
		}
		Rectangle parentBounds = viewport.getBounds();
		origin = new Point();
		for (Object obj : parent.getChildren()) {
			IFigure child = (IFigure) obj;
			Rectangle bounds = (Rectangle) getConstraint(child);

			if (bounds == null || bounds.width <= 0 || bounds.height <= 0) {
				continue;
			}
			if (child instanceof FreeformElementFigure) {
				Rectangle rect = ((FreeformElementFigure) child).getRealBounds()
						.getCopy();
				rect.x += origin.x - rect.width / 2;
				rect.y += origin.y - rect.height / 2;
				child.setBounds(rect);
				continue;
			}
			origin.x = Math.max(0, (int) (((parentBounds.width - bounds.width
					* zoom) / 2) / zoom));
			origin.y = Math.max(0, (int) (((parentBounds.height - bounds.height
					* zoom) / 2) / zoom));

			child.setBounds(new Rectangle(origin.x, origin.y, bounds.width,
					bounds.height));
		}
	}

	/**
	 * @return Returns the origin.
	 */
	public Point getOrigin() {
		return origin;
	}
}
