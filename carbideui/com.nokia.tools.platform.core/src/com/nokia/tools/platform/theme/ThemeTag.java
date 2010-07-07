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
package com.nokia.tools.platform.theme;

import java.io.File;

public class ThemeTag {
	public static final String ORIENTATION_PORTRAIT = "portrait";
	public static final String ORIENTATION_LANDSCAPE = "landscape";
	
	public static final String ORIENTATION = "orientation";
	
	public static final String ORIENTATION_BOTH = "both";
	public static final String ATTR_ENTITY_IMAGE = "garbage";
	public static final String ELEMENT_ANIMATE = "garbage";
	public static final String ATTR_BRIGHTNESS = "garbage";
	public static final String ATTR_CELLSIZE = "garbage";
	public static final String ATTR_X = "garbage";
	public static final String ATTR_Y = "garbage";
	public static final String ATTR_THRESHOLD = "garbage";
	public static String SKN_ATTR_STATUS_SELECTED = "selected";

	// ==========================================================================
	// General constants
	// ==========================================================================
	public static final String DEFAULT = "default";

	// ==========================================================================
	// Constants for xml tags
	// ==========================================================================
	public static final String ELEMENT_ROOT = "skin";
	public static final String STILL = "Still";
	public static final String ANIMATE = "Animate";
	public static final String KEY_FAKE_CALL = "KEY_FAKE_CALL";

	/*
	 * 
	 */
	public static final String ELEMENT_BMPANIM = "bmpanim";

	public static final String ELEMENT_ELEMENT = "element";
	public static final String ELEMENT_BITMAP = "bitmap";
	public static final String ELEMENT_PART = "part";

	// for entitytype="frame"
	public static final String ELEMENT_FRAME = "frame";
	public static final String ELEMENT_COMM_INTERFACE = "interface";
	public static final String ELEMENT_DESIGN_GROUP = "designgroup";
	public static final String ELEMENT_DIMENSION_GROUP = "dimesiongroup";
	public static final String ELEMENT_THEMESETTINGS_GROUP = "themesettingsgroup";
	public static final String ELEMENT_FILE = "file";

	public static final String ATTR_SINGLE_BITMAP = "Single Bitmap";
	public static final String ATTR_9_PIECE = "9 Piece Bitmap";
	public static final String ATTR_3_PIECE = "3 Piece Bitmap";
	
	//This string is used in default design to designate 11 piece element
	public static final String ATTR_11_PIECE = "11 Piece Bitmap";
	public static final String ELEMENT_LAYOUT_GROUP = "layoutgroup";
	public static final String ELEMENT_PREVIEW_GROUP = "previewgroup";
	public static final String ELEMENT_ITEMID_GROUP = "itemidgroup";
	public static final String ELEMENT_PHONE = "phone";
	public static final String ELEMENT_MODEL = "model";
	public static final String ELEMENT_REFER = "refer";
	public static final String ELEMENT_CHILD = "child";
	public static final String ELEMENT_OVERLAY = "overlay";
	public static final String ELEMENT_IMAGEFILE = "imagefile";
	public static final String ELEMENT_FEATURES = "features";
	public static final String ELEMENT_FEATURE = "feature";
	public static final String ELEMENT_SOUNDFORMAT = "soundformat";
	public static final String ELEMENT_FORMAT = "format";
	public static final String ELEMENT_RESTRICTION = "restriction";

	public static final String ELEMENT_PROPERTIES = "properties";
	public static final String ELEMENT_PROPERTY = "property";

	public static final String ELEMENT_RECT = "rect";
	public static final String ELEMENT_ANIMATION = "animation";
	public static final String ELEMENT_SELECTEDIMAGE = "selectedimage";
	public static final String ATTR_IMAGENAME = "imagename";

	public static final String ELEMENT_OTHERNAMES = "othernames";
	public static final String ELEMENT_ALIAS = "alias";

	public static final String ELEMENT_TASK = "task";
	public static final String ELEMENT_COMPONENT_GROUP = "componentgroup";
	public static final String ELEMENT_COMPONENT = "component";
	public static final String ELEMENT_TOOLBOX = "toolbox";

	public static final String ELEMENT_SCREEN = "screen";
	public static final String ELEMENT_LAYOUT = "layout";

	public static final String ELEMENT_PREVIEW = "preview";
	public static final String ELEMENT_IMAGE = "image";
	public static final String ELEMENT_GRAPHIC = "graphic";
	public static final String ELEMENT_LAYER = "layer";
	public static final String ELEMENT_EFFECT = "effect";
	public static final String ELEMENT_BACKGROUND_NAME = "background";

	public static final String ELEMENT_PARAM = "param";
	public static final String ELEMENT_VALUEMODEL = "valuemodel";
	public static final String ELEMENT_TIMINGMODEL = "timingmodel";
	public static final String ELEMENT_VALUEMODELS = "valuemodels";
	public static final String ELEMENT_TIMINGMODELS = "timingmodels";
	public static final String ELEMENT_TIMINGMODEL_REF = "timingmodelref";
	public static final String ELEMENT_VALUEMODEL_REF = "valuemodelref";
	public static final String ELEMENT_SEQNO = "sequenceno";
	public static final String FLAVOUR_NAME = "flavourname";

