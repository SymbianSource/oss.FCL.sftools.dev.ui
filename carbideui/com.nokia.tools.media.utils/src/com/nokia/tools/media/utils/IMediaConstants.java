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
package com.nokia.tools.media.utils;

import org.eclipse.swt.graphics.Color;

/**
 * Constants for S60 theme studio model. Visible to surrounding plugins.
 * 
 */
public interface IMediaConstants {
	Color DEFAULT_GRID_COLOR = new Color(null, 32, 32, 32);

	Color DEFAULT_CURSOR_COLOR = new Color(null, 0, 180, 0);

	Color BACKGROUND_COLOR = new Color(null, 255, 255, 255);

	Color SELECTED_BACKGROUND_COLOR = new Color(null, 150, 150, 200);

	Color DEFAULT_GRID_FG_COLOR = new Color(null, 255, 0, 0);

	Color CENTER_LINE_COLOR = new Color(null, 0, 0, 0);

	Color NODE_LINE_COLOR = DEFAULT_GRID_FG_COLOR;

	Color BACKGROUND_COLOR2 = new Color(null, 255, 128, 128);

	Color BG_COLOR1 = new Color(null, 150, 200, 0);

	Color BG_COLOR2 = new Color(null, 220, 0, 0);

	String BackgroundLayer = "background"; //$NON-NLS-1$

	String APPLY_GRAPHICS = "ApplyGraphics"; //$NON-NLS-1$

	String APPLY_COLOR = "ApplyColor"; //$NON-NLS-1$

	String ADJUSTCHANNELS = "AdjustChannels"; //$NON-NLS-1$

	String ALPHABLENDING = "AlphaBlending"; //$NON-NLS-1$

	String CONTRAST = "Contrast"; //$NON-NLS-1$

	String CONVOLUTION = "Convolution"; //$NON-NLS-1$

	String GRAYSCALE = "Grayscale"; //$NON-NLS-1$

	String CHANNELBLENDING = "ChannelBlending"; //$NON-NLS-1$

	String INVERT = "Invert"; //$NON-NLS-1$

	String SATURATION = "Saturation"; //$NON-NLS-1$

	String SOLARIZE = "Solarize"; //$NON-NLS-1$

	String BLACKANDWHITE = "BlackAndWhite"; //$NON-NLS-1$

	String NOISE = "Noise"; //$NON-NLS-1$

	String MOVINGLAYER = "MovingLayer"; //$NON-NLS-1$

	/* Colourize & Optimize constants */

	/** stretch mode values */
	String STRETCHMODE_NORMAL = "Normal"; //$NON-NLS-1$

	String STRETCHMODE_ASPECT = "WithAspectRatio"; //$NON-NLS-1$

	String STRETCHMODE_STRETCH = "Stretch"; //$NON-NLS-1$

	String[] STRETCHMODES_VALID = { STRETCHMODE_STRETCH, STRETCHMODE_ASPECT }; //$NON-NLS-1$

	String STRETCHMODE_NORMAL_LBL = Messages.IMediaConstants_StretchNormal;

	String STRETCHMODE_ASPECT_LBL = Messages.IMediaConstants_StretchAspect;

	String STRETCHMODE_STRETCH_LBL = Messages.IMediaConstants_StretchStretch;

	/**
	 * Preferences
	 */
	String PREF_BITMAP_EDITOR = UtilsPlugin.PLUGIN_ID + ".bitmap.editor"; //$NON-NLS-1$

	String PREF_AUDIO_PLAYER = UtilsPlugin.PLUGIN_ID + ".audio.player"; //$NON-NLS-1$

	String PREF_VIDEO_PLAYER = UtilsPlugin.PLUGIN_ID + ".video.player"; //$NON-NLS-1$

	String PREF_SOUND_EDITOR = UtilsPlugin.PLUGIN_ID + ".sound.editor"; //$NON-NLS-1$

	String PREF_VECTOR_EDITOR = UtilsPlugin.PLUGIN_ID + ".vector.editor"; //$NON-NLS-1$

	String PREF_SILENT_SVG_CONVERSION = UtilsPlugin.PLUGIN_ID
			+ ".svg.silent.conversion"; //$NON-NLS-1$

	/**
	 * ask for replace or not?
	 */
	String PREF_NINE_PIECE_2SINGLE_ASK = UtilsPlugin.PLUGIN_ID
			+ ".ninePiece2Single.replace.ask"; //$NON-NLS-1$

	/**
	 * Last option selected by user
	 */
	String PREF_NINE_PIECE_2SINGLE = UtilsPlugin.PLUGIN_ID
			+ ".ninePiece2Single.replace"; //$NON-NLS-1$

	/**
	 * ask when converting to 9-piece, if slice image and set to parts
	 */
	String PREF_SINGLE_PIECE_2NINE_ASK = UtilsPlugin.PLUGIN_ID
			+ ".singlePiece2Nine.fillParts.ask"; //$NON-NLS-1$

	String NINE_PIECE_COPY_INFO = UtilsPlugin.PLUGIN_ID + ".9pieceCopy"; //$NON-NLS-1$

	/**
	 * slice image and set to parts when converting?
	 */
	String PREF_SINGLE_PIECE_2NINE = UtilsPlugin.PLUGIN_ID
			+ ".singlePiece2Nine.fillParts"; //$NON-NLS-1$

	String PREF_SVG_CONVERSION_PRESERVE_MASK = UtilsPlugin.PLUGIN_ID
			+ ".svg.conversion.preserve.mask"; //$NON-NLS-1$

	String PREF_SVG_CONVERSION_MASK2HARD = UtilsPlugin.PLUGIN_ID
			+ ".svg.conversion.mask2hard"; //$NON-NLS-1$

	String PREF_ANIMATION_CONTROL_VISIBLE = UtilsPlugin.PLUGIN_ID
			+ ".animation.control.visible"; //$NON-NLS-1$

	/**
	 * media file = non-image content of skinned element, like screensaver or
	 * sound
	 */
	String PROPERTY_MEDIA_FILE = "filepath";

	/*
	 * Handling sequence duration attribute
	 */
	String PROPERTY_DURATION = "duration";

	String PROPERTY_DURATION_MAX = "duration_max";

	String PROPERTY_DURATION_MIN = "duration_min";

}
