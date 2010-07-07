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
package com.nokia.tools.screen.ui;

import java.awt.datatransfer.Clipboard;
import java.awt.image.RenderedImage;
import java.util.Map;

import org.eclipse.gef.commands.Command;

import com.nokia.tools.content.core.IContentData;
import com.nokia.tools.media.utils.clipboard.IClipboardContentType;
import com.nokia.tools.media.utils.layers.IImage;
import com.nokia.tools.media.utils.layers.IPasteTargetAdapter;
import com.nokia.tools.platform.theme.BitmapProperties;
import com.nokia.tools.platform.theme.ThemeException;

/**
 */
public interface ISkinnableEntityAdapter extends IPasteTargetAdapter {

	
	
	/**
	 * Handy method to take control of unnecessary notifications
	 * @param suppressNotification
	 */	
	public void setSuppressNotification(boolean suppressNotification);
	
	/**
	 * Handy method to take control of unnecessary notifications
	 */	
	public boolean isSuppressNotification();
	
	public int getMultiPiecePartCount();
	
	/**
	 * get edited theme graphics object from editing layer instance
	 * 
	 * @param editModel should be com.nokia.tools.media.utils.layer.IImage
	 *            instance
	 * @return
	 */
	public Object getEditedThemeGraphics(Object editModel);

	/**
	 * when forceAbsolutePaths, returned TG has absolute paths and images and
	 * masks are copied also if they were not changed.
	 * 
	 * @param editModel
	 * @param forceAbsolutePaths
	 * @return
	 */
	public Object getEditedThemeGraphics(Object editModel,
			boolean forceAbsolutePaths);

	/**
	 * get original theme graphics object from editing layer instance
	 * 
	 * @param editModel should be com.nokia.tools.media.utils.layer.IImage
	 *            instance
	 * @return
	 */
	public Object getOriginalThemeGraphics(Object editModel);

	/**
	 * sets tehemGraphics to the SkinnableEntity instance with given status
	 * 
	 * @param themeGr
	 * @param status
	 */
	public void setThemeGraphics(Object themeGr, String status);

	/**
	 * returns true if image can be copied to clipboard from this element =
	 * returns true if elements contains image data
	 * 
	 * @return
	 */
	public boolean isCopyAllowed();

	/**
	 * true when element has mask, works only for single-layered images
	 * 
	 * @return
	 */
	public boolean hasMask();

	/**
	 * true when element has image
	 * 
	 * @return
	 */
	public boolean hasImage();

	/**
	 * returns element's mask image
	 * 
	 * @return
	 */
	public RenderedImage getMask();

	/**
	 * perform copy to clipboard operation on given entity. return true if copy
	 * was performed succesfully. If the
	 * 
	 * @param clipboard is null, the system clipboard is used.
	 * 
	 * @param clipboard
	 * @return
	 */
	public boolean copyImageToClipboard(Clipboard clip);

	/**
	 * returns element's id
	 */
	public String getId();

	/**
	 * sets atributess
	 * 
	 * @param map
	 */
	public void setAttributes(Map<Object, Object> map);

	/**
	 * gets entity attrs
	 */
	@SuppressWarnings("unchecked")
	public Map getAttributes();

	/**
	 * perform clear action on the given entity. returns undo object = old
	 * themeGraphics.
	 * 
	 */
	public Object clearThemeGraphics();

	/**
	 * true if element contains bitmap image , works only for single-layer
	 * 
	 * @return
	 */
	public boolean isBitmap();

	/**
	 * true if element contains vector image , works only for single-layer
	 * 
	 * @return
	 */
	public boolean isSVG();

	/**
	 * for setting ImageLayer attrs
	 * 
	 * @param index
	 * @param attrs
	 */
	public void setLayerAttributes(int index, Map<Object, Object> attrs);

	/**
	 * for setting ImageLayer attrs
	 * 
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map getLayerAttributes(int index);

	/**
	 * Every element has a color depth.
	 * 
	 * @return layer color depth
	 */
	public String getColorDepth(int layerIndex);
	
	/**
	 * sets theme graphics for part
	 * 
	 * @param part
	 * @param newThemeGraphics
	 * @param status
	 */
	public void setPartThemeGraphics(IImage part, Object newThemeGraphics,
			String status);

	/**
	 * sets theme graphics for part
	 * 
	 * @param partNo
	 * @param newThemeGraphics
	 * @param status
	 * @throws Exception
	 */
	public void setPartThemeGraphics(int partNo, Object newThemeGraphics,
			String status) throws Exception;

	void setPartsThemeGraphics(Object[] newThemeGraphis, String status)
			throws Exception;

	/**
	 * Clears theme graphics for given PART child element of this entity
	 * 
	 * @param partEntity
	 * @return
	 */
	public Object clearThemeGraphics(IImage part);

