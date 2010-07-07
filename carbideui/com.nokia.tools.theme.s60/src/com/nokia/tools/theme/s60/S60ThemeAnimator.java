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
package com.nokia.tools.theme.s60;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JComponent;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.player.IPaintAdapter;
import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.media.player.PlayState;
import com.nokia.tools.media.utils.layers.IAnimatedImage;
import com.nokia.tools.media.utils.layers.IAnimationFrame;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.media.utils.layers.ILayer;
import com.nokia.tools.media.utils.layers.TimeSpan;
import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.actions.PlayerController;
import com.nokia.tools.screen.ui.actions.PlayerController.IPlayStateListener;
import com.nokia.tools.theme.s60.editing.EditableAnimationFrame;
import com.nokia.tools.theme.s60.editing.EditableEntityImage;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;

/**
 * This class provides animation support for S60 theme previews.
 * 
 */
public class S60ThemeAnimator extends IPlayer.Stub implements IPaintAdapter,
		IPlayStateListener {
	private static final long MaxCacheSize = 120;

	private static final String KEY_BG_LIST = S60ThemeAnimator.class.getName()
			+ ".animatedBackgrounds";

	private IImage image;

	private IScreenElement element;

	private List<CacheItem> cache = new LinkedList<CacheItem>();

	private long oldTime;

	private boolean dropFrames = false;

	private boolean paused;

	private Boolean isBackground;

	private boolean clearCache = true;

	private Adapter refreshAdapter = new TypedAdapter() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
		 */
		public void notifyChanged(Notification notification) {
			if (EditingUtil.isRemovingAdapter(notification, this)) {
				return;
			}
			if (getState() == PlayState.PLAYING) {
				// if element changed
				// stop and start playing from begining
				doStop();
				doPlay();
			} else {
				doStop();
			}
		}
	};

	/**
	 * Constructs an animator with the provided content data.
	 * 
	 * @param data the content data.
	 */
	public S60ThemeAnimator(IScreenElement element) {
		this.element = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer#isPlayable()
	 */
	public boolean isPlayable() {
		IImageAdapter adapter = (IImageAdapter) element.getData().getAdapter(
				IImageAdapter.class);
		if (adapter == null) {
			return false;
		}
		IImage image = adapter.getImage();
		return image != null && image.canBeAnimated() && getDuration(image) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doPlay()
	 */
	protected synchronized void doPlay() {
		if (paused) {
			paused = false;
			return;
		}

		image = ((IImageAdapter) element.getData().getAdapter(
				IImageAdapter.class)).getImage();

		if (clearCache) {
			cache.clear();
		} else {
			clearCache = true;
		}

		element.getWidget().eAdapters().add(refreshAdapter);
		PlayerController controller = getPlayerController();
		if (controller != null) {
			controller.addPlayStateListener(this);
		}

		image.startAnimation();

		oldTime = 0;

		isBackground = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doPause()
	 */
	@Override
	protected void doPause() {
		paused = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#doStop()
	 */
	protected synchronized void doStop() {
		if (clearCache) {
			cache.clear();
		}

		List<AnimatedBackground> animatedBackgrounds = getAnimatedBackgrounds();
		if (animatedBackgrounds != null) {
			for (AnimatedBackground bg : animatedBackgrounds) {
				CoreImage.dispose(bg.backgroundImage);
			}
			animatedBackgrounds.clear();
		}

		if (refreshAdapter.getTarget() != null) {
			refreshAdapter.getTarget().eAdapters().remove(refreshAdapter);
		}
		PlayerController controller = getPlayerController();
		if (controller != null) {
			controller.removePlayStateListener(this);
		}
		if (image != null) {
			image.endAnimation();
			image = null;
		}

		paused = false;
	}

	private TimingModel getTimingModel(IImage image) {
		if (image instanceof IAnimatedImage) {
			return TimingModel.RealTime;
		}
		if (image.supportsAnimationTiming(TimingModel.Relative)) {
			return TimingModel.Relative;
		}
		return TimingModel.RealTime;
	}

	private long getDuration(IImage image) {
		TimingModel timing = getTimingModel(image);
		long duration = TimingModel.Relative == timing ? image
				.getAnimationDuration(timing) : image.getAnimationDuration();
		for (ILayer layer : image.getLayers()) {
			long d = layer.getAnimationDuration(timing);
			duration = Math.max(duration, d);
		}
		return duration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.player.IPlayer.Stub#stop()
	 */
	@Override
	public synchronized void stop() {
		if (PlayState.STOPPED != getState()) {
			super.stop();
		} else {
			doStop();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.IPaintAdapter#paint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	public synchronized void paint(IPaintAdapter original, Graphics g) {
		List<AnimatedBackground> animatedBackgrounds = getAnimatedBackgrounds();
		if (animatedBackgrounds == null) {
			animatedBackgrounds = new ArrayList<AnimatedBackground>();
			PlayerController controller = getPlayerController();
			if (controller != null) {
				controller.setAttribute(KEY_BG_LIST, animatedBackgrounds);
			}
		}

		if ((!isBackgroundRefresh()) && getState() != PlayState.PLAYING) {
			original.paint(null, g);
			return;
		}

		long duration = getState() != PlayState.PLAYING ? 0
				: getDuration(image);
		if ((!isBackgroundRefresh()) && duration <= 0) {
			original.paint(null, g);
			return;
		}

		IImage bgImage = null;
		String bgImageTimeStamp = "-1";

		if (isBackgroundRefresh()) {
			if (image == null) {
				doPlay();
			}

			Rectangle bounds = getAbsolutePosition((JComponent) original);

			AnimatedBackground background = getRenderedBackground(bounds);

			if (background != null) {
				bgImage = background.image;
				bgImageTimeStamp = background.timeStamp;
				RenderedImage croppedBackground = CoreImage.create(
						background.backgroundImage).crop(
						bounds.x - background.bounds.x,
						bounds.y - background.bounds.y, bounds.width,
						bounds.height).getAwt();
				if (croppedBackground != null) {
					((EditableEntityImage) image)
							.forceBackgroundImage(croppedBackground);
				}
			}
		}

		if (getState() == PlayState.PLAYING && duration > 0) {
			TimingModel timing = getTimingModel(image);

			long currTime = getCurrentTime(timing);
			if (currTime > duration) {
				// leverage the handle to the controller so individual
				// animation
				// shall "stop" upon playing to the last frame
				// currTime = rewind();

				// added by sirokkam
				// do not clear cache for every animation cycle if
				// playercontroller.playOnce == false
				if (getPlayerController().isPlaying()
						&& !getPlayerController().isPlayOnce()) {
					clearCache = false;
				}

				stop();
				return;
			}

			String timeStamp = bgImageTimeStamp + ";" + currTime;

			RenderedImage im = getFromCache(timeStamp, bgImageTimeStamp,
					bgImage);

			if (im == null) {
				im = image.getAggregateImage(timing, currTime, true);
				addToCache(timeStamp, bgImageTimeStamp, bgImage, im);
			}

			if (im != null) {
				((Graphics2D) g).drawRenderedImage(im,
						CoreImage.TRANSFORM_ORIGIN);

				if (animatedBackgrounds != null && isBackground(element)) {
					addRenderedBackground(timeStamp,
							getAbsolutePosition((JComponent) original), im);
				} else {
					CoreImage.dispose(im);
				}
			}
		} else {
			String timeStamp = bgImageTimeStamp + ";-1";

			RenderedImage im = getFromCache(timeStamp, bgImageTimeStamp,
					bgImage);

			if (im == null) {
				im = image.getAggregateImage();
				addToCache(timeStamp, bgImageTimeStamp, bgImage, im);
			}

			if (im != null) {
				((Graphics2D) g).drawRenderedImage(im,
						CoreImage.TRANSFORM_ORIGIN);

				if (animatedBackgrounds != null && isBackground(element)) {
					addRenderedBackground(timeStamp,
							getAbsolutePosition((JComponent) original), im);
				} else {
					CoreImage.dispose(im);
				}
			}
		}
	}

	protected long getCurrentTime(TimingModel timing) {
		long clockIncrement = 0;
		long clockPerIncrement = 0;

		if (TimingModel.RealTime == timing) {
			ITreeTimeLineDataProvider provider = image
					.getRealTimeTimeModelTimeLineProvider();
			clockIncrement = provider.getClockIncrement();
			clockPerIncrement = provider.getClockTimePerIncrement();
		} else {
			clockPerIncrement = Long.MAX_VALUE;
			if (((EditableEntityImage) image)
					.getAnimationDuration(TimeSpan.EHour) != 0) {
				ITreeTimeLineDataProvider provider = image
						.getRelativeTimeModelTimeLineProvider(TimeSpan.EHour);
				clockIncrement = provider.getClockIncrement();
				clockPerIncrement = Math.min(clockPerIncrement, provider
						.getClockTimePerIncrement());
			}

			if (((EditableEntityImage) image)
					.getAnimationDuration(TimeSpan.EDay) != 0) {
				ITreeTimeLineDataProvider provider = image
						.getRelativeTimeModelTimeLineProvider(TimeSpan.EDay);
				clockIncrement += provider.getClockIncrement();
				clockPerIncrement = Math.min(clockPerIncrement, provider
						.getClockTimePerIncrement());
			}

			if (((EditableEntityImage) image)
					.getAnimationDuration(TimeSpan.EWeek) != 0) {
				ITreeTimeLineDataProvider provider = image
						.getRelativeTimeModelTimeLineProvider(TimeSpan.EWeek);
				clockIncrement += provider.getClockIncrement();
				clockPerIncrement = Math.min(clockPerIncrement, provider
						.getClockTimePerIncrement());
			}

			if (((EditableEntityImage) image)
					.getAnimationDuration(TimeSpan.EMonth) != 0) {
				ITreeTimeLineDataProvider provider = image
						.getRelativeTimeModelTimeLineProvider(TimeSpan.EMonth);
				clockIncrement += provider.getClockIncrement();
				clockPerIncrement = Math.min(clockPerIncrement, provider
						.getClockTimePerIncrement());
			}

			if (clockPerIncrement == Long.MAX_VALUE) {
				clockPerIncrement = 0;
			}
		}

		long currTime = clockPerIncrement == 0 ? capture() : ((long) (capture()
				* clockIncrement / (double) clockPerIncrement));

		if (clockIncrement != 0) {
			// modif currtime to be more easily cacheable
			currTime = currTime / clockIncrement * clockIncrement;
		}

		if (!dropFrames) {
			if (image instanceof IAnimatedImage) {
				IAnimatedImage animImg = (IAnimatedImage) image;
				IAnimationFrame frame = animImg.getAnimationFrame(currTime);
				IAnimationFrame oldFrame = animImg.getAnimationFrame(oldTime);
				int seqNo = frame.getSeqNo();
				if (currTime > ((EditableAnimationFrame) frame).getTime()
						+ frame.getAnimateTime()) {
					seqNo++;
				}
				int oldSeqNo = oldFrame.getSeqNo();
				if (seqNo > oldSeqNo + 1) {
					currTime = ((EditableAnimationFrame) oldFrame).getTime()
							+ oldFrame.getAnimateTime();
				}
			} else {
				if (clockIncrement != 0) {
					long oldSeqNo = oldTime / clockIncrement;
					long seqNo = currTime / clockIncrement;
					if (seqNo > oldSeqNo + 1) {
						currTime = (oldSeqNo + 1) * clockIncrement;
					}
				}
			}
		}

		oldTime = currTime;

		return currTime;
	}

	private void addRenderedBackground(String timeStamp, Rectangle bounds,
			RenderedImage renderedBackground) {
		List<AnimatedBackground> animatedBackgrounds = getAnimatedBackgrounds();
		if (animatedBackgrounds != null) {
			AnimatedBackground background = new AnimatedBackground();
			background.image = image;
			background.backgroundImage = renderedBackground;
			background.bounds = bounds;
			background.timeStamp = timeStamp;
			background.animator = this;

			animatedBackgrounds.add(background);
		}
	}

	private AnimatedBackground getRenderedBackground(Rectangle bounds) {
		List<AnimatedBackground> animatedBackgrounds = getAnimatedBackgrounds();
		if (animatedBackgrounds != null) {
			for (int i = animatedBackgrounds.size() - 1; i >= 0; i--) {
				AnimatedBackground bg = animatedBackgrounds.get(i);
				if (bg.animator == this) {
					// added by sirokkam
					// new paint throughput detected
					// backgrounds must be cleared
					for (AnimatedBackground bg2 : animatedBackgrounds) {
						CoreImage.dispose(bg2.backgroundImage);
					}
					animatedBackgrounds.clear();
					return null;
				}
				if (bg.bounds.contains(bounds)) {
					return bg;
				}
			}
		}
		return null;
	}

	private Rectangle getAbsolutePosition(JComponent component) {
		Point absLoc = new Point(0, 0);
		for (Component c = component; c != null; c = c.getParent()) {
			absLoc.x += c.getLocation().x;
			absLoc.y += c.getLocation().y;
		}

		Dimension size = component.getSize();
		Rectangle rect = new Rectangle(absLoc, size);

		return rect;
	}

	private void addToCache(String timeStamp, String bgImageTimeStamp,
			IImage bgImage, RenderedImage renderedImage) {
		if (isCacheable(bgImageTimeStamp)) {
			if (cache.size() >= MaxCacheSize) {
				cache.remove(MaxCacheSize - 1);
			}
			cache.add(0, new CacheItem(timeStamp, bgImage, renderedImage));
		}
	}

	private RenderedImage getFromCache(String timeStamp,
			String bgImageTimeStamp, IImage bgImage) {
		RenderedImage im = null;
		if (isCacheable(bgImageTimeStamp)) {
			int hash = timeStamp.hashCode();
			for (CacheItem item : cache) {
				if (hash == item.hash) {
					im = (RenderedImage) item.renderedImage;
					if (bgImage != null && bgImage != item.image) {
						// bgImage changed
						// invalidate entire cache
						cache.clear();
						im = null;
					}
					break;
				}
			}
		}
		return im;
	}

	private boolean isCacheable(String bgImageTimeStamp) {
		if (image instanceof IAnimatedImage) {
			return false;
		}

		// cache only images with static parent elements
		// to avoid cache overkill
		StringTokenizer st = new StringTokenizer(bgImageTimeStamp, ";");
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (Long.parseLong(token) != -1L) {
				return false;
			}
		}
		return true;
	}

	private boolean isBackground(IScreenElement element) {
		// commented by sirokkam
		// this only returs true for top level backgrounds
		// but we need to return true for nested backgrounds...
		// ISkinnableEntityAdapter sa = (ISkinnableEntityAdapter) element
		// .getData().getAdapter(ISkinnableEntityAdapter.class);
		// return sa != null && sa.isBackground();

		// result is cached
		if (element == this.element && isBackground != null) {
			return isBackground;
		}

		boolean bg = TSDataUtilities.isTopOrNestedBackgroundElement(element
				.getData());
		if (element == this.element) {
			isBackground = bg;
		}
		return bg;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Animator<" + (element == null ? null : element.toString())
				+ ">";
	}

	protected PlayerController getPlayerController() {
		return PlayerController.findByElement(element);
	}

	protected List<AnimatedBackground> getAnimatedBackgrounds() {
		PlayerController controller = getPlayerController();
		if (controller != null) {
			return (List<AnimatedBackground>) controller
					.getAttribute(KEY_BG_LIST);
		}
		return null;
	}

	protected boolean isBackgroundRefresh() {
		for (IScreenElement child : element.getRoot().getAllChildren()) {
			if (isBackground(child)) {
				IPlayer player = (IPlayer) child.getAdapter(IPlayer.class);
				if (player != null && PlayState.PLAYING == player.getState()) {
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.actions.PlayerController.IPlayStateListener#stateChanged(com.nokia.tools.media.utils.player.PlayState)
	 */
	public void stateChanged(PlayState state) {
		List<AnimatedBackground> bgs = getAnimatedBackgrounds();
		if (bgs == null) {
			return;
		}

		if (PlayState.STOPPED == state) {
			for (Iterator<AnimatedBackground> i = bgs.iterator(); i.hasNext();) {
				AnimatedBackground bg = i.next();
				if (bg.image == image) {
					i.remove();
				}
			}
		} else if (PlayState.PLAYING == state) {
			bgs.clear();
		}
	}

	private static class CacheItem {
		String timeStamp;

		IImage image;

		RenderedImage renderedImage;

		int hash;

		CacheItem(String timeStamp, IImage image, RenderedImage renderedImage) {
			this.timeStamp = timeStamp;
			this.image = image;
			this.renderedImage = renderedImage;
			this.hash = timeStamp.hashCode();
		}
	}

	static class AnimatedBackground {
		public IImage image;

		public RenderedImage backgroundImage;

		public Rectangle bounds;

		public String timeStamp;

		public S60ThemeAnimator animator;
	}
}
