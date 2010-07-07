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
package com.nokia.tools.screen.ui.dialogs;

import java.io.File;

public class FileResourceResult<T>
    implements ResourceResult<T> {

	private final boolean isThemeResource;

	private final T value;

	/**
	 * @param isThemeResource
	 * @param value
	 */
	public FileResourceResult(boolean isThemeResource, T value) {
		this.isThemeResource = isThemeResource;
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.ResourceResult#isThemeResource()
	 */
	public boolean isThemeResource() {
		return isThemeResource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.screen.ui.dialogs.ResourceResult#value()
	 */
	public T value() {
		return value;
	}


	static class ValueAdapter {

		public String value(File file) {
			return null;
		}

	}

	public String getFileName() {
		return getValue(new ValueAdapter() {

			@Override
			public String value(File file) {
				return file.getName();
			}

		});
	}

	public String getFolderContainingFile() {
		return getValue(new ValueAdapter() {

			@Override
			public String value(File file) {
				return file.getParent();
			}

		});
	}

	private String getValue(ValueAdapter valueAdapter) {
		String result = null;
		if (value != null) {
			File file = new File((String)value);
			result = valueAdapter.value(file);
		}
		return result;
	}

}
