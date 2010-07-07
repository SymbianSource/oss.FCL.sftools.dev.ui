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

package com.nokia.tools.s60.editor.graphics;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	
	public static String Preview_pasteImage;	
	public static String Preview_copyImage;
	public static String Preview_copyImageWEffects;
	public static String Preview_copyResultImage;
	public static String Preview_previewOnlySelected;
	public static String Preview_layersGroupName;
	
	public static String LayerEffectsComposite_addLayer;
	public static String LayerEffectsComposite_deleteLayer;
	public static String LayerEffectsComposite_renameLayerLbl;
	
	public static String LayerEffectsComposite_editImageLbl;
	public static String LayerEffectsComposite_effectsGroupName;
	
	protected static String LayerListDialog_msg;
	protected static String LayerListDialog_title;
	
	public static String LayerEffectWizardPage_pageDescription;
	public static String LayerEffectWizard_windowTitle;
	public static String LayerEffectWizardPage_pageTitle;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
}
