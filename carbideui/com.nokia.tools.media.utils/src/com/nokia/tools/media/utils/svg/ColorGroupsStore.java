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
package com.nokia.tools.media.utils.svg;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.QualifiedName;

import com.nokia.tools.media.utils.UtilsPlugin;

public class ColorGroupsStore {

	public static final boolean isEnabled = true;
	private static final QualifiedName PROP_KEY = new QualifiedName(null,
			"color.groups");

	public static ColorGroups getColorGroupsForProject(IProject project) {
		// moved color groups to the project session, to get rid of the cached
		// data when the project is deleted
		if (project == null) {
			return null;
		}
		try {
			ColorGroups themeColorGroups = (ColorGroups) project
					.getSessionProperty(PROP_KEY);
			if (themeColorGroups == null) {
				themeColorGroups = loadGroups(project);
				project.setSessionProperty(PROP_KEY, themeColorGroups);
			}

			return themeColorGroups;
		} catch (Exception e) {
			UtilsPlugin.error(e);
		}
		return null;
	}

	/**
	 * Disposes the color groups for the current project.
	 * 
	 * @param project the project that the color groups is associated with.
	 */
	public static void disposeColorGroupsForProject(IProject project) {
		try {
			// clears the session property when it's no longer in the scope,
			// e.g. when the editor closes
			project.setSessionProperty(PROP_KEY, null);
		} catch (Exception e) {
			UtilsPlugin.error(e);
		}
	}

	private static ColorGroups loadGroups(IProject project) {
		if (project != null) {
			ColorGroups newColorGroups = new ColorGroups(project.getName());

			GroupsLoader loader = new GroupsLoader(newColorGroups);
			loader.init();
			List<ColorGroup> colorGrps = loader.getColorGroups();
			if (colorGrps != null) {
				for (ColorGroup grp : colorGrps) {
					newColorGroups.addGroup(grp);
				}
			}
			return newColorGroups;
		}
		return null;
	}

	public static void saveGroups(ColorGroups grps, String filePath) {
		if (grps != null) {
			new GroupSaver().save(grps.getGroups(), filePath);
		}
	}

	public static void changeLayerNameIfPresent(IProject project,
			String itemId, String oldLayerName, String newLayerName) {
		ColorGroups grps = getColorGroupsForProject(project);
		if (grps != null) {
			for (ColorGroup grp : grps.getGroups()) {
				grp.changeLayerOrPartName(itemId, oldLayerName, newLayerName);
			}
		}
	}
}
