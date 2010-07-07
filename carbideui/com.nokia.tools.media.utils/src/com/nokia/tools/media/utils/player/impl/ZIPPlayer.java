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
package com.nokia.tools.media.utils.player.impl;

import java.awt.Graphics;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.nokia.tools.media.player.IPaintAdapter;
import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.media.player.PlayerExtensionManager;
import com.nokia.tools.media.player.impl.URLFramePlayer;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.FileUtils;

public class ZIPPlayer extends URLFramePlayer {
	private static final int DEFAULT_DURATION = 6000;

	private List<IPlayer> players;
	private IPlayer currentPlayer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#doLoad()
	 */
	@Override
	protected boolean doLoad() throws Exception {
		players = new ArrayList<IPlayer>();
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(getUrl().openStream());
			for (ZipEntry ze = null; (ze = zis.getNextEntry()) != null; zis
					.closeEntry()) {
				if (!ze.isDirectory()) {
					URL url = new URL("jar:" + getUrl().toExternalForm() + "!/"
							+ ze.getName());
					String extension = FileUtils.getExtension(ze.getName());
					IPlayer player = PlayerExtensionManager
							.createPlayerByExtension(extension);
					if (player == null) {
						player = PlayerExtensionManager
								.createStillImagePlayer(extension);
					}
					if (player != null) {
						player.load(url);
						players.add(player);
					} else {
						UtilsPlugin.warn("No player is configured for "
								+ ze.getName());
					}
				}
			}
		} finally {
			FileUtils.close(zis);
		}
		return true;
	}

	private long getDuration() {
		return players == null ? 0 : players.size() * DEFAULT_DURATION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#doPaint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	@Override
	protected void doPaint(IPaintAdapter original, Graphics g) throws Exception {
		if (players == null || players.isEmpty()) {
			return;
		}
		long totalDuration = getDuration();
		int f = (int) (capture() % totalDuration);
		int currentFrame = f / DEFAULT_DURATION;
		if (currentFrame >= players.size()) {
			stop();
			currentFrame = 0;
		}

		IPlayer player = players.get(currentFrame);
		if (currentPlayer != player) {
			player.stop();
			player.play();
			currentPlayer = player;
		}
		if (player instanceof IPaintAdapter) {
			((IPaintAdapter) player).paint(original, g);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doPlay()
	 */
	@Override
	protected void doPlay() {
		if (players != null) {
			for (IPlayer player : players) {
				player.play();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doStop()
	 */
	@Override
	protected void doStop() {
		if (players != null) {
			for (IPlayer player : players) {
				player.stop();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doPause()
	 */
	@Override
	protected void doPause() {
		if (players != null) {
			for (IPlayer player : players) {
				player.pause();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.impl.URLFramePlayer#isContentPlayable()
	 */
	@Override
	public boolean isContentPlayable() {
		if (super.isContentPlayable() && players != null) {
			for (IPlayer player : players) {
				if (player.isPlayable()) {
					return true;
				}
			}
			return players.size() > 1;
		}
		return false;
	}
}
