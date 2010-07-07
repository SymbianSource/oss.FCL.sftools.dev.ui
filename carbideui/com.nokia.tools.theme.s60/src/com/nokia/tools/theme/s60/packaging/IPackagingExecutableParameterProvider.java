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
package com.nokia.tools.theme.s60.packaging;

import java.util.List;

import com.nokia.tools.packaging.PackagingContext;

/**
 * This interface helps to get the plugin specific parameters for the
 * executables. This interface can be implemented in the plugins to work with
 * the custom parametrs as per the executables require.
 * 
 */
public interface IPackagingExecutableParameterProvider {

	public List<String> getUpdatedParameterList(
			List<String> originalParameters, PackagingContext context);

}
