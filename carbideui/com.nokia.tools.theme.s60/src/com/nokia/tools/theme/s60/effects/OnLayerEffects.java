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

import java.awt.image.RenderedImage;
import java.util.HashMap;

// import com.sun.media.ui.ColumnData;

/*
 * It changes marked #change are in order to remove dependecy on
 * layer/effect dialog ui code.
 */

/**
 * To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OnLayerEffects {

	

	public static RenderedImage ProcessImage(String effect, RenderedImage img,
			HashMap map) {

		RenderedImage image = null;
		if (effect == null)
			return img;
		EffectObject eObj1 = EffectObject.getEffect(effect);
		ImageProcessor im = ClassLoader.getConversionsInstance(eObj1
				.getAttributeAsString("className"));
		if (im != null) {
			image = im.doProcessing(img, map);
		}
		return image;
	}
}
