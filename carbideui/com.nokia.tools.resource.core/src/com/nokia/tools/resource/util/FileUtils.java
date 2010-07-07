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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.text.html.StyleSheet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.osgi.framework.internal.core.Constants;
import org.eclipse.osgi.internal.baseadaptor.AdaptorUtil;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.Bundle;

import com.nokia.tools.resource.core.Activator;

/**
 * This class provides a collection of static methods for common flie
 * manipulation.
 * 
 */
public class FileUtils {
	/**
	 * Buffer size that is of good performance in most cases.
	 */
	public static final int BUF_SIZE = 0xffff;
	/**
	 * Cleanup job used for deleting temporary files.
	 */
	private static final Runnable CLEANUP_JOB = new FileGarbageCollector();
	/**
	 * Holds temporary resources that are candidates for removal during system
	 * shutdown.
	 */
	private static Set<File> RESOURCES_TO_CLEAN = new HashSet<File>();

	private static boolean systemTempDirectoryCreated;

	private static final String FILE_PROTOCOL_PREFIX = "file://";

	private static final String FILE_PROTOCOL_PREFIX_REPLACEMENT = "\\\\";

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(CLEANUP_JOB));
	}

	/**
	 * No construction.
	 */
	private FileUtils() {
	}

	/**
	 * Checks if the given path is an absolute path.
	 * 
	 * @param path
	 *            the path to be checked.
	 * @return true if the path is absolute, false otherwise.
	 */
	public static boolean isAbsolutePath(String path) {
		if (!StringUtils.isEmpty(path)) {
			return new File(path).isAbsolute();
		}
		return false;
	}

	/**
	 * Copies source file to destination file.
	 * 
	 * @param sourceFileName
	 *            source file path.
	 * @param destFileName
	 *            destination file path.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static void copyFile(String sourceFileName, String destFileName)
			throws IOException {
		copyFile(new File(sourceFileName), new File(destFileName));
	}

	/**
	 * Copies source file to destination file.
	 * 
	 * @param src
	 *            source file to copy.
	 * @param dst
	 *            destination file.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static void copyFile(File src, File dst) throws IOException {
		copyFile(src, dst, null);
	}

	/**
	 * Copies source file to destination file with progress monitor service.,
	 * this will create destination folder if it doesn't exist.
	 * 
	 * @param src
	 *            source file to copy.
	 * @param dst
	 *            destination file.
	 * @param monitor
	 *            the progress monitor, can be null.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static void copyFile(File src, File dst, IProgressMonitor monitor)
			throws IOException {
		if (src.equals(dst)) {
			return;
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			dst.getParentFile().mkdirs();
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			copy(in, out, monitor);
		} finally {
			close(in);
			close(out);
		}
	}

	/**
	 * Copy using nio.
	 * 
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void copyUsingNIO(String src, String dest) throws IOException {
		File sourceFile = new File(src);
		File destFile = new File(dest);

		copyUsingNIO(sourceFile, destFile);
	}

	/**
	 * Copy using nio.
	 * 
	 * @param sourceFile
	 *            the source file
	 * @param destFile
	 *            the dest file
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void copyUsingNIO(File sourceFile, File destFile)
			throws IOException {
		if (!sourceFile.exists()) {
			return;
		}
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * Copies the source content to the destination file, this will create
	 * destination folder if it doesn't exist.
	 * 
	 * @param in
	 *            input stream from where to read source content.
	 * @param file
	 *            the destination file.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static void copyFile(InputStream in, File file) throws IOException {
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		FileOutputStream out = new FileOutputStream(file);
		copy(in, out);
	}

	/**
	 * Copies file from the given input stream to output stream.
	 * 
	 * @param in
	 *            the input stream to read source content.
	 * @param out
	 *            the output stream to write content.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static void copy(InputStream in, OutputStream out)
			throws IOException {
		copy(in, out, null);
	}

	/**
	 * Copies the data from the input stream to the output stream.
	 * 
	 * @param in
	 *            the input stream to read source content.
	 * @param out
	 *            the output stream to write content.
	 * @param monitor
	 *            the progress monitor, can be null.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static void copy(InputStream in, OutputStream out,
			IProgressMonitor monitor) throws IOException {

		byte[] buf = new byte[BUF_SIZE];
		int val;
		int length = in.available();
		int slice = Math.max(1, length / BUF_SIZE);

		try {
			while ((val = in.read(buf)) > 0) {
				out.write(buf, 0, val);
				out.flush();
				if (monitor != null) {
					monitor.worked(slice);
				}
			}
		} finally {
			close(in);
			close(out);
		}
	}

	/**
	 * Deletes a directory and its subdirectories.
	 * 
	 * @param path
	 *            the directory to be deleted.
	 * @return true if the directory has been successfully deleted.
	 */
	public static boolean deleteDirectory(File path) {
		return deleteDirectory(path, null);
	}

	/**
	 * Deletes a directory and its subdirectories.
	 * 
	 * @param path
	 *            the directory to be deleted.
	 * @param monitor
	 *            the progress monitor, can be null.
	 * @return true if the directory has been successfully deleted.
	 */
	public static boolean deleteDirectory(File path, IProgressMonitor monitor) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i], monitor);
				} else {
					files[i].delete();
					if (monitor != null) {
						monitor.worked(1);
					}
				}
			}
		}
		return (path.delete());
	}

	/**
	 * Strips the extension from the given file name. * Note: This will remove
	 * the path separators if the string has path separators in it. ie if the
	 * input is c:\\abcd.xyz output will abcd
	 * 
	 * @param file
	 *            File from where the extension is stripped.
	 * @return the filename without extension.
	 */
	public static String stripExtension(File file) {
		return stripExtension(file.getPath());

	}

	/**
	 * Strips the extension from the given file name. Note: This wont remove the
	 * path separators if the string has path separators in it. ie if the input
	 * is c:\\abcd.xyz output will be c:\\abcd and NOT abcd
	 * 
	 * @param filename
	 *            file name from where the extension is stripped.
	 * @return the filename without extension.
	 */
	public static String stripExtension(String filename) {
		return new Path(filename).removeFileExtension().lastSegment();
	}

	/**
	 * Finds the extension from the given file name.
	 * 
	 * @param filename
	 *            file name from where the extension is determined.
	 * @return the extension.
	 */
	public static String getExtension(String filename) {
		if (filename == null)
			return null;
		int i = filename.lastIndexOf('.');
		if (i != -1 && i < filename.length()) {
			return filename.substring(++i);
		}
		return null;
	}

	/**
	 * Finds the extension from the given file.
	 * 
	 * @param file
	 *            file from where the extension is determined.
	 * @return the extension.
	 */
	public static String getExtension(File f) {
		return f == null ? null : getExtension(f.getName());
	}

	/**
	 * Generate unique file name in system temporary directory, returns absolute
	 * path.
	 * 
	 * @param prefix
	 *            the prefix to be used for the temporary file.
	 * @param extension
	 *            the extension to be used for the temporary file.
	 * @return absolute file name, WITH extension
	 */
	public static String generateUniqueFileName(String prefix, String extension) {
		String dir = getTemporaryDirectory();
		return dir + File.separator
				+ generateUniqueFileName(prefix, dir, extension);
	}

	/**
	 * @return the temporary directory.
	 */
	public static String getTemporaryDirectory() {
		String tempDir = System.getProperty("java.io.tmpdir");
		if (!systemTempDirectoryCreated && tempDir != null) {
			File dir = new File(tempDir);
			if (!dir.isDirectory()) {
				Activator.warn("System temporary directory: " + tempDir
						+ " is not a directory.");
				if (!dir.mkdirs()) {
					Activator.warn("Cannot create system temporary directory: "
							+ tempDir);
				}
			}
			systemTempDirectoryCreated = true;
		}
		return tempDir;
	}

	/**
	 * Generate unique new file name in given dir.
	 * 
	 * @param prefix
	 *            the prefix to be used for the temporary file.
	 * @param ext
	 *            the extension to be used for the temporary file.
	 * @return unique file name WITH extension
	 */
	public static String generateUniqueFileName(String prefix,
			String directory, String ext) {
		return generateUniqueFileName(prefix, directory, ext, true);
	}

	/**
	 * Generate unique new file name in given dir.
	 * 
	 * @param prefix
	 *            the prefix to be used for the temporary file.
	 * @param ext
	 *            the extension to be used for the temporary file.
	 * @param returnWithExtension
	 *            true if the returned path should contain extension, false
	 *            otherwise.
	 * @return unique file name WITH or W/O extension, depend on
	 *         'returnWithExtension'
	 */
	public static String generateUniqueFileName(String prefix,
			String directory, String ext, boolean returnWithExtension) {

		// Batik don't like '%' in file name:
		prefix = prefix.replace('%', '_');

		int count = 0;
		String unique = prefix;
		File file = new File(directory + File.separator + unique + '.' + ext);
		while (file.exists()) {
			unique = prefix + "_" + (++count);
			file = new File(directory + File.separator + unique + '.' + ext);
		}
		return returnWithExtension ? unique + '.' + ext : unique;
	}

	/**
	 * Generates unique temporary directory with the given name.
	 * 
	 * @param desiredName
	 *            the desired name.
	 * @return the path of the generated directory.
	 */
	public static String generateUniqueDirectory(String desiredName) {
		String uniqueDirName = generateUniqueDirectoryName(
				getTemporaryDirectory(), desiredName);
		File f = new File(uniqueDirName);
		if (!f.exists()) {
			boolean b = f.mkdir();
			if (b) {
				// ThemeController.listToDelete.add(uniqueDirName);
				return uniqueDirName;
			} else {
				return null;
			}
		}
		return uniqueDirName;
	}

	/**
	 * Returns a directory name that does not exist in the given directory
	 * 
	 * @param baseDir
	 *            The path of the base directory in which a unique directory has
	 *            to be created
	 * @param preferredName
	 *            The preferred name of the directory to be created
	 * @throws ThemeAppException
	 *             if unable to create a unique directory name
	 */
	public static String generateUniqueDirectoryName(String baseDir,
			String preferredName) {

		StringBuilder fName = new StringBuilder();

		fName.append(baseDir);
		// if ( ! baseDir.equals(getTempDirectory())) {
		// Earlier temp dir was getting appended with additional file.separator

		// for temp dir alredy basedir is appended with file.separator.
		fName.append(File.separator);
		// }
		fName.append(preferredName);

		// Create the directory
		File f = new File(fName.toString());
		boolean status = f.exists();

		int range = Integer.MAX_VALUE / 3 * 2;
		int randId = 0;
		Random random = new Random();

		StringBuffer tmpfName = new StringBuffer(fName.toString());

		try {

			// Keep trying till you get a non-existing directory name
			while (status == true) {
				randId = random.nextInt(range);

				tmpfName.delete(0, tmpfName.length());

				// Now form the directory name with the random id
				tmpfName.append(fName.toString());
				tmpfName.append(randId);

				// Check if the directory exists
				f = new File(tmpfName.toString());
				status = f.exists();

			} // end while

			String tempFilePath = new File(tmpfName.toString())
					.getCanonicalPath();

			// return tmpfName.toString();
			return tempFilePath;

		} catch (Exception e) {
			Activator.error(e);
		}
		return null;
	}

	/**
	 * Creates the absolute path.
	 * 
	 * @param baseDir
	 *            The directory which is taken as the reference
	 * @param file
	 *            The name of the file with its relative path
	 */
	public static String makeAbsolutePath(String baseDir, String file) {

		String absFilePath = null;
		try {
			File tfile = new File(file);
			if (tfile.exists())
				return file;
			boolean fileStatus = tfile.isAbsolute();

			if (fileStatus) {
				absFilePath = tfile.getAbsolutePath();
			} else {
				String filePath = baseDir + File.separator + file;
				absFilePath = new File(filePath).getAbsolutePath();
			}
		} catch (Exception e) {
			Activator.error(e);
		}

		return absFilePath;

	}

	/**
	 * Tests if the file name is valid on the current platform. This passes the
	 * file name to the OS class to check for the specical names that are not
	 * allowed on the platform.
	 * 
	 * @param fileName
	 *            name of the file
	 * @return true if the file name is valid, false otherwise.
	 */
	public static boolean isFileValid(String fileName) {
		return fileName != null && isFileValid(new Path(fileName));
	}

	/**
	 * Tests if the file name is valid on the current platform. This passes the
	 * file name to the OS class to check for the specical names that are not
	 * allowed on the platform.
	 * 
	 * @param file
	 *            the file.
	 * @return true if the file name is valid, false otherwise.
	 */
	public static boolean isFileValid(File file) {
		return file != null && isFileValid(file.getAbsolutePath());
	}

	/**
	 * Tests if the path is valid on the current platform. This passes the file
	 * name to the OS class to check for the specical names that are not allowed
	 * on the platform.
	 * 
	 * @param path
	 *            the path to the file.
	 * @return true if the path is valid, false otherwise.
	 */
	public static boolean isFileValid(IPath path) {
		return isFileValid(path, IResource.FILE | IResource.FOLDER);
	}

	/**
	 * Tests if the path is valid on the current platform. This passes the file
	 * name to the OS class to check for the specical names that are not allowed
	 * on the platform.
	 * 
	 * @param path
	 *            the path to the file.
	 * @param typeMask
	 *            bitwise-or of the resource type constants ( FILE,FOLDER)
	 *            indicating expected resource type(s)
	 * @return true if the path is valid, false otherwise.
	 */
	public static boolean isFileValid(IPath path, int typeMask) {
		if (path == null) {
			return false;
		}
		// use IResource.FILE in validateName, the typeMask is ignored
		if ((IResource.FILE & typeMask) != 0) {
			if (ResourcesPlugin.getWorkspace().validateName(path.toString(),
					IResource.FILE).isOK()) {
				return true;
			}
		}
		if ((IResource.FOLDER & typeMask) != 0) {
			String[] segments = path.segments();
			boolean isValid = true;
			for (String segment : segments) {
				if (!ResourcesPlugin.getWorkspace().validateName(segment,
						IResource.FILE).isOK()) {
					isValid = false;
					break;
				}
			}
			return isValid;
		}
		return false;
	}

	/**
	 * Tests if the file name is valid on the current platform and also whether
	 * file exists and is readable. This passes the file name to the OS class to
	 * check for the specical names that are not allowed on the platform.
	 * 
	 * @param fileName
	 *            name of the file.
	 * @return true if the file name is valid and accessible.
	 */
	public static boolean isFileValidAndAccessible(String fileName) {
		return fileName != null && fileName.length() < 512
				&& isFileValidAndAccessible(new File(fileName));
	}

	/**
	 * Tests if the path is valid on the current platform and also whether file
	 * exists and is readable. This passes the file name to the OS class to
	 * check for the specical names that are not allowed on the platform.
	 * 
	 * @param path
	 *            the path to the file.
	 * @return true if the path is valid and accessible.
	 */
	public static boolean isFileValidAndAccessible(Path path) {
		return path != null && isFileValidAndAccessible(path.toFile());
	}

	/**
	 * Tests if the file name is valid on the current platform and also whether
	 * file exists and is readable. This passes the file name to the OS class to
	 * check for the specical names that are not allowed on the platform.
	 * 
	 * @param file
	 *            the file.
	 * @return true if the file name is valid and accessible.
	 */
	public static boolean isFileValidAndAccessible(File file) {
		if (isFileValid(file)) {
			return file.canRead();
		}
		return false;
	}

	public static void copyDir(File inDir, File outDir) throws IOException {
		copyDir(inDir, outDir, null);
	}

	/**
	 * Does a recursive copy of one directory to another.
	 * 
	 * @param inDir
	 *            input directory to copy.
	 * @param outDir
	 *            output directory to copy to.
	 * @throws IOException
	 *             if any error occurs during the copy.
	 */
	public static void copyDir(File inDir, File outDir, String[] exclude)
			throws IOException {
		copyDir(inDir, outDir, true, exclude);
	}

	public static void copyDir(File inDir, File outDir, boolean override,
			String[] exclude) throws IOException {
		String[] files = inDir.list();
		if (files != null && files.length > 0) {
			outDir.mkdirs();
			for (int i = 0; i < files.length; i++) {
				File inFile = new File(inDir, files[i]);
				File outFile = new File(outDir, files[i]);

				boolean cont = false;
				if (exclude != null)
					for (String excludeItem : exclude) {
						if (inFile.getName().equals(excludeItem)) {
							cont = true;
							break;
						}
					}
				if (cont)
					continue;

				if (inFile.isDirectory()) {
					copyDir(inFile, outFile, exclude);
				} else {
					if (!outFile.exists() || override) {
						InputStream in = new FileInputStream(inFile);
						AdaptorUtil.readFile(in, outFile);
					}
				}
			}
		}
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
	public static void copyDirNIO(File sourceLocation, File targetLocation)
			throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirNIO(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {
			// copy(sourceLocation, targetLocation);
			copyUsingNIO(sourceLocation, targetLocation);
		}
	}

	/**
	 * Converts the file name to a url.
	 * 
	 * @param fileName
	 *            the file name.
	 * @return the converted url.
	 */
	public static URL toURL(String fileName) {
		return fileName == null ? null : toURL(new File(fileName));
	}

	/**
	 * Converts the file to a url.
	 * 
	 * @param file
	 *            the file to be converted.
	 * @return the converted url or null if conversion failed.
	 */
	public static URL toURL(File file) {
		try {
			return file == null ? null : new URL(file.toURI().toURL()
					.toString().replace("+", "%2B"));
		} catch (MalformedURLException e) {
			Activator.error(e);
			return null;
		}
	}

	/**
	 * Reads file content.
	 * 
	 * @param file
	 *            the file from where the content will be read.
	 * @return the file content in a byte array.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static byte[] readBytes(File file) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return readBytes(in);
		} finally {
			close(in);
		}
	}

	public static void writeBytes(File file, byte[] bytes) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(bytes, 0, bytes.length);
		} finally {
			close(out);
		}
	}

	/**
	 * Reads content from the given stream.
	 * 
	 * @param in
	 *            the input stream to read data from.
	 * @return the read content.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static byte[] readBytes(InputStream in) throws IOException {
		try {
			byte[] buf = new byte[BUF_SIZE];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int read;
			while ((read = in.read(buf)) > 0) {
				out.write(buf, 0, read);
			}
			return out.toByteArray();
		} finally {
			// Close the input stream and return bytes
			close(in);
		}
	}

	public static void zip(OutputStream out, File folder, String prefix,
			int level) throws IOException {
		ZipOutputStream zout = null;
		try {
			zout = new ZipOutputStream(new BufferedOutputStream(out));
			zout.setLevel(level);
			zip(zout, folder, prefix);
			zout.finish();
		} finally {
			try {
				if (zout != null) {
					zout.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static void zip(ZipOutputStream out, File file, String prefix)
			throws IOException {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				zip(out, f, prefix == null ? ""
						: (prefix + File.separator + file.getName()));
			}
		} else {
			writeZipFile(out, file, prefix);
		}
	}

	public static void writeZipFile(ZipOutputStream out, File file,
			String prefix) throws IOException {
		InputStream in = null;
		if (file.isFile() && file.canRead()) {
			try {
				if (prefix == null) {
					prefix = "";
				}
				// use "/" should be fine but import doesn't work
				out.putNextEntry(new ZipEntry(
						StringUtils.isEmpty(prefix) ? file.getName() : (prefix
								+ File.separator + file.getName())));
				int read;
				byte[] buf = new byte[FileUtils.BUF_SIZE];
				in = new BufferedInputStream(new FileInputStream(file),
						FileUtils.BUF_SIZE);
				while ((read = in.read(buf)) > 0) {
					out.write(buf, 0, read);
					out.flush();
				}
			} finally {
				close(in);
				out.closeEntry();
			}
		}
	}

	/**
	 * This method writes the file to the out stream into a location specified
	 * by prefix. Used progress monitor to check the progress.
	 * 
	 * @param out
	 * @param file
	 * @param prefix
	 * @param monitor
	 * @param workSize
	 *            This is the size used by monitor. If file is of 102400 bytes
	 *            and if buffer size is 1024 bytes, this work is done in 100
	 *            iterations. At any point of these iterations, this method may
	 *            be aborted through the monitor. If workSize is 100, for each
	 *            iteration, the progress bar is updated with one more unit. If
	 *            workSize is 10, the progress bar is updated with 10 units.
	 * @throws Exception
	 */
	public static void writeZipFile(ZipOutputStream out, File file,
			String prefix, IProgressMonitor monitor, int workSize)
			throws Exception {
		InputStream in = null;
		if (file.isFile() && file.canRead()) {
			try {
				monitor.setTaskName(file.getName());
				if (prefix == null) {
					prefix = "";
				}
				// use "/" should be fine but import doesn't work
				out.putNextEntry(new ZipEntry(
						StringUtils.isEmpty(prefix) ? file.getName() : (prefix
								+ File.separator + file.getName())));
				int read;
				byte[] buf = new byte[FileUtils.BUF_SIZE];

				double workInc = 1.0;
				int base = workSize;
				try {
					int numOfInc = 1;
					double tempIters = file.length() / FileUtils.BUF_SIZE;
					double tempWorkSize = workSize;

					workInc = tempWorkSize / tempIters;

				} catch (Exception e) {
					// Suppress monitor related exceptions.
				}

				in = new BufferedInputStream(new FileInputStream(file),
						FileUtils.BUF_SIZE);
				double workedSoFar = 1.0;
				double workNotCounted = 0;
				int workedSoFarPercentage = 0;
				int currentDisplayPercentage = 0;

				while ((read = in.read(buf)) > 0) {
					if (monitor.isCanceled())
						throw new OperationAbortedException();
					workedSoFar = workedSoFar + workInc;
					workedSoFarPercentage = (int) (workedSoFar * 100 / workSize);
					if (workedSoFarPercentage - currentDisplayPercentage > 1) {
						currentDisplayPercentage = workedSoFarPercentage;
						monitor.setTaskName(file.getName() + "... "
								+ currentDisplayPercentage + "%");
						monitor.worked((int) workNotCounted);
						workNotCounted = 0;
					}
					workNotCounted = workNotCounted + workInc;
					out.write(buf, 0, read);
					out.flush();
				}
			} finally {
				close(in);
				out.closeEntry();
			}
		}
	}

	/**
	 * Extracts the archive to specified folder. Pertains zip folder structure
	 * on target
	 * 
	 * @param archive
	 *            to extract
	 * @param destination
	 *            folder where to extract
	 * @return list of extracted RESOURCES_TO_CLEAN and folders
	 */
	public static List<File> unzip(File archive, File destination)
			throws IOException {
		return unzip(archive, destination, null);
	}

	/**
	 * Extracts the archive to specified folder. Pertains zip folder structure
	 * on target
	 * 
	 * @param archive
	 *            to extract
	 * @param destination
	 *            folder where to extract
	 * @param monitor
	 *            the progress monitor, can be null.
	 * @return list of extracted RESOURCES_TO_CLEAN and folders
	 */
	public static List<File> unzip(File archive, File destination,
			IProgressMonitor monitor) throws IOException {
		List<File> filesToRemove = new ArrayList<File>();
		ZipFile zf = new ZipFile(archive);

		try {
			destination.mkdirs();
			for (Enumeration entries = zf.entries(); entries.hasMoreElements();) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (!entry.isDirectory()) {
					InputStream is = zf.getInputStream(entry);
					File destFile = new File(destination, entry.getName());
					if (destFile.isDirectory()) {
						destFile.mkdirs();
					} else {
						destFile.getParentFile().mkdirs();
						FileUtils.copyFile(is, destFile);
						if (monitor != null) {
							monitor.worked(1);
						}
					}
					filesToRemove.add(destFile);
				}
			}
		} finally {
			zf.close();
		}
		return filesToRemove;
	}

	/**
	 * Converts the given path to the url. If the path starts with
	 * &quot;/&quot;, then this try to find the bundle with name defined in the
	 * first segment. If the bundle is not found, it will fall back to use the
	 * configuration element provided to locate the enclosing bundle.
	 * 
	 * @param element
	 *            the configuration element from where the enclosing bundle is
	 *            determined.
	 * @param path
	 *            the path to the resource, can be either absolute (starts with
	 *            &quot;/&quot;) or relative.
	 * @return the converted url.
	 */
	public static URL getURL(IConfigurationElement element, String path) {
		if (path == null) {
			return null;
		}
		Bundle bundle = null;
		String relativePath = null;
		if (path.startsWith("/")) {
			int index = path.indexOf("/", 1);
			String bundleName = null;
			if (index > 0) {
				bundleName = path.substring(1, index);
				relativePath = path.substring(index + 1);
			} else {
				bundleName = path.substring(1);
				relativePath = "";
			}
			bundle = Platform.getBundle(bundleName);
		} else {
			relativePath = path;
		}
		if (bundle == null && element != null) {
			bundle = Platform.getBundle(element.getNamespaceIdentifier());
		}
		if (bundle == null && element != null) {
			// no installed bundle found, try the projects in the current
			// workspace
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(element.getNamespaceIdentifier());
			if (!project.isAccessible()) {
				return null;
			}
			return toURL(project.getFile(relativePath).getLocation().toFile());
		}
		return getURL(bundle, relativePath);
	}

	/**
	 * Tests if the resource represented by the given path exists in the bundle.
	 * 
	 * @param bundle
	 *            the bundle holding the resource
	 * @param path
	 *            the path to the resource
	 * @return true if the resource exists, false otherwise.
	 */
	public static boolean resourceExists(Bundle bundle, String path) {
		if (bundle == null || path == null) {
			return false;
		}
		return FileLocator.find(bundle, new Path(path), null) != null;
	}

	/**
	 * Converts the path to the proper url that is valid under the given plugin.
	 * 
	 * @param plugin
	 *            the plugin from where the bundle is determined.
	 * @param path
	 *            the path relative to this plugin.
	 */
	public static URL getURL(Plugin plugin, String path) {
		if (plugin == null) {
			return null;
		}
		return getURL(plugin.getBundle(), path);
	}

	/**
	 * Converts the path to the proper url that is valid under the given bundle.
	 * 
	 * @param bundle
	 *            the given bundle.
	 * @param path
	 *            the path relative to this plugin.
	 */
	public static URL getURL(Bundle bundle, String path) {
		if (bundle == null || path == null) {
			return null;
		}
		try {
			URL url = FileLocator.find(bundle, new Path(path), null);
			if (url == null) {
				url = FileLocator.find(bundle, new Path(""), null);
				if (url != null) {
					String base = url.toExternalForm();
					url = new URL(base + "/" + path);
				}
			}
			return url;
		} catch (Exception e) {
			Activator.error(e);
			return null;
		}
	}

	/**
	 * Converts the given url to the file.
	 * 
	 * @param fileUrl
	 *            the file url to be converted.
	 * @return the converted file.
	 */
	public static File getFile(URL fileUrl) {
		if (fileUrl == null) {
			return null;
		}
		try {
			URLConverter converter = org.eclipse.core.internal.runtime.Activator
					.getURLConverter(fileUrl);
			if (converter == null) {
				try {
					return new File(fileUrl.toURI());
				} catch (URISyntaxException e) {
					return new File(fileUrl.getFile());
				}
			}
			return new File(converter.resolve(fileUrl).getFile());
		} catch (Exception e) {
			Activator.error(e);
			String path = fileUrl.toString();
			if (path.startsWith(FILE_PROTOCOL_PREFIX)) {
				path = path.replace(FILE_PROTOCOL_PREFIX,
						FILE_PROTOCOL_PREFIX_REPLACEMENT);
				return new File(path);
			}
			return null;
		}
	}

	/**
	 * Converts the url to the path relative to the bundle root.
	 * 
	 * @param fileUrl
	 *            url to be converted.
	 * @return the path relative to the bundle root.
	 */
	public static String getBundlePath(URL fileUrl) {
		if (fileUrl == null) {
			return null;
		}
		String str = fileUrl.toExternalForm();
		return str.replaceAll(Constants.OSGI_ENTRY_URL_PROTOCOL + "://[^/]*/",
				"");
	}

	/**
	 * Returns folder or file url for the bundle
	 * 
	 * @param bundle
	 * @return
	 */
	public static URL getBundleFileURL(Bundle bundle) {
		URL u = FileLocator.find(bundle, new Path("/"), null);
		try {
			return FileLocator.toFileURL(u);
		} catch (IOException e) {
			Activator.error(e);
		}
		return null;
	}

	/**
	 * Converts the url to the absolute path in the format "/BUNDLE_ID/path".
	 * 
	 * @param url
	 *            url to be converted.
	 * @return the absolute bundle path.
	 */
	public static String getAbsoluteBundlePath(URL url) {
		String host = url.getHost();
		String protocol = url.getProtocol();
		String newUrl = null;
		if (host != null && Constants.OSGI_ENTRY_URL_PROTOCOL.equals(protocol)) {
			try {
				newUrl = FileLocator.toFileURL(url).getPath();
				if (newUrl != null) {
					return newUrl;
				}
			} catch (IOException e) {
				Activator.error(e);
			}
		}
		return getBundlePath(url);
	}

	/**
	 * Finds the eclipse configuration directory. If the configuration directory
	 * is read-only, then return an alternate location (plugin's own state
	 * location) rather than null or throwing an Exception.
	 * 
	 * @param plugin
	 *            the plugin, whose state location is used for failsafe reason.
	 * @return the configuration directory.
	 */
	public static File getConfigDir(Plugin plugin) {
		Location location = Platform.getConfigurationLocation();
		if (location != null) {
			URL configURL = location.getURL();
			return getFile(configURL);
		}
		return plugin.getStateLocation().toFile();
	}

	/**
	 * Loads text content from the given path.
	 * 
	 * @param path
	 *            the path to the file.
	 * @return the text content.
	 * @throws IOException
	 *             if I/O error occurred.
	 */
	public static String loadFully(String path) throws IOException {
		byte[] buf = readBytes(new File(path));
		return new String(buf);
	}

	/**
	 * normalize path delimiters to File.separator and also removes doubled
	 * slashes and replaces them with single File.separator.
	 * 
	 * @param path
	 *            the path to be normalized.
	 * @return the normalized path.
	 */
	public static String normalizePath(String path) {
		try {
			path = path.replace('/', '\\').replace("\\\\", "\\").replace("\\",
					File.separator).trim();
		} catch (Throwable e) {
			Activator.error(e);
		}
		return path;
	}

	/**
	 * validates path from syntactical poitn of view spaces in file name are
	 * allowed when strict == false, or strict == true && alphanumericalOnly ==
	 * false
	 * 
	 * <pre>
	 * Examples of names that passes particular mode:
	 * alphanumerical: 'file-name.ext', '_____.__009' strict: 'my file.my_ext',
	 * 'filewithbrackets{test1}(test2)[test3].my-ext' non strict:
	 * '&tilde;!@#$#%&circ;&amp;()_+ .ext', 'a............zip'
	 * </pre>
	 * 
	 * @param strict
	 *            - when true, allow only more 'normal' file names,
	 *            '~!@#$#%^&()_+' file name does not pass this check, although
	 *            is it valid windows file identifier.
	 * @param alphanumericalOnly
	 *            - works only with strict == true, allows only alphanumerical
	 *            chars + underscope + dash. In this mode only file names like
	 *            'normal-file_1.some_extension' will pass check. Does not allow
	 *            spaces.
	 * 
	 */
	public static boolean isValidFileName(String fName, boolean strict,
			boolean alphanumericalOnly) {
		if (fName == null)
			return false;

		String crazyChars = "+=~!@#$%&§\\^";
		String brackets = "\\(\\)\\{\\}\\[\\]";

		fName = fName.trim();
		return !strict ? Pattern.matches("[a-zA-Z0-9_\\-" + crazyChars
				+ brackets + "]{1,1}[ \\.a-zA-Z0-9_\\-" + crazyChars + brackets
				+ "]*([\\.]{1,1}[a-zA-Z0-9_\\-]+)?", fName)
				: alphanumericalOnly ? Pattern.matches(
						"[a-zA-Z0-9_\\-]+([\\.]{1,1}[a-zA-Z0-9_\\-]+)?", fName)
						: Pattern
								.matches(
										"[a-zA-Z0-9_\\-\\(\\)\\{\\}\\[\\]]{1,1}[ a-zA-Z0-9_\\-\\(\\)\\{\\}\\[\\]]*([\\.]{1,1}[a-zA-Z0-9_\\-]+)?",
										fName);
	}

	/**
	 * Finds all files, incl. directories, from the given directory.
	 * 
	 * @param directory
	 *            the directory to begin lookup.
	 * @return all files inside the given directory.
	 */
	public static List<File> getAllContent(String directory) {
		List<File> files = new ArrayList<File>();
		Stack<File> stack = new Stack<File>();
		stack.push(new File(directory));
		while (!stack.isEmpty()) {
			File[] content = stack.pop().listFiles();
			for (File f : content) {
				files.add(f);
				if (f.isDirectory())
					stack.push(f);
			}
		}
		return files;
	}

	/**
	 * Returns the file name without the extension.
	 * 
	 * @param file
	 *            the file.
	 * @return the basename of the file.
	 */
	public static String getBaseName(IFile file) {
		return getBaseName(file.getName());
	}

	/**
	 * Returns the file name without the extension.
	 * 
	 * @param file
	 *            the file.
	 * @return the basename of the file.
	 */
	public static String getBaseName(File file) {
		return file == null ? null : getBaseName(file.getName());
	}

	/**
	 * Returns the file name without the extension.
	 * 
	 * @param fileName
	 *            name of the file.
	 * @return the basename of the file.
	 */
	public static String getBaseName(String fileName) {
		if (fileName == null) {
			return null;
		}
		if (fileName.indexOf('.') == -1)
			return fileName;
		return fileName.substring(0, fileName.indexOf('.'));
	}

	/**
	 * Gets the file name from the given url.
	 * 
	 * @param url
	 *            the url.
	 * @return the file name in the given url.
	 */
	public static String getFileName(URL url) {
		String file = url.getFile();
		int index = file.lastIndexOf("/");
		if (index >= 0) {
			return file.substring(index + 1);
		}
		return file;
	}

	/**
	 * Tests if the resource represented in the url exists and can be read.
	 * 
	 * @param url
	 *            the url of the resource.
	 * @return true if the resource exists.
	 */
	public static boolean resourceExists(URL url) {
		InputStream in = null;
		try {
			in = url.openStream();
			return true;
		} catch (Exception e) {
		} finally {
			close(in);
		}
		return false;
	}

	/**
	 * Adds file to the cleanup stack.
	 * 
	 * @param file
	 *            the file to be removed.
	 */
	public static void addForCleanup(File file) {
		synchronized (RESOURCES_TO_CLEAN) {
			RESOURCES_TO_CLEAN.add(file);
		}
	}

	/**
	 * Performs cleanup routine.
	 */
	public static void performCleanup() {
		CLEANUP_JOB.run();
	}

	/**
	 * Closes the streams and resources.
	 * 
	 * @param resource
	 *            to be closed.
	 */
	public static void close(Object resource) {
		try {
			if (resource instanceof InputStream) {
				((InputStream) resource).close();
			} else if (resource instanceof OutputStream) {
				((OutputStream) resource).close();
			} else if (resource instanceof Reader) {
				((Reader) resource).close();
			} else if (resource instanceof Writer) {
				((Writer) resource).close();
			} else if (resource instanceof RandomAccessFile) {
				((RandomAccessFile) resource).close();
			} else if (resource != null) {
				throw new IllegalArgumentException("Unknown resource: "
						+ resource);
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Maintains list of temporary RESOURCES_TO_CLEAN, that have to be deleted
	 * upon application exit. Hooks on VM shutdown but should also be called
	 * when the plugin is stopped because the classloaders are not available
	 * when the VM shuts down.
	 * 
	 */
	static class FileGarbageCollector implements Runnable {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			synchronized (RESOURCES_TO_CLEAN) {
				for (Iterator<File> i = RESOURCES_TO_CLEAN.iterator(); i
						.hasNext();) {
					File f = i.next();
					try {
						if (f != null && f.exists()) {
							boolean succ;
							if (f.isDirectory())
								succ = deleteDirectory(f);
							else
								succ = f.delete();
							if (DebugHelper.debugCleanResource()) {
								DebugHelper.debug(this,
										"FileGarbageHook: '"
												+ f
												+ (succ ? "' deleted"
														: "' not deleted"));
							}
							if (succ) {
								i.remove();
							}
						}
					} catch (Exception e) {
						Activator.error(e);
					}
				}
			}
		}
	}

	/**
	 * true if files are the same.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean compareByContent(File a, File b) {
		if (a.equals(b)) {
			return true;
		}
		FileInputStream ia = null;
		FileInputStream ib = null;
		try {
			if (a.length() != b.length())
				return false;
			byte[] buf_a = new byte[32000];
			byte[] buf_b = new byte[32000];

			ia = new FileInputStream(a);
			ib = new FileInputStream(b);

			int readed = 0;
			while ((readed = ia.read(buf_a)) > 0) {
				ib.read(buf_b);
				if (!Arrays.equals(buf_a, buf_b))
					return false;
			}
			return true;
		} catch (Exception e) {
			Activator.error(e);
		} finally {
			close(ia);
			close(ib);
		}
		return false;
	}

	/**
	 * true if input streams have same content.
	 * 
	 * @param ia
	 * @param ib
	 * @return
	 */
	public static boolean compareByContent(InputStream ia, InputStream ib) {
		try {
			byte[] buf_a = new byte[32000];
			byte[] buf_b = new byte[32000];

			int readed = 0;
			while ((readed = ia.read(buf_a)) > 0) {
				ib.read(buf_b);
				if (!Arrays.equals(buf_a, buf_b))
					return false;
			}
			return true;
		} catch (Exception e) {
			Activator.error(e);
		} finally {
			close(ia);
			close(ib);
		}
		return false;

	}

	/**
	 * Recursively listing directory contents
	 * 
	 * @param aStartingDir
	 * @param filter
	 *            FileNameFilter
	 * @return
	 */
	static public List<File> getFiles(File aStartingDir, FilenameFilter filter) {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List filesDirs = Arrays.asList(filesAndDirs);
		Iterator filesIter = filesDirs.iterator();
		File file = null;
		while (filesIter.hasNext()) {
			file = (File) filesIter.next();
			result.add(file); // always add, even if directory
			if (!file.isFile()) {
				List<File> deeperList = getFiles(file, filter);
				result.addAll(deeperList);
			}

		}
		return result;
	}

	static public List<File> getFilesRealWorking(File aStartingDir,
			FilenameFilter filter) {
		List<File> result = new ArrayList<File>();
		File[] filesAndDirs = aStartingDir.listFiles();
		List filesDirs = Arrays.asList(filesAndDirs);
		Iterator filesIter = filesDirs.iterator();
		File file = null;
		while (filesIter.hasNext()) {
			file = (File) filesIter.next();
			if (filter.accept(aStartingDir, file.getName())) {
				result.add(file); // always add, even if directory
			}
			if (!file.isFile()) {
				List<File> deeperList = getFilesRealWorking(file, filter);
				result.addAll(deeperList);
			}

		}
		return result;
	}

	public static File createFileWithExtension(String filePath, String extension) {
		if (filePath == null || extension == null) {
			return new File(filePath);
		}
		extension = "." + extension.toLowerCase();
		if (filePath.toLowerCase().endsWith(extension)) {
			return new File(filePath);
		}
		if (filePath.lastIndexOf('.') != -1
				&& filePath.lastIndexOf(File.separator) < filePath
						.lastIndexOf('.')) {
			filePath = filePath.substring(0, filePath.lastIndexOf('.'));
			return new File(filePath + extension);
		}
		return new File(filePath + extension);
	}

	/**
	 * Used to write string contents to a zip file. Creates a new text file and
	 * populates it with the contents passed. It, then, adds the file to the
	 * output zip stream in the folder as suggested by 'prefix'.
	 * 
	 * @param out
	 *            zipOutputStream to which to add this text file.
	 * @param fileName
	 *            file name of the file to be added.
	 * @param prefix
	 *            folder name (relative path) of the file to be added. if null
	 *            or "", root folder is used.
	 * @param contents
	 *            the string that contains the contents of this new file.
	 * @throws IOException
	 *             if it could not use the zip stream.
	 */
	public static void writeZipFile(ZipOutputStream out, String fileName,
			String prefix, String contents) throws IOException {
		InputStream in = null;

		try {
			if (prefix == null) {
				prefix = "";
			}
			// use "/" should be fine but import doesn't work
			out.putNextEntry(new ZipEntry(
					StringUtils.isEmpty(prefix) ? fileName : (prefix
							+ File.separator + fileName)));

			out.write(contents.getBytes());
			out.flush();

		} finally {

			out.closeEntry();
		}
	}

	/**
	 * Used to write string contents to a zip file. Creates a new text file and
	 * populates it with the contents passed. It, then, adds the file to the
	 * output zip stream in the folder as suggested by 'prefix'.
	 * 
	 * @param out
	 *            zipOutputStream to which to add this text file.
	 * @param fileName
	 *            file name of the file to be added.
	 * @param prefix
	 *            folder name (relative path) of the file to be added. if null
	 *            or "", root folder is used.
	 * @param contents
	 *            the byte[] that contains the contents of this new file.
	 * @throws IOException
	 *             if it could not use the zip stream.
	 */
	public static void writeZipFile(ZipOutputStream out, String fileName,
			String prefix, byte[] contents) throws IOException {
		InputStream in = null;

		try {
			if (prefix == null) {
				prefix = "";
			}
			// use "/" should be fine but import doesn't work
			out.putNextEntry(new ZipEntry(
					StringUtils.isEmpty(prefix) ? fileName : (prefix
							+ File.separator + fileName)));

			out.write(contents);
			out.flush();

		} finally {

			out.closeEntry();
		}
	}

	/**
	 * Delete given resource
	 * 
	 * @param resource
	 *            Resource to delete
	 * @param force
	 *            Force flag
	 * @param monitor
	 *            Progress monitor
	 */
	public static void delete(IResource resource, boolean force,
			IProgressMonitor monitor) {
		try {
			resource.delete(force, monitor);
		} catch (CoreException e) {
			Activator.error("Failed to remove resource "
					+ resource.getProjectRelativePath(), e);
		}
	}

	/**
	 * Loads a set of rules that have been specified in terms of CSS1 grammar.
	 * 
	 * @param path
	 *            absolute path to the css file.
	 * @return style sheet (css1)
	 * @throws IOException
	 *             if file doesn't exist or some problem occurs during reading
	 *             data from file
	 */
	public static StyleSheet loadStyleSheet(String path) throws IOException {
		StyleSheet ss = new StyleSheet();
		FileReader fr = new FileReader(path);
		BufferedReader reader = new BufferedReader(fr);
		ss.loadRules(reader, null);
		reader.close();
		return ss;
	}

	/**
	 * Loads a set of rules that have been specified in terms of CSS1 grammar.
	 * 
	 * @param url
	 *            location to the css file
	 * @return style sheet (css1)
	 * @throws IOException
	 *             if file doesn't exist or some problem occurs during reading
	 *             data from file
	 */
	public static StyleSheet loadStyleSheet(URL url) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(url
				.openStream()));
		StyleSheet ss = new StyleSheet();
		ss.loadRules(reader, null);
		return ss;
	}

}