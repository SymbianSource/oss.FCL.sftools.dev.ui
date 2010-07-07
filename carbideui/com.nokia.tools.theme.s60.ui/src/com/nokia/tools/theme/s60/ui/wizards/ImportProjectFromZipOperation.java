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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * The Class ImportProjectFromZipOperation.
 */
public class ImportProjectFromZipOperation implements IRunnableWithProgress {

	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];

	private String source = null;
	private IProject project = null;
	private IProgressMonitor monitor = null;
	private int noOfFiles;
	private ZipFile zipFile;

	/**
	 * Instantiates a new import project from zip operation.
	 * 
	 * @param source
	 *            the source
	 * @param project
	 *            the project
	 */
	public ImportProjectFromZipOperation(String source, IProject project) {
		this.source = source;
		this.project = project;
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
		try {
			zipFile = new ZipFile(source);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		noOfFiles = zipFile.size();
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
			copyZipDirectory(source, targetLocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		monitor.setTaskName("");
		aMonitor.setTaskName("");
		monitor.done();
	}

	/**
	 * Copy zip directory.
	 * 
	 * @param sourceLocation
	 *            the source location
	 * @param targetLocation
	 *            the target location
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void copyZipDirectory(String sourceLocation, File targetLocation)
			throws IOException {

		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		if (!targetLocation.exists()) {
			targetLocation.mkdir();
		}

		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String entryName = entry.getName();

			if (entry.isDirectory()) {
				if (!new File(targetLocation, entry.getName()).exists()) {
					(new File(targetLocation, entry.getName())).mkdirs();
				}
				monitor.subTask("Importing... " + entry.getName());
				monitor.worked(1);
				continue;
			}

			File newFile = new File(entryName);
			if (newFile.getParent() != null) {
				File directory = new File(targetLocation, newFile.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
			}

			monitor.subTask("Importing... " + entry.getName());
			monitor.worked(1);
			copy(zipFile.getInputStream(entry), new BufferedOutputStream(
					new FileOutputStream(new File(targetLocation, entry
							.getName()))));
		}

		zipFile.close();

	}

	/**
	 * Copy.
	 * 
	 * @param in
	 *            the input stream
	 * @param bout
	 *            the buffered output stream
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void copy(InputStream in, BufferedOutputStream bout)
			throws IOException {
		while (true) {
			synchronized (buffer) {
				int len = in.read(buffer);
				if (len == -1) {
					break;
				}
				bout.write(buffer, 0, len);
			}
		}

		in.close();
		bout.close();
	}

}
