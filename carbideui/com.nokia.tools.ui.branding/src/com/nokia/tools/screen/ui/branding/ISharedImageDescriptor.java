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
package com.nokia.tools.screen.ui.branding;

import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.tools.ui.branding.Activator;

/**
 * 
 */
public interface ISharedImageDescriptor {
	ImageDescriptor ICON16_INFO = Activator
			.getImageDescriptor("icons/etool16/info_tsk.gif");
	ImageDescriptor ICON16_WARNING = Activator
			.getImageDescriptor("icons/etool16/showwarn_tsk.gif");
	ImageDescriptor ICON16_ERROR = Activator
			.getImageDescriptor("icons/etool16/error_tsk.gif");
	ImageDescriptor WIZBAN_OPEN_PROJECT = Activator
			.getImageDescriptor("icons/wizban/open_project.png");
	ImageDescriptor WIZBAN_CREATE_PROJECT = Activator
			.getImageDescriptor("icons/wizban/create_project.png");
}
