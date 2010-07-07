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
package com.nokia.tools.platform.theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.extension.IThemeContainerDescriptor;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeLayoutGroupDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.platform.layout.LayoutManager;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;

public class ThemePlatform {
	private static IThemeContainerDescriptor[] containerDescriptors;
	private static IThemeDescriptor[] themeDescriptors;
	private static IThemeModelDescriptor[] themeModelDescriptors;
	private static Set<String> soundExtensions;

	private static Map<String, IThemeManager> managers = new HashMap<String, IThemeManager>();
	private static Map<String, IdMappingsHandler> idMappingsHandlers = new HashMap<String, IdMappingsHandler>();

	private ThemePlatform() {
	}

	public static IDevice getDevice(String themeId, Display display) {
		if (display == null) {
			return null;
		}

		IThemeDescriptor descriptor = getThemeDescriptorById(themeId);
		if (descriptor == null) {
			return null;
		}
		for (IThemeLayoutGroupDescriptor desc : descriptor
				.getLayoutGroupDescriptors()) {
			IDevice device = desc.getDevice();
			if (display.equals(device.getDisplay())) {
				return device;
			}
		}
		return null;
	}

	public synchronized static IThemeContainerDescriptor[] getContainerDescriptors() {
		if (containerDescriptors == null) {
			containerDescriptors = PlatformExtensionManager
					.getThemeContainerDescriptors();
		}
		return containerDescriptors;
	}

	public synchronized static IThemeDescriptor[] getThemeDescriptors() {
		if (themeDescriptors == null) {
			themeDescriptors = PlatformExtensionManager.getThemeDescriptors();
		}
		return themeDescriptors;
	}

	public synchronized static IThemeModelDescriptor[] getThemeModelDescriptors() {
		if (themeModelDescriptors == null) {
			themeModelDescriptors = PlatformExtensionManager
					.getThemeModelDescriptors();
		}
		return themeModelDescriptors;
	}

	public static IThemeContainerDescriptor getContainerDescriptorById(
			String containerId) {
		if (containerId == null) {
			return null;
		}
		for (IThemeContainerDescriptor desc : getContainerDescriptors()) {
			if (containerId.equalsIgnoreCase(desc.getId())) {
				return desc;
			}
		}
		return null;
	}

	public static IThemeContainerDescriptor getContainerDescriptorByName(
			String containerName) {
		if (containerName == null) {
			return null;
		}
		for (IThemeContainerDescriptor desc : getContainerDescriptors()) {
			if (containerName.equalsIgnoreCase(desc.getName())) {
				return desc;
			}
		}
		return null;
	}

	public static IThemeDescriptor getThemeDescriptorById(String themeId) {
		if (themeId == null) {
			return null;
		}

		for (IThemeDescriptor theme : getThemeDescriptors()) {
			if (themeId.equalsIgnoreCase(theme.getId())) {
				return theme;
			}
		}
		return null;
	}

	public static IThemeModelDescriptor getThemeModelDescriptorById(
			String modelId) {
		if (modelId == null) {
			return null;
		}
		for (IThemeModelDescriptor model : getThemeModelDescriptors()) {
			if (modelId.equalsIgnoreCase(model.getId())) {
				return model;
			}
		}
		return null;
	}

	public static IThemeModelDescriptor getThemeModelDescriptorByName(
			String name) {
		if (name == null) {
			return null;
		}

		for (IThemeModelDescriptor model : getThemeModelDescriptors()) {
			if (name.equalsIgnoreCase(model.getName())) {
				return model;
			}
		}
		return null;
	}

	public static IThemeModelDescriptor[] getThemeModelDescriptorsByContainer(
			String containerId) {
		if (containerId == null) {
			return new IThemeModelDescriptor[0];
		}
		List<IThemeModelDescriptor> descriptors = new ArrayList<IThemeModelDescriptor>();
		for (IThemeModelDescriptor model : getThemeModelDescriptors()) {
			if (containerId.equals(model.getThemeDescriptor().getContainerId())) {
				descriptors.add(model);
			}
		}
		return descriptors
				.toArray(new IThemeModelDescriptor[descriptors.size()]);
	}

	public static IThemeDescriptor[] getThemeDescriptorsByContainer(
			String containerId) {
		if (containerId == null) {
			return new IThemeDescriptor[0];
		}
		List<IThemeDescriptor> descriptors = new ArrayList<IThemeDescriptor>();
		for (IThemeDescriptor descriptor : getThemeDescriptors()) {
			if (containerId.equalsIgnoreCase(descriptor.getContainerId())) {
				descriptors.add(descriptor);
			}
		}
		return descriptors.toArray(new IThemeDescriptor[descriptors.size()]);
	}

	public static IThemeModelDescriptor getDefaultThemeModelDescriptor(
			String containerId) {
		IThemeModelDescriptor[] descriptors = getThemeModelDescriptorsByContainer(containerId);
		for (IThemeModelDescriptor descriptor : descriptors) {
			if (descriptor.isDefault()) {
				return descriptor;
			}
		}
		return descriptors.length > 0 ? descriptors[0] : null;
	}

	public synchronized static IThemeManager getThemeManagerByContainerId(
			String containerId) {
		if (containerId == null) {
			return null;
		}
		IThemeManager manager = (IThemeManager) managers.get(containerId);
		if (manager == null) {
			for (IThemeContainerDescriptor container : getContainerDescriptors()) {
				if (containerId.equals(container.getId())) {
					manager = container.createManager();
					break;
				}
			}
			if (manager != null) {
				managers.put(containerId, manager);
			}
		}
		return manager;
	}

