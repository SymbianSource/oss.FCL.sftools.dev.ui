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

import java.net.URL;

import com.nokia.tools.media.core.Activator;

public interface IPlayer {
	/**
	 * @param isPlayable false to disable playing
	 */
	void setPlayable(boolean isPlayable);

	/**
	 * @return true if the player is able to play the content.
	 */
	boolean isPlayable();

	/**
	 * Loads the content from the given url.
	 * 
	 * @param url the data url.
	 */
	void load(URL url);

	/**
	 * @return the url
	 */
	URL getUrl();

	/**
	 * Starts playing.
	 */
	void play();

	/**
	 * Pauses playing.
	 */
	void pause();

	/**
	 * Stops playing.
	 */
	void stop();

	/**
	 * Sets the clock speed factor
	 */
	void setSpeedFactor(float factor);

	/**
	 * @return the current state.
	 */
	PlayState getState();

	/**
	 * Sets the error handler
	 * 
	 * @param handler the error handler
	 */
	void setErrorHandler(IErrorHandler handler);

	/**
	 * @return the error handler.
	 */
	IErrorHandler getErrorHandler();

	/**
	 * Disposes the resource.
	 */
	void dispose();

	/**
	 * @return true if the player is disposed.
	 */
	boolean isDisposed();

	/**
	 * Stub implementation, timing details are handled here.
	 */
	public abstract class Stub implements IPlayer {
		private PlayState state = PlayState.STOPPED;
		private long startTime;
		private long elapsedTime;
		private float speedFactor = 1.0f;
		private IErrorHandler errorHandler;
		private boolean isPlayable = true;
		private boolean isDisposed;

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#isPlayable()
		 */
		public boolean isPlayable() {
			return isPlayable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#setPlayable(boolean)
		 */
		public void setPlayable(boolean isPlayable) {
			this.isPlayable = isPlayable;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#load(java.net.URL)
		 */
		public final void load(URL url) {
			doLoad(url);
			isDisposed = false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#getUrl()
		 */
		public URL getUrl() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#getState()
		 */
		public synchronized PlayState getState() {
			return state;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#pause()
		 */
		public synchronized void pause() {
			if (PlayState.PLAYING != state) {
				return;
			}
			state = PlayState.PAUSED;
			capture();
			doPause();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#play()
		 */
		public synchronized void play() {
			if (PlayState.PLAYING == state) {
				return;
			}
			if (PlayState.PAUSED == state) {
				state = PlayState.PLAYING;
				resume();
			} else {
				state = PlayState.PLAYING;
				rewind();
			}
			doPlay();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#stop()
		 */
		public synchronized void stop() {
			if (PlayState.STOPPED == state) {
				return;
			}
			state = PlayState.STOPPED;
			startTime = 0;
			elapsedTime = 0;
			doStop();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#setSpeedFactor(float)
		 */
		public void setSpeedFactor(float speedFactor) {
			this.speedFactor = speedFactor;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#setErrorHandler(com.nokia.tools.media.utils.player.IErrorHandler)
		 */
		public void setErrorHandler(IErrorHandler handler) {
			this.errorHandler = handler;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#getErrorHandler()
		 */
		public IErrorHandler getErrorHandler() {
			return errorHandler;
		}

		protected void doLoad(URL url) {
		}

		/**
		 * Performs actions when the player is paused.
		 */
		protected void doPause() {
		}

		/**
		 * Performs actions when the player starts playing.
		 */
		protected void doPlay() {
		}

		/**
		 * Performs actions when the player is stopped.
		 */
		protected void doStop() {
		}

		protected void doDispose() {
		}

		/**
		 * Takes a snapshot.
		 * 
		 * @return the elapsed time.
		 */
		protected synchronized long capture() {
			if (PlayState.STOPPED == state) {
				return elapsedTime;
			}
			long time = System.nanoTime() / 1000000L;
			elapsedTime += (time - startTime) * speedFactor;
			startTime = time;
			return elapsedTime;
		}

		/**
		 * Rewinds to the beginning.
		 * 
		 * @return the elapsed time.
		 */
		protected synchronized long rewind() {
			startTime = System.nanoTime() / 1000000L;
			elapsedTime = 0;
			return elapsedTime;
		}

		protected synchronized long resume() {
			startTime = System.nanoTime() / 1000000L;
			return elapsedTime;
		}

		protected void handleError(Throwable e) {
			if (errorHandler != null) {
				errorHandler.handleError(this, e);
			}
			Activator.error(e);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.utils.player.IPlayer#dispose()
		 */
		public final void dispose() {
			stop();
			doDispose();
			isDisposed = true;
		}

		/**
		 * @return the isDisposed
		 */
		public boolean isDisposed() {
			return isDisposed;
		}
	}
}
