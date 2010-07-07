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

package com.nokia.tools.media.utils.tooltip;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.ui.tooltip.CompositeInformationControl;

public class ExtendedCompositeInformationControl extends
		CompositeInformationControl {

	protected TitlebarComposite titlebar;
	
	protected Composite composite;

	protected Color background = null;

	public ExtendedCompositeInformationControl(Shell parent, int shellStyle,
			int style) {
		this(parent, shellStyle, style, null);
	}

	public ExtendedCompositeInformationControl(Shell parentShell,
			int shellStyle, final int style, String statusFieldText) {
		super(parentShell, shellStyle, style, statusFieldText);
	}

	public void setTitleText(String text) {
		titlebar.setTitleText(text);
	}

	public void setTitleTextAlignment(int alignment) {
		titlebar.setTitleTextAlignment(alignment);
	}

	@Override
	protected Composite createComposite(Composite parent, int style) {
		titlebar = new TitlebarComposite(parent);
		GridData gd = new GridData(GridData.BEGINNING
				| GridData.FILL_HORIZONTAL);
		titlebar.setLayoutData(gd);
		
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = layout.verticalSpacing = 3;
		comp.setLayout(layout);

		gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		gd.horizontalIndent = INNER_BORDER;
		gd.verticalIndent = INNER_BORDER;
		comp.setLayoutData(gd);
		
		composite = new Composite(comp, SWT.NONE);
		return composite;
	}

		public MenuManager getTitleMenu() {
		return titlebar.getTitleMenu();
	}

	public void addTitleAction(final IAction action) {
		titlebar.addTitleAction(action);
	}
	
	public void setMoveArea(Rectangle moveArea) {
		titlebar.setMoveArea(moveArea);
	}

	public boolean isMoveable() {
		return titlebar.isMoveable();
	}

	public void setMoveable(boolean moveable) {
		titlebar.setMoveable(moveable);
	}

	public Color getBackground() {
		return background;
	}

	public void setBackground(Color background) {
		this.background = background;
		if (titlebar != null && !titlebar.isDisposed()) {
			titlebar.getParent().setBackground(background);
			titlebar.setBackground(background);
		}
		if (composite != null && !composite.isDisposed()) {
			composite.getParent().setBackground(background);
			composite.setBackground(background);
		}
	}

	public class CloseAction extends Action {
		public CloseAction() {
			super();
			setText(Messages.CloseAction_Label);
			setImageDescriptor(UtilsPlugin
					.getImageDescriptor("icons/close.gif"));
			setHoverImageDescriptor(UtilsPlugin
					.getImageDescriptor("icons/closeHover.gif"));
		}

		@Override
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					fPopupDialog.close();
				}
			});
		}

		@Override
		public boolean isEnabled() {
			return fPopupDialog != null;
		}
	}
	
	public class TooltipHelpAction extends Action {
		public TooltipHelpAction(String toolTipText) {
			super();
		
			setText(Messages.HelpAction_Label);
			setImageDescriptor(UtilsPlugin
					.getImageDescriptor("icons/help.png"));
			setHoverImageDescriptor(UtilsPlugin
					.getImageDescriptor("icons/help_hover.png"));
			setDisabledImageDescriptor(UtilsPlugin
					.getImageDescriptor("icons/help_disabled.png"));
			setToolTipText(toolTipText);
		}

		@Override
		public void run() {
		}

		@Override
		public boolean isEnabled() {
			return fPopupDialog != null && getToolTipText() != null;
		}
	}
}
