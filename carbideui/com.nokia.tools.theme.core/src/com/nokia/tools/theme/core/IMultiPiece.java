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
package com.nokia.tools.theme.core;

import java.awt.Dimension;
import java.util.List;

import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.SkinnableEntity;

/**
 * Represents the contract for a multipiece element.
 */
public interface IMultiPiece {
	/**
	 * For Core Multipiece logic.
	 * Core to the logic of identifying a multipiece element.
	 * @return number of parts that makes this multipiece element.
	 */
	public int getPartCount();
	
	/**
	 * For Core Multipiece logic.
	 * Every multipiece element type has an Id. This is
	 * specified in the defaultdesign.xml file with a properties tag.
	 * @return String provides the element type (whether it is a nine piece or a 
	 * three piece or so on). 
	 */
	public String getElementTypeId();
	
	/**
	 * Used in CopyGraphicsAction.
	 * @return multipiece-content-type.
	 */
	public ContentType getClipboardContentDescriptorType();
	
	/**
	 * Used in CopyContentDataAction.
	 * @return multipiece name.
	 */
	public String getCopyElementInfo();
		
	/**
	 * For Extended Copy/Paste enablement. 
	 * @return boolean indicating whether extended copy paste is allowed or not.
	 */
	public boolean isCopyImageAvailable();
	
	/**
	 * Some multipiece elements can be converted to single piece or certain list of n-pieces. Hence,
	 * this method allows to know if this element can be converted to a targetPartCount element. 
	 * For example, if targetPartCount is 1 and this method returns true, it means that switching to single piece is allowed. 
	 * @param targetPartCount
	 * @return true if the targetPartCount is valid for this element type.
	 */
	public boolean supportsConversionTo(int targetPartCount);
	
	/**
	 * For Layers View.
	 * Every part has a name to be shown in Layers View.
	 * @return String[] provides the part names (For example, Top-Left, Left, etc).
	 */
	public String[] getPartNames();
	
	/**
	 * Get part name provided a part id.
	 * @param partId
	 * @return part name (like Top-Left)
	 */
	public String getPartName(String partId);
	
	/**
	 * For Search View
	 * @return text to show in search view for the multi piece type. 
	 */
	public String getSearchViewText();
	
	/**
	 * For packaging.
	 * @return boolean mentions whether frames are to be used. Usually its either frames
	 * or each part goes as a separate entry.
	 */
	public boolean isFrameRequired();
	
	/**
	 * For packaging.
	 * @param parts
	 * @return boolean says if we have enough information to construct the frame.
	 */
	public boolean isFrameInputsAvailable(List<SkinnableEntity> parts);
	
	/**
	 * Decides where to place each part while constructing the aggregate image.
	 * @param layouts Provides layout info for each part.
	 * @param w Available Width.
	 * @param h Available Height.
	 * @return array of patched dimensions. This is necessary to resize the components before rendering.
	 */
	public Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h);
	
	/**
	 * Sets the component dimensions using the layout information. This method
	 * encapsulates the logic for rendering element parts.
	 * @param partInstances lists the parts whose width and height are to be set from layout information
	 */
	public void setDimensions(List<IImage> partInstances);
	
	/**
	 * Give the part name (Like Top-Left), this method returns the part index. Used for sorting the parts before rendering.
	 * @param partType
	 * @return location of occurrence of part type within the part name array.  
	 */
	public int getPartIndex(String partType);
}
