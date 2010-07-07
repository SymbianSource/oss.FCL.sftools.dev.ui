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

public interface IEffectParameterDescriptor {	
	
	public String getCaption();

	public String getDefaultVal();
	
	public String getMaxVal();

	public String getMinVal();

	public String[] getOptions();

	public int getPos();

	public String getUiName();

	public String getUiType();

	/**
	 * returns position of value in options
	 * @return
	 */
	public int getLiteralValueNumber(String literalValue);
	
	public String getLiteralValue(int number);
	
}
