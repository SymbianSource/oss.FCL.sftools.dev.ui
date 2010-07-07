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
package com.nokia.tools.packaging;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the context used in the packaging process. All
 * information related to the packaging should be specified in the context.
 * 
 */
public class PackagingContext {
	private Map<String, Object> attributes = new HashMap<String, Object>();

	/**
	 * Sets the input object.
	 * 
	 * @param value the input value.
	 */
	public void setInput(Object value) {
		setAttribute(PackagingAttribute.input.name(), value);
	}

	/**
	 * @return the input object.
	 */
	public Object getInput() {
		return getAttribute(PackagingAttribute.input.name());
	}

	/**
	 * Sets the output object.
	 * 
	 * @param value the output value.
	 */
	public void setOutput(Object value) {
		setAttribute(PackagingAttribute.output.name(), value);
	}

	/**
	 * @return the output value.
	 */
	public Object getOutput() {
		return getAttribute(PackagingAttribute.output.name());
	}

	/**
	 * Sets the attribute {name, value} pair.
	 * 
	 * @param name name of the attribute.
	 * @param value value of the attribute.
	 */
	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	/**
	 * Returns the attribute value.
	 * 
	 * @param name name of attribute.
	 * @return value of attribute.
	 */
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	/**
	 * Removes the attribute of the given name.
	 * 
	 * @param name name of the attribute to be removed.
	 */
	public void removeAttribute(String name) {
		attributes.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return attributes.toString();
	}
}
