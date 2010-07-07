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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.FileEditorInput;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.AbstractOpenProjectAction;
import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

/**
 * This dialog is for opening an existing project from the current workspace.
 * Also an existing theme project can be copied or linked by creating a new
 * project in the workspace.
 * 
 */
public class OpenProjectDialog extends BrandedTitleAreaDialog {

	public static final String OPEN_PROJECT_DIALOG_CONTEXT = "com.nokia.tools.s60.ide.openProjectWizard_context"; //$NON-NLS-1$

	private String[] strSelProjects;

	private Boolean dialogComplete = false;

	Shell shell;

	private Button btnRad1, btnRad2, btnBrowse, btnOpen;

	private List lstProjects;

	private Label lblDir, lblInfoImage, lblInfo;

	private Text txtLocation;

	private String[] filterExt;

	private Image labelImage;

	/**
	 * The constructor
	 */
	public OpenProjectDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected Control createDialogArea(Composite parent) {

		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
				OpenProjectDialog.OPEN_PROJECT_DIALOG_CONTEXT);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		setTitle(WizardMessages.Open_Project_Banner_Title);
		setMessage(WizardMessages.Open_Project_Banner_Message);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 8;

		btnRad1 = new Button(container, SWT.RADIO);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		btnRad1.setLayoutData(gd);
		btnRad1.setText(WizardMessages.Open_Project_btnRad1_Text);
		btnRad1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateStates();
				updateStatus(null);
			}
		});

		lstProjects = new List(container, SWT.V_SCROLL | SWT.BORDER
				| SWT.H_SCROLL | SWT.MULTI);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 424;
		gd.heightHint = 202;
		gd.horizontalSpan = 3;
		gd.minimumHeight = 30;
		lstProjects.setLayoutData(gd);
		lstProjects.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				strSelProjects = lstProjects.getSelection();
				updateStates();
			}
		});
		lstProjects.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				if (lstProjects.getSelectionIndex() != -1) {
					strSelProjects = lstProjects.getSelection();
					updateStates();
					buttonPressed(IDialogConstants.OK_ID);
				}
			}

			public void mouseDown(MouseEvent e) {
			
			}

			public void mouseUp(MouseEvent e) {
				
			}
		});

		btnRad2 = new Button(container, SWT.RADIO);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		btnRad2.setLayoutData(gd);
		btnRad2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				strSelProjects = new String[] { txtLocation.getText() };
				dialogChanged();
				updateStates();
				txtLocation.setFocus();
			}
		});
		btnRad2.setText(WizardMessages.Open_Project_btnRad2_Text);

		lblDir = new Label(container, SWT.NONE);
		lblDir.setText(WizardMessages.Open_Project_lblDir_Text);

		txtLocation = new Text(container, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		txtLocation.setLayoutData(gd);
		txtLocation.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				strSelProjects = new String[] { ResourceUtils
						.extractDotProjectPath(txtLocation.getText()) };
				dialogChanged();
				updateStates();
			}
		});

		shell = this.getShell();
		filterExt = getFileExtensions();
		class Open implements SelectionListener {
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
				fileDialog
						.setText(WizardMessages.Open_Project_fileDialog_Title);
				fileDialog.setFilterPath(ResourcesPlugin.getWorkspace()
						.getRoot().getLocation().toOSString());
				fileDialog.setFilterExtensions(filterExt);
				if (txtLocation.getText() != "")
					fileDialog.setFileName(txtLocation.getText());
				if (fileDialog.open() != null) {
					String separator = "";
					int length = fileDialog.getFilterPath().trim().length();
					if (length > 0
							&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
						separator = File.separator;
					strSelProjects = new String[] { new Path(fileDialog
							.getFilterPath()
							+ separator + fileDialog.getFileName())
							.toOSString() };
					txtLocation.setText(strSelProjects[0]);
				} else {
					dialogChanged();
					txtLocation.setFocus();
				}
			}

			public void widgetDefaultSelected(SelectionEvent event) {
			}
		}

		btnBrowse = new Button(container, SWT.NONE);
		initializeDialogUnits(btnBrowse);
		setButtonLayoutData(btnBrowse);
		btnBrowse.setText(WizardMessages.Open_Project_btnBrowse_Text);
		btnBrowse.addSelectionListener(new Open());

		lblInfoImage = new Label(container, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblInfoImage.setLayoutData(gd);
		labelImage = ISharedImageDescriptor.ICON16_INFO.createImage();
		lblInfoImage.setImage(labelImage);

		lblInfo = new Label(container, SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 424;
		gd.horizontalSpan = 2;
		lblInfo.setLayoutData(gd);
		lblInfo.setText(WizardMessages.Open_Project_lblInfo_Text);

		Composite container2 = new Composite(area, SWT.NONE);
		container2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		container2.setLayout(layout2);
		layout2.numColumns = 1;
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.verticalSpacing = 0;
		final Label separator = new Label(container2, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);

		listOpenWorkspaceProjects();

		if (lstProjects.getItemCount() == 0) {
			btnRad1.setSelection(false);
			btnRad1.setEnabled(false);
			lstProjects.setEnabled(false);
			btnRad2.setSelection(true);
			lblDir.setEnabled(true);
			txtLocation.setEnabled(true);
			btnBrowse.setEnabled(true);
			filterExt = new String[] { filterExt[0] };
			lblInfo.setVisible(true);
			lblInfoImage.setVisible(true);
			txtLocation.setFocus();
		} else {
			btnRad1.setSelection(true);
			btnRad1.setEnabled(true);
			lstProjects.setEnabled(true);
			btnRad2.setSelection(false);
			lblDir.setEnabled(false);
			txtLocation.setEnabled(false);
			btnBrowse.setEnabled(false);
			lblInfo.setVisible(false);
			lblInfoImage.setVisible(false);
		}

		return area;
	}

	/**
	 * This method lists all non opened projects in the workspace
	 */
	private void listOpenWorkspaceProjects() {
		lstProjects.removeAll();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		Arrays.sort(projects, new Comparator<IProject>() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(T, T)
			 */
			public int compare(IProject o1, IProject o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		});
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IEditorReference[] references = page.getEditorReferences();
		java.util.List<IEditorReference> editors = new ArrayList<IEditorReference>(
				Arrays.asList(references));
		java.util.List<IPath> editorContentMappings = getCurrentlyOpenedThemeFiles(editors);
		for (IProject project : projects) {
			try {
				if (project.isOpen()) {
					IFile file = getProjectFileName(project,
							editorContentMappings);
					boolean isOpen = false;

					if (file != null && editorContentMappings != null
							&& !editorContentMappings.isEmpty()) {
						// checks if the editor is already open for the project
						// don't use page.findEditorPart(input) because it will
						// restore the editors by default !
						FileEditorInput input = new FileEditorInput(file);
						for (Iterator<IEditorReference> i = editors.iterator(); i
								.hasNext();) {
							IEditorReference reference = i.next();
							IEditorDescriptor desc = ((EditorReference) reference)
									.getDescriptor();
							if (desc != null) {
								IEditorMatchingStrategy matchingStrategy = desc
										.getEditorMatchingStrategy();
								// first uses matching strategy if there is one,
								// otherwise just compares the inputs
								if (matchingStrategy != null) {
									if (matchingStrategy.matches(reference,
											input)) {
										isOpen = true;
										i.remove();
										break;
									}
								} else {
									IEditorPart editor = (IEditorPart) reference
											.getPart(false);
									if (null != editor && editor.getEditorInput() != null
											&& editor.getEditorInput().equals(
													input)) {
										isOpen = true;
										i.remove();
										break;
									}
								}
							}
						}
					}
					if (!isOpen) {
						lstProjects.add(project.getName());
					}
				}
			} catch (Exception e) {
				UiPlugin.error(e);
			}
		}
	}

	protected void createButtonsForButtonBar(Composite parent) {
		btnOpen = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OPEN_LABEL, true);
		btnOpen.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			setMessage(null);
		}

		if (buttonId == IDialogConstants.CANCEL_ID) {
			setMessage(null);
		}

		super.buttonPressed(buttonId);
	}

	/**
	 * This method handles enabled/disabled state logic of components
	 */
	private void updateStates() {
		if (btnRad1.getSelection() == true) {
			lblDir.setEnabled(false);
			btnBrowse.setEnabled(false);
			txtLocation.setEnabled(false);
			lblInfo.setVisible(false);
			lblInfoImage.setVisible(false);
			if (lstProjects.getItemCount() > 0) {
				lstProjects.setEnabled(true);
				if (lstProjects.getSelection().length > 0) {
					btnOpen.setEnabled(true);
				} else
					btnOpen.setEnabled(false);
			} else {
				lstProjects.setEnabled(false);
				btnOpen.setEnabled(false);
			}
		} else {
			lstProjects.setEnabled(false);
			lblDir.setEnabled(true);
			btnBrowse.setEnabled(true);
			txtLocation.setEnabled(true);
			lstProjects.deselectAll();
			if (btnRad2.getSelection() == true) {
				filterExt = new String[] { filterExt[0] };
				lblInfo.setVisible(true);
				lblInfoImage.setVisible(true);
			} else {
				lblInfo.setVisible(false);
				lblInfoImage.setVisible(false);
			}
			if (dialogComplete)
				btnOpen.setEnabled(true);
			else
				btnOpen.setEnabled(false);
		}
	}

	/**
	 * This method verifies the project
	 */
	private void dialogChanged() {
		String selected = txtLocation.getText();
		if (selected.trim().length() == 0) {
			updateStatus(WizardMessages.Open_Project_No_File_Error);
			return;
		}

		selected = ResourceUtils.extractDotProjectPath(selected);
		Path path = new Path(selected);
		String ext = path.getFileExtension();

		if (ext == null
				|| (!getFileExtensionsAsList().contains(ext) && !ext
						.equalsIgnoreCase(IProjectDescription.DESCRIPTION_FILE_NAME
								.substring(1)))) {
			updateStatus(generateExtnsErrorMessage(getFileExtensionsAsList()));
			dialogComplete = false;
			return;
		}

		if (!FileUtils.isFileValidAndAccessible(selected)) {
			updateStatus(WizardMessages.Open_Project_Not_Exist_Error);
			dialogComplete = false;
			return;
		}

		if (btnRad2.getSelection() == true) {
			IProject project = ResourceUtils.findImportedProject(path);
			if (project == null) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(
						path.removeLastSegments(1).lastSegment());

			}
			String[] closedProjects = lstProjects.getItems();
			boolean isOpen = project.exists() && project.isOpen();
			boolean isCaseInsensitive = Platform.OS_WIN32.equals(Platform
					.getOS());
			String projectName = project.getName();
			if (isCaseInsensitive) {
				projectName = projectName.toLowerCase();
			}
			for (String prj : closedProjects) {
				if (isCaseInsensitive) {
					prj = prj.toLowerCase();
				}
				if (prj.equals(projectName)) {
					isOpen = false;
					break;
				}
			}

			if (isOpen) {
				updateStatus(WizardMessages.Open_Project_Project_Already_Open);
				dialogComplete = false;
				return;
			}
		}
		dialogComplete = true;
		updateStatus(null);
	}

	/**
	 * This method updates error messages in the banner area
	 */
	private void updateStatus(String message) {
		setErrorMessage(message);
	}

	/**
	 * This method returns name of the selected project in the workspace or
	 * selected project with full path if the selection is made outside current
	 * workspace
	 * 
	 * @return project with full path
	 */
	public String[] getSelectedProjects() {
		return strSelProjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	@Override
	public boolean close() {
		if (labelImage != null) {
			labelImage.dispose();
		}
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return ISharedImageDescriptor.WIZBAN_OPEN_PROJECT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.utils.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return WizardMessages.Open_Project_Title;
	}

	private String[] getFileExtensions() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(IProjectDescription.DESCRIPTION_FILE_NAME);
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractOpenProjectAction operation = (AbstractOpenProjectAction) desc
					.createAction(IContributorDescriptor.OPERATION_OPEN_PROJECT);
			if (operation != null) {
				String[] temp = operation.getFileExtensions();
				for (String string : temp) {
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

	private ArrayList<String> getFileExtensionsAsList() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(IProjectDescription.DESCRIPTION_FILE_NAME);
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractOpenProjectAction operation = (AbstractOpenProjectAction) desc
					.createAction(IContributorDescriptor.OPERATION_OPEN_PROJECT);
			if (operation != null) {
				String[] temp = operation.getFileExtensions();
				for (String string : temp) {
					list.add(string);
				}
			}
		}
		return list;
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

		return "Theme has to be " + buffer.toString();
	}

	/**
	 * Getting the theme file from project
	 * 
	 * @param project
	 * @param editorContentMappings
	 *            map containing details of currently opened theme files
	 * @return
	 */
	private IFile getProjectFileName(IProject project,
			java.util.List<IPath> editorContentMappings) {
		IFile temp = null;
		for (IPath path : editorContentMappings) {
			temp = ResourceUtils
					.getProjectResourceByAbsolutePath(project, path);
			if (temp != null)
				return temp;
		}
		return temp;
	}

	/**
	 * Getting the currently opened theme details as map
	 * 
	 * @param editors
	 * @return
	 */
	private java.util.List<IPath> getCurrentlyOpenedThemeFiles(
			java.util.List<IEditorReference> editors) {
		java.util.List<IPath> fileMappings = new ArrayList<IPath>();
		IPath path = null;
		for (IEditorReference reference : editors) {
			if (reference.getEditor(false) == null)
				continue;
			IContent content = (IContent) reference.getEditor(false)
					.getAdapter(IContent.class);
			if (content == null) {
				content = (IContent) reference.getEditor(false).getAdapter(
						IContentData.class);
				if (content == null)
					continue;
			}
			path = (IPath) content.getAdapter(IPath.class);
			if (path == null)
				continue;
			fileMappings.add(path);
		}
		return fileMappings;
	}
}
