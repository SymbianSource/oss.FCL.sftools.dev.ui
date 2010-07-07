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
 * This class represents the comment part of the package file. <br/><br/><a
 * href="http://www.symbian.com/developer/techlib/v9.1docs/doc_source/n10356/Installing-ref/PKG_format/index.html#Installing%2dref%2epkg%2dformat">Symbian
 * package file format specification</a>
 */
public class Comment {
	private static final MessageFormat FORMAT = new MessageFormat(";{0}");

	private String comment;

	/**
	 * Constructs a comment.
	 * 
	 * @param comment comment text.
	 */
	public Comment(String comment) {
		this.comment = comment;
	}

	/**
	 * Parses the line to the comment if the line is in the comment format.
	 * 
	 * @param line the line to be parsed.
	 * @return the comment statement if the format is correct, null otherwise.
	 */
	public static Comment parse(String line) {
		if (line.startsWith(";")) {
			try {
				String comment = (String) FORMAT.parse(line)[0];
				return new Comment(comment);
			} catch (Exception e) {
			}
		}
		return null;
	}

	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return FORMAT.format(new Object[] { comment });
	}
}
