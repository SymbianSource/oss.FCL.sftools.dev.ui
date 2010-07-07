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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentSourceManager;
import com.nokia.tools.content.core.project.S60DesignProjectNature;
import com.nokia.tools.media.utils.RunnableWithParameter;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.ResourceUtils;
import com.sun.org.apache.xpath.internal.XPathAPI;

public class ImportThemeOperation implements IRunnableWithProgress {
	private IProject target;

	private String source;

	private String importType;

	public static final String ZIP = "zip";

	public static final String TPF = "tpf";

	public static final String DIR = "dir";

	public static final String THEME_EXT = ".tdf"; //$NON-NLS-1$

	public static final String TPF_EXT = ".tpf"; //$NON-NLS-1$

	public static final String ZIP_EXT = ".zip"; //$NON-NLS-1$

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

		IProject project = null;
		if (TPF.equals(importType)) {
			try {
				project = importProjectFromZip(monitor);
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			}
		}

		if (ZIP.equals(importType)) {
			try {
				project = importProjectFromZipFile(monitor);
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			}
		}

		if (DIR.equals(importType)) {
			try {
				project = importProjectFromDirOptimized(monitor);
			} catch (Throwable e) {
				throw new InvocationTargetException(e);
			}
		}
		/**
		 * Changing the code to correct the following: 1. Importing third part
		 * icons during project import from all cases. 2. Ensuring that during
		 * the import of third party icons follow the following rules: a. If
		 * there is only .icons file present and not icons.xml file present
		 * [theme exported from old tool and hence the icons should go in as
		 * theme specific icons] b. If both .icons file and icons.xml file
		 * exist, then we import contents from .icons file as tool specific
		 * icons and ones present in icons.xml as theme specific ones. c. If
		 * only icons.xml file is present, then it is the export from the new
		 * tool and hence will be imported as theme specific icons.
		 */

