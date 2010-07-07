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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.media.image.RenderedImageDescriptor;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

/**
 * Not used in current UI - was cancelled.
 * @deprecated
 */
public class CStoreManageContentDialog extends BrandedTitleAreaDialog {
		
	private ListViewer list;
	
	private Button deleteButton;
	
	private Composite rightComposite;
	
	private Label imagePreview;
	
	private IComponentStore store;
	
	private Text searchText;
	private String searchString = "";
	
	private List<String> filterTags = new ArrayList<String>();

	public CStoreManageContentDialog(Shell shell, int type) {
		this(shell, type, ComponentStoreFactory.getComponentStore());
	}
	
	public CStoreManageContentDialog(Shell shell, int type, IComponentStore store) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.store = store;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#
	 *      createDialogArea(org.eclipse.swt.widgets.Composite) Here we fill the
	 *      center area of the dialog
	 */
	protected Control createDialogArea(Composite parent) {
		
		Composite area = (Composite) super.createDialogArea(parent);
		
		Composite root = new Composite(area, SWT.None);
		root.setLayout(new GridLayout(2, false));
		
		{ //filter bar 
			Composite searchRoot = new Composite(root, SWT.None);
			GridData gd = new GridData();
			gd.horizontalIndent = gd.verticalIndent = 0;
			GridLayout gridl = new GridLayout(3, false);
			searchRoot.setLayout(gridl);
			
			Label searchLabel = new Label(searchRoot, SWT.None);
			searchLabel.setText("Find:");
			
			searchText = new Text(searchRoot, SWT.BORDER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.widthHint = 150;
			searchText.setLayoutData(gd);
			searchText.addKeyListener(new KeyAdapter(){
				@Override
				public void keyReleased(KeyEvent e) {
					handleSearchTextKeyPress(e);
				}
				
			});
			
			//filter button
			Button filterButton = new Button(searchRoot, SWT.PUSH);
			filterButton.setText("Filters..");
			filterButton.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleFilterButtonSelected(e);
				}
			});
		
		}
		
		{ //right composite
			rightComposite = new Composite(root, SWT.None);		
			rightComposite.setLayout(new GridLayout());
			GridData gd = new GridData();
			gd.verticalSpan = 5;
			gd.verticalAlignment = SWT.TOP;
			gd.widthHint = 400;
			gd.heightHint = 400;
			rightComposite.setLayoutData(gd);
			
			org.eclipse.swt.widgets.Group preview = new org.eclipse.swt.widgets.Group(rightComposite, SWT.None);
			preview.setText("Preview");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.heightHint = 160;
			preview.setLayoutData(gd);
			preview.setLayout(new GridLayout());
			
			imagePreview = new Label(preview, SWT.BORDER);
			gd = new GridData();
			//gd.heightHint = 150;
			//gd.widthHint = 150;
			imagePreview.setLayoutData(gd);
			
			org.eclipse.swt.widgets.Group tagsGroup = new org.eclipse.swt.widgets.Group(rightComposite, SWT.None);
			tagsGroup.setText("Filter Tags");
			gd = new GridData(GridData.FILL_BOTH);
			tagsGroup.setLayoutData(gd);
			tagsGroup.setLayout(new FillLayout());
			
			Composite tagsRootComposite = new Composite(tagsGroup, SWT.None);			
		}
		
		list = new ListViewer(root, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = gd.minimumWidth = 250;
		gd.heightHint = gd.minimumHeight = 300;
		list.getList().setLayoutData(gd);
		
		list.setContentProvider(new IStructuredContentProvider(){

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
							
			}

			public Object[] getElements(Object inputElement) {
				
				if (StringUtils.isEmpty(searchString))
					return ((List)inputElement).toArray();
				else {
					List<StoredElement> result = new ArrayList<StoredElement>();
					for (Object item: (List)inputElement) {
						StoredElement elem = (StoredElement) item;
						if (elem.name.toLowerCase().startsWith(searchString))
							result.add(elem);
					}
					return result.toArray();
				}				
			}
		});
		
		list.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object e) {
				StoredElement element = (StoredElement) e;
				return element.name;
			}
		});
		
		
		
		deleteButton = new Button(root, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
			public void widgetSelected(SelectionEvent e) {
				performDeletePressed(e);
			}			
		});
		deleteButton.setEnabled(false);
		
		
		list.addSelectionChangedListener(new ISelectionChangedListener(){
			public void selectionChanged(SelectionChangedEvent event) {				
				updatePreview();
			}			
		});
		
		
		list.setInput(store.getContents());
		
		return area;
	}

	protected void handleFilterButtonSelected(SelectionEvent e) {
		AcquireFilterTagsDialog dlg = new AcquireFilterTagsDialog(getShell(), ComponentStoreFactory.getComponentStore(), false, filterTags, "Select Filters");		
		if (dlg.open() == Window.OK) {
			filterTags = dlg.getResultFilterTags();
			if (filterTags.size() > 0) {
				list.setInput(store.getFilteredContent(filterTags));
			} else
				list.setInput(store.getContents());
		}
	}

	protected void handleSearchTextKeyPress(KeyEvent e) {
		String oldStr = searchString;
		searchString = searchText.getText().toLowerCase();
		if (StringUtils.isEmpty(searchString))
			searchString = "";
		if (!searchString.equalsIgnoreCase(oldStr))
			list.refresh();
	}

	protected void updatePreview() {
		
		IStructuredSelection sel = (IStructuredSelection) list.getSelection();
		
		if (imagePreview.getImage() != null)
			imagePreview.getImage().dispose();
		imagePreview.setImage(null);
		
		if (sel.isEmpty()) {
			deleteButton.setEnabled(false);			
		} else {
			deleteButton.setEnabled(true);
			StoredElement selected = (StoredElement) sel.getFirstElement();
			ThemeData td = (ThemeData) selected.link;
			SkinnableEntity ske = (SkinnableEntity) td.getData();
			try {
				EditableEntityImage img = new EditableEntityImage(ske, null, null, 150, 150, 255);
				ImageDescriptor imgdesc = new RenderedImageDescriptor(img.getAggregateImage());				
				imagePreview.setImage(imgdesc.createImage());				
			} catch (ThemeException e) {				
				e.printStackTrace();
			}			
		}
				
		imagePreview.getParent().layout(true);
		rightComposite.layout(true);	
		rightComposite.getParent().pack(true);		
	}

	private static void clearComposite(Composite cc) {
		for (Control c: cc.getChildren()) {
			if (c instanceof Label && ((Label)c).getImage() != null)
				((Label)c).getImage().dispose();
			c.dispose();
		}
	}

	protected void performDeletePressed(SelectionEvent e) {
		
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);		
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void okPressed() {				
		super.okPressed();
	}

	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
	
		return null;
	}

	@Override
	protected String getTitle() {		
		return "Component Store";
	}
}
