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
package com.nokia.tools.media.jai;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.operator.CompositeDescriptor;
import javax.media.jai.operator.MedianFilterDescriptor;
import javax.media.jai.operator.TransposeDescriptor;

import org.eclipse.swt.graphics.Image;

import com.nokia.tools.media.image.CoreImage;
import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class JAIImage extends CoreImage {

	public JAIImage() {
	}

	public JAIImage(RenderedImage awt) {
		super(awt);
	}

	public JAIImage(Image swt) {
		super(swt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#newInstance()
	 */
	@Override
	public CoreImage newInstance() {
		return new JAIImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#newInstance(org.eclipse.swt.graphics.Image)
	 */
	@Override
	public CoreImage newInstance(Image swt) {
		return new JAIImage(swt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#newInstance(java.awt.image.RenderedImage)
	 */
	@Override
	public CoreImage newInstance(RenderedImage awt) {
		return new JAIImage(awt);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#convertToBuffered(java.awt.image.RenderedImage)
	 */
	@Override
	protected BufferedImage convertToBuffered(RenderedImage image) {
		if (image instanceof PlanarImage) {
			return ((PlanarImage) image).getAsBufferedImage();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getNumBands(java.awt.image.RenderedImage)
	 */
	@Override
	protected int getNumBands(RenderedImage image) {
		if (image instanceof PlanarImage)
			return ((PlanarImage) image).getNumBands();

		return super.getNumBands(image);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();

		if (awt instanceof PlanarImage) {
			((PlanarImage) awt).dispose();
		}
	}

	protected void disposeTemp(RenderedImage image) {
		disposeTemp(this, image);
	}

	protected static void disposeTemp(CoreImage core, RenderedImage image) {
		if (core.getAwt() != image && image instanceof PlanarImage) {
			((PlanarImage) image).dispose();
		}
	}

	public PlanarImage getPlanarImage() {
		RenderedImage image = getAwt();

		if (image == null) {
			return null;
		}
		return getPlanarImage(image);
	}

	private static PlanarImage getPlanarImage(RenderedImage image) {
		if (image instanceof PlanarImage) {
			return (PlanarImage) image;
		}
		return PlanarImage.wrapRenderedImage(image);
	}

	private static LookupTableJAI createLookUpTable(int win, int lev) {
		int wStart = lev - win / 2;
		int wEnd = lev + win / 2;
		if (wStart <= 0)
			wStart = 0;
		if (wEnd >= 256)
			wEnd = 256;
		byte lut[] = new byte[256];
		double windowMappingRatio = (255 - 0) / (double) win;
		for (int i = 0; i < wStart; i++) {
			lut[i] = (byte) 0;
		}
		for (int i = wStart; i < wEnd; i++) {
			lut[i] = (byte) ((i - wStart) * windowMappingRatio);
		}
		for (int i = wEnd; i < 256; i++) {
			lut[i] = (byte) 255;
		}
		// for(int i=0;i<256;i++)
		// {
		// lut[i]=(byte)((i* (255 + win) - win * 127) );
		// }
		return new LookupTableJAI(lut, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#bandMerge(com.nokia.tools.media.image.CoreImage)
	 */
	@Override
	public CoreImage bandMerge(final CoreImage mask) {
		if (mask == null || mask.getAwt() == null) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src1 = getPlanarImage(image);
				PlanarImage src2 = getPlanarImage(mask.getAwt());

				pb.addSource(src1);
				pb.addSource(src2);

				try {
					return JAI.create("bandmerge", pb);
				} finally {
					disposeTemp(src1);
					disposeTemp(mask, src2);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#changeContrast(int)
	 */
	@Override
	public CoreImage changeContrast(final int intensity) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				LookupTableJAI blut = createLookUpTable(255 - intensity / 2,
						127);
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(blut);
				try {
					return JAI.create("lookup", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#colorize(byte[][])
	 */
	@Override
	public CoreImage colorize(final byte[][] lt) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				LookupTableJAI lookup = new LookupTableJAI(lt);
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(lookup);
				try {
					return JAI.create("lookup", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#bandcombine()
	 */
	@Override
	public CoreImage bandcombine() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				CoreImage copy = newInstance(image);
				if (copy.getNumBands() > 3) {
					copy.reduceToThreeBand();
				}
				image = copy.getAwt();
				ParameterBlock pb = new ParameterBlock();
				double[][] matrix = { { .114D, 0.587D, 0.299D, 0.0D } };

				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(matrix);
				try {
					return JAI.create("bandcombine", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#reduceToThreeBand()
	 */
	@Override
	public CoreImage reduceToThreeBand() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				if (getNumBands() < 3) {
					return image;
				}
				int[] band = { 0, 1, 2 };
				ParameterBlock pm = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pm.addSource(src);
				pm.add(band);
				try {
					return JAI.create("bandselect", pm);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#composite(com.nokia.tools.media.image.CoreImage,
	 *      int)
	 */
	@Override
	public CoreImage composite(final CoreImage src2, final int blendFactor) {
		if (src2 == null || src2.getAwt() == null) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				List<PlanarImage> v = new ArrayList<PlanarImage>();
				// PlanarImage mask=null;
				// double factor = (double) (255.0 + 255.0);
				double value = blendFactor / 255.0;
				double value1 = 1.0 - value;
				double[] constants = null;
				if (src2.getNumBands() == 4)
					constants = new double[4];
				else
					constants = new double[3];

				for (int i = 0; i < constants.length; i++) {
					constants[i] = value;
				}
				double[] constants1 = null;

				if (getNumBands() == 4)
					constants1 = new double[4];
				else
					constants1 = new double[3];

				for (int i = 0; i < constants1.length; i++) {
					constants1[i] = value1;
				}

				ParameterBlock pb = new ParameterBlock();
				PlanarImage s1 = getPlanarImage(src2.getAwt());
				pb.addSource(s1);
				pb.add(constants);
				PlanarImage tmp1 = JAI.create("multiplyconst", pb, null);
				pb = new ParameterBlock();
				PlanarImage s2 = getPlanarImage(image);
				pb.addSource(s2);
				pb.add(constants1);
				PlanarImage tmp2 = JAI.create("multiplyconst", pb, null);

				v.add(0, tmp2);
				v.add(1, tmp1);
				pb = new ParameterBlock();
				pb.addSource(v);

				RenderedImage result = JAI.create("addcollection", pb, null);

				disposeTemp(s1);
				disposeTemp(src2, s2);
				tmp1.dispose();
				tmp2.dispose();

				JAIImage temp = new JAIImage(result);
				temp.convertColorModel(DataBuffer.TYPE_BYTE);
				return temp.getAwt();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#convertColorModel(int)
	 */
	@Override
	public CoreImage convertColorModel(final int type) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				PlanarImage src = getPlanarImage(image);
				try {
					return JAI.create("format", src, new Integer(type));
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#convertColorToGray()
	 */
	@Override
	public CoreImage convertColorToGray() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				// creates a copy to avoid changing self if the whole operation
				// fails
				CoreImage copy = newInstance(image).convertColorModel(
						DataBuffer.TYPE_BYTE);

				if (copy.getNumBands() > 3) {
					copy.reduceToThreeBand();
				}
				image = copy.getAwt();
				double[][] matrix = { { .3D, 0.59D, 0.11D, 0 },
						{ .3D, 0.59D, 0.11D, 0 }, { .3D, 0.59D, 0.11D, 0 } };

				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(matrix);
				try {
					return JAI.create("bandcombine", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#convertSoftMaskToHard(boolean)
	 */
	@Override
	public CoreImage convertSoftMaskToHard(final boolean invert) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				byte[] lut = new byte[256];
				for (int i = 0; i < 256; i++) {
					if (invert)
						lut[i] = (byte) ((i == 255) ? 0 : ((i > 127) ? 0 : 255));
					else
						lut[i] = (byte) ((i == 255) ? 255 : ((i < 127) ? 0
								: 255));
				}
				LookupTableJAI lookup = new LookupTableJAI(lut);

				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(lookup);
				try {
					return JAI.create("lookup", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#convertToThreeBand(java.awt.image.RenderedImage)
	 */
	@Override
	protected RenderedImage convertToThreeBand(RenderedImage image) {
		ColorModel cm = image.getColorModel();
		if (cm instanceof IndexColorModel) {
			IndexColorModel icm = (IndexColorModel) cm;
			byte[][] data = new byte[3][icm.getMapSize()];

			icm.getReds(data[0]);
			icm.getGreens(data[1]);
			icm.getBlues(data[2]);

			LookupTableJAI lut = new LookupTableJAI(data);
			PlanarImage src = getPlanarImage(image);
			try {
				return JAI.create("lookup", src, lut);
			} finally {
				disposeTemp(src);
			}
		}

		return super.convertToThreeBand(image);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#createMask(java.awt.image.RenderedImage,
	 *      boolean)
	 */
	@Override
	protected RenderedImage extractMask(RenderedImage source, boolean softMask) {
		// get mask from bitmap
		RenderedImage mask = null;
		if (getNumBands(source) == 4) {

			if (source.getColorModel() instanceof DirectColorModel) {
				DirectColorModel dcm = (DirectColorModel) source
						.getColorModel();
				if (dcm.getAlphaMask() != 0xff000000) {
					// post process with LUT to achieve correct 8-bit mask
					byte data[] = new byte[256];

					// interpolate range 0-255 between values defined by
					// alpha mask
					int alphaMask = dcm.getAlphaMask();
					while ((alphaMask & 1) == 0) {
						alphaMask = alphaMask >> 1;
					}

					for (int i = 0; i <= alphaMask; i++) {
						int maskValue = (int) (((float) i / alphaMask) * 255);
						data[alphaMask] = (byte) maskValue;
					}

					LookupTableJAI table = new LookupTableJAI(data);
					ParameterBlock pb = new ParameterBlock();
					PlanarImage src = getPlanarImage(source);
					pb.addSource(src);
					pb.add(table);
					try {
						mask = JAI.create("Lookup", pb);
					} finally {
						disposeTemp(src);
					}
				}
			}

			if (mask == null) {
				mask = newInstance(source).extractMask().getAwt();
			}

		} else {
			

			// create blank
			if (mask == null)
				mask = getBlank3Image(source.getWidth(), source.getHeight(),
						Color.WHITE);

		}
		// if not softmask type convert to hardmask
		if (!softMask) {
			mask = newInstance(mask).convertSoftMaskToHard(true).getAwt();
		}
		return mask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#createMaskForColourIndicationItem()
	 */
	@Override
	public CoreImage extractMaskForColourIndicationItem() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				int[] bandValue = { 0 };
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(bandValue);
				try {
					return JAI.create("bandselect", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#cropImage(java.awt.Rectangle)
	 */
	@Override
	public CoreImage crop(final Rectangle bounds) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				int x = bounds.x;
				int y = bounds.y;
				int width = bounds.width;
				int height = bounds.height;

				if (x == 0 && y == 0 && image.getWidth() == width
						&& image.getHeight() == height) {
					return image;
				}
				if (((x + width) > image.getWidth())) {
					width = image.getWidth() - x;
				}
				if (((y + height) > image.getHeight())) {
					height = image.getHeight() - y;
				}

				PlanarImage src1 = getPlanarImage(image);
				PlanarImage src = null;
				try {
					ParameterBlock pb = new ParameterBlock();
					pb.add((float) x);
					pb.add((float) y);
					pb.add((float) width);
					pb.add((float) height);
					pb.addSource(src1);
					src = JAI.create("crop", pb, null);
					pb = new ParameterBlock();
					pb.add((float) -(src.getMinX()));
					pb.add((float) -(src.getMinY()));
					pb.addSource(src);
					return JAI.create("translate", pb);
				} catch (Exception e) {
					return getBlankImage(image.getWidth(), image.getHeight(),
							Color.WHITE, 0);
				} finally {
					disposeTemp(src1);
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getMask()
	 */
	@Override
	public CoreImage extractMask() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				if (getNumBands(image) == 4) {
					int[] bandValue = { 3 };
					ParameterBlock pb = new ParameterBlock();
					PlanarImage src = getPlanarImage(image);
					pb.addSource(src);
					pb.add(bandValue);
					try {
						return JAI.create("bandselect", pb);
					} finally {
						disposeTemp(src);
					}
				}
				return image;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getColoredImage(java.awt.Color,
	 *      boolean)
	 */
	@Override
	public CoreImage overlay(final Color overlayColor,
			final boolean processAlpha) {
		if (overlayColor == null) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				int red = overlayColor.getRed();
				int green = overlayColor.getGreen();
				int blue = overlayColor.getBlue();

				if ((red == 255) && (green == 255) && (blue == 255))
					blue = 254;

				// Raster raster = overlayImage.getData();
				// ColorModel model = overlayImage.getColorModel();

				RenderedImage bimg = null;
				CoreImage alpha = null;
				if (processAlpha) {
					alpha = newInstance(image).extractMask();
					bimg = newInstance(image).reduceToThreeBand().getAwt();

				} else {
					bimg = image;
				}

				if (bimg.getColorModel() instanceof IndexColorModel) {
					IndexColorModel ic = (IndexColorModel) bimg.getColorModel();
					bimg = ic.convertToIntDiscrete(bimg.getData(), false);
				}

				byte lut[][] = new byte[3][256];

				for (int i = 0; i < 256; i++) {
					if (i == 255) {
						lut[0][i] = (byte) 255;
						lut[1][i] = (byte) 255;
						lut[2][i] = (byte) 255;
					} else {
						lut[0][i] = (byte) red;
						lut[1][i] = (byte) green;
						lut[2][i] = (byte) blue;
					}
				}

				LookupTableJAI lookup = new LookupTableJAI(lut);

				PlanarImage src = getPlanarImage(bimg);
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(src);
				pb.add(lookup);
				RenderedImage dst = JAI.create("lookup", pb, null);
				disposeTemp(src);

				if (alpha != null) {
					return newInstance(dst).bandMerge(alpha).getAwt();
				}
				return dst;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getGrayScaleImage()
	 */
	@Override
	public CoreImage convertToGrayScale() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				JAIImage tmp = new JAIImage(image);
				tmp.convertToGray();
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = tmp.getPlanarImage();
				pb.addSource(src);
				pb.add(new int[] { 0 }); // all bands contains
				// same value in Grayscale-image
				try {
					return JAI.create("bandselect", pb); //$NON-NLS-1$
				} finally {
					tmp.disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#invertGrayscaleImage()
	 */
	@Override
	public CoreImage invertGrayScale() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				byte[] lut = new byte[256];
				for (int i = 0; i < 256; i++) {
					lut[i] = (byte) (255 - i);
				}
				LookupTableJAI lookup = new LookupTableJAI(lut);

				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(lookup);
				try {
					return JAI.create("lookup", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#invertSingleBandMask()
	 */
	@Override
	public CoreImage invertSingleBandMask() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				PlanarImage src = getPlanarImage(image);
				try {
					return JAI.create("invert", src);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#makeTransparency(int)
	 */
	@Override
	public CoreImage applyAlpha(final int alphaValue) {
		final int numBands = getNumBands();
		if (numBands < 3) {
			return this;
		}

		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				// Create a constant 1-band byte image to represent the alpha
				// channel. It has the source dimensions and is filled with
				// 255 to indicate that the entire source is opaque(Full
				// visible).
				int i = alphaValue;
				ParameterBlock pb = new ParameterBlock();
				pb.add(new Float(getWidth()));
				pb.add(new Float(getHeight()));
				pb.add(new Byte[] { new Byte((byte) i) });
				PlanarImage alpha = JAI.create("constant", pb);

				// Combine the source and alpha images such that the source
				// image
				// occupies the first band(s) and the alpha image the last band.
				// RenderingHints are used to specify the destination
				// SampleModel and
				// ColorModel.

				pb = new ParameterBlock();
				PlanarImage planar = getPlanarImage(image);
				pb.addSource(planar);
				pb.addSource(planar);
				pb.add(alpha);
				pb.add(alpha);
				pb.add(Boolean.FALSE);
				pb.add(CompositeDescriptor.DESTINATION_ALPHA_LAST);

				SampleModel sm = RasterFactory.createComponentSampleModel(image
						.getSampleModel(), DataBuffer.TYPE_BYTE, image
						.getTileWidth(), image.getTileHeight(), numBands + 1);
				ColorSpace cs = ColorSpace
						.getInstance(numBands == 1 ? ColorSpace.CS_GRAY
								: ColorSpace.CS_sRGB);
				ColorModel cm = RasterFactory.createComponentColorModel(
						DataBuffer.TYPE_BYTE, cs, true, false,
						Transparency.BITMASK);
				ImageLayout il = new ImageLayout();
				il.setSampleModel(sm).setColorModel(cm);
				RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
				try {
					return JAI.create("composite", pb, rh);
				} finally {
					disposeTemp(planar);
					disposeTemp(alpha);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#removeAlpha()
	 */
	@Override
	public CoreImage removeAlpha() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				if (getNumBands(image) == 4) {
					ParameterBlock pb = new ParameterBlock();
					PlanarImage src = getPlanarImage(image);
					pb.addSource(src);
					pb.add(new int[] { 0, 1, 2 });
					try {
						return JAI.create("bandselect", pb);
					} finally {
						disposeTemp(src);
					}
				}
				return image;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#rotate(int)
	 */
	@Override
	public CoreImage rotate(final int degree) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				switch (degree) {
				case 180:
					PlanarImage src = getPlanarImage(image);
					try {
						return JAI.create("transpose", src,
								TransposeDescriptor.FLIP_VERTICAL);
					} finally {
						disposeTemp(src);
					}
				default:
					return image;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#scale(int, int)
	 */
	@Override
	public CoreImage scale(final int width, final int height) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				float w = (float) width / getWidth();
				float h = (float) height / getHeight();
				ParameterBlock pm = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pm.addSource(src);
				pm.add(w);
				pm.add(h);
				try {
					return JAI.create("scale", pm);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#scale(float, float, float,
	 *      float)
	 */
	@Override
	public CoreImage scale(final float magx, final float magy,
			final float transx, final float transy) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(magx);
				pb.add(magy);
				pb.add(transx);
				pb.add(transy);
				pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
				try {
					return JAI.create("scale", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#translate(int, int)
	 */
	@Override
	public CoreImage translate(final int x, final int y) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add((float) x);
				pb.add((float) y);
				try {
					return JAI.create("translate", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#darken(com.nokia.tools.media.image.CoreImage,
	 *      int, int)
	 */
	@Override
	public CoreImage darken(final CoreImage image2, final int x, final int y) {
		if (image2 == null || image2.getAwt() == null) {
			return this;
		}
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				CoreImage tmp2 = newInstance(image2.getAwt());
				if (x != 0 || y != 0) {
					tmp2.relocate(x, y);
				}
				ParameterBlock pm = new ParameterBlock();

				PlanarImage src1 = getPlanarImage(image);
				PlanarImage src2 = getPlanarImage(tmp2.getAwt());

				pm.addSource(src1);
				pm.addSource(src2);
				PlanarImage dst = JAI.create("min", pm, null);

				disposeTemp(src1);
				disposeTemp(tmp2, src2);
				return dst;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#convolve(float[])
	 */
	@Override
	public CoreImage convolve(final float[] data) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				ParameterBlock pm = new ParameterBlock();

				// int l=(int)Math.sqrt(data.length);
				KernelJAI kernel = new KernelJAI(3, 3, data);

				PlanarImage src = getPlanarImage(image);
				pm.add(kernel);
				pm.addSource(src);

				try {
					return JAI.create("convolve", pm, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#median()
	 */
	@Override
	public CoreImage median() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				ParameterBlock pb = new ParameterBlock();
				PlanarImage src = getPlanarImage(image);
				pb.addSource(src);
				pb.add(MedianFilterDescriptor.MEDIAN_MASK_SQUARE);
				pb.add(3);
				try {
					return JAI.create("MedianFilter", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#mean()
	 */
	@Override
	public CoreImage mean() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				PlanarImage src = getPlanarImage(image);
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(src);
				pb.add(3);
				pb.add(3);
				pb.add(1);
				pb.add(1);
				try {
					return JAI.create("boxfilter", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#dilate()
	 */
	@Override
	public CoreImage dilate() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				PlanarImage src = getPlanarImage(image);
				float[] data = new float[9];
				data[0] = 1F;
				data[1] = 1F;
				data[2] = 1F;
				data[3] = 1F;
				data[4] = 1F;
				data[5] = 1F;
				data[6] = 1F;
				data[7] = 1F;
				data[8] = 1F;
				KernelJAI kernel = new KernelJAI(3, 3, data);
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(src);
				pb.add(kernel);
				try {
					return JAI.create("dilate", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#erode()
	 */
	@Override
	public CoreImage erode() {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				PlanarImage src = getPlanarImage(image);
				float[] data = new float[9];
				data[0] = 0F;
				data[1] = 0F;
				data[2] = 0F;
				data[3] = 0F;
				data[4] = 0F;
				data[5] = 0F;
				data[6] = 0F;
				data[7] = 0F;
				data[8] = 0F;
				KernelJAI kernel = new KernelJAI(3, 3, data);
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(src);
				pb.add(kernel);
				try {
					return JAI.create("erode", pb);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#addConst(double[])
	 */
	@Override
	public CoreImage addConst(final double[] constants) {
		return execute(new AWTWork() {
			protected RenderedImage process(RenderedImage image) {
				PlanarImage src = getPlanarImage(image);
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(src);
				pb.add(constants);
				try {
					return JAI.create("addconst", pb, null);
				} finally {
					disposeTemp(src);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getBlankImage(int, int,
	 *      java.awt.Color, int, int)
	 */
	@Override
	public RenderedImage getBlankImage(int width, int height, Color c,
			int opacity, int bands) {
		Byte[] bandValues = new Byte[4];
		Byte alpha1 = new Byte((byte) c.getRed());
		Byte alpha2 = new Byte((byte) c.getGreen());
		Byte alpha3 = new Byte((byte) c.getBlue());
		Byte alpha4 = new Byte((byte) opacity);
		bandValues[0] = alpha1;
		bandValues[1] = alpha2;
		bandValues[2] = alpha3;
		bandValues[3] = alpha4;

		ParameterBlock pb = new ParameterBlock();
		pb.add((float) width);
		pb.add((float) height);
		pb.add(bandValues);

		return (PlanarImage) JAI.create("constant", pb, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getBlankImage(int, int,
	 *      java.awt.Color, int)
	 */
	@Override
	public RenderedImage getBlankImage(int width, int height, Color c,
			int opacity) {
		Byte[] bandValues = new Byte[4];
		if (c == null)
			c = Color.WHITE;
		Byte alpha1 = new Byte((byte) c.getRed());
		Byte alpha2 = new Byte((byte) c.getGreen());
		Byte alpha3 = new Byte((byte) c.getBlue());
		Byte alpha4 = new Byte((byte) opacity);
		bandValues[0] = alpha1;
		bandValues[1] = alpha2;
		bandValues[2] = alpha3;
		bandValues[3] = alpha4;

		ParameterBlock pb = new ParameterBlock();
		pb.add((float) width);
		pb.add((float) height);
		pb.add(bandValues);

		return (PlanarImage) JAI.create("constant", pb, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#getBlankImage(int, int,
	 *      java.awt.Color)
	 */
	@Override
	public RenderedImage getBlankImage(int width, int height, Color c) {
		Byte[] bandValues = new Byte[3];
		if (c == null)
			c = Color.WHITE;
		Byte alpha1 = new Byte((byte) c.getRed());
		Byte alpha2 = new Byte((byte) c.getGreen());
		Byte alpha3 = new Byte((byte) c.getBlue());
		bandValues[0] = alpha1;
		bandValues[1] = alpha2;
		bandValues[2] = alpha3;

		ParameterBlock pb = new ParameterBlock();
		pb.add((float) width);
		pb.add((float) height);
		pb.add(bandValues);

		return (PlanarImage) JAI.create("constant", pb, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#saveBmp(java.io.OutputStream)
	 */
	@Override
	protected void saveBmp(OutputStream out) throws IOException {
		RenderedImage awt = getAwt();
		PlanarImage srcImage;
		if (!(awt instanceof PlanarImage)) {
			if (awt == null) return;
			srcImage = new RenderedImageAdapter(awt);
		} else {
			srcImage = (PlanarImage) awt;
		}
		RenderedImage image = null;
		try {
			ColorModel srcCM = srcImage.getColorModel();
			if (srcCM != null && srcCM instanceof IndexColorModel) {
				IndexColorModel srcICM = (IndexColorModel) srcCM;
				int size = srcICM.getMapSize();
				byte[] r = new byte[size];
				byte[] g = new byte[size];
				byte[] b = new byte[size];
				srcICM.getReds(r);
				srcICM.getGreens(g);
				srcICM.getBlues(b);
				ColorModel dstCM = new IndexColorModel(8, size, r, g, b);
				Raster srcData = srcImage.getData();
				WritableRaster dstData = srcData
						.createCompatibleWritableRaster();
				dstData.setRect(srcData);
				image = new BufferedImage(dstCM, dstData, dstCM
						.isAlphaPremultiplied(), null);
			}
			if (srcCM != null && srcCM instanceof DirectColorModel) {
				int bands = getNumBands(srcImage);
				if (bands > 3) {
					PlanarImage tmp = JAI.create("BandSelect", srcImage,
							new int[] { 0, 1, 2 });
					disposeTemp(srcImage);
					srcImage = tmp;
				} else if (bands == 2) {
					PlanarImage tmp = JAI.create("BandSelect", srcImage,
							new int[] { 0 });
					disposeTemp(srcImage);
					srcImage = tmp;
				}
				BufferedImage srcbImage = getBufferedImage(srcImage);
				int wid = srcbImage.getWidth(), ht = srcbImage.getHeight();
				byte data1[] = new byte[wid * ht * 3];
				SampleModel sm = srcbImage.getSampleModel();
				WritableRaster wr = srcbImage.getRaster();
				DataBuffer db = wr.getDataBuffer();
				int numbands = wr.getNumBands();
				int sample[] = new int[numbands];
				for (int i = 0; i < ht; i++) {
					for (int j = 0; j < wid; j++) {
						int pix[] = null;
						sample = (sm.getPixel(j, i, pix, db));
						for (int l = 0; l < numbands; l++) {
							data1[i * wid * 3 + (j * 3) + l] = (byte) sample[l];
						}
					}
				}
				image = createInterleavedRGBImage(wid, ht, 8, data1, false,
						false);
			} else {
				// dstCM = srcCM;
				int bands = getNumBands(srcImage);
				if (bands > 3) {
					image = JAI.create("BandSelect", srcImage, new int[] { 0,
							1, 2 });
					disposeTemp(srcImage);
				} else if (bands == 2) {
					image = JAI.create("BandSelect", srcImage, new int[] { 0 });
					disposeTemp(srcImage);
				} else {
					image = srcImage;
				}
			}
			if (image == null) {
				throw new NullPointerException("Image is null.");
			}

			// Create a BMPEncoder object. It transform BMP image into output
			// stream.
			BMPEncodeParam param = new BMPEncodeParam();

			// createImageEncoder of ImageCodec class creates the encoder for
			// the
			// given image format (BMP) using the BMPEncoder Object.
			ImageEncoder encoder = ImageCodec.createImageEncoder(TYPE_BMP, out,
					param);

			// encode() method encodes the image and write it back to output
			// stream.
			encoder.encode(image);
		} finally {
			disposeTemp(image);
			disposeTemp(srcImage);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.media.image.CoreImage#saveTiff(java.io.OutputStream)
	 */
	@Override
	protected void saveTiff(OutputStream out) throws IOException {
		PlanarImage image = getPlanarImage(getAwt());
		try {
			if (getNumBands(image) > 3) {
				image = JAI.create("BandSelect", image, new int[] { 0, 1, 2 });
			}

			// Create a TIFFEncoder object. It transform TIFF image into output
			// stream.
			TIFFEncodeParam param = new TIFFEncodeParam();

			// createImageEncoder of ImageCodec class creates the encoder for
			// the
			// given image format (TIFF) using the TIFFEncoder Object.
			ImageEncoder encoder = ImageCodec.createImageEncoder(TYPE_TIFF,
					out, param);

			// encode() method encodes the image and write it back to output
			// stream.
			encoder.encode(image);
		} finally {
			disposeTemp(image);
		}
	}
}
