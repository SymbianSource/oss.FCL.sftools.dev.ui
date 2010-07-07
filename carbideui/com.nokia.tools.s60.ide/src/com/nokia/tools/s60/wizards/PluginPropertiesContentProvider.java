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
package com.nokia.tools.s60.wizards;

import java.net.URL;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.ILayoutDescriptor;
import com.nokia.tools.platform.extension.ILayoutFontDescriptor;
import com.nokia.tools.platform.extension.ILayoutSetDescriptor;
import com.nokia.tools.platform.extension.ILayoutVariantDescriptor;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeDesignDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PluginEntry;

public class PluginPropertiesContentProvider extends ArrayContentProvider {
	private PluginContentProvider provider;

	public PluginPropertiesContentProvider(PluginContentProvider provider) {
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		PluginProperties props = new PluginProperties();
		if (inputElement == PluginContentProvider.PLUGIN_ROOT) {
			if (provider.getRoot() instanceof PluginEntry) {
				PluginEntry entry = (PluginEntry) provider.getRoot();
				props.property("Id", entry.getSymbolicName());
				props.property("Name", entry.getName());
				props.property("Version", entry.getVersion());
				props.property("Vendor", entry.getVendor());
			} else if (provider.getRoot() instanceof Bundle) {
				Bundle bundle = (Bundle) provider.getRoot();
				props.property("Id", bundle.getSymbolicName());
				props.property("Name", bundle.getHeaders().get(
						Constants.BUNDLE_NAME));
				props.property("Version", bundle.getHeaders().get(
						Constants.BUNDLE_VERSION));
				props.property("Vendor", bundle.getHeaders().get(
						Constants.BUNDLE_VENDOR));
			}
		} else if (inputElement instanceof IDevice) {
			IDevice device = (IDevice) inputElement;

			props.property("Id", device.getId());
			props.property("Name", device.getName());
			props.property("Width", device.getDisplay().getWidth());
			props.property("Height", device.getDisplay().getHeight());
			props.property("Variant", device.getDisplay().getVariant());
		} else if (inputElement instanceof ILayoutSetDescriptor) {
			ILayoutSetDescriptor layouts = (ILayoutSetDescriptor) inputElement;
			props.property("Id", layouts.getId());
			ILayoutFontDescriptor fonts = layouts.getFontDescriptor();
			if (fonts != null) {
				props.property("Default Font", fonts.getDefaultFont());
				props.property("Font", changePath(fonts.getFontPath()));
				props.separator();
				props.title("Font Mappings:");
				Map<String, String> mappings = fonts.getFontMappings();
				for (String key : mappings.keySet()) {
					props.property(key, mappings.get(key));
				}
			}
		} else if (inputElement instanceof ILayoutDescriptor) {
			ILayoutDescriptor layout = (ILayoutDescriptor) inputElement;
			props.property("Id", layout.getId());
			props.property("Name", layout.getName());
			props.property("Device Id", layout.getDeviceId());
			props.separator();
			props.title("Variants:");

			for (ILayoutVariantDescriptor variant : layout.getVariants()) {
				props.property("Variant Id", variant.getId());
				for (URL componentPath : variant.getComponentPaths()) {
					props.property("Component", changePath(componentPath));
				}
				for (URL attributePath : variant.getAttributePaths()) {
					props.property("Attribute", changePath(attributePath));
				}
			}
		} else if (inputElement instanceof IThemeDescriptor) {
			IThemeDescriptor theme = (IThemeDescriptor) inputElement;
			props.property("Id", theme.getId());
			props.property("Container", theme.getContainerId());
			props.property("Layout Set", theme.getLayoutSet());
			props.property("Default Device", theme.getDefaultDeviceId());
			props.separator();
			props.title("Designs:");
			for (IThemeDesignDescriptor design : theme.getDesigns()) {
				URL path = design.getPath();
				props.property(null, changePath(path));
			}
			props.separator();
			props.title("Previews:");
			for (URL url : theme.getPreviewPaths()) {
				props.property(null, changePath(url));
			}
			props.separator();
			props.title("Settings:");
			for (URL url : theme.getSettings()) {
				props.property(null, changePath(url));
			}
			props.separator();
			props.title("Id Mappings:");
			for (URL url : theme.getIdMappingPaths()) {
				props.property(null, changePath(url));
			}
			props.separator();
			props.title("Dimensions:");
			for (URL url : theme.getDimensions()) {
				props.property(null, changePath(url));
			}
		} else if (inputElement instanceof IThemeModelDescriptor) {
			IThemeModelDescriptor model = (IThemeModelDescriptor) inputElement;
			props.property("Id", model.getId());
			props.property("name", model.getName());
			props.property("Theme Id", model.getThemeId());
			props.property("Path", changePath(model.getModelPath()));
		}
		return props.getProperties();
	}
	
	private String changePath(URL url) {
		if (url == null) {
			return null;
		}
		
		String pluginPath = "/Eclipse/plugins/com.nokia.tools.theme.s60";
		String path = url.getPath().toString();
		if(path.contains("data")) {
			return pluginPath.concat(path);
		}
		else {
			return path;
		}
	}
}
