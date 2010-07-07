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
package com.nokia.tools.media.color;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import com.nokia.tools.media.core.Activator;
import com.nokia.tools.resource.util.StringUtils;

public class ValuedColors {

	// CSS named valued colors marked as 'final',
	// system named colors not - css colors collection is grabbed by
	// introspection based on this
	public static HashMap<String, String> BASIC_CSS_COLOR_NAME_VALUES;

	public static String[] BASIC_CSS_COLOR_NAMES;

	public static String[] CSS_COLOR_NAMES;

	public static Map<String, String> CSS_COLOR_MAP;

	public static String[] BASIC_HEXA_COLOR_VALUES;

	public static boolean isCssColorName(String name) {
		for (String a : CSS_COLOR_NAMES)
			if (a.equalsIgnoreCase(name))
				return true;
		return false;
	}

	/**
	 * It converts the hexa value of a basic color to a corresponding name
	 * 
	 * @param name: holds the hexa decimal color
	 * @return color: name or color value with "0x" replaced by "#"
	 */
	public static String convertHashToColorName(String name) {
		String colorName = (String) BASIC_CSS_COLOR_NAME_VALUES.get(name);
		if (colorName == null)
			colorName = name.toLowerCase();
		return colorName;
	}

	public static String getNamedColorValue(String nc) {
		if (StringUtils.isEmpty(nc))
			return nc;
		if (CSS_COLOR_MAP.containsKey(nc.toLowerCase()))
			return CSS_COLOR_MAP.get(nc.toLowerCase());
		else
			throw new RuntimeException("Not valid CSS color name: " + nc);
	}

	public static String getColorName(String colorHashRepresentation) {
		if (Pattern.matches("#([a-fA-F0-9]){6}", colorHashRepresentation)) {
			String redString = colorHashRepresentation.substring(1, 3);
			String greenString = colorHashRepresentation.substring(3, 5);
			String blueString = colorHashRepresentation.substring(5, 7);
			int red = Integer.parseInt(redString, 16);
			int green = Integer.parseInt(greenString, 16);
			int blue = Integer.parseInt(blueString, 16);
			return getColorName(red, green, blue);
		} else
			return null;
	}

	private static String toHexTwoDigit(int a) {
		return a < 10 ? "0" + Integer.toHexString(a) : Integer.toHexString(a);
	}

	public static String getColorName(int r, int g, int b) {
		String hash = "#" + toHexTwoDigit(r) + toHexTwoDigit(g)
				+ toHexTwoDigit(b);
		Iterator i = CSS_COLOR_MAP.keySet().iterator();
		while (i.hasNext()) {
			String key = (String) i.next();
			String val = CSS_COLOR_MAP.get(key);
			if (hash.equalsIgnoreCase(val))
				return key;
		}
		return null;
	}

	public static final String ALICE_BLUE = "#F0F8FF";

	public static final String ANTIQUE_WHITE = "#FAEBD7";

	public static final String AQUA = "#00FFFF";

	public static final String AQUAMARINE = "#7FFFD4";

	public static final String AZURE = "#F0FFFF";

	public static final String BEIGE = "#F5F5DC";

	public static final String BISQUE = "#FFE4C4";

	public static final String BLACK = "#000000";

	public static final String BLANCHEDALMOND = "#FFEBCD";

	public static final String BLUE = "#0000FF";

	public static final String BLUEVIOLET = "#8A2BE2";

	public static final String BROWN = "#A52A2A";

	public static final String BURLYWOOD = "#DEB887";

	public static final String CADETBLUE = "#5F9EA0";

	public static final String CHARTREUSE = "#7FFF00";

	public static final String CHOCOLATE = "#D2691E";

	public static final String CORAL = "#FF7F50";

	public static final String CORNFLOWERBLUE = "#6495ED";

	public static final String CORNSILK = "#FFF8DC";

	public static final String CRIMSON = "#DC143C";

	public static final String CYAN = "#00FFFF";

	public static final String DARKBLUE = "#00008B";

	public static final String DARKCYAN = "#008B8B";

	public static final String DARKGOLDENROD = "#B8860B";

	public static final String DARKGRAY = "#A9A9A9";

