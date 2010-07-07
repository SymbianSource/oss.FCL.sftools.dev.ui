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

package com.nokia.tools.carbide.ui.productsupport.perspectivehack;

import com.nokia.tools.carbide.ui.productsupport.ProductsupportPlugin;
import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.s60.ide.IS60IDEConstants;
import com.nokia.tools.s60.ide.PerspectiveUtil;
import com.nokia.tools.ui.ide.ForcePerspectiveOnStart;
import com.nokia.tools.ui.ide.PerspectiveHackManager;

public class CarbideForcePerspectiveOnStart extends ForcePerspectiveOnStart {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.carbide.ui.productsupport.perspectivehack.ForcePerspectiveOnStart#earlyStartup()
	 */
	@Override
	public synchronized void earlyStartup() {
		super.earlyStartup();
		AbstractContentSourceManager.initContent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.carbide.ui.productsupport.perspectivehack.ForcePerspectiveOnStart#getPerspectiveId()
	 */
	@Override
	protected String getPerspectiveId() {
		return IS60IDEConstants.CARBIDE_UI_PERSPECTIVE_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.ide.ForcePerspectiveOnStart#openPerspective()
	 */
	@Override
	protected void openPerspective() {
		PerspectiveUtil.openPerspective(getPerspectiveId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.ui.ide.ForcePerspectiveOnStart#getPerspectiveHackManager()
	 */
	@Override
	protected PerspectiveHackManager getPerspectiveHackManager() {
		return ProductsupportPlugin.getDefault().getPerspectiveHackManager();
	}
}
