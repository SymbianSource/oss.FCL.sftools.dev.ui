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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class implements this interface will provide dynamic content change listener
 * services to the interested clients.
 *
 */
public interface IContentService {
	/**
	 * Adds a new content listener.
	 * 
	 * @param listener the listener to be added.
	 */
	void addContentListener(IContentListener listener);

	/**
	 * Removes the content listener.
	 * 
	 * @param listener the listener to be removed.
	 */
	void removeContentListener(IContentListener listener);
	
	public void fireRootContentChanged(IContent content); 
	
	public void fireContentModified(String[] modifiedContentIds);

	/**
	 * Notification Service implementation
	 */
	public class Stub implements IContentService {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentService#addContentListener(com.nokia.tools.content.core.IContentListener)
		 */
		public void addContentListener(IContentListener listener) {
			if (!contentListeners.contains(listener))
				contentListeners.add(listener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IContentService#removeContentListener(com.nokia.tools.content.core.IContentListener)
		 */
		public void removeContentListener(IContentListener listener) {
			contentListeners.remove(listener);
		}

		protected Set<IContentListener> contentListeners;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.services.IDisposable#dispose()
		 */
		public void dispose() {
			if (contentListeners != null) {
				contentListeners.clear();
			}
		}

		public Stub() {
			contentListeners = new HashSet<IContentListener>(2);
		}

		/**
		 * @param content
		 */
		public void fireRootContentChanged(IContent content) {
			IContentListener[] listeners = contentListeners
					.toArray(new IContentListener[contentListeners.size()]);
			for (IContentListener listener : listeners) {
				listener.rootContentChanged(content);
			}
		}

		/**
		 * @deprecated
		 * @param modifiedContentIds
		 */
		public void fireContentModified(final String[] modifiedContentIds) {
			for (IContentListener listener : contentListeners) {
				try {
					listener.contentModified(new IContentDelta() {

						/* (non-Javadoc)
						 * @see com.nokia.tools.content.core.IContentDelta#getRemovedContent()
						 */
						public List<IContentData> getRemovedContent() {
							return null;
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see com.nokia.tools.content.core.IContentDelta#getAffectedContent()
						 */
						public List<IContentData> getAddedContent() {
							return null;
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see com.nokia.tools.content.core.IContentDelta#getType()
						 */
						public int getType() {
							return IContentDelta.CONTENTCHANGED;
						}

						/*
						 * (non-Javadoc)
						 * 
						 * @see com.nokia.tools.content.core.IContentDelta#getAffectedElementIDs()
						 */
						public List<String> getAffectedElementIDs() {
							return Arrays.asList(modifiedContentIds);
						}

					});
				} catch (Throwable e) {
					CorePlugin.error(e);
				}
			}
		}

		/**
		 * @param delta
		 */
		public void fireContentModified(final IContentDelta delta) {
			for (IContentListener listener : contentListeners) {
				listener.contentModified(delta);
			}
		}
	}

}
