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
package com.nokia.tools.theme.core;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nokia.tools.media.utils.UtilsPlugin;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor;
import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeGraphic;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.editing.BasicEntityImage;
import com.nokia.tools.theme.editing.BasicImageLayer;

/**
 * Represents a nine piece element. Note that for multi-piece elements like
 * nine piece elements, following features behave differently:
 * Layers View
 * Icon View
 * Extended Copy Paste Availability
 * Packaging
 * Search View
 *
 */
public class NinePieceElement implements IMultiPiece {
	
	private final int PART_COUNT = 9;
	private final String ELEMENT_TYPE_ID = "9 Piece Bitmap"; 
	public static final String PIECE_COPY_INFO = "pieceCopy";
	private final String SEARCH_VIEW_TEXT = "Nine piece";
	
	private static final String[] PART_NAMES = new String[] { "Left", "Right", "Top",
		"Bottom", "Top-Left", "Top-Right", "Bottom-Left", "Bottom-Right",
	"Center" };
	
	private static final String[] PART_IDS = {
		"L", "R", "T", "B", "Tl", "Tr",
		"Bl", "Br", "C" };
	
	public boolean isFrameRequired() {
		return true;
	}
	
	public boolean hasElementImages(List<File> files) {
		boolean hasElementImages = false;
		boolean[] partFound = new boolean[PART_COUNT];
		for (int i = 0; i < PART_COUNT; i++) {
			partFound[i] = false;
		}
		
		for (int i = 0; i < PART_IDS.length; i++) {
			String posKey = "_" + PART_IDS[i];
						
			for (int j = 0; j < files.size(); j++) {
				File image = (File) files.get(i);
				String partId = image.getName().toLowerCase();
					
				//Not sure if the second part of the condition is correct.
				if ((partId.endsWith(posKey))
				    || (partId.contains(posKey + "_"))) {
					partFound[i] = true;
					break;
				}
			}
		}
		
		//Have we found all the parts? if not, return false.
		for (int i = 0; i < PART_IDS.length; i++) {
			if (partFound[i] != true) {
				hasElementImages = false;
				break;
			}
		}
		return hasElementImages;
	}
	
	public boolean isFrameInputsAvailable(List<SkinnableEntity> parts) {
		
		boolean frameInputsAvailable = true;
		
		//Let's have partFound to indicate that the expected parts are found.
		//Initialized to false explicitly.
		boolean[] partFound = new boolean[parts.size()];
		for (int i = 0; i < parts.size(); i++) {
			partFound[i] = false;
		}
		
		//Lets take each part id and ensure we have all the parts.
		for (int i = 0; i < parts.size(); i++) {
			SkinnableEntity nextPart = (SkinnableEntity) parts.get(i);
			String partId = nextPart.getIdentifier().toLowerCase();
			for (int j = 0; j < PART_IDS.length; j++) {
				String posKey = "_" + PART_IDS[j];
				if ((partId.endsWith(posKey))
				    || (partId.contains(posKey + "_"))) {
					partFound[i] = true;
					break;
				}
			}
		}
		
		//Have we found all the parts? if not, return false.
		for (int i = 0; i < partFound.length; i++) {
			if (partFound[i] != true) {
				frameInputsAvailable = false;
				break;
			}
		}
		
		return frameInputsAvailable;
	}

	public boolean isCopyImageAvailable() {		
		return true;
	}
	
	public String getPartName(String partId) {
		String partName = "";
		int i=0;
		for (String part_id: PART_IDS) {
			if (part_id.equalsIgnoreCase(partId)) {
				partName = PART_NAMES[i];
			}
			i++;
		}
		return partName;
	}
	
	public String getSearchViewText() {
		return SEARCH_VIEW_TEXT;
	}
	
	public String getCopyElementInfo() {
		return UtilsPlugin.PLUGIN_ID + "." + PART_COUNT +  PIECE_COPY_INFO;
	}
	
	public String getElementTypeId() {
		return ELEMENT_TYPE_ID;
	}

	public int getPartCount() {
		return PART_COUNT;
	}

	public ContentType getClipboardContentDescriptorType() {
		return ClipboardContentDescriptor.ContentType.CONTENT_GRAPHICS_NINEPIECE;
	}

	public String[] getPartNames() {
		return PART_NAMES;
	}
	
	public int getPartIndex(String partName) {
		ArrayList<String> myList = new ArrayList<String>(Arrays.asList(PART_NAMES));
		return myList.indexOf(partName);
	}
		
