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
package com.nokia.tools.s60.editor.ui.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.media.utils.clipboard.ClipboardContentElement;
import com.nokia.tools.s60.editor.ui.views.Messages;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

/**
 */
public class ExtendedPasteConfirmDialog extends ListSelectionDialog {

	private Object[] checkedElements;

	private Composite errorComposite;

	/**
	 * Message displayed as error
	 */
	private String errorMessage;

	private String errorTitle;

	private boolean displayError = false;

	/**
	 * Text widget containing error message.
	 */
	private Text errorText;

	private Label errorLabel;

	private boolean showFiles;
	
	private CheckboxTableViewer viewer;
	
	private boolean updateColSizes = true;

	public ExtendedPasteConfirmDialog(Shell parentShell,
			List<ClipboardContentElement> input, final IContent targetTheme) {
		super(parentShell, input, new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				// Show only icons present in target theme
				List result = new ArrayList();
				for (Object x : (List) inputElement) {
					ClipboardContentElement el = (ClipboardContentElement) x;
					if (targetTheme.findById((String) el.getMetadata()) != null)
						result.add(el);
				}
				Collections.sort(result);
				return result.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}
		}, getLabelProvider(), Messages.PasteConfirmDlg_Msg);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control c = super.createContents(parent);

		if (showFiles) {
			viewer = (CheckboxTableViewer) getViewer();
			final TableColumn colElements = new TableColumn(viewer.getTable(), SWT.NONE);
			colElements.setText("Element(s)");
			final TableColumn colFiles = new TableColumn(viewer.getTable(), SWT.NONE);
			colFiles.setText("File(s)");
			viewer.getTable().setHeaderVisible(true);
			// viewer.getTable().setLinesVisible(true);
			viewer.setInput(viewer.getInput());
			viewer.getTable().addListener(SWT.Resize, new Listener() {
				public void handleEvent(Event event) {
					if (updateColSizes) {
						Table tbl = viewer.getTable();
						int w = tbl.getClientArea().width;
						int c1 = w / 3 * 2;
						int c2 = w - c1;
						updateColSizes = false;
						colElements.setWidth(c2);
						colFiles.setWidth(c1);
						updateColSizes = true;
					}
				}
			});

		}
		return c;
	}

	private static ILabelProvider getLabelProvider() {
		return new PasteLabelProvider();
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
	}

	@Override
	protected void okPressed() {
		checkedElements = getViewer().getCheckedElements();
		super.okPressed();
	}

	public List<ClipboardContentElement> getSelectedContentData() {
		List<ClipboardContentElement> result = new ArrayList<ClipboardContentElement>();
		if (checkedElements != null) {
			for (Object o : checkedElements) {
				if (o instanceof TableItem)
					o = ((TableItem) o).getData();
				result.add((ClipboardContentElement) o);
			}
		}
		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridData gd = (GridData) ((Composite) composite).getLayoutData();
		gd.widthHint = 580;
		gd.heightHint = 487;
		GridLayout gl = (GridLayout) ((Composite) composite).getLayout();
		gl.marginHeight = 13;
		gl.marginWidth = 13;
		gl.verticalSpacing = 7;

		if (displayError) {
			GridData ld = new GridData(GridData.FILL_BOTH);
			errorComposite = new Composite(composite, SWT.NONE);
			errorComposite.setLayoutData(ld);
			GridLayout layout = new GridLayout();
			errorComposite.setLayout(layout);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 7;

			createErrorDialogArea(errorComposite);
		}
		return composite;
	}

	private void createErrorDialogArea(Composite composite) {
		Composite labelComp = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		labelComp.setLayout(layout);

		Label label = new Label(labelComp, SWT.NONE);
		final Image image = S60WorkspacePlugin.getImageDescriptor(
				"icons/showwarn_tsk.gif").createImage();
		label.setImage(image);
		label.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				image.dispose();
			}
		});

		errorLabel = new Label(labelComp, SWT.NONE);
		errorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false));

		errorText = new Text(composite, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.horizontalSpan = 2;
		errorText.setLayoutData(ld);
		errorText.setEditable(false);
		errorText.setBackground(ColorConstants.white);

		prepareOpen();
	}

	private void prepareOpen() {
		if (errorMessage != null) {// if error message exists
			errorLabel.setText(errorTitle);
			errorText.setText(errorMessage);
		} else {
			errorLabel.setText("NO ERROR TEXT SPECIFIED");
		}
		((GridData) errorText.getLayoutData()).heightHint = 100;
		errorComposite.layout(true, true);
	}

	public boolean isDisplayError() {
		return displayError;
	}

	public void setDisplayError(boolean displayError) {
		this.displayError = displayError;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public int open() {
		int result = super.open();
		return result;
	}

	public String getErrorTitle() {
		return errorTitle;
	}

	public void setErrorTitle(String errorTitle) {
		this.errorTitle = errorTitle;
	}

	static class PasteLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0) {
				ClipboardContentElement iccd = (ClipboardContentElement) element;
				return (String) (iccd.getMetadata(1) != null ? iccd
						.getMetadata(1) : iccd.getMetadata());
			}
			if (columnIndex == 1) {
				ClipboardContentElement iccd = (ClipboardContentElement) element;
				if (iccd.getContent() instanceof List) {
					List files = (List) iccd.getContent();
					StringBuffer sb = new StringBuffer();
					for (Object file : files) {
						if (file != null) {
							if (sb.length() > 0) {
								sb.append("; ");
							}
							sb.append(((File) file).getName());
						}
					}
					return sb.toString();
				}
			}
			return null;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}
	}

	public void setShowFiles(boolean showFiles) {
		this.showFiles = showFiles;
	}

}
