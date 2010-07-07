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

package com.nokia.tools.packaging.commandline.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nokia.tools.packaging.commandline.Packager;
import com.nokia.tools.packaging.commandline.PackagerMessages;
import com.nokia.tools.resource.util.FileUtils;

/**
 * This class parses the arguments and create the complete list after loading
 * the defaults from the config file in this tool
 * 
 * @author surmathe
 */
public class CommandParser {

	private LinkedHashMap<String, String> parameterHashStore;

	private void populateLinkedHashMap(List<Parameter> paramList)
			throws CommandLineException {
		parameterHashStore = new LinkedHashMap<String, String>(paramList.size());
		for (Parameter parameter : paramList) {
			if (parameterHashStore.containsKey(parameter.getStrSwitch())) {
				throw new CommandLineException(
						PackagerMessages.Packager_Error_Duplicate_Parameter
								+ parameter.getStrSwitch());
			}
			parameterHashStore.put(parameter.getStrSwitch(), parameter
					.getStrValue());
		}
	}

	/**
	 * gets the Flag value
	 * 
	 * @param strSwitch
	 * @return
	 * @throws MissingParameterException
	 */
	public String getFlagValue(String strSwitch)
			throws MissingParameterException {
		if (parameterHashStore.containsKey(strSwitch))
			return parameterHashStore.get(strSwitch);
		else {
			PackagerMessages
					.printMessageOnConsole(PackagerMessages.Packager_Commandline
							+ PackagerMessages.Packager_Error
							+ " "
							+ PackagerMessages.Packager_Error_Missing_Parameter
							+ strSwitch);
			throw new MissingParameterException(
					PackagerMessages.Packager_Command);
		}
	}

	/**
	 * builds the command list from the string array and the config file. based
	 * on precedence rules.
	 * 
	 * @param args
	 * @return
	 * @throws CommandLineException
	 */
	public List<Parameter> buildParameterList(String args[],
			CMDProgressMonitor progressBar) throws CommandLineException {
		List<Parameter> parameterList = new ArrayList<Parameter>();
		progressBar.setTaskName("buildParameterList");
		String arg = null;
		String value = "";
		try {
			if (args.length == 0)
				throw new ArrayIndexOutOfBoundsException();

			String[] flag = PackagerMessages.Packager_Flags.split("\\s+|,");

			for (int i = 0; i < args.length; i++) {
				arg = args[i].toLowerCase();
				i++;
				if (Arrays.asList(flag).contains(arg)) {
					value = args[i];
				} else {
					PackagerMessages
							.printMessageOnConsole(PackagerMessages.Packager_Commandline
									+ PackagerMessages.Packager_Error
									+ " "
									+ PackagerMessages.Packager_Flag_invalid
									+ arg);
					throw new CommandLineException(
							PackagerMessages.Packager_Command);
				}
				Parameter parameter = new Parameter(arg.toString(), value);
				parameterList.add(parameter);
			}
		} catch (ArrayIndexOutOfBoundsException exception) {
			throw new CommandLineException(PackagerMessages.Packager_Command);
		} finally {
			// progressBar.addWorked(5);
		}
		addConfigAndDefaultValues(parameterList);
		populateLinkedHashMap(parameterList);
		// progressBar.addWorked(2);
		return parameterList;
	}

	private void addConfigAndDefaultValues(List<Parameter> parameterList)
			throws CommandLineException {
		// may need in future
		// for (Parameter parameter : parameterList) {
		// if (PackagerMessages.Packager_Flag_ConfigFile
		// .equalsIgnoreCase(parameter.getStrSwitch())) {
		// parameterList.remove(parameter);
		// populateConfigValues(parameterList, parameter.getStrValue());
		// return;
		// }
		// }
		populateConfigValues(parameterList,
				PackagerMessages.Packager_DefaultConfigFile);
	}

	private void populateConfigValues(List<Parameter> parameterList,
			String configFileName) throws CommandLineException {
		InputStream in = null;
		try {
			URL url = FileUtils.getURL(Platform.getBundle(Packager.PLUGIN_ID),
					configFileName);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = documentBuilderFactory
					.newDocumentBuilder();
			in = url.openStream();
			Document document = docBuilder.parse(in);
			NodeList nodeList = document
					.getElementsByTagName("command-parameter");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Parameter parameter = new Parameter(
						((Element) nodeList.item(i)).getAttribute("switch"),
						((Element) nodeList.item(i)).getAttribute("value"));
				if (!parameterList.contains(parameter)) {
					parameterList.add(parameter);
				}
			}

		} catch (ParserConfigurationException e) {
			throw new CommandLineException(
					PackagerMessages.Packager_Error_ConfigFileCorrupted);
		} catch (SAXException e) {
			throw new CommandLineException(
					PackagerMessages.Packager_Error_ConfigFileCorrupted);
		} catch (IOException e) {
			throw new CommandLineException(
					PackagerMessages.Packager_Error_ConfigFileCorrupted);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
	}
}
