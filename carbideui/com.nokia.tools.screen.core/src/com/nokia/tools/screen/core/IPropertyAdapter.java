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
package com.nokia.tools.screen.core;

public interface IPropertyAdapter {
	int GENERAL = 1;
	int ADVANCED = 1 << 1;

	IPropertyAdapter GENERAL_ADAPTER = new IPropertyAdapter() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IPropertyAdapter#supports(int)
		 */
		public boolean supports(int type) {
			return (GENERAL & type) != 0;
		}
	};
	IPropertyAdapter ADVANCED_ADAPTER = new IPropertyAdapter() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IPropertyAdapter#supports(int)
		 */
		public boolean supports(int type) {
			return (ADVANCED & type) != 0;
		}
	};
	IPropertyAdapter FULL_ADAPTER = new IPropertyAdapter() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IPropertyAdapter#supports(int)
		 */
		public boolean supports(int type) {
			return (GENERAL & type) != 0 || (ADVANCED & type) != 0;
		}
	};
	IPropertyAdapter NULL_ADAPTER = new IPropertyAdapter() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.core.IPropertyAdapter#supports(int)
		 */
		public boolean supports(int type) {
			return false;
		}
	};

	boolean supports(int type);
}
