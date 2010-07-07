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
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;
import com.nokia.tools.ui.dialog.MessageDialogWithTextContent;

public abstract class AbstractPackagingOperation implements
		IRunnableWithProgress {
	protected PackagingContext context;

	protected IProject project;

	/**
	 * @param context
	 */
	public void setContext(PackagingContext context) {
		this.context = context;
	}

	/**
	 * @param project
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor parentMonitor) throws InvocationTargetException,
			InterruptedException {
		
		SubProgressMonitor childMonitor;
		String progressString = "Packaging theme...";
		String subProgressString = "";
		File workingDir;
		IFolder folder = null, folderTemp = null;
		String dir = UiPlugin.getDefault().getPreferenceStore().getString(
				IScreenConstants.PREF_PACKAGING_DIR);
		
		parentMonitor.beginTask(progressString,IProgressMonitor.UNKNOWN);
		
		//parentMonitor.worked(INCREMENT);
		
		parentMonitor.subTask(subProgressString);
		childMonitor = new SubProgressMonitor(parentMonitor,IProgressMonitor.UNKNOWN);
		if (dir == null
				|| !IScreenConstants.PACKAGING_DIR_PROJECT
						.equalsIgnoreCase(dir)) {
			workingDir = new File(System.getProperty("java.io.tmpdir")
					+ File.separator + project.getName() + "_"
					+ IScreenConstants.PACKAGING_DIR_NAME);
			if (workingDir.isFile()) {
				workingDir.delete();
			}
			workingDir.mkdirs();
			File[] files = workingDir.listFiles();
			if (files != null) {
				for (File file : files) {
					file.delete();
				}
			}
		} else {
			folder = project.getFolder(IScreenConstants.PACKAGING_DIR_NAME);
			folderTemp = project.getFolder(IScreenConstants.PACKAGING_DIR_NAME
					+ "_temp");
			if (!folder.exists()) {
				try {
					folder.create(true, true, childMonitor);
				} catch (Exception e) {
					UiPlugin.error(e);
				}
			}

			try {
				folderTemp.delete(true, false, childMonitor);
			} catch (CoreException e1) {
				UiPlugin.error(e1);
			}
			if (!folderTemp.exists()) {
				try {
					folderTemp.create(true, true, childMonitor);
				} catch (Exception e) {
					UiPlugin.error(e);
				}
			}
			workingDir = folderTemp.getLocation().toFile();
		}
		
		
		parentMonitor.subTask(subProgressString);
		childMonitor = new SubProgressMonitor(parentMonitor,IProgressMonitor.UNKNOWN);
		
		context.setAttribute(PackagingAttribute.workingDir.name(), workingDir
				.getAbsolutePath());
		try {
			doPackaging(childMonitor);

			
			if (folder != null && folderTemp != null) {
				try {
					folder.delete(true, false, null);
				} catch (Exception e) {
					showErrorMessage();
					UiPlugin.error(e);
					return;
				}
				try {
					folderTemp.move(folder.getProjectRelativePath(), true,
							false, null);
				} catch (Exception e) {
					UiPlugin.error(e);
				}
			}

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */

				public void run() {
					try {
						IPreferenceStore store = UiPlugin.getDefault()
								.getPreferenceStore();
						String file = (String) context
								.getAttribute(PackagingAttribute.sisFile.name());
						if (file == null) {
							file = (String) context
									.getAttribute(PackagingAttribute.output
											.name());
						}
						IBrandingManager branding = BrandingExtensionManager
								.getBrandingManager();
						Image image = null;
						if (branding != null) {
							image = branding.getIconImageDescriptor()
									.createImage();
						}
						String message = MessageFormat
								.format(
										WizardMessages.New_Package_Package_Created_Description,
										new Object[] { file });
						String[] dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL };

						MessageDialog dialog = new MessageDialog(
								PlatformUI.getWorkbench().getDisplay()
										.getActiveShell(),
								WizardMessages.New_Package_Package_Created_Title,
								image, message, 2, dialogButtonLabels, 0);

						dialog.open();
						if (image != null) {
							image.dispose();
						}
					} catch (Exception e) {

						UiPlugin.error(e);
					}
				}
			});
			
			
			parentMonitor.subTask(subProgressString); 
			childMonitor = new SubProgressMonitor(parentMonitor,IProgressMonitor.UNKNOWN);
			
			// cleans up the temporary folder only when the packaging is
			// successful, so we could examine more easily what is wrong.
			if (folderTemp != null) {
				try {
					folderTemp.delete(true, false, childMonitor);
				} catch (final CoreException e) {
					PlatformUI.getWorkbench().getDisplay().asyncExec(
					    new Runnable() {
						    public void run() {
							    MessageDialog.openInformation(
							    	PlatformUI.getWorkbench().getDisplay().getActiveShell(),
							    	WizardMessages.New_Package_Package_Creation_Error_Title,
							    	WizardMessages.New_Package_Package_Deleting_Temp_Folder_Problem);
						    }
					    });
				}
			} else {
				FileUtils.deleteDirectory(workingDir);
			}
		} catch (final Exception e) {
			UiPlugin.error(e);
			String sisTempFile = (String) context
					.getAttribute(PackagingAttribute.sisTempFile.name());
			if (sisTempFile != null) {
				// removes the sis temporary file in case signing failed
				new File(sisTempFile).delete();
			}
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialogWithTextContent
							.openError(
									PlatformUI.getWorkbench().getDisplay()
											.getActiveShell(),
									WizardMessages.New_Package_Package_Creation_Error_Title,
									MessageFormat
											.format(
													WizardMessages.New_Package_Package_Creation_Error_Description,
													new Object[] { e
															.getMessage() }),
									e instanceof PackagingException ? ((PackagingException) e)
											.getDetails()
											: StringUtils.dumpThrowable(e));
				}
			});
		} finally {			
			parentMonitor.setTaskName("");			
			parentMonitor.subTask(subProgressString); 
			childMonitor = new SubProgressMonitor(parentMonitor,IProgressMonitor.UNKNOWN);
			childMonitor.done();
			parentMonitor.done();
			try {
				project.refreshLocal(IProject.DEPTH_INFINITE, childMonitor);
			} catch (Exception e) {
				UiPlugin.error(e);
			}
			 
		}
	}

	private void showErrorMessage() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
			    MessageDialog.openInformation(
			    	PlatformUI.getWorkbench().getDisplay().getActiveShell(),
			    	WizardMessages.New_Package_Package_Creation_Error_Title,
			    	WizardMessages.New_Package_Package_Deleting_Working_Folder_Problem);
		    }
		});
	}
		
	

	protected abstract void doPackaging(IProgressMonitor monitor)
			throws Exception;

	protected abstract void doSilentModeDeployment() throws Exception;

	protected abstract boolean isSilentModeDeploymentAvailable()
			throws Exception;

}
