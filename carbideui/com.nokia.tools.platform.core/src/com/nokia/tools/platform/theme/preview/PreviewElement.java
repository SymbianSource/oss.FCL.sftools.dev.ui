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
package com.nokia.tools.platform.theme.preview;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.layout.LayoutException;
import com.nokia.tools.platform.layout.LayoutNode;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.resource.util.StringUtils;

public class PreviewElement extends ThemeBasicData {
	private SkinnableEntity element;
	private Set<Display> displays;
	private PreviewImage screen;

	/**
	 * @return the screen
	 */
	public PreviewImage getScreen() {
		return screen;
	}

	public void setElementId(String elementId) {
		setAttribute(PreviewTagConstants.ATTR_ELEMENT_ID, elementId);
	}

	public String getElementId() {
		return getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_ID);
	}

	public void setText(String text) {
		setAttribute(PreviewTagConstants.ATTR_ELEMENT_TEXT, text);
	}

	public String getText() {
		return getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_TEXT);
	}

	public boolean canBeSkinned() {
		if (getIdentifier() != null)
			return true;

		if (getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID) != null)
			return true;

		return false;
	}

	/**
	 * @param screen the screen to set
	 */
	public void setScreen(PreviewImage screen) {
		this.screen = screen;
	}

	public ComponentInfo getComponentInfo() {
		return new ComponentInfo(getCompName(), getVarietyId(), getLocId());
	}

	public Layout getLayoutInfo() throws LayoutException {
		ComponentInfo component = getComponentInfo();
		getLayoutContext(getParent().getDisplay()).calculate(component);
		return component.getLayout();
	}

	public LayoutNode getLayoutNode() throws LayoutException {
		return getLayoutContext(getDisplay()).getLayoutNode(getComponentInfo());
	}

	/**
	 * This method defines the layout position(location) of this PreviewElement
	 * by analyzing an LayoutNode hierarchy rooted by <code>node</code>
	 * 
	 * @param node tree of layout nodes which is updated
	 */
	public void setLayoutNode(LayoutNode node) {
		LayoutNode leaf = node.getLeaf();
		// detect if layout is default or custom
		LayoutNode defaultLayout = leaf.getDefaultLayoutTree();
		LayoutNode root = leaf.getRoot();
		if (defaultLayout.equalsTo(root)) {
			// added component has default layout
			setCompName(leaf.getName());
			setVarietyId(Integer.toString(leaf.getVariety()));
			setLocId(null);
		} else {
			// component layout location has diferent end part of it
			// layout tree of LaoutNode of shape:
			// layout_tree_start_part(layoutNode*) ->
			// layout_tree_end_part(layoutNode+)
			// we need to find the layout_tree_end_part

			LayoutNode currentNode = leaf;

			while (currentNode != null) {
				// while layout_tree_start_part's layout location is different
				// from currentNode layout location
				// we continue in the searching of the default layout location.
				// At every loop cycle we remember an next LayoutNode of the
				// layout location hierarchy,
				// which creates the part of the location which is different
				// from default layout

				// to avoid of the hierarchy destruction we do copying of the
				// hierarchy and working on this copy
				LayoutNode nodeCopy = currentNode.copy();
				while (nodeCopy.getChild() != null) {
					nodeCopy.removeChild(nodeCopy.getChild());
				}
				// compares two LayoutNode trees from its roots
				boolean areEqual = currentNode.getDefaultLayoutTree().equalsTo(
						nodeCopy.getRoot());

				if (areEqual) {
					// if layout_tree_start_part is equal to currentNode layout
					// location
					// we trim this default location and set the rest to the
					// location of the inserted(set) layout node
					// into the hierarchy
					LayoutNode treeCopy = currentNode.copy();
					treeCopy.setParent(null);
					setCompName(leaf.getName());
					setVarietyId(Integer.toString(leaf.getVariety()));
					setLocId(treeCopy.getLeaf().getLocId(false));
//					if (leaf.getRow() != 1 || leaf.getColumn() != 1) {
//						setLocId(treeCopy.getLeaf().getLocId(true));
//					} else {
//						setLocId(treeCopy.getLeaf().getLocId(false));
//					}
					return;
				}

				// next iteration
				currentNode = currentNode.getParent();
			}
			setCompName(leaf.getName());
			setVarietyId(Integer.toString(leaf.getVariety()));
			setLocId(leaf.getParent() == null ? null : leaf.getParent()
					.getLocId());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return false;
	}

	// PreviewElemment does not have any children. and this is a dummy method
	public void addChild(int position, Object obj) {
	}

	// override to return preview element skin name
	public String getID() {
		String ID = getElementId();
		// if there is no icon id, try to return color id
		if (null == ID) {
			return getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);
		}
		return ID;
	}

	public String getIdentifier() {
		return getID();
	}

	public void setVarietyId(String varietyId) {
		setAttribute(PreviewTagConstants.ATTR_ELEMENT_VARIETYID, varietyId);
	}

	public String getVarietyId() {
		return getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_VARIETYID);
	}

	public String getCompName() {
		return getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COMPNAME);
	}

	public void setCompName(String compName) {
		setAttribute(PreviewTagConstants.ATTR_ELEMENT_COMPNAME, compName);
	}

	public String getLocId() {
		String locId = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_LOCID);
		return StringUtils.isEmpty(locId) ? null : locId;
	}

	public void setLocId(String locId) {
		setAttribute(PreviewTagConstants.ATTR_ELEMENT_LOCID, locId);
	}

	public int getPreviewElementType() {
		SkinnableEntity entity = getSkinnableEntity();

		String entityType = entity == null ? null : entity.isEntityType();

		if (ThemeTag.ELEMENT_SOUND.equals(entityType)
				|| ThemeTag.ELEMENT_EMBED_FILE.equals(entityType)) {
			return ThemeConstants.ELEMENT_TYPE_SOUND;
		}

		return Integer.parseInt((String) attributes
				.get(ThemeConstants.ELEMENT_TYPE));
	}

	/**
	 * This method returns true if preview element has a soft mask associated
	 * with it .i.e., both the spl_mask_type and spl_mask_id attributes must be
	 * not null and greater than 0 length.
	 * 
	 * @return boolean true if this preview element has spl mask associated,
	 *         false otherwise
	 */
	public boolean hasSplMask() {
		String spl_mask_type = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SPL_MASK_TYPE);
		String spl_mask_id = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SPL_MASK_ID);
		return !StringUtils.isEmpty(spl_mask_type)
				&& !StringUtils.isEmpty(spl_mask_id);
	}

	public String getSplMask() {
		String spl_mask_id = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_SPL_MASK_ID);
		return StringUtils.isEmpty(spl_mask_id) ? null : spl_mask_id;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();

		result.append("ID =" + getID());
		result.append("CompName = " + getCompName());
		result.append("VarietyId = " + getVarietyId());

		return result.toString();
	}

	public String getRotate() {
		return getAttributeValue(PreviewTagConstants.ATTR_ROTATE);
	}

	public synchronized Set<Display> getDisplays() {
		if (displays == null) {
			Set<Display> ownDisplays = ThemePreview.getDisplays(this);
			Set<Display> parentDisplays = ((PreviewImage) getParent())
					.getDisplays();
			displays = new HashSet<Display>(parentDisplays.size());
			displays.addAll(parentDisplays);
			for (Iterator<Display> i = displays.iterator(); i.hasNext();) {
				Display parentDisplay = i.next();
				if (!ownDisplays.contains(parentDisplay)) {
					i.remove();
				} else if (screen != null) {
					boolean contains = false;
					for (Display display : screen.getDisplays()) {
						if (ownDisplays.contains(display)) {
							contains = true;
							break;
						}
					}
					if (!contains) {
						i.remove();
					}
				}
			}
		}
		return displays;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#getSkinnableEntity()
	 */
	public synchronized SkinnableEntity getSkinnableEntity() {
		// local caching, speed purpose. PreviewElement changes
		// skinnable entity it is representing
		String id = getIdentifier();
		if (null == id) {
			id = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);
		}
		if (null != element) {
			if (!element.getId().equals(id))
				element = null;
		}
		if (null == element) {
			element = ((Theme) getRoot()).getSkinnableEntity(id);
		}
		return element;
	}

	public SkinnableEntity getColorEntity() {
		SkinnableEntity entity = getSkinnableEntity();
		if (entity != null
				&& entity.isEntityType().equals(ThemeTag.ELEMENT_COLOUR)) {
			return entity;
		}
		String id = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_COLORID);
		if (id != null) {
			return ((Theme) getRoot()).getSkinnableEntity(id);
		}
		return null;
	}

	public Rectangle getBounds() {
		Layout currElementLayout = null;

		SkinnableEntity currElemEntity = getSkinnableEntity();
		try {
			if (currElemEntity != null) {
				currElementLayout = currElemEntity.getLayoutInfoForPreview(
						getDisplay(), getComponentInfo(),
						(PreviewImage) getParent());
			} else if (currElemEntity == null) {
				currElementLayout = getLayoutForPreviewNonFrame(getDisplay(),
						getComponentInfo());
			}
		} catch (ThemeException exApp) {
			PlatformCorePlugin.error(exApp);
		}
		if (currElementLayout != null) {
			return currElementLayout.getBounds();

		}
		return new Rectangle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#supportsDisplay(com.nokia.tools.platform.core.Display)
	 */
	public boolean supportsDisplay(Display display) {
		return getDisplays().contains(display);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#getDisplay()
	 */
	public Display getDisplay() {
		Display screenDisplay = screen == null ? getParent().getDisplay()
				: screen.getDisplay();
		if (supportsDisplay(screenDisplay)) {
			// always use the parent display when we're the compatiable
			return screenDisplay;
		}
		return ThemePreview.getDefaultDisplay(this);
	}

	public String getFileName() {
		String fileName = getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_IMAGE);
		return ((Theme) getRoot()).getFileName(fileName, getDisplay());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#clone()
	 */
	@Override
	public Object clone() {
		PreviewElement b = (PreviewElement) super.clone();
		// here we should not inherit the cached element from the model,
		// otherwise some tricky bugs will occur, e.g. the
		// ThemeData.getSkinnableEntity() will retrieve one from the model
		// instead of current theme
		b.element = null;
		b.displays = null;
		b.screen = null;
		return b;
	}

	public void setSupportedDisplays(String newSupportedDisplays) {
		if (newSupportedDisplays == null || newSupportedDisplays.length() == 0) {
			setAttribute(PreviewTagConstants.ATTR_DISPLAY, null);
		} else {
			setAttribute(PreviewTagConstants.ATTR_DISPLAY, newSupportedDisplays);
		}
		// this forces display list to refresh
		this.displays = null;
	}

	public Set<String> getBackgroundDependancy() {
		if (StringUtils.isEmpty(getID())) {
			return Collections.EMPTY_SET;
		}
		Theme root = (Theme) getRoot();
		Set<String> allDependancies = new HashSet<String>();
		for (Map.Entry<String, Set<String>> entry : root
				.getBackgroundDependency().entrySet()) {
			if (entry.getValue().contains(getID())) {
				allDependancies.add(entry.getKey());
			}
		}
		if (allDependancies.isEmpty()) {
			return Collections.EMPTY_SET;
		}
		Set<String> dependancy = new HashSet<String>();
		for (ThemeBasicData child : ((PreviewImage) getParent())
				.getChildren(true)) {
			PreviewElement element = (PreviewElement) child;
			if (allDependancies.contains(element.getID())) {
				dependancy.add(element.getID());
			}
		}
		return dependancy;
	}
}
