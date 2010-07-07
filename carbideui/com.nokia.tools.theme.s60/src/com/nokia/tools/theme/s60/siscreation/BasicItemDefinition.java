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
 * ======================================================================================
 * The file provides basic writing functionality
 * ======================================================================================
 */

package com.nokia.tools.theme.s60.siscreation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;

/**
 * The file provides basic writing functionalities.
 */
class BasicItemDefinition {

	// Data memebers
	StringBuffer errors = new StringBuffer();
	List processedFiles = new ArrayList();

	final static String SVG_EXT = ".svg";
	final static String BMP_EXT = ".bmp";
	final static String SOFTMASK = "SOFTMASK";
	final static String HARDMASK = "MASK";

	final static String SPACE = " ";
	final static String TAB = "\t";
	final static String COMMENT = "//";
	final static String NL = "\r\n";
	final static String EQUAL = "=";
	final static String FSLASH = "/";
	final static String END = "END";
	final static String UID = "UID";
	final static String IID = "IID";
	final static String STRING = "STRING";
	
	private static final String COLOR_DEPTH_PROPERTY = "colourdepth";

	final Map properties;

	/**
	 * Default constructor
	 * 
	 * @param properties
	 */
	protected BasicItemDefinition(Map properties) throws ThemeException {
		this.properties = properties;
	}

	/**
	 * Resets the internal data structures (As good as creating a new class and
	 * starting over)
	 */
	protected void reset() {
		errors.delete(0, errors.length());
	}

	/**
	 * Returns the errors found during the last operation.
	 */
	protected StringBuffer getErrors() {
		return errors;
	}

	/**
	 * Returns the errors found during the last operation.
	 */
	protected String getErrorString() {
		if (errors != null && errors.length() >= 0)
			return errors.toString();

		return null;
	}

	/**
	 * Fetches the compliance level for skin creation. If the compliance level
	 * is not set or set to SCALEABLE_SKIN - the generated skin text will be of
	 * scaleable skin format. set to BITMAP_SKIN - the generated skin text will
	 * be on the non-scaleable skin format.
	 * 
	 * @param complianceLevel
	 */
	protected SkinCompliance getComplianceLevel() {
		SkinCompliance compl = (SkinCompliance) properties
				.get(ThemeTag.KEY_SKIN_COMPLIANCE);
		if (compl == null) {
			compl = SkinCompliance.SCALEABLE_SKIN;
		}

		return compl;
	}

	/**
	 * Generates the id from the design file. The function removes all the
	 * underscores and replaces the first character after the underscore to
	 * upper case letter.
	 * 
	 * @param str The input string to be processed
	 * @return The generated id
	 */
	protected static String makeItemId(String str) {

		if (str == null)
			return null;

		int dot = str.indexOf('.');
		if (dot != -1) {
			str = str.substring(0, dot);
		}

		
		// while processing the item id - the version prefix should not be
		// packed (meaning the underscores
		// must not be removed from the version prefix string)
		// the id may be prefixed with version prefix delimited by % symbol (so
		// the version prefixed id
		// will look like <version_prefix>%<id> -- eg BITMAP_SKIN%qsn_fr_list

		str.trim();
		String versionDelimiter = null;
		int versionDelimiterPos = str.indexOf('%');
		if (versionDelimiterPos != -1) {
			versionDelimiter = str.substring(0, versionDelimiterPos + 1); 
			str = str.substring(versionDelimiterPos + 1);
		}

		String temp;
		StringBuffer newStr = new StringBuffer();

		StringTokenizer st = new StringTokenizer(str, "_");

		while (st.hasMoreTokens()) {

			int len = newStr.length();

			temp = st.nextToken();

			newStr.append(temp);
			newStr.setCharAt(len, Character.toUpperCase(temp.charAt(0)));
		}

		
		// if the versionDelimiter was processed earlier then it should be put
		// back (prepended) to the id again
		if (versionDelimiter != null) {
			newStr.insert(0, versionDelimiter);
		}

		return newStr.toString();
	}

