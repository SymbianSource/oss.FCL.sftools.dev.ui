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
package com.nokia.tools.platform.layout;

import java.util.HashSet;
import java.util.Set;

import com.nokia.tools.platform.extension.ILayoutDescriptor;
import com.nokia.tools.platform.extension.ILayoutSetDescriptor;
import com.nokia.tools.platform.extension.PlatformExtensionManager;


public class LayoutSet {
	private static ILayoutSetDescriptor[] layoutSetDescriptors;
	private static final Set<LayoutSet> LAYOUT_SETS = new HashSet<LayoutSet>();

	private ILayoutSetDescriptor descriptor;
	private LayoutFontXmlData fontData;

	private LayoutSet(ILayoutSetDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	public synchronized static LayoutSet getLayoutSet(String id) {
		if (id == null) {
			return null;
		}
		for (LayoutSet layoutSet : LAYOUT_SETS) {
			if (id.equals(layoutSet.getDescriptor().getId())) {
				return layoutSet;
			}
		}
		for (ILayoutSetDescriptor desc : getLayoutSetDescriptors()) {
			if (id.equals(desc.getId())) {
				LayoutSet layoutSet = new LayoutSet(desc);
				LAYOUT_SETS.add(layoutSet);
				return layoutSet;
			}
		}
		return null;
	}

	/**
	 * @return the descriptor
	 */
	public ILayoutSetDescriptor getDescriptor() {
		return descriptor;
	}

	public ILayoutDescriptor getLayoutDescriptor(String layoutId) {
		if (layoutId == null) {
			return null;
		}
		for (ILayoutDescriptor desc : descriptor.getLayoutDescriptors()) {
			if (layoutId.equals(desc.getId())) {
				return desc;
			}
		}
		return null;
	}

	synchronized LayoutFontXmlData getFontData() {
		if (fontData == null) {
			fontData = new LayoutFontXmlData(descriptor.getFontDescriptor());
		}
		return fontData;
	}

	public LayoutInfo createLayoutInfo(String layoutId, String variantId) {
		return new LayoutInfo(this, layoutId, variantId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LayoutSet) {
			LayoutSet b = (LayoutSet) obj;
			return descriptor.getId().equals(b.descriptor.getId());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return descriptor.getId().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return descriptor.getId();
	}

	private synchronized static ILayoutSetDescriptor[] getLayoutSetDescriptors() {
		if (layoutSetDescriptors == null) {
			layoutSetDescriptors = PlatformExtensionManager.getLayoutSets();
		}
		return layoutSetDescriptors;
	}

	public synchronized static void release() {
		layoutSetDescriptors = null;
		LAYOUT_SETS.clear();
	}
}
