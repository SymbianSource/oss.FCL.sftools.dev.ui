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

package com.nokia.tools.ui.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Label;

import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.ui.Activator;

public class FileResourceSelectionPage extends IResourceSelectionPage.Adapter {

	private boolean allowMultipleSelection;
	
	private static final String ID = FileResourceSelectionPage.class.getName();

	private IFileContentProvider contentProvider;

	protected Combo txtFolder;

	private Button btnBrowse;

	private boolean shouldCreateImageLink;

	private Set<String> selectedItems = new HashSet<String>();

	private FileTableViewer fileViewer;

	public FileResourceSelectionPage() {
		setIconImageDescriptor(Activator.getImageDescriptor("icons/folder.gif"));
		setTitle(Messages.ResourceSelectionDialog_File_Tab_Text);
		setId(ID);
		shouldCreateImageLink = true;
	}

	public FileResourceSelectionPage(boolean createImageLink) {
		this();
		shouldCreateImageLink = createImageLink;
	}
	
	public FileResourceSelectionPage(boolean createImageLink, boolean allowMultiple) {
		this();
		this.allowMultipleSelection = allowMultiple;		
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
		if (fileViewer != null) {
			fileViewer.setContentProvider(contentProvider);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#createPage(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPage(Composite parent) {

		Composite folderArea = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		folderArea.setLayoutData(gd);
		GridLayout gl = new GridLayout(3, false);
		gl.marginHeight = 0;
		gl.verticalSpacing = 7;
		gl.marginWidth = 0;
		folderArea.setLayout(gl);

		Label lblFolder = new Label(folderArea, SWT.NONE);
		lblFolder.setText(Messages.ResourceSelectionDialog_ImageLabel);

		txtFolder = new Combo(folderArea, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 150;
		gd.minimumWidth = 50;
		txtFolder.setLayoutData(gd);
		updatePaths();
		txtFolder.setVisibleItemCount(20);
		txtFolder.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fileViewer.setInput(new File(txtFolder.getText()));
				if (fileViewer.getElementAt(0) != null) {
					fileViewer.setSelection(new StructuredSelection(fileViewer
					    .getElementAt(0)), true);
					ISelection selection = fileViewer.getSelection();
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
				if (fileViewer.getElementAt(0) != null)
					updateRecentList(txtFolder.getText());
			}

			public void widgetSelected(SelectionEvent e) {
				if (fileViewer.getElementAt(0) != null)
					updateRecentList(txtFolder.getText());
			}
		});

		btnBrowse = new Button(folderArea, SWT.PUSH);
		getManager().updateLayout(btnBrowse);
		btnBrowse.setText(Messages.ResourceSelectionDialog_BrowseLabel);
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
				fd.setText(Messages.ResourceSelectionDialog_FolderDialog_Title);
				fd
				    .setMessage(Messages.ResourceSelectionDialog_FolderDialog_Message);
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

						if (fileViewer.getElementAt(0) != null) {
							fileViewer.setSelection(new StructuredSelection(
							    fileViewer.getElementAt(0)));
							ISelection selection = fileViewer.getSelection();
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

		if(allowMultipleSelection){
			fileViewer = new FileTableViewer(parent, this, allowMultipleSelection);
		}else{
			fileViewer = new FileTableViewer(parent, this);
			
		}		
		fileViewer.setContentProvider(contentProvider);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 50;
		if (getManager().getPages().length > 1) {
			gd.widthHint = 313;
			gd.heightHint = 197;
		} else {
			gd.widthHint = 339;
			gd.heightHint = 246;
		}
		fileViewer.getTable().setLayoutData(gd);

		return parent;
	}

	/**
	 * @return
	 */
	protected boolean shouldCreateImageLink() {
		return shouldCreateImageLink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#dispose()
	 */
	public void dispose() {
	}

	protected void updateRecentList(String newPath) {
	
	}


	protected void updatePaths() {
		txtFolder.setItems(selectedItems.toArray(new String[0]));

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
		ISelection selection = fileViewer.getSelection();
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
			fileViewer.setInput(new File(txtFolder.getText()));
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
						fileViewer.setSelection(new StructuredSelection(
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
			return Messages.ResourceSelectionDialog_No_File_Error;
		}

		Path pluginPath = new Path(strPath);

		if (!FileUtils.isFileValidAndAccessible(pluginPath)) {
			return Messages.ResourceSelectionDialog_Invalid_File_Path;
		}

		if (fileViewer.getElementAt(0) == null) {
			return Messages.ResourceSelectionDialog_No_images_Error;
		}
		return null;
	}

	
}
