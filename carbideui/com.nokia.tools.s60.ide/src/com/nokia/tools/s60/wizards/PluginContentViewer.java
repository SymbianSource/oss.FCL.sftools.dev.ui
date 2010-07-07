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
package com.nokia.tools.s60.wizards;

import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class PluginContentViewer implements IInputSelectionProvider{
	private TreeViewer treeViewer;

	private TableViewer tableViewer;

	private boolean updateColSizes = true;

	public PluginContentViewer(Composite parent) {
		SashForm sash = new SashForm(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 28;
		sash.setLayoutData(gd);

		treeViewer = new TreeViewer(sash, SWT.BORDER);
		PluginContentProvider provider = new PluginContentProvider();
		treeViewer.setContentProvider(provider);
		treeViewer.setLabelProvider(new PluginLabelProvider(provider));
		treeViewer.setInput(PluginContentProvider.EMPTY_INPUT);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
			 */
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object input = ((IStructuredSelection) selection)
							.getFirstElement();
					if (input != null) {
						tableViewer.setInput(input);
					}
				}
			}
		});

		tableViewer = new TableViewer(sash, SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setContentProvider(new PluginPropertiesContentProvider(
				provider));
		tableViewer
				.setLabelProvider(new PluginPropertiesLabelProvider(provider));		
		Table table = tableViewer.getTable();
		final TableColumn colPro = new TableColumn(table, SWT.LEFT, 0);
		colPro.setText("Property");

		final TableColumn colVal = new TableColumn(table, SWT.LEFT, 1);
		colVal.setText("Value");

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.getTable().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (updateColSizes) {
					Table tbl = tableViewer.getTable();
					int w = tbl.getClientArea().width;
					int c1 = w / 3 * 2;
					int c2 = w - c1;
					updateColSizes = false;
					colPro.setWidth(c2);
					colVal.setWidth(c1);
					updateColSizes = true;
				}
			}
		});

		sash.setWeights(new int[] { 50, 50 });
	}

	public void setPlugin(Object plugin) {
		if (plugin == null)
			treeViewer.setInput(PluginContentProvider.EMPTY_INPUT);
		else {
			treeViewer.setInput(plugin);
			treeViewer.expandToLevel(2);
			treeViewer.setSelection(new StructuredSelection(
					PluginContentProvider.PLUGIN_ROOT));
		}
		tableViewer.setInput(null);
	}

	public PluginContentProvider getContentProvider() {
		return (PluginContentProvider) treeViewer.getContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		treeViewer.addSelectionChangedListener(listener);
	}
	
	
	public void addKeyListener(KeyListener listener) {
		treeViewer.getTree().addKeyListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return treeViewer.getSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		treeViewer.removeSelectionChangedListener(listener);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		treeViewer.setSelection(selection);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IInputProvider#getInput()
	 */
	public Object getInput() {
		return treeViewer.getInput();
	}
}
