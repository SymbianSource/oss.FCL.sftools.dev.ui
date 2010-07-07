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
package com.nokia.tools.screen.ui.utils;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.media.utils.IFileConstants;
import com.nokia.tools.media.utils.IMediaConstants;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.svg.SvgUtil;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.XmlUtil;
import com.nokia.tools.screen.ui.branding.IBrandingManager;
import com.nokia.tools.screen.ui.editor.Messages;
import com.nokia.tools.ui.branding.extension.BrandingExtensionManager;

/**
 * when 3rd party image editing tool is launched, this thread watch file status
 * and updates selected layer if file is changed. 
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

			FileUtils.addForCleanup(this.file);
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

	public FileInfo addWatchedFile(File file, Runnable callback) {
		FileInfo info = new FileInfo(file, callback);
		synchronized (watchedFiles) {
			watchedFiles.add(info);
			if (DebugHelper.debugCleanResource()) {
				DebugHelper.debug(this, "Added: " + file + ", callback: "
						+ callback + ", watch size: " + watchedFiles.size());
			}
		}

		synchronized (this) {
			notify();
		}

		return info;
	}

	public FileInfo addWatchedFile(File file, Runnable callback,
			boolean canUpdate, boolean callbackInUIThread) {
		FileInfo info = new FileInfo(file, callback, canUpdate,
				callbackInUIThread);
		synchronized (watchedFiles) {
			watchedFiles.add(info);
			if (DebugHelper.debugCleanResource()) {
				DebugHelper.debug(this, "Added: " + file + ", callback: "
						+ callback + ", watch size: " + watchedFiles.size());
			}
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
										|| info.initialSize != info.file
												.length()) {

									if (info.callback instanceof RunnableWithParameter) {
										((RunnableWithParameter) info.callback)
												.setParameter(info.file
														.getAbsolutePath());
									}

									if (info.callback != null) {
										try {

											if (info.callbackInUIThread) {
												if (Display.getCurrent() == null) {
													Display
															.getDefault()
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
						if (DebugHelper.debugCleanResource()) {
							DebugHelper.debug(this, "Removed: " + info.file
									+ ", callback: " + info.callback
									+ ", watch size: " + watchedFiles.size());
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static File getWorkDir(String editorPath) {
		return new File(editorPath).getParentFile();
	}

	/**
	 * opens 3rd party program and edit given file. Convenience method, should
	 * not be here, move later.
	 * 
	 * @param editorPath
	 * @param im
	 * @param layer
	 * @param callback
	 *            when image succesfully updated, callback is executed
	 * @param runCallBackInUIThread
	 * @throws IOException
	 */
	public static FileInfo open3rdPartyEditor(String editorPath,
			RenderedImage im, String imagePrefix,
			RunnableWithParameter callback, boolean runCallBackInUIThread)
			throws IOException {
		return open3rdPartyEditor(editorPath, im, imagePrefix, callback,
				runCallBackInUIThread, null, false);
	}

	/**
	 * opens 3rd party program and edit given file. Convenience method, should
	 * not be here, move later.
	 * 
	 * @param editorPath
	 * @param im
	 * @param layer
	 * @param callback
	 *            when image succesfully updated, callback is executed
	 * @param runCallBackInUIThread
	 * @throws IOException
	 */
	public static FileInfo open3rdPartyEditor(String editorPath,
			RenderedImage im, String imagePrefix,
			RunnableWithParameter callback, boolean runCallBackInUIThread,
			String format, boolean removeAplha) throws IOException {

		if (format == null)
			format = "png";

		if (IMediaConstants.PREF_BITMAP_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_BITMAP_EDITOR);
		} else if (IMediaConstants.PREF_VECTOR_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_VECTOR_EDITOR);
		} else if (IMediaConstants.PREF_SOUND_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_SOUND_EDITOR);
		} else if (editorPath == null) {
			editorPath = getEditorPath(IMediaConstants.PREF_BITMAP_EDITOR);
		}

		if (editorPath == null || editorPath.trim().length() == 0) {
			throw new IllegalArgumentException("Editor not defined.");
		}

		if (removeAplha) {
			// remove alpha channel
			im = CoreImage.create(im).removeAlpha().getAwt();
		}

		// create a temp file
		File imFile = new File(FileUtils.generateUniqueFileName(
				imagePrefix == null ? "image" : imagePrefix, format));
		ImageIO.write(im, format, imFile);

		Runtime.getRuntime().exec(editorPath + " " + imFile.getAbsolutePath());

		// add file to watcher thread
		return INSTANCE.addWatchedFile(imFile, callback, true,
				runCallBackInUIThread);
	}

	/**
	 * opens 3rd party program and edit given file. Convenience method, should
	 * not be here, move later.
	 * 
	 * @param program
	 * @param im
	 * @param layer
	 * @param callback
	 *            when image succesfully updated, callback is executed
	 * @param runCallBackInUIThread
	 * @throws IOException
	 */
	public static FileInfo open3rdPartyProgram(Program program,
			RenderedImage im, String imagePrefix,
			RunnableWithParameter callback, boolean runCallBackInUIThread)
			throws IOException {
		return open3rdPartyProgram(program, im, imagePrefix, callback,
				runCallBackInUIThread, null, false);
	}

	/**
	 * opens 3rd party program and edit given file. Convenience method, should
	 * not be here, move later.
	 * 
	 * @param editorPath
	 * @param im
	 * @param layer
	 * @param callback
	 *            when image succesfully updated, callback is executed
	 * @param runCallBackInUIThread
	 * @throws IOException
	 */
	public static FileInfo open3rdPartyProgram(Program program,
			RenderedImage im, String imagePrefix,
			RunnableWithParameter callback, boolean runCallBackInUIThread,
			String format, boolean removeAplha) throws IOException {

		if (format == null)
			format = "png";

		if (removeAplha) {
			// remove alpha channel
			im = CoreImage.create(im).removeAlpha().getAwt();
		}

		// create a temp file
		File imFile = new File(FileUtils.generateUniqueFileName(
				imagePrefix == null ? "image" : imagePrefix, format));
		ImageIO.write(im, format, imFile);

		program.execute(imFile.getAbsolutePath());

		// add file to watcher thread
		return INSTANCE.addWatchedFile(imFile, callback, true,
				runCallBackInUIThread);
	}

	private static String getEditorPath(String type) {
		String editorPath = UtilsPlugin.getDefault().getPreferenceStore()
				.getString(type);
		if (!(new File(editorPath).exists()))
			throw new IllegalArgumentException();
		return UtilsPlugin.getDefault().getPreferenceStore().getString(type);
	}

	public static FileInfo open3rdPartyEditor(String editorPath,
			String fileAbsolutePath, String imageDisplayName,
			RunnableWithParameter callback, boolean runCallBackInUIThread)
			throws IOException {

		if (IMediaConstants.PREF_BITMAP_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_BITMAP_EDITOR);
		} else if (IMediaConstants.PREF_VECTOR_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_VECTOR_EDITOR);
		} else if (IMediaConstants.PREF_SOUND_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_SOUND_EDITOR);
		}
		// determine which use from extension
		String extension = FileUtils.getExtension(fileAbsolutePath)
				.toLowerCase().trim();

		if (editorPath == null) {
			try {
				if (IFileConstants.FILE_EXT_SVG.equals(extension))
					editorPath = getEditorPath(IMediaConstants.PREF_VECTOR_EDITOR);
				else if ("wav".equals(extension))
					editorPath = getEditorPath(IMediaConstants.PREF_SOUND_EDITOR);
				else
					editorPath = getEditorPath(IMediaConstants.PREF_BITMAP_EDITOR);
			} catch (Exception e) {
			}
		}

		// no need to launch the process if it's not yet defined
		if (editorPath == null || editorPath.trim().length() == 0) {
			throw new IllegalArgumentException("Editor not defined");
		}

		String sourceFileName = fileAbsolutePath;
		// make a copy of the file and pass it to the filechangewatchthread
		File sourceFile = new File(sourceFileName);

		String extensions = FileUtils.getExtension(fileAbsolutePath);

		String destinationFileName = FileUtils.generateUniqueFileName(
				imageDisplayName == null ? "image" : imageDisplayName,
				extensions);
		File destinationFile = new File(destinationFileName);

		copy(sourceFile, destinationFile);

		boolean isSvg = false;
		if (IFileConstants.FILE_EXT_SVG.equals(extension)) {
			isSvg = true;
		}
		if (isSvg) {
			try {
				Document document = XmlUtil.parse(destinationFile);
				SvgUtil.fixDocTypeProblem(document);
				XmlUtil.write(document, destinationFile);
				isSvg = false;
			} catch (Exception e) {
				
				e.printStackTrace();
			}

		}

		Runtime.getRuntime().exec(
				editorPath + " " + destinationFile.getAbsolutePath());

		// add file to watcher thread
		return INSTANCE.addWatchedFile(destinationFile, callback, true,
				runCallBackInUIThread);
	}

	public static FileInfo open3rdPartyProgram(Program program,
			String fileAbsolutePath, String imageDisplayName,
			RunnableWithParameter callback, boolean runCallBackInUIThread)
			throws IOException {

		// determine which use from extension
		String extension = FileUtils.getExtension(fileAbsolutePath)
				.toLowerCase().trim();

		String sourceFileName = fileAbsolutePath;
		// make a copy of the file and pass it to the filechangewatchthread
		File sourceFile = new File(sourceFileName);

		String extensions = FileUtils.getExtension(fileAbsolutePath);

		String destinationFileName = FileUtils.generateUniqueFileName(
				imageDisplayName == null ? "image" : imageDisplayName,
				extensions);
		File destinationFile = new File(destinationFileName);

		copy(sourceFile, destinationFile);

		boolean isSvg = false;
		if (IFileConstants.FILE_EXT_SVG.equals(extension)) {
			isSvg = true;
		}
		if (isSvg) {
			try {
				Document document = XmlUtil.parse(destinationFile);
				SvgUtil.fixDocTypeProblem(document);
				XmlUtil.write(document, destinationFile);
				isSvg = false;
			} catch (Exception e) {
			
				e.printStackTrace();
			}

		}

		program.execute(destinationFile.getAbsolutePath());

		// add file to watcher thread
		return INSTANCE.addWatchedFile(destinationFile, callback, true,
				runCallBackInUIThread);
	}

	public static FileInfo open3rdPartyVectorEditor(String editorPath,
			String imageFileName, String imagePrefix, RenderedImage image,
			Runnable callback, boolean runCallBackInUIThread)
			throws IOException {

		if (editorPath == null
				|| IMediaConstants.PREF_VECTOR_EDITOR.equals(editorPath)) {
			editorPath = getEditorPath(IMediaConstants.PREF_VECTOR_EDITOR);
		}

		if (editorPath == null || editorPath.trim().length() == 0) {
			throw new IllegalArgumentException("Editor not defined.");
		}

		// create a temp file
		File imFile = new File(FileUtils.generateUniqueFileName(
				imagePrefix == null ? "image" : imagePrefix,
				IFileConstants.FILE_EXT_SVG));

		if (!imageFileName.toLowerCase().endsWith(
				IFileConstants.FILE_EXT_DOTSVG)) {

			DOMImplementation impl = SVGDOMImplementation
					.getDOMImplementation();
			String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
			Document doc = impl.createDocument(svgNS,
					IFileConstants.FILE_EXT_SVG, null);

			// get the root element (the svg element)
			Element svgRoot = doc.getDocumentElement();

			// set the width and height attribute on the root svg element
			svgRoot.setAttributeNS(null, "width", "100%");
			svgRoot.setAttributeNS(null, "height", "100%");
			svgRoot
					.setAttributeNS(null, "preserveAspectRatio",
							"xMidYMid meet");
			svgRoot.setAttributeNS(null, "version", "1.0");
			svgRoot.setAttributeNS(null, "viewBox", "0 0 " + image.getWidth()
					+ " " + image.getHeight());
			svgRoot.setAttributeNS(null, "zoomAndPan", "magnify");

			OutputStream ostream = new FileOutputStream(imFile);
			Writer out = new OutputStreamWriter(ostream, "UTF-8");
			DOMUtilities.writeDocument(doc, out);
			out.close();
			ostream.close();
		} else {
			copy(new File(imageFileName), imFile);

			String extension = FileUtils.getExtension(imFile).toLowerCase()
					.trim();
			boolean isSvg = false;
			if (IFileConstants.FILE_EXT_SVG.equals(extension)) {
				isSvg = true;
			}
			if (isSvg) {
				try {
					Document document = XmlUtil.parse(imFile);
					SvgUtil.fixDocTypeProblem(document);
					XmlUtil.write(document, imFile);
					isSvg = false;
				} catch (Exception e) {
				
					e.printStackTrace();
				}

			}

		}

		Runtime.getRuntime().exec(
				new String[] { editorPath, imFile.getAbsolutePath() }, null,
				getWorkDir(editorPath));

		// add file to watcher thread
		return INSTANCE.addWatchedFile(imFile, callback, true,
				runCallBackInUIThread);
	}

	public static void displayThirdPartyEditorMisssingErrorMessageBox() {
		IBrandingManager branding = BrandingExtensionManager
				.getBrandingManager();
		Image image = null;
		if (branding != null) {
			image = branding.getIconImageDescriptor().createImage();
		}
		MessageDialog dialog = new MessageDialog(PlatformUI.getWorkbench()
				.getDisplay().getActiveShell(),
				Messages.FileChangeWatchThread_editorSettingsError, image,
				Messages.FileChangeWatchThread_editorError, 1, new String[] {
						IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		dialog.open();
		if (image != null) {
			image.dispose();
		}
	}

	public static void copy(File source, File dest) throws IOException {
		FileUtils.copyFile(source, dest);
	}
}
