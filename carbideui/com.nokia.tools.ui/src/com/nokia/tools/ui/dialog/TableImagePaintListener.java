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
package com.nokia.tools.ui.dialog;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Listener for table paint events. This will generate the column images only
 * when they are visible, also this is responsible for disposing the images.
 * 
 */
public class TableImagePaintListener implements Listener {
	private IResourcePageManager manager;
	private Table table;

	/**
	 * Constructs a new paint listener.
	 * 
	 * @param manager the resource page manager.
	 * @param table the table control.
	 */
	public TableImagePaintListener(IResourcePageManager manager, Table table) {
		this.manager = manager;
		this.table = table;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		Rectangle bounds = table.getClientArea();
		for (final TableItem item : table.getItems()) {
			if (item.getBounds().intersects(bounds)
					&& (item.getImage() == null || item.getImage() == manager
							.getDefaultResourceImage())) {
				item.setImage(manager.createImage(item.getData(),
						IResourcePageManager.ICON_SIZE,
						IResourcePageManager.ICON_SIZE, false));
				item.addDisposeListener(new DisposeListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
					 */
					public void widgetDisposed(DisposeEvent e) {
						if (item.getImage() != null
								&& item.getImage() != manager
										.getDefaultResourceImage()) {
							item.getImage().dispose();
						}
					}

				});

			}
		}
	}
}