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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.ui.IFileEditorInput;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentProvider;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.player.MediaPlayer;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.theme.ElementType;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.PreviewRefer;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.theme.ecore.ThemeModelFactory;
import com.nokia.tools.theme.editing.IEntityImageFactory;
import com.nokia.tools.theme.screen.NormalImageElement;
import com.nokia.tools.theme.screen.ThemeElement;
import com.nokia.tools.theme.screen.ThemeGraphicElement;
import com.nokia.tools.theme.screen.ThemeImageElement;
import com.nokia.tools.theme.screen.ThemeLineGraphicElement;
import com.nokia.tools.theme.screen.ThemeScreenElement;
import com.nokia.tools.theme.screen.ThemeTextElement;

public abstract class AbstractThemeProvider
    implements IContentProvider {

	/*
	 * (non-Javadoc) If input is defined in list of parameters, it refers to
	 * path to the theme input can be String absolute and short path to tdf
	 * file.
	 * 
	 * @see com.nokia.tools.content.core.IContentProvider#getRootContents(java.lang.Object)
	 */
	public final List<IContent> getRootContents(Object input,
	    IProgressMonitor monitor) throws IOException, ContentException {
		init(monitor);

		List<IContent> list = new ArrayList<IContent>(1);
		if (input != null) {
			IContent theme = openTheme(input, monitor);
			if (theme != null) {
				list.add(theme);
			}
			return list;
		}
		Theme model = getDefaultModel(monitor);
		if (model != null) {
			IContent content = buildThemeContent(model);
			list.add(content);
		}
		return list;
	}

	public static synchronized void clearCache(ThemeContent content) {
		SimpleCache.clear(AbstractThemeProvider.class, content);

		if (DebugHelper.debugDeepclean()) {
			boolean exists = false;
			for (Object group : SimpleCache.getGroups()) {
				if (group instanceof ThemeContent) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				DebugHelper.debug(AbstractThemeProvider.class,
				    "Releasing all theme resources");
				for (IContentProvider provider : AbstractContentSourceManager
				    .findContentProviders()) {
					if (provider instanceof AbstractThemeProvider) {
						((AbstractThemeProvider) provider).release();
					}
				}
				// disposes all cached models
				for (Object group : SimpleCache.getGroups().toArray()) {
					if (group instanceof Theme) {
						((Theme) group).dispose();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentProvider#createRootContent(java.lang.Object)
	 */
	public final List<IContent> createRootContent(
	    Map<String, Object> creationData, IProgressMonitor monitor)
	    throws ContentException {
		try {
			init(monitor);

			Theme theme = createTheme(creationData, monitor);
			List<IContent> list = new ArrayList<IContent>(1);
			list.add((IContent) buildThemeContent((Theme) theme));
			return list;
		} catch (Exception e) {
			throw new ContentException(e);
		}
	}

	public ThemeContent buildThemeContent(Theme theme) {
		try {
			EditObject resource = ThemeModelFactory.createEditTree(theme);
			ThemeContent root = (ThemeContent) createThemeContentData(resource);
			return buildThemeContent(root);
		} catch (Exception e) {
			Activator.error(e);
			return null;
		}
	}

	public ThemeContent buildThemeContent(ThemeContent root) {
		EditObject resource = root.getResource();
		for (IContentData child : root.getChildren()) {
			if (child.getAdapter(IScreenAdapter.class) != null) {
				root.removeChild(child);
			}
		}
		for (EditObject child : resource.getChildren()) {
			buildTree(root, child);
		}
		return root;
	}

	public ThemeData buildTree(ThemeData parent, EditObject resource) {
		ThemeData contentData = createThemeContentData(resource);
		if (parent != null) {
			parent.addChild(contentData);
			contentData.init();
		}

		for (EditObject child : resource.getChildren()) {
			buildTree(contentData, child);
		}
		return contentData;
	}

	public ThemeData updateTree(ThemeData parent, ThemeBasicData data) {
		ThemeContent root = (ThemeContent) (parent.getRoot());
		ThemeData content = (ThemeData) root.findByData(data);
		if (content == null) {
			// new data
			try {
				EditObject resource = new ThemeModelFactory()
				    .createEditObject(data);
				synchronized (parent.getResource()) {
					parent.getResource().getChildren().add(resource);
				}
				content = root.findByData(data);
			} catch (Exception e) {
				Activator.error(e);
			}
		}

		List children = data.getChildren();
		IContentData contents[] = content.getAllChildren();
		for (IContentData child : contents) {
			boolean contains = false;
			if (null != children) {
				for (Object tbd : children) {
					if (((ThemeBasicData) tbd).isSameTarget(((ThemeData) child)
					    .getData())) {
						contains = true;
						break;
					}
				}
			}
			if (!contains) {
				content.getResource().getChildren().remove(
				    ((ThemeData) child).getResource());
			}
		}
		if (children != null) {
			for (Object obj : children) {
				updateTree((ThemeData) content, (ThemeBasicData) obj);
			}
		}
		return (ThemeData) content;
	}

	public ThemeData createThemeContentData(EditObject resource) {
		Object data = EditingUtil.getBean(resource);
		if (data instanceof Theme) {
			return createThemeContent(resource);
		}
		if (data instanceof PreviewImage) {
			return new ThemeScreenData(resource);
		}
		if (data instanceof PreviewElement) {
			return new ThemeScreenElementData(resource);
		}
		if (data instanceof PreviewRefer) {
			return new ThemeScreenReferData(resource);
		}
		return new ThemeData(resource);
	}

	public IScreenElement createScreenElement(IContentData data) {
		if (data instanceof ThemeScreenData) {
			return new ThemeScreenElement((ThemeScreenData) data);
		}
		if (data instanceof ThemeScreenElementData) {
			ThemeScreenElementData element = (ThemeScreenElementData) data;

			ElementType type = element.getType();
			if (type == ElementType.TEXT) {
				return createTextElement(element);
			}
			if (type == ElementType.IMAGE) {
				String colorId = ((ThemeScreenElementData) data)
				    .getData()
				    .getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);
				return colorId == null ? new NormalImageElement(
				    (ThemeScreenElementData) element) : new ThemeImageElement(
				    (ThemeScreenElementData) element);
			}
			if (type == ElementType.GRAPHIC) {
				// lines has special screen element
				SkinnableEntity entity = element.getSkinnableEntity();
				if (entity != null && entity.isLine()) {
					return new ThemeLineGraphicElement(
					    (ThemeScreenElementData) element);
				}
				// normal theme graphic
				return new ThemeGraphicElement((ThemeScreenElementData) element);
			}
			return null;
		}
		if (data instanceof ThemeScreenReferData) {
			return null;
		}
		if (data instanceof ThemeData) {
			return new ThemeGraphicElement((ThemeData) data);
		}

		return null;
	}

	public Object[] createVMAdapters(ThemeElement element) {
		final MediaPlayer player = element.getMediaPlayer();
		if (player != null) {
			element.getWidget().eAdapters().add(new TypedAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
				 */
				public void notifyChanged(Notification notification) {
					if (EditingUtil.isRemovingAdapter(notification, this)) {
						return;
					}
					player.refresh();
				}

			});

			return new Object[] { player };
		}
		return null;
	}

	protected void init(IProgressMonitor monitor) throws ContentException {
	}

	protected void release() {
	}

	protected IContent openTheme(Object input, IProgressMonitor monitor)
	    throws ContentException {
		File themeFile = null;
		IProject project = null;
		if (input instanceof String) {
			themeFile = new File((String) input);
		} else if (input instanceof IFileEditorInput) {
			project = ((IFileEditorInput) input).getFile().getProject();
			themeFile = ((IFileEditorInput) input).getFile().getLocation()
			    .makeAbsolute().toFile();
		} else if (input instanceof IFile) {
			project = ((IFile) input).getProject();
			themeFile = ((IFile) input).getLocation().makeAbsolute().toFile();
		} else if (input instanceof File) {
			themeFile = (File) input;
		}
		if (isFileValid(themeFile)) {
			Map<Object, Object> map = SimpleCache
			    .getGroupData(AbstractThemeProvider.class);
			IContent content = null;
			if (map != null) {
				for (Object obj : map.keySet()) {
					if (obj instanceof ThemeContent) {
						ThemeContent tc = (ThemeContent) obj;
						if (themeFile.equals(((Theme) tc.getData())
						    .getThemeFile())) {
							content = tc;
							break;
						}
					}
				}
			}
			if (content == null) {
				try {
					Theme theme = ThemeUtil.openThemeFromFile(themeFile,
					    getContainerId(), monitor);

					if (null != theme) {
						content = buildThemeContent(theme);
					}
					if (content != null) {
						content.setAttribute(ContentAttribute.PROJECT.name(),
						    project);
						SimpleCache.cache(AbstractThemeProvider.class, content,
						    content);
					}
				} catch (Exception e) {
					throw new ContentException(e);
				}
			}
			return content;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IDevice[].class == adapter) {
			return ThemePlatform.getDevicesByContainerId(getContainerId());
		}
		return null;
	}

	protected abstract String getContainerId();

	protected abstract boolean isFileValid(File file);

	protected abstract Theme createTheme(Map<String, Object> creationData,
	    IProgressMonitor monitor) throws ContentException;

	protected abstract ThemeContent createThemeContent(EditObject themeResource);

	protected abstract IPackager createPackager();

	protected abstract ISkinnableEntityAdapter createSkinnableEntityAdapter(
	    ThemeData data);

	protected abstract IImageAdapter createImageAdapter(ThemeData data);

	public abstract IEntityImageFactory getEntityImageFactory();

	public Theme getDefaultModel(IProgressMonitor monitor) {
		return ThemeUtil.getDefaultModel(getContainerId(), monitor);
	}

	protected ThemeTextElement createTextElement(ThemeData element) {
		return new ThemeTextElement(element);
	}
}
