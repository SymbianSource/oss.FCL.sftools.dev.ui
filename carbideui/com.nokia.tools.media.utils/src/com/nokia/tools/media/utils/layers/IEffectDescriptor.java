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

import java.util.List;


public interface IEffectDescriptor {

		public String getClassName();			

		public boolean isInput();

		public String getName();

		public List<IEffectParameterDescriptor> getParameters();
		
		public IEffectParameterDescriptor getParameterDescriptor(String paramName);
		
		public IEffectParameterDescriptor getParameterDescriptor(int pos);
		
		public int getParameterLiteralValueNumber(String paramName, String literalValue);
		
		public String getParameterLiteralValue(String paramName, int number);
		
		/**
		 * @see ILayerEffect.EffectTypes
		 * @return
		 */
		public EffectTypes getType();

		public String getUid();
			
}


