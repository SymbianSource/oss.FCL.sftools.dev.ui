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
 To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package com.nokia.tools.theme.s60.effects;

import java.awt.image.RenderedImage;
import java.util.HashMap;
import java.util.Map;

/**
 To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public interface ImageProcessor {

	/*
	 * CONSTANTS DEFINITIONS USED IN SCALEABLE ITEM DEFINITION
	 */
	public static final String ATTR_LDATA = "LayerData";

	public static final String ATTR_IMAGELAYER = "ImageLayer";

	public static final String HIGHLIGHT_TYPE = "highlight-type";

	final String SCALEABLEITEM = "SCALEABLEITEM";
	final String IID = "IID";
	final String INPUT = "INPUT";
	final String INPUTA = "INPUTA";
	final String INPUTB = "INPUTB";
	final String OUTPUT = "OUTPUT";
	final String REFER = "REF";
	final String END = "END";
	final String UID_STR = "UID";
	final String EFFECT = "EFFECT";
	final String ANIMATION = "ANIMATION";
	final String MININTERVAL = "MININTERVAL";
	final String PREPROCESS = "PREPROCESS";
	final String COMMAND = "COMMAND";
	final String VALUE = "VALUE";
	final String TIMINGMODEL = "TIMINGMODEL";
	final String TIMINGID = "TIMINGID";
	final String COMMENT = "//";
	final String NL = "\r\n";
	final String EQUAL = "=";
	final String FSLASH = "/";
	final String RGB = "RGB";
	final String RGBA = "RGBA";
	final String A = "A";
	final String NONE = "none";
	final String SPACE = " ";
	final String TAB = "\t";
	final String MORPHED = "MORPHED";
	final String NAMEDREF = "NAMEDREF";
	final String VALUEID = "VALUEID";

	public RenderedImage doProcessing(RenderedImage src, HashMap map);

	public HashMap getOutputParams(Map uiMap, Map attrMap, String type);

	public HashMap getEffectParameters(Map uiMap, Map attrMap, String type);

	// public HashMap getEffectProperties();

	/**
	 * Returns the effect string that could be written into the output text
	 * file. The system will reject the entry if the returned position is less
	 * than the currentInputPosition (the effect is not allowed to
	 * destroy/overwrite the previous layers image. Ideally,to preserve image
	 * position, the effect must write the result into the current input
	 * position itself.
	 * 
	 * @param prevLayerPosition The position where the previous image layer data
	 *            has been recorded.
	 * @param currentPosition The position at which the current layers data can
	 *            be written
	 * @param effectValues The effect values (as stored from the gui)
	 * @return effectStr The generated effect string
	 */
	public StringBuffer getEffectString(int prevLayerPosition,
			int currentPosition, java.util.Map effectValues);
}
