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
package com.nokia.tools.platform.theme.preview;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeTag;

public class ThemePreview extends ThemeBasicData {
	private static final Object EMPTY = new Object();
	private PreviewController previewController;

	private Map<CacheItem, Object> screenCache = new HashMap<CacheItem, Object>();
	private Display themeDisplay;

	public ThemePreview(Theme skinDetails) {
		setTheme(skinDetails);
	}
	
	public int getScreenCacheSize() {
		return screenCache.size();
	}

	public void setTheme(Theme theme) {
		theme.setThemePreview(this);
		setParent(theme);
	}

	public void intializePreviewController() {
		// at this point the theme should have already been attached
		this.previewController = new PreviewController((Theme) getRoot());
	}

	public PreviewImage getPreviewImageByName(String screenName,
			Set<Display> displays) {
		if (screenName == null) {
			return null;
		}
		PreviewImage failSafeScreen = null;
		List childrenList = getChildren();
		if (childrenList != null) {
			for (Object child : childrenList) {
				PreviewImage image = (PreviewImage) child;
				if (image.getName().equals(screenName)) {
					failSafeScreen = image;
					if (displays == null || displays.isEmpty()) {
						if (image.supportsDisplay(getRoot().getDisplay())) {
							return image;
						}
					} else {
						for (Display display : displays) {
							if (image.supportsDisplay(display)) {
								return image;
							}
						}
					}
				}
			}
		}
		return failSafeScreen;
	}

