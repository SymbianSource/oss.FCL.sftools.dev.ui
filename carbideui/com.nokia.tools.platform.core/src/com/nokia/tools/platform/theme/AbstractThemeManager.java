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

package com.nokia.tools.platform.theme;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nokia.tools.platform.core.IDevice;
import com.nokia.tools.platform.extension.IThemeDescriptor;
import com.nokia.tools.platform.extension.IThemeDesignDescriptor;
import com.nokia.tools.platform.extension.IThemeModelDescriptor;
import com.nokia.tools.platform.theme.preview.ThemePreviewParser;
import com.nokia.tools.resource.util.DebugHelper;

public abstract class AbstractThemeManager
    implements IThemeManager {

	private Map<String, Theme> models = new HashMap<String, Theme>();

	private String containerId;

	private boolean isCreatingModel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#createTheme(java.lang.String)
	 */
	public Theme createTheme(String modelId, IProgressMonitor monitor)
	    throws ThemeException {
		Theme model = getModel(modelId, monitor);
		return (Theme) model.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#getContainerId()
	 */
	public String getContainerId() {
		return containerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#getModel(java.lang.String)
	 */
	public synchronized Theme getModel(String modelId, IProgressMonitor monitor)
	    throws ThemeException {
		if (modelId == null) {
			return null;
		}

		modelId = modelId.trim().toLowerCase();

		Theme model = models.get(modelId);
		if (model == null) {
			
			if (isCreatingModel) {
				return createModel(modelId, monitor);
			}
			isCreatingModel = true;
			try {
				model = createModel(modelId, monitor);
				models.put(modelId, model);
			} finally {
				isCreatingModel = false;
			}
		}
		return model;
	}

	/**
	 * Returns the loaded model for the passed model id. This method is different from the getModel() method
	 * in the sense that this method will not load the model if its is not already loaded. If the model is 
	 * already loaded then it will return the loaded model and if not then the method returns null.
	 * @param modelId - the model id for which the Theme model should be looked up.
	 * @return - null if the model with the id has not yet been loaded, else the loaded model.
	 */
	public final synchronized Theme getLoadedModel(String modelId){
		if (modelId == null) {
			return null;
		}
		modelId = modelId.trim().toLowerCase();
		return models.get(modelId);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#releaseTheme(java.lang.String)
	 */
	public synchronized void releaseTheme(String themeId) {
		if (themeId != null) {
			for (Iterator<String> i = models.keySet().iterator(); i.hasNext();) {
				IThemeModelDescriptor descriptor = ThemePlatform
				    .getThemeModelDescriptorById(i.next());
				if (themeId.equalsIgnoreCase(descriptor.getThemeDescriptor()
				    .getId())) {
					i.remove();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#setContainerId(java.lang.String)
	 */
	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.platform.theme.IThemeManager#dispose()
	 */
	public void dispose() {
		models.clear();
	}

	protected final Theme createModel(String modelId, IProgressMonitor monitor)
	    throws ThemeException {

		IThemeModelDescriptor modelDescriptor = ThemePlatform
		    .getThemeModelDescriptorById(modelId);
		IThemeDescriptor descriptor = modelDescriptor.getThemeDescriptor();
		Theme theme = createModelSpi(modelId, monitor);
		theme.setSoundFormats(descriptor.getSoundFormats());
		for (IThemeDesignDescriptor desc : descriptor.getDesigns()) {
			if(!desc.isCustomizable())
				ThemeDesignParser.parse(theme, desc.getPath());
		}
		for (URL url : descriptor.getPreviewPaths()) {
			new ThemePreviewParser(theme, url);
		}
		for (URL url : descriptor.getSettings()) {
			ThemeDesignParser.parse(theme, url);
		}
		for (URL url : descriptor.getDimensions()) {
			ThemeDesignParser.parse(theme, url);
		}
		handleExtendedDefaultDesign(descriptor, theme);
		handleExtendedDefaultPreview(descriptor, theme);

		ThemePlatform.getIdMappingsHandler(descriptor.getId());
		// loads the layout for default display
		IDevice device = descriptor.getDefaultDevice();
		if (device != null) {
			theme.setDisplay(device.getDisplay());
			// theme.computeAllPreviewElementLayout(monitor);
		}

		processModelSpi(theme, monitor);

		if (DebugHelper.debugPerformance()) {
			// some time-consuming parts are processed here
			theme.getBackgroundDependency();
		}
		return theme;
	}

	private void handleExtendedDefaultPreview(IThemeDescriptor descriptor,
	    Theme theme) throws ThemeException {
		URL extendedDefaultPreviewPath = descriptor
		    .getExtendedDefaultPreviewPath();
		if (null != extendedDefaultPreviewPath) {
			new ThemePreviewParser(theme, extendedDefaultPreviewPath);
		}
	}

	private void handleExtendedDefaultDesign(IThemeDescriptor descriptor,
	    Theme theme) throws ThemeException {
		URL extendedDefaultDesignPath = descriptor
		    .getExtendedDefaultDesignPath();
		if (null != extendedDefaultDesignPath) {
			ThemeDesignParser.parse(theme, extendedDefaultDesignPath);
		}
	}

	protected abstract Theme createModelSpi(String themeId,
	    IProgressMonitor monitor) throws ThemeException;

	protected abstract void processModelSpi(Theme model,
	    IProgressMonitor monitor) throws ThemeException;
}
