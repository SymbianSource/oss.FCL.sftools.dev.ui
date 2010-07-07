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
package com.nokia.tools.screen.ui.propertysheet.color;

import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Label;

import com.nokia.tools.media.color.NamedColors;
import com.nokia.tools.media.color.ValuedColors;
import com.nokia.tools.media.utils.svg.ColorGroup;
import com.nokia.tools.ui.color.ColorDescriptor;

public class ColorChangedLabelWrapper {

	private Label colorChangedLabel;

	private ColorDescriptor colorDescriptor;

	private ColorGroup parentColorGroup = null;

	public static final int HASHED_PRESENTATION_TYPE = 0;

	public static final String HASHED_PRESENTATION_TYPE_NAME = "Hash(#rrggbb)";

	public static final int RGB_FUNCTION_PRESENTATION_TYPE = 1;

	public static final String RGB_FUNCTION_PRESENTATION_TYPE_NAME = "Function(rgb(red,green,blue))";

	public static final int NAMED_PRESENTATION_TYPE = 2;

	public static final String NAMED_PRESENTATION_TYPE_NAME = "Name(red)";

	public static final int RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE = 3;

	public static final String RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE_NAME = "Function (rgb(r%,g%,b%))";

	public static final int HASHED_PRESENTATION_SHORT_TYPE = 4;

	public static final String HASHED_PRESENTATION_SHORT_TYPE_NAME = "Short Hash(#rgb)";

	private int currentPresentationType = ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE;

	/*
	 * returns All presentation types available, note that the order in which
	 * they are returned is important
	 */
	public static String[] getPresentationTypes() {
		String[] arrayToReturn = new String[5];
		arrayToReturn[0] = ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE_NAME;
		arrayToReturn[1] = ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE_NAME;
		arrayToReturn[2] = ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE_NAME;
		arrayToReturn[3] = ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE_NAME;
		arrayToReturn[4] = ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE_NAME;
		return arrayToReturn;

	}

	public ColorDescriptor getColorDescriptor() {
		return this.colorDescriptor;
	}

	public Label getColorChangedLabel() {
		return colorChangedLabel;
	}

	public void setColorChangedLabel(Label colorChangedLabel) {
		this.colorChangedLabel = colorChangedLabel;
	}

	public void setColorDescriptor(ColorDescriptor colorDescriptor) {
		this.colorDescriptor = colorDescriptor;
	}

	public String getStringAccordingToPresentationType() {
		switch (currentPresentationType) {
		case ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE:
			return this.getHashedColorString();
		case ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE:
			return this.getFunctionStyleColorString();
		case ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE:
			return this.getColorNameColorStringWithHash();
		default:
			return this.getHashedColorString();
		}
	}

	public String getHashedColorString() {
		String red = Integer.toHexString(colorDescriptor.getRed());
		String green = Integer.toHexString(colorDescriptor.getGreen());
		String blue = Integer.toHexString(colorDescriptor.getBlue());
		if (colorDescriptor.getRed() < 16) {
			red = "0" + red;
		}
		if (colorDescriptor.getGreen() < 16) {
			green = "0" + green;
		}
		if (colorDescriptor.getBlue() < 16) {
			blue = "0" + blue;
		}

		return new String("#" + red + green + blue);
	}

	public String getFunctionStyleColorString() {
		String red = Integer.toString(colorDescriptor.getRed());
		String green = Integer.toString(colorDescriptor.getGreen());
		String blue = Integer.toString(colorDescriptor.getBlue());
		return new String("rgb(" + red + "," + green + "," + blue + ")");
	}

	public String getColorNameColorStringWithHash() {
		if (colorDescriptor.getName() != null) {
			return new String(getHashedColorString() + " ("
					+ colorDescriptor.getName() + ")");
		} else {
			return new String(getHashedColorString());
		}
	}

	public String getColorNameColorString() {
		return new String(colorDescriptor.getName());
	}

	public int getCurrentPresentationType() {
		return currentPresentationType;
	}

	public void setCurrentPresentationType(int currentPresentationType) {
		this.currentPresentationType = currentPresentationType;
	}

