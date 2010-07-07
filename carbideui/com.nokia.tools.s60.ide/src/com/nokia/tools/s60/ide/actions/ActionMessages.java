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

package com.nokia.tools.s60.ide.actions;

import org.eclipse.osgi.util.NLS;

public class ActionMessages extends NLS {
	public static String GalleryDropDownAction_tooltip;
	public static String GalleryModeAction_tooltip;
	public static String GalleryLayoutAction_tooltip;
	public static String GalleryAction_description;
	public static String GalleryAction_tooltip;
	public static String ToggleTextAction_tooltip;
	public static String OpenGraphicsEditorAction_tooltip;
	public static String OpenGraphicsEditorAction_text;
	public static String PortraitOrientationAction_tooltip;
	public static String LandscapeOrientationAction_tooltip;
	public static String OpenProjectAction_Error_LoadingContent_Title;
	public static String OpenProjectAction_Error_LoadingContent_Message;
	public static String OpenProjectAction_Error_ContentMissing_Message;
	public static String SearchAction_Dialog_Title;
	public static String SearchAction_Dialog_Search_Instructions;
	public static String SearchAction_Dialog_Elements_Label_Text;
	
	static {
		NLS.initializeMessages(ActionMessages.class.getName(),
				ActionMessages.class);
	}
}
