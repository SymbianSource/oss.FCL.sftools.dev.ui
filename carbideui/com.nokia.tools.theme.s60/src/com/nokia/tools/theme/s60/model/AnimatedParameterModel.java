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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.nokia.tools.platform.theme.ParameterModel;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

public class AnimatedParameterModel extends ParameterModel {
	public AnimatedParameterModel(ThemeGraphic tg) {
		super(tg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ParameterModel#getValue(java.lang.String)
	 */
	@Override
	public String getValue(String parameterName) {
		BaseValueModelInterface valueModel = (BaseValueModelInterface) ((MorphedGraphic) getThemeGraphic())
				.getValueModel((String) getAttributes().get(
						ThemeTag.ELEMENT_VALUEMODEL_REF));
		if (valueModel == null)
			return new Integer((String) this.getAttributes().get(
					ThemeTag.ATTR_VALUE)).toString();
		if (getThemeGraphic().isAnimationStarted())
		{
			HashMap map = valueModel.getValue();
			if (map.get(parameterName) != null)
				return ((map.get(parameterName).toString()));
			if (map.size() == 1) {
				Set set = map.keySet();
				Iterator iter = set.iterator();
				String key = (String) iter.next();
				if (key.equalsIgnoreCase(ValueConstants.TYPE_INTEGER)) {
					return ((map.get(key)).toString());
				} else if (key.equalsIgnoreCase(ValueConstants.TYPE_DOUBLE)) {
					return ((map.get(key)).toString());
				}
			}
		} else {
			// instead of returning default from animation model,
			// return value from parameter first.

			if (getAttributes().get(ThemeTag.ATTR_VALUE) != null) {
				return getAttributes().get(ThemeTag.ATTR_VALUE).toString();
			} else {
				String defVal = valueModel.getParameters().get(
						ValueConstants.DEFAULTVALUE);
				if (defVal != null)
					return defVal;
				return valueModel.getParameters().get(ThemeTag.DEFAULT);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ParameterModel#isAnimatedModel()
	 */
	@Override
	public boolean isAnimatedModel() {
		return true;
	}
}