	public static final String ATTR_PREVIEW = "preview";

	public static final String ATTR_CHILD_ID = "childid";
	public static final String ATTR_CHILD_NAME = "childname";

	public static final String ATTR_BMPANIM = "animate";
	public static final String ATTR_STILL = "still";
	public static final String ATTR_ANIMATE_PARENTID = "animateparentid";
	public static final String ATTR_ANIMATE_TIME = "animatetime";
	public static final String ATTR_ANIMATE_SEQNO = "animateseqno";
	public static final String ATTR_ANIMATE_DEFAULT_TIME = "160";

	public static final String ATTR_SHOW = "show";
	public static final String ATTR_MASTERID = "masterid";
	public static final String ATTR_PREVIEW_HINT = "previewhint";
	public static final String ATTR_PARENT_ELEMENT = "parentelement";
	public static final String ATTR_SHOW_FALSE = "false";
	public static final String ATTR_FIX = "fix";
	public static final String ATTR_FIX_TRUE = "true";

	public static final String ATTR_REFER_ID = "referid";
	public static final String ATTR_REFER_NAME = "refername";

	// public static final String ATTR_BGSCREEN = "backgroundscreen";
	public static final String ATTR_PICTURE = "picture";
	public static final String ATTR_PREVIEWSCREEN = "previewscreen";
	public static final String ATTR_ANIMATE = "animate";

	public static final String ATTR_CURR_PHONE = "currphone";

	public static final String ATTR_DEF_COLOUR_RGB = "defaultcolour_rgb";
	public static final String ATTR_DEF_COLOUR_IDX = "defaultcolour_idx";
	
	public static final String ATTR_COLOUR_GROUP_ID = "colourgroupid";
	public static final String ATTR_COLOUR_GROUP_IDX = "colourgroupidx";
	public static final String ATTR_COLOUR_GROUP_MAJOR_ID = "colourgroupmajorid";
	public static final String ATTR_COLOUR_GROUP_MINOR_ID = "colourgroupminorid";

	public static final String ATTR_ENG_NAME = "name1";
	public static final String ATTR_AUTHOR = "author";
	public static final String ATTR_UID = "uid";
	public static final String KEY_OVERWRITE_INPUT = "KEY_OVERWRITE_INPUT";
	public static final String ATTR_APPUID = "appuid";
	public static final String ATTR_TEXT = "text";

	public static final String ATTR_VERSION = "version";
	public static final String ATTR_VERSION_NO = "2.0";
	public static final String MANIFEST_VERSION = "Manifest-Version";
	public static final String ATTR_SKINTYPE = "skintype";
	public static final String ATTR_PACKAGE = "package";
	public static final String ATTR_TRANSFER = "transfername";
	public static final String ATTR_SCREEN_SAVER = "screensaver";
	public static final String ATTR_SCREEN_SAVER_UID = "screensaveruid";
	public static final String ATTR_COPYRIGHT = "copyright";
	public static final String ATTR_PROTECT = "protect";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_TYPE_NORMAL = "normal";
	public static final String ATTR_DEFAULT_PALETTE = "default_palette";

	public static final String ATTR_ID = "id";
	public static final String FILE_NAME = "filename";
	
	//Whenever a transparent image is created, this is tagged as one attribute.
	public static final String ATTR_NO_IMAGE = "no_image";
	
	public static final String MASK_FILE_NAME = "maskfilename";
	public static final String IS_SOFT_MASK = "issofrmask";
	public static final String ATTR_NAME = "name";
	public static final String ATTR_MAJORID = "majorid";
	public static final String ATTR_MINORID = "minorid";
	public static final String ATTR_LOC_ID = "loc_id";
	public static final String ATTR_GROUP = "group";

	public static final String ATTR_GROUP_DEFAULT = "0";
	public static final String ATTR_LOC_ID_DEFAULT_VALUE = "default";

	public static final String ID_BACKGROUND_STATUS = "qsn_bg_area_status";
	public static final String ID_BACKGROUND_MAIN = "qsn_bg_area_main";
	public static final String ID_BACKGROUND_CONTROL = "qsn_bg_area_control";
	public static final Object KEY_VALUE_MODEL_DATA = "KEY_VALUE_MODEL_DATA";

	public static final String[] SIS_BACKGROUND = { ID_BACKGROUND_STATUS,
			ID_BACKGROUND_MAIN, ID_BACKGROUND_CONTROL };

	public static final String LAYOUT_WIDTH = "w";
	public static final String LAYOUT_HEIGHT = "h";
	public static final String LAYOUT_TOP = "t";
	public static final String LAYOUT_LEFT = "l";
	public static final String LAYOUT_RIGHT = "r";
	public static final String LAYOUT_BOTTOM = "b";
	public static final String LAYOUT_PARENT = "p";

	public static final String ATTR_COLOUR_GRP = "colourgroup";
	public static final String ATTR_OPACITY = "opacity";
	public static final String ATTR_SPLMASKID = "spl_mask_id";
	public static final String ATTR_ISMASK = "is_mask";
	public static final String ATTR_SPLMASKFILE = "spl_mask_file";
	public static final String ATTR_SPLMASKTYPE = "spl_mask_type";

	public static final String ATTR_VALUE_SPLMASKGREY = "grey";
	public static final String ATTR_VALUE_SPLMASKINVERTEDGREY = "invertedgrey";

