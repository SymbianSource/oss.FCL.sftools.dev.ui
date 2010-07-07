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

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class AknsAlPolyBase {
	
	//Constructor 
    public AknsAlPolyBase(){
	    iPoints = null; // not needed, but just in case
    }

    protected Dimension iAreaSize;

	protected int iPointCount; // number of points

	protected Point[] iPoints;

	// for holding cumulative lengths of poly segments - last length is the
    // whole poly's length
    int[] iSegmentLengths ;
	
    /**
     * Internally all positions are scaled to spacing from 0 to 1.
     * This method sets the area where positions are scaled
     * before returning.
     *
     * @param aAreaSize
     *
     * @since 1.0
     */
    public void setScaledArea( Dimension aAreaSize ){
	    iAreaSize = aAreaSize;
    }
	
    /**
     * Helper method for scaling point to scaled area.
     * Possible combinations:
     * ScaledArea(x,y) -> return TPoint(x*TFixed(x), y*TFixed(y))
     * ScaledArea(x,0) -> return TPoint(x*Tfixed(x), TFixed(y))
     * ScaledArea(0,y) -> return TPoint(Tfixed(x), y*TFixed(y))
     * ScaledArea(0,0) -> return TPoint(Tfixed(x), TFixed(y))
     *
     * @param aPoint input point with (TFixed(x), TFixed(y))
     *
     * @return TPoint scaled to given scaled area
     *
     * @since 1.0
     */
     public Point calculateScaledPoint( Point aPoint ){
			Point ret = new Point(0,0);
			
		    if (iAreaSize.width == 0){
		        ret.x = aPoint.x;
		    }
		    else{
		        ret.x = (aPoint.x * iAreaSize.width) >>> 16;
		    }
		    if(iAreaSize.height== 0){
		        ret.y = aPoint.y;
		    }
		    else{

		        ret.y = (aPoint.y * iAreaSize.height) >>> 16;
		    }
		    return ret;
     }
	 
     /**
      * Sets the points for Poly(Line/Spline).
      * TDesC holds 16-bit integers, which represent values from 0 to 1.
      * Format of string is: point1_x, point1_y, point2_x, point2_y,...
      *
      * @return Leaves with KErrArgument, if the string has odd number of items
      *   or if 2 adjacent points are the same (zero length line/spline segment).
      *   Leaves with KErrNoMemory, if there were not enough memory for new points.
      * @since 1.0
      * @param aPointString 
      */
      public void setPolyPoints( String aPointString ) throws KErrArgument{

		StringTokenizer tokenizer = new StringTokenizer(aPointString,":");
		String str = null;
//		new ArrayList();
		List<String> list1 =  new ArrayList<String>();
		while(tokenizer.hasMoreTokens()){
			str = tokenizer.nextToken().trim();
			//list.add(str);
			StringTokenizer tokenizer1 = new StringTokenizer(str ,",");
			String str1 = null;
			while(tokenizer1.hasMoreTokens()){
				str1 = tokenizer1.nextToken().trim();
                if(str1.indexOf(';')!=-1)
                    str1=str1.substring(0,str1.indexOf(';'));
				list1.add(str1);
			}
		}
		

		iPointCount = (int)(list1.size()/2);
		if(iPointCount < 4)
			throw new KErrArgument();
			
		 iPoints = new Point[iPointCount];

		Point p = new Point(0,0);

        
        
		p.x = Integer.parseInt((String)list1.get(0));
		p.y = Integer.parseInt((String)list1.get(1));

		iPoints[0] = p;

		int oldX = iPoints[0].x;
	    int oldY = iPoints[0].y;

	    // parse numbers from the descriptor
	    for (int i = 1; i < iPoints.length;i++){ // first point already set
			
			Point point = new Point(0,0);

			point.x = Integer.parseInt((String)list1.get(i*2)); 
			point.y = Integer.parseInt((String)list1.get(i*2+1)); 
			iPoints[i] = point;
			
	        if (oldX == iPoints[i].x && oldY == iPoints[i].y)
				 throw new KErrArgument();

	        oldX = iPoints[i].x;
	        oldY = iPoints[i].y;
	     }
		
		if(iPoints.length < 4 ){
			throw new KErrArgument();
		}

		  
      }
	  
      /**
       * Sets the points for 1-dimensional Poly(Line/Spline).
       * TDesC holds 16-bit integers, which represent values from 0 to 1.
       * Format of string is: point1_x, point1_y, point2_x, point2_y,...
       *
       * @return Leaves with KErrArgument, if the string has odd number of items
       *   or if 2 adjacent are in wrong order (points must be in ascending order).
       *   Leaves with KErrNoMemory, if there were not enough memory for new points.
       * @since 1.0
       * @param aPointString 
       */
       public void setPolyPoints1DL( String aPointString ) throws KErrArgument{
		   
			StringTokenizer tokenizer = new StringTokenizer(aPointString,":");
			String str = null;
			new ArrayList();
			List<String> list1 =  new ArrayList<String>();
			while(tokenizer.hasMoreTokens()){
				str = tokenizer.nextToken().trim();
				//list.add(str);
				StringTokenizer tokenizer1 = new StringTokenizer(str ,",");
				String str1 = null;
				while(tokenizer1.hasMoreTokens()){
					str1 = tokenizer1.nextToken().trim();
					list1.add(str1);
				}
			}



			
			
			iPointCount = (int)(list1.size()/2);
			if(iPointCount < 4)
				throw new KErrArgument();
				
			 iPoints = new Point[iPointCount];



			 
			Point p = new Point(0,0);
			p.x = Integer.parseInt((String)list1.get(0)); 
			p.y = Integer.parseInt((String)list1.get(1));

			iPoints[0] = p;


		    int oldX = iPoints[0].x;

		    // parse numbers from the descriptor
		    for (int i = 1; i < iPoints.length;i++){ // first point already set
				
				Point point = new Point(0,0);
				point.x = Integer.parseInt((String)list1.get(i*2)); 
				point.y = Integer.parseInt((String)list1.get(i*2+1));
				iPoints[i] = point;
				
		        if ( iPoints[i].x < oldX )
					 throw new KErrArgument();

		        oldX = iPoints[i].x;
		     }
			
			if(iPoints.length < 4 ){
				throw new KErrArgument();
			}


       }
	   
       /**
        * Fast square root for integers. If the input is 16.16 fixed point,
        * the output is 8.8 fixed point. Use SqrtFixed to get correct output.
        *
        * @param aParam
        *
        * @return Integer square root.
        *
        * @since 1.0
        */
        public int sqrt( int aParam ){
		    int t;
		    int b;
		    int c=0;

		    for (b=0x40000000;b!=0;b>>=2){
		        t = c + b;
		        c >>= 1;
		        if (t <= aParam){
		            aParam -= t;
		            c += b;
		        }
		    }
		    return c;
        }
		
        /**
        * Fast square root for integers. This version returns 16.16 fixed value.
        *
        * @param aParam
        *
        * @return Integer square root.
        *
        * @since 1.0
        */


        /**
        * Returns the point in poly(line/spline) in scaled area.
        *
        * @param aPosition 32-bit integer representing the position in poly(line/spline).
        *   If aPosition is 0, starting point is returned and if aPosition is 0xffffffff,
        *   the last point is returned.
        *
        * @return Point in poly(line/spline) scaled to the set area or if no
        *   area is set, returned values are in scale from 0 to 1.
        *
        * @since 1.0
        */
        abstract Point getPolyPoint( int aPosition );

        /**
        * Calculates and sets the lengths of poly segments. This is automatically
        * called after the points have been set.
        *
        * @return Returns KErrNoMemory if the lengths couldn't be saved.
        */
        abstract void calculateLengthsL();

}
