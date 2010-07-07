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

import java.util.HashMap;
import java.util.Map;

public class LayerEffectRegistry {

	private static final String DEFAULT = "_default_factory";

	private static Map<String, IEffectFactory> registered = new HashMap<String, IEffectFactory>();

	public static IEffectFactory getFactory(String context) {
		return registered.get(context == null ? DEFAULT : context);
	}

	public static void registerFactory(String context, IEffectFactory fc) {
		registered.put(context, fc);
	}

	public static void registerDefaultFactory(IEffectFactory fc) {
		if (registered.get(DEFAULT) != null)
			System.out
					.println("LayerEffectHelper: Warning - multiple register of default effect factory.");
		registered.put(DEFAULT, fc);
	}

}
