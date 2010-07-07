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


public class ConstantValueModel implements BaseValueModelInterface { 

    //Animation value UID
    final String KAknsAlValueConstantUID = "0x10207627";
    
//    final String uid="0x10207627";

    // Constants for parameter names
    String KAknsAlValueConstantValue = "c"; 

    private int iValue;
    
    HashMap<String, String> map=new HashMap<String, String>();
    
    public ConstantValueModel(){
        iValue = 0;
    }
    

    
    public void Tick( int par1, int par2){
    }
    
    public HashMap getValue(){
        HashMap<String,String> map=new HashMap<String,String>();
		String s1 = (new Integer(iValue)).toString();
        map.put(AnimationConstants.TYPE_INTEGER,s1);
        return map;
    }
    
    public void setParameters( HashMap<String, String> aParameters )throws KErrArgument{
        
        
        Set aParametersKeySet = aParameters.keySet();
        Iterator aParametersIterator = aParametersKeySet.iterator(); 

        // Iterate over available parameters
        try{
        while( aParametersIterator.hasNext() ){
            final String param = (String)aParametersIterator.next();
    
            if( param.equals( KAknsAlValueConstantValue )){
                try{
                    iValue = new Integer((String)aParameters.get(param)).intValue();;
                }
                catch(NumberFormatException e){
                    throw new KErrArgument();
                }
            }
        }
        map.putAll(aParameters);
        }catch(Exception e)
        {

            e.printStackTrace();
        }
    }
    
    public void Begin(){
    }
    
    public HashMap<String, String> getParameters(){

        return map;
    }

    public StringBuffer getValueModelString(int prevLayerPosition, int currentPosition, Map effectValues) {
        StringBuffer effectStr = new StringBuffer();

        effectStr.append(TAB).append(VALUE).append(SPACE);
        effectStr.append(UID_STR).append(EQUAL).append(KAknsAlValueConstantUID).append(SPACE);
        effectStr.append(TIMINGID).append(EQUAL).append(currentPosition).append(NL);
        
        String constant=(String)map.get(KAknsAlValueConstantValue);
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlValueConstantValue).append(SPACE);
        effectStr.append(constant).append(NL);

        effectStr.append(TAB).append(END);

        return effectStr;
    }

}
