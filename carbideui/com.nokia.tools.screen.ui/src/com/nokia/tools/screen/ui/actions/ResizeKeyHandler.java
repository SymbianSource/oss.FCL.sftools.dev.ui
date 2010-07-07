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
package com.nokia.tools.screen.ui.actions;

import java.awt.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPart;

public class ResizeKeyHandler extends ConstraintKeyHandler {
	public ResizeKeyHandler(IWorkbenchPart part) {
		super(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.ConstraintKeyHandler#getNewConstraint(int,
	 *      java.awt.Rectangle)
	 */
	@Override
	protected Rectangle getNewConstraint(int keyCode, Rectangle bounds) {
		switch (keyCode) {
		case SWT.ARROW_LEFT:
			return new Rectangle(bounds.x, bounds.y, bounds.width - 1,
					bounds.height);
		case SWT.ARROW_RIGHT:
			return new Rectangle(bounds.x, bounds.y, bounds.width + 1,
					bounds.height);
		case SWT.ARROW_UP:
			return new Rectangle(bounds.x, bounds.y, bounds.width,
					bounds.height + 1);
		case SWT.ARROW_DOWN:
			return new Rectangle(bounds.x, bounds.y + 1, bounds.width,
					bounds.height - 1);
		default:
			throw new IllegalArgumentException("Invalid keycode: " + keyCode);
		}
	}
}
