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
package com.nokia.tools.content.core;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class represents the top-level content node.
 * 
 */
public interface IContent extends IContentData {
	/**
	 * @return the content type
	 */
	String getType();

	/**
	 * Persists the content.
	 * 
	 * @param monitor the progress monitor, can be null if not available.
	 * @throws IOException if I/O error occurred.
	 * @throws ContentException if content saving failed.
	 */
	void save(IProgressMonitor monitor) throws IOException, ContentException;

	/**
	 * Saves the content into a different location.
	 * 
	 * @param newFileName new file name.
	 * @param monitor the progress monitor, can be null if not available.
	 */
	void saveAs(String newFileName, IProgressMonitor monitor)
			throws IOException, ContentException;

	/**
	 * Deletes the content from persistent storage.
	 * 
	 * @throws IOException if I/O error occurred.
	 * @throws ContentException if content deletion failed.
	 */
	void delete() throws IOException, ContentException;

	/**
	 * Releases the resources used by this content.
	 */
	void dispose();
}
