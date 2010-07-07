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
package com.nokia.tools.screen.ui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TreeItem;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorCustomizer;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * Super class of all wizard pages related to packaging process.
 */
public abstract class AbstractNewPackagePage extends WizardPage implements
		Listener {
	private static final int ICON_SIZE = 16;

	private PackagingContext context;
	private String helpContextId;

	/**
	 * Default constructor.
	 */
	public AbstractNewPackagePage() {
		// @Externalize
		super("NewPackageWizardPage");
		setTitle(WizardMessages.New_Package_Banner_Title);
	}

	/**
	 * @param helpContextId
	 *            the helpContextId to set
	 */
	public void setHelpContextId(String helpContextId) {
		this.helpContextId = helpContextId;
	}

	/**
	 * @return the helpContextId
	 */
	public String getHelpContextId() {
		return helpContextId;
	}

	/**
	 * @return Returns the context.
	 */
	public PackagingContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            The context to set.
	 */
	public void setContext(PackagingContext context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		if (getControl() != null) {
			updateModel();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		AbstractNewPackagePage page = (AbstractNewPackagePage) super
				.getNextPage();
		if (page != null) {
			page.updateDefaultStates();
		}
		return page;
	}

	/**
	 * Updates the packaging model.
	 */
	protected abstract void updateModel();

	protected abstract IPlatform[] getSelectedPlatforms();

	public abstract boolean performFinish();

	public void updateDefaultStates() {
	}

	protected void pageChanged() {
	}

	/**
	 * @return the current theme to be packaged.
	 */
	public IContent getPrimaryContent() {
		IContent[] contents = (IContent[]) context.getInput();
		for (IContent content : contents) {
			if (ScreenUtil.isPrimaryContent(content)) {
				return content;
			}
		}
		return null;
	}


	public IContent[] getSecondaryContents() {
		IContent[] contents = (IContent[]) context.getInput();
		List<IContent> list = new ArrayList<IContent>(contents.length);
		for (IContent content : contents) {
			if (!ScreenUtil.isPrimaryContent(content)) {
				list.add(content);
			}
		}
		return list.toArray(new IContent[list.size()]);
	}

	/**
	 * @return all active contents.
	 */
	public IContent[] getContents() {
		return (IContent[]) context.getInput();
	}

	protected CheckboxTreeViewer createTaskViewer(Composite composite) {
		final CheckboxTreeViewer taskViewer = new CheckboxTreeViewer(composite,
				SWT.CHECK | SWT.NONE);
		taskViewer.setContentProvider(new TaskContentProvider());
		taskViewer.setLabelProvider(new TaskLabelProvider());
		taskViewer.setSorter(new TaskViewerSorter());
		taskViewer.setInput(getContents());
		taskViewer.expandToLevel(2);
		taskViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof ContentGroup) {
					ContentGroup group = (ContentGroup) event.getElement();
					for (IContent content : group.getContents()) {
						taskViewer.setChecked(content, event.getChecked());
						// this method call internally sets PACKAGING's
						// attribute value for corresponding ancestors and
						// children
						content.setAttribute(ContentAttribute.PACKAGING.name(),
								new Boolean(event.getChecked()).toString());

					}
				} else {
					IContentData data = (IContentData) event.getElement();
					if (taskViewer.getGrayed(data)) {
						taskViewer.setChecked(data, false);
						return;
					}
					// this method call internally sets PACKAGING's attribute
					// value for corresponding ancestors and children
					data.setAttribute(ContentAttribute.PACKAGING.name(),
							new Boolean(event.getChecked()).toString());
					if (!event.getChecked()) {
						// unchecks the parent if necessary
						IContentData work = data.getParent();
						while (work != null) {
							boolean nonSelected = true;
							for (IContentData child : work.getChildren()) {
								if (new Boolean(
										(String) child
												.getAttribute(ContentAttribute.PACKAGING
														.name()))
										&& new Boolean(
												(String) child
														.getAttribute(ContentAttribute.MODIFIED
																.name()))) {
									nonSelected = false;
									break;
								}
							}
							if (nonSelected) {
								// this method call internally sets PACKAGING's
								// attribute value for corresponding ancestors
								// and children
								work.setAttribute(ContentAttribute.PACKAGING
										.name(), "false");
							} else {
								break;
							}

							work = work.getParent();
						}
					}
				}
				updateTree(taskViewer);
				pageChanged();
			}
		});
		taskViewer.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
			}

			/**
			 * Notifies that a node in the tree has been expanded.
			 * 
			 * @param event
			 *            event object describing details
			 */
			public void treeExpanded(TreeExpansionEvent event) {
				updateTree(taskViewer);
			}
		});
		return taskViewer;
	}

	protected void updateTree(CheckboxTreeViewer taskViewer) {
		for (TreeItem item : taskViewer.getTree().getItems()) {
			updateTreeItem(item);
		}
	}

	public static boolean supportsPlatform(IPackager packager,
			IPlatform[] platforms) {
		for (IPlatform platform : platforms) {
			if (!packager.supportPlatform(platform)) {
				return false;
			}
		}
		return true;
	}

	protected void updateTreeItem(TreeItem item) {
		if (item.getData() == null) {
			return;
		}
		if (item.getData() instanceof ContentGroup) {
			boolean isChecked = false;
			for (IContent content : ((ContentGroup) item.getData())
					.getContents()) {
				IPackager packager = (IPackager) content
						.getAdapter(IPackager.class);
				if (packager != null
						&& new Boolean(
								(String) content
										.getAttribute(ContentAttribute.PACKAGING
												.name()))) {
					isChecked = true;
					break;
				}
			}
			item.setChecked(isChecked);
		} else {
			IContentData data = (IContentData) item.getData();
			IPackager packager = (IPackager) data.getRoot().getAdapter(
					IPackager.class);

			boolean isInPackage = new Boolean((String) data
					.getAttribute(ContentAttribute.PACKAGING.name()));
			boolean isModified = new Boolean((String) data
					.getAttribute(ContentAttribute.MODIFIED.name()));
			boolean isPlatformSupported = packager != null
					&& supportsPlatform(packager, getSelectedPlatforms());

			item.setChecked(isInPackage && isModified && isPlatformSupported);
			item.setGrayed(!isPlatformSupported || !isModified);
		}
		for (TreeItem child : item.getItems()) {
			updateTreeItem(child);
		}
	}

	class ContentGroup {
		List<IContent> contents = new ArrayList<IContent>();
		String name;

		ContentGroup(String name) {
			this.name = name;
		}

		void add(IContent content) {
			contents.add(content);
		}

		void remove(IContent content) {
			contents.remove(content);
		}

		String getName() {
			return name;
		}

		IContent[] getContents() {
			return contents.toArray(new IContent[contents.size()]);
		}
	}

	class TaskContentProvider implements ITreeContentProvider {
		/**
		 * Name of color group.
		 */
		private static final String COLOURS_GROUP = "Colours";

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IContentData
					&& parentElement instanceof ContentSelectionProviderMarker) {
				return ((IContentData) parentElement).getChildren();
			}
			if (parentElement instanceof IContent[]
					&& (((IContent[]) parentElement).length == 1)) {
				IContent content1 = ((IContent[]) parentElement)[0];

				if (content1 instanceof ContentSelectionProviderMarker) {
					ContentSelectionProviderMarker selectableContent = (ContentSelectionProviderMarker) content1;
					return selectableContent.getSelectableChildren();
				}
			}
			if (parentElement instanceof IContent[]) {
				IContent[] contents = (IContent[]) parentElement;

				IEmbeddedEditorDescriptor desc = null;
				for (IContent content : contents) {
					desc = ExtensionManager
							.getEmbeddedEditorDescriptorByContentType(content
									.getType());
					if (desc != null) {
						break;
					}
				}
				if (desc == null) {
					return (IContent[]) parentElement;
				}
				IEmbeddedEditorCustomizer customizer = desc
						.getEmbeddedEditorCustomizer();

				List<Object> elements = new ArrayList<Object>();
				Map<String, ContentGroup> groups = new HashMap<String, ContentGroup>();
				ContentGroup misc = new ContentGroup(
						WizardMessages.New_Package_Group_Misc);
				if (customizer != null) {
					for (IContent content : contents) {
						if (content.getAdapter(IPackager.class) == null) {
							continue;
						}
						if (!ScreenUtil.isPrimaryContent(content)) {
							if (customizer.isDefaultForContent(content)) {
								String uid = (String) content
										.getAttribute(ContentAttribute.APP_UID
												.name());
								if (!StringUtils.isEmpty(uid)) {
									ContentGroup group = groups.get(uid);
									if (group == null) {
										group = new ContentGroup(customizer
												.getCategoryName(content));
										groups.put(uid, group);
									}
									group.add(content);
									continue;
								}
							}
							misc.add(content);
						} else {
							elements.add(content);
						}
					}
				}
				for (ContentGroup group : groups.values()) {
					elements.add(group);
				}
				if (!misc.contents.isEmpty()) {
					elements.add(misc);
				}
				return elements.toArray();
			}
			if (parentElement instanceof IContent) {
				List<IContentData> tasks = new ArrayList<IContentData>();
				for (IContentData child : ((IContent) parentElement)
						.getChildren()) {
					if (child.getAdapter(IScreenAdapter.class) == null
							&& new Boolean(
									(String) child
											.getAttribute(ContentAttribute.PACKAGING_TASK
													.name()))) {
						tasks.add(child);
					}
				}
				return tasks.toArray();
			}
			if (parentElement instanceof ContentGroup) {
				return ((ContentGroup) parentElement).getContents();
			}
			IContentData[] children = ((IContentData) parentElement)
					.getChildren();
			if (children.length == 0
					|| new Boolean((String) children[0]
							.getAttribute(ContentAttribute.PACKAGING_TASK
									.name()))) {
				return children;
			}
			return new Object[0];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof IContent) {
				return getContents();
			}
			if (!(element instanceof IContentData)) {
				return null;
			}
			return ((IContentData) element).getParent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof IContent[]) {
				return getContents().length > 0;
			}
			if (element instanceof IContent) {
				return getChildren(element).length > 0;
			}
			if (element instanceof ContentGroup) {
				return ((ContentGroup) element).getContents().length > 0;
			}
			IContentData[] children = ((IContentData) element).getChildren();
			if (children.length == 0) {
				return false;
			} 
			/*
			 * hajduivo: Is not allowed to package sub elements of "Colours" hence tree
			 * shall not provide children of mentioned element. 
			 */
			else if (element instanceof IContentData) {
				if (((IContentData) element).getName().equals(COLOURS_GROUP)) {
					return false;
				}
			}
			return new Boolean((String) children[0]
					.getAttribute(ContentAttribute.PACKAGING_TASK.name()));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class TaskLabelProvider extends LabelProvider {

		// images needed to be disposed
		protected List<Resource> imageToDispose = new ArrayList<Resource>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		@Override
		public Image getImage(Object elem) {
			ImageDescriptor desc;
			if (elem instanceof ContentGroup) {
				desc = UiPlugin.getImageDescriptor("icons/icons16/group.png");
			} else {
				IContentData element = (IContentData) elem;
				desc = element.getImageDescriptor(ICON_SIZE, ICON_SIZE);
			}
			if (desc != null) {
				Image image = desc.createImage();
				imageToDispose.add(image);
				return image;
			}
			// icons for default elements
			Image image = new Image(Display.getCurrent(), ICON_SIZE, ICON_SIZE);
			imageToDispose.add(image);
			return image;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		@Override
		public String getText(Object element) {
			if (element instanceof ContentGroup) {
				return ((ContentGroup) element).getName();
			}
			if (((IContentData) element).getParent() != null
					&& ((IContentData) element).getParent().getParent() == null
					&& ScreenUtil.isPrimaryContent((IContentData) element)) {
				return ((IContentData) element).getName();
			}
			return ((IContentData) element).getName();
		}

		@Override
		public void dispose() {
			super.dispose();
			for (Object img : imageToDispose) {
				try {
					((Image) img).dispose();
				} catch (Exception e) {
			
				}
			}
		}
	}

	class TaskViewerSorter extends ViewerSorter {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof IContent && e2 instanceof IContent) {
				if (ScreenUtil.isPrimaryContent((IContent) e1)) {
					return -1;
				}
				if (ScreenUtil.isPrimaryContent((IContent) e2)) {
					return 1;
				}
				return ((IContent) e1).getName().compareToIgnoreCase(
						((IContent) e2).getName());
			}
			// keeps the natural order of the elements.
			return 0;
		}
	}
	
	/**
	 * Will be called at the end of the completion of the Packaging Job which is done in a Workspace runnable.
	 * Any activities on the end of the packaging that needs in the page can be done in this method.
	 */
	public  void handlePackagingFinish()
	{
		// Do nothing by default
	}
	
	
	/**
	 * Will be called at the cancellation of the Packaging operation.
	 * Any activities on the cancellation of the packaging that needs in the page can be done in this method.
	 */
	
	public  void handlePackagingCancel()
	{
		// Do nothing by default
	}
}
