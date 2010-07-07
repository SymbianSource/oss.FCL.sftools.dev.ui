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
package com.nokia.tools.screen.ui.extension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.nokia.tools.content.core.AbstractContentSourceManager;
import com.nokia.tools.resource.util.StringUtils;
import com.nokia.tools.screen.ui.UiPlugin;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorContributor;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorCustomizer;
import com.nokia.tools.screen.ui.editor.embedded.IEmbeddedEditorPart;
import com.nokia.tools.screen.ui.wizards.AbstractNewPackagePage;
import com.nokia.tools.screen.ui.wizards.AbstractPackagingOperation;
import com.nokia.tools.screen.ui.wizards.IWizardValidator;
import com.nokia.tools.ui.editor.EmbeddedEditorSite;

public class ExtensionManager {
	private static final String CONTRIBUTOR_ID = "com.nokia.tools.screen.ui.contributors";
	private static final String EMBEDDED_EDITOR_ID = "com.nokia.tools.screen.ui.embeddedEditors";
	private static final String EXT_OPERATION = "operation";
	private static final String EXT_EDITOR = "editor";
	private static final String EXT_BRANDING = "branding";
	private static final String EXT_PACKAGING = "packaging";
	private static final String EXT_WIZARD_PAGE = "wizardPage";
	private static final String EXT_GALLERY = "gallery";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_CONTRIBUTOR = "contributor";
	private static final String ATTR_CONTENT_TYPE = "contentType";
	private static final String ATTR_EXTENSIONS = "extensions";
	private static final String ATTR_CUSTOMIZER = "customizer";
	private static final String EXT_CLASSPATH_CONTAINER = "classpathContainer";
	private static final String ATTR_ID = "id";
	private static final String ATTR_OPERATION_CLASS = "operationClass";
	private static final String ATTR_HELP_CONTEXT_ID = "helpContextId";
	private static final String ATTR_DEFAULT = "default";
	private static final String ATTR_SCREENS = "screens";
	private static final String DELIMITER = ",";
	private static final String EXT_DEPLOYMENT = "deployment";
	private static final String EXT_ID_WIZARD_VALIDATOR = "com.nokia.tools.screen.ui.wizardValidator";
	private static final String EXT_WIZARD_VALIDATOR = "wizardValidator";
	private static final String EXT_ATTR_WIZARD_VALIDATOR = "validator";
	
	private static final String EXT_ID_PACKAGING_PREPROCESSING_ACTION = "com.nokia.tools.screen.ui.packagingPreProcessingAction";
	private static final String EXT_PACKAGING_PREPROCESSING_ACTION = "packagingPreProcessingAction";
	private static final String EXT_ATTR_PACKAGING_PREPROCESSING_ACTION_CLASS = "actionClass";
	private static final String EXT_ATTR_PACKAGING_PREPROCESSING_ACTION_SUPPORTED_CONTENT = "contentType";

	private static final ArrayList<IWizardValidator> validators = new ArrayList<IWizardValidator>();

	public static IEmbeddedEditorDescriptor[] getEmbeddedEditorDescriptors() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(EMBEDDED_EDITOR_ID);
		IExtension[] extensions = point.getExtensions();
		List<IEmbeddedEditorDescriptor> descriptors = new ArrayList<IEmbeddedEditorDescriptor>(
				extensions.length);
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (EXT_EDITOR.equals(element.getName())) {
					descriptors.add(new EmbeddedEditorDescriptor(element));
				}
			}
		}
		return descriptors.toArray(new IEmbeddedEditorDescriptor[descriptors
				.size()]);
	}

	/**
	 * This returns an array of validators contributed by clients for 
	 * a wizard. 
	 * 
	 * @param clazz The wizard class name
	 * @return - array of validators being contributed for this wizard
	 */
	public static IWizardValidator[] getWizardValidators(Class clazz) {
		if (validators.size() == 0) {
			IExtensionPoint point = Platform.getExtensionRegistry()
					.getExtensionPoint(EXT_ID_WIZARD_VALIDATOR);
			IExtension[] extensions = point.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension
						.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (EXT_WIZARD_VALIDATOR.equals(element.getName())) {
						try {
							IWizardValidator validator = (IWizardValidator) element
									.createExecutableExtension(EXT_ATTR_WIZARD_VALIDATOR);
							if (validator.getWizardClasses().contains(clazz))
								validators.add(validator);
						} catch (CoreException e) {
							
							e.printStackTrace();
						}
					}
				}
			}
		}
		return validators.toArray(new IWizardValidator[validators.size()]);
	}



	public static IEmbeddedEditorDescriptor getEmbeddedEditorDescriptorByContentType(
			String contentType) {
		for (IEmbeddedEditorDescriptor desc : getEmbeddedEditorDescriptors()) {
			if (contentType.equals(desc.getContentType())) {
				return desc;
			}
		}
		return null;
	}

	public static IEmbeddedEditorDescriptor getEmbeddedEditorDescriptorByExtension(
			String extension) {
		for (IEmbeddedEditorDescriptor desc : getEmbeddedEditorDescriptors()) {
			String[] exts = desc.getExtensions();
			if (exts != null) {
				for (String ext : exts) {
					if (ext.equalsIgnoreCase(extension)) {
						return desc;
					}
				}
			}
		}
		return null;
	}

	public static IEmbeddedEditorDescriptor getEmbeddedEditorDescriptor(
			IEditorInput input) {
		String extension = input.getName().toLowerCase();
		if (extension.contains(".")) {
			extension = extension.substring(extension.lastIndexOf("."));
		}
		return getEmbeddedEditorDescriptorByExtension(extension);
	}

	public static IContributorDescriptor[] getContributorDescriptors() {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CONTRIBUTOR_ID);
		IExtension[] extensions = point.getExtensions();
		List<IContributorDescriptor> contributors = new ArrayList<IContributorDescriptor>();
		for (IExtension extension : extensions) {
			contributors.add(new ContributorDescriptor(extension));
		}

		return contributors.toArray(new IContributorDescriptor[contributors
				.size()]);
	}

