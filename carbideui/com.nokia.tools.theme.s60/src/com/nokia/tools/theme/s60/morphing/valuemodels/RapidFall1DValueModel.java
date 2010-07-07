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

/**
 * 
 */


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


public class RapidFall1DValueModel implements BaseValueModelInterface{

    //Animation value UID
    final String KAknsAlValuePoly1DUID = "0x10207629";

//   Constants for parameter names
    public String KAknsAlValuePoly1DPoints = "p"; 
    public static String rawString = "RAW";
    public String KAknsAlValuePoly1DFactor = "f";
	final String SIZEBOUNDPARAM="SIZEBOUNDPARAM";
	final String VALUEID="VALUEID";
	final String FACTOR="f";
    final String FLAGS="FLAGS";
    final String W="W";

    private int iValue;
    HashMap<String, String> map=new HashMap<String, String>();
    
    // The numeric implementation of polyline, owned
    AknsAlPolyLine1D iPoly;
    
    
    public RapidFall1DValueModel(){
        // Derived from CBase -> members zeroed
        this.constructL();
    }

    public void constructL(){
        iPoly = AknsAlPolyLine1D.getInstance();
    }
    
    public AknsAlValuePoly1D getInstance(){
        AknsAlValuePoly1D self = new AknsAlValuePoly1D();
        self.constructL();
        return self;
    }
    
    public void Tick( int par1, int aRelative ){
        Point p = iPoly.getPolyPoint( aRelative << 16 ); // Make relative .32 integer
        iValue = p.y;
    }

    public HashMap getValue(){
        HashMap<String, String> map=new HashMap<String, String>();
		String s1 = (new Integer(iValue)).toString();
        map.put(AnimationConstants.TYPE_INTEGER,s1);
		System.out.println(" Rapid Fall get Value called " + iValue);
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
            


                iPoly.setPolyPoints( ((String)aParameters.get(param)) );
            }
            else if( param.equals( KAknsAlValuePoly1DFactor )){
                
                String tokenstr = (String)aParameters.get(param);
               
                if( tokenstr.length() < 2 ){
                    throw new KErrArgument();
                }
                StringTokenizer tokenizer = new StringTokenizer(tokenstr,",");
                String str = null;
                List<Object> list =  new ArrayList<Object>();
                while(tokenizer.hasMoreTokens()){
                    str = tokenizer.nextToken().trim();
                    if(str.indexOf(';')!=-1)
                        str=str.substring(0,str.indexOf(';'));
                    list.add(str);
                }

                
                try{
                    iPoly.setScaledArea( new Dimension( 1, Integer.parseInt((String)list.get(1))));
                }
                catch(NumberFormatException e){
                    e.printStackTrace();
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
        StringBuffer effectStr = new StringBuffer();

        effectStr.append(TAB).append(VALUE).append(SPACE);
        effectStr.append(UID_STR).append(EQUAL).append(KAknsAlValuePoly1DUID).append(SPACE);
        effectStr.append(TIMINGID).append(EQUAL).append(currentPosition).append(NL);
        


        String points=(String)map.get(KAknsAlValuePoly1DPoints);
        String p=makePointsString(points);
        effectStr.append(TAB).append(TAB);

        effectStr.append(rawString).append(SPACE).append(KAknsAlValuePoly1DPoints).append(SPACE);
        effectStr.append(p).append(NL);
        
		effectStr.append(TAB).append(END).append(NL);
		effectStr.append(TAB).append(SIZEBOUNDPARAM).append(SPACE).append(FACTOR).append(SPACE)
        .append(VALUEID).append(EQUAL).append(currentPosition).append(SPACE).append(FLAGS).append(EQUAL).append(W)
        .append(SPACE).append(NL);

        return effectStr;
    }

    private String makePointsString(String points) 
    {
        String p=new String(points);
        StringBuffer pointString= new StringBuffer();
        String singlePoint="";
        while(p.indexOf(';')!=-1)
        {
            if(p.indexOf(':')==-1)
            {
                p=p.substring(0,p.length()-1);
                singlePoint=p;
            }
            else
            {
                singlePoint=p.substring(0,p.indexOf(':'));  
                p=p.substring(p.indexOf(':')+1,p.length());
            }
            
            String x=singlePoint.substring(0,singlePoint.indexOf(','));
            String y=singlePoint.substring(singlePoint.indexOf(',')+1,singlePoint.length());
            
            /*
             * 1. The x and y points are supposed to be in a scale from 0x0000 (for value 0)
             * to 0xFFFF (for value 1) -- The range is uniformly distributed between 0 and 1.
             * 
             * 2. The x and y value must be written seperately seperated by space (implictly says
             * that there will be even number of entries)
             * 
             * 3. The x and y value must be written to the text file using unicode. Meaning the
             * hexa value must be put within '<' and '>'. For example 0 will be <0x0000> and 1 
             * will be <0xFFFF>
             */
            double valueX = Double.valueOf(x);
            double valueY = Double.valueOf(y);
            
          
            
            String hexX = "0x" + Integer.toHexString((int)valueX).toUpperCase();
            String hexY = "0x" + Integer.toHexString((int)valueY).toUpperCase();
            

            String currentPoint = hexX +" " +hexY ;
            
            pointString=pointString.append(currentPoint).append(" ");
            
        }
        if(pointString != null ){
            pointString = pointString.insert(0,"[ ").append("]");
        }
            
        return pointString.toString();
    }

}