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
package com.nokia.tools.media.utils.layers;

import java.awt.image.RenderedImage;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.nokia.tools.media.utils.timeline.ITreeTimeLineDataProvider;

public interface IImage {

	/**
	 * object state changed
	 */
	public static final String PROPERTY_STATE = "STATE";

	/**
	 * animation properties changed - have not impact on non-anim preview
	 */
	public static final String PROPERTY_ANIMATION_STATE = "ANIMATION_STATE";

	/**
	 * when image in some layer changed. Currently not used.
	 */
	public static final String PROPERTY_IMAGE = "IMAGE";

	/**
	 * when childs added / removed
	 */
	public static final String PROPERTY_STRUCTURE_CHANGE = "STRUCTURE_CHANGE";

	/**
	 * @return Returns the layer.
	 */
	public List<ILayer> getLayers();

	/**
	 * @return Returns the layer.
	 */
	public List<ILayer> getSelectedLayers();

	/**
	 * @return Adds new layer.
	 */
	public ILayer addLayer();

	/**
	 * @return Adds new layer at specified position.
	 */
	public ILayer addLayer(int index);

	/**
	 * @return Removes layer.
	 */
	public void removeLayer(ILayer layer);

	/**
	 * 
	 * @param index
	 * @return
	 */
	public ILayer getLayer(int index);

	/**
	 * 
	 * @param name
	 * @return
	 */
	public ILayer getLayer(String name);

	/**
	 * 
	 * @param name
	 * @return
	 */
	public int getSelectedLayerCount();

	/**
	 * @return
	 */
	public RenderedImage getAggregateImage();

	/**
	 * for realtime effect, with timeline positioned into time location
	 * 
	 * @param timeOffset
	 * @return
	 */
	public RenderedImage getAggregateImage(TimingModel timing, long time,
			boolean forPreview);

	/**
	 * return image of given dimensions, scaled/stretched if needed
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public RenderedImage getAggregateImage(int width, int height);

	/**
	 * moves given layer up or down
	 * 
	 * @param layer
	 * @param positionChange - -N, or +N, N is number
	 */
	public void changeLayerOrder(ILayer layer, int positionChange)
			throws Exception;

	/**
	 * 
	 * @return
	 */
	public int getWidth();

	/**
	 * 
	 * @return
	 */
	public int getHeight();

	public long getAnimationDuration();

	public void startAnimation();

	public void endAnimation();

	public boolean isAnimationStarted();

	/**
	 * Sets start of animation timeline on normal datetime.
	 * 
	 * @param time
	 */
	public void setAnimationStartLocation(long time);

	/**
	 * 
	 * @return
	 */
	public boolean isAnimated();

	/**
	 * true when image has animated effects with given timing model
	 * 
	 * @return
	 */
	public boolean isAnimatedFor(TimingModel timingType);

	/**
	 * returns animation length by timing type - for realtime, or relative
	 * effects
	 * 
	 * @param timingType
	 * @return
	 */
	public long getAnimationDuration(TimingModel timingType);

	/**
	 * returns animation length in given span or 0 if current timing is set to
	 * different timing model or time span
	 * 
	 * @param span
	 * @return
	 */
	public long getAnimationDuration(TimeSpan span);

	/**
	 * returns ITreeTimeProvider for timeline display
	 * 
	 * @return
	 */
	public ITreeTimeLineDataProvider getRealTimeTimeModelTimeLineProvider();

	/**
	 * returns ITreeTimeProvider for timeline display
	 * 
	 * @return
	 */
	public ITreeTimeLineDataProvider getRelativeTimeModelTimeLineProvider(
			TimeSpan timeSpan);

	/**
	 * clears animation nodes
	 * 
	 */
	public void clearTimeLineNodes();

	/**
	 * returns clone of this instance, except with images rendered for given
	 * resolution.
	 * 
	 * @param w
	 * @param h
	 * @return
	 */
	public IImage getAnotherInstance(int w, int h);

	/**
	 * returns element id
	 * 
	 * @return
	 */
	public String getId();

	/**
	 * clears icon images possibly created
	 */
	public void clearPreviewImages();

	/**
	 * returns element name
	 * 
	 * @return
	 */
	public String getName();

	public void addPropertyListener(PropertyChangeListener ps);

	public void removePropertyChangeListener(PropertyChangeListener ps);

	public int getLayerCount();

	public int getMaximumLayerCount();

	
	/**
	 * sets filtering for control points to display only points for params in
	 * given timing model
	 * 
	 * @param timeModel
	 * @param span
	 */
	public void updateControlPointsFiltering(TimingModel timeModel,
			TimeSpan span);

	public boolean isMultiPiece();
	
	public int getPartCount();
	
	public boolean isNinePiece();

	public List<IImage> getPartInstances() throws Exception;

	/**
	 * Returns identification for part, i.e. TL, T, BR, ...
	 * 
	 * @return
	 */
	public String getPartType();

	public boolean isPart();

	/**
	 * returns true if element supports animations
	 * 
	 * @return
	 */
	public boolean canBeAnimated();

	/**
	 * returns true if element supports nine piece graphics
	 * 
	 * @return
	 */
	public boolean supportsMultiPiece();

	public boolean supportsAnimationTiming(TimingModel relative);

	boolean isBackground();

	/**
     * @return
     */
    public boolean isElevenPiece();

}
