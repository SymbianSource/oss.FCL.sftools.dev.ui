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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

import com.nokia.tools.content.core.IContent;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public class AdvancedPropertySection extends MultipleSelectionWidgetSection {
	private boolean suppressRefreshEvent;
	/**
	 * The Property Sheet Page.
	 */
	private PropertySheetPage page;

	/**
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ISection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.wst.common.ui.properties.internal.provisional.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {

		super.createControls(parent, tabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
				.createFlatFormComposite(parent);

		page = new PropertySheetPage();
		updateRootEntry();

		page.setPropertySourceProvider(new AdvancedPropertySourceProvider());
		page.createControl(composite);

		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.height = 100;
		data.width = 100;
		page.getControl().setLayoutData(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#doSetInput(org.eclipse.ui.IWorkbenchPart,
	 *      com.nokia.tools.screen.core.IScreenElement)
	 */
	@Override
	protected EObject doSetInput(IWorkbenchPart part, IScreenElement adapter) {
		page.selectionChanged(part, getSelection());
		return adapter.getWidget();
	}

	/**
	 * Sets a new root entry to the page.
	 */
	protected void updateRootEntry() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentListener#contentChanged(com.nokia.tools.content.core.IContent)
	 */
	public void rootContentChanged(IContent content) {
		// root entry needs to be cleared when the content changes, e.g. during
		// resolution changes, otherwise, the old property data will cause
		// problem and may not be released in time.
		updateRootEntry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ISection#dispose()
	 */
	public void dispose() {
		super.dispose();

		if (page != null) {
			page.dispose();
			page = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.propertysheet.tabbed.section.WidgetSection#doRefresh()
	 */
	protected void doRefresh() {
		if (suppressRefreshEvent) {
			return;
		}

		suppressRefreshEvent = true;
		try {
			page.refresh();
			page.getControl().getParent().getParent().layout();
		} finally {
			suppressRefreshEvent = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ISection#shouldUseExtraSpace()
	 */
	public boolean shouldUseExtraSpace() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.widget.propertysheet.tabbed.section.WidgetSection#doHandleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	protected void doHandleEvent(Event e) {
	}

	class AdvancedPropertySourceProvider implements IPropertySourceProvider {

		@SuppressWarnings("unchecked")
		public IPropertySource getPropertySource(Object object) {
			if (object instanceof EditPart) {

				EditObject editObject = (EditObject) ((EditPart) object)
						.getModel();
				IScreenElement adapter = JEMUtil.getScreenElement(editObject);
				IScreenElement targetAdapter = adapter.getTargetAdapter();
				if (targetAdapter != null) {
					adapter = targetAdapter;
				}
				EObject eObject = (EObject) adapter
						.getAdapter(IPropertySource.class);

				List featureList = eObject.eClass().getEAllStructuralFeatures();
				Map propMap = new HashMap();
				for (int i = 0; i < featureList.size(); i++) {
					Object val = eObject.eGet((EStructuralFeature) featureList
							.get(i));
					if (!(("diagram".equals(((EReference) featureList.get(i))
							.getName())) || "parent"
							.equals(((EReference) featureList.get(i)).getName())))
						propMap.put(featureList.get(i), val);
				}
				List<String> propNames = (List<String>) adapter
						.getAdapter(ICellEditorFactory.class);
				Map xPropMap = new HashMap();
				if (propNames != null) {
					for (String propName : propNames) {
						xPropMap.put(EditingUtil.getFeature(editObject,
								propName), EditingUtil.getFeatureValue(
								editObject, propName));
					}
				}
				return new AdvancedPropertySource(propMap, xPropMap,
						editObject, AdvancedPropertySection.this);

			}
			return null;
		}

	}

}