	public static final String ATTR_SHAPE_LINK_ID = "shape_link_id";

	public static final int MIN_OPACITY = 0;
	public static final int MAX_OPACITY = 100;

	public static final String PREVIEW_HINT_SWAP = "swap";

	// the attribute is present in defaultlayout xml file
	// for the nodes : bitmap,frame,part.
	// if the value of this attr is true, then the layout is taken
	// as the default layout

	public static final String ATTR_DEFAULT = "default";

	// used in the design file.
	public static final String ATTR_RESET = "reset";
	public static final String ATTR_RESET_DEFAULT_VALUE = "none";

	public static final String ATTR_RECT_TYPE = "type";

	public static final int LAYOUT_FORMAT_ERROR = -1000;
	public static final int CHILD_NOT_FOUND = -1;

	public static final int SKINIMAGE_REMOVE_ALL = -1;
	public static final int THEMEGRAPHIC_REMOVE_ALL = -1;

	public static final String LAYOUT_PART_TL = "_corner_tl";
	public static final String LAYOUT_PART_TR = "_corner_tr";
	public static final String LAYOUT_PART_BL = "_corner_bl";
	public static final String LAYOUT_PART_BR = "_corner_br";
	public static final String PART_TL_NAME = "TL";

	public static final String LAYOUT_PART_T = "_side_t";
	public static final String LAYOUT_PART_B = "_side_b";
	public static final String LAYOUT_PART_L = "_side_l";
	public static final String LAYOUT_PART_R = "_side_r";

	public static final String LAYOUT_PART_CENTER = "_center";

	public static final String LAYOUT_ALIGN = "align";

	public static final String LAYOUT_ALIGN_TL = "_corner_tl";
	public static final String LAYOUT_ALIGN_TR = "_corner_tr";
	public static final String LAYOUT_ALIGN_BL = "_corner_bl";
	public static final String LAYOUT_ALIGN_BR = "_corner_br";

	public static final String LAYOUT_ALIGN_T = "_side_t";
	public static final String LAYOUT_ALIGN_B = "_side_b";
	public static final String LAYOUT_ALIGN_L = "_side_l";
	public static final String LAYOUT_ALIGN_R = "_side_r";

	public static final String LAYOUT_ALIGN_C = "_center";

	public static final String LAYOUT_LAYER = "layer";

	// -------------------------------------------------------------
	// holds the lower and maximum layer values possible
	// -------------------------------------------------------------
	public static final int LAYOUT_LAYER_MIN = 0;
	public static final int LAYOUT_LAYER_MAX = 8;

	// ----------------------------------------------------------------
	// holds the l,t,w and h for animation tags
	// ----------------------------------------------------------------

	public static final String ATTR_ANIM_L = "l";
	public static final String ATTR_ANIM_T = "t";
	public static final String ATTR_ANIM_W = "w";
	public static final String ATTR_ANIM_H = "h";

	public static final String ATTR_ANIM_MODE = "animationmode";
	public static final String ATTR_MODE = "mode";

	public static final String ATTR_ANIM_BIDIRECTIONAL = "bidirectional";
	public static final String ATTR_ANIM_HALFCYCLE = "halfcycle";
	public static final String ATTR_ANIM_CONTINUOUS = "continuous";
	public static final String ATTR_PHONENAME = "phonename";

	public static final String SIS_MAKER = "SIS_MAKER";
	public static final String SKIN_EXPORTER = "SKIN_EXPORTER";
	public static final String SKIN_IMPORTER = "SKIN_IMPORTER";
	public static final String SKIN_IMPORT_STR = "SKIN_IMPORT_STR";
	public static final String CONFIG_DIR = "CONFIG_DIR";
	public static final String DATA_DIR = "DATA_DIR";
	public static final String MYWORKS_DIR = "MYWORKS_DIR";
	public static final String LIBRARY_DIR = "LIBRARY_DIR";
	public static final String SIS_DIR = "SIS_PACKAGE_DIRNAME";
	public static final String SKIN_DEFAULT_IMAGES = "SKIN_DEFAULT_IMAGES";
	public static final String SKINDATA_DTD = "SKINDATA_DTD";
	public static final String PHONE_DTD = "PHONE_DTD";

	public static final String IN_EFFECT = "in_effect";

	// public static final String
	// DEFAULT_ELEMENT_BGSCREENS_DIR="DEFAULT_ELEMENT_BGSCREENS_DIR";

	// public static final String DEFAULT_PICTURE_DIR="DEFAULT_PICTURE_DIR";

	public static final String ATTR_STATUS = "status";
	public static final String ATTR_VALUE_ACTUAL = "actual";
	public static final String ATTR_VALUE_DRAFT = "draft";
	public static final String ATTR_HARDMASK = "hardmask";
	public static final String ATTR_SOFTMASK = "softmask";
	public static final String ATTR_ENTITY_X = "entity_x";
	public static final String ATTR_ENTITY_Y = "entity_y";
	public static final String ATTR_IMAGE_X = "image_x";
	public static final String ATTR_IMAGE_Y = "image_y";


	public static final String ATTR_TMP_IMAGE = "TMP_IMAGE";
	public static final String ATTR_TMP_MASK_IMAGE = "TMP_MASK_IMAGE";

