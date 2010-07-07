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
/*
 * ======================================================================================
 * The file provides bitmap writing functionality
 * ======================================================================================
 */
package com.nokia.tools.theme.s60.siscreation;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.nokia.tools.media.image.CoreImage;
import com.nokia.tools.platform.theme.ImageLayer;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeException;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.ThemeTag.SkinCompliance;
import com.nokia.tools.theme.s60.S60ThemePlugin;

/**
 * The file provides bitmap writing functionality
 */
public class BitmapItemDefinition extends BasicItemDefinition {

	// Data members
	String bitmapFile = null;
	String hardMaskFile = null;
	String softMaskFile = null;
	String attributeString = null;
	String colourDepth = "c16";
	String defaultAttr = ThemeTag.SKN_TAG_STRETCH;
	
	// If any special stretch info is specified then we use it as it is.
	// This is needed as the stetch attribute value does not get stored into
	// the tdf file and hence we cannot use it. 
	// We have thus introduced a special attribute for storing the stretch info
	// and this will be stored in the tdf file and will be used when we do the
	// packaging [if not null by specifying this in the BaseGraphics.xml file] instead 
	// of the default ThemeTag.SKN_TAG_STRETCH attribute. 
	private static final String SPECIAL_STRETCH_INFO = "specialStretchInfo";

	// If any special Soft Mask Info needs to be passed on than the default one 
	// of SOFTMASK for svg file then it needs to be mentioned with this attribute value
	// in the BaseGraphics.xml file and this will be used during packaging.
	private static final String SVG_SOFT_MASK_INFO = "svgsoftmaskInfo";
	
	/**
	 * Constructor
	 */
	protected BitmapItemDefinition(Map properties) throws ThemeException {
		super(properties);
	}

	/**
	 * Resets the internal data structures (As good as creating a new class and
	 * starting over)
	 */
	protected void reset() {
		super.reset();
		attributeString = null;
		bitmapFile = null;
		hardMaskFile = null;
		softMaskFile = null;
		colourDepth = "c16";
	}

	/**
	 * Sets the default attribute string - if set to null the no default
	 * attribute is written to bitmap definition string
	 */
	protected void setDefaultAttributeString(String attr) {
		defaultAttr = attr;
	}

	/**
	 * Sets the attribute string that will override the attribute data
	 * determined from itemgraphics defintion. NOTE that the phone accepts only
	 * one image attribute.
	 */
	protected void attributeString(String attrName, String attrValue) {
		if (!ThemeTag.SKN_TAG_COORDS.equals(attrName)
				&& !ThemeTag.SKN_TAG_SIZE.equals(attrName)
				&& !ThemeTag.SKN_TAG_STRETCH.equals(attrName)
				&& !ThemeTag.SKN_TAG_TILE.equals(attrName)
				&& !ThemeTag.SKN_TAG_TILEX.equals(attrName)
				&& !ThemeTag.SKN_TAG_TILEY.equals(attrName)
				&& !ThemeTag.SKN_TAG_ALIGN.equals(attrName))
			return; // dont accept the value

		this.attributeString = attrName;
		if (attrValue != null && !(attrValue.equalsIgnoreCase(""))) {
			attributeString = attributeString + EQUAL + attrValue;
		}
	}

	/**
	 * Generates the bitmap item definition
	 * 
	 * @param cEntity The bitmap entity whose definition has to be written
	 * @return The string buffer containing the bitmap definition.
	 * @throws ThemeException
	 */
	protected StringBuffer generateDefinition(SkinnableEntity cEntity)
			throws ThemeException {
		if (cEntity == null)
			return null;

		String itemId = (String) cEntity.getAttributeValue(ThemeTag.ATTR_ID);
		if (itemId == null) {
			errors.append("Item id is null for input entity");
			return null;
		}

		StringBuffer bitmapWithMaskDefn = generateBitmapDefinition(cEntity);
		if (bitmapWithMaskDefn == null)
			return null;

		StringBuffer bitmapDefn = new StringBuffer();
		bitmapDefn.append(ThemeTag.SKN_TAG_BITMAP).append(SPACE);
		bitmapDefn.append(ThemeTag.SKN_TAG_IID).append(EQUAL).append(
				makeItemId(itemId));
		bitmapDefn.append(SPACE).append(bitmapWithMaskDefn).append(NL);

		return bitmapDefn;
	}

