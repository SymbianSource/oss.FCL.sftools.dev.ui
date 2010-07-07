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

package com.nokia.tools.packaging.commandline.commands;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.nokia.tools.content.core.ContentAttribute;
import com.nokia.tools.content.core.ContentException;
import com.nokia.tools.content.core.ContentSourceManager;
import com.nokia.tools.content.core.IContent;
import com.nokia.tools.content.core.IContentProvider;
import com.nokia.tools.packaging.IPackager;
import com.nokia.tools.packaging.IPackagingProcessor;
import com.nokia.tools.packaging.PackagingAttribute;
import com.nokia.tools.packaging.PackagingConstants;
import com.nokia.tools.packaging.PackagingContext;
import com.nokia.tools.packaging.PackagingException;
import com.nokia.tools.packaging.commandline.CmdParameters;
import com.nokia.tools.packaging.commandline.PackagerMessages;
import com.nokia.tools.packaging.commandline.util.CMDProgressMonitor;
import com.nokia.tools.packaging.commandline.util.CommandLineException;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.theme.content.ThemeUtil;
import com.nokia.tools.theme.s60.S60ThemeProvider;
import com.nokia.tools.theme.s60.packaging.SigningProcessor;
import com.nokia.tools.theme.s60.packaging.SisPackagingProcessor;

/**
 * This command generates skin descriptor file.
 * 
 * @author surmathe
 */
public class TDF2SkinDescCommand implements ICommand {

	private IContent theme = null;

	private CmdParameters cmdParameters;

	private CMDProgressMonitor progressBar = null;

	public TDF2SkinDescCommand(CmdParameters cmdParameters,
			CMDProgressMonitor progressBar) {
		this.progressBar = progressBar;
		this.cmdParameters = cmdParameters;
	}

	/**
	 * generates from tdf to skin descriptor, pkg,mif
	 */
	public void execute() throws CommandLineException {
		progressBar.setTaskName("Generating skin description file");
		IContentProvider provider = ContentSourceManager
				.getGlobalContentProvider(S60ThemeProvider.CONTENT_TYPE);
		List<IContent> contents = null;
		try {
			contents = provider.getRootContents(cmdParameters.getTdfFile(),
					new NullProgressMonitor());
			theme = contents.get(0);
			if (new Boolean((String) theme
					.getAttribute(ContentAttribute.MODIFIED.name())) == false) {
				throw new CommandLineException(
						PackagerMessages.Packager_Error_no_content);
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw new CommandLineException(PackagerMessages.Packager_Error_IO);
		} catch (ContentException e) {
			e.printStackTrace();
			throw new CommandLineException(
					PackagerMessages.Packager_Error_content_read_failed);
		} finally {
			progressBar.addWorked(20);
		}
		progressBar.progress(progressBar.getWorked());
		// theme = contents.get(0);
		//		
		// PackagerMessages
		// .printMessageOnConsole(PackagerMessages.Packager_Commandline
		// + theme.getAttribute(ContentAttribute.MODIFIED.name()));

		theme.setAttribute(ContentAttribute.PLATFORM.name(), cmdParameters
				.getPlatform());

		theme.setAttribute(ContentAttribute.COPYRIGHT.name(), cmdParameters
				.getStrNotice());
		theme.setAttribute(ContentAttribute.ALLOW_COPYING.name(), cmdParameters
				.getStrCopyAllowed());
		theme.setAttribute(ContentAttribute.PACKAGING.name(), "true");
		if (cmdParameters.getStrThemeName().trim().length() != 0)
			theme.setAttribute(ContentAttribute.APPLICATION_NAME.name(),
					cmdParameters.getStrThemeName().trim());
		theme.setAttribute(ContentAttribute.ALLOW_COPYING.name(), cmdParameters
				.getStrCopyAllowed());
		theme.setAttribute(ContentAttribute.APP_UID.name(), cmdParameters
				.getStrSelThemeUID());

		IPackager packager = (IPackager) theme.getAdapter(IPackager.class);
		for (IPackagingProcessor processor : packager.getProcessors()) {
			if (processor instanceof SisPackagingProcessor
					|| processor instanceof SigningProcessor) {
				packager.removeProcessor(processor);
			}
		}
		progressBar.addWorked(20);
		progressBar.progress(progressBar.getWorked());
		try {
			packager.buildPackage(createContext(this.cmdParameters
					.getPlatform()));
		} catch (PackagingException e) {
			e.printStackTrace();
			throw new CommandLineException(
					PackagerMessages.Packager_Error_skincompiler);
		} finally {
			progressBar.addWorked(40);
			progressBar.progress(progressBar.getWorked());
		}
	}

	/**
	 * packaging context
	 * 
	 * @param platform
	 * @return
	 * @throws Exception
	 */
	protected PackagingContext createContext(IPlatform platform) {
		PackagingContext context = new PackagingContext();
		context.setAttribute(PackagingAttribute.platform.name(), platform);
		
		/* Setting the primary and secondary model Id */
		
		/* Fetching the model Id based version number provided in the command line packaging  */
		
		if(cmdParameters.getStrVer() == null || cmdParameters.getStrVer().trim().length() == 0){
			context.setAttribute(PackagingAttribute.primaryModelId.name(),platform.getId());
		}
		else{
			List<Theme> allLoadedModels = ThemeUtil.getAllLoadedModels();
			
			String primaryModelId = null;
			
			for(Theme model: allLoadedModels){
				if(model.getModelId().contains(cmdParameters.getStrVer().trim())){
					primaryModelId = model.getId();
					break;
				}
			}
			if(primaryModelId == null){
				primaryModelId = platform.getId();
			}
			if (primaryModelId.equals("E75")) {
				context.setAttribute(PackagingAttribute.primaryModelId.name(),
						"S60_31_E75");
			}else if (primaryModelId.equals("E66")) {
				context.setAttribute(PackagingAttribute.primaryModelId.name(),
				"S60_32_E66");
			} 
			else if (primaryModelId.equals("E71")) {
				context.setAttribute(PackagingAttribute.primaryModelId.name(),
						"S60_31_E71");
			}
			else {
				context.setAttribute(PackagingAttribute.primaryModelId.name(),
						primaryModelId);
			}
		}
		context.setAttribute(PackagingAttribute.secondaryModelId.name(),platform.getId());
		
		context.setAttribute(PackagingAttribute.algorithm.name(),
				PackagingConstants.ALGORITHM_DSA);
		context.setAttribute(PackagingAttribute.input.name(), theme);
		context.setAttribute(PackagingAttribute.theme.name(), theme);
		context.setAttribute(PackagingAttribute.workingDir.name(),
				cmdParameters.getOutPut().getAbsolutePath());
		return context;
	}
}
