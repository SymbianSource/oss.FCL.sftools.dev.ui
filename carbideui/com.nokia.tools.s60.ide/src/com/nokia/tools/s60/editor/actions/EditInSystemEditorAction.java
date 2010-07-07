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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.symbian.mbm.BitmapConverter;
import com.nokia.tools.media.utils.BaseRunnable;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.editor.commands.SetThemeGraphicsCommand;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread.FileInfo;

/**
 * Action for opening editor of image or sound element in editor and related
 * views.
 */
public class EditInSystemEditorAction extends AbstractEditAction {

	public static final String ID = "EditInSystemEditor"; 

	public EditInSystemEditorAction(ISelectionProvider provider,
			CommandStack stack) {
		super(provider, stack);
	}

	public EditInSystemEditorAction(IWorkbenchPart part) {
		super(part);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractEditAction#doRun(java.lang.Object)
	 */
	@Override
	public void doRun(Object element) {
		try {
			// graphics
			IImageHolder holder = getImageHolder(element);
			if (holder != null) {
				Program program = getProgram(holder.getImageFile());
				if (program != null) {
					openEditor(program, holder, element);
					return;
				}
			}
			final ILayer layer = getLayer(false, element);
			if (layer != null) {
				Program program = getProgram(new File(layer.getFileName(true)));
				if (program != null) {
					openEditor(program, layer, element);
					return;
				}
			}
			// sounds
			IContentData data = getContentData(element);
			if (data != null) {
				IMediaFileAdapter fileAdapter = (IMediaFileAdapter) data
						.getAdapter(IMediaFileAdapter.class);
				if (fileAdapter != null
						&& !StringUtils.isEmpty(fileAdapter.getFileName(true))
						&& fileAdapter.isSound()) {
					Program program = getProgram(new File(fileAdapter
							.getFileName(true)));
					if (program != null) {
						openEditor(program, fileAdapter, element);
						return;
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			handleProcessException(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractEditAction#doCalculateEnabled(java.lang.Object)
	 */
	@Override
	protected boolean doCalculateEnabled(Object element) {
		IContentData data = getContentData(element);
		// color elements
		if (isColor(data)) {
			return false;
		}

		// bitmaps or svgs
		IImageHolder holder = getImageHolder(element);
		if (holder != null) {
			if (getProgram(holder.getImageFile()) != null) {
				return true;
			}
		}
		final ILayer layer = getLayer(false, element);

		if (layer != null && layer.getFileName(true) != null) {
			if (null != getProgram(new File(layer.getFileName(true)))) {
				return true;
			}
		}
		// sound elements

		if (data != null) {
			IMediaFileAdapter fileAdapter = (IMediaFileAdapter) data
					.getAdapter(IMediaFileAdapter.class);
			try {
				if (null != fileAdapter
						&& !StringUtils.isEmpty(fileAdapter.getFileName(true))
						&& fileAdapter.isSound()
						&& getProgram(new File(fileAdapter.getFileName(true))) != null)
					return true;
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Method checks whether data are from "color" element.
	 * 
	 * @param data
	 * @return true if input data are from color element, otherwise false
	 */
	private boolean isColor(IContentData data) {
		if (data != null) {
			ISkinnableEntityAdapter skAdapter = (ISkinnableEntityAdapter) data
					.getAdapter(ISkinnableEntityAdapter.class);
			if (skAdapter != null
					&& !(skAdapter.isColour() || skAdapter.isColourIndication())) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Opens editor of images by layer information about this image.
	 * 
	 * @param command program to run
	 * @param layer layer to open
	 * @param element element of layer
	 * @throws IOException
	 */
	protected void openEditor(Program program, final ILayer layer,
			final Object element) throws IOException {
		IPasteTargetAdapter pasteAdapter = getPasteTargetAdapter(getContentData(element));
		if (null == pasteAdapter)
			pasteAdapter = getPasteTargetAdapter(layer);
		final IPasteTargetAdapter paster = pasteAdapter;

		RunnableWithParameter callback = new BaseRunnable() {
			public void run() {
				IContentData data = getContentData(element);
				try {
					if (layer.getParent() instanceof IAnimatedImage) {
						((IAnimatedImage) layer.getParent())
								.getAnimationFrames()[0].paste(getParameter(),
								null);
					} else {
						paster.paste(getParameter(), layer);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (data != null) {
					updateGraphicWithCommand(layer, data);
				}
			}

		};

		// returns absolute path
		String imageAbsolutePath = layer.getFileName(true);

		if (!BitmapConverter.isBMPType(new File(imageAbsolutePath))) {
			paintProcessThreads.add(FileChangeWatchThread.open3rdPartyProgram(
					program, imageAbsolutePath, layer.getParent().getId(),
					callback, true));
		} else {
			paintProcessThreads.add(FileChangeWatchThread.open3rdPartyProgram(
					program, layer.getRAWImage(), layer.getParent().getId(),
					callback, true));
		}
	}

	/**
	 * Opens sound editor by sound adapter for this action.
	 * 
	 * @param command command to run
	 * @param soundAdapter Sound adapter representing sound file.
	 * @param element Element, to open
	 */
	protected void openEditor(Program program,
			final IMediaFileAdapter soundAdapter, final Object element)
			throws IOException {
		final IContentData content = (IContentData) element;
		RunnableWithParameter callback = new BaseRunnable() {
			public void run() {
				// data = new sounds file path, perform update here
				Object oldTG = soundAdapter.getThemeGraphics();
				Object newTG = soundAdapter.getEditedThemeGraphics(oldTG,
						(String) getParameter());

				SetThemeGraphicsCommand cmd = new SetThemeGraphicsCommand(
						content, oldTG, newTG, null);
				execute(cmd, null);
			}
		};

		// get absolute path
		File soundFile = new File(soundAdapter.getFileName(true));

		paintProcessThreads.add(FileChangeWatchThread.open3rdPartyProgram(
				program, soundFile.getAbsolutePath(), content.getId(),
				callback, true));
	}

	/**
	 * Opens image editor by image holder for this action.
	 * 
	 * @param command command to run
	 * @param holder ImageHolder representing image.
	 * @param element Element, to open
	 */
	protected void openEditor(Program program, final IImageHolder holder,
			final Object element) throws IOException {
		RunnableWithParameter callback = new BaseRunnable() {
			public void run() {
				if (getParameter() != null) {
					EditPart ep = getEditPart(element);
					execute(new UndoableImageHolderActionCommand(holder,
							new Runnable() {
								public void run() {
									try {
										holder.paste(getParameter(), null);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}), ep);
				}
			};
		};

		String prefix = holder.getImageFile().getName().substring(0,
				holder.getImageFile().getName().lastIndexOf('.'));
		FileInfo info = FileChangeWatchThread.open3rdPartyEditor(null, holder
				.getRAWImage(false), prefix, callback, true);
		paintProcessThreads.add(info);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	@Override
	protected void init() {
		setId(ID);
		setText(Messages.EditInSystemEditorAction_Name);
		super.init();
	}

	/**
	 * Returns command to run external program to open selected file.
	 * 
	 * @param f file to open
	 * @return command to run to open this file (without appended file name)
	 */
	private Program getProgram(File f) {
		if (null != f && f.exists()) {
			IPath path = new Path(f.getAbsolutePath());
			String ext = path.getFileExtension();
			if (ext != null) {
				Program p = Program.findProgram(ext);
				if (p != null) {
					return p;
				}
			}
		}
		return null;
	}
}