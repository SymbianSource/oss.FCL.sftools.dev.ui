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
package com.nokia.tools.platform.theme;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.Layout;

/**
 * The class hold Element information of the default skin design
 * 
 * 
 */

public class Element extends SkinnableEntity implements Cloneable {

	/**
	 * Creates an Element with a name
	 * 
	 * @param elementName name of the Element
	 */
	public Element(String elementName) {
		setThemeName(elementName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#isChildValid(java.lang.Object)
	 */
	@Override
	public boolean isChildValid(Object obj) {
		return obj instanceof Part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.ThemeBasicData#addChild(int,
	 *      java.lang.Object)
	 */
	public void addChild(int position, Object objPart) throws ThemeException {

		try {
			Part part = (Part) objPart;
			boolean duplicate = false;

			Part existingPart = null;
			// for a part - the presence of id and name is taken by the dtd
			if (link.children != null) {

				String existingPartId = null;
				String partId = null;
				for (int i = 0; i < link.children.size(); i++) {

					existingPart = (Part) link.children.get(i);

					if (part.getAttribute().containsKey(ThemeTag.ATTR_CHILD_ID)) {
						partId = part.getAttributeValue(ThemeTag.ATTR_CHILD_ID);
						existingPartId = existingPart
								.getAttributeValue(ThemeTag.ATTR_CHILD_ID);
						if (existingPartId != null) {
							if (existingPartId.equalsIgnoreCase(partId)) {
								return;
							}
						}
					} else {
						existingPartId = existingPart
								.getAttributeValue(ThemeTag.ATTR_ID);
						partId = part.getAttributeValue(ThemeTag.ATTR_ID);

						if (existingPartId.equalsIgnoreCase(partId)) {
							duplicate = true;
							break;
						}
					}
				}
			}
			if (!duplicate) {
				synchronized (this) {
					part.setParent(this);

					if (link.children == null)
						link.children = new ArrayList<Object>();

					if (position < 0) {
						link.children.add(part);
					} else {
						link.children.add(position, part);
					}
				}
			} else {
				if ((part.getAttributeValue("overwrite") != null)
						&& (part.getAttributeValue("overwrite").equals("true")))
					existingPart.overWrite(part);
				else
					existingPart.merge(part);
				// existingPart.merge(part);
			}
		} catch (Exception e) {
			// e.printStackTrace();
			throw new ThemeException("Adding part to element  failed : "
					+ e.getMessage() + " for element : " + this.getThemeName());
		}
	}

	/**
	 * Method to merge the data of an element with an existing one
	 * 
	 * @param duplicateElement Element object whose details needs to be added to
	 *            the existing element
	 */
	public void merge(ThemeBasicData dupElement) throws ThemeException {

		if ((dupElement == null) || !(dupElement instanceof Element))
			return;

		Element duplicateElement = (Element) dupElement;

		// to avoid overwriting the name given in the design file
		// by the names given in layout and skin file

		synchronized (this) {
			Object value = duplicateElement.getAttribute().remove(
					ThemeTag.ATTR_NAME);

			link.setAttribute(duplicateElement.getAttribute());

			duplicateElement.getAttribute().put(ThemeTag.ATTR_NAME, value);

			if (link.toolBox != null)
				link.toolBox.update(duplicateElement.getToolBox());

			if (duplicateElement.hasChildNodes()) {
				for (int i = 0; i < duplicateElement.children.size(); i++) {
					link.addChild((Part) duplicateElement.children.get(i));
				}
			}
		}
	}

	/**
	 * This method returns a rectangle equal having the same dimension of the
	 * element passed as a parameter.
	 * 
	 * @param element type is com.nokia.themeapp.model.Element.
	 * @return Rectangle. Retruns the rectangle obtained by taking the union of
	 *         the rectangles got from the part layoutInfo-s.
	 */
	public static Rectangle getElementLayout(Element element)
			throws ThemeException {

		Rectangle rect = null;
		// Debug.out("[Element] element.getName() :" + element.getName());
		List partInfo = element.getChildren();

		if (partInfo == null)
			return rect;

		Layout layoutInfo[] = new Layout[partInfo.size()];

		for (int i = 0; i < partInfo.size(); i++) {
			Part part = (Part) partInfo.get(i);
			layoutInfo[i] = part.getLayoutInfo();
		}

		for (int i = 0; i < layoutInfo.length; i++) {

			if (rect == null && layoutInfo[i] != null) {
				rect = new Rectangle(layoutInfo[i].L(), layoutInfo[i].T(),
						layoutInfo[i].W(), layoutInfo[i].H());

			} else if (rect != null && layoutInfo[i] != null) {

				rect = rect
						.union(new Rectangle(layoutInfo[i].L(), layoutInfo[i]
								.T(), layoutInfo[i].W(), layoutInfo[i].H()));

			}

		}
		return rect;
	}

	/**
	 * Gives the string representation of the data in the element
	 */
	public String toString() {
		StringBuffer s = new StringBuffer("ELEMENT :");

		s.append(getThemeName());
		s.append(" ID :");
		s.append(getAttributeValue(ThemeTag.ATTR_ID));
		s.append("\n\tATTR :");
		s.append(getAttribute());
		s.append("\n\tACTUAL IMAGE :");
		try {
			if (getActualThemeGraphic() != null)
				s.append(getActualThemeGraphic().getImageFile());
		} catch (Exception e) {
		}
		s.append("\n\tDRAFT IMAGES :");
		try {
			List l = getDraftThemeGraphics();
			if (l != null) {
				s.append("\n\t size : " + l.size());
				for (int i = 0; i < l.size(); i++)
					s.append("\n\t" + ((ThemeGraphic) l.get(i)).getImageFile());
			}
		} catch (Exception e) {
		}

		if (this.isShown()) {
			s.append("\n\tLAYOUT :");

			try {
				s.append(getLayoutInfo());
			} catch (ThemeException e1) {
				PlatformCorePlugin.error(e1);
			}
		}

		return s.toString();
	}

	/**
	 * Method to clone the Element object
	 * 
	 * @return Object object of the cloned Element
	 */
	public Object clone() {

		Element elementObj = (Element) super.clone();

		// clone the attributes Map
		HashMap<Object, Object> m = new HashMap<Object, Object>();
		m.putAll(this.getAttribute());
		elementObj.setAttribute(m);

		// clone the toolBox
		if (this.toolBox != null)
			elementObj.toolBox = (ToolBox) (this.toolBox.clone());

		// clone the themeGraphics
		if (this.themeGraphics != null) {
			List<Object> diList = this.themeGraphics;
			ArrayList<Object> al = new ArrayList<Object>(diList);
			al = (ArrayList) al.clone();
			for (int j = 0; j < al.size(); j++) {
				ThemeGraphic tg = (ThemeGraphic) al.get(j); // (ThemeGraphic)
				// al.get(j);
				tg = (ThemeGraphic) tg.clone();
				tg.setData(elementObj);
				al.set(j, tg);
			}
			elementObj.themeGraphics = (List<Object>) al;
		}
		return (Object) elementObj;
	}

	protected void copyProperties(Element element){
		super.copyProperties(element);
	}
} // end of the Element class
