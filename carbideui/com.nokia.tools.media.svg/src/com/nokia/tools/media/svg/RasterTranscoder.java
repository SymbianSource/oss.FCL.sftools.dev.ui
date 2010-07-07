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


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.StringTokenizer;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *         This class is used to convert the SVG image to
 *         BufferedImage.
 */
class RasterTranscoder extends ImageTranscoder {
	public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

	/*
	 * This is not an efficient way of creating the bufferedimage. However the
	 * alternative - of implementing a logic similar to transcoder (with all its
	 * transforms) is a complex and un-maintainable task.
	 * 
	 * The logic of this program is to simply extend the ImageTranscoder class
	 * available in batik. (Transcoder is the batik api provided to convert
	 * images from svg format to other formats like bmp, png tiff, jpeg).
	 * 
	 * In the writeImage function (one of the must functions for implementing
	 * ImageTranscoder), i have simply copied the input svg image's buffered
	 * image (as transcoded by the transcoder) to a local class data member).
	 * 
	 */

	static final String PRESET_ATTR = "preserveAspectRatio";

	static final String PRESET_ATTR_SLICE = "xMidYMid slice";

	static final String ATTR_NONE = "none";

	static final String ATTR_VIEWBOX = "viewBox";

	private boolean customSlice;

	private float offX, offY, viewportW, viewportH;

	private String preserveAspectRatio;

	private BufferedImage image;

	/**
	 * Constructor
	 */
	public RasterTranscoder() {
	}

	/**
	 * Constructor Overrides the aspect ratio definition in the svg document
	 * with the one specified in this parameter
	 * 
	 * @param preserveAspectRatio The svg aspect ratio setting. Should be one of
	 *            the allowed svg settings as sepecified in the svg
	 *            specification. Reference :
	 *            http://www.w3.org/TR/SVG11/coords.html#PreserveAspectRatioAttribute
	 */
	public RasterTranscoder(String preserveAspectRatio) {
		this.preserveAspectRatio = preserveAspectRatio;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.batik.transcoder.image.ImageTranscoder#createImage(int,
	 *      int)
	 */
	public BufferedImage createImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.batik.transcoder.image.ImageTranscoder#writeImage(java.awt.image.BufferedImage,
	 *      org.apache.batik.transcoder.TranscoderOutput)
	 */
	public void writeImage(BufferedImage img, TranscoderOutput output)
			throws TranscoderException {
		image = img;
	}

	/**
	 * @return Returns the buffered image for the input svg file.
	 */
	public BufferedImage getBufferedImage() {

		if (customSlice) {
			
			BufferedImage bim = new BufferedImage((int) viewportW,
					(int) viewportH, BufferedImage.TYPE_INT_ARGB);
			bim.getGraphics().drawImage(
					image.getSubimage((int) offX, (int) offY, (int) viewportW,
							(int) viewportH), 0, 0, COLOR_TRANSPARENT, null);
			return bim;
		}
		return image;
	}

	/**
	 * Transcodes the specified Document as an image in the specified output.
	 * 
	 * @param document the document to transcode
	 * @param uri the uri of the document or null if any
	 * @param output the ouput where to transcode
	 * @exception TranscoderException if an error occured while transcoding
	 */
	protected void transcode(Document doc, String uri, TranscoderOutput output)
			throws TranscoderException {
		Element svgElem = doc.getDocumentElement();
		/*
		 * Overridding the aspect ratio setting here 1. If the aspectRatio
		 * object is null then no setting is done 2. If the aspectRatio is
		 * explicitly set then we use the data set in the object
		 */
		if (preserveAspectRatio != null) {
			if (PRESET_ATTR_SLICE.equals(preserveAspectRatio)) {
				
				try {
					if (doc.getDocumentElement().hasAttribute(ATTR_VIEWBOX)) {
						String viewBox = doc.getDocumentElement().getAttribute(
								ATTR_VIEWBOX);
						StringTokenizer st = new StringTokenizer(viewBox, " ");
						st.nextToken();
						st.nextToken();

						float imagew = Float.parseFloat(st.nextToken());
						float imageh = Float.parseFloat(st.nextToken());

						Float f = (Float) getTranscodingHints().get(
								ImageTranscoder.KEY_WIDTH);
						if (f != null) {
							viewportW = f;
						}
						f = (Float) getTranscodingHints().get(
								ImageTranscoder.KEY_HEIGHT);
						if (f != null) {
							viewportH = f;
						}

						if (imagew != viewportW) {
							float ratio = imagew / viewportW;
							imagew = imagew / ratio;
							imageh = imageh / ratio;
						}
						if (imageh < viewportH) {
							float ratio = imageh / viewportH;
							imagew = imagew / ratio;
							imageh = imageh / ratio;
						}

						offX = (int) ((imagew - viewportW) / 2);
						offY = (int) ((imageh - viewportH) / 2);

						
						width = imagew;
						height = imageh;
						if (imagew > 0) {
							addTranscodingHint(ImageTranscoder.KEY_WIDTH,
									imagew);
						}
						if (imageh > 0) {
							addTranscodingHint(ImageTranscoder.KEY_HEIGHT,
									imageh);
						}
						svgElem.setAttribute(PRESET_ATTR, ATTR_NONE);

						customSlice = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			svgElem.setAttribute(PRESET_ATTR, preserveAspectRatio);
		}

		super.transcode(doc, uri, output);
	}

}