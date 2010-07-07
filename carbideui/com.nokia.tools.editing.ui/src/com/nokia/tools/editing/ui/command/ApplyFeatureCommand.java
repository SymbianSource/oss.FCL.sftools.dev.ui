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
package com.nokia.tools.editing.ui.command;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.commands.Command;
import org.eclipse.osgi.util.NLS;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.ui.Activator;

public class ApplyFeatureCommand extends Command {
	private EObject target;
	private EStructuralFeature feature;
	private Object value;
	private Object oldValue;
	// used for debugging
	private boolean isInCompound;

	/**
	 * @return the feature
	 */
	public EStructuralFeature getFeature() {
		return feature;
	}

	/**
	 * @param feature the feature to set
	 */
	public void setFeature(EStructuralFeature feature) {
		this.feature = feature;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the target
	 */
	public EObject getTarget() {
		return target;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(EObject target) {
		this.target = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		return getFeature() != null && getTarget() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#canUndo()
	 */
	@Override
	public boolean canUndo() {
		return canExecute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		oldValue = EditingUtil.getFeatureValue(getTarget(), getFeature());
		if (oldValue == getValue() && !isInCompound) {
			Activator.error(
					"Command: same value objects, undo won't work, value: "
							+ value, new Exception());
		}
		EditingUtil.setFeatureValue(getTarget(), getFeature(), getValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo() {
		execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		value = EditingUtil.getFeatureValue(getTarget(), getFeature());
		EditingUtil.setFeatureValue(getTarget(), getFeature(), oldValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.commands.Command#getLabel()
	 */
	@Override
	public String getLabel() {
		String label = super.getLabel();
		if (label == null) {
			label = NLS.bind(Messages.Command_SetProperty, getFeature()
					.getName());
		}
		return label;
	}

	public void setInCompound(boolean isInCompound) {
		this.isInCompound = isInCompound;
	}
}
