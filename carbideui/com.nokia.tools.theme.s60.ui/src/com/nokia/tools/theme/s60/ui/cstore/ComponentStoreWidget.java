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
package com.nokia.tools.theme.s60.ui.cstore;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.theme.s60.cstore.ComponentStore;
import com.nokia.tools.theme.s60.cstore.ComponentPoolBackend.NamedResult;
import com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement;
import com.nokia.tools.theme.s60.ui.cstore.StoredElementViewer.ActionProvider2;
import com.nokia.tools.theme.s60.ui.cstore.StoredElementViewer.ModifyOperation;

/**
 * Component Store UI realization
 * 
 * 
 */
public class ComponentStoreWidget extends Composite implements
		DropTargetListener, PropertyChangeListener, DisposeListener {

	public enum DisplayMode {
		IdMatching, All
	}

	/**
	 * object used as indication that drag operation was inited from component
	 * store
	 */
	public static final String DRAG_FROM_POOL = "DragFromPool"; //$NON-NLS-1$

	/** indicating that element comes from element pool = similar components */
	static final String POOL_TYPE = "POOL_TYPE"; //$NON-NLS-1$

	protected List<ISelectionChangedListener> listeners = new ArrayList<ISelectionChangedListener>();

	/* widget for displaying list of IContentData */
	private StoredElementViewer contentViewer;

	private IContentData sourceContentData;

	private IWorkbenchPart sourcePart;

	private DisplayMode dispMode = DisplayMode.IdMatching;

	private List<String> filterTags = new ArrayList<String>();

	private ActionProvider2 clientActionProvider;

	/**
	 * Store = user defined elements
	 * 
	 * @return
	 */
	public IComponentStore getStore() {
		return ComponentStoreFactory.getComponentStore();
	}

	/**
	 * pool = example themes source
	 * 
	 * @return
	 */
	public IComponentPool getPool() {
		return ComponentStoreFactory.getComponentPool();
	}

	public ComponentStoreWidget(Composite parent, int style) {
		super(parent, style);

		ActionProvider2 actionProvider = new ActionProvider2() {

			public void fillElementContextMenu(IMenuManager manager,
					final Object _element, final List<Object> selection) {
				final StoredElement element = (StoredElement) _element;

				// Action applySkin = new Action(Messages.cstore_apply) {
				// @Override
				// public void run() {
				// if (clientActionProvider != null)
				// clientActionProvider.processDoubleClick(
				// element.link, selection);
				// }
				// };
				// applySkin.setImageDescriptor(PlatformUI.getWorkbench()
				// .getSharedImages().getImageDescriptor(
				// ISharedImages.IMG_TOOL_REDO));
				// if (selection != null && selection.size() < 2)
				// manager.add(applySkin);

				if (clientActionProvider != null) {
					try {
						clientActionProvider.fillElementContextMenu(manager,
								element.link, selection);
					} catch (Exception e) {
					}
				}
				if (!POOL_TYPE.equals(element.metadata)) {

					boolean disable = false;
					if (selection.contains(element)) {
						for (Object a : selection)
							if (a instanceof StoredElement)
								if (POOL_TYPE
										.equals(((StoredElement) a).metadata)) {
									disable = true;
									break;
								}
					}

					Separator sep = new Separator();
					manager.add(sep);
					// custom element, can be deleted etc..
					Action delete = new Action(Messages.cstore_delete) {
						@Override
						public void run() {
							if (selection.contains(element)) {
								List<StoredElement> l = new ArrayList<StoredElement>();
								for (Object a : selection)
									if (a instanceof StoredElement)
										l.add((StoredElement) a);
								ComponentStore.SINGLETON.deleteElements(l);
							} else {
								ComponentStore.SINGLETON.deleteElement(element);
							}
						}
					};
					delete.setEnabled(!disable);
					delete.setImageDescriptor(PlatformUI.getWorkbench()
							.getSharedImages().getImageDescriptor(
									ISharedImages.IMG_TOOL_DELETE));
					manager.add(delete);

					// add other actions
					ComponentStoreWidget.this.fillElementContextMenu(manager,
							element);
				}
			}

			public void processDoubleClick(Object element,
					List<Object> selection) {
				StoredElement se = (StoredElement) element;
				if (clientActionProvider != null)
					clientActionProvider.processDoubleClick(se.link, selection);
			}

		};

		setLayout(new GridLayout());
		new Label(this, SWT.None).setText(Messages.cstore_default);
		contentViewer = new StoredElementViewer(this, SWT.None, actionProvider);

		Transfer[] transfers = new Transfer[] {
				LocalSelectionTransfer.getInstance(),
				FileTransfer.getInstance() };
		DropTarget target = new DropTarget(contentViewer.getContentComposite(),
				DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(transfers);
		target.addDropListener(this);

		getStore().addPropertyChangeListener(this);

		/* hook for saving on close */
		addDisposeListener(this);
	}

	public void setDisplayMode(DisplayMode mode) {
		if (dispMode != mode) {
			dispMode = mode;
			refreshContents();
		}
	}

	public void setFilterTags(List<String> tags) {
		this.filterTags.clear();
		if (tags != null)
			this.filterTags.addAll(tags);

		refreshContents();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {

		if (selection instanceof IStructuredSelection
				&& dispMode == DisplayMode.IdMatching) {
			IStructuredSelection ss = (IStructuredSelection) selection;

			Object element = ss.getFirstElement();
			IContentData icd = JEMUtil.getContentData(element);

			if (icd != null) {

				ISkinnableEntityAdapter skA = (ISkinnableEntityAdapter) icd
						.getAdapter(ISkinnableEntityAdapter.class);

				/*
				 * filter non-list elements but allow nine-piece, those are list
				 * with childs..
				 */
				if (icd.getChildren() != null
						&& icd.getChildren().length > 0
						&& (skA == null || (skA != null && !skA
								.supportsMultiPiece()))) {
								//.supportsNinePiece()))) {
					// clears the content viewer
					sourceContentData = null;
					sourcePart = null;
					refreshContents();
					return;
				}

				IToolBoxAdapter mfa = (IToolBoxAdapter) icd
						.getAdapter(IToolBoxAdapter.class);
				if (skA == null || mfa == null || skA.isColour()
						|| mfa.isFile() || mfa.isText()) {
					// clears the content viewer
					sourceContentData = null;
					sourcePart = null;
					refreshContents();
					return;
				}

				String prevId = sourceContentData == null ? null
						: sourceContentData.getId();
				if (icd.getId() != null && !(icd.getId().equals(prevId) && icd.toString().equals(sourceContentData.toString()))) {
					sourceContentData = icd;
					sourcePart = part;
					refreshContents();
				}
			} else {
                sourceContentData = null;
                sourcePart = null;
                refreshContents();
            }
		}
	}

	public void refreshContents() {

		// must be in UI thread
		if (Display.getCurrent() == null && false) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					refreshContents();
				}
			});
			return;
		}

		try {
			if (getChildren()[0] instanceof Label) {
				getChildren()[0].dispose();
				setLayout(new FillLayout());
				layout(true);
				getParent().layout(true);
				getParent().pack(true);
			}
		} catch (Exception e) {
		}

		contentViewer.clearContent();

		if (dispMode == DisplayMode.IdMatching && sourceContentData != null) {

			String elementId = sourceContentData.getId();
			List<StoredElement> cList = new ArrayList<StoredElement>();

			cList.addAll(getStore().getMatches(elementId));

			try {
				NamedResult[] similar = getPool().getElementListFromPool(
						elementId, null);
				for (NamedResult nr : similar) {
					if (nr == null)
						continue;
					IContentData data = (IContentData) nr.data;
					StoredElement sElement = new StoredElement(null,
							nr.themeName, 0, null, data);
					// mark as Pool type, comes from Example Themes, not from
					// Custom Component Store
					sElement.metadata = POOL_TYPE;
					cList.add(sElement);
				}
			} catch (ThemeException e) {
				e.printStackTrace();
			}

			// determine display size
			IContentData cData = sourceContentData;
			IImageAdapter imgAd = (IImageAdapter) cData
					.getAdapter(IImageAdapter.class);

			IImage selectedElement = imgAd.getImage();

			if (selectedElement == null)
				return;

			int width = selectedElement.getWidth();
			int height = selectedElement.getHeight();

			int SIZE_LIMIT = 75;
			float scale = 1.0f;
			if (width > SIZE_LIMIT || height > SIZE_LIMIT) {
				float scaleX = SIZE_LIMIT / (float) width;
				float scaleY = SIZE_LIMIT / (float) height;
				scale = scaleX < scaleY ? scaleX : scaleY;
				width = Math.max((int) (width * scale), 25);
				height = Math.max((int) (height * scale), 25);
			} else {
				int SIZE_MIN_LIMIT = 40;
				scale = 1.0f;
				if (width < SIZE_MIN_LIMIT || height < SIZE_MIN_LIMIT) {
					float scaleX = SIZE_MIN_LIMIT / (float) width;
					float scaleY = SIZE_MIN_LIMIT / (float) height;
					scale = scaleX < scaleY ? scaleX : scaleY;
					width = (int) (width * scale);
					height = (int) (height * scale);
				}
			}

			contentViewer.setContent(cList, width, height);
		}

		if (dispMode == DisplayMode.All) {

			List<StoredElement> cList = new ArrayList<StoredElement>();

			if (filterTags.size() > 0) {
				cList.addAll(getStore().getFilteredContent(filterTags));
			} else {
				cList.addAll(getStore().getContents());
			}

			if (cList.size() > 100)
				for (int i = cList.size() - 1; i >= 100; i--)
					cList.remove(i);

			contentViewer.setContent(cList);
		}

	}

	/*
	 * menu for each stored component
	 */
	protected void fillElementContextMenu(IMenuManager mgr,
			final StoredElement __ei) {

		List<String> allTags = ComponentStore.SINGLETON.getAvailableTags();
		List<String> elTags = __ei.tags;
		if (elTags == null)
			elTags = new ArrayList<String>();

		MenuManager submenu = new MenuManager(Messages.cstore_filterTagsMenu);
		for (final String t : allTags) {
			Action tagAction = new TagFilterAction(t, __ei);
			tagAction.setChecked(elTags.contains(t));
			submenu.add(tagAction);
		}

		submenu.add(new Separator());
		Action newTag = new Action(Messages.cstore_newTagAction) {
			@Override
			public void run() {
				InputDialog input = new InputDialog(getShell(),
						Messages.cstore_newTagDlgLbl,
						Messages.cstore_newTagDlgMsg, "", null); //$NON-NLS-3$ //$NON-NLS-1$
				if (input.open() == Window.OK) {
					if (__ei.tags == null)
						__ei.tags = new ArrayList<String>();
					if (!__ei.tags.contains(input.getValue())
							&& !StringUtils.isEmpty(input.getValue()))
						__ei.tags.add(input.getValue());
				}
			}
		};
		submenu.add(newTag);

		// #filter disabled not in 3rd iteration
		// mgr.add(submenu);

		if (dispMode == DisplayMode.All) {
			// action for selection of filter tags
			Action action = new Action(Messages.cstore_filterAction) {
				@Override
				public void run() {
					doShowAdjustFiltersDialog();
				}
			};
			mgr.add(action);
		}

	}

	protected void doShowAdjustFiltersDialog() {
		ListSelectionDialog dialog = new ListSelectionDialog(getShell(),
				ComponentStore.SINGLETON.getAvailableTags(),
				new IStructuredContentProvider() {
					public Object[] getElements(Object inputElement) {
						return ((List) inputElement).toArray();
					}

					public void dispose() {
					};

					public void inputChanged(Viewer viewer, Object oldInput,
							Object newInput) {
					}
				}, new LabelProvider(), Messages.cstore_filterDlgLblb);
		dialog.setInitialElementSelections(filterTags);
		dialog.setTitle(Messages.cstore_filterDlgMsg);
		if (dialog.open() == Window.OK) {
			filterTags.clear();
			
			Object results[] = dialog.getResult();
			if(results != null && results.length >0 ){
				for (Object result : results) {
					filterTags.add(result.toString());
				}
			}
			refreshContents();
		}
	}

	/* drop listener START */

	public void dragEnter(DropTargetEvent event) {
		dragOver(event);
	}

	public void dragLeave(DropTargetEvent event) {
		// turn off highlight
		contentViewer.getContentComposite().setBackground(getBackground());
	}

	public void dragOperationChanged(DropTargetEvent event) {
		dragOver(event);
	}

	public void dragOver(DropTargetEvent event) {

		boolean validDrag = false;
		if (!(event.data == DRAG_FROM_POOL || this.contentViewer.dragInProgress)
				&& LocalSelectionTransfer.getInstance().isSupportedType(
						event.currentDataType)) {
			event.data = LocalSelectionTransfer.getInstance().getSelection();
			List<IContentData> param = getContentData(event);
			validDrag = param != null && param.size() > 0;
		}
		if (!validDrag) {
			event.detail = DND.DROP_NONE;
		} else {
			event.detail = DND.DROP_COPY;
			contentViewer.getContentComposite().setBackground(
					ColorConstants.blue);
		}
	}

	private List<IContentData> getContentData(DropTargetEvent event) {
		Object data = event.data;

		// test if content is selection with ClipboardContentDescriptor's
		if (data instanceof IStructuredSelection) {
			List items = Arrays.asList(((IStructuredSelection) data).toArray());
			boolean ccd = true;
			for (Object item : items) {
				if (!(item instanceof ClipboardContentDescriptor))
					ccd = false;
			}
			if (ccd) {

				final List<IContentData> toAdd = new ArrayList<IContentData>();

				try {
					for (Object item : items) {
						ClipboardContentDescriptor cDesc = (ClipboardContentDescriptor) item;
						if (cDesc.getType() == ContentType.CONTENT_ELEMENT) {
							IContentData cData = JEMUtil.getContentData(cDesc
									.getContent());
							IToolBoxAdapter toolbox = (IToolBoxAdapter) cData
									.getAdapter(IToolBoxAdapter.class);
							if (toolbox != null) {
								if (toolbox.isFile() || toolbox.isText())
									continue;
								else {
									// lookup ThemeData, not ThemeScreenData
									IContentData resourceData = cData.getRoot()
											.findById(cData.getId());
									if (resourceData != null
											&& resourceData != cData)
										cData = resourceData;
									toAdd.add(cData);
								}
							}
						}
					}
					return toAdd;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	// private List<File> getFileList(DropTargetEvent event) {
	// Object data = event.data;
	//
	// if (data instanceof IStructuredSelection) {
	// data = ((IStructuredSelection) data).getFirstElement();
	// }
	//
	// if (data instanceof Clipboard) {
	// data = ClipboardHelper
	// .getSupportedClipboardContent((Clipboard) data);
	// }
	//		
	// final List<File> imgFiles = new ArrayList<File>();
	//
	// if (data instanceof List) {
	// for (Object item : (List) data) {
	// File file = getFile(item);
	// if (file != null)
	// imgFiles.add(file);
	// }
	// } else if (data instanceof Object[]) {
	// for (Object item : (Object[]) data) {
	// File file = getFile(item);
	// if (file != null)
	// imgFiles.add(file);
	// }
	// }
	//		
	// if (getFile(data) != null) {
	// imgFiles.add((File) data);
	// }
	//		
	// /* assemble files / elements to be added */
	//		
	// if (imgFiles.size() == 1 && imgFiles.get(0).isDirectory()) {
	//
	// File[] list = imgFiles.get(0).listFiles();
	// List<File> dirContent = new ArrayList<File>();
	// for (File a : list) {
	// if (a.isFile() && ImageUtil.isParameterUsableAsImage(a)) {
	// dirContent.add(a);
	// }
	// }
	// imgFiles.clear();
	// imgFiles.addAll(dirContent);
	// } else {
	// for (int i = imgFiles.size() - 1; i >= 0; i--) {
	// File f = imgFiles.get(i);
	// if (f.isDirectory() || !f.exists()
	// || !ImageUtil.isParameterUsableAsImage(f))
	// imgFiles.remove(i);
	// }
	// }
	//		
	// return imgFiles;
	// }

	public void drop(DropTargetEvent event) {

		// test if content is selection with ClipboardContentDescriptor's
		final List<IContentData> cDataList = getContentData(event);

		if (cDataList != null) {
			/* run as job */

			final String jobName = "Adding " + cDataList.size() + " element(s).."; //$NON-NLS-1$ //$NON-NLS-2$

			Job job = new Job(jobName) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask(jobName, cDataList.size());
					try {
						ComponentStore.SINGLETON
								.addElements(cDataList, monitor);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
			return;
		}
	}

	// private File getFile(Object data) {
	// if (data instanceof IFile) {
	// data = ((IFile) data).getLocation().toFile().getAbsoluteFile();
	// }
	// if (data instanceof String) {
	// data = new File((String) data);
	// }
	// if (data instanceof File) {
	// return (File) data;
	// }
	// return null;
	// }

	public void dropAccept(DropTargetEvent event) {

	}

	/* drop listener END */

	public void propertyChange(final PropertyChangeEvent evt) {

		if (ComponentStore.PROP_CONTENT_CHANGE.equals(evt.getPropertyName())) {
			refreshContents();
			return;
		}

		List<StoredElement> changed = new ArrayList<StoredElement>();
		Object data = evt.getNewValue();
		if (data instanceof StoredElement) {
			changed.add((StoredElement) data);
		} else if (data instanceof List) {
			for (Object item : ((List) data)) {
				if (item instanceof StoredElement) {
					changed.add((StoredElement) item);
				}
			}
		}

		if (ComponentStore.PROP_CONTENT_REMOVED.equals(evt.getPropertyName())) {
			contentViewer.notifyModified(ModifyOperation.Removed, changed);
		}

		if (ComponentStore.PROP_CONTENT_ADDED.equals(evt.getPropertyName())) {

			if (sourceContentData != null && dispMode == DisplayMode.IdMatching) {
				String activeId = sourceContentData.getId();
				for (int i = changed.size() - 1; i >= 0; i--) {
					if (!activeId.equals(changed.get(i).id))
						changed.remove(i);
				}
				contentViewer.addDeltaContent(changed);
			} else {
				refreshContents();
			}
		}
	}

	public void widgetDisposed(DisposeEvent e) {
		ComponentStore.SINGLETON.removePropertyChangeListener(this);
		new Thread(new Runnable() {
			public void run() {
				ComponentStore.SINGLETON.saveStore();
			}
		}).start();
	}

	public void clearContents() {
		sourceContentData = null;
		sourcePart = null;
		contentViewer.clearContent();
	}

	public IWorkbenchPart getSourcePart() {
		return sourcePart;
	}

	public IContentData getSourceData() {
		return sourceContentData;
	}

	// /* selection provider */
	//
	// protected void fireSelectionEvent(IContentData element) {
	// _selected = new StructuredSelection(element);
	// SelectionChangedEvent evt = new SelectionChangedEvent(this, _selected);
	// for (ISelectionChangedListener l: listeners) try {
	// l.selectionChanged(evt);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// public void addSelectionChangedListener(ISelectionChangedListener
	// listener) {
	// listeners.add(listener);
	// }
	// public ISelection getSelection() {
	// return _selected;
	// }
	// public void removeSelectionChangedListener(ISelectionChangedListener
	// listener) {
	// listeners.remove(listener);
	// }
	// public void setSelection(ISelection selection) {
	// }

	/* selection provider END */

	public DisplayMode getDisplayMode() {
		return dispMode;
	}

	public void setClientActionProvider(ActionProvider2 clientActionProvider) {
		this.clientActionProvider = clientActionProvider;
	}

}
