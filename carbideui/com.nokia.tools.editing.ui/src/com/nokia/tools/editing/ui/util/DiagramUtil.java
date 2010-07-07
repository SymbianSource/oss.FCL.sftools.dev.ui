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

package com.nokia.tools.editing.ui.util;

import java.awt.Point;
import java.awt.Rectangle;

import org.eclipse.emf.ecore.EObject;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditDiagram;

public class DiagramUtil {
	private DiagramUtil() {
	}

	public static Point getDiagramExtent(EObject element) {
		EObject curr = element.eContainer();
		Point p = new Point();
		while (curr != null && !(curr instanceof EditDiagram)) {
			Rectangle bounds = EditingUtil.getBounds(curr);
			p.x += bounds.x;
			p.y += bounds.y;
			curr = curr.eContainer();
		}
		return p;
	}

	public static Rectangle translateToDiagram(EObject element,
			Rectangle elementBounds) {
		Point p = getDiagramExtent(element);
		return new Rectangle(elementBounds.x + p.x, elementBounds.y + p.y,
				elementBounds.width, elementBounds.height);
	}

	public static Rectangle translateToElement(EObject element,
			Rectangle boundsRelativeToDiagram) {
		Point p = getDiagramExtent(element);
		return new Rectangle(boundsRelativeToDiagram.x - p.x,
				boundsRelativeToDiagram.y - p.y, boundsRelativeToDiagram.width,
				boundsRelativeToDiagram.height);
	}
}
