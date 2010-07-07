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
package com.nokia.tools.theme.editing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.UnexecutableCommand;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.InvocationAdapter;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.clipboard.IClipboardContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.AnimatedThemeGraphic;
import com.nokia.tools.platform.theme.AnimatedThemeGraphicCache;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.LayerUtils;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.theme.command.ApplyThemeGraphicCommand;
import com.nokia.tools.theme.content.ArtificialNotification;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.core.Activator;
import com.nokia.tools.theme.core.MultiPieceManager;

public class SkinnableEntityAdapter implements ISkinnableEntityAdapter {
	private ThemeData data;
	protected volatile boolean suppressNotification;

	public boolean isSuppressNotification() {
		return suppressNotification;
	}

	public void setSuppressNotification(boolean suppressNotification) {
		this.suppressNotification = suppressNotification;
	}

	public SkinnableEntityAdapter(ThemeData data) {
		this.data = data;
	}

	public void notifyChanged() {
		notifyChanged(data);
	}

	public void notifyChanged(SkinnableEntity entity) {
		notifyChanged(((ThemeContent) getData().getRoot()).findByData(entity));
	}

	public void notifyChanged(ThemeData data) {
		if (data == null) {
			return;
		}
		if (suppressNotification && (data == this.data)) {
			// suppress notification applies only to adapter acting on the
			// current data
			return;
		}
		ArtificialNotification notification = new ArtificialNotification(data);
		EList<Adapter> adapters = data.getResource().eAdapters();
		for (Adapter adapter : adapters.toArray(new Adapter[adapters.size()])) {
			if (adapter instanceof InvocationAdapter) {
				continue;
			}
			adapter.notifyChanged(notification);
		}
	}

