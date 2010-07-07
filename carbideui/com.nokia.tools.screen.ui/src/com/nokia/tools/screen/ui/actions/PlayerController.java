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
package com.nokia.tools.screen.ui.actions;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.jfc.JFCDiagramAdapter;
import com.nokia.tools.editing.model.EditDiagram;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.player.IErrorHandler;
import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.media.player.PlayState;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

/**
 * This controller controls all animated elements in screen. Individual
 * animation shall be turned on/off by calling {@link IPlayer#play()} and
 * {@link IPlayer#stop(}.
 * 
 */
public class PlayerController extends TypedAdapter implements Runnable,
		IErrorHandler {
	private static Set<PlayerController> controllers = new HashSet<PlayerController>();

	public static final int REFRESH_INTERVAL = 50;

	private volatile Thread thread;

	private EditPartViewer viewer;

	private Set<IScreenElement> toRefresh = Collections
			.synchronizedSet(new HashSet<IScreenElement>());

	private Set<IPlayer> badPlayers = new HashSet<IPlayer>();

	private float speedFactor = 1.0f;

	private Set<IPlayStateListener> listeners = new HashSet<IPlayStateListener>();

	private PlayState state;

	private long duration = Long.MAX_VALUE;

	private boolean isPlayOnce;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private Shell shell;

	/**
	 * Constructs a controller for the given screen.
	 * 
	 * @param screen the root screen element.
	 */
	private PlayerController(EditPartViewer viewer) {
		this.viewer = viewer;
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		screen.getWidget().eAdapters().add(this);
	}

	/**
	 * Creates a new instance of the controller if it's not available, otherwise
	 * returns the existing one.
	 * 
	 * @param viewer the viewer where the animation will be run.
	 * @return the controller instance.
	 */
	public synchronized static PlayerController getInstance(
			EditPartViewer viewer) {
		if (viewer == null) {
			return null;
		}

		IWorkbenchPart part = ((DefaultEditDomain) viewer.getEditDomain())
				.getEditorPart();
		if (part == null) {
			throw new IllegalArgumentException("No editor part found.");
		}
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		if (screen == null || screen.getWidget() == null) {
			return null;
		}
		PlayerController controller = null;
		for (PlayerController c : controllers) {
			if (c.viewer == viewer) {
				controller = c;
				break;
			}
		}
		if (controller == null) {
			controller = new PlayerController(viewer);
			controllers.add(controller);
		}
		return controller;
	}

	public static PlayerController findByElement(IScreenElement element) {
		IScreenElement root = element.getRoot();
		for (PlayerController controller : controllers
				.toArray(new PlayerController[controllers.size()])) {
			IScreenElement screen = ScreenUtil.getScreen(controller.viewer);
			if (root == screen) {
				return controller;
			}
		}
		return null;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		if (duration <= 0) {
			throw new IllegalArgumentException("The duration is not valid: "
					+ duration);
		}
		this.duration = duration;
	}

	/**
	 * @return the isPlayOnce
	 */
	public boolean isPlayOnce() {
		return isPlayOnce;
	}

	/**
	 * @param isPlayOnce the isPlayOnce to set
	 */
	public void setPlayOnce(boolean isPlayOnce) {
		this.isPlayOnce = isPlayOnce;
	}

	private void playElement(IScreenElement element) {
		IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
		if (player != null) {
			if (player.isPlayable()) {
				toRefresh.add(element);
				player.setSpeedFactor(speedFactor);
				player.setErrorHandler(this);
				player.play();
			}
		}
	}

	/**
	 * Tells all animators to play.
	 */
	public void play() {
		if (isPlaying()) {
			return;
		}

		badPlayers.clear();
		shell = Display.getCurrent().getActiveShell();
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		for (IScreenElement element : screen.getAllChildren()) {
			playElement(element);
		}

		startPlaying();

		notifyPlayStateChanged(PlayState.PLAYING);
	}

	/**
	 * Tells animators in selection to play.
	 */
	public void playSelection(IStructuredSelection sel) {
		if (isPlaying()) {
			return;
		}

		for (Object element : sel.toArray()) {
			IScreenElement se = JEMUtil.getScreenElement(element);
			playElement(se);
		}

		startPlaying();
		notifyPlayStateChanged(PlayState.PLAYING);
	}

	/**
	 * Tells all animators to play.
	 */
	public void pause() {
		if (!isPlaying()) {
			return;
		}

		IScreenElement screen = ScreenUtil.getScreen(viewer);
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (player != null) {
				player.pause();
			}
		}

		stopPlaying();
		notifyPlayStateChanged(PlayState.PAUSED);
	}

	/**
	 * Tells all animators to play.
	 */
	public void resume() {
		if (!isPaused()) {
			return;
		}

		IScreenElement screen = ScreenUtil.getScreen(viewer);
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (player != null && PlayState.PAUSED == player.getState()) {
				player.play();
			}
		}

		startPlaying();
		notifyPlayStateChanged(PlayState.PLAYING);
	}

	private synchronized void stopPlaying() {
		if (thread == null) {
			return;
		}
		Thread tmp = thread;
		thread = null;
		tmp.interrupt();

		JFCDiagramAdapter adapter = getDiagramAdapter();
		if (adapter != null) {
			adapter.setAnimating(false);
		}
	}

	private synchronized void startPlaying() {
		if (thread != null) {
			return;
		}
		thread = new Thread(this, getClass().getSimpleName());
		thread.start();
		JFCDiagramAdapter adapter = getDiagramAdapter();
		if (adapter != null) {
			adapter.setAnimating(true);
		}
	}

	/**
	 * Tells all animators to stop.
	 */
	public void stop() {
		stop(true);
	}

	private void stop(boolean forceImageUpdate) {
		if (!isPlaying() && !isPaused()) {
			return;
		}

		toRefresh.clear();

		IScreenElement screen = ScreenUtil.getScreen(viewer);
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (player != null) {
				player.stop();
			}
		}

		if (forceImageUpdate) {
			try {
				// force image update to draw the first frame
				imageUpdate();
			} catch (InterruptedException e) {
			} catch (Exception e) {
				UiPlugin.error(e);
			}
		}

		stopPlaying();
		notifyPlayStateChanged(PlayState.STOPPED);
	}

	public void dispose() {
		stop();
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (player != null) {
				player.dispose();
			}
		}

	}

	/**
	 * @return true if the controller is current playing animation, false
	 *         otherwise.
	 */
	public boolean isPlaying() {
		return thread != null;
	}

	/**
	 * @return true if the controller is current playing animation, false
	 *         otherwise.
	 */
	public boolean isPaused() {
		return PlayState.PAUSED == state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		long animationStart = System.currentTimeMillis();

		while (Thread.currentThread() == thread) {
			long repaintStart = System.nanoTime();

			try {
				boolean allStopped = true;
				synchronized (toRefresh) {
					for (Iterator<IScreenElement> i = toRefresh.iterator(); i
							.hasNext();) {
						IScreenElement element = i.next();
						IPlayer player = (IPlayer) element
								.getAdapter(IPlayer.class);
						if (player == null || player.isDisposed()) {
							i.remove();
							continue;
						}
						if (PlayState.PLAYING == player.getState()) {
							allStopped = false;
						}

						if (!isPlayOnce
								&& PlayState.STOPPED == player.getState()
								&& !badPlayers.contains(player)) {
							// let it loop when there is other things
							// playing
							player.play();
							allStopped = false;
						}
					}
				}

				imageUpdate();

				Thread.yield();

				if (allStopped || PlayState.STOPPED == state) {
					stop();
					break;
				}
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				UiPlugin.error(e);
				stop();
			}
			if (Thread.interrupted()) {
				break;
			}
			try {
				long repaintTook = System.nanoTime() - repaintStart;
				long sleepTime = REFRESH_INTERVAL * 1000000 - repaintTook;
				if (sleepTime > 0) {
					Thread.sleep(sleepTime / 1000000,
							(int) (sleepTime % 1000000));
				}
			} catch (Exception e) {
			}

			long animationTook = System.currentTimeMillis() - animationStart;
			if (animationTook > duration) {
				if (isPlayOnce) {
					PlayerController.this.stop();
				} else {
					PlayerController.this.pause();
				}
			}
		}
	}

	protected void imageUpdate() throws Exception {
		
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		Object bean = screen.getBean();
		final JComponent component = (JComponent) bean;
		if (component.getSize().width <= 0 || component.getSize().height <= 0) {
			stop(false);
			return;
		}

		IEditorPart editorPart = ((DefaultEditDomain) viewer.getEditDomain())
				.getEditorPart();
		EditPartViewer currentViewer = (EditPartViewer) editorPart
				.getAdapter(EditPartViewer.class);
		if (viewer != currentViewer) {
			// screen changed
			// stop animation preview
			stop(false);
			return;
		}
		// updates the figure, the adapter may choose not to refresh the image
		// figures when in the live mode
		JFCDiagramAdapter adapter = getDiagramAdapter();
		if (adapter != null) {
			adapter.notifyChanged(null);
		}
	}

	protected JFCDiagramAdapter getDiagramAdapter() {
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		EditDiagram diagram = EditingUtil.getDiagram((EditObject) screen
				.getWidget());
		if (diagram != null) {
			return (JFCDiagramAdapter) EcoreUtil.getExistingAdapter(diagram,
					JFCDiagramAdapter.class);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
	 */
	public void notifyChanged(Notification notification) {
		if (Notification.REMOVING_ADAPTER == notification.getEventType()
				&& notification.getNewValue() == null
				&& notification.getOldValue() == this) {
			for (Iterator<PlayerController> i = controllers.iterator(); i
					.hasNext();) {
				PlayerController controller = i.next();
				if (controller.viewer == viewer) {
					i.remove();
					controller.dispose();
				}
			}
		}
	}

	public float getSpeedFactor() {
		return speedFactor;
	}

	public void setSpeedFactor(float speedFactor) {
		this.speedFactor = speedFactor;
		IScreenElement screen = ScreenUtil.getScreen(viewer);
		for (IScreenElement element : screen.getAllChildren()) {
			IPlayer player = (IPlayer) element.getAdapter(IPlayer.class);
			if (player != null) {
				player.setSpeedFactor(speedFactor);
			}
		}
	}

	public void addPlayStateListener(IPlayStateListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener)) {
				listeners.add(listener);
			}
		}
	}

	public void removePlayStateListener(IPlayStateListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void setAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	protected void notifyPlayStateChanged(final PlayState state) {
		this.state = state;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				synchronized (listeners) {
					for (IPlayStateListener listener : listeners) {
						listener.stateChanged(state);
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IErrorHandler#handleError(com.nokia.tools.media.utils.player.IPlayer,
	 *      java.lang.Throwable)
	 */
	public void handleError(final IPlayer player, final Throwable e) {
		if (badPlayers.contains(player)) {
			return;
		}

		badPlayers.add(player);
		player.stop();

		if (shell != null) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					// make sure we're in the GUI mode
					if (shell != null) {
						MessageDialog.openError(shell,
								Messages.Error_Animation_Title, NLS.bind(
										Messages.Error_Animation_Message,
										new Object[] {
												player.getUrl(),
												player.getClass()
														.getSimpleName(),
												e.getMessage() }));
					}
				}
			});
		}
	}
	public interface IPlayStateListener {

		void stateChanged(PlayState state);

	}
}
