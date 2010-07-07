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

package com.nokia.tools.screen.core;

public interface IPropertyCustomizer {

	public static String TEXT = "text";
	public static String ID = "id";
	
	public boolean isPropertyModifiable(String type);
	public boolean isEditable(String property);
	public boolean isDisplayable(String property);
	
	abstract class TexPropertyCustomizer implements IPropertyCustomizer
	{
		public boolean isPropertyModifiable(String type)
		{
			if(TEXT.equals(type))
				return true;
		
			return false;
		}
		
		public boolean isEditable(String property)
		{
			return true;
		}
		public boolean isDisplayable(String property)
		{
			return true;
		}
	}
	
}
