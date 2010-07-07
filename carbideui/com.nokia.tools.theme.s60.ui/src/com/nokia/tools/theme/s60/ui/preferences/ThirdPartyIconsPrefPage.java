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
package com.nokia.tools.theme.s60.ui.preferences;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeDesignDescriptor;
import com.nokia.tools.platform.theme.IThemeManager;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.XmlUtil;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.s60.IThemeConstants;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.model.S60Theme;
import com.nokia.tools.theme.s60.model.tpi.DefinedIcons;
import com.nokia.tools.theme.s60.model.tpi.TPIconConflictEntry;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIcon;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconChangesPublisher;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconDefinitionInputDialog;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconLoadException;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconStoreException;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconType;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconWrapper;
import com.nokia.tools.theme.s60.model.tpi.TPIconConflictEntry.TPIconConflitingField;
import com.nokia.tools.theme.s60.ui.Messages;

public class ThirdPartyIconsPrefPage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {
	
	private static final String ICONS_XML = "icons.xml";

	public static final String HLP_CTX = "com.nokia.tools.theme.s60.ui" + '.' + "third_party_icon_preferences_context"; //$NON-NLS-2$

	static String APPUID_COL = "Application UID"; 

	static String NAMECOL = "Name"; //$NON-NLS-1$

	static String TYPECOL = "Type"; 

	static String MAJORIDCOL = "Major ID"; 

	static String MINORIDCOL = "Minor ID";

	static String thirdPartyIconColumnNames[] = { TYPECOL, NAMECOL, APPUID_COL,
			MAJORIDCOL, MINORIDCOL };

	public TableViewer toolSpecificTPIViewer, themeSpecificTPIViewer;

	public static final String DTD_RESOURCE = "design.dtd";
	
	private Button toolSpecificAdd, toolSpecificRemove, toolSpecificRemoveAll, toolSpecificRemoveAllConflicts;

	private Button themeSpecificAdd, themeSpecificRemove, themeSpecificRemoveAll, themeSpecificRemoveAllConflicts;

	private DefinedIcons toolSpecificThirdPartyIconsModel, toolSpecificThirdPartyIconsBackupModel;

	private DefinedIcons themeSpecificThirdPartyIconsModel,	themeSpecificThirdPartyIconsBackupModel;

	private boolean updateColSize = true;

	// Combo box choices
	static final String[] ICON_TYPES = { "Application", "Non-Application" };

	private S60Theme currentTheme;
	
	private static IContentProvider tpiTableViewerContentProvider = new TableViewerThirdPartyIconContentProvider();
	
	private static ILabelProvider tpiTableViewerLabelProvider = new TableViewerThirdPartyIconLabelProvider();
	
	private static Map<ThirdPartyIconWrapper, List<TPIconConflictEntry>> conflictingIconList;
	
	@Override
	protected Control createContents(Composite parent) {
		//NLS.initializeMessages(Messages.class.getName(), Messages.class);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, HLP_CTX);
		Composite generalComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		generalComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalIndent = 5;
		generalComposite.setLayoutData(gd);

		setDescription(Messages.Icons3rd_desc);
		createDescriptionLabel(generalComposite);

		Label toolSpecificLabel = new Label(generalComposite, SWT.HORIZONTAL
				| SWT.VERTICAL);
		toolSpecificLabel.setText("Tool Specific Icons:");
		noDefaultAndApplyButton();
		createTableForToolSpecificTPI(generalComposite);
		createTableForThemeSpecificTPI(generalComposite);
		
		Theme activeTheme = ThemeUtil.getCurrentActiveTheme();
		boolean isS60Theme = isS60Theme(activeTheme);
		
		if(ThirdPartyIconManager.getToolSpecificThirdPartyIconUrl() == null){
			setToolSpecificTPIControlsEnablement(false);
			setThemeSpecificTPIControlsEnablement(false);
			isS60Theme = false;
		}
		else{
			if (!isS60Theme) {			
				setThemeSpecificTPIControlsEnablement(false);
			}
			else{
				currentTheme = (S60Theme)activeTheme;
			}
			initializeValues();
		}

		return parent;
	}
	
