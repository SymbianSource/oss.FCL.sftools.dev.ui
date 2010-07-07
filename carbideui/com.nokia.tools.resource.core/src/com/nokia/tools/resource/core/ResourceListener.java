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

/**
 * The listener interface for receiving resource events. 
 * The class that is interested in processing an resource event
 * implements this interface, and the object created with that
 * class is registered with a ResourceManager, using the ResourceManager's
 * <code>addResourceListener</code> method. When the action event
 * occurs, that object's <code>resourceChanged</code> method is
 * invoked.
 * 
 * @see ResourceManager
 * @see ResourceEventHandler
 *
 */
public interface ResourceListener {

	/**
	 * Invoked when a resource change occurs.
	 * 
	 * @param event
	 */
	void resourceChanged(ResourceEvent event);

}
