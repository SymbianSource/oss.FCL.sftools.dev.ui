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

package com.nokia.tools.packaging.commandline;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPlatformRunnable;

/**
 * This class controls all aspects of the application's execution
 * 
 * 
 */
public class Application implements IPlatformRunnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		String[] arg = (String[]) args;
		List<String> list = new ArrayList<String>(arg.length);
		for (String str : arg) {
			if (!"-pdelaunch".equals(str)) {
				list.add(str);
			}
		}
		try {
			PrintStream outStream = new PrintStream(System.out, true);
			// command output messages
			CommandlineStream outputStream = new CommandlineStream(outStream);
			System.setOut(outputStream);
			System.setErr(new PrintStream(new FileOutputStream("log.err")));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		Packager.main(list.toArray(new String[list.size()]));
		return IPlatformRunnable.EXIT_OK;
	}
}

class CommandlineStream extends PrintStream {
	PrintStream printStream;

	public CommandlineStream(PrintStream printStream) {
		super(printStream, true);
		this.printStream = printStream;
	}

	public void println(String info) {
		synchronized (this) {
			if (null != info && null != printStream) {
				if (info.startsWith(PackagerMessages.Packager_Commandline)) {
					info = info.substring(PackagerMessages.Packager_Commandline
							.length(), info.length());
					// if (info.startsWith("ERROR") || info.startsWith("USAGE:")
					// || info.startsWith("WARNING:"))
						printStream.println(info);
				}
			}
		}
	}

	public void print(String status) {
		synchronized (this) {
			if (status.startsWith(PackagerMessages.Packager_Commandline)) {
				status = status.substring(PackagerMessages.Packager_Commandline
						.length(), status.length());
				if (null != status && null != printStream) {
					if (status.startsWith("STATUS:"))
						printStream.print("\r" + status);
				}
			}
		}
	}
}