	public static final String ATTR_ENTITY_X_DEFAULT = "0";
	public static final String ATTR_ENTITY_Y_DEFAULT = "0";
	public static final String ATTR_IMAGE_X_DEFAULT = "0";
	public static final String ATTR_IMAGE_Y_DEFAULT = "0";
	public static final String ATTR_TILE_DEFAULT = "true";
	public static final String ATTR_STRETCH_DEFAULT = "false";
	public static final String ATTR_ANGLE_DEFAULT = "0";

	public static final String ATTR_COLOURDEPTH = "colourdepth";
	public static final String ATTR_ANGLE = "angle";
	public static final String ATTR_TILE = "tile";
	public static final String ATTR_TILEX = "tilex";
	public static final String ATTR_TILEY = "tiley";

	public static final String ATTR_STRETCH = "stretch";
	public static final String ATTR_ALIGN = "align";
	public static final String ATTR_SCALE = "scale";
	public static final String ATTR_COLOUR_RGB = "colourrgb";
	public static final String ATTR_COLOUR_IDX = "colouridx";

	public static final String ATTR_VALUE_NORMALSKIN = "normal";
	public static final String ATTR_VALUE_SYSTEMSKIN = "system";
	public static final String ATTR_VALUE_COPY_DISALLOWED = "disablecopy";
	public static final String ATTR_VALUE_COPY_ALLOWED = "allowcopy";

	public static final String ATTR_VALUE_COLOUR_DEPTH_DEFAULT = "c16";
	public static final String ATTR_VALUE_COLOUR_DEPTH_C8 = "c8";

	public static final String SKN_ATTR_STATUS_DONE = "done";
	public static final String SKN_ATTR_STATUS_NOT_DONE = "not done";
	public static final String SKN_ATTR_STATUS_FIX = "fix";
	public static final String SKN_ATTR_STATUS_NOT_SELECTED = "not selected";

	public static final String SKN_BINARY_ANIMATION = "BMPANIM";
	public static final String SKN_BINARY_PLAYMODE = "MODE";
	public static final String SKN_BINARY_BITMAP_INDEX = "BMPINDEX";
	public static final String SKN_BINARY_BITMAP_MASK_INDEX = "BMPMASKINDEX";
	public static final String SKN_BINARY_BITMAP_INDEX_MAP = "BMPINDEXMAP";
	public static final String SKN_BINARY_BITMAP_MASK_INDEX_MAP = "BMPMASKINDEXMAP";
	public static final String SKN_BINARY_TIME = "TIME";
	public static final String SKN_FILE_EXTN = ".tdf";
	public static final String SIS_FILE_EXTN = ".sis";
	public static final String SKN_PKG_FILE_EXTN = ".tpf";
	public static final String FILE_TYPE_BMP = ".bmp";
	public static final String FILE_TYPE_EXE = ".exe";
	public static final String SVG_FILE_EXTN = ".svg";
	public static final String SVG_TINY_FILE_EXTN = ".svgt";
	public static final String MP3_FILE_EXTN = ".mp3";
	public static final String WAVE_FILE_EXTN = ".wav";

	public static final String ITEMIDLIST = "itemId.txt";
	public static final String NO_ITEMID_LIST = "noitemidlist";

	public static final String MASK_FILE = "_mask";
	public static final String SOFTMASK_FILE = "_mask_soft";
	public static final String SCROLL_GRADIENT = "scroll_";

	public static final String COLOUR_WHITE = "white";

	// =================================================================
	// Constants for the attributes of ToolBox
	// =================================================================

	public static final String ATTR_MOVE = "move";
	// public static final String ATTR_SCALE = "scale";
	public static final String ATTR_SPLIT = "split";
	public static final String ATTR_MIRROR = "mirror";
	// public static final String ATTR_TILE = "tile";

	// used by <overlay> in preview.dtd and <toolbox> in design.dtd
	public static final String ATTR_MASK = "mask";
	public static final String ATTR_ALWAYS = "always";
	public static final String CHECKNAME = "checkname";

	public static final String ATTR_COLOURIZE = "colourize";
	public static final String ATTR_COLOURLOOKUP = "colourlookup";
	public static final String ATTR_INTENSITY = "intensity";
	public static final String ATTR_CREAANOTHER = "createanother";
	public static final String ATTR_MULTIELEMS = "multipleelements";
	public static final String ATTR_MULTICOMPS = "multiplecomponents";
	public static final String ATTR_SAMECOMP = "samecomponent";
	public static final String ATTR_DESIGN = "designaids";
	public static final String ATTR_ROTATE = "rotate";
	public static final String ATTR_BITMAPBLOCKED = "bitmapblocked";
	public static final String ATTR_TEST = "test";
	public static final String ATTR_ELEMENT_SINGLE = "single:";

	// =======================================================================
	// Constants related to skin descriptor file
	// =======================================================================
	public static final String SKN_TAG_SCALABLEITEM = "SCALABLEITEM";
	public static final String SKN_TAG_INPUT = "INPUT";
	public static final String SKN_TAG_INPUTA = "INPUTA";
	public static final String SKN_TAG_INPUTB = "INPUTB";
	public static final String SKN_TAG_OUTPUT = "OUTPUT";
	public static final String SKN_TAG_EFFECT = "EFFECT";
	public static final String SKN_TAG_SLASH_RGB = "/RGB";
	public static final String SKN_TAG_SLASH_A = "/A";
	public static final String SKN_TAG_SLASH_RGBA = "/RGBA";
	public static final String SKN_TAG_NONE = "none";
	public static final String SKN_TAG_BMP = "BMP";
	public static final String SKN_TAG_FILE = "f";

