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
package com.nokia.tools.theme.s60.morphing.timemodels;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.KErrArgument;

public class RealTimingModel implements BaseTimingModelInterface {
 
	//Timing model UID
	public String KAknsAlTimingModelReatTimeUID = "0x10207624";
	public int KAlMaxDurationVal = 65536; // In milliseconds, ~65 seconds
	
    /// The length of one active duration, in milliseconds
    private int iDuration;

    /// The total animation length, in milliseconds
	private int iTotalDuration;

    /// Elapsed time since the start of the animation, in milliseconds
	private int iElapsedTime;

    /// The current relative value, fixed point 16.16 format.
	private int iRelative;

	/// Used wrapping mode.
	private boolean iWrap;
    
    private HashMap<String, String> attributeMap=new HashMap<String, String>();
	
//	int duration = 0;
//	int repeatCount = 0;
//	int repeatDuration = 0;
//    boolean wrap = false;


	//Constants for parameter names
	public static String KAknsAlRealTimeDuration = "d";
	public static String KAknsAlRealTimeRepeatCount = "rc";
	public static String KAknsAlRealTimeRepeatDuration = "rd";
	public static String KAknsAlRealTimeWrap = "w";
	
	
//	 CLASS DECLARATION
	/**
	Real-time timing model that advances by keeping track on elapsed time between
	subsequent updates. The next named parameters are supported:
	- "d", duration
	- "rc", repeat count
	- "rd", repeat duration
	- "w", wrap mode

	Timing model configuration works as follows:

	Duration undefined:
	D    RC    RD     Single duration  Total duration
	0    Inf   0      Inf              Inf
	0    Inf   Def    RD               RD
	0    Inf   Inf    Inf              Inf
	0    0     0      0                0
	0    0     Def    RD               RD
	0    0     Inf    Inf              Inf
	0    Def   0      0                0
	0    Def   Def    RD               RD
	0    Def   Inf    Inf              Inf

	Duration defined:
	D    RC    RD    Single duration   Total duration
	Def  Inf   0     D                 Inf
	Def  Inf   Def   D                 RD
	Def  Inf   Inf   D                 Inf
	Def  0     0     D                 D
	Def  0     Def   D                 RD
	Def  0     Inf   D                 Inf
	Def  Def   0     D                 D * RC
	Def  Def   Def   D                 MIN(D * RC, RD)
	Def  Def   Inf   D                 D * RC

	Duration infinite:
	D    RC    RD    Single duration   Total duration
	Inf  Inf   0     Inf               Inf
	Inf  Inf   Def   RD                RD
	Inf  Inf   Inf   Inf               Inf
	Inf  0     0     Inf               Inf
	Inf  0     Def   RD                RD
	Inf  0     Inf   Inf               Inf
	Inf  Def   0     Inf               Inf
	Inf  Def   Def   RD                RD
	Inf  Def   Inf   Inf               Inf

	The above tables can reduced to:
	    D    RC    RD    Single duration   Total duration
	#1  0    *     Def   RD                RD
	#2  0    *     Inf   Inf               Inf
	#3  0    Inf   0     Inf               Inf
	#4  0    0     0     0                 0
	#5  0    Def   0     0                 0
	#6  Def  Inf   0     D                 Inf
	#7  Def  Inf   Def   D                 RD
	#8  Def  Inf   Inf   D                 Inf
	#9  Def  0     0     D                 D
	#10 Def  0     Def   D                 RD
	#11 Def  0     Inf   D                 Inf
	#12 Def  Def   0     D                 D * RC
	#13 Def  Def   Def   D                 MIN(D * RC, RD)
	#14 Def  Def   Inf   D                 D * RC
	#15 Inf  *     Def   RD                RD
	#16 Inf  *     Inf   Inf               Inf
	#17 Inf  *     0     Inf               Inf

	Abbreviations used:
	D   duration
	RC  repeat count
	RD  repeat duration
	Def defined
	0   undefined (zero in resulting durations)
	Inf infinite
	*   any value (defined, undefined, infinite)

	Note: Case #3 follows SMIL, it could have been defined to produce the same
	result as #4 and #5. Cases #4 and #5 could have been implemented as impossible
	case that causes a leave. Current implementation just sets the total duration
	to zero which produces an animation that finishes immediately.

	Configuration value formats:
	- Duration, repeat count and repeat duration are all defined as follows:
	  < 0, value is infinite
	    0, value is undefined
	  > 0, value is defined
	- Duration is milliseconds, when defined
	- Repeat count is the number of repeated durations, when defined
	- Repeat duration is milliseconds, when defined
	- Because fixed point calculations are used, durations and repeat durations
	  larger than KAlMaxDurationVal are clamped to KAlMaxDurationVal. It is
	  possible to produce longer total durations by using repeat count.

	Wrap:
	If duration is repeated, wrapping defines how successive durations are mapped
	to relative values ranges.
	- If ETrue the sequence is wrapped [0,1][1,0][0,1]...
	- If EFalse the sequence is tiled [0,1][0,1][0,1]...
	- The default wrap mode is tiled. Wrapping is ignored if the duration is not
	  repeated.

	Relative value format:
	- 32-bit unsigned integer, fixed point, 16.16 format.
	- Relative value range [0, 65536] maps effectively to real number range [0, 1].
	- Relative value is quaranteed to be in range [0, 65536].
	- If animation duration is infinite the relative value is always 0.

	
	*/

