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
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.nokia.tools.resource.util.StringUtils;

public class AcquireFilterTagsDialog extends ListSelectionDialog {

	private Text inputText;	
	private String customInput;
	private List<String> initial;
	private boolean custom;
	
	public AcquireFilterTagsDialog(Shell parentShell, IComponentStore store, boolean custom, List<String> initial, String message) {
		super(parentShell,store.getAvailableTags(),new IStructuredContentProvider(){

			public Object[] getElements(Object inputElement) {
				return ((java.util.List)inputElement).toArray();
			}

			public void dispose() {				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
		}, new LabelProvider(), message == null ? "Please select filter tags for elements being added:" : message);
		setTitle("Component Store");
		this.initial = initial;
		if (initial != null) {
			setInitialElementSelections(initial);
		}
		this.custom = custom;
    }
	
	@Override
	protected Control createDialogArea(Composite parent) {		
		Composite area = (Composite) super.createDialogArea(parent);
		
		if (custom) {
			Label custom = new Label(area, SWT.NONE);
			custom.setText("optionally, define custom tags (comma-separated list):");
			
			inputText = new Text(area, SWT.BORDER);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			inputText.setLayoutData(gd);		
		}
		
		return area;
	}
	
	@Override
	protected void okPressed() {
		
		if (custom)
			customInput = inputText.getText();
		
		super.okPressed();
	}
	
	@Override
	public Object[] getResult() {
		Object object[] = super.getResult();
		List result = new ArrayList();
		result.addAll(Arrays.asList(object));
		if (!StringUtils.isEmpty(customInput)) {
			StringTokenizer st = new StringTokenizer(customInput, ",");
			while(st.hasMoreTokens())
			{
				String tok = st.nextToken().trim();
				if (!StringUtils.isEmpty(tok))
					result.add(tok);
			}
		}
		return result.toArray();
	}

	public List<String> getResultFilterTags() {
		List<String> _tags = new ArrayList<String>();		
		Object[] r = getResult();
		for (Object tag: r)
			_tags.add((String) tag);
		return _tags;
	}
	
	
	
}
