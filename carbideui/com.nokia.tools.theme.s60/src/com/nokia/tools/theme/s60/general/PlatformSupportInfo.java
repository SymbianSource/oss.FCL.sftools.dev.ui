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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.platform.core.DevicePlatform;
import com.nokia.tools.platform.core.IPlatform;
import com.nokia.tools.platform.extension.PlatformExtensionManager;
import com.nokia.tools.resource.util.DebugHelper;
import com.nokia.tools.theme.s60.S60ThemePlugin;
import com.nokia.tools.theme.s60.general.PlatformSuppportInfoProvider.PlatformSupportExtension;

public class PlatformSupportInfo {
	private static final String DOC_PATH = "/doc/IDs_supported.xml";

	private static final String NOT_SUPPORTED = "N";

	private static Map<String, Set<String>> unsupportedPlatforms;

	private static Map<String, Map<String, String>> helps = new HashMap<String, Map<String, String>>();

	public static boolean isPlatformSupported(String id, IPlatform platform) {
		//init();
		neoInit();

		if (DevicePlatform.UNSPECIFIED_PLATFORM == platform) {
			return true;
		}

		boolean isValid = false;
		for (IPlatform p : PlatformExtensionManager.getPlatforms()) {
			if (p.getId().equals(platform.getId())) {
				isValid = true;
				break;
			}
		}
		if (!isValid) {
			return false;
		}
		
		Set<String> suppPlatforms = supportedPlatforms.get(id);
		
		boolean oldSupported = false, newSupported = false;
		
		if(backwardCompatibilityEnabled){
			oldSupported = isPlatformSupported(id, platform.getId());
		}
		
		if(suppPlatforms !=null){
			newSupported = suppPlatforms.contains(platform.getId());
		}

		return oldSupported | newSupported;
	}
	
	
	private static boolean isPlatformSupported(String id, String platformId) {
		Set<String> platforms = unsupportedPlatforms.get(id);
		if (platforms == null) {
			return false;
		}
		return !platforms.contains(platformId);
	}

	public static String getHelp(String id, IPlatform platform) {
		//init();
		neoInit();
		Map<String, String> idHelps = helps.get(id);
		if (idHelps == null) {
			return null;
		}
		return idHelps.get(platform.getId());
	}

