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

package com.nokia.tools.s60.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip;
import com.nokia.tools.media.utils.tooltip.EditorActionTooltipActionProvider;
import com.nokia.tools.media.utils.tooltip.IDynamicTooltipUIContribution;
import com.nokia.tools.media.utils.tooltip.TitlebarComposite;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip.EStyle;
import com.nokia.tools.media.utils.tooltip.DynamicTooltip.ToolbarItemContribution;
import com.nokia.tools.ui.tooltip.CompositeTooltip;

public abstract class AbstractFloatingToolbarUIContributor implements
		IDynamicTooltipUIContribution {

	private DynamicTooltip tooltip;

	private String titleText;

	public AbstractFloatingToolbarUIContributor() {
	}

	public ToolbarItemContribution[] contributeToolbar() {
		return new ToolbarItemContribution[0];
	}

	public final void createControls(Composite parent, boolean focusState) {
		((GridLayout) parent.getLayout()).verticalSpacing = 0;
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setBackground(parent.getBackground());

		GridData gd = new GridData(GridData.FILL, GridData.BEGINNING, true,
				false);
		comp.setLayoutData(gd);

		GridLayout gl = new GridLayout(1, false);
		gl.marginWidth = gl.marginHeight = gl.verticalSpacing = 0;
		comp.setLayout(gl);

		TitlebarComposite title = new TitlebarComposite(comp);
		gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
		title.setLayoutData(gd);
		title.setMoveable(false);
		title.setTitleText(titleText);

		final Composite comp2 = new Composite(comp, SWT.NONE);
		comp2.setBackground(comp.getBackground());

		gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
		comp2.setLayoutData(gd);

		gl = new GridLayout(1, false);
		gl.marginWidth = gl.marginHeight = 0;

		boolean hide = UtilsPlugin.getDefault().getPreferenceStore()
				.getBoolean(this.getClass().getName() + ".hide");
		if (hide) {
			gd.heightHint = 0;
			comp2.setVisible(false);
		}

		comp2.setLayout(gl);

		final Action a = new Action() {
			@Override
			public void run() {
				if (comp2.getVisible()) {
					UtilsPlugin.getDefault().getPreferenceStore().setValue(
							AbstractFloatingToolbarUIContributor.this
									.getClass().getName()
									+ ".hide", true);
					setImageDescriptor(UtilsPlugin
							.getImageDescriptor("icons/arrowD.png"));
					GridData gd = ((GridData) comp2.getLayoutData());
					Point size = comp2.getSize();
					for (int i = 100; i >= 0; i -= 10) {
						int y = (int) (size.y * i / 100f);
						gd.heightHint = y;
						comp2.getShell().pack(true);
						comp2.getShell().update();
					}

					comp2.setVisible(false);
				} else {
					UtilsPlugin.getDefault().getPreferenceStore().setValue(
							AbstractFloatingToolbarUIContributor.this
									.getClass().getName()
									+ ".hide", false);
					comp2.setVisible(true);
					setImageDescriptor(UtilsPlugin
							.getImageDescriptor("icons/arrowU.png"));
					GridData gd = ((GridData) comp2.getLayoutData());
					Point size = comp2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					for (int i = 0; i <= 100; i += 10) {
						int y = (int) (size.y * i / 100f);
						gd.heightHint = y;
						comp2.getShell().pack(true);
						comp2.getShell().update();
					}
				}
			}
		};

		title.getChildren()[0].addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				a.run();
			}
		});

		a.setImageDescriptor(hide ? UtilsPlugin
				.getImageDescriptor("icons/arrowD.png") : UtilsPlugin
				.getImageDescriptor("icons/arrowU.png"));
		title.addTitleAction(a);

		doCreateControls(comp2);
	}

	protected abstract void doCreateControls(Composite parent);

	protected IAction getAction(String id) {
		return new EditorActionTooltipActionProvider().getAction(null, id);
	}

	protected boolean isVertical() {
		return EStyle.VERTICAL_VERTICAL == tooltip.getStyle();
	}

	protected ToolBarManager contributeToolBarToCoolBar(IContributionItem item) {
		return contributeToolBarToCoolBar(item, isVertical() ? SWT.VERTICAL
				: SWT.HORIZONTAL);
	}

	protected ToolBarManager contributeToolBarToCoolBar(IContributionItem item,
			int style) {
		ToolBarManager toolBar = new ToolBarManager(SWT.FLAT | style);
		toolBar.add(item);
		return toolBar;
	}

	protected ToolBarManager contributeToolBarToCoolBar(IAction action) {
		return contributeToolBarToCoolBar(new ActionContributionItem(action));
	}

	protected ToolBarManager contributeToolBarToCoolBar(IAction action,
			int style) {
		return contributeToolBarToCoolBar(new ActionContributionItem(action),
				style);
	}

	public void setContext(Object context) {
	}

	public void setSelection(Object selection) {
	}

	public void setTooltip(CompositeTooltip tooltip) {
		this.tooltip = (DynamicTooltip) tooltip;
	}

	public void setUIContainer(Object uiContainer) {
	}

	public String getTitleText() {
		return titleText;
	}

	public void setTitleText(String titletext) {
		this.titleText = titletext;
	}
}