	public static final String DARKGREY = "#A9A9A9";

	public static final String DARKGREEN = "#006400";

	public static final String DARKKHAKI = "#BDB76B";

	public static final String DARKMAGENTA = "#8B008B";

	public static final String DARKOLIVEGREEN = "#556B2F";

	public static final String DARKORANGE = "#FF8C00";

	public static final String DARKORCHID = "#9932CC";

	public static final String DARKRED = "#8B0000";

	public static final String DARKSALMON = "#E9967A";

	public static final String DARKSEAGREEN = "#8FBC8F";

	public static final String DARKSLATEBLUE = "#483D8B";

	public static final String DARKSLATEGRAY = "#2F4F4F";

	public static final String DARKSLATEGREY = "#2F4F4F";

	public static final String DARKTURQUOISE = "#00CED1";

	public static final String DARKVIOLET = "#9400D3";

	public static final String DEEPPINK = "#FF1493";

	public static final String DEEPSKYBLUE = "#00BFFF";

	public static final String DIMGRAY = "#696969";

	public static final String DIMGREY = "#696969";

	public static final String DODGERBLUE = "#1E90FF";

	public static final String FELDSPAR = "#D19275";

	public static final String FIREBRICK = "#B22222";

	public static final String FLORALWHITE = "#FFFAF0";

	public static final String FORESTGREEN = "#228B22";

	public static final String FUCHSIA = "#FF00FF";

	public static final String GAINSBORO = "#DCDCDC";

	public static final String GHOSTWHITE = "#F8F8FF";

	public static final String GOLD = "#FFD700";

	public static final String GOLDENROD = "#DAA520";

	public static final String GRAY = "#808080";

	public static final String GREY = "#808080";

	public static final String GREEN = "#008000";

	public static final String GREENYELLOW = "#ADFF2F";

	public static final String HONEYDEW = "#F0FFF0";

	public static final String HOTPINK = "#FF69B4";

	public static final String INDIANRED = "#CD5C5C";

	public static final String INDIGO = "#4B0082";

	public static final String IVORY = "#FFFFF0";

	public static final String KHAKI = "#F0E68C";

	public static final String LAVENDER = "#E6E6FA";

	public static final String LAVENDERBLUSH = "#FFF0F5";

	public static final String LAWNGREEN = "#7CFC00";

	public static final String LEMONCHIFFON = "#FFFACD";

	public static final String LIGHTBLUE = "#ADD8E6";

	public static final String LIGHTCORAL = "#F08080";

	public static final String LIGHTCYAN = "#E0FFFF";

	public static final String LIGHTGOLDENRODYELLOW = "#FAFAD2";

	public static final String LIGHTGRAY = "#D3D3D3";

	public static final String LIGHTGREY = "#D3D3D3";

	public static final String LIGHTGREEN = "#90EE90";

	public static final String LIGHTPINK = "#FFB6C1";

	public static final String LIGHTSALMON = "#FFA07A";

	public static final String LIGHTSEAGREEN = "#20B2AA";

	public static final String LIGHTSKYBLUE = "#87CEFA";

	public static final String LIGHTSLATEBLUE = "#8470FF";

	public static final String LIGHTSLATEGRAY = "#778899";

	public static final String LIGHTSLATEGREY = "#778899";

	public static final String LIGHTSTEELBLUE = "#B0C4DE";

	public static final String LIGHTYELLOW = "#FFFFE0";

	public static final String LIME = "#00FF00";

	public static final String LIMEGREEN = "#32CD32";

	public static final String LINEN = "#FAF0E6";

	public static final String MAGENTA = "#FF00FF";

	public static final String MAROON = "#800000";

	public static final String MEDIUMAQUAMARINE = "#66CDAA";

	public static final String MEDIUMBLUE = "#0000CD";

	public static final String MEDIUMORCHID = "#BA55D3";

	public static final String MEDIUMPURPLE = "#9370D8";

	public static final String MEDIUMSEAGREEN = "#3CB371";

	public static final String MEDIUMSLATEBLUE = "#7B68EE";

	public static final String MEDIUMSPRINGGREEN = "#00FA9A";

