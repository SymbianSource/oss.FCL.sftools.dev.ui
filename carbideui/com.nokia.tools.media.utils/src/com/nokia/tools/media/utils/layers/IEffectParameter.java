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

public interface IEffectParameter {
	
	/**
	 * raised when animation enabled / disabled on param
	 */
	public static final String PROPERTY_ANIMATED = "ANIMATED";
	
	public static final String PROPERTY_TIMING = "TIMING";
	
	/**
	 * when changed animation properties
	 */
	public static final String PROPERTY_ANIMATION_STATE = "ANIMATION_STATE";
	
	/**
	 * when parameter static value is changed 
	 */
	public static final String PROPERTY_VALUE_NON_ANIMATED = "VALUE_N_A";
	
	public String getValue();
	
	public String getValue(long time);
	
	public void setValue(String value);
	
	public boolean isAnimated();
	
	public void setAnimated(boolean animated);
	
	public String getName();
	
	public void addPropertyListener(PropertyChangeListener ps);
	
	public void removePropertyChangeListener(PropertyChangeListener ps);
	
	public TimingModel getTimingModel();
	
	public TimeSpan getTimeSpan();
	
	public ILayerEffect getParent();
	
}
