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
package com.nokia.tools.theme.s60.editing.anim;

/**
 * 
 * interface used to extract values from control point model data objects, 
 * so that control point model class can interpolate values between control points.
 *
 *
 */
public interface IValueMediator {

	/** returns value from given data obj. */
	float getValue(Object data);
	
	/** returns value from given data obj. for given par. index*/
	float getValue(Object data, int paramPos);	
	
	boolean isParamSet(Object data, int paramPos);	

	/** set value for given parameter in given data obj. */
	void setValue(Object data, int paramPos, float value);
	
	/** marks parameter as not set */
	void unset(Object data,int index);
	
	/** marks parameter as set*/
	void set(Object data,int index);
	
	/**
	 * sets all params to given value and marks them as set
	 */
	void setAll(Object data, float value);

	/** marks all params as set*/
	void setAll(Object data);
	
	/** marks all params as unset*/
	void unsetAll(Object data);
	
	/** true if value not set for any parameter*/
	boolean isEmpty(Object data);
	
	void setParamCount(int i);
	
	Object createCPDataObject();
	
}
