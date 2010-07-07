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
package com.nokia.tools.screen.ui.utils;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class EclipseUtils implements IWindowListener, IPageListener,
		IPartListener {

	public static EclipseUtils INSTANCE;
	private IEditorPart activeEditor = null;
	private boolean inited = false;

	static { // static initializer
		INSTANCE = new EclipseUtils();
	}

	/**
	 * In case when view is maximized or when calling from other than display
	 * thread, call to PlatformUI.getWorkbench().getActiveWorkbenchWindow()
	 * .getActivePage().getActiveEditor() returns null. For those purposes this
	 * should be used. It is more robust method, returns default if not null. If
	 * PlatformUI... getActiveEditor returns null, tracks the references of the
	 * last active editor and returns them as active editor.
	 * 
	 * @return active or last active if no editor is displayed editor
	 */
	public static IEditorPart getActiveSafeEditor() {
		IEditorPart editorPart = null;
		try {
			if (PlatformUI.getWorkbench() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage() != null) {
				editorPart = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.getActiveEditor();
			} else {
				if (PlatformUI.getWorkbench() != null) {
					if ((PlatformUI.getWorkbench().getWorkbenchWindowCount() > 0) &&
							PlatformUI.getWorkbench().getWorkbenchWindows() != null &&
							PlatformUI.getWorkbench().getWorkbenchWindows()[0] != null &&
							PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage() != null)
						editorPart = PlatformUI.getWorkbench()
							.getWorkbenchWindows()[0].getActivePage()
							.getActiveEditor();
				}

			}
		} catch (NullPointerException e) {
			// do nothing
		}
		if (null == editorPart) {
			return getActiveEditor();
		}
		return editorPart;
	}

	/**
	 * use getActiveSafe Editor
	 * 
	 * @return
	 */
	private static IEditorPart getActiveEditor() {
		synchronized (INSTANCE) {
			return INSTANCE.activeEditor;
		}
	}

	/**
	 * Initializes listeners to all elements - main goal is to get informed on
	 * every part, which is activated
	 */
	public static void init() {
		INSTANCE.initInstance();
	}

	private EclipseUtils() {
	}

	/**
	 * Initializes listeners to all elements - main goal is to get informed on
	 * every part, which is activated
	 */
	private void initInstance() {
		synchronized (INSTANCE) {
			if (inited) {
				return;
			}
			inited = true;
		}
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		// first he tries to set active editor as is
		setDefaultActiveEditor();

		// first set this as listener for window creating
		workbench.addWindowListener(this);
		// go over all windows and set this as listener for page creating
		if (workbench.getWorkbenchWindowCount() > 0) {
			for (int i = 0; i < workbench.getWorkbenchWindowCount(); i++) {
				addListeners(workbench.getWorkbenchWindows()[i]);
			}
		}
		// now, every time, when any new window, or page is added, we
		// will be informed about it and we could add part listener
		// to every page
	}

	/**
	 * Sets listeners to window.
	 * 
	 * @param window
	 */
	private void addListeners(IWorkbenchWindow window) {
		window.addPageListener(this);
		// now go over all pages and add part listener
		if (window.getPages() != null && window.getPages().length > 0) {
			for (int i = 0; i < window.getPages().length; i++) {
				window.getPages()[i].addPartListener(this);
			}
		}
	}

	/**
	 * Sets listener to page.
	 * 
	 * @param page
	 */
	private void addListeners(IWorkbenchPage page) {
		page.addPartListener(this);
	}

	/**
	 * Old way :)
	 */
	private void setDefaultActiveEditor() {
		try {
			activeEditor = null;
			if (PlatformUI.getWorkbench() != null) {
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
					activeEditor = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.getActiveEditor();
				} else if (PlatformUI.getWorkbench()
						.getWorkbenchWindowCount() > 0) {
					activeEditor = PlatformUI.getWorkbench()
							.getWorkbenchWindows()[0].getActivePage()
							.getActiveEditor();
				}
			}
		} catch (NullPointerException ex) {
			// do nothing
		}
	}

	private void setEditor(IWorkbenchPart part) {
		if (part == null || !(part instanceof IEditorPart)) {
			return;
		}
		synchronized (INSTANCE) {
			activeEditor = (IEditorPart) part;
		}
		// System.out.println("editor switched to " + part);
	}

	public void partActivated(IWorkbenchPart part) {
		setEditor(part);
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		setEditor(part);
	}

	public void partOpened(IWorkbenchPart part) {
		setEditor(part);
	}

	// if actual editor were closed
	public void partClosed(IWorkbenchPart part) {
		synchronized (INSTANCE) {
			if (part == activeEditor) {
				if (part == activeEditor) {
					setDefaultActiveEditor();
				}
			}
		}
	}

	// new window created - add part listeners
	public void windowOpened(IWorkbenchWindow window) {
		addListeners(window);
	}

	// new page created - add part listener
	public void pageOpened(IWorkbenchPage page) {
		addListeners(page);
	}

	// not important for us
	public void windowActivated(IWorkbenchWindow window) {
	}

	public void windowClosed(IWorkbenchWindow window) {
	}

	public void windowDeactivated(IWorkbenchWindow window) {
	}

	public void pageActivated(IWorkbenchPage page) {
	}

	public void pageClosed(IWorkbenchPage page) {
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

}
