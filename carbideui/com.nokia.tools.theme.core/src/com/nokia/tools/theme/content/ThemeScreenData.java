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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.preview.PreviewImage;
import com.nokia.tools.screen.core.IPropertyAdapter;
import com.nokia.tools.screen.core.IScreenAdapter;

public class ThemeScreenData extends ThemeData {
	protected ThemeScreenData(EditObject resource) {
		super(resource);
	}

	public List<ThemeScreenElementData> findBySkinnableEntity(
			SkinnableEntity entity) {
		List<ThemeScreenElementData> list = new ArrayList<ThemeScreenElementData>();
		for (IContentData child : getChildren()) {
			if (child instanceof ThemeScreenReferData) {
				list.addAll(((ThemeScreenReferData) child)
						.findBySkinnableEntity(entity));
			} else if (child instanceof ThemeScreenElementData) {
				if (((ThemeScreenElementData) child)
						.containsSkinnableEntity(entity)) {
					list.add((ThemeScreenElementData) child);
				}
			}
		}
		return list;
	}

	public List<ThemeScreenElementData> findAllElements() {
		List<ThemeScreenElementData> list = new ArrayList<ThemeScreenElementData>();
		for (IContentData child : getChildren()) {
			if (child instanceof ThemeScreenReferData) {
				list.addAll(((ThemeScreenReferData) child).findAllElements());
			} else if (child instanceof ThemeScreenElementData) {
				list.add((ThemeScreenElementData) child);
			}
		}
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getAdapter(java.lang.Class)
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (getData() == null) {
			// indicating not existing anymore, client shall act properly on
			// this case
			return null;
		}
		if (IScreenAdapter.class == adapter) {
			return new ThemeScreenAdapter(this);
		}
		if (adapter == IPropertyAdapter.class) {
			return IPropertyAdapter.NULL_ADAPTER;
		}
		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#supportsPlatform(com.nokia.tools.platform.core.IPlatform)
	 */
	@Override
	public boolean supportsPlatform(IPlatform platform) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.ThemeData#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String name) {
		if (ContentAttribute.APPLICATION_NAME.name().equals(name)) {
			return ((PreviewImage) getData()).getName();
		}
		if (ContentAttribute.DISPLAY.name().equals(name)) {
			return getData().getDisplay();
		}
		if (ContentAttribute.BOUNDS.name().equals(name)) {
			Display display = getData().getDisplay();
			return new Rectangle(display.getWidth(), display.getHeight());
		}

		return super.getAttribute(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentData.Stub#findById(java.lang.String)
	 */
	public IContentData findById(String id) {
		if (id == null) {
			return null;
		}

		// there might be multiple elements having the same id, we only return
		// the one that is good for the current display
		Display rootDisplay = ((ThemeContent) getRoot()).getData().getDisplay();
		for (IContentData child : getAllChildren()) {
			if (id.equals(child.getId())
					&& ((ThemeData) child).getData().supportsDisplay(
							rootDisplay)) {
				return child;
			}
		}
		return null;
	}
}
