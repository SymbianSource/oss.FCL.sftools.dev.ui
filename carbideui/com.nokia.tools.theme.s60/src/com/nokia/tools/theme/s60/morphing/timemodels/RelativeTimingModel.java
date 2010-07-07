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



import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.nokia.tools.theme.s60.morphing.AnimationConstants;
import com.nokia.tools.theme.s60.morphing.KErrArgument;

//CLASS DECLARATION
/**
* Implements relative timing model. The timing model is relative to anchors in
* real-time. Timespans define the location of anchors. Possible timespans are
* hour, day, week and month. For example, when the used timespan is hour the
* start of real-time hour is used as an anchor and relative value goes from [0,
* 1] over this timespan.
*
* Timing model also supports slices and wrapping which affect how relative
* values are generated over one timespan.
*
*/

public class RelativeTimingModel implements BaseTimingModelInterface{

	
	//Timing model UID
	final String KAknsAlTimingModelRelativeUID = "0x10207625";

	//Constants for parameter names
	public static String KAknsAlRelativeTimeSpan = "t";
	public static String KAknsAlRelativeSlices = "s";
	public static String KAknsAlRelativeWrap = "w";
	
	public final int KAlMaxRelative = 65536;
	
    public enum TimeSpan {EHour,EDay ,EWeek,EMonth };
    
    private HashMap<String, String> map=new HashMap<String,String>();

	/**
        * Supported named parameters are:
        * - "t", enumeration TTimeSpan, used timespan
        * - "s", integer >= 1, number of slices in timespan
        * - "w", boolean (0/1), wrapping toggle
        *
        * Timespan, "t", must be one of the values in TTimeSpan.
        *
        * Slicing defines the number of slices in the used timespan. For
        * example, using timespan hour and 4 slices will effectively divide
        * hour in 15 minute slices (results in four relative value spans over
        * hour, [0, 1][1, 0][0, 1][1, 0] with wrapping,
        * [0, 1][0, 1][0, 1][0, 1] without wrapping).
        *
        * Number of slices must be >= 1.
        *
        * If wrapping is true, every second slice is reversed (relative value
        * goes over range [1, 0] instead of [0, 1]). Wrapping affects only one
        * slice. To achieve continuous wrapping over multiple timespans an even
        * value for slices should be used. Note that wrapping is ignored when
        * slice count is 1.
        *
        * The default parameter values are:
        * - "t" = EHour
        * - "s" = 1
        * - "w" = EFalse (0)
        */
	
		/// Time span type
	    private TimeSpan iTimeSpan;
		
	    /// Number of slices in time span, >= 1
		private int iSlices;
			
        /// Used wrapping mode.
		private boolean iWrap;

        /// The current relative value, fixed point 16.16 format.
		private int iRelative;
		
		private int KAknsAlRelativeTimeSpanValue;
        
//        File f=new File("C:\\test.txt");
//        BufferedWriter fS=null;
        


		public RelativeTimingModel(){
			iTimeSpan =  TimeSpan.EHour; 
		    iSlices = 1;
		    iWrap = false;
		    iRelative = 0; 

	    }
		
		public RelativeTimingModel getInstance(){
		    return new RelativeTimingModel();
	    }
		
		public void setParameters( HashMap<String, String> aParameters ) throws KErrArgument{

            
			Set aParametersKeySet = aParameters.keySet();
			Iterator aParametersIterator = aParametersKeySet.iterator(); 
//            map.putAll(aParameters);
			// Iterate over available parameters
            try{
                
		    while( aParametersIterator.hasNext() ){

				final String param = (String)aParametersIterator.next();

		        if( param.equals( KAknsAlRelativeTimeSpan )){
					
					
					try{
						KAknsAlRelativeTimeSpanValue = new Integer((String)aParameters.get(param)).intValue();
					}
					catch(NumberFormatException e){
						throw new KErrArgument(); 
					}
					
					switch(KAknsAlRelativeTimeSpanValue){
						case 0 :
							iTimeSpan = TimeSpan.EHour;
							break;
						case 1 :	
							iTimeSpan = TimeSpan.EDay;
							break;
						case 2 :	
							iTimeSpan = TimeSpan.EWeek;
							break;
						case 3 :	
							iTimeSpan = TimeSpan.EMonth;
							break;
						default:
			            	throw new KErrArgument();
					}
		        }
		        else if( param.equals( KAknsAlRelativeSlices )){
					try{
			            iSlices = new Integer((String)aParameters.get(param)).intValue();
					}
					catch(NumberFormatException e){
						throw new KErrArgument(); 
					}

		            if( iSlices < 1 )
						throw new KErrArgument();

		        }
		        else if( param.equals( KAknsAlRelativeWrap )){
					try{
			            iWrap = new Boolean((String)aParameters.get(param)).booleanValue();//((((String)aParameters.get(param))) != "true" ) ? true: false;
					}
					catch(NumberFormatException e){
						throw new KErrArgument(); 
					}

		        }
		    }
            map.putAll(aParameters);
            }catch(Exception e)
            {
//                ThemeOptionPane.showMessageDialog(null,"Invalid Data");
                e.printStackTrace();
            }
	    }
		