	public List<PreviewImage> getPreviewImagesList(boolean getAll) {
		List<PreviewImage> previewImages = new ArrayList<PreviewImage>();
		for (Object obj : getChildren()) {
			PreviewImage screen = (PreviewImage) obj;
			if (getAll || screen.isPreview()) {
				previewImages.add(screen);
			}
		}
		return previewImages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof PreviewImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public void addChild(int position, Object objImage) {
		PreviewImage previewImage = (PreviewImage) objImage;

		synchronized (this) {
			previewImage.setParent(this);
			if (this.children == null) {
				this.children = new ArrayList<Object>();
			}
			if (position < 0) {
				this.children.add(previewImage);
			} else {
				children.add(position, previewImage);
			}
		}
	}

	public RenderedImage getBackgroundLayerImage(String skinId,
			int elementImageParam, PreviewElement preElem)
			throws PreviewException {
		return previewController
				.getBackgroundLayerImage(preElem == null ? null
						: (PreviewImage) preElem.getParent(), skinId,
						elementImageParam);
	}

	public Set<String> getDependentsForBackgroundLayer(String skinId) {
		Theme skinDetails = (Theme) getRoot();
		SkinnableEntity entity = skinDetails.getSkinnableEntity(skinId);

		Set<String> result = new HashSet<String>();
		// checks all preview images, especially the music player where the
		// control pane is affected by the music player background
		// also display is not significant here
		for (PreviewImage screen : getPreviewImagesList(false)) {
			try {
				List<ThemeBasicData> childElementsList = screen
						.getChildren(true);
				boolean isValidScreen = false;
				for (ThemeBasicData child : childElementsList) {
					if (skinId.equals(((PreviewElement) child).getID())) {
						isValidScreen = true;
						break;
					}
				}
				if (!isValidScreen) {
					continue;
				}
				Layout eleLayout = entity.getLayoutInfo();

				Rectangle eleRectangle = eleLayout.getBounds();

				for (ThemeBasicData child : childElementsList) {

					PreviewElement pElement = (PreviewElement) child;

					if (!pElement.supportsDisplay(screen.getDisplay())) {
						continue;
					}

					String id = pElement.getID();
					if (id == null) {
						continue;
					}

					if (id.equals(skinId)) {
						break;
					}

					Layout currEleLayout = null;
					SkinnableEntity se = skinDetails.getSkinnableEntity(id);
					if (se == null) {
						PlatformCorePlugin.error("Element: " + id
								+ " not fount in theme.");
					} else {
						if (screen != null) {
							currEleLayout = se.getLayoutInfoForPreview(screen
									.getDisplay(), pElement.getComponentInfo(),
									screen);
						} else {
							currEleLayout = se.getLayoutInfo();
						}
					}

					if (currEleLayout != null) {
						Rectangle currElementRectangle = currEleLayout
								.getBounds();

						if (eleRectangle.intersects(currElementRectangle)) {
							result.add(id);
						}
					} else {
						PlatformCorePlugin
								.error("Layout is not available for: " + id);
					}
				}

			} catch (Exception ex) {
				PlatformCorePlugin.error(ex);
			}
		}
		return result;
	}

	public RenderedImage getBackgroundLayerImage(String skinId,
			int elementImageParam, Layout layout, PreviewElement preElem)
			throws PreviewException {
		return previewController.getBackgroundLayerImage(preElem == null ? null
				: (PreviewImage) preElem.getParent(), skinId, layout);
	}

	public PreviewImage getPreviewImageForElem(ThemeBasicData elem,
			boolean createScreenIfNotFound) {
		return getPreviewImageForElem(elem, null, createScreenIfNotFound, true);
	}
	
	public PreviewImage getPreviewImageForElem(ThemeBasicData elem,
			String screenName, boolean createScreenIfNotFound, boolean canClonePreviewElement) {
		// not slow but accessed too many times, so caching is better here
		synchronized (screenCache) {
			Theme root = (Theme) elem.getRoot();
			// flush cache when display changes
			if (!root.getDisplay().equals(themeDisplay)) {
				themeDisplay = root.getDisplay();
				screenCache.clear();
			}

			CacheItem item = new CacheItem(elem, screenName,
					createScreenIfNotFound);
			Object cache = screenCache.get(item);
			if (cache == null || createScreenIfNotFound) {
				cache = getPreviewImageForElem0(elem, screenName,
						createScreenIfNotFound, canClonePreviewElement);
				if (cache == null) {
					cache = EMPTY;
				}
				screenCache.put(item, cache);
			}
			return cache instanceof PreviewImage ? (PreviewImage) cache : null;
		}
	}

	private PreviewImage getPreviewImageForElem0(ThemeBasicData elem,
			String screenName, boolean createScreenIfNotFound, boolean canClonePreviewElement) {
		// screenname may be different than the element's default screen
		// name in the part case
		String screen = screenName == null ? elem
				.getAttributeValue(ThemeTag.ATTR_PREVIEWSCREEN) : screenName;
		// check whether the screen exist
		// lets use old theme studio settings - which screen should be
		// displayed for specific element
		PreviewImage previewScreen = (PreviewImage) getPreviewImageByName(
				screen, Collections.singleton(getRoot().getDisplay()));
		if (null != previewScreen) {
			if (previewScreen.isPreview()) {
				PreviewElement pe = previewScreen.getElement(elem, false);
				if (pe != null
						&& pe.supportsDisplay(elem.getRoot().getDisplay())) {
					// only when element supports the current display
					return previewScreen;
				}
			}
		}

		// here find only screens that are to be displayed, no hidden screens
		PreviewImage otherScreen = findPreviewImageWithElem(elem, screen, false);
		if (otherScreen == null) {
			// a valid screen is now preferred, which relies on the preview.xml,
			// otherwise go for the swap candidate that is specified in the
			// design.xml, but may be out of sync with preview.xml

			return previewScreen;
		}
		return otherScreen;
	}

	private PreviewImage getPreviewImageForElem0(ThemeBasicData elem,
			String screenName, boolean createScreenIfNotFound) {
		return getPreviewImageForElem0(elem,
				 screenName,  createScreenIfNotFound, true);
	}


	public PreviewImage findPreviewImageWithElem(ThemeBasicData elem,
			String preferredScreenName, boolean searchHidden) {
		Theme root = (Theme) elem.getRoot();
		List images = getPreviewImagesList(searchHidden);
		List<PreviewImage> screens = new ArrayList<PreviewImage>(images.size());

		// first tries the screen that is in the current orientation,
		// otherwise returns one that simply contains the element
		for (Object screen : images) {
			PreviewImage image = (PreviewImage) screen;
			if (searchHidden && image.isPreview())
				continue;
			if (image.getElement(elem) != null) {
				screens.add(image);
			}
		}

		PreviewImage screenMatchingDisplay = null;

		if (preferredScreenName != null) {
			for (PreviewImage screen : screens) {
				if (screen.getName().equalsIgnoreCase(preferredScreenName)) {
					if (screen.supportsDisplay(root.getDisplay())) {
						return screen;
					}
				} else if (screen.supportsDisplay(root.getDisplay())) {
					screenMatchingDisplay = screen;
				}
			}
		} else {
			for (PreviewImage screen : screens) {
				if (screen.supportsDisplay(root.getDisplay())) {
					return screen;
				}
			}
		}
		if (screenMatchingDisplay != null) {
			return screenMatchingDisplay;
		}

		// returns the preferred screen even if the display is not suppported
		PreviewImage defaultScreen = null;
		for (PreviewImage screen : screens) {
			if (screen.getDisplays().isEmpty()) {
				// filters out unsupported screens
				continue;
			}
			if (preferredScreenName == null) {
				if (screen.supportsDisplay(root.getDisplay())) {
					return screen;
				}
			}

			if (defaultScreen == null) {
				defaultScreen = screen;
			}
			if (preferredScreenName != null
					&& screen.getName().equalsIgnoreCase(preferredScreenName)) {
				if (screen.supportsDisplay(root.getDisplay())) {
					return screen;
				}
			}
		}
		if ((defaultScreen != null) && (defaultScreen.supportsDisplay(root.getDisplay())))
			return defaultScreen;
		return null;
	}

	
	public PreviewImage getPreviewImage(String skinId) {
		SkinnableEntity entity = ((Theme) getRoot()).getSkinnableEntity(skinId);

		if (entity != null) {
			return getPreviewImageForElem(entity, false);
		}

		return null;
	}

	public static Set<Display> getDisplays(ThemeBasicData data) {
		ThemeBasicData tbd = data.getRoot();
		if (!(tbd instanceof Theme)) {
			return Collections.EMPTY_SET;
		}

		Theme theme = (Theme) tbd;

		Set<Display> displays = theme.getDisplays();
		Set<Display> list = new HashSet<Display>(displays.size());
		for (Display display : displays) {
			if (supportsDisplay(display, data)) {
				list.add(display);
			}
		}
		return list;
	}

	public static Display getDefaultDisplay(ThemeBasicData data) {
		Set<Display> displays = null;
		if (data instanceof PreviewImage) {
			displays = ((PreviewImage) data).getDisplays();
		} else if (data instanceof PreviewElement) {
			displays = ((PreviewElement) data).getDisplays();
		}
		Display themeDisplay = data.getRoot().getDisplay();
		if (displays == null || displays.isEmpty()
				|| displays.contains(themeDisplay)) {
			return themeDisplay;
		}
		// tries the closest match, otherwise it will show screen like 320x240
		// when in 176x268 case
		for (Display display : displays) {
			if (display.getWidth() == themeDisplay.getHeight()
					&& display.getHeight() == themeDisplay.getWidth()) {
				return display;
			}
		}
		return displays.isEmpty() ? themeDisplay : displays.iterator().next();
	}

	public static boolean supportsDisplay(Display display, ThemeBasicData data) {
		String tokens = data
				.getAttributeValue(PreviewTagConstants.ATTR_DISPLAY);
		return Theme.supportsDisplay(tokens, display);
	}

	class CacheItem {
		ThemeBasicData data;
		String screenName;
		boolean createIfNotFound;

		CacheItem(ThemeBasicData data, String screenName,
				boolean createIfNotFound) {
			this.data = data;
			this.screenName = screenName;
			this.createIfNotFound = createIfNotFound;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CacheItem) {
				CacheItem b = (CacheItem) obj;
				String sa = screenName == null ? "" : screenName;
				String sb = b.screenName == null ? "" : b.screenName;
				return data == b.data && sa.equalsIgnoreCase(sb)
						&& createIfNotFound == b.createIfNotFound;
			}
			return super.equals(obj);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return 0;
		}
	}
}