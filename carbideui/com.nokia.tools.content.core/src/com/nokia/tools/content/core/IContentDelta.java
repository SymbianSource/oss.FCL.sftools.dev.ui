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
package com.nokia.tools.content.core;

import java.util.List;

/**
 * ContentDelta represents changes in the IContentData tree structure
 * 
 */
public interface IContentDelta {
	/**
	 * Delta of this type allways means that the getAffectedElementIDs elements
	 * have been replaced with getAffectedContent. all removed from parent
	 * contentdata and on their place, instead the final removed ID (the last
	 * ID) from the list are inserted elements from getAffectedContent list - of
	 * content. Note that there is no content in the relevant parentcontent
	 * assicoated to IDs from getAffectedElementsIds list.
	 */
	public static final int REPLACED = 0x1;

	/**
	 * Delta of this type means that the IContentData element has been added to
	 * it's parent
	 */
	public static final int ADDED = 0x2;

	/**
	 * The elements has been removed from its parent
	 */
	public static final int REMOVED = 0x4;

	/**
	 * The elements' content has been altered
	 */
	public static final int CONTENTCHANGED = 0x8;

	/**
	 * @return the change type: {@link #REPLACED}, {@link #ADDED},
	 *         {@link #REMOVED} or {@link #CONTENTCHANGED}.
	 */
	public int getType();

	/**
	 * @return a list of element ids that are affected by this change.
	 */
	public List<String> getAffectedElementIDs();

	/**
	 * @return the affected content data. added or updated
	 */
	public List<IContentData> getAddedContent();
	
	/**
	 * used in case of remove operation
	 * @return
	 */
	public List<IContentData> getRemovedContent();
	
	
	
}
