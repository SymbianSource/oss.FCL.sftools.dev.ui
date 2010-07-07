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

package com.nokia.tools.theme.s60.general;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.extension.PlatformExtensionManager;

public class PlatformSuppportInfoProvider {

	private static String PLATFORM_SUPPORT_INFO_EXTENSION_POINT_ID = "com.nokia.tools.theme.s60.platformSupportInfo";
	
	private static String PLATFORM_SUPPORT_INFO_ELEMENT_NAME = "platformSupportInfo";
	
	private static String DOCUMENT_PATH_ATTRIBUTE = "docPath";
	
	private static String ICON_FILE_PATH_ATTRIBUTE = "iconFilePath"; 
	
	private static String PLATFORM_ID_ATTRIBUTE = "platformID"; 
	
	public static ArrayList<URL> getDocumentPath(){
		ArrayList<URL> documentPathURL = new ArrayList<URL>();
		IExtension[] extensions = PlatformExtensionManager.getExtensions(PLATFORM_SUPPORT_INFO_EXTENSION_POINT_ID);
		if(extensions != null){
			for (IExtension extension : extensions) {
				IConfigurationElement[] configurationElements = extension.getConfigurationElements();
				if(configurationElements != null){
					for (IConfigurationElement configurationElement : configurationElements) {
						if(configurationElement != null && PLATFORM_SUPPORT_INFO_ELEMENT_NAME.equals(configurationElement.getName())){
							String relDocumentPath = configurationElement.getAttribute(DOCUMENT_PATH_ATTRIBUTE);
							Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
							if(bundle != null){
								documentPathURL.add(bundle.getEntry(relDocumentPath));
							}
						}
					}
				}
			}
		}
		return documentPathURL;
	}
	
	
	public static ArrayList<PlatformSupportExtension> getExtensions(){
		ArrayList<PlatformSupportExtension> pSIExtensions = new ArrayList<PlatformSupportExtension>();
		IExtension[] extensions = PlatformExtensionManager.getExtensions(PLATFORM_SUPPORT_INFO_EXTENSION_POINT_ID);
		if(extensions != null){
			for (IExtension extension : extensions) {
				pSIExtensions.add(new PlatformSupportExtension(extension));
			}
		}
		return pSIExtensions;
	}
	
	
	static class PlatformSupportExtension {

		private IExtension pSInfo;

		public PlatformSupportExtension(IExtension iExtension) {
			pSInfo = iExtension;
		}

		public URL getDocumentPath() {
			URL docPath = null;
			IConfigurationElement[] configurationElements = pSInfo
					.getConfigurationElements();
			if (configurationElements != null) {
				for (IConfigurationElement configurationElement : configurationElements) {
					if (configurationElement != null
							&& PLATFORM_SUPPORT_INFO_ELEMENT_NAME
									.equals(configurationElement.getName())) {
						String relDocumentPath = configurationElement
								.getAttribute(DOCUMENT_PATH_ATTRIBUTE);
						Bundle bundle = Platform.getBundle(pSInfo
								.getNamespaceIdentifier());
						if (bundle != null) {
							docPath = bundle.getEntry(relDocumentPath);
						}
					}
				}
			}
			return docPath;
		}

		public URL getIconPath() {
			URL iconFilePath = null;
			IConfigurationElement[] configurationElements = pSInfo
					.getConfigurationElements();
			if (configurationElements != null) {
				for (IConfigurationElement configurationElement : configurationElements) {
					if (configurationElement != null
							&& PLATFORM_SUPPORT_INFO_ELEMENT_NAME
									.equals(configurationElement.getName())) {
						String relDocumentPath = configurationElement
								.getAttribute(ICON_FILE_PATH_ATTRIBUTE);
						Bundle bundle = Platform.getBundle(pSInfo
								.getNamespaceIdentifier());
						if (bundle != null) {
							iconFilePath = bundle.getEntry(relDocumentPath);
						}
					}
				}
			}
			return iconFilePath;
		}

		public String getPlatformId() {
			String platformId = null;
			IConfigurationElement[] configurationElements = pSInfo
					.getConfigurationElements();
			if (configurationElements != null) {
				for (IConfigurationElement configurationElement : configurationElements) {
					if (configurationElement != null
							&& PLATFORM_SUPPORT_INFO_ELEMENT_NAME
									.equals(configurationElement.getName())) {
						platformId = configurationElement
								.getAttribute(PLATFORM_ID_ATTRIBUTE);
					}
				}
			}
			return platformId;
		}
	}


	public static Image getImage(IPlatform platform) {
		IExtension[] extensions = PlatformExtensionManager.getExtensions(PLATFORM_SUPPORT_INFO_EXTENSION_POINT_ID);
		if(extensions != null){
			for (IExtension extension : extensions) {
				IConfigurationElement[] configurationElements = extension.getConfigurationElements();
				if(configurationElements != null){
					for (IConfigurationElement configurationElement : configurationElements) {
						if(configurationElement != null && PLATFORM_SUPPORT_INFO_ELEMENT_NAME.equals(configurationElement.getName())){
							if(platform.getId().equals(configurationElement.getAttribute(PLATFORM_ID_ATTRIBUTE))){
								String iconFilePath = configurationElement.getAttribute(ICON_FILE_PATH_ATTRIBUTE);
								Bundle bundle = Platform.getBundle(extension.getNamespaceIdentifier());
								URL iconFileURL = null;
								if(bundle != null){
									iconFileURL = bundle.getEntry(iconFilePath);
								}
								return ImageDescriptor.createFromURL(iconFileURL).createImage();
							}
						}
					}
				}
			}
		}
		return null;
	}
	
}
