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
 * This class represents the depency statement. <br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class Dependency {
	private static final MessageFormat FORMAT = new MessageFormat(
			"({0}),{1},{2},{3},'{'\"{4}\"'}'");

	private String uid;
	private String majorVersion;
	private String minorVersion;
	private String buildNumber;
	private String name;

	/**
	 * Constructs a dependency statement.
	 * 
	 * @param uid dependent UID.
	 * @param majorVersion dependent major version number.
	 * @param minorVersion dependent minor version number.
	 * @param buildNumber dependent build number.
	 * @param name name of the dependent.
	 */
	public Dependency(String uid, String majorVersion, String minorVersion,
			String buildNumber, String name) {
		this.uid = uid;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.buildNumber = buildNumber;
		this.name = name;
	}

	/**
	 * Parses the given line to the dependency statement.
	 * 
	 * @param line the line to be parsed.
	 * @return the statement or null.
	 */
	public static Dependency parse(String line) {
		if (line.startsWith("(")) {
			try {
				Object[] values = FORMAT.parse(line);
				String uid = ((String) values[0]).trim();
				String majorVersion = ((String) values[1]).trim();
				String minorVersion = ((String) values[2]).trim();
				String buildNumber = ((String) values[3]).trim();
				String name = ((String) values[4]).trim();
				return new Dependency(uid, majorVersion, minorVersion,
						buildNumber, name);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return Returns the buildNumber.
	 */
	public String getBuildNumber() {
		return buildNumber;
	}

	/**
	 * @param buildNumber The buildNumber to set.
	 */
	public void setBuildNumber(String buildNumber) {
		this.buildNumber = buildNumber;
	}

	/**
	 * @return Returns the majorVersion.
	 */
	public String getMajorVersion() {
		return majorVersion;
	}

	/**
	 * @param majorVersion The majorVersion to set.
	 */
	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}

	/**
	 * @return Returns the minorVersion.
	 */
	public String getMinorVersion() {
		return minorVersion;
	}

	/**
	 * @param minorVersion The minorVersion to set.
	 */
	public void setMinorVersion(String minorVersion) {
		this.minorVersion = minorVersion;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
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
		return FORMAT.format(new Object[] { uid, majorVersion, minorVersion,
				buildNumber, name });
	}
}
