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
package com.nokia.tools.platform.extension;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.nokia.tools.platform.core.PlatformCorePlugin;
import com.nokia.tools.resource.util.FileUtils;
import com.nokia.tools.resource.util.StringUtils;

/**
 * Represents informations about plugin.
 * 
 * Used for saving data mainly from plugin.xml, such as defined layouts or devices for
 * S60 plugin.
 */
public class PluginEntry {
	public static final String PLUGIN_XML = "plugin.xml";
	private static final String EXTENSION = "extension";
	private static final String POINT = "point";
	private static final String PLUGIN_TAG = "plugin";

	private String symbolicName;
	private String name;
	private String version;
	private String vendor;
	private Map<String, List<IExtension>> extensions;
	private File file;

	/**
	 * Constructor.
	 * 
	 * Instantiates this class from file.
	 * 
	 * @param file plugin.xml file or JAR/ZIP archive file of plug-in.
	 * @throws Exception
	 */
	public PluginEntry(File file) throws Exception {
		if (!file.isFile()) {
			throw new IllegalArgumentException(
					"Plugin JAR file or plugin.xml is expected but found: "
							+ file);
		}
		this.file = file;
		if (isPluginDescriptor()) {
			parsePluginDescriptor();
		} else {
			parseJarFile();
		}
	}

	/**
	 * Returns true if plug-in represented by this entry is valid.
	 * @return true if plug-in represented by this entry is valid
	 */
	public boolean isValid() {
		return name != null && version != null && extensions != null
				&& !extensions.isEmpty();
	}

	/**
	 * Returns true if file passed here by constructor is plugin descriptor
	 * file (has plugin.xml name).
	 * @return true if file passed in constructor is plugin.xml
	 */
	public boolean isPluginDescriptor() {
		return PLUGIN_XML.equals(file.getName());
	}

