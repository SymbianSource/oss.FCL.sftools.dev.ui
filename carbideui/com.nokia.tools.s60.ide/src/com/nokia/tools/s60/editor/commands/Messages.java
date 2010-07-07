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
package com.nokia.tools.s60.editor.commands;

import org.eclipse.osgi.util.NLS;

/**
 * NLS messages.
 */
public class Messages extends NLS {
	public static String Convert2Single_Label;
	public static String Convert2Nine_Label;
	public static String Convert2Eleven_Label;
	public static String AddToGroup_Label;
	public static String ChangeGroupColor_Label;
	public static String ChangeResolution_Label;
	public static String ChangeModel_Label;
	public static String Clear_Label;
	public static String CreateAnimationFrame_Label;
	public static String PasteImage_Label;
	public static String RemoveAnimationFrame_Label;
	public static String RemoveFromGroup_Label;
	public static String SetGraphics_Label;
	public static String SetImage_Label;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