	public static IThemeManager getThemeManagerByThemeModelId(String modelId) {
		if (modelId == null) {
			return null;
		}

		String containerId = null;
		for (IThemeModelDescriptor model : getThemeModelDescriptors()) {
			if (modelId.equalsIgnoreCase(model.getId())) {
				containerId = model.getThemeDescriptor().getContainerId();
				break;
			}
		}
		if (containerId == null) {
			PlatformCorePlugin.error("No container found for theme model: "
					+ modelId);
			return null;
		}

		return getThemeManagerByContainerId(containerId);
	}

	public static IThemeManager getThemeManagerByThemeModelName(String modelName) {
		if (modelName == null) {
			return null;
		}

		String containerId = null;
		for (IThemeModelDescriptor model : getThemeModelDescriptors()) {
			if (modelName.equalsIgnoreCase(model.getName())) {
				containerId = model.getThemeDescriptor().getContainerId();
				break;
			}
		}
		if (containerId == null) {
			PlatformCorePlugin.error("No container found for theme: "
					+ modelName);
			return null;
		}

		return getThemeManagerByContainerId(containerId);
	}

	public synchronized static IdMappingsHandler getIdMappingsHandler(
			String themeId) {
		IdMappingsHandler handler = idMappingsHandlers.get(themeId);
		if (handler == null) {
			handler = new IdMappingsHandler(getThemeDescriptorById(themeId)
					.getIdMappingPaths());
			idMappingsHandlers.put(themeId, handler);
		}
		return handler;
	}

	public synchronized static void init() {
		if (DebugHelper.debugBatch()) {
			
			try {
				computeAllThemeLayouts();
			} catch (Exception e) {
				PlatformCorePlugin.error(e);
			}
		}
	}

	/**
	 * Releases all theme related data, useful in debugging the memory leak and
	 * is called when the debug/deepClean is turned on.
	 */
	public synchronized static void release() {
		LayoutManager.release();
		managers.clear();
		idMappingsHandlers.clear();
		containerDescriptors = null;
		themeModelDescriptors = null;
		themeDescriptors = null;
		soundExtensions = null;
	}

	public synchronized static boolean isSoundFile(String path) {
		if (soundExtensions == null) {
			soundExtensions = new HashSet<String>();
			for (IThemeDescriptor descriptor : getThemeDescriptors()) {
				for (String extension : descriptor.getSoundFormats().values()) {
					soundExtensions.add(extension);
				}
			}
		}
		return soundExtensions.contains("." + FileUtils.getExtension(path));
	}

	public static IDevice[] getDevicesByContainerId(String containerId) {
		List<IDevice> devices = new ArrayList<IDevice>();
		if (containerId != null) {
			for (IThemeDescriptor descriptor : getThemeDescriptors()) {
				if (containerId.equals(descriptor.getContainerId())) {
					for (IThemeLayoutGroupDescriptor lg : descriptor
							.getLayoutGroupDescriptors()) {
						devices.add(lg.getDevice());
					}
				}
			}
		}
		return devices.toArray(new IDevice[devices.size()]);
	}

	public static IDevice[] getDevicesByThemeId(String themeId) {
		List<IDevice> devices = new ArrayList<IDevice>();
		IThemeDescriptor descriptor = getThemeDescriptorById(themeId);
		if (descriptor != null) {
			for (IThemeLayoutGroupDescriptor lg : descriptor
					.getLayoutGroupDescriptors()) {
				devices.add(lg.getDevice());
			}
		}
		return devices.toArray(new IDevice[devices.size()]);
	}

	public static IDevice[] getDevicesByThemeModelId(String modelId) {
		List<IDevice> devices = new ArrayList<IDevice>();
		IThemeModelDescriptor descriptor = getThemeModelDescriptorById(modelId);
		if (descriptor != null) {
			for (IThemeLayoutGroupDescriptor lg : descriptor
					.getThemeDescriptor().getLayoutGroupDescriptors()) {
				devices.add(lg.getDevice());
			}
		}
		return devices.toArray(new IDevice[devices.size()]);
	}

	public static IDevice[] getDevicesByThemeModelName(String modelName) {
		List<IDevice> devices = new ArrayList<IDevice>();
		IThemeModelDescriptor descriptor = getThemeModelDescriptorByName(modelName);
		if (descriptor != null) {
			for (IThemeLayoutGroupDescriptor lg : descriptor
					.getThemeDescriptor().getLayoutGroupDescriptors()) {
				devices.add(lg.getDevice());
			}
		}
		return devices.toArray(new IDevice[devices.size()]);
	}

	public static void computeAllThemeLayouts() throws ThemeException {
		for (IThemeDescriptor descriptor : ThemePlatform.getThemeDescriptors()) {
			IThemeManager manager = ThemePlatform
					.getThemeManagerByContainerId(descriptor.getContainerId());

			if (manager == null) {
				continue;
			}

			for (IThemeModelDescriptor modelDescriptor : ThemePlatform
					.getThemeModelDescriptors()) {
				if (descriptor.getId().equalsIgnoreCase(
						modelDescriptor.getThemeDescriptor().getId())) {

					Theme theme = manager.getModel(modelDescriptor.getId(),
							null);
					Set<Display> displays = theme.getDisplays();
					theme.computeAllElementLayout(displays, null);
					theme.computeAllPreviewElementLayout(displays, null);

					
					theme.computeAllSwappedElementLayout(displays, null);
					manager.releaseTheme(descriptor.getId());
					
					break;
				}
			}
		}
	}
}
