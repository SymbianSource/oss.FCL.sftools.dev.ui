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
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.theme.ElementType;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.resource.util.SimpleCache;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.core.Activator;

public class ThemeScreenElementData extends ThemeData {
	/**
	 * Implementation of content framework - AVKON application is publiching
	 * content id data and UI to outside
	 */
	public static final String CONTENTID = "data";

	private List<ThemeData> skinnableData;
	private Adapter designAdapter;
	private Adapter swapAdapter;
	private ICategoryAdapter categoryAdapter;

	public ThemeScreenElementData(EditObject resource) {
		super(resource);
		swapAdapter = new TypedAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
			 */
			public void notifyChanged(Notification notification) {
				if (notification.getFeature() instanceof EStructuralFeature) {
					EStructuralFeature feature = (EStructuralFeature) notification
							.getFeature();
					if (feature.getName().equals("elementId")) {
						for (Iterator<ThemeData> i = skinnableData.iterator(); i
								.hasNext();) {
							ThemeData data = i.next();
							if (data.getData().getIdentifier().equals(
									notification.getOldValue())) {
								data.getResource().eAdapters().remove(
										designAdapter);
								i.remove();
							}
						}
						String newId = (String) notification.getNewValue();
						if (newId != null) {
							SkinnableEntity newEntity = ((Theme) getData()
									.getRoot()).getSkinnableEntity(newId);
							ThemeData newData = ((ThemeContent) getRoot())
									.findByData(newEntity);
							if (newData == null) {
								Activator.error("Data with id not found: "
										+ newId);
							} else {
								newData.getResource().eAdapters().add(
										designAdapter);
								skinnableData.add(newData);
							}
						}
					}
				}
			}

		};
		resource.eAdapters().add(swapAdapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#initFeatures()
	 */
	@Override
	protected void initFeatures() {
		super.initFeatures();

		EditingUtil.setFeatureValue(getResource(), "elementId",
				((PreviewElement) getData()).getElementId());
	}

	public boolean containsSkinnableEntity(SkinnableEntity entity) {
		for (ThemeData data : skinnableData) {
			if (data.getData().isSameTarget(entity)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#init()
	 */
	@Override
	public void init() {
		super.init();
		designAdapter = new TypedAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
			 */
			public void notifyChanged(Notification notification) {
				if (EditingUtil.isRemovingAdapter(notification, this)) {
					return;
				}
				// forwards resource change notification to the preview resource
				// so listeners can update
				if (!(notification.getNotifier() instanceof EditObject)) {
					return;
				}
				IContentData data = ContentAdapter
						.getContentData((EditObject) notification.getNotifier());
				if (!(data instanceof ThemeData)) {
					return;
				}
				if (((ThemeData) data).getData() instanceof Part) {
					// part change doesn't trigger screen update, the element
					// notification is always triggered after part change and
					// that
					// will be used for update to reduce number of repaints
					// not enabled.
				}
				for (Adapter adapter : getResource().eAdapters()) {
					adapter.notifyChanged(notification);
				}
			}
		};
		if (skinnableData == null) {
			skinnableData = new ArrayList<ThemeData>(2);
			PreviewElement element = (PreviewElement) getData();
			SkinnableEntity entity = element.getSkinnableEntity();
			SkinnableEntity colorEntity = element.getColorEntity();
			ThemeContent root = (ThemeContent) getRoot();
			if (entity != null) {
				ThemeData data = (ThemeData) root.findByData(entity);
				if (data != null) {
					skinnableData.add(data);
				}
			}
			if (colorEntity != null && colorEntity != entity) {
				ThemeData data = (ThemeData) root.findByData(colorEntity);
				if (data != null) {
					skinnableData.add(data);
				}
			}
		}
		Set<EditObject> resources = new HashSet<EditObject>();
		// default skinnable entities
		for (ThemeData data : skinnableData) {
			resources.add(data.getResource());
		}
		// background dependancies
		ThemeContent root = (ThemeContent) getRoot();
		Theme theme = ((Theme) getData().getRoot());
		Map<String, Set<String>> backgroundDependency = theme
				.getBackgroundDependency();
		String id = getId();
		for (Map.Entry<String, Set<String>> entry : backgroundDependency
				.entrySet()) {
			if (entry.getValue().contains(id)) {
				SkinnableEntity entity = theme.getSkinnableEntity(entry
						.getKey());

				ThemeData data = (ThemeData) root.findByData(entity);
				if (data != null) {
					resources.add(data.getResource());
				}
			}
		}
		for (EditObject resource : resources) {
			resource.eAdapters().add(designAdapter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getName()
	 */
	@Override
	public String getName() {
		if (getData() == null) {
			return null;
		}
		return ((PreviewElement) getData()).getCompName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		if (ContentAttribute.TEXT.name().equals(name)) {
			String text = getData().getAttributeValue(
					PreviewTagConstants.ATTR_ELEMENT_TEXT_FULL);
			if (text == null) {
				text = getData().getAttributeValue(
						PreviewTagConstants.ATTR_ELEMENT_TEXT);
			}
			return text;
		}
		if (CONTENTID.equals(name)) {
			return getData().getAttributeValue(
					PreviewTagConstants.ATTR_ELEMENT_DATA);
		}
		if (ContentAttribute.APP_UID.name().equals(name)) {
			ThemeBasicData peer = getData().getSkinnableEntity();
			if (peer != null) {
				return peer.getAttributeValue(ThemeTag.ATTR_APPUID);
			}
			return null;
		}
		if (ContentAttribute.BOUNDS.name().equals(name)) {
			return getBounds();
		}

		return super.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (getData() == null) {
			// indicating not existing anymore, client shall act properly on
			// this case
			return null;
		}
		if (adapter == ICategoryAdapter.class) {
			return getCategoryAdapter();
		}
		if (adapter == IImageAdapter.class) {
			return super.getAdapter(adapter);
		}
		// always returns the adapter from the skinnable entity
		if (adapter == IColorAdapter.class || adapter == ILineAdapter.class
				|| adapter == IMediaFileAdapter.class) {
			for (ThemeData data : skinnableData) {
				Object object = data.getAdapter(adapter);
				if (object != null) {
					return object;
				}
			}
			return null;
		}
		for (ThemeData data : skinnableData) {
			ISkinnableEntityAdapter object = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			if (!object.isColour()) {
				return data.getAdapter(adapter);
			}
		}
		if (!skinnableData.isEmpty()) {
			return skinnableData.get(0).getAdapter(adapter);
		}
		return super.getAdapter(adapter);
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
					return skinnableData.toArray(new IContentData[skinnableData
							.size()]);
				}
			};
		}
		return categoryAdapter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getImageLayers()
	 */
	@Override
	public List<ImageLayer> getImageLayers() {
		SkinnableEntity entity = getData().getSkinnableEntity();
		List<ImageLayer> layers = new ArrayList<ImageLayer>();
		if (entity == null) {
			Activator.warn("Can't find skinnable entity for " + getName());
		} else {
			try {
				if (entity.getThemeGraphic() != null) {
					layers.addAll(getLayers(entity));
				}
				if (entity.hasChildNodes()) {
					List children = entity.getLayeredChildren();
					if (children != null) {
						for (Object o : children) {
							SkinnableEntity child = (SkinnableEntity) o;
							layers.addAll(getLayers(child));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return layers;
	}

	private List<ImageLayer> getLayers(SkinnableEntity entity) throws Exception {
		List<ImageLayer> layers = new ArrayList<ImageLayer>();
		ThemeGraphic tg = entity.getPreviewThemeGraphic();
		if (tg != null) {
			for (Object ob : tg.getImageLayers()) {
				ImageLayer layer = (ImageLayer) ob;
				layers.add(layer);
			}
		}
		return layers;
	}

	public boolean canBeSkinned() {
		if (getData() == null) {
			return false;
		}
		return ((PreviewElement) getData()).canBeSkinned();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#supportsPlatform(com.nokia.tools.platform.core.IPlatform)
	 */
	@Override
	public boolean supportsPlatform(IPlatform platform) {
		return canBeSkinned() && super.supportsPlatform(platform);
	}

	public ElementType getType() {
		int type = ((PreviewElement) getData()).getPreviewElementType();
		switch (type) {
		case ThemeConstants.ELEMENT_TYPE_GRAPHIC:
			return ElementType.GRAPHIC;
		case ThemeConstants.ELEMENT_TYPE_IMAGEFILE:
			return ElementType.IMAGE;
		case ThemeConstants.ELEMENT_TYPE_MEDIA:
			return ElementType.MEDIA;
		case ThemeConstants.ELEMENT_TYPE_SOUND:
			return ElementType.SOUND;
		case ThemeConstants.ELEMENT_TYPE_TEXT:
			return ElementType.TEXT;
		default:
			throw new RuntimeException("Unknown element type: " + type);
		}
	}

	public Rectangle getBounds() {
		ThemeBasicData data = getData();
		if (data == null) {
			return new Rectangle();
		}
		return ((PreviewElement) data).getBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#generateImageSpi()
	 */
	protected RenderedImage generateImageSpi() {
		// Overriden, need to check image bounds against data in
		// ThemeScreenElementData
		IImageAdapter adapter = (IImageAdapter) getAdapter(IImageAdapter.class);
		if (adapter != null) {
			try {
				Rectangle bounds = getBounds();
				if ((bounds.width ==0)||(bounds.height==0)) return null;
				IImage image = adapter.getImage(bounds.width, bounds.height);
				if (image != null)
					return image.getAggregateImage();
				else
					return null;
			} catch (Exception e) {
				System.out.println("Error painting image for: " + getName());
				e.printStackTrace();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#clearCachedData()
	 */
	@Override
	public void clearCachedData() {
		super.clearCachedData();
		// also clears the skin element in other resolutions, mostly for
		// backgrounds
		int identity = System.identityHashCode(getSkinnableEntity());
		Map<Object, Object> map = SimpleCache.getGroupData(getRoot());
		if (map == null) {
			return;
		}
		synchronized (SimpleCache.getMutex()) {
			for (Iterator<Object> i = map.keySet().iterator(); i.hasNext();) {
				Object key = i.next();
				if (key instanceof Long) {
					long value = (Long) key;
					int hashCode = (int) ((value >> 32) & 0xffffffff);
					if (identity == hashCode) {
						i.remove();
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#identityHashCode()
	 */
	@Override
	protected long identityHashCode() {
		if (getSkinnableEntity() == null) {
			Activator.error("No entity found: " + this);
			return -1;
		}
		if (this.getData() instanceof PreviewElement
				&& (getSkinnableEntity().isEntityType()
						.equals(ThemeTag.ELEMENT_MORPHING))
				|| getSkinnableEntity().isEntityType().equals(
						ThemeTag.ELEMENT_IMAGE)
				|| getSkinnableEntity().isEntityType().equals(
						ThemeTag.ELEMENT_FRAME)
				|| getSkinnableEntity().isEntityType().equals(
						ThemeTag.ELEMENT_FASTANIMATION)) {
			Rectangle bounds = ((PreviewElement) getData()).getBounds();
			if (bounds == null) {
				Activator.error("Bounds not available in " + getData());
			} else {
				int rotate = 0;
				PreviewElement previewElement = (PreviewElement) getData();
				if (previewElement.getRotate() != null
						&& previewElement.getRotate().equalsIgnoreCase(
								PreviewTagConstants.ATTR_ROTATE_VALUE_180)) {
					rotate = 1;
				}

				long bgCode = previewElement.getBackgroundDependancy()
						.hashCode();

				/*
				 * caching using skinabble entity causes problems if
				 * the same objects have different PreviewElements for example
				 * with displaying digital clock digits...
				 */
				// replaced with the skinnable entity, the digital clock case
				// has been solved with the actual shape id (shape_link_id)
				// rather than the same entity with an spl_mask
				return ((long) System.identityHashCode(getSkinnableEntity())) << 32
						| ((long) (bounds.height + bounds.width + rotate + bgCode))
						& 0xffffffff;
			}
		}
		return super.identityHashCode();
	}
}
