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
package com.nokia.tools.media.svg;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class SvgImage {

	Map transcoderHints = new HashMap();

	RenderingHints renderingHints = null;

	private URL url;

	private BufferedImage svgAsBufferedImage = null;

	private String preserveAspectRatio = null;

	public static final String PRESERVE_ASPECT = "xMidYMid slice";

	public static final String PRESERVE_STRETCH = "none";
	public static final String STRETCH = "STRETCH";
	public static final String ASPECT_RATIO = "PRESERVE_ASPECT_RATIO_CROP_EXCEEDING";

	/**
	 * Default constructor
	 */
	public SvgImage() {
	}

	/**
	 * Constructor
	 * 
	 * @param svgFileName The name of the svg file to be loaded
	 * @throws IOException if the svg file load fails
	 */
	public SvgImage(URL url) throws IOException {
		setImage(url);
	}

	/**
	 * Constructor
	 * 
	 * @param svgFileName The name of the svg file to be loaded
	 * @param scalingMode The scaling mode that overrides the svg aspect ratio
	 *            setting.
	 * @throws IOException if the svg file load fails
	 */
	public SvgImage(URL url, String scalingMode) throws IOException {
		setImage(url);
		setScalingMode(scalingMode);
	}

	/**
	 * Constructor
	 * 
	 * @param svgFileName The name of the svg file to be loaded
	 * @param renderingHints The hints for rendering
	 * @throws IOException if the svg file load fails
	 */
	public SvgImage(URL url, RenderingHints renderingHints) throws IOException {
		setRenderingHints(renderingHints);
		setImage(url);
	}

	/**
	 * Constructor
	 * 
	 * @param svgFileName The name of the svg file to be loaded
	 * @param renderingHints The hints for rendering
	 * @param scalingMode The scaling mode that overrides the svg aspect ratio
	 *            setting.
	 * @throws IOException if the svg file load fails
	 */
	public SvgImage(URL url, RenderingHints renderingHints, String scalingMode)
			throws IOException {
		setRenderingHints(renderingHints);
		setImage(url);
		setScalingMode(scalingMode);
	}

	/**
	 * Sets the file containing the svg document (image)
	 * 
	 * @param svgFile The svg file name
	 */
	public void setImage(URL url) {
		this.url = url;
	}

	/**
	 * Returns the svg image as a bufferedImage object
	 * 
	 * @return The svg image as buffered image
	 */
	public BufferedImage getAsBufferedImage() throws Exception {

		if (svgAsBufferedImage == null) {
			if (svgAsBufferedImage == null) {
				TranscoderInput input = new TranscoderInput(url.toURI()
						.toString());
				RasterTranscoder transcoder = new RasterTranscoder(
						preserveAspectRatio);
				transcoderHints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, "http://www.w3.org/2000/svg");
			transcoderHints.put(XMLAbstractTranscoder.KEY_XML_PARSER_VALIDATING, false);
			
				transcoder.setTranscodingHints(transcoderHints);
				
				transcoder.transcode(input, null);
				
				
				svgAsBufferedImage = transcoder.getBufferedImage();
			}
		}

		return svgAsBufferedImage;
	}

	/**
	 * Sets the dimension of the rendered image. Also calls repaint once the
	 * dimension is set
	 * 
	 * @param imgWidth The width of the image
	 * @param imgHeight The height of the image
	 */
	public void setSize(int imgWidth, int imgHeight) {

		if (imgWidth > 0) {
			transcoderHints.put(ImageTranscoder.KEY_WIDTH, new Float(imgWidth));
		}

		if (imgHeight > 0) {
			transcoderHints.put(ImageTranscoder.KEY_HEIGHT,
					new Float(imgHeight));
		}

		/*
		 * If the width or height of the image is different from the size of the
		 * existing buffered image then set it to null (so that it will be
		 * regenerated when requested again)
		 */
		if (svgAsBufferedImage != null) {
			int bufImgHeight = svgAsBufferedImage.getHeight();
			int bugImgWidth = svgAsBufferedImage.getWidth();

			if (imgHeight > 0 && imgHeight != bufImgHeight)
				svgAsBufferedImage = null;

			if (imgWidth > 0 && imgWidth != bugImgWidth)
				svgAsBufferedImage = null;
		}
	}

	/**
	 * Sets the rendering hints
	 * 
	 * @param hints The rendering hints
	 */
	public void setRenderingHints(RenderingHints hints) {
		renderingHints = hints;
	}

	/**
	 * Sets the scaling mode for the image
	 * 
	 * @param scalingMode Sets the scaling mode.Currently supports
	 *            "PRESERVE_ASPECT_RATIO_CROP_EXCEEDING" and "STRETCH" modes.
	 */
	public void setScalingMode(String scalingMode) {
		String svgAspectRatioString = null;
		if (scalingMode != null) {
			scalingMode = scalingMode.trim();
		} else {
			scalingMode = "";
		}

		if (ASPECT_RATIO.equalsIgnoreCase(scalingMode)) {
			svgAspectRatioString = PRESERVE_ASPECT;
		} else if (STRETCH.equalsIgnoreCase(scalingMode))
			svgAspectRatioString = PRESERVE_STRETCH; // doesnt maintain the
		// svg aspect
		// ratio definition

		if (preserveAspectRatio != svgAspectRatioString) {
			preserveAspectRatio = svgAspectRatioString;
			svgAsBufferedImage = null; // need to reconstruct if the aspect
			// ratio setting has changed
		}
	}
}