	private static boolean  backwardCompatibilityEnabled = false;
	private synchronized static void init() {
		if(backwardCompatibilityEnabled){
			return;
		}
		backwardCompatibilityEnabled = true;
		unsupportedPlatforms = new HashMap<String, Set<String>>();		
		InputStream in = null;
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();

			// Parse the input
			SAXParser saxParser = parserFactory.newSAXParser();

			ArrayList<URL> documentPathList = PlatformSuppportInfoProvider
					.getDocumentPath();
			if (documentPathList.size() > 0) {
				for (URL document : documentPathList) {
					in = document.openStream();
					saxParser.parse(in, new PlatformSupportHandler());
					in.close();
				}
			} else {

				in = PlatformSupportInfo.class.getResourceAsStream(DOC_PATH);
				saxParser.parse(in, new PlatformSupportHandler());
			}
		} catch (Exception e) {
			e.printStackTrace();
			S60ThemePlugin.error(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
			}
		}
		
	
	}
	
	private static int ptfmCount = 0;
	private static boolean sPtfmUpdated(){
		if(PlatformExtensionManager.getPlatforms().length != ptfmCount){
			return false;
		}
		return true;
	}
	
	private static Map<String, Set<String>>  supportedPlatforms;
	
	public synchronized static void neoInit(){
		if (sPtfmUpdated() && (supportedPlatforms != null)) {
			return;
		}
		
		supportedPlatforms = new HashMap<String, Set<String>>();
		/*
		 * populate the map from the supported id information available form the
		 * extensions in plug-ins.
		 */
		BufferedReader bufReader = null;
		ArrayList<PlatformSupportExtension> extensions = PlatformSuppportInfoProvider
				.getExtensions();
		if (extensions.size() == 0) {
			init();
			return;
		}
		boolean extnExists = false;
		try {
			for (PlatformSupportExtension pSExtension : extensions) {
				if (pSExtension.getDocumentPath() != null) {
					bufReader = new BufferedReader(new InputStreamReader(
							pSExtension.getDocumentPath().openStream()));
					process(bufReader, pSExtension.getPlatformId());
					bufReader.close();
					extnExists = true;
				} else {
					init();
				}
			}
		} catch (IOException e) {
			
		} finally {
			if (bufReader != null) {
				try {
					bufReader.close();
				} catch (IOException e) {

				}
			}
		}
		
		if(!extnExists){
			init();
			return;
		}
		
		ptfmCount = PlatformExtensionManager.getPlatforms().length;
	}
	
	private static void process(BufferedReader bufReader, String platformId)
			throws IOException {
		if (bufReader != null) {
			String line = bufReader.readLine();
			while ((line = bufReader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length > 2 || tokens.length < 1) {
					S60ThemePlugin.warn("Supported ID file is corrupt");
					break;
				}
				String elementID = (String) tokens[0];
				if (supportedPlatforms.containsKey(elementID)) {
					supportedPlatforms.get(elementID).add(platformId);
				} else {
					HashSet<String> platforms = new HashSet<String>();
					platforms.add(platformId);
					supportedPlatforms.put(elementID, platforms);
				}
				
				String help = null;
				if (tokens.length == 1) {
					help = "";
				} else {
					help = (String) tokens[1];
				}
				
				if (helps.containsKey(elementID)) {
					helps.get(elementID).put(platformId, help);
				} else {
					HashMap<String, String> pHelps = new HashMap<String, String>();
					pHelps.put(platformId, help);
					helps.put(elementID, pHelps);
				}
			}
		}
	}
	
	public static void processOneItem(String id, String content26,
			String content28, String content30, String content31,
			String content32, String content50, String help2_6, String help2_8,
			String help3, String help3_1, String help3_2, String help5_0) {
		if (id == null) {
			if (DebugHelper.debugParser()) {
				DebugHelper.debug(PlatformSupportInfo.class,
						"Empty id occurence");
			}
		}

		Set<String> platforms = new HashSet<String>();
		if (NOT_SUPPORTED.equalsIgnoreCase(content50)) {
			platforms.add(DevicePlatform.S60_5_0.getId());
		}

		if (unsupportedPlatforms.put(id, platforms) != null) {
			if (DebugHelper.debugParser()) {
				DebugHelper.debug(PlatformSupportInfo.class,
						"Overridding existing id: " + id);
			}
		}

		Map<String, String> idHelps = new HashMap<String, String>();

		idHelps.put(DevicePlatform.S60_5_0.getId(), help5_0);

		helps.put(id, idHelps);
	}

}

class PlatformSupportHandler extends DefaultHandler {

	String id;

	String p2_6;

	String p2_8;

	String p3;

	String p3_1;

	String p3_2;

	String p5_0;

	String help2_6 = "";

	String help2_8 = "";

	String help3 = "";

	String help3_1 = "";

	String help3_2 = "";

	String help5_0 = "";

	String term;

	final String ITEM = "item";

	final String ID = "id";

	final int ID_TAG = 0;

	final String P2_6 = "p2_6";

	final int P2_6_TAG = 1;

	final String P2_8 = "p2_8";

	final int P2_8_TAG = 2;

	final String P3 = "p3";

	final int P3_TAG = 3;

	final String P3_1 = "p3_1";

	final int P3_1_TAG = 4;

	final String P3_2 = "p3_2";

	final int P3_2_TAG = 10;

	final String P5_0 = "p5_0";// newly added

	final int P5_0_TAG = 12;// newly added

	final String HELP_2_6 = "hlp2_6";

	final int HELP_2_6TAG = 5;

	final String TERM = "term";

	final int TERM_TAG = 6;

