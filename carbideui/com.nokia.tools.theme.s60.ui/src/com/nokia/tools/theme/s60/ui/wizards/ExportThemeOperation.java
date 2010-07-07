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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.XmlUtil;
import com.nokia.tools.theme.s60.model.tpi.ThirdPartyIconManager;
import com.sun.org.apache.xpath.internal.XPathAPI;

public class ExportThemeOperation implements IRunnableWithProgress {

	private String target;

	private IProject source;

	private String tempDirectory;

	private String exportType;

	private String tName;

	public static final String ZIP = "zip";

	public static final String TPF = "tpf";

	public static final String DIR = "dir";

	public static final String THEME_EXT = ".tdf";

	public static final String TPF_EXT = ".tpf";

	public static final String ZIP_EXT = ".zip";

	public static final String TEMPORARY_FOLDER = "tmp";

	static final String PREF_LAST_EXPORT_LOCATION = "exportWizard.lastLoc";

	/**
	 * Constant for the file name in the project that will contain the tool
	 * specific third party icons.
	 */
	public static final String TOOL_SPECIFIC_TPI_FILENAME = ".icons";

	/**
	 * Constant for the file name in the project that will contain the theme
	 * specific third party icons.
	 */
	public static final String THEME_SPECIFIC_TPI_FILENAME = "icons.xml";

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {

		if(monitor instanceof SubProgressMonitor){
			monitor = ((SubProgressMonitor)monitor).getWrappedProgressMonitor();
		}
		if(monitor != null){
			monitor.beginTask("Exporting "+tName+"...", IProgressMonitor.UNKNOWN);
		}
		try {

			tempDirectory = FileUtils.getTemporaryDirectory() + File.separator
					+ "tmpExport";
			File tmpDirFile = new File(tempDirectory);
			while (tmpDirFile.exists()) {
				tempDirectory = FileUtils.getTemporaryDirectory()
						+ File.separator + "tmpExport" + new Random().nextInt();
				tmpDirFile = new File(tempDirectory);
			}

			tmpDirFile = new File(tempDirectory);
			// create it
			tmpDirFile.mkdir();

			if (TPF.equals(exportType)) {
				exportType = ZIP;
			}

			if (TPF.equals(exportType)) {
				// find directory that contains TDF file
				IContainer tdfContainer = findTdf(source);
				processDir(tempDirectory, tdfContainer);
			} else {
				// step two - resursively process all resources in project
				processDir(tempDirectory, source);
			}
			if (monitor != null)
				monitor.worked(1);

			// adjust .project - remove linked resources
			removeLinkedResources(tempDirectory);
			// adjust .project - update theme name
			updateThemeName(tempDirectory, tName);

			URL iconUrl = ThirdPartyIconManager
					.getToolSpecificThirdPartyIconUrl();
			if (iconUrl != null) {
				try {
					InputStream in = iconUrl.openStream();
					FileUtils.copyFile(in, new File(tempDirectory
							+ File.separator + TOOL_SPECIFIC_TPI_FILENAME));
					in.close();
				} catch (Exception e) {

				}
			}
			if (monitor != null)
				monitor.worked(1);
			if (!DIR.equals(exportType)) {
				// pack this to target dir
				packDirectoryToFile(tempDirectory, target, source.getName());
			} else {
				// copy temp dir to target
				/*FileUtils.copyDir(new File(tempDirectory), new File(target
						+ File.separator + source.getName()));*/
				FileUtils.copyDirNIO(new File(tempDirectory), new File(target
						+ File.separator + source.getName()));
			}

			if (monitor != null)
				monitor.worked(1);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileUtils.deleteDirectory(new File(tempDirectory));
			if (monitor != null)
				monitor.worked(1);
		}

		try {
			// remember target dir
			IPreferenceStore iPreferenceStore = UtilsPlugin.getDefault()
					.getPreferenceStore();
			if (!new File(target).isDirectory())
				target = new File(target).getParent();
			iPreferenceStore.setValue(PREF_LAST_EXPORT_LOCATION, target);

		} catch (Exception e) {
		}
	}

	private void updateThemeName(String dir, String name) throws Exception {
		String oldName = null;
		if (new File(dir + File.separator + ".project").exists()) {
			Document xml = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(
							new File(dir + File.separator + ".project"));
			Element nameNode = (Element) XPathAPI.selectSingleNode(xml,
					"projectDescription/name");
			oldName = nameNode.getTextContent();
			nameNode.setTextContent(name);
			XmlUtil.write(xml, new File(dir + File.separator + ".project"),
					null);
		}

		/*
		 * change the name of themename.tdf and directory name 'themename' if
		 * found
		 */
		List<File> allFiles = FileUtils.getAllContent(dir);
		File renameDir = null, targetRenameDir = null, tdf = null;
		for (File f : allFiles) {
			if (f.getName().equalsIgnoreCase(oldName + ".tdf")) {
				// rename
				File renamed = new File(f.getParent() + File.separator + name
						+ ".tdf");
				f.renameTo(renamed);
				tdf = renamed;
			}
			if (f.isDirectory() && f.getName().equalsIgnoreCase(oldName)) {
				// rename to new name
				File renamed = new File(f.getParent() + File.separator + name);
				// need to rename later
				renameDir = f;
				targetRenameDir = renamed;
			}
		}
		/* update theme name in .tdf */
		if (tdf != null && tdf.exists()) {
			String theme = FileUtils.loadFully(tdf.getAbsolutePath());
			theme = theme.replace("name1=\"" + oldName + "\"", "name1=\""
					+ name + "\"");
			FileOutputStream fos = new FileOutputStream(tdf);
			fos.write(theme.getBytes());
			fos.close();
		}

		// last step - update dir name
		if (renameDir != null && targetRenameDir != null)
			renameDir.renameTo(targetRenameDir);
	}

