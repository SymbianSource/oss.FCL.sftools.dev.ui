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

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentStructureAdapter;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.packaging.PackagingConstants;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.ComponentGroup;
import com.nokia.tools.platform.theme.Element;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.ThemePreview;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.core.IPropertyAdapter;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenFactory;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.theme.command.ApplyThemeAttributeCommand;
import com.nokia.tools.theme.command.ApplyThemeGraphicCommand;
import com.nokia.tools.theme.command.Messages;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.theme.core.IContentLabelProvider;
import com.nokia.tools.theme.core.ThemeLabelProvider;
import com.nokia.tools.widget.theme.ImageProvider;

public class ThemeData extends IContentData.Stub {
	private static final List<ImageLayer> EMPTY_LAYER_LIST = new ArrayList<ImageLayer>();

	private EditObject resource;

	private Adapter structuralAdapter;

	private ThemeData linked;

	private ToolBoxAdapter toolboxAdapter;

	private IImageAdapter imageAdapter;

	private INamingAdapter namingAdapter;

	private ISkinnableEntityAdapter skinnableEntityAdapter;

	protected ICategoryAdapter categoryAdapter;

	protected ThemeData(final EditObject resource) {
		this.resource = resource;

		// initializes all necessary features, thus no notifications will be
		// sent to content adapter
		initFeatures();

		// adds the structural change adapter to update the content tree, extra
		// unnecessary work if content data removed
		structuralAdapter = new ContentAdapter(this);
		resource.eAdapters().add(structuralAdapter);
	}

	/**
	 * @return the resource
	 */
	public EditObject getResource() {
		return resource;
	}

	// sets the resource when the platform changes, find a better way
	void setResource(EditObject resource) {
		this.resource = resource;
	}

	protected void initFeatures() {
	}

