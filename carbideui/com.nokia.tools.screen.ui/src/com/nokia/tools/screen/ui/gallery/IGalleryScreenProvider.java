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

import java.awt.Dimension;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.widgets.Control;

import com.nokia.tools.content.core.IContentData;

/**
 * Interface for the screen provider.
 * 
 */
public interface IGalleryScreenProvider extends ISelectionChangedListener {
	/**
	 * Interface for the gallery screen model.
	 * 
	 */
	public interface IGalleryScreen {
		/**
		 * Returns the name of screen.
		 * 
		 * @return the name of screen.
		 */
		String getName();

		/**
		 * Returns true if this screen is the default one and should be
		 * generated automatically.
		 * 
		 * @return true if the screen is default, false otherwise.
		 */
		boolean isDefault();

		/**
		 * @return true if the screen is currently active in the main editor.
		 */
		boolean isActive();

		/**
		 * Sets the control that is created in the gallery page.
		 * 
		 * @param control
		 *            the control.
		 */
		void setControl(Control control);

		/**
		 * Returns the control, or null if the preview has not been generated
		 * yet.
		 * 
		 * @return the control or null if the preview is not available.
		 */
		Control getControl();

		/**
		 * Returns the associated viewer.
		 * 
		 * @param monitor
		 *            the progress monitor service.
		 * @return the associated viewer.
		 */
		GraphicalViewer getViewer(IProgressMonitor monitor);

		/**
		 * Returns the data.
		 */
		IContentData getData();
	}

	/**
	 * Returns the preview size.
	 * 
	 * @return the preview size.
	 */
	Dimension getSize();

	/**
	 * Returns all gallery screens.
	 * 
	 * @return all gallery screens.
	 */
	List<IGalleryScreen> getGalleryScreens();

	/**
	 * Called when the screen is created.
	 * 
	 * @param screen
	 *            the created screen.
	 */
	void screenCreated(IGalleryScreen screen);

	/**
	 * Called when the screen is disposed.
	 * 
	 * @param screen
	 *            the disposed screen.
	 */
	void screenDisposed(IGalleryScreen screen);

	/**
	 * Called when the gallery is disposed.
	 */
	void galleryDisposed();

	/**
	 * Checks if the provider itself is disposed.
	 */
	boolean isDisposed();

	/**
	 * @deprecated
	 * @param screen
	 */
	void addScreen(String appUid, String screenName);

	/**
	 * @param screenName2 
	 * @deprecated - the same as for addScreen
	 * underlying model is IContent based, notification should come from model
	 */
	void removeScreen(String appUid, String screenName);

}
