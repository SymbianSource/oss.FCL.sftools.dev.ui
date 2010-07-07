package com.nokia.tools.screen.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.DeleteResourceAction;

import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.screen.ui.wizards.DeleteProjectDialog;


/**
 * Deleting the theme action Wraps standard DeleteResourceAction and provides
 * custom dialog TODO: dialog should have listener about closed and open
 * projects
 * 
 * 
 * @author balasjay
 */
public class DeleteProjectAction extends Action implements
IWorkbenchWindowActionDelegate {
	private Shell shell;

	private DeleteResourceAction fWorkbenchAction;

	protected DeleteResourceAction getWorkbenchAction() {
		if (null == fWorkbenchAction)
			fWorkbenchAction = new DeleteResourceAction(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell());
		return fWorkbenchAction;
	}

	Shell getShell() {
		return shell;
	}

	public DeleteProjectAction() {
		super();
	}

	public DeleteProjectAction(Shell shell) {
		super();
		try {
			this.shell = shell;
			fWorkbenchAction = new DeleteResourceAction(shell);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IViewReference[] viewRefs = page.getViewReferences();	
		for(int i=0;i<viewRefs.length;i++){		 
			if (viewRefs[i].getId().equals("org.eclipse.ui.internal.introview")) {
				IViewPart part = page.findView("org.eclipse.ui.internal.introview");
				page.hideView(part);	       
			}
		}

		DeleteProjectDialog deleteProjectDialog = new DeleteProjectDialog(getShell());
		deleteProjectDialog.create();
		if (deleteProjectDialog.open() == Dialog.OK) {
			String[] projectNames = deleteProjectDialog.getSelectedProjects();
			for (String projectName : projectNames) {
				String extractedDotProject = ResourceUtils
				.extractDotProjectPath(projectName);
				IPath projectPath = new Path(extractedDotProject);
				IProject openedProject = null;
				if (projectPath.segmentCount() == 1
						&& ResourcesPlugin.getWorkspace().getRoot().getProject(
								projectName).exists()) {
					openedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					try {
						openedProject.delete(true, new NullProgressMonitor());
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		} else
			notifyResult(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}
}
