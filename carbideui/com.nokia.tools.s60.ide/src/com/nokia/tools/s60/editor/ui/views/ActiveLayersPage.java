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
package com.nokia.tools.s60.editor.ui.views;

import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.image.RenderedImageDescriptor;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorizeSVGFilter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.AbstractAction;
import com.nokia.tools.s60.editor.actions.AbstractEditAction;
import com.nokia.tools.s60.editor.actions.BrowseForFileAction;
import com.nokia.tools.s60.editor.actions.ColorizeSvgAction2;
import com.nokia.tools.s60.editor.actions.ConvertAndEditSVGInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.EditImageInBitmapEditorAction;
import com.nokia.tools.s60.editor.actions.EditImageInSVGEditorAction;
import com.nokia.tools.s60.editor.actions.EditInSystemEditorAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction;
import com.nokia.tools.s60.editor.actions.EditMaskAction2;
import com.nokia.tools.s60.editor.actions.PasteImageAction;
import com.nokia.tools.s60.editor.actions.SetStretchModeDDown;
import com.nokia.tools.s60.editor.actions.layers.AddBgLayerAction;
import com.nokia.tools.s60.editor.actions.layers.AddLayerAction;
import com.nokia.tools.s60.editor.actions.layers.BaseLayerAction;
import com.nokia.tools.s60.editor.actions.layers.ChangeOrderAction;
import com.nokia.tools.s60.editor.actions.layers.ClearLayerAction;
import com.nokia.tools.s60.editor.actions.layers.CustomizeAction;
import com.nokia.tools.s60.editor.actions.layers.DeleteSelectedAction;
import com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost;
import com.nokia.tools.s60.editor.actions.layers.RenameLayerAction;
import com.nokia.tools.s60.editor.dnd.S60BaseDropListener;
import com.nokia.tools.s60.editor.ui.views.LayersPage.Pair;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.theme.editing.BasicEntityImage;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

/**
 * Page for Animation Editor, contains additional actions and features.

 */
