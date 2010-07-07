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
package com.nokia.tools.theme.ui.bitmap.propertysheet;

import org.eclipse.jface.viewers.IFilter;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.theme.content.ThemeData;

public class BitmapFilter implements IFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFilter#select(java.lang.Object)
	 */
	public boolean select(Object object) {
		IContentData data = JEMUtil.getContentData(object);
		if (data instanceof ThemeData) {
			IToolBoxAdapter tba = (IToolBoxAdapter) data
					.getAdapter((IToolBoxAdapter.class));
			if (tba != null
					&& (tba.isMultipleLayersSupport() || tba.isFile() || tba
							.isText()))
				return false;

			ISkinnableEntityAdapter ska = (ISkinnableEntityAdapter) data
					.getAdapter((ISkinnableEntityAdapter.class));
			if (ska != null) {
				if (ska.isColour()) {
					return false;
				} else {
					if (ska.getContentData() == null)
						return false;
					if (((ThemeData) ska.getContentData()).getSkinnableEntity() == null)
						return false;
					IImageAdapter adapter = (IImageAdapter) ska
							.getContentData().getAdapter(IImageAdapter.class);
					// checking for bitMapProperties in the bean to make sure
					// that the feature can be set from BitMapProperties Section
					if (adapter != null) {
						// use single image instance because adapter.getImage()
						// always creates a new one and the layer init part is
						// expensive thus slow when large amount of data is
						// selected.
						IImage image = adapter.getImage();
						if (image != null) {
							ILayer layer = image.getLayer(0);
							if (layer != null) {
								if (!layer.getAvailableLayerEffects().isEmpty()) {
									return true;
								}
							}
						}
					}
					return false;
				}
			}
		}

		return false;
	}
}
