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

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class ContentPaneFigure extends Figure {
	public ContentPaneFigure() {
		setLayoutManager(new AbstractHintLayout() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.AbstractLayout#calculatePreferredSize(org.eclipse.draw2d.IFigure,
			 *      int, int)
			 */
			protected Dimension calculatePreferredSize(IFigure container,
					int wHint, int hHint) {
				ContentPaneFigure cf = (ContentPaneFigure) container;
				return cf.getContentPane() != null ? cf.getContentPane()
						.getPreferredSize(wHint, hHint) : new Dimension();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.draw2d.LayoutManager#layout(org.eclipse.draw2d.IFigure)
			 */
			public void layout(IFigure container) {
				ContentPaneFigure cf = (ContentPaneFigure) container;
				Rectangle r = cf.getClientArea();
				if (cf.getContentPane() != null)
					cf.getContentPane().setBounds(r);
			}
		});
	}

	public void setContentPane(IFigure figure) {
		add(figure);
	}

	public IFigure getContentPane() {
		return getChildren().isEmpty() ? null : (IFigure) getChildren().get(0);
	}
}