	public void init() {
		ThemeContent content = (ThemeContent) getRoot();
		content.register(this);

		// drawline changes need to be passed to all registered clients
		ThemeBasicData data = getData();
		if (data instanceof SkinnableEntity) {
			if (((SkinnableEntity) data).isLine()) {
				content.getResource().eAdapters().add(
						new ThemeResourceAdapter());
			}
		}
		// shared parts
		if (data.isLink() && data.getLink() != null) {
			linked = content.findByData(data.getLink());
			if (linked != null) {
				linked.getResource().eAdapters().add(new TypedAdapter() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
					 */
					public void notifyChanged(Notification notification) {
						if (notification.getNotifier() == getResource()) {
							// notifies the linked resources only when the
							// linked itself changes
							return;
						}
						if (EditingUtil.isRemovingAdapter(notification, this)) {
							return;
						}
						if (Notification.SET == notification.getEventType()
								|| Notification.UNSET == notification
										.getEventType()) {
							// cares only about property changes, structural
							// change is not affecting this
							for (Adapter adapter : getResource().eAdapters()) {
								adapter.notifyChanged(notification);
							}
						}
					}
				});
			}
		}
	}

	/**
	 * @return the linked
	 */
	public ThemeData getLinked() {
		return linked;
	}

	/**
	 * @return the provider
	 */
	public AbstractThemeProvider getProvider() {
		if (getRoot() == null) {
			return null;
		}
		return ((ThemeContent) getRoot()).getProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData.Stub#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		ThemeBasicData data = getData();
		if (ContentAttribute.MODIFIED.name().equals(name)) {
			if (data == null) {
				return Boolean.FALSE.toString();
			}
			return new Boolean(data.isAnyChildDone()).toString();
		}
		if (ContentAttribute.PACKAGING.name().equals(name)) {
			return new Boolean(data.getSelectionForTransfer()).toString();
		}
		if (ContentAttribute.APPLICATION_NAME.name().equals(name)) {
			IScreenFactory factory = (IScreenFactory) getRoot().getAdapter(
					IScreenFactory.class);
			if (factory != null) {
				IContentData preview = factory.getScreenForData(this, false);
				if (preview == null) {
					return preview.getName();
				}
			}
			return null;
		}
		if (ContentAttribute.PACKAGING_TASK.name().equals(name)) {
			return new Boolean(
					data.isShown()
							&& (data instanceof Task || !(data.getThemeName()
									.equalsIgnoreCase(data.getParent()
											.getThemeName())))
							&& !(data.getParent() instanceof Component))
					.toString();
		}
		if (ContentAttribute.BOUNDS.name().equals(name)) {
			// check first return layout and then try to locate preview
			// element as
			// it is doing the same logic.
			if (data instanceof SkinnableEntity) {
				try {
					Layout layout = ((SkinnableEntity) data).getLayoutInfo();
					return layout.getBounds();
				} catch (Exception e) {
					PlatformCorePlugin.error(e);
				}
			}
			return null;
		}
		if (ContentAttribute.IMAGE_PATH.name().equals(name)) {
			List<ImageLayer> imageLayers = getImageLayers();
			for (ImageLayer imageLayer : imageLayers) {
				String fileName = imageLayer.getFileName((Theme) getData()
						.getRoot());
				if (fileName != null) {
					File file = new File(fileName);
					if (!file.isAbsolute()) {
						file = new File(((ThemeContent) getRoot())
								.getThemeFile().getParentFile(), fileName);
					}
					return file.getAbsolutePath();
				}
			}
		}
		if (ContentAttribute.MASK_PATH.name().equals(name)) {
			List<ImageLayer> imageLayers = getImageLayers();
			for (ImageLayer imageLayer : imageLayers) {
				String fileName = imageLayer
						.getAttribute(ThemeTag.ATTR_SOFTMASK);
				if (fileName == null) {
					fileName = imageLayer.getAttribute(ThemeTag.ATTR_HARDMASK);
				}
				if (fileName != null) {
					File file = new File(fileName);
					if (!file.isAbsolute()) {
						file = new File(((ThemeContent) getRoot())
								.getThemeFile().getParentFile(), fileName);
					}
					return file.getAbsolutePath();
				}
			}
		}
		if (ContentAttribute.APP_UID.name().equals(name)) {
			return data.getAttributeValue(ThemeTag.ATTR_APPUID);
		}
		if (ContentAttribute.TEXT.name().equals(name)) {
			String text = (String) data.getAttributeValue(ThemeTag.ATTR_TEXT);
			if (text != null) {
				return text;
			}
			return getName();
		}
		return super.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData#getId()
	 */
	public String getId() {
		ThemeBasicData data = getData();
		// change because ComponentGroup and Component have the same id
		if (data instanceof ComponentGroup || data instanceof Component) {
			// not modelled so let them be here
			StringBuilder sb = new StringBuilder();
			ThemeBasicData d = getData();
			while (d != null && !(d instanceof Theme)) {
				sb.insert(0, d.getIdentifier() + ":");
				d = d.getParent();
			}
			return sb.toString();
		}
		if (data == null) {
			return null;
		}
		return data.getIdentifier();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData.Stub#findByName(java.lang.String)
	 */
	@Override
	public IContentData findByName(String name) {
		return findByName(this, name);
	}

	/**
	 * Traverses down the content tree to find the node that matches the given
	 * id.
	 * 
	 * @param node the starting node.
	 * @param id id of the node.
	 * @return the node with the given id or null if not found.
	 */
	private static IContentData findByName(IContentData node, String name) {
		if (name == null) {
			return null;
		}

		if (name.equals(node.getName())) {

			ISkinnableEntityAdapter isea = (ISkinnableEntityAdapter) node
					.getAdapter(ISkinnableEntityAdapter.class);

			if (node.hasChildren()) {
				if (isea.supportsMultiPiece()) {
					return node;
				} else {// if the node has childrens and one of those
					// childrens has the same name
					// that child is returned
					IContentData[] children = node.getChildren();
					IContentData data = null;
					for (IContentData child : children) {
						data = findByName(child, name);
						if (data != null) {
							return data;
						}
					}
					// if none of the children has the same name, return
					// node
					return node;

				}

			} else { // returns node that has not any children (most
				// elements)
				return node;
			}// end

		}

		IContentData[] children = node.getChildren();
		for (IContentData child : children) {
			IContentData data = findByName(child, name);
			if (data != null) {
				return data;
			}
		}
		return null;
	}

	/**
	 * Returns true if the element is supported on the given platform.
	 * 
	 * @param platform the platform.
	 * @return true if the element is suppported, false otherwise.
	 * @see PackagingConstants
	 */
	public boolean supportsPlatform(IPlatform platform) {
		ICategoryAdapter adapter = (ICategoryAdapter) getAdapter(ICategoryAdapter.class);
		if (adapter != null) {
			IContentData[] peers = adapter.getCategorizedPeers();
			for (IContentData peer : peers) {
				if (((ThemeData) peer).getData().supportsPlatform(platform)) {
					return true;
				}
			}
			return false;
		}
		return getData() != null && getData().supportsPlatform(platform);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData.Stub#setAttribute(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public void setAttribute(String name, Object value) {
		if (ContentAttribute.PACKAGING.name().equals(name)) {
			getData().setSelectionForTransfer(new Boolean((String) value));
		}
		super.setAttribute(name, value);
	}

	/**
	 * @return the wrapped model data.
	 */
	public ThemeBasicData getData() {
		return (ThemeBasicData) EditingUtil.getBean(resource);
	}

	public void clearCachedData() {
		SimpleCache.clear(getRoot(), identityHashCode());
	}

	public ImageProvider getImageProvider() {
		return new ImageProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.widget.theme.ImageProvider#getImage()
			 */
			public RenderedImage getImage() {
				return generateImage();
			}

		};
	}

	public final synchronized RenderedImage generateImage() {
		long code = identityHashCode();
		RenderedImage cached = (RenderedImage) SimpleCache.getData(getRoot(),
				code);
		if (cached != null) {
			return cached;
		}
		cached = generateImageSpi();
		SimpleCache.cache(getRoot(), code, cached);
		return cached;
	}

	protected RenderedImage generateImageSpi() {
		IImageAdapter adapter = (IImageAdapter) getAdapter(IImageAdapter.class);
		try {
			if (adapter != null) {
				IImage image = adapter.getImage();
				if (image != null) {
					return image.getAggregateImage();
				}
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return null;
	}

	protected long identityHashCode() {
		return System.identityHashCode(this);
	}

	public String getImageCacheGroup() {
		return "imageCache" + getRoot();
	}

	public String getImageCacheKey(int width, int height) {
		return getId() + "!" + width + "x" + height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData#getImageDescriptor(int,
	 *      int)
	 */
	public ImageDescriptor getImageDescriptor(int width, int height) {
		IContentLabelProvider provider = (IContentLabelProvider) getAdapter(IContentLabelProvider.class);
		if (provider != null) {
			return provider.getIconImageDescriptor(this, width, height);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		ThemeBasicData data = getData();
		if (data == null) {
			// indicating not existing anymore, client shall act properly on
			// this case
			return null;
		}

		if (EditObject.class == adapter) {
			return getResource();
		}

		if (adapter == IContentStructureAdapter.class) {
			return new IContentStructureAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.content.core.IContentStructureAdapter#getChildren()
				 */
				public IContentData[] getChildren() {
					List<IContentData> children = new ArrayList<IContentData>();
					for (IContentData child : ThemeData.this.getChildren()) {
						if (((ThemeData) child).getData().isShown()) {
							children.add(child);
						}
					}
					return children.toArray(new IContentData[children.size()]);
				}

			};
		}

		if (adapter == IPropertyAdapter.class) {
			return IPropertyAdapter.GENERAL_ADAPTER;
		}

		if (IScreenAdapter.class == adapter) {
			if (getData() instanceof ThemePreview) {
				IScreenFactory factory = (IScreenFactory) getRoot().getAdapter(
						IScreenFactory.class);
				return factory.getScreens()[0].getAdapter(adapter);
			}
		}
		if (ILabelProvider.class == adapter
				|| IContentLabelProvider.class == adapter) {
			// only apply when element contains bitmap, else dont needed
			if (getProvider() == null) {
				return null;
			}
			return new ThemeLabelProvider(this);
		}

		if (IToolBoxAdapter.class == adapter) {
			return (toolboxAdapter == null ? (toolboxAdapter = new ToolBoxAdapter())
					: toolboxAdapter);
		}
		if (IImageAdapter.class == adapter) {
			return (imageAdapter == null ? (imageAdapter = getProvider()
					.createImageAdapter(this)) : imageAdapter);
		}

		if (IMediaFileAdapter.class == adapter) {
			IToolBoxAdapter toolbox = (IToolBoxAdapter) getAdapter(IToolBoxAdapter.class);
			if (toolbox.isFile()) {
				return new MediaFileAdapter();
			}
			return null;
		}

		if (IColorAdapter.class == adapter) {
			return isColor() ? new ColorAdapter() : null;
		}

		if (ILineAdapter.class == adapter) {
			if (data instanceof SkinnableEntity
					&& ((SkinnableEntity) data).isLine()) {
				return new ILineAdapter() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.nokia.tools.theme.content.ILineAdapter#drawLines()
					 */
					public boolean drawLines() {
						return ((Theme) getData().getRoot()).isDrawLines();
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see com.nokia.tools.theme.content.ILineAdapter#getApplyDrawLinesCommand(boolean)
					 */
					public Command getApplyDrawLinesCommand(boolean drawLines) {
						EditObject rootResource = ((ThemeContent) getRoot())
								.getResource();
						ApplyFeatureCommand command = new ApplyFeatureCommand();
						command.setLabel(Messages.Command_SetDrawLines_Label);
						command.setTarget(rootResource);
						command.setFeature(EditingUtil.getFeature(rootResource,
								"drawLines"));
						command.setValue(drawLines);
						return command;
					}

				};
			}
		}

		if (ISkinnableEntityAdapter.class == adapter) {
			return (skinnableEntityAdapter == null ? (skinnableEntityAdapter = getProvider()
					.createSkinnableEntityAdapter(this))
					: skinnableEntityAdapter);
		}
		if (ICategoryAdapter.class == adapter) {
			return getCategoryAdapter();
		}
		// solution for IconsView & properties
		if (IPropertySource.class == adapter) {

			EditObject widget = (EditObject) getAdapter(EditObject.class);

			if (widget == null) {
				return null;
			}

			return EcoreUtil
					.getRegisteredAdapter(widget, IPropertySource.class);
		}
		if (INamingAdapter.class == adapter) {
			return (namingAdapter == null ? (namingAdapter = new NamingAdapter())
					: namingAdapter);
		}
		// special adapter for returning untyped wrapped data
		if (IContentData.class == adapter) {
			return data;
		}
		return null;
	}

	protected ICategoryAdapter getCategoryAdapter() {
		if (categoryAdapter == null) {
			categoryAdapter = new ICategoryAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.screen.core.ICategoryAdapter#getCategorizedPeers()
				 */
				public IContentData[] getCategorizedPeers() {
					if (getData() instanceof SkinnableEntity) {
						return new IContentData[] { ThemeData.this };
					}
					return new IContentData[0];
				}
			};
		}
		return categoryAdapter;
	}

	public List<ImageLayer> getImageLayers() {
		try {
			if (null != getSkinnableEntity()) {
				ThemeGraphic graphic = getSkinnableEntity().getThemeGraphic();
				if (graphic != null) {
					return graphic.getImageLayers();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return EMPTY_LAYER_LIST;
	}

	public SkinnableEntity getSkinnableEntityByColor() {
		SkinnableEntity[] entities = getSkinnableEntities();
		for (SkinnableEntity entity : entities) {
			if (ThemeTag.ELEMENT_COLOUR.equals(entity.isEntityType())) {
				return entity;
			}
		}
		return null;
	}

	class NamingAdapter implements INamingAdapter {

		public NamingAdapter() {
		}

		public String getId() {
			SkinnableEntity element = getSkinnableEntity();
			return element == null ? ThemeData.this.getId() : element.getId();
		}

		public String getName() {
			SkinnableEntity element = getSkinnableEntity();
			return element == null ? ThemeData.this.getName() : element
					.getThemeName();
		}

	}

	class ToolBoxAdapter implements IToolBoxAdapter {

		public boolean isMultipleLayersSupport() {
			Object element = getSkinnableEntity();
			if (!(element instanceof Element))
				return false;
			Element _element = (Element) element;
			return _element.getToolBox().multipleLayersSupport;
		}

		public boolean isEffectsSupport() {
			Element element = (Element) getSkinnableEntity();

			if (element != null) {
				return element.getToolBox().effectsSupport;
			}

			return false;
		}

		public boolean isFile() {
			SkinnableEntity element = getSkinnableEntity();
			if (element != null) {
				if (element.isEntityType().equals(ThemeTag.ELEMENT_SOUND)
						|| element.isEntityType().equals(
								ThemeTag.ELEMENT_EMBED_FILE)) {
					return true;
				}
			}
			return false;
		}

		public boolean isText() {
			if (getData() == null) {
				return false;
			}
			String typeStr = getData().getAttributeValue(
					ThemeConstants.ELEMENT_TYPE);

			if (typeStr == null || typeStr.length() == 0) {
				return false;
			}

			int type = Integer.parseInt(typeStr);

			return type == ThemeConstants.ELEMENT_TYPE_TEXT;
		}

		public boolean supportMask() {
			Element element = (Element) getSkinnableEntity();

			if (element != null) {
				return element.getToolBox().Mask;
			}

			return false;
		}

		public boolean supportSoftMask() {
			Element element = (Element) getSkinnableEntity();

			if (element != null) {
				return element.getToolBox().SoftMask;
			}

			return false;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IColorAdapter#isColor()
	 */
	public boolean isColor() {
		try {
			SkinnableEntity skinnableEntity = getSkinnableEntity();
			if (skinnableEntity != null)
				return skinnableEntity.isEntityType().equalsIgnoreCase(
					ThemeTag.ELEMENT_COLOUR);
		} catch (Exception e) {
		}
		return false;
	}

	class ColorAdapter implements IColorAdapter {
		private ThemeGraphic getThemeGraphic() {
			try {
				return getSkinnableEntity().getThemeGraphic();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IColorAdapter#getColor()
		 */
		public Color getColor() {
			if (!isColor())
				return null;
			ThemeGraphic g = getThemeGraphic();
			if (g instanceof ColourGraphic) {
				return ColorUtil.toColor(((ColourGraphic) g).getColour());
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IColorAdapter#getApplyColorCommand(java.awt.Color,
		 *      boolean)
		 */
		public Command getApplyColorCommand(Color color, boolean makeSkinned) {
			ThemeGraphic graphic = (ThemeGraphic) getThemeGraphic();
			graphic = (ThemeGraphic) graphic.clone();

			((ColourGraphic) graphic).setColour(ColorUtil.asHexString(color));
			if (makeSkinned) {
				graphic.setAttribute(ThemeTag.ATTR_STATUS,
						ThemeTag.ATTR_VALUE_ACTUAL);
			}
			ApplyThemeGraphicCommand command = new ApplyThemeGraphicCommand();
			command.setLabel(Messages.Command_ApplyColor_Label);
			command.setTarget(getResource());
			command.setFeature(EditingUtil.getFeature(getResource(),
					"themeGraphic"));
			command.setValue(graphic);
			return command;
		}

		public Color getColourFromGraphics(Object graphics) {

			if (graphics == null) {
				// indication that graphics should be extracted from this obj.
				SkinnableEntity se = getSkinnableEntityByColor();
				if (se != null) {
					try {
						ThemeGraphic gr = se.getThemeGraphic();
						if (gr instanceof ColourGraphic)
							return ColorUtil.toColor(((ColourGraphic) gr)
									.getColour());
					} catch (ThemeException e) {
						e.printStackTrace();
					}
				}
			}

			return graphics instanceof ColourGraphic ? ColorUtil
					.toColor(((ColourGraphic) graphics).getColour()) : null;
		}

	}

	class MediaFileAdapter implements IMediaFileAdapter {

		public boolean isSound() {
			SkinnableEntity entity = getSkinnableEntity();
			// entity can be null when drags over the application_window
			if (entity == null
					|| entity.isEntityType().equals(ThemeTag.ELEMENT_SOUND))
				return true;
			return false;
		}

		public String getFileName(boolean absolute) {
			try {
				if (getSkinnableEntity() == null)
					return null;

				ThemeGraphic sg = getSkinnableEntity().getThemeGraphic();
				if (sg == null)
					return null;

				List layers = sg.getImageLayers();
				for (Object layer : layers) {
					ImageLayer l = (ImageLayer) layer;
					String filename = l
							.getFileName((Theme) getData().getRoot());
					if (filename != null) {
						try {
							return absolute ? FileUtils
									.makeAbsolutePath(((Theme) getData()
											.getRoot()).getThemeDir(), filename)
									: filename;
						} catch (Exception e) {
							PlatformCorePlugin.error(e);
							return filename;
						}
					}
				}
			} catch (ThemeException e) {
				e.printStackTrace();
			}
			return null;
		}

		public Object getThemeGraphics() {
			try {
				ThemeGraphic sg = getSkinnableEntity().getThemeGraphic();
				return sg;
			} catch (ThemeException e) {
				e.printStackTrace();
			}
			return null;
		}

		public Object getEditedThemeGraphics(Object oldTG, String soundFilePath) {
			// copy theme graphics, copy sound to theme TMP dir, set attrs,
			// return it
			SkinnableEntity entity = getSkinnableEntity();
			Theme theme = (Theme) entity.getRoot();

			// get temp dir
			String themeDir = theme.getThemeDir();
			try {
				themeDir = new File(themeDir).getCanonicalPath();
			} catch (Exception e) {
				e.printStackTrace();
			}

			String tempDir = themeDir + File.separator + "tmp";

			File tempDirFile = new File(tempDir);
			if (!tempDirFile.exists()) {
				tempDirFile.mkdir();
			}
			// mark for delete on app exit
			FileUtils.addForCleanup(tempDirFile);

			String curExt = FileUtils.getExtension(soundFilePath);

			// copy sound to tmp dir
			String tmpName = FileUtils.generateUniqueFileName(entity.getId(),
					tempDir, curExt);

			String filePath = tmpName;
			String absPath = tempDir + File.separator + filePath + "." + curExt;

			try {
				FileUtils.copyFile(new File(soundFilePath), new File(absPath));
			} catch (IOException e) {

				e.printStackTrace();
			}

			try {
				ThemeGraphic sg = (ThemeGraphic) entity.getThemeGraphic()
						.clone();

				List layers = sg.getImageLayers();
				for (Object layer : layers) {
					ImageLayer l = (ImageLayer) layer;
					String filename = l
							.getFileName((Theme) getData().getRoot());
					if (filename != null) {
						// this was layer that we edited, set new sound
						l.setAttribute(ThemeTag.FILE_NAME, absPath);
					}
				}

				return sg;

			} catch (ThemeException e) {
				e.printStackTrace();
			}

			markForDeleteOnExit(absPath);

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#getMaxDuration()
		 */
		public long getMaxDuration() {
			Long value = getDurationProperty(IMediaConstants.PROPERTY_DURATION_MAX);
			return value == null ? Long.MAX_VALUE : value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#getMinDuration()
		 */
		public long getMinDuration() {
			Long value = getDurationProperty(IMediaConstants.PROPERTY_DURATION_MIN);
			return value == null ? 0 : value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#getDuration()
		 */
		public long getDuration() {
			Long value = getDurationProperty(IMediaConstants.PROPERTY_DURATION);
			return value == null ? 0 : value;
		}

		private Long getDurationProperty(String name) {
			ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) getAdapter(ISkinnableEntityAdapter.class);
			if (ska != null) {
				Map map = ska.getLayerAttributes(0);
				if (map != null && map.get(name) != null) {
					try {
						return Long.valueOf((String) map.get(name));
					} catch (Exception e) {
						Activator.error(e);
					}
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#getDurationFromGraphics()
		 */
		public long getDurationFromGraphics() {
			ThemeGraphic tg = (ThemeGraphic) getThemeGraphics();
			if (null == tg || null == tg.getImageLayers())
				return 0;
			ImageLayer layer = tg.getImageLayers().get(0);
			if (layer.getAttributes().get(IMediaConstants.PROPERTY_DURATION) != null) {
				try {
					return Long.parseLong((String) layer.getAttributes().get(
							IMediaConstants.PROPERTY_DURATION));
				} catch (Exception e) {
					Activator.error(e);
				}
			}
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#hasDuration()
		 */
		public boolean hasDuration() {
			return getDurationProperty(IMediaConstants.PROPERTY_DURATION) != null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#getApplyDurationCommand(long)
		 */
		public Command getApplyDurationCommand(final long duration) {
			Command command = new ApplyThemeAttributeCommand(ThemeData.this) {
				String oldDuration;

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.theme.content.ApplyThemeAttributeCommand#doExecute()
				 */
				@Override
				protected boolean doExecute() {
					oldDuration = Long.toString(getDuration());
					IImageAdapter adapter = (IImageAdapter) getAdapter(IImageAdapter.class);
					IImage image = adapter.getImage(true);
					ILayer layer = image.getLayer(0);
					layer.getAttributes().put(
							IMediaConstants.PROPERTY_DURATION,
							Long.toString(duration));
					ISkinnableEntityAdapter sk = (ISkinnableEntityAdapter) getAdapter(ISkinnableEntityAdapter.class);
					sk.updateThemeGraphic(image);
					return true;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.theme.content.ApplyThemeAttributeCommand#doUndo()
				 */
				@Override
				protected boolean doUndo() {
					IImageAdapter adapter = (IImageAdapter) getAdapter(IImageAdapter.class);
					IImage image = adapter.getImage(true);
					ILayer layer = image.getLayer(0);
					layer.getAttributes().put(
							IMediaConstants.PROPERTY_DURATION, oldDuration);
					ISkinnableEntityAdapter sk = (ISkinnableEntityAdapter) getAdapter(ISkinnableEntityAdapter.class);
					sk.updateThemeGraphic(image);
					return true;
				}
			};
			command.setLabel(Messages.Command_SetDuration_Label);
			return command;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.layers.IMediaFileAdapter#getApplyMediaFileCommand(java.lang.String)
		 */
		public Command getApplyMediaFileCommand(final String filePath) {
			Command command = new ApplyThemeAttributeCommand(ThemeData.this) {
				String oldFilePath;

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.theme.content.ApplyThemeAttributeCommand#doExecute()
				 */
				@Override
				protected boolean doExecute() {
					oldFilePath = getFileName(false);
					try {
						ISkinnableEntityAdapter sk = (ISkinnableEntityAdapter) getAdapter(ISkinnableEntityAdapter.class);
						IImageAdapter adapter = (IImageAdapter) getAdapter(IImageAdapter.class);
						IImage image = adapter.getImage(true);
						ILayer layer = image.getLayer(0);
						if (StringUtils.isEmpty(filePath)) {
							// clears the image, should we check the duration?
							sk.clearThemeGraphics();
						} else {
							layer.paste(filePath.replace('\\', '/'));
						}
						sk.updateThemeGraphic(image);
						return true;
					} catch (Exception e) {
						Activator.error(e);
					}
					return false;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.nokia.tools.theme.content.ApplyThemeAttributeCommand#doUndo()
				 */
				@Override
				protected boolean doUndo() {
					ISkinnableEntityAdapter sk = (ISkinnableEntityAdapter) getAdapter(ISkinnableEntityAdapter.class);
					IImageAdapter adapter = (IImageAdapter) getAdapter(IImageAdapter.class);
					IImage image = adapter.getImage(true);
					ILayer layer = image.getLayer(0);
					try {
						if (StringUtils.isEmpty(filePath)) {
							// clears the image, should we check the duration?
							sk.clearThemeGraphics();
						} else {
							layer.paste(oldFilePath);
						}
						sk.updateThemeGraphic(image);
						return true;
					} catch (Exception e) {
						Activator.error(e);
					}
					return false;
				}
			};
			command.setLabel(Messages.Command_SetFile_Label);
			return command;
		}

		private void markForDeleteOnExit(String path) {
			try {
				File file = new File(path);
				file = file.getAbsoluteFile();
				FileUtils.addForCleanup(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public SkinnableEntity getSkinnableEntity() {
		SkinnableEntity[] entities = getSkinnableEntities();
		if (entities.length > 0) {
			return entities[0];
		}
		return null;
	}

	public SkinnableEntity[] getSkinnableEntities() {
		ThemeBasicData data = getData();
		if (data instanceof SkinnableEntity)
			return new SkinnableEntity[] { (SkinnableEntity) data };

		List<SkinnableEntity> entities = new ArrayList<SkinnableEntity>(2);
		ICategoryAdapter adapter = (ICategoryAdapter) getAdapter(ICategoryAdapter.class);
		if (adapter != null) {
			IContentData[] peers = adapter.getCategorizedPeers();
			for (IContentData peer : peers) {
				ThemeData d = (ThemeData) peer;
				SkinnableEntity entity = d.getSkinnableEntity();
				if (entity != null) {
					entities.add(entity);
				}
			}
		}
		return (SkinnableEntity[]) entities
				.toArray(new SkinnableEntity[entities.size()]);
	}

	public boolean supportsDisplay(com.nokia.tools.platform.core.Display display) {
		return getData().supportsDisplay(display);
	}

	class ThemeResourceAdapter extends TypedAdapter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
		 */
		public void notifyChanged(Notification notification) {
			if (notification.getFeature() instanceof EStructuralFeature) {
				if (((EStructuralFeature) notification.getFeature()).getName()
						.equals("drawLines")) {
					for (Adapter adapter : resource.eAdapters()) {
						adapter.notifyChanged(notification);
					}
				}
			}
		}
	}
}