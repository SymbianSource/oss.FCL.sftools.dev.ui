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
/*
 * File Name SkinFileUtils.java Description File contains the utility functions
 * for file handling 
 */

package com.nokia.tools.theme.s60.general;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Class for file handling utilities
 */
public class ThemeUtils {

	public static final String filePostFix = "_i";

	// the partsOrder cant be changed. Since the order is essential to write the
	// skin descriptor file and is
	// used in SkinTextFileWriter.java
	public static final String[] partsOrder = { "tl", "tr", "bl", "br", "t",
	    "b", "l", "r", "c"};

	public static final String[] coloursOrder = { "qsn_component_colors_cg1",
	    "qsn_component_colors_cg2", "qsn_component_colors_cg3",
	    "qsn_component_colors_cg4", "qsn_component_colors_cg5",
	    "qsn_component_colors_cg6a", "qsn_component_colors_cg6b",
	    "qsn_component_colors_cg7", "qsn_component_colors_cg8",
	    "qsn_component_colors_cg9", "qsn_component_colors_cg10",
	    "qsn_component_colors_cg11", "qsn_component_colors_cg12",
	    "qsn_component_colors_cg13", "qsn_component_colors_cg14",
	    "qsn_component_colors_cg15", "qsn_component_colors_cg16",
	    "qsn_component_colors_cg17", "qsn_component_colors_cg18",
	    "qsn_component_colors_cg19", "qsn_component_colors_cg20",
	    "qsn_component_colors_cg21", "qsn_component_colors_cg22",
	    "qsn_component_colors_cg23", "qsn_component_colors_cg24" };

	public static final String[] colourGroups = { "1", "2", "3", "4", "5",
	    "6a", "6b", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
	    "17", "18", "19", "20", "21", "22", "23", "24" };

	/**
	 * Determines the position of the part in the frame
	 * 
	 * @param id The id of the part
	 * @return The position identifier (in lowercase string)
	 */
	public static String partPosition(String id) {

		if (id == null)
			return null;
		char underscore = '_';
		int k = id.lastIndexOf(underscore);
		if (k == -1) {
			return null;
		} else {

			// for the center part , the identifier "center" may not be the
			// last token in the id. for ex : qsn_fr_popup_center_submenu

			// the check is done for _<position> or _<position>_
			// if the string ends with _<position> or
			// _<position>_ is there in the string

			// becos for the position "l" in
			// "qsn_fr_list_side_l - the position shld be returned as "l"
			// and it shouldnt pick the "_l" in "_list"

			for (int i = 0; i < partsOrder.length; i++) {

				String s1 = underscore + partsOrder[i] + underscore;
				String s2 = underscore + partsOrder[i];

				if ((id.indexOf(s1) != -1) || (id.endsWith(s2))) {

					return partsOrder[i];
				}
			}

			return null;
		}
	}

	/**
	 * Generates a prefix to be used while generating file names
	 * 
	 * @name The name of the entity
	 * @id The id of the entity
	 */
	private static String generateFilePrefix(String name, String id) {

		String filePrefix = null;

		String identity = (id != null) ? id : name;

		filePrefix = normalizeIdentity(identity);

		return filePrefix;
	}

	/**
	 * Normalizes the identifier. Replaces all the spaces with '_' character
	 * 
	 * @param identity The identifier for the entity
	 */
	private static String normalizeIdentity(String identity) {
		String outString = null;

		if (identity != null) {
			String temp = identity.trim();
			outString = temp.replace(' ', '_');
		}

		return outString;
	}

	/**
	 * Generate the file name to be used while saving.
	 * 
	 * @param name The name of the entity
	 * @param id The id of the entity
	 * @param dir The skin directory name
	 * @return file name, WITHOUT extension
	 */
	public static String generateNonUniqueFileName(String name, String id,
	    String path) {
		try {
			String prefix = generateFilePrefix(name, id);
			if (prefix.indexOf('.') != -1)
				prefix = prefix.substring(0, prefix.lastIndexOf('.'));

			return prefix;
		} catch (Exception e) {
			e.printStackTrace();
			return name;
		}
	}
}


/**
 * Implements the FilenameFilter class
 */
class PatternMatchFilter
    implements FilenameFilter {

	private String matchString;

	private boolean isDirTrue;

	private boolean filterParts = false;

	private static final String maskPostFix = "_mask";

	// this function needs to return false for all files that has
	// tl tr bl br t b l r and center. the part images are not shown
	// to the user
	String[] partNameTags = new String[] { "_side_l", "_side_r", "_side_t",
	    "_side_b", "_corner_tl", "_corner_tr", "_corner_bl", "_corner_br",
	    "_center" };

	/**
	 * Constructor Makes the filter such that directories are also returned as
	 * accepted
	 */
	public PatternMatchFilter(String subString) {
		
		matchString = subString.toLowerCase();
		isDirTrue = true;
		filterParts = true;
	}

	/**
	 * Constructor
	 * 
	 * @param subString The string to be checked in the file name
	 * @param alsoDirectory Value indicating if search should return true for
	 *            directories
	 */
	public PatternMatchFilter(String subString, boolean alsoDirectory,
	    boolean noParts) {
		matchString = subString.toLowerCase();
		isDirTrue = alsoDirectory;
		filterParts = noParts;
	}

	public boolean accept(File path, String name) {

		boolean result = false;

		int pos = (name.toLowerCase()).indexOf(matchString);
		

		// check if the mathc is found in the beginning itself
		if (pos != 0)
			pos = -1; // nullify the detection

		// Parts are not considered for display in the library (components) tab
		// So filter out the parts name if filterParts is set to true
		if ((pos != -1) && (filterParts)) {
			// Check if it is a part
			for (int i = 0; i < partNameTags.length; i++) {
				int pos1 = (name.toLowerCase()).indexOf(partNameTags[i]);
				if (pos1 != -1) { // it contains a part tag
					pos = -1; // invalidating the previous selection
					break;
				}
			}
		}

		// Check if the name has a mask word in it ...may be a mask
	
		int maskpos = (name.toLowerCase()).indexOf(maskPostFix, matchString
		    .length());

		if (pos != -1 && maskpos == -1) {
			result = true;
		} else {
			result = (isDirTrue) ? new File(path, name).isDirectory() : false;
		}
		return result;
	}
}
