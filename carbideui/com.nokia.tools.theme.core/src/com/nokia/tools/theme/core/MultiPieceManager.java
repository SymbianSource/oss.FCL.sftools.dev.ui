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
import java.util.HashMap;
import java.util.List;

import com.nokia.tools.media.utils.clipboard.ClipboardContentDescriptor.ContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.platform.layout.Layout;
import com.nokia.tools.platform.theme.Element;

/**
 * Also @see 
 * ClipboardContentDescriptor
 * ClipboardHelper
 * ***PieceConvertAction.java
 * *PieceOperationConfirmationDialog.java
 *
 */
public class MultiPieceManager {

	final static IMultiPiece[] KNOWN_ELEMENT_TYPES = {
				new NinePieceElement(), 
				new ThreePieceElement(), 
				new ElevenPieceElement(),
				new ThreePieceVerticalElement()
			};
	
	final static String UNKNOWN_ELEMENT_TYPE = "Not a multipiece element"; 
	
	public static final String COPY_CLIPBOARD_MESSAGE = "Clipboard now contains a set of images from element's Parts. This content can be pasted only to another similar multi-piece element. This way all the parts of multi-piece element can be altered using a single operation.";
	
	public static String getCopyMessageText() {
		return COPY_CLIPBOARD_MESSAGE;
	}
	
	public static boolean supportsConversion(int sourcePartCount, int targetPartCount) {
		boolean supportsConversion = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(sourcePartCount);
		if (knownType != null) {
			supportsConversion = knownType.supportsConversionTo(targetPartCount);
		}
		return supportsConversion;
	}
	
