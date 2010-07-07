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
package com.nokia.tools.media.utils.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Metadata about clipboard content, used for 
 * Extended Copy/Paste op's using local JVM clipboard.
 */
public class ClipboardContentDescriptor implements ClipboardOwner {
	
	//dummy clipboard owner
	public static final ClipboardOwner DummyClipOwner = new ClipboardContentDescriptor(null, null);
	
	public enum ContentType {
		/**
		 * plain image / mask
		 */
		CONTENT_IMAGE,
		/**
		 * single-layer theme graphics
		 */
		CONTENT_GRAPHICS,
		/**
		 * multilayers-enabled theme graphics
		 */
		CONTENT_GRAPHICS_MULTILAYER,
		/**
		 * nine-piece skin
		 */
		CONTENT_GRAPHICS_NINEPIECE,
		/**
		 * nine-piece skin
		 */
		CONTENT_GRAPHICS_THREEPIECE,

		/**
		 * nine-piece skin
		 */
		CONTENT_GRAPHICS_ELEVENPIECE,
		
		/**
		 * element = graphics + information about element ID = semantic value
		 * element can be also category, not 'list' element.
		 * In this case clipboard contains list of ClipboardContentElements, which represents particular 
		 * node in clipboard. 
		 */
		CONTENT_ELEMENT,
		/**
		 * group of elements (maybe also Task, Component Group node from resource view).
		 * In this case clipboard contains list of ClipboardContentElements, which represents particular 
		 * node in clipboard. 
		 */
		CONTENT_ELEMENT_GROUP
	}
	
	private Object content;
	
	private ContentType type;
	
	private Map<Object, Object> attributes;
	
	public ClipboardContentDescriptor(Object content, ContentType type) {
		this.content = content;
		this.type = type;
	}
	
	public void setAttribute(Object key, Object val) {
		if (attributes == null)
			attributes = new HashMap<Object, Object>();
		attributes.put(key, val);
	}
	
	public Object getAttribute(Object key) {
		return attributes == null ? null : attributes.get(key);
	}
	
	public Object getContent() {
		return content;
	}
	
	public ContentType getType() {
		return type;
	}
	
	public Set<Object> getAttributeKeys() {
		return attributes == null ? null : attributes.keySet();
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {		
	}

}
