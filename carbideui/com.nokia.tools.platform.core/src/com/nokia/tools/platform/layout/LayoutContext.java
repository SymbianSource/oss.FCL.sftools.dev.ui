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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.resource.util.SimpleCache;

public class LayoutContext implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<LayoutInfo> layouts;

	public LayoutContext(LayoutInfo[] layouts) {
		if (layouts == null || layouts.length == 0) {
			throw new NullPointerException();
		}
		// order is important
		this.layouts = new LinkedHashSet<LayoutInfo>(layouts.length);
		for (LayoutInfo info : layouts) {
			this.layouts.add(info);
		}
	}

	/**
	 * @return the layouts
	 */
	public LayoutInfo[] getLayouts() {
		return layouts.toArray(new LayoutInfo[layouts.size()]);
	}

	public LayoutSet getLayoutSet() {
		return getLayouts()[0].getLayoutSet();
	}

	public LayoutXmlData[] getXmlData() throws LayoutException {
		LayoutInfo[] layouts = getLayouts();
		LayoutXmlData[] data = new LayoutXmlData[layouts.length];
		for (int i = 0; i < layouts.length; i++) {
			data[i] = layouts[i].getLayoutData();
		}
		return data;
	}

	public LayoutNode getLayoutNode(ComponentInfo component)
			throws LayoutException {
		List list = new LayoutCalculator(this).processLocationData(component);
		LayoutNode parent = null;
		for (Object obj : list) {
			// String str = ((String) obj).toLowerCase();
			String str = ((String) obj);
			LayoutNode node = new LayoutNode(this, str);
			if (parent != null) {
				parent.addChild(node);
			}
			parent = node;
		}
		return parent == null ? null : parent.getRoot();
	}

	public Map<String, LayoutNode> getAllNodes() {
		Map<String, LayoutNode> nodes = new TreeMap<String, LayoutNode>();
		try {
			for (LayoutXmlData data : getXmlData()) {
				for (CompactElement element : data.getAllComponents()) {
					String name = element
							.getAttribute(LayoutConstants.ATTR_COMPONENT_NAME);
					if (nodes.containsKey(name)) {
						continue;
					}
					LayoutNode node = new LayoutNode(this);
					node.setName(name);
					nodes.put(name, node);
				}
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return nodes;
	}

	public void calculate(ComponentInfo component) throws LayoutException {
		calculate(component, true);
	}

	public void calculate(ComponentInfo component, boolean shallCache)
			throws LayoutException {
		if (component == null) {
			return;
		}
		component.setContext(this);
		ComponentInfo cached = (ComponentInfo) SimpleCache.getData(
				LayoutManager.class, component);
		
		if (cached != null && cached.getLayout() != null) {
			component.setLayout(cached.getLayout());
			return;
		}
		LayoutManager.markCacheDirty();
		new LayoutCalculator(this).calculateLayout(component);
		if (shallCache) {
			//Looks like its ok to comment this.
			SimpleCache.cache(LayoutManager.class, component, component);
		}
	}

	public void calculate(ComponentInfo[] components) throws LayoutException {
		if (components == null || components.length == 0) {
			return;
		}

		List<ComponentInfo> list = new ArrayList<ComponentInfo>(
				components.length);
		for (ComponentInfo component : components) {
			component.setContext(this);
			ComponentInfo cached = (ComponentInfo) SimpleCache.getData(
					LayoutManager.class, component);
			if (cached != null) {
				component.setLayout(cached.getLayout());
			} else {
				list.add(component);
			}
		}

		if (list.isEmpty()) {
			return;
		}

		// computes new layout info, marks cache dirty so new cache will be
		// generated
		LayoutManager.markCacheDirty();

		ComponentInfo[] newOnes = list.toArray(new ComponentInfo[list.size()]);
		new LayoutCalculator(this).calculateLayout(newOnes);
		for (ComponentInfo newOne : newOnes) {
			SimpleCache.cache(LayoutManager.class, newOne, newOne);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return layouts.equals(((LayoutContext) obj).layouts);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return layouts.hashCode();
	}
}
