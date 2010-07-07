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
package com.nokia.tools.editing.ui.command;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;

public class CommandBuilder {
	private CompoundCommand command;

	public CommandBuilder() {
		this(null, true);
	}

	public CommandBuilder(boolean isForwardUndo) {
		this(null, isForwardUndo);
	}

	public CommandBuilder(String label) {
		this(label, true);
	}

	public CommandBuilder(String label, boolean isForwardUndo) {
		command = isForwardUndo ? new ForwardUndoCompoundCommand(label)
				: new CompoundCommand(label);
	}

	public void applyFeature(EObject target, EStructuralFeature feature,
			Object value) {
		ApplyFeatureCommand cmd = new ApplyFeatureCommand();
		applyFeature(cmd, target, feature, value);
	}

	public void applyFeature(ApplyFeatureCommand cmd, EObject target,
			EStructuralFeature feature, Object value) {
		cmd.setTarget(target);
		cmd.setFeature(feature);
		cmd.setValue(value);
		command.add(cmd);
	}

	/**
	 * @return the command
	 */
	public CompoundCommand getCommand() {
		List commands = command.getCommands();
		if (!commands.isEmpty()) {
			ApplyFeatureCommand significant = (ApplyFeatureCommand) commands
					.get(command instanceof ForwardUndoCompoundCommand ? commands
							.size() - 1
							: 0);
			if (command.getLabel() == null) {
				command.setLabel(significant.getLabel());
			}

			int size = commands.size();
			if (size > 1) {
				if (command instanceof ForwardUndoCompoundCommand) {
					for (int i = size - 1; i > 0; i--) {
						((ApplyFeatureCommand) commands.get(i))
								.setInCompound(true);
					}
				} else {
					for (int i = 0; i < size - 1; i++) {
						((ApplyFeatureCommand) commands.get(i))
								.setInCompound(true);
					}
				}
			}
		}
		return command;
	}
}