	/**
	 * @param generalComposite
	 */
	private void createTableForToolSpecificTPI(Composite generalComposite) {
		GridLayout layout;
		GridData gd;
		Composite thirdPartyComposite = new Composite(generalComposite,	SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		thirdPartyComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		thirdPartyComposite.setLayout(layout);

		Composite tableComposite = new Composite(thirdPartyComposite, SWT.NONE);
		FillLayout layout2 = new FillLayout();
		tableComposite.setLayout(layout2);
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 5;
		tableComposite.setLayoutData(gd);

		toolSpecificTPIViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.FULL_SELECTION
				| SWT.BORDER);

		initializeThirdPartyIconTableViewer(toolSpecificTPIViewer);

		toolSpecificTPIViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				// refresh buttons state
				ISelection s = event.getSelection();
				toolSpecificRemove.setEnabled(!s.isEmpty());
				if(!s.isEmpty() && s instanceof IStructuredSelection){
					IStructuredSelection selection = (IStructuredSelection)s;
					removeAllBackgroundHighlights(); // Clearing previous highlighting for the conflicts which were shown earlier.
					
					Iterator iterator = selection.iterator();
					while(iterator.hasNext()){
						Object selectedItem = iterator.next();
						if(selectedItem instanceof ThirdPartyIcon){
							highlightConflictDataForTPI((ThirdPartyIcon)selectedItem);
						}
					}
					
				}
			}
		});

		toolSpecificTPIViewer.getTable().addKeyListener(
				new org.eclipse.swt.events.KeyAdapter() {

					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.DEL
								&& toolSpecificThirdPartyIconsModel != null
								&& toolSpecificTPIViewer.getTable().getSelectionIndex() >= 0) {
							toolSpecificThirdPartyIconsModel.remove(toolSpecificTPIViewer.getTable().getSelectionIndex());
							refreshViewers();
						}
					}
				});

		
		toolSpecificAdd = new Button(thirdPartyComposite, SWT.NONE);
		toolSpecificAdd.setText(Messages.Icons3rd_add);
		calculateButtonSize(toolSpecificAdd);

		toolSpecificRemove = new Button(thirdPartyComposite, SWT.NONE);
		toolSpecificRemove.setText(Messages.Icons3rd_delete);
		calculateButtonSize(toolSpecificRemove);

		toolSpecificRemoveAllConflicts = new Button(thirdPartyComposite, SWT.NONE);
		toolSpecificRemoveAllConflicts.setText(Messages.Icons3rd_btnRemoveConflicts);
		calculateButtonSize(toolSpecificRemoveAllConflicts);
		
		toolSpecificRemoveAll = new Button(thirdPartyComposite, SWT.NONE);
		toolSpecificRemoveAll.setText(Messages.Icons3rd_btnRemoveAll);
		calculateButtonSize(toolSpecificRemoveAll);
		
		toolSpecificAdd.addSelectionListener(this);
		toolSpecificRemove.addSelectionListener(this);
		toolSpecificRemoveAllConflicts.addSelectionListener(this);
		toolSpecificRemoveAll.addSelectionListener(this);

		toolSpecificRemove.setEnabled(false);
	}



	private void initializeThirdPartyIconTableViewer(final TableViewer tableViewer){
		Table table = tableViewer.getTable();

		final TableColumn colType = new TableColumn(table, SWT.LEFT);
		colType.setText(thirdPartyIconColumnNames[0]);

		final TableColumn colName = new TableColumn(table, SWT.LEFT);
		colName.setText(thirdPartyIconColumnNames[1]);

		final TableColumn colUid = new TableColumn(table, SWT.LEFT);
		colUid.setText(thirdPartyIconColumnNames[2]);

		final TableColumn colMajorId = new TableColumn(table, SWT.LEFT);
		colMajorId.setText(thirdPartyIconColumnNames[3]);

		final TableColumn colMinorId = new TableColumn(table, SWT.LEFT);
		colMinorId.setText(thirdPartyIconColumnNames[4]);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setColumnProperties(thirdPartyIconColumnNames);
		tableViewer.setContentProvider(tpiTableViewerContentProvider);
		tableViewer.setLabelProvider(tpiTableViewerLabelProvider);

		tableViewer.getTable().addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				if (updateColSize) {
					Table tbl = tableViewer.getTable();
					int w = tbl.getClientArea().width;
					int c1 = 85;
					int c2 = 100;
					int c3 = 85;
					int c4 = 75;
					updateColSize = false;
					colType.setWidth(c1);
					colName.setWidth(c2);
					colUid.setWidth(c3);
					colMajorId.setWidth(c4);
					colMinorId.setWidth(w - c1 - c2 - c3 - c4);
					updateColSize = true;
				}
			}
		});

		tableViewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if(!selection.isEmpty() && selection instanceof IStructuredSelection){
					ThirdPartyIcon thirdPartyIcon = (ThirdPartyIcon)((IStructuredSelection)selection).getFirstElement();
					if(thirdPartyIcon != null){
						ThirdPartyIconDefinitionInputDialog dialog = new ThirdPartyIconDefinitionInputDialog(getShell(), 
								                 thirdPartyIcon.getThirdPartyIconType(),"Edit Third Party Icon", thirdPartyIcon);
						if(dialog.open() == IDialogConstants.OK_ID){
							refreshViewers();
							removeAllBackgroundHighlights();
							highlightConflictDataForTPI(thirdPartyIcon);
						}	
					}
				}
			}
			
		});
	}

	private void refreshViewers(){
		removeAllBackgroundHighlights();
		DefinedIcons aggregatedThirdPartyIcons = new DefinedIcons();
		aggregatedThirdPartyIcons.addAll(toolSpecificThirdPartyIconsModel);
		aggregatedThirdPartyIcons.addAll(themeSpecificThirdPartyIconsModel);
		conflictingIconList = ThirdPartyIconManager.getConflictingIconList(aggregatedThirdPartyIcons);
		toolSpecificTPIViewer.refresh();
		themeSpecificTPIViewer.refresh();
		if(toolSpecificThirdPartyIconsModel == null || toolSpecificThirdPartyIconsModel.isEmpty()){
			toolSpecificRemoveAll.setEnabled(false);
		}
		else{
			toolSpecificRemoveAll.setEnabled(true);
		}

		if(themeSpecificThirdPartyIconsModel == null || themeSpecificThirdPartyIconsModel.isEmpty()){
			themeSpecificRemoveAll.setEnabled(false);
		}
		else{
			themeSpecificRemoveAll.setEnabled(true);
		}

		// checking if conflicts exits and enabling or disabling the remove conflicts button for tool specific third part icons
		boolean conflictExists = false;
        Iterator<ThirdPartyIcon> iterator = toolSpecificThirdPartyIconsModel.iterator();
        while(iterator.hasNext()){
                List<TPIconConflictEntry> list = conflictingIconList.get(new ThirdPartyIconWrapper(iterator.next()));
                if(list != null && !list.isEmpty()){
                        conflictExists = true;
                        break;
                }
        }

       	toolSpecificRemoveAllConflicts.setEnabled(conflictExists);

		// checking if conflicts exits and enabling or disabling the remove conflicts button for tool specific third part icons
		conflictExists = false;
		iterator = themeSpecificThirdPartyIconsModel.iterator();
        while(iterator.hasNext()){
                List<TPIconConflictEntry> list = conflictingIconList.get(new ThirdPartyIconWrapper(iterator.next()));
                if(list != null && !list.isEmpty()){
                        conflictExists = true;
                        break;
                }
        }

       	themeSpecificRemoveAllConflicts.setEnabled(conflictExists);
    }

	/**
	 * @param generalComposite
	 */
	private void createTableForThemeSpecificTPI(Composite generalComposite) {
		GridLayout layout;
		GridData gd;
		
		Label themeSpecificLabel = new Label(generalComposite, SWT.HORIZONTAL
				| SWT.VERTICAL);
		themeSpecificLabel.setText("Theme Specific Icons:");

		Composite thirdPartyComposite = new Composite(generalComposite,
				SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		thirdPartyComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		thirdPartyComposite.setLayout(layout);

		Composite tableComposite = new Composite(thirdPartyComposite, SWT.NONE);
		FillLayout layout2 = new FillLayout();
		tableComposite.setLayout(layout2);
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 5;
		tableComposite.setLayoutData(gd);

		themeSpecificTPIViewer = new TableViewer(tableComposite, SWT.MULTI
				| SWT.FULL_SELECTION | SWT.BORDER);

		
		initializeThirdPartyIconTableViewer(themeSpecificTPIViewer);

		themeSpecificTPIViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				// refresh buttons state
				ISelection s = event.getSelection();
				themeSpecificRemove.setEnabled(!s.isEmpty());
				
				if(!s.isEmpty() && s instanceof IStructuredSelection){
					IStructuredSelection selection = (IStructuredSelection)s;
					removeAllBackgroundHighlights(); // Clearing previous highlighting for the conflicts which were shown earlier.
					
					Iterator iterator = selection.iterator();
					while(iterator.hasNext()){
						Object selectedItem = iterator.next();
						if(selectedItem instanceof ThirdPartyIcon){
							highlightConflictDataForTPI((ThirdPartyIcon)selectedItem);
						}
					}	
				}
			}
		});

		themeSpecificTPIViewer.getTable().addKeyListener(
				new org.eclipse.swt.events.KeyAdapter() {

					@Override
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == SWT.DEL
								&& themeSpecificThirdPartyIconsModel != null
								&& themeSpecificTPIViewer.getTable().getSelectionIndex() >= 0) {
							themeSpecificThirdPartyIconsModel.remove(themeSpecificTPIViewer.getTable().getSelectionIndex());
							refreshViewers();
						}
					}
				});
		
		
		themeSpecificAdd = new Button(thirdPartyComposite, SWT.NONE);
		themeSpecificAdd.setText(Messages.Icons3rd_add);
		calculateButtonSize(themeSpecificAdd);

		themeSpecificRemove = new Button(thirdPartyComposite, SWT.NONE);
		themeSpecificRemove.setText(Messages.Icons3rd_delete);
		calculateButtonSize(themeSpecificRemove);

		themeSpecificRemoveAllConflicts = new Button(thirdPartyComposite, SWT.NONE);
		themeSpecificRemoveAllConflicts.setText(Messages.Icons3rd_btnRemoveConflicts);
		calculateButtonSize(themeSpecificRemoveAllConflicts);
		
		themeSpecificRemoveAll = new Button(thirdPartyComposite, SWT.NONE);
		themeSpecificRemoveAll.setText(Messages.Icons3rd_btnRemoveAll);
		calculateButtonSize(themeSpecificRemoveAll);
		
		themeSpecificAdd.addSelectionListener(this);
		themeSpecificRemove.addSelectionListener(this);
		themeSpecificRemoveAll.addSelectionListener(this);
		themeSpecificRemoveAllConflicts.addSelectionListener(this);
		
		themeSpecificRemove.setEnabled(false);

	}

	private void setThemeSpecificTPIControlsEnablement(boolean enabled) {		
		themeSpecificAdd.setEnabled(enabled);
		themeSpecificRemove.setEnabled(enabled);
		themeSpecificRemoveAll.setEnabled(enabled);
		themeSpecificRemoveAllConflicts.setEnabled(enabled);

		if(!enabled){
			themeSpecificTPIViewer.getTable().clearAll();
		}
		themeSpecificTPIViewer.getTable().setEnabled(enabled);
	}

	private void setToolSpecificTPIControlsEnablement(boolean enabled) {		
		toolSpecificAdd.setEnabled(enabled);
		toolSpecificRemove.setEnabled(enabled);
		toolSpecificRemoveAll.setEnabled(enabled);
		toolSpecificRemoveAllConflicts.setEnabled(enabled);

		if(!enabled){
			toolSpecificTPIViewer.getTable().clearAll();
		}
		toolSpecificTPIViewer.getTable().setEnabled(enabled);
	}
	
	private boolean isS60Theme(Theme currentActiveTheme) {
		if (currentActiveTheme instanceof S60Theme)
		   return true;
		return false;
	}

	public void init(IWorkbench workbench) {
	}

	/**
	 * Returns the path to icons.xml in the current theme if present or null
	 * otherwise
	 * 
	 * @return
	 * @throws IOException
	 */
	private URL thirdPartyIconsPathInCurrentTheme() throws Exception {
		if (null != currentTheme) {
			String themeDir = currentTheme.getThemeDir();
			String pathToIconsDotXmlInTheme = themeDir + File.separatorChar
					+ ICONS_XML;
			File iconsDotXml = new File(pathToIconsDotXmlInTheme);
			URL url = iconsDotXml.toURI().toURL();
			if (!iconsDotXml.exists()) {
				createXmlFile(iconsDotXml, url);
			}
			return url;
		}
		return null;
	}

	private void createXmlFile(File iconsDotXml, URL url) throws IOException,
			ParserConfigurationException, Exception {
		iconsDotXml.createNewFile();
		Map<String, String> props = new HashMap<String, String>();
		Document xml = createDocumentFrom(getIconUrl(), true);
		props.put(OutputKeys.DOCTYPE_SYSTEM, DTD_RESOURCE);
		XmlUtil.write(xml, FileUtils.getFile(url), props);
	}


	public boolean performOk() {

		if(ThirdPartyIconManager.getToolSpecificThirdPartyIconUrl() == null){
			return true;
		}
		if(conflictingIconList != null && !conflictingIconList.isEmpty()){
			MessageDialog.openError(getShell(), "Unresolved Conflicts", "There are some unresolved conflicts [indicated in red]. Please resolve them before proceeding");
			return false;
		}
		try {
			saveThirdPartyIcons(toolSpecificTPIViewer, toolSpecificThirdPartyIconsModel,
								toolSpecificThirdPartyIconsBackupModel, getIconUrl(), false, true);

			saveThirdPartyIcons(themeSpecificTPIViewer,	themeSpecificThirdPartyIconsModel,
						themeSpecificThirdPartyIconsBackupModel, thirdPartyIconsPathInCurrentTheme(), true, false);
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}

		disposeUnusedModels();
		return true;
	}

	private void disposeUnusedModels() {
		currentTheme = null;
		themeSpecificThirdPartyIconsBackupModel = null;
		themeSpecificThirdPartyIconsModel = null;
		toolSpecificThirdPartyIconsModel = null;
		toolSpecificThirdPartyIconsBackupModel = null;
	}


	private void saveThirdPartyIcons(TableViewer viewer,
			DefinedIcons modelToSave, DefinedIcons backupModel,
			URL iconsDotXmlUrl, boolean themeSpecific, boolean releaseModels) throws Exception {
		if (null == modelToSave || iconsDotXmlUrl == null)
			return;

		try {
			ThirdPartyIconManager.storeThirdPartyIcons(modelToSave, iconsDotXmlUrl);
		} catch (ThirdPartyIconStoreException e) {
			S60ThemePlugin.error(e);
		}

		DefinedIcons newModel = null;
		
		if(themeSpecific)
			newModel = ThirdPartyIconManager.loadThirdPartyIcons(iconsDotXmlUrl, ThirdPartyIconType.THEME_SPECIFIC);
		else
			newModel = ThirdPartyIconManager.loadThirdPartyIcons(iconsDotXmlUrl, ThirdPartyIconType.TOOL_SPECIFIC);

		S60Theme theme = null;
		if(themeSpecific){
			theme = currentTheme;
		}
		ThirdPartyIconChangesPublisher.refresh3rdPartyIcons(backupModel, newModel, themeSpecific, theme, releaseModels);

		viewer.setInput(modelToSave);

		backupModel.clear();
		backupModel.addAll(modelToSave.clone());

	}

	public static URL getIconUrl() throws Exception {
		for (IThemeDescriptor descriptor : ThemePlatform
				.getThemeDescriptorsByContainer(IThemeConstants.THEME_CONTAINER_ID)) {
			for (IThemeDesignDescriptor desc : descriptor.getDesigns()) {
				if (desc.isCustomizable()) {
					return desc.getPath();
				}
			}
		}
		return null;
	}

	private static Document createDocumentFrom(URL url, boolean deleteElements)
			throws ParserConfigurationException {
		DocumentBuilder db = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		db.setEntityResolver(new EntityResolver() {

			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				return new InputSource(IThemeDescriptor.class
						.getResourceAsStream(IThemeManager.DTD_FOLDER
								+ new File(systemId).getName()));
			}
		});

		InputStream in = null;
		try {
			in = url.openStream();
			Document document = db.parse(in);
			if (deleteElements)
				document = removeElementsFrom(document);
			return document;
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	private static Document removeElementsFrom(Document document) {
		Node component = document.getElementsByTagName(
				ThemeTag.ELEMENT_COMPONENT).item(0);
		while (component.hasChildNodes()) {
			Node toRemove = component.getFirstChild();
			component.removeChild(toRemove);
		}

		return document;
	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		try {
			toolSpecificThirdPartyIconsModel = ThirdPartyIconManager.getToolSpecificThirdPartyIcons(true);
		} catch (ThirdPartyIconLoadException e) {
			S60ThemePlugin.error(e);
			toolSpecificThirdPartyIconsModel = new DefinedIcons();
		}
		if(currentTheme != null){
			themeSpecificThirdPartyIconsModel = currentTheme.getThemeSpecificThirdPartyIcons().clone();
		}
		else{
			themeSpecificThirdPartyIconsModel = new DefinedIcons();
		}

		toolSpecificThirdPartyIconsBackupModel = toolSpecificThirdPartyIconsModel.clone();
		themeSpecificThirdPartyIconsBackupModel = themeSpecificThirdPartyIconsModel.clone();
		
		DefinedIcons aggregatedThirdPartyIcons = new DefinedIcons();
		aggregatedThirdPartyIcons.addAll(toolSpecificThirdPartyIconsModel);
		aggregatedThirdPartyIcons.addAll(themeSpecificThirdPartyIconsModel);
		
		conflictingIconList = ThirdPartyIconManager.getConflictingIconList(aggregatedThirdPartyIcons);
		toolSpecificTPIViewer.setInput(toolSpecificThirdPartyIconsModel);
		themeSpecificTPIViewer.setInput(themeSpecificThirdPartyIconsModel);
		refreshViewers();
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void appendNewRow(String type) {
		int index = 0;
		String icnUid = null;
		while (true) {
			icnUid = "0x" + Long.toHexString(0x100000000L | index).substring(1);
			// if (model.getUid(icnUid) == null && !appUids.contains(icnUid))
			if (toolSpecificThirdPartyIconsModel.getUid(icnUid) == null)
				break;
			else
				index++;
		}

		index = 0;
		String icnName = null;
		String icnId = null;
		while (true) {
			icnName = "Custom Icon " + index;//$NON-NLS-1$
			icnId = icnName.replace(' ', '_');
			if (toolSpecificThirdPartyIconsModel.getIconById(icnId) == null)// !ids.contains(icnId))
				break;
			else
				index++;
		}
		if (type.equalsIgnoreCase("apps")) {
			toolSpecificThirdPartyIconsModel.add(new ThirdPartyIcon(icnUid,
					icnName, null, null, ThirdPartyIconType.TOOL_SPECIFIC));//$NON-NLS-1$
		} else {
			toolSpecificThirdPartyIconsModel.add(new ThirdPartyIcon(null,
					icnName.replace(' ', '_'), icnName, "0x11111111",
					"0x11111111", ThirdPartyIconType.TOOL_SPECIFIC));
		}
		toolSpecificTPIViewer.refresh();
	}

	public void widgetSelected(SelectionEvent e) {

		if (e.getSource() == themeSpecificAdd) {
			ThirdPartyIconDefinitionInputDialog tpiCreationDialog =  
				new ThirdPartyIconDefinitionInputDialog(getShell(), ThirdPartyIconType.THEME_SPECIFIC, "New Third Party Icon", null); 
			if(tpiCreationDialog.open() == IDialogConstants.OK_ID)
				themeSpecificThirdPartyIconsModel.add(tpiCreationDialog.getThirdPartyIcon());//$NON-NLS-1$			
		}
		else if (e.getSource() == themeSpecificRemove) {
			for (TableItem item : themeSpecificTPIViewer.getTable().getSelection()) {
				themeSpecificThirdPartyIconsModel.remove(item.getData());
			}
		}
		else if (e.getSource() == themeSpecificRemoveAll){
			themeSpecificThirdPartyIconsModel.clear();
		}
		else if (e.getSource() == themeSpecificRemoveAllConflicts){
			Iterator<ThirdPartyIcon> iterator = themeSpecificThirdPartyIconsModel.iterator();
			while(iterator.hasNext()){
				List<TPIconConflictEntry> list = conflictingIconList.get(new ThirdPartyIconWrapper(iterator.next()));
				if(list != null && !list.isEmpty()){
					iterator.remove();
				}
			}
		}
		else if (e.getSource() == toolSpecificAdd) {
			ThirdPartyIconDefinitionInputDialog tpiCreationDialog =  
				new ThirdPartyIconDefinitionInputDialog(getShell(), ThirdPartyIconType.TOOL_SPECIFIC, "New Third Party Icon", null); 
			if(tpiCreationDialog.open() == IDialogConstants.OK_ID)			
				toolSpecificThirdPartyIconsModel.add(tpiCreationDialog.getThirdPartyIcon());
		}

		else if (e.getSource() == toolSpecificRemove) {
			for (TableItem item : toolSpecificTPIViewer.getTable().getSelection()) {
				toolSpecificThirdPartyIconsModel.remove(item.getData());
			}
		}
		else if(e.getSource() == toolSpecificRemoveAll) {
			toolSpecificThirdPartyIconsModel.clear();
		}
		else if(e.getSource() == toolSpecificRemoveAllConflicts) {
			Iterator<ThirdPartyIcon> iterator = toolSpecificThirdPartyIconsModel.iterator();
			while(iterator.hasNext()){
				List<TPIconConflictEntry> list = conflictingIconList.get(new ThirdPartyIconWrapper(iterator.next()));
				if(list != null && !list.isEmpty()){
					iterator.remove();
				}
			}			
		}
		refreshViewers();
	}

	private void calculateButtonSize(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int wHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point mSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(wHint, mSize.x);
		button.setLayoutData(data);
	}

	private static class TableViewerThirdPartyIconContentProvider implements IStructuredContentProvider{
		public Object[] getElements(Object inputElement) {
			return ((DefinedIcons) inputElement).toArray();
		}

		public void dispose() {
			// Simply do nothing
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Simply do nothing
		}
	}

	private static class TableViewerThirdPartyIconLabelProvider extends LabelProvider implements ITableColorProvider, ITableLabelProvider{

		public Color getBackground(Object element, int columnIndex) {
			
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			List<TPIconConflictEntry> list = conflictingIconList.get(new ThirdPartyIconWrapper((ThirdPartyIcon)element));
			if(list == null || list.size() == 0)
				return null;
			else{
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			}
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			ThirdPartyIcon i = (ThirdPartyIcon) element;
			List<TPIconConflictEntry> conflictingList = conflictingIconList.get(new ThirdPartyIconWrapper(i));
			String name = null, appUID = null, majorID = null, minorID = null;
			
			if(conflictingList != null && !conflictingList.isEmpty()){
				for(TPIconConflictEntry conflictEntry: conflictingList){
					for(TPIconConflitingField field : conflictEntry.getConflictingFields()){
						if(field == TPIconConflitingField.TP_ICON_NAME){
							name = i.getName() + "*";
						}
						else if(field == TPIconConflitingField.TP_ICON_ID){
							appUID = i.getAppUid() + "*";
						}
						else if(field == TPIconConflitingField.TP_ICON_MAJORID_MINORID){
							majorID = i.getMajorId() + "*";
							minorID = i.getMinorId() + "*";
						}
						
					}
				}
			}
			
			switch (columnIndex) {
			case 0:
				return i.isApplication() ? "Application"
						: "Non-Application";
			case 1:
				return name == null ? i.getName() : name;
			case 2:
				return appUID == null ? i.getAppUid() : appUID;
			case 3:
				return majorID == null ? i.getMajorId() : majorID;
			case 4:
				return minorID == null ? i.getMinorId() : minorID;
			}
			return null;
		}
		
	}

	private void highlightConflictDataForTPI(ThirdPartyIcon thirdPartyIcon) {
		List<TPIconConflictEntry> conflictEntryList = conflictingIconList.get(new ThirdPartyIconWrapper(thirdPartyIcon));
		
		if(conflictEntryList != null  && !conflictEntryList.isEmpty()){
			for(TPIconConflictEntry conflictEntry: conflictEntryList){
				ThirdPartyIcon conflictThirdPartyIcon = conflictEntry.getConflictThirdPartyIcon();
				TableViewer tableViewer = null;
				int index = -1;
				if(conflictThirdPartyIcon.getThirdPartyIconType() == ThirdPartyIconType.THEME_SPECIFIC){
					tableViewer = themeSpecificTPIViewer;
					int count = 0;
					for(ThirdPartyIcon tpi: themeSpecificThirdPartyIconsModel){
						if(tpi == conflictThirdPartyIcon){
							index = count;
						}
						count++;
					}
				}
				else{
					tableViewer = toolSpecificTPIViewer;
					int count = 0;
					for(ThirdPartyIcon tpi: toolSpecificThirdPartyIconsModel){
						if(tpi == conflictThirdPartyIcon){
							index = count;
							break;
						}
						count++;
					}

				}
				if(index != -1){
					TableItem item = tableViewer.getTable().getItem(index);
					item.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_YELLOW));
					tableViewer.getTable().deselect(index);
				}
			}
		}
	}
	
	private void removeAllBackgroundHighlights() {
		Table table = toolSpecificTPIViewer.getTable();
		for(int i = 0; i < table.getItemCount(); i++){
			table.getItem(i).setBackground(null);
		}
		
		table = themeSpecificTPIViewer.getTable();
		for(int i = 0; i < table.getItemCount(); i++){
			table.getItem(i).setBackground(null);
		}
	}	
	
}