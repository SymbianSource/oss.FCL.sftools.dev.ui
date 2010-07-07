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
package com.nokia.tools.media.font;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nokia.tools.media.core.Activator;
import com.nokia.tools.resource.util.FileUtils;

public class FontExtensionManager {
	public static final String FONT_CONTRIBUTOR_ID = Activator.PLUGIN_ID
			+ ".fonts";
	private static final String EXT_FONT = "font";
	private static final String ATTR_ID = "id";
	private static final String ATTR_TYPE = "type";
	private static final String ATTR_PATH = "path";

	private FontExtensionManager() {
	}

	public static IFontDescriptor[] getFontDescriptors() {
		if (Platform.getExtensionRegistry() == null) {
			// no platform runtime
			return new IFontDescriptor[0];
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(FONT_CONTRIBUTOR_ID);
		IExtension[] extensions = point.getExtensions();
		List<IFontDescriptor> descriptors = new ArrayList<IFontDescriptor>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (EXT_FONT.equals(element.getName())) {
					FontDescriptorImpl desc = new FontDescriptorImpl(element);
					descriptors.add(desc);
				}
			}
		}
		return descriptors.toArray(new IFontDescriptor[descriptors.size()]);
	}

	static class FontDescriptorImpl implements IFontDescriptor {
		private String id;
		private Type type;
		private URL path;

		FontDescriptorImpl(IConfigurationElement element) {
			id = element.getAttribute(ATTR_ID);
			String str = element.getAttribute(ATTR_TYPE);
			if (Type.BITMAP.name().equalsIgnoreCase(str)) {
				type = Type.BITMAP;
			} else {
				type = Type.TRUETYPE;
			}
			str = element.getAttribute(ATTR_PATH);
			path = FileUtils.getURL(element, str);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.font.IFontDescriptor#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.font.IFontDescriptor#getPath()
		 */
		public URL getPath() {
			return path;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.font.IFontDescriptor#getType()
		 */
		public Type getType() {
			return type;
		}
	}
}