	public RealTimingModel(){
	    iDuration =  0 ;
	    iTotalDuration = 0;
	    iElapsedTime = 0; 
	    iRelative = 0;
	    iWrap = false;
	}    
	
	public RealTimingModel getInstance(){
		return new  RealTimingModel(); 
	}
	
	private void Configure( int aD, int aRC, int aRD, boolean aWrap )
    {
	    iElapsedTime = 0;
	    iWrap = aWrap;
	    iRelative = 0;

	    // Because we use fixed point arithmetic, duration and repeat duration
	    // absolute value must be clamped.
	    if( Defined( aRD ) & (aRD > KAlMaxDurationVal) ){
			aRD = KAlMaxDurationVal;
        }
	
	    if( Defined( aD ) & (aD > KAlMaxDurationVal) ){
			aD = KAlMaxDurationVal;
        }

    // Determine duration and total duration
    if( Defined( aD ) ){ // Cases from #6 to #14
        // Duration is the same in all cases
        iDuration = aD;

        if( Infinite( aRC ) )
            {
            if( Undefined( aRD ) ) // #6
                {
                iTotalDuration = -1;
                }
            else if( Defined( aRD ) ) // #7
                {
                iTotalDuration = aRD;
                }
            else // aRD must be infinite, #8
                {
                iTotalDuration = -1;
                }
            }
        else if( Undefined( aRC ) )
            {
            if( Undefined( aRD ) ) // #9
                {
                iTotalDuration = aD;
                }
            else if( Defined( aRD ) ) // #10
                {
                iTotalDuration = aRD;
                }
            else // aRD must be infinite, #11
                {
                iTotalDuration = -1;
                }
            }
        else // aRC must be defined
            {
            if( Undefined( aRD ) ) // #12
                {
                iTotalDuration = aD * aRC;
                }
            else if( Defined( aRD ) ) // #13
                {
                // total duration = MIN( D * RC, RD )
                iTotalDuration = aD * aRC < aRD ? aD * aRC: aRD;
                }
            else // aRD must be infinite, #14
                {
                iTotalDuration = aD * aRC;
                }
            }
        }
    else if( Infinite( aD ) ) // Cases from #15 to #17
        {
        // Repeat count is ignored

        if( Defined( aRD ) ) // #15
            {
            iDuration      = aRD;
            iTotalDuration = aRD;
            }
        else // #16 and #17
            {
            // Undefined and infinite produce the same result
            iDuration      = -1;
            iTotalDuration = -1;
            }
        }
    else // Duration is undefined, cases from #1 to #5
        {
        // Cases, #4 and #5
        if( Undefined( aRD ) & (!Infinite( aRC )) )
            {
            // These are basically impossible cases
            iDuration      = 0;
            iTotalDuration = 0;
            }
        // Case #1
        else if( Defined( aRD ) )
            {
            iDuration      = aRD;
            iTotalDuration = aRD;
            }
        else // Must be cases #2 and #3
            {
            iDuration      = -1;
            iTotalDuration = -1;
            }
         }
    }
	
	
	public void setParameters( HashMap<String, String> aParameters ) throws KErrArgument { 

		
        try{
        
		Set aParametersKeySet = aParameters.keySet();
		Iterator aParametersIterator = aParametersKeySet.iterator(); 
        
//        attributeMap.putAll(aParameters);
		//Collection aParametersValues = aParameters.values();

	    // Iterate over available parameters
	    while( aParametersIterator.hasNext()) {
			
			//param is the key
	        final String param = (String)aParametersIterator.next();

	        if( param.equals( KAknsAlRealTimeDuration )){
				//Checking whether the contents of the string are of type number or not. 
				try{
					iDuration= new Integer((String)aParameters.get(param)).intValue();
				}
				catch(NumberFormatException e){
					throw new KErrArgument(); 
				}
	        }
	        else if( param.equals( KAknsAlRealTimeRepeatCount )){
				try{
		            iElapsedTime = new Integer((String)aParameters.get(param)).intValue();
				}
				catch(NumberFormatException e){
					throw new KErrArgument(); 
				}
	        }
	        else if( param.equals( KAknsAlRealTimeRepeatDuration )){
				try{
		            iTotalDuration = new Integer((String)aParameters.get(param)).intValue();
				}
				catch(NumberFormatException e){
					throw new KErrArgument(); 
				}
	        }
	        else if(param.equals( KAknsAlRealTimeWrap )){
				try{
		            iWrap= ( new Boolean((String)aParameters.get(param)).booleanValue() ) ? true: false;
				}
				catch(NumberFormatException e){
					throw new KErrArgument(); 
				}
	        }
	    }

	    // Configure the model
	    Configure( iDuration, iElapsedTime, iTotalDuration, iWrap );
        attributeMap.putAll(aParameters);
        }catch(Exception e)
        {
//            ThemeOptionPane.showMessageDialog(null,"Invalid Data");
            e.printStackTrace();
        }
    }
	