	public static final String MEDIUMTURQUOISE = "#48D1CC";

	public static final String MEDIUMVIOLETRED = "#C71585";

	public static final String MIDNIGHTBLUE = "#191970";

	public static final String MINTCREAM = "#F5FFFA";

	public static final String MISTYROSE = "#FFE4E1";

	public static final String MOCCASIN = "#FFE4B5";

	public static final String NAVAJOWHITE = "#FFDEAD";

	public static final String NAVY = "#000080";

	public static final String OLDLACE = "#FDF5E6";

	public static final String OLIVE = "#808000";

	public static final String OLIVEDRAB = "#6B8E23";

	public static final String ORANGE = "#FFA500";

	public static final String ORANGERED = "#FF4500";

	public static final String ORCHID = "#DA70D6";

	public static final String PALEGOLDENROD = "#EEE8AA";

	public static final String PALEGREEN = "#98FB98";

	public static final String PALETURQUOISE = "#AFEEEE";

	public static final String PALEVIOLETRED = "#D87093";

	public static final String PAPAYAWHIP = "#FFEFD5";

	public static final String PEACHPUFF = "#FFDAB9";

	public static final String PERU = "#CD853F";

	public static final String PINK = "#FFC0CB";

	public static final String PLUM = "#DDA0DD";

	public static final String POWDERBLUE = "#B0E0E6";

	public static final String PURPLE = "#800080";

	public static final String RED = "#FF0000";

	public static final String ROSYBROWN = "#BC8F8F";

	public static final String ROYALBLUE = "#4169E1";

	public static final String SADDLEBROWN = "#8B4513";

	public static final String SALMON = "#FA8072";

	public static final String SANDYBROWN = "#F4A460";

	public static final String SEAGREEN = "#2E8B57";

	public static final String SEASHELL = "#FFF5EE";

	public static final String SIENNA = "#A0522D";

	public static final String SILVER = "#C0C0C0";

	public static final String SKYBLUE = "#87CEEB";

	public static final String SLATEBLUE = "#6A5ACD";

	public static final String SLATEGRAY = "#708090";

	public static final String SLATEGREY = "#708090";

	public static final String SNOW = "#FFFAFA";

	public static final String SPRINGGREEN = "#00FF7F";

	public static final String STEELBLUE = "#4682B4";

	public static final String TAN = "#D2B48C";

	public static final String TEAL = "#008080";

	public static final String THISTLE = "#D8BFD8";

	public static final String TOMATO = "#FF6347";

	public static final String TURQUOISE = "#40E0D0";

	public static final String VIOLET = "#EE82EE";

	public static final String VIOLETRED = "#D02090";

	public static final String WHEAT = "#F5DEB3";

	public static final String WHITE = "#FFFFFF";

	public static final String WHITESMOKE = "#F5F5F5";

	public static final String YELLOW = "#FFFF00";

	public static final String YELLOWGREEN = "#9ACD32";

	// named system colors

	public static String ACTIVE_BORDER_WINDOWS = "#d4d0c8";

	public static String ACTIVE_CAPTION_WINDOWS = "#8ba169";

	public static String APP_WORKSPACE_WINDOWS = "#808080";

	public static String BACKGROUND_WINDOWS = "#9dacbd";

	public static String BUTTON_FACE_WINDOWS = "#ece9d8";

	public static String BUTTON_HIGHLIGHT_WINDOWS = "#ffffff";

	public static String BUTTON_SHADOW_WINDOWS = "#aca899";

	public static String BUTTON_TEXT_WINDOWS = "#000000";

	public static String CAPTION_TEXT_WINDOWS = "#000000";

	public static String GRAY_TEXT_WINDOWS = "#aca899";

	public static String HIGHLIGHT_WINDOWS = "#93a070";

	public static String HIGHLIGHT_TEXT_WINDOWS = "#ffffff";

	public static String INACTIVE_BORDER_WINDOWS = "#d4d0c8";

	public static String INACTIVE_CAPTION_WINDOWS = "#d4d6ba";

	public static String INACTIVE_CAPTION_TEXT_WINDOWS = "#ffffff";

	public static String INFO_BACKGROUND_WINDOWS = "#ffffe1";

