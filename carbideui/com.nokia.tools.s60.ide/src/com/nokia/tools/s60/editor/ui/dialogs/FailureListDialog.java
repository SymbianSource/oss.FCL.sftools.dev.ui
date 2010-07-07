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
package com.nokia.tools.s60.editor.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.nokia.tools.s60.editor.ui.dialogs.IFailure.ESeverity;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class FailureListDialog extends Dialog {
	private Table table = null;

	private StyledText detail = null;

	private List<IFailure> failures;

	private String title;

	public FailureListDialog(Shell shell, String title) {
		super(shell);
		this.title = title;
	}

	public void setFailures(List<IFailure> failures) {
		this.failures = failures;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		if (title != null) {
			parent.getShell().setText(title);
		}

		Composite composite = (Composite) super.createDialogArea(parent);
		Composite fc = new Composite(composite, SWT.NONE);
		fc.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout gridLayout = new GridLayout();
		fc.setLayout(gridLayout);

		Label msgLabel = new Label(fc, SWT.NONE);
		msgLabel.setText(Messages.FailureListDialog_message);

		TableViewer viewer = new TableViewer(fc, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.VERTICAL);
		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setWidth(25);
		TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
		tableColumn1.setWidth(500);
		tableColumn1.setText(Messages.FailureListDialog_element_lbl);
		TableColumn tableColumn2 = new TableColumn(table, SWT.NONE);
		tableColumn2.setWidth(300);
		tableColumn2.setText(Messages.FailureListDialog_detail_lbl);
		table.setLayoutData(new GridData(600, 200));

		new Label(fc, SWT.NONE);

		Label detailLabel = new Label(fc, SWT.NONE);
		detailLabel.setText(Messages.FailureListDialog_description_lbl);

		detail = new StyledText(fc, SWT.BORDER | SWT.VERTICAL | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.heightHint = 200;
		gridData.widthHint = 600;
		detail.setLayoutData(gridData);

		viewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List) {
					return ((List) inputElement).toArray();
				}
				return null;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		});

		viewer.setLabelProvider(new ITableLabelProvider() {
			Image err_image = S60WorkspacePlugin.getImageDescriptor(
					"icons/error_tsk.gif").createImage();

			Image warn_image = S60WorkspacePlugin.getImageDescriptor(
					"icons/warn_tsk.gif").createImage();

			Image info_image = S60WorkspacePlugin.getImageDescriptor(
					"icons/info_tsk.gif").createImage();

			public Image getColumnImage(Object element, int columnIndex) {
				if (columnIndex == 0) {
					ESeverity severity = (((IFailure) element).getSeverity());
					if (ESeverity.INFO == severity) {
						return info_image;
					} else if (ESeverity.WARN == severity) {
						return warn_image;
					} else if (ESeverity.ERROR == severity) {
						return err_image;
					}
				}
				return null;
			}

			public String getColumnText(Object element, int columnIndex) {
				if (columnIndex == 1) {
					return ((IFailure) element).getSource();
				}

				if (columnIndex == 2) {
					return ((IFailure) element).getMessage();
				}
				return null;
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public void dispose() {
				err_image.dispose();
				warn_image.dispose();
				info_image.dispose();
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void removeListener(ILabelProviderListener listener) {
			}

		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event
						.getSelection();
				if (sel.isEmpty()) {
					detail.setText("");
				} else {
					IFailure f = (IFailure) sel.getFirstElement();
					detail.setText(f.getDetail());
				}
			}
		});

		viewer.setInput(failures);

		viewer.setSelection(new StructuredSelection(failures.get(0)));

		return composite;
	}
}