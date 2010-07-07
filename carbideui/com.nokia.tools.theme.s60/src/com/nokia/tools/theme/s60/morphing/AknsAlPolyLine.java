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

package com.nokia.tools.theme.s60.morphing;


import java.awt.Point;

/**
* Base class for PolyLine and PolySpline classes.
* The class is not intended for derivation outside the library.
*
*/

public class AknsAlPolyLine extends AknsAlPolyBase{
	


	public static AknsAlPolyLine getInstance(){
		return new AknsAlPolyLine();
	}

    public static AknsAlPolyLine getInstance(String aPointString ){
		AknsAlPolyLine self = new AknsAlPolyLine();
		self.constructL(aPointString);
		return self;
    }
	
	private void constructL(String aPointString) {
		try {
			setPolyPoints(aPointString);
		} catch (KErrArgument e) {
			e.printStackTrace();
		}
	    calculateLengthsL();
	}
	
	/**
     * Returns the point in poly(line/spline) in scaled area.
     *
     * @param aPosition 32-bit integer representing the position in poly(line/spline).
     *   If aPosition is 0, starting point is returned and if aPosition is 0xffffffff,
     *   the last point is returned.
     * @return Point in poly(line/spline) scaled to the set area or if no
     *   area is set, returned values are in scale from 0 to 1.
     * @since 1.0
     * @param aPos 
     */
	public Point getPolyPoint(int aPos){

			
		// Converting to 32 bit
		long aPosition = ((long)aPos  << 16);
		
		if(iSegmentLengths == null ){
			calculateLengthsL();
		}
		
		long pos64 = aPosition * iSegmentLengths[iPointCount-2];
		long pos = pos64 >>> 32;
		
		//because of possible rounding errors, first check for last pixel
		if (pos >= iSegmentLengths[iPointCount-2]){
			return new Point(iPoints[iPointCount-1].x,iPoints[iPointCount-1].y);
        }
		
		//find the segment, where pos is pointing
		int i = 0; // first index
		int j = iPointCount-2; // last index
		int k;
		while ( i < j ){
			k = (i + j)/2;
			if ( pos < iSegmentLengths[k] ){
				j = k;
			}
			else{
				i = k + 1;
			}
		}
		
		
		int segLength; // real segment length (not cumulative)
		if (i > 0){
			pos -= iSegmentLengths[i-1]; // position between 0...segment length
			segLength = iSegmentLengths[i]-iSegmentLengths[i-1];
		}else{
			segLength =  iSegmentLengths[0]; // we are in first segment
		}

		int deltaX;
		int deltaY;
		boolean negative = false; // must handle negative values differently
		
		if ( (iPoints[i+1].x - iPoints[i].x) < 0){
			deltaX = iPoints[i].x - iPoints[i+1].x; // max 16 bits
			negative = true;
		}else{
			deltaX = iPoints[i+1].x - iPoints[i].x; // max 16 bits
		}

		if((iPoints[i+1].y - iPoints[i].y) < 0){
			deltaY = iPoints[i].y - iPoints[i+1].y; // max 16 bits
			negative = true;
		}else{
			deltaY = iPoints[i+1].y - iPoints[i].y; // max 16 bits
		}

		int deltaXpos = (int)((pos * deltaX) / segLength);
		int deltaYpos = (int)((pos * deltaY) / segLength);

		if (negative){
			deltaXpos = iPoints[i].x - deltaXpos;
			deltaYpos = iPoints[i].y - deltaYpos;
		}else{
			deltaXpos = iPoints[i].x + deltaXpos;
			deltaYpos = iPoints[i].y + deltaYpos;
		}
		
		return calculateScaledPoint(new Point(deltaXpos,deltaYpos));
	}
	
	
	/**
     * Calculates and sets the lengths of poly segments. This is automatically
     * called after the points have been set.
     *
     * @return Returns KErrNoMemory if the lengths couldn't be saved.
     */
	public void calculateLengthsL(){
		// won't get here if not at least 2 points are already set
		iSegmentLengths = new int[iPointCount-1];
		int diffX;
		int diffY;
		int length = 0; // for holding the cumulative length
		for (int i = 0; i < (iPointCount-1); i++){
	        // Points have their x and y coordinates in form 0.16 
			// (e.g. only decimal
	        // part is set), so there won't be overflow when multiplying. 
			// But after
	        // multiply the results can't be added together 
			// without a possibility
	        // for overflow. That's why the coordinates are first divided by 2.
	        diffX = (iPoints[i+1].x - iPoints[i].x) >>> 1; 
			// result can be negative!
	        diffY = (iPoints[i+1].y - iPoints[i].y) >>> 1; 
			// result can be negative!
	
	        // this is not the actual length, but length/2
	        // length might overflow if the poly has over 60000 segments...
	        length += sqrt( diffX*diffX + diffY*diffY ); 
	        iSegmentLengths[i] = length;
		}
	}

}
