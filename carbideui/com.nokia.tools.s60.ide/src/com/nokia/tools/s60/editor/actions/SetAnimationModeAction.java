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

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.content.ThemeData;

/**
*/
public class SetAnimationModeAction extends AbstractAction {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.'
			+ "setanimatetime_context";

	public static final String ID = "Set_Frame_Time";

	private String animMode;

	private ThemeData data;

	private Menu menu;

	@Override
	protected void init() {
		setId(ID);
		setText("Set Animation Mode");
		setLazyEnablementCalculation(true);
		/*
		 * PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
		 * SetAnimateTimeAction.HLP_CTX);
		 */
	}

	public SetAnimationModeAction(IWorkbenchPart part) {
		super(part);
	}

	public SetAnimationModeAction(ISelectionProvider provider,
			CommandStack stack, String animMode,Menu menu) {
		super(null);
		this.animMode = animMode;
		this.menu = menu;
		setSelectionProvider(provider);
		this.stack = stack;
	}

	@Override
	public void doRun(Object sel) {
		data = (ThemeData) sel;
		try {
			execute(new Command() {
				String oldAnimateMode;

				@Override
				public boolean canExecute() {
					return true;
				}

				@Override
				public boolean canUndo() {
					return true;
				}

				@Override
				public void execute() {
					oldAnimateMode = (String) data
							.getAttribute(ThemeTag.ATTR_ANIM_MODE);
					redo();
				}

				@Override
				public void undo() {
					data.setAttribute(ThemeTag.ATTR_ANIM_MODE, oldAnimateMode);
					for(MenuItem c:menu.getItems()){						
						if(c.getText().equals(oldAnimateMode))
							c.setSelection(true);
						else
							c.setSelection(false);
					}
				}

				@Override
				public void redo() {
					data.setAttribute(ThemeTag.ATTR_ANIM_MODE, animMode);
					for(MenuItem c:menu.getItems()){						
						if(c.getText().equals(animMode))
							c.setSelection(true);
						else
							c.setSelection(false);
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.gef.commands.Command#getLabel()
				 */
				public String getLabel() {
					return getText();
				}
			}, null);

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object sel) {
		return false;
	}
}
