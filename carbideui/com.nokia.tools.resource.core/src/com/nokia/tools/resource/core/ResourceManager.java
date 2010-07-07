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
 * Manager for managing the resources like custom widget (compounds)etc
 * This class provides the abstraction for file operations and resource 
 * event handling.
 * 
 * All the external plugins should make of <code>ResourceManager</code> interface
 * for accessing the resources.
 * 
 * The file operations are delegate <code>ResourceRecorder</code> and the 
 * event handling is done through <code>ResourceEventer</code>
 * 
 * @see ResourceListener
 * @see ResourceEventHandler
 * @see ResourceHelper
 *  
 */
public interface ResourceManager {

	/**
	 * 
	 * Read the resource at a particular path
	 * 
	 * @param path
	 * @return
	 */
	Object read(String path)throws ResourceException;

	/**
	 * Write the resource to a particular path
	 * 
	 * @param path
	 * @param value
	 */
	void write(String path, Object value);

	/**
	 * List the elements ( files) in a the path
	 * 
	 * @param path -
	 *            folder path
	 * @return
	 */
	String[] list(String path);

	/**
	 * Add listeners interested in particular event
	 * 
	 * @param listener
	 */
	void addResourceListener(ResourceListener listener);

	/**
	 * Remove an addlistener
	 * 
	 * @param listener
	 */
	void removeResourceListener(ResourceListener listener);

	/**
	 * Return true if folder
	 * 
	 * @return
	 */
	public boolean isFolder(String path);
	
	/**
	 * Delete a resource on a specific path
	 * 
	 * @param path
	 * @return
	 */
	public boolean delete(String path)throws ResourceException;;
	
	/**
	 * Check whether a file exist 
	 * @param path
	 * @return
	 */
	public boolean exists(String path);
	
	/**
	 * 
	 * Copy file/Folder from one location to other 
	 * @param originalfilePath - Path of the original location
	 * @param newLocation - relative path of new location 
	 */
	public void copy( String originalfilePath, String newLocation)throws ResourceException;
	
	/**
	 * Get the attributes for a particular resource
	 * 
	 * @param path - path of the resource
	 * @return - the attributes
	 */
	public Map<String, Object> getAttributes(String path);
	
	/**
	 * Rename the resource
	 * 
	 * @param path - the path of the resource whose name is changed
	 * @param newName - new name of the resource
	 * @return - true is the file is renamed
	 */
	public boolean rename(String path, String newName);

}
