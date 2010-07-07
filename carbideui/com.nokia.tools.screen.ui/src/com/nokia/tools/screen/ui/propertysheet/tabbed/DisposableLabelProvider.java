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
package com.nokia.tools.screen.ui.propertysheet.tabbed;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Workaround for the current tabbed property sheet page that never disposes any
 * images.
 */
public abstract class DisposableLabelProvider extends LabelProvider {
	private Map<Object, Image> images = new HashMap<Object, Image>();
	private Object contextPage;

	/**
	 * @return the contextPage
	 */
	public Object getContextPage() {
		return contextPage;
	}

	/**
	 * @param contextPage the contextPage to set
	 */
	public void setContextPage(Object contextPage) {
		this.contextPage = contextPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public final Image getImage(Object element) {
		Image image = images.remove(contextPage);
		if (image != null) {
			image.dispose();
		}
		image = createImage(element);
		if (image != null) {
			images.put(contextPage, image);
		}
		return image;
	}

	protected abstract Image createImage(Object element);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
	 */
	@Override
	public void dispose() {
		// never called, pity
		super.dispose();
		for (Image image : images.values()) {
			if (image != null) {
				image.dispose();
			}
		}
		images.clear();
	}

	/**
	 * Dispose the image when the property page is closed.
	 * 
	 * @param contextPage the property page.
	 */
	public void pageDisposed(Object contextPage) {
		Image image = images.remove(contextPage);
		if (image != null) {
			image.dispose();
		}
	}
}
