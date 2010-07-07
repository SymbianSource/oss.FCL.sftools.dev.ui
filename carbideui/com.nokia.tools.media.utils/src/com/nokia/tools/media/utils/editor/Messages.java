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

package com.nokia.tools.media.utils.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	public static String AnimationImageContainer_FrameMenuItem;
	public static String AnimationImageContainer_moveFrame;
	public static String AnimationImageContainer_moveFrameToBegin;
	public static String AnimationImageContainer_moveFrameToLeft;
	public static String AnimationImageContainer_moveFrameToRight;
	public static String AnimationImageContainer_moveFrameToEnd;
	
	public static String AnimationImageContainer_pasteImg;
	public static String AnimationImageContainer_deleteFrame;
	
	public static String DeleteFrameToolTip;
	public static String AddFrameToolTip;
	public static String MoveFrameToBeginToolTip;
	public static String MoveFrameToLeftToolTip;
	public static String MoveFrameToRightToolTip;
	public static String MoveFrameToEndToolTip;
	public static String DistributeAnimateTimeToolTip;
	
	public static String CreateCP_label;	

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	public static String CreateCPAction_msg_NAN;
	public static String CreateCPAction_msg_createCP;
	public static String CreateCPAction_msg_enterTime;
	public static String CreateCPAction_msg_invalidTime;
	
}
