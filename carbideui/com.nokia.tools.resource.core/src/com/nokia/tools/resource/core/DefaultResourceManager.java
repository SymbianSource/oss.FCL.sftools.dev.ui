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
package com.nokia.tools.resource.core;

import java.util.Map;


/**
 * The default implementation of  resource manager 
 *  
 */
public class DefaultResourceManager implements ResourceManager {

	ResourceHelper recorder = null;

	ResourceEventHandler eventer = null;

	public DefaultResourceManager(ResourceHelper recorder,
			ResourceEventHandler eventer) {
		super();
		this.recorder = recorder;
		this.eventer = eventer;
	}

	public void addResourceListener(ResourceListener listener) {
		eventer.addResourceListener(listener);

	}

	public void fireResourceChanged(String[] resouStrings, int eventType) {
		eventer.fireResourceChanged(resouStrings, eventType);
	}

	public String[] list(String path) {
		return recorder.list(path);
	}

	public Object read(String path) throws ResourceException {
		return recorder.read(path);
	}

	public void removeResourceListener(ResourceListener listener) {
	

	}

	public void write(String path, Object value) {
		recorder.write(path, value);
		eventer.fireResourceChanged(new String[] { path },
				ResourceEvent.UPDATED);

	}

	public void copy(String originalfilePath, String newFolderLoc)
			throws ResourceException {
		recorder.copy(originalfilePath, newFolderLoc);
		eventer.fireResourceChanged(new String[] { newFolderLoc },
				ResourceEvent.ADDED);
	}

	public boolean delete(String path)throws ResourceException
	{
		return recorder.delete(path);
	}

	public boolean exists(String path) {

		return recorder.exists(path);
	}

	public boolean isFolder(String path) {

		return recorder.isFolder(path);
	}

	public Map<String, Object> getAttributes(String path) {
		return recorder.getAttributes(path);
	}

	public boolean rename(String path, String newName) {
		return recorder.rename(path, newName);
	}

}
