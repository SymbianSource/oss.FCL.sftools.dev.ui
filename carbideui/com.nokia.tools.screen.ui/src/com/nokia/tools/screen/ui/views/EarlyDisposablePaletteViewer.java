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
package com.nokia.tools.screen.ui.views;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.editparts.PaletteEditPart;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;

import com.nokia.tools.screen.ui.UiPlugin;

/**
 * Dispose the images created in the figures before the display is disposed to
 * release resources ealier than the default palette viewer's behavior.
 * 
 */
public class EarlyDisposablePaletteViewer extends PaletteViewer {
	private Set<Resource> resourcesToDispose = new HashSet<Resource>();
	private Set<ImageDescriptor> descriptorsToDispose = new HashSet<ImageDescriptor>();

	protected void addResourceToDispose(Resource resource) {
		resourcesToDispose.add(resource);
	}

	protected void addImageDescriptorToDispose(ImageDescriptor descriptor) {
		if (descriptor != null) {
			descriptorsToDispose.add(descriptor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.palette.PaletteViewer#hookControl()
	 */
	@Override
	protected void hookControl() {
		super.hookControl();
		getControl().addDisposeListener(new DisposeListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
			 */
			public void widgetDisposed(DisposeEvent e) {
				for (Resource resource : resourcesToDispose) {
					if (resource != null) {
						resource.dispose();
					}
				}
				resourcesToDispose.clear();

				try {
					Field field = PaletteEditPart.class
							.getDeclaredField("globalImageCache");
					field.setAccessible(true);
					Object imageCache = field.get(null);

					if (imageCache != null) {
						field = imageCache.getClass()
								.getDeclaredField("images");
						field.setAccessible(true);

						Map images = (Map) field.get(imageCache);
						for (ImageDescriptor descriptor : descriptorsToDispose) {
							Image image = (Image) images.remove(descriptor);
							if (image != null) {
								image.dispose();
							}
						}
					}
				} catch (Exception ee) {
					UiPlugin.error(ee);
				}
			}
		});
	}

}
