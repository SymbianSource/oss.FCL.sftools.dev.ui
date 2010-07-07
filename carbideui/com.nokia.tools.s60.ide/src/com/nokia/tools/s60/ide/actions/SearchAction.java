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
package com.nokia.tools.s60.ide.actions;

import java.awt.Graphics2D;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.ImageAdapter;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.ShowInResourceViewAction;
import com.nokia.tools.s60.views.SearchView;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

public class SearchAction extends Action implements
		IWorkbenchWindowActionDelegate, IPartListener {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide.searchDialog_context"; 

	private IAction fAction = null;

	public void dispose() {
		
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		if (activeWorkbenchWindow != null) {
			activeWorkbenchWindow.getPartService().removePartListener(this);
		}
		
	}

	public void init(IWorkbenchWindow window) {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
				.addPartListener(this);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				SearchAction.HLP_CTX);
	}

	public void run(IAction action) {
		if (fAction.isEnabled()) {

			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IViewPart searchView = page.findView(SearchView.ID);
					if (searchView == null) {
						IWorkbenchPart activePart = page.getActivePart();
						try {
							searchView = page.showView(SearchView.ID);
						} catch (PartInitException pie) {
						}
					} else {
						page.bringToTop(searchView);

					}
				}
			}

			/*
			 * SearchDialog dialog = new SearchDialog(Display.getCurrent()
			 * .getActiveShell());
			 * dialog.setTitle(ActionMessages.SearchAction_Dialog_Title);
			 * dialog.open();
			 */
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (fAction != action) {
			fAction = action;
		}
		recalculateAndEnable();
	}

	public void partActivated(IWorkbenchPart part) {
		recalculateAndEnable();
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		

	}

	public void partClosed(IWorkbenchPart part) {
		recalculateAndEnable();
	}

	public void partDeactivated(IWorkbenchPart part) {
		

	}

	public void partOpened(IWorkbenchPart part) {
		

	}

	class SearchDialog extends SelectionDialog {

		Canvas previewCanvas;

		Text pattern;

		Table items;

		TableViewer tableViewer;

		private IStructuredSelection previousSelection = null;

		protected SearchDialog(Shell parentShell) {
			super(parentShell);
			
		}

		protected Control createDialogArea(Composite parent) {

			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					parent.getParent(), SearchAction.HLP_CTX);

			Composite dialogArea = (Composite) super.createDialogArea(parent);
			dialogArea.setLayout(new GridLayout(4, false));
			Label l = new Label(dialogArea, SWT.NONE);
			l.setText(ActionMessages.SearchAction_Dialog_Search_Instructions);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 4;
			l.setLayoutData(data);

			// finds all component names in a system
			createInput();

			pattern = new Text(dialogArea, SWT.BORDER);
			pattern.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					tableViewer.setInput(filterInput(container
							.getAllAllowedItems(), pattern.getText()));
					if (tableViewer.getElementAt(0) != null) {
						tableViewer.setSelection(new StructuredSelection(
								tableViewer.getElementAt(0)));
					}
				}

			});

			pattern.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent event) {
					if (event.keyCode == SWT.ARROW_DOWN) {
						tableViewer.scrollDown(0, 1);
						tableViewer.getTable().setFocus();
					}
				}
			});

			data = new GridData();
			data.widthHint = 300;
			data.verticalAlignment = SWT.TOP;
			data.horizontalSpan = 2;
			pattern.setLayoutData(data);

			Button caseSensitive = new Button(dialogArea, SWT.CHECK);
			caseSensitive.setText("Case sensitive");
			caseSensitive.setEnabled(false);
			data = new GridData();
			data.horizontalAlignment = SWT.CENTER;
			data.verticalAlignment = SWT.TOP;
			caseSensitive.setLayoutData(data);

			previewCanvas = new Canvas(dialogArea, SWT.NONE);
			data = new GridData();
			data.widthHint = 60;
			data.heightHint = 60;
			data.horizontalAlignment = SWT.CENTER;
			data.verticalSpan = 3;
			previewCanvas.setLayoutData(data);

			Button colorsOnly = new Button(dialogArea, SWT.CHECK);
			colorsOnly.setText("Show only texts with color:");
			colorsOnly.setLayoutData(new GridData());
			colorsOnly.setEnabled(false);

			Combo colors = new Combo(dialogArea, SWT.NONE);
			colors.setEnabled(false);
			colors.setText("Color");
			data = new GridData();
			data.horizontalSpan = 2;
			data.horizontalAlignment = SWT.BEGINNING;
			colors.setLayoutData(data);

			Button skinnedOnly = new Button(dialogArea, SWT.CHECK);
			skinnedOnly.setEnabled(false);
			skinnedOnly.setText("Show only modified items");
			data = new GridData();
			data.horizontalSpan = 3;
			data.verticalAlignment = SWT.TOP;
			skinnedOnly.setLayoutData(data);

			l = new Label(dialogArea, SWT.NONE);
			l.setText(ActionMessages.SearchAction_Dialog_Elements_Label_Text);

			data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 4;
			l.setLayoutData(data);

			tableViewer = new TableViewer(dialogArea, SWT.MULTI | SWT.BORDER
					| SWT.V_SCROLL);
			tableViewer
					.setSorter(new SearchTableSorter(SearchTableSorter.NAME));
			final Table items = tableViewer.getTable();

			items.setLinesVisible(true);
			items.setHeaderVisible(true);

			// 1st column
			TableColumn column = new TableColumn(items, SWT.CENTER, 0);
			column.setText("Name");
			column.setWidth(200);
			column.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new SearchTableSorter(
							SearchTableSorter.NAME));
				}
			});

			// 2nd column
			column = new TableColumn(items, SWT.LEFT, 1);
			column.setText("ID");
			column.setWidth(200);
			column.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new SearchTableSorter(
							SearchTableSorter.ID));
				}
			});

			/*
			 * // 3rd column column = new TableColumn(items, SWT.LEFT, 2);
			 * column.setText("Resource path"); column.setWidth(200);
			 * column.addSelectionListener(new SelectionAdapter() { public void
			 * widgetSelected(SelectionEvent e) { tableViewer.setSorter(new
			 * SearchTableSorter( SearchTableSorter.RESOURCE_PATH)); } });
			 */

			// 4th column
			column = new TableColumn(items, SWT.LEFT, 2);
			column.setText("Special editing");
			column.setWidth(100);
			column.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new SearchTableSorter(
							SearchTableSorter.SPECIAL_EDITING));
				}
			});

			// 5th column
			column = new TableColumn(items, SWT.LEFT, 3);
			column.setText("Skinned");
			column.setWidth(60);
			column.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					tableViewer.setSorter(new SearchTableSorter(
							SearchTableSorter.SKINNED));
				}
			});

			// setOfContentDataNames.clear();
			// listOfContentDataNames.clear();
			tableViewer.setLabelProvider(new SearchDataLabelProvider());
			tableViewer.setContentProvider(new ArrayContentProvider() {
				@Override
				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {
					super.inputChanged(viewer, oldInput, newInput);
				}

				@Override
				public Object[] getElements(Object inputElement) {
					if (inputElement instanceof IDGroupContainer) {
						IDGroupContainer gc = (IDGroupContainer) inputElement;
						ArrayList<ElementTableItem> allAllowedItems = gc
								.getAllAllowedItems();
						ArrayList<String> itemsToBeShown = new ArrayList<String>();
						/*
						 * for(int i=0;i<allAllowedItems.size();i++){
						 * itemsToBeShown.add(allAllowedItems.get(i).getName()); }
						 */
						return allAllowedItems.toArray();
						// return itemsToBeShown.toArray(new
						// String[itemsToBeShown.size()]);//allAllowedItems.toArray(new
						// String[allAllowedItems.size()]);
					}
					return super.getElements(inputElement);
				}

				@Override
				public void dispose() {
					super.dispose();
				}

			});

			tableViewer.setInput(container);
			/*
			 * for (Button b : buttons) { b.addSelectionListener(new
			 * SelectionListener() { public void widgetSelected(SelectionEvent
			 * e) { Button b = (Button)e.getSource(); boolean selected =
			 * b.getSelection(); if (selected) {
			 * container.checkGroup(b.getText()); } else {
			 * container.uncheckGroup(b.getText()); } tableViewer.refresh(); }
			 * public void widgetDefaultSelected(SelectionEvent e) { } }); }
			 */

			data = new GridData(GridData.FILL_BOTH);
			data.heightHint = 12 * items.getItemHeight();
			data.widthHint = 560;
			data.horizontalSpan = 4;
			items.setLayoutData(data);

			tableViewer
					.addSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(SelectionChangedEvent event) {
							Object source = event.getSource();
							IStructuredSelection selection = (IStructuredSelection) tableViewer
									.getSelection();
							if (previousSelection == null
									|| selection.getFirstElement() != previousSelection
											.getFirstElement()) {
								previousSelection = selection;
								GC gc = new GC(previewCanvas);
								gc.fillRectangle(0, 0, 100, 100);
								repaintPreviewCanvas(gc);
							}
							/*
							 * System.out.println(event.getSelection().getClass()
							 * .getName());
							 * System.out.println(source.getClass().getName());
							 */
							// TableViewer viewer = (TableViewer)source;
						}

					});

			if (tableViewer.getElementAt(0) != null) {
				tableViewer.setSelection(new StructuredSelection(tableViewer
						.getElementAt(0)));
			}
			tableViewer.addDoubleClickListener(new IDoubleClickListener() {

				public void doubleClick(DoubleClickEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event
							.getSelection();
					ElementTableItem selectedElement = (ElementTableItem) selection
							.getFirstElement();
					selectAndFocusElement(selectedElement.getName());
					close();
				}

			});

			previewCanvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent pe) {
					GC gc = pe.gc;
					repaintPreviewCanvas(gc);
					gc.dispose();

				}
			});

			applyDialogFont(dialogArea);
			return dialogArea;

		}

		private void repaintPreviewCanvas(GC gc) {
			IStructuredSelection selection = (IStructuredSelection) tableViewer
					.getSelection();

			ElementTableItem selectedElement = (ElementTableItem) selection
					.getFirstElement();
			IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();

			if (activeEd instanceof Series60EditorPart) {
				Series60EditorPart sep = (Series60EditorPart) activeEd;

				IContent[] cnt = sep.getContents();
				IContent root = ScreenUtil.getPrimaryContent(cnt);
				if (selectedElement != null) {
					IContentData data = root.findByName(selectedElement
							.getName());
					IImageAdapter imageAdapter = (IImageAdapter) data
							.getAdapter(IImageAdapter.class);
					if (imageAdapter != null) {
						IImage image = imageAdapter.getImage();
						int width = image.getWidth();
						int height = image.getHeight();
						RenderedImage img = null;
						if (width <= 60 && height <= 60) {
							img = image.getAggregateImage();
						} else {
							if (width > 60) {
								width = 60;
							}
							if (height > 60) {
								height = 60;
							}
							img = image.getAggregateImage(60, 60);
						}

						ImageAdapter vpAdaptor = new ImageAdapter(width, height);
						Graphics2D vpG = (Graphics2D) vpAdaptor.getGraphics();
						vpG.drawRenderedImage(img, CoreImage.TRANSFORM_ORIGIN);
						vpG.dispose();
						Image swtImage = vpAdaptor.toSwtImage();
						gc.drawImage(swtImage, 0, 0);
						gc.dispose();
					}
				}
			}

		}

		protected void okPressed() {
			IStructuredSelection selection = (IStructuredSelection) tableViewer
					.getSelection();
			ElementTableItem selectedElement = (ElementTableItem) selection
					.getFirstElement();
			selectAndFocusElement(selectedElement.getName());

			super.okPressed();
		}

		private List<String> idSelectionToList(IStructuredSelection selection) {
			List<String> idList = new ArrayList<String>();
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				String elementId = (String) iter.next();
				idList.add(elementId);
			}
			return idList;
		}

		public Object filterInput(ArrayList<ElementTableItem> dataToFilter,
				String pattern) {

			ArrayList<ElementTableItem> filteredInput = new ArrayList<ElementTableItem>();
			
			pattern = pattern.replaceAll("\\*", ".*");
			pattern = pattern + ".*";
			for (int i = 0; i < dataToFilter.size(); i++) {
				String testedName = dataToFilter.get(i).getName();

				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(testedName);

				if (m.matches()) {
					filteredInput.add(dataToFilter.get(i));
				}
			}
			// }

			return filteredInput;
		}

	}

	public class SearchDataLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getImage(Object element) {
			return null;
		}

		/*
		 * public String getText(Object element) { String elementName = (String)
		 * element; return elementName; }
		 */

		public Image getColumnImage(Object element, int columnIndex) {
			
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			ElementTableItem tmpItem = (ElementTableItem) element;
			String result = null;
			switch (columnIndex) {
			case 0:
				result = tmpItem.getName();
				break;
			case 1:
				result = tmpItem.getId();
				break;
			/*
			 * case 2: result = tmpItem.getResourcePath(); break;
			 */
			case 2:
				result = tmpItem.getSpecialEditing();
				break;
			case 3:
				result = tmpItem.isSkinned() ? "Y" : "N";
			default:
				break;
			}
			return result;
		}

	}

	// Select and focus element in the Resource view.
	private void selectAndFocusElement(String elementName) {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();

		if (activeEd instanceof Series60EditorPart) {
			Series60EditorPart sep = (Series60EditorPart) activeEd;

			IContent[] cnt = sep.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);

			IContentData data = root.findByName(elementName);

			if (data != null) {

				/*
				 * if (!isLeafElement(data)) { data = getFirstChild(data); }
				 */

				
				// find and run 'show in resource view'
				ShowInResourceViewAction act = (ShowInResourceViewAction) ((ActionRegistry) sep
						.getAdapter(ActionRegistry.class))
						.getAction(ShowInResourceViewAction.ID);
				act.doRun(data);

				try {
					sep.selectReveal(new StructuredSelection(data));
				} catch (Exception e) {
					// element does not have preview screen
				}
			}

		}
	}

	// Creating input for table viewer
	private void createInput() {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();

		if (activeEd instanceof Series60EditorPart) {
			Series60EditorPart sep = (Series60EditorPart) activeEd;
			IContent[] cnt = sep.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);

			if (root != null) {
				container = digTheTree(root);
			}
			/*
			 * listOfContentDataNames.addAll(setOfContentDataNames); // sort
			 * list to enable faster filtering
			 * Collections.sort(listOfContentDataNames);
			 */}

	}

	IDGroupContainer container;

	public IDGroupContainer digTheTree(IContentData root) {
		IDGroupContainer groupContainer = new IDGroupContainer();
		for (IContentData n1 : root.getChildren()) {
			if (n1.getAdapter(IScreenAdapter.class) != null) {
				continue;
			}
			IDGroup group = new IDGroup(n1.getName());
			for (IContentData n2 : n1.getChildren()) {
				SortedSet<ElementTableItem> icons = new TreeSet<ElementTableItem>();
				for (IContentData n3 : n2.getChildren()) {
					for (IContentData n4 : n3.getChildren()) {
						ElementTableItem eti = new ElementTableItem();
						eti.setName(n4.getName());
						eti.setId(n4.getId());
						eti.setResourcePath(n2.getName() + "->" + n3.getName());
						IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) n4
								.getAdapter(IToolBoxAdapter.class);
						if (toolBoxAdapter != null
								&& toolBoxAdapter.isMultipleLayersSupport()) {
							eti.setSpecialEditing(eti.getSpecialEditing()
									+ "layers/effects");
						}

						IColorAdapter colorAdapter = (IColorAdapter) n4
								.getAdapter(IColorAdapter.class);
						if (colorAdapter != null) {
							eti.setSpecialEditing(eti.getSpecialEditing()
									+ "color");
						}

						IMediaFileAdapter mediaFileAdapter = (IMediaFileAdapter) n4
								.getAdapter(IMediaFileAdapter.class);

						if (mediaFileAdapter != null
								&& mediaFileAdapter.isSound()) {
							eti.setSpecialEditing(eti.getSpecialEditing()
									+ "sound");
						}

						IImageAdapter imageAdapter = (IImageAdapter) n4
								.getAdapter(IImageAdapter.class);
						if (imageAdapter != null && imageAdapter.isAnimated()) {
							eti.setSpecialEditing(eti.getSpecialEditing()
									+ "animated image");

						}

						ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) n4
								.getAdapter(ISkinnableEntityAdapter.class);
						
						
						//if (skAdapter != null && skAdapter.isNinePiece()) {
						if (skAdapter != null && skAdapter.isMultiPiece()) {
							String searchViewText = skAdapter.getMultiPieceSearchViewText();
							eti.setSpecialEditing(eti.getSpecialEditing()
									+ searchViewText);
									//+ "nine piece");
						}

						if (skAdapter != null && skAdapter.isSkinned()) {
							eti.setSkinned(true);
						}

						icons.add(eti);
					}
				}
				group.addAll(icons);
			}
			groupContainer.putGroup(group.getName(), group);
		}

		return groupContainer;
	}

	/*
	 * private boolean isLeafElement(IContentData node) {
	 * ISkinnableEntityAdapter isea = (ISkinnableEntityAdapter) node
	 * .getAdapter(ISkinnableEntityAdapter.class); if (isea.supportsNinePiece()) {
	 * return true; } if (node.hasChildren()) { return false; } else { return
	 * true; } } private IContentData getFirstChild(IContentData node) { if
	 * (!isLeafElement(node)) { return getFirstChild(node.getChildren()[0]); }
	 * else { return node; } }
	 */
	// HashSet<String> setOfContentDataNames = new HashSet<String>();
	// use list (has to be sorted)
	// ArrayList<String> listOfContentDataNames = new ArrayList<String>();
	/**
	 * Takes the active editor and calculate whether to enable search action and
	 * then do that enablin/disabling
	 */
	private void recalculateAndEnable() {
		if (null == fAction
				|| PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null
				|| PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage() == null)
			return;
		IEditorPart part = EclipseUtils.getActiveSafeEditor();
		if (null != part && part.getAdapter(IPackager.class) != null
				&& part.getAdapter(IContentAdapter.class) != null) {
			fAction.setEnabled(true);
		} else
			fAction.setEnabled(false);
	}

	/**
	 * Container of IDGroups
	 */
	public class IDGroupContainer {
		/**
		 * pair of <Group name, Group>
		 */
		private HashMap<String, IDGroup> groupMap = new HashMap<String, IDGroup>();

		/**
		 * List of checked groups
		 */
		List<String> checkedGroupNames = new ArrayList<String>();

		public void checkGroup(String groupName) {
			if (!checkedGroupNames.contains(groupName)) {
				checkedGroupNames.add(groupName);
			}
		}

		public void uncheckGroup(String groupName) {
			if (checkedGroupNames.contains(groupName)) {
				checkedGroupNames.remove(groupName);
			}
		}

		public IDGroup getGroup(String groupName) {
			return groupMap.get(groupName);
		}

		public ArrayList<ElementTableItem> getAllAllowedItems() {
			// returns sorted list of all allowed items
			ArrayList<ElementTableItem> itemsFromCheckedGroups = getSortedItemsFromCheckedGroups(checkedGroupNames
					.toArray(new String[checkedGroupNames.size()]));
			Collections.sort(itemsFromCheckedGroups);
			return itemsFromCheckedGroups;
		}

		public void putGroup(String groupName, IDGroup group) {
			groupMap.put(groupName, group);
		}

		/**
		 * Returns all ids from checked groups
		 */
		public ArrayList<ElementTableItem> getSortedItemsFromCheckedGroups(
				String[] checkedGroups) {
			ArrayList<ElementTableItem> items = new ArrayList<ElementTableItem>();
			if (checkedGroups.length == 0) {
				checkedGroups = IDGroupContainer.this.getGroupNames();
			}

			for (int i = 0; i < checkedGroups.length; i++) {
				IDGroup group = getGroup(checkedGroups[i]);
				SortedSet<ElementTableItem> allIds = group.getAll();
				items.addAll(allIds);
			}
			return items;
		}

		public String[] getGroupNames() {
			Set<String> groupNameSet = groupMap.keySet();
			String[] groupNames = groupNameSet.toArray(new String[groupNameSet
					.size()]);
			return groupNames;
		}
	}

	/**
	 * Container of Item ids
	 * 

	 */
	public class IDGroup {
		public IDGroup(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		/**
		 * Group name
		 */
		private String name;

		private SortedSet<ElementTableItem> groupItems = new TreeSet<ElementTableItem>();

		public void add(ElementTableItem id) {
			groupItems.add(id);
		}

		public void addAll(SortedSet<ElementTableItem> idSet) {
			groupItems.addAll(idSet);
		}

		public SortedSet<ElementTableItem> getAll() {
			return groupItems;
		}
	}

	class ElementTableItem implements Comparable {
		private String name;

		private String iconName;

		private String id;

		private String resourcePath;

		private boolean isSkinned;

		private String specialEditing;

		private ImageData icon;

		public ImageData getIcon() {
			return icon;
		}

		public void setIcon(ImageData icon) {
			this.icon = icon;
		}

		public String getSpecialEditing() {
			return (null == specialEditing) ? "" : specialEditing;
		}

		public void setSpecialEditing(String specialEditing) {
			this.specialEditing = specialEditing;
		}

		public String getIconName() {
			return iconName;
		}

		public void setIconName(String iconName) {
			this.iconName = iconName;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public boolean isSkinned() {
			return isSkinned;
		}

		public void setSkinned(boolean isSkinned) {
			this.isSkinned = isSkinned;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getResourcePath() {
			return resourcePath;
		}

		public void setResourcePath(String resourcePath) {
			this.resourcePath = resourcePath;
		}

		public int compareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return this.getName().compareTo(tmp.getName());
			} else if (o instanceof String) {
				return this.getName().compareTo((String) o);
			} else {
				throw new ClassCastException();
			}
		}

		public int idCompareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return this.getId().compareTo(tmp.getId());
			} else if (o instanceof String) {
				return this.getId().compareTo((String) o);
			} else {
				throw new ClassCastException();
			}
		}

		public int resourcePathCompareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return this.getResourcePath().compareTo(tmp.getResourcePath());
			} else if (o instanceof String) {
				return this.getResourcePath().compareTo((String) o);
			} else {
				throw new ClassCastException();
			}
		}

		public int specialEditingCompareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return this.getSpecialEditing().compareTo(
						tmp.getSpecialEditing());
			} else if (o instanceof String) {
				return this.getSpecialEditing().compareTo((String) o);
			} else {
				throw new ClassCastException();
			}
		}

		public int skinnedCompareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return new Boolean(this.isSkinned()).compareTo(tmp.isSkinned());
			} else {
				throw new ClassCastException();
			}
		}

	}

	class SearchTableSorter extends ViewerSorter {
		public static final int NAME = 0;

		public static final int ID = 1;

		public static final int RESOURCE_PATH = 2;

		public static final int SPECIAL_EDITING = 3;

		public static final int SKINNED = 4;

		private int criteria = 0;

		public SearchTableSorter(int criteria) {
			super();
			this.criteria = criteria;
		}

		public int compare(Viewer viewer, Object o1, Object o2) {

			ElementTableItem task1 = (ElementTableItem) o1;
			ElementTableItem task2 = (ElementTableItem) o2;

			switch (criteria) {
			case NAME:
				return task1.compareTo(task2);
			case ID:
				return task1.idCompareTo(task2);
			case RESOURCE_PATH:
				return task1.resourcePathCompareTo(task2);
			case SPECIAL_EDITING:
				return task1.specialEditingCompareTo(task2);
			case SKINNED:
				return task1.skinnedCompareTo(task2);
			default:
				return 0;
			}
		}

	}

}
