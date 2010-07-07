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
 * This class represents the file to be installed to the device directly.<br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class InstallFile {
	private static final MessageFormat FORMAT = new MessageFormat(
			"\"{0}\" - \"{1}\"{2}");

	private String source;
	private String destination;
	private String options;

	/**
	 * Constructs an install file statement.
	 * 
	 * @param source the source file.
	 * @param destination the destination file on the device.
	 */
	public InstallFile(String source, String destination) {
		this(source, destination, null);
	}

	/**
	 * Constructs an install file statement.
	 * 
	 * @param source the source file.
	 * @param destination the destination file on the device.
	 * @param options the install options.
	 */
	public InstallFile(String source, String destination, String options) {
		this.source = source;
		this.destination = destination;
		this.options = options;
	}

	/**
	 * Parses the line to the statement.
	 * 
	 * @param line the line to be parsed.
	 * @return the statement or null.
	 */
	public static InstallFile parse(String line) {
		if (line.startsWith("\"")) {
			try {
				Object[] values = FORMAT.parse(line);
				String source = ((String) values[0]).trim();
				String destination = ((String) values[1]).trim();
				String options = ((String) values[2]).trim();
				return new InstallFile(source, destination, options);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return Returns the destination.
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination The destination to set.
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return Returns the source.
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source The source to set.
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return Returns the options.
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options The options to set.
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (options == null) {
			options = "";
		}
		return FORMAT.format(new Object[] { source, destination, options });
	}
	
	public boolean equals(Object installFile){
		return getSource().equals(((InstallFile)installFile).getSource())
				&& getDestination().equals(((InstallFile)installFile).getDestination());
				//&& getOptions().equals(((InstallFile)installFile).getOptions()
	}
}
