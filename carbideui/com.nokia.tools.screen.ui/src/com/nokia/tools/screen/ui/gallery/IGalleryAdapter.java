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
package com.nokia.tools.screen.ui.gallery;

/**
 * Class implementing this interface will notify the listeners when the gallery
 * changes.
 * 
 */
public interface IGalleryAdapter {
	/**
	 * Interface for the gallery listener.
	 * 
	 */
	public interface IGalleryListener {
		/**
		 * Called when the gallery changes.
		 * 
		 * @param screenProvider the screen provider from where the new gallery
		 *            screens can be obtained.
		 */
		void galleryChanged(IGalleryScreenProvider screenProvider);
	}

	/**
	 * @param listener listener to be registered.
	 */
	void addGalleryListener(IGalleryListener listener);

	/**
	 * @param listener listener to be unregistered.
	 */
	void removeGalleryListener(IGalleryListener listener);
	
	/**
	 * Notifies that gallery has changed.
	 */
	void notifyGalleryChanged();
}