	public static final String SKN_TAG_SKINTYPE = "SKINTYPE";
	public static final String SKN_TAG_UID = "UID";
	public static final String SKN_TAG_NAME = "NAME";
	public static final String SKN_TAG_BITMAP = "BITMAP";
	public static final String SKN_TAG_COORDS = "COORDS";
	public static final String SKN_TAG_SIZE = "SIZE";
	public static final String SKN_TAG_STRETCH = "STRETCH";
	public static final String SKN_TAG_TILE = "TILE";
	public static final String SKN_TAG_TILEX = "TILEX";
	public static final String SKN_TAG_TILEY = "TILEY";
	public static final String SKN_TAG_ALIGN = "ALIGN";
	public static final String SKN_TAG_SCALE = "SCALE";
	public static final String SKN_TAG_IID = "IID";
	public static final String SKN_TAG_MASK = "MASK";
	public static final String SKN_TAG_SOFTMASK = "SOFTMASK";
	public static final String SKN_TAG_FRAME = "FRAME";
	public static final String SKN_TAG_END = "END";
	public static final String SKN_TAG_RGB = "RGB";
	public static final String SKN_TAG_IDX = "IDX";
	public static final String SKN_TAG_COLOURTABLE = "COLORTABLE";
	public static final String SKN_TAG_COPYRIGHT = "COPYRIGHT";
	public static final String ELEMENT_SCALEABLE = "scaleable";
	public static final String SKN_TAG_APPICON = "APPICON";
	public static final String SKN_TAG_PALETTE_SCHEME = "PALETTE SCHEME";
	public static final String ELEMENT_MORPHING = "morphing";

	public static final String SKN_SPECIAL_COMPONENT_COLOURS_IID = "QsnComponentColors";

	public static final String SKN_SPECIAL_COMPONENT_COLOUR_BITMAP = "qsn_component_color_bmp_cg";
	public static final String ELEMENT_FASTANIMATION = "fastanimation";

	public static final String SKN_ID_COLOUR_ELEMENT = "qsn_component_colors_cg";

	public static final String SKN_ID_COLOUR_GRADIENT = "qsn_scroll_colors";

	public static final String SKN_TAG_PROTECT = "PROTECT";
	public static final String SKN_VALUE_DISABLECOPY = "disablecopy";

	public static final String SKN_VALUE_NORMAL = "normal";
	public static final String SKN_VALUE_SYSTEM = "system";
	public static final String SKN_VALUE_SCALABLE = "scalable";

	// =======================================================================
	// Constant to hold filename of Default background image
	// =======================================================================
	public static final String SKIN_DEFAULT_BACKGROUND_IMAGE = "background_slice.jpg";

	// =======================================================================
	// Constant to hold fields of skn binary data specification
	// =======================================================================

	public static final String SKN_BINARY_LENGTH = "LENGTH";
	public static final String SKN_BINARY_FILE_LENGTH = "FILELENGTH";
	public static final String SKN_BINARY_TYPE = "TYPE";
	public static final String SKN_BINARY_VERSION = "VERSION";
	public static final String SKN_BINARY_SKINPID1 = "SKINPID1";
	public static final String SKN_BINARY_SKINPID2 = "SKINPID2";
	public static final String SKN_BINARY_SKINCF = "SKINCF";
	public static final String SKN_BINARY_SKINTYPE = "SKINTYPE";
	public static final String SKN_BINARY_COLORSCHEMEUID = "COLORSCHEMEUID";
	public static final String SKN_BINARY_RESERVED = "RESERVED";

	public static final String SKN_BINARY_PROTECTION = "PROTECTION";
	public static final String SKN_BINARY_DATEHIGH = "DATEHIGH";
	public static final String SKN_BINARY_DATELOW = "DATELOW";
	public static final String SKN_BINARY_SCVER = "SCVER";
	public static final String SKN_BINARY_STVER = "STVER";
	public static final String SKN_BINARY_IDENTIFIER = "SKNBINARYID";

	public static final String SKN_BINARY_AUTHOR = "AUTHOR";

	public static final String SKN_BINARY_COPYRIGHT = "COPYRIGHT";
	public static final String SKN_BINARY_LANGUAGE = "LANGUAGE";

	public static final String SKN_BINARY_NAME = "NAME";
	public static final String SKN_BINARY_OVERRIDETYPE = "OVERRIDETYPE";
	public static final String SKN_BINARY_OVERRIDEPID1 = "OVERRIDEPID1";
	public static final String SKN_BINARY_OVERRIDEPID2 = "OVERRIDEPID2";
	public static final String SKN_BINARY_WALLPAPERTYPE = "WALLPAPERTYPE";

	public static final String SKN_BINARY_FILENAME = "FILENAME";

