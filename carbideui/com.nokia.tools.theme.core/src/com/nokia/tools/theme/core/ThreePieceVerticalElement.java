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
 */
public class ThreePieceVerticalElement implements IMultiPiece {

	private final int PART_COUNT = 3;
	private final String ELEMENT_TYPE_ID = "3 Piece Bitmap (Vertical)"; 
	public static final String PIECE_COPY_INFO = "pieceCopy";
	private final String SEARCH_VIEW_TEXT = "three piece (vertical)";
	
	private static final String[] PART_NAMES = {
			"Top",  "Middle", "Bottom" };
	private static final String[] PART_IDS = {
			"T", "M", "B" };
	
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

		BasicEntityImage t = (BasicEntityImage) partInstances.get(0);
		BasicEntityImage  m = (BasicEntityImage) partInstances.get(1);
		BasicEntityImage  b = (BasicEntityImage) partInstances.get(2);
		
		y1 = t.getHeight();
		//y1 = tl.getHeight();

		y2 = y1 + m.getHeight();
		//y2 = y1 + c.getHeight();
		
		t.setBounds(new Rectangle(0, 0, t.getWidth(), t.getHeight()));
		m.setBounds(new Rectangle(0, y1, m.getWidth(), m.getHeight()));
		b.setBounds(new Rectangle(0, y2, b.getWidth(), b.getHeight()));
	}
	
	public Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h) {
		
		Dimension dims[] = new Dimension[layouts.size()];
		int i = 0;
		for (Layout l: layouts) {
			Dimension d = new Dimension(l.W(), l.H());
			dims[i++] = d;
		}

		
		Layout t = (Layout) layouts
				.get(0);
		Layout m = (Layout) layouts
				.get(1);
		Layout b = (Layout) layouts
				.get(2);
		

		// check, that parts layout matches element dimensions
		float yRatio;

		yRatio = (float) h / (t.H() + m.H() + b.H());
		
	

		if (yRatio != 1) {
			
			dims[0].width = (int) (t.H() * yRatio);
			dims[1].width = (int) (m.H() * yRatio);
			dims[2].width = (int) (b.H() * yRatio);
			 
			
			
		}

		
		return dims;		
	}

	public boolean supportsConversionTo(int targetPartCount) {
		
		return false;
	}
}
