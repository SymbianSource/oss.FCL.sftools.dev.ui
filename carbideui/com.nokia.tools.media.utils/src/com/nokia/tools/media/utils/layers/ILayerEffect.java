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

import java.beans.PropertyChangeListener;
import java.util.List;

public interface ILayerEffect {
	
	public static final String PROPERTY_STATE = "STATE";
	
	public static final String PROPERTY_ANIMATION_STATE = "ANIMATION_STATE";

	public String getName();

	public List<IEffectParameterDescriptor> getAttributeDescriptors();
	
	public List<IEffectParameter> getParameters();

	public IEffectDescriptor getDescriptor();

	public Object getEffectParameter(String key);
	
	public Object getStaticEffectParameter(String key);

	public Object getEffectParameter(String key, long time);

	public void setEffectParameter(String key, Object value);

	public IEffectParameter getParameter(String name);

	/**
	 * @return Returns the layer.
	 */
	public ILayer getParent();

	public EffectTypes getType();

	public boolean isSelected();

	public void setSelected(boolean selected);

	public void addPropertyListener(PropertyChangeListener ps);

	public void removePropertyChangeListener(PropertyChangeListener ps);

	public boolean isAnimated();

	public long getAnimationDuration();

	public void startAnimation();

	public void endAnimation();

	/**
	 * true when image has animated effects with given timing model
	 * 
	 * @return
	 */
	public boolean isAnimatedFor(TimingModel timingType);
	
	public boolean isAnimatedFor(TimeSpan span);

	/**
	 * returns animation length by timing type - for realtime, or relative
	 * effects
	 * 
	 * @param timingType
	 * @return
	 */
	public long getAnimationDuration(TimingModel timingType);

	/**
	 * if has at least one animatable parameter, can be animated
	 */
	public boolean animationAllowed();

	public boolean isAnimatedInPreview();

	public void setAnimatedInPreview(boolean animatedInPreview);

	public boolean isParameterSet(String name);

}
