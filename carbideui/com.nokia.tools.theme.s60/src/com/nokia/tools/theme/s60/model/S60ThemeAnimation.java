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


package com.nokia.tools.theme.s60.model;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;

/**
 * The class holds skin preview information
 * 
 */

public class S60ThemeAnimation extends ThemeBasicData {

	/**
	 * Default constructor
	 * 
	 */
	public S60ThemeAnimation() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof AnimationImage;
	}

	/**
	 * Method to add a Image to the Preview's children list
	 * 
	 * @param objImage PreviewImage object to be added as a child to Preview
	 */
	public void addChild(int position, Object objImage) throws ThemeException {
		try {

			AnimationImage animationImage = (AnimationImage) objImage;
			String name = animationImage.getThemeName();
			String model = animationImage
					.getAttributeValue(ThemeTag.ATTR_PHONENAME);

			boolean duplicate = false;

			AnimationImage existingAnimationImage = null;
			String phModel = null;

			if (this.children != null) {
				for (int i = 0; i < this.children.size(); i++) {
					existingAnimationImage = (AnimationImage) this.children
							.get(i);
					phModel = existingAnimationImage
							.getAttributeValue(ThemeTag.ATTR_PHONENAME);
					if (((existingAnimationImage.getThemeName()).toLowerCase())
							.equals(name.toLowerCase())
							&& (model.equalsIgnoreCase(phModel))) {
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
					this.children.add(animationImage);
				} else {
					this.children.add(position, animationImage);
				}
			} else {
				
			}

		} catch (Exception e) {
			throw new ThemeException(
					"Adding component to componentgroup  failed : "
							+ e.getMessage());
		}

	}

	/**
	 * Method to update the existing S60ThemeAnimation with a new one
	 * 
	 * @param newPreview S60ThemeAnimation object with which the existing
	 *            S60ThemeAnimation has to be updated.
	 */
	void update(S60ThemeAnimation newAnimation) throws ThemeException {

		if (newAnimation == null) {
			return;
		}

		List newAnimationChildren = newAnimation.getChildren();
		if (newAnimationChildren != null) {

			for (int i = 0; i < newAnimationChildren.size(); i++) {

				this.addChild((AnimationImage) newAnimationChildren.get(i));
			}
		}

		return;
	}

} // end of the S60SkinAniamtion class
