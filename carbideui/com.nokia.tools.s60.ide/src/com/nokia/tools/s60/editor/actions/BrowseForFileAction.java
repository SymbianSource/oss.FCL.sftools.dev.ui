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
/**
 * 
 */

package com.nokia.tools.s60.editor.actions;

import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.s60.editor.commands.DropImageCommand;
import com.nokia.tools.s60.editor.commands.PasteImageCommand;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.s60.editor.ui.views.LayersView;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.INamingAdapter;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.dialogs.FileResourceSelectionPage;
import com.nokia.tools.screen.ui.dialogs.ImageFileContentProvider;
import com.nokia.tools.screen.ui.dialogs.ThemeResourceSelectionDialog;
import com.nokia.tools.ui.dialog.IResourceSelectionPage;

/**
 * Enables skinning of ISkinnableentity adaptable selection by browsing to the
 * file to be skinned. 
 */
public class BrowseForFileAction extends AbstractAction {

	public static final String ID = BrowseForFileAction.class.getPackage()
			.getName()
			+ ".selectFile";

	public BrowseForFileAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
		multipleSelection = false;
		multipleSelectionEnablement = MultipleSelectionEnablementEnum.ONE;
	}

	public BrowseForFileAction(ISelectionProvider provider, CommandStack _stack) {
		super(null);
		setSelectionProvider(provider);
		setLazyEnablementCalculation(true);
		stack = _stack;
		multipleSelection = false;
		multipleSelectionEnablement = MultipleSelectionEnablementEnum.ONE;
	}

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.BrowseForFileAction_name);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/select_file.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/select_file.gif"));
		setToolTipText(Messages.BrowseForFileAction_tooltip);
		super.init();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.s60.editor.actions.AbstractAction#doRun(java.lang.Object)
	 */
	@Override
	protected void doRun(Object element) {
		final IContentData data = getContentData(element);
		if (data != null || element instanceof IAnimationFrame) {
			String path = null;
			if (data != null) {
				path = selectFileForData(data);
			} else {
				List<String> files = selectFiles(null, null);
				if (files.size() > 0) {
					path = files.get(0);
				}
			}
			if (path == null) {
				// cancel pressed
				return;
			}
			final String p = path;
			/* check for file existence, show warning in status bar if not */
			if (!new File(path).exists()) {
				// shows warning in the status bar and exit
				if (getWorkbenchPart() != null) {
					IWorkbenchPartSite site = getWorkbenchPart().getSite();
					if (site instanceof IWorkbenchPartSite) {
						try {
							IActionBars bars = (IActionBars) site.getClass()
									.getMethod("getActionBars", (Class[]) null)
									.invoke(site, (Object[]) null);
							IStatusLineManager manager = bars
									.getStatusLineManager();
							if (manager != null) {
								String message = Messages.bind(
										Messages.BrowseForFileAction_notfound,
										path);
								manager.setErrorMessage(message);
								manager.update(false);
							}
						} catch (Exception e) {
						}
					}
				}
				return;
			}

			if (data != null) {
				ISkinnableEntityAdapter sa = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
				if (sa != null) {
					
					if (sa.isMultiPiece()
							|| PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.getActivePart() instanceof LayersView) {

						ILayer layer = getLayer(false, element);
						IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(element);
						if (layer != null && pasteAdapter != null) {
							try {
								
								// Adding a layer, if one doesnot exist
								IImage img = layer.getParent();
								if (layer.isBackground()) {
									if (img.getLayerCount() > 1) {
										layer = img.getLayer(1);
									} else {
										// add layer when only BG present 
										layer = img.addLayer();
									}
								}
							// Backup suppression status
								boolean isSuppressNotification = sa
										.isSuppressNotification();
								sa.setSuppressNotification(true);
								pasteAdapter.paste(path, layer);
								sa
										.setSuppressNotification(isSuppressNotification);
								updateGraphicWithCommand(layer, layer);
							} catch (Exception e) {
								S60WorkspacePlugin.error(e);
							}
						}
						return;
					}

					IMediaFileAdapter fileAdapter = (IMediaFileAdapter) data
							.getAdapter(IMediaFileAdapter.class);

					ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
							com.nokia.tools.s60.editor.commands.Messages.PasteImage_Label);
					if (fileAdapter != null) {
						command.add(new DropImageCommand(
								getContentData(element), path, null));
					} else {
						command.add(new PasteImageCommand(data,
								getEditPart(element), path));
					}
					Command stretchModeCommand = sa
							.getApplyStretchModeCommand(sa.getStretchMode());
					if (stretchModeCommand.canExecute()) {
						command.add(stretchModeCommand);
					}
					execute(command, getEditPart(element));
					return;
				}
			}
			
			final IImageHolder holder = getImageHolder(element);
			if (holder != null) {
				EditPart part = getEditPart(element);
				final IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(element);
				execute(new UndoableImageHolderActionCommand(holder,
						new Runnable() {

							public void run() {
								try {
									pasteAdapter.paste(Collections
											.singletonList(new File(p)), null);
								} catch (HeadlessException e) {
									e.printStackTrace();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}), part);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.s60.editor.actions.AbstractAction#doCalculateEnabled(
	 * java.lang.Object)
	 */
	@Override
	protected boolean doCalculateEnabled(Object element) {
		IContentData data = getContentData(element);
		if (data != null) {
			IColorAdapter toolBoxAdapter = (IColorAdapter) data
					.getAdapter(IColorAdapter.class);
			if (null != toolBoxAdapter) {
				return false;
			}

			IImageHolder holder = getImageHolder(element);
			if (holder != null) {
				return true;
			}

			ILayer layer = getLayer(false, element);

			if (layer != null) {
				
				if (PlatformUI.getWorkbench() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage() != null) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().getActivePart();

				}
				if (!layer.getParent().isMultiPiece()
						&& (layer.isBitmapImage() || layer.isSvgImage() || PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().getActivePart() instanceof LayersView)) {
					return true;
				}
			} else if (element instanceof IAnimationFrame) {
				return true;
			}

			ISkinnableEntityAdapter sa = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			if (sa != null && sa.isMultiPiece()) {
				return false;
			}
		} else {
			if (element instanceof IAnimationFrame) {
				return true;
			}
		}
		return false;
	}

	public static List<String> selectFiles(String initialSelection,
			final String[] extensions) {
		FileResourceSelectionPage page = new FileResourceSelectionPage();
		page.setContentProvider(new ImageFileContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * com.nokia.tools.screen.ui.dialogs.ImageFileContentProvider#acceptFile
			 * (java.io.File)
			 */
			@Override
			protected boolean acceptFile(File file) {
				if (null != extensions) {
					String extension = "." + FileUtils.getExtension(file);
					for (String ext : extensions) {
						
						if (!(extension.equals(".zip")))
							if (ext.equalsIgnoreCase(extension))
								return true;
					}
				}
				return super.acceptFile(file);
			}

		});

		ThemeResourceSelectionDialog<String> rd = new ThemeResourceSelectionDialog<String>(
				Display.getCurrent().getActiveShell(),
				new IResourceSelectionPage[] { page },
				Messages.SelectImage_bannerMessage);
		if (null != initialSelection && new File(initialSelection).exists()) {
			rd.setResources(new Object[] { new File(initialSelection) });
		} else
			rd.create();
		List<String> paths = new ArrayList<String>();
		if (Dialog.OK == rd.open()) {
			Object[] resources = rd.getResources();
			if (resources != null && resources.length > 0) {
				paths.add(new File(resources[0].toString()).getAbsolutePath());
			}
		}
		return paths;
	}

	public String selectFileForData(final IContentData data) {
		INamingAdapter nameFetch = (INamingAdapter) data
				.getAdapter(INamingAdapter.class);
		ISkinnableEntityAdapter editingHelper = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
		IToolBoxAdapter toolbox = (IToolBoxAdapter) data
				.getAdapter(IToolBoxAdapter.class);
		if (null != toolbox && toolbox.isFile()) {
			FileDialog fileDialog = new FileDialog(Display.getCurrent()
					.getActiveShell(), SWT.OPEN);
			fileDialog.setText(nameFetch.getName() + " - " + getText());

			fileDialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot()
					.getLocation().toOSString());
			String exts = "";
			for (String ext : editingHelper.getSupportedFileExtensions()) {
				exts += ("*" + ext + ";");
			}
			if (0 != exts.length())
				exts = exts.substring(0, exts.length() - 1);

			fileDialog.setFilterExtensions(new String[] { exts.toLowerCase() });

			if (fileDialog.open() != null) {

				String separator = "";
				int length = fileDialog.getFilterPath().trim().length();
				if (length > 0
						&& fileDialog.getFilterPath().charAt(length - 1) != File.separatorChar)
					separator = File.separator;

				return new Path(fileDialog.getFilterPath() + separator
						+ fileDialog.getFileName()).toOSString();
			}
		} else {
			List<String> files = selectFiles(null,
					(null == editingHelper) ? null : editingHelper
							.getSupportedFileExtensions());
			if (files.size() > 0)
				return files.get(0);
		}
		return null;
	}

}
