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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.framework.Bundle;

import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.ILayoutSetDescriptor;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.platform.extension.PluginEntry;
import com.nokia.tools.resource.util.FileUtils;

public class PluginContentProvider implements ITreeContentProvider {
	public static final Object EMPTY_INPUT = new Object();
	public static final Object PLUGIN_ROOT = new Object();

	private Object root;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof PluginEntry
				|| parentElement instanceof Bundle
				|| parentElement == EMPTY_INPUT) {
			return new Object[] { PLUGIN_ROOT };
		}

		if (parentElement == PLUGIN_ROOT) {
			if (root instanceof PluginEntry) {
				PluginEntry entry = (PluginEntry) root;
				List<IExtension[]> extensions = new ArrayList<IExtension[]>();
				for (String point : new String[] {
						PlatformExtensionManager.DEVICE_CONTRIBUTOR_ID,
						PlatformExtensionManager.LAYOUT_CONTRIBUTOR_ID,
						PlatformExtensionManager.THEME_CONTRIBUTOR_ID }) {
					IExtension[] exts = entry.getExtensions(point);
					if (exts != null && exts.length > 0) {
						extensions.add(exts);
					}
				}
				return extensions.toArray();
			}
			if (root instanceof Bundle) {
				List<IExtension[]> extensions = new ArrayList<IExtension[]>();
				for (String pointId : new String[] {
						PlatformExtensionManager.DEVICE_CONTRIBUTOR_ID,
						PlatformExtensionManager.LAYOUT_CONTRIBUTOR_ID,
						PlatformExtensionManager.THEME_CONTRIBUTOR_ID }) {
					IExtensionPoint point = Platform.getExtensionRegistry()
							.getExtensionPoint(pointId);
					List<IExtension> exts = new ArrayList<IExtension>();
					for (IExtension extension : point.getExtensions()) {
						Bundle bundle = Platform.getBundle(extension
								.getNamespaceIdentifier());
						if (bundle == root) {
							exts.add(extension);
						}
					}
					if (!exts.isEmpty()) {
						extensions.add(exts
								.toArray(new IExtension[exts.size()]));
					}
				}
				return extensions.toArray();
			}
		}
		if (parentElement instanceof IExtension[]) {
			IExtension[] extensions = (IExtension[]) parentElement;
			if (extensions.length == 0) {
				return null;
			}
			String point = extensions[0].getExtensionPointUniqueIdentifier();
			if (PlatformExtensionManager.DEVICE_CONTRIBUTOR_ID.equals(point)) {
				return PlatformExtensionManager.getDevices(extensions);
			}
			if (PlatformExtensionManager.LAYOUT_CONTRIBUTOR_ID.equals(point)) {
				return PlatformExtensionManager.getLayoutSets(extensions);
			}
			if (PlatformExtensionManager.THEME_CONTRIBUTOR_ID.equals(point)) {
				IThemeDescriptor[] themes = PlatformExtensionManager
						.getThemeDescriptors(extensions);
				IThemeModelDescriptor[] models = PlatformExtensionManager
						.getThemeModelDescriptors(extensions);
				Map<String, IThemeDescriptor> map = new HashMap<String, IThemeDescriptor>();
				for (IThemeDescriptor theme : themes) {
					String id = theme.getId();
					if (id != null) {
						map.put(id.toLowerCase(), theme);
					}
				}
				for (IThemeModelDescriptor model : models) {
					IThemeDescriptor existingTheme = model.getThemeDescriptor();
					if (existingTheme != null) {
						map.put(existingTheme.getId().toLowerCase(),
								existingTheme);
					}
				}
				return map.values().toArray();
			}
		}
		if (parentElement instanceof IThemeDescriptor) {
			IThemeDescriptor theme = (IThemeDescriptor) parentElement;
			if (root instanceof PluginEntry) {
				PluginEntry entry = (PluginEntry) root;
				IExtension[] extensions = entry
						.getExtensions(PlatformExtensionManager.THEME_CONTRIBUTOR_ID);
				IThemeModelDescriptor[] models = PlatformExtensionManager
						.getThemeModelDescriptors(extensions);
				List<IThemeModelDescriptor> list = new ArrayList<IThemeModelDescriptor>();
				for (IThemeModelDescriptor model : models) {
					if (theme.getId().equalsIgnoreCase(model.getThemeId())) {
						list.add(model);
					}
				}
				return list.toArray();
			}
		}
		if (parentElement instanceof ILayoutSetDescriptor) {
			ILayoutSetDescriptor layouts = (ILayoutSetDescriptor) parentElement;
			return layouts.getLayoutDescriptors();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof IExtension) {
			return root;
		}
		if (root instanceof PluginEntry) {
			PluginEntry entry = (PluginEntry) root;
			if (element instanceof IDevice) {
				return entry
						.getExtensions(PlatformExtensionManager.DEVICE_CONTRIBUTOR_ID);
			}
			if (element instanceof ILayoutSetDescriptor) {
				return entry
						.getExtensions(PlatformExtensionManager.LAYOUT_CONTRIBUTOR_ID);
			}
			if (element instanceof IThemeDescriptor) {
				return entry
						.getExtensions(PlatformExtensionManager.THEME_CONTRIBUTOR_ID);
			}
			if (element instanceof IThemeModelDescriptor) {
				IThemeModelDescriptor model = (IThemeModelDescriptor) element;
				IThemeDescriptor existingTheme = model.getThemeDescriptor();
				if (existingTheme != null) {
					return existingTheme;
				}
				IThemeDescriptor[] themes = PlatformExtensionManager
						.getThemeDescriptors(entry
								.getExtensions(PlatformExtensionManager.THEME_CONTRIBUTOR_ID));
				for (IThemeDescriptor theme : themes) {
					if (theme.getId().equalsIgnoreCase(model.getThemeId())) {
						return theme;
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		return children != null && children.length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput != null) {
			root = newInput;
		}
	}

	/**
	 * @return the root
	 */
	public Object getRoot() {
		return root;
	}

	public boolean hasConfigurationError() {
		PluginPropertiesContentProvider provider = new PluginPropertiesContentProvider(
				this);
		return hasConfigurationError(root, provider);
	}

	public boolean hasConfigurationError(Object parent,
			PluginPropertiesContentProvider provider) {
		Object[] elements = provider.getElements(parent);
		for (Object element : elements) {
			if (element instanceof Object[]) {
				for (Object object : (Object[]) element) {
					if (hasConfigurationError(object)) {
						return true;
					}
				}
			}
		}
		Object[] children = getChildren(parent);
		if (children != null) {
			for (Object child : children) {
				if (hasConfigurationError(child, provider)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasConfigurationError(Object value) {
		if (value instanceof URL) {
			if (!FileUtils.resourceExists((URL) value)) {
				if (root instanceof PluginEntry) {
					value = FileUtils.getBundlePath((URL) value);
					if (((PluginEntry) root).containsResource((String) value)) {
						return false;
					}
				}
			} else {
				return false;
			}
			return true;
		}
		return false;
	}
}
