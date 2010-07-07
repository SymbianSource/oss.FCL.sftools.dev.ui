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
package com.nokia.tools.theme.s60;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.AbstractThemeProvider;
import com.nokia.tools.theme.content.IThemeContentProvider;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.s60.cstore.ComponentPoolBackend;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;

public class S60ThemeContent extends ThemeContent {
	private IThemeContentProvider contentProvider;

	public S60ThemeContent(EditObject resource, AbstractThemeProvider provider) {
		super(resource, provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContent#getType()
	 */
	public String getType() {
		return S60ThemeProvider.CONTENT_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeContent#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (IThemeContentProvider.class == adapter) {
			synchronized (this) {
				if (contentProvider == null) {
					contentProvider = new S60ThemeContentProvider(this);
				}
			}
			return contentProvider;
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeContent#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		if (name.equals(ContentAttribute.PLATFORM.name())) {
			String version;
			if (DevicePlatform.S60_5_0 == value) {
				version = ThemeTag.VERSION_FIVE_DOT_ZERO;
			}
			else if(DevicePlatform.SF_2 == value){
				version = ThemeTag.VERSION_SYMBIAN_2;
			}
			else {
				version = ThemeTag.VERSION_UNSPECIFIED;
			}

			getData().setSelectedVersion(version);

		} else {
			super.setAttribute(name, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeContent#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		Theme theme = (Theme) getData();
		if (ContentAttribute.PLATFORM.name().equals(name)) {
			String version = theme.getSelectedVersion();
			if (ThemeTag.VERSION_FIVE_DOT_ZERO.equals(version)) {
				return DevicePlatform.S60_5_0;
			}
			else if(ThemeTag.VERSION_SYMBIAN_2.equals(version)){
				return DevicePlatform.SF_2;
			}
			return ((Theme) getData()).getThemeDescriptor()
					.getDefaultPlatform();
		}
		return super.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeContent#dispose()
	 */
	@Override
	public void dispose() {
		// clears the reference to this theme and causes a refresh in the
		// subsequent calls to the parsed themes
		ComponentPoolBackend.clear();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeContent#save(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void save(IProgressMonitor monitor) throws IOException,
			ContentException {
		try {
			TSDataUtilities.resolveColorize(this);
		} finally {
			super.save(monitor);
		}
	}
}
