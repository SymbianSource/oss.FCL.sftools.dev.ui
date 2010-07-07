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
package com.nokia.tools.theme.editing;

import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.theme.core.Activator;

public class BasicEntityImageFactory implements IEntityImageFactory {
	private static BasicEntityImageFactory self = new BasicEntityImageFactory();

	private BasicEntityImageFactory() {
	}

	public static BasicEntityImageFactory getInstance() {
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
				if (screenElement instanceof PreviewElement) {
					int opacity = 0;
					if (((PreviewElement) screenElement)
							.getAttributeValue(ThemeTag.ATTR_OPACITY) != null) {
						opacity = Integer
								.parseInt(((PreviewElement) screenElement)
										.getAttributeValue(ThemeTag.ATTR_OPACITY));
					}
					return new BasicEntityImage(element, null, (PreviewElement) screenElement, width, height,
							opacity);
				}
				return new BasicEntityImage(element, null, null, width, height, 0);
			} catch (Exception e) {
				Activator.error(e);
			}
		}
		return null;
	}

}
