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
package com.nokia.tools.s60.views;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.Page;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentStructureAdapter;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.ShowInComponentViewAction;
import com.nokia.tools.s60.editor.actions.ShowInLayersViewAction;
import com.nokia.tools.s60.editor.actions.ShowInResourceViewAction;
import com.nokia.tools.s60.editor.actions.ShowPropertiesAction;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.screen.ui.views.ViewIDs;
import com.nokia.tools.theme.content.ContentDataAdapter;

public class SearchViewPage extends Page {
	private static final Color TABLE_SECOND_BG_COLOR = new Color(null, 250,
			250, 250);

	private static String NAME_COLUMN = "name";

	private static String ID_COLUMN = "id";

	private static String INFO_COLUMN = "info";

	private static String RESOURCE_PATH_COLUMN = "resource_path";

	private static String SPECIAL_EDITING_COLUMN = "special_editing";

	private static List<String> tableColumns;

	private static List<String> tableColumnsSortingOrder;
	static {
		tableColumns = new ArrayList<String>();
		tableColumnsSortingOrder = new ArrayList<String>();

		tableColumns.add(NAME_COLUMN);
		tableColumns.add(ID_COLUMN);
		tableColumns.add(INFO_COLUMN);
		tableColumns.add(RESOURCE_PATH_COLUMN);
		tableColumns.add(SPECIAL_EDITING_COLUMN);

		tableColumnsSortingOrder.add(ID_COLUMN);
		tableColumnsSortingOrder.add(NAME_COLUMN);
	}

	private IEditorPart sourceEditor;

