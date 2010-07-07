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
 * Value mediator for extracting values from TSCPData
 */
public class TSCPValueMediator implements IValueMediator {

	private int paramCount;
	
	public float getValue(Object data) {
		throw new RuntimeException("This mediator supports only indexed parameters");
	}

	public float getValue(Object data, int paramPos) {
		return ((TSCPData)data).getValue(paramPos);
	}

	public boolean isParamSet(Object data, int paramPos) {
		return ((TSCPData)data).isSet(paramPos);
	}

	public Object createCPDataObject() {
		return new TSCPData(paramCount);
	}

	public void setParamCount(int i) {
		paramCount = i;
	}

	public void setAll(Object data, float value) {
		TSCPData d = (TSCPData) data;
		if (d != null) {
			for(int i=0;i<paramCount;i++) {
				d.setValue(i, Math.round(value));
			}
		}
	}

	public void setAll(Object data) {
		TSCPData d = (TSCPData) data;
		if (d != null) {
			for(int i=0;i<paramCount;i++) {
				d.set(i);
			}
		}
	}

	public boolean isEmpty(Object data) {
		TSCPData d = (TSCPData) data;
		for (int i=0;i<paramCount;i++)
			if (d.isSet(i))
				return false;
		return true;
	}

	public void set(Object data, int index) {
		TSCPData d = (TSCPData) data;
		d.set(index);
	}

	public void setValue(Object data, int paramPos, float value) {
		TSCPData d = (TSCPData) data;
		d.setValue(paramPos, (int) value);
	}

	public void unset(Object data, int index) {
		TSCPData d = (TSCPData) data;
		d.unset(index);
	}

	public void unsetAll(Object data) {
		TSCPData d = (TSCPData) data;
		if (d != null) {
			for(int i=0;i<paramCount;i++) {
				d.unset(i);
			}
		}
	}

}
