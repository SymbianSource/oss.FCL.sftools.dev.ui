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

import java.util.ArrayList;
import java.util.List;

/** 
 * Class for handling the listeners and events 
 *
 */
public  class ResourceEventHandler {
	
	List< ResourceListener> listenerList = new ArrayList<ResourceListener>();
	
	/**
	 * Add listeners interested in particular event
	 * 
	 * @param listener
	 */
	void addResourceListener(ResourceListener listener)
	{
		if(!listenerList.contains(listener))
		{
			listenerList.add(listener);
		}		
	}

	/**
	 * Remove an addlistener
	 * 
	 * @param listener
	 */
	void removeResourceListener(ResourceListener listener)
	{
		listenerList.remove(listener);
	}

	/**
	 * Fire resource change to the listeners
	 * 
	 */
	void fireResourceChanged(ResourceEvent event)
	{
		for(ResourceListener listener :listenerList)
		{
			listener.resourceChanged(event);
		}
	}
	
	void  fireResourceChanged(String[] resouStrings, int eventType )
	{
		ResourceEvent event = new ResourceEvent(resouStrings, eventType );
		fireResourceChanged(event);
	}
	


}