		public HashMap<String, String> getParameters(){
//			HashMap map = new HashMap();
//			map.put(KAknsAlRelativeTimeSpan,new Integer(KAknsAlRelativeTimeSpanValue));
//			map.put(KAknsAlRelativeSlices, new Integer(iSlices));
//			map.put(KAknsAlRelativeWrap, new Boolean(iWrap));
			return map;
		}
		
		
		//Used for setting the limits of the time interval
		public void Tick( HashMap map ){
            
            Calendar aStamp=(Calendar)map.get(AnimationConstants.CALENDER);

			//*******Return the current time in the default time zone and locale*******/ 	
			Calendar cur = aStamp; // Calendar.getInstance();aStamp.getTime()
			Calendar startStamp = Calendar.getInstance();
			Calendar endStamp = Calendar.getInstance();
	
		    switch( iTimeSpan )
		        {
                case EHour:{
                    startStamp.set( cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH), cur.get(Calendar.HOUR_OF_DAY), 0, 0 );
//                    
//                    startStamp.set(Calendar.HOUR_OF_DAY,cur.get(Calendar.HOUR_OF_DAY));
//                    startStamp.set(Calendar.MINUTE,0);
//                    startStamp.st(Calendar.S)
                    endStamp.set( cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH), cur.get(Calendar.HOUR_OF_DAY), 0, 0 );
                    endStamp.set(Calendar.HOUR,startStamp.get(Calendar.HOUR)+1);
//                    endStamp.set(Calendar.MINUTE,0);
                }
	            break;
		        case EDay:{
                    startStamp.set( cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH), 0, 0, 0 );
//                    int hourOfDay = cur.get(Calendar.HOUR_OF_DAY);
//                    int hoursToAdd = 24 - hourOfDay; 
//                    startStamp.add(Calendar.HOUR_OF_DAY,-hourOfDay  );endStamp.getTime();
                    endStamp.set( cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH)+1, 0, 0, 0 );
