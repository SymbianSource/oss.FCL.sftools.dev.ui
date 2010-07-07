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
package com.nokia.tools.resource.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * This class provides a collection of convenient methods for handling strings.
 * 
 */
public class StringUtils {
	/**
	 * No instantiation.
	 */
	private StringUtils() {
	}

	/**
	 * Return true if s is null, or zero-length, or contains only white-spaces.
	 * 
	 * @param s the string to test.
	 * @return true if the string is empty, false otherwise.
	 */
	public static boolean isEmpty(String s) {
		if (s == null)
			return true;
		return s.trim().length() < 1;
	}

	/**
	 * Semantic comparison ("" == null)
	 * 
	 * @param s1 first string
	 * @param s2 second string
	 * @return true if the two strings are equal logically, false otherwise.
	 */
	public static boolean equals(String s1, String s2) {
		if (isEmpty(s1)) {
		    return isEmpty(s2);
		}
		return s1.equals(s2);
	}

	/**
	 * Extracts the stack trace from the throwable to string. If the throwable
	 * has a cause, the cause will be dumped instead.
	 * 
	 * @param e the throwable.
	 * @return the stack trace in text format.
	 */
	public static String dumpThrowable(Throwable e) {
		if (e == null) {
			return null;
		}
		if (e.getCause() != null) {
			e = e.getCause();
		}
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
	
	public static boolean isBlank(String s){
		if(s.equals("")){
			return isEmpty(s);
		}
		return false;
	}
	/**
	 * In method will append all elements in the array seperated by 
	 * a user specified delimator.<BR>
	 * Arrays.toString(Object[] objects) method doesn't have the flexibility of
	 * taking delimator from user.Also have to tweak extra to get rid of the "["
	 * and "]" chars in the inbuilt method.
	 * @param array
	 * @param delimiter
	 * @return String
	 */
	public static String formatArray(Object[] array, String delimiter) {
		if (array == null) {
			return null;
		}
		if (delimiter == null) {
			throw new IllegalArgumentException("The delimiter is null.");
		}
		StringBuilder sb = new StringBuilder();
		for (Object obj : array) {
			sb.append(obj).append(delimiter);
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - delimiter.length());
		}
		return sb.toString();
	}
}
