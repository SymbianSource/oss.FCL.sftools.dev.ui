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

package com.nokia.tools.platform.extension;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;

public class PlatformExtensionManager {

	public static final String PLATFORM_CONTRIBUTOR_ID = "com.nokia.tools.platform.core.platforms";

	public static final String DEVICE_CONTRIBUTOR_ID = "com.nokia.tools.platform.core.devices";

	public static final String LAYOUT_CONTRIBUTOR_ID = "com.nokia.tools.platform.core.layouts";

	public static final String THEME_CONTRIBUTOR_ID = "com.nokia.tools.platform.core.themes";

	private static final String EXT_PLATFORM = "platform";

	private static final String EXT_DEVICE = "device";

	private static final String EXT_LAYOUT_SET = "layoutSet";

	private static final String EXT_LAYOUT = "layout";

	private static final String EXT_VARIANT = "variant";

	private static final String EXT_FONT = "font";

	private static final String EXT_MAPPING = "mapping";

	private static final String EXT_CONTAINER = "container";

	private static final String EXT_THEME = "theme";

	private static final String EXT_THEME_MODEL = "themeModel";

	private static final String EXT_DESIGN = "design";

	private static final String EXT_PREVIEW = "preview";

	private static final String EXT_EXTENDED_DEFAULT_DESIGN = "extended-design";

	private static final String EXT_EXTENDED_DEFAULT_PREVIEW = "extended-preview";

	private static final String EXT_SETTING = "setting";

	private static final String EXT_DIMENSION = "dimension";

	private static final String EXT_IDMAPPING = "idmapping";

	private static final String EXT_LAYOUT_GROUP = "layoutGroup";

	private static final String EXT_SOUND_FORMAT = "soundFormat";

	private static final String EXT_FORMAT = "format";

	private static final String ATTR_NAME = "name";

	private static final String ATTR_ID = "id";

	private static final String ATTR_CONTENT_TYPE = "contentType";

	private static final String ATTR_WIDTH = "width";

	private static final String ATTR_HEIGHT = "height";

	private static final String ATTR_TYPE = "type";

	private static final String ATTR_VARIANT = "variant";

	private static final String ATTR_DEVICE_ID = "deviceId";

	private static final String ATTR_PATH = "path";

	private static final String ATTR_DEFAULT = "default";

	private static final String ATTR_VALUE = "value";

	private static final String ATTR_COMPONENT_PATH = "componentPath";

	private static final String ATTR_ATTRIBUTE_PATH = "attributePath";

	private static final String ATTR_LAYOUT_SET = "layoutSet";

	private static final String ATTR_CONTAINER_ID = "containerId";

	private static final String ATTR_MANAGER = "manager";

	private static final String ATTR_MODEL_PATH = "modelPath";

	private static final String ATTR_EXTENSION = "extension";

	private static final String ATTR_CUSTOMIZABLE = "customizable";

	private static final String ATTR_DEFAULT_DEVICE_ID = "defaultDeviceId";

	private static final String ATTR_THEME_ID = "themeId";

	private static final String ATTR_LARGE_ICON = "largeIcon";

	private static final String ATTR_SMALL_ICON = "smallIcon";

	private static final String ATTR_DESCRIPTION = "description";

	private static Map<String, IExtension[]> extensionCache = new HashMap<String, IExtension[]>();

	private static Set<PluginEntry> dynamicEntries = Collections
	    .synchronizedSet(new HashSet<PluginEntry>());

	public static void registerDynamicEntry(PluginEntry entry) {
		synchronized (extensionCache) {
			dynamicEntries.add(entry);
			extensionCache.clear();
		}
	}

	public static void deregisterDynamicEntry(PluginEntry entry) {
		synchronized (extensionCache) {
			dynamicEntries.remove(entry);
			extensionCache.clear();
		}
	}

