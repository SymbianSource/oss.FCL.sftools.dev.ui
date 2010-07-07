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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.ui.IWorkbenchPart;

public abstract class AbstractMultipleSelectionAction extends AbstractAction {

	protected List<Execution> executions;

	public AbstractMultipleSelectionAction(IWorkbenchPart part) {
		super(part);
		multipleSelectionEnablement = MultipleSelectionEnablementEnum.ALL;
		multipleSelection = true;
	}

	public AbstractMultipleSelectionAction(IWorkbenchPart part, int style) {
		super(part, style);
		multipleSelectionEnablement = MultipleSelectionEnablementEnum.ALL;
		multipleSelection = true;
	}

	protected void doRun(List<Object> element) {
		if (multipleSelection && element instanceof List) {
			executions = new ArrayList<Execution>();
			List elements = (List) element;
			for (Object object : elements) {
				if (multipleSelectionEnablement == MultipleSelectionEnablementEnum.ALL
						|| doCalculateEnabled(object)) {
					doRun(object);
				}
			}
			List<Execution> _executions = executions;
			executions = null;
			execute(_executions);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.editor.actions.AbstractAction#execute(org.eclipse.gef.commands.Command,
	 *      org.eclipse.gef.EditPart)
	 */
	@Override
	protected void execute(Command command, EditPart part) {
		if (multipleSelection) {
			if (executions == null) {
				super.execute(command, part);
			} else {
				executions.add(new Execution(command, part));
			}
		} else {
			super.execute(command, part);
		}
	}

	protected void execute(List<Execution> executions) {
		if (executions == null || executions.size() == 0) {
			return;
		}
		if (executions.size() == 1) {
			Execution ex = executions.get(0);
			super.execute(ex.cmd, ex.ep);
		} else {
			CompoundCommand cmds = new CompoundCommand(
					executions.get(0).cmd.getLabel());
			for (Execution execution : executions) {
				cmds.add(execution.cmd);
			}

			super.execute(cmds, executions.get(0).ep);
		}
	}

	protected static class Execution {
		EditPart ep;

		Command cmd;

		public Execution(Command cmd, EditPart ep) {
			this.ep = ep;
			this.cmd = cmd;
		}
	}
}
