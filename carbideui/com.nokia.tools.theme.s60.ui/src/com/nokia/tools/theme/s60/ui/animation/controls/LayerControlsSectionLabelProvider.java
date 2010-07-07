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
package com.nokia.tools.theme.s60.ui.animation.controls;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.screen.ui.propertysheet.tabbed.DisposableLabelProvider;

public class LayerControlsSectionLabelProvider extends DisposableLabelProvider {
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.DisposableLabelProvider#createImage(java.lang.Object)
	 */
	@Override
	protected Image createImage(Object element) {
		if (element instanceof IStructuredSelection) {
			element = ((IStructuredSelection) element).getFirstElement();
		}
		if (element instanceof ILayer) {
			ILayer layer = (ILayer) element;
			return new Image(null, layer.getLayerIconImage().getImageData()
					.scaledTo(16, 16));
		}
		if (element instanceof ILayerEffect) {
			ILayerEffect layerEffect = (ILayerEffect) element;
			return new Image(null, layerEffect.getParent().getLayerIconImage()
					.getImageData().scaledTo(16, 16));
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ve.internal.cde.emf.DefaultLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		if (element instanceof IStructuredSelection) {
			element = ((IStructuredSelection) element).getFirstElement();
		}

		if (element instanceof ILayer) {
			return ((ILayer) element).getName();
		}

		if (element instanceof ILayerEffect) {
			return ((ILayerEffect) element).getParent().getName() + " - "
					+ ((ILayerEffect) element).getName();
		}

		if (element instanceof IAnimatedImage) {
			return null;
		}

		return super.getText(element);
	}
}
