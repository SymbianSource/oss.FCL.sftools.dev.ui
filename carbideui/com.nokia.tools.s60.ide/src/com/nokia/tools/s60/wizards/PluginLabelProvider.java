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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchPlugin;

import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.ILayoutDescriptor;
import com.nokia.tools.platform.extension.ILayoutSetDescriptor;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.platform.extension.PluginEntry;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class PluginLabelProvider extends LabelProvider {
	private static final Image IMG_ERROR = WorkbenchPlugin.getDefault()
			.getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);

	private List<Resource> resources = new ArrayList<Resource>();
	private PluginContentProvider provider;

	public PluginLabelProvider(PluginContentProvider provider) {
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		Image image = null;
		if (element == PluginContentProvider.PLUGIN_ROOT) {
			image = S60WorkspacePlugin.getImageDescriptor(
					"icons/icons16/plugin_obj.gif").createImage();
		} else if (element instanceof IExtension[]) {
			image = S60WorkspacePlugin.getImageDescriptor(
					"icons/icons16/extension_obj.gif").createImage();
		} else {
			image = S60WorkspacePlugin.getImageDescriptor("icons/bullet.gif")
					.createImage();
		}
		if (image != null) {
			if (provider.hasConfigurationError(element,
					new PluginPropertiesContentProvider(provider))) {
				int w = image.getBounds().width;
				int h = image.getBounds().height;

				Image image2 = new Image(Display.getDefault(), w, h);
				GC gc = new GC(image2);
				gc.drawImage(image, 0, 0);
				image.dispose();
				gc.drawImage(IMG_ERROR, 0, 0, w, h, 0, h - 8, 7, 8);
				gc.dispose();
				image = image2;
			}
			resources.add(image);
		}

		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element == PluginContentProvider.PLUGIN_ROOT) {
			return "Plugin";
		}
		if (element instanceof IExtension[]) {
			IExtension[] extensions = (IExtension[]) element;
			if (extensions.length > 0) {
				String id = extensions[0].getExtensionPointUniqueIdentifier();
				return id.substring(id.lastIndexOf(".") + 1);
			}
		}
		if (element instanceof IDevice) {
			IDevice device = (IDevice) element;
			return device.getDisplay().getWidth() + " x "
					+ device.getDisplay().getHeight();
		}
		if (element instanceof ILayoutSetDescriptor) {
			return ((ILayoutSetDescriptor) element).getId();
		}
		if (element instanceof ILayoutDescriptor) {
			IDevice device = ((ILayoutDescriptor) element).getDevice();
			if (device == null) {
				if (provider.getRoot() instanceof PluginEntry) {
					IExtension[] extensions = ((PluginEntry) provider.getRoot())
							.getExtensions(PlatformExtensionManager.DEVICE_CONTRIBUTOR_ID);
					if (extensions != null) {
						for (IDevice d : PlatformExtensionManager
								.getDevices(extensions)) {
							if (d.getId()
									.equalsIgnoreCase(
											((ILayoutDescriptor) element)
													.getDeviceId())) {
								device = d;
								break;
							}
						}
					}
				}
			}
			String deviceText;
			if (device != null) {
				deviceText = getText(device);
			} else {
				deviceText = "Unknown device";
			}
			return ((ILayoutDescriptor) element).getId() + " (" + deviceText
					+ ")";
		}
		if (element instanceof IThemeDescriptor) {
			return ((IThemeDescriptor) element).getId();
		}
		if (element instanceof IThemeModelDescriptor) {
			return ((IThemeModelDescriptor) element).getName();
		}
		if (element != null) {
			return element.toString();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		for (Resource resource : resources) {
			resource.dispose();
		}
	}

}
