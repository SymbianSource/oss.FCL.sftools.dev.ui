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

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.ThemePreview;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.theme.ecore.ThemeModelFactory;
import com.nokia.tools.theme.preview.PreviewHandler;

public abstract class ThemeContent extends ThemeData implements IContent {
	private Map<ThemeBasicData, ThemeData> reverseLookups = new HashMap<ThemeBasicData, ThemeData>();
	private AbstractThemeProvider provider;

	private IContentService.Stub contentService;

	protected ThemeContent(EditObject resource, AbstractThemeProvider provider) {
		super(resource);
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getProvider()
	 */
	public AbstractThemeProvider getProvider() {
		return provider;
	}

	public void register(ThemeData data) {
		reverseLookups.put(data.getData(), data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (IPath.class == adapter) {
			return new Path(getThemeFile().getAbsolutePath());
		}
		if (IPackager.class == adapter) {
			return getProvider().createPackager();
		}
		if (IDevice[].class == adapter) {
			return ThemePlatform.getDevicesByThemeId(((Theme) getData())
					.getThemeId());
		}
		if (IContentService.class == adapter) {
			return getContentService();
		}
		if (IScreenFactory.class == adapter) {
			return new IScreenFactory() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.screen.core.IScreenFactory#createScreenForData(com.nokia.tools.content.core.IContentData)
				 */
				public IContentData getScreenForData(IContentData data,
						boolean createIfNotFound) {
					if (data instanceof ThemeData) {
						return new PreviewHandler(ThemeContent.this)
								.findScreenByData((ThemeData) data,
										createIfNotFound);
					}
					return null;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.screen.core.IScreenFactory#getScreens()
				 */
				public IContentData[] getScreens() {
					List<IContentData> screens = new ArrayList<IContentData>();
					for (IContentData child : getChildren()) {
						ThemeBasicData data = ((ThemeData) child).getData();
						if (data instanceof ThemePreview) {
							for (IContentData preview : child.getChildren()) {
								if (((PreviewImage) ((ThemeData) preview)
										.getData()).isPreview()) {
									screens.add(preview);
								}
							}
						}
					}
					return screens.toArray(new IContentData[screens.size()]);
				}
			};
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContent#delete()
	 */
	public void delete() throws ContentException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContent#save(IProgressMonitor)
	 */
	public void save(IProgressMonitor monitor) throws IOException,
			ContentException {
		try {
			((Theme) getData()).save(monitor);
			ColorGroups grps = ColorGroupsStore
					.getColorGroupsForProject((IProject) getAttribute(ContentAttribute.PROJECT
							.name()));
			saveColorGroups(grps);
		} catch (Throwable e) {
			throw new ContentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContent#saveAs(java.lang.String)
	 */
	public void saveAs(String newFileName, IProgressMonitor monitor)
			throws IOException, ContentException {
		try {
			ColorGroups grps = ColorGroupsStore
					.getColorGroupsForProject((IProject) getAttribute(ContentAttribute.PROJECT
							.name()));
			((Theme) getData()).saveAs(newFileName, monitor);
			saveColorGroups(grps);
		} catch (Throwable e) {
			throw new ContentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContent#dispose()
	 */
	public void dispose() {
		disposeImages();
		((Theme) getData()).dispose();
		AbstractThemeProvider.clearCache(this);
	}

	protected void disposeImages() {
		// dispose cached images
		SimpleCache.clear(getData().getImageCacheGroup());
		SimpleCache.clear(this);
	}

	/**
	 * @return the theme file.
	 */
	public File getThemeFile() {
		return ((Theme) getData()).getThemeFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		super.setAttribute(name, value);
		if (name.equals(ContentAttribute.AUTHOR.name())) {
			getData().setAttribute(ThemeTag.ATTR_AUTHOR, value.toString());
		} else if (name.equals(ContentAttribute.COPYRIGHT.name())) {
			getData().setAttribute(ThemeTag.ATTR_COPYRIGHT, value.toString());
		} else if (name.equals(ContentAttribute.ALLOW_COPYING.name())) {
			getData()
					.setAttribute(
							ThemeTag.ATTR_PROTECT,
							new Boolean(value.toString()) ? ThemeTag.ATTR_VALUE_COPY_ALLOWED
									: ThemeTag.ATTR_VALUE_COPY_DISALLOWED);
		} else if (name.equals(ContentAttribute.DRM_PROTECTION.name())) {
			getData().setAttribute(ThemeTag.ADVANCE_DRM, value.toString());
		} else if (name.equals(ContentAttribute.BITS_24PIXEL_SUPPORT.name())) {
			getData().setAttribute(ThemeTag.BITS_PIXEL_SUPPORT,
					value.toString());
		} else if (name.equals(ContentAttribute.DISPLAY.name())) {
			Display oldDisplay = (Display) getAttribute(ContentAttribute.DISPLAY
					.name());
			try {
				((Theme) getData()).setDisplay((Display) value);
				if (!value.equals(oldDisplay)) {
					SimpleCache.clear(getRoot());
				}
			} catch (Exception e) {
				Activator.error(e);
			}
		} else if (name.equals(ContentAttribute.APPLICATION_NAME.name())) {
			getData().setThemeName((String) value);
		} else if (name.equals(ContentAttribute.APP_UID.name())) {
			getData().setAttribute(ThemeTag.ADVANCE_UID, (String) value);
		} else if (name.equals(ContentAttribute.MODEL.name())) {
			String oldModelId = (String) getAttribute(ContentAttribute.MODEL
					.name());
			String newModelId = (String) value;
			if (!newModelId.equalsIgnoreCase(oldModelId)) {
				IThemeManager manager = ThemePlatform
						.getThemeManagerByThemeModelId(newModelId);
				try {
					Theme model = manager.getModel(newModelId, null);
					Theme theme = (Theme) getData();			
					theme.setModel(model);

					removeAdapters(getResource());
					//theme.clearAllCaches();					
					removeAllChildren();

					this.reverseLookups.clear();
					ContentAdapter contentAdapter = (ContentAdapter) this.getAdapter(ContentAdapter.class);
					if (contentAdapter != null) {
						contentAdapter.invalidateCache(this);
					}
					
					try {
						setResource(new ThemeModelFactory()
								.createEditTree(theme));
					} catch (Exception e) {
						Activator.error(e);
					}
					
					provider.buildThemeContent(this);
					
					disposeImages();
				} catch (Exception e) {
					Activator.error(e);
				}
			}
		}
	}

	protected void removeAdapters(EditObject resource) {
		/*for (Object obj: resource.eAdapters()) {
			if (obj instanceof ContentAdapter) {
				((ContentAdapter) obj).i
			}
		}*/
		
		resource.eAdapters().clear();
		for (EditObject child : resource.getChildren()) {
			removeAdapters(child);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		Theme theme = (Theme) getData();
		if (ContentAttribute.DISPLAY.name().equals(name)) {
			return theme.getDisplay();
		}
		if (ContentAttribute.AUTHOR.name().equals(name)) {
			return theme.getAttributeValue(ThemeTag.ATTR_AUTHOR);
		}
		if (ContentAttribute.COPYRIGHT.name().equals(name)) {
			return theme.getAttributeValue(ThemeTag.ATTR_COPYRIGHT);
		}
		if (ContentAttribute.ALLOW_COPYING.name().equals(name)) {
			return !"disablecopy".equals(theme
					.getAttributeValue(ThemeTag.ATTR_PROTECT)) ? "true"
					: "false";
		}
		if (ContentAttribute.BITS_24PIXEL_SUPPORT.name().equals(name)) {
			return theme.getAttributeValue(ThemeTag.BITS_PIXEL_SUPPORT);
		}
		if (ContentAttribute.DRM_PROTECTION.name().equals(name)) {
			return theme.getAttributeValue(ThemeTag.ADVANCE_DRM);
		}
		if (ContentAttribute.APPLICATION_NAME.name().equals(name)) {
			return getName();
		}
		if (ContentAttribute.MODEL.name().equals(name)) {
			return theme == null ? null : theme.getModelId();
		}
		if (ContentAttribute.APP_UID.name().equals(name)) {
			return theme.getAttributeValue(ThemeTag.ADVANCE_UID);
		}
		if (ContentAttribute.BOUNDS.name().equals(name)) {
			Display display = (Display) getAttribute(ContentAttribute.DISPLAY
					.name());
			return new Rectangle(display.getWidth(), display.getHeight());
		}
		if (ContentAttribute.THEME_ID.name().equals(name)) {
			return theme.getThemeId();
		}

		return super.getAttribute(name);
	}

	/**
	 * content change service notifier
	 * 
	 * @return
	 */
	protected IContentService getContentService() {
		if (null == contentService) {
			contentService = new IContentService.Stub();
		}
		return contentService;
	}

	/**
	 * Finds the content data that is corresponding to the theme data.
	 * 
	 * @param data the theme data input.
	 * @return the content data that is wrapping the given theme data or null if
	 *         no such data can be found.
	 */
	public ThemeData findByData(ThemeBasicData data) {
		if (data == null) {
			return null;
		}
		ThemeData themeData = reverseLookups.get(data);
		if (themeData != null) {
			return themeData;
		}
		if (data.getLink() != data) {
			themeData = reverseLookups.get(data.getLink());
			if (themeData != null) {
				return themeData;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData.Stub#findById(java.lang.String)
	 */
	@Override
	public IContentData findById(String id) {
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		// called many times, a little optimization
		SkinnableEntity entity = ((Theme) getData()).getSkinnableEntityById(id);
		if (entity != null) {
			return findByData(entity);
		}

		return super.findById(id);
	}

	public IContentData getPreview() {
		for (IContentData child : getChildren()) {
			ThemeBasicData data = ((ThemeData) child).getData();
			if (data instanceof ThemePreview) {
				return child;
			}
		}

		return null;
	}

	/**
	 * Saves the color group.
	 */
	protected void saveColorGroups(ColorGroups grps) {
		if (ColorGroupsStore.isEnabled) {
			ColorGroupsStore
					.saveGroups(grps, ((Theme) getData()).getThemeDir());
		}
	}
}