/*	public static IDeploymentDescriptor getDeploymentDescriptor(
			String contentType) {
		if (contentType == null) {
			return null;
		}

		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CONTRIBUTOR_ID);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (final IConfigurationElement element : elements) {
				if (EXT_DEPLOYMENT.equals(element.getName())) {
					final String type = element.getAttribute(ATTR_CONTENT_TYPE);
					if (contentType.equals(type)) {
						return new IDeploymentDescriptor() {

							public AbstractDeploymentOperation getDeploymentClass() {
								try {
									return (AbstractDeploymentOperation) element
											.createExecutableExtension(ATTR_OPERATION_CLASS);
								} catch (Exception e) {
									UiPlugin.error(e);
									return null;
								}
							}
						};
					}
				}
			}
		}
		return null;
	}
*/
	public static IPackagingDescriptor getPackagingDescriptor(String contentType) {
		if (contentType == null) {
			return null;
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CONTRIBUTOR_ID);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (final IConfigurationElement element : elements) {
				if (EXT_PACKAGING.equals(element.getName())) {
					final String type = element.getAttribute(ATTR_CONTENT_TYPE);
					if (contentType.equals(type)) {
						return new IPackagingDescriptor() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IPackagingDescriptor#getContentType()
							 */
							public String getContentType() {
								return type;
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IPackagingDescriptor#getHelpContextId()
							 */
							public String getHelpContextId() {
								return element
										.getAttribute(ATTR_HELP_CONTEXT_ID);
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IPackagingDescriptor#getPackagingOperation()
							 */
							public AbstractPackagingOperation getPackagingOperation() {
								try {
									return (AbstractPackagingOperation) element
											.createExecutableExtension(ATTR_OPERATION_CLASS);
								} catch (Exception e) {
									UiPlugin.error(e);
									return null;
								}
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IPackagingDescriptor#getWizardPages()
							 */
							public AbstractNewPackagePage[] getWizardPages() {
								List<AbstractNewPackagePage> list = new ArrayList<AbstractNewPackagePage>();
								for (IConfigurationElement child : element
										.getChildren()) {
									if (EXT_WIZARD_PAGE.equals(child.getName())) {
										try {
											list
													.add((AbstractNewPackagePage) child
															.createExecutableExtension(ATTR_CLASS));
										} catch (Exception e) {
											UiPlugin.error(e);
										}
									}
								}
								return list
										.toArray(new AbstractNewPackagePage[list
												.size()]);
							}

						};
					}
				}
			}
		}
		return null;
	}

	public static IGalleryDescriptor[] getGalleryDescriptors() {
		String[] contentTypes = AbstractContentSourceManager.getContentTypes();
		List<IGalleryDescriptor> descriptors = new ArrayList<IGalleryDescriptor>();
		for (String type : contentTypes) {
			IGalleryDescriptor desc = getGalleryDescriptor(type);
			if (desc != null) {
				descriptors.add(desc);
			}
		}
		return descriptors.toArray(new IGalleryDescriptor[descriptors.size()]);
	}

	public static IGalleryDescriptor getGalleryDescriptor(String contentType) {
		if (contentType == null) {
			return null;
		}
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(CONTRIBUTOR_ID);
		IExtension[] extensions = point.getExtensions();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension
					.getConfigurationElements();
			for (final IConfigurationElement element : elements) {
				if (EXT_GALLERY.equalsIgnoreCase(element.getName())) {
					final String type = element.getAttribute(ATTR_CONTENT_TYPE);
					if (contentType.equals(type)) {
						return new IGalleryDescriptor() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IGalleryDescriptor#getContentType()
							 */
							public String getContentType() {
								return type;
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IGalleryDescriptor#getDefault()
							 */
							public String getDefault() {
								return element.getAttribute(ATTR_DEFAULT);
							}

							/*
							 * (non-Javadoc)
							 * 
							 * @see com.nokia.tools.screen.ui.extension.IGalleryDescriptor#getScreens()
							 */
							public String[] getScreens() {
								String screens = element
										.getAttribute(ATTR_SCREENS);
								List<String> list = new ArrayList<String>();
								if (screens != null) {
									for (String screen : screens
											.split(DELIMITER)) {
										if (!StringUtils.isEmpty(screen)) {
											list.add(screen.trim());
										}
									}
								}
								return list.toArray(new String[list.size()]);
							}
						};
					}
				}
			}
		}
		return null;
	}

	static class ContributorDescriptor implements IContributorDescriptor {
		IExtension extension;

		ContributorDescriptor(IExtension extension) {
			this.extension = extension;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IContributorDescriptor#createOperation(java.lang.String)
		 */
		public WorkspaceModifyOperation createOperation(String type) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (EXT_OPERATION.equals(element.getName())
						&& element.getAttribute(type) != null) {
					try {
						if (element.createExecutableExtension(type) instanceof WorkspaceModifyOperation)
							return (WorkspaceModifyOperation) element
									.createExecutableExtension(type);
					} catch (Exception e) {
						UiPlugin.error(e);
					}
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IContributorDescriptor#getClasspathContainer()
		 */
		public String getClasspathContainer() {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (EXT_CLASSPATH_CONTAINER.equals(element.getName())) {
					return element.getAttribute(ATTR_ID);
				}
			}
			return null;
		}

		public Action createAction(String type) {
			for (IConfigurationElement element : extension
					.getConfigurationElements()) {
				if (EXT_OPERATION.equals(element.getName())
						&& element.getAttribute(type) != null) {
					try {
						return (Action) element.createExecutableExtension(type);
					} catch (Exception e) {
						UiPlugin.error(e);
					}
				}
			}
			return null;
		}
	}

	static class EmbeddedEditorDescriptor implements IEmbeddedEditorDescriptor {
		IConfigurationElement element;

		EmbeddedEditorDescriptor(IConfigurationElement element) {
			this.element = element;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor#getContentType()
		 */
		public String getContentType() {
			return element.getAttribute(ATTR_CONTENT_TYPE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor#getExtensions()
		 */
		public String[] getExtensions() {
			String extensions = element.getAttribute(ATTR_EXTENSIONS);
			List<String> exts = new ArrayList<String>();
			if (extensions != null) {
				for (String ext : extensions.split(",")) {
					if ((ext = ext.trim()).length() > 0) {
						exts.add(ext);
					}
				}
			}
			return exts.toArray(new String[exts.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.editor.IEmbeddedEditorDescriptor#createEditorPart(org.eclipse.ui.IEditorPart,
		 *      org.eclipse.ui.IEditorInput)
		 */
		public IEmbeddedEditorPart createEditorPart(IEditorPart mainEditor,
				IEditorInput input) {
			try {
				IEmbeddedEditorPart part = (IEmbeddedEditorPart) element
						.createExecutableExtension(ATTR_CLASS);
				part.init(new EmbeddedEditorSite(mainEditor, part), input);
				return part;
			} catch (Exception e) {
				UiPlugin.error(e);
				return null;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.editor.IEmbeddedEditorDescriptor#getContributor()
		 */
		public IEmbeddedEditorContributor getContributor() {
			try {
				if (element.getAttribute(ATTR_CONTRIBUTOR) != null) {
					return (IEmbeddedEditorContributor) element
							.createExecutableExtension(ATTR_CONTRIBUTOR);
				}
			} catch (Exception e) {
				UiPlugin.error(e);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.screen.ui.extension.IEmbeddedEditorDescriptor#getEmbeddedEditorCustomizer()
		 */
		public IEmbeddedEditorCustomizer getEmbeddedEditorCustomizer() {
			try {
				if (element.getAttribute(ATTR_CUSTOMIZER) != null) {
					return (IEmbeddedEditorCustomizer) element
							.createExecutableExtension(ATTR_CUSTOMIZER);
				}
			} catch (Exception e) {
				UiPlugin.error(e);
			}
			return null;
		}
	}

	public static IPackagingPreprocessingAction[] getPackagingPreProcessingActions(String contentType) {
		if(contentType == null){
			return null;
		}
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(EXT_ID_PACKAGING_PREPROCESSING_ACTION);
		IExtension[] extensions = point.getExtensions();
		List<IPackagingPreprocessingAction> packagingPreprocessingActions = new ArrayList<IPackagingPreprocessingAction>(); 
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (EXT_PACKAGING_PREPROCESSING_ACTION.equals(element.getName())) {
					Object attributeValue = element.getAttribute(EXT_ATTR_PACKAGING_PREPROCESSING_ACTION_SUPPORTED_CONTENT);
					if(attributeValue != null){
						String contentTypeFound = attributeValue.toString();
						if(contentTypeFound.equalsIgnoreCase(contentType)){
							try {
								packagingPreprocessingActions.add( (IPackagingPreprocessingAction)element.createExecutableExtension(EXT_ATTR_PACKAGING_PREPROCESSING_ACTION_CLASS) );
							} catch (CoreException e) {
								UiPlugin.error(e);
							}
						}
					}
					
				}
			}
		}
		return packagingPreprocessingActions.toArray(new IPackagingPreprocessingAction[packagingPreprocessingActions.size()]);
	}
}
