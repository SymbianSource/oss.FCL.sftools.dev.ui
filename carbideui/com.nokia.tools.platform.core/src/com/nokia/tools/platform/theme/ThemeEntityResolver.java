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

import java.io.File;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nokia.tools.platform.extension.IThemeDescriptor;

public class ThemeEntityResolver implements EntityResolver {
	
	public InputSource resolveEntity(String publicId,
			String systemId) throws SAXException, IOException {
		return new InputSource(IThemeDescriptor.class
				.getResourceAsStream(IThemeManager.DTD_FOLDER
						+ new File(systemId).getName()));
	}
}
