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
package com.nokia.tools.screen.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.dialogs.MessageDialogWithCheckBox;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IPackagingPreprocessingAction;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.wizards.NewPackageWizard;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

public class NewPackageAction extends Action implements
		IWorkbenchWindowActionDelegate, IPartListener {
	public static final String ID = "com.nokia.tools.s60.ide.actions.NewPackageAction";

	/**
	 * The action used to render this delegate.
	 */
	private IAction fAction = null;

	public void run() {
		final IEditorPart currentEditor = EclipseUtils.getActiveSafeEditor();

		if(currentEditor == null) 
			return;

		if (currentEditor.isDirty()) {
			boolean saveWithoutConfirm = false;
			IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
			saveWithoutConfirm = store
					.getBoolean(IScreenConstants.PREF_SAVE_BEFORE_PACKAGING_ASK);
			if (!saveWithoutConfirm) {
				IBrandingManager branding = BrandingExtensionManager
						.getBrandingManager();
				Image image = null;
				if (branding != null) {
					image = branding.getIconImageDescriptor().createImage();
				}
				MessageDialog messageDialog = new MessageDialogWithCheckBox(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(),
						Messages.NewPackageAction_Save_MsgBox_Title,
						image,
						Messages.NewPackageAction_Save_MsgBox_Message,
						Messages.NewPackageAction_Save_MsgBox_Confirmation_text,
						saveWithoutConfirm, null, null, null, 3, new String[] {
								IDialogConstants.YES_LABEL,
								IDialogConstants.NO_LABEL }, 0);
				messageDialog.open();
				if (image != null) {
					image.dispose();
				}
				store.setValue(IScreenConstants.PREF_SAVE_BEFORE_PACKAGING_ASK,
						((MessageDialogWithCheckBox) messageDialog)
								.getCheckBoxValue());

				if (messageDialog.getReturnCode() != 0)
					return;
			}
			try {
				new ProgressMonitorDialog(PlatformUI.getWorkbench()
						.getDisplay().getActiveShell()).run(false, false,
						new IRunnableWithProgress() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
							 */
							public void run(IProgressMonitor monitor)
									throws InvocationTargetException,
									InterruptedException {
								currentEditor.doSave(monitor);
							}
						});
			} catch (Exception e) {
				UiPlugin.error(e);
			}
			performPackaging();

		} else
			performPackaging();
	}

	private void performPackaging(){
		final IEditorPart currentEditor = EclipseUtils.getActiveSafeEditor();
		if(currentEditor == null)
			return;
		IContentAdapter adapter = (IContentAdapter) currentEditor.getAdapter(IContentAdapter.class);
		
		if(adapter != null){
			IContent[] contents = adapter.getContents();
			if(contents != null){
				for(IContent content: contents){
					IPackagingPreprocessingAction[] packagingPreProcessingActions = 
						 ExtensionManager.getPackagingPreProcessingActions(content.getType());
					if(packagingPreProcessingActions != null){
						for(IPackagingPreprocessingAction packagingPreProcessingAction: packagingPreProcessingActions){
							if(!packagingPreProcessingAction.performPackagingPreProcessing(content)){
								return;
							}
						}
					}
				}
			}
		}
		
		NewPackageWizard newPackageWizard = new NewPackageWizard();
		WizardDialog dialog = new WizardDialog(Display.getCurrent()
				.getActiveShell(), newPackageWizard);
		dialog.open();
	}
	
	public NewPackageAction() {
	}

	public void dispose() {

		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow != null) {
			activeWorkbenchWindow.getPartService().removePartListener(this);
		}

	}

	public void init(IWorkbenchWindow window) {
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
				.addPartListener(this);
	}

	public void run(IAction action) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (fAction != action) {
			fAction = action;
		}
		recalculateAndEnable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(IWorkbenchPart part) {
		recalculateAndEnable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		recalculateAndEnable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
	}

	/**
	 * Takes the active editor and calculate whether to enable packaging action
	 * and then do that enablin/disabling
	 */
	private void recalculateAndEnable() {
		if (null == fAction )
			return;
		IEditorPart part = EclipseUtils.getActiveSafeEditor();
		if (null != part
				&& part.getAdapter(IPackager.class) != null
				&& part.getAdapter(IContentAdapter.class) != null
				&& !part
						.getClass()
						.getName()
						.equals(
								"com.nokia.tools.variant.plugin.ui.editor.PluginEditorPart")) {
			fAction.setEnabled(true);
		} else
			fAction.setEnabled(false);
	}
}
