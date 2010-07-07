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
package com.nokia.tools.content.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * The content has tree structure. This class represents the node in the tree.
 * 
 */
public interface IContentData extends IAdaptable {
	/**
	 * Unique id of the content data.
	 * 
	 * @return the id of the content data.
	 */
	String getId();

	/**
	 * Returns the name of content data.
	 * 
	 * @return the name of content data.
	 */
	String getName();

	/**
	 * Returns the root node of the content tree.
	 * 
	 * @return the root node of the content tree.
	 */
	IContent getRoot();

	/**
	 * Returns the parent node.
	 * 
	 * @return the parent node.
	 */
	IContentData getParent();

	/**
	 * Adds the child node.
	 * 
	 * @param child the node to be added.
	 */
	void addChild(IContentData child);

	/**
	 * Adds the child node at the specified position.
	 * 
	 * @param index the position of the child node.
	 * @param child the node to be added.
	 */
	void addChild(int index, IContentData child);

	/**
	 * Removes the child node.
	 * 
	 * @param child the node to be removed.
	 */
	void removeChild(IContentData child);

	/**
	 * Removes all children.
	 */
	void removeAllChildren();

	/**
	 * Returns all child nodes.
	 * 
	 * @return all child nodes.
	 */
	IContentData[] getChildren();

	/**
	 * Returns true if this node has child nodes.
	 * 
	 * @return true if this node has child nodes, false otherwise.
	 */
	boolean hasChildren();

	/**
	 * Returns all children.
	 * 
	 * @return all children.
	 */
	IContentData[] getAllChildren();

	/**
	 * Sets the content attribute.
	 * 
	 * @param name the attribute name.
	 * @param value the attribute value.
	 */
	void setAttribute(String name, Object value);

	/**
	 * Returns the content attribute with the given name.
	 * 
	 * @param name name of the attribute.
	 * @return the content attribute.
	 */
	Object getAttribute(String name);

	/**
	 * Removes the attribute.
	 * 
	 * @param name name of attribute to be removed.
	 * @return the removed attribute value.
	 */
	Object removeAttribute(String name);

	/**
	 * Returns all attribute names.
	 */
	String[] getAttributeNames();

	/**
	 * Returns the icon of this node.
	 * 
	 * @return the icon of this node.
	 */
	Image getIcon();
	Image getIcon(boolean useLocId);
	
	/**
	 * Returns the image descriptor of this node.
	 * 
	 * @param width
	 * @param height
	 * @return the image descriptor of this node.
	 */
	ImageDescriptor getImageDescriptor(int width, int height);

	/**
	 * Finds the content by the given id.
	 * 
	 * @param id id of the content data.
	 * @return the content data or null if not found.
	 */
	IContentData findById(String id);

	/**
	 * Finds the content by the given name.
	 * 
	 * @param name name of the content data.
	 * @return the content data or null if not found.
	 */
	IContentData findByName(String name);
	public boolean useLocId();
	/**
	 * Stub implementation.
	 *
	 */
	public abstract class Stub implements IContentData {
		private List<IContentData> children = Collections
				.synchronizedList(new ArrayList<IContentData>());

		private Map<String, Object> attributes = new HashMap<String, Object>();

		private IContentData parent;

		private Map<String, Integer> childIndeses;
		
