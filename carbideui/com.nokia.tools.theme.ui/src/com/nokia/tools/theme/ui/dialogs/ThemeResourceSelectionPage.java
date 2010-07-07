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
package com.nokia.tools.theme.ui.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.dialogs.IThemeResourcePageManager;
import com.nokia.tools.screen.ui.dialogs.ResourceResult;
import com.nokia.tools.screen.ui.dialogs.WizardMessages;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.theme.core.IContentLabelProvider;
import com.nokia.tools.theme.ui.Activator;
import com.nokia.tools.ui.dialog.IResourceSelectionPage;
import com.nokia.tools.ui.dialog.TableImagePaintListener;

public class ThemeResourceSelectionPage extends IResourceSelectionPage.Adapter {
	private static final String ID = ThemeResourceSelectionPage.class.getName();

	private static final int ORDER_NAME = 0;

	private static final int ORDER_ID = 1;

	private IContentData[] themeElements;

	private Text pattern;

	private TableViewer themeTableViewer;

	private TableColumn colName, colId;

	private IFilter filter;

	private String triggerKey = "Ctrl+Space";

	private int suggestionsAndHistorySize = 10;

	private double columnRatio = 0.6;

	boolean updateColumnSizes = false;

	private ArrayList<String> suggestionsAndHistory = new ArrayList<String>();

	public ThemeResourceSelectionPage() {
		setIconImageDescriptor(Activator.imageDescriptorFromPlugin(
				Activator.PLUGIN_ID, "icons/theme.gif"));
		setTitle(Messages.ResourceSelectionDialog_Theme_Tab_Text);
		setId(ID);
	}

	/**
	 * @return the filter
	 */
	public IFilter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(IFilter filter) {
		this.filter = filter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#createImage(java.lang.Object,
	 *      int, int, boolean)
	 */
	public Image createImage(Object data, int width, int height,
			boolean keepAspectRatio) {
		if (data instanceof IContentData) {
			IContentData item = (IContentData) data;
			IContentLabelProvider adapter = (IContentLabelProvider) item
					.getAdapter(IContentLabelProvider.class);
			if (adapter != null) {
				return adapter.getImageDescriptor(item, width, height)
						.createImage();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#createPage(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPage(Composite parent) {
		// finds all component names in a system
		createInput();
		GridLayout parentLayout = (GridLayout) parent.getLayout();
		parentLayout.numColumns = 1;
		parent.setLayout(parentLayout);
		Composite filterArea = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		filterArea.setLayoutData(gd);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 7;
		filterArea.setLayout(gl);

		final Label lblFilter = new Label(filterArea, SWT.NONE);
		lblFilter.setText(WizardMessages.ResourceSelectionDialog_FilterLabel);

		pattern = new Text(filterArea, SWT.BORDER);
		ControlDecoration filterDecoration = new ControlDecoration(pattern,
				SWT.TOP | SWT.LEFT);
		Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage();

		filterDecoration.setImage(image);
		filterDecoration.setDescriptionText("Press Ctrl+Space to see history");
		filterDecoration.setShowOnlyOnFocus(true);
		pattern
				.setToolTipText(WizardMessages.ResourceSelectionDialog_Pattern_Tooltip_Text);
		initSuggestionsAndHistory();
		installContentProposalAdapter(pattern, new TextContentAdapter());
		gd = new GridData(GridData.FILL_BOTH);
		pattern.setLayoutData(gd);

		pattern.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				themeTableViewer.setInput(filterInput(pattern.getText()));
				if (themeTableViewer.getElementAt(0) != null) {
					themeTableViewer.setSelection(new StructuredSelection(
							themeTableViewer.getElementAt(0)), true);
				}
				getManager().refresh();
			}
		});

