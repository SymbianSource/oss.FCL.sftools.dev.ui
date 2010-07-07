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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.nokia.tools.theme.s60.cstore.ComponentPoolBackend;
import com.nokia.tools.theme.s60.examplethemes.ExampleThemeProvider;
import com.sun.org.apache.xpath.internal.XPathAPI;

public class ExampleThemesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener {
	public static final String PREFERENCES_CONTEXT = "com.nokia.tools.s60.ide"
			+ '.' + "example_themes_preferences_context";
	private final static String PROPS_FILE = "exampleThemes.properties";
	private final static String PROP_THEMECOUNT = "exampleThemesCount";
	private final static String PROP_THEME = "exampleTheme.";
	private final static String THEME_FOLDER = "ExampleThemes" + File.separator;
	private static final String THEME_EXT = ".tdf";
	private static final String PRJ_XPATH = "/projectDescription/name/text()";
	private static final String MODEL_XPATH = "/skin/phone/model[1]";
	private static final String MODEL = "MODEL";
	private static final String PRJ_NAME = "PRJ";
	private static final String TDFPATH = "TDFPATH";
	private ExampleThemesBean selectedBean = null;
	private Button btnRemove;
	private Button btnAdd;
	private TableViewer exampleThemesViewer;
	private boolean updateColSizes;
	private String[] externalToolFilter = { "*.zip" };
	private String selImportTheme;
	private String source;
	private String themeFolderLocation;
	private URL propUrl;
	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

	private enum TYPE {
		Add, Remove
	};

	public ExampleThemesPreferencePage() {
		// TODO Auto-generated constructor stub
	}

	public ExampleThemesPreferencePage(String title) {
		super(title);

		// TODO Auto-generated constructor stub
	}

	public ExampleThemesPreferencePage(String title, ImageDescriptor image) {
		super(title, image);

		// TODO Auto-generated constructor stub
	}

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

