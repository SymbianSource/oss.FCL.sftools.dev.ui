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
package com.nokia.tools.media.utils.layers;

import java.awt.datatransfer.Clipboard;

public interface IPasteTargetAdapter {

	public boolean isPasteAvailable(Clipboard clip, Object params);
	
	public boolean isPasteAvailable(Object data, Object params);
	
	/**
	 * pastes image to specified layer, returns undo object = old themeGraphics.
	 * returns null if paste failed. 'targetLayer' can be null, in such case
	 * object is paste to default layer.
	 * 
	 * @return undo object. 
	 */
	public Object paste(Object data, Object params) throws Exception;
	
	interface IPasteMaskAdapter extends IPasteTargetAdapter{
	};

}


