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

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IContributedContentsView;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.commands.SetThemeGraphicsCommand;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * Base action class for Series60Editor actions, defines common functionality.
 */
public abstract class AbstractAction extends WorkbenchPartAction implements
		ISelectionChangedListener {

	/*
	 * constants for identifying type of selection contents (see LayersPage &
	 * ActiveLayersPage)
	 */
	public static final String TYPE_IMAGE = "IMAGE";

	public static final String TYPE_MASK = "MASK";

	public static final String TYPE_LAYER = "LAYER";

	public static final String TYPE_PART = "PART";

	public static final String TYPE_COLOR = "COLOR";

	public static final String TYPE_COLOR_GROUP = "COLOR_GROUP";

	private boolean isListeningSelection;

	protected boolean multipleSelection;

	protected enum MultipleSelectionEnablementEnum {
		ALL, ONE
	}

	protected MultipleSelectionEnablementEnum multipleSelectionEnablement = MultipleSelectionEnablementEnum.ALL;

	/** selection provider for selection , or null */
	private ISelectionProvider provider;

	protected CommandStack stack;

	/**
	 * original selection from provider. Can contain editPart, IContentData,
	 * etc.. in opposite, getSelection() returns pre-processed selection.
	 */
	protected IStructuredSelection selection;

	// check releasing cache when editor is closed..
	private static LayerCache layerCache = new LayerCache();

	public AbstractAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	public AbstractAction(IWorkbenchPart part, int style) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	/**
	 * returns explicitly set selection provider or one from workbench part
	 * 
	 * @return
	 */
	protected ISelectionProvider getSelectionProvider() {
		if (provider != null) {
			return provider;
		}
		try {
			if (getWorkbenchPart() != null)
				return getWorkbenchPart().getSite().getSelectionProvider();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * set explicit selection provider for action
	 * 
	 * @param provider
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		if (isListeningSelection && this.provider != null)
			this.provider.removeSelectionChangedListener(this);
		this.provider = provider;
		this.selection = null;
		if (isListeningSelection && this.provider != null)
			this.provider.addSelectionChangedListener(this);
	}

	/**
	 * gets first non-bg layer
	 * 
	 * @param selection
	 * @return
	 */
	protected ILayer getLayer(EditPart ep) {
		return getLayer(ep, false, false);
	}

	protected ILayer getLayer(EditPart ep, boolean loadImages) {
		return getLayer(ep, false, loadImages);
	}

	/**
	 * gets first non-bg layer
	 * 
	 * @param selection
	 * @return
	 */
	protected ILayer getLayer(EditPart ep, boolean withImage, boolean loadImages) {
		IContentData data = JEMUtil.getContentData(ep);
		if (data == null) {
			return null;
		}
		return getLayer(data, withImage, loadImages);
	}

	protected ILayer getLayer(IContentData data, boolean loadImages) {
		return getLayer(data, false, loadImages);
	}

	/**
	 * gets first non-bg layer
	 * 
	 * @param selection
	 * @return
	 */
	protected ILayer getLayer(IContentData data, boolean withImage,
			boolean loadImages) {
		ILayer cachedLayer = layerCache.getLayerFromCache(data, withImage,
				loadImages);
		if (cachedLayer != null) {
			return cachedLayer;
		}

		IImageAdapter imageAdapter = (IImageAdapter) data
				.getAdapter(IImageAdapter.class);
		if (imageAdapter == null) {
			return null;
		}
		Object element = imageAdapter.getImage(!loadImages);
		if (element instanceof IImage) {
			IImage image = (IImage) element;
			for (ILayer layer : image.getSelectedLayers()) {
				if (withImage) {
					if (layer.hasImage()
							&& !layer.isBackground()
							&& layer.getEffect(IMediaConstants.APPLY_GRAPHICS)
									.isSelected()) {
						element = layer;
						break;
					}
				} else {
					if (!layer.isBackground()) {
						element = layer;
						break;
					}
				}
			}
		}
		if (element instanceof ILayer) {
			layerCache.addLayerToCache(data, withImage, loadImages,
					(ILayer) element);
			return (ILayer) element;
		}
		layerCache.addLayerToCache(data, withImage, loadImages, null);
		return null;
	}

	protected Object getAdapter(EditPart selection, Class<?> adapterClass) {
		IContentData data = getContentData(selection);
		if (data == null) {
			return null;
		}
		return data.getAdapter(adapterClass);
	}

	abstract protected void doRun(Object element);

	/**
	 * default impl, returns true if selection contains content data or model
	 * edit part
	 * 
	 * @param element
	 * @return
	 */
	protected boolean doCalculateEnabled(Object element) {
		return element instanceof IContentData
				|| (element instanceof EditPart && ((EditPart) element)
						.getModel() instanceof EditObject);
	}

	protected boolean doCalculateEnabled(Object element,
			int elementPosInSelection) {
		return doCalculateEnabled(element);
	}

	@Override
	public void run() {

		selection = new StructuredSelection(
				((IStructuredSelection) getSelectionProvider().getSelection())
						.toArray());

		/* fix for selection for TreeNode's */
		selection = preprocessSelection(selection);

		final IStructuredSelection sel = (IStructuredSelection) getSelection();
		if (!multipleSelection && sel.size() == 1) {
			doRun(sel.getFirstElement());
		} else if (multipleSelection && sel.size() > 0) {
			Iterator it = sel.iterator();
			List<Object> elements = new ArrayList<Object>();
			while (it.hasNext())
				elements.add(it.next());
			if (this instanceof AbstractMultipleSelectionAction) {
				((AbstractMultipleSelectionAction) this).doRun(elements);
			} else {
				doRun(elements);
			}

		}
	}

	/**
	 * replaces items in selection, so that there are object user friendly for
	 * later processing, i.e. replaces TreeNode's with it's data object.
	 * 
	 * @param s
	 * @return
	 */
	private IStructuredSelection preprocessSelection(IStructuredSelection s) {
		if (s != null && s.size() > 0) {
			List<Object> tmp = new ArrayList<Object>();
			Iterator it = s.iterator();
			while (it.hasNext()) {
				Object obj = it.next();


				if (obj.getClass().getName().indexOf("TreeNode") > 0) {
					try {
						Object data = obj.getClass().getMethod("getContent",
								(Class[]) null).invoke(obj, (Object[]) null);
						if (data instanceof IContentData) {
						
							Set childrens = (Set) obj.getClass().getMethod(
									"getChildren", (Class[]) null).invoke(obj,
									(Object[]) null);
							if (childrens.size() == 1) {
								data = childrens.toArray()[0];
								data = data.getClass().getMethod("getContent",
										(Class[]) null).invoke(data,
										(Object[]) null);
								if (data instanceof IScreenElement)
									obj = ((IScreenElement) data).getData();
							}
						} else if (data instanceof IScreenElement) {
							obj = ((IScreenElement) data).getData();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				tmp.add(obj);
			}
			return new StructuredSelection(tmp);
		}
		return s;
	}

	@Override
	protected boolean calculateEnabled() {
		layerCache.enable();
		try {
			if (getSelectionProvider() == null) {
				return false;
			}
			if (!(getSelectionProvider().getSelection() instanceof IStructuredSelection))
				return false;

			// store original selection
			selection = (IStructuredSelection) getSelectionProvider()
					.getSelection();

			
			selection = preprocessSelection(selection);

			final IStructuredSelection sel = (IStructuredSelection) getSelection();
			if (sel.size() == 1) {
				return doCalculateEnabled(sel.getFirstElement(), 0);
			} else if (multipleSelection && sel.size() > 1) {
				Iterator it = sel.iterator();
				int index = 0;
				while (it.hasNext()) {
					Object next = it.next();
					if (next != null) {
						boolean enabled = doCalculateEnabled(next, index++);
						if (!enabled
								&& multipleSelectionEnablement == MultipleSelectionEnablementEnum.ALL) {
							return false;
						} else if (enabled
								&& multipleSelectionEnablement == MultipleSelectionEnablementEnum.ONE) {
							return true;
						}
					}
				}
				if (multipleSelectionEnablement == MultipleSelectionEnablementEnum.ONE) {
					return false;
				} else {
					return true;
				}
			}
			return false;
		} finally {
			layerCache.disable();
		}
	}

	/**
	 * returns selection, when selection is
	 * 
	 * <pre>
	 * Object[4]= {String, Layer, Editpart, TYPE} 
	 * </pre>
	 * 
	 * Returns layer as selection. This happens when selection source is layers
	 * view.
	 * 
	 * @return
	 */
	protected IStructuredSelection getSelection() {
		IStructuredSelection sel = this.selection;
		// when sel == null, get from provider
		if (sel == null)
			sel = (IStructuredSelection) getSelectionProvider().getSelection();
		if (sel != null) {
			if (sel.getFirstElement() instanceof Object[]) {
				// called from layers view, sel = [string, layer,
				// editPart/IContentData, TYPE];
				// extract layer
				Object layer = extractLayer((Object[]) sel.getFirstElement());
				if (layer != null)
					return new StructuredSelection(layer);

				Object[] data = (Object[]) sel.getFirstElement();
				if (data[0] instanceof IAnimationFrame) {
					return new StructuredSelection(data[0]);
				}
			}
		}

		return sel;
	}

	/**
	 * when selection comes from LayersView, contains more objects
	 * 
	 * @param array
	 * @return
	 */
	protected ILayer extractLayer(Object[] array) {
		
		if (array.length > 1) {
			if (array[1] instanceof ILayer)
				return (ILayer) array[1];
			if (array[1] instanceof IImage
					&& array[3] == AbstractAction.TYPE_PART) {
				return ((IImage) ((Object[]) array)[1]).getLayer(0);
			}
		}
		return null;
	}

	/**
	 * when selection comes from LayersView, contains more objects
	 * 
	 * @param array
	 * @return
	 */
	protected EditPart extractEditPart(Object[] array) {
		// array = [tree node label, layer, editPart/content data instance,
		// TYPE_STR];
		// Object array[] = (Object[]) selection.getFirstElement();
		if (array.length > 2) {
			if (array[2] instanceof EditPart)
				return (EditPart) array[2];
		}
		return null;
	}

	protected EditPart getEditPart(Object element) {
		if (element instanceof EditPart) {
			return (EditPart) element;
		} else if (element instanceof Object[]) {
			// extract edit part
			EditPart ep = extractEditPart((Object[]) element);
			if (ep != null)
				return ep;
		} else if (selection != null
				&& selection.getFirstElement() instanceof Object[]) {
			// extract edit part
			EditPart ep = extractEditPart((Object[]) selection
					.getFirstElement());
			if (ep != null)
				return ep;
		}
		return null;
	}

	protected IImageHolder getImageHolder(Object element) {
		if (element instanceof IImageHolder) {
			return (IImageHolder) element;
		}

		IScreenElement screenElement = getScreenElement(element);
		if (screenElement != null) {
			return (IImageHolder) screenElement.getAdapter(IImageHolder.class);
		}

		return null;
	}

	protected IContentData getContentData(Object element) {
		if (element instanceof IContentData) {
			return (IContentData) element;
		} else if (element instanceof Object[]) {
			Object array[] = (Object[]) element;
			if (array.length > 2) {
				if (array[2] instanceof IContentData)
					return (IContentData) array[2];
			}
		} else if (selection != null
				&& selection.getFirstElement() instanceof Object[]) {
			Object array[] = (Object[]) selection.getFirstElement();
			if (array.length > 2) {
				if (array[2] instanceof IContentData)
					return (IContentData) array[2];
			}
		}

		/* code for palette viewer */
		if (element instanceof ToolEntryEditPart) {
			Object model = ((ToolEntryEditPart) element).getModel();
			if (model instanceof CombinedTemplateCreationEntry) {
				Object template = ((CombinedTemplateCreationEntry) model)
						.getTemplate();
				if (template instanceof IContentData)
					return (IContentData) template;
			}
		}

		// try to get from EP
		EditPart part = getEditPart(element);
		IScreenElement el = JEMUtil.getScreenElement(part);
		return el != null ? el.getData() : null;
	}

	protected IImage getImage(boolean loadImages, Object element) {
		if (element instanceof ILayer) {
			return ((ILayer) element).getParent();
		} else if (getEditPart(element) != null) {
			EditPart part = getEditPart(element);
			return getImage(part, loadImages);
		} else if (getContentData(element) != null) {
			return getImage(getContentData(element), loadImages);
		} else if (element instanceof IImage) {
			return (IImage) element;
		}

		return null;
	}

	protected IScreenElement getScreenElement(Object element) {
		EditPart ep = getEditPart(element);
		if (ep != null)
			return JEMUtil.getScreenElement(ep);
		IContentData cd = getContentData(element);
		if (cd != null)
			return JEMUtil.getScreenElement(cd);
		return JEMUtil.getScreenElement(element);
	}

	protected IImage getImage(IContentData data, boolean loadImages) {
		IImageAdapter imageAdapter = (IImageAdapter) data
				.getAdapter(IImageAdapter.class);
		if (imageAdapter == null) {
			return null;
		}
		return imageAdapter.getImage(!loadImages);
	}

	protected IImage getImage(EditPart part, boolean loadImages) {
		final IScreenElement screenElement = JEMUtil.getScreenElement(part);
		if (screenElement == null) {
			return null;
		}
		return getImage(screenElement.getData(), loadImages);
	}

	protected ILayer getLayer(boolean loadImages, Object element) {
		return getLayer(loadImages, false, element);
	}

	/**
	 * try to returns layer from selection. If selection contains
	 * editpart/content data, EditableEntityImage is created;
	 * 
	 * @param loadImages
	 * @param selection
	 * @return
	 */
	protected ILayer getLayer(boolean loadImages, boolean withImage,
			Object element) {

		if (element instanceof Object[]) {
			// called from view, [string, layer, EditPart/IContentData,
			// TYPE_STR];
			// extract layer
			ILayer layer = extractLayer((Object[]) element);
			if (layer != null)
				return layer;
		}

		if (element instanceof ILayer) {
			return (ILayer) element;
		} else if (getEditPart(element) != null) {
			return getLayer(getEditPart(element), withImage, loadImages);
		} else if (element instanceof IContentData) {
			return getLayer((IContentData) element, withImage, loadImages);
		} else if (element instanceof IImage) {
			IImage image = (IImage) element;
			for (ILayer layer : image.getSelectedLayers()) {
				if (withImage) {
					if (layer.hasImage()
							&& !layer.isBackground()
							&& layer.getEffect(IMediaConstants.APPLY_GRAPHICS)
									.isSelected())
						return layer;
				} else {
					if (!layer.isBackground()) {
						return layer;
					}
				}
			}
			return null;
		}
		return null;
	}

	// used when called from layers view, action is performed on ILayer , but we
	
	protected void updateGraphicWithCommand(ILayer layer, Object element) {
		IContentData data = getContentData(element);

		if (data != null) {
			EditPart ep = getEditPart(element);

			SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(layer
					.getParent(), getContentData(element), ep, null);

			execute(cmd, ep);
		}
	}

	/**
	 * returns compound commands that contains SetThemeGraphic command and also
	 * updates affected screen elements
	 * 
	 * @param layer
	 * @param _sel
	 * @return
	 */
	protected Command getUpdateLayerCommand(ILayer layer, Object element) {
		IContentData data = getContentData(element);

		if (data != null) {
			EditPart ep = getEditPart(element);

			SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(layer
					.getParent(), data, ep, null);
			
			return cmd;
		}
		return null;
	}

	protected void execute(Command command, EditPart part) {
		if (stack == null) {
			if (part != null) {
				stack = part.getViewer().getEditDomain().getCommandStack();
			}

			if (stack == null) {
				IWorkbenchPart wp = getWorkbenchPart();
				if (wp == null) {
					wp = EclipseUtils.getActiveSafeEditor();
					if (wp != null) {
						stack = (CommandStack) wp
								.getAdapter(CommandStack.class);
					}
				} else {
					stack = (CommandStack) wp.getAdapter(CommandStack.class);
					if (stack == null) {
						IContributedContentsView view = (IContributedContentsView) wp
								.getAdapter(IContributedContentsView.class);
						if (view != null) {
							stack = (CommandStack) view.getContributingPart()
									.getAdapter(CommandStack.class);
						}
					}
				}
			}
		}
		if (stack != null) {
			stack.execute(command);
		} else {
			command.execute();
		}
	}

	protected IStructuredSelection getEditorSelection(Class desiredEditorClass) {
		IEditorPart part = EclipseUtils.getActiveSafeEditor();
		if (part != null && part.getClass() == desiredEditorClass) {
			IStructuredSelection sel = (IStructuredSelection) part.getSite()
					.getSelectionProvider().getSelection();
			return sel;
		}
		return null;
	}

	/**
	 * return EditPart selected in current editor.
	 * 
	 * @return
	 */
	protected AbstractGraphicalEditPart getGraphicalEditPart(Object element) {
		if (getEditPart(element) instanceof AbstractGraphicalEditPart)
			return (AbstractGraphicalEditPart) getEditPart(element);
		// selected from outline, try to get selection from active editor
		IEditorPart part = EclipseUtils.getActiveSafeEditor();
		if (part != null && part instanceof ScreenEditorPart) {
			IStructuredSelection sel = (IStructuredSelection) part.getSite()
					.getSelectionProvider().getSelection();
			Object s = sel.getFirstElement();
			// return only SkinGraphical part = valid elements
			if (s instanceof AbstractGraphicalEditPart) {
				Object model = ((AbstractGraphicalEditPart) s).getModel();
				if (model instanceof EObject) {
					IScreenElement adapter = JEMUtil
							.getScreenElement((EObject) model);
					if (adapter != null) {
						IContentData data = adapter.getData();
						if (data != null && ScreenUtil.isPrimaryContent(data)) {
							return (AbstractGraphicalEditPart) s;
						}
					}
				}
			}
		}
		return null;
	}

	protected boolean isNodeOfType(String type) {
		try {
			if (selection != null) {
				Object element = selection.getFirstElement();
				if (element instanceof Object[]) {
					Object[] arr = (Object[]) element;
					if (arr.length > 3) {
						return type.equals(arr[3]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	protected boolean isMaskNode() {
		return isNodeOfType(TYPE_MASK);
	}

	protected boolean isImageNode() {
		return isNodeOfType(TYPE_IMAGE);
	}

	protected boolean isLayerNode() {
		return isNodeOfType(TYPE_LAYER);
	}

	public void listenSelection() {
		try {
			isListeningSelection = true;
			if (getSelectionProvider() != null)
				getSelectionProvider().addSelectionChangedListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		if (getSelectionProvider() != null) {
			getSelectionProvider().removeSelectionChangedListener(this);
		}
		super.dispose();
	}

	public void selectionChanged(SelectionChangedEvent event) {
		try {
			setEnabled(calculateEnabled());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isListeningSelection() {
		return isListeningSelection;
	}

	protected IEditorPart getActiveEditorPart() {
		if (EclipseUtils.getActiveSafeEditor() != null)
			return EclipseUtils.getActiveSafeEditor();
		return null;
	}

	/**
	 * Get current selection's resource as
	 */
	protected IResource getResource() {
		ISelection selection = null;
		IResource resource = null;
		if (null != (selection = getActiveEditorPart().getEditorSite()
				.getSelectionProvider().getSelection())) {
			if (selection instanceof IAdaptable) {
				resource = (IResource) ((IAdaptable) selection)
						.getAdapter(IResource.class);
			}
		}
		if (null == resource
				&& getActiveEditorPart().getEditorInput() instanceof IFileEditorInput) {
			resource = ((IFileEditorInput) getActiveEditorPart()
					.getEditorInput()).getFile();
		}
		return resource;
	}

	static class LayerCache implements CommandStackListener, IPartListener {
		List<LayerCacheItem> cache = new ArrayList<LayerCacheItem>();

		boolean enabled = false;

		public void enable() {
			enabled = true;
		}

		public void disable() {
			enabled = false;
		}

		public void clear() {
			synchronized (cache) {
				cache.clear();
			}
		}

		public void clearCache(IContentData data) {
			synchronized (cache) {
				for (Iterator iter = cache.iterator(); iter.hasNext();) {
					LayerCacheItem item = (LayerCacheItem) iter.next();
					if (item != null && item.data != null && data != null
							&& item.data.getId().equals(data.getId())) {
						iter.remove();
					}
				}
			}
		}

		public void commandStackChanged(EventObject event) {
			clear();
		}

		public ILayer getLayerFromCache(IContentData data, boolean withImage,
				boolean loadImages) {
			if (!enabled) {
				return null;
			}

			synchronized (cache) {
				LayerCacheItem key = new LayerCacheItem(data, withImage,
						loadImages, null);
				int idx = cache.indexOf(key);
				if (idx >= 0) {
					return cache.get(idx).layer;
				} else {
					return null;
				}
			}
		}

		public void addLayerToCache(IContentData data, boolean withImage,
				boolean loadImages, ILayer layer) {
			if (!enabled) {
				return;
			}

			synchronized (cache) {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				if(null != page)
				{
					if (page.getActiveEditor() instanceof Series60EditorPart
							|| page.getActiveEditor() instanceof GraphicsEditorPart) {
						page.removePartListener(this);
						page.addPartListener(this);
						CommandStack stack = (CommandStack) page.getActiveEditor()
								.getAdapter(CommandStack.class);
						if (stack != null) {
							stack.removeCommandStackListener(this);
							stack.addCommandStackListener(this);

							LayerCacheItem key = new LayerCacheItem(data,
									withImage, loadImages, layer);
							cache.remove(key);
							cache.add(key);
							return;
						}
					}
				}
			}
		}

		public void partActivated(IWorkbenchPart part) {
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
			clear();
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}
	}

	static class LayerCacheItem {
		IContentData data;

		boolean withImage;

		boolean loadImages;

		ILayer layer;

		public LayerCacheItem(IContentData _data, boolean _withImage,
				boolean _loadImages, ILayer _layer) {
			this.data = _data;
			this.withImage = _withImage;
			this.loadImages = _loadImages;
			this.layer = _layer;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LayerCacheItem) {
				LayerCacheItem item = (LayerCacheItem) obj;
				return data == item.data && withImage == item.withImage
						&& loadImages == item.loadImages;
			} else {
				return super.equals(obj);
			}
		}

		@Override
		public int hashCode() {
			return data.hashCode() + ((Boolean) withImage).hashCode()
					+ ((Boolean) loadImages).hashCode();
		}
	}

	protected IPasteTargetAdapter getPasteTargetAdapter(Object obj) {

		if (obj == null) {
			return null;
		}

		Class toAdopt = IPasteTargetAdapter.class;

		if (isMaskNode()) {
			toAdopt = IPasteTargetAdapter.IPasteMaskAdapter.class;
		}

		if (toAdopt.isInstance(obj)) {
			return (IPasteTargetAdapter) obj;
		}

		if (obj instanceof IAdaptable) {
			IPasteTargetAdapter adapter = (IPasteTargetAdapter) ((IAdaptable) obj)
					.getAdapter(toAdopt);
			if (adapter != null) {
				return adapter;
			}
		}

		IImageHolder holder = getImageHolder(obj);
		IPasteTargetAdapter adapter = getPasteTargetAdapter(holder);
		if (adapter != null) {
			return adapter;
		}

		IContentData contentData = getContentData(obj);
		if (null != contentData) {
			adapter = (IPasteTargetAdapter) contentData
					.getAdapter(IPasteTargetAdapter.class);
			if (null == adapter) {
				adapter = (IPasteTargetAdapter) contentData
						.getAdapter(ISkinnableEntityAdapter.class);
			}
		}
		if (adapter != null) {
			return adapter;
		}

		IImage image = getImage(false, obj);
		adapter = getPasteTargetAdapter(image);
		if (adapter != null) {
			return adapter;
		}

		ILayer layer = getLayer(false, false, obj);
		adapter = getPasteTargetAdapter(layer);
		if (adapter != null) {
			return adapter;
		}

		return adapter;
	}

	/**
	 * method for testing if selected object is node from layers view: PART,
	 * IMAGE, MASK, LAYER
	 * 
	 * @param selection
	 * @param type
	 * @return
	 */
	public static boolean isNodeOfType(Object selection, String type) {
		try {
			if (selection instanceof Object[]) {
				Object[] arr = (Object[]) selection;
				if (arr.length > 3) {
					return type.equals(arr[3]);
				}
			}
		} catch (Exception e) {
		}
		return false;
	}
}