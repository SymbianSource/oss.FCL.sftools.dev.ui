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

package com.nokia.tools.media.utils;

import java.io.File;
import java.lang.reflect.Field;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.program.Program;

public class MediaPreferenceInitializer extends AbstractPreferenceInitializer {

	public MediaPreferenceInitializer() {
		super();
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = UtilsPlugin.getDefault().getPreferenceStore();
		prefs.setDefault(IMediaConstants.PREF_BITMAP_EDITOR,
				getExternalEditorCommandForExtension(IFileConstants.FILE_EXT_BMP));
		prefs.setDefault(IMediaConstants.PREF_VECTOR_EDITOR,
				getExternalEditorCommandForExtension(IFileConstants.FILE_EXT_SVG));
		prefs.setDefault(IMediaConstants.PREF_SOUND_EDITOR,
				getExternalEditorCommandForExtension("wav"));

		prefs.setDefault(IMediaConstants.PREF_VIDEO_PLAYER,
				getExternalEditorCommandForExtension("avi"));
		prefs.setDefault(IMediaConstants.PREF_AUDIO_PLAYER,
				getExternalEditorCommandForExtension("mp3"));
	}

	public static String getExternalEditorCommandForExtension(String extension) {
		Program p = Program.findProgram(extension);
		if (null == p)
			return "";
		Class c = p.getClass();
		try {
			Field f = c.getDeclaredField("command");
			f.setAccessible(true);
			String command = (String) f.get(p);
			command = command.substring(0,
					(command.indexOf("%1") == -1) ? command.length() : command
							.indexOf("%1"));
			command = command.replace("\"", "").trim();
			if (new File(command).exists())
				return command;
		} catch (NoSuchFieldException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		}
		return "";
	}
}
