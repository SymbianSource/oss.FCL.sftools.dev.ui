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

package com.nokia.tools.editing.ui.adapter;

import org.eclipse.draw2d.Graphics;
import org.eclipse.gef.EditPart;

public interface IFreeformDecorator {
	void paint(Graphics g, EditPart[] freeformEditParts);
}