public class ActiveLayersPage extends Page implements PropertyChangeListener,
		ILayersPage, ISelectionChangedListener, ISelectionListener,
		ILayerActionsHost {

	/** adapter for copy/paste via shortcuts */
	public class CopyPasteKeyAdapter extends KeyAdapter {

		ISelectionProvider provider;

		public CopyPasteKeyAdapter(ISelectionProvider provider) {
			this.provider = provider;
		}

		public void keyPressed(KeyEvent e) {
			// CTRL+C
			if (e.keyCode == 99 && e.stateMask == 262144) {
				IAction action = new CopyImageAction(provider, null);
				if (action.isEnabled()) {
					action.run();
				}
			}
			// CTRL+V
			if (e.keyCode == 118 && e.stateMask == 262144) {
				IAction action = new PasteImageAction(provider,
						getCommandStack(), null);
				if (action.isEnabled()) {
					action.run();
				}
			}
		}
	}

	/* move DOWN action id */
	protected static int UP = -1;

	/* move UP action id */
	protected static int DOWN = 1;

	private BaseLayerAction moveDownAction, moveUpAction, deleteAction,
			addLayerAction;
	private Action layersViewAddEffectDropDown;

	private List<WorkbenchPartAction> actionsToDispose = new ArrayList<WorkbenchPartAction>();

	private IWorkbenchPart owner;

	public ActiveLayersPage(IWorkbenchPart owner) {
		this.owner = owner;
	}

	private List<LayerSelectionListener> listeners = new ArrayList<LayerSelectionListener>();

	private static boolean filterNonselectedEffects = true;

	private Composite treeTabComposite;

	private TreeViewer treeViewer;

	private TreeViewLabelProvider treeViewerLabelProvider;

	private IImage image;

	private ColorizingHelper colorizingHelper;

	private CompositeTooltip colorPickerTooltip;

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
			{
				return null;
			}
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof IAnimationFrame) {
				IAnimationFrame frame = (IAnimationFrame) parent;
				Object img = new Object[] { frame, frame.getRAWImage(false),
						frame.getImageFile(), AbstractAction.TYPE_IMAGE };
				Object mask = new Object[] { frame, frame.getMask(),
						frame.getMaskFile(), AbstractAction.TYPE_MASK };

				List<Object> result = new ArrayList<Object>();
				result.add(img);
				if (!frame.isSvg()) {
					result.add(mask);
				}

				createColorsAndGroupContent(result, frame);

				return result.toArray();
			}

			if (parent instanceof IImage) {

				
				List<ILayer> layers = ((IImage) parent).getLayers();
				Object list[] = new Object[layers.size()];
				for (int i = 0; i < layers.size(); i++) {
					list[i] = layers.get(i);
				}
				return list;
			} else if (parent instanceof ILayer) {

				List<Object> result = new ArrayList<Object>();

				// disable ApplyGraphics for background layer
				if (filterNonselectedEffects) {
					for (ILayerEffect e : ((ILayer) parent)
							.getSelectedLayerEffects()) {
						if (!IMediaConstants.APPLY_GRAPHICS.equals(e.getName()))
							result.add(e);
					}
				} else {
					for (ILayerEffect e : ((ILayer) parent).getLayerEffects()) {
						if (!IMediaConstants.APPLY_GRAPHICS.equals(e.getName()))
							result.add(e);
					}
				}

				return result.toArray();

			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			return getChildren(parent).length > 0;
		}

		public Map<RGB, Integer> getColors(IAnimationFrame frame) {
			if (!frame.isSvg()) {
				return Collections.EMPTY_MAP;
			}

			final List<RGB> colors = ColorizeSVGFilter.getColors2(frame
					.getImageFile());

			TreeMap<RGB, Integer> colorsWithoutDuplicatesMap = new TreeMap<RGB, Integer>(
					new Comparator<RGB>() {
						public int compare(RGB o1, RGB o2) {
							return colors.indexOf(o1) - colors.indexOf(o2);
						}
					});

			for (RGB rgb : colors) {
				if (!colorsWithoutDuplicatesMap.containsKey(rgb)) {
					colorsWithoutDuplicatesMap.put(rgb, 1);
				} else {
					Integer currentCount = colorsWithoutDuplicatesMap.get(rgb);
					colorsWithoutDuplicatesMap.put(rgb, currentCount + 1);
				}
			}

			return colorsWithoutDuplicatesMap;
		}

		private void createColorsAndGroupContent(List<Object> list,
				IAnimationFrame frame) {
			if (!ColorizeSvgAction2.IS_ENABLED) {
				return;
			}

			if (frame.isSvg()) {
				// colors
				Map<RGB, Integer> colors = getColors(frame);
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
								frame, frame, AbstractAction.TYPE_COLOR, rgb });

					}
				}

				try {
					List<ColorGroup> groups = ((BasicEntityImage) frame
							.getParent()).getLayer(0).getGroups();
					if (groups != null) {
						for (ColorGroup grp : groups) {
							if (colors.containsKey(grp.getGroupColor())) {

								int index = colorsListForGroups.get(grp
										.getGroupColor());
								// replace color with group if the item
								// is registered in group with the same
								// color
								list.set(index, new Object[] { grp.getName(),
										frame, frame,
										AbstractAction.TYPE_COLOR_GROUP,
										grp.getGroupColor() });
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	class TreeViewLabelProvider extends LabelProvider {

		Map<Object, Pair<ImageDescriptor, Image>> element2ImageCache = new HashMap<Object, Pair<ImageDescriptor, Image>>();

		public String getText(Object obj) {
			if (obj instanceof Object[]) {
				Object p[] = (Object[]) obj;
				if (AbstractAction.TYPE_IMAGE.equals(p[3])) {
					// frame img
					RenderedImage img = (RenderedImage) p[1];
					File imgFile = (File) p[2];

					/* construct additional info about images to node name */
					if (imgFile != null || img != null) {
						String imgType = null;
						try {
							String exte = FileUtils.getExtension(imgFile);
							if (exte != null)
								imgType = exte.toUpperCase();
							else
								imgType = Messages.LayersPage_bitmapImage; // cannot
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						String resolution = "";
						if (img != null)
							resolution = img.getWidth() + "x" + img.getHeight();
						return "Image: " + imgType + ", " + resolution;
					}
					return "Image: none";
				} else if (AbstractAction.TYPE_MASK.equals(p[3])) {
					// frame mask
					RenderedImage img = (RenderedImage) p[1];
					File imgFile = (File) p[2];
					/* construct additional info about images to node name */
					if (imgFile != null || img != null) {
						String resolution = "";
						if (img != null)
							resolution = img.getWidth() + "x" + img.getHeight();
						return "Mask: 8-bit, " + resolution;
					}
					return "Mask: none";
				} else if (AbstractAction.TYPE_COLOR.equals(p[3])
						|| AbstractAction.TYPE_COLOR_GROUP.equals(p[3])) {
					return p[0].toString();
				}
			}

			if (obj instanceof ILayer) {

				ILayer ll = (ILayer) obj;
				if (ll.isBackground())
					return ll.getName();
				/* construct additional info about images to node name */

				if (ll.getFileName(false) != null || ll.hasImage()) {
					String imgType = null;
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
					String resolution = "";
					if (ll.getRAWImage() != null)
						resolution = ll.getRAWImage().getWidth() + "x"
								+ ll.getRAWImage().getHeight();
					return ll.getName() + " (" + imgType + ", " + resolution
							+ ")";
				}
				// background or not known
				return ll.getName();

			} else if (obj instanceof ILayerEffect) {
				return ((ILayerEffect) obj).getName();
			}
			return "Unknown";
		}

		public Image getImage(Object obj) {
			synchronized (element2ImageCache) {
				if (obj instanceof IImage) {
					return null;
				} else if (obj instanceof ILayer) {
					Pair<ImageDescriptor, Image> pair = element2ImageCache
							.get(obj);
					if (pair == null) {
						ImageDescriptor desc = ((ILayer) obj)
								.getLayerIconImage();
						pair = new Pair<ImageDescriptor, Image>(desc,
								desc != null ? desc.createImage() : null);
						element2ImageCache.put(obj, pair);
						return pair.e2;
					} else {
						ImageDescriptor desc = ((ILayer) obj)
								.getLayerIconImage();
						if (desc != pair.e1) {
							// image changed
							// dispose old image and create new
							if (pair.e2 != null && !pair.e2.isDisposed()) {
								pair.e2.dispose();
							}
							pair.e1 = desc;
							pair.e2 = desc != null ? desc.createImage() : null;
						}
						return pair.e2;
					}
				} else if (obj instanceof Object[]) {
					Pair<ImageDescriptor, Image> pair = element2ImageCache
							.get(obj.toString());
					if (pair == null) {
						Object data[] = (Object[]) obj;
						ImageDescriptor desc = null;
						if (AbstractAction.TYPE_COLOR == data[3]
								|| AbstractAction.TYPE_COLOR_GROUP == data[3]) {
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
							desc = ((ImageDescriptor
									.createFromImageData(imageData)));
						} else if (data[1] instanceof RenderedImage) {
							RenderedImage image = (RenderedImage) data[1];
							if (image != null) {
								desc = new RenderedImageDescriptor(CoreImage
										.create(image).scale(18, 18).getAwt());
							}
						}

						if (desc != null) {
							pair = new Pair<ImageDescriptor, Image>(desc,
									desc != null ? desc.createImage() : null);
							element2ImageCache.put(obj.toString(), pair);
							return pair.e2;
						} else {
							return null;
						}
					} else {
						return pair.e2;
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

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createControl(Composite parent) {
		treeTabComposite = new Composite(parent, SWT.NONE);
		treeTabComposite.setLayout(new FillLayout());

		treeViewer = new TreeViewer(treeTabComposite, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);

		colorizingHelper = new ColorizingHelper(getEditor(),
				((GraphicsEditorPart) getWorkbenchPart()).getCommandStack(),
				treeViewer);

		addDragDropSupport();

		treeViewer.setContentProvider(new TreeViewContentProvider());
		treeViewerLabelProvider = new TreeViewLabelProvider();
		treeViewer.setLabelProvider(treeViewerLabelProvider);
		treeViewer.setInput(image);
		treeViewer.addSelectionChangedListener(this);

		// add double click support
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object eff = ((IStructuredSelection) treeViewer.getSelection())
						.getFirstElement();
				if (eff instanceof ILayerEffect) {

					// make sure that properties view is visible
					IWorkbenchPage page = getSite().getWorkbenchWindow()
							.getActivePage();
					if (page.findView("org.eclipse.ui.views.PropertySheet") == null) {
						try {
							page.showView("org.eclipse.ui.views.PropertySheet",
									null, IWorkbenchPage.VIEW_CREATE);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
					}

				} else if (eff instanceof ILayer) {
					ILayer layer = (ILayer) eff;
					if (layer.isBitmapImage()) {
						AbstractEditAction action = new EditImageInBitmapEditorAction(
								treeViewer, getCommandStack());
						action.run();
					} else if (layer.isSvgImage()) {
						AbstractEditAction action = new EditImageInSVGEditorAction(
								treeViewer, getCommandStack());
						action.run();
					}
				}
			}
		});

		

		// add ctrl+c, ctrl+v support
		treeViewer.getTree()
				.addKeyListener(new CopyPasteKeyAdapter(treeViewer));

		colorPickerTooltip = colorizingHelper.createTooltip();

		hookContextMenu();
		contributeToActionBars();

		getSite().setSelectionProvider(treeViewer);
	}

	private Series60EditorPart getEditor() {
		GraphicsEditorPart gep = (GraphicsEditorPart) getWorkbenchPart();
		return (Series60EditorPart) gep.getParentEditor();
	}

	protected CommandStack getCommandStack() {
		return (CommandStack) owner.getAdapter(CommandStack.class);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); 
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
	}

	private void contributeToActionBars() {
		IActionBars bars = getSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		bars.updateActionBars();
	}

	private void fillContextMenu(IMenuManager manager) {
		colorPickerTooltip.hide();
		// colorizingHelper.fillContextMenu(manager);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();

		SetStretchModeDDown setStretch = new SetStretchModeDDown(null,
				treeViewer, getCommandStack());

		// copy/paste group
		manager.add(new CopyImageAction(treeViewer, null));
		manager.add(new PasteImageAction(treeViewer, getCommandStack(), null));
		manager.add(new Separator());

		boolean _layer, _image, _part;
		_layer = _image = _part = false;
		if (selection.getFirstElement() instanceof Object[]) {
			Object array[] = (Object[]) selection.getFirstElement();
			if (AbstractAction.TYPE_IMAGE.equals(array[3])) {
				_image = true;
			} else if (AbstractAction.TYPE_PART.equals(array[3])) {
				_part = true;
			} else if (AbstractAction.TYPE_LAYER.equals(array[3])) {
				_layer = true;
			}
		}
		if (_layer || _image || _part) {
			BrowseForFileAction fileAction = new BrowseForFileAction(owner);
			fileAction.setSelectionProvider(treeViewer);
			manager.add(fileAction);
		}

		if (selection.getFirstElement() instanceof ILayer) {
			// add layer
			IAction action = new AddLayerAction(image, this, false);
			manager.add(action);

			// add bg
			action = new AddBgLayerAction(image, this, false);
			if (action.isEnabled())
				manager.add(action);

			// remove action
			action = new DeleteSelectedAction(this,
					Messages.Layers_RemoveLayerAction_title, false);
			manager.add(action);

			// rename action
			action = new RenameLayerAction(image, this, false);
			manager.add(action);

			manager.add(new Separator());
			manager.add(new AddEffectDropDown(this));

			// stretch mode submenu
			if (setStretch.isEnabled()) {
				manager.add(new Separator());
				manager.add(setStretch);
			}

			// layer
			manager.add(new Separator());
			action = new EditImageInBitmapEditorAction(treeViewer,
					getCommandStack());
			if (action.isEnabled()) {
				manager.add(action);
			}
			action = new ConvertAndEditSVGInBitmapEditorAction(treeViewer,
					getCommandStack());
			if (action.isEnabled()) {
				manager.add(action);
			}
			action = new EditImageInSVGEditorAction(treeViewer,
					getCommandStack());
			if (action.isEnabled()) {
				manager.add(action);
			}
			action = new EditInSystemEditorAction(treeViewer, getCommandStack());
			if (action.isEnabled()) {
				manager.add(action);
			}

			action = new ClearLayerAction(image, this, false);
			if (action.isEnabled()) {
				manager.add(new Separator());
				manager.add(action);
			}

		} else if (selection.getFirstElement() instanceof ILayerEffect) {
			// remove effect action
			Action action = new DeleteSelectedAction(this,
					Messages.Layers_RemoveEffectAction_title, false);
			manager.add(action);

			// show dialog action
			action = new CustomizeAction(image, this,
					Messages.Layers_AdjustEffect_title, false);
			if (action.isEnabled())
				manager.add(action);
		} else if (selection.getFirstElement() instanceof Object[]) {
			Object data[] = (Object[]) selection.getFirstElement();
			if ("IMAGE".equals(data[3])) {
				manager.add(new Separator());
				IAction action = new EditImageInBitmapEditorAction(treeViewer,
						getCommandStack());
				if (action.isEnabled()) {
					manager.add(action);
				}
				action = new ConvertAndEditSVGInBitmapEditorAction(treeViewer,
						getCommandStack());
				if (action.isEnabled()) {
					manager.add(action);
				}
				action = new EditImageInSVGEditorAction(treeViewer,
						getCommandStack());
				if (action.isEnabled()) {
					manager.add(action);
				}
				action = new EditInSystemEditorAction(treeViewer,
						getCommandStack());
				if (action.isEnabled()) {
					manager.add(action);
				}
			} else if ("MASK".equals(data[3])) {
				manager.add(new Separator());
				IAction action = new EditMaskAction(treeViewer,
						getCommandStack());
				if (action.isEnabled()) {
					manager.add(action);
				}
				// internal edit mask action
				action = new EditMaskAction2(treeViewer, getCommandStack());
				if (action.isEnabled()) {
					manager.add(action);
				}
			}
		}

	}

	private void fillLocalToolBar(IToolBarManager manager) {

		moveDownAction = new ChangeOrderAction(this, DOWN, false);
		moveDownAction.listenSelection();
		actionsToDispose.add(moveDownAction);

		moveUpAction = new ChangeOrderAction(this, UP, false);
		moveUpAction.listenSelection();
		actionsToDispose.add(moveUpAction);

		/* delete action */
		deleteAction = new DeleteSelectedAction(this, null, false);
		deleteAction.listenSelection();
		actionsToDispose.add(deleteAction);

		// effects dropdown combo
		layersViewAddEffectDropDown = new AddEffectDropDown(this);
		layersViewAddEffectDropDown
				.setEnabled(!(image instanceof IAnimatedImage));

		/* add layers action */
		addLayerAction = new AddLayerAction(image, this, true);
		addLayerAction.listenSelection();
		actionsToDispose.add(addLayerAction);

		/* add */
		manager.add(addLayerAction);
		manager.add(layersViewAddEffectDropDown);

		manager.add(new Separator());
		manager.add(moveUpAction);
		manager.add(moveDownAction);

		manager.add(new Separator());
		manager.add(deleteAction);

		/* hook to global action handlers */
		IActionBars bars = getSite().getActionBars();

		bars.setGlobalActionHandler(addLayerAction.getId(), addLayerAction);
		bars.setGlobalActionHandler(deleteAction.getId(), deleteAction);
		bars.setGlobalActionHandler(moveDownAction.getId(), moveDownAction);
		bars.setGlobalActionHandler(moveUpAction.getId(), moveUpAction);

		BaseLayerAction action = new AddBgLayerAction(image, this, true);
		actionsToDispose.add(action);
		action.listenSelection();
		bars.setGlobalActionHandler(action.getId(), action);

		action = new RenameLayerAction(image, this, true);
		actionsToDispose.add(action);
		action.listenSelection();
		bars.setGlobalActionHandler(action.getId(), action);

		action = new ClearLayerAction(image, this, true);
		actionsToDispose.add(action);
		action.listenSelection();
		bars.setGlobalActionHandler(action.getId(), action);

		CopyImageAction copy = new CopyImageAction(treeViewer, null);
		copy.listenSelection();
		actionsToDispose.add(copy);
		bars.setGlobalActionHandler(CopyImageAction.ID, copy);

		PasteImageAction paste = new PasteImageAction(treeViewer,
				getCommandStack(), null);
		paste.listenSelection();
		actionsToDispose.add(paste);
		bars.setGlobalActionHandler(PasteImageAction.ID, paste);

		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);

	}

	public void selectActiveLayer(ILayer layer) {
		if (layer != null) {
			treeViewer.setSelection(new StructuredSelection(layer));
		}
	}

	/**
	 * removes selected layer / effect from the model
	 */
	public void deleteSelected(Object sel) {
		Object selection = ((IStructuredSelection) treeViewer.getSelection())
				.getFirstElement();
		if (sel != null)
			selection = sel;
		if (selection instanceof ILayer) {
			ILayer layer = (ILayer) selection;
			
			if (layer.isEnabled()) {
				if (image.getSelectedLayerCount() <= 1)
					return; // cannot delete as this is last selected
				// layer
			}
			IImage parent = layer.getParent();
			int index = parent.getLayers().indexOf(layer);
			image.removeLayer(layer);
			if (index > -1) {
				treeViewer.setSelection(new StructuredSelection(parent
						.getLayer(index == 0 ? index : index - 1)));
			}
		} else if (selection instanceof ILayerEffect) {
			ILayer parent = ((ILayerEffect) selection).getParent();
			((ILayerEffect) selection).setSelected(false);
			List effects = parent.getSelectedLayerEffects();
			if (effects.size() > 0) {
				treeViewer.setSelection(new StructuredSelection(effects
						.get(effects.size() - 1)));
			}
		}
	}

	public boolean canDeleteSelected(Object sel) {
		if (image instanceof IAnimatedImage)
			return false;

		Object selection = ((IStructuredSelection) treeViewer.getSelection())
				.getFirstElement();
		if (sel != null)
			selection = sel;
		if (selection instanceof ILayer) {
			return image.getLayerCount() > 1;
		} else if (selection instanceof ILayerEffect) {
			return !((ILayerEffect) selection).getName().equals(
					IMediaConstants.APPLY_GRAPHICS);
		}
		return false;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	/**
	 * Called when the editor input is changed and the view must be refreshed.
	 * 
	 * @param part
	 *            the editor part.
	 */
	private void inputChanged(IAnimationFrame input) {

		
		image = input.getParent();

		if (treeViewerLabelProvider != null && treeViewer.getInput() != input) {
			treeViewerLabelProvider.flushImageCache();
		}
		treeViewer.setInput(input);
		treeViewer.expandAll();

		// disable actions from toolbar
		this.deleteAction.setEnabled(false);
		this.layersViewAddEffectDropDown.setEnabled(false);
		this.moveDownAction.setEnabled(false);
		this.moveUpAction.setEnabled(false);
		this.addLayerAction.setEnabled(false);
	}

	/**
	 * Called when the editor input is changed and the view must be refreshed.
	 * 
	 * @param part
	 *            the editor part.
	 */
	private void inputChanged(IImage input) {

		if (input instanceof IAnimatedImage) {
			input.removePropertyChangeListener(this);
			input.addPropertyListener(this);
			image = null;
			if (treeViewerLabelProvider != null)
				treeViewerLabelProvider.flushImageCache();
			treeViewer.setInput(null);
			// disable actions from toolbar
			this.deleteAction.setEnabled(false);
			this.layersViewAddEffectDropDown.setEnabled(false);
			this.moveDownAction.setEnabled(false);
			this.moveUpAction.setEnabled(false);
			this.addLayerAction.setEnabled(false);
			return;
		}

		ISelection _sel = treeViewer.getSelection();
		
		image = input;
		image.removePropertyChangeListener(this);
		image.addPropertyListener(this);

		if (treeViewerLabelProvider != null && treeViewer.getInput() != image) {
			treeViewerLabelProvider.flushImageCache();
		}
		treeViewer.setInput(image);
		treeViewer.expandAll();

		treeViewer.setSelection(_sel, true);
	}

	@Override
	public Control getControl() {
		return treeTabComposite;
	}

	public void propertyChange(final PropertyChangeEvent evt) {

		if (Display.getCurrent() == null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					propertyChange(evt);
				}
			});
		}

		if (treeViewer.getTree().isDisposed()) {
			return;
		}

		if (evt.getSource() instanceof IImage) {
			if (evt.getPropertyName() == IImage.PROPERTY_STRUCTURE_CHANGE
					|| evt.getPropertyName() == ILayer.PROPERTY_EFFECT_ORDER
					|| IImage.PROPERTY_IMAGE.equals(evt.getPropertyName())) {
				treeViewer.refresh();
				treeViewer.expandAll();

				/**
				 * **Object[] layers = image.getSelectedLayers().toArray();
				 * treeViewer.setCheckedElements(layers); // dimm check buttons
				 * in effects for (int i = 0; i < image.getLayerCount(); i++) {
				 * ILayer layer = image.getLayer(i); if
				 * (filterNonselectedEffects) { List elements =
				 * layer.getSelectedLayerEffects(); for (Object x : elements) {
				 * treeViewer.setGrayChecked(x, true); } } else { List elements =
				 * layer.getLayerEffects(); for (Object x : elements) {
				 * treeViewer.setGrayChecked(x, ((ILayerEffect) x)
				 * .isSelected()); } } }
				 */
				// force selectionChanged() occurs
				treeViewer.setSelection(treeViewer.getSelection());
			} else if (IImage.PROPERTY_STATE.equals(evt.getPropertyName())) {
				treeViewer.refresh();
			}
		}
	}

	/*
	 * refreshes view tree
	 */
	public void refresh() {
		inputChanged(image);
	}

	public void addLayerSelectionListener(LayerSelectionListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public void removeLayerSelectionListener(LayerSelectionListener listener) {
		listeners.remove(listener);
	}

	private void fireLayerSelection(ILayer selected) {
		for (int i = 0; i < listeners.size(); i++) {
			try {
				listeners.get(i).layerSelected(selected);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void fireEffectSelection(ILayerEffect selected) {
		for (int i = 0; i < listeners.size(); i++) {
			try {
				listeners.get(i).effectSelected(selected);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/* treeViewer selection changed */
	public void selectionChanged(SelectionChangedEvent event) {

		// update action toolbar
		IStructuredSelection selection = (IStructuredSelection) treeViewer
				.getSelection();
		if (selection.getFirstElement() instanceof ILayer) {
			layersViewAddEffectDropDown.setEnabled(true);
		} else {
			layersViewAddEffectDropDown.setEnabled(false);
		}

		if (selection.getFirstElement() instanceof ILayerEffect) {
			EffectControlsEditorPage page = getEffectControlsEditorPage();
			if (page != null) {
				page.inputChanged((ILayerEffect) ((IStructuredSelection) event
						.getSelection()).getFirstElement());
			}
		} else {
			EffectControlsEditorPage page = getEffectControlsEditorPage();
			if (page != null) {
				page.inputChanged(null);
			}
		}
		if (selection.isEmpty()) {
			fireEffectSelection(null);
			fireLayerSelection(null);
		} else {
			if (selection.getFirstElement() instanceof ILayer) {
				fireEffectSelection(null);
				fireLayerSelection((ILayer) selection.getFirstElement());
			} else if (selection.getFirstElement() instanceof ILayerEffect) {
				fireLayerSelection(null);
				fireEffectSelection((ILayerEffect) selection.getFirstElement());
			}
		}
	}

	private EffectControlsEditorPage getEffectControlsEditorPage() {
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page != null) {
			EffectControlsEditorView effectControlsView = null;

			effectControlsView = (EffectControlsEditorView) page
					.findView(EffectControlsEditorView.ID);

			if (effectControlsView != null) {
				// need to call activate to prepare new page
				effectControlsView.partActivated(this.getSite().getPage()
						.getActivePart());
				IPage currentPage = effectControlsView.getCurrentPage();
				if (currentPage instanceof EffectControlsEditorPage) {
					return (EffectControlsEditorPage) currentPage;
				}
			}
		}
		return null;
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) treeViewer.getSelection();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// update view with new input

		if (part != owner)
			return;

		Object o = ((IStructuredSelection) selection).getFirstElement();
		if (o instanceof IImage) {
			inputChanged((IImage) o);
		}
		if (o instanceof ILayer) {
			inputChanged(((ILayer) o).getParent());
		}
		if (o instanceof ILayerEffect) {
			inputChanged(((ILayerEffect) o).getParent().getParent());
		}
		if (o instanceof IAnimationFrame) {
			inputChanged((IAnimationFrame) o);
		}
	}

	private void addDragDropSupport() {

		final Transfer fileTransfer = FileTransfer.getInstance();
		final Transfer localSelection = LocalSelectionTransfer.getInstance();

		Transfer[] transfers = new Transfer[] { localSelection, fileTransfer };

		treeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_DEFAULT, transfers,
				new S60BaseDropListener(null, getCommandStack()) {

					public void dragOver(DropTargetEvent event) {

						Object dropData = unifyDropData(getDropData(event));
						Clipboard dropTest = null;
						if (dropData instanceof Clipboard)
							dropTest = (Clipboard) dropData;

						event.detail = DND.DROP_NONE;

						if (dropTest != null) {
							TreeItem item = (TreeItem) event.item;
							if (item == null) {
								return;
							}
							Object obj = item.getData();
							if (obj instanceof ILayer
									&& !((ILayer) obj).isBackground()) {
								if (ClipboardHelper
										.clipboardContainsData(
												ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK,
												dropTest))
									event.detail = DND.DROP_COPY;
							} else if (obj instanceof Object[]) {
								// frame image or mask
								Object[] data = (Object[]) obj;
								if (AbstractAction.TYPE_IMAGE.equals(data[3])) {
									if (ClipboardHelper
											.clipboardContainsData(
													ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK,
													dropTest))
										event.detail = DND.DROP_COPY;
								}
								if (AbstractAction.TYPE_MASK.equals(data[3])) {
									if (ClipboardHelper.clipboardContainsData(
											ClipboardHelper.CONTENT_TYPE_MASK,
											dropTest))
										event.detail = DND.DROP_COPY;
								}
							}
						}
					}

					public void drop(DropTargetEvent event) {
						TreeItem item = (TreeItem) event.item;
						Object itemData = item.getData();

						try {

							if (itemData instanceof ILayer) {
								ILayer layer = (ILayer) itemData;
								if (layer.isBackground())
									return;

								Object dropData = unifyDropData(getDropData(event));
								if (dropData instanceof Clipboard) {
									dropData = ClipboardHelper
											.getClipboardContent(
													ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE,
													(Clipboard) dropData);
									layer.paste(dropData);
									refresh();
								}

							} else if (itemData instanceof Object[]) {
								Object[] data = (Object[]) itemData;

								Object dropData = unifyDropData(getDropData(event));
								if (dropData instanceof Clipboard) {
									if (AbstractAction.TYPE_IMAGE
											.equals(data[3])) {
										dropData = ClipboardHelper
												.getClipboardContent(
														ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK,
														(Clipboard) dropData);
										((IAnimationFrame) data[0]).clearMask();
										((IImageHolder) data[0]).paste(
												dropData, null);
									}
									if (AbstractAction.TYPE_MASK
											.equals(data[3])) {
										dropData = ClipboardHelper
												.getClipboardContent(
														ClipboardHelper.CONTENT_TYPE_MASK,
														(Clipboard) dropData);
										((IImageHolder) data[0])
												.pasteMask(dropData);
									}
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
							S60WorkspacePlugin.error(e);
						}

					}
				});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		for (WorkbenchPartAction action : actionsToDispose) {
			action.dispose();
		}
		actionsToDispose.clear();
		addLayerAction = deleteAction = moveDownAction = moveUpAction = null;

		getSite().getActionBars().clearGlobalActionHandlers();
		if (null != treeViewer)
			treeViewer.removeSelectionChangedListener(this);

		colorPickerTooltip.dispose();
		colorPickerTooltip = null;

		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#getShell()
	 */
	public Shell getShell() {
		return treeViewer.getControl().getShell();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#getWorkbenchPart()
	 */
	public IWorkbenchPart getWorkbenchPart() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#getCurrentImage()
	 */
	public IImage getActiveImage() {
		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#addSelectionListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionListener(ISelectionChangedListener l) {
		treeViewer.addSelectionChangedListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost#removeSelectionListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionListener(ISelectionChangedListener l) {
		treeViewer.removeSelectionChangedListener(l);
	}

	private String _getElementName() {
		return image == null ? null : image.getName();
	}

	public String getPartName(String baseName) {

		boolean emptyLayer, ninePiece, isSound = false;
		String elName = _getElementName();
		if (elName == null)
			return baseName;

		emptyLayer = image.getLayerCount() == 0;
		ninePiece = image.isNinePiece();

		if (isSound)
			return baseName;

		if (emptyLayer)
			return elName;
		if (ninePiece)
			return elName + Messages.LayersPage_9piecesSuffix;

		if (treeViewer.getInput() instanceof IAnimationFrame) {
			int no = ((IAnimationFrame) treeViewer.getInput()).getSeqNo();
			return elName + " - Frame " + no;
		}

		return elName + " " + baseName;
	}

	public boolean isMultiPiece() {
		return image.isNinePiece();
	}

}