	public static final String SKN_BINARY_FILENAMEID = "FILENAMEID";
	public static final String SKN_BINARY_CLASS = "CLASS";
	public static final String SKN_BINARY_MAJOR = "MAJOR";
	public static final String SKN_BINARY_MINOR = "MINOR";
	public static final String SKN_BINARY_IMAGE = "IMAGE";

	public static final String SKN_BINARY_SOUND = "SOUND";
	public static final String SKN_BINARY_MASKIMAGE = "MASKIMAGE";
	public static final String SKN_BINARY_COLORSN = "COLORSN";
	public static final String SKN_BINARY_COLORINDEX = "COLORINDEX";
	public static final String SKN_BINARY_COLORRGB = "COLORRGB";
	public static final String SKN_BINARY_IMAGESN = "IMAGESN";
	public static final String SKN_BINARY_IMAGEMAJOR = "IMAGEMAJOR";
	public static final String SKN_BINARY_IMAGEMINOR = "IMAGEMINOR";
	public static final String SKN_BINARY_INTERVAL = "INTERVAL";
	public static final String ANIMATION_CHILD = "_child";
	public static final String ANIMATION_CHILD0 = "_child0";
	public static final String SKN_BINARY_FLASH = "FLASH";
	public static final String SKN_BINARY_FRAMESN = "FRAMESN";
	public static final String SKN_BINARY_FRAMEMAJOR = "FRAMEMAJOR";
	public static final String SKN_BINARY_FRAMEMINOR = "FRAMEMINOR";
	public static final String SKN_BINARY_FRAMETIME = "FRAMETIME";
	public static final String SKN_BINARY_FRAMEPOSX = "FRAMEPOSX";
	public static final String SKN_BINARY_FRAMEPOSY = "FRAMEPOSY";
	public static final String SKN_BINARY_ATTRIBUTEFLAGS = "ATTRIBUTEFLAGS";
	public static final String SKN_BINARY_ALIGNMENT = "ALIGNMENT";
	public static final String SKN_BINARY_COORDX = "COORDX";
	public static final String SKN_BINARY_COORDY = "COORDY";
	public static final String SKN_BINARY_SIZEW = "SIZEW";
	public static final String SKN_BINARY_SIZEH = "SIZEH";

	public static final String SKN_TILE_TRUE = "true";
	public static final String SKN_STRETCH_TRUE = "true";

	public static final int HEX_ZERO = 0x00;
	public static final int HEX_ONE = 0x01;
	public static final int HEX_TWO = 0x02;
	public static final int HEX_FOUR = 0x04;
	public static final int HEX_EIGHT = 0x08;
	public static final int HEX_TEN = 0x10;
	public static final int HEX_TWENTY = 0x20;

	// ==========================================================================
	// Constants to hold the binary chunks in the skn binary file.
	// ==========================================================================
	/*
	 * public static final String SKN_BINARY_INFORMATIONCHUNK =
	 * "INFORMATIONCHUNK"; public static final String SKN_BINARY_NAMECHUNK =
	 * "NAMECHUNK"; public static final String SKN_BINARY_LANGUAGEOVERRIDECHUNK =
	 * "LANGUAGEOVERRIDECHUNK"; public static final String
	 * SKN_BINARY_WALLPAPERCHUNK = "WALLPAPERCHUNK"; public static final String
	 * SKN_BINARY_FILENAMECHUNK = "FILENAMECHUNK"; public static final String
	 * SKN_BINARY_CLASSCHUNK = "CLASSCHUNK"; public static final String
	 * SKN_BINARY_BITMAPITEMCHUNK = "BITMAPITEMCHUNK"; public static final
	 * String SKN_BINARY_COLORTABLEITEMCHUNK = "COLORTABLEITEMCHUNK"; public
	 * static final String SKN_BINARY_IMAGETABLEITEMCHUNK =
	 * "IMAGETABLEITEMCHUNK"; public static final String
	 * SKN_BINARY_BITMAPANIMATIONITEM = "BITMAPANIMATIONITEMCHUNK";
	 */

	// ==========================================================================
	// Constants to hold the tags found in tagged strings
	// ==========================================================================
	public static final String TAG_APP_START = "<app>";
	public static final String TAG_SKIN_START = "<skin>";
	public static final String TAG_TASK_START = "<task>";
	public static final String TAG_COMPONENTGROUP_START = "<componentgroup>";
	public static final String TAG_COMPONENT_START = "<component>";
	public static final String TAG_ELEMENT_START = "<element>";
	public static final String TAG_PART_START = "<part>";
	public static final String TAG_NAME_START = "<name>";
	public static final String TAG_ID_START = "<id>";
	public static final String TAG_LOCID_START = "<locid>";

	public static final String TAG_APP_END = "</app>";
	public static final String TAG_SKIN_END = "</skin>";
	public static final String TAG_TASK_END = "</task>";
	public static final String TAG_COMPONENTGROUP_END = "</componentgroup>";
	public static final String TAG_COMPONENT_END = "</component>";
	public static final String TAG_ELEMENT_END = "</element>";
	public static final String TAG_PART_END = "</part>";
	public static final String TAG_NAME_END = "</name>";
	public static final String TAG_ID_END = "</id>";
	public static final String TAG_LOCID_END = "</locid>";

