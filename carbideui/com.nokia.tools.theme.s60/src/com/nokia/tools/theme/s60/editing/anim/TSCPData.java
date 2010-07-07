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
 * ControlPoint Data - data object
 * for IControlPoint holding multiple param values
 * and it's state - isSet/isNotSet
 */
public class TSCPData {
	
	private int data[];
	
	private boolean setmask[];
	
	public TSCPData(int pcount) {
		data = new int[pcount];
		setmask = new boolean[pcount];
	}
	
	public TSCPData(int[] fs, boolean set) {
		data = fs;
		setmask = new boolean[fs.length];
		for(int i=0;i<fs.length;i++)
			setmask[i] = set;
	}

	public int getValue(int i) {
		return data[i];
	}
	
	public void setValue(int i, int v){
		data[i] = v;
		setmask[i] = true;
	}
	
	public boolean isSet(int i) {
		return setmask[i];
	}
	
	public void unset(int i){
		setmask[i] = false;
	}

	public void set(int i) {
		setmask[i] = true;
	}

}
