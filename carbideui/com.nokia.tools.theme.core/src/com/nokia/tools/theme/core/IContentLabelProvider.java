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
package com.nokia.tools.theme.core;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;

public interface IContentLabelProvider extends ILabelProvider {
	/**
	 * @param element the element
	 * @return the image descriptor for the given element
	 */
	ImageDescriptor getImageDescriptor(Object element, int width, int height);
	

	/**
	 * While constructing images, we may/may not want to use
	 * the loc id. Hence, introduced it as a boolean parameter.
	 * 
	 * @param element to be rendered
	 * @param useLocId false if should not be used (as in icon view where only idmappings is used)
	 * @param width
	 * @param height
	 * @return
	 */
	ImageDescriptor getImageDescriptor(Object element, boolean useLocId, int width, int height);
	
	/**
	 * @param element the element
	 * @return the icon image descriptor for the given element
	 */
	ImageDescriptor getIconImageDescriptor(Object element, int width, int height);
}
