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

import com.nokia.tools.media.utils.layers.IColorAdapter;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.theme.content.ILineAdapter;
import com.nokia.tools.theme.content.ThemeScreenElementData;
import com.nokia.tools.widget.theme.ThemeLineGraphic;

/**
 * Screen element for Lines - custom functionality needed: - when user set
 * showLines property on some line, all lines's properties should be set to that
 * value.
 * 
 */
public class ThemeLineGraphicElement extends ThemeGraphicElement {
	public ThemeLineGraphicElement(ThemeScreenElementData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeGraphicElement#createWidget()
	 */
	@Override
	protected Object createWidget() {
		return new ThemeLineGraphic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeGraphicElement#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		super.initWidgetSpi(context);
		// get current settings

		ILineAdapter lineAdapter = (ILineAdapter) getData().getAdapter(
				ILineAdapter.class);
		boolean drawLines = lineAdapter.drawLines();
		ThemeLineGraphic lg = (ThemeLineGraphic) getBean();
		lg.setDrawLines(drawLines);

		IColorAdapter adapter = (IColorAdapter) getData().getAdapter(
				IColorAdapter.class);
		if (adapter != null) {
			lg.setColor(adapter.getColor());
		}
	}
}
