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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.actions.AbstractCreateProjectAction;
import com.nokia.tools.screen.ui.actions.OpenGUIForProjectOperation;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IContributorDescriptor;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

/**
 * The wizard for creating a new Project
 */
public class NewProjectWizard extends Wizard implements INewWizard {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page1.getThemeCreationMode() != NewProjectPage1.CREATEDEFAULT) {
			return null;
		}
		IWizardPage next = super.getNextPage(page);
		if (next instanceof OptionalPage) {
			OptionalPage o = (OptionalPage) next;
			while (!o.isEnabled(page1.getRelease())) {
				next = next.getNextPage();
				if (next instanceof OptionalPage) {
					o = (OptionalPage) next;
				} else {
					return next;
				}
			}
			return next;
		}
		return next;
	}

	public interface SubTaskEnabledPage {
		void performFinish(IThemeDescriptor descriptor, IProject proj);
	}

	public interface OptionalPage {
		boolean isEnabled(String release);
	}

	public static final String NEW_PROJECT_CONTEXT = "com.nokia.tools.s60.ide.createNewProjectWizard_context"; //$NON-NLS-1$

	private static final String EXTPOINT_CUSTOM_WIZPAGE = UiPlugin.getDefault()
			.getBundle().getSymbolicName()
			+ ".newThemeAdditionalWizardPage"; //$NON-NLS-1$

	private NewProjectPage1 page1;

	IWorkbenchPage workbenchPage;

	private ISelection selection;

	protected IProject createdProject;

	private Image windowImage;

	private ImageDescriptor bannerIcon;

	private String fileName;

	private String projectName;

	private Integer creationMode;

	private boolean isOpenInDefaultEditor;

	private boolean shouldSubTaskFinishExecute;

	/**
	 * Constructor for NewProjectWizard.
	 */
	public NewProjectWizard() {
		super();
		workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();

		setWindowTitle(WizardMessages.New_Project_Title);
		setNeedsProgressMonitor(true);
		IBrandingManager manager = BrandingExtensionManager
				.getBrandingManager();
		if (manager != null) {
			bannerIcon = ISharedImageDescriptor.WIZBAN_CREATE_PROJECT;
			setDefaultPageImageDescriptor(manager
					.getBannerImageDescriptor(bannerIcon));
		}
	}

	/**
	 * Adding the pages to the wizard.
	 */
	public void addPages() {
		
		page1 = new NewProjectPage1(selection);
		if (fileName != null) {
			page1.setFileName(fileName);
		}
		if (creationMode != null) {
			page1.setThemeCreationMode(creationMode);
		}
		if (projectName != null) {
			page1.setProjectName(projectName);
		}
		addPage(page1);

		// add
		try {
			addContributedPages();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Wizard image have to set here.
		if (getShell() != null) {
			IBrandingManager branding = BrandingExtensionManager
					.getBrandingManager();
			if (branding != null) {
				windowImage = branding.getIconImageDescriptor().createImage();
				getShell().setImage(windowImage);
			}
		}
	}

	private void addContributedPages() throws Exception {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getExtensionPoint(EXTPOINT_CUSTOM_WIZPAGE)
				.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			if ("wizpage".equalsIgnoreCase(element.getName())) {
				String _clazz = element.getAttribute("classname");
				String importance = element.getAttribute("importance");
				Bundle declaringPlugin = Platform.getBundle(element
						.getNamespaceIdentifier());
				if (declaringPlugin == null) {
					declaringPlugin = Platform.getBundle(element
							.getContributor().getName());
				}
				try {
					Class clazz = declaringPlugin.loadClass(_clazz);
					if (WizardPage.class.isAssignableFrom(clazz)) {
						WizardPage custom = (WizardPage) clazz.newInstance();
						addPage(custom);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		AbstractContentSourceManager.initContent();
		// obtain absolute path to the folder where contents will be created
		String projectFolder = new Path(ResourcesPlugin.getWorkspace()
				.getRoot().getLocation().toFile().getAbsolutePath()).append(
				page1.getProjectName()).toString();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				page1.getProjectName());

		shouldSubTaskFinishExecute(false);
		isOpenInDefaultEditor(false);
		
		AbstractCreateProjectAction operation = null;
		
		switch (page1.getThemeCreationMode()) {
		case NewProjectPage1.CREATEDEFAULT:
			// try contributors
			operation = createNewThemeProject(projectFolder, project);
			break;
		case NewProjectPage1.COPYEXISTING:
		case NewProjectPage1.COPYEXAMPLETHEMES:
			// try contributors
			operation = copyExistingProject(projectFolder, project);
			break;
		}
		
		if(operation != null){
			try {
				isOpenInDefaultEditor(operation.isOpenInDefaultEditor());
				getContainer().run(true, false, operation);
				
				try {
					if (shouldSubTaskFinishExecute) {
						evaluateOptionsOnOtherPages(project);
					}
					if (isOpenInDefaultEditor) {
						// refresh created project and open editor
						refreshProjectInEditor(project);
					}
				} catch (Throwable e) {
					handleExcpetions(e);
					return false;
				}
				
				return true;
				
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				Throwable t = e;
				while (t instanceof InvocationTargetException) {
					t = ((InvocationTargetException) t)
							.getTargetException();
				}
				if (t instanceof CoreException) {
					ErrorDialog.openError(getShell(),
							"Problem in creating project", t.getLocalizedMessage(),
							((CoreException) t).getStatus());
				} else {
					MessageDialog.openError(getShell(),
							"Problem in creating project", t.getLocalizedMessage());
				}
			} catch (InterruptedException e) {				
				e.printStackTrace();
			}
		}
		
		return false;
	}

	private void handleExcpetions(Throwable e) {
		UiPlugin.error(e);
		if (e instanceof InvocationTargetException) {
			// ie.- one aof the steps resulted in a core exception
			e = ((InvocationTargetException) e).getTargetException();
		}
		if (e instanceof CoreException) {
			ErrorDialog.openError(getShell(),
					WizardMessages.New_Project_Error_Create_Project, e
							.getMessage(), ((CoreException) e).getStatus());

		} else {
			MessageDialogWithTextContent.openError(getShell(),
					WizardMessages.New_Project_Error_Create_Project,
					WizardMessages.New_Project_Error_Create_Project,
					StringUtils.dumpThrowable(e));
		}
	}

	private void refreshProjectInEditor(IProject project)
			throws InvocationTargetException {
		try {
			project.refreshLocal(IProject.DEPTH_INFINITE, null);
			createdProject = project;

			final OpenGUIForProjectOperation openGuiOp = new OpenGUIForProjectOperation(
					project, (String) null, PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage());

			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					try {
						openGuiOp.run(new NullProgressMonitor());
					} catch (InvocationTargetException e) {
						
						e.printStackTrace();
					} catch (InterruptedException e) {
					
						e.printStackTrace();
					}

				}

			});

		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
	}

	private void evaluateOptionsOnOtherPages(IProject project) {
		for (Object page : getPages()) {
			if (page instanceof SubTaskEnabledPage) {
				if (page instanceof OptionalPage) {
					if (!((OptionalPage) page).isEnabled(page1.getRelease())) {
						continue;
					}
				}
				try {
					IThemeModelDescriptor descriptor = ThemePlatform
							.getThemeModelDescriptorById(page1.getRelease());

					IThemeDescriptor themeDescriptor = null;
					if (descriptor != null) {
						themeDescriptor = descriptor.getThemeDescriptor();
					}

					((SubTaskEnabledPage) page).performFinish(themeDescriptor,
							project);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private AbstractCreateProjectAction createNewThemeProject(String projectFolder, IProject project) {
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			AbstractCreateProjectAction operation = (AbstractCreateProjectAction) desc
					.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT);
			
			if (operation != null){
				if(operation.setCreateDefaultThemeParameters(project, page1.getProjectName(),
						projectFolder, page1.getRelease(), page1
						.getResolution())){
					if(operation.isSupportsRelease(page1.getRelease())){
						shouldSubTaskFinishExecute(true);
						return operation;
					}
				}				
			}
		}
		return null;
	}
	
	private AbstractCreateProjectAction copyExistingProject(String projectFolder, IProject project) {
		for (IContributorDescriptor desc : ExtensionManager
				.getContributorDescriptors()) {
			if (null != desc
					.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT)
					& desc
							.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT) instanceof AbstractCreateProjectAction) {
				
				AbstractCreateProjectAction operation = (AbstractCreateProjectAction) desc
						.createOperation(IContributorDescriptor.OPERATION_CREATE_PROJECT);
				
				if(operation != null){
					if(operation.setCopyExistingThemeParameters(project, page1.getProjectName(),
							projectFolder, page1.getRelease(), page1
							.getResolution(), page1.getFileName())){
						if(operation.supportsPath(page1.getFileName())){
							return operation;
						}
					}
				}				
			}
		}
		return null;
	}

	private void shouldSubTaskFinishExecute(boolean value) {
		shouldSubTaskFinishExecute = value;
	}

	private void isOpenInDefaultEditor(boolean openInDefaultEditor) {
		isOpenInDefaultEditor = openInDefaultEditor;
	}

	public boolean canFinish() {
		if (!page1.isPageComplete()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

	/**
	 * @return project That was created in wizard
	 */
	public IProject getProject() {
		return createdProject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	@Override
	public void dispose() {
		if (windowImage != null) {
			windowImage.dispose();
		}
		super.dispose();
	}

	public void setFileName(String fileName) {
		this.fileName = fileName != null ? fileName.replace("%20", " ") : null;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;

	}

	public void setCreationMode(int creationMode) {
		this.creationMode = creationMode;
	}
}