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
 * This is the default implementation of ElementCreator and creates Element
 * instances which are the default instances to be created for S60.
 * In this case it will simply create an Element instance and return it.
 * This is now used as a singleton as no point in having multiple instances of it.
 * As one instance is always likely to be present to be used when the theme loads, we
 * use the static instance creation method for the singleton pattern.
 * 
 *
 */

public class DefaultElementCreator implements ElementCreator {

	private static final ElementCreator elementCreator = new DefaultElementCreator();
	
	private DefaultElementCreator(){
	}
	
	public Element createElement(String elementName) {
		return new Element(elementName);
	}

	public static ElementCreator getInstance(){
		return elementCreator;
	}
}
