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

package com.nokia.tools.media.utils.tooltip;

import org.eclipse.jface.action.IAction;
import org.osgi.framework.Bundle;

public class DefaultTooltipActionProvider implements ITooltipActionProvider {

	public IAction getAction(Bundle contributor, String actionId) {

		if (actionId != null) {
			try {
				Class actionProviderClass = contributor.loadClass(actionId);

				if (IAction.class.isAssignableFrom(actionProviderClass)) {
					return (IAction) actionProviderClass.newInstance();
				} else {
					System.err.println("Invalid actionId:\n" + actionId
							+ " contributor: " + contributor.getBundleId());
					System.err.println("action class must implement interface "
							+ IAction.class.getName());
				}
			} catch (ClassNotFoundException cnfe) {
				System.err.println("Invalid actionId:\n" + actionId
						+ " contributor: " + contributor.getBundleId());
				System.err.println("action class not found: " + actionId);
			} catch (Throwable t) {
				System.err.println("Invalid definition:\n" + actionId
						+ " contributor: " + contributor.getBundleId());
				System.err.println("cannot instantiate action class "
						+ actionId);
				t.printStackTrace();
			}
		}

		return null;
	}

}
