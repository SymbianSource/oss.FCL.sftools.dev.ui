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
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenData;
import com.nokia.tools.theme.content.ThemeScreenElementData;

public class PreviewHandler {
	private static final IEntityPreviewer[] PREVIEWERS = { new IconSwapper(),
			new HiddenScreenSwapper() };

	private ThemeContent content;

	public PreviewHandler(ThemeContent content) {
		this.content = content;
	}

	public ThemeScreenData findScreenByData(ThemeData data,
			boolean createScreenIfNotFound) {
		if (data instanceof ThemeScreenData) {
			return (ThemeScreenData) data;
		}
		if (data instanceof ThemeScreenElementData) {
			return (ThemeScreenData) data.getParent();
		}
		if (data.getData() instanceof SkinnableEntity) {
			PreviewImage image = ((Theme) content.getData()).getThemePreview()
					.getPreviewImageForElem(data.getData(),
							createScreenIfNotFound);
			if (image == null) {
				return null;
			}

			ThemeScreenData preview = (ThemeScreenData) content
					.findByData(image);
			for (ThemeScreenElementData elementData : preview
					.findBySkinnableEntity((SkinnableEntity) data.getData())) {
				if (elementData.getData().supportsDisplay(
						preview.getData().getDisplay())) {
					return preview;
				}
			}

			for (IEntityPreviewer previewer : PREVIEWERS) {
				ThemeScreenData result = previewer.preview(preview, data);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

}
