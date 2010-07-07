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

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.content.core.IContentAdapter;
import com.nokia.tools.editing.ui.adapter.IComponentAdapter;

/**
 * Interface for the screen customizer to implement.
 */
public interface IScreenCustomizer {
	/**
	 * @return true if this is the default one, which will be invoked only when
	 *         there is no customizer available
	 */
	boolean isDefault();

	/**
	 * Returns the customized component adapter for the specific screen element.
	 * 
	 * @param element the screen element to which the component adapter will
	 *        appy.
	 * @return the customized component adapter.
	 */
	IComponentAdapter getComponentAdapter(IScreenElement element);

	/**
	 * Customizes the screen.
	 * 
	 * @param contentAdapter the content adapter.
	 * @param screen the screen to apply customization.
	 * @return true if the customization has been applied, false otherwise.
	 */
	boolean customizeScreen(IContentAdapter contentAdapter,
			IScreenElement screen, IProgressMonitor monitor);
}
