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
package com.nokia.tools.theme.s60;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.core.MultiPieceManager;
import com.nokia.tools.theme.editing.BasicCopyPasteSupport;
import com.nokia.tools.theme.editing.IEntityImageFactory;
import com.nokia.tools.theme.editing.SkinnableEntityAdapter;
import com.nokia.tools.theme.s60.editing.EditableAnimatedEntity;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.editing.EditableEntityImageFactory;
import com.nokia.tools.theme.s60.editing.EditableImageLayer;
import com.nokia.tools.theme.s60.editing.utils.SkinnableEntityCopyPasteSupport;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;

/*
 * ISkinnableEntityAdapter implementation
 */
public class S60SkinnableEntityAdapter extends SkinnableEntityAdapter {

	
	@Override
	public String getColorDepth(int layerIndex) {
		Map map = getLayerAttributes(layerIndex);
		if ((map != null) && (map.containsKey(ThemeTag.ATTR_COLOURDEPTH))) {
			if (map.get(ThemeTag.ATTR_COLOURDEPTH) != null) {
				return (String) map.get(ThemeTag.ATTR_COLOURDEPTH);
			}
		}
		return "";
	}

	public S60SkinnableEntityAdapter(ThemeData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#createCopyPasteSupport(com.nokia.tools.platform.theme.SkinnableEntity)
	 */
	@Override
	protected BasicCopyPasteSupport createCopyPasteSupport(
			SkinnableEntity entity) {
		return new SkinnableEntityCopyPasteSupport(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#getAnimatedThemeGraphic(com.nokia.tools.platform.theme.SkinnableEntity)
	 */
	@Override
	protected ThemeGraphic getAnimatedThemeGraphic(SkinnableEntity entity)
			throws ThemeException {
		return new EditableAnimatedEntity(entity).getThemeGraphics();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#isSingleImageLayer()
	 */
	@Override
	public boolean isSingleImageLayer() {
		SkinnableEntity element = getData().getSkinnableEntity();
		int c = 0;
		try {
			EditableEntityImage eei = new EditableEntityImage(element);
			for (ILayer il : eei.getLayers()) {
				if (il.isEnabled()) {
					if (il.hasImage()
							&& il.getEffect(EffectConstants.APPLYGRAPHICS)
									.isSelected() && !il.isBackground()) {
						c++;
						if (c > 1)
							return false;
					}
				}
			}
			return c == 1;
		} catch (ThemeException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getCopyPieceInfo() {
		SkinnableEntity element = getData().getSkinnableEntity();
		if ((element != null) && (element.getCurrentProperty() != null))
			return MultiPieceManager.getCopyElementInfo(element.getCurrentProperty());
			//return MultipieceHelper.getCopyElementInfo(element.getCurrentProperty());
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#setStretchMode(java.lang.String)
	 */
	@Override
	public void setStretchMode(String newStretchMode) {
		getApplyStretchModeCommand(newStretchMode).execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#getStretchMode()
	 */
	@Override
	public String getStretchMode() {
		SkinnableEntity element = getData().getSkinnableEntity();
		String defaultStretchMode = "";
		try {
			EditableEntityImage iimage = new EditableEntityImage(element);
			for (ILayer layer : iimage.getSelectedLayers()) {
				if (layer.hasImage()
						&& layer.getEffect(EffectConstants.APPLYGRAPHICS)
								.isSelected()) {
					EditableImageLayer ll = (EditableImageLayer) layer;
					String stretch = ll.getStretchMode();
					if (!StringUtils.isEmpty(stretch)) {
						if (ll.isBackground())
							defaultStretchMode = stretch;
						else
							return stretch;
					}
				}
			}
		} catch (ThemeException e) {
			PlatformCorePlugin.error(e);
		}
		return defaultStretchMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#setPartThemeGraphics(int,
	 *      java.lang.Object, java.lang.String)
	 */
	@Override
	public void setPartThemeGraphics(int partNo, Object newThemeGraphics,
			String status) throws ThemeException, Exception {
		Part _part = null;
		try {
			SkinnableEntity element = getData().getSkinnableEntity();
			if (element != null) {
				ThemeGraphic graphic = (ThemeGraphic) newThemeGraphics;
				if (graphic != null) {
					graphic.setAttribute(ThemeTag.ATTR_STATUS,
							status == null ? ThemeTag.ATTR_VALUE_ACTUAL
									: status);
				}

				List<IImage> parts = new EditableEntityImage(element)
						.getPartInstances();
				IImage prt = parts.get(partNo);

				EditableEntityImage part = (EditableEntityImage) prt;
				_part = (Part) part.getEntity();
				_part.setThemeGraphic(graphic);
			}
		} finally {
			notifyPartChanged(_part);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#setPartsThemeGraphics(java.lang.Object[],
	 *      java.lang.String)
	 */
	@Override
	public void setPartsThemeGraphics(Object[] newThemeGraphics, String status)
			throws Exception {
		try {
			suppressNotification = true;
			for (int i = 0; i < newThemeGraphics.length; i++) {
				setPartThemeGraphics(i, newThemeGraphics[i], status);
			}
		} finally {
			suppressNotification = false;
			notifyChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#getEntityImageFactory()
	 */
	@Override
	protected IEntityImageFactory getEntityImageFactory() {
		return EditableEntityImageFactory.getInstance();
	}

	@Override
	public boolean isBackground() {
		return TSDataUtilities.isBackgroundElementId(getData().getId());
	}

	public ILayer getGraphicsLayer() {
		try {
			SkinnableEntity element = getData().getSkinnableEntity();
			if (element == null) {
				return null;
			}
			EditableEntityImage iimage = new EditableEntityImage(element);
			ILayer defaultLayer = null;
			for (ILayer layer : iimage.getSelectedLayers()) {
				if (layer.hasImage()
						&& layer.getEffect(EffectConstants.APPLYGRAPHICS)
								.isSelected()) {
					defaultLayer = layer;
					if (!layer.isBackground()) {
						return layer;
					}
				}
			}
			return defaultLayer;
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#getApplyStretchModeCommand(java.lang.String)
	 */
	@Override
	public Command getApplyStretchModeCommand(final String stretchMode) {
		return new ApplyStretchModeCommand(getData(), stretchMode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#convertToSinglePieceBitmap(boolean)
	 */
	public Object convertToSinglePieceBitmap(boolean replaceGraphics)
			throws Exception {
		suppressNotification = true;
		try {
			IImageAdapter adapter = (IImageAdapter) getData().getAdapter(
					IImageAdapter.class);
			EditableEntityImage img = (EditableEntityImage) adapter
					.getImage(false);
			RenderedImage currentImgMasked = img.getAggregateImage(false);

			for (IImage part : img.getPartInstances())
				((EditableImageLayer) part.getLayer(0)).clearMask();
			RenderedImage currentImgNoMask = img.getAggregateImage(false);

			ThemeGraphic tg = img.getEntity().getThemeGraphic();
			Object backup = null;
			if (tg != null) {
				backup = tg.clone();
			}
			/*
			 * Set theme graphics to Element - set single-piece
			 * bitmap property - create image from childs as PNG - clear childs
			 */
			//img.createThemeGraphics();
			img.setSinglePieceBitmap();
			try {
				if (replaceGraphics) {
					img.createThemeGraphics();
					img.getThemeGraphics().clearThemeGraphic();

					// remove all layers
					while (img.getLayerCount() > 0)
						img.removeLayer(img.getLayer(0));
					ILayer first = img.addLayer();
					first.clearLayer();
					first.paste(currentImgNoMask);
					first.pasteMask(CoreImage.create(currentImgMasked)
							.extractMask(first.supportSoftMask()).getAwt());
				} else {
					// only ensure that img has graphics and at least one layer
					img.createThemeGraphics();
					if ((img.getLayerCount() > 0) && (img.getLayer(0) instanceof EditableImageLayer)) {
						if ( ((EditableImageLayer) img.getLayer(0)).getAttributes().containsKey(ThemeTag.ATTR_NO_IMAGE)) {
						
							// remove all layers
							while (img.getLayerCount() > 0)
								img.removeLayer(img.getLayer(0));
							ILayer first = img.addLayer();
							first.clearLayer();
							first.paste(currentImgNoMask);
							first.pasteMask(CoreImage.create(currentImgMasked)
									.extractMask(first.supportSoftMask()).getAwt());
							
						}
					}
				}
				setThemeGraphics(getEditedThemeGraphics(img), null);

				return backup;
			} catch (ThemeException e) {
				throw e;
			}
		} finally {
			suppressNotification = false;
			notifyChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.SkinnableEntityAdapter#convertToNinePieceBitmap(boolean)
	 */
	public Object[] convertToMultiPieceBitmap(boolean fillParts)
			throws Exception {
		suppressNotification = true;
		try {
			IImageAdapter adapter = (IImageAdapter) getData().getAdapter(
					IImageAdapter.class);
			EditableEntityImage img = (EditableEntityImage) adapter
					.getImage(false);
			RenderedImage currentImg = img.getAggregateImage(false);

			/*
			 * Set nine-piece bitmap property - create childs
			 * graphics by slicing if necessary
			 */
			img.setMultiPieceBitmap();

			if (!fillParts)
				return null;

			RenderedImage _mask = img.getLayer(0).getMaskImage();
			if (_mask == null)
				_mask = CoreImage.create(currentImg).extractMask(
						img.getEntity().getToolBox().SoftMask).getAwt();

			if (_mask.getWidth() != currentImg.getWidth()
					|| _mask.getHeight() != currentImg.getHeight()) {
				// stretch
				_mask = CoreImage.create(_mask).scale(currentImg.getWidth(),
						currentImg.getHeight()).getAwt();
			}

			List<Object> undos = new ArrayList<Object>();

			// slice image if child parts are empty?
			List<IImage> parts = img.getPartInstances();
			if (parts != null) {
				for (Object p : parts) {
					EditableEntityImage part = (EditableEntityImage) p;
					undos.add(part.getThemeGraphics().clone());

					Rectangle bounds = part.getBounds();
					EditableImageLayer layer = (EditableImageLayer) part
							.getLayer(0);
					layer.clearMask();
					layer.paste(CoreImage.create(currentImg).crop(bounds)
							.getAwt());

					/*
					 * only perform mask division, if mask has same dimensions
					 * as image
					 */
					if (_mask != null) {
						int maskW = _mask.getWidth();
						int maskH = _mask.getHeight();
						if (maskW == currentImg.getWidth()
								&& maskH == currentImg.getHeight()) {
							try {
								layer.pasteMask(CoreImage.create(_mask).crop(
										bounds).getAwt());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					
					ThemeGraphic tg = part.getSavedThemeGraphics(false);
					tg.setAttribute(ThemeTag.ATTR_STATUS,
							ThemeTag.ATTR_VALUE_ACTUAL);
					part.getEntity().setThemeGraphic(tg);
				}
			}

			return undos.toArray();
		} finally {
			suppressNotification = false;
			notifyChanged();
		}
	}
	
	@Override
	public boolean isMultiPiece() {
		SkinnableEntity element = getData().getSkinnableEntity();
		if (element != null
				&& MultiPieceManager.isMultiPiece(element
						.getCurrentProperty())) {

			if (element.getChildren() != null
					&& MultiPieceManager.isMultiPiece(element.getChildren().size())) {
				return true;
			}
		}
		return false;
	}	
	
	public String getMultiPieceSearchViewText() {
		String searchViewText = "";
		SkinnableEntity element = getData().getSkinnableEntity();
		if (element != null
				&& MultiPieceManager.isMultiPiece(element
						.getCurrentProperty())) {
			searchViewText = MultiPieceManager.getSearchViewText(element
						.getCurrentProperty());
		}
		return searchViewText;
	}
}
