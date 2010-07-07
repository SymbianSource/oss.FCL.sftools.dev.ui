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

import java.awt.Graphics;
import java.io.File;

import com.nokia.tools.resource.util.FileUtils;

public class MediaPlayer implements IPaintAdapter {
	private IMediaProvider provider;
	private File file;
	private IPlayer player;

	public MediaPlayer(IMediaProvider provider) {
		this.provider = provider;
		refresh();
	}

	/**
	 * @return the paintAdapter
	 */
	public IPaintAdapter getPaintAdapter() {
		return player instanceof IPaintAdapter ? (IPaintAdapter) player : null;
	}

	/**
	 * @return the player
	 */
	public IPlayer getPlayer() {
		return player;
	}

	public void refresh() {
		File newFile = provider.getFile();
		if (newFile == null) {
			if (player != null) {
				player.dispose();
			}
			player = null;
			return;
		}
		if (newFile.equals(file) && player != null) {
			return;
		}

		file = newFile;

		PlayState oldState = null;
		IErrorHandler oldHandler = null;
		if (player != null) {
			oldState = player.getState();
			oldHandler = player.getErrorHandler();
			player.dispose();
		}

		player = null;

		player = createPlayer();

		if (player != null) {
			player.setErrorHandler(oldHandler);
			player.setPlayable(provider.isPlayable());
		}

		if (player instanceof IPaintAdapter) {
			player.load(FileUtils.toURL(file));
		}

		if (PlayState.PLAYING == oldState && player != null) {
			player.play();
		}
	}

	protected IPlayer createPlayer() {
		String contentType = provider.getContentType();
		IPlayer player = null;
		// first try content type
		if (contentType != null) {
			player = PlayerExtensionManager
					.createPlayerByContentType(contentType);
		}
		if (player == null && file != null) {
			String extension = FileUtils.getExtension(file);
			player = PlayerExtensionManager.createPlayerByExtension(extension);
			if (player == null) {
				player = PlayerExtensionManager
						.createStillImagePlayer(extension);
			}
		}
		return player;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the provider
	 */
	public IMediaProvider getProvider() {
		return provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.IPaintAdapter#paint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	public void paint(IPaintAdapter original, Graphics g) {
		refresh();
		if (getPaintAdapter() != null) {
			getPaintAdapter().paint(original, g);
		} else {
			original.paint(null, g);
		}
	}
}