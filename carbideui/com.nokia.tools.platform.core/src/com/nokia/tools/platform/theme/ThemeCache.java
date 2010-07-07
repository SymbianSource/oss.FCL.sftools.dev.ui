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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.theme.preview.PreviewException;
import com.nokia.tools.platform.theme.preview.RenderablePreviewImage;

public class ThemeCache {

	private Hashtable<Display, Hashtable<Object, Object>> allResolutionsMap = new Hashtable<Display, Hashtable<Object, Object>>();

	private Hashtable<Display, Hashtable<Object, Object>> allElementsMap = new Hashtable<Display, Hashtable<Object, Object>>();

	private String cacheName = null;

	public ThemeCache(String cacheName) {

		this.cacheName = cacheName;
	}

	public String getName() {
		return this.cacheName;
	}

	synchronized public void clearElementCache() {
		allElementsMap.clear();
	}

	synchronized public void clearPreviewScreenCache() {
		allResolutionsMap.clear();
	}

	synchronized public void putElement(Display display, String skinId,
			String locId, Object value) {

		if (value == null || skinId == null || skinId.trim().length() <= 0) {
			return;
		}

		skinId = skinId.toLowerCase();

		// check if the resolution also exists
		// if resolution also exists
		// add it to the resolution
		// if resolution does not exist
		// create a Hashtable for resolutions and to that add this.
		if (allElementsMap.containsKey(display)) {

			Hashtable<Object, Object> resMap = (Hashtable<Object, Object>) allElementsMap
					.get(display);
			String key = skinId;
			if (locId != null && locId.trim().length() > 0) {
				key = key + locId.toLowerCase();
			} else {
				key = key + "nolocid";
			}
			resMap.put(key, value);
		} else {
			Hashtable<Object, Object> resMap = new Hashtable<Object, Object>(25);

			String key = skinId;

			if (locId != null && locId.trim().length() > 0)
				key = key + locId.toLowerCase();
			else
				key = key + "nolocid";

			resMap.put(key, value);

			allElementsMap.put(display, resMap);

		}
	}

	synchronized public void invalidateElements(String[] skinIds, String locId)
			throws PreviewException {
		if (skinIds == null || skinIds.length <= 0) {

			throw new PreviewException(
					"Cannot invalidate preview screens from the preview cache, invalid data");

		}

		// for each resolution do twice
		for (Display currResolutionKey : allElementsMap.keySet()) {

			Hashtable currResMap = (Hashtable) allElementsMap
					.get(currResolutionKey);
		
			for (int i = 0; i < skinIds.length; i++) {

				Enumeration currKeys = currResMap.keys();

				String currSkinId = skinIds[i].toLowerCase();

				while (currKeys.hasMoreElements()) {

					String currkey = (String) currKeys.nextElement();

					// currkey = currkey.toLowerCase();

					if (currkey.startsWith(currSkinId + locId)) {

						currResMap.remove(currkey);

					}
				}
			}
		}
	}

	synchronized public void invalidateElements(String[] skinIds) {
		
		if (skinIds == null || skinIds.length <= 0) {

			throw new RuntimeException(
					"Cannot invalidate preview screens from the preview cache, invalid data");

		}

		for (Display currResolutionKey : allElementsMap.keySet()) {

			Hashtable currResMap = (Hashtable) allElementsMap
					.get(currResolutionKey);
			
			for (int i = 0; i < skinIds.length; i++) {
				if (skinIds[i] == null) {
					
					continue;
				}
				Enumeration currKeys = currResMap.keys();

				String currSkinId = skinIds[i].toLowerCase();

				while (currKeys.hasMoreElements()) {

					String currkey = (String) currKeys.nextElement();

					// currkey = currkey.toLowerCase();

					if (currkey.startsWith(currSkinId)) {

						currResMap.remove(currkey);

					}
				}

			}
		}
	}

