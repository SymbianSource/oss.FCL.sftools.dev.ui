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
package com.nokia.tools.screen.ui.gef;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

/**
 * This is the JFace label provider for the screen elements. The icon and text
 * are provided by the underlying content data.
 * 
 */
public class WidgetLabelProvider extends LabelProvider {
	private Image image;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element) {
		if (image != null) {
			image.dispose();
		}
		IScreenElement adapter = JEMUtil.getScreenElement((EObject) element);
		if (adapter != null) {
			image = adapter.getImage();
			return image;
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		IScreenElement adapter = JEMUtil.getScreenElement((EObject) element);
		if (adapter != null) {
			String text = adapter.getText();
			if (text != null) {
				return text;
			}
		}
		return super.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (image != null) {
			image.dispose();
		}
	}
}
