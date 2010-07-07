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
package com.nokia.tools.s60.editor.commands;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;

import com.nokia.tools.media.utils.layers.IImageHolder;
import com.nokia.tools.resource.util.FileUtils;

public class UndoableImageHolderActionCommand extends Command {

	IImageHolder holder;

	Runnable action;

	File oldImageFile, oldMaskFile, backupOldImageFile, backupOldMaskFile;

	File newImageFile, newMaskFile, backupNewImageFile, backupNewMaskFile;

	long oldImageFileLastModified, oldMaskFileLastModified;

	public UndoableImageHolderActionCommand(IImageHolder holder, Runnable action) {
		setLabel(Messages.SetImage_Label);
		this.holder = holder;
		this.action = action;
	}

	public void execute() {
		try {
			oldImageFile = holder.getImageFile();
			oldMaskFile = holder.getMaskFile();

			backupOldFiles();

			action.run();

			backupNewFiles();

			deleteOldFiles();
			restoreNewFiles();

			holder.refresh();
			refreshEclipseResources();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	public void redo() {
		try {
			deleteOldFiles();
			restoreNewFiles();

			holder.setImageFile(newImageFile);
			holder.setMaskFile(newMaskFile);

			holder.refresh();
			refreshEclipseResources();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	public void undo() {
		try {
			deleteNewFiles();
			restoreOldFiles();

			holder.setImageFile(oldImageFile);
			holder.setMaskFile(oldMaskFile);

			holder.refresh();
			refreshEclipseResources();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	public boolean canExecute() {
		return holder != null;
	};

	public boolean canUndo() {
		return holder != null;
	};

	private void deleteOldFiles() {
		if (oldImageFile != null && backupNewImageFile != null) {
			oldImageFile.delete();
		}
		if (oldMaskFile != null && backupNewMaskFile != null) {
			oldMaskFile.delete();
		}
	}

	private void deleteNewFiles() {
		if (newImageFile != null && backupOldImageFile != null) {
			newImageFile.delete();
		}
		if (newMaskFile != null && backupOldMaskFile != null) {
			newMaskFile.delete();
		}
	}

	private void backupOldFiles() throws IOException {
		if (oldImageFile != null && oldImageFile.exists()) {
			oldImageFileLastModified = oldImageFile.lastModified();
			backupOldImageFile = File.createTempFile(oldImageFile.getName(),
					".tmp");
			FileUtils.copyFile(oldImageFile, backupOldImageFile);
			backupOldImageFile.deleteOnExit();
		}

		if (oldMaskFile != null && oldMaskFile.exists()) {
			oldMaskFileLastModified = oldMaskFile.lastModified();
			backupOldMaskFile = File.createTempFile(oldMaskFile.getName(),
					".tmp");
			backupOldMaskFile.deleteOnExit();
			FileUtils.copyFile(oldMaskFile, backupOldMaskFile);
		}
	}

	private void backupNewFiles() throws IOException {
		newImageFile = holder.getImageFile();
		if (newImageFile != null && newImageFile.exists()) {
			if (newImageFile.equals(oldImageFile)
					&& newImageFile.lastModified() > oldImageFileLastModified) {
				backupNewImageFile = File.createTempFile(
						newImageFile.getName(), ".tmp");
				FileUtils.copyFile(newImageFile, backupNewImageFile);
				backupNewImageFile.deleteOnExit();
			} else {
				if (backupOldImageFile != null) {
					backupOldImageFile.delete();
					backupOldImageFile = null;
				}
			}
		} else {
			if (backupOldImageFile != null) {
				backupOldImageFile.delete();
				backupOldImageFile = null;
			}
		}

		newMaskFile = holder.getMaskFile();
		if (newMaskFile != null && newMaskFile.exists()) {
			if (newMaskFile.equals(oldMaskFile)
					&& newMaskFile.lastModified() > oldMaskFileLastModified) {
				backupNewMaskFile = File.createTempFile(newMaskFile.getName(),
						".tmp");
				FileUtils.copyFile(newMaskFile, backupNewMaskFile);
				backupNewMaskFile.deleteOnExit();
			} else {
				if (backupOldMaskFile != null) {
					backupOldMaskFile.delete();
					backupOldMaskFile = null;
				}
			}
		} else {
			if (backupOldMaskFile != null) {
				backupOldMaskFile.delete();
				backupOldMaskFile = null;
			}
		}
	}

	private void restoreOldFiles() throws IOException {
		if (backupOldImageFile != null) {
			FileUtils.copyFile(backupOldImageFile, oldImageFile);
		}
		if (backupOldMaskFile != null) {
			FileUtils.copyFile(backupOldMaskFile, oldMaskFile);
		}
	}

	private void restoreNewFiles() throws IOException {
		if (backupNewImageFile != null) {
			FileUtils.copyFile(backupNewImageFile, newImageFile);
		}
		if (backupNewMaskFile != null) {
			FileUtils.copyFile(backupNewMaskFile, newMaskFile);
		}
	}

	private void refreshEclipseResources() {
		// refresh eclipse resources
		IPath destPath = new Path(holder.getWorkDir().toString());
		IContainer[] cont = ResourcesPlugin.getWorkspace().getRoot()
				.findContainersForLocation(destPath);
		if (cont.length > 0) {
			try {
				cont[0].refreshLocal(IResource.DEPTH_ONE,
						new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public IImageHolder getImageHolder() {
		return holder;
	}
}
