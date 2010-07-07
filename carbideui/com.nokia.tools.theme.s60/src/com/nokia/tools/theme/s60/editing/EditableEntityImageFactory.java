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

package com.nokia.tools.theme.s60.editing;

import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.theme.editing.BasicEntityImage;
import com.nokia.tools.theme.editing.IEntityImageFactory;
import com.nokia.tools.theme.s60.S60ThemePlugin;

/**
 * The place for instantiating IImage wrappers arround skina
 */
public class EditableEntityImageFactory implements IEntityImageFactory {
	private static EditableEntityImageFactory self = new EditableEntityImageFactory();

	private EditableEntityImageFactory() {
	}

	public static EditableEntityImageFactory getInstance() {
		return self;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.editing.IEntityImageFactory#createEntityImage(com.nokia.tools.platform.theme.SkinnableEntity,
	 *      com.nokia.tools.platform.theme.ThemeBasicData, int, int)
	 */

	public BasicEntityImage createEntityImage(SkinnableEntity element,
			ThemeBasicData screenElement, int width, int height) {
		if (element != null) {
			try {
				String type = element.isEntityType();
				if (ThemeTag.ELEMENT_BMPANIM.equalsIgnoreCase(type)) {
					return new EditableAnimatedEntity(element, width, height);
				}
				if (screenElement instanceof PreviewElement) {
					int opacity = 0;
					if (((PreviewElement) screenElement)
							.getAttributeValue(ThemeTag.ATTR_OPACITY) != null) {
						opacity = Integer
								.parseInt(((PreviewElement) screenElement)
										.getAttributeValue(ThemeTag.ATTR_OPACITY));
					}
					
					if ((width == 0)||(height == 0)) {
						
						Layout layoutInfo = null;						
						try {
							layoutInfo = element.getLayoutInfo();
						} catch (Exception e) {
							
							
						}
						
						if (layoutInfo != null) {
							if (width == 0 ) {
							  width  = layoutInfo.W();
							}
							if (height == 0) {
								height = layoutInfo.H();
							}
						}
						
					}
					
					
					return new EditableEntityImage(element, null, (PreviewElement) screenElement, width, height,
							 opacity);
				}
				return new EditableEntityImage(element, null, width, height);

			} catch (Exception e) {
				S60ThemePlugin.error(e);
			}
		}
		return null;
	}

}
