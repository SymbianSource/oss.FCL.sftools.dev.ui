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
package com.nokia.tools.theme.s60.packaging.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents distinguished names used in creating certificates. The
 * distinguished names are name and value pairs, e.g. CN, OU, etc.
 */
public class DistinguishedNames {
	public static final String COMMON_NAME = "CN";
	public static final String COUNTRY = "C";
	public static final String ORGANIZATION = "O";
	public static final String ORGANIZATION_UNIT = "OU";
	public static final String EMAIL = "EM";

	private static final String[] ALLOWED = { COMMON_NAME, COUNTRY,
			ORGANIZATION, ORGANIZATION_UNIT, EMAIL };

	private Map<String, String> names = new HashMap<String, String>();

	/**
	 * Constructs a new distinguished names with provided common name (CN).
	 * 
	 * @param commonName the common name.
	 */
	public DistinguishedNames(String commonName) {
		setName(COMMON_NAME, commonName);
	}

	/**
	 * Tests if the name is a valid name identifier.
	 * 
	 * @param name name to be tested.
	 * @exception IllegalArgumentException if the name is not recognized.
	 */
	private void testName(String name) {
		boolean isAllowed = false;
		for (String allowed : ALLOWED) {
			if (allowed.equals(name)) {
				isAllowed = true;
				break;
			}
		}
		if (!isAllowed) {
			throw new IllegalArgumentException("The name is not valid: " + name);
		}
	}

	/**
	 * Sets the name-value pair.
	 * 
	 * @param name the name.
	 * @param value the value.
	 * @exception IllegalArgumentException if the name is not recognized.
	 */
	public void setName(String name, String value) {
		testName(name);
		if (value == null || (value = value.trim()).length() == 0) {
			throw new IllegalArgumentException("The value is not valid: "
					+ value);
		}

		names.put(name, value);
	}

	/**
	 * Removes the pair with the given name.
	 * 
	 * @param name name of the pair to be removed.
	 */
	public void remove(String name) {
		testName(name);
		if (COMMON_NAME.equals(name)) {
			throw new IllegalArgumentException(
					"The common name cannot be removed.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String name : names.keySet()) {
			sb.append(name + "=" + names.get(name));
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