	/**
	 * In the bitmap defintion format BITMAP IID=itemdId <bitmapsourcewithmask>
	 * This function generates the <bitmapsourcewithmask> definition See section
	 * 3.2.12 of the skin descriptor source file format specification
	 * 
	 * @param cEntity The entity whose bitmapsourcewithmask definition has to be
	 *            generated
	 */
	protected StringBuffer generateBitmapDefinition(SkinnableEntity cEntity)
			throws ThemeException {
		if (cEntity == null)
			return null;

		ThemeGraphic itemGraphics = cEntity.getPhoneThemeGraphic();
		if (itemGraphics == null) {
			return null;
		}

		StringBuffer bitmapDefn = generateBitmapDefinition(cEntity,
				itemGraphics, false);
		return bitmapDefn;
	}

	/**
	 * In the bitmap defintion format BITMAP IID=itemdId <bitmapsourcewithmask>
	 * This function generates the <bitmapsourcewithmask> definition See section
	 * 3.2.12 of the skin descriptor source file format specification
	 * 
	 * @param cEntity The entity whose bitmapsourcewithmask definition has to be
	 *            generated
	 * @param cItemGrp The theme graphics object to be processed
	 * @param changeName If true then prefixs 'tsimage' to the name (to prevent
	 *            the generated name from clashing with a known iid).
	 */
	protected StringBuffer generateBitmapDefinition(SkinnableEntity cEntity,
			ThemeGraphic cItemGrp, boolean changeName) throws ThemeException {

		

		StringBuffer itemDefinition = null;
		SkinCompliance compliance = getComplianceLevel();
		colourDepth = getColourDepth(cEntity); // picked from the properties if
		// cEntity is null

		switch (compliance) {
		case BITMAP_SKIN:
			itemDefinition = generateDefinitionForBitmapSkin(cEntity, cItemGrp,
					changeName);
			break;
		case SCALEABLE_SKIN:
			itemDefinition = generateDefinitionForScaleableSkin(cItemGrp,
					changeName);
			break;
		}

		return itemDefinition;
	}

	/**
	 * Writes bitmap definition
	 */
	private StringBuffer generateDefinitionForScaleableSkin(
			ThemeGraphic cItemGrp, boolean changeName) throws ThemeException {

		if (cItemGrp == null) {
			return null;
		}

		ImageLayer layer0 = getLayer0(cItemGrp);
		if (layer0 == null)
			return null;

		bitmapFile = (String) layer0.getAttribute(ThemeTag.FILE_NAME);
		hardMaskFile = (String) layer0.getAttribute(ThemeTag.ATTR_HARDMASK);
		softMaskFile = (String) layer0.getAttribute(ThemeTag.ATTR_SOFTMASK);

		String suggestedBitmapFile = (changeName == true) ? "tsimage_"
				+ bitmapFile : bitmapFile;
		if (bitmapFile == null)
			return null;
		String savedFile = null;
		try{
			savedFile = copyFile(bitmapFile, suggestedBitmapFile);
		}catch(Exception e){
			return null; 
		}
		if (savedFile == null || savedFile.trim().length() <= 0) {
			errors.append("Unable to save the file ").append(bitmapFile);
			return null;
		}
		bitmapFile = savedFile;
		colourDepth = getColourDepth((SkinnableEntity)cItemGrp.getData());
		if (bitmapFile.toLowerCase().endsWith(BMP_EXT)) {

			if (softMaskFile != null) {
				String suggestedSoftMaskFileName = generateMaskFileName(
						bitmapFile, true);
				softMaskFile = copyFile(softMaskFile, suggestedSoftMaskFileName);
				hardMaskFile = null;
			} else if (hardMaskFile != null) {
				String suggestedHardMaskFileName = generateMaskFileName(
						bitmapFile, false);
				hardMaskFile = copyFile(hardMaskFile, suggestedHardMaskFileName);
				softMaskFile = null;
			}
		} else { // Svg file
			hardMaskFile = null;
			softMaskFile = null;
		}

		Map layerAttr = layer0.getAttributes();
		setAttributeString(layerAttr);

		StringBuffer bitmapDefn = generateBitmapWithMaskString(cItemGrp);

		
		return bitmapDefn;
	}

