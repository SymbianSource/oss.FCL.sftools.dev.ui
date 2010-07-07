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

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.media.core.Activator;

public class ColorUtil {
    public static final Pattern HASH_FORMAT = Pattern.compile("#([a-fA-F0-9]){6}");
    public static final Pattern HASH_FORMAT2 = Pattern.compile("0x([a-fA-F0-9]){6}");
    public static final Pattern RGB_FORMAT = Pattern
        .compile("[rR][gG][bB]\\(([0-9]{1,3}),([0-9]{1,3}),([0-9]{1,3})\\)");
    public static final Pattern RGB_PERCENTAGE_FORMAT = Pattern
        .compile("[rR][gG][bB]\\(([0-9]{1,3}\\.{0,1}[0-9]{0,1})\\%,([0-9]{1,3}\\.{0,1}[0-9]{0,1})\\%,([0-9]{1,3}\\.{0,1}[0-9]{0,1})\\%\\)");
    private static final Pattern PATTERN = Pattern
        .compile("([rR][gG][bB]\\((\\s*\\d{1,3}\\s*),(\\s*\\d{1,3}\\s*),(\\s*\\d{1,3}\\s*)\\))");

    private ColorUtil() {
    }

    public static boolean isColor(String colorString) {
        return HASH_FORMAT.matcher(colorString).matches() || RGB_FORMAT.matcher(colorString).matches()
            || RGB_PERCENTAGE_FORMAT.matcher(colorString).matches();
    }

    public static Color toColor(String colorString) {
        RGB rgb = getRGB(colorString);
        return toColor(rgb);
    }

    public static RGB toRGB(Color color) {
        if (color == null) {
            return new RGB(0, 0, 0);
        }
        return new RGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static Color toColor(RGB rgb) {
        if (rgb == null) {
            return new Color(0, 0, 0);
        }
        return new Color(rgb.red, rgb.green, rgb.blue);
    }

    /*
     * Converts color string to RGB
     */
    public static RGB getRGB(String colorString) {
        if (colorString == null) {
            return null;
        }
        if (HASH_FORMAT.matcher(colorString).matches()) {
            String redString = colorString.substring(1, 3);
            String greenString = colorString.substring(3, 5);
            String blueString = colorString.substring(5, 7);
            int red = Integer.parseInt(redString, 16);
            int green = Integer.parseInt(greenString, 16);
            int blue = Integer.parseInt(blueString, 16);
            return new RGB(red, green, blue);
        }
        if (HASH_FORMAT2.matcher(colorString).matches()) {
            String redString = colorString.substring(2, 4);
            String greenString = colorString.substring(4, 6);
            String blueString = colorString.substring(6, 8);
            int red = Integer.parseInt(redString, 16);
            int green = Integer.parseInt(greenString, 16);
            int blue = Integer.parseInt(blueString, 16);
            return new RGB(red, green, blue);
        }
        Matcher matcher = RGB_FORMAT.matcher(colorString);
        if (matcher.matches()) {
            try {
                int red = Integer.parseInt(matcher.group(1));
                int green = Integer.parseInt(matcher.group(2));
                int blue = Integer.parseInt(matcher.group(3));
                return new RGB(red, green, blue);
            } catch (Exception e) {
            }
        }
        matcher = RGB_PERCENTAGE_FORMAT.matcher(colorString);
        if (matcher.matches()) {
            try {
                int red = (int) (Double.parseDouble(matcher.group(1)) * 255 / 100);
                int green = (int) (Double.parseDouble(matcher.group(2)) * 255 / 100);
                int blue = (int) (Double.parseDouble(matcher.group(3)) * 255 / 100);
                return new RGB(red, green, blue);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static String asHashString(Color color) {
        return null != color ? asHashString(new RGB(color.getRed(), color.getGreen(), color.getBlue())) : null;
    }

    public static String asHashString(Color color, String mark) {
        return asHashString(new RGB(color.getRed(), color.getGreen(), color.getBlue()), mark);
    }

    public static String asHashString(RGB rgb) {
        return asHashString(rgb, "#");
    }

    public static String asHashString(RGB rgb, String mark) {
        StringBuilder builder = new StringBuilder(Integer.toHexString(((rgb.red & 0xff) << 16)
            | ((rgb.green & 0xff) << 8) | (rgb.blue & 0xff)));
        int padding = 6 - builder.length();
        for (int i = 0; i < padding; i++) {
            builder.insert(0, "0");
        }
        return mark + builder;
    }

    public static String asHexString(Color color) {
        return asHashString(new RGB(color.getRed(), color.getGreen(), color.getBlue()), "0x");
    }

    public static String convertRGBtoHex(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StringBuffer result = null;
        while (matcher.find()) {
            if (result == null) {
                result = new StringBuffer();
            }
            String r = matcher.group(2).trim();
            String g = matcher.group(3).trim();
            String b = matcher.group(4).trim();
            try {
                matcher.appendReplacement(result, ColorUtil.asHashString(new RGB(Integer.parseInt(r), Integer
                    .parseInt(g), Integer.parseInt(b))));
            } catch (Exception e) {
                Activator.error(e);
            }
        }
        if (result != null && result.length() > 0) {
            matcher.appendTail(result);
            return result.toString();
        }
        return text;
    }

    public static String asCommaSeparatedRGB(RGB rgb) {
        if (rgb != null) {
            return rgb.red + ", " + rgb.green + ", " + rgb.blue;
        } else {
            return "";
        }

    }

    public static org.eclipse.swt.graphics.Color asSWTColor(Display display, java.awt.Color awtColor) {
        org.eclipse.swt.graphics.Color swtColor = new org.eclipse.swt.graphics.Color(display, awtColor.getRed(),
            awtColor.getGreen(), awtColor.getBlue());
        return swtColor;
    }

    public static RGB getRGB(int rgbValue) {
        return new RGB((rgbValue >> 16) & 0xFF, (rgbValue >> 8) & 0xFF, (rgbValue >> 0) & 0xFF);
    }

    public static int getRGB(RGB color) {
        return ((color.red & 0xFF) << 16) | ((color.green & 0xFF) << 8) | ((color.blue & 0xFF) << 0);
    }
}
