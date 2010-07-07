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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EReferenceImpl;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;
import com.nokia.tools.screen.ui.utils.EclipseUtils;

@SuppressWarnings("unchecked")
public class AdvancedPropertySource implements IPropertySource {

	protected Map propMap;
	protected Map xPropMap;
	protected EditObject editObj;
	final static Class ADAPTER_KEY = IPropertySource.class;
	final boolean PERF_GETDESCRIPTORS = false;
	private MultipleSelectionWidgetSection section;

	public AdvancedPropertySource(Map props, Map xProps, EditObject editObject,
			MultipleSelectionWidgetSection sect) {
		super();
		propMap = props;
		xPropMap = xProps;
		editObj = editObject;
		section = sect;
	}

	public Object getEditableValue() {
		return "";
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		Iterator iProperties = Collections.EMPTY_LIST.iterator();
		iProperties = propMap.keySet().iterator();
		List<IPropertyDescriptor> resultList = new ArrayList<IPropertyDescriptor>();

		Iterator iPropertiesX = Collections.EMPTY_LIST.iterator();
		iPropertiesX = xPropMap.keySet().iterator();

		while (iPropertiesX.hasNext()) {
			EReferenceImpl temp = (EReferenceImpl) iPropertiesX.next();
			AdvancedPropertyDescriptor descriptor = new AdvancedPropertyDescriptor(
					temp, xPropMap.get(temp), editObj);
			descriptor.setCategory(null);
			descriptor.setEditable(false);
			resultList.add(descriptor);
		}
		// css properties
		while (iProperties.hasNext()) {
			EReferenceImpl temp = (EReferenceImpl) iProperties.next();
			AdvancedPropertyDescriptor descriptor = new AdvancedPropertyDescriptor(
					temp, propMap.get(temp), editObj);
			descriptor.setCategory("CSS");
			resultList.add(descriptor);
		}

		IPropertyDescriptor[] resultArray = new IPropertyDescriptor[resultList
				.size()];
		return (IPropertyDescriptor[]) resultList.toArray(resultArray);

	}

	public Object getPropertyValue(Object name) {
		if (name == null) {
			return ""; //$NON-NLS-1$
		}
		if (propMap.containsKey(name))
			return propMap.get(name) == null ? "" : propMap.get(name);
		else
			return xPropMap.get(name) == null ? "" : xPropMap.get(name);
	}

	public boolean isPropertySet(Object property) {

		return true;
	}

	public void resetPropertyValue(Object str) {
		if (str == null) {
			return;
		}

	}

	private IPropertyDescriptor getPropertyDescriptor(Object id) {
		for (IPropertyDescriptor propertyDescriptor : getPropertyDescriptors()) {
			if (propertyDescriptor.getId() == id) {
				return propertyDescriptor;
			}
		}
		return null;
	}

	public void setPropertyValue(Object name, Object value) {
		if (name == null) {
			return;
		}
		if (propMap.containsKey(name)) {
			propMap.remove(name);
			propMap.put(name, value);
		} else {
			xPropMap.remove(name);
			xPropMap.put(name, value);
		}

		if (getPropertyDescriptor(name) != null
				&& "CSS".equals(getPropertyDescriptor(name).getCategory())) {

			IScreenElement scrElem = JEMUtil.getScreenElement(editObj);
			IScreenElement targetAdapter = scrElem.getTargetAdapter();
			if (targetAdapter != null) {
				scrElem = targetAdapter;
			}

			EObject cssObject = (EObject) scrElem
					.getAdapter(IPropertySource.class);

			EditObject eObject = (EditObject) scrElem.getTarget();
			AdvancedFeatureCommand command = new AdvancedFeatureCommand();
			command.setFeature((EStructuralFeature) name);
			command.setValue(value);
			command.setTarget(cssObject);

			execute(command);

		} else {
			ApplyFeatureCommand applyFeatureCommand1 = new ApplyFeatureCommand();
			applyFeatureCommand1.setFeature((EStructuralFeature) name);
			applyFeatureCommand1.setValue(value);
			applyFeatureCommand1.setTarget(editObj);
			execute(applyFeatureCommand1);
		}
	}

	private void execute(Command command) {
		IEditorPart editor = EclipseUtils.getActiveSafeEditor();

		if (editor != null) {
			// can happen when the editor is closed
			CommandStack stack = (CommandStack) editor
					.getAdapter(CommandStack.class);
			if (stack != null) {
				stack.execute(command);
			}
		}
	}

	public class AdvancedFeatureCommand extends ApplyFeatureCommand {

		@Override
		public void execute() {
		
			super.execute();
			section.refresh();
		}

		@Override
		public void redo() {
			
			super.redo();
			section.refresh();
		}

		@Override
		public void undo() {
		
			super.undo();
			section.refresh();
		}

	}

}
