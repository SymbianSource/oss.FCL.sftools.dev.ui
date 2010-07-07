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

/**
 * This class reprensents any statements that are not recognized by the
 * available parsers.
 */
public class OriginalStatement {
	private String statement;

	/**
	 * Constructs an original statement.
	 * 
	 * @param statement the statement content.
	 */
	public OriginalStatement(String statement) {
		this.statement = statement;
	}

	/**
	 * Parses the line to the statement.
	 * 
	 * @param line the line to be parsed.
	 * @return the statement.
	 */
	public static OriginalStatement parse(String line) {
		return new OriginalStatement(line);
	}

	/**
	 * @return Returns the statement.
	 */
	public String getStatement() {
		return statement;
	}

	/**
	 * @param statement The statement to set.
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return statement;
	}
}
