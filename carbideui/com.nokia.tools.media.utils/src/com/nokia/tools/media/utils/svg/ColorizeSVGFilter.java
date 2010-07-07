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
package com.nokia.tools.media.utils.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.svg.SVGDocument;

import com.nokia.tools.media.color.ColorUtil;
import com.nokia.tools.resource.util.XmlUtil;

/**
 * Replaces one color to another one in SVG file.
 */
public class ColorizeSVGFilter {

	public static final Pattern rgb_pattern = Pattern
			.compile("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})"); //$NON-NLS-1$

	public static void colorizeSVG(File input, File output, RGB color,
			int opacity) {
		byte[] buf = new byte[(int) input.length()];
		FileInputStream in;
		FileOutputStream out;
		try {
			in = new FileInputStream(input);
			in.read(buf);
			in.close();

			String str = new String(buf, "ascii");

			int idx = -1;
			while ((idx = str.indexOf("#", idx + 1)) >= 0) {
				RGB oldColor = parseColorString(str.substring(idx, Math.min(
						idx + 7, str.length())));
				if (oldColor != null) {
					float colorPartMultipier = Math.min(1f,
							((float) opacity) / 255f);
					float oldColorPartMultiplier = 1f - colorPartMultipier;

					int newRed = (int) ((((float) color.red) * colorPartMultipier) + (((float) oldColor.red) * oldColorPartMultiplier));
					int newGreen = (int) ((((float) color.green) * colorPartMultipier) + (((float) oldColor.green) * oldColorPartMultiplier));
					int newBlue = (int) ((((float) color.blue) * colorPartMultipier) + (((float) oldColor.blue) * oldColorPartMultiplier));
					RGB newColor = new RGB(newRed, newGreen, newBlue);

					byte[] newColorBytes = createColorString(newColor)
							.getBytes("ascii");

					System.arraycopy(newColorBytes, 0, buf, idx,
							newColorBytes.length);
				}
			}

			out = new FileOutputStream(output);
			out.write(buf);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void colorizeSVGChangeSingleColor(File input, File output, RGB changingColor,RGB changedColor, 
			int opacity) {
		byte[] buf = new byte[(int) input.length()];
		FileInputStream in;
		FileOutputStream out;
		try {
			in = new FileInputStream(input);
			in.read(buf);
			in.close();

			String str = new String(buf, "ascii");

			int idx = -1;
			while ((idx = str.indexOf("#", idx + 1)) >= 0) {
				RGB oldColor = parseColorString(str.substring(idx, Math.min(
						idx + 7, str.length())));
				if ((oldColor != null)&&(oldColor.equals(changedColor))) {
					float colorPartMultipier = Math.min(1f,
							((float) opacity) / 255f);
					float oldColorPartMultiplier = 1f - colorPartMultipier;

					int newRed = (int) ((((float) changingColor.red) * colorPartMultipier) + (((float) oldColor.red) * oldColorPartMultiplier));
					int newGreen = (int) ((((float) changingColor.green) * colorPartMultipier) + (((float) oldColor.green) * oldColorPartMultiplier));
					int newBlue = (int) ((((float) changingColor.blue) * colorPartMultipier) + (((float) oldColor.blue) * oldColorPartMultiplier));
					RGB newColor = new RGB(newRed, newGreen, newBlue);

					byte[] newColorBytes = createColorString(newColor)
							.getBytes("ascii");

					System.arraycopy(newColorBytes, 0, buf, idx,
							newColorBytes.length);
				}
			}

			out = new FileOutputStream(output);
			out.write(buf);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void colorizeSVGChangeSingleColor2( File input, File output, RGB changingColor,RGB changedColor, 
			int opacity) {	
		try {
			SVGDocument svgDoc=SvgUtil.parseSvg(input);
			SvgUtil.bootCSSandSVGDom(svgDoc);
			String oldColor=ColorUtil.asHashString(changedColor);
			String colorToBeReplaced=ColorUtil.asHashString(changingColor);
			SVGDocumentUtils.setColorAll(svgDoc,oldColor,colorToBeReplaced,false, true);	
			XmlUtil.write(svgDoc, output);
		} catch (Exception e) {
			
			e.printStackTrace();			
		}			
	
	}
	
	public static void colorizeSVGChangeGradientColor( File input, File output, RGB changingColor,RGB changedColor, 
			int opacity) {	
		try {
			SVGDocument svgDoc=SvgUtil.parseSvg(input);
			SvgUtil.bootCSSandSVGDom(svgDoc);
			String oldColor=ColorUtil.asHashString(changedColor);
			String colorToBeReplaced=ColorUtil.asHashString(changingColor);
			SVGDocumentUtils.setColor(svgDoc,"stop-color",oldColor,colorToBeReplaced,false);			
			XmlUtil.write(svgDoc, output);
		} catch (Exception e) {
		
			e.printStackTrace();			
		}			
	
	}
	
	
	
	

	public static RGB parseColorString(String color) {
		Matcher m = rgb_pattern.matcher(color);
		if (m.matches()) {
			String[] values = new String[] { m.group(1), m.group(2), m.group(3) };
			RGB rgb = new RGB(Integer.parseInt(values[0], 16), Integer
					.parseInt(values[1], 16), Integer.parseInt(values[2], 16));
			return rgb;
		} else {
			return null;
		}
	}
	
	public static List<RGB> getColors(File input){
		List<RGB> svgColors= new ArrayList<RGB>();
		byte[] buf = new byte[(int) input.length()];
		FileInputStream in;		
		try {
			in = new FileInputStream(input);
			in.read(buf);
			in.close();

			String str = new String(buf, "ascii");

			int idx = -1;
			while ((idx = str.indexOf("#", idx + 1)) >= 0) {
				RGB color = parseColorString(str.substring(idx, Math.min(
						idx + 7, str.length())));
				if (color != null){
					svgColors.add(color);
				}
			}			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return svgColors;
	}
	
	public static List<RGB> getColors2(File input){
		List<RGB> svgColors= new ArrayList<RGB>();
		try {
			SVGDocument svgDoc=SvgUtil.parseSvg(input);
			SvgUtil.bootCSSandSVGDom(svgDoc);			
			SvgColorComposite colorComposite=SVGDocumentUtils.getAllColors(svgDoc, false);
			List<SvgColor> colorList=colorComposite.getSvgColors();
			List<SvgGradient> gradientList=colorComposite.getSvgGradients();
			for(SvgColor color:colorList){
				svgColors.add(color.getRGB());
			}
			for(SvgGradient gradient:gradientList){
				if(gradient.isSVGLinearGradientElement()){
					SvgStopElement[] elements=gradient.getSVGStopElements();
					for(SvgStopElement stopElement:elements){
						svgColors.add(stopElement.getStopColorAsRGB());
					}
				}else if (gradient.isSVGRadialGradientElement()){
					SvgStopElement[] elements=gradient.getSVGStopElements();
					for(SvgStopElement stopElement:elements){
						svgColors.add(stopElement.getStopColorAsRGB());
					}
				}
			}
			
		} catch (Exception e) {
		
			e.printStackTrace();
			return null;
		} 
		return svgColors;
	}
	

	public static String createColorString(RGB rgb) {
		String red = Integer.toHexString(rgb.red);
		while (red.length() < 2) {
			red = "0" + red;
		}

		String green = Integer.toHexString(rgb.green);
		while (green.length() < 2) {
			green = "0" + green;
		}

		String blue = Integer.toHexString(rgb.blue);
		while (blue.length() < 2) {
			blue = "0" + blue;
		}

		return "#" + red + green + blue;
	}

	// /**
	// * @param args
	// */
	// public static void main(String[] args) {
	// if (args.length == 3) {
	// String input = args[0];
	// String color = args[1];
	// String output = args[2];
	// colorizeSVG(new File(input).getAbsoluteFile(), new File(output)
	// .getAbsoluteFile(), parseColorString(color), 128);
	// } else {
	// System.out
	// .println("Usage: java ColorizeSVGFilter inputFile #rrggbb outputFile");
	//
	// colorizeSVG(new File("test.svg").getAbsoluteFile(), new File(
	// "colorized.svg").getAbsoluteFile(),
	// parseColorString("#00ff00"), 128);
	// }
	// }
}