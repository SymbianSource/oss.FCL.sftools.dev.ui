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
package com.nokia.tools.s60.editor.menus;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.screen.ui.utils.EclipseUtils;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 */
public abstract class ContentComboContributionItem extends
		ComboContributionItem {
	public ContentComboContributionItem(String id) {
		super(id);
	}

	/**
	 * Retrieves the content from the editor.
	 * 
	 * @return the retrieved content or null if any of the following conditions
	 *         is satisfied:
	 *         <ul>
	 *         <li>editor itself is null</li>
	 *         <li>there is no {@link IContentAdapter} associated with the
	 *         editor</li>
	 *         <li>the {@link IContentAdapter} doesn't provide any content.</li>
	 *         </ul>
	 */
	public IContent getContent() {
		IWorkbenchPart part = getEditorPart();
		if (part == null) {
			return null;
		}
		IContentAdapter adapter = (IContentAdapter) part
				.getAdapter(IContentAdapter.class);
		if (adapter == null) {
			return null;
		}
		return ScreenUtil.getPrimaryContent(adapter.getContents());
	}

	public IEditorPart getEditorPart() {
		return EclipseUtils.getActiveSafeEditor();
	}
}
