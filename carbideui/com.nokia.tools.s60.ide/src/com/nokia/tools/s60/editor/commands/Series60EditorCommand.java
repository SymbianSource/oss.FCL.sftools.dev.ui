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
package com.nokia.tools.s60.editor.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.s60.editor.ui.dialogs.IFailure;
import com.nokia.tools.s60.internal.utils.PasteFolderUtils;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;

/**
 */
public abstract class Series60EditorCommand extends Command {
	private IContent content;
	private IContentData data;
	private String id;

	/**
	 * set to true by execute(), if element is not skinned before this command
	 * executes. Important for correct undo.
	 */
	protected boolean originalSkinned;

	// used for storing generic other attributes
	private HashMap<String, Object> params;

	private boolean collectFailedCommands;

	private static List<IFailure> failedCommands = new ArrayList<IFailure>();

	public Series60EditorCommand(IContentData data, EditPart part) {
		this(data == null ? JEMUtil.getContentData(part) : data);
	}

	public Series60EditorCommand(IContentData data) {
		this.data = data;
		id = data.getId();
		content = data.getRoot();

		ISkinnableEntityAdapter adapter = getAdapter();
		if (null != adapter) {
			originalSkinned = adapter.isSkinned();
		}
	}

	public IContentData getContentData() {
		return data;
	}

	public synchronized void setParam(String name, Object o) {
		if (params == null)
			params = new HashMap<String, Object>();
		params.put(name, o);
	}

	public Object getParam(String name) {
		return params == null ? null : params.get(name);
	}

	@Override
	public void execute() {
		doExecute();
	}

	@Override
	public void undo() {
		if (!originalSkinned && getAdapter() != null) {
			try {
				getAdapter().clearThemeGraphics();
				// when clearing colour, special action is needed
				if (getAdapter().isColour()) {
					// update category colour and update widgets
					IColorAdapter ca = (IColorAdapter) getAdapter().getAdapter(
							IColorAdapter.class);
					if (ca != null) {
						// extract color and set it
						ca.getApplyColorCommand(ca.getColourFromGraphics(null),
								false).execute();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			doUndo();
		}
	}

	@Override
	public void redo() {
		doRedo();
	}

	protected abstract void doRedo();

	protected abstract void doUndo();

	protected abstract void doExecute();

	/**
	 * @return Returns the adapter.
	 */
	public ISkinnableEntityAdapter getAdapter() {
		// no need to cache adapter, get it every time from content data
		ISkinnableEntityAdapter adapter = data == null ? null
				: (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
		if (adapter == null && content != null) {
			// indication that the resource structure changed, query a new one
			data = content.findById(id);
			if (data != null) {
				adapter = (ISkinnableEntityAdapter) data
						.getAdapter(ISkinnableEntityAdapter.class);
			}
		}
		return adapter;
	}

	protected void commandFailed(Throwable cause, String message) {
		if (isCollectFailedCommands()) {
			failedCommands.add(new FailedCommand(this, cause, message));
		}
	}

	private boolean isCollectFailedCommands() {
		return collectFailedCommands;
	}

	public void setCollectFailedCommands(boolean collectFailedCommands) {
		this.collectFailedCommands = collectFailedCommands;
	}

	public static void clearFailedCommands() {
		failedCommands.clear();
	}

	public static List<IFailure> getFailedCommands() {
		return failedCommands;
	}

	public static class FailedCommand implements IFailure {
		public Series60EditorCommand command;

		public Throwable cause;

		public String message;

		FailedCommand(Series60EditorCommand command, Throwable cause,
				String message) {
			this.command = command;
			this.cause = cause;
			this.message = message;
		}

		public String getDetail() {
			ByteArrayOutputStream sw = new ByteArrayOutputStream();
			PrintStream pw = new PrintStream(sw);
			cause.printStackTrace(pw);
			return sw.toString();
		}

		public String getSource() {
			IContentData cd = command.getContentData();
			String caption = "" + PasteFolderUtils.getGroupName(cd.getParent())
					+ "\\" + cd.getName();
			return caption;
		}

		public String getMessage() {
			return message;
		}

		public ESeverity getSeverity() {
			return ESeverity.ERROR;
		}

		public Throwable getCause() {
			return cause;
		}
	}
}
