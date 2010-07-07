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

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.content.core.IContentDelta;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.screen.core.IScreenAdapter;
import com.nokia.tools.screen.core.IScreenContext;
import com.nokia.tools.screen.core.IScreenElement;

public class ThemeScreenAdapter implements IScreenAdapter {
	private ThemeData data;

	public ThemeScreenAdapter(ThemeData data) {
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenAdapter#buildScreen(com.nokia.tools.screen.core.IScreenContext)
	 */
	public IScreenElement buildScreen(IScreenContext context) {
		IScreenElement element = ((ThemeData) data).getProvider()
				.createScreenElement(data);
		buildScreen(element, data, false, context);
		return element;
	}

	private void buildScreen(IScreenElement parentElement, IContentData data,
			boolean isReferred, IScreenContext context) {
		for (IContentData child : data.getChildren()) {
			ThemeBasicData tbd = ((ThemeData) child).getData();
			if (tbd instanceof PreviewElement) {
				if (isReferred) {
					// for referred element need to set the current screen in
					// order to get the proper layout info
					ThemeBasicData parentData = ((ThemeData) parentElement
							.getData()).getData();
					if (parentData instanceof PreviewImage) {
						((PreviewElement) tbd)
								.setScreen((PreviewImage) parentData);
					}
				}
				if (!((PreviewElement) tbd).supportsDisplay(context
						.getDisplay())) {
					continue;
				}
			}
			if (child instanceof ThemeScreenReferData) {
				ThemeScreenData preview = ((ThemeScreenReferData) child)
						.getReferredScreen();
				if (preview == null) {
					continue;
				}
				buildScreen(parentElement, preview, true, context);
			}
			IScreenElement element = ((ThemeData) child).getProvider()
					.createScreenElement(child);
			if (element != null) {
				parentElement.addChild(element);
			}
			buildScreen(element, child, false, context);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenAdapter#isModelScreen()
	 */
	public boolean isModelScreen() {
		PreviewImage screen = (PreviewImage) data.getData();
		return screen.isPreview()
				&& screen.supportsDisplay(screen.getRoot().getDisplay());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.core.IScreenAdapter#updateScreen(com.nokia.tools.content.core.IContentDelta,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IScreenElement updateScreen(IContentDelta delta,
			IProgressMonitor monitor) {
		return null;
	}
}
