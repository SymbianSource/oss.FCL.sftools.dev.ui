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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The class defines the details of the Animated image associated with the
 * skinnable element/part
 * 
 */

public class AnimatedThemeGraphicCache extends ThemeGraphic implements
		ThemeGraphicInterface {
	protected List<ThemeGraphic> themeGraphics = new ArrayList<ThemeGraphic>();

	/**
	 * Constructor
	 */
	public AnimatedThemeGraphicCache(ThemeBasicData data) {
		super(data);
		themeGraphics = new ArrayList<ThemeGraphic>();
	}

	public void setGraphic(ThemeGraphic graphic) {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			for (int i = 0; i < themeGraphics.size(); i++) {
				ThemeGraphic tg = (ThemeGraphic) themeGraphics.get(i);
				if (tg.getAttribute(ThemeTag.UNIQUE_ID).equals(
						graphic.getAttribute(ThemeTag.UNIQUE_ID))) {
					String time = tg.getAttribute(ThemeTag.ATTR_ANIMATE_TIME);
					String seqno = tg.getAttribute(ThemeTag.ATTR_ANIMATE_SEQNO);
					graphic.setAttribute(ThemeTag.ATTR_ANIMATE_TIME, time);
					graphic.setAttribute(ThemeTag.ATTR_ANIMATE_SEQNO, seqno);
					themeGraphics.set(i, graphic);
				}
			}
		}
	}

	public ThemeGraphic getPreviewThemeGraphic() throws ThemeException {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			return (ThemeGraphic) themeGraphics.get(0);
		}
		return null;
	}

	public ThemeGraphic getActualThemeGraphic() throws ThemeException {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			return (ThemeGraphic) themeGraphics.get(0);
		}
		return null;
	}

	public ThemeGraphic getThemeGraphic(String seqNo) {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			if (Integer.parseInt(seqNo) < themeGraphics.size())
				return (ThemeGraphic) themeGraphics
						.get(Integer.parseInt(seqNo));
		}
		return null;
	}

	public ThemeGraphic getThemeGraphic() throws ThemeException {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			ThemeGraphic tg = getActualThemeGraphic();
			if (tg != null) {
				return tg;
			}
		}
		return null;
	}

	public List getDraftThemeGraphics() throws ThemeException {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			return themeGraphics;
		}
		return null;
	}

	public void removeActualThemeGraphic() throws ThemeException {
		ThemeGraphic tg = getActualThemeGraphic();
		themeGraphics.remove(tg);
	}

	public void rearrangeThemeGraphics(List animateSeqNos, int newPosition) {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			// System.out.println(" Rearranging :::: " + animateSeqNos + " , to
			// pos : " + newPosition);
			// if(newPosition >= themeGraphics.size())
			// newPosition = themeGraphics.size() - 1;
			List<ThemeGraphic> selected = new ArrayList<ThemeGraphic>();
			int beforeCount = 0;
			for (int i = 0; i < animateSeqNos.size(); i++) {
				int animateSeqNo = Integer.parseInt((String) animateSeqNos
						.get(i));
				if ((animateSeqNo < newPosition))// &&
					// (animateSeqNos.size()>1))
					beforeCount++;
				// else if((animateSeqNo < newPosition) &&
				// (animateSeqNos.size()==1))
				// beforeCount = 1;
				selected.add(themeGraphics.get(animateSeqNo));
			}
			// if(beforeCount == animateSeqNos.size())
			// beforeCount --;
			newPosition = newPosition - beforeCount;
			if (newPosition < 0)
				newPosition = 0;

			for (int i = animateSeqNos.size() - 1; i >= 0; i--) {
				int animateSeqNo = Integer.parseInt((String) animateSeqNos
						.get(i));
				themeGraphics.remove(animateSeqNo);
			}

			for (int i = selected.size() - 1; i >= 0; i--) {
				if (newPosition > themeGraphics.size())
					newPosition = themeGraphics.size();
				themeGraphics.add(newPosition, selected.get(i));
			}
			changeSequenceNosInThemeGraphics();
		}
	}

	public void clearAnimatedThemeGraphics() {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			for (int i = 0; i < this.themeGraphics.size(); i++) {
				ThemeGraphic tg = (ThemeGraphic) this.themeGraphics.get(i);
				tg.clearThemeGraphic();
			}
		}
		this.themeGraphics.clear();
	}

	/**
	 * Sets the detail of the imageLayer associated with the object
	 * 
	 * @param image The ImageLayer object containing the image details
	 */
	public void addThemeGraphic(ThemeGraphic tg) {
		if (tg == null)
			return;
		themeGraphics.add(tg);
		changeSequenceNosInThemeGraphics();
	}

	public void addThemeGraphics(List<ThemeGraphic> list) {
		if (list == null)
			return;
		this.themeGraphics = list;
		changeSequenceNosInThemeGraphics();
	}

	public List getThemeGraphics() {
		return this.themeGraphics;
	}

	public void removeThemeGraphic(ThemeGraphic tg) throws ThemeException {
		if (tg == null)
			return;
		if (themeGraphics != null) {
			themeGraphics.remove(tg);
			changeSequenceNosInThemeGraphics();
		}
	}

	public void removeThemeGraphic(int position) throws ThemeException {
		if (themeGraphics.size() > position) {
			themeGraphics.remove(position);
			changeSequenceNosInThemeGraphics();
		}
	}

	private void changeSequenceNosInThemeGraphics() {
		if ((themeGraphics != null) && (themeGraphics.size() > 0)) {
			for (int i = 0; i < themeGraphics.size(); i++) {
				ThemeGraphic tg = (ThemeGraphic) themeGraphics.get(i);
				tg.removeAttribute(ThemeTag.ATTR_ANIMATE_SEQNO);
				tg.setAttribute(ThemeTag.ATTR_ANIMATE_SEQNO, i + "");
			}
		}
	}

	public void remove() {
		this.themeGraphics.clear();
	}

	public String toString() {
		return ((ThemeGraphic) themeGraphics.get(0)).toString();
	}

	/**
	 * Method to set the Map holding the attribute name,value pair
	 * 
	 * @param mapAttribute Map with the attribute name,value pair
	 */
	public void setAttribute(Map<Object, Object> mapAttribute) {
		((ThemeGraphic) themeGraphics.get(0)).setAttributes(mapAttribute);
	}

	/**
	 * Removes the specified attribute from the attribute map
	 * 
	 * @param attr The name of the attribute to be removed
	 */
	public void removeAttribute(String attr) {
		((ThemeGraphic) themeGraphics.get(0)).removeAttribute(attr);
	}

	/**
	 * Method to get the attribute list for a node
	 */
	public Map getAttribute() {
		return ((ThemeGraphic) themeGraphics.get(0)).getAttributes();
	}

	/**
	 * Method to get the type of the graphic .
	 * 
	 * @return A string containing the type. if the value is not found then it
	 *         returns a null
	 */
	public String getType() {
		return ((ThemeGraphic) themeGraphics.get(0)).getType();
	}

	/**
	 * Method to get the value of the status .
	 * 
	 * @return A string containing the status. if the value is not found then it
	 *         returns a null
	 */
	public String getStatus() {
		return ((ThemeGraphic) themeGraphics.get(0)).getStatus();
	}

	/**
	 * Method to compare two objects
	 * 
	 * @param obj Object to be compared
	 * @return boolean true if both are equal else false
	 */

	public boolean equals(Object obj) {
		return ((ThemeGraphic) themeGraphics.get(0)).equals(obj);
	}

	/**
	 * Method to compare two Strings
	 * 
	 * @return boolean true if both are same else false
	 */
	protected boolean isSame(String value1, String value2) {
		return ((ThemeGraphic) themeGraphics.get(0)).isSame(value1, value2);
	}

	/**
	 * Sets the detail of the imageLayer associated with the object
	 * 
	 * @param image The ImageLayer object containing the image details
	 */
	public void setImageLayers(ImageLayer il) {
		((ThemeGraphic) themeGraphics.get(0)).setImageLayers(il);
	}

	public List getImageLayers() {
		return ((ThemeGraphic) themeGraphics.get(0)).getImageLayers();
	}

	public ImageLayer getImageLayer(String Name) {
		return ((ThemeGraphic) themeGraphics.get(0)).getImageLayer(Name);
	}

	public String getImageFile() {
		return ((ThemeGraphic) themeGraphics.get(0)).getImageFile();
	}

	public String getMask() {
		return ((ThemeGraphic) themeGraphics.get(0)).getMask();
	}

	public String getMask(boolean isSoft) {
		return ((ThemeGraphic) themeGraphics.get(0)).getMask(isSoft);
	}

}