	/**
	 * Returns true if there is specified resource within plugin.
	 * @param path resource to search within plugn
	 * @return if there is speficied resource in plugin.
	 */
	public boolean containsResource(String path) {
		if (isPluginDescriptor()) {
			return new File(file.getParentFile(), path).exists();
		}

		JarFile jar = null;
		try {
			jar = new JarFile(file);
			return jar.getJarEntry(path) != null;
		} catch (Exception e) {
		} finally {
			try {
				jar.close();
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * Parses plugin.xml file.
	 * @throws Exception
	 */
	private void parsePluginDescriptor() throws Exception {
		File manifest = new File(file.getParentFile(), "META-INF/MANIFEST.MF");
		InputStream in = null;
		try {
			in = new FileInputStream(manifest);
			setManifest(in);
		} finally {
			FileUtils.close(in);
		}
		try {
			in = new FileInputStream(file);
			setPlugin(in);
		} finally {
			FileUtils.close(in);
		}
	}

	/**
	 * Parses information about plug-in packed in ZIP/JAR file
	 * @throws Exception
	 */
	private void parseJarFile() throws Exception {
		JarFile jar = null;
		try {
			jar = new JarFile(file);
			// tries to find MANIFEST file - lot of variants :)
			JarEntry manifest = jar.getJarEntry("META-INF/MANIFEST.MF");
			if (manifest == null) {
				manifest = jar.getJarEntry("/META-INF/MANIFEST.MF");
			}			
			if (manifest == null) {
				manifest = jar.getJarEntry("META-INF\\MANIFEST.MF");
			}
			if (manifest == null) {
				manifest = jar.getJarEntry("\\META-INF\\MANIFEST.MF");
			}				
			if (manifest != null) {
				// if found, stores it
				InputStream in = null;
				try {
					in = jar.getInputStream(manifest);
					setManifest(in);
				} finally {
					FileUtils.close(in);
				}
			}
			// tries to find plugin.xml - lot of variants
			JarEntry plugin = jar.getJarEntry("plugin.xml");
			if (plugin == null) {
				plugin = jar.getJarEntry("/plugin.xml");
			}
			if (plugin == null) {
				plugin = jar.getJarEntry("\\plugin.xml");
			}		
			if (plugin != null) {
				// if found, stores it
				InputStream in = null;
				try {
					in = jar.getInputStream(plugin);
					setPlugin(in);
				} finally {
					FileUtils.close(in);
				}
			}
		} finally {
			try {
				if (jar != null) {
					jar.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @return the symbolicName
	 */
	public String getSymbolicName() {
		return symbolicName;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	public IExtension[] getExtensions(String id) {
		if (extensions != null) {
			List<IExtension> list = extensions.get(id);
			if (list != null) {
				return list.toArray(new IExtension[list.size()]);
			}
		}
		return new IExtension[0];
	}

	private void setManifest(InputStream in) throws Exception {
		Manifest manifest = new Manifest(in);
		Attributes a = manifest.getMainAttributes();
		// plugin id
		String value = a.getValue(Constants.BUNDLE_SYMBOLICNAME);
		if (value != null) {
			ManifestElement[] elements = ManifestElement.parseHeader(
					Constants.BUNDLE_SYMBOLICNAME, value);
			if (elements.length > 0) {
				boolean singleton = new Boolean(elements[0]
						.getDirective(Constants.SINGLETON_DIRECTIVE));
				if (!singleton) {
					return;
				}
				symbolicName = elements[0].getValue();
			}
		}
		name = a.getValue(Constants.BUNDLE_NAME);
		version = a.getValue(Constants.BUNDLE_VERSION);
		vendor = a.getValue(Constants.BUNDLE_VENDOR);
	}

	private void setPlugin(InputStream in) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Document document = factory.newDocumentBuilder().parse(in);
		if (!PLUGIN_TAG.equals(document.getDocumentElement().getTagName())) {
			throw new Exception("plugin.xml is not valid");
		}
		NodeList list = document.getElementsByTagName(EXTENSION);
		extensions = new HashMap<String, List<IExtension>>();
		for (int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
			String point = element.getAttribute(POINT);
			if (!StringUtils.isEmpty(point)) {
				List<IExtension> exts = extensions.get(point);
				if (exts == null) {
					exts = new ArrayList<IExtension>();
					extensions.put(point, exts);
				}

				exts.add(new Extension(element));
			}
		}
	}

	class Extension implements IExtension {
		Element element;

		Extension(Element element) {
			this.element = element;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getConfigurationElements()
		 */
		public IConfigurationElement[] getConfigurationElements()
				throws InvalidRegistryObjectException {
			NodeList list = element.getChildNodes();
			List<IConfigurationElement> elements = new ArrayList<IConfigurationElement>();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node instanceof Element) {
					elements
							.add(new ConfigurationElement(this, (Element) node));
				}
			}

			return elements.toArray(new IConfigurationElement[elements.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getContributor()
		 */
		public IContributor getContributor()
				throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getDeclaringPluginDescriptor()
		 */
		public IPluginDescriptor getDeclaringPluginDescriptor()
				throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getExtensionPointUniqueIdentifier()
		 */
		public String getExtensionPointUniqueIdentifier()
				throws InvalidRegistryObjectException {
			return element.hasAttribute(POINT) ? element.getAttribute(POINT)
					: null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getLabel()
		 */
		public String getLabel() throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getNamespace()
		 */
		public String getNamespace() throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getNamespaceIdentifier()
		 */
		public String getNamespaceIdentifier()
				throws InvalidRegistryObjectException {
			return PlatformCorePlugin.getDefault().getBundle()
					.getSymbolicName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getSimpleIdentifier()
		 */
		public String getSimpleIdentifier()
				throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#getUniqueIdentifier()
		 */
		public String getUniqueIdentifier()
				throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IExtension#isValid()
		 */
		public boolean isValid() {
			throw new UnsupportedOperationException();
		}
	}

	class ConfigurationElement implements IConfigurationElement {
		Extension extension;
		Element element;

		ConfigurationElement(Extension extension, Element element) {
			this.extension = extension;
			this.element = element;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#createExecutableExtension(java.lang.String)
		 */
		public Object createExecutableExtension(String propertyName)
				throws CoreException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getAttribute(java.lang.String)
		 */
		public String getAttribute(String name)
				throws InvalidRegistryObjectException {
			return element.hasAttribute(name) ? element.getAttribute(name)
					: null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getAttributeAsIs(java.lang.String)
		 */
		public String getAttributeAsIs(String name)
				throws InvalidRegistryObjectException {
			return getAttribute(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getAttributeNames()
		 */
		public String[] getAttributeNames()
				throws InvalidRegistryObjectException {
			NamedNodeMap map = element.getAttributes();
			String[] names = new String[map.getLength()];
			for (int i = 0; i < names.length; i++) {
				names[i] = map.item(i).getNodeName();
			}
			return names;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getChildren()
		 */
		public IConfigurationElement[] getChildren()
				throws InvalidRegistryObjectException {
			NodeList list = element.getChildNodes();
			List<IConfigurationElement> children = new ArrayList<IConfigurationElement>();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node instanceof Element) {
					children.add(new ConfigurationElement(extension,
							(Element) node));
				}
			}
			return children.toArray(new IConfigurationElement[children.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getChildren(java.lang.String)
		 */
		public IConfigurationElement[] getChildren(String name)
				throws InvalidRegistryObjectException {
			List<IConfigurationElement> children = new ArrayList<IConfigurationElement>();
			if (name != null) {
				NodeList list = element.getChildNodes();
				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					if (node instanceof Element) {
						if (name.equals(node.getNodeName())) {
							children.add(new ConfigurationElement(extension,
									(Element) node));
						}
					}
				}
			}
			return children.toArray(new IConfigurationElement[children.size()]);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getContributor()
		 */
		public IContributor getContributor()
				throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getDeclaringExtension()
		 */
		public IExtension getDeclaringExtension()
				throws InvalidRegistryObjectException {
			return extension;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getName()
		 */
		public String getName() throws InvalidRegistryObjectException {
			return element.getNodeName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getNamespace()
		 */
		public String getNamespace() throws InvalidRegistryObjectException {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getNamespaceIdentifier()
		 */
		public String getNamespaceIdentifier()
				throws InvalidRegistryObjectException {
			return getSymbolicName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getParent()
		 */
		public Object getParent() throws InvalidRegistryObjectException {
			if (element.getParentNode() == extension.element) {
				return extension;
			}
			return new ConfigurationElement(extension, (Element) element
					.getParentNode());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getValue()
		 */
		public String getValue() throws InvalidRegistryObjectException {
			return element.getNodeValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#getValueAsIs()
		 */
		public String getValueAsIs() throws InvalidRegistryObjectException {
			return getValue();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.IConfigurationElement#isValid()
		 */
		public boolean isValid() {
			return true;
		}
	}
}
