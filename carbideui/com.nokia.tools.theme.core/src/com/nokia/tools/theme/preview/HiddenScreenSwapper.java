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
package com.nokia.tools.theme.preview;

import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Task;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenData;

public class HiddenScreenSwapper implements IEntityPreviewer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.preview.IEntityPreviewer#preview(com.nokia.tools.theme.content.ThemeScreenData,
	 *      com.nokia.tools.theme.content.ThemeData)
	 */
	public ThemeScreenData preview(ThemeScreenData preview, ThemeData data) {
		SkinnableEntity entity = (SkinnableEntity) data.getData();
		if (!isSupported(entity)) {
			return null;
		}
		// first find the screen among hidden screens that contains the
		// element
		PreviewImage newScreen = ((Theme) entity.getRoot()).getThemePreview()
				.findPreviewImageWithElem(entity, null, false);

		// happens e.g. tries to reveal control pane in the landscape mode
		if (newScreen == null) {
			return null;
		}
		return (ThemeScreenData) ((ThemeContent) preview.getRoot())
				.findByData(newScreen);
	}

	private boolean isSupported(SkinnableEntity element) {
		ThemeBasicData parent = element;
		while (parent != null && !(parent instanceof Task)) {
			parent = parent.getParent();
		}
		return (null != parent && parent.getThemeName()
				.equals("POP-UP WINDOWS"));
	}
}
