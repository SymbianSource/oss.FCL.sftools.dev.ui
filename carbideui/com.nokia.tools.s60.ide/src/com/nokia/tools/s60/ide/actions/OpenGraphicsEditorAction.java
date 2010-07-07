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
package com.nokia.tools.s60.ide.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.s60.editor.GraphicsEditorInput;
import com.nokia.tools.s60.editor.GraphicsEditorPart;
import com.nokia.tools.s60.editor.Series60EditorPart;
import com.nokia.tools.s60.editor.actions.AbstractMultipleSelectionAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.editor.IContentDependentEditor;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

/**
 */
public class OpenGraphicsEditorAction extends AbstractMultipleSelectionAction {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "editAnimateSelectedElements_context"; 

	public static final String ID = "com.nokia.tools.s60.ide.actions.OpenGraphicsEditorAction";

	public OpenGraphicsEditorAction(IWorkbenchPart part) {
		super(part);
		setId(ID);
		initialize();
	}

	public OpenGraphicsEditorAction(IWorkbenchPart part,
			ISelectionProvider provider) {
		super(part);
		setId(ID);
		setSelectionProvider(provider);
		initialize();
	}

	private void initialize() {
		setText(ActionMessages.OpenGraphicsEditorAction_text);
		setToolTipText(ActionMessages.OpenGraphicsEditorAction_tooltip);
		ImageDescriptor enabled = S60WorkspacePlugin.getIconImageDescriptor(
				"edit_animate16x16.png", true);
		ImageDescriptor disabled = S60WorkspacePlugin.getIconImageDescriptor(
				"edit_animate16x16.png", false);
		setImageDescriptor(enabled);
		setHoverImageDescriptor(enabled);
		setDisabledImageDescriptor(disabled);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				OpenGraphicsEditorAction.HLP_CTX);
	}

	protected Set<String> getContentDataIds(GraphicsEditorInput input) {
		Set<String> result = new HashSet<String>();
		List data = input.getData();
		for (Object object : data) {
			if (object instanceof IContentData) {
				result.add(((IContentData) object).getId());
			} else if (object instanceof EditPart) {
				EditPart part = (EditPart) object;
				IScreenElement scrEl = getScreenElement(part);
				if (scrEl != null) {
					result.add(scrEl.getData().getId());
				}
			}
		}
		return result;
	}

	protected IEditorInput createGraphicsInput(Object element) {

		List<Object> sels = new ArrayList<Object>(); // getSelectedElements(fetchSelection());
		sels.add(element);
		if (null != sels && !sels.isEmpty()) {
			Object sel = sels.get(0);
			if (sel instanceof EditPart) {
				EditPart part = (EditPart) sel;
				if (!(part.getModel() instanceof EObject)) {
					throw new RuntimeException("Invalid editPart: "
							+ part.getClass().getName());
				}
			}

			Series60EditorPart parent = null;
			if (getWorkbenchPart() instanceof Series60EditorPart) {
				parent = (Series60EditorPart) getWorkbenchPart();
			} else {
				IEditorPart ed = EclipseUtils.getActiveSafeEditor();
				if (ed instanceof Series60EditorPart) {
					parent = (Series60EditorPart) ed;
				} else if (ed instanceof IContentDependentEditor) {
					IEditorPart tmp = ((IContentDependentEditor) ed)
							.getParentEditor();
					parent = (Series60EditorPart) (tmp instanceof Series60EditorPart ? tmp
							: null);
				} else if (ed != null) {
					// tries the adapter for the case the main editor is
					// embedded
					parent = (Series60EditorPart) ed
							.getAdapter(Series60EditorPart.class);
				}
			}
			if (parent != null)
				return new GraphicsEditorInput(sels, (CommandStack) parent
						.getAdapter(CommandStack.class), parent);
			else
				throw new RuntimeException(
						"Cannot create editor input - parent editor not found");
		}
		throw new RuntimeException(
				"Cannot create editor input - selection is empty");
	}

	@Override
	protected void doRun(Object element) {

		IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		GraphicsEditorInput editorInput = (GraphicsEditorInput) createGraphicsInput(element);
		Set<String> editorInputContentDataIds = getContentDataIds(editorInput);

		// check if editor already opened
		IEditorReference[] references = window.getActivePage()
				.getEditorReferences();
		for (IEditorReference reference : references) {
			try {
				IEditorInput input = reference.getEditorInput();
				if (input instanceof GraphicsEditorInput) {
					GraphicsEditorInput refEditorInput = (GraphicsEditorInput) input;
					if (refEditorInput.getParentEditor() == editorInput
							.getParentEditor()) {
						Set<String> refEditorInputContentDataIds = getContentDataIds(refEditorInput);
						for (String id : refEditorInputContentDataIds) {
							if (editorInputContentDataIds.contains(id)) {
								// element already opened in another editor
								window.getActivePage().activate(
										reference.getEditor(true));
								return;
							}
						}
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}

		try {
			window.getActivePage().openEditor(editorInput,
					GraphicsEditorPart.ID);
		} catch (PartInitException e) {
			S60WorkspacePlugin.error(e);
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object obj) {

		IContentData data = null;
		if (obj instanceof IContentData) {
			data = (IContentData) obj;
		} else {
			data = getContentData(obj);
		}

		if (data != null) {
			IToolBoxAdapter toolBoxAdapter = (IToolBoxAdapter) data
					.getAdapter(IToolBoxAdapter.class);
			if (toolBoxAdapter != null
					&& toolBoxAdapter.isMultipleLayersSupport())
				return true;

			IImageAdapter imageAdapter = (IImageAdapter) data
					.getAdapter(IImageAdapter.class);
			if (imageAdapter != null)
				if (imageAdapter.isAnimated())
					return true;
		}

		return false;
	}

}