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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.KErrArgument;


public class LinearRangeValueModel implements BaseValueModelInterface {
    
    // Animation value UID
    final String KAknsAlValueLinearRangeUID = "0x10207626";
//    final String uid="0x10207626";

    // Constants for parameter names
    private String KAknsAlValueLinearRangeStart =  "s" ;
    private String KAknsAlValueLinearRangeEnd = "e";
    
    
    private int  iValue;
    private int  iStart;
    private int  iEnd;
    
    HashMap<String, String> map=new HashMap<String, String>();
    
    public LinearRangeValueModel(){
        iValue = 0; 
        iStart = 0;
        iEnd = 255;
    }
    
    public LinearRangeValueModel getInstance(){
        return new LinearRangeValueModel();
    }

    public void Tick( int par1, int aRelative ){

        // It is assumed that arithmetic shifting is supported
        // -> negative values are shifted correctly
        iValue = iStart + ( ( ( iEnd - iStart ) * aRelative ) >> 16 );
        System.out.println("-------------------------------"+iValue);
    }

    public HashMap<String, String> getValue(){
        HashMap<String, String> map=new HashMap<String, String>();
		String s1 = (new Integer(iValue)).toString();
        map.put(AnimationConstants.TYPE_INTEGER,s1);
        return map;
        }
    
    public void setParameters( HashMap<String, String> aParameters ) throws KErrArgument{
        
        
        Set aParametersKeySet = aParameters.keySet(); //returns the set of keys
        Iterator aParametersIterator = aParametersKeySet.iterator(); 


        // Iterate over available parameters
        try{
        while( aParametersIterator.hasNext() ){
            
            final String param = (String)aParametersIterator.next();
    
            if( param.equals( KAknsAlValueLinearRangeStart )){
                
                try{
                    iStart = new Integer((String)aParameters.get(param)).intValue();
                }
                catch(NumberFormatException e){
                    throw new KErrArgument();
                }
            }
            else if( param.equals( KAknsAlValueLinearRangeEnd )){
                
                try{
                    iEnd = new Integer((String)aParameters.get(param)).intValue();
                }
                catch(NumberFormatException e){
                    throw new KErrArgument();
                }
            }
        }
        map.putAll(aParameters);
        }
        catch(Exception e)
        {

            e.printStackTrace();
        }
    }
    
    public HashMap<String, String> getParameters(){

        return map;
    }


    public void Begin(){
        iValue = iStart;
    }

    public StringBuffer getValueModelString(int prevLayerPosition, int currentPosition, Map effectValues) {
        StringBuffer effectStr = new StringBuffer();

        effectStr.append(TAB).append(VALUE).append(SPACE);
        effectStr.append(UID_STR).append(EQUAL).append(KAknsAlValueLinearRangeUID).append(SPACE);
        effectStr.append(TIMINGID).append(EQUAL).append(currentPosition).append(NL);
        
        String start=(String)map.get(KAknsAlValueLinearRangeStart);
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlValueLinearRangeStart).append(SPACE);
        effectStr.append(start).append(NL);

        String end=(String)map.get(KAknsAlValueLinearRangeEnd);
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlValueLinearRangeEnd).append(SPACE);
        effectStr.append(end).append(NL);
        
        effectStr.append(TAB).append(END);

        return effectStr;
    }

    
    public static void main(String args[])
    {
        new LinearRangeValueModel();
    }
}
