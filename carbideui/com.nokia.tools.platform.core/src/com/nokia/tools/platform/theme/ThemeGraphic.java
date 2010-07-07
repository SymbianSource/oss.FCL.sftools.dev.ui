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

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.core.Display;
import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.preview.PreviewElement;

public class ThemeGraphic implements Cloneable {
	private Map<Object, Object> attributes;

	protected List<ImageLayer> imageLayers;

	private boolean animationStarted = false;

	private ThemeBasicData data;

	public String toString() {

		StringBuffer data = new StringBuffer();

		data.append("\nATTRIBUTES\n\t");
		data.append(attributes);

		data.append("\nImage Layer List\n\t");
		data.append(imageLayers);
		return data.toString();
	}

	/**
	 * Constructor
	 */
	public ThemeGraphic(ThemeBasicData data) {
		attributes = new HashMap<Object, Object>();
		imageLayers = new ArrayList<ImageLayer>();
		setData(data);
	}

	/**
	 * @param data the data to set
	 */
	public void setData(ThemeBasicData data) {
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public ThemeBasicData getData() {
		return data;
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

	public void clearAttributes() {
		attributes.clear();
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
	 * Method to get the type of the graphic .
	 * 
	 * @return A string containing the type. if the value is not found then it
	 *         returns a null
	 */
	public String getType() {
		return (String) attributes.get(ThemeTag.ATTR_TYPE);
	}

	/**
	 * Method to get the value of the status .
	 * 
	 * @return A string containing the status. if the value is not found then it
	 *         returns a null
	 */
	public String getStatus() {
		return (String) attributes.get(ThemeTag.ATTR_STATUS);
	}

	/**
	 * Method to compare two objects
	 * 
	 * @param obj Object to be compared
	 * @return boolean true if both are equal else false
	 */

	public boolean equals(Object obj) {

		if (!isSame(this.getAttribute(ThemeTag.ATTR_STATUS),
				((ThemeGraphic) obj).getAttribute(ThemeTag.ATTR_STATUS))) {
			return false;
		}

		return true;
	}

	/**
	 * Method to compare two Strings
	 * 
	 * @return boolean true if both are same else false
	 */
	protected boolean isSame(String value1, String value2) {

		if ((value1 == null && value2 != null)
				|| (value1 != null && value2 == null)) {
			return false;
		} else if (value1 == null && value2 == null) {
			return true;
		} else {
			if (value1.equalsIgnoreCase(value2))
				return true;
		}

		return false;
	}

	/**
	 * Sets the detail of the imag associated with the object
	 * 
	 * @param image The ImageLayer object containing the image details
	 */
	public void setImageLayers(ImageLayer il) {

		if (il == null)
			return;
		if (il.getAttribute(ThemeTag.ATTR_NAME) != null
				&& il.getAttribute(ThemeTag.ATTR_NAME).equalsIgnoreCase(
						ThemeTag.ELEMENT_BACKGROUND_NAME))
			imageLayers.add(0, il);
		else
			imageLayers.add(il);
	}

	public List<ImageLayer> getImageLayers() {
		return this.imageLayers;
	}

	public ImageLayer getImageLayer(String Name) {
		if (Name == null)
			return null;
		for (int i = 0; i < imageLayers.size(); i++) {
			ImageLayer iml = (ImageLayer) getImageLayers().get(i);
			// attribute ATTR_NAME can be null? check needed
			if (iml.getAttribute(ThemeTag.ATTR_NAME) == null)
				return null;
			if (iml.getAttribute(ThemeTag.ATTR_NAME).equalsIgnoreCase(Name))
				return iml;
		}
		return null;
	}

	public void clearThemeGraphic() {
		if (((Theme) data.getRoot()).isModel()) {
			PlatformCorePlugin
					.error("Cleared model graphics, should never happen: "
							+ this);
		}
		if (this.imageLayers != null) {
			for (int i = 0; i < this.imageLayers.size(); i++) {
				ImageLayer il = (ImageLayer) this.imageLayers.get(i);
				il.clearImageLayer();
			}
		}
		this.imageLayers.clear();
	}

	/**
	 * Method to clone the ThemeGraphic object
	 * 
	 * @return Object object of the clone ThemeGraphic
	 */
	public Object clone() {
		try {
			ThemeGraphic obj = null;
			obj = (ThemeGraphic) super.clone();
			// clone the attributes Map
			HashMap<Object, Object> m = new HashMap<Object, Object>(
					this.attributes);
			m = (HashMap) m.clone();
			obj.attributes = (Map<Object, Object>) m;
			List<ImageLayer> les = this.imageLayers;
			ArrayList<ImageLayer> al = new ArrayList<ImageLayer>(les);
			al = (ArrayList) al.clone();
			for (int j = 0; j < al.size(); j++) {
				ImageLayer il = (ImageLayer) al.get(j); // (ThemeGraphic)
				// al.get(j);
				il = (ImageLayer) il.clone1(obj);
				al.set(j, il);
			}
			obj.imageLayers = (List<ImageLayer>) al;
			return (Object) obj;
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	
	public String getImageFile() {
		if (this.getImageLayers() != null) {
			ImageLayer il = (ImageLayer) this.getImageLayers().get(0);
			return (String) il.getFileName((Theme) data.getRoot());
		}
		return null;
	}

	public String getMask() {
		return getMask(false);
	}

	
	public String getMask(boolean isSoft) {
		if (this.getImageLayers() != null) {
			ImageLayer il = (ImageLayer) this.getImageLayers().get(0);
			if (isSoft)
				return (String) il.getAttribute(ThemeTag.ATTR_SOFTMASK);
			else
				return (String) il.getAttribute(ThemeTag.ATTR_HARDMASK);
		}
		return null;
	}

	/*
	 * @returns boolean true if anyof the layer contains SVG image
	 */

	public boolean isSVG() {
		boolean isSVG = false;
		if (this.imageLayers != null) {
			for (int i = 0; i < this.imageLayers.size(); i++) {
				ImageLayer il = (ImageLayer) this.imageLayers.get(i);
				String fileName = il.getFileName((Theme) data.getRoot());
				if (fileName != null) {
					fileName = fileName.toLowerCase();
					if (fileName.endsWith(ThemeTag.SVG_FILE_EXTN)) {
						isSVG = true;
						break;
					}
				}
			}
		}
		return isSVG;
	}

	public boolean hasBackground() {
		for (int i = 0; i < this.getImageLayers().size(); i++) {
			ImageLayer il = (ImageLayer) this.getImageLayers().get(i);
			if (il.isBackground())
				return true;
		}
		return false;
	}

	public boolean doSetGraphic() {
		try {
			for (int i = 0; i < this.getImageLayers().size(); i++) {
				ImageLayer il = (ImageLayer) this.getImageLayers().get(i);
				if (il.isBackground()) {
					List layerEffects = il.getLayerEffects();
					if (layerEffects != null && layerEffects.size() > 0)
						return true;
				} else {
					if (((il.hasImage()))
							|| ((il.getLayerEffects() != null && il
									.getLayerEffects().size() > 0)))
						return true;
				}
			}
		} catch (ThemeException e) {
			PlatformCorePlugin.error(e);
		}
		return false;
	}

	public RenderedImage getProcessedImage(SkinnableEntity entity,
			String locid, boolean SoftMask, boolean applyMask) {
		try {
			Layout layout = entity.getLayoutInfo(locid);
			return getProcessedImage(entity, layout, SoftMask, applyMask);
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return null;
	}

	private RenderedImage getProcessedImage(SkinnableEntity entity,
			Layout layout, boolean softMask, boolean applyMask, int elementParam)
			throws ThemeException {
		return getProcessedImage(entity, layout, ((Theme) entity.getRoot())
				.getDisplay(), softMask, applyMask, elementParam, null);
	}

	public RenderedImage getProcessedImage(SkinnableEntity entity,
			Layout layout, Display display, boolean softMask,
			boolean applyMask, int elementParam, PreviewElement preElem)
			throws ThemeException {

		String masterId = (String) entity.getAttribute().get(
				ThemeTag.ATTR_MASTERID);
		if (masterId != null) {
			entity = ((Theme) entity.getRoot()).getSkinnableEntity(masterId);
		}

		Theme theme = (Theme) entity.getRoot();
		String entityType = entity.isEntityType();
		if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_SOUND)
				|| entityType.equalsIgnoreCase(ThemeTag.ELEMENT_EMBED_FILE)) {
			String fileName = ((ImageLayer) getImageLayers().get(0))
					.getFileName(theme, preElem);
			String ext;
			if (fileName == null) {
				
				if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_SOUND)) {
					ext = "wav";
				} else {
					ext = "swf";
				}
			} else {
				ext = fileName.substring(fileName.lastIndexOf('.') + 1,
						fileName.length());
			}
			return ThemeappJaiUtil.getImageForFile(ext);
		}
		if (entityType.equalsIgnoreCase(ThemeTag.ELEMENT_COLOUR)) {
			// get the correct Color
			Color c = null;
			String colour = ((ImageLayer) getImageLayers().get(0))
					.getAttribute(ThemeTag.ATTR_COLOUR_RGB);
			if (colour == null) {
				colour = ((ImageLayer) getImageLayers().get(0))
						.getAttribute(ThemeTag.ATTR_COLOUR_IDX);
				c = Color.decode(colour);
			} else {
				c = ColorUtil.toColor(colour);
			}
			return CoreImage.create().init(layout.W(), layout.H(), c).getAwt();
		}
		List imageLayerList = getImageLayers();
		if (imageLayerList == null)
			return null;

		ILayeredImageCompositor compositor = ((Theme) entity.getRoot())
				.getLayeredImageCompositor(this);
		RenderedImage img = compositor.ProcessList(entity, layout, display,
				imageLayerList, softMask, applyMask, elementParam, preElem);

		return img;

	}

	private RenderedImage getProcessedImage(SkinnableEntity entity,
			Layout layout, boolean softMask, boolean applyMask)
			throws ThemeException {
		return getProcessedImage(entity, layout, softMask, applyMask, 3);
	}

	public RenderedImage getProcessedImage(SkinnableEntity entity,
			Layout layout, boolean softMask) throws ThemeException {
		return getProcessedImage(entity, layout, softMask, true);
	}

	// /**
	// * @return Returns the isSelectedLayer.
	// */
	public void addLayer(Map<Object, Object> map) {
		ImageLayer iml = new ImageLayer(this);
		iml.setAttributes(map);
		setImageLayers(iml);
	}

	public void Deletelayer(ImageLayer iml) throws ThemeException {
		getImageLayers().remove(iml);
	}

	public boolean isMorphedGraphic() {
		try {
			for (int i = 0; i < imageLayers.size(); i++) {
				ImageLayer iml = (ImageLayer) imageLayers.get(i);
				for (int j = 0; j < iml.getLayerEffects().size(); j++) {
					LayerEffect le = (LayerEffect) iml.getLayerEffects().get(j);
					Map attr = le.getAttributes();
					Set set = attr.keySet();
					Iterator iter = set.iterator();
					while (iter.hasNext()) {
						String name = (String) iter.next();
						if (attr.get(name) instanceof ParameterModel) {
							ParameterModel model = (ParameterModel) attr
									.get(name);
							if (model.isAnimatedModel())
								return true;
						}
					}
				}
			}
		} catch (Exception e) {
			PlatformCorePlugin.error(e);
		}
		return false;

	}

	public boolean isAnimationStarted() {
		return animationStarted;
	}

	public void setAnimationStarted(boolean animationStarted) {
		this.animationStarted = animationStarted;
	}

}
