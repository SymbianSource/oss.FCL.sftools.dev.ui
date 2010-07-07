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
package com.nokia.tools.theme.screen;

import java.awt.Rectangle;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.theme.content.ThemeScreenData;
import com.nokia.tools.widget.theme.ThemeScreen;

public class ThemeScreenElement extends ThemeElement {
	public ThemeScreenElement(ThemeScreenData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeElement#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		super.initWidgetSpi(context);
		Display display = context.getDisplay();
		ThemeScreen screen = (ThemeScreen) getBean();
		screen
				.setBounds(new Rectangle(display.getWidth(), display
						.getHeight()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenElement.ScreenElementAdapter#createWidget()
	 */
	@Override
	protected Object createWidget() {
		return new ThemeScreen();
	}
}
