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
package com.nokia.tools.media.utils.timeline.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.nokia.tools.media.utils.timeline.ITimeLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineDataProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineGridLabelProvider;
import com.nokia.tools.media.utils.timeline.ITimeLineRow;
import com.nokia.tools.media.utils.timeline.ITimeLineTreeContentProvider;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;

public class TreeTimeLine extends TimeLine {

	protected Composite treeComposite;

	protected GridData treeGridData;

	protected TreeViewer treeViewer;

	public TreeTimeLine(Composite parent, int style) {
		super(parent, style);
	}

	public TreeTimeLine(Composite parent, int style,
			ITreeTimeLineDataProvider provider) {
		super(parent, style, provider);
	}

	public TreeTimeLine(Composite parent, int style, long startTime,
			long endTime, long displayWidth) {
		super(parent, style, startTime, endTime, displayWidth);
	}

	public TreeTimeLine(Composite parent, int style, long startTime,
			long endTime, long displayWidth,
			ITimeLabelProvider timeLabelProvider,
			ITimeLineGridLabelProvider gridLabelProvider) {
		super(parent, style, startTime, endTime, displayWidth,
				timeLabelProvider, gridLabelProvider);
	}

	@Override
	public void initialize(ITimeLineDataProvider dataProvider) {
		super.initialize(dataProvider);
		if (dataProvider instanceof ITreeTimeLineDataProvider) {
			ITreeTimeLineDataProvider treeDataProvider = (ITreeTimeLineDataProvider) dataProvider;

			setTreeContentProvider(treeDataProvider.getTreeContentProvider());
			setTreeLabelProvider(treeDataProvider.getTreeLabelProvider());

			setInput(treeDataProvider.getInput());
		}
	}

	protected TreeViewer createTreeViewer(Tree tree) {
		if ((SWT.CHECK & tree.getStyle()) == SWT.CHECK) {
			return new CheckboxTreeViewer(tree);
		} else {
			return new TreeViewer(tree);
		}
	}

