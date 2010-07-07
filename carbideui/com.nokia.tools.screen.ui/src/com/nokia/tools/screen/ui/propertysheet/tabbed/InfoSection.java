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
package com.nokia.tools.screen.ui.propertysheet.tabbed;

import java.awt.Rectangle;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

public class InfoSection extends SingleSelectionWidgetSection {
	private Text idText;
	private Text boundsText;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);

		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		Composite group = getWidgetFactory().createFlatFormComposite(composite);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.left = new FormAttachment(0, 0);
		group.setLayoutData(data);

		group.setLayout(new GridLayout(2, false));
		((GridLayout) group.getLayout()).marginWidth = 0;
		((GridLayout) group.getLayout()).marginHeight = 0;
		((GridLayout) group.getLayout()).marginRight = 1;
		((GridLayout) group.getLayout()).horizontalSpacing = 1;

		int leftCoordinate = getStandardLabelWidth(group, new String[] {
				Messages.Label_Name, Messages.Label_Id, Messages.Label_Bounds });

		// id
		CLabel idLabel = getWidgetFactory().createCLabel(group,
				Messages.Label_Id);
		idText = getWidgetFactory().createText(group, "", SWT.NONE);

		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.widthHint = 200;
		idText.setLayoutData(gd);
		idText.addListener(SWT.Modify, this);
		idText.addListener(SWT.FocusOut, this);
		idText.addKeyListener(enterAdapter);
		idText.setEditable(isModifiable());

		gd = new GridData();
		gd.widthHint = leftCoordinate;
		idLabel.setLayoutData(gd);

		// bounds
		CLabel boundsLabel = getWidgetFactory().createCLabel(group,
				Messages.Label_Bounds);
		gd = new GridData();
		gd.widthHint = leftCoordinate;
		boundsLabel.setLayoutData(gd);

		boundsText = getWidgetFactory().createText(group, "", SWT.NONE);
		gd = new GridData();
		gd.widthHint = 200;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		boundsText.setLayoutData(gd);
		boundsText.addListener(SWT.Modify, this);
		boundsText.addListener(SWT.FocusOut, this);
		boundsText.addKeyListener(enterAdapter);
		boundsText.setEditable(isModifiable());

		composite.layout(true, true);

		syncWithOther(group, InfoSection.class.getName());
		parent.getParent().pack(); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#doSetInput(org.eclipse.ui.IWorkbenchPart,
	 *      com.nokia.tools.screen.core.IScreenElement)
	 */
	@Override
	protected EObject doSetInput(IWorkbenchPart part, IScreenElement adapter) {
		return adapter.getWidget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.propertysheet.tabbed.section.WidgetSection#doRefresh()
	 */
	protected void doRefresh() {
		IContentData content = getContent();

		String id = (content != null) ? content.getId() : "";
		if (null == id)
			id = "";

		Rectangle rect = EditingUtil.getBounds(getTarget());
		if (rect == null) {
			rect = (Rectangle) content.getAttribute(ContentAttribute.BOUNDS
					.name());
			if (rect == null) {
				rect = new Rectangle();
			}
		}
		String bounds = ScreenUtil.formatRectangle(rect);

		idText.setText(id);
		boundsText.setText(bounds);

		idText.setEditable(isModifiable());
		boundsText.setEditable(isModifiable());
		idText.getParent().getParent().getParent().layout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.propertysheet.tabbed.section.WidgetSection#doHandleEvent(org.eclipse.swt.widgets.Event)
	 */
	protected void doHandleEvent(Event event) {
		if (SWT.FocusOut == event.type) {
			if (event.widget == idText && idText.getEditable()) {
				applyAttributeSetting("id", idText.getText());
			} else if (event.widget == boundsText && boundsText.getEditable()) {
				Rectangle rect = ScreenUtil
						.parseRectangle(boundsText.getText());
				if (rect != null) {
					applyAttributeSetting(EditingUtil
							.getBoundsFeature(getTarget()), rect);
				}
			}
		} else if (SWT.Modify == event.type) {
			if (event.widget == boundsText) {
				verifyRectangle(boundsText.getText());
			}
		}
	}

	private boolean isModifiable() {
		return getElement() != null
				&& !ScreenUtil.isPrimaryContent(getElement().getData());
	}

	protected void verifyRectangle(String text) {
		if (ScreenUtil.parseRectangle(text) == null) {
			setErrorMessage(Messages.Error_Input);
		} else {
			clearErrorMessage();
		}
	}
}
