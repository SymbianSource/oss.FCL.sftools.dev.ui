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
 * This class represents the conditional statements (if-else-endif). <br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class ConditionalStatement {
	private String statement;

	/**
	 * Constructs a conditional statement.
	 * 
	 * @param statement the statement body.
	 */
	public ConditionalStatement(String statement) {
		this.statement = statement;
	}

	/**
	 * Parses the line to the conditional statement if the line follows the
	 * specification.
	 * 
	 * @param line the line to be parsed.
	 * @return the conditional statement or null.
	 */
	public static ConditionalStatement parse(String line) {
		line = line.toUpperCase();
		if (line.startsWith("IF") || line.startsWith("ELSE")
				|| line.startsWith("ENDIF")) {
			try {
				return new ConditionalStatement(line);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return the statement
	 */
	public String getStatement() {
		return statement;
	}

	/**
	 * @param statement the statement to set
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