	synchronized public Object getElement(Display display, String skinId,
			String locId) {

		Object result = null;
		if (skinId == null || skinId.trim().length() <= 0) {


			result = null;
		} else {

			if (allElementsMap.containsKey(display)) {

				Hashtable currResMap = (Hashtable) allElementsMap.get(display);

				String valKey = skinId;

				if (locId != null && locId.trim().length() > 0) {
					valKey = valKey + locId.toLowerCase();
				} else {
					valKey = valKey + "nolocid";
				}
				if (currResMap != null && currResMap.containsKey(valKey)) {
					// result = currResMap.get(skinId);
					result = currResMap.get(valKey);
				}
			}
		}
		return result;
	}

	synchronized public void putPreviewScreen(Display display,
			String screenName, RenderablePreviewImage value) {

		if (value == null || screenName == null
				|| screenName.trim().length() <= 0) {
			return;
		}

		if (allResolutionsMap.containsKey(display)) {

			Hashtable<Object, Object> resMap = allResolutionsMap.get(display);

			resMap.put(screenName, value);
		} else {
			Hashtable<Object, Object> resMap = new Hashtable<Object, Object>(25);

			resMap.put(screenName, value);

			allResolutionsMap.put(display, resMap);
		}
	}

	// even though the screen name will be different for the portrait or
	// landscape preview screens still if the screen is for both orientaiton
	// then while
	// storing the preview screens they should be separately stored into two
	// separate lists
	// so using orientation for preivew screen's caching.
	synchronized public boolean containsPreviewScreen(Display display,
			String screenName) {

		boolean result = false;
		if (screenName == null || screenName.trim().length() <= 0) {

			result = false;
		} else {

			if (allResolutionsMap != null
					&& allResolutionsMap.containsKey(display)) {

				Hashtable currResMap = (Hashtable) allResolutionsMap
						.get(display);

				if (currResMap != null && currResMap.containsKey(screenName)) {

					result = true;
				}
			}
		}
		return result;
	}

	synchronized public void invalidatePreviewScreens(List screenNames,
			boolean matchAsStartsWith) throws PreviewException {

		if (screenNames == null || screenNames.size() <= 0) {

			throw new PreviewException(
					"Cannot invalidate preview screens from the preview cache, invalid data");

		} else {
			Enumeration allResoultionKeySet = allResolutionsMap.keys();

			// Iterator iter = allResoultionKeySet.iterator();

			// for each resolution do twice
			while (allResoultionKeySet.hasMoreElements()) {

				Hashtable currResMap = (Hashtable) allResolutionsMap
						.get(allResoultionKeySet.nextElement());

				RenderablePreviewImage tempImage = null;

				if (matchAsStartsWith == false) {
					for (int i = 0; i < screenNames.size(); i++) {
						if (currResMap != null
								&& currResMap.containsKey(screenNames.get(i))) {

							tempImage = (RenderablePreviewImage) currResMap
									.get(screenNames.get(i));

							tempImage.disposeImage();

							currResMap.remove(screenNames.get(i));
						}
					}
				} else {
					for (int i = 0; i < screenNames.size(); i++) {
						Enumeration currKeys = currResMap.keys();

						if (!currKeys.hasMoreElements()) {
							break;
						}

						String currScreenName = (String) screenNames.get(i);

						while (currKeys.hasMoreElements()) {

							String currkey = (String) currKeys.nextElement();

							if (currkey.startsWith(currScreenName)) {

								currResMap.remove(currkey);
							}
						}
					}
				}
			}
		}

	}

	synchronized public RenderablePreviewImage getPreviewScreen(
			Display display, String screenName) {

		RenderablePreviewImage result = null;
		if (screenName == null || screenName.trim().length() <= 0) {
			result = null;
		} else {

			if (allResolutionsMap.containsKey(display)) {

				Hashtable currResMap = (Hashtable) allResolutionsMap
						.get(display);

				if (currResMap != null && currResMap.containsKey(screenName)) {

					result = (RenderablePreviewImage) currResMap
							.get(screenName);
				}
			}
		}
		return result;
	}

}