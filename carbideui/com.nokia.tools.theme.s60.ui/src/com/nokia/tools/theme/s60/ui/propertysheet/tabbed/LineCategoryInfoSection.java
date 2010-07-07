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

import org.eclipse.gef.commands.ForwardUndoCompoundCommand;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.screen.ui.propertysheet.tabbed.MultipleSelectionWidgetSection;
import com.nokia.tools.theme.content.ILineAdapter;

public class LineCategoryInfoSection extends MultipleSelectionWidgetSection {
	private Button drawLinesButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#doHandleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	protected void doHandleEvent(Event e) {
		if (e.widget == drawLinesButton) {
			ForwardUndoCompoundCommand command = new ForwardUndoCompoundCommand(
					"Set draw lines");
			for (IContentData data : getContents()) {
				ILineAdapter adapter = (ILineAdapter) data
						.getAdapter(ILineAdapter.class);
				command.add(adapter.getApplyDrawLinesCommand(drawLinesButton
						.getSelection()));
			}
			execute(command);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#doRefresh()
	 */
	@Override
	protected void doRefresh() {
		IContentData data = getFirstContent();
		ILineAdapter adapter = (ILineAdapter) data
				.getAdapter(ILineAdapter.class);
		drawLinesButton.setSelection(adapter.drawLines());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.wst.common.ui.properties.internal.provisional.TabbedPropertySheetPage)
	 */
	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		int leftCoordinate = getStandardLabelWidth(composite,
				new String[] { Messages.Label_DrawLines });

		// draw lines
		CLabel lineLabel = getWidgetFactory().createCLabel(composite,
				Messages.Label_DrawLines);

		drawLinesButton = getWidgetFactory().createButton(composite, null,
				SWT.CHECK);
		drawLinesButton.addListener(SWT.Selection, this);

		FormData data = new FormData();
		data.left = new FormAttachment(0, leftCoordinate);
		data.right = new FormAttachment(30, -rightMarginSpace
				- ITabbedPropertyConstants.HSPACE);
		drawLinesButton.setLayoutData(data);
		drawLinesButton.setToolTipText(Messages.Tooltip_DrawLines);

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(drawLinesButton,
				-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(drawLinesButton, 0, SWT.CENTER);
		lineLabel.setLayoutData(data);
	}
}
