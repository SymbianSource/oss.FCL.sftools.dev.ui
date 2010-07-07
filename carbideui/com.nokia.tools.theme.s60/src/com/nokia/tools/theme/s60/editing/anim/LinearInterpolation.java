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
package com.nokia.tools.theme.s60.editing.anim;

/**
 * 
 * helper class - Linear interpolation engine - gets an input 
 * as array of control points and return value in given time point.
 */
public class LinearInterpolation {
	
	/**
	 * Returns y-value in given x-value, performing linear interpolation. 
	 * @param points
	 * @param t
	 * @return
	 */
	public static float getInterpolatedValue(long[][] points, long t) {
		
		long start = 0, end = 0;
		if (points.length == 0)
			return 0;
		if (points.length == 1)
			return points[0][1];
		if (t < points[0][0])
			return points[0][1];
		if (t > points[points.length -1][0])
			return points[points.length -1][1];
		//find two points we want to interpolate between
		for (; start < points.length - 1; start++) {
			long time = points[(int) start][0];
			long time2 = points[(int) (start + 1)][0];
			if (time <= t && time2 >= t) {
				end = start + 1;
				break;
			}
		}
		//we have start and end
		long startTime = points[(int) start][0];
		long endTime = points[(int) end][0];
		long startVal = points[(int) start][1];
		long endVal = points[(int) end][1];
		float alpha = (t - startTime) / (float)(endTime - startTime);
		
		return ( (1-alpha) * startVal + (alpha) * endVal);
	}


}
