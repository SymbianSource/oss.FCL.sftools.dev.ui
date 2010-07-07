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
 * This class represents the language section of the package file.<br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class Languages {
	private static final MessageFormat FORMAT = new MessageFormat("&{0}");

	private String languages;

	/**
	 * Constructs a languges statement.
	 * 
	 * @param languages the language details.
	 */
	public Languages(String languages) {
		this.languages = languages;
	}

	/**
	 * Parses the line to the statement.
	 * 
	 * @param line line to be parsed.
	 * @return the statement or null.
	 */
	public static Languages parse(String line) {
		if (line.startsWith("&")) {
			try {
				Object[] values = FORMAT.parse(line);
				String languages = ((String) values[0]).trim();
				return new Languages(languages);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return Returns the languages.
	 */
	public String getLanguages() {
		return languages;
	}

	/**
	 * @param languages The languages to set.
	 */
	public void setLanguages(String languages) {
		this.languages = languages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return FORMAT.format(new Object[] { languages });
	}
}
