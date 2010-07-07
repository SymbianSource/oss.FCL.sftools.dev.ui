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

package com.nokia.tools.carbide.ui.productsupport.perspectivehack;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.nokia.tools.carbide.ui.dialogs.CarbideAboutDialog;
import com.nokia.tools.screen.ui.actions.ExportAction;
import com.nokia.tools.screen.ui.actions.ImportAction;
import com.nokia.tools.ui.ide.MenuCustomizer;

public class CarbideMenuCustomizer extends MenuCustomizer {
	private static final String GENERAL_PREFERENCES_ID = "com.nokia.tools.s60.preferences.S60UICustomizationGeneralPreferencePage";

	private static final String EXTERNALTOOLS_PREFERENCES_ID = "com.nokia.tools.s60.preferences.ExternalToolsPreferencePage";

	private static final String THIRDPARTYICONS_PREFERENCES_ID = "com.nokia.tools.theme.s60.ui.preferences.ThirdPartyIconsPrefPage";

	private static final String PLUGINHANDLING_PREFERENCES_ID = "com.nokia.tools.s60.preferences.PluginHandlingPreferencePage";

	private static final String COMPONENTSTORE_PREFERENCES_ID = "com.nokia.tools.s60.preferences.ComponentStorePrefPage";

	private static final String PATHHANDLING_PREFERENCES_ID = "com.nokia.tools.screen.ui.preferences.PathHandlingPreferencePage";
	
	private static final String STARTUPTIPS_PREFERENCES_ID = "com.nokia.tools.startuptip.preferencePage";
	
	private static final String EXAMPLETHEMES_PREFERENCES_ID = "com.nokia.tools.s60.preferences.ExampleThemesPreferencePage";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.carbide.ui.productsupport.perspectivehack.MenuCustomizer#customizeMenu(org.eclipse.jface.action.MenuManager)
	 */
	@Override
	protected void customizeMenu(MenuManager mgr) {
		IWorkbenchWindow window = PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow();
		if (MENU_WINDOW.equals(mgr.getId())) {
			// Preference -filter
			replaceAction(mgr, MENU_WINDOW_PREFERENCES, new Runnable() {
				public void run() {
					PreferenceDialog prefdlg = PreferencesUtil
					    .createPreferenceDialogOn(Display.getCurrent()
					        .getActiveShell(), GENERAL_PREFERENCES_ID,
					        new String[] { GENERAL_PREFERENCES_ID,
					            EXTERNALTOOLS_PREFERENCES_ID,
					            THIRDPARTYICONS_PREFERENCES_ID,
					            PLUGINHANDLING_PREFERENCES_ID,
					            COMPONENTSTORE_PREFERENCES_ID,
					            PATHHANDLING_PREFERENCES_ID,
					            STARTUPTIPS_PREFERENCES_ID,
					            EXAMPLETHEMES_PREFERENCES_ID
					        /*
							 * UPDATE_INSTALL_PREFERENCES_ID,
							 * AUTOMATIC_UPDATES_PREFERENCES_ID
							 */
					        }, null);
					PreferenceManager pManager = prefdlg.getPreferenceManager();
					pManager.remove(PREFERENCES_ANT);
					pManager.remove(PREFERENCES_JAVA);
					pManager.remove(PREFERENCES_RUN_DEBUG);
					pManager.remove(PREFERENCES_TEAM);
					prefdlg.open();
				}
			});

			// // Remove Other -command under Show View in Window-menu
			// for (IContributionItem item2 : mgr.getItems()) {
			// if (SHOW_VIEW_ID.equals(item2.getId())) {
			// if (item2 instanceof MenuManager) {
			// MenuManager mgr2 = (MenuManager) item2;
			// mgr2.getMenu().addMenuListener(
			// new MenuAdapter() {
			//
			// /*
			// * (non-Javadoc)
			// *
			// * @see
			// org.eclipse.swt.events.MenuAdapter#menuShown(org.eclipse.swt.events.MenuEvent)
			// */
			// @Override
			// public void menuShown(MenuEvent e) {
			// Menu menu = ((Menu) e.widget);
			// // removes show other and
			// // separator
			// MenuItem[] items = menu
			// .getItems();
			// if (items.length > 2) {
			// items[items.length - 1]
			// .dispose();
			// items[items.length - 2]
			// .dispose();
			// }
			// }
			//
			// });
			// }
			// }
			// }

		} else if (MENU_FILE.equals(mgr.getId())) {
			removePropertiesMenuItem(mgr);
			// Import replacement
			IContributionItem contrib = mgr.find(MENU_FILE_IMPORT);
			if (contrib instanceof ActionContributionItem) {
				mgr.replaceItem(MENU_FILE_IMPORT, new ActionContributionItem(
				    new ImportAction(window)));
			}

			// Export replacement
			contrib = mgr.find(MENU_FILE_EXPORT);
			if (contrib instanceof ActionContributionItem) {
				mgr.replaceItem(MENU_FILE_EXPORT, new ActionContributionItem(
				    new ExportAction(window)));
			}

		} else if (MENU_HELP.equals(mgr.getId())) {
			replaceAction(mgr, MENU_HELP_ABOUT, new Runnable() {
				public void run() {
					new CarbideAboutDialog(Display.getCurrent()
					    .getActiveShell()).open();
				}
			});
		}
	}
}
