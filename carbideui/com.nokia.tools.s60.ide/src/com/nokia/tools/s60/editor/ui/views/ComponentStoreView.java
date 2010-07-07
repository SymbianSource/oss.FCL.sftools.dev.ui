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
package com.nokia.tools.s60.editor.ui.views;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.part.ViewPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.IRefreshableObject;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.s60.editor.actions.CopyImageAction;
import com.nokia.tools.s60.editor.actions.PasteGraphicsAction;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.editor.ScreenEditorPart;
import com.nokia.tools.screen.ui.utils.SimpleSelectionProvider;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreFactory;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreWidget;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreWidget.DisplayMode;
import com.nokia.tools.theme.s60.ui.cstore.StoredElementViewer.ActionProvider2;

public class ComponentStoreView extends ViewPart implements ISelectionListener,
		IPartListener, IRunnableWithProgress, Runnable,
		IContributedContentsView, ActionProvider2, IRefreshableObject {

	static String[] partNameTags = new String[] { "Left", "Right", "Top",
			"Bottom", "Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right",
			"Center" };

	private Composite parent;
	private ComponentStoreWidget widget;
	private IContentData data;
	private Object graphics;
	protected int runWhat;
	public static final String COMPONENTSTORE_CONTEXT = "com.nokia.tools.s60.ide.componentStore_context"; 
	
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		createView();
		getSite().getWorkbenchWindow().getPartService().addPartListener(this);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				COMPONENTSTORE_CONTEXT);
		

	}

	private void createView() {
		for (Control c : parent.getChildren()) {
			try {
				c.dispose();
			} catch (Exception e) {
			}
		}
		widget = new ComponentStoreWidget(parent, SWT.NONE);

		widget.setClientActionProvider(this);

		parent.layout();

		/* initialize selection */
		try {
			selectionChanged(getSite().getWorkbenchWindow().getActivePage()
					.getActivePart(), getSite().getWorkbenchWindow()
					.getSelectionService().getSelection());
		} catch (Exception e) {
		}
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getWorkbenchWindow().getSelectionService().addSelectionListener(
				this);
		setPartName(Messages.SimilarComponents);
		setTitleToolTip(Messages.SimilarComponentsTooltip);

		contributeActions(site.getActionBars());
	}

	/**
	 * called from pref page when apply happens to apply changes before dialog
	 * closes
	 */
	public void notifyRefreshNeeded(Object hint) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				String viewId = "com.nokia.tools.theme.s60.ui.views.ComponentStore";
				ComponentStoreView instance = (ComponentStoreView) PlatformUI
						.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().findView(viewId);
				if (instance != null) {
					try {
						instance.runWhat = 1;
						instance.getViewSite().getWorkbenchWindow()
								.getWorkbench().getProgressService()
								.busyCursorWhile(instance);
					} catch (Exception e) {
					}
				}
			}

		});
	}

	private void contributeActions(IActionBars actionBars) {

		IToolBarManager mngr = actionBars.getToolBarManager();
		Action customize = new Action(Messages.cstore_customizeAction) {
			@Override
			public void run() {

				String linkAddress = "com.nokia.tools.s60.preferences.ComponentStorePrefPage";

				PreferencesUtil.createPreferenceDialogOn(
						Display.getCurrent().getActiveShell(), linkAddress,
						new String[] { linkAddress }, null).open();
				try {
					runWhat = 1;
					getViewSite().getWorkbenchWindow().getWorkbench()
							.getProgressService().busyCursorWhile(
									ComponentStoreView.this);
				} catch (Exception e) {
				}
			}
		};
		customize.setToolTipText(Messages.cstore_customizeAction);
		customize.setText(Messages.cstore_customizeAction);
		customize.setDescription(Messages.cstore_customizeAction);
		customize.setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/configs.gif"));
		mngr.add(customize);

	}

	/**
	 * performs switch between id matching mode and manual navigation mode
	 */
	protected void doSwitchMode() {
		DisplayMode mode = widget.getDisplayMode();
		int pos = (mode.ordinal() + 1) % DisplayMode.values().length;
		widget.setDisplayMode(DisplayMode.values()[pos]);
	}

	@Override
	public void setFocus() {
		widget.setFocus();
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		widget.selectionChanged(part, selection);
	}

	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		getSite().getWorkbenchWindow().getPartService()
				.removePartListener(this);
		super.dispose();
	}

	public void partActivated(IWorkbenchPart part) {
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
		if (part instanceof ScreenEditorPart) {
			widget.clearContents();
			// clears the reference
			data = null;
		}
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		Display.getDefault().asyncExec(this);
	}

	public void run() {
		if (runWhat == 1) {
			runWhat = 0;
			ComponentStoreFactory.refreshComponentPool();
			widget.refreshContents();
		} else {
			// execute through copy/paste layers
			PasteGraphicsAction pga = new PasteGraphicsAction(
					new SimpleSelectionProvider(data), null, null);
			pga.setThemeGraphic(graphics);
			pga.run();
		}
	}

	@Override
	public Object getAdapter(Class adapter) {

		if (widget.getSourcePart() != null
				&& adapter == IContributedContentsView.class) {
			if (widget.getSourcePart() instanceof IContributedContentsView)
				return widget.getSourcePart();
			else {
				return this;
			}
		}

		return super.getAdapter(adapter);
	}

	public IWorkbenchPart getContributingPart() {
		return widget.getSourcePart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.s60.ui.views.scv.IContentDataListViewer.ActionProvider#fillElementContextMenu(org.eclipse.jface.action.IMenuManager,
	 *      com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement)
	 */
	public void fillElementContextMenu(IMenuManager manager,
			final Object element, final List<Object> multiSel) {
		if (multiSel != null && multiSel.size() < 2) {

			/* apply graphics action */
			Action applySkin = new Action(Messages.cstore_apply) {
				@Override
				public void run() {
					processDoubleClick(element, multiSel);
				}
			};
			applySkin.setImageDescriptor(PlatformUI.getWorkbench()
					.getSharedImages().getImageDescriptor(
							ISharedImages.IMG_TOOL_REDO));
			manager.add(applySkin);

			/* copy action */
			ISelectionProvider sp = new SimpleSelectionProvider(element);
			CopyImageAction cia = new CopyImageAction(sp, null);
			manager.add(cia);
		}
	}

	public void processDoubleClick(Object element, List<Object> multiSel) {
		data = widget.getSourceData();

		// adapt screen element to screen context if needed
		data.getAdapter(EditObject.class);

		if (element instanceof IContentData) {
			IImageAdapter helper = (IImageAdapter) ((IContentData) element)
					.getAdapter(IImageAdapter.class);
			element = helper.getImage(true);
		}

		if (element instanceof IImage) {
			IImage img = (IImage) element;
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);

			// execute through copy/paste layers
			//if (!img.isNinePiece()) {
			if (!img.isMultiPiece()) {
				this.graphics = skAdapter.getEditedThemeGraphics(img, true);
			} else {
				ArrayList<File> files = new ArrayList<File>();
				try {
					List<IImage> parts = img.getPartInstances();

					// sort parts to unified order
					List<String> partNames = Arrays.asList(partNameTags);

					IImage[] sorted = new IImage[9];
					for (int i = 0; i < parts.size(); i++) {
						int idx = partNames.indexOf(parts.get(i).getPartType());
						sorted[idx] = parts.get(i);
					}

					parts = Arrays.asList(sorted);

					for (IImage part : parts) {
						files.add(new File(part.getLayer(0).getFileName(true)));
					}

					for (IImage part : parts) {
						files.add(new File(part.getLayer(0).getMaskFileName(
								true)));
					}
				} catch (Exception e1) {
				}
				this.graphics = files;
			}
		}

		/* run skin action in own job */
		try {
			getSite().getWorkbenchWindow().getWorkbench().getProgressService()
					.busyCursorWhile(this);
		} catch (Exception e) {
		}
	}

}
