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
import com.nokia.tools.theme.content.ThemeScreenElementData;
import com.nokia.tools.widget.theme.ThemeImage;

public class ThemeImageElement extends NormalImageElement {
	public ThemeImageElement(ThemeScreenElementData data) {
		super(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.NormalImageElement#createWidget()
	 */
	@Override
	protected Object createWidget() {
		return new ThemeImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.NormalImageElement#initWidgetSpi(com.nokia.tools.screen.core.IScreenContext)
	 */
	@Override
	protected void initWidgetSpi(IScreenContext context) {
		super.initWidgetSpi(context);

		IColorAdapter adapter = (IColorAdapter) getData().getAdapter(
				IColorAdapter.class);
		if (adapter != null) {
			((ThemeImage) getBean()).setColor(adapter.getColor());
		}
	}
}
