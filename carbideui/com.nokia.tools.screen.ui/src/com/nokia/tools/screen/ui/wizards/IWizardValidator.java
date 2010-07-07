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
package com.nokia.tools.screen.ui.wizards;

import java.util.ArrayList;

/**
 * 
 * This class should be sub classed by client who would like 
 * to use the common wizard but have different
 * validations to be run through for inputs.
 * See also com.nokia.tools.screen.ui.wizardValidator through which
 * concrete implementation should be contributed.
 *
 */
public interface IWizardValidator {

	//This string should be passed by clients if there are no errors
	// after the validate method.
	public static String NO_ERROR = "";
	
		
	/**
	 * @param input the object whoich can be used to get the inputs
	 *        most likely this would the wizard page itself.
	 * @return String - NO_ERROR if there are no errors, and 
	 * 		  otherwise the error String
	 */
	public String validate(Object input);

	
	/**
	 * return the wizard classes in which the client wants to execute
	 * the above validate method
	 * @return the ArrayList - contains the wizard classes in which this 
	 * 						   method should be executed.
	 */
	public ArrayList<Class> getWizardClasses();
	
	

}
