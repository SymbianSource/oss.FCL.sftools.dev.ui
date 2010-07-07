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
package com.nokia.tools.screen.ui.utils;

import org.eclipse.core.runtime.jobs.Job;


/**
 * Used for jobs that we don't want be user-cancellable, 
 * instead we introduce job-specific field for cancellation that 
 * task implementation will check.
 * 
 */
public abstract class CustomCancellableJob extends Job {

	boolean systemCancel;
	
	public CustomCancellableJob(String name) {
		super(name);			
	}

	public boolean isSystemCancel() {
		return systemCancel;
	}

	/**
	 * Cancel this JOB
	 * @param systemCancel
	 */
	public void setSystemCancel(boolean systemCancel) {
		this.systemCancel = systemCancel;
	}


}
