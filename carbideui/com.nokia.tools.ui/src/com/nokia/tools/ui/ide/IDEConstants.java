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


package com.nokia.tools.ui.ide;

public interface IDEConstants {
    String ACTION_SET_NAVIGATION = "org.eclipse.ui.edit.text.actionSet.navigation";
    String ACTION_SET_EXTERNAL_TOOLS = "org.eclipse.ui.externaltools.ExternalToolsSet";
    String ACTION_SET_EXTERNAL_FILE = "org.eclipse.ui.edit.text.actionSet.openExternalFile";
    String ACTION_SET_LINE_DELIMITER = "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo";
    String ACTION_SET_WEBBROWSER = "org.eclipse.wst.server.ui.internal.webbrowser.actionSet";
    String ACTION_SET_KEYBINDINGS = "org.eclipse.ui.actionSet.keyBindings";
    String ACTION_SET_NAVIGATE = "org.eclipse.ui.NavigateActionSet";
    String ACTION_SET_SEARCH = "org.eclipse.search.searchActionSet";
    String ACTION_SET_WORKINGSET = "org.eclipse.ui.WorkingSetActionSet";

    String TOOLBAR_NEW_WIZARD_DROP_DOWN = "newWizardDropDown";
    String TOOLBAR_BUILD_GROUP = "build.group";
    String TOOLBAR_BUILD = "build";
    String TOOLBAR_BUILD_EXT = "build.ext";

    String TOOLBAR_GROUP_LAUNCH = "org.eclipse.debug.ui.launchActionSet";
    String TOOLBAR_GROUP_NAVIGATE = "org.eclipse.ui.workbench.navigate";
    String TOOLBAR_GROUP_SEARCH = ACTION_SET_SEARCH;
    String TOOLBAR_GROUP_NAVIGATION = ACTION_SET_NAVIGATION;
    String TOOLBAR_GROUP_WEBBROWSER = ACTION_SET_WEBBROWSER;
    String TOOLBAR_GROUP_WORKINGSET = ACTION_SET_WORKINGSET;

    String MENU_WINDOW = "window";
    String MENU_WINDOW_NEWWINDOW = "openNewWindow";
    String MENU_WINDOW_NEWEDITOR = "newEditor";
    String MENU_WINDOW_WORKINGSET = "selectWorkingSets";
    String MENU_WINDOW_OPENPERSPECTIVE = "openPerspective";
    String MENU_WINDOW_SAVEPERSPECTIVE = "savePerspective";
    String MENU_WINDOW_CLOSEPERSPECTIVE = "closePerspective";
    String MENU_WINDOW_CLOSEALLPERPECTIVE = "closeAllPerspectives";
    String MENU_WINDOW_CUSTOMIZEPERSPECTIVE = "editActionSets";
    String MENU_WINDOW_NAVIGATION = "shortcuts";
    String MENU_WINDOW_PREFERENCES = "preferences";

    String MENU_FILE = "file";
    String MENU_FILE_NEW = "new";
    String MENU_FILE_OPEN_FILE = "org.eclipse.ui.openLocalFile";
    String MENU_FILE_IMPORT = "import";
    String MENU_FILE_EXPORT = "export";
    String MENU_FILE_REVERT = "revert";
    String MENU_FILE_MOVE = "move";
    String MENU_FILE_RENAME = "rename";
    String MENU_FILE_REFRESH = "refresh";
    String MENU_FILE_PROPERTIES = "properties";
    String MENU_FILE_CLOSE = "close";
    String MENU_FILE_CLOSE_ALL = "closeAll";

    String MENU_EDIT = "edit";
    String MENU_EDIT_CUT = "cut";
    String MENU_EDIT_SELECTALL = "selectall";
    String MENU_EDIT_FIND = "find";
    String MENU_EDIT_ADD_BOOKMARK = "bookmark";
    String MENU_EDIT_ADD_TASK = "addTask";

    String MENU_REFACTORING = "org.eclipse.jdt.ui.refactoring.menu";
    String MENU_NAVIGATE = "navigate";
    String MENU_SEARCH = "org.eclipse.search.menu";
    String MENU_PROJECT = "project";
    String MENU_ADDITIONS = "additions";
    String MENU_RUN = "org.eclipse.ui.run";

    String MENU_HELP = "help";
    String MENU_HELP_ABOUT = "about";
    String MENU_SHOW_VIEW = "showView";
    String MENU_HELP_INSTALL = "org.eclipse.equinox.p2.ui.sdk.install"; 

    String PREFERENCES_ANT = "org.eclipse.ant.ui.AntPreferencePage";
    String PREFERENCES_UPDATE_INSTALL = "org.eclipse.update.internal.ui.preferences.MainPreferencePage";
    String PREFERENCES_AUTOMATIC_UPDATES = "org.eclipse.update.scheduler.AutomaticUpdatesPreferencePage";
    String PREFERENCES_JAVA = "org.eclipse.jdt.ui.preferences.JavaBasePreferencePage";
    String PREFERENCES_RUN_DEBUG = "org.eclipse.debug.ui.DebugPreferencePage";
    String PREFERENCES_TEAM = "org.eclipse.team.ui.TeamPreferences";
}
