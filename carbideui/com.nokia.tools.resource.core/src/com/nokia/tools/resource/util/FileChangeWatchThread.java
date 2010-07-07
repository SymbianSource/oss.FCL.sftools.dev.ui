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

package com.nokia.tools.resource.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.nokia.tools.resource.util.FileUtils;

/**
 * The basic functionality to watch the changes in a file 
 *         
 */
public class FileChangeWatchThread extends Thread {

	private static final long CHECK_INTERVAL = 1000; // in millis

	public static final FileChangeWatchThread INSTANCE;

	static {
		INSTANCE = new FileChangeWatchThread();
		INSTANCE.start();
	}

	public static class FileInfo {
		File file;

		long initialDate, initialSize;

		Runnable callback;

		boolean canUpdate;

		private boolean callbackInUIThread;

		FileInfo(File file, Runnable callback) {
			this(file, callback, true, callback != null);
		}

		FileInfo(File file, Runnable callback, boolean canUpdate,
		    boolean callbackInUIThread) {
			this.file = file.getAbsoluteFile();
			this.initialDate = file.lastModified();
			this.initialSize = file.length();
			this.callback = callback;
			this.canUpdate = canUpdate;
			this.callbackInUIThread = callbackInUIThread;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FileInfo) {
				return file.equals(((FileInfo) obj).file);
			} else {
				return super.equals(obj);
			}
		}

		@Override
		public int hashCode() {
			return file.hashCode();
		}

		public boolean isCanUpdate() {
			return canUpdate;
		}

		public void setCanUpdate(boolean canUpdate) {
			this.canUpdate = canUpdate;
		}
	}

	// file we are watching
	private Set<FileInfo> watchedFiles = new HashSet<FileInfo>();

	private FileChangeWatchThread() {
		try {
			setDaemon(true);
			setName(FileChangeWatchThread.class.getSimpleName());
			setPriority(Thread.MIN_PRIORITY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FileInfo addWatchedFile(File file, Runnable callback,
	    boolean canUpdate, boolean callbackInUIThread) {
		FileInfo info = new FileInfo(file, callback, canUpdate,
		    callbackInUIThread);
		synchronized (watchedFiles) {
			watchedFiles.add(info);
		}

		synchronized (this) {
			notify();
		}

		return info;
	}

	@Override
	public void run() {
		int counter = 0;
		while (true) {
			try {
				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (InterruptedException e) {
				}
				counter++;
				Set<FileInfo> files = null;
				synchronized (watchedFiles) {
					files = new HashSet<FileInfo>(watchedFiles);
				}

				for (FileInfo info : files) {
					if (info.canUpdate) {
						if (info.initialDate != info.file.lastModified()
						    || info.initialSize != info.file.length()) {
							// file modified
							try {

								if (info.initialDate != info.file
								    .lastModified()
								    || info.initialSize != info.file.length()) {

									if (info.callback != null) {
										try {

											if (info.callbackInUIThread) {
												if (Display.getCurrent() == null) {
													Display.getDefault()
													    .asyncExec(
													        info.callback);
												}
											} else
												info.callback.run();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									info.initialDate = info.file.lastModified();
									info.initialSize = info.file.length();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					} else {
						watchedFiles.remove(info);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static FileInfo open3rdPartyEditor(String editorPath,
	    String fileAbsolutePath, Runnable callback,
	    boolean runCallBackInUIThread) throws IOException {
		
		//Convert to canonical path sine some editors can give problems
		if(new File(fileAbsolutePath).exists())
		{
			try {
	            fileAbsolutePath = new File(fileAbsolutePath).getCanonicalPath();
            } catch (IOException e) {
	            
            }
            catch(SecurityException se)
            {
            	
            }
		}

		if (editorPath == null) {
			String extn = FileUtils.getExtension(fileAbsolutePath);
			Program program = Program.findProgram(extn);
			if (program != null) {
				Program.launch(fileAbsolutePath);
			} else {
				throw new IllegalArgumentException("Editor not defined.");
			}
		} else {
			Runtime.getRuntime().exec(editorPath + " \"" + fileAbsolutePath + "\"");
		}

		// add file to watcher thread
		return INSTANCE.addWatchedFile(new File(fileAbsolutePath), callback,
		    true, runCallBackInUIThread);
	}
}
