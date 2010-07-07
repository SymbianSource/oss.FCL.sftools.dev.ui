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
package com.nokia.tools.s60.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.s60.editor.commands.ClearImageCommand;
import com.nokia.tools.s60.editor.commands.ProgressMonitorWrapperCommand;
import com.nokia.tools.s60.editor.commands.Series60EditorCommand;
import com.nokia.tools.s60.editor.commands.UndoableImageHolderActionCommand;
import com.nokia.tools.screen.core.ICategoryAdapter;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.utils.ISeries60Command;
import com.nokia.tools.theme.command.ApplyThemeGraphicCommand;

public class CommandInspector {

	public static String[] getAffectedElements(Command _comm) {
		// extract list if non-compound commands from command
		List<Command> effectiveCommands = new ArrayList<Command>();

		if (_comm instanceof ProgressMonitorWrapperCommand) {
			_comm = ((ProgressMonitorWrapperCommand) _comm).getCommand();
		}

		if (_comm instanceof CompoundCommand) {
			Stack<Command> stack = new Stack<Command>();
			stack.push((Command) _comm);
			while (!stack.isEmpty()) {
				Command c = stack.pop();
				if (c instanceof ProgressMonitorWrapperCommand) {
					c = ((ProgressMonitorWrapperCommand) c).getCommand();
				}
				if (c instanceof CompoundCommand) {
					for (Object x : ((CompoundCommand) c).getCommands()) {
						if (x instanceof ProgressMonitorWrapperCommand) {
							x = ((ProgressMonitorWrapperCommand) x)
									.getCommand();
						}
						if (x instanceof CompoundCommand)
							stack.push((Command) x);
						else
							effectiveCommands.add((Command) x);
					}
				}
			}
		} else {
			effectiveCommands.add(_comm);
		}

		// altered elements
		List<IContentData> altered = new ArrayList<IContentData>();

		// altered elements IDs
		List<String> alteredIDs = new ArrayList<String>();

		Helper helper = new Helper(altered, alteredIDs);

		// process all commands
		for (Command comm : effectiveCommands) {
			if (comm instanceof ProgressMonitorWrapperCommand) {
				comm = ((ProgressMonitorWrapperCommand) comm).getCommand();
			}

			if (comm instanceof ApplyFeatureCommand) {
				ApplyFeatureCommand cmd = (ApplyFeatureCommand) comm;
				IContentData data = null;
				EObject target = cmd.getTarget();
				if (target != null) {
					IScreenElement element = JEMUtil.getScreenElement(target);
					if (element != null) {
						data = element.getData();
					} else if (comm instanceof ApplyThemeGraphicCommand) {
						data = ((ApplyThemeGraphicCommand) comm).getData();
					}
				}
				helper.addAlteredElement(data);
			} else if (comm instanceof UndoableImageHolderActionCommand) {
				UndoableImageHolderActionCommand cmd = (UndoableImageHolderActionCommand) comm;
				IContentData data = null;
				IImageHolder holder = cmd.getImageHolder();
				if (holder != null && holder instanceof IAdaptable) {
					IScreenElement element = (IScreenElement) ((IAdaptable) holder)
							.getAdapter(IScreenElement.class);
					if (element != null) {
						data = element.getData();
					}
				}
				helper.addAlteredElement(data);
			} else if (comm instanceof Series60EditorCommand) {
				Series60EditorCommand cmd = (Series60EditorCommand) comm;
				IContentData data = cmd.getContentData();
				helper.addAlteredElement(data);
			} else if (comm instanceof ISeries60Command) {
				IContentData data = ((ISeries60Command) comm).getData();
				helper.addAlteredElement(data);
			} else {
				// unknown object modified
				helper.addAlteredElement(null);
			}
		} // END for (... effectiveCommands)

		return alteredIDs.toArray(new String[0]);

	}

	private static class Helper {
		private List<IContentData> altered;

		private List<String> alteredIDs;

		private Helper(List<IContentData> altered, List<String> alteredIDs) {
			this.altered = altered;
			this.alteredIDs = alteredIDs;
		}

		void addAlteredElement(IContentData data) {
			if (!altered.contains(data)) {
				altered.add(data);
				String id = data != null ? data.getId() : null;
				if (id != null && !alteredIDs.contains(id)) {
					alteredIDs.add(id);
				}
			}
			if (data != null) {
				ICategoryAdapter category = (ICategoryAdapter) data
						.getAdapter(ICategoryAdapter.class);
				IContentData[] peers = category.getCategorizedPeers();
				for (IContentData peer : peers) {
					if (!altered.contains(peer)) {
						altered.add(peer);
						String id = peer != null ? peer.getId() : null;
						if (!alteredIDs.contains(id)) {
							alteredIDs.add(id);
						}
					}
				}
			}
		}
	}
	
	public static boolean fireContentModified(Command command){
		if(command instanceof ClearImageCommand){
			return ((ClearImageCommand)command).fireImmediateContentModified();
		}
		else if(command instanceof ForwardUndoCompoundCommand){
			List<Command> commands = (List<Command>)((ForwardUndoCompoundCommand)command).getCommands();
			for(Command cmd: commands){
				if(!fireContentModified(cmd)){
					return false;
				}
			}
		}
		return true;
	}
}
