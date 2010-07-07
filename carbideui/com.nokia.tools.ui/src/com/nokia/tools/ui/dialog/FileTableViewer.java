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
import java.util.Comparator;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class FileTableViewer extends TableViewer {
	private IResourceSelectionPage.Adapter page;
	private IFileContentProvider contentProvider;

	/**
	 * @param parent
	 * @param style
	 */
	public FileTableViewer(Composite parent, int style,
	    IResourceSelectionPage.Adapter page) {
		super(parent, style);
		this.page = page;
		init(parent);
	}

	/**
	 * @param parent
	 */
	public FileTableViewer(Composite parent, IResourceSelectionPage.Adapter page) {
		this(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL, page);
	}
	
	/**
	 * @param parent
	 */
	public FileTableViewer(Composite parent, IResourceSelectionPage.Adapter page, boolean allowMultiple) {
		this(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL| SWT.MULTI, page);
	}

	/**
	 * @param contentProvider the contentProvider to set
	 */
	public void setContentProvider(IFileContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	protected void init(Composite parent) {
		final Table fileTable = getTable();
		fileTable.addListener(SWT.Paint, new TableImagePaintListener(page
		    .getManager(), getTable()));
		fileTable.setLinesVisible(false);
		fileTable.setHeaderVisible(true);

		final TableColumn column = new TableColumn(fileTable, SWT.LEFT, 0);
		column.setText(Messages.ResourceSelectionDialog_FileLabel);

		MouseMoveListener listener = new MouseMoveListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseMove(MouseEvent e) {
				page.handleMouseMove(fileTable, e);
			}

		};
		parent.addMouseMoveListener(listener);
		fileTable.addMouseMoveListener(listener);

		int w = fileTable.getClientArea().width;
		column.setWidth(w);

		fileTable.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				int w = fileTable.getClientArea().width;
				column.setWidth(w);
			}
		});
		setContentProvider(new FileContentProvider());
		setLabelProvider(new FileLabelProvider());
		setSorter(new ViewerSorter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#getComparator()
			 */
			@Override
			protected Comparator<String> getComparator() {
				return new Comparator<String>() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Comparator#compare(java.lang.Object,
					 *      java.lang.Object)
					 */
					public int compare(String o1, String o2) {
						return o1.compareToIgnoreCase(o2);
					}
				};
			}
		});
		addSelectionChangedListener(page);
		addOpenListener(page);
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

	class FileLabelProvider extends LabelProvider
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
				return page.getManager().getDefaultResourceImage();
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
