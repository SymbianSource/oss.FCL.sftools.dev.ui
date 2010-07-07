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
package com.nokia.tools.theme.s60.ui.propertysheet.tabbed;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.propertysheet.tabbed.MultipleSelectionWidgetSection;
import com.nokia.tools.theme.s60.internal.utilities.TSDataUtilities;

public class StretchSection extends MultipleSelectionWidgetSection {

	private static String[] STRETCH_OPTIONS = {
			IMediaConstants.STRETCHMODE_ASPECT,
			IMediaConstants.STRETCHMODE_STRETCH };

	private static final String PROP_STRETCH = "stretchMode";

	private Map<String, Control> map = new HashMap<String, Control>();
	private SelectionListener checkListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Button check = (Button) e.widget;
			if (check.getSelection()) {
				String key = findKey(map, check);
				if (key != null) {
					StringTokenizer st = new StringTokenizer(key, ".");
					String propname = st.nextToken();
					String val = st.hasMoreTokens() ? st.nextToken() : "";

					// raise modify event
					Event evt = new Event();
					evt.widget = check;
					evt.type = SWT.Modify;
					handleEvent(evt);

					ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
							"Set stretch mode");
					for (IContentData data : getContents()) {
						ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
								.getAdapter(ISkinnableEntityAdapter.class);
						command.add(adapter.getApplyStretchModeCommand(val));
					}
					execute(command);
				}
			}
		}
	};

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		createControls(parent);
	}

	private void createControls(Composite parent) {
		Composite root = getWidgetFactory().createFlatFormComposite(parent);
		root.setLayout(new GridLayout(4, false));
		getWidgetFactory().createLabel(root, "Stretch mode:");

		int MARGIN_WIDTH = 10;
		int MARGIN_HEIGHT = 5;
		int SPACING_W = 10, SPACING_H = 10;
		((GridLayout) root.getLayout()).horizontalSpacing = SPACING_H;
		((GridLayout) root.getLayout()).verticalSpacing = SPACING_W;
		((GridLayout) root.getLayout()).marginHeight = MARGIN_HEIGHT;
		((GridLayout) root.getLayout()).marginWidth = MARGIN_WIDTH;

		String[] STRETCH_LABELS = { IMediaConstants.STRETCHMODE_ASPECT,
				IMediaConstants.STRETCHMODE_STRETCH };
		int i = 0;
		for (String option : STRETCH_OPTIONS) {
			Button check = getWidgetFactory().createButton(root,
					UtilsPlugin.getStretchModeLabel(STRETCH_LABELS[i++]),
					SWT.RADIO);
			check.addSelectionListener(checkListener);
			check.addListener(SWT.Modify, this);
			map.put(PROP_STRETCH + "." + option.toLowerCase(), check);
		}
	}

	@Override
	protected void doHandleEvent(Event event) {
	}

	private String findKey(Map map, Object value) {
		for (Object key : map.keySet())
			if (value == map.get(key))
				return (String) key;
		return null;
	}

	@Override
	protected void doRefresh() {
		IContentData data = getFirstContent();
		if (data == null) {
			map.get(PROP_STRETCH + "." + STRETCH_OPTIONS[0]).getParent()
					.setVisible(false);
			return;
		}
		ISkinnableEntityAdapter adapter = (ISkinnableEntityAdapter) data
				.getAdapter(ISkinnableEntityAdapter.class);
		//NPE
		String value = null;
		if(null != adapter)
			value = adapter.getStretchMode();

		for (Object key : map.keySet()) {
			Control c = map.get(key);
			if (c instanceof Button)
				((Button) c).setSelection(false);
		}

		if (IMediaConstants.STRETCHMODE_NORMAL.equals(value))
			value = "";

		if (StringUtils.isEmpty(value)) {
			IScreenElement screenElement = getFirstElement();
			if (screenElement != null) {
				String elementId = screenElement.getData().getId();
				value = TSDataUtilities.getDefaultStretchMode(elementId);
				Button check = (Button) map.get(PROP_STRETCH
						+ "." + value.toLowerCase()); //$NON-NLS-1$
				if (check != null) {
					check.setSelection(true);
				}
			}
		} else {
			Button check = (Button) map.get(PROP_STRETCH
					+ "." + value.toLowerCase()); //$NON-NLS-1$
			if (check != null) {
				check.setSelection(true);
			}
		}
	}
}
