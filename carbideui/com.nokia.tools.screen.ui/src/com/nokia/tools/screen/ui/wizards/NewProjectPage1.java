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
package com.nokia.tools.screen.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.IThemeContainerDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.AbstractCreateProjectAction;
import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;

/**
 * The first page of NewProjectWizard
 */
public class NewProjectPage1 extends WizardPage {
	public static final int CREATEDEFAULT = 0;

	public static final int COPYEXISTING = 2;

	public static final int COPYEXAMPLETHEMES = 3;

	private static final ImageDescriptor DEFAULT_IMAGE_DESCRIPTOR = UiPlugin
			.getImageDescriptor("icons/thumb_default.png");

	private Text txtName, txtTheme;

	private Composite scrComposite;

	private ScrolledComposite scroll;

	private Label lblDirectory, lblTheme;

	private StyledText txtInfo;

	private Button btnCopy, btnBrowse;

	private boolean ownTheme;

	private String selectedRelease = getDefaultRelease().getName();

	private int TEXT_LIMIT = 32;

	private Cursor arrowCursor, handCursor;

	/**
	 * Storing Wizard page UI state handling corrensponds to selection on
	 * wheather to create new theme, to link to existing or copy from template
	 */
	private int creationMode = 0;

	private Image infoImage;

	private String projectName;

	private String fileName;

	private Button btnExampleThemes;

	private Label lblExampleThemes;

	private Combo combo;
	private final static String PROPS_FILE = "exampleThemes.properties";
	private final static String PROP_THEMECOUNT = "exampleThemesCount";
	private final static String PROP_THEME = "exampleTheme.";
	private List<ExampleThemesBean> exampleThemeList;

	private String themeFolderLocation;
	private static String EXAMPLE_THEMES_EXTENSION_POINT_ID = "com.nokia.tools.theme.s60.exampleThemes";

	/**
	 * @return
	 */
	public int getThemeCreationMode() {
		return creationMode;
	}

	/**
	 * @param newMode
	 *            mode to be set called from UI event handlers
	 */
	public void setThemeCreationMode(int newMode) {
		creationMode = newMode;
	}

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewProjectPage1(ISelection selection) {
		// @Externalize
		super("wizardPage");
		setTitle(WizardMessages.New_Project_Banner_Title);
		setDescription(WizardMessages.New_Project_Banner_Message1);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		arrowCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_ARROW);
		handCursor = new Cursor(getShell().getDisplay(), SWT.CURSOR_HAND);

		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		mainComposite.setLayout(layout);
		layout.numColumns = 2;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;

		Label lblName = new Label(mainComposite, SWT.NULL);
		lblName.setText(WizardMessages.New_Project_Project_Name_Label);