	/**
	 * TRUE for overriden items
	 * 
	 * @return
	 */
	public boolean isSkinned();
	
	public String getCopyPieceInfo();
	
	/**
	 * true if element really is Three piece element
	 * 
	 * @return
	 */
	//public boolean isThreePiece();

	/**
	 * true if element really is nine piece element
	 * 
	 * @return
	 */
	//public boolean isNinePiece();
	
	/**
	 * true if element is eleven piece element
	 * 
	 * @return
	 */
	//public boolean isElevenPiece();	
	
	/**
	 * true if element is eleven piece element
	 * 
	 * @return
	 */
	public boolean isMultiPiece();	

	/**
	 * true if element represents colour object
	 * 
	 * @return
	 */
	public boolean isColour();

	/**
	 * true for navigation indicators, elements where image defines shape and
	 * colour attribute defines colour
	 * 
	 * @return
	 */
	public boolean isColourIndication();

	/**
	 * return IContentData associated with this adapter.
	 * 
	 * @return
	 */
	public IContentData getContentData();

	/** returns clone of given ThemeGraphics */
	public Object getClone(Object themeGraphics);

	/** returns currenty valid ThemeGraphics object associated with this entity */
	public Object getThemeGraphics();

	/**
	 * returns clone of currenty valid ThemeGraphics object associated with this
	 * entity, alternatively, makes paths independent of theme root directory
	 * 
	 * @throws Exception
	 */
	public Object getThemeGraphics(boolean makePathsAbsolute) throws Exception;

	/**
	 * marks this entity aseffectively NINE_PIECE does not touch theme graphics
	 */
	/*public void setNinePieceBitmap();
	
	public void setElevenPieceBitmap();
	
	public void setThreePieceBitmap();
	*/
	public void setMultiPieceBitmap();

	/**
	 * marks this entity aseffectively SINGLE_PIECE does not touch theme
	 * graphics
	 */
	public void setSinglePieceBitmap();

	/**
	 * returns another adapter
	 * 
	 * @param c
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class c);

	
	public boolean isSingleImageLayer();
	
	/**
	 * true if element support three-piece mode
	 * 
	 * @return
	 */
	//public boolean supportsThreePiece();

	/**
	 * true if element support nine-piece mode
	 * 
	 * @return
	 */
	//public boolean supportsNinePiece();

	/**
	 * true if element support eleven-piece mode
	 * 
	 * @return
	 */
	//public boolean supportsElevenPiece();
	
	/**
	 * true if element support eleven-piece mode
	 * 
	 * @return
	 */
	public boolean supportsMultiPiece();

	/**
	 * @return list if file extensions that can be applied to edit the element
	 */
	public String[] getSupportedFileExtensions();

	/**
	 * true if given element represents screen background
	 * 
	 * @return
	 */
	boolean isBackground();

	/**
	 * sets stretch mode for image, if element has more layers, sets it to the
	 * first layer with image
	 * 
	 * @param stretchMode
	 */
	void setStretchMode(String stretchMode);

	String getStretchMode();

	/**
	 * Returns default stretch mode for element. Can vary according to element
	 * type.
	 * 
	 * @return
	 */
	String getDefaultStretchMode();

	/**
	 * sets element's content from <i>image</i> parameter.
	 * 
	 * @param image
	 */
	void updateThemeGraphic(IImage image);

	/**
	 * returns clipboard content type for given clipboard, if clipboard contains
	 * supported content type
	 * 
	 * @param clipboardContent
	 * @return
	 */
	public IClipboardContentType getClipboardContentType(Object clipboardContent);

	/**
	 * Returns undo-theme graphicss, but only when filParts = true;
	 * 
	 * @param _image
	 * @return backup object
	 * @throws Exception
	 */
	//Object[] convertToNinePieceBitmap(boolean fillParts) throws Exception;

	Object[] convertToMultiPieceBitmap(boolean fillParts) throws Exception;
	
	/**
	 * Converts 9-piece element to single piece element
	 * 
	 * @param image
	 * @param replaceGraphics - when true, aggregate image of nine-pieces is set
	 *            as TG image
	 * @throws ThemeException
	 */
	Object convertToSinglePieceBitmap(boolean replaceGraphics) throws Exception;

	Command getApplyStretchModeCommand(String stretchMode);

	Command getApplyBitmapPropertiesCommand(BitmapProperties properties);

	//public Object[] convertToElevenPieceBitmap(boolean fillParts)throws Exception;
	
	//public Object[] convertToThreePieceBitmap(boolean fillParts)throws Exception;
	
	public String getMultiPieceSearchViewText();

	public boolean isConvertedFromMultipiece();

}
