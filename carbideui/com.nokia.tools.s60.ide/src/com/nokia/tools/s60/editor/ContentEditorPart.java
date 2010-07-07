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
package com.nokia.tools.s60.editor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.content.core.IContentListener;
import com.nokia.tools.content.core.IContentService;
import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorPart;

/**
 * Some content moved from Series60EditorPart
 */
abstract class ContentEditorPart extends ScreenEditorPart implements
		IContentAdapter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#dispose()
	 */
	@Override
	public void dispose() {
		if (contentService != null) {
			contentService.dispose();
		}
		super.dispose();
	}

	protected ScreenModelMediator mediator;

	protected IContentService.Stub contentService;

	public static final String THEME_CONTEXT = IS60IDEConstants.PLUGIN_ID + '.'
			+ "theme_context";

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		super.setInput(input);

		if (checkInput() != null) {
			return;
		}
		contentService = new IContentService.Stub();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentAdapter#getContents()
	 */
	public IContent[] getContents() {
		return mediator.getActiveContents().toArray(
				new IContent[mediator.getActiveContents().size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentAdapter#getContents(java.lang.String)
	 */
	public IContent[] getContents(String type) {
		if (mediator == null || type == null) {
			return new IContent[0];
		}
		List<IContent> contents = new ArrayList<IContent>();
		for (IContent content : getContents()) {
			if (type.equals(content.getType())) {
				contents.add(content);
			}
		}
		return contents.toArray(new IContent[contents.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.IContentAdapter#refresh(Object)
	 */
	public void updateContent(Object hint) {
		if (hint == ContentAttribute.PLATFORM) {
			mediator.refreshViewers();
		} else {
			reloadContent();
		}
	}

	protected void reloadContent() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#checkInput()
	 */
	protected String checkInput() {
		IEditorInput input = getEditorInput();
		if (input.getAdapter(IContent.class) != null) {
			return null;
		}
		try {
			if (!(input instanceof IFileEditorInput)) {
				return MessageFormat.format(
						EditorMessages.Editor_Error_InvalidInput,
						new Object[] { input.getName() });
			}
			IPath location = ((IFileEditorInput) input).getFile().getLocation();
			if (!location.toFile().exists()) {
				return MessageFormat.format(
						EditorMessages.Editor_Error_InputNotExist,
						new Object[] { location });
			}
		} catch (Exception e) {
			return MessageFormat
					.format(
							EditorMessages.Editor_Error_MissingEmbeddedEditorDescriptor,
							new Object[] { input.getName() });
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#performSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void performSave(IProgressMonitor monitor) {
		for (GraphicsEditorPart gep : getDirtyGraphicsEditors()) {
			if (MessageDialog
					.openConfirm(
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							EditorMessages.GraphicsEditor_SavingConfirmation_Label,
							MessageFormat
									.format(
											EditorMessages.Editor_SaveGraphicsEditorConfirmation,
											new Object[] { gep.getPartName() }))) {
				gep.doSave(monitor);
			}
		}

		try {
			mediator.saveContents(monitor);
			getProject().refreshLocal(IProject.DEPTH_INFINITE, monitor);

			// force dependent view to refresh - 'null' means that unknown
			// object is modified,
			// thus all should be refreshed
			fireContentModified(new String[] { null });
		} catch (Exception e) {
			S60WorkspacePlugin.error(e);
			MessageDialog.openError(getSite().getShell(),
					EditorMessages.Error_Editor_Save, e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		ProjectSaveAsDialog dialog = new ProjectSaveAsDialog(getSite()
				.getShell());
		if (dialog.open() == Window.OK) {
			IPath newPath = null;
			if (!dialog.useDefaults()) {
				newPath = dialog.getLocation();
			}

			IProject destination = dialog.getProject();
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProjectDescription description = workspace
					.newProjectDescription(destination.getName());
			description.setLocation(newPath);

			try {
				destination.create(description, null);
				destination.open(IResource.BACKGROUND_REFRESH, null);

				S60DesignProjectNature.addNatureToProject(destination,
						S60DesignProjectNature.NATURE_ID);

				IContent theme = mediator.getPrimaryContent();
				if (theme != null) {
					theme.setAttribute(
							ContentAttribute.APPLICATION_NAME.name(),
							destination.getName());
				}

				PlatformUI.getWorkbench().getProgressService().run(true, false,
						new SaveAsOperation(getContents(), destination));
				getEditDomain().getCommandStack().markSaveLocation();

				IFile file = ResourceUtils.getProjectResourceByAbsolutePath(
						destination, (IPath) theme.getAdapter(IPath.class));
				if (file != null) {
					IEditorInput editorInput = new FileEditorInput(file);
					setInput(editorInput);
				} else {
					S60WorkspacePlugin.error("File save as failed: no path: "
							+ theme.getAdapter(IPath.class)
							+ " can be matched in the new project: "
							+ destination);
				}

				Object[] editorPart = mediator.getEditors().toArray();
				for (int i = 0; i < editorPart.length; i++) {
					if (editorPart[i] instanceof IEmbeddedEditorPart) {
						((IEmbeddedEditorPart) editorPart[i])
								.doSaveAs(destination);
					}
				}

				for (IContent content : getContents()) {
					content.setAttribute(ContentAttribute.PROJECT.name(),
							destination);
				}
				// refreshes other views
				fireContentRootChanged();
			} catch (Exception e) {
				boolean errorDisplayed = false;
				if (e instanceof CoreException) {
					if (((CoreException) e).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
						MessageDialog
								.openError(
										getSite().getShell(),
										EditorMessages.Error_Editor_SaveAs,
										EditorMessages.Error_Editor_SaveAs_caseVariantExistsError);
						errorDisplayed = true;
					}
				}
				if (!errorDisplayed) {
					S60WorkspacePlugin.error(e);
					MessageDialog.openError(getSite().getShell(),
							EditorMessages.Error_Editor_SaveAs, e.getMessage());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.editor.ScreenEditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (super.isDirty()) {
			return true;
		}

		if (!getDirtyGraphicsEditors().isEmpty()) {
			return true;
		}

		if (mediator == null) {
			return false;
		}

		return mediator.isEmbeddedEditorDirty();
	}

	private List<GraphicsEditorPart> getDirtyGraphicsEditors() {
		// check thorugh all open editors, if there are AnimationEditors opened
		// from this
		List<GraphicsEditorPart> gps = new ArrayList<GraphicsEditorPart>();
		IWorkbenchPage page = getSite().getWorkbenchWindow().getActivePage();
		if (page != null) {
			IEditorReference[] eds = page.getEditorReferences();
			for (int i = 0; i < eds.length; i++) {
				if (eds[i].getEditor(false) instanceof GraphicsEditorPart) {
					GraphicsEditorPart gep = (GraphicsEditorPart) eds[i]
							.getEditor(false);
					GraphicsEditorInput input = (GraphicsEditorInput) gep
							.getEditorInput();
					if (input.getParentEditor() == this && gep.isDirty()) {
						gps.add(gep);
					}
				}
			}
		}
		return gps;
	}

	/**
	 * Notifies that the content has changed.
	 */
	protected void fireContentRootChanged() {
		fireContentRootChanged(mediator.getContent(getEditPartViewer()));
	}

	protected void fireContentRootChanged(IContent content) {
		if (contentService != null) {
			contentService.fireRootContentChanged(content);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.IContentProvider#addContentListener(com.nokia.tools.content.core.IContentListener)
	 */
	public void addContentListener(IContentListener listener) {
		if (contentService != null) {
			contentService.addContentListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.IContentProvider#removeContentListener(com.nokia.tools.content.core.IContentListener)
	 */
	public void removeContentListener(IContentListener listener) {
		if (contentService != null) {
			contentService.removeContentListener(listener);
		}
	}

	public void fireContentModified(final String[] modifiedContentIds) {
		if (contentService != null) {
			contentService.fireContentModified(modifiedContentIds);
		}
	}

}