	public HashMap<String, String> getParameters(){
//		HashMap map = new HashMap();
//		
//        attributeMap.put(KAknsAlRealTimeDuration, new Integer(iDuration).toString());
//        attributeMap.put(KAknsAlRealTimeRepeatCount,new Integer(iElapsedTime).toString());
//        attributeMap.put(KAknsAlRealTimeRepeatDuration,new Integer(iTotalDuration).toString());
        attributeMap.put(KAknsAlRealTimeWrap, new Boolean(iWrap).toString());
//        attributeMap.remove(EffectConstants.LAYOUT);
//		return map;
        return attributeMap;
	}
	
	
	public void Tick( HashMap map ){
        
        
        int aDeltaTime= new Integer((String)map.get(AnimationConstants.DELTA_TIME)).intValue();

		iElapsedTime = iElapsedTime + aDeltaTime;
		
		// Cases #4 and #5 are the only ones that will produce undefined durations
		// in Configure. This kind of animation will never run properly
		if( Undefined( iDuration ) | (Undefined( iTotalDuration )) )
		    return;
		
		// Starting from here the durations are either defined or infinite
		
		// Check if the the total duration has been exceeded
		if( IsFinished() )
		    return;
		
		// Relative value is undefined if duration is either undefined or infinite
		if( !Defined( iDuration ) ) // iDuration <= 0
		{
			iRelative = 0;
		}
		else{ // Duration is defined, relative value can be calculated
		
		    // Note: iDuration is always > 0 here
		
		    // Elapsed time and duration always >= 0 -> conversion ok
		    int durationPos = (int)( iElapsedTime % iDuration );
		    boolean ascending = true;
		
		    // When wrapping, odd durations are descending, even ascending
		    if( iWrap && (( iElapsedTime / iDuration ) % 2) != 0 ) // Remainder != 0
		        ascending = false;
		
		    
		    // Calculate the relative value
		    if( ascending )
		    {
				iRelative = ( durationPos << 16 ) / (int)( iDuration );
		    }
		    else // Current range must be descending
		    {
				iRelative = ( ( iDuration - durationPos ) << 16 ) / (int)( iDuration );
		    }
		}
	}



	public int getTimeValue(){
		return iRelative;
    }
	
	public boolean IsFinished(){
	    if( Defined( iTotalDuration ) && (iElapsedTime > iTotalDuration) )
	    {
			return true;
	    }
		return false;
    }
	
	public void Begin(){
	    iElapsedTime = 0;
	    iRelative = 0;
    }

	private static boolean Infinite (int aValue){
        return ( aValue < 0 ? true: false );
	}
	
    private static boolean Undefined( int aValue )
    {
		return ( aValue == 0 ? true: false );
    }

    private static boolean Defined( int aValue )
    {
		return ( aValue > 0 ? true: false );
    }

    public StringBuffer getTimingModelString(int currentPosition, int prevPos) {
        StringBuffer effectStr = new StringBuffer();
//        String inputA = "" + currentPosition + FSLASH + RGB;
//        String inputB = "none";
//        String output = "" + currentPosition + FSLASH + RGB;
        
//        String timingId=(String)map.get(ThemeTag.ELEMENT_TIMINGMODEL_REF);
        effectStr.append(TAB).append(TIMINGMODEL).append(SPACE);
        effectStr.append(UID_STR).append(EQUAL).append(KAknsAlTimingModelReatTimeUID).append(SPACE).append(NL);
        
        String duration=(String)attributeMap.get(KAknsAlRealTimeDuration);
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlRealTimeDuration).append(SPACE);
        effectStr.append(duration).append(NL);

        String rd=(String)attributeMap.get(KAknsAlRealTimeRepeatDuration);
        // null is not allowed
        if (rd == null) {
        	rd = "0";
        }
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlRealTimeRepeatDuration).append(SPACE);
        effectStr.append(rd).append(NL);
        
        String rc=(String)attributeMap.get(KAknsAlRealTimeRepeatCount);
        if (rc == null) {
        	rc = "-1";
        }
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlRealTimeRepeatCount).append(SPACE);
        effectStr.append(rc).append(NL);

        String wrap=(String)attributeMap.get(KAknsAlRealTimeWrap);
        String w="0";
        if(new Boolean(wrap).booleanValue())
            w="1";
        else
            w="0";
        effectStr.append(TAB).append(TAB);
        effectStr.append(INT).append(SPACE);
        effectStr.append(KAknsAlRealTimeWrap).append(SPACE);
        effectStr.append(w).append(NL);
        effectStr.append(TAB).append(END).append(NL);

        return effectStr;
    }
	
}
