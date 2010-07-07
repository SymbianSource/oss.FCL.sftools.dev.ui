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

package com.nokia.tools.theme.s60.effects;

import java.util.HashMap;

import com.nokia.tools.theme.s60.morphing.timemodels.BaseTimingModelInterface;
import com.nokia.tools.theme.s60.morphing.valuemodels.BaseValueModelInterface;

/**
 *
 * The class used for loading all the effects classes while performing the image aggregation
 */
public class ClassLoader {
   
   //map to store classes once it is loaded
   private static HashMap<String,ImageProcessor> imgProcessorMap=new HashMap<String,ImageProcessor>();

   /**
    * Given the class name this api loads the class and returns the instance.
    * This API is for loading the effects classes while image aggragation
    * @param className
    * @return
    */
   public static ImageProcessor getConversionsInstance( String className )
    {
        try
        {
            Class keyValueClass = Class.forName( className );
            if(imgProcessorMap.containsKey(className))
                return ( ImageProcessor ) imgProcessorMap.get(className);
            Object instance = keyValueClass.newInstance();
            imgProcessorMap.put(className,(ImageProcessor)instance);
            return ( ImageProcessor ) instance;
        }
        catch ( Exception ex )
        {
            System.out.println( ex.getMessage() );
            return null;
        }
    }
   
   /**
    * Loads the valueModel classes 
    * @param className
    * @return
    */
   public static BaseValueModelInterface getFlavourInstance( String className )
   {
       try
       {
           Class keyValueClass = Class.forName( className );

           Object instance = keyValueClass.newInstance();
           return ( BaseValueModelInterface ) instance;
       }
       catch ( Exception ex )
       {
           System.out.println( ex.getMessage() );ex.printStackTrace();
           return null;
       }
   }
   
   /**
    * Loads the timing model classes
    * @param className
    * @return
    */
   
   public static BaseTimingModelInterface getTimingModelInstance( String className )
   {
       try
       {
           Class keyValueClass = Class.forName( className );

           Object instance = keyValueClass.newInstance();
           return ( BaseTimingModelInterface ) instance;
       }
       catch ( Exception ex )
       {
           System.out.println( ex.getMessage() );
           ex.printStackTrace();
           return null;
       }
   }
   
   

}