	private IContainer findTdf(IContainer cont) throws CoreException {
		for (IResource r : cont.members(true)) {
			if (r.getName().toLowerCase().endsWith(THEME_EXT))
				return cont;
			if (r instanceof IContainer) {
				IContainer cc = findTdf((IContainer) r);
				if (cc != null)
					return cc;
			}
		}
		return null;
	}

	private void packDirectoryToFile(String sourceDir, String targetFile,
			String projectName) throws IOException {
		FileOutputStream fos = new FileOutputStream(targetFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		Stack<File> files = new Stack<File>();
		files.push(new File(sourceDir));
		while (!files.empty()) {
			File f = files.pop();
			if (f.isDirectory()) {
				files.addAll(Arrays.asList(f.listFiles()));
			} else {
				String name = f.getCanonicalPath().substring(
						new File(sourceDir).getCanonicalPath().length());
				ZipEntry entry = new ZipEntry(name);
				zos.putNextEntry(entry);
				try {
					FileInputStream fis = new FileInputStream(f);
					byte buf[] = new byte[32768];
					int readed = 0;
					while ((readed = fis.read(buf)) > 0) {
						zos.write(buf, 0, readed);
					}
					fis.close();
					zos.closeEntry();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		zos.finish();
		zos.close();

		fos.close();
	}

	private void removeLinkedResources(String dir) throws Exception {
		if (new File(dir + File.separator + ".project").exists()) {
			Document xml = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(
							new File(dir + File.separator + ".project"));
			Element linked = (Element) XPathAPI.selectSingleNode(xml,
					"*//linkedResources");
			if (linked != null)
				linked.getParentNode().removeChild(linked);
			XmlUtil.write(xml, new File(dir + File.separator + ".project"),
					null);
		}
	}

	private void processDir(String tempDir, IContainer container)
			throws CoreException, IOException {
		for (IResource resource : container.members(true)) {
			if (resource instanceof IContainer
					&& TEMPORARY_FOLDER.equals(resource.getName())) {
				// ommit temporary folder(s)
				continue;
			}
			if (resource instanceof IContainer && !TPF.equals(exportType)) {
				String destPath = tempDir + File.separator + resource.getName();
				new File(destPath).mkdir();
				processDir(destPath, (IContainer) resource);
			} else {
				if (resource instanceof IContainer && TPF.equals(exportType)) {
					// don't export sub-directories to TPF
					continue;
				}
				if (resource.isLinked()) {
					String destPath = tempDir + File.separator
							+ resource.getName();
					String sourcePath = resource.getLocation().makeAbsolute()
							.toOSString();
					// FileUtils.copyFile(sourcePath, destPath);
					FileUtils.copyUsingNIO(sourcePath, destPath);
					if (resource.getName().toLowerCase().endsWith(THEME_EXT)) {
						// linked resource was theme - copy all files
						copyThemesContent(tempDir, resource);
					}
				} else {
					String destPath = tempDir + File.separator
							+ resource.getName();
					String sourcePath = resource.getLocation().makeAbsolute()
							.toOSString();

					if (TPF.equals(exportType)) {
						if (resource.getName().equals(".project")
								|| resource.getName().equals(".classpath"))
							continue;
					}
					try {
						// FileUtils.copyFile(sourcePath, destPath);
						FileUtils.copyUsingNIO(sourcePath, destPath);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void copyThemesContent(String dest, IResource theme)
			throws IOException {
		File themeFile = new File(theme.getLocation().makeAbsolute()
				.toOSString());
		FileInputStream fis = new FileInputStream(themeFile);
		byte buf[] = new byte[fis.available()];
		fis.read(buf);
		String tdf = new String(buf);
		Pattern p = Pattern.compile("filename=\"([^\"]+)\"");
		Matcher m = p.matcher(tdf);
		while (m.find()) {
			String fName = m.group(1);
			// copy
			if (new File(themeFile.getParent() + File.separator + fName)
					.exists())
				/*
				 * FileUtils.copyFile(themeFile.getParent() + File.separator +
				 * fName, dest + File.separator + fName);
				 */
				FileUtils.copyUsingNIO(themeFile.getParent() + File.separator
						+ fName, dest + File.separator + fName);
		}
		p = Pattern.compile("filename='([^\']+)'");
		m = p.matcher(tdf);
		while (m.find()) {
			String fName = m.group(1);
			// copy
			if (new File(themeFile.getParent() + File.separator + fName)
					.exists())
				/*
				 * FileUtils.copyFile(themeFile.getParent() + File.separator +
				 * fName, dest + File.separator + fName);
				 */
				FileUtils.copyUsingNIO(themeFile.getParent() + File.separator
						+ fName, dest + File.separator + fName);
		}
	}

	public IProject getSource() {
		return source;
	}

	public void setSource(IProject source) {
		this.source = source;
		if (tName == null && source != null)
			setTargetThemeName(source.getName());
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTempDirectory() {
		return tempDirectory;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}

	public void setTargetThemeName(String targetThemeName) {
		this.tName = targetThemeName;
	}

}
