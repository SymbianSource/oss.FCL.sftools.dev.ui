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
package com.nokia.tools.screen.ui.extension;

import com.nokia.tools.content.core.IContent;

/**
 * This interface defines the behavior of a Packaging pre processing operation.
 * The specific implementation based on the ones registered for the platform
 * will be called with the IContent instance and requested to perform any packaging
 * pre-checks and if there are problems found through the pre-checks then the packaging
 * proceeds with the showing of wizard dialog for the packaging.
 */
public interface IPackagingPreprocessingAction {

	/**
	 * Requests the pre-processing on the passed content and return
	 * if we could proceed further on the packaging or not.
	 *  
	 * @param content - the content for the theme which needs to be pre-processed if needed
	 * @return - a boolean indicating whether the packaging proceed further or not. 
	 */
	public boolean performPackagingPreProcessing(IContent content);
}