	public static String INFO_TEXT_WINDOWS = "#000000";

	public static String MENU_WINDOWS = "#ffffff";

	public static String MENU_TEXT_WINDOWS = "#000000";

	public static String SCROLLBAR_WINDOWS = "#d4d0c8";

	public static String THREE_D_DARK_SHADOW_WINDOWS = "#716f64";

	public static String THREE_D_FACE_WINDOWS = "#ece9d8";

	public static String THREE_D_HIGHLIGHT_WINDOWS = "#ffffff";

	public static String THREE_D_LIGHT_SHADOW_WINDOWS = "#f1efe2";

	public static String THREE_D_SHADOW_WINDOWS = "#aca899";

	public static String WINDOW_WINDOWS = "#ffffff";

	public static String WINDOW_FRAME_WINDOWS = "#000000";

	public static String WINDOW_TEXT_WINDOWS = "#000000";

	static {

		BASIC_CSS_COLOR_NAME_VALUES = new HashMap<String, String>();
		BASIC_CSS_COLOR_NAME_VALUES.put(GREEN, NamedColors.GREEN);
		BASIC_CSS_COLOR_NAME_VALUES.put(LIME, NamedColors.LIME);
		BASIC_CSS_COLOR_NAME_VALUES.put(TEAL, NamedColors.TEAL);
		BASIC_CSS_COLOR_NAME_VALUES.put(AQUA, NamedColors.AQUA);
		BASIC_CSS_COLOR_NAME_VALUES.put(NAVY, NamedColors.NAVY);
		BASIC_CSS_COLOR_NAME_VALUES.put(BLUE, NamedColors.BLUE);
		BASIC_CSS_COLOR_NAME_VALUES.put(PURPLE, NamedColors.PURPLE);
		BASIC_CSS_COLOR_NAME_VALUES.put(FUCHSIA, NamedColors.FUCHSIA);
		BASIC_CSS_COLOR_NAME_VALUES.put(MAROON, NamedColors.MAROON);
		BASIC_CSS_COLOR_NAME_VALUES.put(RED, NamedColors.RED);
		BASIC_CSS_COLOR_NAME_VALUES.put(OLIVE, NamedColors.OLIVE);
		BASIC_CSS_COLOR_NAME_VALUES.put(YELLOW, NamedColors.YELLOW);
		BASIC_CSS_COLOR_NAME_VALUES.put(WHITE, NamedColors.WHITE);
		BASIC_CSS_COLOR_NAME_VALUES.put(SILVER, NamedColors.SILVER);
		BASIC_CSS_COLOR_NAME_VALUES.put(GRAY, NamedColors.GRAY);
		BASIC_CSS_COLOR_NAME_VALUES.put(BLACK, NamedColors.BLACK);

		BASIC_CSS_COLOR_NAMES = new String[] { NamedColors.GREEN,
				NamedColors.LIME, NamedColors.TEAL, NamedColors.AQUA,
				NamedColors.NAVY, NamedColors.BLUE, NamedColors.PURPLE,
				NamedColors.FUCHSIA, NamedColors.MAROON, NamedColors.RED,
				NamedColors.OLIVE, NamedColors.YELLOW, NamedColors.WHITE,
				NamedColors.SILVER, NamedColors.GRAY, NamedColors.BLACK };
		Arrays.sort(BASIC_CSS_COLOR_NAMES);

		CSS_COLOR_MAP = new HashMap<String, String>();
		ArrayList<String> l = new ArrayList<String>();
		// init border style opts
		Field[] fs = ValuedColors.class.getFields();
		for (Field f : fs) {
			if (Modifier.isPublic(f.getModifiers())
					&& Modifier.isStatic(f.getModifiers())
					&& Modifier.isFinal(f.getModifiers())) {
				String name = f.getName().toLowerCase().replace("_", "");
				String value = null;
				try {
					value = f.get(null).toString();
				} catch (Exception e) {
					Activator.error(e);
				}
				CSS_COLOR_MAP.put(name, value);
				l.add(name);
			}
		}
		CSS_COLOR_NAMES = (String[]) l.toArray(new String[l.size()]);
	}

}
