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
package com.nokia.tools.theme.s60.packaging.pkg;

import java.text.MessageFormat;

/**
 * This class represents the vender logo statement
 * http://www.symbian.com/developer/techlib/v9.1docs/doc_source/N10356/Installing-ref/PKG_format/PKG_logo.html#Installing%2dref%2epkg%2elogo
 *
 */
public class VendorLogo {
	private static final MessageFormat FORMAT = new MessageFormat("=\"{0}\", \"image/jpeg\", \"\"");

	private String name;

	/**
	 * Constructs a Vender Logo statement.
	 * 
	 * @param name name of the vendor.
	 */
	public VendorLogo(String name) {
		this.name = name;
	}

	/**
	 * Parses the line to the statement.
	 * 
	 * @param line the line to be parsed.
	 * @return the statement or null.
	 */
	public static VendorLogo parse(String line) {
		if (line.startsWith("=")) {
			try {
				Object[] values = FORMAT.parse(line);
				String name = ((String) values[0]).trim();
				return new VendorLogo(name);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new String(FORMAT.format(new Object[] { name }));
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
