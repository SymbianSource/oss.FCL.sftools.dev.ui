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
package com.nokia.tools.media.utils.svg2svgt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.Document;

import com.nokia.svg2svgt.SVG2SVGTConstants;
import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.svg.SvgException;
import com.nokia.tools.media.utils.svg.SvgUtil;
import com.nokia.tools.resource.util.ResourceUtils;
import com.nokia.tools.resource.util.XmlUtil;

public class SVGTUtil {
	public static final String OPT_DIMENSION = SVG2SVGTConstants.DIMENSION_OPTION;

	public static final String DIMENSION_NORMAL = "normal";

	private static final String DEFAULT_CONVERSIONSFILE = ResourceUtils
			.getPluginFile(UtilsPlugin.getDefault(),
					"/data/config/Conversions.properties").getAbsolutePath();

	private static final String DEFAULT_PASSTHROUGHFILE = ResourceUtils
			.getPluginFile(UtilsPlugin.getDefault(),
					"/data/config/passThrough.xml").getAbsolutePath();
	static {
		String dataDir = ResourceUtils.getPluginFile(UtilsPlugin.getDefault(),
				"/data").getAbsolutePath();
		ResourceBundle bundleTemplate = ResourceBundle
				.getBundle("SVG2SVGTPropertiesTemplate");
		Properties destBundle = new Properties();
		Enumeration<String> keys = bundleTemplate.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			String value = bundleTemplate.getString(key);
			destBundle.put(key,
					new File(value.replace("${datadir}", dataDir).replace(
							"${tmpdir}", System.getProperty("java.io.tmpdir")))
							.toString());
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(new File(dataDir,
					"config/SVG2SVGTProperties.properties"));
			destBundle.store(out,
					"Generated from SVG2SVGTPropertiesTemplate.properties");
			out.flush();
		} catch (Exception e) {
			UtilsPlugin.error(e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Wrapper function to ocnvert SVG to SVGT
	 * 
	 * @param inputStream
	 *            SVG InputStream
	 * @param outputStream
	 *            SVG OutputStream
	 * @return
	 * @throws SVGUtilException
	 */
	@SuppressWarnings("unchecked")
	public static void convertToSVGT(InputStream inputStream,
			OutputStream outputStream, Map<String, String> options)
			throws SvgException {
		try {
			Document src = SvgUtil.parseSvg(inputStream);
			convertToSVGT(src, outputStream, options);

		} catch (SvgException e) {
			throw e;
		} catch (Throwable e) {
			throw new SvgException(e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
			}
		}

	}

    public static org.w3c.dom.Document getDocument(SVGOMDocument src) throws Exception {
        // Create a simple DOM Document.
        javax.xml.parsers.DocumentBuilderFactory factory = 
            javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = DOMUtilities.deepCloneDocument(src,builder.getDOMImplementation());
        return doc;
    }

	
	public static void convertToSVGT(Document src, OutputStream outputStream,
			Map<String, String> options) throws SvgException {
		try {
			Document dst = convertToSVGT(src, options);			
			XmlUtil.write(dst, outputStream);
		} catch (SvgException e) {
			throw e;
		} catch (Throwable e) {
			throw new SvgException(e);
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static Document convertToSVGT(Document src,
			Map<String, String> options) throws SvgException {
		Hashtable<String, String> opts = new Hashtable<String, String>();
		if (options != null) {
			for (String key : options.keySet()) {
				String value = options.get(key);
				if (value != null) {
					opts.put(key, value);
				}
			}
		}

		if (!opts.containsKey(SVG2SVGTConstants.CONVERSIONSFILE_OPTION)) {
			opts.put(SVG2SVGTConstants.CONVERSIONSFILE_OPTION,
					DEFAULT_CONVERSIONSFILE);
		}
		if (!opts.containsKey(SVG2SVGTConstants.PASSTHROUGHFILE_OPTION)) {
			opts.put(SVG2SVGTConstants.PASSTHROUGHFILE_OPTION,
					DEFAULT_PASSTHROUGHFILE);
		}
		if (!opts.containsKey(SVG2SVGTConstants.CRITICAL_SIZE)) {
			opts.put(SVG2SVGTConstants.CRITICAL_SIZE, Long
					.toString(Long.MAX_VALUE));
		}

		try {
			Document doc = new SVGTController(src, opts).doConvert();
			String version = System.getProperty("java.version");
			char minor = version.charAt(2);
		    if (minor == '6') {		    
				if (doc instanceof SVGOMDocument) {
					doc = getDocument((SVGOMDocument)doc);
				}
		    }
			return doc;
		} catch (Throwable e) {
			UtilsPlugin.error(e);
			throw new SvgException(e);
		}
	}
}
