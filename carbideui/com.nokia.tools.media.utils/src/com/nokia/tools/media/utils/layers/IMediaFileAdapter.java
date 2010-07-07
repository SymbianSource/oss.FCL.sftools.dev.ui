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

import org.eclipse.gef.commands.Command;

public interface IMediaFileAdapter {

	String getFileName(boolean absolutePath);

	boolean hasDuration();

	long getMinDuration();

	long getMaxDuration();

	long getDuration();

	long getDurationFromGraphics();

	Command getApplyDurationCommand(long duration);

	Command getApplyMediaFileCommand(String filePath);

	boolean isSound();

	/**
	 * from s60 ui and remove toolbox, skinnable entity and this class
	 * 
	 * @return
	 */
	Object getThemeGraphics();

	/**
	 * pastes sound to new theme graphics and returns it.
	 * 
	 * @param oldTG
	 * @param data
	 * @return
	 */
	Object getEditedThemeGraphics(Object oldTG, String soundFilePath);

}
