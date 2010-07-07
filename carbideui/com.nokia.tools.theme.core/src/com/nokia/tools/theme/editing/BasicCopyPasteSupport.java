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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentElement;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.clipboard.IClipboardContentType;
import com.nokia.tools.media.utils.clipboard.ImageTransferable;
import com.nokia.tools.media.utils.clipboard.JavaObjectTransferable;
import com.nokia.tools.media.utils.clipboard.PasteHelper;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.SoundGraphic;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ToolBox;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.theme.core.MultiPieceManager;

public class BasicCopyPasteSupport {

	private SkinnableEntity entity;

	private IImage modelInstance;

	public BasicCopyPasteSupport(SkinnableEntity e) {
		entity = e;
	}

	public BasicCopyPasteSupport(SkinnableEntity e, IImage instance) {
		entity = e;
		modelInstance = instance;
	}

	protected synchronized IImage getEModel() {

		if (modelInstance == null) {
			modelInstance = getEntityImageFactory().createEntityImage(entity,
					null, 0, 0);
		}

		return modelInstance;
	}

	protected IEntityImageFactory getEntityImageFactory() {
		return BasicEntityImageFactory.getInstance();
	}

	public SkinnableEntity getEntity() {
		return entity;
	}

	public void doPasteImage(Object param, Object targetLayer) throws Exception {
		IImage emodel = getEModel();

		if (emodel instanceof IPasteTargetAdapter) {
			((IPasteTargetAdapter) emodel).paste(param, targetLayer);
			return;
		}

		if (emodel instanceof IAdaptable) {
			IPasteTargetAdapter adapter = (IPasteTargetAdapter) ((IAdaptable) emodel)
					.getAdapter(IPasteTargetAdapter.class);
			if (adapter != null) {
				adapter.paste(param, targetLayer);
				return;
			}
		}
		handlePasteImage(param, (String) targetLayer);
	}

	public boolean doCopyImage(Clipboard clip) {
		if (!isCopyImageAvailable())
			return false;
		return handleCopyImg(clip);
	}

	protected boolean handleCopyAnimatedImg(Clipboard clip) {
		return true;
	}

	
	protected boolean handleCopyMultiPieceImg(Clipboard clip) {
		return true;
	}

	protected boolean handleCopyImgForBg(Clipboard clip, ILayer layer) {
		return true;
	}