	/**
	 * Writes bitmap definition
	 */
	private StringBuffer generateDefinitionForBitmapSkin(
			SkinnableEntity cEntity, ThemeGraphic cThmGrp, boolean changeName)
			throws ThemeException {

		if (cEntity == null)
			return null;

		if (cThmGrp == null) {
			return null;
		}

		CoreImage finalPlImage = CoreImage.create(cThmGrp.getProcessedImage(
				cEntity, (String) null, true, true));
		if (finalPlImage.getAwt() == null)
			return null;

		CoreImage finalPlMask = finalPlImage.copy().extractMask();


		
		String outputDir = (String) properties.get(ThemeTag.KEY_OUTPUT_DIR);
		outputDir = (outputDir == null) ? "." : outputDir;

		String ident = cEntity.getIdentifier();
		ident = (ident != null) ? ident : "image";
		ident = ident.trim().replace(' ', '_');
		ident = (changeName == true) ? "tsimage_" + ident : ident;

		String sourceName = ident + BMP_EXT;

		try {
			File outDirFile = new File(outputDir);
			File sourceFile = File.createTempFile(ident, BMP_EXT, outDirFile);
			sourceName = sourceFile.getName();
		} catch (IOException e) {
			e.printStackTrace();
			errors.append(ident).append(" :Unable to save file in ").append(
					outputDir);
			return null;
		}

		String maskName = generateMaskFileName(sourceName, true);

		try {
			finalPlImage.save(CoreImage.TYPE_BMP, new File(outputDir
					+ File.separator + sourceName));
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}
		try {
			finalPlMask.save(CoreImage.TYPE_BMP, new File(outputDir
					+ File.separator + maskName));
		} catch (Exception e) {
			S60ThemePlugin.error(e);
		}

		colourDepth = getColourDepth(cEntity);
		bitmapFile = sourceName;
		softMaskFile = maskName;
		hardMaskFile = null;

		setAttributeString(null);
		StringBuffer bitmapDefn = generateBitmapWithMaskString(cThmGrp);


		return bitmapDefn;
	}

	/**
	 * In the bitmap defintion format BITMAP IID=itemdId <bitmapsourcewithmask>
	 * This function generates the <bitmapsourcewithmask> definition See section
	 * 3.2.12 of the skin descriptor source file format specification
	 */
	private StringBuffer generateBitmapWithMaskString(ThemeGraphic themeGraphic) throws ThemeException {

		bitmapFile = bitmapFile.trim().toLowerCase();
		String maskString = null;
		
		if (bitmapFile.endsWith(BMP_EXT.toLowerCase())) { // it is a bitmap
			if (softMaskFile != null)
				maskString = ThemeTag.SKN_TAG_SOFTMASK + EQUAL + softMaskFile;
			else if (hardMaskFile != null)
				maskString = ThemeTag.SKN_TAG_MASK + EQUAL + hardMaskFile;
		} else { // not a bitmap
			
			if(themeGraphic != null){
				List<ImageLayer> imageLayers = themeGraphic.getImageLayers();
				if(imageLayers != null && imageLayers.size()>=1){
					maskString = imageLayers.get(0).getAttribute(SVG_SOFT_MASK_INFO);
				}
			}

			if(maskString == null)
				maskString = ThemeTag.SKN_TAG_SOFTMASK;
		}

		StringBuffer bitmapDefinition = new StringBuffer();
		bitmapDefinition.append(colourDepth).append(SPACE);
		bitmapDefinition.append(bitmapFile).append(SPACE);
		if (maskString != null)
			bitmapDefinition.append(maskString);

		if (attributeString != null)
			bitmapDefinition.append(SPACE).append(attributeString);

		return bitmapDefinition;
	}

	/*
	 * Sets the image attribute definition string
	 */
	private void setAttributeString(Map imageAttr) throws ThemeException {
		if (attributeString != null) // explicitly set by the calling agent
			return;

		String temp = null;
		String userSetAttrData = null;

		if (imageAttr != null && imageAttr.size() > 0) {
			// Check if tile is specified - otherwise write stretch by default
			temp = (String) imageAttr.get(ThemeTag.ATTR_TILE);
			boolean tile = (temp == null) ? false : Boolean.valueOf(temp)
					.booleanValue();
			if (tile)
				userSetAttrData = ThemeTag.SKN_TAG_TILE;

			temp = (String) imageAttr.get(ThemeTag.ATTR_TILEX);
			boolean tilex = (temp == null) ? false : Boolean.valueOf(temp)
					.booleanValue();
			if (tilex)
				userSetAttrData = ThemeTag.SKN_TAG_TILEX;

			temp = (String) imageAttr.get(ThemeTag.ATTR_TILEY);
			boolean tiley = (temp == null) ? false : Boolean.valueOf(temp)
					.booleanValue();
			if (tiley)
				userSetAttrData = ThemeTag.SKN_TAG_TILEY;

		}

		if (userSetAttrData == null && defaultAttr != null) {
			
			Object stretchInfoValue = null;
			//Added to ensure that null pointer exception is avoided while
			//packaging special attributes. 
			if(imageAttr != null)
				stretchInfoValue = imageAttr.get(SPECIAL_STRETCH_INFO);
			if(stretchInfoValue != null){
				userSetAttrData = stretchInfoValue.toString();
			}
			else
				userSetAttrData = defaultAttr;
		}

		

		attributeString = userSetAttrData;
	}

}
