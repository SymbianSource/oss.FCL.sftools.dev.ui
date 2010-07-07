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
 * This class represents the header statement.<br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class Header {
	private static final MessageFormat FORMAT = new MessageFormat(
			"#'{'\"{0}\"'}',({1}),{2},{3},{4},TYPE={5}");
	private static final MessageFormat FORMAT_DEFAULT = new MessageFormat(
			"#'{'\"{0}\"'}',({1}),{2},{3},{4}");

	private String name;
	private String uid;
	private String majorVersion;
	private String minorVersion;
	private String buildNumber;
	private String type;

	/**
	 * Constructs a header statement.
	 * 
	 * @param name name of this application.
	 * @param uid uid of this application.
	 * @param majorVersion major version of this application.
	 * @param minorVersion minor version of this application.
	 * @param buildNumber build number of this application.
	 * @param type type of the application.
	 */
	public Header(String name, String uid, String majorVersion,
			String minorVersion, String buildNumber, String type) {
		this.name = name;
		this.uid = uid;
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.buildNumber = buildNumber;
		this.type = type;
	}

	/**
	 * Parses the line to the header statement.
	 * 
	 * @param line line to be parsed.
	 * @return the statement or null.
	 */
	public static Header parse(String line) {
		if (line.startsWith("#")) {
			try {
				Object[] values;
				try {
					values = FORMAT.parse(line);
				} catch (Exception e) {
					values = FORMAT_DEFAULT.parse(line);
				}
				String name = ((String) values[0]).trim();
				String uid = ((String) values[1]).trim();
				String majorVersion = ((String) values[2]).trim();
				String minorVersion = ((String) values[3]).trim();
				String buildNumber = ((String) values[4]).trim();
				String type = null;
				if (values.length > 5) {
					type = ((String) values[5]).trim();
				}
				return new Header(name, uid, majorVersion, minorVersion,
						buildNumber, type);
			} catch (Exception e) {
				e.printStackTrace();
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
	public void setMajorVersion(String majorVerion) {
		this.majorVersion = majorVerion;
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

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (type == null ? FORMAT_DEFAULT : FORMAT).format(new Object[] {
				name, uid, majorVersion, minorVersion, buildNumber, type });
	}
}
