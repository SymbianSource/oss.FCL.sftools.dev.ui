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
package com.nokia.tools.media.utils.layers;

import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.media.utils.svg.ColorGroup;

public interface ILayer {
	/**
	 * Max number of characters the layer name can have.
	 */
	int LAYER_NAME_LIMIT = 15;

	
	public static final String SCALE_MODE_ATTR = "ms";

	/**
	 * when layer image is changed
	 */
	public static final String PROPERTY_IMAGE = "LAYER_IMAGE";

	/**
	 * when object state changed, or child effect state changed
	 */
	public static final String PROPERTY_STATE = "STATE";

	/**
	 * when animation properties of child effect changed
	 */
	public static final String PROPERTY_ANIMATION_STATE = "ANIMATION_STATE";

	/**
	 * when childs added / removed
	 */
	public static final String PROPERTY_STRUCTURE_CHANGE = "STRUCTURE_CHANGE";

	/**
	 * effect order property
	 */
	public static final String PROPERTY_EFFECT_ORDER = "EFFECT_ORDER";

	/**
	 * Change order of effect
	 * 
	 * @param effect
	 * @param delta
	 */
	public void changeEffectOrder(ILayerEffect effect, int delta);

	/**
	 * returns given effect if present or null
	 * 
	 * @param name
	 * @return
	 */
	public ILayerEffect getEffect(String name);

	/**
	 * @return Returns enabled layer effects
	 */
	public List<ILayerEffect> getSelectedLayerEffects();

	/**
	 * @return Returns all layer effects
	 */
	public List<ILayerEffect> getLayerEffects();

	/**
	 * @return Returns disabled layer effects
	 */
	// public List<ILayerEffect> getDisabledLayerEffects();
	/**
	 * Return's list of which are available for layer in current state. state.
	 */
	public List<ILayerEffect> getAvailableLayerEffects();

	/**
	 * Adds new effect to layer. If effect already exists in layer but is
	 * disabled, then is re-enabled.
	 */
	public ILayerEffect addLayerEffect(String effectName);

	/**
	 * removes effect by name
	 * 
	 * @param effectName
	 */
	public void removeLayerEffect(String effectName);

	/**
	 * @return Returns the image.
	 * @uml.property name="image"
	 * @uml.associationEnd inverse="layers:com.nokia.tools.themeeditor.layers.api.IImage"
	 * @uml.association name="layers"
	 */
	public IImage getParent();

	/**
	 * Returns TRUE if this layer is background layer
	 */
	public boolean isBackground();

	/**
	 * Returns TRUE if this layer enabled
	 */
	public boolean isEnabled();

	/**
	 * sets enabled state
	 * 
	 * @param enb
	 */
	public void setEnabled(boolean e);

	/**
	 * return processed layer image with on-layer effect applied
	 * 
	 * @return
	 */
	public RenderedImage getProcessedImage();

	/**
	 * returns image from given time offset - layer must be animated and
	 * timeOffset must be smaller than
	 * 
	 * @param timeOffset
	 * @param preview
	 * @return
	 */
	public RenderedImage getProcessedImage(long timeOffset, boolean preview);

	/**
	 * return original image
	 * 
	 * @return
	 */
	public RenderedImage getRAWImage();

	/**
	 * return original image
	 * 
	 * @return
	 */
	public RenderedImage getMaskImage();

	/**
	 * return icon preview image
	 * 
	 * @return
	 */
	public ImageDescriptor getLayerIconImage();

	/**
	 * return icon preview image for image
	 * 
	 * @return
	 */
	public ImageDescriptor getLayerRawImageIcon();

	/**
	 * return icon preview image for mask
	 * 
	 * @return
	 */
	public ImageDescriptor getLayerMaskImageIcon();

	/**
	 * returns layer name
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * returns true if layer has effect and is active
	 * 
	 * @param name
	 * @return
	 */
	public boolean isEffectSelected(String name);

	/**
	 * returns layer index.
	 * 
	 * @return
	 */
	public int getOrderIndex();

	/**
	 * renames layer. throw exception if new name is not valid.
	 * 
	 * @param newName
	 * @throws Exception
	 */
	public void setName(String newName) throws Exception;

	public boolean isSvgImage();

	public boolean isBitmapImage();

	public boolean hasMask();

	public boolean hasImage();

	public boolean supportMask();

	public boolean supportSoftMask();

	public void clearImage();

	public void clearMask();

	public void clearLayer();

	public void addPropertyListener(PropertyChangeListener ps);

	public void removePropertyChangeListener(PropertyChangeListener ps);

	/* ************** Animation specific ****************** */

	/**
	 * true if layer has animated effects
	 * 
	 * @return
	 */
	public boolean isAnimated();

	/**
	 * true when image has animated effects with given timing model
	 * 
	 * @return
	 */
	public boolean isAnimatedFor(TimingModel timingType);

	public boolean isAnimatedInPreview();

	public void setAnimatedInPreview(boolean animatedInPreview);

	/**
	 * @return returns anim duration according to timing model that apply for
	 *         this layer or 0 if is not animated at all.
	 */
	public long getAnimationDuration();

	/**
	 * returns animation length by timing type - for realtime, or relative
	 * effects
	 * 
	 * @param timingType
	 * @return
	 */
	public long getAnimationDuration(TimingModel timingType);

	/**
	 * returns duration for given timespan in relative timing mode.
	 * 
	 * @param span
	 * @return
	 */
	public long getAnimationDuration(TimeSpan span);

	/**
	 * Sets start of animation timeline on normal datetime.
	 * 
	 * @param time
	 */
	public void setAnimationStartLocation(long time);

	
	public String getStretchMode();

	/**
	 * works on underlying ImageLayer
	 * 
	 * @return
	 */
	public Map getAttributes();

	/**
	 * works on underlying ImageLayer
	 * 
	 * @return
	 */
	public void setAttributes(Map<Object, Object> attrs);

	public String getMaskFileName(boolean makeAbsolute);

	public String getFileName(boolean makeAbsolute);

	public void copyImageToClipboard(Clipboard clip);

	public void paste(Object data) throws Exception;

	public void pasteMask(Object data) throws Exception;

	public void disableNonDefaultEffects();

	public boolean isPasteImageAvailable(Clipboard clip);

	public boolean isPasteImageAvailable(Object object);

	/**
	 * <COLOR, #of occurences of color>
	 * @return
	 */
	public Map<RGB, Integer> getColors();

	/**
	 * List of all color groups for given theme
	 * @return
	 */
	public List<ColorGroup> getGroups();

	public void setStretchMode(String stretchMode);
}