	private boolean handleCopyImg(Clipboard clip) {

		IImage emodel = getEModel();

		if (emodel instanceof IAnimatedImage) {
			return handleCopyAnimatedImg(clip);
		}

		if (emodel.isMultiPiece()) {
			try {
				return handleCopyMultiPieceImg(clip);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		// get first layer with image
		for (ILayer l : emodel.getLayers()) {
			if (l.hasImage() && !l.isBackground()) {
				String filename = l.getFileName(true);
				String mask = l.getMaskFileName(true);
				if (!FileUtils.isAbsolutePath(filename)) {
					// make absolute
					filename = ((Theme) entity.getRoot()).getThemeDir()
							+ File.separator + filename;
				}
				File image = new File(filename);
				if (image.exists()) {

					File maskFile = null;
					if (mask != null)
						maskFile = new File(mask);

					Object imgObject = null;
					if (maskFile != null && maskFile.exists()) {
						imgObject = new ArrayList<File>();
						((ArrayList) imgObject).add(image);
						((ArrayList) imgObject).add(maskFile);
					} else {
						imgObject = image;
					}

					Transferable trans = new ImageTransferable(imgObject);
					clip.setContents(trans, null);
					return true;
				}
			}
		}

		// we have no image - try copy BG layer
		if ((emodel.getLayer(0)).isBackground()) {
			// special handle
			return handleCopyImgForBg(clip, emodel.getLayer(0));
		}
		return false;
	}

	public boolean isPasteAvailable(Object object) {

		IImage emodel = getEModel();
		if (entity == null || emodel == null)
			return false;

		if (emodel instanceof IPasteTargetAdapter) {
			return ((IPasteTargetAdapter) emodel)
					.isPasteAvailable(object, null);
		}

		if (emodel instanceof IAdaptable) {
			IPasteTargetAdapter adapter = (IPasteTargetAdapter) ((IAdaptable) emodel)
					.getAdapter(IPasteTargetAdapter.class);
			if (adapter != null) {
				return adapter.isPasteAvailable(object, null);
			}
		}

		return _isPasteAvailable(object);
	}

	private boolean _isPasteAvailable(Object object) {
		BasicEntityImage emodel = (BasicEntityImage) getEModel();
		if (entity == null || emodel == null || object == null)
			return false;

		if (object instanceof ClipboardContentDescriptor) {
			ClipboardContentDescriptor cDesc = (ClipboardContentDescriptor) object;
			if (emodel.supportsMultiPiece() && MultiPieceManager.isMultiPiece(cDesc.getType())) {
				return true;
			}
			// delegate to full ClipboardContentDescriptor evaluation
			Clipboard dummyClip = new Clipboard("");
			JavaObjectTransferable t = new JavaObjectTransferable(object);
			dummyClip.setContents(t, (ClipboardContentDescriptor) object);
			return _isPasteAvailable(dummyClip);

		}
		/*
		 * test if param is ClipboardContentElement definition - it means that
		 * extended copy/paste is going on
		 */
		if (object instanceof ClipboardContentElement) {
			ClipboardContentElement content = (ClipboardContentElement) object;
			if (content.getContent() instanceof ThemeGraphic) {
				ThemeGraphic themeGr = (ThemeGraphic) content.getContent();
				ThemeGraphic our = emodel.getThemeGraphics();

				if (themeGr instanceof SoundGraphic) {
					/* check if target supports content */
					SoundGraphic sg = (SoundGraphic) themeGr;
					try {
						String ext = "."
								+ FileUtils.getExtension(sg.getImageFile());
						if (emodel.getEntity().getSupportedExtensions()
								.contains(ext))
							return true;
					} catch (Exception e) {
					}
					return false;
				}

				/*
				 * when entity is nine piece, and have been cleared before
				 * converting to nine piece, single-piece theme graphic is null.
				 * It means that entity has not specified single-piece graphic,
				 * but support it.
				 */
				//if ((emodel.isNinePiece() && our == null)
				if ((emodel.isMultiPiece() && our == null)
						|| themeGr.getClass().getName().equals(
								our.getClass().getName())) {
					// also test supported anim models
					boolean isAnimated = false;
					boolean isRealtime = false;
					boolean isRelative = false;
					if (content.getMetadata(2) instanceof Boolean) {
						isAnimated = (Boolean) content.getMetadata(2);
						isRealtime = (Boolean) content.getMetadata(3);
						isRelative = (Boolean) content.getMetadata(4);
					}
					boolean result = (!isAnimated || emodel.canBeAnimated())
							&& (!isRealtime || emodel
									.supportsAnimationTiming(TimingModel.RealTime))
							&& (!isRelative || emodel
									.supportsAnimationTiming(TimingModel.Relative));
					return result;
				}
				return false;
			}
		}

		if (emodel instanceof IAnimatedImage) {
			return PasteHelper.isParameterUsableAsImage(object);
		}

		if (emodel.isMultiPiece() || emodel.supportsMultiPiece()) {
			if (object instanceof List) {
				try {
					Object first = ((List) object).get(0);
					//if ( MultiPieceManager.isMultiPiece(((List) object).size())
							if (first instanceof File || first instanceof ThemeGraphic)
									return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// special case for nine piece dont support anything else
			// to paste except 9 piece
			if (emodel.isMultiPiece())
				return false;
		}
		
		if (emodel.getEntity().getToolBox().multipleLayersSupport) {
			return PasteHelper.isParameterUsableAsImage(object);
		}

		/* non-multilayer */
		if (emodel.getLayerCount() < 1 || emodel.getLayer(0).isBackground())
			return false;

		// paste not allowed to text / colour elements
		if (emodel.getThemeGraphics() instanceof ColourGraphic)
			return false;

		// else ask layer
		return emodel.getLayer(0).isPasteImageAvailable(object);
	}

	public boolean isPasteAvailable(Clipboard clip) {

		IImage emodel = getEModel();
		if (entity == null || emodel == null)
			return false;

		if (emodel instanceof IPasteTargetAdapter) {
			return ((IPasteTargetAdapter) emodel).isPasteAvailable(clip, null);
		}

		if (emodel instanceof IAdaptable) {
			IPasteTargetAdapter adapter = (IPasteTargetAdapter) ((IAdaptable) emodel)
					.getAdapter(IPasteTargetAdapter.class);
			if (adapter != null) {
				return adapter.isPasteAvailable(clip, null);
			}
		}

		return _isPasteAvailable(clip);
	}

	private boolean _isPasteAvailable(Clipboard clip) {

		if (entity == null || getEModel() == null)
			return false;

		BasicEntityImage emodel = (BasicEntityImage) getEModel();

		// paste not allowed to text / colour elements
		if (emodel.getThemeGraphics() instanceof ColourGraphic)
			return false;

		/* test if clipboard contains ThemeGraphics definition */
		for (DataFlavor fl : clip.getAvailableDataFlavors()) {
			if (fl.getMimeType().startsWith(
					DataFlavor.javaJVMLocalObjectMimeType)) {
				try {
					Object content = clip.getData(fl);
					if (content instanceof ClipboardContentDescriptor) {

						ClipboardContentDescriptor contentDescriptor = (ClipboardContentDescriptor) content;

						/* IContenData content? */
						if (contentDescriptor.getType() == ClipboardContentDescriptor.ContentType.CONTENT_ELEMENT) {
							// must get content decsriptor with type = graphics
							// & flags..

							IContentData source = (IContentData) contentDescriptor
									.getContent();

							ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) source
									.getAdapter(ISkinnableEntityAdapter.class);
							IToolBoxAdapter tbAdapter = (IToolBoxAdapter) source
									.getAdapter(IToolBoxAdapter.class);

							/*
							 * Flags meaning: boolean[] flags =
							 * {skAdapter.isColour(), toolBoxAdapter.isFile(),
							 * toolBoxAdapter.isText(),
							 * toolBoxAdapter.isMultipleLayersSupport(),
							 * toolBoxAdapter.isEffectsSupport()};
							 */
							boolean[] flags = { skAdapter.isColour(),
									tbAdapter.isFile(), tbAdapter.isText(),
									tbAdapter.isMultipleLayersSupport(),
									tbAdapter.isEffectsSupport() };
							IImageAdapter imgAdapter = (IImageAdapter) source
									.getAdapter(IImageAdapter.class);
							IImage sourceImage = imgAdapter.getImage(true);
							// content: boolean[] amimFlags = {animated,
							// realtime, relative};
							boolean animFlags[] = {
									sourceImage.isAnimated(),
									sourceImage
											.isAnimatedFor(TimingModel.RealTime),
									sourceImage
											.isAnimatedFor(TimingModel.Relative) };

							ClipboardContentDescriptor cDesc = new ClipboardContentDescriptor(
									skAdapter.getThemeGraphics(),
									ContentType.CONTENT_GRAPHICS);
							cDesc.setAttribute(IToolBoxAdapter.class, flags);
							cDesc.setAttribute(TimingModel.class, animFlags);
							contentDescriptor = cDesc;
						}

						if (contentDescriptor.getType() == ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS
								|| contentDescriptor.getType() == ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_MULTILAYER) {

							// paste graphics is never allowed for parts,
							// graphics represents whole element
							if (emodel.isPart())
								return false;

							/*
							 * Flags meaning: boolean[] flags =
							 * {skAdapter.isColour(), toolBoxAdapter.isFile(),
							 * toolBoxAdapter.isText(),
							 * toolBoxAdapter.isMultipleLayersSupport(),
							 * toolBoxAdapter.isEffectsSupport()};
							 */
							boolean[] flags = (boolean[]) contentDescriptor
									.getAttribute(IToolBoxAdapter.class);
							// only for graphics element is paste layers allowed
							if (flags[0] || flags[1] || flags[2])
								return false;
							ThemeGraphic tg = (ThemeGraphic) contentDescriptor
									.getContent();
							ToolBox tb = entity.getToolBox();

							if (flags[4]) {
								// effect support
								if (!tb.effectsSupport)
									return false;
							}
							if (flags[3] && tb.multipleLayersSupport) {
								// multi layer element - check animation types
								boolean animFlags[] = (boolean[]) contentDescriptor
										.getAttribute(TimingModel.class);
								if (animFlags != null) {
									// content: boolean[] amimFlags = {animated,
									// realtime, relative};
									boolean supportAnim = emodel
											.canBeAnimated();
									boolean realtime = emodel
											.supportsAnimationTiming(TimingModel.RealTime);
									boolean relative = emodel
											.supportsAnimationTiming(TimingModel.Relative);
									return (!animFlags[0] || supportAnim)
											&& (!animFlags[1] || realtime)
											&& (!animFlags[2] || relative);
								}
							}

							Boolean override = isPasteAvailableForAnimation(
									emodel, tg);
							if (override != null) {
								return override;
							}
							return (emodel.supportsMultiPiece() && tg instanceof ThemeGraphic)
									|| emodel.getThemeGraphics().getClass()
											.getName().equals(
													tg.getClass().getName());
						}
						/* nine piece content? */
						if (MultiPieceManager.isMultiPiece(contentDescriptor.getType())) {
							return emodel.supportsMultiPiece();
						}						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		}

		if (emodel instanceof IAnimatedImage) {
			return ClipboardHelper.clipboardContainsData(
					ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE
							| ClipboardHelper.CONTENT_TYPE_MULTIPLE_IMAGES,
					clip);
		}

		if (emodel.isMultiPiece() || emodel.supportsMultiPiece()) {			
			if (clip.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
				try {					
					/*if (((List) clip.getData(DataFlavor.javaFileListFlavor))
							.size() > 8)*/
					if (MultiPieceManager.isMultiPiece(((List) clip.getData(DataFlavor.javaFileListFlavor))
							.size()))
						return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (emodel.isMultiPiece())
				return false;
		}

		/*
		 * if entity is multi layer, test only if CB contains image data, we can
		 * always paste
		 */
		if (entity.getToolBox().multipleLayersSupport) {
			return ClipboardHelper.clipboardContainsData(
					ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE, clip);
		}

		/* not multilayer */
		if (emodel.getLayerCount() < 1)
			return false;
		// paste to background is not allowed
		if (emodel.getLayer(0).isBackground())
			return false;
		// else ask layer
		return emodel.getLayer(0).isPasteImageAvailable(clip);
	}

	protected Boolean isPasteAvailableForAnimation(BasicEntityImage emodel,
			ThemeGraphic tg) {
		return null;
	}

	public boolean isCopyImageAvailable() {
		IImage emodel = getEModel();
		if (emodel == null)
			return false;
		int partCount = 0;
		try {
			
			if (emodel instanceof BasicEntityImage) {
				partCount = MultiPieceManager.getElementPartCount(((BasicEntityImage) emodel).getEntity().getCurrentProperty());
			} else {
				partCount = emodel.getPartInstances().size();
			}
		} catch (Exception e) {
			
		}
		
		if (MultiPieceManager.isMultiPiece(partCount) && MultiPieceManager.isCopyImageAvailable(partCount))
			return true;
		if (emodel.getLayerCount() < 1)
			return false;
		// check that there is an imageObject in layer '0'
		return emodel.getLayer(0).hasImage();
	}

	protected void handlePasteImage(Object clipboardData, String targetLayerName)
			throws Exception {

		IImage emodel = getEModel();
		ILayer targetLayer = emodel.getLayer(targetLayerName);

		{

			ILayer iml = emodel.getLayer(0);
			if (iml.isBackground()) {
				if (emodel.getLayerCount() > 1) {
					iml = emodel.getLayer(1);
				} else {
					/* add layer when only BG present */
					iml = emodel.addLayer();
				}
			}

			if (targetLayer != null)
				iml = targetLayer;

			iml.clearLayer();
			iml.paste(clipboardData);

			// emodel updated, update originating entity
			ThemeGraphic tg = ((BasicEntityImage) emodel)
					.getSavedThemeGraphics(false);
			tg.setAttribute(ThemeTag.ATTR_STATUS, ThemeTag.ATTR_VALUE_ACTUAL);
			entity.setActualGraphic(tg);
		}
	}

	public void dispose() {
		entity = null;
	}

	public IPasteTargetAdapter getPasteTargetAdapter(Object target) {
		return new PasteTargetAdapter(this, target == null ? getEModel()
				: target);
	}

	public static class PasteTargetAdapter implements IPasteTargetAdapter {
		BasicCopyPasteSupport copyPaste;

		Object target;

		public PasteTargetAdapter(BasicCopyPasteSupport _copyPaste,
				Object _target) {
			this.copyPaste = _copyPaste;
			this.target = _target;
		}

		public boolean isPasteAvailable(Clipboard clip, Object params) {
			return copyPaste._isPasteAvailable(clip);
		}

		public boolean isPasteAvailable(Object data, Object params) {
			return copyPaste._isPasteAvailable(data);
		}

		public Object paste(Object data, Object params) throws Exception {
			if (params == null && target instanceof ILayer) {
				params = ((ILayer) target).getName();
			}

			if (params == null || params instanceof String) {
				copyPaste.handlePasteImage(data, (String) params);
				return null;
			}

			if (params != null && params instanceof ILayer) {
				((ILayer) params).paste(data);
				return null;
			}

			throw new UnsupportedOperationException(
					"unsupported object to paste");
		}
	}

	public IClipboardContentType getClipboardContentType(Object object) {
		if (object instanceof List) {
			try {
				if (((List) object).size() > 2
						&& (((List) object).get(0) instanceof File || ((List) object)
								.get(0) instanceof ThemeGraphic))
					return IClipboardContentType.MULTI_PIECE_GRAPHICS;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (object instanceof ClipboardContentDescriptor)
			if (MultiPieceManager.isMultiPiece(((ClipboardContentDescriptor) object).getType()))				
				return IClipboardContentType.MULTI_PIECE_GRAPHICS;
		return IClipboardContentType.UNKNOWN;
	}

}