	final String HELP_2_8 = "hlp2_8";

	final int HELP_2_8TAG = 7;

	final String HELP_3 = "hlp3";

	final int HELP_3TAG = 8;

	final String HELP_3_1 = "hlp3_1";

	final int HELP_3_1TAG = 9;

	final String HELP_3_2 = "hlp3_2";

	final int HELP_3_2TAG = 11;

	final String HELP_5_0 = "hlp5_0";

	final int HELP_5_0TAG = 13;

	int currentItemTag = -1;

	int counter = 0; // counter for debug

	public void startElement(String namespaceURI, String lName, // local name
			String qName, // qualified name
			Attributes attrs) throws SAXException {

		if (ID.equals(qName)) {
			id = "";
			currentItemTag = ID_TAG;
		} else if (P2_6.equals(qName)) {
			p2_6 = "";
			currentItemTag = P2_6_TAG;
		} else if (P2_8.equals(qName)) {
			p2_8 = "";
			currentItemTag = P2_8_TAG;
		} else if (P3.equals(qName)) {
			p3 = "";
			currentItemTag = P3_TAG;
		} else if (P3_1.equals(qName)) {
			p3_1 = "";
			currentItemTag = P3_1_TAG;
		} else if (P3_2.equals(qName)) {
			p3_2 = "";
			currentItemTag = P3_2_TAG;
		} else if (P5_0.equals(qName)) {
			p5_0 = "";
			currentItemTag = P5_0_TAG;
		} else if (HELP_2_6.equals(qName)) {
			currentItemTag = HELP_2_6TAG;
		} else if (TERM.equals(qName)) {
			currentItemTag = TERM_TAG;
		} else if (HELP_2_8.equals(qName)) {
			currentItemTag = HELP_2_8TAG;
		} else if (HELP_3.equals(qName)) {
			currentItemTag = HELP_3TAG;
		} else if (HELP_3_1.equals(qName)) {
			currentItemTag = HELP_3_1TAG;
		} else if (HELP_3_2.equals(qName)) {
			currentItemTag = HELP_3_2TAG;
		} else if (HELP_5_0.equals(qName)) {
			currentItemTag = HELP_5_0TAG;
		} else {
			currentItemTag = -1;
		}

	}

	final public void characters(final char[] ch, final int start, final int len) {
		String text = new String(ch, start, len);
		String trimmedString = text.trim();
		boolean isEmpty = trimmedString.length() == 0 ? true : false;
		if (isEmpty) {
			return;
		}

		switch (currentItemTag) {
		case ID_TAG:
			counter++;
			id += trimmedString;
			break;
		case P2_6_TAG:
			p2_6 += trimmedString;
			break;
		case P2_8_TAG:
			p2_8 += trimmedString;
			break;
		case P3_TAG:
			p3 += trimmedString;
			break;
		case P3_1_TAG:
			p3_1 += trimmedString;
			break;
		case P3_2_TAG:
			p3_2 += trimmedString;
			break;
		case P5_0_TAG:
			p5_0 += trimmedString;
			break;
		case HELP_2_6TAG:
			help2_6 += text;
			break;
		case TERM_TAG:
			term += text;
			break;
		case HELP_2_8TAG:
			help2_8 += text;
			break;
		case HELP_3TAG:
			help3 += text;
			break;
		case HELP_3_1TAG:
			help3_1 += text;
			break;
		case HELP_3_2TAG:
			help3_2 += text;
			break;
		case HELP_5_0TAG:
			help5_0 += text;
			break;
		default:
			break;

		}
	}

	final public void endElement(final String namespace,
			final String localname, final String type) {
		if (ITEM.equals(type)) {
			PlatformSupportInfo.processOneItem(id, p2_6, p2_8, p3, p3_1, p3_2,
					p5_0, help2_6, help2_8, help3, help3_1, help3_2, help5_0); 
			help5_0 = help3_2 = help3_1 = help3 = help2_8 = help2_6 = "";
		}
	}

}
