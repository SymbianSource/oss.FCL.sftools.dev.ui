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
package com.nokia.tools.theme.screen;

import java.awt.Rectangle;
import java.io.File;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;
import com.nokia.tools.media.player.IMediaProvider;
import com.nokia.tools.media.player.MediaPlayer;
import com.nokia.tools.media.player.PlayerExtensionManager;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.ScreenComponentAdapter;
import com.nokia.tools.theme.content.ThemeData;


public abstract class ThemeElement extends IScreenElement.ScreenElementAdapter {

	private Object[] adapters;

	public ThemeElement(ThemeData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		com.nokia.tools.widget.theme.ThemeElement bean = (com.nokia.tools.widget.theme.ThemeElement) getBean();
		if (bean != null) {
			Rectangle bounds = (Rectangle) getData().getAttribute(
					ContentAttribute.BOUNDS.name());
			if (bounds != null) {
				bean.setBounds(bounds);
			}
		}

		// listens for the resource changes
		EditObject resource = ((ThemeData) getData()).getResource();
		if (!resource.eAdapters().contains(this)) {
			resource.eAdapters().add(this);
		}
	}

	protected void handleResourceChange(Notification notification) {
		if (!(notification.getFeature() instanceof EStructuralFeature)) {
			return;
		}
		EStructuralFeature feature = (EStructuralFeature) notification
				.getFeature();
		if (notification.getNotifier() == ((ThemeData) getData()).getResource()) {
			// preview element change
			// need to optimize?
			initWidget(getContext());
		} else {
			// skinnable entity change
			initWidget(getContext());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#handleNotification(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	protected void handleNotification(Notification notification) {
		try {
			super.handleNotification(notification);

			if (EditingUtil.isRemovingAdapter(notification, this)) {
				// disposed, detaches self from the resource adapters
				((ThemeData) getData()).getResource().eAdapters().remove(this);
			} else if (notification.getFeature() instanceof EStructuralFeature
					&& !EditingUtil
							.isDynamicFeature((EStructuralFeature) notification
									.getFeature())) {
				return;
			}
			if (notification.getNotifier() != this) {
				handleResourceChange(notification);
				return;
			}
		} finally {
			// since the widget itself doesn't listen on changes, we will
			// forward the resource changes to the registered adapters, e.g. GEF
			// refresh adapters
			if (getWidget() != null) {
				for (Object adapter : getWidget().eAdapters().toArray()) {
					if (adapter != this) {
						((Adapter) adapter).notifyChanged(notification);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IComponentAdapter.class) {
			ThemeData data = (ThemeData) getData();
			IPlatform platform = null;
			// if (data.getRoot() != null) {
			// platform = (String) data.getRoot().getAttribute(
			// ContentAttribute.PLATFORM.name());
			// }
			if (platform == null) {
				platform = DevicePlatform.UNSPECIFIED_PLATFORM;
			}
			final SkinnableEntity entity = data.getSkinnableEntity();
			if (data.supportsPlatform(platform)) {
				final IImageAdapter imageAdapter = (IImageAdapter) data
						.getAdapter(IImageAdapter.class);
				return new ScreenComponentAdapter(this) {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.nokia.tools.screen.core.IComponentAdapter.Stub#getSupportedTypes()
					 */
					@Override
					protected int getSupportedTypes() {
						if (imageAdapter != null) {
							return imageAdapter.canBeAnimated()? (READONLY | ANIMATION): READONLY;
						}
						return READONLY;
						/*IImage image = imageAdapter != null ? imageAdapter
								.getImage() : null;*/
						/*return image != null && image.canBeAnimated() ? (READONLY | ANIMATION)
								: READONLY;*/
						/*return imageAdapter.isAnimated()  image != null && image.canBeAnimated() ? (READONLY | ANIMATION)
								: READONLY;*/
					}

				};
			}
			return IComponentAdapter.UNSELECTABLE_ADAPTER;
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#getVMAdapters()
	 */
	@Override
	public synchronized Object[] getVMAdapters() {
		if (adapters == null) {
			if (((ThemeData) getData()).getProvider() == null) {
				return null;
			}
			adapters = ((ThemeData) getData()).getProvider().createVMAdapters(
					this);
		}
		return adapters;
	}

	public MediaPlayer getMediaPlayer() {
		return new MediaPlayer(new IMediaProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.player.IMediaProvider#getContentType()
			 */
			public String getContentType() {
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.player.IMediaProvider#getFile()
			 */
			public File getFile() {
				ThemeBasicData data = ((ThemeData) getData()).getData();
				String fileName = null;
				IImageAdapter adapter = (IImageAdapter) getData().getAdapter(
						IImageAdapter.class);
				IImage image = adapter.getImage();
				if (image != null) {
					fileName = image.getLayer(0).getFileName(true);
				}
				if (fileName == null && data instanceof PreviewElement) {
					fileName = ((PreviewElement) data).getFileName();
				}
				if (fileName != null) {
					return new File(fileName);
				}

				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.media.utils.player.IMediaProvider#isPlayable()
			 */
			public boolean isPlayable() {
				ThemeData data = (ThemeData) getData();
				SkinnableEntity[] entities = data.getSkinnableEntities();
				if (entities.length > 0) {
					for (SkinnableEntity entity : entities) {
						if (Boolean.valueOf(entity
								.getAttributeValue(ThemeTag.ATTR_ANIMATE))) {
							File file = getFile();
							if (file != null) {
								return PlayerExtensionManager
										.createPlayerByExtension(FileUtils
												.getExtension(file)) != null;
							}
						}
					}
					return false;
				}
				return true;
			}

		});
	}
}
