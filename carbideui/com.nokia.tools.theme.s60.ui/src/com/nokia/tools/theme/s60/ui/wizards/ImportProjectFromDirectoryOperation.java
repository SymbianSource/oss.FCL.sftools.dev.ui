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
package com.nokia.tools.theme.s60.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.nokia.tools.resource.util.FileUtils;

/**
 * The Class ImportProjectFromDirectoryOperation.
 */
public class ImportProjectFromDirectoryOperation implements
		IRunnableWithProgress {

	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];
	private IProgressMonitor monitor = null;
	private String source = null;
	private IProject project = null;
	private int noOfFiles;

	/**
	 * Instantiates a new import project from directory operation.
	 * 
	 * @param source
	 *            the source
	 * @param project
	 *            the project
	 * @param noOfFiles
	 */
	public ImportProjectFromDirectoryOperation(String source, IProject project,
			int noOfFiles) {
		this.source = source;
		this.project = project;
		this.noOfFiles = noOfFiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core
	 * .runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor aMonitor)
			throws InvocationTargetException, InterruptedException {
		aMonitor.beginTask("", noOfFiles);
		aMonitor.worked(1);
		monitor = new SubProgressMonitor(aMonitor, noOfFiles);
		monitor.beginTask("", noOfFiles);

		if (!project.exists()) {
			try {
				project.create(monitor);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		// Copy from source to destination folder
		File targetLocation = new File(project.getLocation().toString());
		try {
			copyDirectory(new File(source), targetLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		monitor.setTaskName("");
		aMonitor.setTaskName("");
		monitor.done();
	}

	/**
	 * Copy directory.
	 * 
	 * @param sourceLocation
	 *            the source location
	 * @param targetLocation
	 *            the target location
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {
		monitor.setTaskName("Importing... " + sourceLocation.getName());
		monitor.worked(1);
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {
			// copy(sourceLocation, targetLocation);
			FileUtils.copyUsingNIO(sourceLocation, targetLocation);
		}
	}

	/**
	 * Copy.
	 * 
	 * @param from
	 *            the source file
	 * @param to
	 *            the target file
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void copy(File from, File to) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
}
