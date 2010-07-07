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
package com.nokia.tools.theme.s60;

import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenElementData;
import com.nokia.tools.theme.screen.ThemeTextElement;


public class S60ThemeTextElement extends ThemeTextElement {

	public S60ThemeTextElement(ThemeData data) {
		super(data);
	}

	public S60ThemeTextElement(ThemeScreenElementData data) {
		super(data);
	}

	public S60ThemeTextElement(ThemeTextElement textElement) {
		super(textElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.screen.ThemeTextElement#getDeviceSize(int,
	 *      java.lang.String)
	 */
	@Override
	protected int getDeviceSize(int fontSize, String typeFace) {
		boolean isDigi = typeFace.toLowerCase().contains("digi");
		if (fontSize <= 8) {
			if (isDigi) {
				return fontSize - 1;
			}
			return fontSize;
		}
		if (fontSize <= 13) {
			if (isDigi) {
				return fontSize;
			}
			return fontSize - 2;
		}

		if (fontSize == 16) {
			return fontSize - 2;
			//return fontSize;
		}
		
		if (fontSize == 24) {
			return fontSize - 2;
		}
		
		if (fontSize == 21) {
			return fontSize - 3;
		}

		if (fontSize < 36) {
			return fontSize - 3;
		}
		return fontSize - 4;
	}
}
