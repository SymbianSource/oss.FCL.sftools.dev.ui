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
 * An event which indicate that some resource is added / updated / deleted 
 * The event is passed to every <code>ResourceListener</code> object
 * that registered to receive such events using the ResourceManager's
 * <code>addResourceListener</code> method.
 * 
 * The object that implements the <code>ResourceListener</code> interface
 * gets this <code>ResourceEvent</code> when the event occurs.
 * 
 */
public class ResourceEvent {

	public static int ADDED = 0;
	
	public static int UPDATED = 1;
	
	public static int DELETED = 2;
	
	public static int RENAMED = 3;
		
	private String[] resouStrings = null;
	
	private int eventType;	
	
	public ResourceEvent(String[] resouStrings, int eventType) {
		super();
		this.resouStrings = resouStrings;
		this.eventType = eventType;
	}

	/**
	 * Get the resources changed 
	 * 
	 * @return - String[] resources changed
	 */
	public String[] getResources()
	{
		return resouStrings;
	}

	/**
	 * Get the event type
	 * 
	 * @return - the type of event happened
	 */
	public int getEventType() {
		return eventType;
	}
	
	
	
}
