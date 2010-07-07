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
package com.nokia.tools.theme.content;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.screen.core.IContentDataAdapter;
import com.nokia.tools.theme.content.ThemeData.ThemeResourceAdapter;

/**
 * adds the structural change adapter to update the content tree and updates the
 * caches
 */
public class ContentAdapter extends TypedAdapter implements IContentDataAdapter {
	private ThemeData data;

	public ContentAdapter(ThemeData data) {
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
		if (notification.getFeature() == EditingUtil.getContainmentFeature(data
				.getResource())) {
			if (Notification.ADD == notification.getEventType()) {
				EditObject newResource = (EditObject) notification
						.getNewValue();
				ThemeData newData = getContentData(newResource);
				if (newData == null) {
					newData = ((ThemeContent) data.getRoot()).getProvider()
							.createThemeContentData(newResource);
				}
				data.addChild(notification.getPosition(), newData);
				newData.init();
			} else if (Notification.REMOVE == notification.getEventType()) {
				EditObject oldResource = (EditObject) notification
						.getOldValue();
				ThemeData oldData = getContentData(oldResource);
				if (oldData != null) {
					data.removeChild(oldData);
				}
			}
		} else if (notification.getFeature() instanceof EStructuralFeature
				&& EditingUtil.isValidFeature((EStructuralFeature) notification
						.getFeature())) {
			invalidateCache(getData());
		}

		// linked content also needs to be notified
		if (notification.getNotifier() == data.getResource()) {
			ThemeData linked = data.getLinked();
			if (linked != null) {
				for (Adapter adapter : linked.getResource().eAdapters()) {
					adapter.notifyChanged(notification);
				}
			}
		}

		// part change also notifies the parent
		if (data.getData() instanceof Part) {
			for (Adapter adapter : ((ThemeData) data.getParent()).getResource()
					.eAdapters().toArray(
							new Adapter[((ThemeData) data.getParent())
									.getResource().eAdapters().size()])) {
				adapter.notifyChanged(notification);
			}
		}

		if (notification.getNotifier() == data.getResource()) {
			// forwards changes to the root content so the clients interested on
			// the
			// content modified will get notified
			ThemeData root = (ThemeData) data.getRoot();
			if (root != data && root != null) {
				for (Adapter adapter : root.getResource().eAdapters()) {
					if (adapter instanceof ThemeResourceAdapter) {
						// internal resource adapter that listens for the theme
						// level resource changes
						continue;
					}
					adapter.notifyChanged(notification);
				}
			}
		}
	}

	public ThemeData getData() {
		return data;
	}

	public void invalidateCache(ThemeData data) {
		// screen cache
		data.clearCachedData();

		ThemeBasicData tbd = data.getData();

		// theme cache
		if (getData().getRoot() != null) {
			Theme theme = (Theme) ((ThemeContent) getData().getRoot())
					.getData();

			// no need to invalidate the dependant elements because they are
			// also receiving notifications and will enter here
			String[] ids = { data.getId() };
			theme.getBackgroundLayerCache().invalidateElements(ids);
			theme.getPreviewCache().invalidateElements(ids);
			theme.getImageCache().invalidateElements(ids);
		}

		// icon cache
		Map<Object, Object> group = SimpleCache.getGroupData(tbd
				.getImageCacheGroup());
		if (group != null) {
			synchronized (group) {
				for (Iterator<Object> i = group.keySet().iterator(); i
						.hasNext();) {
					String key = (String) i.next();
					if (key.startsWith(tbd.getImageCacheKey())) {
						i.remove();
					}
				}
			}
		}

		// part changes, clears the parent element as well
		if (tbd instanceof Part) {
			ThemeData parentData = (ThemeData) getData().getParent();
			invalidateCache(parentData);
		}
	}

	public static ThemeData getContentData(EditObject resource) {
		ContentAdapter adapter = (ContentAdapter) EcoreUtil.getExistingAdapter(
				resource, ContentAdapter.class);
		if (adapter != null) {
			return adapter.getData();
		}
		return null;
	}
}
