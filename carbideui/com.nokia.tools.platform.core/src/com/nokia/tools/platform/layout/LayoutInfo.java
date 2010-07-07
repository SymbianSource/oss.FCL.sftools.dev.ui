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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import com.nokia.tools.platform.extension.ILayoutDescriptor;
import com.nokia.tools.platform.extension.ILayoutVariantDescriptor;
import com.nokia.tools.resource.util.SimpleCache;

public class LayoutInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private LayoutSet layoutSet;
	private String layoutId;
	private String variantId;

	LayoutInfo(LayoutSet layoutSet, String layoutId, String variantId) {
		if (layoutSet == null || layoutId == null || variantId == null) {
			throw new NullPointerException();
		}
		this.layoutSet = layoutSet;
		this.layoutId = layoutId;
		this.variantId = variantId;
	}

	/**
	 * @return the layoutId
	 */
	public String getLayoutId() {
		return layoutId;
	}

	/**
	 * @return the variantId
	 */
	public String getVariantId() {
		return variantId;
	}

	/**
	 * @return the layoutSet
	 */
	public LayoutSet getLayoutSet() {
		return layoutSet;
	}

	public ILayoutDescriptor getLayoutDescriptor() {
		return layoutSet.getLayoutDescriptor(layoutId);
	}

	public ILayoutVariantDescriptor getVariantDescriptor() {
		ILayoutDescriptor desc = getLayoutDescriptor();
		if (desc != null) {
			return desc.getVariant(variantId);
		}
		return null;
	}

	synchronized LayoutXmlData getLayoutData() throws LayoutException {
		Map<Object, Object> cache = SimpleCache
				.getGroupData(LayoutManager.class);
		if (cache != null) {
			LayoutXmlData data = (LayoutXmlData) cache.get(this);
			if (data != null) {
				return data;
			}
		}
		LayoutXmlData data = new LayoutXmlData(this);
		SimpleCache.cache(LayoutManager.class, this, data);
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LayoutInfo) {
			LayoutInfo b = (LayoutInfo) obj;
			return layoutSet.equals(b.layoutSet) && layoutId.equals(b.layoutId)
					&& variantId.equals(b.variantId);
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
		
		return (layoutSet == null ? 0 : layoutSet.hashCode())
				^ layoutId.hashCode() ^ variantId.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return layoutSet + ": " + layoutId + ":" + variantId;
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		String layoutSetId = (String) in.readObject();
		layoutSet = LayoutSet.getLayoutSet(layoutSetId);
		layoutId = (String) in.readObject();
		variantId = (String) in.readObject();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(layoutSet.getDescriptor().getId());
		out.writeObject(layoutId);
		out.writeObject(variantId);
	}
}
