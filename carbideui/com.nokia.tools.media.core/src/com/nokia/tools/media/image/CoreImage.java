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

package com.nokia.tools.media.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import com.nokia.tools.media.player.IPaintAdapter;
import com.nokia.tools.media.player.IPlayer;
import com.nokia.tools.media.player.PlayerExtensionManager;
import com.nokia.tools.resource.util.FileUtils;

public class CoreImage
    implements Cloneable {
	public static final AffineTransform TRANSFORM_ORIGIN = AffineTransform
	    .getTranslateInstance(0, 0);
	/**
	 * Stretch mode: no stretching
	 */
	public static final int KEEP_ORIGINAL = 0;
	/**
	 * Stretch in both x and y to fit the target area, using INTERP_NEAREST
	 */
	public static final int STRETCH = 1;
	/**
	 * Stretch to fit and keep aspect ratio
	 */
	public static final int SCALE_TO_FIT = 2;
	/**
	 * Same as {@link #SCALE_TO_FIT} but only if image is bigger than the target
	 * area
	 */
	public static final int SCALE_DOWN_TO_FIT = 3;
	/**
	 * Stretch to biggest size and keep aspect ratio
	 */
	public static final int SCALE_TO_BEST = 4;
	/**
	 * Similar to {@link #SCALE_DOWN_TO_FIT}, but also scales up the small
	 * image
	 */
	public static final int SCALE_UP_TO_FIT = 5;

	public static final String TYPE_PNG = "png";
	public static final String TYPE_BMP = "bmp";
	public static final String TYPE_TIFF = "tiff";

	protected RenderedImage awt;
	protected Image swt;

	public CoreImage() {
	}

	public CoreImage(RenderedImage awt) {
		setAwt(awt);
	}

	public CoreImage(Image swt) {
		setSwt(swt);
	}

	public CoreImage newInstance() {
		return new CoreImage();
	}

	public CoreImage newInstance(RenderedImage awt) {
		return new CoreImage(awt);
	}

	public CoreImage newInstance(Image swt) {
		return new CoreImage(swt);
	}

	public CoreImage copy() {
		return (CoreImage) clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(e);
		}
	}

	public CoreImage load(File file) throws Exception {
		return load(FileUtils.toURL(file));
	}

	public CoreImage load(File file, int width, int height) throws Exception {
		return load(FileUtils.toURL(file), width, height);
	}

	public CoreImage load(File file, int width, int height, int scaleMode)
	    throws Exception {
		return load(FileUtils.toURL(file), width, height, scaleMode, false);
	}

	public CoreImage load(File file, int width, int height, int scaleMode,
	    boolean adjustSize) throws Exception {
		return load(FileUtils.toURL(file), width, height, scaleMode, adjustSize);
	}

	public CoreImage load(URL url) throws Exception {
		return load(url, -1, -1);
	}

	public CoreImage load(URL url, int width, int height) throws Exception {
		return load(url, width, height, KEEP_ORIGINAL);
	}

	public CoreImage load(URL url, int width, int height, int scaleMode)
	    throws Exception {
		return load(url, width, height, scaleMode, false);
	}

	public CoreImage load(URL url, int width, int height, int scaleMode,
	    boolean adjustSize) throws Exception {
		LoaderContext context = new LoaderContext();
		context.setUrl(url);
		context.setWidth(width);
		context.setHeight(height);
		context.setScaleMode(scaleMode);
		context.setAdjustSize(adjustSize);
		return load(context);
	}

	public CoreImage load(LoaderContext context) throws Exception {
		if (context.getUrl() == null) {
			throw new NullPointerException("URL is null.");
		}

		String urlString = context.getUrl().toExternalForm();
		String extension = FileUtils.getExtension(urlString);

		if (null == extension) {
			throw new NullPointerException("Extension is null: " + urlString);
		}

		// first tries the extension
		if (!FileUtils.resourceExists(context.getUrl())) {
			throw new IllegalArgumentException("URL resource doesn't exist: "
			    + urlString);
		}

		IImageLoader loader = ImageExtensionManager
		    .getLoaderByExtension(extension);
		if (loader != null) {
			loader.load(this, context);
			return this;
		}

		BufferedImage buf = null;
		if (ImageIO.getImageReadersBySuffix(extension).hasNext()) {

			// ImageIO.read has performance problem with certain jpeg files
			// (defect 997),
			// and JAI.create is not closing the stream in time or not at
			// all in multi-threaded environments (1178), for safety reason,
			// we use ImageIO.read and hopefully the performance will be
			// improved later by jvm.
			buf = ImageIO.read(context.getUrl());
		} else {
			if (context.getWidth() > 0 && context.getHeight() > 0) {
				IPlayer player = PlayerExtensionManager
				    .createPlayerByExtension(FileUtils.getExtension(urlString));
				if (player instanceof IPaintAdapter) {
					player.load(context.getUrl());

					buf = new BufferedImage(context.getWidth(), context
					    .getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics g = ((BufferedImage) buf).getGraphics();
					g.setClip(0, 0, context.getWidth(), context.getHeight());
					((IPaintAdapter) player).paint(null, g);
					g.dispose();
				}
			}
		}
		setAwt(buf);
		stretch(context);
		return this;
	}

	public CoreImage stretch(LoaderContext context) {
		if (CoreImage.KEEP_ORIGINAL != context.getScaleMode()
		    && context.getWidth() > 0 && context.getHeight() > 0) {
			// should pass the adjustSize to stretching, but it may break.
			stretch(context.getWidth(), context.getHeight(), context
			    .getScaleMode());
		}
		return this;
	}

	/**
	 * @return the awt
	 */
	public final RenderedImage getAwt() {
		if (awt == null && swt != null) {
			awt = convert(swt);
		}
		return awt;
	}

	/**
	 * @param awt the awt to set
	 */
	public final void setAwt(RenderedImage awt) {
		if (this.awt != awt) {
			dispose();
		}
		this.awt = awt;
	}

	/**
	 * @return the swt
	 */
	public final Image getSwt() {
		if (swt == null && awt != null) {
			swt = convert(awt);
		}
		return swt;
	}

	/**
	 * @param swt the swt to set
	 */
	public final void setSwt(Image swt) {
		if (this.swt != swt) {
			dispose();
		}
		this.swt = swt;
	}

	public CoreImage init(Image swt) {
		setSwt(swt);
		return this;
	}

	public CoreImage init(RenderedImage awt) {
		setAwt(awt);
		return this;
	}

	public CoreImage init(int width, int height, Color c) {
		return init(getBlankImage(width, height, c));
	}

	public CoreImage init(int width, int height, Color c, int opacity) {
		return init(getBlankImage(width, height, c, opacity));
	}

	public CoreImage init(int width, int height, Color c, int opacity, int bands) {
		return init(getBlankImage(width, height, c, opacity, bands));
	}

	public int getWidth() {
		if (awt != null) {
			return awt.getWidth();
		}
		if (swt != null) {
			return swt.getBounds().width;
		}
		return 0;
	}

	public int getHeight() {
		if (awt != null) {
			return awt.getHeight();
		}
		if (swt != null) {
			return swt.getBounds().height;
		}
		return 0;
	}

	public final BufferedImage getBufferedImage() {
		RenderedImage image = getAwt();

		if (image == null) {
			return null;
		}
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		BufferedImage bi = convertToBuffered(image);
		if (bi != null) {
			return bi;
		}
		bi = new BufferedImage(image.getWidth(), image.getHeight(),
		    BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		g.drawRenderedImage(image, TRANSFORM_ORIGIN);
		g.dispose();
		return bi;
	}

	protected BufferedImage convertToBuffered(RenderedImage image) {
		return null;
	}

	public final int getNumBands() {
		RenderedImage image = getAwt();
		if (image == null) {
			return 0;
		}
		int bands = getNumBands(image);
		if (bands > 0) {
			return bands;
		}
		return image.getData().getNumBands();
	}

	protected int getNumBands(RenderedImage image) {
		return image.getData().getNumBands();
	}

	protected final CoreImage execute(ImageWork work) {
		if (work != null) {
			work.execute();
		}
		return this;
	}

	public CoreImage applyBackground(final Color color) {
		return execute(new AWTWork() {
			public RenderedImage process(RenderedImage image) {
				BufferedImage bi = new BufferedImage(image.getWidth(), image
				    .getHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = (Graphics2D) bi.getGraphics();
				g.setColor(color);
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
				g.drawRenderedImage(image, TRANSFORM_ORIGIN);
				g.dispose();
				return bi;
			}
		});
	}

	public CoreImage applyMask(CoreImage maskImage, boolean isInverted) {
		return applyMask(maskImage, isInverted, false);
	}

	public CoreImage applyMask(final CoreImage maskImage,
	    final boolean isInverted, final boolean cascade) {
		return execute(new AWTWork() {
			public RenderedImage process(RenderedImage image) {
				// if(entityImage.getNumBands()==4)
				// return entityImage;

				if (maskImage == null) {
					return image;
				}

				BufferedImage enImage = getBufferedImage();
				BufferedImage mImage = maskImage.getBufferedImage();
				if (enImage == null || mImage == null) {
					return image;
				}
				WritableRaster enRaster = enImage.getAlphaRaster();

				boolean cascadedMask = cascade;
				if (cascadedMask && (enRaster == null)) {
					cascadedMask = false; // No softmask present so ignore
					// cascade
				}

				int enWdth = enImage.getWidth();
				int enHght = enImage.getHeight();
				int maWdth = mImage.getWidth();
				int maHght = mImage.getHeight();

				// For the output image
				BufferedImage outImage = new BufferedImage(enWdth, enHght,
				    BufferedImage.TYPE_INT_ARGB);

				for (int i = 0; i < enWdth; i++) {
					for (int j = 0; j < enHght; j++) {

						int opacity = 255;

						if ((i < maWdth) && (j < maHght)) {
							int mask = mImage.getRGB(i, j);

							Color c = new Color(mask);

							// since the mask is supposed to have only shades of
							// gray (r
							// = g = b) taking only r value for sampling
							int greylevel = c.getRed();
							// //system.out.print ("in=" + greylevel);

							if (isInverted) {
								// white area of mask is made opaque and black
								// is made
								// transparent
								// the level of grey decides the level of
								// transparency
								// (opposite of opacity)
								opacity = greylevel;
							} else {
								// white area of mask is made transparent and
								// black is
								// made opaque
								// the level of grey decides the level of
								// opacity
								opacity = 255 - greylevel;
							}
						}

						// get the colour from the image
						int encolour = enImage.getRGB(i, j);
						Color enCo = new Color(encolour);
						if (cascadedMask) {
							opacity = (opacity == 0) ? opacity : enRaster
							    .getSample(i, j, 0);
						}
						Color opCo = new Color(enCo.getRed(), enCo.getGreen(),
						    enCo.getBlue(), opacity);

						int outColour = opCo.getRGB();
						outImage.setRGB(i, j, outColour);
					}
				}

				return outImage;
			}
		});
	}

	public CoreImage bandMerge(CoreImage mask) {
		return this;
	}

	public CoreImage changeContrast(int intensity) {
		return this;
	}

	public CoreImage colorize(final Color newColor) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				BufferedImage buffer = getBufferedImage(image);

				int w = buffer.getWidth();
				int h = buffer.getHeight();
				int r = newColor.getRed();
				int g = newColor.getGreen();
				int b = newColor.getBlue();

				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						int rgb = buffer.getRGB(i, j);

						if (rgb != 0) {
							int a = rgb >> 24 & 0xff;
							int value = ((a & 0xff) << 24) | ((r & 0xff) << 16)
							    | ((g & 0xff) << 8) | ((b & 0xff) << 0);
							buffer.setRGB(i, j, value);
						}
					}
				}
				return buffer;
			}
		});
	}

	/**
	 * API the performs a lookup operation.
	 * 
	 * @param image - the source image
	 * @param lt - The byte array to create a lookup table
	 * @return
	 */

	public CoreImage colorize(byte[][] lt) {
		return this;
	}

	public CoreImage bandcombine() {
		return this;
	}

	public CoreImage reduceToThreeBand() {
		return this;
	}

	public CoreImage composite(CoreImage src2, int blendFactor) {
		return this;
	}

	public CoreImage convertColorModel(int type) {
		return this;
	}

	public CoreImage convertColorToGray() {
		return this;
	}

	public CoreImage colorize(int rFactor, int gFactor, int bFactor) {
		byte[][] lut = new byte[3][256];
		for (int i = 0; i < 256; i++) {
			lut[0][i] = (byte) (rFactor * i / 255.0);
			lut[1][i] = (byte) (gFactor * i / 255.0);
			lut[2][i] = (byte) (bFactor * i / 255.0);
		}
		return colorize(lut);
	}

	public CoreImage convertModel() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				if (image.getColorModel() instanceof DirectColorModel) {
					BufferedImage srcbImage = getBufferedImage(image);
					int wid = srcbImage.getWidth(), ht = srcbImage.getHeight();

					SampleModel sm = srcbImage.getSampleModel();
					WritableRaster wr = srcbImage.getRaster();
					DataBuffer db = wr.getDataBuffer();
					int numbands = wr.getNumBands();
					int sample[] = new int[numbands];
					byte data1[] = new byte[wid * ht * numbands];
					boolean alpha = image.getColorModel().hasAlpha();
					for (int i = 0; i < ht; i++) {
						for (int j = 0; j < wid; j++) {
							int pix[] = null;
							sample = (sm.getPixel(j, i, pix, db));
							for (int l = 0; l < numbands; l++) {
								if (l == 3 && alpha)
									data1[i * wid * numbands + (j * numbands)
									    + l] = (byte) sample[l];
								else
									data1[i * wid * numbands + (j * numbands)
									    + l] = (byte) sample[l];
							}
						}
					}
					return createInterleavedRGBImage(wid, ht, 8, data1, image
					    .getColorModel().hasAlpha(), image.getColorModel()
					    .isAlphaPremultiplied());
				} else if (image.getColorModel() instanceof IndexColorModel) {

				}
				return image;
			}
		});
	}

	public CoreImage convertSoftMaskToHard(boolean invert) {
		return this;
	}

	public CoreImage convertToTransparent(final int transparentPixel) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				int[] pixels = getBufferedImage(image).getRGB(0, 0,
				    image.getWidth(), image.getHeight(), null, 0,
				    image.getWidth());
				// Convert the transparent pixel
				for (int j = 0; j < pixels.length; j++) {
					if (pixels[j] == transparentPixel) {
						pixels[j] = 0;
					}
				}
				BufferedImage img = new BufferedImage(image.getWidth(), image
				    .getHeight(), BufferedImage.TYPE_INT_ARGB);
				img.setRGB(0, 0, image.getWidth(), image.getHeight(), pixels,
				    0, image.getWidth());
				return img;
			}
		});
	}

	public CoreImage convertToGray() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				ColorSpace gray_space = ColorSpace
				    .getInstance(ColorSpace.CS_GRAY);
				ColorConvertOp op = new ColorConvertOp(gray_space, null);
				BufferedImage dst = new BufferedImage(image.getWidth(), image
				    .getHeight(), BufferedImage.TYPE_INT_ARGB);
				return op.filter(getBufferedImage(image), dst);
			}
		});
	}

	public CoreImage convertToThreeBand() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				return convertToThreeBand(image);
			}
		});
	}

	protected RenderedImage convertToThreeBand(RenderedImage image) {
		if (image.getColorModel() instanceof IndexColorModel) {
			return image;
		}

		BufferedImage buff = new BufferedImage(image.getWidth(), image
		    .getHeight(), BufferedImage.TYPE_INT_RGB);
		BufferedImage srcImg = getBufferedImage(image);
		WritableRaster wr = buff.getRaster();
		Color c;
		for (int h = buff.getMinY(); h < image.getHeight(); h++) {
			for (int w = buff.getMinX(); w < image.getWidth(); w++) {
				c = new Color(srcImg.getRGB(w, h));
				wr.setSample(w, h, 0, c.getRed());
				wr.setSample(w, h, 1, c.getGreen());
				wr.setSample(w, h, 2, c.getBlue());
			}
		}
		return buff;
	}

	/**
	 * creates mask from alpha channel or creates default mask for given
	 * element. Always returns non-null mask.
	 * 
	 * @param source
	 * @return
	 */
	public CoreImage extractMask(final boolean softMask) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				return extractMask(image, softMask);
			}
		});
	}

	protected RenderedImage extractMask(RenderedImage image, boolean softMask) {
		return getBlank3Image(image.getWidth(), image.getHeight(), Color.WHITE);
	}

	/**
	 * special case for colourindication. mask is taken from bitmap image, black =
	 * visible, white = transparent
	 * 
	 * @param planar
	 * @return
	 */
	public CoreImage extractMaskForColourIndicationItem() {
		return this;
	}

	public CoreImage crop(int x, int y, int width, int height) {
		return crop(new Rectangle(x, y, width, height));
	}

	public CoreImage crop(Rectangle bounds) {
		return this;
	}

	/**
	 * API that returns the mask image seperately if a 4 band image is supplied
	 * 
	 * @param src2
	 * @return
	 */
	public CoreImage extractMask() {
		return this;
	}

	public CoreImage convertToBlackAndWhite(final int threshold) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				byte[][] lut = new byte[3][256];
				CoreImage mask = null;
				if (getNumBands(image) >= 4) {
					mask = newInstance(image).extractMask();
					image = newInstance(image).reduceToThreeBand().getAwt();
				}
				CoreImage ci = newInstance(image).convertColorToGray();
				for (int i = 0; i < 256; i++) {
					if (i > threshold) {
						lut[0][i] = (byte) (255);
						lut[1][i] = (byte) (255);
						lut[2][i] = (byte) (255);
					} else {
						lut[0][i] = (byte) (0);
						lut[1][i] = (byte) (0);
						lut[2][i] = (byte) (0);
					}
				}
				ci.colorize(lut);
				if (mask != null) {
					ci.applyMask(mask, false);
				}

				return ci.getAwt();
			}
		});
	}

	public CoreImage overlay(Color overlayColor) {
		return overlay(overlayColor, true);
	}

	public CoreImage overlay(Color overlayColor, boolean processAlpha) {
		return this;
	}

	/**
	 * Method to get the color at a given pixel position in an image
	 * 
	 * @param x int x position
	 * @param y int y position
	 * @return Color at the given position
	 */
	public Color getColourAtPixel(int x, int y) {
		RenderedImage image = getAwt();
		if (image == null) {
			return null;
		}
		Raster maskRaster = image.getData();

		int r = maskRaster.getSample(x, y, 0);
		int g = maskRaster.getSample(x, y, 1);
		int b = maskRaster.getSample(x, y, 2);

		return new Color(r, g, b);
	}

	/**
	 * Method to get a list of colours from a image
	 * 
	 * @param url String holds the path of the file
	 * @param sampleSize int holds the width of the sample
	 * @param regions int holds the number of regions
	 * @return List holds the Color objects for the regions
	 */
	public List<Color> getColoursList(int sampleSize, int yPosition, int regions)
	    throws Exception {
		List<Color> coloursList = new ArrayList<Color>();

		int imWidth = getWidth();
		int y = yPosition;
		int x = 0;

		if (yPosition >= getHeight()) {

			Color black = new Color(0, 0, 0);
			for (int i = 0; i < regions; i++) {
				coloursList.add(black);
			}
			return coloursList;
		}

		for (int i = 0; i < regions; i++) {
			Color c = getColourAtPixel(x, y);
			coloursList.add(c);
			x = (x + sampleSize < imWidth) ? (x + sampleSize) : x;
		}

		return coloursList;
	}

	public CoreImage fade(final int opacity) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				CoreImage img = newInstance(image);
				if (img.getNumBands() < 3) {
					img.convertToThreeBand();
					if (img == null) {
						return img.getAwt();
					}
				}
				img.convertModel();

				image = img.getAwt();
				BufferedImage buf = getBufferedImage(image);
				Graphics2D g = (Graphics2D) buf.getGraphics();
				g.setComposite(AlphaComposite.DstIn);
				g.drawRenderedImage(getBlankImage(img.getWidth(), img
				    .getHeight(), Color.WHITE, opacity), TRANSFORM_ORIGIN);
				g.setComposite(AlphaComposite.SrcOver);
				return buf;
			}
		});
	}

	public CoreImage convertToGrayScale() {
		return this;
	}

	public CoreImage opacity(int opacity) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				CoreImage tmp = newInstance(image);
				if (tmp.getNumBands() < 3) {
					tmp.convertToThreeBand();
				}

				// composite
				tmp.convertModel();

				if (tmp.getAwt() == null) {
					return image;
				}

				BufferedImage buf = getBufferedImage(getBlank3Image(tmp
				    .getWidth(), tmp.getHeight(), Color.WHITE));
				Graphics2D g = (Graphics2D) buf.createGraphics();
				g.setComposite(AlphaComposite.SrcOver);
				g.drawRenderedImage(tmp.getAwt(), TRANSFORM_ORIGIN);
				// g.setComposite(AlphaComposite.DstIn);
				// g.drawRenderedImage(getBlankImage(image.getWidth(),image.getHeight(),Color.WHITE,opacity),AffineTransform.getTranslateInstance(0,0));
				g.setComposite(AlphaComposite.SrcOver);
				return buf;
			}
		});
	}

	public CoreImage invertGrayScale() {
		return this;
	}

	public CoreImage invertSingleBandMask() {
		return this;
	}

	public CoreImage extractBlackAndWhiteMask() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				BufferedImage im = getBufferedImage(image);

				int screenWidth = (int) im.getWidth();
				int screenHeight = (int) im.getHeight();

				int white = (Color.WHITE).getRGB();
				int black = (Color.BLACK).getRGB();

				BufferedImage out = new BufferedImage(screenWidth,
				    screenHeight, BufferedImage.TYPE_BYTE_GRAY);

				for (int i = 0; i < screenWidth; i++) {
					for (int j = 0; j < screenHeight; j++) {
						int sample = im.getRGB(i, j);
						WritableRaster alpha = im.getAlphaRaster();
						int a = 0;

						if (alpha != null)
							a = alpha.getSample(i, j, 0);
						int outsample = (sample == white) ? white : black;

						if (alpha != null)
							outsample = (a == 0) ? white : outsample;

						out.setRGB(i, j, outsample);
					}
				}

				return out;
			}
		});
	}

	public CoreImage applyAlpha(int alphaValue) {
		return this;
	}

	/**
	 * This mehtod masks the given image with the given masking image.
	 * 
	 * @param rect type is Rectangle.
	 * @param xpos type is int.
	 * @param ypos type is int.
	 * @param tranperancy type is int. poly is the polygon outside which the
	 *            image will be transparent. image is the image that is to be
	 *            modified. xpos is the x coordinate of the image inside the
	 *            component. ypos is the y coordinate of the image inside the
	 *            component. tranperancy is the amount of transperancy required
	 *            outside the polygon. Value 0 is full transpearant and 255 is
	 *            full opeque. visibility is the amount of transperancy required
	 *            inside the polygon. Value 0 is full transpearant and 255 is
	 *            full opeque.
	 * @return BufferedImage. returns the masked image.
	 */
	public CoreImage maskImages(final Polygon poly, final int xpos,
	    final int ypos, final int transparency, final int visibility) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {

				int src = 0;

				int minx = image.getMinX();
				int miny = image.getMinY();

				BufferedImage buffImage;

				// Get the raster of the image.
				Raster raster = image.getData();

				ColorModel model = image.getColorModel();

				if (model instanceof IndexColorModel) {

					buffImage = ((IndexColorModel) model).convertToIntDiscrete(
					    raster, true);
					WritableRaster wRaster = buffImage.getRaster();

					for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
						for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
							if (poly.contains(w + xpos, h + ypos)) {
								wRaster.setSample(w - minx, h - miny, 3,
								    visibility);
							} else {
								wRaster.setSample(w - minx, h - miny, 3,
								    transparency);
							}

						}

					}

				} else {

					// No. of bands present in the main raster
					int numBands = raster.getNumBands();

					if (numBands > 3) {

						// Create a buffered image whose size is equal to the
						// mai image.
						buffImage = getBufferedImage(image);

						// Get the writable raster of the buffered image.
						WritableRaster wRaster = buffImage.getAlphaRaster();

						for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
							for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
								// Debug.out("Source is...." + src);
								if (poly.contains(w + xpos, h + ypos)) {
									src = visibility;
								} else {
									src = transparency;
								}
								wRaster.setSample(w - minx, h - miny, 0, src);
							}
						}
					} else {

						// Create a buffered image whose size is equal to the
						// main
						// image.
						buffImage = new BufferedImage(image.getWidth(), image
						    .getHeight(), BufferedImage.TYPE_INT_ARGB);

						// Get the writable raster of the buffered image.
						WritableRaster wRaster = buffImage.getRaster();

						for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
							for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
								for (int n = numBands - 1; n >= 0; n--) {
									src = raster.getSample(w, h, n);
									wRaster.setSample(w - minx, h - miny, n,
									    src);
								}
								if (poly.contains(w + xpos, h + ypos)) {
									src = visibility;
								} else {
									src = transparency;
								}
								wRaster.setSample(w - minx, h - miny, numBands,
								    src);
							}
						}
					}
				}

				return buffImage;
			}
		});
	}

	/**
	 * This mehtod returns a planar image which is opaque inside a given
	 * rectangle and transperant outside it.
	 * 
	 * @param rect type is Rectangle.
	 * @param image type is PlanarImage.
	 * @param xpos type is int.
	 * @param ypos type is int.
	 * @param tranperancy type is int
	 * @param visibility type is int rect is the rectangle outside which the
	 *            image will be transparent. image is the image that is to be
	 *            modified. xpos is the x coordinate of the image inside the
	 *            component. ypos is the y coordinate of the image inside the
	 *            component. tranperancy is the amount of transperancy required
	 *            outside the rectangle. Value 0 is full transpearant and 255 is
	 *            full opeque. visibility is the amount of transperancy required
	 *            inside the rectangle. Value 0 is full transpearant and 255 is
	 *            full opeque.
	 * @return PlanarImage. returns the masked image.
	 */
	public CoreImage maskImages(final Rectangle rect, final int xpos,
	    final int ypos, final int transparency, final int visibility) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				int src = 0;

				int minx = image.getMinX();
				int miny = image.getMinY();

				BufferedImage buffImage;

				// Get the raster of the image.
				Raster raster = image.getData();

				ColorModel model = image.getColorModel();

				if (model instanceof IndexColorModel) {

					buffImage = ((IndexColorModel) model).convertToIntDiscrete(
					    raster, true);
					WritableRaster wRaster = buffImage.getRaster();

					for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
						for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
							if (rect.contains(w + xpos, h + ypos)) {
								wRaster.setSample(w - minx, h - miny, 3,
								    visibility);
							} else {
								wRaster.setSample(w - minx, h - miny, 3,
								    transparency);
							}

						}

					}

				} else {

					// No. of bands present in the main raster
					int numBands = raster.getNumBands();

					// If the alpha band is already present then manipulate the
					// alpha
					// band only.
					if (numBands > 3) {

						// Create a buffered image whose size is equal to the
						// mai image.
						buffImage = getBufferedImage(image);

						// Get the writable raster of the buffered image.
						WritableRaster wRaster = buffImage.getAlphaRaster();

						for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
							for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
								// Do not change the Alpha band when the
								// application
								// passes an invalid value
								src = -1;
								if (rect.contains(w + xpos, h + ypos)) {
									if (visibility >= 0)
										src = visibility;
								} else {
									if (transparency >= 0)
										src = transparency;
								}
								if (src >= 0)
									wRaster.setSample(w - minx, h - miny, 0,
									    src);
							}
						}
					}
					// If the alpha banf is not present
					else {

						// Create a buffered image whose size is equal to the
						// main
						// image.
						buffImage = new BufferedImage(image.getWidth(), image
						    .getHeight(), BufferedImage.TYPE_INT_ARGB);

						// Get the writable raster of the buffered image.
						WritableRaster wRaster = buffImage.getRaster();

						// If the no of bands is one, as in single band jpg
						// (gray scale
						// jpg)
						if (numBands == 1) {

							for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
								for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
									for (int n = 2; n >= 0; n--) {
										src = raster.getSample(w, h, 0);
										wRaster.setSample(w - minx, h - miny,
										    n, src);
									}
									if (rect.contains(w + xpos, h + ypos)) {
										src = visibility;
									} else {
										src = transparency;
									}
									wRaster.setSample(w, h, 3, src);
								}

							}

						}
						// For images with thre bands
						else {

							for (int w = image.getWidth() - 1 + minx; w >= minx; w--) {
								for (int h = image.getHeight() - 1 + miny; h >= miny; h--) {
									for (int n = numBands - 1; n >= 0; n--) {
										src = raster.getSample(w, h, n);
										wRaster.setSample(w - minx, h - miny,
										    n, src);
									}
									if (rect.contains(w + xpos, h + ypos)) {
										src = visibility;
									} else {
										src = transparency;
									}
									wRaster.setSample(w - minx, h - miny,
									    numBands, src);
								}
							}
						}
					}
				}

				return newInstance(buffImage).translate(minx, miny).getAwt();
			}
		});
	}

	public CoreImage maskImages(final CoreImage maskImage,
	    final boolean softMask) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				// int src = 0;

				if (maskImage == null) {
					return image;
				}
				CoreImage clone = maskImage.copy();
				if (clone.getAwt() == null) {
					return image;
				}

				// Get the raster of both the images.
				Raster maskRaster = clone.getAwt().getData();
				Raster mainRaster = image.getData();

				// Create a buffered image whose size is equal to the mai image.
				// BufferedImage buffImage =
				// new BufferedImage(
				// mainImage.getWidth(),
				// mainImage.getHeight(),
				// BufferedImage.TYPE_INT_ARGB);

				// Get the writable raster of the buffered image.
				// WritableRaster wRaster = buffImage.getRaster();
				//
				// Rectangle rect = maskRaster.getBounds();

				// No. of bands present in the main raster
				int numBands = mainRaster.getNumBands();

				// numBands = (numBands > 3) ? 3 : numBands;

				CoreImage tmp = newInstance(image);
				if (numBands < 3) {
					tmp.convertToThreeBand();
				}

				if (numBands > 3) {

					// Sandeep - This is quick solution. For the problem of
					// Preivew getting messed up when the image to crop is out
					// of bounds. The image is fully transperant image is
					// currently being returned by the crop image. The preview
					// is not
					// working because masks are again reducing this image to
					// three
					// band.
					// So here I am tunring off the application masks for fully
					// transparent images.

					int srcSample = 0;
					for (int w = 0; w < tmp.getWidth(); w++) {
						for (int h = 0; h < tmp.getHeight(); h++) {
							srcSample += mainRaster.getSample(w, h, 3);
							if (srcSample != 0)
								break;
						}
					}

					if (srcSample == 0)
						return tmp.getAwt();
					// mainImage =
					// PlanarImage.wrapRenderedImage(
					// Colorize.reduceToThreeBand(mainImage.getAsBufferedImage()));
				}

				if (maskRaster.getNumBands() < 3) {
					clone.convertToThreeBand();
				}

				if (maskRaster.getNumBands() > 3) {
					// maskImage =
					// PlanarImage.wrapRenderedImage(
					// Colorize.reduceToThreeBand(maskImage.getAsBufferedImage()));
				}

				mainRaster = tmp.getAwt().getData();
				maskRaster = clone.getAwt().getData();
				numBands = mainRaster.getNumBands();

				return tmp.applyMask(clone, softMask).getAwt();
			}
		});
	}

	public CoreImage maskImages(CoreImage maskImage, boolean softMask,
	    boolean cascadedMask) {
		if (cascadedMask)
			return applyMask(maskImage, softMask, true);

		return maskImages(maskImage, softMask);
	}

	public CoreImage processMask(final CoreImage maskImage,
	    final boolean inverseMask) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				if (maskImage == null) {
					return image;
				}

				CoreImage cloned = maskImage.copy();
				BufferedImage mask = cloned.getBufferedImage();

				if (mask == null) {
					return image;
				}

				BufferedImage buf = getBufferedImage(image);
				/*
				 * If the image does not support transparency: Create a new
				 * BufferedImage with alpha layer, copy RGB values from buf and
				 * replace buf with the new image.
				 */
				if (!buf.getColorModel().hasAlpha()) {

					BufferedImage maskable = new BufferedImage(buf.getWidth(),
					    buf.getHeight(), BufferedImage.TYPE_INT_ARGB);
					for (int x = 0; x < maskable.getWidth(); x++) {
						for (int y = 0; y < maskable.getHeight(); y++) {
							maskable.setRGB(x, y, buf.getRGB(x, y));
						}
					}
					buf = maskable;
				}

				// Inverse mask pixel by pixel
				if (inverseMask) {
					WritableRaster raster = mask.getRaster();
					int[] maskData = null;
					maskData = raster.getPixels(0, 0, mask.getWidth(), mask
					    .getHeight(), maskData);
					for (int i = 0; i < maskData.length; i++) {
						maskData[i] = ~maskData[i];
					}
					raster.setPixels(0, 0, mask.getWidth(), mask.getHeight(),
					    maskData);
					mask.setData(raster);
				}
				// Apply the mask
				buf.getAlphaRaster().setRect(mask.getData());
				return buf;
			}
		});
	}

	public CoreImage removeAlpha() {
		return this;
	}

	public CoreImage rotate(int degree) {
		return this;
	}

	public CoreImage scale(final float magx, final float magy,
	    final float transx, final float transy) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				BufferedImage bi = new BufferedImage(
				    (int) (image.getWidth() * magx),
				    (int) (image.getHeight() * magy),
				    BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = bi.createGraphics();
				g.scale(magx, magy);
				g.drawRenderedImage(image, AffineTransform
				    .getTranslateInstance(transx, transy));
				g.dispose();
				return bi;
			}
		});
	}

	/**
	 * scales image to given width, height
	 * 
	 * @param icon
	 */
	public CoreImage scale(int width, int height) {
		if (getWidth() <= 0) {
			return this;
		}
		if (getHeight() <= 0) {
			return this;
		}
		float sx = width / (float) getWidth();
		float sy = height / (float) getHeight();
		return scale(sx, sy, 0, 0);
	}

	public CoreImage stretch(int width, int height, int scaleMode) {
		return stretch(width, height, scaleMode, false);
	}

	public CoreImage stretch(final int width, final int height,
	    final int scaleMode, final boolean adjustSize) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				if (width == image.getWidth() && height == image.getHeight()) {
					return image;
				}

				CoreImage tmp = newInstance(image);

				float wFactor = width / (float) image.getWidth();
				float hFactor = height / (float) image.getHeight();

				if (SCALE_TO_FIT == scaleMode || SCALE_DOWN_TO_FIT == scaleMode
				    || SCALE_TO_BEST == scaleMode
				    || SCALE_UP_TO_FIT == scaleMode) {
					float val = scaleMode != SCALE_TO_BEST
					    && scaleMode != SCALE_TO_FIT ? Math.min(wFactor,
					    hFactor) : Math.max(wFactor, hFactor);
					wFactor = val;
					hFactor = val;

					RenderedImage image2 = null;
					if (SCALE_DOWN_TO_FIT == scaleMode) {
						if (val >= 1.0) {
							image2 = image;
						}
					} else if (SCALE_TO_BEST == scaleMode) {
						if (val >= 1.0) {
							image2 = image;
						}
					}

					if (image2 == null) {
						image2 = tmp.scale(wFactor, hFactor, 0, 0).getAwt();
					}

					if (adjustSize) {
						return image2;
					}

					BufferedImage buf = new BufferedImage(width, height,
					    BufferedImage.TYPE_INT_ARGB);
					int extraW = image2.getWidth() - width;
					int extraH = image2.getHeight() - height;

					Graphics2D g = (Graphics2D) buf.getGraphics();
					g.drawRenderedImage(image2, AffineTransform
					    .getTranslateInstance(image2.getMinX() - extraW / 2,
					        image2.getMinY() - extraH / 2));
					g.dispose();
					return buf;
				}
				return tmp.scale(wFactor, hFactor, 0, 0).getAwt();
			}
		});
	}

	public CoreImage tileImage(final int x, final int y, final int width,
	    final int height) {
		if (getWidth() >= width && getHeight() >= height) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				BufferedImage buf = getBufferedImage(getBlankImage(width,
				    height, Color.WHITE));

				Graphics2D gd = (Graphics2D) buf.getGraphics();
				// gd.drawRect(X,Y,image.getWidth(),image.getHeight());

				int X = x, Y = y;
				if (X != 0) {
					do {
						X = X - image.getWidth();
					} while (X > 0);

				}
				if (Y != 0) {
					do {
						Y = Y - image.getHeight();
					} while (Y > 0);
				}

				if ((image.getWidth() <= width)
				    || (image.getHeight() <= height)) {
					// int i=X;
					for (int i = X; i < width; i += image.getWidth()) {
						for (int j = Y; j < height; j += image.getHeight()) {
							gd.drawRenderedImage(image, AffineTransform
							    .getTranslateInstance(i, j));

						}
					}

				}
				return buf;
			}
		});
	}

	public CoreImage tileX(final int x, final int y, final int width,
	    final int height) {
		if (getWidth() >= width && getHeight() >= height) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				BufferedImage buf = getBufferedImage(getBlankImage(width,
				    height, Color.WHITE));
				CoreImage tmp = newInstance(image);

				Graphics2D gd = (Graphics2D) buf.getGraphics();
				// gd.drawRect(X,Y,image.getWidth(),image.getHeight());
				float h = ((float) height) / image.getHeight();
				if (image.getWidth() <= width || image.getHeight() <= height) {
					image = tmp.scale(1, h, 0, 0).getAwt();
				}
				int X = x, Y = y;
				if (X != 0) {
					do {
						X = X - image.getWidth();
					} while (X > 0);

				}
				if (Y != 0) {
					do {
						Y = Y - image.getHeight();
					} while (Y > 0);
				}

				if ((image.getWidth() <= width)
				    || (image.getHeight() <= height)) {
					// int i=X;
					for (int i = X; i < width; i += image.getWidth()) {
						for (int j = Y; j < height; j += image.getHeight()) {
							gd.drawRenderedImage(image, AffineTransform
							    .getTranslateInstance(i, j));

						}
					}

				}
				return buf;
			}
		});
	}

	public CoreImage tileY(final int x, final int y, final int width,
	    final int height) {
		if (getWidth() >= width && getHeight() >= height) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {

				BufferedImage buf = getBufferedImage(getBlankImage(width,
				    height, Color.WHITE));
				CoreImage tmp = newInstance(image);
				Graphics2D gd = (Graphics2D) buf.getGraphics();
				// gd.drawRect(X,Y,image.getWidth(),image.getHeight());
				float w = ((float) width) / image.getWidth();
				if (image.getWidth() <= width || image.getHeight() <= height) {
					image = tmp.scale(w, 1, 0, 0).getAwt();
				}
				int X = x, Y = y;
				if (X != 0) {
					do {
						X = X - image.getWidth();
					} while (X > 0);

				}
				if (Y != 0) {
					do {
						Y = Y - image.getHeight();
					} while (Y > 0);
				}
				if ((image.getWidth() <= width)
				    || (image.getHeight() <= height)) {
					// int i=X;
					for (int i = X; i < width; i += image.getWidth()) {
						for (int j = Y; j < height; j += image.getHeight()) {
							gd.drawRenderedImage(image, AffineTransform
							    .getTranslateInstance(i, j));

						}
					}

				}
				return buf;
			}
		});
	}

	/**
	 * Api for redrawing the image at a specified x, y position. Used while
	 * processing effects like channelblending.
	 */
	public CoreImage translate(final int x, final int y) {
		return relocate(x, y);
	}

	public CoreImage relocate(final int x, final int y) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				BufferedImage buf = new BufferedImage(image.getWidth() + x,
				    image.getHeight() + y, BufferedImage.TYPE_INT_ARGB);
				((Graphics2D) buf.getGraphics()).drawRenderedImage(image,
				    AffineTransform.getTranslateInstance(x, y));
				return newInstance(buf).convertModel().getAwt();
			}
		});
	}

	public CoreImage darken(CoreImage image2, int x, int y) {
		return this;
	}

	public CoreImage convolve(float[] data) {
		return this;
	}

	/**
	 * API to get the output image by calculating the median of each a pixel
	 * 
	 * @param src
	 * @return
	 */
	public CoreImage median() {
		return this;
	}

	/**
	 * API that gets an output image by calculating the mean pixel value of each
	 * pixel.
	 * 
	 * @param src
	 * @return
	 */
	public CoreImage mean() {
		return this;
	}

	/**
	 * API for produce a dilate effect on the image
	 * 
	 * @param src
	 * @return
	 */
	public CoreImage dilate() {
		return this;
	}

	/**
	 * API for producing the erode effect
	 * 
	 * @param src
	 * @return
	 */
	public CoreImage erode() {
		return this;
	}

	public CoreImage addConst(double[] constants) {
		return this;
	}

	public RenderedImage getBlankImage(int width, int height, Color c) {
		return getBlank3Image(width, height, c);
	}

	public RenderedImage getBlankImage(int width, int height, Color c,
	    int opacity) {
		return getBlank3Image(width, height, c);
	}

	public RenderedImage getBlankImage(int width, int height, Color c,
	    int opacity, int bands) {
		return getBlank3Image(width, height, c);
	}

	public boolean isImageBlackWhiteImage() {
		BufferedImage bw = getBufferedImage();
		if (bw == null) {
			return false;
		}
		int BLACK = Color.BLACK.getRGB();
		int WHITE = Color.WHITE.getRGB();
		for (int x = 0; x < bw.getWidth(); x++)
			for (int y = 0; y < bw.getHeight(); y++) {
				int rgb = bw.getRGB(x, y);
				if (rgb != WHITE && rgb != BLACK)
					return false;
			}
		return true;
	}

	public boolean hasTransparency() {
		BufferedImage buf = getBufferedImage();
		if (buf == null) {
			return false;
		}
		for (int x = 0; x < buf.getWidth(); x++)
			for (int y = 0; y < buf.getHeight(); y++) {
				int rgba = buf.getRGB(x, y);
				if ((rgba & 0xff000000) < 255)
					return true;
			}
		return false;
	}

	public boolean isImageGrayScaleImage() {
		BufferedImage bw = getBufferedImage();
		if (bw == null) {
			return false;
		}
		if (bw.getType() == BufferedImage.TYPE_BYTE_GRAY
		    || bw.getType() == BufferedImage.TYPE_USHORT_GRAY)
			return true;
		for (int x = 0; x < bw.getWidth(); x++)
			for (int y = 0; y < bw.getHeight(); y++) {
				int rgb = bw.getRGB(x, y);
				int a = rgb & 0xff;
				int b = rgb & 0xff00;
				int c = rgb & 0xff0000;
				if (((a & b) & c) != a)
					return false;
			}
		return true;
	}

	public void dispose() {
		if (swt != null) {
			swt.dispose();
		}
		awt = null;
		swt = null;
	}

	protected Image convert(RenderedImage awt) {
		if (awt == null) {
			return null;
		}
		return new RenderedImageDescriptor(awt).createImage();
	}

	protected RenderedImage convert(Image swt) {
		if (swt == null) {
			return null;
		}

		ImageData data = swt.getImageData();
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask,
			    palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
			    colorModel.createCompatibleWritableRaster(data.width,
			        data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[4];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					pixelArray[0] = rgb.red;
					pixelArray[1] = rgb.green;
					pixelArray[2] = rgb.blue;
					pixelArray[3] = data.getAlpha(x, y);
					raster.setPixel(x, y, pixelArray);
				}
			}
			CoreImage image = new CoreImage(bufferedImage);
			image.convertToTransparent(data.transparentPixel);
			return image.getAwt();
		}
		RGB[] rgbs = palette.getRGBs();
		byte[] red = new byte[rgbs.length];
		byte[] green = new byte[rgbs.length];
		byte[] blue = new byte[rgbs.length];
		for (int i = 0; i < rgbs.length; i++) {
			RGB rgb = rgbs[i];
			red[i] = (byte) rgb.red;
			green[i] = (byte) rgb.green;
			blue[i] = (byte) rgb.blue;
		}
		if (data.transparentPixel != -1) {
			colorModel = new IndexColorModel(data.depth, rgbs.length, red,
			    green, blue, data.transparentPixel);
		} else {
			colorModel = new IndexColorModel(data.depth, rgbs.length, red,
			    green, blue);
		}
		BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel
		    .createCompatibleWritableRaster(data.width, data.height), false,
		    null);
		WritableRaster raster = bufferedImage.getRaster();
		int[] pixelArray = new int[1];
		for (int y = 0; y < data.height; y++) {
			for (int x = 0; x < data.width; x++) {
				int pixel = data.getPixel(x, y);
				pixelArray[0] = pixel;
				raster.setPixel(x, y, pixelArray);
			}
		}
		return bufferedImage;
	}

	public void save(String type, File file) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			save(type, out);
		} finally {
			FileUtils.close(out);
		}
	}

	public void save(String type, OutputStream out) throws IOException {
		if (TYPE_PNG.equalsIgnoreCase(type)) {
			savePng(out);
		} else if (TYPE_BMP.equalsIgnoreCase(type)) {
			saveBmp(out);
		} else if (TYPE_TIFF.equalsIgnoreCase(type)) {
			saveTiff(out);
		}
	}

	protected void savePng(OutputStream out) throws IOException {
		ImageIO.write(getAwt(), TYPE_PNG, out);
	}

	protected void saveBmp(OutputStream out) throws IOException {
		ImageIO.write(getAwt(), TYPE_BMP, out);
	}

	protected void saveTiff(OutputStream out) throws IOException {
		ImageIO.write(getAwt(), TYPE_TIFF, out);
	}

	public static CoreImage create() {
		return ImageExtensionManager.createImage();
	}

	public static CoreImage create(RenderedImage awt) {
		return create().init(awt);
	}

	public static CoreImage create(Image swt) {
		return create().init(swt);
	}

	public static void dispose(RenderedImage image) {
		create().init(image).dispose();
	}

	public static BufferedImage getBufferedImage(RenderedImage image) {
		return CoreImage.create().init(image).getBufferedImage();
	}

	public static RenderedImage getBlank3Image(int width, int height,
	    Color color) {

		BufferedImage blankImg = new BufferedImage(width, height,
		    BufferedImage.TYPE_INT_RGB);
		if (color == null)
			color = Color.white;

		Graphics g = blankImg.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, width, height);
		g.dispose();

		return blankImg;
	}

	public static RenderedImage createInterleavedRGBImage(int imageWidth,
	    int imageHeight, int imageDepth, byte data[], boolean hasAlpha,
	    boolean isAlphaMultiplied) {
		int pixelStride, transparency;
		if (hasAlpha) {
			pixelStride = 4;
			transparency = Transparency.BITMASK;
		} else {
			pixelStride = 3;
			transparency = Transparency.OPAQUE;
		}
		int[] numBits = new int[pixelStride];
		int[] bandoffsets = new int[pixelStride];

		for (int i = 0; i < pixelStride; i++) {
			numBits[i] = imageDepth;
			bandoffsets[i] = i;
		}

		ComponentColorModel ccm = new ComponentColorModel(ColorSpace
		    .getInstance(ColorSpace.CS_sRGB), numBits, hasAlpha,
		    isAlphaMultiplied,
		    // Alpha pre-multiplied
		    transparency, DataBuffer.TYPE_BYTE);
		PixelInterleavedSampleModel csm = new PixelInterleavedSampleModel(
		    DataBuffer.TYPE_BYTE, imageWidth, imageHeight, pixelStride,
		    // Pixel stride
		    imageWidth * pixelStride, // Scanline stride
		    bandoffsets);

		DataBuffer dataBuf = new DataBufferByte(data, imageWidth * imageHeight
		    * pixelStride);
		WritableRaster wr = Raster.createWritableRaster(csm, dataBuf,
		    new Point(0, 0));
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put("owner", "Platform");
		return new BufferedImage(ccm, wr, false, ht);
	}

	public static String getRealImageTypeName(File image) {
		ImageInputStream in = null;
		try {
			in = ImageIO.createImageInputStream(image);
			Iterator<ImageReader> imr = ImageIO.getImageReaders(in);
			if (imr.hasNext()) {
				return imr.next().getFormatName();
			}
		} catch (Exception e) {
		} finally {
			try {
				in.close();
			} catch (Exception es) {
			}
		}
		return null;
	}

	protected abstract class ImageWork {
		protected abstract void execute();
	}

	protected abstract class SWTWork extends ImageWork {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.image.CoreImage.Worker#work()
		 */
		protected final void execute() {
			Image image = getSwt();
			if (image != null) {
				Image result = process(image);
				setSwt(result);
			}
		}

		protected abstract Image process(Image image);
	}

	protected abstract class AWTWork extends ImageWork {
		/*
		 * (non-Javadoc)
		 * 
		 * @see com.nokia.tools.media.image.CoreImage.Worker#work()
		 */
		protected final void execute() {
			RenderedImage image = getAwt();
			if (image != null) {
				RenderedImage result = process(image);
				setAwt(result);
			}
		}

		protected abstract RenderedImage process(RenderedImage image);
	}
}