	public static IExtension[] getExtensions(String contributorId) {
		synchronized (extensionCache) {
			IExtension[] extensions = extensionCache.get(contributorId);
			if (extensions == null) {
				IExtensionPoint point = Platform.getExtensionRegistry()
				    .getExtensionPoint(contributorId);
				IExtension[] staticExtensions = point.getExtensions();
				if (dynamicEntries.isEmpty()) {
					extensions = staticExtensions;
				} else {
					List<IExtension> list = new ArrayList<IExtension>();
					for (PluginEntry entry : dynamicEntries) {
						for (IExtension extension : entry
						    .getExtensions(contributorId)) {
							list.add(extension);
						}
					}
					IExtension[] dynamicExtensions = list
					    .toArray(new IExtension[list.size()]);
					extensions = new IExtension[staticExtensions.length
					    + dynamicExtensions.length];
					
					System.arraycopy(dynamicExtensions, 0, extensions, 0,
					    dynamicExtensions.length);
					System.arraycopy(staticExtensions, 0, extensions,
					    dynamicExtensions.length, staticExtensions.length);

				}
			}
			return extensions;
		}
	}

	public static IPlatform[] getPlatforms() {
		IExtension[] extensions = getExtensions(PLATFORM_CONTRIBUTOR_ID);
		List<IPlatform> platforms = new ArrayList<IPlatform>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
			    .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (EXT_PLATFORM.equals(element.getName())) {
					IPlatform platform = new PlatformImpl(element);
					platforms.add(platform);
				}
			}
		}
		return platforms.toArray(new IPlatform[platforms.size()]);
	}

	public static IDevice[] getDevices() {
		return getDevices(getExtensions(DEVICE_CONTRIBUTOR_ID));
	}

	public static IDevice[] getDevices(IExtension[] extensions) {
		List<IDevice> devices = new ArrayList<IDevice>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
			    .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (EXT_DEVICE.equals(element.getName())) {
					IDevice device = new DeviceImpl(element);
					devices.add(device);
				}
			}
		}
		return devices.toArray(new IDevice[devices.size()]);
	}

	public static ILayoutSetDescriptor[] getLayoutSets() {
		return getLayoutSets(getExtensions(LAYOUT_CONTRIBUTOR_ID));
	}

	public static ILayoutSetDescriptor[] getLayoutSets(IExtension[] extensions) {
		List<ILayoutSetDescriptor> layouts = new ArrayList<ILayoutSetDescriptor>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
			    .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (!EXT_LAYOUT_SET.equals(element.getName())) {
					continue;
				}

				ILayoutSetDescriptor set = new LayoutSetDescriptorImpl(element);
				layouts.add(set);
			}
		}
		return layouts.toArray(new ILayoutSetDescriptor[layouts.size()]);
	}

	public static IThemeContainerDescriptor[] getThemeContainerDescriptors() {
		IExtension[] extensions = getExtensions(THEME_CONTRIBUTOR_ID);
		List<IThemeContainerDescriptor> containers = new ArrayList<IThemeContainerDescriptor>();

		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
			    .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (!EXT_CONTAINER.equals(element.getName())) {
					continue;
				}
				IThemeContainerDescriptor container = new ThemeContainerDescriptorImpl(
				    element);
				containers.add(container);
			}
		}
		return containers.toArray(new IThemeContainerDescriptor[containers
		    .size()]);
	}

	public static IThemeDescriptor[] getThemeDescriptors() {
		return getThemeDescriptors(getExtensions(THEME_CONTRIBUTOR_ID));
	}

	public static IThemeDescriptor[] getThemeDescriptors(IExtension[] extensions) {
		List<IThemeDescriptor> themes = new ArrayList<IThemeDescriptor>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
			    .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (!EXT_THEME.equals(element.getName())) {
					continue;
				}
				IThemeDescriptor theme = new ThemeDescriptorImpl(element);
				themes.add(theme);
			}
		}
		return themes.toArray(new IThemeDescriptor[themes.size()]);
	}

	public static IThemeModelDescriptor[] getThemeModelDescriptors() {
		return getThemeModelDescriptors(getExtensions(THEME_CONTRIBUTOR_ID));
	}

	public static IThemeModelDescriptor[] getThemeModelDescriptors(
	    IExtension[] extensions) {
		List<IThemeModelDescriptor> models = new ArrayList<IThemeModelDescriptor>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
			    .getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (!EXT_THEME_MODEL.equals(element.getName())) {
					continue;
				}
				IThemeModelDescriptor theme = new ThemeModelDescriptorImpl(
				    element);
				models.add(theme);
			}
		}
		return models.toArray(new IThemeModelDescriptor[models.size()]);
	}

	private static ImageDescriptor createImageDescriptor(
	    IConfigurationElement element, String path) {
		if (!StringUtils.isEmpty(path)) {
			URL url = FileUtils.getURL(element, path);
			if (FileUtils.resourceExists(url)) {
				return ImageDescriptor.createFromURL(url);
			}
		}
		return null;
	}


	static class PlatformImpl
	    implements IPlatform {

		String id, name;

		PlatformImpl(IConfigurationElement element) {
			id = element.getAttribute(ATTR_ID);
			name = element.getAttribute(ATTR_NAME);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IPlatform#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IPlatform#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getId() + ":" + getName();
		}

	}


	static class DeviceImpl
	    implements IDevice {

		Display display;

		String id, name;

		DeviceImpl(IConfigurationElement element) {
			int width = 0, height = 0;
			try {
				width = Integer.parseInt(element.getAttribute(ATTR_WIDTH));
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
			try {
				height = Integer.parseInt(element.getAttribute(ATTR_HEIGHT));
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
			String variant = element.getAttribute(ATTR_VARIANT);
			String type = element.getAttribute(ATTR_TYPE);
			display = new Display(width, height, variant, type);
			id = element.getAttribute(ATTR_ID);
			name = element.getAttribute(ATTR_NAME);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IDevice#getDisplay()
		 */
		public Display getDisplay() {
			return display;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IDevice#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IDevice#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IDevice) {
				IDevice b = (IDevice) obj;
				return getId().equals(b.getId())
				    && getName().equals(b.getName())
				    && getDisplay().equals(b.getDisplay());
			}
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return getId().hashCode() ^ getName().hashCode()
			    ^ getDisplay().hashCode();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return getId() + ":" + getName() + ":" + getDisplay();
		}
	}


	static class LayoutSetDescriptorImpl
	    implements ILayoutSetDescriptor {

		ILayoutFontDescriptor fontDescriptor;

		String id;

		ILayoutDescriptor[] layouts;

		LayoutSetDescriptorImpl(IConfigurationElement element) {
			IConfigurationElement[] children = element.getChildren(EXT_FONT);
			if (children.length > 0) {
				fontDescriptor = new LayoutFontDescriptorImpl(children[0]);
			}
			id = element.getAttribute(ATTR_ID);
			children = element.getChildren(EXT_LAYOUT);
			layouts = new ILayoutDescriptor[children.length];
			for (int i = 0; i < children.length; i++) {
				layouts[i] = new LayoutDescriptorImpl(children[i]);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutSetDescriptor#getFontDescriptor()
		 */
		public ILayoutFontDescriptor getFontDescriptor() {
			return fontDescriptor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutSetDescriptor#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutSetDescriptor#getLayoutDescriptors()
		 */
		public ILayoutDescriptor[] getLayoutDescriptors() {
			return layouts;
		}
	}


	static class LayoutFontDescriptorImpl
	    implements ILayoutFontDescriptor {

		URL fontPath;

		Map<String, String> map;

		String defaultFont;

		LayoutFontDescriptorImpl(IConfigurationElement font) {
			fontPath = FileUtils.getURL(font, font.getAttribute(ATTR_PATH));
			defaultFont = font.getAttribute(ATTR_DEFAULT);
			IConfigurationElement[] mappings = font.getChildren(EXT_MAPPING);
			map = new HashMap<String, String>(mappings.length);
			for (IConfigurationElement ce : mappings) {
				map
				    .put(ce.getAttribute(ATTR_NAME), ce
				        .getAttribute(ATTR_VALUE));
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutFontDescriptor#getFontPath()
		 */
		public URL getFontPath() {
			return fontPath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutFontDescriptor#getDefaultFont()
		 */
		public String getDefaultFont() {
			return defaultFont;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutFontDescriptor#getFontMappings()
		 */
		public Map<String, String> getFontMappings() {
			return map;
		}
	}


	static class LayoutDescriptorImpl
	    implements ILayoutDescriptor {

		IDevice device;

		String id;

		String name;

		String deviceId;

		ILayoutVariantDescriptor[] variants;

		LayoutDescriptorImpl(IConfigurationElement layout) {
			deviceId = layout.getAttribute(ATTR_DEVICE_ID);
			for (IDevice device : getDevices()) {
				if (device.getId().equals(deviceId)) {
					this.device = device;
					break;
				}
			}
			id = layout.getAttribute(ATTR_ID);
			name = layout.getAttribute(ATTR_NAME);
			IConfigurationElement[] children = layout.getChildren(EXT_VARIANT);
			Map<String, List<IConfigurationElement>> map = new HashMap<String, List<IConfigurationElement>>();
			for (IConfigurationElement child : children) {
				String id = child.getAttribute(ATTR_ID);
				List<IConfigurationElement> elements = map.get(id);
				if (elements == null) {
					elements = new ArrayList<IConfigurationElement>();
					map.put(id, elements);
				}
				elements.add(child);
			}
			variants = new ILayoutVariantDescriptor[map.size()];
			int index = 0;
			for (List<IConfigurationElement> elements : map.values()) {
				variants[index++] = new LayoutVariantDescriptorImpl(elements);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.ILayoutDescriptor#getDeviceId()
		 */
		public String getDeviceId() {
			return deviceId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutDescriptor#getDevice()
		 */
		public IDevice getDevice() {
			return device;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutDescriptor#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.ILayoutDescriptor#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutDescriptor#getVariant(java.lang.String)
		 */
		public ILayoutVariantDescriptor getVariant(String id) {
			if (id == null || variants == null) {
				return null;
			}
			for (ILayoutVariantDescriptor variant : variants) {
				if (id.equals(variant.getId())) {
					return variant;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutDescriptor#getVariants()
		 */
		public ILayoutVariantDescriptor[] getVariants() {
			return variants;
		}
	}


	static class LayoutVariantDescriptorImpl
	    implements ILayoutVariantDescriptor {

		List<URL> attrList, compList;

		String id;

		LayoutVariantDescriptorImpl(List<IConfigurationElement> elements) {
			id = elements.get(0).getAttribute(ATTR_ID);
			attrList = new ArrayList<URL>(elements.size());
			compList = new ArrayList<URL>(elements.size());
			for (IConfigurationElement element : elements) {
				String path = element.getAttribute(ATTR_ATTRIBUTE_PATH);
				if (path != null) {
					attrList.add(FileUtils.getURL(element, path));
				}
				path = element.getAttribute(ATTR_COMPONENT_PATH);
				if (path != null) {
					compList.add(FileUtils.getURL(element, path));
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutVariantDescriptor#getAttributePath()
		 */
		public URL[] getAttributePaths() {
			return attrList.toArray(new URL[attrList.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutVariantDescriptor#getComponentPath()
		 */
		public URL[] getComponentPaths() {
			return compList.toArray(new URL[compList.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.ILayoutVariantDescriptor#getId()
		 */
		public String getId() {
			return id;
		}
	}


	static class ThemeContainerDescriptorImpl
	    implements IThemeContainerDescriptor {

		String id;

		IConfigurationElement element;

		ImageDescriptor largeIconDescriptor;

		ImageDescriptor smallIconDescriptor;

		String name;

		String description;

		boolean isDefault;

		ThemeContainerDescriptorImpl(IConfigurationElement element) {
			this.element = element;
			id = element.getAttribute(ATTR_ID);
			String largeIcon = element.getAttribute(ATTR_LARGE_ICON);
			String smallIcon = element.getAttribute(ATTR_SMALL_ICON);
			largeIconDescriptor = createImageDescriptor(element, largeIcon);
			smallIconDescriptor = createImageDescriptor(element, smallIcon);
			description = element.getAttribute(ATTR_DESCRIPTION);
			name = element.getAttribute(ATTR_NAME);
			isDefault = Boolean.valueOf(element.getAttribute(ATTR_DEFAULT));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeContainerDescriptor#createManager()
		 */
		public IThemeManager createManager() {
			try {
				IThemeManager manager = (IThemeManager) element
				    .createExecutableExtension(ATTR_MANAGER);
				manager.setContainerId(id);
				return manager;
			} catch (Throwable e) {
				//Investigate why sun.reflect can't be found.
				//PlatformCorePlugin.error(e);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeContainerDescriptor#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeContainerDescriptor#isDefault()
		 */
		public boolean isDefault() {
			return isDefault;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeContainerDescriptor#getDescription()
		 */
		public String getDescription() {
			return description;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeContainerDescriptor#getLargeIconDescriptor()
		 */
		public ImageDescriptor getLargeIconDescriptor() {
			return largeIconDescriptor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeContainerDescriptor#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeContainerDescriptor#getSmallIconDescriptor()
		 */
		public ImageDescriptor getSmallIconDescriptor() {
			return smallIconDescriptor;
		}
	}


	static class ThemeModelDescriptorImpl
	    implements IThemeModelDescriptor {

		String themeId;

		String id;

		String name;

		boolean isDefault;

		URL modelPath;

		ThemeModelDescriptorImpl(IConfigurationElement element) {
			themeId = element.getAttribute(ATTR_THEME_ID);
			id = element.getAttribute(ATTR_ID);
			name = element.getAttribute(ATTR_NAME);
			isDefault = Boolean.valueOf(element.getAttribute(ATTR_DEFAULT));
			modelPath = FileUtils.getURL(element, element
			    .getAttribute(ATTR_MODEL_PATH));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeModelDescriptor#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeModelDescriptor#getModelPath()
		 */
		public URL getModelPath() {
			return modelPath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeModelDescriptor#getName()
		 */
		public String getName() {
			return name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeModelDescriptor#getThemeDescriptor()
		 */
		public IThemeDescriptor getThemeDescriptor() {
			return ThemePlatform.getThemeDescriptorById(themeId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeModelDescriptor#getThemeId()
		 */
		public String getThemeId() {
			return themeId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeModelDescriptor#isDefault()
		 */
		public boolean isDefault() {
			return isDefault;
		}
	}


	static class ThemeDescriptorImpl
	    implements IThemeDescriptor {

		String id;

		String contentType;

		String containerId;

		String layoutSet;

		String deviceId;

		IDevice defaultDevice;

		IPlatform defaultPlatform;

		IPlatform[] platforms;

		IThemeDesignDescriptor[] designs;

		URL[] dimensions;

		URL[] settings;

		URL[] previews;

		URL[] idMappings;

		URL extendedDefaultDesignPath;

		URL extendedDefaultPreviewPath;

		Map<String, String> soundFormats;

		IThemeLayoutGroupDescriptor[] layoutGroups;

		ImageDescriptor largeIconDescriptor;

		ImageDescriptor smallIconDescriptor;

		String description;

		ThemeDescriptorImpl(IConfigurationElement element) {
			id = element.getAttribute(ATTR_ID);
			contentType = element.getAttribute(ATTR_CONTENT_TYPE);
			containerId = element.getAttribute(ATTR_CONTAINER_ID);
			layoutSet = element.getAttribute(ATTR_LAYOUT_SET);
			deviceId = element.getAttribute(ATTR_DEFAULT_DEVICE_ID);
			if (deviceId != null) {
				for (IDevice device : getDevices()) {
					if (deviceId.equals(device.getId())) {
						defaultDevice = device;
						break;
					}
				}
			}

			IConfigurationElement[] children = element
			    .getChildren(EXT_PLATFORM);
			List<IPlatform> list = new ArrayList<IPlatform>(children.length);
			for (IConfigurationElement el : children) {
				String id = el.getAttribute(ATTR_ID);
				boolean isDefault = new Boolean(el.getAttribute(ATTR_DEFAULT));
				IPlatform platform = null;
				for (IPlatform p : DevicePlatform.getPlatforms()) {
					if (p.getId().equals(id)) {
						platform = p;
						break;
					}
				}
				if (platform == null) {
					PlatformCorePlugin.error("Platform: " + id
					    + " is not found");
				} else {
					list.add(platform);
					if (isDefault) {
						defaultPlatform = platform;
					}
				}
			}
			if (defaultPlatform == null) {
				PlatformCorePlugin
				    .error("No default platform defined for theme: " + id);
			}
			platforms = list.toArray(new IPlatform[list.size()]);
			children = element.getChildren(EXT_DESIGN);
			designs = new IThemeDesignDescriptor[children.length];
			for (int i = 0; i < children.length; i++) {
				designs[i] = new ThemeDesignDescriptorImpl(children[i]);
			}
			children = element.getChildren(EXT_DIMENSION);
			dimensions = new URL[children.length];
			for (int i = 0; i < children.length; i++) {
				dimensions[i] = FileUtils.getURL(element, children[i]
				    .getAttribute(ATTR_PATH));
			}
			children = element.getChildren(EXT_SETTING);
			settings = new URL[children.length];
			for (int i = 0; i < children.length; i++) {
				settings[i] = FileUtils.getURL(element, children[i]
				    .getAttribute(ATTR_PATH));
			}
			children = element.getChildren(EXT_PREVIEW);
			previews = new URL[children.length];
			for (int i = 0; i < children.length; i++) {
				previews[i] = FileUtils.getURL(element, children[i]
				    .getAttribute(ATTR_PATH));
			}
			extendedDefaultDesignPath = processExtendedContributions(element,
			    EXT_EXTENDED_DEFAULT_DESIGN);
			extendedDefaultPreviewPath = processExtendedContributions(element,
			    EXT_EXTENDED_DEFAULT_PREVIEW);
			children = element.getChildren(EXT_IDMAPPING);
			idMappings = new URL[children.length];
			for (int i = 0; i < children.length; i++) {
				idMappings[i] = FileUtils.getURL(element, children[i]
				    .getAttribute(ATTR_PATH));
			}

			soundFormats = new HashMap<String, String>();
			children = element.getChildren(EXT_SOUND_FORMAT);
			if (children.length > 0) {
				children = children[0].getChildren(EXT_FORMAT);
				for (IConfigurationElement child : children) {
					String name = child.getAttribute(ATTR_NAME);
					String extension = child.getAttribute(ATTR_EXTENSION);
					soundFormats.put(name, extension);
				}
			}
			IConfigurationElement[] layouts = element
			    .getChildren(EXT_LAYOUT_GROUP);
			layoutGroups = new IThemeLayoutGroupDescriptor[layouts.length];
			for (int i = 0; i < layouts.length; i++) {
				layoutGroups[i] = new ThemeLayoutGroupDescriptorImpl(layouts[i]);
			}
			String largeIcon = element.getAttribute(ATTR_LARGE_ICON);
			String smallIcon = element.getAttribute(ATTR_SMALL_ICON);
			largeIconDescriptor = createImageDescriptor(element, largeIcon);
			smallIconDescriptor = createImageDescriptor(element, smallIcon);
			description = element.getAttribute(ATTR_DESCRIPTION);
		}

		private URL processExtendedContributions(IConfigurationElement element,
		    String contributionName) {
			IConfigurationElement[] children = element
			    .getChildren(contributionName);
			if (null != children) {
				for (int i = 0; i < children.length; i++) {
					URL path = FileUtils.getURL(element, children[0]
					    .getAttribute(ATTR_PATH));
					return path;
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getLargeIconDescriptor()
		 */
		public ImageDescriptor getLargeIconDescriptor() {
			return largeIconDescriptor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getSmallIconDescriptor()
		 */
		public ImageDescriptor getSmallIconDescriptor() {
			return smallIconDescriptor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getDescription()
		 */
		public String getDescription() {
			return description;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getContentType()
		 */
		public String getContentType() {
			return contentType;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getDefaultDeviceId()
		 */
		public String getDefaultDeviceId() {
			return deviceId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getContainerId()
		 */
		public String getContainerId() {
			return containerId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getDefaultDevice()
		 */
		public IDevice getDefaultDevice() {
			return defaultDevice;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getDefaultPlatform()
		 */
		public IPlatform getDefaultPlatform() {
			return defaultPlatform == null ? DevicePlatform.UNSPECIFIED_PLATFORM
			    : defaultPlatform;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getPlatforms()
		 */
		public IPlatform[] getPlatforms() {
			return platforms.length == 0 ? new IPlatform[] { DevicePlatform.UNSPECIFIED_PLATFORM }
			    : platforms;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getDesigns()
		 */
		public IThemeDesignDescriptor[] getDesigns() {
			return designs;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getDimensions()
		 */
		public URL[] getDimensions() {
			return dimensions;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getSettings()
		 */
		public URL[] getSettings() {
			return settings;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getSoundFormats()
		 */
		public Map<String, String> getSoundFormats() {
			return soundFormats;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getId()
		 */
		public String getId() {
			return id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getIdMappingPaths()
		 */
		public URL[] getIdMappingPaths() {
			return idMappings;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getLayoutGroupDescriptors()
		 */
		public IThemeLayoutGroupDescriptor[] getLayoutGroupDescriptors() {
			return layoutGroups;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getLayoutSet()
		 */
		public String getLayoutSet() {
			return layoutSet;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getPreviewPaths()
		 */
		public URL[] getPreviewPaths() {
			return previews;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeDescriptor#getLayoutGroupDescriptor(com.nokia.tools.platform.core.IDevice)
		 */
		public IThemeLayoutGroupDescriptor getLayoutGroupDescriptor(
		    IDevice device) {
			for (IThemeLayoutGroupDescriptor desc : getLayoutGroupDescriptors()) {
				if (desc.getDevice().equals(device)) {
					return desc;
				}
			}
			return null;
		}

		public URL getExtendedDefaultDesignPath() {
			return extendedDefaultDesignPath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDescriptor#getExtendedDefaultPreviewPath()
		 */
		public URL getExtendedDefaultPreviewPath() {
			return extendedDefaultPreviewPath;
		}
	}


	static class ThemeDesignDescriptorImpl
	    implements IThemeDesignDescriptor {

		URL path;

		boolean isCustomizable;

		ThemeDesignDescriptorImpl(IConfigurationElement ce) {
			path = FileUtils.getURL(ce, ce.getAttribute(ATTR_PATH));
			isCustomizable = new Boolean(ce.getAttribute(ATTR_CUSTOMIZABLE));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDesignDescriptor#getURL()
		 */
		public URL getPath() {
			return path;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.extension.IThemeDesignDescriptor#isCustomizable()
		 */
		public boolean isCustomizable() {
			return isCustomizable;
		}
	}


	static class ThemeLayoutGroupDescriptorImpl
	    implements IThemeLayoutGroupDescriptor {

		IDevice device;

		String[] layoutIds;

		ThemeLayoutGroupDescriptorImpl(IConfigurationElement layout) {
			String deviceId = layout.getAttribute(ATTR_DEVICE_ID);
			if (deviceId != null) {
				for (IDevice device : getDevices()) {
					if (deviceId.equals(device.getId())) {
						this.device = device;
						break;
					}
				}
			}
			IConfigurationElement[] children = layout.getChildren(EXT_LAYOUT);
			layoutIds = new String[children.length];
			for (int j = 0; j < children.length; j++) {
				layoutIds[j] = children[j].getAttribute(ATTR_ID);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeLayoutGroupDescriptor#getDevice()
		 */
		public IDevice getDevice() {
			return device;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.platform.core.IThemeLayoutGroupDescriptor#getLayoutIds()
		 */
		public String[] getLayoutIds() {
			return layoutIds;
		}

	}
}
