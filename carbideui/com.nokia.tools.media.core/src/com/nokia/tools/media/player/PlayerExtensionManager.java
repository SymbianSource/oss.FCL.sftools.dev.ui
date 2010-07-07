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
package com.nokia.tools.media.player;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import com.nokia.tools.media.core.Activator;
import com.nokia.tools.media.image.ImageExtensionManager;
import com.nokia.tools.media.player.impl.StillImagePlayer;

public class PlayerExtensionManager {
	/**
	 * Player contributor id.
	 */
	public static final String PLAYER_CONTRIBUTOR_ID = Activator.PLUGIN_ID
			+ ".players";
	private static final String EXT_PLAYER = "player";
	private static final String ATT_CONTENT_TYPE = "contentType";
	private static final String ATT_EXTENSION = "extension";
	private static final String ATT_CLASS = "class";

	/**
	 * No instantiation.
	 */
	private PlayerExtensionManager() {
	}

	/**
	 * Creates a new player supporting the given content type.
	 * 
	 * @param contentType the content type used for lookup.
	 * @return a new player instance if it supports the content type.
	 */
	public static IPlayer createPlayerByContentType(String contentType) {
		if (contentType == null) {
			return null;
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PLAYER_CONTRIBUTOR_ID);
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (EXT_PLAYER.equals(element.getName())) {
					String type = element.getAttribute(ATT_CONTENT_TYPE);
					if (contentType.equalsIgnoreCase(type)) {
						try {
							return (IPlayer) element
									.createExecutableExtension(ATT_CLASS);
						} catch (Throwable e) {
							Activator.error(e);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new player supporting the given file extension.
	 * 
	 * @param contentType the content type used for lookup.
	 * @return a new player instance if it supports the file extension.
	 */
	public static IPlayer createPlayerByExtension(String ext) {
		if (ext == null) {
			return null;
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PLAYER_CONTRIBUTOR_ID);
		for (IExtension extension : point.getExtensions()) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (EXT_PLAYER.equals(element.getName())) {
					String type = element.getAttribute(ATT_EXTENSION);
					if (ext.equalsIgnoreCase(type)) {
						try {
							return (IPlayer) element
									.createExecutableExtension(ATT_CLASS);
						} catch (Throwable e) {
							Activator.error(e);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Creates a new still image player.
	 * 
	 * @param ext the image file extension.
	 * @return the image player if the extension is supported, null otherwise.
	 */
	public static IPlayer createStillImagePlayer(String ext) {
		if (ext != null
				&& (ImageIO.getImageReadersBySuffix(ext).hasNext() || ImageExtensionManager
						.getLoaderByExtension(ext) != null)) {
			return new StillImagePlayer();
		}
		return null;
	}
}