	protected void notifyPartChanged(SkinnableEntity part) {
		notifyChanged(part);
		// also the parent part
		if (part == data.getSkinnableEntity()) {
			notifyChanged((SkinnableEntity) part.getParent());
		} else {
			if (data == null) {
				return;
			}
			if (suppressNotification && (data == this.data)) {
				// suppress notification applies only to adapter acting on the
				// current data
				return;
			}
			ArtificialNotification notification = new ArtificialNotification(data);
			notification.setPart(part); //set the part which got modified
			EList<Adapter> adapters = data.getResource().eAdapters();
			for (Adapter adapter : adapters.toArray(new Adapter[adapters.size()])) {
				if (adapter instanceof InvocationAdapter) {
					continue;
				}
				adapter.notifyChanged(notification);
			}		
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getEditedThemeGraphics(java.lang.Object)
	 */
	public Object getEditedThemeGraphics(Object editModel, boolean forceAbsolute) {
		if (editModel instanceof BasicEntityImage) {
			try {
				return ((BasicEntityImage) editModel)
						.getSavedThemeGraphics(forceAbsolute);
			} catch (Exception e) {
				Activator.error(e);
				return null;
			}
		}
		throw new RuntimeException("editModel should be instance of "
				+ BasicEntityImage.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getEditedThemeGraphics(java.lang.Object)
	 */
	public Object getEditedThemeGraphics(Object editModel) {
		return getEditedThemeGraphics(editModel, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getOriginalThemeGraphics(java.lang.Object)
	 */
	public Object getOriginalThemeGraphics(Object editModel) {

		if (editModel instanceof BasicEntityImage) {

			try {
				SkinnableEntity element = data.getSkinnableEntity();
				if (element.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
					return getAnimatedThemeGraphic(element);
				} else {
					ThemeGraphic tg = ((BasicEntityImage) editModel)
							.getEntity().getThemeGraphic();
					return tg != null ? tg.clone() : null;
				}
			} catch (Exception e) {
				Activator.error(e);
				return null;
			}
		} else {
			try {
				SkinnableEntity element = null;
				if (editModel instanceof SkinnableEntity) {
					element = (SkinnableEntity) editModel;
				} else {
					element = data.getSkinnableEntity();
				}
				if (element.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
					return getAnimatedThemeGraphic(element);
				} else {
					ThemeGraphic tg = element.getThemeGraphic();
					return tg != null ? tg.clone() : null;
				}
			} catch (ThemeException e) {
				Activator.error(e);
			}
		}
		throw new RuntimeException("editModel should be instance of "
				+ BasicEntityImage.class);
	}

	protected ThemeGraphic getAnimatedThemeGraphic(SkinnableEntity entity)
			throws ThemeException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#setThemeGraphics(java.lang.Object,
	 *      java.lang.String)
	 */
	public void setThemeGraphics(Object themeGr, String status) {
		try {
			SkinnableEntity[] entities = data.getSkinnableEntities();
			SkinnableEntity element = null;
			for (SkinnableEntity entity : entities) {
				if ((themeGr instanceof ColourGraphic && ThemeTag.ELEMENT_COLOUR
						.equals(entity.isEntityType()))
						|| (!(themeGr instanceof ColourGraphic) && !ThemeTag.ELEMENT_COLOUR
								.equals(entity.isEntityType()))) {
					element = entity;
					break;
				}
			}
			if (element != null) {
				ThemeGraphic gr = (ThemeGraphic) themeGr;
				if (gr != null) {
					gr.setAttribute(ThemeTag.ATTR_STATUS,
							status == null ? ThemeTag.ATTR_VALUE_ACTUAL
									: status);
				} else {
					/*
					 * when users clears 9-piece elements and performs undo,
					 * themeGr == null, but we cannot set null graphics, so we
					 * create dummy one with no content
					 */

					try {
						element.setActualGraphic(null);
					} catch (ThemeException e) {
						Activator.error(e);
					}
					return;

				}
				try {
					if (gr instanceof AnimatedThemeGraphic) {
						if (((AnimatedThemeGraphic) gr).getThemeGraphics()
								.size() == 0) {
							element.clearThemeGraphic();
						} else if (((AnimatedThemeGraphic) gr)
								.getThemeGraphics().size() == 1) {
							((AnimatedThemeGraphic) gr).setAttribute(
									ThemeTag.ATTR_TYPE, ThemeTag.ATTR_STILL);
							element.setAttribute(ThemeTag.ATTR_TYPE,
									ThemeTag.ATTR_STILL);
							List childs = ((AnimatedThemeGraphic) gr)
									.getThemeGraphics();
							for (Object object : childs) {
								((ThemeGraphic) object)
										.setAttribute(ThemeTag.ATTR_TYPE,
												ThemeTag.ATTR_STILL);
							}
							element.setCurrentProperty(ThemeTag.ATTR_STILL);
							element.setThemeGraphic(gr);
						} else {
							((AnimatedThemeGraphic) gr).setAttribute(
									ThemeTag.ATTR_TYPE, ThemeTag.ATTR_BMPANIM);
							element.setAttribute(ThemeTag.ATTR_TYPE,
									ThemeTag.ATTR_BMPANIM);
							List childs = ((AnimatedThemeGraphic) gr)
									.getThemeGraphics();
							for (Object object : childs) {
								((ThemeGraphic) object).setAttribute(
										ThemeTag.ATTR_TYPE,
										ThemeTag.ATTR_BMPANIM);
							}
							element.setCurrentProperty(ThemeTag.ATTR_BMPANIM);
							element.setThemeGraphic(gr);
						}
					} else if (gr instanceof AnimatedThemeGraphicCache) {
						if (((AnimatedThemeGraphicCache) gr).getThemeGraphics()
								.size() == 0) {
							element.clearThemeGraphic();
						} else if (((AnimatedThemeGraphicCache) gr)
								.getThemeGraphics().size() == 1) {
							((AnimatedThemeGraphicCache) gr).setAttribute(
									ThemeTag.ATTR_TYPE, ThemeTag.ATTR_STILL);
							element.setAttribute(ThemeTag.ATTR_TYPE,
									ThemeTag.ATTR_STILL);
							List childs = ((AnimatedThemeGraphicCache) gr)
									.getThemeGraphics();
							for (Object object : childs) {
								((ThemeGraphic) object)
										.setAttribute(ThemeTag.ATTR_TYPE,
												ThemeTag.ATTR_STILL);
							}
							element.setCurrentProperty(ThemeTag.ATTR_STILL);
							element.setThemeGraphic(gr);
						} else {
							((AnimatedThemeGraphicCache) gr).setAttribute(
									ThemeTag.ATTR_TYPE, ThemeTag.ATTR_BMPANIM);
							element.setAttribute(ThemeTag.ATTR_TYPE,
									ThemeTag.ATTR_BMPANIM);
							List childs = ((AnimatedThemeGraphicCache) gr)
									.getThemeGraphics();
							for (Object object : childs) {
								((ThemeGraphic) object).setAttribute(
										ThemeTag.ATTR_TYPE,
										ThemeTag.ATTR_BMPANIM);
							}
							element.setCurrentProperty(ThemeTag.ATTR_BMPANIM);
							element.setThemeGraphic(gr);
						}
					} else {
						element.setThemeGraphic(gr);
					}
				} catch (ThemeException e) {
					Activator.error(e);
				}
			}
		} finally {
			notifyChanged();
		}
	}

	private BasicCopyPasteSupport getCopyPasteSupport() {
		return createCopyPasteSupport(data.getSkinnableEntity());
	}

	protected BasicCopyPasteSupport createCopyPasteSupport(
			SkinnableEntity entity) {
		return new BasicCopyPasteSupport(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isCopyAllowed()
	 */
	public boolean isCopyAllowed() {
		return getCopyPasteSupport().isCopyImageAvailable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#copyImage(java.lang.Object)
	 */
	public boolean copyImageToClipboard(Clipboard clip) {
		Clipboard clipboard = clip;
		if (clip == null) {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		return getCopyPasteSupport().doCopyImage((Clipboard) clipboard);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getId()
	 */
	public String getId() {
		return data.getId();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#clearThemeGraphics()
	 */
	public Object clearThemeGraphics() {
		try {
			
			SkinnableEntity[] elements = data.getSkinnableEntities();
			List<ThemeGraphic> undoObjs = new ArrayList<ThemeGraphic>();
			List<ThemeGraphic> undoPartObjs = new ArrayList<ThemeGraphic>();
			for (SkinnableEntity element : elements) {
				
				/**
				 * If an element has been converted from multipiece to single piece,
				 * ensure that the element goes to its initial state (multipiece) before
				 * clearing it.
				 */
				if (isCovertedElement(element)) {
					//Need to convert back to multipiece and clear
					try {						
						convertToMultiPieceBitmap(false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				ThemeGraphic backupTG = getEntityImageFactory()
						.createEntityImage(element, null, 0, 0)
						.getThemeGraphics();

				if (backupTG != null) {
					undoObjs.add(backupTG);
				}

				   if (MultiPieceManager.isMultiPiece(element.getCurrentProperty())|| ((element.getDesignRectangles() != null) && (element.getDesignRectangles().size() >0)) )
					{					   
					   
					element.clearThemeGraphic();
					
					// clear childs
					BasicEntityImage img = (BasicEntityImage) ((IImageAdapter) getAdapter(IImageAdapter.class))
							.getImage(true);
					try {
						suppressNotification = true;
						List<IImage> parts = img.getPartInstances();
						for (IImage part : parts) {
							ThemeGraphic partUndo = (ThemeGraphic) clearThemeGraphics(part);
							undoPartObjs.add(partUndo);
						}
					} catch (Exception e) {
						Activator.error(e);
					} finally {
						suppressNotification = false;
					}
				} else {
					// normal element
					element.clearThemeGraphic();
				}
			    element.setSkinned(false);
			}

			if (undoPartObjs.size() == 0)
				return undoObjs;
			else {
				return new Object[] { undoObjs, undoPartObjs };
			}
			
		} finally {
			
			notifyChanged();			
		}
		
		
	}

	/**
	 * To know if a multipiece element has been converted to single piece
	 * @param element
	 * @return
	 */
	private boolean isCovertedElement(SkinnableEntity element) {
		boolean isConverted = false;
		if ((element.getDesignRectangles() != null)&&(element.getDesignRectangles().size() > 1)) {
			if ((element.getCurrentProperty()!=null) && (element.getCurrentProperty().equals(ThemeTag.ATTR_SINGLE_BITMAP))) {
				isConverted = true;
			}
		}
		return isConverted;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#clearThemeGraphics(com.nokia.tools.media.utils.layers.IImage)
	 */
	public Object clearThemeGraphics(IImage part) {
		Part element = (Part) ((BasicEntityImage) part).getEntity();
		try {
			ThemeGraphic backupTG = null;
			try {
				backupTG = (ThemeGraphic) element.getThemeGraphic().clone();
			} catch (ThemeException e) {
				Activator.error(e);
			}
			element.clearThemeGraphic();
			return backupTG;
		} finally {
			notifyPartChanged(element);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#setAttributes(java.util.Map)
	 */
	public void setAttributes(Map<Object, Object> map) {
		try {
			SkinnableEntity element = data.getSkinnableEntity();
			element.setAttribute(map);
		} finally {
			notifyChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#setLayerAttributes(int,
	 *      java.util.Map)
	 */
	public void setLayerAttributes(int index, Map<Object, Object> attrs) {
		try {
			SkinnableEntity element = data.getSkinnableEntity();
			try {
				ThemeGraphic tg = element.getThemeGraphic();
				List layers = tg.getImageLayers();
				if (layers.size() > index) {
					ImageLayer l = (ImageLayer) layers.get(index);
					l.setAttributes(attrs);
				}
			} catch (ThemeException e) {
				Activator.error(e);
			}
		} finally {
			notifyChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getLayerAttributes(int)
	 */
	public Map getLayerAttributes(int index) {
		SkinnableEntity element = data.getSkinnableEntity();
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			if (tg != null) {
				List layers = tg.getImageLayers();
				if (layers.size() > index) {
					ImageLayer l = (ImageLayer) layers.get(index);
					return l.getAttributes();
				}
			}
		} catch (ThemeException e) {
			Activator.error(e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getAttributes()
	 */
	public Map getAttributes() {
		SkinnableEntity element = data.getSkinnableEntity();
		if (element == null) {
			PlatformCorePlugin.error("No skinnable element found for "
					+ data.getData());
			return new HashMap();
		}
		// a cloned map to avoid client change the attribute directly, changes
		// should always go to set attributes or command
		return new HashMap(element.getAttribute());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isBMPImage()
	 */
	public boolean isBMPImage() {
		SkinnableEntity element = data.getSkinnableEntity();
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			List layers = tg.getImageLayers();
			if (!isSingleImageLayer(tg))
				return false;
			for (Object layer : layers) {
				ImageLayer l = (ImageLayer) layer;
				if (l.isBackground())
					continue;
				String filename = l.getFileName((Theme) data.getData()
						.getRoot());
				if (filename != null)
					if (filename.toLowerCase().endsWith(".bmp")) {
						return true;
					}
			}
		} catch (ThemeException e) {
			Activator.error(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isBitmap()
	 */
	public boolean isBitmap() {
		if (getId() == null) {
			return false;
		}
		SkinnableEntity element = data.getSkinnableEntity();
		if (element == null) {
			// preview image
			return false;
		}
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			if (tg == null) {
				return false;
			}
			List layers = tg.getImageLayers();
			if (!isSingleImageLayer(tg))
				return false;
			for (Object layer : layers) {
				ImageLayer l = (ImageLayer) layer;
				if (l.isBackground())
					continue;
				String filename = l.getFileName((Theme) data.getData()
						.getRoot());
				if (filename != null) {
					if (!filename.toLowerCase().trim().endsWith(
							IFileConstants.FILE_EXT_DOTSVG)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Activator.error(e);
		}
		return false;
	}

	/**
	 * @param tg
	 * @return
	 * @throws ThemeException
	 */
	public boolean isSingleImageLayer(ThemeGraphic tg) throws ThemeException {
		List layers = tg.getImageLayers();
		int c = 0;
		for (int i = 0; i < layers.size(); i++) {
			ImageLayer ll = (ImageLayer) layers.get(i);
			if (ll.isSelected()
					&& ll.getFileName((Theme) data.getData().getRoot()) != null) {
				c++;
				if (c > 1)
					return false;
			}
		}
		return c == 1;
		/*
		 * if (layers.size() == 1) return true; if (layers.size() == 2) return
		 * ((ImageLayer) layers.get(0)).isBackground(); return false;
		 */
	}

	public boolean isSingleImageLayer() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#hasMask()
	 */
	public boolean hasMask() {
		SkinnableEntity element = data.getSkinnableEntity();
		if (element == null)
			return false;
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			if (tg == null)
				return false;
			List layers = tg.getImageLayers();
			if (!isSingleImageLayer(tg))
				return false;
			for (Object layer : layers) {
				ImageLayer l = (ImageLayer) layer;
				if (l.isBackground())
					continue;

				String filename = l.getAttribute(ThemeTag.ATTR_SOFTMASK);
				if (filename == null)
					filename = l.getAttribute(ThemeTag.ATTR_HARDMASK);
				return filename != null;
			}
		} catch (Exception e) {
			Activator.error(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#hasImage()
	 */
	public boolean hasImage() {
		SkinnableEntity element = data.getSkinnableEntity();
		if (element == null)
			return false;
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			List layers = tg.getImageLayers();
			if (!isSingleImageLayer(tg))
				return false;
			for (Object layer : layers) {
				ImageLayer l = (ImageLayer) layer;
				if (l.isBackground())
					continue;
				String filename = l.getFileName((Theme) data.getData()
						.getRoot());

				// trick - colours also have defined filename - transparent.svg
				if (tg instanceof ColourGraphic && filename != null
						&& filename.endsWith("transparent.svg"))
					return false;

				return filename != null;
			}
		} catch (Exception e) {
			Activator.error(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getMask()
	 */
	public RenderedImage getMask() {
		SkinnableEntity element = data.getSkinnableEntity();
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			String maskPath = tg.getMask(false);
			if (maskPath == null)
				maskPath = tg.getMask(true);
			if (maskPath != null) {
				Layout lay = element.getLayoutInfo();
				// LayerUtils utils = new LayerUtils(element, lay.W(),
				// lay.H());
				return LayerUtils.getMaskImage(((Theme) data.getData()
						.getRoot()).getThemeDir(), (ImageLayer) tg
						.getImageLayers().get(0), element, lay.W(), lay.H());
			}
		} catch (Exception e) {
			Activator.error(e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isSVG()
	 */
	public boolean isSVG() {
		if (getId() == null) {
			return false;
		}
		SkinnableEntity element = data.getSkinnableEntity();
		try {
			ThemeGraphic tg = element.getThemeGraphic();
			if (tg instanceof ColourGraphic)
				return false;
			if (tg == null)
				return false;
			List layers = tg.getImageLayers();
			if (!isSingleImageLayer(tg))
				return false;
			for (Object layer : layers) {
				ImageLayer l = (ImageLayer) layer;
				if (l.isBackground())
					continue;
				String filename = l.getFileName((Theme) data.getData()
						.getRoot());
				if (filename != null)
					if (filename.toLowerCase().endsWith(
							IFileConstants.FILE_EXT_DOTSVG)) {
						return true;
					}
			}
		} catch (Exception e) {
			Activator.error(e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#setPartThemeGraphics(com.nokia.tools.media.utils.layers.IImage,
	 *      java.lang.Object, java.lang.String)
	 */
	public void setPartThemeGraphics(IImage prt, Object themeGr, String status) {
		Part _part = null;
		SkinnableEntity element = data.getSkinnableEntity();
		try {
			if (element != null) {
				ThemeGraphic graphic = (ThemeGraphic) themeGr;
				if (graphic != null) {
					graphic.setAttribute(ThemeTag.ATTR_STATUS,
							status == null ? ThemeTag.ATTR_VALUE_ACTUAL
									: status);
				}

				BasicEntityImage part = (BasicEntityImage) prt;
				_part = (Part) part.getEntity();
				_part.setThemeGraphic(graphic);
			}
		} catch (Exception e) {
			Activator.error(e);
		} finally {
			notifyPartChanged(_part);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isSkinned()
	 */
	public boolean isSkinned() {
		// because model if self is preview element, must ask
		// correct element if is skinned

		SkinnableEntity[] entities = data.getSkinnableEntities();
		for (SkinnableEntity entity : entities) {
			if (entity.isAnyChildDone()) {
				return true;
			}
		}

		// ask default
		return new Boolean((String) data.getAttribute(ContentAttribute.MODIFIED
				.name()));
	}
	
	public String getCopyPieceInfo() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getContentData()
	 */
	public IContentData getContentData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isPasteImageAllowed()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getClone(java.lang.Object)
	 */
	public Object getClone(Object themeGraphic) {
		if (themeGraphic instanceof ThemeGraphic)
			return ((ThemeGraphic) themeGraphic).clone();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getThemeGraphics()
	 */
	public Object getThemeGraphics() {
		SkinnableEntity e = data.getSkinnableEntity();
		if (e != null)
			return getEntityImageFactory().createEntityImage(e, null, 0, 0)
					.getThemeGraphics();
		// try {
		// if (e.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
		// return new EditableAnimatedEntity(e).getThemeGraphics();
		// } else {
		// return e.getThemeGraphic();
		// }
		// } catch (Exception ee) {
		// }
		return null;
	}

	public Object getThemeGraphics(boolean makePathsAbsolute) throws Exception {
		if (!makePathsAbsolute)
			return getThemeGraphics();

		SkinnableEntity e = data.getSkinnableEntity();
		if (e == null)
			return null;
		return getEntityImageFactory().createEntityImage(e, null, 0, 0)
				.getThemeGraphics(true);
	}

	public int getMultiPiecePartCount() {
		SkinnableEntity ent = data.getSkinnableEntity();
		if (ent != null) {
			if (ent.getChildren() != null) return ent.getChildren().size();
		}
		return 0;
	}
	
	public void setMultiPieceBitmap() {
		try {
			SkinnableEntity ent = data.getSkinnableEntity();
			if (ent != null) {
				if (ent.getChildren() == null || ent.getChildren().size() == 0)
					throw new RuntimeException(
							"this entity does not support this operation");
				if(ent.getAttributeValue(ThemeTag.ATTR_ENTITY_TYPE)!=null
						&& ent.getAttributeValue(ThemeTag.ATTR_ENTITY_TYPE).equals(ThemeTag.ELEMENT_FRAME)){ 
					ent.setCurrentProperty(ThemeTag.ATTR_9_PIECE);
				}
				else{
					ent.setCurrentProperty(ThemeTag.ATTR_11_PIECE);
				}
			}
		} finally {
			notifyChanged();
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#setSinglePieceBitmap()
	 */
	public void setSinglePieceBitmap() {
		try {
			SkinnableEntity ent = data.getSkinnableEntity();
			if (ent != null) {
				if (ent.getChildren() == null || ent.getChildren().size() == 0)
					throw new RuntimeException(
							"this entity does not support this operation");
				ent.setCurrentProperty(ThemeTag.ATTR_SINGLE_BITMAP);
			}
		} finally {
			notifyChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isColour()
	 */
	public boolean isColour() {
		return data.getSkinnableEntityByColor() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#isColourIndication()
	 */
	public boolean isColourIndication() {
		return !StringUtils.isEmpty(data.getData()
				.getAttributeValue("colourId"))
				&& (isSVG() || isBitmap());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class c) {
		if (IPasteTargetAdapter.class == c) {
			return this;
		}
		return data.getAdapter(c);
	}

	/**
	 * sets stretrch mode attr to first layer containing bitmap
	 */
	public void setStretchMode(String newStretchMode) {
	}

	/**
	 * gets stretrch mode attr from first layer containing bitmap
	 */
	public String getStretchMode() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#setPartThemeGraphics(int,
	 *      java.lang.Object, java.lang.String)
	 */
	public void setPartThemeGraphics(int partNo, Object newThemeGraphics,
			String status) throws ThemeException, Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#setPartsThemeGraphics(java.lang.Object[],
	 *      java.lang.String)
	 */
	public void setPartsThemeGraphics(Object[] newThemeGraphis, String status)
			throws Exception {
	}

	public boolean supportsMultiPiece() {
		if (isMultiPiece())
			return true;
		try {
			SkinnableEntity skinnableEntity = data.getSkinnableEntity();
			if (skinnableEntity != null) {
				List childs = skinnableEntity.getChildren();
				return (childs != null && MultiPieceManager.isMultiPiece(childs.size()));
			}
		} catch (Exception e) {
		}
		return false;
	}
		
	public String[] getSupportedFileExtensions() {
		Set<String> exts = data.getSkinnableEntity().getSupportedExtensions();
		if (null != exts)
			return exts.toArray(new String[exts.size()]);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#isBackground()
	 */
	public boolean isBackground() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#getDefaultStretchMode()
	 */
	public String getDefaultStretchMode() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.ISkinnableEntityAdapter#updateThemeGraphic(com.nokia.tools.media.utils.layers.IImage)
	 */
	public void updateThemeGraphic(IImage image) {
		try {
			setThemeGraphics(((BasicEntityImage) image)
					.getSavedThemeGraphics(false), null);
		} catch (Exception e) {
			Activator.error(e);
		} finally {
			notifyChanged();
		}
	}

	public IClipboardContentType getClipboardContentType(Object clipboardContent) {
		return getCopyPasteSupport().getClipboardContentType(clipboardContent);
	}

	protected IEntityImageFactory getEntityImageFactory() {
		return BasicEntityImageFactory.getInstance();
	}

	public ThemeData getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#isPasteAvailable(java.lang.Object,
	 *      java.lang.Object)
	 */
	public boolean isPasteAvailable(Object imageParam, Object params) {

		if (null == imageParam)
			return false;

		SkinnableEntity element = data.getSkinnableEntity();
		if (element == null && data.getData() instanceof SkinnableEntity) {
			element = (SkinnableEntity) data.getData();
		}
		if (element == null) {
			return false;
		}

		return getCopyPasteSupport().isPasteAvailable(imageParam);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#isPasteAvailable(java.awt.datatransfer.Clipboard,
	 *      java.lang.Object)
	 */
	public boolean isPasteAvailable(Clipboard clip, Object params) {
		if (clip == null) {
			clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		return getCopyPasteSupport().isPasteAvailable(clip);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.layers.IPasteTargetAdapter#paste(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Object paste(Object imageParameter, Object params) throws Exception {
		List children = null;
		try {
			SkinnableEntity element = data.getSkinnableEntity();

			if (params instanceof ILayer) {
				// if layer supplied
				((ILayer) params).paste(imageParameter);
				return null;
			}
			if (element == null) {
				return null;
			}

			if (isMultiPiece()) {
				// we are pasting 9-images to 9-piece elements
				children = element.getChildren();
				List<ThemeGraphic> backups = new ArrayList<ThemeGraphic>();
				for (Object child : children) {
					SkinnableEntity ch = (SkinnableEntity) child;
					try {
						ThemeGraphic backup = (ThemeGraphic) ch
								.getThemeGraphic().clone();
						backups.add(backup);
					} catch (Exception e) {
					}
				}

				getCopyPasteSupport().doPasteImage(imageParameter, null);
				// paste ok, return old theme graphics for undo
				return backups.toArray();
			} else {
				ThemeGraphic backupTG = getEntityImageFactory()
						.createEntityImage(element, null, 0, 0)
						.getThemeGraphics();

				getCopyPasteSupport().doPasteImage(imageParameter, params);
				// paste ok, return old theme graphics for undo
				return backupTG;
			}
		} finally {
			// no need to check if the actual changed one is part or element, if
			// it's part the setPartGraphics is always called where the
			// notification to the part itself can be captured

			if (params == null && children != null) {
				for (Object part : children) {
					notifyChanged(((SkinnableEntity) part));
				}
			}
			notifyChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#convertToNinePieceBitmap(boolean)
	 */
	public Object[] convertToMultiPieceBitmap(boolean fillParts)
			throws Exception {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#convertToNinePieceBitmap(boolean)
	 */
	public Object[] convertToNinePieceBitmap(boolean fillParts)
			throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#convertToSinglePieceBitmap(boolean)
	 */
	public Object convertToSinglePieceBitmap(boolean replaceGraphics)
			throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#getApplyAttributesCommand(java.util.Map)
	 */
	public Command getApplyBitmapPropertiesCommand(BitmapProperties properties) {
		ApplyThemeGraphicCommand command = new ApplyThemeGraphicCommand();
		command
				.setLabel(com.nokia.tools.theme.command.Messages.Command_ApplyBitmapProperties_Label);
		command.setTarget(data.getResource());
		command.setFeature(EditingUtil.getFeature(data.getResource(),
				"bitmapProperties"));
		command.setValue(properties);
		return command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.ISkinnableEntityAdapter#getApplyStretchModeCommand(java.lang.String)
	 */
	public Command getApplyStretchModeCommand(String stretchMode) {
		return UnexecutableCommand.INSTANCE;
	}

	public Object[] convertToElevenPieceBitmap(boolean fillParts)
			throws Exception {
		return null;
	}
	public Object[] convertToThreePieceBitmap(boolean fillParts)
			throws Exception {
		return null;
	}

	public boolean isMultiPiece() {
		return false;
	}
	
	public String getMultiPieceSearchViewText() {
		return "";
	}

	public boolean isConvertedFromMultipiece() {
		return isCovertedElement(data.getSkinnableEntity());
	}

	public String getColorDepth(int layerIndex) {
		return "";
	}
	
}