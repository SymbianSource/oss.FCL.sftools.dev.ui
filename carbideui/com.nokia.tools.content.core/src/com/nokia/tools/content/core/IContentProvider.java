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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for the content providers to implement. The content provider
 * implementation class should be defined in the extension to the point:
 * <code>com.nokia.tools.content.core.providers</code>.
 
 */
public interface IContentProvider extends IAdaptable {
	/**
	 * @return the supported content type.
	 */
	String getContentType();

	/**
	 * Creates new root content with the specific name.
	 * 
	 * @param creationData data to be interpreted by client and corresponding
	 *            provider implementation
	 * @param monitor the progress monitor.
	 * @return the newly create content root.
	 * @throws ContentException if content creation failed.
	 */
	List<IContent> createRootContent(Map<String, Object> creationData,
			IProgressMonitor monitor) throws ContentException;

	/**
	 * Creates root contents from the given input source.
	 * 
	 * @param input the input to be handled.
	 * @param monitor the progress monitor.
	 * @return the root contents obtained from the input source.
	 * @throws IOException if I/O error occurred.
	 * @throws ContentException if content reading failed.
	 */
	List<IContent> getRootContents(Object input, IProgressMonitor monitor)
			throws IOException, ContentException;
}
