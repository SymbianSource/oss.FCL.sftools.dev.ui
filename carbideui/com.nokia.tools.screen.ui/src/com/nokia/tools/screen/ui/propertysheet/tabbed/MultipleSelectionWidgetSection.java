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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.editing.ui.command.CommandBuilder;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public abstract class MultipleSelectionWidgetSection extends WidgetSection {

	private List<IScreenElement> elements;

	private List<EObject> targets;

	private List<EObject> widgets;

	private List<IContentData> contents;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public final void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);

		removeRefreshAdapters();

		elements = new ArrayList<IScreenElement>();
		targets = new ArrayList<EObject>();
		widgets = new ArrayList<EObject>();
		contents = new ArrayList<IContentData>();
		IStructuredSelection ssel = (IStructuredSelection) selection;

		for (Iterator iter = ssel.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			IScreenElement adapter = JEMUtil.getScreenElement(obj);
			if (adapter != null) {
				IScreenElement element = adapter;
				if (adapter.getTargetAdapter() != null) {
					adapter = adapter.getTargetAdapter();
				}

				EObject target = doSetInput(part, adapter);
				if (target != null) {
					if (!(elements.contains(element) && targets
							.contains(target))) {
						elements.add(element);
						targets.add(target);
						widgets.add(adapter.getWidget());
						contents.add(element.getData());
					}
				} else {
					IContentData data = JEMUtil.getContentData(obj);
					if (data != null) {
						contents.add(data);
					}
				}
			} else {
				IContentData data = JEMUtil.getContentData(obj);
				if (data != null) {
					contents.add(data);
				}
			}
		}
		for (EObject target : targets) {
			addRefreshAdapter(target);
		}
		for (IContentData data : contents) {
			addRefreshAdapter(data);
		}

		clearErrorMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#removeRefreshAdapters()
	 */
	protected void removeRefreshAdapters() {
		if (targets != null) {
			for (EObject target : targets) {
				removeRefreshAdapter(target);
			}
		}
		if (contents != null) {
			for (IContentData data : contents) {
				removeRefreshAdapter(data);
			}
		}
	}

	/**
	 * @return Returns the elements.
	 */
	public IScreenElement[] getElements() {
		return elements.toArray(new IScreenElement[0]);
	}

	/**
	 * @return Returns the element widgets.
	 */
	public EObject[] getWidgets() {
		return widgets.toArray(new EObject[0]);
	}

	/**
	 * @return Returns the targets.
	 */
	public EObject[] getTargets() {
		return targets.toArray(new EObject[0]);
	}

	public IContentData[] getContents() {
		return contents.toArray(new IContentData[0]);
	}

	public IScreenElement getElement(EObject target) {
		int idx = targets.indexOf(target);
		if (idx >= 0) {
			return elements.get(idx);
		}
		return null;
	}

	public EObject getTarget(IScreenElement element) {
		int idx = elements.indexOf(element);
		if (idx >= 0) {
			return targets.get(idx);
		}
		return null;
	}

	public IScreenElement getFirstElement() {
		if (elements.size() > 0) {
			return elements.get(0);
		}
		return null;
	}

	public EObject getFirstTarget() {
		if (null != targets) {
			if (targets.size() > 0) {
				return targets.get(0);
			}
		}
		return null;
	}

	public EObject getFirstWidget() {
		if (widgets.size() > 0) {
			return widgets.get(0);
		}
		return null;
	}

	public IContentData getFirstContent() {
		if (contents.size() > 0) {
			return contents.get(0);
		}
		return null;
	}

	public void applyAttributeSetting(String featureName, Object value) {
		if (targets.isEmpty()) {
			return;
		}
		applyAttributeSetting(EditingUtil.getFeature(targets.get(0),
				featureName), value);
	}

	protected void applyAttributeSetting(EStructuralFeature feature,
			Object value) {

		if (!isDirty) {
			return;
		}

		isDirty = false;

		CommandStack commandStack = (CommandStack) getPart().getAdapter(
				CommandStack.class);

		if (targets.size() == 0 || commandStack == null) {
			return;
		}

		if (validatePropertyValue(feature, value)) {
			CommandBuilder builder = new CommandBuilder(Messages.Command_Apply
					+ " " + customizeFeatureName(feature), isForwardUndo());
			buildCommand(builder, getElements(), getTargets(), feature, value);
			applyParentAttributeSettings(builder);

			suppressStackEvent = true;
			try {
				commandStack.execute(builder.getCommand());
			} finally {
				suppressStackEvent = false;
			}
		}
	}

	protected Object getFeatureValue(EObject widget, String path) {
		return getFeatureValues(new EObject[] { widget }, path)[0];
	}

	protected Object[] getFeatureValues(EObject[] widgets, String path) {
		StringTokenizer st = new StringTokenizer(path, "\\/");
		Object[] values = new Object[widgets.length];
		while (st.hasMoreTokens()) {
			String featureName = st.nextToken();
			for (int i = 0; i < widgets.length; i++) {
				if (widgets[i] != null) {
					values[i] = EditingUtil.getFeatureValue(
							(EObject) widgets[i], featureName);
				} else {
					values[i] = null;
				}
			}
		}
		return values;
	}

	protected EObject[] getFeatureObjects(EObject[] widgets, String path) {
		Object[] values = getFeatureValues(widgets, path);
		EObject[] objects = new EObject[values.length];
		for (int i = 0; i < values.length; i++) {
			objects[i] = createEditObject(values[i]);
		}
		return objects;
	}

	protected void buildCommand(CommandBuilder builder,
			IScreenElement[] elements, EObject[] targets,
			EStructuralFeature feature, Object[] newValue) {
		for (int i = 0; i < targets.length; i++) {
			builder.applyFeature(new ApplyFeatureCommand(), targets[i],
					feature, newValue[i]);
		}
	}

	protected void buildCommand(CommandBuilder builder,
			IScreenElement[] elements, EObject[] targets,
			EStructuralFeature feature, Object newValue) {
		for (int i = 0; i < targets.length; i++) {
			builder.applyFeature(new ApplyFeatureCommand(), targets[i],
					feature, newValue);
		}
	}
}
