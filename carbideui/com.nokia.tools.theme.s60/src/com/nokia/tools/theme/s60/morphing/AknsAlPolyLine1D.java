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

public final class AknsAlPolyLine1D extends AknsAlPolyBase{
	
	//Constructor
	private AknsAlPolyLine1D(){
		
	}
	
    private void constructL( String aPointString ){
		try {
			setPolyPoints1DL(aPointString);
		} catch (KErrArgument e) {
			e.printStackTrace();
		}
    }

	
    public static AknsAlPolyLine1D getInstance(){
		return new AknsAlPolyLine1D(); 
    }

    public static AknsAlPolyLine1D getInstance( String aPointString ){
		AknsAlPolyLine1D self = new AknsAlPolyLine1D();
		self.constructL( aPointString );
		return self;
    }
	
    /**
     * Returns the point in poly(line/spline) in scaled area.
     *
     * @param aPosition 32-bit integer representing the position in poly(line/spline).
     *   If aPosition is 0, starting point is returned and if aPosition is 0xffffffff,
     *   the last point is returned.
     *
     * @return Point in poly(line/spline) scaled to the set area or if no
     *   area is set, returned values are in scale from 0 to 1.
     */
	
    public Point getPolyPoint( int aPosition ){

		// linepoints have x-coordinates between 0...16bit,
	    // so we only need to use upper 16 bits from aPosition

	    int pos = (aPosition >>> 16);

		
	    // find the line, where pos is pointing
	    int i = 0; // first index
	    int j = iPointCount-1; // last index
		
	    int k;
	    while ( i < j ){
	        k = (i + j)/2;
	        if ( pos < iPoints[k].x ){
	            j = k;
	        }
	        else{
	            i = k + 1;
	        }
	    }
	    // i = j = wanted line's end point index
		
		
		int deltaX ;
		int calculatedY;
		int deltaY; 
		if(i == 0){
			int points = 0;
			int points1 = 0;
		    deltaX = iPoints[points].x - iPoints[points1].x;
			// always positive, 16 bits
		    // might be negative, so use flag to get correct results
		    boolean negativeY = false;;
		    if ((iPoints[points].y - iPoints[points1].y) < 0){
		        deltaY = iPoints[points1].y - iPoints[points].y;
		        negativeY = true;
		    }
		    else{
		        deltaY = iPoints[points].y - iPoints[points1].y;
		    }
		    pos -= iPoints[points1].x;

		    if (pos == 0 || deltaX ==0){ // we are in line's first pixel
		        return new Point((iPoints[points1].x * iAreaSize.width) >>> 16,
		                  (iPoints[points].y * iAreaSize.height) >>> 16);
		    }

		    calculatedY = (deltaY * pos) / deltaX; 
			// first get delta from start point
		    if (negativeY){
		        calculatedY = iPoints[points1].y - calculatedY; 
				// line going downwards
		    }
		    else{
		        calculatedY = iPoints[points1].y + calculatedY; 
				// line going upwards
		    }

		}
		else{
		    deltaX = iPoints[i].x - iPoints[i-1].x; 
			// always positive, 16 bits
		    // might be negative, so use flag to get correct results
		    boolean negativeY = false;;
		    if ((iPoints[i].y - iPoints[i-1].y) < 0){
		        deltaY = iPoints[i-1].y - iPoints[i].y;
		        negativeY = true;
		    }
		    else{
		        deltaY = iPoints[i].y - iPoints[i-1].y;
		    }
		    pos -= iPoints[i-1].x;

		    if (pos == 0 || deltaX == 0){ // we are in line's first pixel

		        return new Point((iPoints[i-1].x * iAreaSize.width) >>> 16,
		                  (iPoints[i-1].y * iAreaSize.height >>> 16) );

		    }

		    calculatedY = (deltaY * pos) / deltaX; 
			// first get delta from start point
		    if (negativeY){
		        calculatedY = iPoints[i-1].y - calculatedY; 
				// line going downwards
		    }
		    else{
		        calculatedY = iPoints[i-1].y + calculatedY; 
				// line going upwards
		    }

		}
		
		


		return calculateScaledPoint(new Point(aPosition >>> 16,calculatedY));
    }

	@Override
	void calculateLengthsL() {
	}

}
