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
package com.nokia.tools.theme.s60.parser;

import java.io.IOException;
import java.net.URI;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.theme.s60.general.ThemeAppBundle;


public abstract class AbstractHandler extends DefaultHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws IOException, SAXException {
		try {
			new URI(systemId);
			return new InputSource(systemId);
		} catch (Exception e) {
		}
		try {
			String dtdLocation = ThemeAppBundle.getPropBundle().getString(
					ThemeTag.CONFIG_DIR);
			String dtdPath = FileUtils.makeAbsolutePath(dtdLocation, systemId);
			return new InputSource(FileUtils.toURL(dtdPath).toURI().toString());
		} catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}
}
