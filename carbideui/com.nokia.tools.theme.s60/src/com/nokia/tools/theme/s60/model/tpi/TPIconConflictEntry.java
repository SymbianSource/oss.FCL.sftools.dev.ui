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

package com.nokia.tools.theme.s60.model.tpi;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data about the conflict between 2 Third Party Icons.
 * This is during the computation of conflicts between the Third Party Icons
 * which is then indicated in the display in the Third Party Icons preference page.
 */

public class TPIconConflictEntry {

	/**
	 * This enumeration defines the fields for Third Party Icon on which
	 * the collision can occur. 	
	 * @author thanthry
	 */
	public enum TPIconConflitingField {
		TP_ICON_NAME, 				// When collision happens on the TPI icon name
		TP_ICON_ID,   				// When collision happens on the TPI id value
		TP_ICON_MAJORID_MINORID;	// When collision happens on Major and Minor IDs
	}

	/**
	 * The field value on which the conflict is present.
	 */
	private List<TPIconConflitingField> conflitingFields = new ArrayList<TPIconConflitingField>();	
	
	/**
	 * The third party icon with which this icon conflicts.
	 */
	private ThirdPartyIcon conflictThirdPartyIcon = null;
	
	public TPIconConflictEntry(ThirdPartyIcon conflictThirdPartyIcon){
		this.conflictThirdPartyIcon = conflictThirdPartyIcon;
	}
	
	/**
	 * @return
	 */
	public List<TPIconConflitingField> getConflictingFields() {
		return conflitingFields;
	}

	/**
	 * @param conflitingField
	 */
	public void addConflitingField(TPIconConflitingField conflitingField) {
		conflitingFields.add(conflitingField);
	}

	public ThirdPartyIcon getConflictThirdPartyIcon(){
		return conflictThirdPartyIcon;
	}
	
	@Override
	public int hashCode() {
		return conflictThirdPartyIcon.hashCode() + conflitingFields.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TPIconConflictEntry){
			return ((TPIconConflictEntry)obj).conflictThirdPartyIcon.equals(conflictThirdPartyIcon)
			       && ((TPIconConflictEntry)obj).conflitingFields.equals(conflitingFields);
		}
		return false;
	}
}
