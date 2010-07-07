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
package com.nokia.tools.theme.s60.morphing.valuemodels;



import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.nokia.tools.theme.s60.morphing.AknsAlPolyLine1D;
import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.KErrArgument;

class AknsAlValuePoly1D implements BaseValueModelInterface{

	//Animation value UID
	final int KAknsAlValuePoly1DUID = 0x10207629;

//	 Constants for parameter names
	public String KAknsAlValuePoly1DPoints = "p"; 
	public String KAknsAlValuePoly1DFactor = "f";

    private int iValue;
	
	private HashMap<String, String> map=new HashMap<String, String>();
	
	// The numeric implementation of polyline, owned
    AknsAlPolyLine1D iPoly;
	
	
	AknsAlValuePoly1D(){
		// Derived from CBase -> members zeroed
    }

	public void constructL(){
		iPoly = AknsAlPolyLine1D.getInstance();
    }
	
	public static AknsAlValuePoly1D getInstance(){
	    AknsAlValuePoly1D self = new AknsAlValuePoly1D();
		self.constructL();
		return self;
    }
	
	public void Tick( int par1, int aRelative ){
	    Point p = iPoly.getPolyPoint( aRelative << 16 ); // Make relative .32 integer
		
	    iValue = p.y;
    }

	public HashMap getValue(){
		HashMap<Object, Object> map = new HashMap<Object, Object>();
		String s1 = (new Integer(iValue)).toString();
		map.put(AnimationConstants.TYPE_INTEGER,s1);
		return map;
    }
	
	public void setParameters( HashMap<String, String> aParameters ) throws KErrArgument{
		map.putAll(aParameters);
		
		Set aParametersKeySet = aParameters.keySet();
		Iterator aParametersIterator = aParametersKeySet.iterator(); 
		
	    // Iterate over available parameters
	    while( aParametersIterator.hasNext() ){
	        
			final String param = (String)aParametersIterator.next();

	        if( param.equals( KAknsAlValuePoly1DPoints )){
			
				String str = (String)aParameters.get(param);


	            iPoly.setPolyPoints( str );
	        }
	        else if( param.equals( KAknsAlValuePoly1DFactor )){
				
				String tokenstr = (String)aParameters.get(param);
	            // We need 2 or more elements
	            if( tokenstr.length() < 2 ){
					throw new KErrArgument();
	            }
				StringTokenizer tokenizer = new StringTokenizer(tokenstr,",");
				String str = null;
				List<String> list =  new ArrayList<String>();
				while(tokenizer.hasMoreTokens()){
					str = tokenizer.nextToken().trim();
					list.add(str);
				}

				
				try{
		            iPoly.setScaledArea( new Dimension( 1, Integer.parseInt((String)list.get(1))));
				}
				catch(NumberFormatException e){
					throw new KErrArgument();
				}
	        }
	    }
	}
	

	public void Begin(){
	    Point p = iPoly.getPolyPoint(0);
	    iValue = p.y;
    }

	public HashMap<String, String> getParameters() {
		return map;
	}

    public StringBuffer getValueModelString(int prevLayerPosition, int currentPosition, Map effectValues) {
      
        return null;
    }
}
