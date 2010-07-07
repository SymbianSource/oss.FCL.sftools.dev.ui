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

package com.nokia.tools.theme.s60;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;

import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.editing.model.EditObject;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IImageAdapter;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.packaging.IPackager.Packager;
import com.nokia.tools.packaging.optimization.OptimizingPackageProcessor;
import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemePlatform;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.screen.ui.IScreenConstants;
import com.nokia.tools.screen.ui.ISkinnableEntityAdapter;
import com.nokia.tools.screen.ui.IToolBoxAdapter;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.theme.content.AbstractThemeProvider;
import com.nokia.tools.theme.content.ThemeContent;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.editing.IEntityImageFactory;
import com.nokia.tools.theme.s60.editing.EditableEntityImageFactory;
import com.nokia.tools.theme.s60.internal.core.TSModelController;
import com.nokia.tools.theme.s60.packaging.BitmapConverter;
import com.nokia.tools.theme.s60.packaging.ExternalFilesExtractingProcessor;
import com.nokia.tools.theme.s60.packaging.ItemIdExtensionListProcessor;
import com.nokia.tools.theme.s60.packaging.MifConverter;
import com.nokia.tools.theme.s60.packaging.PipProcessor;
import com.nokia.tools.theme.s60.packaging.S60ThemePackageAccessor;
import com.nokia.tools.theme.s60.packaging.SigningProcessor;
import com.nokia.tools.theme.s60.packaging.SisPackagingProcessor;
import com.nokia.tools.theme.s60.packaging.SkinDescCompiler;
import com.nokia.tools.theme.s60.packaging.ThemeDescriptionProcessor;
import com.nokia.tools.theme.s60.packaging.ThemePackagingProcessor;
import com.nokia.tools.theme.screen.ThemeElement;
import com.nokia.tools.theme.screen.ThemeTextElement;

public class S60ThemeProvider
    extends AbstractThemeProvider {

	public static final String CONTENT_TYPE = "S60THEME";
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.content.core.IContentProvider#getContentType()
	 */
	public String getContentType() {
		return CONTENT_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createTheme(java.util.Map,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Theme createTheme(Map<String, Object> creationData,
	    IProgressMonitor monitor) throws ContentException {
		try {
			return ThemeUtil.createTheme(creationData, null, monitor);
		} catch (Exception e) {
			throw new ContentException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createThemeContent(com.nokia.tools.editing.model.EditObject)
	 */
	@Override
	protected ThemeContent createThemeContent(EditObject themeResource) {
		return new S60ThemeContent(themeResource, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#getContainerId()
	 */
	@Override
	protected String getContainerId() {
		return IThemeConstants.THEME_CONTAINER_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#isFileValid(java.io.File)
	 */
	@Override
	protected boolean isFileValid(File file) {
		return file != null && file.canRead()
		    && file.getName().toLowerCase().endsWith(ThemeTag.SKN_FILE_EXTN);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createPackager()
	 */
	protected IPackager createPackager() {
		IPackager packager = new Packager() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.packaging.IPackager#supportPlatform(java.lang.String)
			 */
			public boolean supportPlatform(IPlatform platform) {
				return Platform.OS_WIN32.equals(Platform.getOS());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.packaging.IPackager.Packager#getPlatformPackagingName(com.nokia.tools.platform.core.IPlatform)
			 */
			public String getPlatformPackagingName(IPlatform platform) {
				return super.getPlatformPackagingName(platform);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.nokia.tools.packaging.IPackager.Packager#getSupportedPlatforms()
			 */
			public IPlatform[] getSupportedPlatforms() {
				return new IPlatform[]{DevicePlatform.S60_5_0, DevicePlatform.SF_2};
			}
		};

		packager.addProcessor(new ExternalFilesExtractingProcessor());
		packager.addProcessor(new ThemeDescriptionProcessor());

		// temporarily disabled the optimization processor to avoid the appicon
		// and normal icon problem
		IPreferenceStore store = UiPlugin.getDefault().getPreferenceStore();
		if (store
		    .getBoolean(IScreenConstants.PREF_PACKAGING_OPTIMIZATION_ENABLED)) {
			packager.addProcessor(new OptimizingPackageProcessor(
			    new S60ThemePackageAccessor()));
		}
		packager.addProcessor(new ItemIdExtensionListProcessor());
		packager.addProcessor(new SkinDescCompiler());
		packager.addProcessor(new MifConverter());
		packager.addProcessor(new BitmapConverter());
		packager.addProcessor(new ThemePackagingProcessor());
		packager.addProcessor(new SisPackagingProcessor());
		packager.addProcessor(new SigningProcessor());
		packager.addProcessor(new PipProcessor());
		return packager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createImageAdapter(com.nokia.tools.theme.content.ThemeData)
	 */
	protected IImageAdapter createImageAdapter(ThemeData data) {
		return new S60ImageAdapter(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createSkinnableEntityAdapter(com.nokia.tools.theme.content.ThemeData)
	 */
	protected ISkinnableEntityAdapter createSkinnableEntityAdapter(
	    ThemeData data) {
		return new S60SkinnableEntityAdapter(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#getEntityImageFactory()
	 */
	@Override
	public IEntityImageFactory getEntityImageFactory() {
		return EditableEntityImageFactory.getInstance();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#init(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void init(IProgressMonitor monitor) throws ContentException {
		TSModelController.init(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#release()
	 */
	@Override
	protected void release() {
		TSModelController.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createVMAdapters(com.nokia.tools.theme.screen.ThemeElement)
	 */
	@Override
	public Object[] createVMAdapters(ThemeElement element) {
		IImageAdapter adapter = (IImageAdapter) element.getData().getAdapter(
		    IImageAdapter.class);
		IToolBoxAdapter toolAdapter = (IToolBoxAdapter) element.getData()
		    .getAdapter(IToolBoxAdapter.class);
		if (adapter != null) {
			IImage image = adapter.getImage();
			if (image != null
			    && (adapter.isAnimated() || image.canBeAnimated() || toolAdapter
			        .isMultipleLayersSupport())) {
				return new Object[] { new S60ThemeAnimator(element) };
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.content.AbstractThemeProvider#createTextElement(com.nokia.tools.theme.content.ThemeData)
	 */
	@Override
	protected ThemeTextElement createTextElement(ThemeData element) {
		return new S60ThemeTextElement(element);
	}
}
