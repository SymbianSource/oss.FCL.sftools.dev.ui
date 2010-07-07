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
package com.nokia.tools.theme.s6092;

import java.util.ArrayList;
import java.util.List;

import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.theme.s60.packaging.IPackagingExecutableParameterProvider;

/**
 * This class provides the plugin specific parameters for the executables for
 * the plugin.
 * 
 */
public class PackagingParameterProvider implements
		IPackagingExecutableParameterProvider {

	public PackagingParameterProvider() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * This method provides the modified parameter that is plugin specific for
	 * executing the executables.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.nokia.tools.theme.s60.packaging.IPackagingExecutableParameterProvider
	 * #getUpdatedParameter(java.lang.String[],
	 * com.nokia.tools.theme.s60.packaging.PackagingContext)
	 */
	public List<String> getUpdatedParameterList(
			List<String> originalParameters, PackagingContext context) {
		List<String> modifiedparameterList = new ArrayList<String>();
		for (String parameter : originalParameters) {
			if (parameter.equals("/V1")) {
				modifiedparameterList.add("/V5");
			} else {
				if(parameter.contains("/B"))
					continue;
				modifiedparameterList.add(parameter);
			}
		}
		return modifiedparameterList;
	}

}
