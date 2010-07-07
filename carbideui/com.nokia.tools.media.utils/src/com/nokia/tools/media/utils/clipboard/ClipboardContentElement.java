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

import java.util.ArrayList;
import java.util.List;

/**
 * metadata about one element of the set of elements representing clipboard content.
 * Holds link to original data object, and 0,1 or more metadata objects.
 * Can comprise a tree structure.
 */
public class ClipboardContentElement implements Comparable {
	
	private Object content;	
	private Object[] metadata;
	private int sortColumn;
	
	private List<ClipboardContentElement> childs;
	
	public ClipboardContentElement(Object data, int sortColumn, Object... metadata) {
		content = data;
		this.metadata = metadata;
		this.sortColumn = sortColumn;
	}

	public Object getContent() {
		return content;
	}

	public Object getMetadata() {
		return metadata.length > 0 ? metadata[0] : null;
	}
	
	public Object getMetadata(int index) {
		return metadata.length > index ? metadata[index] : null;
	}
	
	public int getChildCount() {
		return childs == null ? 0 : childs.size();
	}
	
	public ClipboardContentElement getChild(int no) {
		return childs.get(no);
	}
	
	public synchronized void addChild(ClipboardContentElement child) {
		if (childs == null)
			childs = new ArrayList<ClipboardContentElement>();
		childs.add(child);
	}

	public int compareTo(Object o) {
		if (o instanceof ClipboardContentElement && getMetadata(sortColumn) instanceof String &&
				((ClipboardContentElement)o).getMetadata(((ClipboardContentElement)o).sortColumn) instanceof String) {
			return ((String)getMetadata(sortColumn)).compareTo((String) ((ClipboardContentElement)o).getMetadata(((ClipboardContentElement)o).sortColumn));
		}
		return 0;
	}
	
}
