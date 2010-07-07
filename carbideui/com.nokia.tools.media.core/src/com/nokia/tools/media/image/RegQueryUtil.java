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


package com.nokia.tools.media.image;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;

public class RegQueryUtil {

	static class StreamReader extends Thread {
		private final InputStream is;
		private final StringWriter sw;

		StreamReader(final InputStream is) {
			this.is = is;
			sw = new StringWriter();
		}

		String getResult() {
			return sw.toString();
		}

		@Override
		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1) {
					sw.write(c);
				}
			} catch (final IOException e) {
				;
			}
		}
	}

	private static final String CMD = "CMD";

	private static final String EDIT_CMD = "edit";

	private static final String EXE_NAME = "EXE_NAME";

	private static final String EXTENSION_KEY = "EXTENSION_KEY";

	private static final String FULL_PATH_QUERY = RegQueryUtil.REGQUERY_UTIL
	    + "\"HKEY_LOCAL_MACHINE\\SOFTWARE\\Classes\\Applications\\"
	    + RegQueryUtil.EXE_NAME + "\\shell\\" + RegQueryUtil.CMD
	    + "\\command\"";

	private static final String FULL_PATH_QUERY2 = RegQueryUtil.REGQUERY_UTIL
	    + "HKEY_CLASSES_ROOT\\Applications\\" + RegQueryUtil.EXE_NAME
	    + "\\shell\\" + RegQueryUtil.CMD + "\\command";

	private static final String OPEN_CMD = "open";

	private static final String OPEN_WITH_LIST = RegQueryUtil.REGQUERY_UTIL
	    + "\"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\"
	    + RegQueryUtil.EXTENSION_KEY + "\\OpenWithList\"";

	private static final String REG_EXPAND_STR_TOKEN = "REG_EXPAND_SZ";

	private static final String REG_STR_TOKEN = "REG_SZ";

	private static final String REGQUERY_UTIL = "reg query ";

	private static final String SYS_ROOT = "SystemRoot";

	private static String executeQuery(final String query) {
		try {
			final Process process = Runtime.getRuntime().exec(query);
			final StreamReader reader = new StreamReader(process
			    .getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			final String result = reader.getResult();
			return result;
		} catch (final Exception e) {
			return null;
		}
	}

	private static String[] extractExeNames(final String result) {
		final List<String> names = new ArrayList<String>();
		final StringTokenizer tokenizer = new StringTokenizer(result, "\n");

		while (tokenizer.hasMoreTokens()) {
			String exe = tokenizer.nextToken();
			if (exe.indexOf(".exe") != -1) {
				final String[] arr = exe.split("\\s");
				if (arr.length > 0) {
					exe = arr[arr.length - 1].trim();
					if (exe.endsWith(exe)) {
						names.add(exe);
					}
				}
			}
		}
		return names.toArray(new String[0]);
	}

	private static String extractPath(String result) {
		if (result != null) {
			int pos = result.indexOf(RegQueryUtil.REG_EXPAND_STR_TOKEN);
			if (pos != -1) {
				result = result.substring(
				    pos + RegQueryUtil.REG_EXPAND_STR_TOKEN.length()).trim();
				result = RegQueryUtil.replaceEnvVariable(result);

			} else {
				pos = result.indexOf(RegQueryUtil.REG_STR_TOKEN);
				if (pos != -1) {
					result = result.substring(
					    pos + RegQueryUtil.REG_STR_TOKEN.length()).trim();
				}

			}
			result = RegQueryUtil.formulateCmdPath(result);

		}
		return result;

	}

	private static String formatQuery(String query, final String value,
	    final String key) {
		query = query.replaceFirst(key, value);
		return query;

	}

	private static String formulateCmdPath(String command) {
		command = command.substring(0, (command.indexOf("%1") == -1) ? command
		    .length() : command.indexOf("%1"));
		String commandPath = command;
		command = command.replace("\"", "").trim();
		if (new File(command).exists()) {
			return command;
		}
		if (commandPath.indexOf(".exe") > 0) {
			commandPath = commandPath.substring(0,
			    (commandPath.indexOf(".exe") + 4));
			commandPath = commandPath.replace("\"", "").trim();
			if (new File(commandPath).exists()) {
				return commandPath;
			}
		}
		return null;
	}

	public static String getEditProgramforBitmap() {
		if (SWT.getPlatform().equals("win32")) {
			final String query = RegQueryUtil.REGQUERY_UTIL
			    + "HKEY_CLASSES_ROOT\\Paint.Picture\\shell\\edit\\command";

			String result = RegQueryUtil.executeQuery(query);
			result = RegQueryUtil.extractPath(result);
			if ((result != null) && new File(result).exists()) {
				return result;
			}

		}
		return "";
	}

	private static String[] getExeNames(final String extension) {
		final String query = RegQueryUtil.formatQuery(
		    RegQueryUtil.OPEN_WITH_LIST, extension, RegQueryUtil.EXTENSION_KEY);
		final String result = RegQueryUtil.executeQuery(query);
		return RegQueryUtil.extractExeNames(result);

	}

	private static ProgramDescriptor getProgramDescriptor(final String exeName,
	    final String query) {
		String result = RegQueryUtil.runOpenQuery(exeName, query);
		if ((result == null) || (result.trim().length() == 0)) {
			result = RegQueryUtil.runEditQuery(exeName, query);
		}
		result = RegQueryUtil.extractPath(result);
		if ((result != null) && new File(result).exists()) {
			return new ProgramDescriptor(exeName, result, null);
		}

		return null;
	}

	public static ProgramDescriptor[] getRecommendedPrograms(
	    final String extension) {
		final List<ProgramDescriptor> descriptor = new ArrayList<ProgramDescriptor>();
		final String[] names = RegQueryUtil.getExeNames(extension);
		if (names != null) {
			for (final String name : names) {
				ProgramDescriptor des = RegQueryUtil.getProgramDescriptor(name,
				    RegQueryUtil.FULL_PATH_QUERY);
				if (des != null) {
					descriptor.add(des);
				} else {
					des = RegQueryUtil.getProgramDescriptor(name,
					    RegQueryUtil.FULL_PATH_QUERY2);
					if (des != null) {
						descriptor.add(des);
					}
				}
			}
		}

		return descriptor.toArray(new ProgramDescriptor[0]);
	}

	
	public static String getUserName() {
		if ("linux".equalsIgnoreCase(System.getProperty("os.name"))) {
			return RegQueryUtil.getUserNameInLinux();
		}
		return RegQueryUtil.getUserNameInWindows();

	}

	private static String getUserNameInLinux() {
		final String query = "finger -l " + System.getProperty("user.name");
		final String result = RegQueryUtil.executeQuery(query);
		if (result != null) {
			final int i = result.indexOf("Name:");
			final int j = result.indexOf("Directory:");
			if ((i != -1) && (j != -1)) {
				return result.substring(i + 5, j).trim();
			}
		}
		return "";
	}

	private static String getUserNameInWindows() {
		final String query = "reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Group Policy\\State\\"
		    + RegQueryUtil.getUserSID() + "\"" + " /v Distinguished-Name";
		final String userString = (RegQueryUtil.executeQuery(query));
		
		if (userString == null) {
			return "";
		}
		int si = -1, ei = -1;
		si = userString.indexOf("CN=", 0);
		if (si != -1) {
			ei = userString.indexOf('(', si);
			;
		}
		if ((si != -1) && (ei != -1)) {
			final String name = userString.substring(si + 3, ei);
			return name.trim();
		}
		return "";
	}

	private static String getUserSID() {
		Class<?> ntSytem = null;
		try {
			ntSytem = Class.forName("com.sun.security.auth.module.NTSystem");
		} catch (final ClassNotFoundException e) {
			try {
				ntSytem = Class
				    .forName("com.ibm.security.auth.module.NTSystem");
			} catch (final ClassNotFoundException e1) {
				
			}
		}
		if (ntSytem != null) {
			try {
				final Object nySyObject = ntSytem.newInstance();
				final Method getUserSID = nySyObject.getClass()
				    .getDeclaredMethod("getUserSID", new Class[] {});
				final Object value = getUserSID.invoke(nySyObject,
				    new Object[] {});
				return (String) value;
			} catch (final Exception e) {
				
			}
		}
		return null;
	}

	private static String replaceEnvVariable(String result) {
		final String rootPath = System.getenv(RegQueryUtil.SYS_ROOT);
		final int index = result.indexOf("%systemroot%");
		if ((index != -1) && (rootPath != null)) {
			result = result.substring(index + "%systemroot%".length(), result
			    .length());
			result = rootPath + result;
		}
		return result;

	}

	private static String runEditQuery(final String exeName, String query) {
		query = RegQueryUtil.formatQuery(query, exeName, RegQueryUtil.EXE_NAME);
		query = RegQueryUtil.formatQuery(query, RegQueryUtil.EDIT_CMD,
		    RegQueryUtil.CMD);
		final String result = RegQueryUtil.executeQuery(query);
		return result;
	}

	private static String runOpenQuery(final String exeName, String query) {
		query = RegQueryUtil.formatQuery(query, exeName, RegQueryUtil.EXE_NAME);
		query = RegQueryUtil.formatQuery(query, RegQueryUtil.OPEN_CMD,
		    RegQueryUtil.CMD);
		final String result = RegQueryUtil.executeQuery(query);
		return result;
	}
}