	/**
	 * Extended Copy Paste - Option to hide/show
	 * @return
	 */
	public static boolean isCopyImageAvailable(int partCount) {
		boolean available = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
			available = knownType.isCopyImageAvailable();
		}
		return available;
	}
	
	public static String getSearchViewText(String elementTypeId) {
		String searchViewText = "";
		IMultiPiece knownType = getKnownMultiPieceElementType(elementTypeId);
		if (knownType != null) {
			searchViewText = knownType.getSearchViewText();
		}
		return searchViewText;
	}
	
	public static String getPartName(String partId, String elementTypeId) {
		String partName="";
		IMultiPiece knownType = getKnownMultiPieceElementType(elementTypeId);
		if (knownType != null) {
			partName = knownType.getPartName(partId);
		}
		return partName;
	}
	
    public static String[] getAllKnownElementTypeIds() {
    	String[] allKnownElementTypeIds = new String[KNOWN_ELEMENT_TYPES.length];
    	int i=0;
    	for(IMultiPiece knownType:KNOWN_ELEMENT_TYPES) {
    		allKnownElementTypeIds[i++] = knownType.getElementTypeId();
    	}
    	return allKnownElementTypeIds;
    }
    
    @Deprecated
    public static String[] getPartNames(int partCount) {
		String[] partNames = null;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
			partNames = knownType.getPartNames();
		}		
		return partNames;
	}
	
	public static String getElementTypeId(int partCount) {
		String elementTypeId = UNKNOWN_ELEMENT_TYPE;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
				elementTypeId = knownType.getElementTypeId();
		}		
		return elementTypeId;
	}
	
	public static int getElementPartCount(String elementTypeId) {
		int elementPartCount = 0;
		IMultiPiece knownType = getKnownMultiPieceElementType(elementTypeId);
		if (knownType != null) {
			elementPartCount = knownType.getPartCount();
		}
		return elementPartCount;
	}
	
	public static boolean isMultiPiece(int partCount) {
		boolean isMultiPiece = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
			isMultiPiece = true;
		}		
		return isMultiPiece;
	}
	
	public static boolean isMultiPiece(String elementTypeId) {
		boolean isMultiPiece = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(elementTypeId);
		if (knownType != null) {
			isMultiPiece = true;
		}		
		return isMultiPiece;
	}
	
	public static boolean isMultiPiece(ContentType contentType) {
		boolean isMultiPiece = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(contentType);
		if (knownType != null) {
			isMultiPiece = true;
		}		
		return isMultiPiece;
	}
	
	private static IMultiPiece getKnownMultiPieceElementType(ContentType contentType) {
		IMultiPiece multiPiece = null;
		for(IMultiPiece knownType:KNOWN_ELEMENT_TYPES) {
			if (knownType.getClipboardContentDescriptorType().toString().equals(contentType.toString())) {
				multiPiece = knownType;
			}
		}
		return multiPiece;
	}

	public static ContentType getClipboardContentDescriptorType(int partCount) {
		ContentType contentType = null;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
			contentType = knownType.getClipboardContentDescriptorType();
		}		
		return contentType;
	}
	
	private static IMultiPiece getKnownMultiPieceElementType(int partCount) {
		IMultiPiece multiPiece = null;
		for(IMultiPiece knownType:KNOWN_ELEMENT_TYPES) {
			if (knownType.getPartCount() == partCount) {
				multiPiece = knownType;
			}
		}
		return multiPiece;
	}
	
	private static IMultiPiece getKnownMultiPieceElementType(String elementTypeId) {
		IMultiPiece multiPiece = null;
		for(IMultiPiece knownType:KNOWN_ELEMENT_TYPES) {
			if (knownType.getElementTypeId().equals(elementTypeId)) {
				multiPiece = knownType;
			}
		}
		return multiPiece;
	}
	
	public static String getCopyElementInfo(int partCount) {
		String copyElementInfo = null;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
			copyElementInfo = knownType.getCopyElementInfo();
		}
		return copyElementInfo;
	}
	
	public static String getCopyElementInfo(String elementTypeId) {
		String copyElementInfo = null;
		IMultiPiece knownType = getKnownMultiPieceElementType(elementTypeId);
		if (knownType != null) {
			copyElementInfo = knownType.getCopyElementInfo();
		}
		return copyElementInfo;
	}
	
	public static boolean isFrameRequired(String elementTypeId) {
		boolean isFrameRequired = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(elementTypeId);
		if (knownType != null) {
			isFrameRequired = knownType.isFrameRequired();
		}		
		return isFrameRequired;
	}

	public static boolean isFrameRequired(int partCount) {
		boolean isFrameRequired = false;
		IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
		if (knownType != null) {
			isFrameRequired = knownType.isFrameRequired();
		}		
		return isFrameRequired;		
	}
	
	public static Dimension[] patchPieceLayout(List<Layout> layouts, int w, int h) {		
		Dimension[] dims = null;
		if (layouts == null) return null;
		IMultiPiece knownType = getKnownMultiPieceElementType(layouts.size());
		if (knownType != null) {
			dims = knownType.patchPieceLayout(layouts, w, h);		
		}
		return dims;
	}
	
	public static void setDimensions(List<IImage> partInstances) {
		
		//Pre-Condition
		if (partInstances == null) return;
		
		IMultiPiece knownType = getKnownMultiPieceElementType(partInstances.size());
		if (knownType != null) {
			knownType.setDimensions(partInstances);
		}
	}
	
	public static int getPartIndex(String partName, int partCount) {
		int partIndex = 0;
		for (IMultiPiece knownType : KNOWN_ELEMENT_TYPES) {
			if (knownType.getPartCount() == partCount) {
				partIndex = knownType.getPartIndex(partName);
				if (partIndex != -1) {
					break;
				}
			}
		}
		return partIndex;
	}
	
	public static int getPartIndex(String partName, String currentProperty) {
		int partIndex = 0;
		IMultiPiece knownType = getKnownMultiPieceElementType(currentProperty);
		if (knownType != null) {
			partIndex = knownType.getPartIndex(partName);
		}
		return partIndex;
	}

	/**
	 * currentProperty is one of the strings that identifies if the element is a multipiece element. 
	 * @param elementEntity whose property is to be set
	 * @param partCount 
	 */
	public static void setCurrentProperty(Element elementEntity, int partCount) {
		List properties = elementEntity.getProperties();
		if (properties != null) {
			for (int i=0; i<properties.size();i++) {
				HashMap<String, String> property = (HashMap<String, String>) properties.get(i);
				String name =(String) property.get("name");
				if (name != null) {
					IMultiPiece knownType = getKnownMultiPieceElementType(name);
					if (knownType != null) {
						if (knownType.getPartCount() == partCount) {
							elementEntity.setCurrentProperty(knownType.getElementTypeId());
							return;
						}
					}
				}
			}
		}
		
			IMultiPiece knownType = getKnownMultiPieceElementType(partCount);
			if (knownType != null) {
					elementEntity.setCurrentProperty(knownType.getElementTypeId());
			}	
	}

	/**
	 * Usage of property tag in defaultdesign.xml to identify the element type is adviced. Do not
	 * use partcount to identify. Note that there are two different types of three piece elements.
	 * 
	 * @see patchPieceLayoutList<Layout> layouts, int width, int height)
	 * @param partsInstances
	 * @param currentProperty
	 */
	public static Dimension[] patchPieceLayout(List<Layout> layouts, int width,
			int height, String currentProperty) {
		Dimension[] dims = null;
		if (layouts == null) return null;
		IMultiPiece knownType = getKnownMultiPieceElementType(currentProperty);
		if (knownType != null) {
			dims = knownType.patchPieceLayout(layouts, width, height);		
		}
		return dims;
	}

	/**
	 * Usage of property tag in defaultdesign.xml to identify the element type is adviced. Do not
	 * use partcount to identify. Note that there are two different types of three piece elements.
	 * 
	 * @see setDimensions(List<IImage>, String)
	 * @param partsInstances
	 * @param currentProperty
	 */
	public static void setDimensions(List<IImage> partsInstances,
			String currentProperty) {
		
		if (partsInstances == null) return;
		
		IMultiPiece knownType = getKnownMultiPieceElementType(currentProperty);
		if (knownType != null) {
			knownType.setDimensions(partsInstances);
		}
		
	}
	
    /**
     * Check if the copy element info is known. Every multipiece element has a copy element info string to
     * identify its type. Note that for now, both type of three piece elements have the same string. This
     * must not be a problem. 
     * Used by "Take content from" action 
     * @see xxxPieceElement.copyElementInfo attribute.
     * @param copyElementInfo
     * @return true if element type is known
     */
	public static boolean isKnownCopyElementType(String copyElementInfo) {
		for(IMultiPiece knownType:KNOWN_ELEMENT_TYPES) {
			if (knownType.getCopyElementInfo().equals(copyElementInfo)) {
				return true;
			}
		}
		return false;
	}
}
