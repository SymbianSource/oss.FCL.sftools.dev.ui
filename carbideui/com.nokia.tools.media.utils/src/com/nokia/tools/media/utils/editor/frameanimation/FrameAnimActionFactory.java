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
/**
 * 
 */
package com.nokia.tools.media.utils.editor.frameanimation;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * To be extended in frame animation editor
 */
public abstract class FrameAnimActionFactory {
	public abstract Action getNewFrameAction(ISelectionProvider provider);

	public abstract Action getRemoveFrameAction(ISelectionProvider provider);

	public abstract Action getDistributeAnimationTimeAction(
			ISelectionProvider provider);
	
	public abstract Action getCopyFrameAction( ISelectionProvider provider );
	
	public abstract Action getPasteFrameAction( ISelectionProvider provider);
	
	public abstract IContributionManager getPopupMenuContribution(ISelectionProvider provider);
	
}