	/**
	 * Function to copy the files
	 * 
	 * @param source The source file to be copied (relative to input/theme
	 *        directory)
	 * @param dest The expected target file (relative to output directory)
	 * @return The destination file name if successfully copied ; otherwise null
	 * @throws ThemeException
	 */
	protected String copyFile(String source, String dest) throws ThemeException  {
		String themeDir = (String) properties.get(ThemeTag.KEY_INPUT_DIR);
		if (themeDir == null)
			themeDir = ".";

		String outputDir = (String) properties.get(ThemeTag.KEY_OUTPUT_DIR);
		if (outputDir == null)
			outputDir = ".";

		File sourceFile = new File(themeDir, source);

		// source can be absolute path to nine-piece element's part
		// default graphics
		try {
			File absSource = new File(source);
			if (absSource.isAbsolute()) {
				sourceFile = absSource;
				source = absSource.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (dest != null)
			try {
				File destFile = new File(dest);
				if (destFile.isAbsolute()) {
					dest = destFile.getName();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	

		String sourceFileName = sourceFile.getName();
		String destFileName = (dest == null) ? sourceFileName : dest;
		File destinationFile = new File(outputDir, destFileName.toLowerCase());
		destFileName = destinationFile.getName();

		try {
			FileInputStream fInS = new FileInputStream(sourceFile);
			FileOutputStream fOpS = new FileOutputStream(destinationFile);

			byte data[] = new byte[1024];
			int b = -1;
			while ((b = fInS.read(data, 0, data.length)) != -1) {
				fOpS.write(data, 0, b);
			}

			fInS.close();
			fOpS.close();

		} catch (Exception e) {
			e.printStackTrace();
			throw new ThemeException(e.getMessage());
		}

		return destFileName;
	}

	/**
	 * Returns the colour depth information
	 * 
	 * @param cItem
	 * @return
	 */
	protected String getColourDepth(SkinnableEntity cItem) throws ThemeException{

		// For overriding Skinnable enity
		// Colordepth
		// with Adveced dialog Selection.
		// This is not a correct fix but only effects future releases wherein
		// skinnable entity can contain ColorDepth by themselves. Current they
		// do not contain.
		
		String colourDepth = null;
		
		ImageLayer imageLayer = getLayer0(cItem.getActualThemeGraphic());
		if(imageLayer != null){
			colourDepth = imageLayer.getAttribute(COLOR_DEPTH_PROPERTY);
		}

		if(colourDepth != null){
			return colourDepth;
		}

		colourDepth = (String) properties.get(ThemeTag.ATTR_COLOURDEPTH);
		if (colourDepth != null) {
			return colourDepth;
		}

		if (cItem != null) {
			colourDepth = cItem.getAttributeValue(ThemeTag.ATTR_COLOURDEPTH);
		}
		
		if (colourDepth == null) {
			colourDepth = "c16";
		}

		return colourDepth;
	}

	/**
	 * Returns the layer 0 information
	 * 
	 * @param tGrap
	 * @return
	 * @throws ThemeException
	 */
	protected ImageLayer getLayer0(ThemeGraphic tGrap) throws ThemeException {

		if (tGrap == null) // meaning no image is set for this icon
			return null;

		// Get the default layer that stores the image -- taking layer 0
		// directly
		// since icons cannot have layers

		List layerList = tGrap.getImageLayers();
		if (layerList == null || layerList.size() <= 0) {
			errors.append("Theme graphics has no layer data definition");
			return null;
		}

		ImageLayer layerZero = (ImageLayer) layerList.get(0);
		return layerZero;
	}

	/**
	 * Generates the mask name from the given entity name string If softmask is
	 * set to true then appends _mask_soft to the source name; otherwise appends
	 * _mask to the source name The returned string will also add .bmp extension
	 * to the generated name.
	 */
	protected static String generateMaskFileName(String source,
			boolean isSoftMask) {

		String sourceName = new File(source).getName().trim().toLowerCase();
		int dotPos = sourceName.lastIndexOf(".");
		dotPos = (dotPos == -1) ? sourceName.length() : dotPos;
		String justName = sourceName.substring(0, dotPos);

		String postFix = (isSoftMask) ? "_mask_soft" : "_mask";
		String maskName = justName + postFix + BMP_EXT;
		return maskName;
	}
}
