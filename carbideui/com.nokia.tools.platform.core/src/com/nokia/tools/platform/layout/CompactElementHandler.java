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
package com.nokia.tools.platform.layout;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This handles the SAX parsing events and constructs the compact elements.
 * 
 */
public class CompactElementHandler extends DefaultHandler {
	private CompactElement root;
	private Stack<CompactElement> stack = new Stack<CompactElement>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		CompactElement parent = stack.isEmpty() ? null : stack.pop();
		CompactElement element = new CompactElement(qName);
		int len = attributes.getLength();
		for (int i = 0; i < len; i++) {
			element
					.setAttribute(attributes.getQName(i), attributes
							.getValue(i));
		}
		if (parent != null) {
			parent.add(element);
			stack.push(parent);
		} else {
			root = element;
		}
		stack.push(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		CompactElement element = stack.pop();
		element.done();
	}
	
	

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		root.done();
		super.endDocument();
	}

	/**
	 * Resets the handler states.
	 */
	public void reset() {
		root = null;
		stack.clear();
	}

	/**
	 * @return the root element.
	 */
	public CompactElement getRoot() {
		return root;
	}
}
