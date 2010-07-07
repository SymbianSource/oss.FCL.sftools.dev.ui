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
 * This class represents a statement that specifies the embedded sis file.<br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class EmbeddedSis {
	public static final MessageFormat FORMAT = new MessageFormat(
			"@\"{0}\",({1})");

	private String file;
	private String uid;

	/**
	 * Constructs a new embedded sis file statement.
	 * 
	 * @param file the sis file name.
	 * @param uid the sis file uid.
	 */
	public EmbeddedSis(String file, String uid) {
		this.file = file;
		this.uid = uid;
	}

	/**
	 * Parses the line to the statement.
	 * 
	 * @param line the line to be parsed.
	 * @return the statement or null.
	 */
	public static EmbeddedSis parse(String line) {
		if (line.startsWith("@")) {
			try {
				Object[] values = FORMAT.parse(line);
				String file = ((String) values[0]).trim();
				String uid = ((String) values[1]).trim();
				return new EmbeddedSis(file, uid);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return Returns the file.
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @param file The file to set.
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * @return Returns the uid.
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid The uid to set.
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return FORMAT.format(new Object[] { file, uid });
	}
}
