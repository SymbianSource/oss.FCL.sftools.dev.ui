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
package com.nokia.tools.s60.preferences;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.wizards.PluginContentViewer;
import com.nokia.tools.s60.wizards.PluginInstallationDialog;
import com.nokia.tools.s60.wizards.UninstallPluginOperation;
import com.nokia.tools.screen.ui.utils.ScreenUtil;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public class PluginHandlingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {

	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "plugin_handling_preferences_context"; 

	private static final String PLUGIN_THEME_S60_ID = "com.nokia.tools.theme.s60";

	private TableViewer pluginViewer;

	private PluginContentViewer contentViewer;

	private Button btnAdd, btnRemove;

	private boolean updateColSizes = true;

	Shell shell;

	@Override
	protected Control createContents(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				PREFERENCES_CONTEXT);

		Composite generalComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		generalComposite.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		generalComposite.setLayoutData(gd);

		setDescription(Messages.PluginHandlingPreferencePage_PreferencesDescriptionLabel);
		createDescriptionLabel(generalComposite);

		SashForm sash = new SashForm(generalComposite, SWT.VERTICAL);
		gd = new GridData(GridData.FILL_BOTH);
		sash.setLayoutData(gd);

		Composite pluginsComposite = new Composite(sash, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		pluginsComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		pluginsComposite.setLayout(layout);

		Label lblPlugins = new Label(pluginsComposite, SWT.NONE);
		lblPlugins
				.setText(Messages.PluginHandlingPreferencePage_PluginsTitleLabel);
		gd = new GridData();
		gd.verticalIndent = 5;
		gd.horizontalSpan = 2;
		lblPlugins.setLayoutData(gd);

		Composite tableComposite = new Composite(pluginsComposite, SWT.NONE);
		FillLayout layout2 = new FillLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		tableComposite.setLayout(layout2);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		tableComposite.setLayoutData(gd);

		pluginViewer = new TableViewer(tableComposite, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.BORDER);
		Table pluginTable = pluginViewer.getTable();
		final TableColumn colUid = new TableColumn(pluginTable, SWT.LEFT);
		colUid.setText("Id");
		colUid.setWidth(100);

		final TableColumn colName = new TableColumn(pluginTable, SWT.LEFT);
		colName.setText("Name");
		colName.setWidth(80);

		pluginTable.setHeaderVisible(true);
		pluginTable.setLinesVisible(true);

		pluginViewer.setColumnProperties(new String[] { "Id", "Name" });

		pluginViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {
						// refresh buttons state
						ISelection s = event.getSelection();
						int index = pluginViewer.getTable().getSelectionIndex();
						if (index >= 0) {
							// selection will be lost when a plugin is
							// uninstalled
							String pluginId = pluginViewer.getTable().getItem(
									index).getText();
							btnRemove.setEnabled(!s.isEmpty()
									&& !isSystemPlugin(pluginId));

							// refreshes the properties viewer
							if (s instanceof IStructuredSelection) {
								contentViewer
										.setPlugin(((IStructuredSelection) s)
												.getFirstElement());
							}
						}
					}
				});

		pluginViewer.getTable().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (updateColSizes) {
					Table tbl = pluginViewer.getTable();
					int w = tbl.getClientArea().width;
					int c1 = w / 5 * 3;
					int c2 = w - c1;
					updateColSizes = false;
					colUid.setWidth(c1);
					colName.setWidth(c2);
					updateColSizes = true;
				}
			}
		});

		pluginViewer.setContentProvider(new ArrayContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
			 */
			@Override
			public Object[] getElements(Object inputElement) {
				Set<Bundle> namespaces = new HashSet<Bundle>();
				for (String pointId : new String[] {
						PlatformExtensionManager.DEVICE_CONTRIBUTOR_ID,
						PlatformExtensionManager.LAYOUT_CONTRIBUTOR_ID,
						PlatformExtensionManager.THEME_CONTRIBUTOR_ID }) {
					IExtensionPoint point = Platform.getExtensionRegistry()
							.getExtensionPoint(pointId);
					for (IExtension extension : point.getExtensions()) {
						Bundle bundle = Platform.getBundle(extension
								.getNamespaceIdentifier());
						if (bundle != null) {
							namespaces.add(bundle);
						}
					}
				}
				return namespaces.toArray();
			}

		});
		pluginViewer.setLabelProvider(new BundleLabelProvider());
		pluginViewer.setComparator(new ViewerComparator() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Bundle a = (Bundle) e1;
				Bundle b = (Bundle) e2;

				if (isSystemPlugin(a.getSymbolicName())) {
					if (isSystemPlugin(b.getSymbolicName())) {
						return a.getSymbolicName().compareTo(
								b.getSymbolicName());
					}
					return -1;
				}
				if (isSystemPlugin(b.getSymbolicName())) {
					return 1;
				}

				return a.getSymbolicName().compareTo(b.getSymbolicName());
			}

		});
		pluginViewer.setInput(PlatformExtensionManager.class);

		btnAdd = new Button(pluginsComposite, SWT.PUSH);
		btnAdd.setText(Messages.PluginHandlingPreferencePage_AddButtonLabel);
		calculateButtonSize(btnAdd);
		btnAdd.addSelectionListener(this);

		btnRemove = new Button(pluginsComposite, SWT.PUSH);
		btnRemove
				.setText(Messages.PluginHandlingPreferencePage_RemoveButtonLabel);
		calculateButtonSize(btnRemove);
		btnRemove.setEnabled(false);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		btnRemove.setLayoutData(gd);
		btnRemove.addSelectionListener(this);

		Composite pluginInfoComposite = new Composite(sash, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.minimumHeight = 48;
		pluginInfoComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		pluginInfoComposite.setLayout(layout);

		Label lblPluginInfo = new Label(pluginInfoComposite, SWT.NONE);
		lblPluginInfo
				.setText(Messages.PluginHandlingPreferencePage_PluginInfoTitleLabel);
		gd = new GridData();
		gd.verticalIndent = 3;
		lblPluginInfo.setLayoutData(gd);

		contentViewer = new PluginContentViewer(pluginInfoComposite);

		sash.setWeights(new int[] { 40, 60 });

		noDefaultAndApplyButton();
		setSystemPluginsGrayed();
		return parent;
	}

	private void setSystemPluginsGrayed() {
		Color color = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
		Color color2 = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		for (TableItem pluginItem : pluginViewer.getTable().getItems()) {
			String pluginId = pluginItem.getText();
			if (isSystemPlugin(pluginId)) {
				pluginItem.setForeground(color);
				pluginItem.setForeground(1, color);
			} else {
				pluginItem.setForeground(color2);
				pluginItem.setForeground(1, color2);
			}
		}
	}

	private boolean isSystemPlugin(String id) {
		return id.equals(PLUGIN_THEME_S60_ID);
	}

	public void init(IWorkbench workbench) {
		
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnAdd) {
			PluginInstallationDialog pluginInstallationDialog = new PluginInstallationDialog(
					shell);
			if (Window.OK == pluginInstallationDialog.open()) {
				pluginViewer.refresh();
				setSystemPluginsGrayed();
				Bundle newEntry = pluginInstallationDialog.getBundle();
				if (newEntry != null) {
					pluginViewer.setSelection(
							new StructuredSelection(newEntry), true);
				}
			}
		}
		if (e.getSource() == btnRemove) {
			ISelection selection = pluginViewer.getSelection();
			if (selection instanceof IStructuredSelection) {
				Bundle bundle = (Bundle) ((IStructuredSelection) selection)
						.getFirstElement();
				IEditorReference[] editorsToClose = getEditorsToClose(bundle);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < editorsToClose.length; i++) {
					try {
						IEditorInput input = editorsToClose[i].getEditorInput();
						String name;
						if (input instanceof IFileEditorInput) {
							name = ((IFileEditorInput) input).getFile()
									.getProject().getName();
						} else {
							name = input.getName();
						}
						sb.append(name + ", ");
					} catch (Exception ex) {
						S60WorkspacePlugin.error(ex);
					}
				}
				if (sb.length() > 0) {
					sb.delete(sb.length() - 2, sb.length());
				}
				if (MessageDialog
						.openConfirm(
								getShell(),
								Messages.PluginHandlingPreferencePage_Uninstall_Confirm_Title,
								sb.length() > 0 ? MessageFormat
										.format(
												Messages.PluginHandlingPreferencePage_Uninstall_Confirm_Message2,
												new Object[] {
														bundle
																.getSymbolicName(),
														sb.toString() })
										: MessageFormat
												.format(
														Messages.PluginHandlingPreferencePage_Uninstall_Confirm_Message,
														new Object[] { bundle
																.getSymbolicName() }))) {
					for (IEditorReference ref : editorsToClose) {
						ref.getEditor(false).doSave(null);
					}

					try {
						new ProgressMonitorDialog(getShell()).run(true, false,
								new UninstallPluginOperation(bundle));
						pluginViewer.refresh();
					} catch (Exception ex) {
						MessageDialogWithTextContent
								.openError(
										getShell(),
										Messages.PluginHandlingPreferencePage_Uninstall_Error_Title,
										Messages.PluginHandlingPreferencePage_Uninstall_Error_Message,
										StringUtils.dumpThrowable(ex));
					}

					if (editorsToClose.length > 0) {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().closeEditors(editorsToClose,
										true);
					}
				}
			}
		}
	}

	protected IEditorReference[] getEditorsToClose(Bundle bundle) {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(
						PlatformExtensionManager.THEME_CONTRIBUTOR_ID);
		List<IExtension> exts = new ArrayList<IExtension>();
		for (IExtension extension : point.getExtensions()) {
			Bundle b = Platform.getBundle(extension.getNamespaceIdentifier());
			if (bundle == b) {
				exts.add(extension);
			}
		}
		IExtension[] extensions = exts.toArray(new IExtension[exts.size()]);
		IThemeDescriptor[] themes = PlatformExtensionManager
				.getThemeDescriptors(extensions);
		IThemeModelDescriptor[] models = PlatformExtensionManager
				.getThemeModelDescriptors(extensions);

		Set<IEditorReference> editorsToClose = new HashSet<IEditorReference>();
		// closes the editors using the removed plugin
		for (IWorkbenchPage page : PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getPages()) {
			for (IEditorReference ref : page.getEditorReferences()) {
				IEditorPart editor = ref.getEditor(false);
				if (editor != null) {
					IContentAdapter adapter = (IContentAdapter) editor
							.getAdapter(IContentAdapter.class);
					if (adapter != null) {
						IContent content = ScreenUtil.getPrimaryContent(adapter
								.getContents());
						if (content != null) {
							String themeId = (String) content
									.getAttribute(ContentAttribute.THEME_ID
											.name());
							String modelId = (String) content
									.getAttribute(ContentAttribute.MODEL.name());
							for (IThemeDescriptor desc : themes) {
								if (desc.getId().equalsIgnoreCase(themeId)) {
									editorsToClose.add(ref);
									break;
								}
							}
							for (IThemeModelDescriptor desc : models) {
								if (desc.getId().equalsIgnoreCase(modelId)) {
									editorsToClose.add(ref);
									break;
								}
							}
						}
					}
				}
			}
		}
		return editorsToClose.toArray(new IEditorReference[editorsToClose
				.size()]);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

	private void storeValuesIntoPreferenceStore() {
	}

	public boolean performOk() {
		storeValuesIntoPreferenceStore();
		S60WorkspacePlugin.getDefault().savePluginPreferences();
		return true;
	}

	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
	}

	private void initializeDefaults() {
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return UtilsPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public void setValid(boolean b) {
		
		super.setValid(b);
	}

	private void calculateButtonSize(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int wHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point mSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(wHint, mSize.x);
		button.setLayoutData(data);
	}

	class BundleLabelProvider extends LabelProvider implements
			ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
		 *      int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
		 *      int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			Bundle bundle = (Bundle) element;
			switch (columnIndex) {
			case 0:
				return bundle.getSymbolicName();
			case 1:
				return (String) bundle.getHeaders().get(Constants.BUNDLE_NAME);
			default:
				return null;
			}
		}
	}
}
