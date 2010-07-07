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
import java.util.Random;
import java.util.Set;

import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.KErrArgument;


public class RandomValueModel implements BaseValueModelInterface{
    
        //Animation value UID
        final String KAknsAlValueRandomUID = "0x10207628";
    
        //Constants for parameter names
        String KAknsAlValueRandomMin = "min";
        String KAknsAlValueRandomMax = "max";
    
        private long iCurrentSeed;
        //private double iCurrentSeed;
        private int iValue;
        private int iMin;
        private int iMax;
        HashMap<String, String> map=new HashMap<String, String>();
        Random random = null;
        
        public RandomValueModel(){
            iCurrentSeed = 0;
            iValue = 0;
            iMin = 0;
            iMax = 255;
        }
        
        public static RandomValueModel getInstance(){
            return new RandomValueModel();
        }
        
        public void Tick( int par1, int par2){
            iValue =iMin + Math.abs( (int)(((random.nextLong()) % ( iMax - iMin ))) );
           
        }
    
        public HashMap getValue(){
            HashMap<String, String> map  = new HashMap<String, String>();
			String s1 = (new Integer(iValue)).toString();
            map.put(AnimationConstants.TYPE_INTEGER, s1);
            return map;
        }
        
        public void setParameters( HashMap<String, String> aParameters ) throws KErrArgument{
            
            
            Set aParametersKeySet = aParameters.keySet();
            Iterator aParametersIterator = aParametersKeySet.iterator(); 
    
            // Iterate over available parameters
            try{
            while( aParametersIterator.hasNext() ){
                final String param = (String)aParametersIterator.next();
        
                if( param.equals( KAknsAlValueRandomMin )){
                    try{
                        iMin = new Integer((String)aParameters.get(param)).intValue();
                    }
                    catch(NumberFormatException e){
                        throw new KErrArgument();
                    }
                }
                else if( param.equals( KAknsAlValueRandomMax )){
                    
                    try{
                        iMax = new Integer((String)aParameters.get(param)).intValue();
                    }
                    catch(NumberFormatException e){
                        throw new KErrArgument();
                    }
                }
             }
        
            // Sanity check
            if( iMax < iMin )
                throw new KErrArgument();
        
            // Randomize the starting seed
            iCurrentSeed = System.currentTimeMillis();
            map.putAll(aParameters);
            }catch(Exception e)
            {

                e.printStackTrace();
            }
        }
    
        public void Begin(){
            iCurrentSeed =  System.currentTimeMillis();
            random = new Random();
            random.setSeed(iCurrentSeed);
        }
        
        public HashMap<String, String> getParameters(){
            //HashMap map = new HashMap();
            map.put(KAknsAlValueRandomMin, new Integer(iMin).toString());
            map.put(KAknsAlValueRandomMax, new Integer(iMax).toString());
            return map;
        }

        public StringBuffer getValueModelString(int prevLayerPosition, int currentPosition, Map effectValues) {
            StringBuffer effectStr = new StringBuffer();

            effectStr.append(TAB).append(VALUE).append(SPACE);
            effectStr.append(UID_STR).append(EQUAL).append(KAknsAlValueRandomUID).append(SPACE);
            effectStr.append(TIMINGID).append(EQUAL).append(currentPosition).append(NL);
            
            String start=(String)map.get(KAknsAlValueRandomMin);
            effectStr.append(TAB).append(TAB);
            effectStr.append(INT).append(SPACE);
            effectStr.append(KAknsAlValueRandomMin).append(SPACE);
            effectStr.append(start).append(NL);

            String end=(String)map.get(KAknsAlValueRandomMax);
            effectStr.append(TAB).append(TAB);
            effectStr.append(INT).append(SPACE);
            effectStr.append(KAknsAlValueRandomMax).append(SPACE);
            effectStr.append(end).append(NL);
            
            effectStr.append(TAB).append(END);

            return effectStr;
        }

}
