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
/*
 * File Name Part.java Description File contains class to hold Part information
 * for the Series 60 Skins Creation Application project. 
 */

package com.nokia.tools.platform.theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewImage;

/**
 * holds the information of a Part
 */

public class Part extends SkinnableEntity {
	/**
	 * Creates a Part with a name
	 */
	public Part(String partName) {
		setThemeName(partName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.SkinnableEntity#getPhoneThemeGraphic()
	 */
	public ThemeGraphic getPhoneThemeGraphic() throws ThemeException {
		ThemeGraphic tg = getActualThemeGraphic();
		if (tg == null && getParent().isAnyChildDone()) {
			
			ThemeGraphic tgDefault = getThemeGraphic();
			return tgDefault;
		}
		return tg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.SkinnableEntity#getDefaultLocId(com.nokia.tools.platform.core.Display)
	 */
	public String getDefaultLocId(Display display) {
		//Let's ignore loc id if we are populating icon view page
		if (getParent().getAttributeValue(ThemeConstants.ATTR_USE_LOCID) != null) return null;
		
		PreviewImage screen = ((Theme) getRoot()).getThemePreview()
				.getPreviewImageForElem(getParent(), false);
		if (screen != null) {
			PreviewElement pelement = screen.getPreviewElement(display,
					getParent().getIdentifier());
			if (pelement != null) {
				// get the loc_id from the preview element
				return pelement.getLocId();
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.SkinnableEntity#getLayoutInfo(com.nokia.tools.platform.core.Display,
	 *      int, java.lang.String, java.lang.String)
	 */
	@Override
	public Layout getLayoutInfo(Display display, int varietyId, String loc_id,
			String parentDefaultScreen) throws ThemeException {
		if (display == null) {
			display = getDisplay();
		}

		String skinElementId = getLayoutId();
		if (varietyId < 0) {
			varietyId = Integer.MIN_VALUE;
		}

		ComponentInfo layoutComponent = getComponentInfo(display);
		if (layoutComponent == null) {
			throw new ThemeException("Layout idmappings not available for "
					+ skinElementId);
		}
		if (loc_id != null) {
			if (getParent().getAttributeValue(ThemeConstants.ATTR_USE_LOCID) == null){			
				layoutComponent.setLocId(loc_id);				
			}
			
		}
		try {
			getLayoutContext(display).calculate(layoutComponent);
		} catch (Throwable e) {
			PlatformCorePlugin.error(e);
			throw new ThemeException(e);
		}
		return layoutComponent.getLayout();

	}

	public Layout getLayoutInfo(Display display, PreviewElement parent)
			throws ThemeException {
		if (getParent().getAttributeValue(ThemeConstants.ATTR_USE_LOCID) != null) return null; 
		if (display == null) {
			if (parent == null) {
				display = getDisplay();
			} else {
				display = parent.getDisplay();
			}
		}
		String loc_id = null;
		if (parent != null) {
			loc_id = parent.getLocId();
			ComponentInfo component = parent.getComponentInfo();
			loc_id = (loc_id != null ? loc_id : "") + "<LC>"
					+ component.getName() + "@V:" + component.getVariety()
					+ "</LC>";
		}
		return getLayoutInfo(display, loc_id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.SkinnableEntity#getLayoutId()
	 */
	@Override
	public String getLayoutId() {
		return getIdentifier();
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

	// Part dont have any children. and this is a dummy method
	public void addChild(int position, Object obj) throws ThemeException {
	}

	// Part dont have any children. and this is a dummy method
	public void removeChild(Object obj) {
		// dummy method
	}

	// Part dont have any children. the toolbox is initialized
	public void removeAll() {
		link.toolBox = new ToolBox();
	}

	/**
	 * Method to merge the data of a part with an existing one
	 * 
	 * @param duplicatePart Part object whose details needs to be added to the
	 *            existing Part
	 */
	public void merge(ThemeBasicData dupPart) throws ThemeException {

		if ((dupPart == null) || !(dupPart instanceof Part))
			return;

		// Debug.out(this," merge happens here in part ******************");

		Part duplicatePart = (Part) dupPart;

		// to avoid overwriting the name given in the design file
		// by the names given in layout and skin file
		synchronized (this) {
			Object value = duplicatePart.getAttribute().remove(
					ThemeTag.ATTR_NAME);

			link.setAttribute(duplicatePart.getAttribute());

			duplicatePart.getAttribute().put(ThemeTag.ATTR_NAME, value);

			if (link.toolBox != null)
				link.toolBox.update(duplicatePart.getToolBox());
		}
		// Debug.out(this,this.getLayoutInfo());
	}

	/**
	 * Gives the string representation of the data in the part
	 */
	public String toString() {
		StringBuffer s = new StringBuffer("PART :");
		s.append(getThemeName());
		s.append(" ID :");
		s.append(getAttributeValue(ThemeTag.ATTR_ID));
		s.append("\n\tATTR :");
		s.append(getAttribute());
		s.append("\n\tACTUAL IMAGE :");
		try {
			// if(getActualImage() != null)
			
		} catch (Exception e) {
			// Debug.out(this,"Info before exception occured : " +
			// s.toString());
			// Debug.out(this,"Exception for actual image : " + e.getMessage());
			// System.out.println("******************** excep for actual : " +
			// e.getMessage() + "---------" + s.toString());
		}
		s.append("\n\tDRAFT IMAGES :");
		try {
			List l = getDraftThemeGraphics();
			if (l != null) {
				s.append("\n\t size : " + l.size());
				for (int i = 0; i < l.size(); i++)
					s.append("\n\t" + (ThemeGraphic) l.get(i));

			}
		} catch (Exception e) {
			// Debug.out(this,"Info before exception occured : " +
			// s.toString());
			// Debug.out(this,"Exception for draft image : " + e.getMessage());
			// //System.out.println("excep for draft : " + e.getMessage() +
			// "---------" + s.toString());
		}
		s.append("\n\tLAYOUT :");
		// s.append (getLayoutInfo());

		return s.toString();
	}

	/**
	 * Method to clone the Part object
	 * 
	 * @return Object object of the cloned Element
	 */
	public Object clone() {

		Part partObj = (Part) super.clone();

		// clone the attributes Map
		HashMap<Object, Object> m = new HashMap<Object, Object>(this.attributes);

		m = (HashMap) m.clone();

		partObj.attributes = (Map<Object, Object>) m;

		// clone the toolBox
		if (partObj.toolBox != null)
			partObj.toolBox = (ToolBox) (this.toolBox.clone());

		/*
		 * if(this.actualImage != null) { partObj.actualImage =
		 * (SkinImage)(this.actualThemeGraphic.clone()); }
		 */

		// clone the themeGraphics
		if (this.themeGraphics != null) {
			List<Object> diList = this.themeGraphics;
			ArrayList<Object> al = new ArrayList<Object>(diList);
			al = (ArrayList) al.clone();
			for (int j = 0; j < al.size(); j++) {
				if (!this.isEntityType().equals(ThemeTag.ELEMENT_BMPANIM)) {
					ThemeGraphic tg = (ThemeGraphic) al.get(j); // (ThemeGraphic)
					// al.get(j);
					tg = (ThemeGraphic) tg.clone();
					tg.setData(partObj);
					al.set(j, tg);
				} else {
					AnimatedThemeGraphic atg = (AnimatedThemeGraphic) al.get(j);
					atg = (AnimatedThemeGraphic) atg.clone();
					atg.setData(partObj);
					al.set(j, atg);
				}
			}
			partObj.themeGraphics = (List<Object>) al;
		}

		if (this.toolBox != null) {
			partObj.setToolBox((ToolBox) this.toolBox.clone());
		}

		return (Object) partObj;

	}

	@Override
	public boolean isAnyChildDone() {
		try {
			if (getActualThemeGraphic() != null) {
				return true;
			}
		} catch (ThemeException e) {
			PlatformCorePlugin.error(e);
		}
		boolean result = super.isAnyChildDone();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.SkinnableEntity#getDisplay()
	 */
	@Override
	public Display getDisplay() {
		return ((SkinnableEntity) getParent()).getDisplay();
	}
}
