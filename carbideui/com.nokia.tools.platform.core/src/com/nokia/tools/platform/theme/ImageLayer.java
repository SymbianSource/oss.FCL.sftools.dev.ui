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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.theme.preview.PreviewElement;

/**
 * The class defines the details of the image associated with the skinnable
 * element/part
 * 
 * 
 */
public class ImageLayer implements Cloneable {

	private Map<Object, Object> attributes;
	protected List<Object> layerEffects;
	private boolean isSelected = true;
	private ThemeGraphic tg = null;

	/**
	 * Constructor
	 */
	public ImageLayer(ThemeGraphic tg) {
		attributes = new HashMap<Object, Object>();
		layerEffects = new ArrayList<Object>();
		this.tg = tg;
	}

	public ThemeGraphic getThemeGraphic() {
		return this.tg;
	}

	/**
	 * Method to set the Map holding the attribute name,value pair
	 * 
	 * @param mapAttribute Map with the attribute name,value pair
	 */
	public void setAttributes(Map<Object, Object> mapAttribute) {
		attributes.putAll(mapAttribute);
	}

	/**
	 * Method to set the given attribute
	 * 
	 * @param name The name of the attribute
	 * @param value The value of the attribute
	 */
	public void setAttribute(String name, String value) {
		synchronized (this) {
			attributes.put(name, value);
		}
	}

	public String toString() {
		return this.attributes.toString();
	}

	/**
	 * Removes the specified attribute from the attribute map
	 * 
	 * @param attr The name of the attribute to be removed
	 */
	public void removeAttribute(String attr) {
		synchronized (this) {
			attributes.remove(attr);
		}
	}

	/**
	 * Method to get the attribute list for a node
	 */
	public Map<Object, Object> getAttributes() {
		return attributes;
	}

	/**
	 * Method to get the value of an attribute.
	 * 
	 * @param attrName The name of the attribute whose value is required.
	 * @return A string containing the value of the attrName attribute. If the
	 *         attribute is not found then it returns a null
	 */
	public String getAttribute(String attrName) {
		return (String) attributes.get(attrName);
	}

	/**
	 * Method to get the In Between Effect of Layers .
	 * 
	 * @return A string containing Effect. if the value is not found then it
	 *         returns a null
	 */
	public String getInBetweenEffect() {
		return (String) attributes.get(ThemeTag.IN_EFFECT);
	}

	public String getTile() {
		if (this.attributes.containsKey(ThemeTag.ATTR_TILE)) {
			if (this.attributes.get(ThemeTag.ATTR_TILE).equals(
					ThemeTag.ATTR_TILEX))
				return ThemeTag.ATTR_TILEX;
			else if (this.attributes.get(ThemeTag.ATTR_TILE).equals(
					ThemeTag.ATTR_TILEY))
				return ThemeTag.ATTR_TILEY;
			else
				return ThemeTag.ATTR_TILE;
		}
		return null;
	}

	public void setTile(String tile) {
		if (this.attributes.containsKey(ThemeTag.ATTR_TILE)) {
			this.attributes.remove(ThemeTag.ATTR_TILE);
			this.attributes.put(ThemeTag.ATTR_TILE, tile);
		} else {
			this.attributes.put(ThemeTag.ATTR_TILE, tile);
		}
	}

	public void removeTile() {
		if (this.attributes.containsKey(ThemeTag.ATTR_TILE)) {
			this.attributes.remove(ThemeTag.ATTR_TILE);
		}
	}

	/**
	 * Method to get Abs path with filename.
	 * 
	 * @return A string containing complete path.
	 */
	public String getFileName(Theme s60) {
		return getFileName(s60, (Display) null);
	}

	public String getFileName(Theme s60, PreviewElement element) {
		return getFileName(s60, element == null ? null : element.getDisplay());
	}
	
	public String getFileName(Theme model,Theme s60, PreviewElement element) {
		return getFileName(model,s60, element == null ? null : element.getDisplay());
	}
	
	public String getFileName(Theme model,Theme s60, Display display) {
		String fileName = getAttribute(ThemeTag.FILE_NAME);

		if (display == null && tg != null) {
			Set<Display> displays = tg.getData().getDisplays();
			if (displays.remove(s60.getDisplay())) {
				// matches the current theme so check this first
				String name = model.getFileName(fileName, s60.getDisplay());
				if (name != null) {
					return name;
				}
			}
			for (Display d : displays) {
				// some elements are only available in certain displays,
				// therefore we consult the preferred display of the given
				// element, this is matching the layout
				String name = model.getFileName(fileName, d);
				if (name != null) {
					return name;
				}
			}
		}

		//return s60.getFileName(fileName, display);
		return model.getFileName(fileName, display);
	}


