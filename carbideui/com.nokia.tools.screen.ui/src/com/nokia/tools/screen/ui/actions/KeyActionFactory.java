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
package com.nokia.tools.screen.ui.actions;

import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchPart;

public class KeyActionFactory {
	public static final String LEFT_ID = "key.left";
	public static final String RIGHT_ID = "key.right";
	public static final String UP_ID = "key.up";
	public static final String DOWN_ID = "key.down";
	public static final String CTRL_LEFT_ID = "key.ctrl.left";
	public static final String CTRL_RIGHT_ID = "key.ctrl.right";
	public static final String CTRL_UP_ID = "key.ctrl.up";
	public static final String CTRL_DOWN_ID = "key.ctrl.down";
	public static final String CTRL_SHIFT_LEFT_ID = "key.ctrl.shift.left";
	public static final String CTRL_SHIFT_RIGHT_ID = "key.ctrl.shift.right";
	public static final String CTRL_SHIFT_UP_ID = "key.ctrl.shift.up";
	public static final String CTRL_SHIFT_DOWN_ID = "key.ctrl.shift.down";

	public static final KeyActionFactory LEFT = new KeyActionFactory(LEFT_ID,
			SWT.ARROW_LEFT);
	public static final KeyActionFactory RIGHT = new KeyActionFactory(RIGHT_ID,
			SWT.ARROW_RIGHT);
	public static final KeyActionFactory UP = new KeyActionFactory(UP_ID,
			SWT.ARROW_UP);
	public static final KeyActionFactory DOWN = new KeyActionFactory(DOWN_ID,
			SWT.ARROW_DOWN);
	public static final KeyActionFactory CTRL_LEFT = new KeyActionFactory(
			CTRL_LEFT_ID, SWT.ARROW_LEFT, SWT.CTRL);
	public static final KeyActionFactory CTRL_RIGHT = new KeyActionFactory(
			CTRL_RIGHT_ID, SWT.ARROW_RIGHT, SWT.CTRL);
	public static final KeyActionFactory CTRL_UP = new KeyActionFactory(
			CTRL_UP_ID, SWT.ARROW_UP, SWT.CTRL);
	public static final KeyActionFactory CTRL_DOWN = new KeyActionFactory(
			CTRL_DOWN_ID, SWT.ARROW_DOWN, SWT.CTRL);
	public static final KeyActionFactory CTRL_SHIFT_LEFT = new KeyActionFactory(
			CTRL_SHIFT_LEFT_ID, SWT.ARROW_LEFT, SWT.CTRL | SWT.SHIFT);
	public static final KeyActionFactory CTRL_SHIFT_RIGHT = new KeyActionFactory(
			CTRL_SHIFT_RIGHT_ID, SWT.ARROW_RIGHT, SWT.CTRL | SWT.SHIFT);
	public static final KeyActionFactory CTRL_SHIFT_UP = new KeyActionFactory(
			CTRL_SHIFT_UP_ID, SWT.ARROW_UP, SWT.CTRL | SWT.SHIFT);
	public static final KeyActionFactory CTRL_SHIFT_DOWN = new KeyActionFactory(
			CTRL_SHIFT_DOWN_ID, SWT.ARROW_DOWN, SWT.CTRL | SWT.SHIFT);

	private String id;
	private char character;
	private int keyCode;
	private int stateMask;

	private KeyActionFactory(String id, int keyCode) {
		this(id, keyCode, 0);
	}

	private KeyActionFactory(String id, int keyCode, int stateMask) {
		this(id, '\0', keyCode, stateMask);
	}

	private KeyActionFactory(String id, char character, int keyCode,
			int stateMask) {
		this.id = id;
		this.character = character;
		this.keyCode = keyCode;
		this.stateMask = stateMask;
	}

	public KeyStroke getKeyStroke() {
		return KeyStroke.getPressed(character, keyCode, stateMask);
	}

	public IAction create(IWorkbenchPart part, IKeyHandler handler) {
		return new KeyAction(id, part, keyCode, handler);
	}

	class KeyAction extends WorkbenchPartAction {
		private int keyCode;
		private IKeyHandler handler;

		public KeyAction(String id, IWorkbenchPart part, int keyCode,
				IKeyHandler handler) {
			super(part);
			setId(id);
			this.keyCode = keyCode;
			this.handler = handler;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
		 */
		@Override
		protected boolean calculateEnabled() {
			return handler.calculateEnabled();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			handler.run(keyCode);
		}
	}
}
