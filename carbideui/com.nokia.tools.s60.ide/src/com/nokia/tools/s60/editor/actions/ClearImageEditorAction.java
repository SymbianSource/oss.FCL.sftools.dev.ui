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
package com.nokia.tools.s60.editor.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.media.utils.svg.ColorGroupsStore;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.resource.util.EclipseUtils;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.commands.ClearImageCommand;
import com.nokia.tools.s60.editor.commands.RemoveFromGroupCommand;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.s60.internal.utils.CommandInspector;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableContentDataAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;

public class ClearImageEditorAction extends AbstractAction {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "clear_context"; 

	public static final String ID = "ClearImage"; 

	protected void init() {
		setId(ID);
		setText(Messages.ClearImageAction_name); 
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/clear_co.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/clear_co.gif"));
		setToolTipText(Messages.ClearImageAction_tooltip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				ClearImageEditorAction.HLP_CTX);
	}

	public ClearImageEditorAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
		multipleSelection = true;
		multipleSelectionEnablement = MultipleSelectionEnablementEnum.ONE;
	}

	public ClearImageEditorAction(ISelectionProvider provider,
			CommandStack _stack) {
		super(null);
		setSelectionProvider(provider);
		setLazyEnablementCalculation(true);
		stack = _stack;
		multipleSelection = true;
		multipleSelectionEnablement = MultipleSelectionEnablementEnum.ONE;
	}

	public void selectionChanged(ISelection selection) {
		if (selection == null) {
			this.setEnabled(false);
		}
		this.setEnabled(calculateEnabled());
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		setWorkbenchPart(part);
		selectionChanged(selection);
	}

	public void run() {

		IStructuredSelection selection = this.selection;

		boolean multi = false;
		List<Command> multiCmdList = null;
		if (selection.size() > 1) {
			multi = true;
			multiCmdList = new ArrayList<Command>();
		}

		IContentData data = null;
		EditPart editPart = null;
		// partEntity - for 9-piece elements
		IImage partEntity = null;

		Iterator els = selection.iterator();
		while (els.hasNext()) {

			Object obj = els.next();

			if (obj instanceof IContentData) {
				data = (IContentData) obj;
			} else if (obj instanceof EditPart) {
				IScreenElement screenElement = getScreenElement((EditPart) obj);
				if (screenElement == null) {
					continue;
				}
				data = screenElement.getData();
			} else {
				boolean isPartElement = false;

				
				if (obj instanceof Object[]) {
					IImage selectedImg = null;
					if (((Object[]) obj)[1] instanceof IImage)
						selectedImg = (IImage) ((Object[]) obj)[1];
					else if (((Object[]) obj)[1] instanceof ILayer) {
						selectedImg = ((ILayer) ((Object[]) obj)[1])
								.getParent();
					}
					if (selectedImg != null) {
						// EditPart or IContentData is in selection

						if (selectedImg.isPart()) {
							isPartElement = true;
							partEntity = selectedImg;
						}

						if (((Object[]) obj)[2] instanceof EditPart) {
							editPart = (EditPart) ((Object[]) obj)[2];
							IScreenElement screenElement = getScreenElement(editPart);
							data = screenElement.getData();
						} else if (((Object[]) obj)[2] instanceof IContentData) {
							editPart = null;
							data = (IContentData) ((Object[]) obj)[2];
						}
					}
				}
				if (isPartElement && partEntity == null) {
					continue;
				}
			}

			if (data != null) {

				ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				IMediaFileAdapter fileAdapter = (IMediaFileAdapter) data
						.getAdapter(IMediaFileAdapter.class);

				if (skAdapter != null && (skAdapter.isSkinned())) {
					ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
							com.nokia.tools.s60.editor.commands.Messages.Clear_Label);
					command
							.add(skAdapter
									.getApplyBitmapPropertiesCommand(new BitmapProperties()));
					IColorAdapter ca = (IColorAdapter) data
							.getAdapter(IColorAdapter.class);
					if (ca != null) {
						// extract color and set it
						command.add(ca.getApplyColorCommand(ca
								.getColourFromGraphics(null), false));
					}
					if (fileAdapter != null) {
						command.add(fileAdapter
								.getApplyDurationCommand(fileAdapter
										.getDurationFromGraphics()));
						command.add(fileAdapter.getApplyMediaFileCommand(null));
					}

					ClearImageCommand cmd = null;
					//Set fireContentChanged argument to true if it is the last in the selection so that
					//required views gets refresh.
					if (els.hasNext()) {
						cmd = new ClearImageCommand(data, editPart, partEntity, false);
					}
					else {
						//last element in the selection
						cmd = new ClearImageCommand(data, editPart, partEntity, true);
					}
					command.add(cmd);
					
					
					if (multi) {
						multiCmdList.add(command);
					} else {
						execute(command, getEditPart(obj));
					}
					IEditorPart activeEd = EclipseUtils.getActiveSafeEditor();
					if (activeEd instanceof Series60EditorPart) {
						IFile original = ((FileEditorInput) activeEd.getEditorInput())
								.getFile();
						ColorGroups grps = ColorGroupsStore
							.getColorGroupsForProject(original.getProject());
							for (ColorGroup grp : grps.getGroups()) {
								if (grp.containsItemWithIdAndLayerName(data
										.getId(), null)) {
									//grp.removeItemFromGroup(data.getId(), null);
									RemoveFromGroupCommand removeFromGroupCommand = new RemoveFromGroupCommand(data.getId(),
											null, grp, grps);
									execute(removeFromGroupCommand, getEditPart(obj));
									break;
								}
							}
					}
					
					
				}
			}

			// Support for clearing of audio changed/skinned elements
			if (data != null) {
				ISkinnableContentDataAdapter isca = (ISkinnableContentDataAdapter) data
						.getAdapter(ISkinnableContentDataAdapter.class);
				if (isca != null) {
					Command clearSkinnedElementCommand = isca
							.getClearSkinnedElementCommand();
					if (multi) {
						Command subCmdClear = clearSkinnedElementCommand;
						multiCmdList.add(subCmdClear);
					} else {
						execute(clearSkinnedElementCommand, editPart);
					}
				}
			}
		}

		if (multi && multiCmdList.size() > 0) {
			IRunnableWithProgress runnable = createRunnable(multiCmdList, editPart);			
//			ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
//					Display.getCurrent().getActiveShell());
			
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, runnable);
//				progressMonitorDialog.run(true, false, runnable);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private IRunnableWithProgress createRunnable(final List<Command> multiCmdList,
			final EditPart editPart) {
		return new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				
				if(monitor instanceof SubProgressMonitor){
					monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
				}
				if(monitor != null){
					monitor.beginTask("Please wait: Clearing skinned informations...", multiCmdList.size() + 2);
				}
				
				for (final Command cubcmd : multiCmdList){
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {						
						public void run() {
							execute(cubcmd, editPart);							
						}
					});					
					monitor.worked(1);
				}				
				monitor.done();
			}			
		};
	}

	@Override
	protected void doRun(Object element) {
	}

	@Override
	protected boolean doCalculateEnabled(Object element, int pos) {
		element = selection.toArray()[pos];

		IContentData data = null;
		if (element instanceof Object[]) {
			if (((Object[]) element)[1] instanceof IImage) {
				
				return true;
			}
		}

		if (element instanceof IContentData) {
			data = (IContentData) element;
		} else if (element instanceof EditPart) {
			EditPart part = (EditPart) element;
			final IScreenElement screenElement = getScreenElement(part);
			if (screenElement != null) {
				ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) getAdapter(
						part, ISkinnableEntityAdapter.class);
				if (adapter != null) {
					data = screenElement.getData();
				}
			}
		} else if (element instanceof Object[]) {
			Object ref = ((Object[]) element)[2];
			if (ref instanceof IContentData)
				data = (IContentData) ref;
			else if (ref instanceof EditPart) {
				data = JEMUtil.getContentData(ref);
			}
		}

		if (data != null) {
			ISkinnableEntityAdapter helper = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);

			if (helper != null && (helper.isSkinned()))
				return true;

			ISkinnableContentDataAdapter isca = (ISkinnableContentDataAdapter) data
					.getAdapter(ISkinnableContentDataAdapter.class);
			if (isca != null) {
				return isca.isElementSkinned();
			}
		}

		return false;
	}

}