		if (project != null) {
			IFile toolSpecificFile = project
					.getFile(TOOL_SPECIFIC_TPI_FILENAME);
			IFile themeSpecificFile = null;

			/**
			 * Searching for the folder for the theme under the project. We
			 * check for the folder that contains the tpf for the theme as this
			 * the correct theme folder then and then in it we check if
			 * icons.xml file is present or not.
			 */
			try {

				if (!project.isOpen()) {
					project.open(monitor);
				}

				IResource[] members = project.members();

				for (IResource member : members) {
					if (member instanceof IFolder) {
						IResource[] prospectiveThemeFolderMembers = ((IFolder) member)
								.members();

						for (IResource prospectiveThemeFolderMember : prospectiveThemeFolderMembers) {
							if (prospectiveThemeFolderMember.getName()
									.endsWith(THEME_EXT)) {
								themeSpecificFile = ((IFolder) member)
										.getFile(THEME_SPECIFIC_TPI_FILENAME);
								break;
							}
						}
						if (themeSpecificFile != null) {
							break;
						}
					}
				}
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			}

			if (themeSpecificFile == null) {
				themeSpecificFile = project.getFile(project.getName()
						+ File.separator + THEME_SPECIFIC_TPI_FILENAME);
			}

			if (toolSpecificFile.exists() && !themeSpecificFile.exists()) {
				// This is the case 2.a mentioned above and so we load them as
				// theme specific icons.
				// A simple way of doing this is to rename the file as icons.xml
				// and later on the model load for the theme will
				// take care of loading the contents as theme specific third
				// party icons.

				try {
					toolSpecificFile.copy(themeSpecificFile
							.getProjectRelativePath(), true,
							new NullProgressMonitor());
					toolSpecificFile.delete(true, true,
							new NullProgressMonitor());
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			} else if (toolSpecificFile.exists() && themeSpecificFile.exists()) {
				// This is case 2.b mentioned above and hence we just import the
				// tool specific third party icon here
				// and the model merge during Theme object creation will take
				// care of importing the theme specific third party icons.
				final IFile file = toolSpecificFile;
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						ImportThirdPartyIconDefenitionHelper
								.importThirdpartyIconDefenition(file);
					}
				});
			}

			// In this case of 2.c, we do not have to do anything here as it
			// will be loaded as theme specific third party
			// icons during the merge of the model for the theme.

			openProject(project, monitor);

			// After the import has been complete, there is a check done to see
			// if there are con
		}

	}

	private void openProject(IProject project, IProgressMonitor monitor) {
		try {
			if (!project.isOpen()) {
				project.open(monitor);
			}
			IContent content = null;
			List<IContent> contents = new ArrayList<IContent>();
			IContentSourceManager manager = S60DesignProjectNature
					.getUIDesignData(project);
			List<IContent> cs = null;
			for (String type : AbstractContentSourceManager.getContentTypes()) {
				cs = manager.getRootContents(type, monitor);
				for (IContent content2 : cs) {
					if (content2.hasChildren()) {
						contents.add(content2);
					}
				}
			}

			if (!contents.isEmpty()) {
				content = contents.get(0);
				IPath path = (IPath) content.getAdapter(IPath.class);
				final IFile file = ResourceUtils
						.getProjectResourceByAbsolutePath(project, path);
				try {
					IDE.openEditor(PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage(), file,
							true);
				} catch (Throwable t) {
					PlatformUI.getWorkbench().getDisplay().syncExec(
							new Runnable() {
								public void run() {
									try {
										IDE.openEditor(PlatformUI
												.getWorkbench()
												.getActiveWorkbenchWindow()
												.getActivePage(), file, true);
									} catch (PartInitException e) {

										e.printStackTrace();
									}
								}
							});
				}
			} else {
				MessageDialog.openError(null,
						WizardMessages.ImportWizard_errMsgTitle,
						WizardMessages.ImpWizPg1_invalidproject);
			}
		} catch (IOException e) {

			e.printStackTrace();
		} catch (ContentException e) {

			e.printStackTrace();
		} catch (CoreException e) {

			e.printStackTrace();
			ErrorDialog.openError(null,
					WizardMessages.ImportWizard_errMsgTitle, e.getMessage(),
					((CoreException) e).getStatus());
		}
	}

	private IProject importProjectFromDir(IProgressMonitor monitor)
			throws Throwable {

		String originalProjectName = getProjectName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				originalProjectName);

		int pos = 2;
		boolean filterProjectMetadata = false;
		while (project.exists()) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					originalProjectName + "_" + pos++);
			filterProjectMetadata = true;
		}

		final List<File> fileSystemObjects = new ArrayList<File>();

		new File(source).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				fileSystemObjects.add(pathname);
				if (pathname.isDirectory()) {
					pathname.listFiles(this);
				}
				return true;
			}
		});

		ImportOperation operation = new ImportOperation(project.getFullPath(),
				new File(source), FileSystemStructureProvider.INSTANCE, null,
				fileSystemObjects);

		operation.setCreateContainerStructure(false);
		/*
		 * operation.setContext(PlatformUI.getWorkbench()
		 * .getActiveWorkbenchWindow().getShell());
		 */

		operation.run(monitor);

		/* update '.project' to new name */
		if (filterProjectMetadata) {
			try {
				String path = project.findMember(
						IProjectDescription.DESCRIPTION_FILE_NAME)
						.getLocation().toFile().getAbsolutePath();
				String content = FileUtils.loadFully(path).replace(
						"<name>" + originalProjectName + "</name>",
						"<name>" + project.getName() + "</name>");
				FileOutputStream fos = new FileOutputStream(path);
				fos.write(content.getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return project;
	}

	private IProject importProjectFromDirOptimized(IProgressMonitor monitor)
			throws Throwable {

		String originalProjectName = getProjectName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				originalProjectName);

		int pos = 2;
		boolean filterProjectMetadata = false;
		while (project.exists()) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					originalProjectName + "_" + pos++);
			filterProjectMetadata = true;
		}

		final List<File> files = new ArrayList<File>();
		new File(source).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				files.add(pathname);
				if (pathname.isDirectory()) {
					pathname.listFiles(this);
				}
				return true;
			}
		});

		ImportProjectFromDirectoryOperation operation1 = new ImportProjectFromDirectoryOperation(
				source, project, files.size());
		operation1.run(monitor);

		final List<File> fileSystemObjects = new ArrayList<File>();
		fileSystemObjects.add(new File(source));
		ImportOperation operation2 = new ImportOperation(project.getFullPath(),
				new File(source), FileSystemStructureProvider.INSTANCE, null,
				fileSystemObjects);
		operation2.setOverwriteResources(false);
		operation2.setCreateContainerStructure(false);
		operation2.run(monitor);

		/* update '.project' to new name */
		if (filterProjectMetadata) {
			try {
				String path = project.findMember(
						IProjectDescription.DESCRIPTION_FILE_NAME)
						.getLocation().toFile().getAbsolutePath();
				String content = FileUtils.loadFully(path).replace(
						"<name>" + originalProjectName + "</name>",
						"<name>" + project.getName() + "</name>");
				FileOutputStream fos = new FileOutputStream(path);
				fos.write(content.getBytes());
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return project;
	}

	private IProject importProjectFromZipFile(IProgressMonitor monitor)
			throws Throwable {
		ZipFile zipFile = new ZipFile(source);
		IProject project = null;

		final ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(
				zipFile);

		RunnableWithParameter zipContentExplorer = new RunnableWithParameter() {
			ZipEntry context = null;
			ZipEntry root = null;

			public void run() {
				if (context == null) {
					context = structureProvider.getRoot();
				}
				List children = structureProvider.getChildren(context);
				if (children != null) {
					ZipEntry ctx = context;
					for (Iterator entries = children.iterator(); entries
							.hasNext();) {
						ZipEntry ze = (ZipEntry) entries.next();
						if (root == null
								&& !ze.isDirectory()
								&& new Path(ze.getName())
										.lastSegment()
										.toLowerCase()
										.equals(
												IProjectDescription.DESCRIPTION_FILE_NAME)) {
							root = ctx;
							return;
						}

						context = ze;
						run();
					}
				}
			}

			public Object getParameter() {
				return root;
			}

			public void setParameter(Object data) {
			}
		};

		zipContentExplorer.run();
		ZipEntry root = (ZipEntry) zipContentExplorer.getParameter();

		if (structureProvider.getRoot() == root) {
			// If .project is directly under root node
			// use optimized version of import from zip
			return importProjectFromZipOptimized(monitor);
		} else {
			return importProjectFromZip(monitor);
		}
	}

	private IProject importProjectFromZip(IProgressMonitor monitor)
			throws Throwable {
		ZipFile zipFile = new ZipFile(source);
		IProject project = null;
		try {
			final ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(
					zipFile);
			String originalProjectName = getProjectName();
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					originalProjectName);

			int pos = 2;
			boolean filterProjectMetadata = false;
			while (project.exists()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(
						originalProjectName + "_" + pos++);
				filterProjectMetadata = true;
			}

			final List<ZipEntry> fileSystemObjects = new ArrayList<ZipEntry>();
			RunnableWithParameter zipContentExplorer = new RunnableWithParameter() {
				ZipEntry context = null;
				ZipEntry root = null;

				public void run() {
					if (context == null) {
						context = structureProvider.getRoot();
					}
					List children = structureProvider.getChildren(context);
					if (children != null) {
						ZipEntry ctx = context;
						for (Iterator entries = children.iterator(); entries
								.hasNext();) {
							ZipEntry ze = (ZipEntry) entries.next();
							fileSystemObjects.add(ze);
							if (root == null
									&& !ze.isDirectory()
									&& new Path(ze.getName())
											.lastSegment()
											.toLowerCase()
											.equals(
													IProjectDescription.DESCRIPTION_FILE_NAME)) {
								root = ctx;
							}

							context = ze;
							run();
						}
					}
				}

				public Object getParameter() {
					return root;
				}

				public void setParameter(Object data) {
				}
			};

			zipContentExplorer.run();

			ZipEntry root = (ZipEntry) zipContentExplorer.getParameter();

			if (root == null) {
				if (TPF.equals(importType)) {
					// for backward compatibility only
					// (to import old tpf files)
					return importProjectFromTpf(monitor);
				}
				root = structureProvider.getRoot();
			}

			ImportOperation operation = new ImportOperation(project
					.getFullPath(), root, structureProvider, null,
					fileSystemObjects);
			operation.setCreateContainerStructure(false);

			operation.run(monitor);

			/* update '.project' to new name */
			if (filterProjectMetadata) {
				try {
					String path = project.findMember(
							IProjectDescription.DESCRIPTION_FILE_NAME)
							.getLocation().toFile().getAbsolutePath();
					String content = FileUtils.loadFully(path).replace(
							"<name>" + originalProjectName + "</name>",
							"<name>" + project.getName() + "</name>");
					FileOutputStream fos = new FileOutputStream(path);
					fos.write(content.getBytes());
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} finally {
			zipFile.close();
		}
		return project;
	}

	private IProject importProjectFromZipOptimized(IProgressMonitor monitor)
			throws Throwable {
		IProject project = null;
		try {
			String originalProjectName = getProjectName();
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					originalProjectName);

			int pos = 2;
			boolean filterProjectMetadata = false;
			while (project.exists()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(
						originalProjectName + "_" + pos++);
				filterProjectMetadata = true;
			}

			ImportProjectFromZipOperation operation1 = new ImportProjectFromZipOperation(
					source, project);
			operation1.run(monitor);

			/* update '.project' to new name */
			if (filterProjectMetadata) {
				try {
					String path = project.findMember(
							IProjectDescription.DESCRIPTION_FILE_NAME)
							.getLocation().toFile().getAbsolutePath();
					String content = FileUtils.loadFully(path).replace(
							"<name>" + originalProjectName + "</name>",
							"<name>" + project.getName() + "</name>");
					FileOutputStream fos = new FileOutputStream(path);
					fos.write(content.getBytes());
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} finally {
		}
		return project;
	}

	protected IProject importProjectFromTpf(IProgressMonitor monitor)
			throws Throwable {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Importing...", 100); //$NON-NLS-1$

		String originalProjectName = getProjectName();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				originalProjectName);
		int pos = 2;
		while (project.exists()) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(
					originalProjectName + "_" + pos++);
		}
		if (!project.getName().equals(getProjectName())) {
			// project name is taken from template = tpf file name. copy to temp
			// dir to mirror name change
			String newName = FileUtils.getTemporaryDirectory() + File.separator
					+ project.getName() + TPF_EXT;
			FileUtils.copyFile(new File(source), new File(newName));
			source = newName;
		}

		String projectFolder = new Path(ResourcesPlugin.getWorkspace()
				.getRoot().getLocation().toFile().getAbsolutePath()).append(
				project.getName()).toString();

		// should read from the .tdf file
		String release = "BaseGraphics";
		CreateNewThemeOperation themeCreationOp = new CreateNewThemeOperation(
				project, project.getName(), projectFolder, release, null); //$NON-NLS-1$

		themeCreationOp.setTemplate(source);

		try {
			createTargetProject(new SubProgressMonitor(monitor, 10));

			themeCreationOp.run(new SubProgressMonitor(monitor, 40));

			project.refreshLocal(IResource.DEPTH_INFINITE,
					new SubProgressMonitor(monitor, 10));

			return project;

		} catch (InterruptedException e) {
			return null;
		}
	}

	private void createTargetProject(IProgressMonitor monitor)
			throws CoreException {
		if (getTarget() == null) {
			monitor.beginTask("Creating project", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(getProjectName());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			final IProjectDescription description = workspace
					.newProjectDescription(project.getName());

			description.setLocation(null);

			project.create(description, new SubProgressMonitor(monitor, 300));

			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(
					monitor, 300));

			S60DesignProjectNature.addNatureToProject(project,
					S60DesignProjectNature.NATURE_ID);

			project.refreshLocal(IProject.DEPTH_INFINITE, monitor);

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			monitor.done();
		}
	}

	public String getProjectName() {
		if (target != null) {
			return target.getName();
		}
		if (source != null && ZIP.equals(importType)) {

			if (TPF.equals(new Path(source).getFileExtension())) {
				return new Path(source).removeFileExtension().lastSegment();
			} else {

				if (isValidZippedProjectFile(source)) {
					try {
						ZipFile zf = new ZipFile(source);
						try {
							ZipEntry entry = null;

							for (Enumeration entries = zf.entries(); entries
									.hasMoreElements();) {
								entry = (ZipEntry) entries.nextElement();

								if (!entry.isDirectory()
										&& new Path(entry.getName())
												.lastSegment()
												.toLowerCase()
												.equals(
														IProjectDescription.DESCRIPTION_FILE_NAME)) {
									break;
								} else {
									entry = null;
								}
							}

							if (entry != null) {
								InputStream is = zf.getInputStream(entry);
								try {
									return parseProjectNameFromDotProjectFile(is);
								} finally {
									try {
										is.close();
									} catch (IOException e) {
									}
								}
							} else {
								return null;
							}
						} finally {
							zf.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}
			}
		}

		if (source != null && DIR.equals(importType)) {
			if (isValidDotProjectDirectory(source)) {
				try {
					InputStream is = new FileInputStream(new File(source,
							IProjectDescription.DESCRIPTION_FILE_NAME));
					try {
						return parseProjectNameFromDotProjectFile(is);
					} finally {
						try {
							is.close();
						} catch (IOException e) {
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}

		return null;
	}

	public static boolean isValidTpfArchiveFile(String source) {
		if (source != null && new File(source).exists()) {
			try {
				ZipFile zf = new ZipFile(source);
				try {
					for (Enumeration entries = zf.entries(); entries
							.hasMoreElements();) {
						ZipEntry entry = (ZipEntry) entries.nextElement();

						if (!entry.isDirectory()
								&& new Path(entry.getName()).getFileExtension()
										.toLowerCase().equals(
												THEME_EXT.substring(1))) {
							return true;
						}
					}
				} finally {
					zf.close();
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean isValidZippedProjectFile(String source) {
		boolean isValid = false;
		if (source != null && new File(source).exists()) {

			boolean projectFileExists = false;
			boolean themeFileExists = false;
			try {
				ZipFile zf = new ZipFile(source);
				try {
					for (Enumeration entries = zf.entries(); entries
							.hasMoreElements();) {
						ZipEntry entry = (ZipEntry) entries.nextElement();

						if (!entry.isDirectory()) {
							if (new Path(entry.getName())
									.lastSegment()
									.toLowerCase()
									.equals(
											IProjectDescription.DESCRIPTION_FILE_NAME)) {
								projectFileExists = true;
							}
							if (new Path(entry.getName()).lastSegment()
									.toLowerCase().endsWith(THEME_EXT)) {
								themeFileExists = true;
							}
						}

					}
				} finally {
					zf.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			isValid = projectFileExists == themeFileExists;
		}
		return isValid;
	}

	public static boolean isValidDotProjectDirectory(String source) {
		if (source != null) {
			File srcFile = new File(source);
			if (srcFile.exists() && srcFile.isDirectory()) {
				String[] projectDescription = srcFile
						.list(new FilenameFilter() {
							public boolean accept(File dir, String name) {
								return (IProjectDescription.DESCRIPTION_FILE_NAME
										.equals(name));
							}
						});
				return projectDescription.length == 1;
			}
		}
		return false;
	}

	private String parseProjectNameFromDotProjectFile(InputStream is)
			throws SAXException, IOException, ParserConfigurationException,
			TransformerException {
		Document xml = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().parse(is);
		Node nameNode = XPathAPI.selectSingleNode(xml,
				"/projectDescription/name/text()"); //$NON-NLS-1$
		if (nameNode != null) {
			return nameNode.getNodeValue();
		} else {
			return null;
		}
	}

	public IProject getTarget() {
		return target;
	}

	public void setTarget(IProject target) {
		this.target = target;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}

	public static boolean isValidS60DotProjectDirectory(String source,
			String importType) {
		if (source != null) {
			boolean validDir = isValidDotProjectDirectory(source);
			if (validDir) {
				File srcFile = new File(source);
				if (srcFile.exists() && srcFile.isDirectory()) {
					List<File> files = FileUtils.getFiles(new File(source),
							new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return (IProjectDescription.DESCRIPTION_FILE_NAME
											.equals(name) || name
											.endsWith(THEME_EXT));
								}
							});
					return files.size() >= 2;
				}
			}
		}
		return false;
	}

}