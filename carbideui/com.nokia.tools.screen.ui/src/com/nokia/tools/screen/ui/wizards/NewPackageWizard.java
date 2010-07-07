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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IPackagingDescriptor;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * This class offers all pages for the deployment package creation wizard. Also
 * package building, trickered from the last page of the wizard, is started from
 * here.
 * 
 */
public class NewPackageWizard
    extends Wizard
    implements INewWizard {

	private PackagingContext context;

	private IProject project;

	private Image windowImage;

	private ImageDescriptor bannerIcon;

	private AbstractNewPackagePage[] pages;

	private AbstractPackagingOperation operation;

	/**
	 * Constructor for NewProjectWizard.
	 */
	public NewPackageWizard() {
		setNeedsProgressMonitor(true);

		IBrandingManager manager = BrandingExtensionManager
		    .getBrandingManager();
		if (manager != null) {
			bannerIcon = UiPlugin
			    .getImageDescriptor("icons/wizban/create_package.png");
			setDefaultPageImageDescriptor(manager
			    .getBannerImageDescriptor(bannerIcon));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPageControls(Composite pageContainer) {

		// "project" class variable was NULL when packaging dialog was invoked.
		// This happens very rarely, may be because of some workspace related
		// issues.
		// when the tester changed to a new workspace or restarted the product,
		// there were no issues
		// i couldn't reproduce it, so just adding a null check.
		String projName = null;
		Object content = null;
		if (null != project)
			projName = project.getName();
		if (null != context)
			content = context.getInput();
		if (null == projName && null != content
		    && content instanceof IContent[]) {
			IContent projectContent = ((IContent[]) content)[0];
			// fix for Null Pointer Exception occuring 
			if (null != projectContent) {
				project = ((IProject) projectContent.getAttribute(ContentAttribute.PROJECT.name()));
				if(project != null){
					projName = project.getName();
				}
				else{
					projName = "";
				}
			}
		}
		setWindowTitle(WizardMessages.New_Package_Title + " - " + projName);
		// end of modification
		super.createPageControls(pageContainer);
	}

	/**
	 * Adding the pages to the wizard.
	 */
	public void addPages() {
		IEditorPart editor = EclipseUtils.getActiveSafeEditor();
		IContentAdapter adapter = (IContentAdapter) editor
		    .getAdapter(IContentAdapter.class);
		IContent[] contents = adapter.getContents();
		// filters out all contents that are not packageable
		List<IContent> list = new ArrayList<IContent>(contents.length);
		for (IContent content : contents) {
			if (content.getAdapter(IPackager.class) != null) {
				list.add(content);
				if (project == null) {
					project = (IProject) content
					    .getAttribute(ContentAttribute.PROJECT.name());
				}
			}
		}
		contents = list.toArray(new IContent[list.size()]);

		context = new PackagingContext();
		context.setInput(contents);

		String helpContextId = null;
		for (IContent content : contents) {
			String type = content.getType();
			IPackagingDescriptor descriptor = ExtensionManager
			    .getPackagingDescriptor(type);
			if (descriptor != null) {
				pages = descriptor.getWizardPages();
				if (pages.length > 0) {
					helpContextId = descriptor.getHelpContextId();
					operation = descriptor.getPackagingOperation();
					operation.setContext(context);
					operation.setProject(project);
					break;
				}
			}
		}
		for (AbstractNewPackagePage page : pages) {
			page.setContext(context);
			page.setHelpContextId(helpContextId);
		}

		for (AbstractNewPackagePage page : pages) {
			addPage(page);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		for (AbstractNewPackagePage page : pages) {
			if (!page.performFinish()) {
				return false;
			}
		}

		IContent[] contents = null;
		if (context.getInput() instanceof IContent) {
			contents = new IContent[] { (IContent) context.getInput() };
		} else {
			contents = (IContent[]) context.getInput();
		}

		List<IContent> selected = new ArrayList<IContent>(contents.length);
		for (IContent content : contents) {
			IPackager packager = (IPackager) content
			    .getAdapter(IPackager.class);
			if (packager != null
			    && new Boolean((String) content
			        .getAttribute(ContentAttribute.PACKAGING.name()))
			    && new Boolean((String) content
			        .getAttribute(ContentAttribute.MODIFIED.name()))) {
				selected.add(content);
			}
		}
		context.setInput(selected.toArray(new IContent[selected.size()]));

		// theme has to be saved because the release information
		// might have changed
		IEditorPart currentEditor = EclipseUtils.getActiveSafeEditor();
		// saves all settings
		currentEditor.doSave(null);
		
		final String jobName = MessageFormat.format(
		    WizardMessages.New_Package_Generating_Package,
		    new Object[] { context.getAttribute(PackagingAttribute.sisFile
		        .name()) });
		/**
		 * User should not be allowed to switch themes while packaging is on.
		 */
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
			      public void run(IProgressMonitor monitor) {			    	  
			    	  monitor.beginTask(jobName, 100);
			    	  monitor.worked(5);
			    	  try {
		  					
		  					operation.run(monitor);
		  					
		  					for (AbstractNewPackagePage page : pages) {
		  						page.handlePackagingFinish();
		  					}
		  				} catch (Exception e) {
		  					UiPlugin.error(e);
		  				}
		  			   monitor.done();
			      }
			});
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		return true;
	}
	
	

	@Override
	public boolean needsProgressMonitor() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/**
	 * checks weather can finish the wizard
	 */
	public boolean canFinish() {
		if (this.getContainer().getCurrentPage() == pages[pages.length - 1]
		    && pages[pages.length - 1].isPageComplete() == true)
			return true;
		else
			return false;
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

	public boolean performCancel() {
		for (AbstractNewPackagePage page : pages) {
			page.handlePackagingCancel();
			
		}
        return super.performCancel();
    }
}