	public static final String TAG_TYPE_START = "<type>";
	public static final String TAG_INFO_START = "<info>";
	public static final String TAG_TYPE_END = "</type>";
	public static final String TAG_INFO_END = "</info>";
	public static final String ELEMENT_SIS = "sis";

	// ==========================================================================
	// Constants to hold for paint and soundapplication
	// ==========================================================================
	public static final String PAINT_TOOL_PATH = "PAINT_TOOL_PATH";
	public static final String SOUND_APP_PATH = "SOUND_APP_PATH";

	// ==========================================================================
	// Constants to hold the values for toolbox operation MASK
	// ==========================================================================
	public static final int MASK_ON = 3;
	public static final int MASK_OFF = 4;
	public static final int MASK_BUCKET = 0;
	public static final int MASK_POINTER = 1;
	public static final int MASK_REMOVE = 2;

	// ==========================================================================
	// Constants to hold the values for toolbox operation MIRROR
	// ==========================================================================
	/*
	 * public static final int MIRROR_ORIGINAL = 0; public static final int
	 * MIRROR_HORIZONTAL = 1; public static final int MIRROR_VERTICAL = 2;
	 * public static final int MIRROR_DIAGNOL = 3;
	 */

	// ==========================================================================
	// Constants to hold the component name 
	// ==========================================================================
	public static final String APP_MYWORKS = "myworks";
	public static final String APP_LIBRARY = "library";
	public static final String APP_BROWSER = "browser";

	// ==========================================================================
	// Constants to hold for DesignAds
	// ==========================================================================
	public static final String DESIGNAIDS_ICON_RGB = "DESIGNAIDS_ICON_RGB";

	public static final String ELEMENT_SOUND = "sound";
	public static final String ELEMENT_EMBED_FILE = "embeddedFile";
	public static final String DESIGNAIDS_TEXT_RGB = "DESIGNAIDS_TEXT_RGB";
	public static final String DESIGNAIDS_TEXT_ICON_RGB = "DESIGNAIDS_TEXT_ICON_RGB";
	public static final String DESIGNAIDS_PART_ICON_RGB1 = "DESIGNAIDS_PART_ICON_RGB1";
	public static final String DESIGNAIDS_PART_ICON_RGB2 = "DESIGNAIDS_PART_ICON_RGB2";
	public static final String DESIGNAIDS_ICON = "icon";
	public static final String DESIGNAIDS_TEXT = "text";
	public static final String DESIGNAIDS_TEXT_ICON = "text_icon";
	public static final String DESIGNAIDS_BLANK1 = "blank1";
	public static final String DESIGNAIDS_BLANK2 = "blank2";

	public static final String COLORIZE_PALETTE_PREFIX = "qsn_bg_navipane_wipe";

	public static final double SCALE_TO_FIT = 0.0;

	
	public static final String THIRDPARTY_DUMMY1 = "Dummy1";
	public static final String THIRDPARTY_DUMMY2 = "Dummy2";
	public static final String THIRDPARTY_STATUS = "Status";
	public static final String THIRDPARTY_ICONAME = "IconName";
	public static final String THIRDPARTY_CONTEXT = "Context Pane Icons";
	public static final String THIRDPARTY_APPLICATION = "Application Shell Icons";
	public static final String THIRDPARTY_PAR = "fileload";
	
	public static final String ATTR_EXTENSION = ".bmp";
	public static final String ATTR_IC = "ICONS";
	public static final String ATTR_APPLICON = "Application Icons";
	public static final String ATTR_APPLICATION = "application";
	public static final String ATTR_CONTEXT = "context";
	public static final String ATTR_EXTENSION_SOUND = "extension";
	
	public static final String ATTR_DES = "design";
	public static final String ATTR_DRM = "drm";
	public static final String ADVANCE_PUID = "puid";
	public static final String ADVANCE_UID = "uid";
	public static final String ADVANCE_PUBLICKEY = "publickey";
	public static final String ADVANCE_PRIVATEKEY = "privatekey";
	public static final String ADVANCE_DRM = "drmprotection";
	public static final String ADVANCE_SYSTEM = "systemtheme";
	public static final String ADVANCE_NORMAL = "normaltheme";
	public static final String ADVANCE_VERSION = "versionselected";
	// added for animation
	public static final String FRAME_MAXVALUE = "maxvalue";
	public static final String FRAME_MINVALUE = "minvalue";
	public static final String FRAME_ERROR_MESSAGE = "errormessage";

	
	public static final String ATTR_CURR_PHONE_DISPLAY = "currphoneDisplay";
	
	public static final String LAYOUT_CONFIG_DIR = "LAYOUT_CONFIG_DIR";
	public static final String LAYOUT_TEMP_DIR = "LAYOUT_TEMP_DIR";

	
	public static final String PHONE_ELEMENT_LAYOUT = "layout";
	public static final String PHONE_LAYOUT_HEIGHT = "height";
	public static final String PHONE_LAYOUT_WIDTH = "width";
	public static final String PHONE_LAYOUT_ORIENTAITON = "orientation";
	public static final String PHONE_LAYOUT_ORIENTAITON_PORTRAIT = "portrait";
	public static final String PHONE_LAYOUT_ORIENTAITON_LANDSCAPE = "landscape";

	public static final String ATTR_AGGRGATE_IMAGE = "aggregate_image";

	public static final String ATTR_SELECTED_LAYER_IMAGE = "selected_layer_image";