		private boolean useLocId = true;

		
		public boolean useLocId() {
			return useLocId;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getIcon()
		 */
		public Image getIcon() {
			return getIcon(true);
		}

		public Image getIcon(boolean useLocId) {
			ILabelProvider provider = (ILabelProvider) getAdapter(ILabelProvider.class);
			if (provider != null) {
				this.useLocId = useLocId;
				return provider.getImage(this);
			}
			return null;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getName()
		 */
		public String getName() {
			ILabelProvider provider = (ILabelProvider) getAdapter(ILabelProvider.class);
			if (provider != null) {
				return provider.getText(this);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#addChild(com.nokia.tools.content.core.IContentData)
		 */
		public void addChild(IContentData child) {
			addChild(-1, child);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#addChild(int,
		 *      com.nokia.tools.content.core.IContentData)
		 */
		public void addChild(int index, IContentData child) {
			if (child == this) {
				throw new IllegalArgumentException("Can't add self as child");
			}
			((Stub) child).parent = this;
			if (index < 0) {
				children.add(child);
			} else {
				children.add(index, child);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getAttribute(java.lang.String)
		 */
		public Object getAttribute(String name) {
			if (ContentAttribute.ID.name().equals(name)) {
				return getId();
			} else if (ContentAttribute.NAME.name().equals(name)) {
				return getName();
			}
			return attributes.get(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#removeAttribute(java.lang.String)
		 */
		public Object removeAttribute(String name) {
			return attributes.remove(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getAttributeNames()
		 */
		public String[] getAttributeNames() {
			return attributes.keySet().toArray(new String[attributes.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getChildren()
		 */
		public IContentData[] getChildren() {
			return children.toArray(new IContentData[children.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getParent()
		 */
		public IContentData getParent() {
			return parent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getRoot()
		 */
		public IContent getRoot() {
			IContentData parent = this;
			while (parent.getParent() != null) {
				parent = parent.getParent();
			}
			if (parent instanceof IContent) {
				return (IContent) parent;
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#hasChildren()
		 */
		public boolean hasChildren() {
			return !children.isEmpty();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#removeChild(com.nokia.tools.content.core.IContentData)
		 */
		public void removeChild(IContentData child) {
			if (children.remove(child)) {
				((Stub) child).parent = null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#removeAllChildren()
		 */
		public void removeAllChildren() {
			for (IContentData child : children
					.toArray(new IContentData[children.size()])) {
				removeChild(child);
			}
			children.clear();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#setAttribute(java.lang.String,
		 *      java.lang.Object)
		 */
		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getParent() == null ? getName()
					: (getRoot() + "$" + getName());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#findById(java.lang.String)
		 */
		public IContentData findById(String id) {
			return findById(this, id);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#findByName(java.lang.String)
		 */
		public IContentData findByName(String name) {
			return null;
		}

		/**
		 * Traverses down the content tree to find the node that matches the
		 * given id.
		 * 
		 * @param node the starting node.
		 * @param id id of the node.
		 * @return the node with the given id or null if not found.
		 */
		private static IContentData findById(IContentData node, String id) {
			if (id == null) {
				return null;
			}

			if (id.equals(node.getId())) {
				return node;
			}

			IContentData[] children = node.getChildren();
			for (IContentData child : children) {
				IContentData data = findById(child, id);
				if (data != null) {
					return data;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentData#getAllChildren()
		 */
		public IContentData[] getAllChildren() {
			Stack<IContentData> stack = new Stack<IContentData>();
			List<IContentData> list = new ArrayList<IContentData>();
			stack.push(this);
			while (!stack.isEmpty()) {
				IContentData child = stack.pop();
				if (child != this) {
					list.add(child);
				}
				if (child.getChildren() != null) {
					for (IContentData c : child.getChildren()) {
						stack.push(c);
					}
				}
			}
			return list.toArray(new IContentData[list.size()]);
		}

		/**
		 * Set on the specified index of children the <code>child</code>
		 * IContentData.
		 * 
		 * @param index replace index
		 * @param child replacing child
		 */
		public void replaceChild(int index, IContentData child) {
			children.set(index, child);
		}

		/**
		 * Returns the index of the child element. This index depends on the
		 * order, by which the child elements are defined in the theme xml file.
		 * 
		 * 
		 * @param element
		 * @return -1 if the element is not the direct child of this or has no children 
		 * or is not implemented
		 */
		public int getChildElementIndex(IContentData element) {
			if (childIndeses == null) {
				if (element.getChildren().length == 0) {
					return -1;
				}
				childIndeses = new HashMap<String, Integer>();
				for (int i = 0; i < this.getChildren().length; i++) {
					childIndeses.put(this.getChildren()[i].getId(), i);
				}
			}
			Integer integer = childIndeses.get(element.getId());
			if (integer == null) {
				integer = -1;
			}
			return integer;
		}

		public void setParent(IContentData parent) {
			this.parent = parent;
		}
	}
}
