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

/**
 * This interface defines the contract of an Entity which is responsible for the creation of 
 * Element instances based on the context especially if there are different Element subclasses to 
 * be created for S60, etc.
 * As of now the element instances created for S60 are going to be the same which will be an
 * instance of com.nokia.tools.platform.theme.Element which is the default implementation of this interface.
 * There is also a specific implementation provided for the interface for creating ThirdPartyIcon type of element.
 * This interface in short abstract the creation of specific Element types for specific platforms. 
 * 
 *
 */

public interface ElementCreator {

	public Element createElement(String elementName);
	
}
