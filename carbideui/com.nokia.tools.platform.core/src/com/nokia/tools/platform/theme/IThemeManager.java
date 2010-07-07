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
package com.nokia.tools.platform.theme;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IThemeManager {
	String DTD_FOLDER = "/dtds/";

	void setContainerId(String containerId);

	String getContainerId();

	Theme getModel(String modelId, IProgressMonitor monitor) throws ThemeException;

	/**
	 * Returns the loaded model for the passed model id. This method is different from the getModel() method
	 * in the sense that this method will not load the model if its is not already loaded. If the model is 
	 * already loaded then it will return the loaded model and if not then the method returns null.
	 * @param modelId - the model id for which the Theme model should be looked up.
	 * @return - null if the model with the id has not yet been loaded, else the loaded model.
	 */
	public Theme getLoadedModel(String modelId);
	
	Theme createTheme(String modelId, IProgressMonitor monitor)
			throws ThemeException;
	
	Theme openTheme(File file, IProgressMonitor monitor) throws ThemeException;
	
	void releaseTheme(String themeId);

	void dispose();
}
