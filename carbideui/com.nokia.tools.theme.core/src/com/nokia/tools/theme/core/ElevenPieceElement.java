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
 * Represents a eleven piece element. Note that for multi-piece elements like
 * nine piece elements, following features behave differently:
 * Layers View
 * Icon View
 * Extended Copy Paste Availability
 * Packaging
 * Search View
 * 
 *
 */
public class ElevenPieceElement implements IMultiPiece {

	private final int PART_COUNT = 11;
	private final String ELEMENT_TYPE_ID = "11 Piece Bitmap"; 
	public static final String PIECE_COPY_INFO = "pieceCopy";
	private final String SEARCH_VIEW_TEXT = "eleven piece";
	
	private static final String[] PART_NAMES = {
			"Left", "Right", "Top", "Bottom", "Top-Left", "Top-Right",
			"Bottom-Left", "Bottom-Right", "Center","Top-Middle-Left","Top-Middle-Right" };
	
	private static final String[] PART_IDS = {
		"L", "R", "T", "B", "Tl", "Tr",
		"Bl", "Br", "C","Ttm","Ttr" };
	
	public int getPartIndex(String partName) {
		ArrayList<String> myList = new ArrayList<String>(Arrays.asList(PART_NAMES));
		return myList.indexOf(partName);
	}
	
	public boolean isFrameRequired() {
		return false;
	}
	
	public boolean isFrameInputsAvailable(List<SkinnableEntity> parts) {
		return false;
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
			
	public boolean isCopyImageAvailable() {		
		return true;
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
	
	public String getSearchViewText() {
		return SEARCH_VIEW_TEXT;
	}

	public ContentType getClipboardContentDescriptorType() {
		return ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_ELEVENPIECE;
	}
	
	public String[] getPartNames() {
		return PART_NAMES;
	}
	
	public void setDimensions(List<IImage> partInstances) {
		// compute part's bounds
		int x1, x2, y1, y2;

		BasicEntityImage tl = (BasicEntityImage) partInstances
				.get(0);
		BasicEntityImage t = (BasicEntityImage) partInstances.get(1);
		BasicEntityImage tr = (BasicEntityImage) partInstances
				.get(2);

		BasicEntityImage l = (BasicEntityImage) partInstances.get(3);
		BasicEntityImage c = (BasicEntityImage) partInstances.get(4);
		BasicEntityImage r = (BasicEntityImage) partInstances.get(5);

		BasicEntityImage bl = (BasicEntityImage) partInstances
				.get(6);
		BasicEntityImage b = (BasicEntityImage) partInstances.get(7);
		BasicEntityImage br = (BasicEntityImage) partInstances
				.get(8);

		x1 = tl.getWidth();
		y1 = tl.getHeight();

		x2 = x1 + c.getWidth();
		y2 = y1 + c.getHeight();

		tl.setBounds(new Rectangle(0, 0, x1, y1));
		t.setBounds(new Rectangle(x1, 0, t.getWidth(), t.getHeight()));
		tr.setBounds(new Rectangle(x2, 0, tr.getWidth(), tr.getHeight()));
		l.setBounds(new Rectangle(0, y1, l.getWidth(), l.getHeight()));
		c.setBounds(new Rectangle(x1, y1, c.getWidth(), c.getHeight()));
		r.setBounds(new Rectangle(x2, y1, r.getWidth(), r.getHeight()));
		bl.setBounds(new Rectangle(0, y2, bl.getWidth(), bl.getHeight()));
		b.setBounds(new Rectangle(x1, y2, b.getWidth(), b.getHeight()));
		br.setBounds(new Rectangle(x2, y2, br.getWidth(), br.getHeight()));
		
		BasicEntityImage ttm = null;
		BasicEntityImage ttr = null;			
		
		//width and height does not matter.	
		ttm = (BasicEntityImage) partInstances
		.get(9);
		ttr = (BasicEntityImage) partInstances
		.get(10);
		
		ttm.setBounds(new Rectangle(x1 + t.getWidth(), 0, ttm.getWidth(), ttm.getHeight()));
		ttr.setBounds(new Rectangle(x1+ t.getWidth() + ttm.getWidth(), 0,ttr.getWidth(),ttr.getHeight()));
	}
	
	public Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h) {
		
		Dimension dims[] = new Dimension[layouts.size()];
		int i = 0;
		for (Layout l: layouts) {
			Dimension d = new Dimension(l.W(), l.H());
			dims[i++] = d;
		}

		Layout t = (Layout) layouts
				.get(1);
		Layout l = (Layout) layouts
				.get(3);
		Layout c = (Layout) layouts
				.get(4);
		Layout r = (Layout) layouts
				.get(5);
		Layout b = (Layout) layouts
				.get(7);
		Layout ttm = (Layout) layouts.get(9);
		Layout ttr = (Layout) layouts.get(10);

		// check, that parts layout matches element dimensions
		float xRatio, yRatio;

		xRatio = (float) w / (l.W() + c.W() + r.W());
		yRatio = (float) h / (t.H() + c.H() + b.H());

		if (xRatio != 1 || yRatio != 1) {
			int desiredWidth = w;
			int desiredHeight = h;

			int desiredCenterW = desiredWidth - l.W() - r.W();
			int desiredCenterH = desiredHeight - t.H() - b.H();
								
			//t.setW(desiredCenterW);
			dims[1].width = desiredCenterW; //assuming tm is always at the middle of t
			
			//l.setH(desiredCenterH);
			dims[3].height = desiredCenterH;
			
			//c.setW(desiredCenterW);
			dims[4].width = desiredCenterW;
			
			//c.setH(desiredCenterH);
			dims[4].height = desiredCenterH;
			
			//r.setH(desiredCenterH);
			dims[5].height = desiredCenterH;
			
			//b.setW(desiredCenterW);					
			dims[7].width = desiredCenterW;

		}
		
		xRatio = (float) w / (l.W() + t.W() + ttm.W() + ttr.W() + r.W());
		if (xRatio != 1) {
			dims[1].width = c.W()/2;
			dims[9].width = c.W()/4;
			dims[10].width = c.W()/4;
		}

		
		return dims;		
	}

	public boolean supportsConversionTo(int targetPartCount) {		
		return false;
	}

}