	@Override
	protected Composite createLabelsArea(Composite parent) {
		treeComposite = new Composite(parent, SWT.NONE);
		treeGridData = new GridData(GridData.FILL, GridData.FILL, false, true);
		treeComposite.setLayoutData(treeGridData);

		Tree tree = new Tree(treeComposite, SWT.SINGLE
				| (getStyle() & SWT.CHECK) | SWT.NO_BACKGROUND);
		tree.setLocation(0, 0);
		tree.setSize(2000, 2000);

		treeViewer = createTreeViewer(tree);
		tree.setBackground(parent.getBackground());
		tree.addTreeListener(new TimeLineTreeListener());

		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				if (item != null) {
					Object _row = item.getData();
					if (!(_row instanceof ITimeLineRow) && _row != null) {
						_row = ((ITreeTimeLineDataProvider) dataProvider)
								.getRowForTreeElement(_row);
					}
					ITimeLineRow row = (ITimeLineRow) _row;
					if (row != null) {
						_setSelectedRow(row);
						setSelectedNode(null);
						setSelectedControlPoint(null);
						notifySelectionListeners();
						repaint();
					}
				}
			}
		});

		setRowHeight(tree.getItemHeight());

		return treeComposite;
	}

	public void setRowSelected(Object modelObject) {
		Object _row = modelObject;
		if (!(_row instanceof ITimeLineRow) && _row != null) {
			_row = ((ITreeTimeLineDataProvider) dataProvider)
					.getRowForTreeElement(_row);
		}
		ITimeLineRow row = (ITimeLineRow) _row;
		if (row != null) {
			setSelectedRow(row);
			setSelectedNode(null);
			setSelectedControlPoint(null);
			notifySelectionListeners();
		}
	}

	public @Override
	void setSelectedRow(final ITimeLineRow row) {
		super.setSelectedRow(row);

		Runnable runnable = new Runnable() {
			public void run() {
				final int idx = Arrays.asList(getVisibleRows()).indexOf(row);
				if (idx < 0) {
					treeViewer.setSelection(StructuredSelection.EMPTY);
					return;
				}

				final TreeItem[] items = collectTreeItems();
				if (idx < items.length) {
					TreeItem[] selection = treeViewer.getTree().getSelection();
					if (selection.length == 0 || selection[0] != items[idx]) {
						if (!treeViewer.getTree().isFocusControl()) {
							treeViewer.getTree().setFocus();
						}
						treeViewer.setSelection(new StructuredSelection(
								items[idx].getData()));
					}
				}
			}
		};

		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}
	}

	private void _setSelectedRow(final ITimeLineRow row) {
		super.setSelectedRow(row);
	}

	TreeItem[] collectTreeItems() {
		List<TreeItem> toRet = new ArrayList<TreeItem>();

		TreeItem[] items = treeViewer.getTree().getItems();
		for (TreeItem child : items) {
			toRet.add(child);
			if (child.getExpanded()) {
				toRet.addAll(collectTreeItems(child));
			}
		}
		return toRet.toArray(new TreeItem[toRet.size()]);
	}

	List<TreeItem> collectTreeItems(TreeItem item) {
		List<TreeItem> toRet = new ArrayList<TreeItem>();
		TreeItem[] items = item.getItems();
		for (TreeItem child : items) {
			toRet.add(child);
			if (child.getExpanded()) {
				toRet.addAll(collectTreeItems(child));
			}
		}
		return toRet;
	}

	void synchronizeRowsWithTree() {
		if (treeViewer.getTree().isDisposed()) {
			return;
		}

		TreeItem[] items = treeViewer.getTree().getItems();

		ITimeLineRow[] rows = getRows();
		for (ITimeLineRow row : rows) {
			row.setVisible(false);
		}

		for (TreeItem item : items) {
			treeExpanded(item);
		}

		updateVerticalScrollbar();
		repaint();
	}

	private void treeExpanded(TreeItem item) {
		Object _row = item.getData();
		if (!(_row instanceof ITimeLineRow) && _row != null) {
			_row = ((ITreeTimeLineDataProvider) dataProvider)
					.getRowForTreeElement(_row);
		}
		ITimeLineRow row = (ITimeLineRow) _row;
		if (row != null) {
			row.setVisible(true);
		}
		if (item.getExpanded()) {
			TreeItem[] children = item.getItems();
			for (TreeItem child : children) {
				treeExpanded(child);
			}
		}
	}

	public void setInput(Object input) {

		setRedraw(false);
		try {

			treeViewer.setInput(input);

			// remove old rows
			for (ITimeLineRow row : getRows()) {
				removeRow(row);
			}

			// add new rows
			ITreeContentProvider provider = (ITreeContentProvider) treeViewer
					.getContentProvider();
			Object[] parentElems = provider.getElements(input);
			for (Object object : parentElems) {
				ITimeLineRow row = null;
				if (object instanceof ITimeLineRow) {
					row = (ITimeLineRow) object;
				} else if (this.dataProvider != null) {
					row = ((ITreeTimeLineDataProvider) this.dataProvider)
							.getRowForTreeElement(object);
				}

				if (row != null) {
					row.setVisible(true);
					addRow(row);
					if (treeViewer instanceof CheckboxTreeViewer) {
						((CheckboxTreeViewer) treeViewer).setChecked(object,
								((ITimeLineTreeContentProvider) treeViewer
										.getContentProvider())
										.isSelected(object));
					}
				}

				addChildrens(object);
			}

			synchronizeRowsWithTree();

			treeGridData.minimumHeight = treeGridData.heightHint = getRows().length
					* rowHeight;
			;
			treeGridData.minimumWidth = treeGridData.widthHint = computePrefferedTreeWidth(treeViewer
					.getTree());

			setRowHeight(treeViewer.getTree().getItemHeight());

			layout(true, true);
		} finally {
			setRedraw(true);
		}

		repaintAll();
	}

	protected int computePrefferedTreeWidth(Tree tree) {
		Object[] expandedElements = treeViewer.getExpandedElements();

		treeViewer.expandAll();
		TreeItem[] items = treeViewer.getTree().getItems();
		int maxWidth = 0;
		for (TreeItem child : items) {
			maxWidth = Math.max(maxWidth, computePrefferedTreeWidth(child));
		}
		treeViewer.setExpandedElements(expandedElements);

		return maxWidth;
	}

	private int computePrefferedTreeWidth(TreeItem item) {
		int maxWidth = item.getBounds().x + item.getBounds().width;

		TreeItem[] items = item.getItems();
		for (TreeItem child : items) {
			maxWidth = Math.max(maxWidth, computePrefferedTreeWidth(child));
		}
		return maxWidth;
	}

	protected void addChildrens(Object parent) {
		ITreeContentProvider provider = (ITreeContentProvider) treeViewer
				.getContentProvider();
		Object[] children = provider.getChildren(parent);
		for (Object child : children) {
			ITimeLineRow row = null;
			if (child instanceof ITimeLineRow) {
				row = (ITimeLineRow) child;
			} else if (this.dataProvider != null) {
				row = ((ITreeTimeLineDataProvider) this.dataProvider)
						.getRowForTreeElement(child);
			}

			if (row != null) {
				row.setVisible(false);
				addRow(row);

				if (treeViewer instanceof CheckboxTreeViewer) {
					((CheckboxTreeViewer) treeViewer).setChecked(child,
							((ITimeLineTreeContentProvider) treeViewer
									.getContentProvider()).isSelected(child));
				}
			}

			addChildrens(child);
		}
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void setTreeContentProvider(ITimeLineTreeContentProvider provider) {
		treeViewer.setContentProvider(provider);
	}

	public void setTreeLabelProvider(ILabelProvider provider) {
		treeViewer.setLabelProvider(provider);
	}

	class TimeLineTreeListener implements TreeListener {

		public void treeCollapsed(TreeEvent e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					synchronizeRowsWithTree();
					updateVerticalScrollbar();
					repaint();
				}
			});
		}

		public void treeExpanded(TreeEvent e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					synchronizeRowsWithTree();
					updateVerticalScrollbar();
					repaint();
				}
			});
		}
	}

	@Override
	public void repaintAll() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!isDisposed()) {
					treeViewer.refresh();
					TreeTimeLine.super.repaintAll();
				}
			}
		});
	}

	@Override
	public Viewer getViewer() {
		return getTreeViewer();
	}
}
