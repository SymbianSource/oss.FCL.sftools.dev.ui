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
package com.nokia.tools.s60.editor.ui.views;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.s60.editor.actions.layers.ILayerActionsHost;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;

public class AddEffectDropDown extends Action implements
		IMenuCreator {

	private ILayerActionsHost page;

	private Menu menu;

	/**
	 * Construxts an action.
	 * 
	 * @param view
	 *            the current icon page.
	 */
	public AddEffectDropDown(ILayerActionsHost view) {
		this.page = view;
		setToolTipText(Messages.Layers_AddEffectAction_title);
		setText(Messages.Layers_AddEffectAction_title);
		setImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/etool16/add_effect16x16.gif"));
		setDisabledImageDescriptor(S60WorkspacePlugin
				.getImageDescriptor("icons/dtool16/add_effect16x16.gif"));		
		setMenuCreator(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	/*	
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		_build();
		return menu;
	}
	
	private void _build() {
		IStructuredSelection sel = (IStructuredSelection) page.getSelection();
		if (!sel.isEmpty()) {
			if (sel.getFirstElement() instanceof ILayer) {
				ILayer layer = (ILayer) sel.getFirstElement();
				List<ILayerEffect> effects = layer.getLayerEffects();
				final List<ILayerEffect> availableEffects = layer
						.getAvailableLayerEffects();
				Separator betweenLayerSep = null;
				boolean actionAdded = false;
				for (ILayerEffect a : effects) {											
						AddEffectAction action = new AddEffectAction(a, availableEffects);
						if (action.isEnabled()) {
//							separator before  ChannelBlend
							if (actionAdded)
								if (a.getName().equals(IMediaConstants.CHANNELBLENDING) || a.getName().equals(IMediaConstants.ALPHABLENDING)) {
									if (betweenLayerSep == null) {
										betweenLayerSep = new Separator();
										betweenLayerSep.fill(menu, -1);
									}
								}
							ActionContributionItem item = new ActionContributionItem(action);
							item.fill(menu, -1);
							actionAdded = true;
						}
				}
			}
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		_build();
		return menu;
	}
}
