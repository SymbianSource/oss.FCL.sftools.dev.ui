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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.media.utils.svg.ColorGroups;
import com.nokia.tools.s60.editor.actions.ColorGroupAction.MyMenuCreator;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.editing.BasicImageLayer;
import com.nokia.tools.theme.s60.ui.cstore.ComponentStoreFactory;

public class AdjustListAction extends AbstractAction {
	
	private final String ID = "themeList";
	private static final String CONTENT_PARAM = "\\{0\\}";
	private static final String QUOTES = "\"";
	private IWorkbenchPart part;
	
	private Menu menu;
	public AdjustListAction(IWorkbenchPart part) {
		
		super(part);
		
		this.part = part;
		setId(ID);
		setLazyEnablementCalculation(true);
		setMenuCreator(new MyMenuCreator());
		setText("Theme List");

	}
	class MyMenuCreator implements IMenuCreator{

		public Menu getMenu(Control parent) {
			return null;
		}

		/**
		 * Fill menu with other themes.
		 */
		public Menu getMenu(Menu parent) {
			
			if (menu != null){
				menu.dispose();
			}
			menu = new Menu(parent);

			
			addAction(menu);
			return menu;
		}

		/**
		 * Add actions related to exiting themes.
		 * 
		 * @param menu
		 */
		private void addAction(Menu menu) {

			IStructuredSelection sel = getSelection();
			if (sel.getFirstElement() instanceof IContentData) {
				IContentData cData = (IContentData) sel.getFirstElement();

				try {
					boolean separatorCreated = false;
					for (String name : ComponentStoreFactory.getComponentPool()
							.getThemeNames()) {
						if (ComponentStoreFactory.getComponentPool().isSkinned(
								cData.getId(), cData.getParent().getId(), name)) {
							
							SkinBySimilarAction action = new SkinBySimilarAction(
									getSelectionProvider(), null, name);
							addAction2Menu(menu, action);
						}
					}
					
				} catch (Exception e) {
					System.out.println("exception in addAction");
					S60WorkspacePlugin.error(e);
				}

			}
		}
		  public void dispose() {

			    }
		
		
		
		/**
		 * Add method for new action to given menu.
		 * @param menu
		 * @param action
		 */
		private void addAction2Menu(Menu menu, SkinBySimilarAction action) {
			ActionContributionItem item = new ActionContributionItem(action);
			item.fill(menu, -1);
		}
		
	}
	  
	@Override
	protected void doRun(Object element) {
				
	}
	@Override
	protected boolean doCalculateEnabled(Object element) {
		String[] names=ComponentStoreFactory.getComponentPool()
		.getThemeNames();
		if(null != names && names.length>0 && element instanceof ThemeData){
			ThemeData cData = (ThemeData)element; 
			for(String name : names){
				if (ComponentStoreFactory.getComponentPool().isSkinned(
						cData.getId(), cData.getParent().getId(), name)) {
					return true;
				}
				
			}
			
		}
		return false;
		
	}
}
