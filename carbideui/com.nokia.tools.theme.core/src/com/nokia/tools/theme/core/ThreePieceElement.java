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
package com.nokia.tools.theme.core;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.theme.editing.BasicEntityImage;

/**
 * Represents a three piece element. Note that for multi-piece elements like
 * nine piece elements, following features behave differently:
 * Layers View
 * Icon View
 * Extended Copy Paste Availability
 * Packaging
 * Search View
 *
 */
public class ThreePieceElement implements IMultiPiece {

	private final int PART_COUNT = 3;
	private final String ELEMENT_TYPE_ID = "3 Piece Bitmap"; 
	public static final String PIECE_COPY_INFO = "pieceCopy";
	private final String SEARCH_VIEW_TEXT = "three piece";
	
	private static final String[] PART_NAMES = {
			"Left",  "Center", "Right" };
	private static final String[] PART_IDS = {
			"L", "C", "R" };
	
	public int getPartIndex(String partName) {
		ArrayList<String> myList = new ArrayList<String>(Arrays.asList(PART_NAMES));
		return myList.indexOf(partName);
	}
	
	public boolean isCopyImageAvailable() {		
		return true;
	}
	
	public boolean isFrameRequired() {
		return false;
	}
	
	public boolean isFrameInputsAvailable(List<SkinnableEntity> parts) {
		return false;
	}
	
	public String getSearchViewText() {
		return SEARCH_VIEW_TEXT;
	}
	
	public String getPartName(String partId) {
		String partName = "";
		int i=0;
		for (String part_id: PART_IDS) {
			if (part_id.equalsIgnoreCase(partId)) {
				partName = PART_NAMES[i];
			}
			i++;
		}
		return partName;
	}
	
	public String getCopyElementInfo() {
		return UtilsPlugin.PLUGIN_ID + "." + PART_COUNT +  PIECE_COPY_INFO;
	}
	
	public String getElementTypeId() {
		return ELEMENT_TYPE_ID;
	}

	public int getPartCount() {
		return PART_COUNT;
	}

	public ContentType getClipboardContentDescriptorType() {
		return ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_THREEPIECE;
	}
	
	public String[] getPartNames() {
		return PART_NAMES;
	}
	
	public void setDimensions(List<IImage> partInstances) {
		// compute part's bounds
		int x1, x2, y1, y2;

		BasicEntityImage l = (BasicEntityImage) partInstances.get(0);
		BasicEntityImage  c = (BasicEntityImage) partInstances.get(1);
		BasicEntityImage  r = (BasicEntityImage) partInstances.get(2);
		
		x1 = l.getWidth();
		//y1 = tl.getHeight();

		x2 = x1 + c.getWidth();
		//y2 = y1 + c.getHeight();
		
		l.setBounds(new Rectangle(0, 0, l.getWidth(), l.getHeight()));
		c.setBounds(new Rectangle(x1, 0, c.getWidth(), c.getHeight()));
		r.setBounds(new Rectangle(x2, 0, r.getWidth(), r.getHeight()));
	}
	
	public Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h) {
		
		Dimension dims[] = new Dimension[layouts.size()];
		int i = 0;
		for (Layout l: layouts) {
			Dimension d = new Dimension(l.W(), l.H());
			dims[i++] = d;
		}

		
		Layout l = (Layout) layouts
				.get(0);
		Layout c = (Layout) layouts
				.get(1);
		Layout r = (Layout) layouts
				.get(2);
		

		// check, that parts layout matches element dimensions
		float xRatio, yRatio;

		xRatio = (float) w / (l.W() + c.W() + r.W());
		yRatio = (float) h / (l.H());
		if (xRatio != 1) {
			dims[0].width = (int) (l.W() * xRatio);
			dims[1].width = (int) (c.W() * xRatio);
			dims[2].width = (int) (r.W() * xRatio);
		}

		if (yRatio != 1) {
			dims[0].height = (int) (l.H() * yRatio);
			dims[1].height = (int) (c.H() * yRatio);
			dims[2].height = (int) (r.H() * yRatio);
		}
		
		return dims;		
	}

	public boolean supportsConversionTo(int targetPartCount) {
		
		return false;
	}
}