		setDescription(Messages.ExampleThemesPreferencePage_PreferencesDescriptionLabel);
		createDescriptionLabel(generalComposite);
		Composite exampleThemesComposite = new Composite(generalComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		exampleThemesComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		exampleThemesComposite.setLayout(layout);

		Label lblPlugins = new Label(exampleThemesComposite, SWT.NONE);
		lblPlugins
				.setText(Messages.ExampleThemesPreferencePage_PluginsTitleLabel);
		gd = new GridData();
		gd.verticalIndent = 5;
		gd.horizontalSpan = 2;
		lblPlugins.setLayoutData(gd);

		Composite tableComposite = new Composite(exampleThemesComposite, SWT.NONE);
		FillLayout layout2 = new FillLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		tableComposite.setLayout(layout2);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		tableComposite.setLayoutData(gd);

		exampleThemesViewer = new TableViewer(tableComposite, SWT.SINGLE
				| SWT.FULL_SELECTION | SWT.BORDER);

		Table exampleThemesTable = exampleThemesViewer.getTable();
		final TableColumn colName = new TableColumn(exampleThemesTable,
				SWT.LEFT);
		colName.setText("Name");
		colName.setWidth(150);

		final TableColumn colModel = new TableColumn(exampleThemesTable,
				SWT.LEFT);
		colModel.setText("Platform");
		colModel.setWidth(150);

		exampleThemesTable.setHeaderVisible(true);
		exampleThemesTable.setLinesVisible(true);

		exampleThemesViewer.setColumnProperties(new String[] { "Name",
				"Platform"});

		exampleThemesViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						// refresh buttons state
						ISelection s = event.getSelection();
						int index = exampleThemesViewer.getTable()
								.getSelectionIndex();

						if (index >= 0) {
							btnRemove.setEnabled(true);
							// getting the selected bean object, later this will
							// used on delete operation
							selectedBean = (ExampleThemesBean) ((IStructuredSelection) s)
									.getFirstElement();
						}
					}
				});

		exampleThemesViewer.getTable().addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				if (updateColSizes) {
					Table tbl = exampleThemesViewer.getTable();
					int w = tbl.getClientArea().width;
					int c1 = w / 5 * 3;
					int c2 = w - c1;
					updateColSizes = false;
					colName.setWidth(c1);
					colModel.setWidth(c2);
					updateColSizes = true;
				}
			}
		});

		exampleThemesViewer.setContentProvider(new ArrayContentProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.ArrayContentProvider#getElements(java
			 * .lang.Object)
			 */
			@Override
			public Object[] getElements(Object inputElement) {
				List<ExampleThemesBean> list = (List<ExampleThemesBean>) inputElement;
				return list.toArray();
			}
		});
		exampleThemesViewer.setLabelProvider(new ExampleThemesLabelProvider());
		exampleThemesViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ExampleThemesBean a = (ExampleThemesBean) e1;
				ExampleThemesBean b = (ExampleThemesBean) e2;

				return a.getName().compareTo(b.getName());
			}
		});

		exampleThemesViewer.setInput(createInput());

		btnAdd = new Button(exampleThemesComposite, SWT.PUSH);
		btnAdd.setText(Messages.ExampleThemesPreferencePage_AddButtonLabel);
		calculateButtonSize(btnAdd);
		btnAdd.addSelectionListener(this);

		btnRemove = new Button(exampleThemesComposite, SWT.PUSH);
		btnRemove
				.setText(Messages.ExampleThemesPreferencePage_RemoveButtonLabel);
		calculateButtonSize(btnRemove);
		btnRemove.setEnabled(false);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		btnRemove.setLayoutData(gd);
		btnRemove.addSelectionListener(this);
		noDefaultAndApplyButton();
		return parent;
	}

	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param button
	 */
	private void calculateButtonSize(Button button) {
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		int wHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		Point mSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		data.widthHint = Math.max(wHint, mSize.x);
		button.setLayoutData(data);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 */
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == btnAdd) {
			FileDialog fd = new FileDialog(new Shell(), SWT.SYSTEM_MODAL
					| SWT.OPEN);
			fd.setFilterExtensions(externalToolFilter);

			if (selImportTheme != "") {
				fd.setFileName(selImportTheme);
			}

			source = fd.open();
			if (source != null) {
				final Map<String, String> details = getThemeDetails();
				final String project = details.get(PRJ_NAME) == null ? null
						: details.get(PRJ_NAME);
				final String model = details.get(MODEL) == null ? null
						: details.get(MODEL);
				final String tdfPath = details.get(TDFPATH) == null ? null: details.get(TDFPATH);
				try {
					if (project != null) {
						// check for importing theme is exist already
						// if so error message dialog is shown
						final String path = themeFolderLocation + THEME_FOLDER;
						if (new File(path + project).mkdir()) {
							ProgressMonitorDialog dialog = new ProgressMonitorDialog(
									Display.getCurrent().getActiveShell());
							dialog.run(true, false,
									new IRunnableWithProgress() {
										public void run(IProgressMonitor monitor)
												throws InvocationTargetException,
												InterruptedException {
											monitor
													.beginTask(
															NLS
																	.bind(
																			Messages.ExampleThemesPreferencePage_AddThemeMsg,
																			project),
															IProgressMonitor.UNKNOWN);
											try {												
												copyZipDirectory(source, new File(path+project));
												// Writing into properties file
												String value = THEME_FOLDER
														+ project
														+ tdfPath;
												writeIntoFile(value,model,TYPE.Add);
												clearAndRefreshComponentPool();
												Display.getDefault().syncExec(
														new Runnable() {
															public void run() {
																refreshTableViewer();
															}
														});
											} catch (Exception e) {
												e.printStackTrace();
											}
											monitor.done();

										}
									});
						} else {
							MessageDialog
									.openError(
											null,
											Messages.ExampleThemesPreferencePage_ErrorNoteTitle,
											NLS
													.bind(
															Messages.ExampleThemesPreferencePage_ThemeAlreadyExist,
															project));
						}
					} else {
						MessageDialog
								.openError(
										null,
										Messages.ExampleThemesPreferencePage_ErrorNoteTitle,
										Messages.ExampleThemesPreferencePage_NotValidZip);
					}
				} catch (Throwable e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		if (e.getSource() == btnRemove) {
			if (selectedBean != null) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(
						Display.getCurrent().getActiveShell());
				try {
					dialog.run(true, false, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor)
								throws InvocationTargetException,
								InterruptedException {
							monitor
									.beginTask(
											NLS
													.bind(
															Messages.ExampleThemesPreferencePage_RemoveThemeMsg,
															selectedBean
																	.getName()),
											IProgressMonitor.UNKNOWN);
							String path = selectedBean.getPath().substring(selectedBean.getPath().indexOf(File.separator)+1);
							String toRemove = themeFolderLocation
									+ THEME_FOLDER + selectedBean.getName();
							toRemove = themeFolderLocation + THEME_FOLDER +path.substring(0,path.indexOf(File.separator));
							writeIntoFile(selectedBean.getKey(),null,TYPE.Remove);
							deleteThemeDir(new File(toRemove));
							clearAndRefreshComponentPool();
							Display.getDefault().syncExec(new Runnable() {
								public void run() {
									refreshTableViewer();
									// refresh buttons state
									if ((exampleThemesViewer.getTable()
											.getItemCount()) == 0)
										btnRemove.setEnabled(false);
								}
							});
							monitor.done();
						}

					});
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reads the property file and creates input for ContentProvider
	 * 
	 * @return List of {@link ExampleThemesBean}
	 */
	private List<ExampleThemesBean> createInput() {
		List<ExampleThemesBean> newThemeList = new ArrayList<ExampleThemesBean>();
		String exampleThemesContributor = ExampleThemeProvider
				.getContributorName();
		if (exampleThemesContributor != null) {
			propUrl = Platform.getBundle(exampleThemesContributor).getEntry(
					PROPS_FILE);
			InputStream in;

			try {
				in = propUrl.openStream();

				Properties props = new Properties();
				props.load(in);
				in.close();

				int count = Integer
						.parseInt(props.getProperty(PROP_THEMECOUNT));
				themeFolderLocation = FileLocator.toFileURL(
						FileLocator.find(Platform
								.getBundle(exampleThemesContributor), new Path(
								File.separator), null)).getPath();

				for (int i = 1; i <= count; i++) {
					ExampleThemesBean themeBean = new ExampleThemesBean();
					String themePath = props.getProperty(PROP_THEME + i).trim();
					if (themePath != null && !themePath.equals("")) {
						themeBean.setName(themePath.substring(themePath
								.lastIndexOf(File.separator) + 1, themePath
								.lastIndexOf(".")));
						themeBean.setPath(themePath);
						themeBean.setKey(PROP_THEME + i);
						themeBean.setModel(props.getProperty(PROP_THEME + i
								+ ".model"));
						newThemeList.add(themeBean);
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return newThemeList;
	}

	class ExampleThemesLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java
		 * .lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.
		 * lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			ExampleThemesBean bean = (ExampleThemesBean) element;

			switch (columnIndex) {
			case 0:
				return bean.getName();

			case 1:
				return bean.getModel();
			default:
				return null;
			}
		}
	}

	/**
	 * This class is used to store data for example themes viewer
	 */
	class ExampleThemesBean {
		private String name;
		private String path;
		private String key;
		private String model;

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}
	}

	/**
	 * Checks whether the even zip file is a valid or not, and return theme
	 * details
	 * 
	 * @return Map with theme details or empty map.
	 */
	public Map<String, String> getThemeDetails() {
		Map<String, String> themeDetails = new HashMap<String, String>();
		boolean projectFileExists = false;
		boolean themeFileExists = false;
		ZipFile zf = null;
		try {
			zf = new ZipFile(source);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ZipEntry entry = null;

		for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
			entry = (ZipEntry) entries.nextElement();

			if (!entry.isDirectory()) {
				InputStream is;
				if (new Path(entry.getName()).lastSegment().toLowerCase()
						.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
					try {
						is = zf.getInputStream(entry);
						themeDetails.put(PRJ_NAME, parseXmlFile(is, PRJ_XPATH));
						projectFileExists = true;
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (new Path(entry.getName()).lastSegment().toLowerCase()
						.endsWith(THEME_EXT)) {
					try {
						String tdfpath = entry.getName();
						tdfpath= tdfpath.replace("/", File.separator);
						if(!tdfpath.startsWith(File.separator)){
							tdfpath = File.separator+tdfpath;
						}
						is = zf.getInputStream(entry);
						themeDetails.put(MODEL, parseXmlFile(is, MODEL_XPATH));
						themeDetails.put(TDFPATH, tdfpath);
						themeFileExists = true;
						is.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (projectFileExists && themeFileExists) {
					break;
				}

			}
		}
		return themeDetails;
	}

	/**
	 * Gets the theme details like theme name and model on which it is build
	 * 
	 * @param is
	 *            inputStream for the .tdf or .project file
	 * @param xpath
	 *            xpath for getting theme details
	 * @return theme name and model on which it is build
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	private String parseXmlFile(InputStream is, String xpath)
			throws SAXException, IOException, ParserConfigurationException,
			TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		builder.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String arg0, String arg1)
					throws SAXException, IOException {
				return new InputSource(new StringReader(""));

			}
		});
		Document xml = builder.parse(is);
		Node nameNode = XPathAPI.selectSingleNode(xml, xpath);
		if (nameNode.getAttributes() != null) {
			NamedNodeMap nodeMap = nameNode.getAttributes();
			return nodeMap.item(0).getNodeValue();
		}
		if (nameNode != null) {
			return nameNode.getNodeValue();
		} else {
			return null;
		}
	}

	/**
	 * Writes data into property file
	 * 
	 * @param value
	 *            value to be added into property file
	 * @param type
	 *            type of operation {@link TYPE}
	 */
	private void writeIntoFile(String path,String model,TYPE type) {
		InputStream in;
		try {
			in = propUrl.openStream();
			Properties props = new Properties();
			props.load(in);
			int count = Integer.parseInt(props.getProperty(PROP_THEMECOUNT));
			if (type.toString().equalsIgnoreCase("Add")) {
				props.setProperty(PROP_THEMECOUNT, String.valueOf(++count));
				props.setProperty(PROP_THEME + count, path);
				props.setProperty(PROP_THEME + count+".model", model);
			} else if (type.toString().equalsIgnoreCase("Remove")) {
				// Cleanup the properties file
				props.setProperty(path, "");
				props.setProperty(path+".model","");
			}
			props.store(new FileOutputStream(themeFolderLocation
					+ propUrl.getFile()), null);
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Refresh ContentProvider data after add/delete operation
	 */
	private void refreshTableViewer() {
		// set input once again for refresh
		exampleThemesViewer.setInput(createInput());
		exampleThemesViewer.refresh();
	}

	/**
	 * Deletes theme directory tree
	 * 
	 * @param toDelete
	 *            reference to root directory of the theme file to delete
	 * @return true on successful delete else false
	 */
	private boolean deleteThemeDir(File toDelete) {
		if (toDelete.isDirectory()) {
			String[] children = toDelete.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteThemeDir(new File(toDelete, children[i]));
				if (!success) {
					return false;
				}
			}
		} // The directory is now empty so delete it return dir.delete();
		return toDelete.delete();
	}

	/**
	 * Clears and refresh list of example themes Will be used from Take Content
	 * From->Theme List
	 */
	private void clearAndRefreshComponentPool() {
		// Clears the list of example themes
		ComponentPoolBackend.clear();
		// Create list of example with newly added themes
		ComponentPoolBackend.refresh();
	}
	
	/**
	 * Copy zip directory.
	 * 
	 * @param sourceLocation
	 *            the source location
	 * @param targetLocation
	 *            the target location
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void copyZipDirectory(String sourceLocation, File targetLocation)
			throws IOException {
		ZipFile zipFile = new ZipFile(sourceLocation);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		if (!targetLocation.exists()) {
			targetLocation.mkdir();
		}

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String entryName = entry.getName();

			if (entry.isDirectory()) {
				if (!new File(targetLocation, entry.getName()).exists()) {
					(new File(targetLocation, entry.getName())).mkdirs();
				}
				continue;
			}

			File newFile = new File(entryName);
			if (newFile.getParent() != null) {
				File directory = new File(targetLocation, newFile.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
			}
			copy(zipFile.getInputStream(entry), new BufferedOutputStream(
					new FileOutputStream(new File(targetLocation, entry
							.getName()))));
		}

		zipFile.close();

	}

	/**
	 * Copy.
	 * 
	 * @param in
	 *            the input stream
	 * @param bout
	 *            the buffered output stream
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void copy(InputStream in, BufferedOutputStream bout)
			throws IOException {
		while (true) {
			synchronized (buffer) {
				int len = in.read(buffer);
				if (len == -1) {
					break;
				}
				bout.write(buffer, 0, len);
			}
		}

		in.close();
		bout.close();
	}

}
