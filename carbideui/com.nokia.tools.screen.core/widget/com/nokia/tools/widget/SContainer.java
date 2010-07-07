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
package com.nokia.tools.widget;

import java.awt.Component;

/**
 * Base class for the container widget.
 * 
 * @author shji
 * @version $Revision: 1.4 $ $Date: 2010/04/21 14:45:18 $
 */
public class SContainer extends SComponent {
	static final long serialVersionUID = 2660578343298199602L;

	public SContainer() {
		setOpaque(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.SComponent#isChildValid(com.nokia.tools.widget.SComponent)
	 */
	@Override
	public boolean isChildValid(SComponent child) {
		return true;
	}

	/**
	 * Adds the given component to this container.
	 * 
	 * @param component
	 *        the component to be added.
	 */
	public void addChild(SComponent component) {
		addChild(component, 0);
	}

	public void addChild(SComponent component, int index) {
		if (!isChildValid(component)) {
			throw new IllegalArgumentException(
					"The child component is not valid: " + component);
		}
		
		if (index <= 0) {
			add((Component) component);
		} else {
			add((Component) component, getComponentCount() - index);
		}
	}

	/**
	 * Removes the given component from this container.
	 * 
	 * @param component
	 *        the component to be removed.
	 */
	public void removeChild(SComponent component) {
		remove((Component) component);
	}
}
