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

package com.nokia.tools.theme.s60.ui.cstore;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.theme.s60.cstore.ComponentStore.StoredElement;

public interface IComponentStore {

	public abstract List<StoredElement> getContents();

	public abstract List<StoredElement> getMatches(String id);

	public abstract List<StoredElement> getFilteredContent(
			List<String> filterTags);

	public abstract List<String> getAvailableTags();

	public abstract void addElements(List<IContentData> elements,
			IProgressMonitor monitor) throws Exception;

	public abstract StoredElement addElement(IContentData element)
			throws Exception;

	public abstract void addElements(List<File> image,
			IProgressMonitor monitor, String... _tags) throws Exception;

	public abstract void deleteElements(List<StoredElement> els);

	public abstract StoredElement deleteElement(StoredElement els);

	public abstract StoredElement addElement(File image, String id,
			String name, String... _tags) throws Exception;

	public abstract void saveStore();

	public abstract void addPropertyChangeListener(PropertyChangeListener l);

	public abstract void removePropertyChangeListener(PropertyChangeListener l);

}