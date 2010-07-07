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
package com.nokia.tools.theme.s60.ui.animation.timing;

import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;

/**
 * defines UI contributor for contributing controls for particular
 * timing model into AnimationPropertiesDialog
 *
 */
public interface ITimingModelUI {	
	    
		public void createUI(Composite parent);
	    
	    public void setParameters(HashMap params);
	    
	    public HashMap<String, String> getParameters();

}

	
	

