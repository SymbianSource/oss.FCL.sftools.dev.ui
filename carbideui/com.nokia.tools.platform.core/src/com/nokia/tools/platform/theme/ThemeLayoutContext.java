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

import java.util.HashMap;
import java.util.Map;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeLayoutGroupDescriptor;
import com.nokia.tools.platform.layout.LayoutContext;
import com.nokia.tools.platform.layout.LayoutInfo;
import com.nokia.tools.platform.layout.LayoutSet;

public class ThemeLayoutContext {
	private Map<Display, LayoutContext> contexts = new HashMap<Display, LayoutContext>();

	private IThemeDescriptor descriptor;
	private IDevice device;

	public ThemeLayoutContext(String themeId) {
		if (themeId == null) {
			throw new NullPointerException();
		}
		this.descriptor = ThemePlatform.getThemeDescriptorById(themeId);
	}

	/**
	 * @return the descriptor
	 */
	public IThemeDescriptor getDescriptor() {
		return descriptor;
	}

	/**
	 * @return the device
	 */
	public IDevice getDevice() {
		return device;
	}

	public Display getDisplay() {
		return device == null ? null : device.getDisplay();
	}

	/**
	 * @param device the device to set
	 */
	public void setDevice(IDevice device) {
		if (device == null) {
			throw new NullPointerException();
		}
		this.device = device;
	}

	public void setDisplay(Display display) {
		setDevice(ThemePlatform.getDevice(descriptor.getId(), display));
	}

	public LayoutContext getLayoutContext() {
		return getLayoutContext(device);
	}

	public LayoutContext getLayoutContext(Display display) {
		return getLayoutContext(ThemePlatform.getDevice(descriptor.getId(),
				display));
	}

	public synchronized LayoutContext getLayoutContext(IDevice device) {
		if (device == null) {
			return null;
		}
		LayoutContext context = contexts.get(device.getDisplay());

		if (context == null) {
			String layoutSetId = descriptor.getLayoutSet();
			IThemeLayoutGroupDescriptor layoutGroup = descriptor
					.getLayoutGroupDescriptor(device);
			if (layoutGroup != null) {
				LayoutSet layoutSet = LayoutSet.getLayoutSet(layoutSetId);
				if (layoutSet != null) {
					String[] layoutIds = layoutGroup.getLayoutIds();
					LayoutInfo[] info = new LayoutInfo[layoutIds.length];
					for (int i = 0; i < layoutIds.length; i++) {
						info[i] = layoutSet.createLayoutInfo(layoutIds[i],
								device.getDisplay().getVariant());
					}
					context = new LayoutContext(info);
					contexts.put(device.getDisplay(), context);
				}
			}
		}
		return context;
	}
}
