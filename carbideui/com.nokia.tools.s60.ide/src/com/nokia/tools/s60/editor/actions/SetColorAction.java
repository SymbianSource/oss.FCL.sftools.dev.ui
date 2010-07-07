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

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.platform.theme.Component;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.color.CssColorDialog;
import com.nokia.tools.theme.content.ThemeData;

public class SetColorAction extends AbstractAction {

	public static final String HLP_CTX = "com.nokia.tools.s60.ide" + '.' + "set_color"; 

	public static final String ID = "SetColor"; 

	private RGB _newColor;

	public void setSelectedColor(RGB color) {
		_newColor = color;
	}

	@Override
	protected void init() {
		multipleSelection = true;
		setId(ID);
		setText(Messages.SetColorAction_name); 		
		setToolTipText(Messages.SetColorAction_tooltip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				SetColorAction.HLP_CTX);
	}

	public SetColorAction(IWorkbenchPart part) {
		super(part);
	}

	public SetColorAction(ISelectionProvider provider, CommandStack stack) {
		super(null);
		setSelectionProvider(provider);
		this.stack = stack;
	}

	public void selectionChanged(ISelection selection) {
		if (null == selection)
			this.setEnabled(false);
		this.setEnabled(calculateEnabled());
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		setWorkbenchPart(part);
		selectionChanged(selection);
	}

	@Override
	protected void doRun(Object element) {
		if (element instanceof List) {
			List l = (List) element;
			if (l.size() == 1) {
				Object o = l.get(0);
				if (o instanceof ThemeData) {
					ThemeData c = (ThemeData) o;
					ThemeBasicData data = c.getData();
					if (data instanceof Component) {
						if (hasColorChildren(c)) {
							List<IContentData> colors = Arrays.asList(c
									.getChildren());
							element = colors;
						}
					}
				}
			}
		}
		IContentData data = null;
		EditPart part = null;

		RGB newColor = _newColor;

		ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
				com.nokia.tools.theme.command.Messages.Command_ApplyColor_Label);
		// When MultipleSelection, element = List
		Iterator els = ((List) element).iterator();

		while (els.hasNext()) {

			Object obj = els.next();

			if (obj instanceof IContentData) {
				data = (IContentData) obj;
			} else if (obj instanceof EditPart) {
				IScreenElement screenElement = getScreenElement(obj);
				if (screenElement == null) {
					continue;
				}
				data = screenElement.getData();
			} else {
				
				if (obj instanceof Object[]) {
					if (((Object[]) obj)[1] instanceof IImage) {
						
						part = (EditPart) ((Object[]) obj)[2];
						IScreenElement screenElement = getScreenElement(part);
						if (screenElement != null) {
							data = screenElement.getData();
						}
					}
				}
			}

			if (data != null) {
				IColorAdapter adapter = (IColorAdapter) data
						.getAdapter(IColorAdapter.class);
				if (adapter != null) {
					Color oldColor = adapter.getColor();

					if (newColor == null) {
						String hashColor = ColorUtil.asHashString(oldColor);

						CssColorDialog dialog = new CssColorDialog(PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell());
						dialog.setRGBString(hashColor);

						if (dialog.open() == CssColorDialog.CANCEL) {
							return;
						}

						newColor = dialog.getRGB();
					}
					command.add(adapter.getApplyColorCommand(ColorUtil
							.toColor(newColor), true));
				}
			}
		}
		if (!command.isEmpty()) {
			execute(command, part);
		}
	}

	@Override
	protected boolean doCalculateEnabled(Object element) {
		IContentData data = null;

		if (element instanceof IContentData) {
			data = (IContentData) element;
		} else if (element instanceof EditPart) {

			EditPart part = (EditPart) element;

			final IScreenElement screenElement = getScreenElement(part);

			if (screenElement == null) {
				return false;
			}

			ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) getAdapter(
					part, ISkinnableEntityAdapter.class);

			if (adapter == null) {
				return false;
			}
			data = screenElement.getData();
		}

		if (data != null) {
			IColorAdapter color = (IColorAdapter) data
					.getAdapter(IColorAdapter.class);
			if (color != null)
				return true;
		}
		if (data != null) {
			if (data instanceof ThemeData) {
				ThemeData c = (ThemeData) data;
				ThemeBasicData data2 = c.getData();
				if (data2 instanceof Component) {
					if (hasColorChildren(c)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean hasColorChildren(IContentData data) {
		IContentData[] children = data.getChildren();
		int count = children.length;
		if (count == 0)
			return false;
		for (int i = 0; i < children.length; i++) {
			IColorAdapter color = (IColorAdapter) children[i]
					.getAdapter(IColorAdapter.class);
			if (color != null) {
				count--;
			}
		}
		if (count == 0) {
			return true;
		}
		return false;
	}

}
