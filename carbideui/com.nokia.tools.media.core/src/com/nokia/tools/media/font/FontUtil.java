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
package com.nokia.tools.media.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;

import com.nokia.tools.media.core.Activator;
import com.nokia.tools.media.font.IFontDescriptor.Type;
import com.nokia.tools.resource.util.FileUtils;

/**
 * This class provides font related utilities. The default fonts are read from
 * the <code>font.properties</font> file.
 * 
 */
public class FontUtil {
	private static final Map<String, Font> TRUETYPE_FONTS = new HashMap<String, Font>();
	private static final Map<String, NokiaBitmapFont> BITMAP_FONTS = new HashMap<String, NokiaBitmapFont>();
	private static final Map<String, Font> SIMPLE_NAME_MAP = new HashMap<String, Font>();

	private static boolean isInitialized;
	private static boolean isSwtInitialized;

	/**
	 * No instantiation.
	 */
	private FontUtil() {
	}

	private synchronized static void init() {
		if (!isInitialized) {
			IFontDescriptor[] descriptors = FontExtensionManager
					.getFontDescriptors();
			for (IFontDescriptor desc : descriptors) {
				if (desc.getType() == Type.TRUETYPE) {
					registerTruetypeFont(desc);
				} else if (desc.getType() == Type.BITMAP) {
					registerBitmapFont(desc);
				} else {
					Activator.error("Unknown font type: " + desc.getId());
				}
			}
			isInitialized = true;
		}
	}

	private static void registerTruetypeFont(final IFontDescriptor desc) {
		InputStream in = null;
		try {
			in = desc.getPath().openStream();
			Font font = Font.createFont(Font.TRUETYPE_FONT, in);
			// font on mac has only postscript name, here we quickfix this
			SIMPLE_NAME_MAP.put(strip(font.getName()), font);
			TRUETYPE_FONTS.put(font.getName(), font);
		} catch (Exception e) {
			Activator.error(e);
		} finally {
			FileUtils.close(in);
		}
	}

	private static void registerBitmapFont(IFontDescriptor desc) {
		try {
			NokiaBitmapFont font = new NokiaBitmapFont(desc);
			BITMAP_FONTS.put(desc.getId(), font);
		} catch (Exception e) {
			Activator.error(e);
		}
	}

	private static String strip(String value) {
		if (value == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (char c : value.toCharArray()) {
			if (Character.isLetterOrDigit(c)) {
				sb.append(Character.toLowerCase(c));
			}
		}
		return sb.toString();
	}

	/**
	 * Finds font by name, style and size combination.
	 * 
	 * @param name font name.
	 * @param style font style.
	 * @param size font size.
	 * @return the matching font if exists, otherwise the default font is
	 *         returned.
	 */
	public static IFontResource getFontResource(String name, int style, int size) {
		return getFontResource(name, style, size, false);
	}

	/**
	 * Finds font by name, style and size combination.
	 * 
	 * @param name font name.
	 * @param style font style.
	 * @param size font size.
	 * @return the matching font if exists, otherwise the default font is
	 *         returned.
	 */
	public static IFontResource getFontResource(String name, int style,
			int size, boolean trueTypeOnly) {
		init();

		Font font = TRUETYPE_FONTS.get(name);
		if (font == null) {
			font = SIMPLE_NAME_MAP.get(strip(name));
		}
		if (font != null) {
			return new AWTFont(font.deriveFont(style, size));
		}
		if (!trueTypeOnly) {
			NokiaBitmapFont bitmap = BITMAP_FONTS.get(name);
			if (bitmap != null) {
				return bitmap;
			}
		}

		return new AWTFont(new Font(name, style, size));
	}

	public static IFontResource getAWTFontResource(String name, int style,
			int size) {
		return getFontResource(name, style, size, true);
	}

	/**
	 * Tests if the font with the given name is available. Note: this only
	 * checks the fonts defined in the <code>font.properties</code>, system
	 * fonts are not in this scope.
	 * 
	 * @param name name of the font.
	 * @return true if the font is available, false otherwise.
	 */
	public static boolean isFontAvailable(String name) {
		init();

		return TRUETYPE_FONTS.containsKey(name)
				|| BITMAP_FONTS.containsKey(name)
				|| SIMPLE_NAME_MAP.containsKey(strip(name));
	}

	public static IFontResource getFontResource(Font font) {
		return new AWTFont(font);
	}

	/**
	 * Initializes the swt fonts.
	 */
	public synchronized static void initSwtFonts() {
		if (!isSwtInitialized) {
			IFontDescriptor[] descriptors = FontExtensionManager
					.getFontDescriptors();
			for (final IFontDescriptor desc : descriptors) {
				if (desc.getType() == Type.TRUETYPE) {
					// sync exec to make sure the clients gets the fonts
					// registered immediately
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							String path = FileUtils.getFile(desc.getPath())
									.getAbsolutePath();
							boolean loaded = Display.getDefault()
									.loadFont(path);
							if (!loaded) {
								Activator.warn("Unable to load font into SWT: "
										+ path);
							}
						}
					});
				}
			}
			isSwtInitialized = true;
		}
	}
}
