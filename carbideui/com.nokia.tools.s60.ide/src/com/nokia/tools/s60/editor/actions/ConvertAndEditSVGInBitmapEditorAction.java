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
package com.nokia.tools.s60.editor.actions;

import java.awt.image.RenderedImage;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.BaseRunnable;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread;
import com.nokia.tools.theme.ui.dialogs.SVG2BitmapConversionConfirmationDialog;

public class ConvertAndEditSVGInBitmapEditorAction extends AbstractEditAction {

	public static final String EDIT_3RD_BITMAP_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "ConvertAndEditSVG_context"; 

	public static final String ID = "ConvertAndEditImage"; 

	/* if try to determine size of image by editparts bounds, default = true */
	private boolean sizeFromEditPart = true;

	public ConvertAndEditSVGInBitmapEditorAction(ISelectionProvider provider,
			CommandStack stack) {
		super(provider, stack);
	}

	public ConvertAndEditSVGInBitmapEditorAction(IWorkbenchPart part) {
		super(part);
	}

	@Override
	public void doRun(Object element) {
		
		final Object _element = element;

		IPreferenceStore iPreferenceStore = ((AbstractUIPlugin) UtilsPlugin
				.getDefault()).getPreferenceStore();
		boolean ask = !iPreferenceStore
				.getBoolean(IMediaConstants.PREF_SILENT_SVG_CONVERSION);

		final IImageHolder holder = getImageHolder(_element);

		// changed loadImages to true to generate raw image.
		final ILayer layer = getLayer(true, _element);

		
		final IAnimationFrame frame = (IAnimationFrame) (element instanceof IAnimationFrame ? element
				: null);

		final boolean _layer = layer != null, _holder = holder != null, _animation = frame != null
				&& !_holder;
		final boolean _animatedParent = _layer
				&& layer.getParent() instanceof IAnimatedImage;

		try {

			int result = Window.OK; 
			SVG2BitmapConversionConfirmationDialog dialog = null;
			Rectangle rect = new Rectangle();

			if (ask) {
				// display warning dialog about conversion
				Shell shell = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell();

				if (_layer) {
					AbstractGraphicalEditPart _ep = null;
					if (sizeFromEditPart) {
						_ep = getGraphicalEditPart(_element);
					}
					if (_ep != null && !layer.getParent().isPart()) {
						Rectangle nr = _ep.getFigure().getBounds();
						rect.width = nr.width;
						rect.height = nr.height;
					} else {
						rect.width = layer.getParent().getWidth();
						rect.height = layer.getParent().getHeight();
					}

					dialog = new SVG2BitmapConversionConfirmationDialog(shell,
							rect, layer.supportMask(), layer.supportSoftMask(),
							true);
				} else if (_holder) {
					rect.width = holder.getWidth();
					rect.height = holder.getHeight();
					dialog = new SVG2BitmapConversionConfirmationDialog(shell,
							rect, true, true, true);
				} else if (_animation) {
					rect.width = frame.getWidth();
					rect.height = frame.getHeight();
					dialog = new SVG2BitmapConversionConfirmationDialog(shell,
							rect, false, false, true);
				}
				result = dialog.open();
			} else {

				if (_layer) {
					if (_layer && getGraphicalEditPart(_element) != null
							&& !layer.getParent().isPart()) {
						Rectangle nr = ((AbstractGraphicalEditPart) getGraphicalEditPart(_element))
								.getFigure().getBounds();
						rect.width = nr.width;
						rect.height = nr.height;
					} else {
						rect.width = layer.getParent().getWidth();
						rect.height = layer.getParent().getHeight();
					}
				} else if (_holder) {
					rect.width = holder.getWidth();
					rect.height = holder.getHeight();
				} else if (_animation) {
					rect.width = frame.getWidth();
					rect.height = frame.getHeight();
				}
			}
			if (result == Window.OK) {

				int w = rect.width, h = rect.height;

				if (dialog != null) {
					w = dialog.getSelectedWidth();
					h = dialog.getSelectedHeight();
				}

				boolean _keepMask = !ask ? (iPreferenceStore
						.getBoolean(IMediaConstants.PREF_SVG_CONVERSION_PRESERVE_MASK))
						: dialog.isMaskPreserve();

				
				// ask' = true
				if (!ask
						&& !iPreferenceStore
								.getBoolean(IMediaConstants.PREF_SVG_CONVERSION_PRESERVE_MASK)) {
					_keepMask = true;
				}

				// if elements don't supports mask, keepMask = false
				if (_layer && !_animatedParent)
					if (!layer.supportMask() && !layer.supportSoftMask())
						_keepMask = false;

				final boolean keepMask = _keepMask;

				CoreImage rasterized = CoreImage.create();

				if (_layer) {
					// convert SVG to appropriate dimensions
					IImage img = layer.getParent().getAnotherInstance(w, h);
					rasterized
							.init(img.getLayer(layer.getName()).getRAWImage());

					// special processing for colour indication items
					try {
						IContentData icd = getContentData(element);
						ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) icd
								.getAdapter(ISkinnableEntityAdapter.class);
						if (ska.isColourIndication()) {
							rasterized.extractMaskForColourIndicationItem()
									.invertSingleBandMask();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (_holder) {
					rasterized.init(holder.getRAWImage(w, h, false));
				} else if (_animation) {
					rasterized.init(frame.getRAWImage(false));
				}

				CoreImage _mask = null;
				if (keepMask) {
					// extract mask
					try {
						_mask = rasterized.copy().extractMask(true);

						
						if (_layer && !layer.supportSoftMask()
								&& !_animatedParent) {
							_mask = _mask.convertSoftMaskToHard(true);
						}
					} catch (Exception es) {
						es.printStackTrace();
					}

					if (_mask == null) {
						
						Shell shell = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell();
						MessageDialog.openWarning(shell, "Warning",
								Messages.ExtractMaskAction_error);
					}
				}

				final RenderedImage extractedMask = (_mask == null) ? (null)
						: (_mask.getAwt());

				// remove transparency from image - paint on white BG
				rasterized.reduceToThreeBand();

				RunnableWithParameter callback = new BaseRunnable() {

					public void run() {

						if (_holder) {
							if (getParameter() != null) {
								EditPart ep = getEditPart(_element);
								execute(new UndoableImageHolderActionCommand(
										holder, new Runnable() {
											public void run() {
												try {
													holder.paste(
															getParameter(),
															null);
													if (keepMask) {
														holder
																.pasteMask(extractedMask);
													}
												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}), ep);

							}
						}

						else if (_layer) {

							// called from animation editor
							try {
								if (_animatedParent) {
									((IAnimatedImage) layer.getParent())
											.getAnimationFrames()[0].paste(
											getParameter(), null);
								} else {
									layer.paste(getParameter());
								}
								if (keepMask) {
									if (_animatedParent) {
										((IAnimatedImage) layer.getParent())
												.getAnimationFrames()[0]
												.pasteMask(extractedMask);
									} else {
										layer.pasteMask(extractedMask);
									}
								}

								if (getContentData(_element) != null) {
									updateGraphicWithCommand(layer, _element);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						else if (_animation) {
							if (getParameter() != null) {
								try {
									frame.paste(getParameter(), null);
									if (keepMask) {
										frame.pasteMask(extractedMask);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				};

				if (_layer) {
					paintProcessThreads.add(FileChangeWatchThread
							.open3rdPartyEditor(
									IMediaConstants.PREF_BITMAP_EDITOR,
									rasterized.getAwt(), layer.getParent()
											.getId(), callback, true));
				} else if (_holder) {
					String prefix = holder.getImageFile().getName()
							.substring(
									0,
									holder.getImageFile().getName()
											.lastIndexOf('.'));
					paintProcessThreads.add(FileChangeWatchThread
							.open3rdPartyEditor(null, rasterized.getAwt(),
									prefix, callback, true));
				} else if (_animation) {
					String prefix = frame.getParent().getId()
							+ frame.getSeqNo();
					paintProcessThreads
							.add(FileChangeWatchThread
									.open3rdPartyEditor(
											IMediaConstants.PREF_BITMAP_EDITOR,
											rasterized.getAwt(), prefix,
											callback, true));
				}
			}

		} catch (Throwable e) {
			handleProcessException(e);
		}

	}

	@Override
	protected boolean doCalculateEnabled(Object element) {

		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			return holder.isSvg() && holder.supportsBitmap();
		}

		ILayer layer = getLayer(false, element);
		if (layer != null) {
			
			if (layer.getParent().isMultiPiece())
				return false;
			return layer.isSvgImage();
		} else if (element instanceof IAnimationFrame) {
			return ((IAnimationFrame) element).isSvg();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	@Override
	protected void init() {
		setId(ID);
		setText(Messages.ConvertAndEditAction_name); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ConvertAndEditSVGInBitmapEditorAction.EDIT_3RD_BITMAP_CONTEXT);
		super.init();
	}

	public void setSizeFromEditpart(boolean b) {
		sizeFromEditPart = b;
	}
}