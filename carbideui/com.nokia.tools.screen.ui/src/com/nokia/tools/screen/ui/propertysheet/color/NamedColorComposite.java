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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.nokia.tools.media.color.NamedColors;
import com.nokia.tools.media.color.ValuedColors;
import com.nokia.tools.ui.color.ColorBox;
import com.nokia.tools.ui.color.ColorDescriptor;

public class NamedColorComposite extends Composite {
	static Color[] basicColors = new Color[16];

	static Color[] additionalColors = new Color[126];

	static {
		basicColors[0] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GREEN));
		basicColors[1] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIME));
		basicColors[2] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.TEAL));
		basicColors[3] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.AQUA));
		basicColors[4] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.NAVY));
		basicColors[5] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BLUE));
		basicColors[6] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PURPLE));
		basicColors[7] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.FUCHSIA));
		basicColors[8] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MAROON));
		basicColors[9] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.RED));
		basicColors[10] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.OLIVE));
		basicColors[11] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.YELLOW));
		basicColors[12] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.WHITE));
		basicColors[13] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SILVER));
		basicColors[14] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GRAY));
		basicColors[15] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BLACK));

		additionalColors[0] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKOLIVEGREEN));
		additionalColors[1] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKGREEN));
		additionalColors[2] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKSLATEGRAY));
		additionalColors[3] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SLATEGRAY));
		additionalColors[4] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKBLUE));
		additionalColors[5] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MIDNIGHTBLUE));
		additionalColors[6] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INDIGO));
		additionalColors[7] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKMAGENTA));
		additionalColors[8] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BROWN));
		additionalColors[9] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKRED));
		additionalColors[10] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SIENNA));
		additionalColors[11] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SADDLEBROWN));
		additionalColors[12] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKGOLDENROD));
		additionalColors[13] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BEIGE));
		additionalColors[14] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.HONEYDEW));
		additionalColors[15] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DIMGRAY));
		additionalColors[16] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.OLIVEDRAB));
		additionalColors[17] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.FORESTGREEN));
		additionalColors[18] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKCYAN));
		additionalColors[19] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTSLATEGRAY));
		additionalColors[20] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMBLUE));
		additionalColors[21] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKSLATEBLUE));
		additionalColors[22] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKVIOLET));
		additionalColors[23] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMVIOLETRED));
		additionalColors[24] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.INDIANRED));
		additionalColors[25] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.FIREBRICK));
		additionalColors[26] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CHOCOLATE));
		additionalColors[27] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PERU));
		additionalColors[28] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GOLDENROD));
		additionalColors[29] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTGOLDENRODYELLOW));
		additionalColors[30] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MINTCREAM));
		additionalColors[31] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKGRAY));
		additionalColors[32] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.YELLOWGREEN));
		additionalColors[33] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SEAGREEN));
		additionalColors[34] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CADETBLUE));
		additionalColors[35] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.STEELBLUE));
		additionalColors[36] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ROYALBLUE));
		additionalColors[37] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BLUEVIOLET));
		additionalColors[38] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKORCHID));
		additionalColors[39] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DEEPPINK));
		additionalColors[40] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ROSYBROWN));
		additionalColors[41] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CRIMSON));
		additionalColors[42] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKORANGE));
		additionalColors[43] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BURLYWOOD));
		additionalColors[44] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKKHAKI));
		additionalColors[45] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTYELLOW));
		additionalColors[46] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.AZURE));
		additionalColors[47] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTGRAY));
		additionalColors[48] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LAWNGREEN));
		additionalColors[49] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMSEAGREEN));
		additionalColors[50] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKSEAGREEN));
		additionalColors[51] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DEEPSKYBLUE));
		additionalColors[52] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DODGERBLUE));
		additionalColors[53] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SLATEBLUE));
		additionalColors[54] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMORCHID));
		additionalColors[55] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PALEVIOLETRED));
		additionalColors[56] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SALMON));
		additionalColors[57] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ORANGERED));
		additionalColors[58] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SANDYBROWN));
		additionalColors[59] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.TAN));
		additionalColors[60] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GOLD));
		additionalColors[61] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.IVORY));
		additionalColors[62] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GHOSTWHITE));
		additionalColors[63] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GAINSBORO));
		additionalColors[64] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CHARTREUSE));
		additionalColors[65] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIMEGREEN));
		additionalColors[66] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMAQUAMARINE));
		additionalColors[67] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKTURQUOISE));
		additionalColors[68] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CORNFLOWERBLUE));
		additionalColors[69] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMSLATEBLUE));
		additionalColors[70] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ORCHID));
		additionalColors[71] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.HOTPINK));
		additionalColors[72] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTCORAL));
		additionalColors[73] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.TOMATO));
		additionalColors[74] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ORANGE));
		additionalColors[75] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BISQUE));
		additionalColors[76] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.KHAKI));
		additionalColors[77] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CORNSILK));
		additionalColors[78] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LINEN));
		additionalColors[79] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.WHITESMOKE));
		additionalColors[80] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.GREENYELLOW));
		additionalColors[81] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKSEAGREEN));
		additionalColors[82] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.TURQUOISE));
		additionalColors[83] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMTURQUOISE));
		additionalColors[84] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SKYBLUE));
		additionalColors[85] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMPURPLE));
		additionalColors[86] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.VIOLET));
		additionalColors[87] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTPINK));
		additionalColors[88] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.DARKSALMON));
		additionalColors[89] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CORAL));
		additionalColors[90] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.NAVAJOWHITE));
		additionalColors[91] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.BLANCHEDALMOND));
		additionalColors[92] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PALEGOLDENROD));
		additionalColors[93] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.OLDLACE));
		additionalColors[94] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SEASHELL));
		additionalColors[95] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LAVENDERBLUSH));
		additionalColors[96] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PALEGREEN));
		additionalColors[97] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SPRINGGREEN));
		additionalColors[98] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.AQUAMARINE));
		additionalColors[99] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.POWDERBLUE));
		additionalColors[100] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTSKYBLUE));
		additionalColors[101] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTSTEELBLUE));
		additionalColors[102] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PLUM));
		additionalColors[103] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PINK));
		additionalColors[104] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTSALMON));
		additionalColors[105] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.WHEAT));
		additionalColors[106] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MOCCASIN));
		additionalColors[107] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ANTIQUE_WHITE));
		additionalColors[108] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LEMONCHIFFON));
		additionalColors[109] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.FLORALWHITE));
		additionalColors[110] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.SNOW));
		additionalColors[111] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.ALICE_BLUE));
		additionalColors[112] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTGREEN));
		additionalColors[113] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MEDIUMSPRINGGREEN));
		additionalColors[114] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PALETURQUOISE));
		additionalColors[115] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTCYAN));
		additionalColors[116] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LIGHTBLUE));
		additionalColors[117] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.LAVENDER));
		additionalColors[118] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.THISTLE));
		additionalColors[119] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MISTYROSE));
		additionalColors[120] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PEACHPUFF));
		additionalColors[121] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.PAPAYAWHIP));
		additionalColors[122] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.FELDSPAR));
		additionalColors[123] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.MAGENTA));
		additionalColors[124] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.VIOLETRED));
		additionalColors[125] = new Color(null, ColorDescriptor
				.getRGB(ValuedColors.CYAN));

	}

	protected int basicColorCellNumber = 16;

	protected int additionalColorCellNumber = 7 * 16 + 14;

	protected ColorBox[] basicColorBoxes;

	protected ColorBox[] additionalColorBoxes;

	protected Composite selectedColorBox = null;

	protected ColorChangedLabelWrapper colorChangedLabelWrapper;

	protected IColorPickerListener dialogClose;

	protected int colorBoxSize = 16;

	public NamedColorComposite(Composite parent, int style,
			IColorPickerListener dialogClose) {
		this(parent, style, dialogClose, 16);
	}

	public NamedColorComposite(Composite parent, int style,
			IColorPickerListener dialogClose, int colorBoxSize) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.numColumns = 16;
		layout.marginWidth = 9;
		layout.marginHeight = 9;
		layout.verticalSpacing = 1;
		layout.horizontalSpacing = 1;
		this.dialogClose = dialogClose;

		this.colorBoxSize = colorBoxSize;
		this.setLayout(layout);
		createBasicComposites();
	}

	public void createBasicComposites() {
		basicColorBoxes = new ColorBox[basicColorCellNumber];
		additionalColorBoxes = new ColorBox[additionalColorCellNumber];
		Label basicColorLabel = new Label(this, SWT.NONE);
		basicColorLabel.setText(Messages.NamedColorComposite_Label_Basic);
		GridData gd = new GridData();
		gd.horizontalSpan = 16;
		basicColorLabel.setLayoutData(gd);
		fillBasicColorsAndBorders();
		Label additionalColorLabel = new Label(this, SWT.NONE);
		additionalColorLabel
				.setText(Messages.NamedColorComposite_Label_Additional);
		gd = new GridData();
		gd.horizontalSpan = 16;
		gd.verticalIndent = 14;
		additionalColorLabel.setLayoutData(gd);

		fillAdditionalColorsAndBorders();

		selectedColorBox = basicColorBoxes[0];
		/*
		 * selectedColorBox.setFocus(); selectedColorBox.forceFocus();
		 */
		// updateAfterSelectionChange();
	}

	protected void fillAdditionalColorsAndBorders() {
		String[] additionalColorNames = new String[126];

		additionalColorNames[0] = NamedColors.DARK_OLIVE_GREEN;
		additionalColorNames[1] = NamedColors.DARK_GREEN;
		additionalColorNames[2] = NamedColors.DARK_SLATE_GRAY;
		additionalColorNames[3] = NamedColors.SLATE_GREY;
		additionalColorNames[4] = NamedColors.DARK_BLUE;
		additionalColorNames[5] = NamedColors.MIDNIGHT_BLUE;
		additionalColorNames[6] = NamedColors.INDIGO;
		additionalColorNames[7] = NamedColors.DARK_MAGENTA;
		additionalColorNames[8] = NamedColors.BROWN;
		additionalColorNames[9] = NamedColors.DARK_RED;
		additionalColorNames[10] = NamedColors.SIENNA;
		additionalColorNames[11] = NamedColors.SADDLE_BROWN;
		additionalColorNames[12] = NamedColors.DARK_GOLDEN_ROD;
		additionalColorNames[13] = NamedColors.BEIGE;
		additionalColorNames[14] = NamedColors.HONEY_DEW;
		additionalColorNames[15] = NamedColors.DARK_GRAY;
		additionalColorNames[16] = NamedColors.OLIVE_DRAB;
		additionalColorNames[17] = NamedColors.FOREST_GREEN;
		additionalColorNames[18] = NamedColors.DARK_CYAN;
		additionalColorNames[19] = NamedColors.LIGHT_SLATE_GRAY;
		additionalColorNames[20] = NamedColors.MEDIUM_BLUE;
		additionalColorNames[21] = NamedColors.DARK_SLATE_BLUE;
		additionalColorNames[22] = NamedColors.DARK_VIOLET;
		additionalColorNames[23] = NamedColors.MEDIUM_VIOLET_RED;
		additionalColorNames[24] = NamedColors.INDIAN_RED;
		additionalColorNames[25] = NamedColors.FIREBRICK;
		additionalColorNames[26] = NamedColors.CHOCOLATE;
		additionalColorNames[27] = NamedColors.PERU;
		additionalColorNames[28] = NamedColors.GOLDEN_ROD;
		additionalColorNames[29] = NamedColors.LIGHT_GOLDEN_ROD_YELLOW;
		additionalColorNames[30] = NamedColors.MINT_CREAM;
		additionalColorNames[31] = NamedColors.DARK_GRAY;
		additionalColorNames[32] = NamedColors.YELLOW_GREEN;
		additionalColorNames[33] = NamedColors.SEA_GREEN;
		additionalColorNames[34] = NamedColors.CADET_BLUE;
		additionalColorNames[35] = NamedColors.STEEL_BLUE;
		additionalColorNames[36] = NamedColors.ROYAL_BLUE;
		additionalColorNames[37] = NamedColors.BLUE_VIOLET;
		additionalColorNames[38] = NamedColors.DARK_ORCHID;
		additionalColorNames[39] = NamedColors.DEEP_PINK;
		additionalColorNames[40] = NamedColors.ROSY_BROWN;
		additionalColorNames[41] = NamedColors.CRIMSON;
		additionalColorNames[42] = NamedColors.DARKORANGE;
		additionalColorNames[43] = NamedColors.BURLY_WOOD;
		additionalColorNames[44] = NamedColors.DARK_KHAKI;
		additionalColorNames[45] = NamedColors.LIGHT_YELLOW;
		additionalColorNames[46] = NamedColors.AZURE;
		additionalColorNames[47] = NamedColors.LIGHT_GREY;
		additionalColorNames[48] = NamedColors.LAWN_GREEN;
		additionalColorNames[49] = NamedColors.MEDIUM_SEA_GREEN;
		additionalColorNames[50] = NamedColors.DARK_SEA_GREEN;
		additionalColorNames[51] = NamedColors.DEEP_SKY_BLUE;
		additionalColorNames[52] = NamedColors.DODGER_BLUE;
		additionalColorNames[53] = NamedColors.SLATE_BLUE;
		additionalColorNames[54] = NamedColors.MEDIUM_ORCHID;
		additionalColorNames[55] = NamedColors.PALE_VIOLET_RED;
		additionalColorNames[56] = NamedColors.SALMON;
		additionalColorNames[57] = NamedColors.ORANGE_RED;
		additionalColorNames[58] = NamedColors.SANDY_BROWN;
		additionalColorNames[59] = NamedColors.TAN;
		additionalColorNames[60] = NamedColors.GOLD;
		additionalColorNames[61] = NamedColors.IVORY;
		additionalColorNames[62] = NamedColors.GHOST_WHITE;
		additionalColorNames[63] = NamedColors.GAINSBORO;
		additionalColorNames[64] = NamedColors.CHARTREUSE;
		additionalColorNames[65] = NamedColors.LIMEGREEN;
		additionalColorNames[66] = NamedColors.MEDIUM_AQUAMARINE;
		additionalColorNames[67] = NamedColors.DARK_TURQUOISE;
		additionalColorNames[68] = NamedColors.CORN_FLOWER_BLUE;
		additionalColorNames[69] = NamedColors.MEDIUM_SLATE_BLUE;
		additionalColorNames[70] = NamedColors.ORCHID;
		additionalColorNames[71] = NamedColors.HOT_PINK;
		additionalColorNames[72] = NamedColors.LIGHT_CORAL;
		additionalColorNames[73] = NamedColors.TOMATO;
		additionalColorNames[74] = NamedColors.ORANGE;
		additionalColorNames[75] = NamedColors.BISQUE;
		additionalColorNames[76] = NamedColors.KHAKI;
		additionalColorNames[77] = NamedColors.CORNSILK;
		additionalColorNames[78] = NamedColors.LINEN;
		additionalColorNames[79] = NamedColors.WHITE_SMOKE;
		additionalColorNames[80] = NamedColors.GREEN_YELLOW;
		additionalColorNames[81] = NamedColors.DARK_SEA_GREEN;
		additionalColorNames[82] = NamedColors.TURQUOISE;
		additionalColorNames[83] = NamedColors.MEDIUM_TURQUOISE;
		additionalColorNames[84] = NamedColors.SKY_BLUE;
		additionalColorNames[85] = NamedColors.MEDIUM_PURPLE;
		additionalColorNames[86] = NamedColors.VIOLET;
		additionalColorNames[87] = NamedColors.LIGHT_PINK;
		additionalColorNames[88] = NamedColors.DARK_SALMON;
		additionalColorNames[89] = NamedColors.CORAL;
		additionalColorNames[90] = NamedColors.NAVAJO_WHITE;
		additionalColorNames[91] = NamedColors.BLANCHED_ALMOND;
		additionalColorNames[92] = NamedColors.PALE_GOLDEN_ROD;
		additionalColorNames[93] = NamedColors.OLD_LACE;
		additionalColorNames[94] = NamedColors.SEA_SHELL;
		additionalColorNames[95] = NamedColors.LAVENDER_BLUSH;
		additionalColorNames[96] = NamedColors.PALE_GREEN;
		additionalColorNames[97] = NamedColors.SPRING_GREEN;
		additionalColorNames[98] = NamedColors.AQUAMARINE;
		additionalColorNames[99] = NamedColors.POWDER_BLUE;
		additionalColorNames[100] = NamedColors.LIGHT_SKY_BLUE;
		additionalColorNames[101] = NamedColors.LIGHT_STEEL_BLUE;
		additionalColorNames[102] = NamedColors.PLUM;
		additionalColorNames[103] = NamedColors.PINK;
		additionalColorNames[104] = NamedColors.LIGHT_SALMON;
		additionalColorNames[105] = NamedColors.WHEAT;
		additionalColorNames[106] = NamedColors.MOCCASIN;
		additionalColorNames[107] = NamedColors.ANTIQUE_WHITE;
		additionalColorNames[108] = NamedColors.LEMON_CHIFFON;
		additionalColorNames[109] = NamedColors.FLORAL_WHITE;
		additionalColorNames[110] = NamedColors.SNOW;
		additionalColorNames[111] = NamedColors.ALICE_BLUE;
		additionalColorNames[112] = NamedColors.LIGHT_GREEN;
		additionalColorNames[113] = NamedColors.MEDIUM_SPRING_GREEN;
		additionalColorNames[114] = NamedColors.PALE_TURQUOISE;
		additionalColorNames[115] = NamedColors.LIGHT_CYAN;
		additionalColorNames[116] = NamedColors.LIGHT_SKY_BLUE;
		additionalColorNames[117] = NamedColors.LAVENDER;
		additionalColorNames[118] = NamedColors.THISTLE;
		additionalColorNames[119] = NamedColors.MISTY_ROSE;
		additionalColorNames[120] = NamedColors.PEACH_PUFF;
		additionalColorNames[121] = NamedColors.PAPAYA_WHIP;
		additionalColorNames[122] = NamedColors.FELDSPAR;
		additionalColorNames[123] = NamedColors.MAGENTA;
		additionalColorNames[124] = NamedColors.VIOLET_RED;
		additionalColorNames[125] = NamedColors.CYAN;

		for (int i = 0; i < additionalColorCellNumber; i++) {
			additionalColorBoxes[i] = new ColorBox(this, SWT.NONE,
					new ColorDescriptor(additionalColors[i].getRGB(),
							additionalColorNames[i]), i);

			additionalColorBoxes[i].addMouseListener(new MouseAdapter() {
				public void mouseUp(MouseEvent e) {
					ColorBox colorBox = (ColorBox) e.widget;
					clearSelection();
					selectedColorBox = colorBox;
					updateAfterSelectionChange();
				}

				public void mouseDoubleClick(MouseEvent e) {
					ColorBox colorBox = (ColorBox) e.widget;
					clearSelection();
					selectedColorBox = colorBox;
					updateAfterSelectionChange();
					NamedColorComposite.this.dialogClose.okCloseDialog();
				}

			});

			additionalColorBoxes[i].getColorBox().addMouseListener(
					new MouseAdapter() {
						public void mouseUp(MouseEvent e) {
							Composite colorBox = (Composite) e.widget;
							clearSelection();
							selectedColorBox = (ColorBox) colorBox.getParent();
							updateAfterSelectionChange();
						}

						public void mouseDoubleClick(MouseEvent e) {
							Composite colorBox = (Composite) e.widget;
							clearSelection();
							selectedColorBox = (ColorBox) colorBox.getParent();
							updateAfterSelectionChange();
							NamedColorComposite.this.dialogClose
									.okCloseDialog();
						}

					});

			additionalColorBoxes[i].addKeyListener(new KeyAdapter() {

				public void keyPressed(KeyEvent e) {

					if ((e.keyCode != SWT.ARROW_UP)
							&& (e.keyCode != SWT.ARROW_DOWN)
							&& (e.keyCode != SWT.ARROW_LEFT)
							&& (e.keyCode != SWT.ARROW_RIGHT)) {
						return;
					}
					int index = ((ColorBox) selectedColorBox)
							.getColorPosition();

					if (e.keyCode == SWT.ARROW_UP) {
						if ((index - 16) >= 0) {
							index = index - 16;
						}
					} else if (e.keyCode == SWT.ARROW_DOWN) {
						if ((index + 16) < additionalColorCellNumber) {
							index = index + 16;
						}
					} else if (e.keyCode == SWT.ARROW_LEFT) {
						if ((index % 16) > 0) {
							index = index - 1;
						}
					} else if (e.keyCode == SWT.ARROW_RIGHT) {
						if (index < 112) {
							if ((index % 16) < 15) {
								index = index + 1;
							}
						} else {
							if ((index % 16) < 13) {
								index = index + 1;
							}

						}

					} else {
						return;
					}

					clearSelection();
					selectedColorBox = additionalColorBoxes[index];
					selectedColorBox.forceFocus();
					updateAfterSelectionChange();

				}

			});

			GridData gd = new GridData();

			gd.heightHint = colorBoxSize;
			gd.widthHint = colorBoxSize;
			additionalColorBoxes[i].setLayoutData(gd);
		}
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.heightHint = colorBoxSize;
		gd.widthHint = colorBoxSize;
		additionalColorBoxes[additionalColorCellNumber - 1].setLayoutData(gd);

	}

	protected void fillBasicColorsAndBorders() {
		String[] basicColorNames = new String[16];
		basicColorNames[0] = NamedColors.GREEN;
		basicColorNames[1] = NamedColors.LIME;
		basicColorNames[2] = NamedColors.TEAL;
		basicColorNames[3] = NamedColors.AQUA;
		basicColorNames[4] = NamedColors.NAVY;
		basicColorNames[5] = NamedColors.BLUE;
		basicColorNames[6] = NamedColors.PURPLE;
		basicColorNames[7] = NamedColors.FUCHSIA;
		basicColorNames[8] = NamedColors.MAROON;
		basicColorNames[9] = NamedColors.RED;
		basicColorNames[10] = NamedColors.OLIVE;
		basicColorNames[11] = NamedColors.YELLOW;
		basicColorNames[12] = NamedColors.WHITE;
		basicColorNames[13] = NamedColors.SILVER;
		basicColorNames[14] = NamedColors.GRAY;
		basicColorNames[15] = NamedColors.BLACK;

		for (int i = 0; i < basicColorCellNumber; i++) {

			basicColorBoxes[i] = new ColorBox(this, SWT.NONE,
					new ColorDescriptor(basicColors[i].getRGB(),
							basicColorNames[i]), i);

			basicColorBoxes[i].addMouseListener(new MouseAdapter() {

				public void mouseUp(MouseEvent e) {
					Composite colorBox = (Composite) e.widget;
					clearSelection();
					selectedColorBox = colorBox;
					updateAfterSelectionChange();
				}

				public void mouseDoubleClick(MouseEvent e) {
					Composite colorBox = (Composite) e.widget;
					clearSelection();
					selectedColorBox = (ColorBox) colorBox.getParent();
					updateAfterSelectionChange();
					NamedColorComposite.this.dialogClose.okCloseDialog();
				}

			});

			basicColorBoxes[i].getColorBox().addMouseListener(
					new MouseAdapter() {
						public void mouseUp(MouseEvent e) {
							Composite colorBox = (Composite) e.widget;
							clearSelection();
							selectedColorBox = (ColorBox) colorBox.getParent();
							updateAfterSelectionChange();
						}

						public void mouseDoubleClick(MouseEvent e) {
							Composite colorBox = (Composite) e.widget;
							clearSelection();
							selectedColorBox = (ColorBox) colorBox.getParent();
							updateAfterSelectionChange();
							NamedColorComposite.this.dialogClose
									.okCloseDialog();
						}

					});

			basicColorBoxes[i].addKeyListener(new KeyAdapter() {

				public void keyPressed(KeyEvent e) {

					if ((e.keyCode != SWT.ARROW_UP)
							&& (e.keyCode != SWT.ARROW_DOWN)
							&& (e.keyCode != SWT.ARROW_LEFT)
							&& (e.keyCode != SWT.ARROW_RIGHT)) {
						return;
					}
					int index = ((ColorBox) selectedColorBox)
							.getColorPosition();

					if (e.keyCode == SWT.ARROW_UP) {
					} else if (e.keyCode == SWT.ARROW_DOWN) {
					} else if (e.keyCode == SWT.ARROW_LEFT) {
						if ((index % 16) > 0) {
							index = index - 1;
						}
					} else if (e.keyCode == SWT.ARROW_RIGHT) {
						if (index + 1 < 16) {
							index = index + 1;
						}

					} else {
						return;
					}

					clearSelection();
					selectedColorBox = basicColorBoxes[index];

					updateAfterSelectionChange();

				}

			});

			GridData gd = new GridData();

			gd.heightHint = colorBoxSize;
			gd.widthHint = colorBoxSize;
			basicColorBoxes[i].setLayoutData(gd);

		}

	}

	protected void updateAfterSelectionChange() {
		selectedColorBox.forceFocus();
		selectedColorBox.setBackground(((ColorBox) selectedColorBox)
				.getBorderSelectionColor());
		RGB selectedColorBoxRGB = ((ColorBox) selectedColorBox)
				.getColorBoxColorDescriptor().getRGB();
		String selectedColorBoxColorName = ((ColorBox) selectedColorBox)
				.getColorBoxColorDescriptor().getName();

		if (colorChangedLabelWrapper != null) {
			colorChangedLabelWrapper.getColorDescriptor().setRed(
					selectedColorBoxRGB.red);
			colorChangedLabelWrapper.getColorDescriptor().setGreen(
					selectedColorBoxRGB.green);
			colorChangedLabelWrapper.getColorDescriptor().setBlue(
					selectedColorBoxRGB.blue);

			colorChangedLabelWrapper.getColorDescriptor().setName(
					selectedColorBoxColorName);

			if (colorChangedLabelWrapper.getColorChangedLabel() != null) {
				colorChangedLabelWrapper.getColorChangedLabel().setText(
						this.getProperPresentationString());
			}
		}
		dialogClose.selectionChanged();
	}

	private String getProperPresentationString() {
		String colorString;
		if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_TYPE) {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithHash();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PRESENTATION_TYPE) {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithFunction();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.RGB_FUNCTION_PERCENTAGE_PRESENTATION_TYPE) {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithFunctionAndPercentage();
		} else if (colorChangedLabelWrapper.getCurrentPresentationType() == ColorChangedLabelWrapper.HASHED_PRESENTATION_SHORT_TYPE) {
			colorString = colorChangedLabelWrapper
					.getShortHashedColorWithApproximatedColorName();
		} else {
			colorString = colorChangedLabelWrapper
					.getColorNameColorStringWithHash();
		}
		return colorString;
	}

	protected void clearSelection() {
		if (selectedColorBox != null) {
			selectedColorBox.setBackground(((ColorBox) selectedColorBox)
					.getNotSelectedColor());
			selectedColorBox = null;
		}
		if (selectedColorBox != null) {
			selectedColorBox = null;
		}
	}

	public void setColorChangedLabel(ColorChangedLabelWrapper colorChangedLabel) {

		this.colorChangedLabelWrapper = colorChangedLabel;

		if (this.colorChangedLabelWrapper.getColorDescriptor() != null) {
			ColorDescriptor desc = colorChangedLabelWrapper
					.getColorDescriptor();
			for (ColorBox box : basicColorBoxes) {
				if (desc.getRed() == box.getColorBoxColorDescriptor().getRed()
						&& desc.getGreen() == box.getColorBoxColorDescriptor()
								.getGreen()
						&& desc.getBlue() == box.getColorBoxColorDescriptor()
								.getBlue()) {
					box.setSelected(true);
					box.setBackground(box.getBorderSelectionColor());
					box.redraw();
					break;
				}
			}

			for (ColorBox box : additionalColorBoxes) {
				if (desc.getRed() == box.getColorBoxColorDescriptor().getRed()
						&& desc.getGreen() == box.getColorBoxColorDescriptor()
								.getGreen()
						&& desc.getBlue() == box.getColorBoxColorDescriptor()
								.getBlue()) {
					box.setSelected(true);
					box.setBackground(box.getBorderSelectionColor());
					box.redraw();
					break;
				}
			}
		}
	}
}
