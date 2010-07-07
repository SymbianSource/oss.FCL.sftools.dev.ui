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

package com.nokia.tools.theme.content;

import java.util.Collections;
import java.util.List;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.platform.theme.preview.PreviewRefer;

public class ThemeScreenReferData extends ThemeData {

	public ThemeScreenReferData(EditObject resource) {
		super(resource);
	}

	public ThemeScreenData getReferredScreen() {
		Theme theme = (Theme) getData().getRoot();
		PreviewRefer refer = (PreviewRefer) getData();
		PreviewImage referredImage = (PreviewImage) theme.getThemePreview()
				.getPreviewImageByName(refer.getReferedScreenName(),
						refer.getParent().getDisplays());
		if (referredImage == null) {
			return null;
		}
		IContentData data = ((ThemeContent) getRoot())
				.findByData(referredImage);
		if (data instanceof ThemeScreenData) {
			return (ThemeScreenData) data;
		}
		return null;
	}

	public List<ThemeScreenElementData> findBySkinnableEntity(
			SkinnableEntity entity) {
		ThemeScreenData preview = getReferredScreen();
		if (preview == null) {
			return Collections.EMPTY_LIST;
		}
		return preview.findBySkinnableEntity(entity);
	}

	public List<ThemeScreenElementData> findAllElements() {
		ThemeScreenData preview = getReferredScreen();
		if (preview == null) {
			return Collections.EMPTY_LIST;
		}
		return preview.findAllElements();
	}
}
