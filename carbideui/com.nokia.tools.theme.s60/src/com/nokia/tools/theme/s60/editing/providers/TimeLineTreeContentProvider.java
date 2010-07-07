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
package com.nokia.tools.theme.s60.editing.providers;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;

import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.ILayerEffect;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITimeLineTreeContentProvider;
import com.nokia.tools.platform.theme.EffectConstants;

public class TimeLineTreeContentProvider implements
		ITimeLineTreeContentProvider {

	public TimeLineTreeContentProvider(TimingModel m, TimeSpan s,
			boolean showAnimatedOnly) {
		timing = m;
		span = s;
		this.showAnimatedOnly = showAnimatedOnly;
	}

	public static final Object[] EMPTY = new Object[0];

	private TimingModel timing;

	private TimeSpan span;

	boolean showAnimatedOnly;

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IImage)
			return ((IImage) parentElement).getLayers().toArray();
		if (parentElement instanceof ILayer) {
			// returns only layers, that are animated in given timing

			List<ILayerEffect> effects = ((ILayer) parentElement)
					.getSelectedLayerEffects();
			
			// remove apply graphics effect
			for (int i = 0; i < effects.size(); i++) {
				if (effects.get(i).getName().equals(EffectConstants.APPLYGRAPHICS)) {
						effects.remove(i--);
				}
			}
			
			if (showAnimatedOnly) {
				for (int i = 0; i < effects.size(); i++) {
					if (!effects.get(i).isAnimatedFor(timing))
						effects.remove(i--);
				}
				if (span != null) {
					for (int i = 0; i < effects.size(); i++) {
						if (!effects.get(i).isAnimatedFor(span))
							effects.remove(i--);
					}
				}
			}
			return effects.toArray();
		}
		return EMPTY;
	}

	public Object getParent(Object element) {
		if (element instanceof IImage)
			return null;
		if (element instanceof ILayer) {
			return ((ILayer) element).getParent();
		}
		if (element instanceof ILayerEffect) {
			return ((ILayerEffect) element).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	/*
	 * (non-Javadoc) Returns roots for tree.
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public boolean isSelected(Object element) {
		if (element instanceof ILayer) {
			return ((ILayer) element).isAnimatedInPreview();
		}
		if (element instanceof ILayerEffect) {
			return ((ILayerEffect) element).isAnimatedInPreview();
		}
		return false;
	}

}