	private Adapter refreshAdapter = new ContentDataAdapter() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.theme.content.ContentDataAdapter#contentDataModified(com.nokia.tools.content.core.IContentData)
		 */
		@Override
		protected void contentDataModified(IContentData modified) {
			resourceModified(modified);
		}
	};

	public IEditorPart getSourceEditor() {
		return sourceEditor;
	}

	public SearchViewPage(IEditorPart sourceEd) {
		this.sourceEditor = sourceEd;
	}

	Composite composite = null;

	@Override
	public void createControl(Composite parent) {
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
		// SearchAction.HLP_CTX);
		composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		GridLayout gl = new GridLayout();
		gl.marginTop = gl.marginBottom = gl.marginLeft = gl.marginRight = gl.marginWidth = gl.marginHeight = 0;
		composite.setLayout(gl);

		composite.setBackground(ColorConstants.blue);
		createCompositeArea(composite);

		createActions();
		createToolbar();

		IContentAdapter adapter = (IContentAdapter) sourceEditor
				.getAdapter(IContentAdapter.class);
		if (adapter != null) {
			IContent[] cnt = adapter.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);
			EditObject resource = (EditObject) root
					.getAdapter(EditObject.class);
			resource.eAdapters().add(refreshAdapter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.Page#dispose()
	 */
	@Override
	public void dispose() {
		IContentAdapter adapter = (IContentAdapter) sourceEditor
				.getAdapter(IContentAdapter.class);
		if (adapter != null) {
			IContent[] cnt = adapter.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);
			EditObject resource = (EditObject) root
					.getAdapter(EditObject.class);
			resource.eAdapters().remove(refreshAdapter);
		}
		super.dispose();
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		pattern.setFocus();

	}

	Action switchLayoutAction = null;

	Text pattern;

	DecoratedField decoratedPattern;

	Table items;

	TableViewer tableViewer;

	ListViewer listViewer;

	private SashForm fTypeMethodsSplitter;

	private Composite c1;

	boolean isHorizontalLayout = false;

	int verticalSearchInput = 0;

	int horizontalSearchInput = 0;

	int patternHorizontalLayoutWidth = 100;

	int patternVerticalLayoutWidth = 200;

	private Button searchButton;

	private ArrayList<ElementTableItem> filteredInput = null;

	private IStructuredSelection previousSelection = null;

	private ImageDescriptor horizontalLayoutImage, verticalLayoutImage;

	/**
	 * Create toolbar.
	 */
	private void createToolbar() {
		IToolBarManager mgr = getSite().getActionBars().getToolBarManager();
		mgr.add(switchLayoutAction);

	}

	private void createActions() {
		switchLayoutAction = new Action("Switch layout...") {
			public void run() {
				isHorizontalLayout = !isHorizontalLayout;
				recalculateLayout();
			}

		};

		verticalLayoutImage = S60WorkspacePlugin
				.getImageDescriptor("icons/verticalLayout.png");
		horizontalLayoutImage = S60WorkspacePlugin
				.getImageDescriptor("icons/horizontalLayout.png");

		switchLayoutAction.setImageDescriptor(horizontalLayoutImage);
		switchLayoutAction.setToolTipText("Switch layout");
	}

	private void recalculateLayout() {

		if (isHorizontalLayout) {

			switchLayoutAction.setImageDescriptor(verticalLayoutImage);

			c1.setLayout(new GridLayout(1, false));
			c1.setLayoutData(new GridData(GridData.FILL_BOTH));
			FormData data = (FormData) pattern.getLayoutData();
			data.width = patternHorizontalLayoutWidth;

			int widthAll = fTypeMethodsSplitter.getSize().x;
			int patternMargin = ((GridLayout) c1.getLayout()).marginHeight;
			int spacing = ((GridLayout) c1.getLayout()).horizontalSpacing;

			horizontalSearchInput = (int) ((patternHorizontalLayoutWidth + 4
					* patternMargin + 2 * spacing) * 100 / widthAll);
			fTypeMethodsSplitter.setWeights(new int[] { horizontalSearchInput,
					100 - horizontalSearchInput });
			fTypeMethodsSplitter.setOrientation(SWT.HORIZONTAL);

		} else {

			switchLayoutAction.setImageDescriptor(horizontalLayoutImage);

			fTypeMethodsSplitter.setOrientation(SWT.VERTICAL);

			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 3;
			c1.setLayout(layout);
			c1.setLayoutData(new GridData(GridData.FILL_BOTH));
			FormData data = (FormData) pattern.getLayoutData();
			data.width = patternVerticalLayoutWidth;

			int heightAll = fTypeMethodsSplitter.getSize().y;
			int verticalSearchInputRatio = (verticalSearchInput * 100)
					/ heightAll;
			fTypeMethodsSplitter.setWeights(new int[] {
					verticalSearchInputRatio, 100 - verticalSearchInputRatio });

		}
		SearchViewPage.this.composite.layout(true, true);
	}

	public void createCompositeArea(Composite parent) {
		Composite dialogArea = parent;
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;

		dialogArea.setLayout(gridLayout);

		fTypeMethodsSplitter = new SashForm(dialogArea, SWT.VERTICAL);
		fTypeMethodsSplitter.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTypeMethodsSplitter.setLayout(new GridLayout());
		fTypeMethodsSplitter.setVisible(true);

		fTypeMethodsSplitter.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				SashForm splitter = (SashForm) e.widget;
				if (splitter.getOrientation() == SWT.HORIZONTAL) {
					int widthAll = fTypeMethodsSplitter.getSize().x;
					int patternMargin = ((GridLayout) c1.getLayout()).marginHeight;
					int spacing = ((GridLayout) c1.getLayout()).horizontalSpacing;
					if (widthAll > 0) {
						if (((patternHorizontalLayoutWidth + 4 * patternMargin + 2 * spacing)) <= widthAll) {
							horizontalSearchInput = (int) ((patternHorizontalLayoutWidth
									+ 4 * patternMargin + 2 * spacing) * 100 / widthAll);
						} else {
							horizontalSearchInput = 100;
						}
					} else {
						horizontalSearchInput = 10;
					}
					fTypeMethodsSplitter
							.setWeights(new int[] { horizontalSearchInput,
									100 - horizontalSearchInput });

				} else if (splitter.getOrientation() == SWT.VERTICAL) {
					int heightAll = fTypeMethodsSplitter.getSize().y;
					int verticalSearchInputRatio = 0;
					if (heightAll > 0) {
						if (verticalSearchInput <= heightAll) {
							verticalSearchInputRatio = (verticalSearchInput * 100)
									/ heightAll;
						} else {
							verticalSearchInputRatio = 100;
						}

					} else {
						verticalSearchInputRatio = 20;
					}
					fTypeMethodsSplitter.setWeights(new int[] {
							verticalSearchInputRatio,
							100 - verticalSearchInputRatio });

				}
			}
		});

		c1 = new Composite(fTypeMethodsSplitter, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 3;
		layout.verticalSpacing = 0;
		c1.setLayout(layout);
		c1.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite c2 = new Composite(fTypeMethodsSplitter, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		c2.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		c2.setLayoutData(data);

		tableViewer = new TableViewer(c2, SWT.MULTI | SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);

		items = tableViewer.getTable();

		data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = SWT.BEGINNING;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;

		items.setLayoutData(data);

		// finds all component names in a system
		initSearchInput();

		decoratedPattern = new DecoratedField(c1, SWT.BORDER,
				new TextControlCreator());
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		FieldDecoration standardDecoration = registry
				.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		standardDecoration
				.setDescription("Press Ctrl+Space to see example search inputs and history");
		decoratedPattern.addFieldDecoration(standardDecoration, SWT.TOP
				| SWT.LEFT, true);
		initSuggestionsAndHistory();

		pattern = (Text) decoratedPattern.getControl();
		pattern.setToolTipText("* = any string");
		pattern.addFocusListener(new FocusAdapter() {
			// this is to ensure that after opening when user selects pattern
			// input can view all items
			public void focusGained(FocusEvent event) {
				initSearchInput();

				tableViewer.setInput(filteredInput);
				if (tableViewer.getSelection() == null
						&& tableViewer.getElementAt(0) != null) {
					tableViewer.setSelection(new StructuredSelection(
							tableViewer.getElementAt(0)));
				}

			}
		});
		installContentProposalAdapter(pattern, new TextContentAdapter());

		pattern.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateInput();
			}

		});

		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ARROW_DOWN) {
					tableViewer.scrollDown(0, 1);
					tableViewer.getTable().setFocus();
				} else if (event.keyCode == SWT.CR
						|| event.keyCode == SWT.KEYPAD_CR) {
					addToSuggestionsAndHistory(pattern.getText());
					updateInput();
				}

			}
		});

		FormData formData = (FormData) pattern.getLayoutData();
		formData.width = patternVerticalLayoutWidth;

		searchButton = new Button(c1, SWT.PUSH);
		searchButton.setText("&Go to Element");
		searchButton.setLayoutData(new GridData());
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				initSearchInput();
				IStructuredSelection selection = (IStructuredSelection) tableViewer
						.getSelection();
				ElementTableItem selectedElement = (ElementTableItem) selection
						.getFirstElement();
				
				if(null != selectedElement)
					selectAndFocusElement(selectedElement);
			}
		});

		int heightAll = fTypeMethodsSplitter.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).y;
		int heightSearchButton = searchButton.computeSize(SWT.DEFAULT,
				SWT.DEFAULT).y;
		int buttonMargin = ((GridLayout) c1.getLayout()).marginHeight;
		int spacing = ((GridLayout) c1.getLayout()).verticalSpacing;
		verticalSearchInput = (int) (heightSearchButton + 2 * buttonMargin + spacing);

		int verticalSearchInputRatio = (verticalSearchInput * 100) / heightAll;
		fTypeMethodsSplitter.setWeights(new int[] { verticalSearchInputRatio,
				100 - verticalSearchInputRatio });

		tableViewer
				.setSorter(new SearchTableSorter(SearchTableSorter.COLUMN_0));
		items.setLinesVisible(true);
		items.setHeaderVisible(true);

		// 1st column
		TableColumn column = new TableColumn(items, SWT.CENTER, 0);
		column.setText("Name");
		column.setWidth(200);
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateSorter(SearchTableSorter.COLUMN_0, 0);
			}
		});

		// 2nd column
		column = new TableColumn(items, SWT.LEFT, 1);
		column.setText("ID");
		column.setWidth(180);
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateSorter(SearchTableSorter.COLUMN_1, 1);
			}
		});

		// 3rd column
		column = new TableColumn(items, SWT.LEFT, 2);
		column.setText("Info");
		column.setWidth(100);
		column.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSorter(SearchTableSorter.COLUMN_2, 2);
			}
		});

		// 3rd column
		column = new TableColumn(items, SWT.LEFT, 3);
		column.setText("Resource path");
		column.setWidth(200);
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateSorter(SearchTableSorter.COLUMN_3, 3);
			}
		});

		// 4th column
		column = new TableColumn(items, SWT.LEFT, 4);
		column.setText("Special editing");
		column.setWidth(100);
		column.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				updateSorter(SearchTableSorter.COLUMN_4, 4);
			}
		});
		items.setSortColumn(items.getColumn(0));
		items.setSortDirection(SWT.UP);
		// 5th column
		/*
		 * column = new TableColumn(items, SWT.LEFT, );
		 * column.setText("Skinned"); column.setWidth(60);
		 * column.addSelectionListener(new SelectionAdapter() { public void
		 * widgetSelected(SelectionEvent e) { tableViewer.setSorter(new
		 * SearchTableSorter( SearchTableSorter.SKINNED)); } });
		 */

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
					return allAllowedItems.toArray();
				}
				return super.getElements(inputElement);
			}

			@Override
			public void dispose() {
				super.dispose();
			}

		});

		tableViewer.setInput(container);

		tableViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) tableViewer
								.getSelection();
						if (previousSelection == null
								|| selection.getFirstElement() != previousSelection
										.getFirstElement()) {
							previousSelection = selection;
						}
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
				
				if(null != selectedElement)
					selectAndFocusElement(selectedElement);
			}

		});
	}

	/**
	 * Refresh input
	 * 
	 */
	private void updateInput() {
		initSearchInput();
		filteredInput = (ArrayList<ElementTableItem>) filterInput(container
				.getAllAllowedItems(), pattern.getText());
		tableViewer.setInput(filteredInput);
		if (tableViewer.getElementAt(0) != null) {
			tableViewer.setSelection(new StructuredSelection(tableViewer
					.getElementAt(0)));
		}
	}
	
	/**
	 * Refresh input used while changing resolution or platform
	 * 
	 */
	public void refresh() {
		container=null;
		initSearchInput();
		filteredInput = (ArrayList<ElementTableItem>) filterInput(container
				.getAllAllowedItems(), pattern.getText());
		tableViewer.setInput(filteredInput);
		if (tableViewer.getElementAt(0) != null) {
			tableViewer.setSelection(new StructuredSelection(tableViewer
					.getElementAt(0)));
		}
	}

	private void updateSorter(int sorterId, int tableColumn) {
		if (((SearchTableSorter) tableViewer.getSorter()).getCriteria() == sorterId) {
			if (items.getSortDirection() == SWT.DOWN) {
				items.setSortDirection(SWT.UP);
			} else {
				items.setSortDirection(SWT.DOWN);
			}
			tableViewer.setSorter(new SearchTableSorter(sorterId));
		} else {
			items.setSortColumn(items.getColumn(tableColumn));
			items.setSortDirection(SWT.UP);
			tableViewer.setSorter(new SearchTableSorter(sorterId));
		}
	}

	private void initSearchInput() {
		
		if (container == null) {
			createInput();

			if (container != null) {
				filteredInput = filterInput(container.getAllAllowedItems(), "");
			}
		}
	}

	public void createMinimizedLayout() {

	}

	public void createMaximizedLayout() {

	}


	private String handleUnusedSpecialSymbols(String patternInput) {
		patternInput = patternInput.replaceAll("\\\\", "\\\\\\\\");
		patternInput = patternInput.replaceAll("\\(", "\\\\(");
		patternInput = patternInput.replaceAll("\\)", "\\\\)");
		patternInput = patternInput.replaceAll("\\{", "\\\\}");
		patternInput = patternInput.replaceAll("\\{", "\\\\}");
		patternInput = patternInput.replaceAll("\\[", "\\\\[");
		patternInput = patternInput.replaceAll("\\]", "\\\\]");
		patternInput = patternInput.replaceAll("\\+", "\\\\+");
		patternInput = patternInput.replaceAll("\\|", "\\\\|");
		patternInput = patternInput.replaceAll("\\?", "\\\\?");
		patternInput = patternInput.replaceAll("\\$", "\\\\\\$");

		return patternInput;
	}

	public ArrayList<ElementTableItem> filterInput(
			ArrayList<ElementTableItem> dataToFilter, String pattern) {

		ArrayList<ElementTableItem> filteredInput = new ArrayList<ElementTableItem>();

		pattern = pattern.replaceAll("\\*", ".*");
		pattern = handleUnusedSpecialSymbols(pattern);

		pattern = pattern + ".*";
		for (int i = 0; i < dataToFilter.size(); i++) {

			Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

			Matcher m = null;

			for (String property : tableColumns) {
				String testedProperty = dataToFilter.get(i).getProperty(
						property);
				m = p.matcher(testedProperty);
				if (m.matches()) {
					filteredInput.add(dataToFilter.get(i));
					break;
				}

			}

		}
		return filteredInput;
	}

	class SearchDataLabelProvider extends LabelProvider implements
			ITableLabelProvider, IColorProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			ElementTableItem tmpItem = (ElementTableItem) element;
			String result = null;
			switch (columnIndex) {
			case 0:
				result = tmpItem.getProperty(NAME_COLUMN);
				break;
			case 1:
				result = tmpItem.getProperty(ID_COLUMN);
				break;

			case 2:
				result = tmpItem.getProperty(INFO_COLUMN);
				break;
			case 3:
				result = tmpItem.getProperty(RESOURCE_PATH_COLUMN);
				break;

			case 4:
				result = tmpItem.getProperty(SPECIAL_EDITING_COLUMN);
				break;
			default:
				break;
			}
			return result;
		}

		public Color getBackground(Object element) {
			ElementTableItem tmpItem = (ElementTableItem) element;
			if (filteredInput.indexOf(tmpItem) % 2 == 0) {
				return TABLE_SECOND_BG_COLOR;
				// return ColorConstants.gray;
			} else {
				return ColorConstants.white;
			}
		}

		public Color getForeground(Object element) {
			ElementTableItem tmpItem = (ElementTableItem) element;
			if (tmpItem.isSet()) {
				return ColorConstants.blue;
			}
			return ColorConstants.black;
		}
	}

	// Select and focus element in the Resource view.
	private void selectAndFocusElement(ElementTableItem element) {
		String elementName = element.getProperty(NAME_COLUMN);
		String id = element.getProperty(ID_COLUMN);
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();

		Series60EditorPart sep = (Series60EditorPart) activeEd
				.getAdapter(Series60EditorPart.class);
		if (sep != null) {
			IContent[] cnt = sep.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);

			// IContentData data = root.findByName(elementName);

			IContentData data = root.findById(id);

			if (data != null) {

				/*
				 * if (!isLeafElement(data)) { data = getFirstChild(data); }
				 */
				IWorkbenchPage page = sep.getSite().getWorkbenchWindow()
						.getActivePage();

				try {
					// sep.selectReveal(new StructuredSelection(sel));
					sep.selectReveal(new StructuredSelection(data));
				} catch (Exception e) {
					// element does not have preview screen
				}

				ActionRegistry registry = (ActionRegistry) sep
						.getAdapter(ActionRegistry.class);
				if (registry == null) {
					return;
				}
				if (page.findView(ViewIDs.RESOURCE_VIEW2_ID) != null) {

					ShowInResourceViewAction act = (ShowInResourceViewAction) registry
							.getAction(ShowInResourceViewAction.ID);
					act.doRun(data);
				}

				if (page.findView(ViewIDs.ICON_VIEW_ID) != null) {

					ShowInComponentViewAction act = (ShowInComponentViewAction) registry
							.getAction(ShowInComponentViewAction.ID);
					act.doRun(data);
				}

				if (page.findView(IS60IDEConstants.ID_LAYERS_VIEW) != null) {
					ShowInLayersViewAction act2 = (ShowInLayersViewAction) registry
							.getAction(ShowInLayersViewAction.ID);
					act2.doRun(data);
				}

				IViewPart propertiesView = page
						.findView("org.eclipse.ui.views.PropertySheet");
				if (propertiesView != null) { 
					ShowPropertiesAction act3 = (ShowPropertiesAction) registry
							.getAction(ShowPropertiesAction.ID);
					act3.doRun(data);
				}
			}
		}
	}

	// Creating input for table viewer
	private void createInput() {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
		if (activeEd != null) {
			IContentAdapter adapter = (IContentAdapter) activeEd
					.getAdapter(IContentAdapter.class);
			if (adapter != null) {
				IContent[] cnt = adapter.getContents();
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
					IContentStructureAdapter adapter = (IContentStructureAdapter) n3
							.getAdapter(IContentStructureAdapter.class);
					for (IContentData n4 : adapter.getChildren()) {
						// if(n4 instanceof ThemeBasicData){
						// ThemeBasicData data= (ThemeBasicData)n4;
						// data.supportsPlatform("");
						//							
						// }

						// ((ThemeContent)
						// n1.getRoot()).supportsPlatform(DevicePlatform.S60_2_0,
						// n4.getId());
						ElementTableItem eti = new ElementTableItem(
								tableColumns, tableColumnsSortingOrder);
						eti.setProperty(NAME_COLUMN, n4.getName());
						eti.setProperty(ID_COLUMN, n4.getId());
						eti.setProperty(RESOURCE_PATH_COLUMN, n2.getName()
								+ "->" + n3.getName());
						IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) n4
								.getAdapter(IToolBoxAdapter.class);
						if (toolBoxAdapter != null
								&& toolBoxAdapter.isMultipleLayersSupport()) {
							eti
									.setProperty(
											SPECIAL_EDITING_COLUMN,
											(eti
													.getProperty(SPECIAL_EDITING_COLUMN) + "layers/effects"));
						}

						IMediaFileAdapter mediaFileAdapter = (IMediaFileAdapter) n4
								.getAdapter(IMediaFileAdapter.class);

						if (mediaFileAdapter != null
								&& mediaFileAdapter.isSound()) {
							eti
									.setProperty(
											SPECIAL_EDITING_COLUMN,
											(eti
													.getProperty(SPECIAL_EDITING_COLUMN) + "sound"));
						}

						IImageAdapter imageAdapter = (IImageAdapter) n4
								.getAdapter(IImageAdapter.class);
						if (imageAdapter != null && imageAdapter.isAnimated()) {
							eti
									.setProperty(
											SPECIAL_EDITING_COLUMN,
											(eti
													.getProperty(SPECIAL_EDITING_COLUMN) + "animated image"));

						}

						ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) n4
								.getAdapter(ISkinnableEntityAdapter.class);
						if (skAdapter != null) {
							try {
								Map attributes = skAdapter
										.getLayerAttributes(0);
								if (attributes!= null) {
									String fileName = (String) attributes
											.get("filename");
									if (null != fileName) {
										File f = new File(fileName);
										eti.setProperty(INFO_COLUMN, f.getName());
									}
								}
							} catch (NullPointerException npe) {
								
							}

						}

						IColorAdapter colorAdapter = (IColorAdapter) n4
								.getAdapter(IColorAdapter.class);
						if (colorAdapter != null) {
							eti
									.setProperty(
											SPECIAL_EDITING_COLUMN,
											(eti
													.getProperty(SPECIAL_EDITING_COLUMN) + "color"));
							eti.setProperty(INFO_COLUMN, ColorUtil
									.asHashString(colorAdapter.getColor()));
						}
						
						
						//if (skAdapter != null && skAdapter.isNinePiece()) {
						if (skAdapter != null && skAdapter.isMultiPiece()) {
							String searchViewText = skAdapter.getMultiPieceSearchViewText();
							eti
									.setProperty(
											SPECIAL_EDITING_COLUMN,
											(eti
													.getProperty(SPECIAL_EDITING_COLUMN) + searchViewText));
													//+ "nine piece"));

						}

						if (skAdapter != null && skAdapter.isSkinned()) {
							eti.setSet(true);
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

	/**
	 * Container of IDGroups
	 * 
	 */
	class IDGroupContainer {
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
			// if none of the groups is checked - it is the same as all groups
			// were checked
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

		public void setPropertyToNewValue(String itemId, String property,
				String propertyValue) {
			for (IDGroup grp : groupMap.values()) {

				SortedSet<ElementTableItem> items = grp.getAll();
				for (ElementTableItem item : items) {
					if (item.getProperty(ID_COLUMN).equals(itemId)) {
						item.setProperty(property, propertyValue);
						break;
					}
				}
			}
		}
	}

	/**
	 * Container of Item ids
	 * 
	 */
	class IDGroup {
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

	class ElementTableItem implements Comparable<ElementTableItem> {

		private boolean isSet;

		Map<String, String> propertiesMap = new HashMap<String, String>();

		List<String> propertyNamesSortingPriorityList = new ArrayList<String>();

		public ElementTableItem(List<String> propertyNames,
				List<String> propertyNamesSortingPriorityList) {
			for (String property : propertyNames) {
				if (property != null && property.length() > 0) {
					propertiesMap.put(property, "");
				}
			}
			this.propertyNamesSortingPriorityList = propertyNamesSortingPriorityList;
		}

		public void setProperty(String property, String propertyValue) {
			if (propertiesMap.containsKey(property)) {
				propertiesMap.put(property, propertyValue);
			} else if ("isSkinned".equals(property)) {
				if ("T".equals(propertyValue)) {
					setSet(true);
				} else if ("F".equals(propertyValue)) {
					setSet(false);
				}
			}

		}

		public String getProperty(String propertyName) {
			return propertiesMap.get(propertyName);
		}

		public boolean isSet() {
			return isSet;
		}

		public void setSet(boolean isSkinned) {
			this.isSet = isSkinned;
		}

		public int compareTo(ElementTableItem item) {
			for (String property : propertyNamesSortingPriorityList) {
				if (this.getProperty(property) != null) {
					if (this.getProperty(property).equals(
							item.getProperty(property))) {
						continue; // if the first match fix go to another
						// property
					} else {
						return this.getProperty(property).compareTo(
								item.getProperty(property));
					}
				} else {
					return "".compareTo(item.getProperty(property));
				}
			}

			return 0;
		}

		public int compareTo(String item) {

			for (String property : propertyNamesSortingPriorityList) {
				if (this.getProperty(property) != null) {
					if (this.getProperty(property).equals(item)) {
						continue; // if the first match fix go to another
						// property
					} else {
						return this.getProperty(property).compareTo(item);
					}
				} else {
					return "".compareTo(item);
				}
			}

			return 0;
		}

		public int compareToProperty(Object o, String propertyName) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;

				return this.getProperty(propertyName).compareTo(
						tmp.getProperty(propertyName));

			} else if (o instanceof String) {
				return this.getProperty(propertyName).compareTo((String) o);
			} else {
				throw new ClassCastException();
			}
		}

		public int setCompareTo(Object o) {
			if (o instanceof ElementTableItem) {
				ElementTableItem tmp = (ElementTableItem) o;
				return new Boolean(this.isSet()).compareTo(tmp.isSet());
			} else {
				throw new ClassCastException();
			}
		}

	}

	class SearchTableSorter extends ViewerSorter {
		public static final int COLUMN_0 = 0;

		public static final int COLUMN_1 = 1;

		public static final int COLUMN_2 = 2;

		public static final int COLUMN_3 = 3;

		public static final int COLUMN_4 = 4;

		public static final int COLUMN_5 = 5;

		private int criteria = 0;

		public SearchTableSorter(int criteria) {
			super();
			this.criteria = criteria;
		}

		public int compare(Viewer viewer, Object o1, Object o2) {

			ElementTableItem task1 = (ElementTableItem) o1;
			ElementTableItem task2 = (ElementTableItem) o2;
			if (items.getSortDirection() == SWT.DOWN) {
				// for down sort direction switch tasks to get inverted result
				ElementTableItem taskTemp = task1;
				task1 = task2;
				task2 = taskTemp;
			}
			switch (criteria) {
			case COLUMN_0:
				return task1.compareToProperty(task2, NAME_COLUMN);
			case COLUMN_1:
				return task1.compareToProperty(task2, ID_COLUMN);
			case COLUMN_2:
				return task1.compareToProperty(task2, INFO_COLUMN);
			case COLUMN_3:
				return task1.compareToProperty(task2, RESOURCE_PATH_COLUMN);
			case COLUMN_4:
				return task1.compareToProperty(task2, SPECIAL_EDITING_COLUMN);
			default:
				return 0;
			}
		}

		public int getCriteria() {
			return criteria;
		}

		public void setCriteria(int criteria) {
			this.criteria = criteria;
		}
	}

	private String triggerKey = "Ctrl+Space";

	private void installContentProposalAdapter(Control control,
			IControlContentAdapter contentAdapter) {

		KeyStroke keyStroke;
		char[] autoActivationCharacters = null;

		try {
			keyStroke = KeyStroke.getInstance(triggerKey);
		} catch (ParseException e) {
			keyStroke = KeyStroke.getInstance(SWT.F10);
		}

		ContentProposalAdapter adapter = new ContentProposalAdapter(control,
				contentAdapter, getContentProposalProvider(), keyStroke,
				autoActivationCharacters);
		adapter.setPropagateKeys(true);
		adapter
				.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
	}

	private void addToSuggestionsAndHistory(String searchHistoryItem) {
		if (searchHistoryItem != null && !("".equals(searchHistoryItem))) {
			if (suggestionsAndHistory.size() < suggestionsAndHistorySize) {
				suggestionsAndHistory.add(0, searchHistoryItem);
			} else {
				suggestionsAndHistory.remove(suggestionsAndHistorySize - 1);
				suggestionsAndHistory.add(0, searchHistoryItem);
			}
		}
	}

	private void initSuggestionsAndHistory() {
		suggestionsAndHistory.add("*background");
		suggestionsAndHistory.add("0x000000");
		suggestionsAndHistory.add("calendar");
		suggestionsAndHistory.add("*text");
		suggestionsAndHistory.add("layer*");
	}

	int suggestionsAndHistorySize = 10;

	private ArrayList<String> suggestionsAndHistory = new ArrayList<String>();

	
	private IContentProposalProvider getContentProposalProvider() {
		return new IContentProposalProvider() {
			public IContentProposal[] getProposals(String contents, int position) {
				IContentProposal[] proposals = new IContentProposal[suggestionsAndHistory
						.size()];
				for (int i = 0; i < suggestionsAndHistory.size(); i++) {
					final String currentItem = suggestionsAndHistory.get(i);
					proposals[i] = new IContentProposal() {
						public String getContent() {
							return currentItem;
						}

						public String getLabel() {
							return currentItem;
						}

						public String getDescription() {

							return MessageFormat
									.format(
											"The table will be filtered using following string: \"{0}\". The filter is applied to all columns",
											new Object[] { currentItem });

						}

						public int getCursorPosition() {
							return currentItem.length();
						}
					};
				}
				return proposals;
			}
		};
	}

	public void resourceModified(IContentData data) {
		if (container != null) {
			String info = "";
			if (data != null) {
				ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				if (skAdapter != null) {

					try {
						Map attributes = skAdapter.getLayerAttributes(0);
						if(attributes != null){
							String fileName = (String) attributes.get("filename");
							if (null != fileName) {
								File f = new File(fileName);
								info = f.getName();
							}
						}
					} catch (NullPointerException npe) {
						
					}
					// uses id instead of name because name is not unique
					if (skAdapter.isSkinned()) {
						container.setPropertyToNewValue(data.getId(),
								"isSkinned", "T");
					} else {
						container.setPropertyToNewValue(data.getId(),
								"isSkinned", "F");
					}

				}

				IColorAdapter colorAdapter = (IColorAdapter) data
						.getAdapter(IColorAdapter.class);
				if (colorAdapter != null) {
					info = ColorUtil.asHashString(colorAdapter.getColor());
				}

				container.setPropertyToNewValue(data.getName(), "info", info);

			}
		}
		if (pattern.isDisposed()) {
			filteredInput = (ArrayList<ElementTableItem>) filterInput(container
					.getAllAllowedItems(), "");
		} else {
			filteredInput = (ArrayList<ElementTableItem>) filterInput(container
					.getAllAllowedItems(), pattern.getText());
		}
		if (tableViewer.getContentProvider() != null) {
			tableViewer.setInput(filteredInput);
		}
	}
}