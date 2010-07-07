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
/*
 */
package com.nokia.tools.s60.editor.ui.views;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.DelegatingDragAdapter;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.eclipse.ui.views.properties.PropertySheet;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.part.DiagramGraphicalEditPart;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroupItem;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.platform.theme.ColourGraphic;
import com.nokia.tools.platform.theme.Part;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.ExternalEditorSupport;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AbstractEditAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ClearImageEditorAction;
import com.nokia.tools.s60.editor.actions.ColorizeSvgAction2;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.ExternalToolsDropdownAction;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeDDown;
import com.nokia.tools.s60.editor.dnd.S60BaseDragListener;
import com.nokia.tools.s60.editor.dnd.S60BaseDropListener;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction;
import com.nokia.tools.s60.views.ColorsView;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.CssColorDialog;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;
import com.nokia.tools.theme.content.ArtificialNotification;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.ui.Activator;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

/**
 */
public class LayersPage extends Page implements PropertyChangeListener, IContentListener,
		ILayersPage, ISelectionListener, IMenuListener {

	/** adapter for copy/paste via shortcuts */
	public class CopyPasteKeyAdapter extends KeyAdapter {

		ISelectionProvider provider;

		public CopyPasteKeyAdapter(ISelectionProvider provider) {
			this.provider = provider;
		}

		public void keyPressed(KeyEvent e) {
			
		}
	}

	List<IAction> globalHandlers = new ArrayList<IAction>();

	private Composite treeTabComposite;

	private TreeViewer treeViewer;

	private TreeViewLabelProvider treeViewerLabelProvider;
	
	private List<EditPart> currentParts = new ArrayList<EditPart>();

	private List<IContentData> cdatas = new ArrayList<IContentData>();

	private IViewPart parentView;

	// action for toolbar
	private AbstractAction animateAction;

	private AbstractAction clearAction;

	private ExternalToolsDropdownAction dropdownAction;

	private MenuManager menuMgr;

	private Series60EditorPart owningEditor;

	private ColorizingHelper colorizingHelper;

	/**
	 * Tooltip for picking colors.
	 */
	private CompositeTooltip colorPickerTooltip;

	private ToggleColorModeAction toggleColorModeAction;

	/**
	 * Notification source,
	 */
	private EObject target;

	private Adapter refreshAdapter = new TypedAdapter() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
		 */
		public void notifyChanged(Notification notification) {
			if (Notification.REMOVING_ADAPTER != notification.getEventType()
					&& notification.getNotifier() == getTarget()) {
				
				if(notification instanceof ArtificialNotification){
					//Get the modified multi-piece part 
					//SkinnableEntity carries the part which got modified
					SkinnableEntity skinnableEntityPart = ((ArtificialNotification) notification).getPart();

					if(skinnableEntityPart != null){
//						multi-piece flow
						refresh(skinnableEntityPart);
					}else{
//						single piece flow
						refresh();
					}
				}else{
					if(notification.getNewValue() instanceof ColourGraphic){
						refresh();
					}
				}
			}
		}
	};

	class TreeViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			return null;
		}

		private Object[] createMultiPieceRootContent(IImage image) {
			try {
				Object parts[] = image.getPartInstances().toArray();
				
				Object result[] = new Object[parts.length];
				for (int i = 0; i < result.length; i++) {
					
					Object item[] = new Object[4];
					/* label, sel. obj, editPart, TYPE */
					item[0] = Messages.LayersPage_partPrefix
					+ ((IImage) parts[i]).getPartType();
					item[1] = parts[i];
					item[2] = currentParts.get(0) == null ? cdatas.get(0)
							: currentParts.get(0);
					item[3] = AbstractAction.TYPE_PART;
					result[i] = item;
				}
				return result;
			} catch (Exception e) {
				e.printStackTrace();
				return new Object[0]; 
			}

		}

		private Object[] createImageRootContent(IImage image) {
			
			List<ILayer> layers = image.getSelectedLayers();
			List list = new ArrayList();

			for (ILayer layer : layers) {
				list.add(new Object[] {
						getLabel(layer),
						layer,
						currentParts.get(0) == null ? cdatas.get(0)
								: currentParts.get(0),
						AbstractAction.TYPE_LAYER });
			}

			return list.toArray();
		}

		private String getLabel(ILayer layer) {
			if (layer.getParent().getMaximumLayerCount() > 1) {
				return layer.getName();
			}

			String fileName = layer.getFileName(false);
			if (fileName != null) {
				return new Path(fileName).lastSegment();
			}
			return Messages.LayersPage_LabelNoContent;
		}

		private Object[] createLayerContent(ILayer layer) {
		
			List<ILayerEffect> effs = layer.getSelectedLayerEffects();
			List<Object> result = new ArrayList<Object>(effs.size());
			for (ILayerEffect e : effs) {
				if (IMediaConstants.APPLY_GRAPHICS.equals(e.getName())) {
					result.addAll(Arrays.asList(getChildren(e)));
				} else {
					result.add(e);
				}
			}
			return result.toArray();
		}

		private Object[] createPartContent(ILayer layer) {
			
			return getChildren(new Object[] {
					layer.getName(),
					layer,
					currentParts.get(0) == null ? cdatas.get(0) : currentParts
							.get(0), AbstractAction.TYPE_LAYER });
		}

		private Object[] createMultiSelectionColorContent(List<IImage> images,
				boolean intersection) {
			if (!ColorizeSvgAction2.IS_ENABLED) {
				return new Object[0];
			}

			if (images != null && images.size() > 0) {
				for (IImage image : images) {// in multiselection there
					// mustnt be any bmp
					if (image.getLayerCount() == 1) {
						ILayer layer = image.getLayer(0);
						if (layer.isBitmapImage()) {
							return new Object[0];
						}
					} else if (image.getLayerCount() > 1) { 
						
						return new Object[0];
					} else { // other cases
						return new Object[0];
					}
				}

				toggleColorModeAction.setEnabled(true);

				Map<RGB, Integer> colors = images.get(0).getLayer(0)
						.getColors();

				Comparator comparator = new Comparator<RGB>() {
					public int compare(RGB o1, RGB o2) {
						float[] hsb1 = o1.getHSB();
						float[] hsb2 = o2.getHSB();

						float diff = hsb1[0] - hsb2[0];
						if (diff != 0) {
							return diff < 0 ? -1 : 1;
						}
						diff = hsb1[1] - hsb2[1];
						if (diff != 0) {
							return diff < 0 ? -1 : 1;
						}
						diff = hsb1[2] - hsb2[2];
						return diff < 0 ? -1 : diff > 0 ? 1 : 0;
					}
				};

				if (intersection) {
					final List<RGB> initColors = new ArrayList<RGB>();
					for (RGB color : colors.keySet()) {
						initColors.add(color);
					}
					for (int i = 1; i < images.size(); i++) {
						ILayer layer = images.get(i).getLayer(0);
						Map<RGB, Integer> matchingColors = layer.getColors();
						Map<RGB, Integer> tempColors = new TreeMap<RGB, Integer>(
								comparator);
						for (RGB color : colors.keySet()) {
							if (matchingColors.containsKey(color)) {
								tempColors.put(color, matchingColors.get(color)
										+ colors.get(color));
							}
						}
						colors = tempColors;

					}
				} else {
					for (int i = 1; i < images.size(); i++) {
						Map<RGB, Integer> tempColors = new TreeMap<RGB, Integer>(
								comparator);
						tempColors.putAll(colors);
						ILayer layer = images.get(i).getLayer(0);
						Map<RGB, Integer> imageColors = layer.getColors();
						for (Map.Entry<RGB, Integer> entry : imageColors
								.entrySet()) {
							Integer colorCount = tempColors.get(entry.getKey());
							if (colorCount != null) {
								tempColors.put(entry.getKey(), colorCount
										+ entry.getValue());
							} else {
								tempColors
										.put(entry.getKey(), entry.getValue());
							}
						}

						colors = tempColors;
					}

				}
				List list = new ArrayList();
				if (colors != null) {
					Map<RGB, Integer> colorsListForGroups = new HashMap<RGB, Integer>();

					for (RGB rgb : colors.keySet()) {
						colorsListForGroups.put(rgb, list.size());
						String rgbCountString = "";
						if (colors.get(rgb) > 1) {
							rgbCountString = " (" + colors.get(rgb) + ")";
						}
						list.add(new Object[] {
								ColorUtil.asHashString(rgb) + rgbCountString,
								null, cdatas, AbstractAction.TYPE_COLOR, rgb });

					}

					if (ColorGroupsStore.isEnabled) {
						// substitute colorintersection with group intersection
						// if
						ColorGroups grps = colorizingHelper.getColorGroups();
						for (RGB rgb : colorsListForGroups.keySet()) {
							List<ColorGroup> cgs = grps.getGroupsByRGB(rgb);
							for (ColorGroup group : cgs) {
								boolean switchColorToGroup = true;
								for (IImage image : images) {
									if (!switchColorToGroup) {
										break;
									}
									if (image.getLayerCount() > 1) {
										for (ILayer layer : image.getLayers()) {
											if (group
													.containsItemWithIdAndLayerName(
															image.getId(),
															layer.getName())) {
												continue;
											}
											switchColorToGroup = false;
										}
									} else {
										if (group
												.containsItemWithIdAndLayerName(
														image.getId(), null)) {
											continue;
										} else {
											switchColorToGroup = false;
											break;
										}
									}
								}
								if (switchColorToGroup) {
									int index = colorsListForGroups.get(rgb);

									list.set(index, new Object[] {
											group.getName(), null, cdatas,
											AbstractAction.TYPE_COLOR_GROUP,
											rgb });
								}

							}
						}
					}
				}

				return list.toArray();
			} else {
				return new Object[0];
			}
		}

		private Object[] createColorsAndGroupContent(List list, ILayer layer) {
			if (!ColorizeSvgAction2.IS_ENABLED) {
				return new Object[0];
			}

			boolean isColor = false;
			if (((ISkinnableEntityAdapter) cdatas.get(0).getAdapter(
					ISkinnableEntityAdapter.class)).isColour()) {
				isColor = true;
			}
			if (layer.isSvgImage() || isColor) {
				// colors
				Map<RGB, Integer> colors = layer.getColors();
				if (isColor && cdatas.size() >= 1) {
					IColorAdapter colorAdapter = (IColorAdapter) cdatas.get(0)
							.getAdapter(IColorAdapter.class);
					RGB rgb = ColorUtil.toRGB(colorAdapter.getColor());
					if (rgb != null) {
						for (RGB rgbIter : colors.keySet()) {
							colors.remove(rgbIter);
						}
						colors.put(rgb, 1);
					}

				}
				Map<RGB, Integer> colorsListForGroups = new HashMap<RGB, Integer>();
				if (colors != null) {
					for (RGB rgb : colors.keySet()) {
						colorsListForGroups.put(rgb, list.size());
						String rgbCountString = "";
						if (colors.get(rgb) > 1) {
							rgbCountString = " (" + colors.get(rgb) + ")";
						}
						list.add(new Object[] {

								ColorUtil.asHashString(rgb) + rgbCountString,
								layer,
								currentParts.get(0) == null ? cdatas.get(0)
										: currentParts.get(0),
								AbstractAction.TYPE_COLOR, rgb });

					}
				}

				try {
					List<ColorGroup> groups = layer.getGroups();
					if (groups != null) {
						for (ColorGroup grp : groups) {
							if (colors.containsKey(grp.getGroupColor())) {

								int index = colorsListForGroups.get(grp
										.getGroupColor());
								list // replace color with group if the item
										// is registered in group with the same
										// color
										.set(
												index,
												new Object[] {
														grp.getName(),
														layer,
														currentParts.get(0) == null ? cdatas
																.get(0)
																: currentParts
																		.get(0),
														AbstractAction.TYPE_COLOR_GROUP,
														grp.getGroupColor() });
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return list.toArray();

		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof IImage) {

				IImage image = (IImage) parent;
//				if (image.isNinePiece() || image.isElevenPiece() || image.isThreePiece()) {
				if (image.isMultiPiece()) {					
					return createMultiPieceRootContent(image);
				} else {
					return createImageRootContent(image);
				}
			} else if (parent instanceof Object[]) {
				String itemType = (String) ((Object[]) parent)[3];
				if (itemType == AbstractAction.TYPE_LAYER) {
					return createLayerContent((ILayer) ((Object[]) parent)[1]);
				} else if (itemType == AbstractAction.TYPE_PART) {
					return createPartContent(((IImage) ((Object[]) parent)[1])
							.getLayer(0));
				} else
					return new Object[0];
			} else if (parent instanceof ArrayList) {
				return createMultiSelectionColorContent(
						(List<IImage>) parent,
						toggleColorModeAction.getColorMode() == ToggleColorModeAction.MODE_COMMON);
			} else {

				if (parent instanceof ILayerEffect) {
					ILayerEffect effect = (ILayerEffect) parent;
					if (IMediaConstants.APPLY_GRAPHICS.equals(effect.getName())) {
						// return mask and image
						ILayer ll = effect.getParent();
						boolean image = ll.hasImage();
						boolean mask = ll.hasMask();
						if (!image) {
							return new Object[0];
						}

						// prepare image object
						String resolution = ""; 
						if (ll.getRAWImage() != null)
							resolution = ll.getRAWImage().getWidth() + "x" 
									+ ll.getRAWImage().getHeight();

						String imgType = null;
						if (ll.getFileName(false) != null) {
							try {
								String exte = FileUtils.getExtension(ll
										.getFileName(false));
								if (exte != null)
									imgType = exte.toUpperCase();
								else
									imgType = Messages.LayersPage_bitmapImage; 
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						if (imgType == null)
							imgType = Messages.LayersPage_bgImage;

						Object imageModelObj = new Object[] {
								Messages.LayersPage_image + imgType
										+ ", " + resolution + ")",
								ll,
								currentParts.get(0) == null ? cdatas.get(0)
										: currentParts.get(0),
								AbstractAction.TYPE_IMAGE };

						if (mask) {
							// mask + image
							Object result[] = new Object[2];
							result[0] = imageModelObj;
							resolution = ""; 
							if (ll.getMaskImage() != null)
								resolution = ll.getMaskImage().getWidth() + "x" 
										+ ll.getMaskImage().getHeight();

							boolean softMask = ll.supportSoftMask();

							String startStr = Messages.LayersPage_mask1bit;
							if (softMask) {
								startStr = Messages.LayersPage_mask8bit;
							}
							result[1] = new Object[] {
									startStr + resolution + ")", ll, 
									currentParts.get(0) == null ? cdatas.get(0)
											: currentParts.get(0),
									AbstractAction.TYPE_MASK };
							return result;

						} else {
							List<Object> list = new ArrayList<Object>();
							if (!colorizingHelper.isColor(ll.getParent()
									.getId())) {
								// image
								list.add(imageModelObj);
							}
							try {
								return createColorsAndGroupContent(list, ll);
							} catch (Exception e) {
								e.printStackTrace();
								return list.toArray();
							}
						}
					}
				}
				return new Object[0];
			}
		}

		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}
	}

	static class Pair<E1, E2> {
		E1 e1;

		E2 e2;

		public Pair(E1 a, E2 b) {
			e1 = a;
			e2 = b;
		}
	}

	class TreeViewLabelProvider extends LabelProvider {

		Map<Object, Pair<ImageDescriptor, Image>> element2ImageCache = new HashMap<Object, Pair<ImageDescriptor, Image>>();

		public String getText(Object obj) {
			if (obj instanceof ILayer) {
				return ((ILayer) obj).getName();
			} else if (obj instanceof ILayerEffect) {
				return ((ILayerEffect) obj).getName();
			} else {
				Object array[] = (Object[]) obj;				
				return array[0].toString();

			}
		}

		public Image getImage(Object obj) {
			synchronized (element2ImageCache) {
				if (obj instanceof IImage) {
					try {
						ImageDescriptor desc = ((IImage) obj).getLayer(0)
								.getLayerIconImage();

						Pair<ImageDescriptor, Image> pair = element2ImageCache
								.get(obj);
						if (pair == null) {
							pair = new Pair<ImageDescriptor, Image>(desc,
									desc != null ? desc.createImage() : null);
							element2ImageCache.put(obj, pair);
							return pair.e2;
						} else {
							if (desc != pair.e1) {
								// image changed
								// dispose old image and create new
								if (pair.e2 != null && !pair.e2.isDisposed()) {
									pair.e2.dispose();
								}
								pair.e1 = desc;
								pair.e2 = desc != null ? desc.createImage()
										: null;
							}
							return pair.e2;
						}
					} catch (Exception e) {
						return null;
					}
				}
				if (obj instanceof Object[]) {
					ImageDescriptor desc = null;

					if (AbstractAction.TYPE_LAYER.equals(((Object[]) obj)[3])) {
						desc = ((ILayer) ((Object[]) obj)[1])
								.getLayerIconImage();
					} else if (AbstractAction.TYPE_IMAGE
							.equals(((Object[]) obj)[3])) {
						desc = ((ILayer) ((Object[]) obj)[1])
								.getLayerRawImageIcon();
					} else if (AbstractAction.TYPE_MASK
							.equals(((Object[]) obj)[3])) {
						desc = ((ILayer) ((Object[]) obj)[1])
								.getLayerMaskImageIcon();
					} else if (AbstractAction.TYPE_COLOR
							.equals(((Object[]) obj)[3])
							|| AbstractAction.TYPE_COLOR_GROUP
									.equals(((Object[]) obj)[3])) {
						RGB rgb = (RGB) ((Object[]) obj)[4];
						PaletteData paletteData = new PaletteData(
								new RGB[] { rgb });
						ImageData imageData = new ImageData(10, 10, 1,
								paletteData);
						for (int x = 0; x < 10; x++) {
							for (int y = 0; y < 10; y++) {
								imageData.setPixel(x, y, 0);
							}
						}
						desc = ((ImageDescriptor.createFromImageData(imageData)));
						// return desc.createImage();
					}

					Pair<ImageDescriptor, Image> pair = element2ImageCache
							.get(Arrays.asList(((Object[]) obj)));
					if (pair == null) {
						pair = new Pair<ImageDescriptor, Image>(desc,
								desc != null ? desc.createImage() : null);
						element2ImageCache.put(Arrays.asList(((Object[]) obj)),
								pair);
						return pair.e2;
					} else {
						if (desc != pair.e1) {
							// image changed
							// dispose old image and create new
							if (pair.e2 != null && !pair.e2.isDisposed()) {
								pair.e2.dispose();
							}
							pair.e1 = desc;
							pair.e2 = desc != null ? desc.createImage() : null;
							return pair.e2;
						} else {
							return pair.e2;
						}
					}
				}
				return null;
			}
		}

		@Override
		public void dispose() {
			flushImageCache();
			super.dispose();
		}

		public void flushImageCache() {
			synchronized (element2ImageCache) {
				for (Pair<ImageDescriptor, Image> pair : element2ImageCache
						.values()) {
					if (pair.e2 != null && !pair.e2.isDisposed()) {
						pair.e2.dispose();
					}
				}
				element2ImageCache.clear();
			}
		}
	}

	public LayersPage(Series60EditorPart part) {
		this.owningEditor = part;
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createControl(Composite parent) {
		treeTabComposite = new Composite(parent, SWT.NONE);
		treeTabComposite.setLayout(new FillLayout());

		treeViewer = new TreeViewer(treeTabComposite, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);

		colorizingHelper = new ColorizingHelper(owningEditor,
				getCommandStack(), treeViewer);
		treeViewer.setContentProvider(new TreeViewContentProvider());
		treeViewerLabelProvider = new TreeViewLabelProvider();
		treeViewer.setLabelProvider(treeViewerLabelProvider);

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object sel = ((IStructuredSelection) treeViewer.getSelection())
						.getFirstElement();
				if (sel instanceof Object[]) {
					if (AbstractAction.TYPE_IMAGE.equals(((Object[]) sel)[3])) {
						ILayer layer = (ILayer) ((Object[]) sel)[1];
						if (layer.isBackground())
							return;
						if (layer.isBitmapImage()) {
							AbstractEditAction action = new EditImageInBitmapEditorAction(
									treeViewer, getCommandStack());
							action.run();
						} else {
							AbstractEditAction action = new EditImageInSVGEditorAction(
									treeViewer, getCommandStack());
							action.run();
						}
					} else if (AbstractAction.TYPE_MASK
							.equals(((Object[]) sel)[3])) {
						// execute open-mask-in-editor action
						AbstractEditAction action = new EditMaskAction(
								treeViewer, getCommandStack());
						action.run();
					} else if (AbstractAction.TYPE_LAYER
							.equals(((Object[]) sel)[3])) {
						ILayer layer = (ILayer) ((Object[]) sel)[1];
						if (layer.hasImage() || layer.isBackground()) {
							// open editor
							if (layer.isBitmapImage() || layer.isBackground()) {
								AbstractEditAction action = new EditImageInBitmapEditorAction(
										treeViewer, getCommandStack());
								action.run();
							} else {
								AbstractEditAction action = new EditImageInSVGEditorAction(
										treeViewer, getCommandStack());
								action.run();
							}
						}
					} else if (AbstractAction.TYPE_COLOR
							.equals(((Object[]) sel)[3])
							|| AbstractAction.TYPE_COLOR_GROUP
									.equals(((Object[]) sel)[3])) {
						RGB rgb = (RGB) ((Object[]) sel)[4];
						String hashColor = ColorUtil.asHashString(rgb);

						CssColorDialog dialog = new CssColorDialog(PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell());
						dialog.setRGBString(hashColor);

						if (dialog.open() == CssColorDialog.CANCEL) {
							return;
						}

						RGB newColor = dialog.getRGB();

						colorizingHelper.handleColorChange(newColor, sel);
					}
				}
			}
		});

		
			

		colorPickerTooltip = colorizingHelper.createTooltip();

		addDragAndDropSupport();
		hookContextMenu();
		contributeToActionBars();
	}

	private List<IContentData> findContentDataForGroup(IEditorPart editor,
			ColorGroup group) {
		List<IContentData> datas = new ArrayList<IContentData>();

		IContentAdapter adapter = (IContentAdapter) editor
				.getAdapter(IContentAdapter.class);
		if (adapter != null) {
			IContent[] cnt = adapter.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);
			for (ColorGroupItem item : group.getGroupItems()) {
				IContentData data = root.findById(item.getItemId());
				if (data == null) {
					data = root.findByName(item.getItemId());
				}
				datas.add(data);
			}
		}

		return datas;
	}

	protected CommandStack getCommandStack() {
		return (CommandStack) owningEditor.getAdapter(CommandStack.class);
	}

	private void hookContextMenu() {
		menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(this);
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
	}

	public void menuAboutToShow(IMenuManager manager) {
		colorPickerTooltip.hide();
		fillContextMenu(manager);
	}

	private void contributeToActionBars() {
		IActionBars bars = getSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();

		/* add global handlers */
		for (IAction a : globalHandlers) {
			if (a instanceof AbstractAction) {
				((AbstractAction) a).dispose();
			}
		}
		globalHandlers.clear();

		/* global handlers */

		_setGlobalHandler(bars, new CopyImageAction(treeViewer, null));
		_setGlobalHandler(bars, new PasteImageAction(treeViewer,
				getCommandStack(), null));

		_setGlobalHandler(bars, new EditImageInSVGEditorAction(treeViewer,
				getCommandStack()));
		_setGlobalHandler(bars, new EditImageInSVGEditorAction(treeViewer,
				getCommandStack()));
		_setGlobalHandler(bars, new EditImageInBitmapEditorAction(treeViewer,
				getCommandStack()));
		_setGlobalHandler(bars, new ConvertAndEditSVGInBitmapEditorAction(
				treeViewer, getCommandStack()));
		_setGlobalHandler(bars, new EditInSystemEditorAction(treeViewer,
				getCommandStack()));
		_setGlobalHandler(bars, new EditMaskAction(treeViewer,
				getCommandStack()));
		_setGlobalHandler(bars, new EditMaskAction2(treeViewer,
				getCommandStack()));
		_setGlobalHandler(bars, new SetStretchModeAction(treeViewer,
				getCommandStack(), IMediaConstants.STRETCHMODE_STRETCH));
		_setGlobalHandler(bars, new SetStretchModeAction(treeViewer,
				getCommandStack(), IMediaConstants.STRETCHMODE_ASPECT));

		/* set undo/redo handlers from parent editor */
		ActionRegistry registry = (ActionRegistry) owningEditor
				.getAdapter(ActionRegistry.class);
		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), registry
				.getAction(ActionFactory.UNDO.getId()));
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(), registry
				.getAction(ActionFactory.REDO.getId()));

		/* fill toolbar */
		manager.add(bars.getGlobalActionHandler(CopyImageAction.ID));

		manager.add(bars.getGlobalActionHandler(PasteImageAction.ID));

		manager.add(new Separator());

		animateAction = new OpenGraphicsEditorAction(null, treeViewer);
		manager.add(animateAction);

		dropdownAction = new ExternalToolsDropdownAction(treeViewer,
				getCommandStack());
		manager.add(dropdownAction);

		toggleColorModeAction = new ToggleColorModeAction(
				ToggleColorModeAction.MODE_COMMON);
		manager.add(new Separator());
		manager.add(toggleColorModeAction);

		// 'clear whole element' action
		clearAction = new ClearImageEditorAction(treeViewer, getCommandStack());
		clearAction.setToolTipText(Messages.LayersPage_clearTitle);
		manager.add(new Separator());
		manager.add(clearAction);

		bars.setGlobalActionHandler(ClearImageEditorAction.ID, clearAction);
		bars.setGlobalActionHandler(OpenGraphicsEditorAction.ID, animateAction);

		bars.updateActionBars();
	}

	private void _setGlobalHandler(IActionBars bars, AbstractAction aa) {
		if (aa != null) {
			aa.listenSelection();
			globalHandlers.add(aa);
			bars.setGlobalActionHandler(aa.getId(), aa);
		}
	}

	private void _add(IMenuManager manager, IAction act) {
		if (act.isEnabled())
			manager.add(act);
	}

	private void fillContextMenu(IMenuManager manager) {

		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();

		colorizingHelper.fillContextMenu(manager);

		boolean isCopyPasteSupport = true;
		Part selectedPart=null;
		
		if (selection.getFirstElement() instanceof Object[]) {
			Object array[] = (Object[]) selection.getFirstElement();
			boolean isColor = AbstractAction.TYPE_COLOR.equals(array[3]);
			boolean isColorGroup = AbstractAction.TYPE_COLOR_GROUP
					.equals(array[3]);
			if (isColor || isColorGroup) {
				isCopyPasteSupport = false;
			}
		}

		// copy and paste actions
		if (isCopyPasteSupport) {
			addCopyAndPasteActionsToContextMenu(manager);
		}

		SetStretchModeDDown ddown = new SetStretchModeDDown(null, treeViewer,
				getCommandStack());
		boolean setStretchEnabled = false;

		// only allow stretch mode command for multilayer elemnts:
		if (cdatas != null && (cdatas.get(0) != null)) {
			IToolBoxAdapter tba = (IToolBoxAdapter) cdatas.get(0).getAdapter(
					IToolBoxAdapter.class);
			if (tba != null && tba.isMultipleLayersSupport()) {
				setStretchEnabled = true;
			}
		} else if (currentParts != null && (currentParts.get(0) != null)) {
			IScreenElement screenElement = JEMUtil
					.getScreenElement(currentParts.get(0));
			if (screenElement != null) {
				IToolBoxAdapter tba = (IToolBoxAdapter) screenElement.getData()
						.getAdapter(IToolBoxAdapter.class);
				if (tba != null && tba.isMultipleLayersSupport()) {
					setStretchEnabled = true;
				}
			}
		}

		boolean _layer, _image, _mask, _part;
		_layer = _image = _mask = _part = false;
		if (selection.getFirstElement() instanceof Object[]) {
			Object array[] = (Object[]) selection.getFirstElement();
			if (AbstractAction.TYPE_IMAGE.equals(array[3])) {
				_image = true;
			} else if (AbstractAction.TYPE_MASK.equals(array[3])) {
				_mask = true;
			} else if (AbstractAction.TYPE_PART.equals(array[3])) {
				selectedPart =(Part)(((EditableEntityImage)array[1]).getEntity());
				_part = true;
			} else if (AbstractAction.TYPE_LAYER.equals(array[3])) {
				_layer = true;
			}
		}

		IActionBars bars = getSite().getActionBars();

		if (_image || _layer || _part) {
			manager.add(new Separator());
			BrowseForFileAction fileAction = new BrowseForFileAction(
					owningEditor);
			fileAction.setSelectionProvider(treeViewer);
			manager.add(fileAction);
			manager.add(new Separator());
			IAction action = bars
					.getGlobalActionHandler(EditImageInBitmapEditorAction.ID);
			_add(manager, action);
			action = bars
					.getGlobalActionHandler(ConvertAndEditSVGInBitmapEditorAction.ID);
			_add(manager, action);
			action = bars.getGlobalActionHandler(EditImageInSVGEditorAction.ID);
			_add(manager, action);
			action = bars.getGlobalActionHandler(EditInSystemEditorAction.ID);
			_add(manager, action);
			// stretch mode submenu
			if (setStretchEnabled) {
				manager.add(new Separator());
				manager.add(ddown);
			}
		}
		if (_mask || _layer || _part) {
			manager.add(new Separator());
			IAction action = bars.getGlobalActionHandler(EditMaskAction.ID);
			_add(manager, action);

			action = bars.getGlobalActionHandler(EditMaskAction2.ID);
			_add(manager, action);
		}
		if (_part) {
			// PART NODE in 9-Piece
			try {
				if(((Part) selectedPart.getLink()).getThemeGraphics()!=null){
					manager.add(new Separator());
					IAction clear = new ClearImageEditorAction(treeViewer,
							getCommandStack());
					clear.setText(clear.getText() + " Part");
					manager.add(clear);
				}
			} catch (ThemeException e) {
				// TODO Auto-generated catch block
				Activator.error(e);
			}
		}


	}

	private void addCopyAndPasteActionsToContextMenu(IMenuManager manager) {
	    manager.add(new CopyImageAction(treeViewer, null));
	    manager.add(new PasteImageAction(treeViewer, getCommandStack(),
	    		null));
	    manager.add(new Separator());
    }

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	@Override
	public Control getControl() {
		return treeTabComposite;
	}

	/**
	 * Events coming from EditableEntityImage, listening for adding layers,
	 * etc..
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() instanceof IImage) {

			if (Display.getCurrent() == null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						treeViewer.expandAll();
					}
				});
			} else {
				treeViewer.expandAll();
			}
		}
	}
	
	//Selection changed notification from outside the Layers View
	public void selectionChanged(IWorkbenchPart part, ISelection selection){
		selectionChanged(part, selection, null);
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection, SkinnableEntity skinnableEntity) {
		if (target != null) {
			target.eAdapters().remove(refreshAdapter);
		}
		try {
			if (part instanceof ColorsView) {
				return;
			}

			if (part instanceof PropertySheet
					|| part instanceof Series60EditorPart
					&& part != owningEditor)
				return;

			// ------------------------
			if (part == parentView
					|| !(selection instanceof IStructuredSelection))
				return;

			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			boolean selectionIsList = false;
			boolean selectionIsListWithOneElement = false;
			if (structuredSelection.getFirstElement() instanceof List) {
				selectionIsList = true;
				List tempList = (List) (structuredSelection.getFirstElement());
				if (tempList.size() == 1) {
					selectionIsListWithOneElement = true;
				}
			}

			toggleColorModeAction.setEnabled(false);

			if (structuredSelection.size() == 1
					&& !(selectionIsList && !selectionIsListWithOneElement)) {

				// when selection contains valid contents -
				// EditPart or IContentData, refresh
				Object element = structuredSelection.getFirstElement();
				Object elementToSearch = null;
				if (element instanceof List) {
					elementToSearch = ((List) element).get(0);
				} else {
					elementToSearch = element;
				}
				IContentData cd = findContentData(elementToSearch);
				if (cd != null) {
					target = (EObject) cd.getAdapter(EditObject.class);
				}
				EditPart editPart = (EditPart) (elementToSearch instanceof EditPart ? elementToSearch
						: null);
				if (isAppTheme(editPart)) {
					_clearViewer();
					return;
				}

				if (cd != null) {

					try {
						
						IToolBoxAdapter tba = (IToolBoxAdapter) cd
								.getAdapter(IToolBoxAdapter.class);
						if (tba != null && tba.isFile()) {
							_clearViewer();
							return;
						}

						ISkinnableEntityAdapter sk = (ISkinnableEntityAdapter) cd
								.getAdapter(ISkinnableEntityAdapter.class);

						if (null != sk && sk.isColour() && !sk.hasImage()) {
							IImageAdapter adapter = (IImageAdapter) cd
									.getAdapter(IImageAdapter.class);
							
							IImage image = adapter == null ? null : adapter
									.getImage();
							this.cdatas.clear();
							this.currentParts.clear();
							this.cdatas.add(cd);
							this.currentParts.add(editPart);
							image.removePropertyChangeListener(this);
							image.addPropertyListener(this);
							if (treeViewer.getInput() != image
									&& treeViewerLabelProvider != null) {
								treeViewerLabelProvider.flushImageCache();
							}

							try {
								// parentSelectionIndex, ChildSelectionIndex
								int[] tSIndex = {-1, -1};
								
								if(true == image.isMultiPiece()){									
									if(skinnableEntity != null){
										tSIndex = getTreeSelectionForMultipiece(skinnableEntity);
									}						
								}else{								
									tSIndex = getTreeSelectionForSinglePiece();
								}
								
								treeViewer.setInput(image.getLayer(0)
										.getLayerEffects().get(0));
								treeViewer.expandAll();
								makeTreeSelection(tSIndex);
								
							} catch (Exception e) {
								e.printStackTrace();
							}
							return;
						}

						IImageAdapter adapter = (IImageAdapter) cd
								.getAdapter(IImageAdapter.class);
						// adapter can be null for non-theme elements
						IImage image = adapter == null ? null : adapter
								.getImage();

						if (image != null) {
							this.cdatas.clear();
							this.currentParts.clear();
							this.cdatas.add(cd);
							this.currentParts.add(editPart);

							image.removePropertyChangeListener(this);
							image.addPropertyListener(this);
							if (treeViewer.getInput() != image
									&& treeViewerLabelProvider != null) {
								treeViewerLabelProvider.flushImageCache();
							}
									
							// parentSelectionIndex, ChildSelectionIndex
							int[] tSIndex = {-1, -1};
							
							if(true == image.isMultiPiece()){								
								if(skinnableEntity != null){
									tSIndex = getTreeSelectionForMultipiece(skinnableEntity);
								}						
							}else{								
								tSIndex = getTreeSelectionForSinglePiece();
							}
							
							treeViewer.setInput(image);
							treeViewer.expandAll();
							makeTreeSelection(tSIndex);
							
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				// when selection contains valid multiselection contents -
				// EditParts or IContentDatas, refresh
				Object[] elements = structuredSelection.toArray();
				ArrayList<IImage> images = new ArrayList<IImage>();
				if (elements.length > 0) {
					this.cdatas = new ArrayList<IContentData>();
					this.currentParts = new ArrayList<EditPart>();

					if (elements[0] instanceof List) {
						elements = ((List) elements[0]).toArray();
					}
				}
				for (Object element : elements) {
					IContentData cd = findContentData(element);
					if (cd != null) {
						EObject object = (EObject) cd
								.getAdapter(EditObject.class);
						if (object != null) {
							target = object;
						}
					}
					EditPart editPart = (EditPart) (element instanceof EditPart ? element
							: null);

					try {
						if (null == cd) {
							_clearViewer();
							return;
						}
						
						IToolBoxAdapter tba = (IToolBoxAdapter) cd
								.getAdapter(IToolBoxAdapter.class);
						if (tba != null && tba.isFile()) {
							_clearViewer();
							return;
						}

						IImageAdapter adapter = (IImageAdapter) cd
								.getAdapter(IImageAdapter.class);
						// adapter can be null for non-theme elements
						IImage image = adapter == null ? null : adapter
								.getImage();

						if (image != null) {
							this.currentParts.add(editPart);
							this.cdatas.add(cd);

							image.removePropertyChangeListener(this);
							image.addPropertyListener(this);

							images.add(image);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				treeViewerLabelProvider.flushImageCache();
				treeViewer.setInput(images);
				treeViewer.expandAll();
			}
			// ------------------------

			
			SimpleSelectionProvider ssp = new SimpleSelectionProvider((cdatas
					.size() == 1) ? cdatas.get(0) : null);
			clearAction.setSelectionProvider(ssp);
			clearAction.update();
			animateAction.setSelectionProvider(ssp);
			animateAction.update();
		} finally {
			if (target != null) {
				target.eAdapters().add(refreshAdapter);
			}
		}
	}
	
	private void makeTreeSelection(int[] tSIndex) {
		if(tSIndex[0] >= 0){
			TreeItem[] parentItem = treeViewer.getTree().getItems();
			if(tSIndex[1] >= 0){
				//Select Child
				treeViewer.getTree().showItem(parentItem[tSIndex[0]].getItem(tSIndex[1]));
				treeViewer.getTree().select(parentItem[tSIndex[0]].getItem(tSIndex[1]));
			}else{
				//Select Parent
				treeViewer.getTree().showItem(parentItem[tSIndex[0]]);
				treeViewer.getTree().select(parentItem[tSIndex[0]]);
			}
		}
	}

	private int[] getTreeSelectionForSinglePiece(){
		int parentItemIndex = -1;
		int childItemIndex = -1;
		TreeItem[] treeItem = treeViewer.getTree().getItems();
		
		if(treeItem.length == 0){
			return (new int[] {parentItemIndex, childItemIndex});
		}
		
		parentItemIndex = 0;
		/* label, sel. obj, editPart, TYPE */
		if(! ((treeItem[0].getData()) instanceof Object[])){
			return (new int[] {parentItemIndex, childItemIndex});
		}else{
			TreeItem[] selectedTreeItems = treeViewer.getTree().getSelection();
//			Since Single selection
			if (selectedTreeItems != null && selectedTreeItems.length == 1) {
				if(selectedTreeItems[0].equals(treeItem)){
					return (new int[] {0, -1});
				}else{
					childItemIndex = treeItem[0].indexOf(selectedTreeItems[0]);
				}
			}
		}
		
		return (new int[] {parentItemIndex, childItemIndex});
		
	}
	
	private int[] getTreeSelectionForMultipiece(SkinnableEntity skinnableEntity) {
		TreeItem[] treeItem = treeViewer.getTree().getItems();
		TreeItem parentItem = null;
		int parentItemIndex = -1;
		int childItemIndex = -1;
		int countParent = -1;
		
		for (TreeItem treeItem2 : treeItem) {
			countParent ++;
			/* label, sel. obj, editPart, TYPE */
			if(! ((treeItem2.getData()) instanceof Object[])){
				return (new int[] {parentItemIndex, childItemIndex});
			}else{
				Object[] treeItemDatas = (Object[]) treeItem2.getData();
				for (Object treeItemData : treeItemDatas) {
					if (treeItemData instanceof EditableEntityImage) {
						if(((EditableEntityImage) treeItemData).getEntity() == skinnableEntity){
							parentItem = treeItem2;
							break;
						}
					}					
				}
				
				if (parentItem != null) {
					parentItemIndex = countParent;
					break;
				}				
			}
		}
		
		if(parentItem != null){
			
			TreeItem[] selectedTreeItems = treeViewer.getTree().getSelection();	
			
			boolean selectParent = false;
			if((selectedTreeItems != null) && (selectedTreeItems.length == 1)){
				Object[] selectedTreeItemDatas = (Object[]) selectedTreeItems[0].getData();
				for (Object selectedTreeItemData : selectedTreeItemDatas) {
					if (selectedTreeItemData instanceof EditableEntityImage) {
						selectParent = true;
						break;
					}
				}
			}			
			
			if(selectParent != true){				
				TreeItem[] selectedChildItems = treeViewer.getTree().getSelection();
				if (selectedChildItems != null && selectedChildItems.length == 1) {
					childItemIndex = parentItem.indexOf(selectedChildItems[0]);
				}				
			}			
		}		
		return (new int[] {parentItemIndex, childItemIndex});
	}
	
	

	private IContentData findContentData(Object element) {
		return JEMUtil.getContentData(element);
	}

	// DND support
	private void addDragAndDropSupport() {

		/* DragSourceListener for category viewer */
		if (treeViewer != null) {

			LocalSelectionTransfer localTransfer = LocalSelectionTransfer
					.getInstance();
			FileTransfer fileTransfer = FileTransfer.getInstance();
			Transfer transfers[] = new Transfer[] { localTransfer, fileTransfer };

			DelegatingDragAdapter dragAdapter = new DelegatingDragAdapter();
			dragAdapter.addDragSourceListener(new S60BaseDragListener(
					fileTransfer, treeViewer));
			dragAdapter.addDragSourceListener(new S60BaseDragListener(
					localTransfer, treeViewer));

			treeViewer.addDragSupport(DND.DROP_COPY, transfers, dragAdapter);
			treeViewer.addDropSupport(DND.DROP_COPY, transfers,
					new CustomDropListener(null, getCommandStack()));
		}

	}

	/**
	 * drop listener for layers page
	 */
	class CustomDropListener extends S60BaseDropListener {

		public CustomDropListener(Transfer transfer, CommandStack stack) {
			super(transfer, stack);
		}

		public void dragOperationChanged(DropTargetEvent event) {
			event.detail = DND.DROP_COPY;
		}

		@Override
		protected ISkinnableEntityAdapter getSkinnableEntityAdapter(
				DropTargetEvent event) {
			ISkinnableEntityAdapter adapter = null;
			if (event.item.getData() instanceof Object[]) {
				// Object[] obj = (Object[]) event.item.getData();
				adapter = (ISkinnableEntityAdapter) LayersPage.this.cdatas.get(
						0).getAdapter(ISkinnableEntityAdapter.class);
				/*
				 * if (obj[2] instanceof EditPart) { EditPart part = (EditPart)
				 * obj[2]; Object model = part.getModel(); if (model instanceof
				 * IJavaObjectInstance) { IScreenElement screenElement =
				 * (IScreenElement) EcoreUtil .getRegisteredAdapter(
				 * ((IJavaObjectInstance) model), IScreenElement.class); if
				 * (screenElement != null) { adapter = (ISkinnableEntityAdapter)
				 * screenElement .getData().getAdapter(
				 * ISkinnableEntityAdapter.class); } } }
				 */
			}
			return adapter;
		}

		@Override
		protected EditPart getSourceEditPart(DropTargetEvent event) {
			Object[] obj = (Object[]) event.item.getData();
			if (obj[2] instanceof EditPart) {
				return (EditPart) obj[2];
			} else
				return null;
		}

		@Override
		protected IWorkbenchPart getWorkbenchPart() {
			return LayersPage.this.parentView;
		}

		@Override
		protected ISelectionProvider getSelectionProviderForGenericCommand(
				DropTargetEvent event) {

			Object treeData = event.item.getData();

			if (treeData instanceof Object[]
					&& ((Object[]) treeData)[1] instanceof IImage) {
				/* this node represents part of nine-piece element */
				Object[] obj = (Object[]) treeData;
				return ExternalEditorSupport.getSelectionProvider(obj);
			} else {
				return ExternalEditorSupport
						.getSelectionProvider(cdatas.get(0));
			}
		}

	}

	public void setParentView(IViewPart parentView) {
		this.parentView = parentView;
	}

	private void _clearViewer() {
		if (treeViewerLabelProvider != null) {
			treeViewerLabelProvider.flushImageCache();
		}
		if (treeViewer.getContentProvider() != null)
			treeViewer.setInput(null);
		this.currentParts.clear();
		this.cdatas.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		// removes the menu listener to clear reference to the content data when
		// disposed.
		if (menuMgr != null) {
			menuMgr.removeMenuListener(this);
		}
		for (IAction a : globalHandlers) {
			if (a instanceof WorkbenchPartAction) {
				((WorkbenchPartAction) a).dispose();
			}
		}
		globalHandlers.clear();
		getSite().getActionBars().clearGlobalActionHandlers();

		if (target != null) {
			target.eAdapters().remove(refreshAdapter);
			target = null;
		}
		try {
			clearAction.dispose();
			animateAction.dispose();
			dropdownAction.dispose();
		} catch (Exception e) {
		}
		clearAction = null;
		animateAction = null;
		dropdownAction = null;

		colorizingHelper.dispose();

		colorPickerTooltip.dispose();
		colorPickerTooltip = null;
		super.dispose();
	}

	public String _getElementName() {
		if ((this.cdatas.size() == 1) && (this.cdatas.get(0) != null)) {

			INamingAdapter adapter = (INamingAdapter) this.cdatas.get(0)
					.getAdapter(INamingAdapter.class);
			if (adapter != null) {
				return adapter.getName();
			}
			return cdatas.get(0).getName();

		} else {
			if (currentParts != null && currentParts.size() == 1
					&& (currentParts.get(0) != null)) {

				IScreenElement screenElement = JEMUtil
						.getScreenElement(currentParts.get(0));
				if (screenElement != null) {
					INamingAdapter adapter = (INamingAdapter) screenElement
							.getData().getAdapter(INamingAdapter.class);
					if (adapter != null) {
						return adapter.getName();
					}
					return screenElement.getData().getName();
				}
			}
		}
		return null;
	}

	private boolean isAppTheme(EditPart part) {
		IContentData data = null;
		if (part instanceof DiagramGraphicalEditPart) {
			if (part.getChildren() != null && part.getChildren().size() > 0) {
				if (part.getChildren().get(0) instanceof EditPart) {
					return true;
				}
			}
		}
		if (part != null) {
			IScreenElement screenElement = JEMUtil.getScreenElement(part);
			if (screenElement == null) {
				// disposed while switching resolution, etc.
				return false;
			}
			data = screenElement.getData();
		}
		if (data != null) {
			IImageAdapter imageAdapter = (IImageAdapter) data
					.getAdapter(IImageAdapter.class);
			if (imageAdapter == null) {
				return true;
			}
		}

		return false;
	}

	public String getPartName(String baseName) {

		boolean emptyLayer, multiPiece, isSound = false;
		String elName = _getElementName();
		if (elName == null)
			return baseName;

		IContentData data = cdatas.get(0);
		if (currentParts.get(0) != null) {
			IScreenElement screenElement = JEMUtil
					.getScreenElement(currentParts.get(0));
			if (screenElement == null) {
				// disposed while switching resolution, etc.
				return "";
			}
			data = screenElement.getData();
		}

		IImageAdapter imageAdapter = (IImageAdapter) data
				.getAdapter(IImageAdapter.class);
		if (imageAdapter == null) {
			return "";
		}
		IImage img = imageAdapter.getImage(true);

		IToolBoxAdapter tba = (IToolBoxAdapter) data
				.getAdapter(IToolBoxAdapter.class);
		if (tba != null && tba.isFile()) {
			isSound = true;
		}

		emptyLayer = img == null || img.getLayerCount() == 0;
		//multiPiece = img != null && (img.isNinePiece() || img.isElevenPiece() || img.isThreePiece() );
		multiPiece = img != null && (img.isMultiPiece() );

		if (multiPiece)
			return elName + Messages.LayersPage_9piecesSuffix;

		if (isSound)
			return baseName;

		if (emptyLayer)
			return elName;

		return elName + " " + baseName; 
	}

	public boolean isMultiPiece() {
		if (this.cdatas.get(0) != null) {

			ISkinnableEntityAdapter imageAdapter = (ISkinnableEntityAdapter) cdatas
					.get(0).getAdapter(ISkinnableEntityAdapter.class);
			if (imageAdapter != null) {
				  return imageAdapter.isMultiPiece();
				//return imageAdapter.isNinePiece() || imageAdapter.isElevenPiece() || imageAdapter.isThreePiece();
			}

		} else if (currentParts.get(0) != null) {

			IScreenElement screenElement = JEMUtil
					.getScreenElement(currentParts.get(0));
			if (screenElement == null) {
				return false;
			}
			ISkinnableEntityAdapter imageAdapter = (ISkinnableEntityAdapter) screenElement
					.getData().getAdapter(ISkinnableEntityAdapter.class);
			if (imageAdapter != null) {
				return imageAdapter.isMultiPiece();
				//return imageAdapter.isNinePiece() || imageAdapter.isElevenPiece() || imageAdapter.isThreePiece();
			}
		}
		return false;
	}

	// needed only for ActiveLayersPage
	public void addLayerSelectionListener(LayerSelectionListener listener) {
	}

	public void removeLayerSelectionListener(LayerSelectionListener listener) {
	}

	//Single piece Refresh
	public void refresh(){
		refresh(null);
	}
	
	public void refresh(SkinnableEntity skinnableEntity) {
		if (treeViewer == null || treeViewer.getControl() == null
				|| treeViewer.getControl().isDisposed()) {
			return;
		}
		for (IAction a : globalHandlers) {
			if (a instanceof UpdateAction) {
				UpdateAction u = (UpdateAction) a;
				u.update();
			}
		}
		// refresh
		Object selection = currentParts.isEmpty()
				|| currentParts.get(0) == null ? (!cdatas.isEmpty() ? cdatas
				: null) : currentParts;
		if (selection != null) {
			
			StructuredSelection sel = new StructuredSelection(selection);
			if ((currentParts.size() == 1) && (cdatas.size() == 1)) {
				currentParts = new ArrayList<EditPart>();
				cdatas = new ArrayList<IContentData>();
			}

			selectionChanged(owningEditor, sel, skinnableEntity);
		}
	}

	class ToggleColorModeAction extends Action {

		static final int MODE_ALL = 1;
		static final int MODE_COMMON = 2;

		ImageDescriptor MODE_COMMON_IMAGE = S60WorkspacePlugin
				.getImageDescriptor("icons/cm_common.png");

		ImageDescriptor MODE_ALL_IMAGE = S60WorkspacePlugin
				.getImageDescriptor("icons/cm_all.png");

		int mode;

		ToggleColorModeAction(int initialMode) {
			super(null, org.eclipse.jface.action.IAction.AS_PUSH_BUTTON);
			this.mode = initialMode;
			updateTexts();
			updateImages();
		}

		@Override
		public void run() {
			if (mode == MODE_COMMON) {
				mode = MODE_ALL;
			} else {
				mode = MODE_COMMON;
			}
			updateTexts();
			updateImages();
			refreshView();
		}

		public int getColorMode() {
			return mode;
		}

		private void refreshView() {
			LayersPage.this.refresh();
		}

		private void updateTexts() {
			if (mode == MODE_ALL) {
				setToolTipText(Messages.LayersPage_ToggleColorMode_Common);
			} else {
				setToolTipText(Messages.LayersPage_ToggleColorMode_All);
			}
		}

		private void updateImages() {
			if (mode == MODE_ALL) {
				setImageDescriptor(MODE_COMMON_IMAGE);
			} else {
				setImageDescriptor(MODE_ALL_IMAGE);
			}
		}
	}

	public void contentModified(IContentDelta delta) {
		if(delta != null){	
			List<String> elementIDs = delta.getAffectedElementIDs();			
			if (elementIDs.size() > 0) {				
				refresh();
			}
		}		
	}

	public void rootContentChanged(IContent content) {}
}