		txtName = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
		if (projectName != null) {
			txtName.setText(projectName);
		}
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		txtName.setLayoutData(gd);
		txtName.setTextLimit(TEXT_LIMIT);
		txtName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IPath workspace = ResourcesPlugin.getWorkspace().getRoot()
						.getLocation();
				String projectName = txtName.getText().trim();
				lblDirectory
						.setText(workspace.append(projectName).toOSString());
				dialogChanged();
			}
		});

		Label directoryLabel = new Label(mainComposite, SWT.NULL);
		directoryLabel.setText(WizardMessages.New_Project_Directory_Label);

		lblDirectory = new Label(mainComposite, SWT.SINGLE);
		lblDirectory.setText(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 376;
		lblDirectory.setLayoutData(gd);

		Group grpContent = new Group(mainComposite, SWT.NONE);
		grpContent.setText(WizardMessages.New_Project_Theme_Content_Label);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		grpContent.setLayoutData(gd);
		layout = new GridLayout();
		layout.makeColumnsEqualWidth = false;
		layout.numColumns = 3;
		layout.marginHeight = 9;
		layout.marginWidth = 9;
		layout.verticalSpacing = 7;
		grpContent.setLayout(layout);

		Button btnDefault = new Button(grpContent, SWT.RADIO);
		btnDefault.setText(WizardMessages.New_Project_Default_Button);
		btnDefault.setSelection(true);
		gd = new GridData();
		gd.horizontalSpan = 3;
		btnDefault.setLayoutData(gd);
		btnDefault.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setOwnTheme(false);
				//Creation of lblExampleThemes and combo are done
				//only if we have example themes 
				if (lblExampleThemes != null) {
					lblExampleThemes.setEnabled(false);
				}
				if (combo != null) {
					combo.setEnabled(false);
				}
				setThemeCreationMode(CREATEDEFAULT);
				dialogChanged();
				setInfoText();
				if (txtName.getText().trim() == "")
					txtName.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		btnCopy = new Button(grpContent, SWT.RADIO);
		btnCopy.setText(WizardMessages.New_Project_Copy_Existing_Theme_Button);
		btnCopy.setSelection(false);
		gd = new GridData();
		gd.horizontalSpan = 3;
		btnCopy.setLayoutData(gd);
		btnCopy.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setOwnTheme(true);
				//Creation of lblExampleThemes and combo are done
				//only if we have example themes 
				if (lblExampleThemes != null) {
					lblExampleThemes.setEnabled(false);
				}
				if (combo != null) {
					combo.setEnabled(false);
				}
				setThemeCreationMode(COPYEXISTING);
				dialogChanged();
				setInfoText();
				txtTheme.setFocus();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Composite contentComposite = new Composite(grpContent, SWT.NULL);
		layout = new GridLayout();
		contentComposite.setLayout(layout);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		contentComposite.setLayoutData(gd);

		lblTheme = new Label(contentComposite, SWT.NULL);
		lblTheme.setText(WizardMessages.New_Project_Project_Label);
		lblTheme.setEnabled(false);

		txtTheme = new Text(contentComposite, SWT.BORDER | SWT.SINGLE);
		if (fileName != null) {
			txtTheme.setText(fileName);
		}
		txtTheme.setEnabled(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 288;
		txtTheme.setLayoutData(gd);
		txtTheme.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		btnBrowse = new Button(grpContent, SWT.PUSH);
		btnBrowse.setEnabled(false);
		btnBrowse.setText(WizardMessages.New_Project_Browse_Button);
		initializeDialogUnits(btnBrowse);
		setButtonLayoutData(btnBrowse);
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		exampleThemeList = createInput();		
		if (exampleThemeList != null && !exampleThemeList.isEmpty()) {
			btnExampleThemes = new Button(grpContent, SWT.RADIO);
			btnExampleThemes
					.setText(WizardMessages.New_Project_Copy_Example_Theme_Button);
			btnExampleThemes.setSelection(false);
			gd = new GridData();
			gd.horizontalSpan = 3;
			btnExampleThemes.setLayoutData(gd);
			btnExampleThemes.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					setOwnTheme(false);
					lblExampleThemes.setEnabled(true);
					combo.setEnabled(true);
					setThemeCreationMode(COPYEXAMPLETHEMES);
					dialogChanged();
					setInfoText();
					txtTheme.setFocus();
				}

				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});

			Composite exampleThemesContent = new Composite(grpContent, SWT.NULL);
			layout = new GridLayout();
			exampleThemesContent.setLayout(layout);
			layout.numColumns = 2;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.verticalSpacing = 7;
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			exampleThemesContent.setLayoutData(gd);

			lblExampleThemes = new Label(exampleThemesContent, SWT.NULL);
			lblExampleThemes
					.setText(WizardMessages.New_Project_Project_ExampleTheme_Label);
			lblExampleThemes.setEnabled(false);

			combo = new Combo(exampleThemesContent, SWT.READ_ONLY);
			combo.setItems(prepareComboList());
			combo.setSize(200, 200);
			combo.setEnabled(false);
			combo.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					Combo c = (Combo) e.getSource();
					ExampleThemesBean bean = findExampleObject(c.getText());
					txtName.setText(bean.getName());
					setFileName(themeFolderLocation + bean.getPath());

				}

				private ExampleThemesBean findExampleObject(String name) {
					for (ExampleThemesBean bean : exampleThemeList) {
						if (bean.getName()
								.equals(
										name
												.substring(0,
														name.lastIndexOf("<"))
												.trim())) {
							return bean;
						}
					}

					return null;
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					// TODO Auto-generated method stub
				}
			});
		}
		Composite infoComposite = new Composite(mainComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.heightHint = 90;
		infoComposite.setLayoutData(gd);
		layout = new GridLayout();
		infoComposite.setLayout(layout);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;

		Label lblInfoImage = new Label(infoComposite, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblInfoImage.setLayoutData(gd);
		infoImage = ISharedImageDescriptor.ICON16_INFO.createImage();
		lblInfoImage.setImage(infoImage);

		scroll = new ScrolledComposite(infoComposite, SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		scroll.setLayoutData(gd);
		scroll.setLayout(new FillLayout());
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
		scroll.getVerticalBar().setIncrement(13);
		scroll.getVerticalBar().setPageIncrement(
				2 * scroll.getVerticalBar().getIncrement());

		scrComposite = new Composite(scroll, SWT.NONE);
		scroll.setContent(scrComposite);
		gd = new GridData(GridData.FILL_BOTH);
		scrComposite.setLayoutData(gd);
		layout = new GridLayout();
		scrComposite.setLayout(layout);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;

		txtInfo = new StyledText(scrComposite, SWT.WRAP | SWT.READ_ONLY);
		txtInfo.setBackground(txtInfo.getParent().getBackground());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 363;
		txtInfo.setLayoutData(gd);

		scroll.setMinSize(scrComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		setControl(mainComposite);
		setPageComplete(false);
		setSelectedRelease(selectedRelease);

		if (getThemeCreationMode() == COPYEXISTING) {
			btnDefault.setSelection(false);
			btnCopy.setSelection(true);
			setOwnTheme(true);
			setThemeCreationMode(COPYEXISTING);
			dialogChanged();
			setInfoText();
			scrComposite.layout();
			txtTheme.setFocus();
		}

		if (txtTheme.getText() != "")
			txtTheme.setSelection(txtTheme.getText().length(), txtTheme
					.getText().length());

		txtName.setFocus();

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getShell(),
				NewProjectWizard.NEW_PROJECT_CONTEXT);
	}

	private ImageDescriptor getReleaseImageDescriptor(
			IThemeContainerDescriptor descriptor) {
		ImageDescriptor desc = descriptor.getLargeIconDescriptor();
		if (desc != null) {
			return desc;
		}
		return DEFAULT_IMAGE_DESCRIPTOR;
	}

	private String getReleaseDescription(String release) {
		for (IThemeContainerDescriptor descriptor : getReleases()) {
			if (descriptor.getName().equals(release)) {
				String desc = descriptor.getDescription();
				if (desc != null) {
					return desc;
				}
			}
		}
		return null;
	}

	private void setSelectedRelease(String release) {
		selectedRelease = release;
		setInfoText();
	}

	// Sets info text based on current theme creation mode/selected release
	private void setInfoText() {
		if (getThemeCreationMode() == COPYEXISTING
				|| getThemeCreationMode() == COPYEXAMPLETHEMES) {
			txtInfo.setText(WizardMessages.New_Project_lblInfoText_CopyText);
		} else {
			// Build info text
			String intro = WizardMessages.New_Project_lblInfoText_DefaultIntro;
			txtInfo.setText(intro);
		}
		scroll.setOrigin(0, 0);
		scroll.setMinSize(scrComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scroll.redraw();
		scrComposite.layout();
	}

	protected IThemeContainerDescriptor getDefaultRelease() {
		for (IThemeContainerDescriptor desc : getReleases()) {
			if (desc.isDefault()) {
				return desc;
			}
		}
		return null;
	}

	protected IThemeContainerDescriptor[] getReleases() {
		IThemeContainerDescriptor[] descriptors = ThemePlatform
				.getContainerDescriptors();

		Arrays.sort(descriptors, new Comparator<IThemeContainerDescriptor>() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 * java.lang.Object)
			 */
			public int compare(IThemeContainerDescriptor o1,
					IThemeContainerDescriptor o2) {
				if (o1.isDefault()) {
					return -1;
				}
				if (o2.isDefault()) {
					return 1;
				}
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		return descriptors;
	}

	protected String[] getSizes(IThemeModelDescriptor model) {
		IDevice[] devices = ThemePlatform.getDevicesByThemeModelId(model
				.getId());
		String[] sizes = new String[devices.length];
		if (null != devices) {
			for (int i = 0; i < devices.length; i++) {
				Display display = devices[i].getDisplay();
				sizes[i] = display.getWidth() + "x" + display.getHeight();
			}
		}
		return sizes;
	}

	private void setOwnTheme(boolean b) {
		ownTheme = b;
		btnBrowse.setEnabled(b);
		txtTheme.setEnabled(b);
		lblTheme.setEnabled(b);
		dialogChanged();
	}

	public boolean getOwnTheme() {
		return ownTheme;
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */
	private void handleBrowse() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		// @Externalize
		dialog.setText(WizardMessages.New_Project_themeFileDialog_Title);
		dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot()
				.getLocation().toOSString());
		dialog.setFilterExtensions(getFileExtensions());
		if (txtTheme.getText() != "")
			dialog.setFileName(txtTheme.getText());
		if (dialog.open() != null) {
			String separator = File.separator;
			String strSelProject = (new Path(dialog.getFilterPath() + separator
					+ dialog.getFileName())).toOSString();
			txtTheme.setText(strSelProject);
		}
	}

	/**
	 * Ensures that both text fields are set. and other contributed wizard
	 * validators are executed
	 */
	private void dialogChanged() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		String fileName = getFileName();
		IStatus nameStatus = workspace.validateName(getProjectName(),
				IResource.PROJECT);

		if (getProjectName().equals("plugin")
				&& selectedRelease.equals("S60 Audio Theme")) {
			updateStatus(WizardMessages.Audio_Theme_Plugin_Name_Error);
			return;
		}
		if (getProjectName().length() == 0) {
			updateStatus(WizardMessages.New_Project_No_Name_Error);
			return;
		}

		if (!nameStatus.isOK()) {
			updateStatus(WizardMessages.New_Project_Wrong_Char_Error);
			return;
		}

		IProject project = ResourceUtils.getProjectByName(getProjectName());
		if (project != null && project.exists()) {
			updateStatus(WizardMessages.New_Project_Exist_Error);
			return;
		}

		IWizardValidator[] validators = ExtensionManager
				.getWizardValidators(this.getClass());

		for (IWizardValidator validator : validators) {
			String errorMessage = validator.validate(this);
			if (!IWizardValidator.NO_ERROR.equals(errorMessage)) {
				updateStatus(errorMessage);
				return;

			}
		}

		if (ownTheme) {
			if (fileName.length() == 0) {
				updateStatus(WizardMessages.New_Project_No_File_Error);
				return;
			}

			Path path = new Path(getFileName());
			File file1 = path.removeLastSegments(1).toFile();
			File file2 = path.removeLastSegments(0).toFile();
			String ext = path.getFileExtension();

			if (!file1.exists()) {
				updateStatus(WizardMessages.New_Project_No_Path_Error);
				return;
			}

			if (ext == null)
				if (!file2.exists()) {
					updateStatus(WizardMessages.New_Project_No_Path_Error);
					return;
				}

			if (ext == null) {
				updateStatus(generateExtnsErrorMessage(getFileExtensionsAsList()));
				return;
			} else {
				if (!(getFileExtensionsAsList().contains(ext))) {
					updateStatus(generateExtnsErrorMessage(getFileExtensionsAsList()));
					return;
				} else {
					if (!isValidThemeFile(ext, path.lastSegment())) {
						boolean isSupports = false;
						for (IContributorDescriptor desc : ExtensionManager
								.getContributorDescriptors()) {
							AbstractCreateProjectAction operation = (AbstractCreateProjectAction) desc
									.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT);
							if (operation != null
									&& operation.supportsPath(getFileName())) {
								isSupports = true;
								break;
							}
						}
						if (!isSupports) {
							updateStatus(WizardMessages.New_Project_Invalid_Themefile_error);
							return;
						}
					}
				}
			}

			if (!FileUtils.isFileValidAndAccessible(path)) {
				updateStatus(WizardMessages.New_Project_Not_Exist_Error);
				return;
			}

		}
		updateStatus(null);
	}

	private boolean isValidThemeFile(String fileExt, String fileName) {
		if (fileName == null || fileExt == null)
			return false;
		HashMap<String, ArrayList<String>> map = getSupportedFileNames();
		ArrayList<String> fileNames = map.get(fileExt);
		if (fileNames == null)
			return false;
		return fileNames.contains(fileName);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getProjectName() {
		return txtName.getText().trim();
	}

	public void setProjectName(String text) {
		if (txtName != null) {
			txtName.setText(text);
		}
		projectName = text;
	}

	public String getFileName() {
		return txtTheme.getText().trim();
	}

	public void setFileName(String text) {
		if (txtTheme != null) {
			txtTheme.setText(text);
		}
		fileName = text;
	}

	public IThemeModelDescriptor getSelectedModel() {
		IThemeContainerDescriptor desc = ThemePlatform
				.getContainerDescriptorByName(selectedRelease);
		if (desc != null) {
			IThemeModelDescriptor model = ThemePlatform
					.getDefaultThemeModelDescriptor(desc.getId());
			return model;
		}
		return null;
	}

	public String getRelease() {
		IThemeModelDescriptor desc = getSelectedModel();
		if (desc == null) {
			return null;
		}
		return desc.getId();
	}

	public String getResolution() {
		String[] sizes = getSizes(getSelectedModel());
		if (sizes.length > 0) {
			return sizes[0];
		}
		return null;
	}

	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp(
				NewProjectWizard.NEW_PROJECT_CONTEXT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (infoImage != null) {
			infoImage.dispose();
		}
		if (arrowCursor != null) {
			arrowCursor.dispose();
		}
		if (handCursor != null) {
			handCursor.dispose();
		}
		super.dispose();
	}

	private ArrayList<String> getFileExtensionsAsList() {
		ArrayList<String> list = new ArrayList<String>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractCreateProjectAction operation = (AbstractCreateProjectAction) desc
					.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT);
			if (operation != null) {
				String[] temp = operation.getFileExtensions();
				for (String string : temp) {
					if (!list.contains(string))
						list.add(string);
				}
			}
		}
		return list;
	}

	private HashMap<String, ArrayList<String>> getSupportedFileNames() {
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {

			if (null != desc
					.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT)
					& desc
							.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT) instanceof AbstractCreateProjectAction) {

				AbstractCreateProjectAction operation = (AbstractCreateProjectAction) desc
						.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT);
				if (operation != null) {
					String[] fileExt = operation.getFileExtensions();
					ArrayList<String> temp = operation.getSupportedFileNames();
					if (fileExt != null && temp != null) {
						for (String string : fileExt) {
							map.put(string, temp);
						}
					}
				}
			}
		}
		return map;
	}

	private String[] getFileExtensions() {
		ArrayList<String> list = new ArrayList<String>();
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractCreateProjectAction operation = (AbstractCreateProjectAction) desc
					.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT);
			if (operation != null) {
				String[] temp = operation.getFileExtensions();
				for (String string : temp) {
					if (!list.contains("*." + string))
						list.add("*." + string);
				}
			}
		}
		StringBuffer buffer = new StringBuffer();
		int length = list.size();
		for (int i = 0; i < length; i++) {
			buffer.append(list.get(i));
			if (i == length - 1)
				continue;
			buffer.append(";");
		}

		return new String[] { buffer.toString() };
	}

	private String generateExtnsErrorMessage(ArrayList<String> extns) {
		StringBuffer buffer = new StringBuffer();
		int length = extns.size();
		for (int i = 0; i < length; i++) {
			if (!extns.get(i).startsWith("."))
				buffer.append("*." + extns.get(i));
			else
				buffer.append(extns.get(i));
			if (i == length - 1)
				continue;
			if (i == length - 2)
				buffer.append(" or ");
			else
				buffer.append(", ");
		}

		return WizardMessages.New_Project_Extension_Error + buffer.toString();
	}

	/**
	 * Reads the property file and creates list of example themes
	 * 
	 * @return List of {@link ExampleThemesBean}
	 */
	private List<ExampleThemesBean> createInput() {
		String exampleThemesContributor = getContributorName();
		if (exampleThemesContributor != null) {
			List<ExampleThemesBean> newThemeList = new ArrayList<ExampleThemesBean>();

			URL propUrl = Platform.getBundle(exampleThemesContributor)
					.getEntry(PROPS_FILE);
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
						themeBean.setModel(props.getProperty(PROP_THEME + i
								+ ".model"));
						newThemeList.add(themeBean);

					}
				}
				return newThemeList;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * This class is used to store data for example themes viewer
	 */
	class ExampleThemesBean {
		private String name;
		private String path;
		private String model;

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
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
	 * Check whether the example themes plug-in is available or not
	 * 
	 * @return name of the plug-in which is contributing for example theme
	 */
	private String getContributorName() {
		IExtension[] extensions = PlatformExtensionManager
				.getExtensions(EXAMPLE_THEMES_EXTENSION_POINT_ID);
		if (extensions != null & extensions.length > 0) {
			return extensions[0].getContributor().getName();
		}
		return null;
	}

	/**
	 * Prepare combo box input from the example theme list
	 * 
	 * @return String[]
	 */
	private String[] prepareComboList() {
		if (exampleThemeList != null) {
			int i = 0;
			String[] themeNamesList = new String[exampleThemeList.size()];
			for (ExampleThemesBean bean : exampleThemeList) {
				themeNamesList[i] = (bean.getName() + " < " + bean.getModel() + " >");
				i++;
			}
			return themeNamesList;
		}
		// returning new string[1];instead of null
		return new String[1];
	}

}