	public String getFileName(Theme s60, Display display) {
		String fileName = getAttribute(ThemeTag.FILE_NAME);

		if (display == null && tg != null) {
			Set<Display> displays = tg.getData().getDisplays();
			if (displays.remove(s60.getDisplay())) {
				// matches the current theme so check this first
				String name = s60.getFileName(fileName, s60.getDisplay());
				if (name != null) {
					return name;
				}
			}
			for (Display d : displays) {
				// some elements are only available in certain displays,
				// therefore we consult the preferred display of the given
				// element, this is matching the layout
				String name = s60.getFileName(fileName, d);
				if (name != null) {
					return name;
				}
			}
		}

		return s60.getFileName(fileName, display);
	}

	/**
	 * Method to get MaskFileName for a given layer.
	 * 
	 * @return A string containing complete path.
	 */
	public String getMaskFileName(Theme s60, boolean softMask) {
		return getMaskFileName(s60, softMask, (Display) null);
	}

	public String getMaskFileName(Theme s60, boolean softMask,
			PreviewElement element) {
		return getMaskFileName(s60, softMask, element == null ? null : element
				.getDisplay());
	}

	public String getMaskFileName(Theme s60, boolean softMask, Display display) {
		String fileName;
		if (softMask)
			fileName = this.getAttribute(ThemeTag.ATTR_SOFTMASK);
		else
			fileName = this.getAttribute(ThemeTag.ATTR_HARDMASK);

		return s60.getFileName(fileName, display);
	}

	/**
	 * Sets the detail of the layerEffects associated with the object
	 * 
	 * @param image The LayerEffect object containing the image details
	 */

	public void setLayerEffects(LayerEffect le) throws ThemeException {

		if (le == null)
			return;
		layerEffects.add(le);
	}

	public List<Object> getLayerEffects() throws ThemeException {
		return layerEffects;
	}

	public LayerEffect getLayerEffects(String Name) {
		for (int i = 0; i < layerEffects.size(); i++) {
			LayerEffect l = (LayerEffect) layerEffects.get(i);
			if (l.getEffetName().equalsIgnoreCase(Name))
				return l;
		}
		return null;
	}

	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected The isSelected to set.
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public void addEffect(Map map) {
		try {
			LayerEffect le = new LayerEffect(this.tg);
			le.setEffectName(map.get(ThemeTag.ATTR_NAME).toString());
			Set set = map.keySet();
			Iterator iter = set.iterator();
			while (iter.hasNext()) {
				String name = (String) iter.next();
				if (map.get(name) instanceof ParameterModel) {
					ParameterModel p = (ParameterModel) map.get(name);
					le.getParameterModels().add(p);
					le.getAttributes().put(name, p);
				}
			}
			getLayerEffects().add(le);
		} catch (ThemeException e) {
			PlatformCorePlugin.error(e);
		}
	}

	public void DeleteEffect(LayerEffect le) throws ThemeException {
		getLayerEffects().remove(le);
	}

	/*
	 * returns true if present Layer Name is Background
	 */
	public boolean isBackground() {
		boolean isBackground = false;
		if ((this.attributes.containsKey(ThemeTag.ATTR_NAME))
				&& (this.attributes.get(ThemeTag.ATTR_NAME) != null)
				&& ((this.attributes.get(ThemeTag.ATTR_NAME).toString())
						.equalsIgnoreCase(ThemeTag.ELEMENT_BACKGROUND_NAME)))
			isBackground = true;
		return isBackground;
	}

	/*
	 * returns false if current layers file name is null or filename attribute
	 * is not there
	 */
	public boolean hasImage() {
		return attributes.get(ThemeTag.FILE_NAME) != null;
	}

	public void clearImageLayer() {
		if (this.layerEffects != null) {
			for (int i = 0; i < this.layerEffects.size(); i++) {
				LayerEffect le = (LayerEffect) this.layerEffects.get(i);
				le.clearLayerEffect();
			}
		}
		this.layerEffects.clear();
	}

	/**
	 * Method to clone the ImageLayer object
	 * 
	 * @return Object object of the clone ThemeGraphic
	 */
	public Object clone1(ThemeGraphic obj1) throws CloneNotSupportedException {
		ImageLayer obj = null;
		obj = (ImageLayer) super.clone();
		obj.tg = obj1;
		// clone the attributes Map
		HashMap<Object, Object> m = new HashMap<Object, Object>(this.attributes);
		m = (HashMap) m.clone();
		obj.attributes = (Map<Object, Object>) m;
		List<Object> les = this.layerEffects;
		ArrayList<Object> al = new ArrayList<Object>(les);
		al = (ArrayList) al.clone();
		for (int j = 0; j < al.size(); j++) {
			LayerEffect le = (LayerEffect) al.get(j); // (ThemeGraphic)
			// al.get(j);
			le = (LayerEffect) le.clone1(obj1);
			al.set(j, le);
		}
		obj.layerEffects = (List<Object>) al;
		return (Object) obj;
	}

	public void setEffectsList(ArrayList<Object> list) {
		layerEffects = list;
	}

	public boolean isApplyGraphics() {
		if (getLayerEffects(EffectConstants.APPLYCOLOR) == null)
			return true;
		else
			return false;
	}
}
