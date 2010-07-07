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

import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.screen.ui.UiPlugin;

public class AdvancedPropertyDescriptor extends PropertyDescriptor {
	Object id;
	Object value;
	EditObject editObject;
	boolean editable = true;

	public AdvancedPropertyDescriptor(Object id, Object value,
			EditObject editobj) {
		super(id, id instanceof EReference ? ((EReference) id).getName() : id
				.toString());
		this.id = id;
		this.value = value;
		this.editObject = editobj;
	}

	public CellEditor createPropertyEditor(Composite parent) {
		if (!isEditable())
			return null;
		CellEditor editor = UiPlugin.getDefault().getCellEditor(
				((EReference) id).getEType().getName(), parent);
		editor = editor == null ? new TextCellEditor(parent) : editor;
		if (getValidator() != null) {
			editor.setValidator(getValidator());
		}

		return editor;
	}

	@Override
	public String getDisplayName() {
		return ((EReference) id).getName();
	}

	@Override
	public ILabelProvider getLabelProvider() {
		if (value instanceof Rectangle) {
			return new LabelProvider() {
				public String getText(Object element) {
					Rectangle rect = (Rectangle) element;
					StringBuffer sbuf = new StringBuffer();
					sbuf.append(rect.x);
					sbuf.append(',');
					sbuf.append(rect.y);
					sbuf.append(',');
					sbuf.append(rect.width);
					sbuf.append(',');
					sbuf.append(rect.height);
					return sbuf.toString();
				}
			};
		}
		return super.getLabelProvider();
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

}
