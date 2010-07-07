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

package com.nokia.tools.theme.s60.model.tpi;
/**
 * This class is a wrapper around the ThirdPartyIcons class and provides means
 * to get the wrapping Third Party Icon.
 * This wrapper has been introduced to ensure that the equals() and the hashcode()
 * methods over-ridden in Third Party Icon is eclipsed. This is needed as we want 
 * to use duplicate ThirdPartyIcons as keys in the Map when we want to indicates conflicts
 * and with just using ThirdPartyIcon, the entry in the Map would get replaced. 
 */
public class ThirdPartyIconWrapper {

	private ThirdPartyIcon tpIcon;

	public ThirdPartyIconWrapper(ThirdPartyIcon tpIcon) {
		this.tpIcon = tpIcon;
	}


	public ThirdPartyIcon getTpIcon() {
		return tpIcon;
	}
	
	public boolean equals(Object object){
		if(object instanceof ThirdPartyIconWrapper){
			return ((ThirdPartyIconWrapper)object).tpIcon == tpIcon;
		}
		return false;
	}
	
	public int hashCode(){
		return tpIcon.hashCode();
	}
}
