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

import java.awt.HeadlessException;
import java.awt.datatransfer.Clipboard;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.ClipboardHelper;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.s60.editor.commands.PasteImageCommand;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.ISkinnableContentDataAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;

/**
 * Paste image action for screen elements that cannot have multiple layers Used
 * for paste to layer in animation editor
 */
public class PasteImageAction extends AbstractMultipleSelectionAction {

	public static final String ID = ActionFactory.PASTE.getId();

	private Clipboard clip = null;

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.PasteImageAction_name);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/paste_edit.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/paste_edit.gif"));
		setToolTipText(Messages.PasteImageAction_tooltip);
		setLazyEnablementCalculation(true);
	}

	public PasteImageAction(IWorkbenchPart part, Clipboard clip) {
		super(part);
		this.clip = clip;
	}

	public PasteImageAction(ISelectionProvider provider, CommandStack stack,
			Clipboard clip) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
		this.clip = clip;
	}
	
	@Override
	protected void doRun(List<Object> element) {
		if (multipleSelection && element instanceof List) {
			executions = new ArrayList<Execution>();
			List elements = (List) element;			
			
			if((elements.size() > 1)){
				IRunnableWithProgress runnable = createRunnable(elements);
//				ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
//				Display.getCurrent().getActiveShell());
							
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, runnable);
//					progressMonitorDialog.run(true, false, runnable);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else{
				for (Object object : elements) {
					if (multipleSelectionEnablement == MultipleSelectionEnablementEnum.ALL
							|| doCalculateEnabled(object)) {
						doRun(object);
					}
				}
				List<Execution> _executions = executions;
				executions = null;
				execute(_executions, null);
			}			
			
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	private IRunnableWithProgress createRunnable(final List elements) {
		return new IRunnableWithProgress() {

			public void run(final IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {

				if(monitor != null){
					monitor.beginTask("Please wait: Paste operation in progress...", elements.size() * 2);
				}
				
				for (final Object object : elements) {
					if (multipleSelectionEnablement == MultipleSelectionEnablementEnum.ALL
							|| doCalculateEnabled(object)) {
						
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							public void run() {
								doRun(object);
							}				
						});
						
						monitor.worked(1);
					}
				}
				final List<Execution> _executions = executions;
				executions = null;
				execute(_executions, monitor);
				
				monitor.done();
			}			
		};
	}
	
	protected void execute(List<Execution> executions, IProgressMonitor monitor) {
		if (executions == null || executions.size() == 0) {
			return;
		}
		if (executions.size() == 1) {
			Execution ex = executions.get(0);
			execute(ex.cmd, ex.ep);
		} else {
			for (int i = 0; i < executions.size(); i++) {
				final Execution ex = executions.get(i);
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						execute(ex.cmd, ex.ep);
					}				
				});
				
				if(monitor != null){
					monitor.worked(1);
				}				
			}
		}
		
	}

	@Override
	public void doRun(Object sel) {		
		
		final Clipboard _clip = this.clip == null ? ClipboardHelper.APPLICATION_CLIPBOARD : this.clip;

		final IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(sel);
		final Object contentToPaste = isMaskNode() ? ClipboardHelper
				.getClipboardContent(ClipboardHelper.CONTENT_TYPE_MASK, _clip)
				: ClipboardHelper.getClipboardContent(
						ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE
								| ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK,
						_clip);

		if (sel instanceof ILayer) {
			ILayer layer = (ILayer) sel;
			if (!(layer.getParent() instanceof IAnimatedImage)) {
				try {
					ILayer selectedElement = layer;
					IStructuredSelection userSelection = this.selection;
					pasteAdapter.paste(contentToPaste, layer);
					
					//Paste action is clearing the selection. 
					if (this.selection != userSelection ) {
						this.selection = userSelection;
					}
					
					updateGraphicWithCommand(layer, sel);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
		}

		final IImageHolder holder = getImageHolder(sel);
		if (holder != null) {
			EditPart part = getEditPart(sel);
			execute(new UndoableImageHolderActionCommand(holder,
					new Runnable() {
						public void run() {
							try {
								pasteAdapter.paste(contentToPaste, null);
							} catch (HeadlessException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}), part);
			return;
		}

		IContentData data = getContentData(sel);
		if (data != null) {
			ISkinnableContentDataAdapter skinableAdapter = (ISkinnableContentDataAdapter) data
					.getAdapter(ISkinnableContentDataAdapter.class);
			ISkinnableEntityAdapter entityAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			if (skinableAdapter != null || pasteAdapter != null) {
				EditPart part = getEditPart(sel);
				ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
						com.nokia.tools.s60.editor.commands.Messages.PasteImage_Label);
				PasteImageCommand cmd = new PasteImageCommand(data, part,
						getContentToPaste());
				command.add(cmd);
				Command stretchModeCommand = entityAdapter
						.getApplyStretchModeCommand(entityAdapter
								.getStretchMode());
				if (stretchModeCommand.canExecute()) {
					command.add(stretchModeCommand);
				}
				execute(command, part);
				return;
			}
		}
	}

	protected Object getContentToPaste() {
		Clipboard _clip = this.clip == null ? ClipboardHelper.APPLICATION_CLIPBOARD
				: clip;

		return isMaskNode() ? ClipboardHelper.getClipboardContent(
				ClipboardHelper.CONTENT_TYPE_MASK, _clip) : ClipboardHelper
				.getClipboardContent(ClipboardHelper.CONTENT_TYPE_SINGLE_IMAGE
						| ClipboardHelper.CONTENT_TYPE_IMAGE_WITH_MASK, _clip);

	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		if (clip == null) {
			clip = ClipboardHelper.APPLICATION_CLIPBOARD;
		}

		if (isNodeOfType(TYPE_COLOR) || isNodeOfType((String) TYPE_COLOR_GROUP))
			return false;

		
		ILayer layer = getLayer(false, sel);

		if (layer != null && layer.isBackground()) {
			return false;
		}
		
		if (sel instanceof IContentData) {
			if (!hasMatchingClipboardContent(sel)) {
				return false;
			}
		}

		if (getContentToPaste() != null) {
			if (layer != null) {
				if (layer.isBitmapImage() || layer.isSvgImage()) {
					
					return ClipboardHelper.clipboardContainsImageData(clip);
				}
			}
		}

		IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(sel);
		return pasteAdapter == null ? false : pasteAdapter.isPasteAvailable(
				getContentToPaste(), null);
	}

	private boolean hasMatchingClipboardContent(Object selection) {

		int selectedElementPartCount = 1;
		if (((IContentData) selection).getChildren() != null) {
			selectedElementPartCount = ((IContentData) selection).getChildren().length;
			ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) ((IContentData) selection)
					.getAdapter(ISkinnableEntityAdapter.class);
			// if the element is converted from multi-piece;
			// set the selectedElementPartCount to 1
			if (adapter.getThemeGraphics() != null
					&& adapter.isConvertedFromMultipiece()) {
				selectedElementPartCount = 1;
			}
		}

		if (selectedElementPartCount == 0)
			selectedElementPartCount = 1;

		if (ClipboardHelper.clipboardContainsMultipleImagesData(clip,
				selectedElementPartCount))
			return true;

		return false;
	}

}