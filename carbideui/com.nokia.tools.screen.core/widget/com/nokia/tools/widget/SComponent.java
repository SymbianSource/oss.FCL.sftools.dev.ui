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
package com.nokia.tools.widget;

import java.awt.Graphics;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import com.nokia.tools.media.player.IPaintAdapter;

/**
 * Root class in the phone widget hierarchy.
 * 
 * @author shji
 * @version $Revision: 1.4 $ $Date: 2010/04/21 14:45:18 $
 */
public class SComponent extends JPanel implements IPaintAdapter {
	static final long serialVersionUID = 1339738591370459421L;

	private String id;
	private String name;
	private Set<Object> adapters = new HashSet<Object>();

	/**
	 * Default constructor.
	 */
	public SComponent() {
		// for headless mode
		// addNotify();
	}

	/**
	 * For the actual widget to determine if the given child can be added to
	 * itself.
	 * 
	 * @param child the child widget to test.
	 * @return true if the child widget can be added to this container, false
	 *         otherwise.
	 */
	public boolean isChildValid(SComponent child) {
		return false;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paint(java.awt.Graphics)
	 */
	public final void paint(Graphics g) {
		IPaintAdapter adapter = (IPaintAdapter) getAdapter(IPaintAdapter.class);
		try {
			if (adapter != null) {
				adapter.paint(this, g);
			} else {
				paintDefault(g);
			}
		} catch (Exception e) {
			// catch exception otherwise no way to resume
			e.printStackTrace();
		}
	}

	/**
	 * Default paint without any adapters.
	 */
	protected void paintDefault(Graphics g) {
		super.paint(g);
	}

	/**
	 * Registers an adapter.
	 * 
	 * @param adapterHash the adapter hash code.
	 */
	public void registerAdapter(Object adapter) {
		if (adapter == null) {
			return;
		}
		synchronized (adapters) {
			adapters.add(adapter);
		}
	}

	/**
	 * Deregisters an adapter.
	 * 
	 * @param adapterHash the adapter hash code.
	 */
	public void deregisterAdapter(Object adapter) {
		synchronized (adapters) {
			adapters.remove(adapter);
		}
	}

	/**
	 * Finds the adapter with the given type.
	 * 
	 * @param type type of the adapter.
	 * @return the adapter with the given type.
	 */
	public Object getAdapter(Class type) {
		if (type != null) {
			synchronized (adapters) {
				for (Object adapter : adapters) {
					if (type.isAssignableFrom(adapter.getClass())) {
						return adapter;
					}
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.utils.IPaintAdapter#paint(com.nokia.tools.media.utils.IPaintAdapter,
	 *      java.awt.Graphics)
	 */
	public void paint(IPaintAdapter original, Graphics g) {
		paintDefault(g);
	}
}