	public void setColorString(String colorChangingString) {
		colorChangingString = (colorChangingString.trim()); // remove leading
		// and trailing
		// spaces

		if (Pattern.matches( 
				"#([a-fA-F0-9]){6}", colorChangingString)) {
			String redString = colorChangingString.substring(1, 3);
			String greenString = colorChangingString.substring(3, 5);
			String blueString = colorChangingString.substring(5, 7);
			int red = Integer.parseInt(redString, 16);
			int green = Integer.parseInt(greenString, 16);
			int blue = Integer.parseInt(blueString, 16);
			this.colorDescriptor = new ColorDescriptor(
					new RGB(red, green, blue), null);
			this.currentPresentationType = ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE;
		} else if (Pattern.matches("#([a-fA-F0-9]){3}", colorChangingString)) {
			String redString = colorChangingString.substring(1, 2);
			String greenString = colorChangingString.substring(2, 3);
			String blueString = colorChangingString.substring(3, 4);
			redString = redString + redString; // duplicate the red hexa number
			greenString = greenString + greenString;
			blueString = blueString + blueString;
			int red = Integer.parseInt(redString, 16);
			int green = Integer.parseInt(greenString, 16);
			int blue = Integer.parseInt(blueString, 16);
			this.colorDescriptor = new ColorDescriptor(
					new RGB(red, green, blue), null);
			this.currentPresentationType = ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE;
		} else if (Pattern
				.matches(
						"[rR][gG][bB][\\s]*\\([\\s]*[0-9]{1,3}[\\s]*,[\\s]*[0-9]{1,3}[\\s]*,[\\s]*[0-9]{1,3}[\\s]*\\)",
						colorChangingString)) {
			colorChangingString = colorChangingString.substring(3); // remove
			// "rgb"
			colorChangingString = colorChangingString.trim(); // remove spaces
			colorChangingString = colorChangingString.substring(1); // remove
			// "("
			colorChangingString = colorChangingString.substring(0,
					colorChangingString.length() - 1); // "remove ")"

			String[] splittedColorString = colorChangingString.split(",");
			String redString = splittedColorString[0].trim();
			String greenString = splittedColorString[1].trim();
			String blueString = splittedColorString[2].trim();
			int red = Integer.parseInt(redString);
			int green = Integer.parseInt(greenString);
			int blue = Integer.parseInt(blueString);

			this.colorDescriptor = new ColorDescriptor(
					new RGB(red, green, blue), null);
			this.currentPresentationType = ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE;

		} else if (Pattern
				.matches(
						"[rR][gG][bB][\\s]*\\([\\s]*[0-9]{1,2}([.]*[0-9]{1}){0,1}[\\s]*%[\\s]*"
								+ ",[\\s]*[0-9]{1,2}([.]*[0-9]{1}){0,1}[\\s]*%[\\s]*"
								+ ",[\\s]*[0-9]{1,2}([.]*[0-9]{1}){0,1}[\\s]*%[\\s]*\\)",
						colorChangingString)) {

			colorChangingString = colorChangingString.substring(3); // remove
			// "rgb"
			colorChangingString = colorChangingString.trim(); // remove spaces
			colorChangingString = colorChangingString.substring(1); // remove
			// "("
			colorChangingString = colorChangingString.substring(0,
					colorChangingString.length() - 1); // "remove ")"
			colorChangingString = colorChangingString.replace('%', ' ');
			String[] splittedColorString = colorChangingString.split(",");

			String redString = splittedColorString[0].trim();
			String greenString = splittedColorString[1].trim();
			String blueString = splittedColorString[2].trim();

			float redPercentage = Float.parseFloat(redString);
			float greenPercentage = Float.parseFloat(greenString);
			float bluePercentage = Float.parseFloat(blueString);

			int red = (int) (redPercentage * 255 / 100);
			int green = (int) (greenPercentage * 255 / 100);
			int blue = (int) (bluePercentage * 255 / 100);

			this.colorDescriptor = new ColorDescriptor(
					new RGB(red, green, blue), null);
			this.currentPresentationType = ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE;

		} else if (ColorDescriptor.colorHasCssUsableName(colorChangingString)) {

			String colorValue = ValuedColors
					.getNamedColorValue(colorChangingString);
			String redString = colorValue.substring(1, 3);
			String greenString = colorValue.substring(3, 5);
			String blueString = colorValue.substring(5, 7);
			int red = Integer.parseInt(redString, 16);
			int green = Integer.parseInt(greenString, 16);
			int blue = Integer.parseInt(blueString, 16);
			this.colorDescriptor = new ColorDescriptor(
					new RGB(red, green, blue), colorChangingString);

			this.currentPresentationType = ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE;

		} else {
			this.colorDescriptor = new ColorDescriptor(ColorDescriptor
					.getRGB(ValuedColors.BLACK), NamedColors.BLACK);

		}

	}

	public String getColorNameColorStringWithFunction() {
		String red = Integer.toString(colorDescriptor.getRed());
		String green = Integer.toString(colorDescriptor.getGreen());
		String blue = Integer.toString(colorDescriptor.getBlue());
		return new String("rgb(" + red + "," + green + "," + blue + ") "
				+ colorDescriptor.getName());
	}

