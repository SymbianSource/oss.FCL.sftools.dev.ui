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
/*
 * File Name PreviewImage.java
 * 
 * Description File contains the class which holds the preview image information
 */

package com.nokia.tools.theme.s60.model;

import java.util.ArrayList;

import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;

/**
 * Class which holds which holds the preview image information
 */

public class AnimationImage extends ThemeBasicData {

	/*
	 * Constructor
	 * 
	 */
	public AnimationImage(String name) {
		setThemeName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof String;
	}

	/**
	 * Method to add an elements information to a animation image's children
	 * list
	 * 
	 * @param objElementName Object to be added as a child to preview image
	 */
	public void addChild(int position, Object objSelectedImage)
			throws ThemeException {
		try {

		

			String selectedImageName = (String) objSelectedImage;

			boolean duplicate = false;

			String existingImageName = null;

			if (this.children != null) {

				for (int i = 0; i < this.children.size(); i++) {
					existingImageName = (String) this.children.get(i);
					if (existingImageName.equalsIgnoreCase(selectedImageName)) {
						duplicate = true;
						break;
					}
				}
			}

			if (!duplicate) {
				if (this.children == null) {
					this.children = new ArrayList<Object>();
				}
				
				if (position < 0) {
					this.children.add(selectedImageName);
				} else {
					this.children.add(position, selectedImageName);
				}
			} else {
				// mergeData
			}
		} catch (Exception e) {
			throw new ThemeException(
					"Adding element to animationImage  failed : "
							+ e.getMessage());
		}
	}

} // end of the PreviewImage class
