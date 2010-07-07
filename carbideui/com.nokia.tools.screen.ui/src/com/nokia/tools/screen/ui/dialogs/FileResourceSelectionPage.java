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

package com.nokia.tools.screen.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.preferences.PathHandlingPreferencePage;
import com.nokia.tools.ui.dialog.IResourceSelectionPage;
import com.nokia.tools.ui.dialog.TableImagePaintListener;

public class FileResourceSelectionPage
    extends IResourceSelectionPage.Adapter {

	private static final String ID = FileResourceSelectionPage.class.getName();

	private static final int MAX_RECENT_SAVE = 21;

	private IFileContentProvider contentProvider;

	private PathHandlingConfig data;

	private TableViewer fileTableViewer;

	private Combo txtFolder;

	private Button btnBrowse;

	private boolean shouldCreateImageLink;

	public FileResourceSelectionPage() {
		setIconImageDescriptor(UiPlugin.getImageDescriptor("icons/folder.gif"));
		setTitle(WizardMessages.ResourceSelectionDialog_File_Tab_Text);
		setId(ID);
		shouldCreateImageLink = true;
	}

	public FileResourceSelectionPage(boolean createImageLink) {
		this();
		shouldCreateImageLink = createImageLink;
	}

	/**
	 * @return the contentProvider
	 */
	public IFileContentProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * @param contentProvider the contentProvider to set
	 */
	public void setContentProvider(IFileContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#createPage(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPage(Composite parent) {
		data = PathHandlingConfig.load();

		Composite folderArea = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		folderArea.setLayoutData(gd);
		GridLayout gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.verticalSpacing = 7;
		gl.marginWidth = 0;
		folderArea.setLayout(gl);

		Label lblFolder = new Label(folderArea, SWT.NONE);
		lblFolder.setText(WizardMessages.ResourceSelectionDialog_ImageLabel);

		txtFolder = new Combo(folderArea, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 150;
		txtFolder.setLayoutData(gd);
		updatePaths();
		txtFolder.setVisibleItemCount(20);
		txtFolder.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fileTableViewer.setInput(new File(txtFolder.getText()));
				if (fileTableViewer.getElementAt(0) != null) {
					fileTableViewer.setSelection(new StructuredSelection(
					    fileTableViewer.getElementAt(0)), true);
					ISelection selection = fileTableViewer.getSelection();
					if (selection instanceof IStructuredSelection) {
						getManager().resourcesSelected(
						    ((IStructuredSelection) selection).toArray());
					}
				}
				getManager().refresh();
			}
		});

		txtFolder.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				if (fileTableViewer.getElementAt(0) != null)
					updateRecentList(txtFolder.getText());
			}

			public void widgetSelected(SelectionEvent e) {
				if (fileTableViewer.getElementAt(0) != null)
					updateRecentList(txtFolder.getText());
			}
		});

		btnBrowse = new Button(folderArea, SWT.PUSH);
		getManager().updateLayout(btnBrowse);
		btnBrowse.setText(WizardMessages.ResourceSelectionDialog_BrowseLabel);
		btnBrowse.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog fd = new DirectoryDialog(Display.getCurrent()
				    .getActiveShell(), SWT.OPEN);
				fd
				    .setText(WizardMessages.ResourceSelectionDialog_FolderDialog_Title);
				fd
				    .setMessage(WizardMessages.ResourceSelectionDialog_FolderDialog_Message);
				if (txtFolder.getText() != null
				    && txtFolder.getText().length() > 0
				    && new File(txtFolder.getText()).exists()) {
					fd.setFilterPath(txtFolder.getText());
				} else {
					IContainer rootElement = ResourcesPlugin.getWorkspace()
					    .getRoot();
					IPath location = rootElement.getLocation();
					String name = location.toOSString();
					fd.setFilterPath(name);
				}
				if (fd.open() != null) {
					int length = fd.getFilterPath().trim().length();
					if (length > 0) {
						txtFolder.setText(new Path(fd.getFilterPath())
						    .toOSString());
						if (fileTableViewer.getElementAt(0) != null) {
							fileTableViewer
							    .setSelection(new StructuredSelection(
							        fileTableViewer.getElementAt(0)));
							ISelection selection = fileTableViewer
							    .getSelection();
							if (selection instanceof IStructuredSelection) {
								if (selection instanceof IStructuredSelection) {
									getManager().resourcesSelected(
									    ((IStructuredSelection) selection)
									        .toArray());
								}
								updateRecentList(txtFolder.getText());
							}
						}
					}
				}
			}

		});

		fileTableViewer = new TableViewer(parent, SWT.FULL_SELECTION
		    | SWT.BORDER | SWT.V_SCROLL);
		fileTableViewer.getTable().addListener(
		    SWT.Paint,
		    new TableImagePaintListener(getManager(), fileTableViewer
		        .getTable()));

		final Table fileTable = fileTableViewer.getTable();
		fileTable.setLinesVisible(false);
		fileTable.setHeaderVisible(true);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 50;
		if (getManager().getPages().length > 1) {
			gd.widthHint = 313;
			gd.heightHint = 197;
		} else {
			gd.widthHint = 339;
			gd.heightHint = 246;
		}
		fileTable.setLayoutData(gd);

		final TableColumn column = new TableColumn(fileTable, SWT.LEFT, 0);
		column.setText(WizardMessages.ResourceSelectionDialog_FileLabel);

		MouseMoveListener listener = new MouseMoveListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseMove(MouseEvent e) {
				handleMouseMove(fileTableViewer.getTable(), e);
			}

		};
		parent.addMouseMoveListener(listener);
		fileTableViewer.getTable().addMouseMoveListener(listener);

		int w = fileTableViewer.getTable().getClientArea().width;
		column.setWidth(w);

		fileTableViewer.getTable().addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				int w = fileTableViewer.getTable().getClientArea().width;
				column.setWidth(w);
			}
		});
		fileTableViewer.setContentProvider(new FileContentProvider());
		fileTableViewer.setLabelProvider(new FileLabelProvider());
		fileTableViewer.setSorter(new ViewerSorter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#getComparator()
			 */
			@Override
			protected Comparator getComparator() {
				return new Comparator() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Comparator#compare(java.lang.Object,
					 *      java.lang.Object)
					 */
					public int compare(Object o1, Object o2) {
						String s1 = (String) o1;
						String s2 = (String) o2;
						return s1.compareToIgnoreCase(s2);
					}

				};
			}
		});
		fileTableViewer.addSelectionChangedListener(this);
		fileTableViewer.addOpenListener(this);

		createLinkToImages(parent);

		return parent;
	}

	private void createLinkToImages(Composite parent) {
	    if(!shouldCreateImageLink())
	    	return;
		
		Link lnkImageFolders = new Link(parent, SWT.NONE);
	    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = SWT.RIGHT;
		lnkImageFolders.setLayoutData(gd);
		lnkImageFolders
		    .setText(WizardMessages.ResourceSelectionDialog_Folders_Link_Text);
		lnkImageFolders.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				String linkAddress = PathHandlingPreferencePage.class.getName();
				PreferenceDialog prefdlg = PreferencesUtil
				    .createPreferenceDialogOn(Display.getCurrent()
				        .getActiveShell(), linkAddress,
				        new String[] { linkAddress }, null);
				if (prefdlg.open() == 0) {
					txtFolder.removeAll();
					data = PathHandlingConfig.load();
					updatePaths();
					txtFolder.setText(txtFolder.getItem(0));
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		lnkImageFolders
		    .setToolTipText(WizardMessages.ResourceSelectionDialog_Folders_Link_Tooltip);
    }

	/**
     * @return
     */
    private boolean shouldCreateImageLink() {
    	return shouldCreateImageLink;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#dispose()
	 */
	public void dispose() {
	}

	private void updateRecentList(String newPath) {
		if (data.recentPathList.indexOf(newPath) == 0 || data.usePredefined)
			return;
		if (newPath.endsWith(File.separator) && newPath.length() > 3)
			newPath = newPath.substring(0, newPath.length() - 1);
		int i = 0;
		for (String p : data.recentPathList.toArray(new String[0])) {
			if (p.equalsIgnoreCase(newPath))
				data.recentPathList.remove(i);
			i++;
		}
		data.recentPathList.add(0, newPath);
		if (data.recentPathList.size() > MAX_RECENT_SAVE)
			data.recentPathList.remove(MAX_RECENT_SAVE);
		data.saveRecentPathList();
		updatePaths();
		txtFolder.setText(txtFolder.getItem(0));
	}

	private void updatePaths() {
		if (data.usePredefined)
			txtFolder.setItems(data.predefinedPathList.toArray(new String[0]));
		else {
			String[] allItems = data.recentPathList.toArray(new String[0]);
			if (allItems.length > 0) {
				List<String> selectedItems = new ArrayList<String>();
				int l = allItems.length;
				if (data.recentCount < allItems.length)
					l = data.recentCount;
				for (int i = 0; i < l; i++)
					selectedItems.add(allItems[i]);
				txtFolder.setItems(selectedItems.toArray(new String[0]));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setFocus()
	 */
	public void setFocus() {
		txtFolder.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getSelectedResources()
	 */
	public Object[] getSelectedResources() {
		ISelection selection = fileTableViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toArray();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#createImage(java.lang.Object,
	 *      int, int, boolean)
	 */
	public Image createImage(Object data, int width, int height,
	    boolean keepAspectRatio) {
		if (data instanceof File) {
			return contentProvider.getImage((File) data, width, height,
			    keepAspectRatio);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setSelectedResources(java.lang.Object[])
	 */
	public Object[] init(Object[] resources) {
		List<Object> validResources = new ArrayList<Object>();

		if (txtFolder.getItemCount() > 0) {
			txtFolder.setText(txtFolder.getItem(0));
		}

		if (resources == null) {
			fileTableViewer.setInput(new File(txtFolder.getText()));
		} else {
			for (Object selectedItem : resources) {
				if (selectedItem instanceof File) {
					File file = (File) selectedItem;
					boolean isFile = false;
					if (file.isFile()) {
						file = file.getParentFile();
						isFile = true;
					}
					txtFolder.setText(file.getAbsolutePath());
					if (isFile) {
						fileTableViewer.setSelection(new StructuredSelection(
						    selectedItem), true);
					}
					validResources.add(selectedItem);
				}
			}
		}
		return validResources.toArray(new Object[validResources.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#checkError()
	 */
	public String checkError() {
		String strPath = txtFolder.getText().trim();

		if (strPath.length() == 0) {
			return WizardMessages.ResourceSelectionDialog_No_File_Error;
		}

		Path pluginPath = new Path(strPath);

		if (!FileUtils.isFileValidAndAccessible(pluginPath)) {
			return WizardMessages.ResourceSelectionDialog_Invalid_File_Path;
		}

		if (fileTableViewer.getElementAt(0) == null) {
			return WizardMessages.ResourceSelectionDialog_No_images_Error;
		}
		return null;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object[] resources = ((IStructuredSelection) selection).toArray();
			if (resources.length > 0) {
				handleThemeFileResource(resources[0]);
			}
		}
		super.selectionChanged(event);
	}

	/**
	 * @param fileResource
	 */
	protected void handleThemeFileResource(Object fileResource) {
		if (fileResource instanceof File) {
			File file = (File) fileResource;
			((IThemeResourcePageManager) getManager())
			    .setResult(new FileResourceResult<String>(false, file.getAbsolutePath()));
		}
	}


	class FileContentProvider
	    implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			File dir = (File) inputElement;
			return contentProvider.getFiles(dir);
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


	class FileLabelProvider
	    extends LabelProvider
	    implements ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			// images are generated only when the item is visible
			if (columnIndex == 0) {
				return getManager().getDefaultResourceImage();
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			return contentProvider.getName((File) element);
		}

	}
}
