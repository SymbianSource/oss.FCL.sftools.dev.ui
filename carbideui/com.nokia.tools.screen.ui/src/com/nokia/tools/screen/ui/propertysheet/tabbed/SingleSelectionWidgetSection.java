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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.editing.ui.command.ApplyFeatureCommand;
import com.nokia.tools.editing.ui.command.CommandBuilder;
import com.nokia.tools.screen.core.IScreenElement;
import com.nokia.tools.screen.core.JEMUtil;

public abstract class SingleSelectionWidgetSection extends WidgetSection {
	private IScreenElement element;

	private EObject target;

	private IContentData content;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.wst.common.ui.properties.internal.provisional.ISection#setInput(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);

		removeRefreshAdapters();

		element = null;
		target = null;
		content = null;
		IScreenElement adapter = JEMUtil.getScreenElement(selection);
		if (adapter != null) {
			this.element = adapter;
			content = element.getData();
			if (adapter.getTargetAdapter() != null) {
				adapter = adapter.getTargetAdapter();
			}
			this.target = doSetInput(part, adapter);
		} else {
			content = JEMUtil.getContentData(selection);
			target = (EditObject) content.getAdapter(EditObject.class);
		}

		addRefreshAdapter(target);
		addRefreshAdapter(content);

		clearErrorMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.propertysheet.tabbed.WidgetSection#removeRefreshAdapters()
	 */
	protected void removeRefreshAdapters() {
		removeRefreshAdapter(target);
		removeRefreshAdapter(content);
	}

	/**
	 * @return Returns the element.
	 */
	public IScreenElement getElement() {
		return element;
	}

	/**
	 * @return Returns the target.
	 */
	public EObject getTarget() {
		return target;
	}

	public IContentData getContent() {
		return content;
	}

	protected void setTarget(EObject target) {
		this.target = target;
	}

	protected void applyAttributeSetting(String featureName, Object value) {
		applyAttributeSetting(EditingUtil.getFeature(getTarget(), featureName),
				value);
	}

	protected void applyAttributeSetting(EStructuralFeature feature,
			Object value) {
		if (!isDirty) {
			return;
		}
		isDirty = false;

		if (target == null) {
			return;
		}

		if (validatePropertyValue(feature, value)) {
			CommandBuilder builder = new CommandBuilder(Messages.Command_Apply
					+ " " + customizeFeatureName(feature), isForwardUndo());
			builder.applyFeature(new ApplyFeatureCommand(), target, feature,
					value);
			applyParentAttributeSettings(builder);

			suppressStackEvent = true;
			try {
				execute(builder.getCommand());
			} finally {
				suppressStackEvent = false;
			}
		}
	}
}
