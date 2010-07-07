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
package com.nokia.tools.screen.ui;

public interface IScreenConstants {
	String PLUGIN_ID = "com.nokia.tools.screen.ui";

	String PACKAGING_DIR_TEMP = "temp";
	String PACKAGING_DIR_PROJECT = "project";
	String PACKAGING_DIR_NAME = "packagingOutput";

	/**
	 * Working directory used for packaging process, the following values are
	 * allowed: temp or project. By default, the temp value is used. <ul> <li><code>temp</code> -
	 * {java.io.tempDir}/{projectName}_packagingOutput</li> <li><code>project</code> -
	 * {workspace}/{projectName}/packagingOutput</li> </ul>
	 */
	String PREF_PACKAGING_DIR = PLUGIN_ID + ".packaging.dir";

	/**
	 * Default deployment tool
	 */
	String PREF_DEPLOYMENT_TOOL = PLUGIN_ID + ".deployment.tool";

	/**
	 * Launch automatically deployment tool or not?
	 */
	String PREF_LAUCNH_DEPLOYMENT_TOOL = PLUGIN_ID + ".deployment.launch";

	/**
	 * ask to save before packaging or not?
	 */
	String PREF_SAVE_BEFORE_PACKAGING_ASK = PLUGIN_ID + ".packaging.save.ask";

	/**
	 * Boolean flag indicating whether the same zooming factor shall be applied
	 * to all preview screens.
	 */
	String PREF_ZOOMING_GLOBAL = PLUGIN_ID + ".zooming.global";

	/**
	 * Global zooming factor.
	 */
	String PREF_ZOOMING_FACTOR = PLUGIN_ID + ".zooming.factor";

	/**
	 * boolean flag that defines whether to put content
	 * 
	 */
	String PREF_SHOULD_DEPLOY_TO_EMULATOR = PLUGIN_ID + ".deploy.to.emulator";

	/**
	 * Plays animation automatically when the screen is brought to front
	 */
	String PREF_AUTO_ANIMATION_DISABLED = PLUGIN_ID
			+ ".auto.animation.disabled";

	/**
	 * Duration for automatic animation
	 */
	String PREF_AUTO_ANIMATION_DURATION = PLUGIN_ID
			+ ".auto.animation.duration";

	/**
	 * Duration for automatic animation
	 */
	String PREF_MAX_ANIMATION_DURATION = PLUGIN_ID + ".max.animation.duration";

	/**
	 * Recent ratio for columns in theme resource page in Select Image -dialog
	 */
	String PREF_LAST_COLUMNRATIO_THEME_RESOURCE_PAGE = PLUGIN_ID
	+ ".last.columnratio.theme.resource.page";


	/**
	 * Filter history count in Select Image -dialog
	 */
	String PREF_FILTER_HISTORY_COUNT = PLUGIN_ID + ".filter.history.count";

	/**
	 * Filter history in Select Image -dialog
	 */
	String PREF_ADD_FILTER_HISTORY = PLUGIN_ID + ".add.filter.history";

	/**
	 * Tells whether to enable packaging optimization
	 */
	String PREF_PACKAGING_OPTIMIZATION_ENABLED = PLUGIN_ID
			+ ".packaging.optimization.enabled";
}
