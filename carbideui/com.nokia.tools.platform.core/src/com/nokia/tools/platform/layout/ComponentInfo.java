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

import java.io.Serializable;

public class ComponentInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String variety;
	private String locId;
	private Layout layout;
	private LayoutContext context;

	public ComponentInfo(String name, int variety, String locId) {
		this(name, Integer.toString(variety), locId);
	}

	public ComponentInfo(String name, String variety, String locId) {
		setName(name);
		setVariety(variety);
		setLocId(locId);
	}

	/**
	 * @param locId the locId to set
	 */
	public void setLocId(String locId) {
		this.locId = locId == null
				|| LayoutConstants.NULL.equalsIgnoreCase(locId) ? "" : locId;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	/**
	 * @param variety the variety to set
	 */
	public void setVariety(String variety) {
		this.variety = variety == null
				|| LayoutConstants.NULL.equalsIgnoreCase(variety) ? LayoutConstants.DEFAULT_VARIETY_NO
				: variety;
	}

	/**
	 * @return the locId
	 */
	public String getLocId() {
		return locId.length() == 0 ? null : locId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the variety
	 */
	public String getVariety() {
		return variety;
	}

	/**
	 * @return the layout
	 */
	public Layout getLayout() {
		return layout;
	}

	/**
	 * @param layout the layout to set
	 */
	public void setLayout(Layout layout) {
		if (layout == null) {
			System.err.println("Failed to calculate layout: " + this);
		}
		this.layout = layout;
	}

	void setContext(LayoutContext context) {
		this.context = context;
	}

	LayoutContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComponentInfo) {
			ComponentInfo b = (ComponentInfo) obj;
			return name.equals(b.name)
					&& variety.equals(b.variety)
					&& locId.equals(b.locId)
					&& ((context == null && b.context == null) || (context != null && context
							.equals(b.context)));
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
		return name.hashCode() ^ variety.hashCode() ^ locId.hashCode()
				^ context.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name + ":" + variety + ":" + locId + "\n\t" + layout;
	}
}
