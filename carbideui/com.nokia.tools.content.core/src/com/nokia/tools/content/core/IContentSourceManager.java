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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface to be implemented by Content source managers
 * 
 */
public interface IContentSourceManager {

	/**
	 * Gets the root contents based on the provided input data. This will go
	 * through all registered content providers that can handle the given type
	 * to construct the root contents based on provided input.
	 * 
	 * @param type
	 *        the content type.
	 * @param input
	 *        the input object.
	 * @return the initialized root contents.
	 * @throws IOException
	 *         if I/O error occurred.
	 * @throws ContentException
	 *         if the content can't be obtained from the given input.
	 */
	List<IContent> getRootContents(String type, Object input,
			IProgressMonitor monitor) throws IOException, ContentException;

	/**
	 * Gets all the root contents based on the provided input data. This will go
	 * through all registered content providers that can handle the given type
	 * to construct the root contents.
	 * 
	 * @param type
	 *        the content type.
	 * @return the initialized root contents.
	 * @throws IOException
	 *         if I/O error occurred.
	 * @throws ContentException
	 *         if the content can't be obtained from the given input.
	 */
	List<IContent> getRootContents(String type, IProgressMonitor monitor)
			throws IOException, ContentException;

	/**
	 * Creates the root contents based on type and internally interpreted
	 * creation data
	 * 
	 * @param type
	 * @param creationData
	 * @return list of contents created
	 * @throws exception
	 *         that accured during creation
	 */
	List<IContent> createRootContents(String type,
			Map<String, Object> creationData, IProgressMonitor monitor)
			throws ContentException;

}
