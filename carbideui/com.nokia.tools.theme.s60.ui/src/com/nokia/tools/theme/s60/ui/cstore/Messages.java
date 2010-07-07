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

package com.nokia.tools.theme.s60.ui.cstore;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {	

	public static String cstore_default;

	public static String cstore_delete;

	public static String cstore_filterAction;

	public static String cstore_filterDlgLblb;

	public static String cstore_filterDlgMsg;

	public static String cstore_filterTagsMenu;

	public static String cstore_newTagAction;

	public static String cstore_newTagDlgLbl;

	public static String cstore_newTagDlgMsg;
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
