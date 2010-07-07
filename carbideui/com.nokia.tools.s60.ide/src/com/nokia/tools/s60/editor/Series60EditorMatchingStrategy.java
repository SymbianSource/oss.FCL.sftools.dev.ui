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
package com.nokia.tools.s60.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;

import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.extension.ExtensionManager;
import com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor;
import com.nokia.tools.screen.ui.utils.ScreenUtil;

public class Series60EditorMatchingStrategy implements IEditorMatchingStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IEditorMatchingStrategy#matches(org.eclipse.ui.IEditorReference,
	 *      org.eclipse.ui.IEditorInput)
	 */
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			
			String extension = input.getName().toLowerCase();
			if (extension.contains(".")) {
				extension = extension.substring(extension.lastIndexOf("."));
			}

			try {
				if (((IFileEditorInput) editorRef.getEditorInput()).getFile()
						.getProject().equals(
								((IFileEditorInput) input).getFile()
										.getProject())) {
					if (ScreenUtil.isPrimaryContentInput(input)) {
						return true;
					}
					IEmbeddedEditorDescriptor desc = ExtensionManager
							.getEmbeddedEditorDescriptorByExtension(extension);
					return desc != null;
				}
			} catch (PartInitException e) {
				S60WorkspacePlugin.error(e);
			}
		}
		return false;
	}
}
