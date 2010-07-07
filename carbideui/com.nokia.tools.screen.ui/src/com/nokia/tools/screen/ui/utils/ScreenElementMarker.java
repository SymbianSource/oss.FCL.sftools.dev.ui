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
package com.nokia.tools.screen.ui.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;

public class ScreenElementMarker implements IMarker {

	private Map<String, Object> attribs = new HashMap<String, Object>(10);

	public ScreenElementMarker(IScreenElement screenElement) {
		String realName = (screenElement != null) ? screenElement.getText()
				: screenElement.getData().getName();
		attribs.put(IMarker.MESSAGE, realName + " ("
				+ screenElement.getRoot().getText() + ")");
		if (null != screenElement.getData().getId()) {
			attribs.put(ContentAttribute.ID.name(), screenElement.getData()
					.getId());
		} else {
			attribs.put(ContentAttribute.NAME.name(), screenElement.getData()
					.getName());
		}
		IContentData screen = screenElement.getRoot().getData();
		if (screen.getAdapter(IScreenAdapter.class) == null) {
			// the element is selected from the component view and thus the root
			// is not any screen
			IScreenFactory factory = (IScreenFactory) screenElement.getData()
					.getRoot().getAdapter(IScreenFactory.class);
			for (IContentData data : factory.getScreens()) {
				if (data.findById(screenElement.getData().getId()) != null) {
					screen = data;
					// created
					break;
				}
			}
		}
		attribs.put(ScreenEditorPart.SCREEN, screen.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#delete()
	 */
	public void delete() throws CoreException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#exists()
	 */
	public boolean exists() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String attributeName) {
		return attribs.get(attributeName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String,
	 *      int)
	 */
	public int getAttribute(String attributeName, int defaultValue) {
		Object value = attribs.get(attributeName);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String,
	 *      java.lang.String)
	 */
	public String getAttribute(String attributeName, String defaultValue) {
		Object value = attribs.get(attributeName);
		if (value instanceof String) {
			return ((String) value);
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String,
	 *      boolean)
	 */
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		Object value = attribs.get(attributeName);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttributes()
	 */
	public Map<String, Object> getAttributes() throws CoreException {
		return attribs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getAttributes(java.lang.String[])
	 */
	public Object[] getAttributes(String[] attributeNames) throws CoreException {
		Object[] result = new Object[attributeNames.length];
		for (int i = 0; i < attributeNames.length; i++)
			result[i] = getAttribute(attributeNames[i]);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getCreationTime()
	 */
	public long getCreationTime() throws CoreException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getId()
	 */
	public long getId() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getResource()
	 */
	public IResource getResource() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#getType()
	 */
	public String getType() throws CoreException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#isSubtypeOf(java.lang.String)
	 */
	public boolean isSubtypeOf(String superType) throws CoreException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String,
	 *      int)
	 */
	public void setAttribute(String attributeName, int value)
			throws CoreException {
		attribs.put(attributeName, new Integer(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	public void setAttribute(String attributeName, Object value)
			throws CoreException {
		attribs.put(attributeName, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String,
	 *      boolean)
	 */
	public void setAttribute(String attributeName, boolean value)
			throws CoreException {
		attribs.put(attributeName, new Boolean(value));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map attributes) throws CoreException {
		throw new CoreException(new Status(Status.ERROR, null, 0,
				"Not supported setting attributes", null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IMarker#setAttributes(java.lang.String[],
	 *      java.lang.Object[])
	 */
	public void setAttributes(String[] attributeNames, Object[] values)
			throws CoreException {
		for (int i = 0; i < attributeNames.length; i++)
			setAttribute(attributeNames[i], values[i]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

}
