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
package com.nokia.tools.theme.s60.editing.utils;

import java.awt.Dimension;
import java.util.List;

import com.nokia.tools.platform.layout.Layout;

public class PatchUtils {
	
	public static Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h) {
		
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
									
				
				dims[1].width = desiredCenterW; //assuming tm is always at the middle of t
				
				
				dims[3].height = desiredCenterH;
				
				
				dims[4].width = desiredCenterW;
				
				
				dims[4].height = desiredCenterH;
				
				
				dims[5].height = desiredCenterH;
				
									
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

	public static Dimension[] patchNinePieceLayout(List<Layout> layouts, int w, int h) {
		
		Dimension dims[] = new Dimension[9];
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

		// check, that parts layout matches element dimensions
		float xRatio, yRatio;

		xRatio = (float) w / (l.W() + c.W() + r.W());
		yRatio = (float) h / (t.H() + c.H() + b.H());

		if (xRatio != 1 || yRatio != 1) {
			int desiredWidth = w;
			int desiredHeight = h;

			int desiredCenterW = desiredWidth - l.W() - r.W();
			int desiredCenterH = desiredHeight - t.H() - b.H();
								
			
			dims[1].width = desiredCenterW;
			
			
			dims[3].height = desiredCenterH;
			
			
			dims[4].width = desiredCenterW;
			
			
			dims[4].height = desiredCenterH;
			
			
			dims[5].height = desiredCenterH;
			
								
			dims[7].width = desiredCenterW;

		}
		return dims;		
}
	public static Dimension[] patchThreePieceLayout(List<Layout> layouts, int w, int h) {
		
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
		
	

		if (xRatio != 1) {
			
			dims[0].width = (int) (l.W() * xRatio);
			dims[1].width = (int) (c.W() * xRatio);
			dims[2].width = (int) (r.W() * xRatio);
			 
			
			
		}

		
		return dims;		
}

}
