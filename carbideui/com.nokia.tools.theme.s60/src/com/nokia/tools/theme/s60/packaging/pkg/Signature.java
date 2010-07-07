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
 * This class represents the signature used for signing.<br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class Signature {
	private static final MessageFormat FORMAT = new MessageFormat(
			"*\"{0}\",\"{1}\"{2}");
	private static final MessageFormat OPTION_FORMAT = new MessageFormat(
			",KEY=\"{0}\"");

	private String privateKey;
	private String certificate;
	private String password;

	/**
	 * Constructs a signature statement.
	 * 
	 * @param privateKey path to the private key file.
	 * @param certificate path to the cerficate file.
	 * @param password the password for the private key.
	 */
	public Signature(String privateKey, String certificate, String password) {
		this.privateKey = privateKey;
		this.certificate = certificate;
		this.password = password;
	}

	/**
	 * Parses the line to the statement.
	 * 
	 * @param line the line to be parsed.
	 * @return the statement or null.
	 */
	public static Signature parse(String line) {
		if (line.startsWith("*")) {
			try {
				Object[] values = FORMAT.parse(line);
				String privateKey = ((String) values[0]).trim();
				String certificate = ((String) values[1]).trim();
				String option = ((String) values[2]).trim();
				String password = null;
				if (option.length() > 0) {
					values = OPTION_FORMAT.parse(option);
					password = ((String) values[0]).trim();
				}
				return new Signature(privateKey, certificate, password);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return Returns the certificate.
	 */
	public String getCertificate() {
		return certificate;
	}

	/**
	 * @param certificate The certificate to set.
	 */
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	/**
	 * @return Returns the privateKey.
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey The privateKey to set.
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (password == null) {
			return FORMAT.format(new Object[] { privateKey, certificate, "" });
		}
		return FORMAT.format(new Object[] { privateKey, certificate,
				OPTION_FORMAT.format(new Object[] { password }) });
	}
}
