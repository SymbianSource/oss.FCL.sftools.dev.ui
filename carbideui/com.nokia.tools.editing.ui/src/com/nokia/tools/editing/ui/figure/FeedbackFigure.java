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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;

public class FeedbackFigure extends RectangleFigure {
	private boolean isOutline;

	/**
	 * @return the isOutline
	 */
	public boolean isOutline() {
		return isOutline;
	}

	/**
	 * @param isOutline the isOutline to set
	 */
	public void setOutline(boolean isOutline) {
		this.isOutline = isOutline;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Shape#fillShape(org.eclipse.draw2d.Graphics)
	 */
	public void fillShape(Graphics g) {
		Rectangle r = getBounds();
		g.setBackgroundColor(ColorConstants.gray);
		g.setXORMode(true);
		if (isOutline) {
			g.setForegroundColor(ColorConstants.red);
			Rectangle rect = new Rectangle(r.x, r.y, r.width, r.height);
			g.setClip(rect);
			g.drawRectangle(rect);
		} else {
			g.setForegroundColor(ColorConstants.green);
			g.drawRectangle(r.x, r.y, r.width - 1, r.height - 1);
		}
	}
}
