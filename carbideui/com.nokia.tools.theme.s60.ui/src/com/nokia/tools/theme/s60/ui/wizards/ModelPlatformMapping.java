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
/**
 * 
 */
package com.nokia.tools.theme.s60.ui.wizards;

import java.io.IOException;
import java.util.Properties;

/**
 * Configuration for the platforms which are already have been released without
 * compilers bundled with it.
 * 
 */
public class ModelPlatformMapping extends Properties {

	public static String S60_31_E71 = "S60_31_E71";
	public static String S60_Tube = "S60_Tube";

	private static ModelPlatformMapping instance;
	private String configFileName = "ModelPlatformMapping.properties";

	private ModelPlatformMapping() {
		try {
			load(this.getClass().getResourceAsStream(configFileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ModelPlatformMapping getInstance() {
		if (instance == null) {
			instance = new ModelPlatformMapping();
		}
		return instance;
	}

}
