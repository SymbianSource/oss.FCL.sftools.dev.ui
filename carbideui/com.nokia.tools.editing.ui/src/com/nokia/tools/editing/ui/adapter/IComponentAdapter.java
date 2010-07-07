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
package com.nokia.tools.editing.ui.adapter;


/**
 * Content data may override the default editing behavior by implementing this
 * class.
 * 
 * 
 */
public interface IComponentAdapter {
	/**
	 * Can constraint be changed?
	 */
	int CHANGE_CONSTRAINT = 1;
	/**
	 * Can direct edit?
	 */
	int DIRECT_EDIT = 1 << 1;
	/**
	 * Can child be added?
	 */
	int ADD_CHILD = 1 << 2;
	/**
	 * Can child be removed?
	 */
	int REMOVE_CHILD = 1 << 3;
	/**
	 * Can child be moved inside one container, i.e. change the order?
	 */
	int MOVE_CHILD = 1 << 4;
	/**
	 * Can element be selected?
	 */
	int SELECTION = 1 << 5;
	/**
	 * Can element be animated?
	 */
	int ANIMATION = 1 << 6;
	/**
	 * Can be deleted
	 */
	int DELETE = 1 << 7;
	/**
	 * Can be modified at all?
	 */
	int MODIFY = CHANGE_CONSTRAINT | DIRECT_EDIT | DELETE | ADD_CHILD
			| REMOVE_CHILD | MOVE_CHILD | SELECTION;
	/**
	 * Unmodifiable
	 */
	int READONLY = SELECTION;
	/**
	 * Unselectable
	 */
	int UNSELECTABLE = 0;

	/**
	 * Unselectable
	 */
	IComponentAdapter UNSELECTABLE_ADAPTER = new Stub() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IComponentAdapter.Stub#getSupportedTypes()
		 */
		@Override
		protected int getSupportedTypes() {
			return UNSELECTABLE;
		}
	};

	/**
	 * Fully readonly.
	 */
	IComponentAdapter READ_ONLY_ADAPTER = new Stub() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IComponentAdapter.Stub#getSupportedTypes()
		 */
		@Override
		protected int getSupportedTypes() {
			return READONLY;
		}
	};

	/**
	 * Fully editable.
	 */
	IComponentAdapter FULL_MODIFY_ADAPTER = new Stub() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IComponentAdapter.Stub#getSupportedTypes()
		 */
		@Override
		protected int getSupportedTypes() {
			return MODIFY;
		}
	};

	/**
	 * Constraint changeable.
	 */
	IComponentAdapter CONSTRAINT_ADAPTER = new Stub() {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IComponentAdapter.Stub#getSupportedTypes()
		 */
		@Override
		protected int getSupportedTypes() {
			return CHANGE_CONSTRAINT | SELECTION;
		}
	};

	/**
	 * Tests if the given edit type is supported by this adapter.
	 * 
	 * @param type the edit type: {@link #ADD_CHILD},
	 *            {@link #CHANGE_CONSTRAINT}, {@link #DIRECT_EDIT},
	 *            {@link #REMOVE_CHILD} and {@link #MODIFY}.
	 * @param data the data containing additional information.
	 * @return true if the specific type is supported, false otherwise.
	 */
	boolean supports(int type, Object data);

	/**
	 * Stub implementation of the component adapter.
	 * 
	 */
	abstract class Stub implements IComponentAdapter {
		/**
		 * @return the supported types, which is a bitwise 'OR'ed edit values.
		 */
		protected abstract int getSupportedTypes();

		/**
		 * Subclass may override this to return different values depending on
		 * the child type.
		 * 
		 * @param child the child to test.
		 * @return true if the child can be added, false otherwise.
		 */
		protected boolean supportsAddChild(Object child) {
			return true;
		}

		/**
		 * Subclass may override this to return different values depending on
		 * the child type.
		 * 
		 * @param child the child to test.
		 * @return true if the child can be deleted, false otherwise.
		 */
		protected boolean supportsDeleteChild(Object child) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.content.core.IComponentAdapter#supports(int,
		 *      java.lang.Object)
		 */
		public boolean supports(int type, Object data) {
			int supportedTypes = getSupportedTypes();
			if ((type & supportedTypes) == 0) {
				return false;
			}
			if (ADD_CHILD == type) {
				return supportsAddChild(data);
			}
			if (REMOVE_CHILD == type) {
				return supportsDeleteChild(data);
			}
			return true;
		}
	}
}