	public String getFunctionAndPercentageStyleColorString() {
		String red = Double.toString(Math
				.round(colorDescriptor.getRed() * 1000 / 255.0) / 10);
		if (red.endsWith(".0")) {
			red = red.substring(0, red.length() - 2);
		}
		String green = Double.toString(Math
				.round(colorDescriptor.getGreen() * 1000 / 255.0) / 10);
		if (green.endsWith(".0")) {
			green = green.substring(0, green.length() - 2);
		}
		String blue = Double.toString(Math
				.round(colorDescriptor.getBlue() * 1000 / 255.0) / 10);
		if (blue.endsWith(".0")) {
			blue = blue.substring(0, blue.length() - 2);
		}
		return new String("rgb(" + red + "%," + green + "%," + blue + "%)");
	}

	public String getCurrentPresentationStyleName() {
		switch (currentPresentationType) {
		case ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE:
			return ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE_NAME;
		case ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE:
			return ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE_NAME;
		case ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE:
			return ColorChangedLabelWrapper.NAMED_PRESENTATION_TYPE_NAME;
		case ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE:
			return ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE_NAME;
		case ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE:
			return ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE_NAME;
		default:
			return ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE_NAME;
		}
	}

	public String getColorNameColorStringWithFunctionAndPercentage() {
		String red = Double.toString(Math
				.round(colorDescriptor.getRed() * 1000 / 255.0) / 10);
		if (red.endsWith(".0")) {
			red = red.substring(0, red.length() - 2);
		}
		String green = Double.toString(Math
				.round(colorDescriptor.getGreen() * 1000 / 255.0) / 10);
		if (green.endsWith(".0")) {
			green = green.substring(0, green.length() - 2);
		}
		String blue = Double.toString(Math
				.round(colorDescriptor.getBlue() * 1000 / 255.0) / 10);
		if (blue.endsWith(".0")) {
			blue = blue.substring(0, blue.length() - 2);
		}
		return new String("rgb(" + red + "%," + green + "%," + blue + "%) "
				+ colorDescriptor.getName());

	}

	public String getShortHashedColorString() {
		String red = Integer.toHexString(colorDescriptor.getRed());
		String green = Integer.toHexString(colorDescriptor.getGreen());
		String blue = Integer.toHexString(colorDescriptor.getBlue());
		if ((red.length() == 2)) {
			red = red.substring(0, 1);
		}
		if ((green.length() == 2)) {
			green = green.substring(0, 1);
		}
		if ((blue.length() == 2)) {
			blue = blue.substring(0, 1);
		}

		return new String("#" + red + green + blue);
	}

	public String getShortHashedColorWithApproximatedColorName() {
		String red = Integer.toHexString(colorDescriptor.getRed());
		String green = Integer.toHexString(colorDescriptor.getGreen());
		String blue = Integer.toHexString(colorDescriptor.getBlue());
		if ((red.length() == 2)) {
			red = red.substring(0, 1);
		}
		if ((green.length() == 2)) {
			green = green.substring(0, 1);
		}
		if ((blue.length() == 2)) {
			blue = blue.substring(0, 1);
		}

		return new String("#" + red + green + blue + " (approximately "
				+ colorDescriptor.getName() + ")");
	}

	public static boolean isSupportedColor(String colorToResolve) {
		String colorChangingString = (colorToResolve.trim()); // remove
																// leading
		// and trailing spaces

		if (Pattern.matches( 
				"#([a-fA-F0-9]){6}", colorChangingString)) {
			return true;
		} else if (Pattern.matches("#([a-fA-F0-9]){3}", colorChangingString)) {
			return true;
		} else if (Pattern
				.matches(
						"[rR][gG][bB][\\s]*\\([\\s]*[0-9]{1,3}[\\s]*,[\\s]*[0-9]{1,3}[\\s]*,[\\s]*[0-9]{1,3}[\\s]*\\)",
						colorChangingString)) {
			return true;
		} else if (Pattern
				.matches(
						"[rR][gG][bB][\\s]*\\([\\s]*[0-9]{1,2}([.]*[0-9]{1}){0,1}[\\s]*%[\\s]*"
								+ ",[\\s]*[0-9]{1,2}([.]*[0-9]{1}){0,1}[\\s]*%[\\s]*"
								+ ",[\\s]*[0-9]{1,2}([.]*[0-9]{1}){0,1}[\\s]*%[\\s]*\\)",
						colorChangingString)) {

			return true;
		} else if (ColorDescriptor.colorHasCssUsableName(colorChangingString)) {

			return true;
		} else {
			return false;

		}
	}

	/**
	 * @return the parentColorGroup
	 */
	public ColorGroup getParentColorGroup() {
		return parentColorGroup;
	}

	/**
	 * @param parentColorGroup the parentColorGroup to set
	 */
	public void setParentColorGroup(ColorGroup parentColorGroup) {
		this.parentColorGroup = parentColorGroup;
	}
}
