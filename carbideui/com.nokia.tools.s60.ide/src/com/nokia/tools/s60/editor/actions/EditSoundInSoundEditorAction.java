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

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.BaseRunnable;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.layers.IMediaFileAdapter;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.s60.editor.commands.SetThemeGraphicsCommand;
import com.nokia.tools.screen.ui.utils.FileChangeWatchThread;

public class EditSoundInSoundEditorAction extends AbstractEditAction {

	public static final String EDIT_3RD_SOUND_CONTEXT = "com.nokia.tools.s60.ide" + '.' + "edit3rdPartySound_context"; 

	public static final String ID = "EditSound"; 

	public EditSoundInSoundEditorAction(IWorkbenchPart part) {
		super(part);
	}

	public EditSoundInSoundEditorAction(ISelectionProvider provider,
			CommandStack stack) {
		super(provider, stack);
	}

	@Override
	protected void init() {
		setId(ID);
		setText(Messages.EditSoundAction_name);
		setLazyEnablementCalculation(true);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				EditSoundInSoundEditorAction.EDIT_3RD_SOUND_CONTEXT);
		super.init();
	}

	@Override
	protected boolean doCalculateEnabled(Object obj) {
		IContentData data = getContentData(obj);
		if (data != null) {
			IMediaFileAdapter fileAdapter = (IMediaFileAdapter) data
					.getAdapter(IMediaFileAdapter.class);
			try{
			if (null != fileAdapter
					&& !StringUtils.isEmpty(fileAdapter.getFileName(true))
					&& fileAdapter.isSound()
					&& new File(fileAdapter.getFileName(true)).exists())
				return true;
			}catch (NullPointerException npe){
				npe.printStackTrace();
			}
		}
		return false;
	}

	@Override
	protected void doRun(Object element) {

		final IContentData content = (IContentData) element;

		final IMediaFileAdapter soundAdapter = (IMediaFileAdapter) content
				.getAdapter(IMediaFileAdapter.class);

		try {

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

			String entityName = content.getId();

			paintProcessThreads.add(FileChangeWatchThread.open3rdPartyEditor(
					IMediaConstants.PREF_SOUND_EDITOR, soundFile
							.getAbsolutePath(), entityName, callback, true));

		} catch (Throwable e) {
			handleProcessException(e);
		}
	}

}