		pattern.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.keyCode == SWT.ARROW_DOWN) {
					themeTableViewer.scrollDown(0, 1);
					themeTableViewer.getTable().setFocus();
				} else if (event.keyCode == SWT.CR
						|| event.keyCode == SWT.KEYPAD_CR) {
					addToSuggestionsAndHistory(pattern.getText());
				}
			}
		});

		themeTableViewer = new TableViewer(parent, SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL);
		themeTableViewer.getTable().addListener(
				SWT.Paint,
				new TableImagePaintListener(getManager(), themeTableViewer
						.getTable()));
		themeTableViewer.setSorter(new SearchTableSorter(ORDER_NAME));
		Table items = themeTableViewer.getTable();
		items.setLinesVisible(false);
		items.setHeaderVisible(true);

		colName = new TableColumn(items, SWT.LEFT, 0);
		colName.setText(WizardMessages.ResourceSelectionDialog_NameLabel);
		colName.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				themeTableViewer.setSorter(new SearchTableSorter(ORDER_NAME));
			}
		});
		colId = new TableColumn(items, SWT.LEFT, 1);
		colId.setText(WizardMessages.ResourceSelectionDialog_IdLabel);
		colId.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				themeTableViewer.setSorter(new SearchTableSorter(ORDER_ID));
			}
		});

		themeTableViewer.setLabelProvider(new SearchDataLabelProvider());
		themeTableViewer.setContentProvider(new ArrayContentProvider());
		themeTableViewer.setInput(themeElements);

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.minimumHeight = 50;
		if (getManager().getPages().length > 1) {
			gd.widthHint = 313;
			gd.heightHint = 217;

		} else {
			gd.widthHint = 339;
			gd.heightHint = 266;
		}
		items.setLayoutData(gd);

		themeTableViewer.addSelectionChangedListener(this);
		themeTableViewer.addOpenListener(this);

		MouseMoveListener listener = new MouseMoveListener() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseMove(MouseEvent e) {
				handleMouseMove(themeTableViewer.getTable(), e);
			}

		};
		parent.addMouseMoveListener(listener);
		themeTableViewer.getTable().addMouseMoveListener(listener);

		IPreferenceStore store = UtilsPlugin.getDefault().getPreferenceStore();

		columnRatio = store
				.getDouble(IScreenConstants.PREF_LAST_COLUMNRATIO_THEME_RESOURCE_PAGE);
		setColumnSizes();

		themeTableViewer.getTable().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (updateColumnSizes)
					setColumnSizes();
			}
		});

		colName.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (updateColumnSizes) {
					int w = themeTableViewer.getTable().getClientArea().width;
					int c1 = colName.getWidth();
					columnRatio = (double) c1 / w;
					if (columnRatio > 1)
						columnRatio = 0.99;
					setColumnSizes();
				}
			}
		});

		colId.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (updateColumnSizes) {
					setColumnSizes();
				}
			}
		});

		updateColumnSizes = true;

		return parent;
	}

	private void setColumnSizes() {
		updateColumnSizes = false;
		int w = themeTableViewer.getTable().getClientArea().width;
		int w1 = (int) ((int) w * columnRatio);
		colName.setWidth(w1);
		colId.setWidth(w - w1);
		updateColumnSizes = true;
	}

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
				if(!(suggestionsAndHistory.contains(searchHistoryItem)
						&& (suggestionsAndHistory.indexOf(searchHistoryItem) == 0))) {
					suggestionsAndHistory.add(0, searchHistoryItem);
				}
			} else {
				if(!(suggestionsAndHistory.contains(searchHistoryItem)
						&& (suggestionsAndHistory.indexOf(searchHistoryItem) == 0))) {
					suggestionsAndHistory.remove(suggestionsAndHistorySize - 1);
					suggestionsAndHistory.add(0, searchHistoryItem);
				}
			}
		}
	}

	private void initSuggestionsAndHistory() {
		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
				.getPreferenceStore();

		int historyCount = iPreferenceStore
				.getInt(IScreenConstants.PREF_FILTER_HISTORY_COUNT);
		for (int i = 0; i < historyCount; i++) {
			String history = iPreferenceStore
					.getString(IScreenConstants.PREF_ADD_FILTER_HISTORY + (i));
			suggestionsAndHistory.add(history);
		}
	}

	// taken from eclipse example,
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
											WizardMessages.ResourceSelectionDialog_Pattern_Info_Text,
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#dispose()
	 */
	public void dispose() {
		IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
				.getPreferenceStore();

		int i = 0;
		for (String p : suggestionsAndHistory) {
			iPreferenceStore.setValue(IScreenConstants.PREF_ADD_FILTER_HISTORY
					+ i++, p);
		}
		iPreferenceStore
				.setValue(IScreenConstants.PREF_FILTER_HISTORY_COUNT, i);

		iPreferenceStore.setValue(
				IScreenConstants.PREF_LAST_COLUMNRATIO_THEME_RESOURCE_PAGE,
				columnRatio);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#setFocus()
	 */
	public void setFocus() {
		pattern.setFocus();
		getManager().enablePreviewCheckBox(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getSelectedResources()
	 */
	public Object[] getSelectedResources() {
		ISelection selection = themeTableViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toArray();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#init(java.lang.Object[])
	 */
	public Object[] init(Object[] resources) {
		List<Object> validResources = new ArrayList<Object>();

		if (resources != null) {
			for (Object selectedItem : resources) {
				if (selectedItem instanceof IContentData) {
					List<IContentData> result = (List<IContentData>) filterInput(((IContentData) selectedItem)
							.getId());
					if (result != null && result.size() > 0) {
						validResources.add(result.get(0));
						themeTableViewer.setSelection(new StructuredSelection(
								result.get(0)), true);
					}
				}
			}
		}

		if (validResources.isEmpty() && themeElements.length > 0) {
			if (themeTableViewer.getElementAt(0) != null) {
				themeTableViewer.setSelection(new StructuredSelection(
						themeTableViewer.getElementAt(0)), true);
			}
			getManager().resourcesSelected(
					new Object[] { themeTableViewer.getElementAt(0) });
		}
		return validResources.toArray(new Object[validResources.size()]);
	}

	protected IContentData[] filter(List<IContentData> list) {
		return list.toArray(new IContentData[list.size()]);
	}

	public List<IContentData> filterInput(String pattern) {

		ArrayList<IContentData> filteredInput = new ArrayList<IContentData>();

		pattern = pattern.replaceAll("\\*", ".*");
		pattern = pattern + ".*";

		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

		for (IContentData data : themeElements) {
			if (p.matcher(data.getName()).matches()) {
				filteredInput.add(data);
			} else if (p.matcher(data.getId()).matches()) {
				filteredInput.add(data);
			}
		}

		return filteredInput;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object[] resources = ((IStructuredSelection) selection).toArray();
			if (resources.length > 0) {
				Object selectedResource = resources[0];
				handleThemeResourceResult(selectedResource);
			}
		}

		super.selectionChanged(event);
	}

	private void handleThemeResourceResult(Object selectedResource) {
		if (selectedResource instanceof IContentData) {
			IContentData pluginSkinId = ((IContentData) selectedResource);
			((IThemeResourcePageManager) getManager())
					.setResult(new ThemeResourceResult<String>(true, pluginSkinId
							.getId()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#checkError()
	 */
	public String checkError() {
		if (themeTableViewer.getElementAt(0) == null
				&& pattern.getText().trim().length() > 0) {
			return WizardMessages.ResourceSelectionDialog_No_Images_error;
		}

		return null;
	}

	// Creating input for table viewer
	private void createInput() {
		IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
		IContentAdapter adapter = (IContentAdapter) activeEd
				.getAdapter(IContentAdapter.class);
		List<IContentData> list = new ArrayList<IContentData>();
		if (adapter != null) {
			IContent[] cnt = adapter.getContents();
			IContent root = ScreenUtil.getPrimaryContent(cnt);

			for (IContentData n1 : root.getChildren()) {
				if (n1.getAdapter(IScreenAdapter.class) != null) {
					continue;
				}
				for (IContentData n2 : n1.getChildren()) {
					for (IContentData n3 : n2.getChildren()) {
						for (IContentData n4 : n3.getChildren()) {
							ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) n4
									.getAdapter(ISkinnableEntityAdapter.class);
							IToolBoxAdapter tba = (IToolBoxAdapter) n4
									.getAdapter(IToolBoxAdapter.class);

							if ((ska != null && ska.isColour())
									|| (tba != null && tba.isText())) {
								continue;
							}

							if (filter == null || filter.select(n4)) {
								list.add(n4);
							}
						}
					}
				}
			}
		}
		themeElements = filter(list);
	}

	class SearchDataLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return getManager().getDefaultResourceImage();
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IContentData tmpItem = (IContentData) element;
			switch (columnIndex) {
			case 0:
				return tmpItem.getName();
			case 1:
				return tmpItem.getId();
			default:
				return null;
			}
		}

	}

	class SearchTableSorter extends ViewerSorter {
		private int sortType;

		public SearchTableSorter(int sortType) {
			this.sortType = sortType;
		}

		public int compare(Viewer viewer, Object o1, Object o2) {
			IContentData d1 = (IContentData) o1;
			IContentData d2 = (IContentData) o2;
			String s1 = sortType == ORDER_ID ? d1.getId() : d1.getName();
			String s2 = sortType == ORDER_ID ? d2.getId() : d2.getName();
			return s1.compareToIgnoreCase(s2);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.IResourceSelectionPage#getResult()
	 */
	public ResourceResult getResult() {
			
		return null;
	}

}
