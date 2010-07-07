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

package com.nokia.tools.theme.s60.internal.utilities;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Generic tree implementation, for model purposes.
 * now acts as replacement for DefaultMutableTreeNode in layer model.
 */
public class GenericNode {
	
	public static final String PROPERTY_PARENT = "parent";
	public static final String PROPERTY_USER_OBJ = "userObject";
	
	public static final String EVENT_CHILD_ADDED = "childAdded";
	public static final String EVENT_CHILD_REMOVED = "childRemoved";
	public static final String EVENT_CHILD_REPLACED = "childReplaced";
	
	private PropertyChangeSupport propSup = new PropertyChangeSupport(this);
	
	private static Iterator<GenericNode> emptyIter = new ArrayList<GenericNode>().iterator();
	
	private List<GenericNode> childs;
	
	private Object userObject;
	
	private GenericNode parent;
	
	private boolean strict;
	
	//--------------- event support -------------------
	
	public boolean isStrict() {
		if (strict)
			return strict;
		else {
			
			if (getParent() != null)
				return getParent().isStrict();
			else
				return strict;
		}		
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public void addEventListener(PropertyChangeListener e) {
		propSup.addPropertyChangeListener(e);
	}
	
	public void removeEventListener(PropertyChangeListener e) {
		propSup.removePropertyChangeListener(e);
	}
	
	//----------------------------------
	
	public GenericNode(Object data) {
		userObject = data;
	}	
	
	private void checkList() {
		if (childs == null)
			childs = new ArrayList<GenericNode>();
	}
	
	public boolean isRoot() {
		return getParent() == null;
	}
	
	public boolean hasChilds() {
		return getChildCount() != 0;
	}
		
	public Object getUserObject() {
		return userObject;
	}
	
	public void setUserObject(Object obj) {
		if (obj == userObject)
			return;
		Object old = this.userObject;
		this.userObject = obj;
		propSup.firePropertyChange(PROPERTY_USER_OBJ, old, obj);
	}
	
	public int getChildCount() {
		return childs==null?0:childs.size();
	}
	
	public GenericNode getChild(int i) {		
		return childs==null?null:childs.get(i);
	}
	
	public Iterator<GenericNode> getChilds() {
		if (childs == null) {
			return emptyIter;
		}
		return childs.iterator();
	}
	
	/**
	 * replaces childs. Returns old one.
	 * @param i
	 * @param node
	 * @return
	 */
	public GenericNode setChild(int i, GenericNode node) {
		checkList();
		if (node.getParent() != null)
			throw new RuntimeException("Node " + node + " has already parent, is it part of different tree?");
		GenericNode old = childs.get(i);
		childs.set(i,node);
		node.setParent(this);
		old.setParent(null);
		propSup.firePropertyChange(EVENT_CHILD_REPLACED,old, node);
		return old;
	}
	
	public void addChild(GenericNode n) {	
		checkList();
		if (n.getParent() != null)
			throw new RuntimeException("Node " + n + " has already parent, is it part of different tree?");
		childs.add(n);
		n.setParent(this);
		propSup.firePropertyChange(EVENT_CHILD_ADDED, null, n);
	}
	
	public void addChild(int index, GenericNode child) {
		checkList();
		if (child.getParent() != null)
			throw new RuntimeException("Node " + child + " has already parent, is it part of different tree?");
		childs.add(index, child);
		propSup.firePropertyChange(EVENT_CHILD_ADDED, null, child);
	}
	
	public GenericNode addChild(Object o) {
		checkList();
		synchronized (this) {		
			childs.add(new GenericNode(o));
			childs.get(childs.size()-1).setParent(this);
		}
		propSup.firePropertyChange(EVENT_CHILD_ADDED, null, childs.get(childs.size()-1));
		return childs.get(childs.size()-1);
	}
	
	public GenericNode removeChild(int i) throws RuntimeException {
		if (childs == null) {
			if (!isStrict())
				return null;
			else
				throw new RuntimeException("Child index out of bounds: " + i);
		}
		GenericNode n = childs.remove(i);
		n.setParent(null);
		propSup.firePropertyChange(EVENT_CHILD_REMOVED, null, n);
		return n;
	}
	
	public GenericNode removeChild(GenericNode n) throws RuntimeException {
		if (childs == null)
			if (!isStrict())
				return null;
			else
				throw new RuntimeException("Child not found: " + n);
		if (childs.indexOf(n) != -1){
			childs.remove(childs.indexOf(n));
			n.setParent(null);
			propSup.firePropertyChange(EVENT_CHILD_REMOVED, null, n);
			return n;
		}
		if (!isStrict())
			return null;
		else
			throw new RuntimeException("Child not found: " + n);
	}

	public GenericNode getParent() {
		return parent;
	}

	private void setParent(GenericNode parent) {
		Object op = this.parent;
		this.parent = parent;
		propSup.firePropertyChange(PROPERTY_PARENT, op, parent);
	}
	
	@Override
	public String toString() {
		if (userObject != null)
			return "GenericNode:" + userObject.toString();
		else return "GenericNode:null("+super.toString()+")";
	}

}