//                    endStamp.add(Calendar.HOUR_OF_DAY, 24 );
		        }
		        break;
		        case EWeek:{
                    startStamp.set( cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
//                    Date date=startStamp.getTime();
                    int dayOfWeek = cur.get(Calendar.DAY_OF_WEEK)-1;
                    int daysToAdd  = 7-dayOfWeek;
                    startStamp.add(Calendar.DAY_OF_WEEK, -dayOfWeek);
                    endStamp.set( cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                    endStamp.add(Calendar.DAY_OF_WEEK,daysToAdd);
	            }
	            break;
		        case EMonth:{
                    startStamp.set(  cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), 0, 0, 0, 0 );
                    endStamp.set(startStamp.get(Calendar.YEAR),startStamp.get(Calendar.MONTH)+1,0,0,0,0);
//                    endStamp.add(Calendar.MONTH,1);
//                    endStamp.set(cur.get(Calendar.YEAR),cur.get(Calendar.MONTH)+1,0,0,0,0);
//                    int dayOfMonth = cur.get(Calendar.DAY_OF_MONTH);
//                    Date date=startStamp.getTime();
//                    startStamp.add(Calendar.DAY_OF_MONTH, -dayOfMonth);
//                    int monthOfYear = cur.get(Calendar.MONTH);
//                    int totalNumberOfDaysForMonth = 0;
//                    switch(monthOfYear){
//                            case 0:
//                            case 2:
//                            case 4:
//                            case 6:
//                            case 7:
//                            case 9:
//                            case 11:
//                                totalNumberOfDaysForMonth = 31;
//                                break;
//                            case 3:
//                            case 5:
//                            case 8:
//                            case 10:
//                                totalNumberOfDaysForMonth = 30;
//                                break;
//                            case 1:{
//                                    int year = cur.get(Calendar.YEAR);
//                                    if((year % 400) == 0){
//                                                totalNumberOfDaysForMonth = 29;
//                                    }
//                                    else{
//                                            if((year % 100) == 0){
//                                                        totalNumberOfDaysForMonth = 28;
//                                            }
//                                            else {
//                                                        if((year % 4) == 0 ){
//                                                        totalNumberOfDaysForMonth = 29;
//                                                    }
//                                                    else{
//                                                    totalNumberOfDaysForMonth = 28;
//                                            }
//                                    }
//                            }
//                    }
//                  break;
//                }
//                int daysOfMonthToAdd =  totalNumberOfDaysForMonth - dayOfMonth ;
//                endStamp.add(Calendar.DAY_OF_MONTH, +daysOfMonthToAdd);
//                System.out.println(" The month end is " + endStamp.get(Calendar.MONTH));

	            }
	            break;
		        default:
		            iRelative = 0;
		            return;
		        }
			
			
			   // Determine second spans ( start -> end, start -> cur )
		    int intervalToCur = 0;
			int intervalToEnd = 0;

			long tempaStampStampMillis = aStamp.getTimeInMillis();
			long tempStartStampMillis  = startStamp.getTimeInMillis();
			long tempEndStampMillis  = endStamp.getTimeInMillis();
			
			try{
				long temp1 = tempaStampStampMillis - tempStartStampMillis;
				intervalToCur = (int)temp1/1000; 
			}
			catch(Exception e){
		        iRelative = KAlMaxRelative;
		        return;
			}
			
			try{
				long temp1 = tempEndStampMillis - tempStartStampMillis;
				intervalToEnd = (int)temp1/1000;
			}
			catch(Exception e){
		        iRelative = KAlMaxRelative;
		        return;
			}
	
		    // Floating point calculations used intentionally (relative timing model is
		    // called rarely).
		    double secsToCur = (double)( intervalToCur);
			double secsToEnd = (double)( intervalToEnd);
			double posOnSlices = ( secsToCur / secsToEnd ) * (double)( iSlices );
	
		    int sliceNo = 0;
			double relative = 0.0;

			// Current slice is the integer part of posOnSlices
			try{
				sliceNo = (int)Math.floor(posOnSlices);
			}
			catch(Exception e){
		        iRelative = 0;
		        return;
			}
	
		    // Relative value is the posOnSlices fractional
			try{
				relative = posOnSlices-Math.floor(posOnSlices);
			}
			catch(Exception e){
		        iRelative = 0;
				return;
			}
		    // Reverse every second slice. Wrapping has effect only when there are
		    // more than one slices.
		    if( iWrap && ( sliceNo % 2 ) == 1 ){
		        relative = 1.0 - relative;
		    }
	
		    // Convert to fixed point, relative is always in range [0.0, 1.0]
            
            try{
//                String s=new Double(relative).toString();
//                fS.write("time "+relative);
//                fS.newLine();
            }catch(Exception e)
            {
                e.printStackTrace();
            }
		    iRelative = (int)( ( (double)(KAlMaxRelative) * relative ) + 0.5 );
		}
		
		public int getTimeValue(){
			return iRelative;
	    }
		
		public void Begin(){
		    // Time span type defines the starting point of a time span. System time or
		    // time stamp defines the current poisition in time span -> begin is
		    // irrelevant
	    }

        public StringBuffer getTimingModelString(int curPos, int prevPos) {
            StringBuffer effectStr = new StringBuffer();
//          String inputA = "" + currentPosition + FSLASH + RGB;
//          String inputB = "none";
//          String output = "" + currentPosition + FSLASH + RGB;
          
//          String timingId=(String)map.get(ThemeTag.ELEMENT_TIMINGMODEL_REF);
          effectStr.append(TAB).append(TIMINGMODEL).append(SPACE);
          effectStr.append(UID_STR).append(EQUAL).append(KAknsAlTimingModelRelativeUID).append(SPACE).append(NL);
          
          String duration=(String)map.get(KAknsAlRelativeTimeSpan);
          effectStr.append(TAB).append(TAB);
          effectStr.append(INT).append(SPACE);
          effectStr.append(KAknsAlRelativeTimeSpan).append(SPACE);
          effectStr.append(duration).append(NL);

          String rd=(String)map.get(KAknsAlRelativeSlices);
          effectStr.append(TAB).append(TAB);
          effectStr.append(INT).append(SPACE);
          effectStr.append(KAknsAlRelativeSlices).append(SPACE);
          effectStr.append(rd).append(NL);
          
          String wrap=(String)map.get(KAknsAlRelativeWrap);
          String w="0";
          if(new Boolean(wrap).booleanValue())
              w="1";
          else
              w="0";
          effectStr.append(TAB).append(TAB);
          effectStr.append(INT).append(SPACE);
          effectStr.append(KAknsAlRelativeWrap).append(SPACE);
          effectStr.append(w).append(NL);
          
          effectStr.append(TAB).append(END).append(NL);

          return effectStr;
      }


}