	// for retrieving drawing and spacing information in editing area for
	// showing mlti elements----
	public static final String START_X = "start_X";
	public static final String START_Y = "start_Y";
	public static final String MAX_HEIGHT = "max_Height";

	public static String DUMMYNODE = "DummyNode";

	public static String ATTR_LAYERS_SUPPORT = "layerssupport";
	public static String ATTR_EFFECTS_SUPPORT = "effectssupport";
	public static String ATTR_CURR_PHONE_ORIENTATION = "orientation";
	public static String ATTR_MULTIPLE_LAYERS_SUPPORT = "multiplelayerssupport";
	public static String BITS_PIXEL_SUPPORT = "Bits24PixelSupport";

	public static final String ELEMENT_DESIGNRECTANGLES = "designrectangles";
	public static final String ELEMENT_DESIGNRECTANGLE = "designrectangle";
	public static String ATTR_DERIVED_LAYOUT_ID = "derviedlayoutid";
	public static final String TAG_TG_START = "<tg>";
	public static final String TAG_TG_END = "</tg>";
	public static final String ATTR_ENTITY_TYPE = "entitytype";
	public static final String ATTR_ENTITY = "entity";
	public static final String ATTR_DIMENSIONS = "dimensions";
	public static final String ATTR_DIMENSION = "dimension";

	public static final String ELEMENT_COLOUR = "colour";

	public static final String UNIQUE_ID = "uniqueid";
	public static final String ATTR_SOUND = "attr_sound";
	public static final String ATTR_MULTILAYER = "MultiLayered";
	public static final String ATTR_SINGLE_EFFECTS = "SingleLayer With Effects";
	public static final String ATTR_SINGLE = "SingleLayer With No Effects";
	public static final String IMAGE_DIR = ".." + File.separator + "data"
			+ File.separator + "image";
	public static final String PASS_THROUGH_FILE = "PASS_THROUGH_FILE";
	public static final String DOWN_ARROW_IMAGE = "DOWN_ARROW_IMAGE";
	public static final String RIGHT_ARROW_IMAGE = "RIGHT_ARROW_IMAGE";
	public static final String TOOL_BOX_MINIMISE_TEXT1 = "TOOL_BOX_MINIMISE_TEXT1";
	public static final String TOOL_BOX_MINIMISE_TEXT2 = "TOOL_BOX_MINIMISE_TEXT2";
	public static final String TOOL_BOX_MINIMISE_TEXT3 = "TOOL_BOX_MINIMISE_TEXT3";
	public static final String TOOL_BOX_MINIMISE_TEXT4 = "TOOL_BOX_MINIMISE_TEXT4";
	public static String DERIVED_LAYOUT_ID = "derviedLayoutId";
	public static String FILEPATH = "FilePath";
	public static String MASKFILEPATH = "MaskFilePath";
	public static String KEY_INPUT_DIR = "KEY_INPUT_DIR";
	public static String KEY_OUTPUT_DIR = "KEY_OUTPUT_DIR";
	public static String KEY_SKIN_COMPLIANCE = "KEY_SKIN_COMPLIANCE";
	public static String KEY_SKIN_SKINTYPE = "KEY_SKIN_SKINTYPE";
	public static String KEY_SKIN_SPLENTITY_CLASSNAME = "KEY_SKIN_SPLENTITY_CLASSNAME";
	public static String KEY_ENTITY_PROP = "KEY_ENTITY_PROP";
		
	public static String VERSION_FIVE_DOT_ZERO = "VERSION_FIVEPOINTZERO";
	public static String VERSION_SYMBIAN_2 = "VERSION_SYMBIAN_2";
	public static String VERSION_UNSPECIFIED = "VERSION_UNSPECIFIED";

	public static String MANDATORY_FILES = "PLEASE_ENTER_MANDATORY_FILES";
	public static String EDIT_AND_ENTER_MANDATORY_FILES = "PLEASE_EDIT_AND_ENTER_MANDATORY_FILES";
	public static String EDIT_AND_ENTER_BOTH_FILES = "PLEASE_EDIT_AND_ENTER_BOTH_FILES";
	public static String CHECKOUT_THEME = "CHECKOUT_THEME";
	public static enum SkinCompliance {
		BITMAP_SKIN, SCALEABLE_SKIN
	};

	public static String TASK_COLOURS = "colours";

	
	public static String TILE_WIDTH = "tile_width";
	public static String TILE_HEIGHT = "tile_height";

	// for file chooser
	public static String FILE_CHOOSER_NAME = "file_chooser_name";
	public static String FILE_CHOOSER_DIR = "file_chooser_dir";
	public static String ATTR_VALUE = "value";
	public static String ATTR_MANDATORY = "mandatory";
	public static String SKN_TAG_DRAWLINES = "drawlines";

	public static String CLASS_NAME = "class";
	public static String SKINNABLE_ENTITY = "skinnableentity";
	
	public static final String NEW_THEME_NAME = "newThemeName";
	public static final String OLD_THEME_NAME = "oldThemeName";

	public static final String ATTR_SKINFORMAT = "skinformat";
	public static final String ATTR_ANIMATABLE = "animatable";
	public static final String ATTR_SCALABLE = "scalable";

}// end of the ThemeTag class