	private void handlePasteNinePiece(Object clipboardData, IImage emodel) {
		try {

			// unwrap if needed
			if (clipboardData instanceof ClipboardContentDescriptor)
				clipboardData = ((ClipboardContentDescriptor) clipboardData)
				    .getContent();

			List<IImage> parts = emodel.getPartInstances();

			IImage[] sorted = new IImage[PART_COUNT];
			for (int i = 0; i < parts.size(); i++) {
				int idx = getPartIndex(parts.get(i)
				    .getPartType());
				sorted[idx] = parts.get(i);
			}

			parts = Arrays.asList(sorted);

			List partData = (List) clipboardData;
			for (int i = 0; i < parts.size(); i++) {
				Object _obj = partData.get(i);
				if (_obj instanceof File) {
					/*
					 * in clipboard is set of 9 or more images
					 */
					File image = (File) partData.get(i);
					((BasicImageLayer) parts.get(i).getLayer(0))
					    .clearLayer();
					((BasicImageLayer) parts.get(i).getLayer(0))
					    .paste(image);

					// mask?
					int pos = i + 9;
					if (pos < partData.size()) {
						File mask = (File) partData.get(pos);

						if (mask != null && mask.isFile()) {
							if (mask.getName().toLowerCase().endsWith(".bmp"))
								parts.get(i).getLayer(0).pasteMask(mask);
						} else {
							((BasicImageLayer) parts.get(i).getLayer(0))
							    .clearMask();
						}
					} else {
						((BasicImageLayer) parts.get(i).getLayer(0))
						    .clearMask();
					}

					BasicEntityImage part = (BasicEntityImage) parts
					    .get(i);
					ThemeGraphic tg = part.getSavedThemeGraphics(false);
					tg.setAttribute(ThemeTag.ATTR_STATUS,
					    ThemeTag.ATTR_VALUE_ACTUAL);
					part.getEntity().setActualGraphic(tg);
				}
				if (_obj instanceof ThemeGraphic) {
					/*
					 * in clipboard is list of instances of ThemeGraphic, result
					 * of 'Copy Layers'
					 */
					ThemeGraphic newTg = (ThemeGraphic) ((ThemeGraphic) _obj)
					    .clone();
					// here result to the proper index, either check the entity
					// itself or use the name, we go to the second, even better
					// the graphics should be sorted using the same way when set
					// in the clipboard
					String name = getFriendlyName(newTg
					    .getData());
					int index = getPartIndex(newTg
					    .getData());
					if (index < 0) {
						
						   // .error("Not able to resolve index by name: " + name);
					} else {
						BasicEntityImage part = (BasicEntityImage) parts
						    .get(index);
						newTg.setAttribute(ThemeTag.ATTR_STATUS,
						    ThemeTag.ATTR_VALUE_ACTUAL);
						part.getEntity().setActualGraphic(newTg);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
	}	
	private static String getFriendlyName(ThemeBasicData data) {
		String s = data.getAttributeValue("name");		
		
		String result = MultiPieceManager.getPartName(s, data.getParent().getCurrentProperty());
		return result.toString();
	}
	public int getPartIndex(ThemeBasicData data) {
		String name = getFriendlyName(data);
		return getPartIndex(name);
	}
	
	public void setDimensions(List<IImage> partInstances) {
		// compute part's bounds
		int x1, x2, y1, y2;

		BasicEntityImage tl = (BasicEntityImage) partInstances
				.get(0);
		BasicEntityImage t = (BasicEntityImage) partInstances.get(1);
		BasicEntityImage tr = (BasicEntityImage) partInstances
				.get(2);

		BasicEntityImage l = (BasicEntityImage) partInstances.get(3);
		BasicEntityImage c = (BasicEntityImage) partInstances.get(4);
		BasicEntityImage r = (BasicEntityImage) partInstances.get(5);

		BasicEntityImage bl = (BasicEntityImage) partInstances
				.get(6);
		BasicEntityImage b = (BasicEntityImage) partInstances.get(7);
		BasicEntityImage br = (BasicEntityImage) partInstances
				.get(8);

		x1 = tl.getWidth();
		y1 = tl.getHeight();

		x2 = x1 + c.getWidth();
		y2 = y1 + c.getHeight();

		tl.setBounds(new Rectangle(0, 0, x1, y1));
		t.setBounds(new Rectangle(x1, 0, t.getWidth(), t.getHeight()));
		tr.setBounds(new Rectangle(x2, 0, tr.getWidth(), tr.getHeight()));
		l.setBounds(new Rectangle(0, y1, l.getWidth(), l.getHeight()));
		c.setBounds(new Rectangle(x1, y1, c.getWidth(), c.getHeight()));
		r.setBounds(new Rectangle(x2, y1, r.getWidth(), r.getHeight()));
		bl.setBounds(new Rectangle(0, y2, bl.getWidth(), bl.getHeight()));
		b.setBounds(new Rectangle(x1, y2, b.getWidth(), b.getHeight()));
		br.setBounds(new Rectangle(x2, y2, br.getWidth(), br.getHeight()));
	}

	//Rendering
	public Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h) {
		
		Dimension dims[] = new Dimension[PART_COUNT];
		int i = 0;
		for (Layout l: layouts) {
			Dimension d = new Dimension(l.W(), l.H());
			dims[i++] = d;
		}

		Layout t = (Layout) layouts
				.get(1);
		Layout l = (Layout) layouts
				.get(3);
		Layout c = (Layout) layouts
				.get(4);
		Layout r = (Layout) layouts
				.get(5);
		Layout b = (Layout) layouts
				.get(7);

		// check, that parts layout matches element dimensions
		float xRatio, yRatio;

		xRatio = (float) w / (l.W() + c.W() + r.W());
		yRatio = (float) h / (t.H() + c.H() + b.H());

		if (xRatio != 1 || yRatio != 1) {
			int desiredWidth = w;
			int desiredHeight = h;

			int desiredCenterW = desiredWidth - l.W() - r.W();
			int desiredCenterH = desiredHeight - t.H() - b.H();
								
			//t.setW(desiredCenterW);
			dims[1].width = desiredCenterW;
			
			//l.setH(desiredCenterH);
			dims[3].height = desiredCenterH;
			
			//c.setW(desiredCenterW);
			dims[4].width = desiredCenterW;
			
			//c.setH(desiredCenterH);
			dims[4].height = desiredCenterH;
			
			//r.setH(desiredCenterH);
			dims[5].height = desiredCenterH;
			
			//b.setW(desiredCenterW);					
			dims[7].width = desiredCenterW;

		}
		return dims;		
	}

	public boolean supportsConversionTo(int targetPartCount) {
		if ((targetPartCount == 1) || (targetPartCount == 9)) return true;
		return false;
	}
	
	//Packaging
	
	
}
