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
package com.nokia.tools.platform.theme;
 
 //import java.lang.Exception;

/**
 * The class defines the exception thrown while parsing the xml files.
 *
 * 
 */
 public class ThemeException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
     * Constructor
     */
   	
 	 public ThemeException(Throwable ex) {
 		
 		super(ex);
 	 }
 	
     public ThemeException (){
         super();
     }
     
     /**
     * Constructor
     * @param message Error message
     */
     public ThemeException (String message){
         super(message);
     }
 }