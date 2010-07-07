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
 * 
 *
 * This class contains the constants that are commonly used across the various
 * subsystems of Theme Studio application. 
 */
package com.nokia.tools.platform.theme;

/**
 * 
 *
 * This class contains the constants that are commonly used across the various
 * subsystems of Theme Studio application. 
 */
public final class ThemeConstants {

	public static final long SERIAL_ID = 1L;
	
	public static final String THEMESTUDIO_ROOT_SUBSYSTEM = "ThemeStudio";

	public static final String LOCAL_SUBSYSTEM = "Local";

	public static final String EDITINGAREA_SUBSYSTEM = "EditingArea";
	
	public static final String EDITINGAREA_SUBSYSTEM_WITHANIMATION = "EditingAreaWithAnimation";

	public static final String TASKAREA_SUBSYSTEM = "TaskArea";

	public static final String COMPONENTLIST_SUBSYSTEM = "ComponentList";

	public static final String COMPONENTLIST_SUBSYSTEM_WITHANIMATION = "ComponentListWithAnimation";

	public static final String ELELMENTLIST_SUBSYSTEM = "ElementList";
	
	public static final String ELELMENTLIST_SUBSYSTEM_MIDSIZE = "ElementListMidWidth";

	public static final String ANIMATIONLIST_SUBSYSTEM = "AnimationList";

	public static final String BROWSERAREA_SUBSYSTEM = "BrowserArea";
	
	public static final String BROWSERAREA_TOPPANEL = "BrowserAreaTopPanel";
	
	public static final String BROWSERAREA_MIDDLEPANEL = "BrowserAreaMiddlePanel";
	
	public static final String BROWSERAREA_BOTTOMPANEL = "BrowserAreaBottomPanel";

	public static final String PREVIEW_SUBSYSTEM = "Preview";

	public static final String PREVIEW_CANVAS_SUBSYSTEM = "PreviewCanvas";
	
	public static final String CREATION_PREVIEW_CANVAS_SUBSYSTEM = "CreationPreviewCanvas";

	public static final String LAYOUT_SUBSYSTEM = "Layout";

	public static final String EVENT_SUBSYSTEM = "Event";
	
	public static final String GALLERY_PREVIEW_AREA = "GalleryPreviewArea";

	public static final String CREATION_FUNC_ROW_SUBSYSTEM = "CreationFunctionRow";

	public static final String GALLERY_FUNC_ROW_SUBSYSTEM =	"GalleryFunctionRow";

	public static final String CREATION_TOOL_BOX_SUBSYSTEM = "CreationToolBox";
	
	public static final String CREATION_TOOL_BOX_SUBSYSTEM_ANIMATION = "CreationToolBoxWithAnimation";
	
	public static final String MENU_BAR_SUBSYSTEM = "MenuBar";
	
	public static final String CREATIONVIEW_SUBSYSTEM = "CreationView";
	
	public static final String GALLERYVIEW_SUBSYSTEM = "GalleryView";
	
	public static final String CHECKOUT_DAILOG_AREA = "CheckoutDialog";
	
	public static final String FIX_LIST_WINDOW_AREA = "FixListWindow";	
	
	public static final String COLOURS_TASK = "ColoursTask";

	public static final String[] LOGGING_SUBSYSTEM_LIST = new String[14];
	
	public static final String TOOLBOX_ZOOM_STATE = "ZoomState";
	
	public static final String TOOLBOX_SECTIONS_STATE = "SectionsState";
	
	public static final String TOOLBOX_DIM_STATE = "DimState";
	
	public static final String TOOLBOX_DIM_VALUE = "DimValue";
	
	public static final String TOOLBOX_ICON_WIZARD_STATE = "IconWizardState";
	
	public static final String TOOLBOX_ICON_GENERATOR_VALUE = "IconGeneratorValue";
	
	public static final String TOOLBOX_DESIGN_AIDS_STATE = "DesignAidsState";
	
	public static final String TOOLBOX_MIRROR_STATE = "MirrorState";
	
	public static final String TOOLBOX_TILE_STATE = "TileState";
	
	public static final String TOOLBOX_MASK_STATE = "MaskState";
	
	public static final String TOOLBOX_COLOURISE_STATE = "ColouriseState";
	
	public static final String TOOLBOX_COLOUR_LOOKUP_STATE = "ColourLookUp";
	
	public static final String TOOLBOX_COLOUR_TOOL_TYPE = "colourToolType";
	
	public static final String ZOOM_VALUE = "ZoomValue";
	
	public static final String TOOLBOX_MASK_VALUE = "MaskItemSelected";
	
	public static final String ISELEMENT = "IsElement";
	
	public static final String TOOLBOX = "ToolBox";
	
	public static final String TOOLBOX_CURRENT_STATE_INFO = "ToolBoxCurrentStateInfo";
	
	public static final String HAS_CHILD_NODES = "HasChildNodes";
	
	public static final String THEME_TAG_MASK_OFF = "ThemeTagMaskOff";
	
	public static final String THEME_TAG_MASK_ON = "ThemeTagMaskOn";
	
	public static final String CREATION_FUNC_ROW_OPEN_IN_STATE = "CreationFuncRowOpenInState";
	
	public static final String CREATION_FUNC_ROW_CLEAR_STATE = "CreationFuncRowClearState";
	
	public static final String CREATION_FUNC_ROW_PREVIEW_STATE = "CreationFuncRowPreviewState";
	
	public static final String CREATION_FUNC_ROW_TEST_STATE = "CreationFuncRowTestState";
	
	public static final String CREATION_FUNC_ROW_DRAFT_STATE = "CreationFuncRowDraftState";
	
	public static final String CREATION_FUNC_ROW_SAVE_STATE = "CreationFuncRowSaveState";
	
	public static final String CREATION_FUNC_ROW_CURRENT_STATE = "CreationFuncRowCurrentState";
	
	public static final String GALLERY_FUNC_ROW_NEW_THEME_STATE = "GalleryFuncRowCreateNewThemeState";
	
	public static final String GALLERY_FUNC_ROW_SIS_STATE = "GalleryFuncRowCreateSISState";
	
	public static final String GALLERY_FUNC_ROW_TRANSFER_THEME_STATE = "GalleryFuncRowTransferThemeState";
	
	public static final String GALLERY_FUNC_ROW_EDIT_STATE = "GalleryFuncRowEditState";
	
	public static final String GALLERY_FUNC_ROW_CURRENT_STATE = "GalleryFuncRowCurrentState";
			
	public static final String SELECTED_TDF_FILE = "SelectedTDFFile";
	
	public static final String SELECTED_TPG_FILE = "SelectedTPGFile";
	
	public static final String SBD = "SkinBasicData";
	
	public static final String ACTION_IN_RUBBISHBIN = "ActionInrubbishBin";
	
	public static final String RESTORE_THEMES = "restoreThemes";
	
	public static final String BACKGROUND_COLOUR = "BackGroundColour";
	
	public static final String BLEND_VALUE = "BlendValue";
	
	public static final String AUTO = "Auto";
	
//	public static final String IMPORTED_SKIN = "ImportedSkin";
	
	public static final String MENUBAR_EXPORT_STATE = "ExportMenuItemState";
	
	public static final String MENUBAR_IMPORT_STATE = "ImportMenuItemState";
	
	public static final String MENUBAR_EDIT_STATE = "EditMenuItemState";
	
	public static final String MENUBAR_SAVE_STATE = "SaveMenuItemState";
	
	public static final String MENUBAR_SAVEAS_STATE = "SaveAsMenuItemState";
	
	public static final String MENUBAR_OPEN_STATE = "OpenMenuItemState";
	
	public static final String MENUBAR_CLOSE_STATE = "CloseState";
	
//	public static final String MENUBAR_FIXLIST_STATE = "FixListMenuItemState";
	
	public static final String MENUBAR_THIRDPARTY_STATE = "ThirdPartyMenuItemState";
	
	public static final String MENUBAR_VIEW_MENU_STATE = "ViewMenuState";
	
	public static final String MENUBAR_EDIT_MENU_STATE = "EditMenuState";
	
	public static final String IS_GALLERY_VIEW = "IsGalleryViewTabSelected";
	
	public static final String OPEN_S60THEME_IN_GALLERYVIEW = "OpenS60Theme";
	
	public static final String OPEN_S60THEME_IN_CREATIONVIEW = "EditS60Theme";
	
	public static final String SET_THEME_NAME_IN_GALLERY = "SetThemeNameInGallery";
	
	public static final String DISPLAYCOMPNAME = "DisplayCompName";

	public static final String CHECK_OK_STATUS = "CheckOkStatus";
	
	public static final String OPENED_THEME_NAME = "OpenedThemeName";
	
	public static final String PROLOG = "Prolog";
	
	public static final String CHECKOUT = "Checkout";

	public static final String LAYERTREE_SUBSYSTEM ="LayerTree";
		 
	public static final String EVENT_LAYERTREE_SELCTION_CHANGED ="LayerTreeSelectionChanged";
		 
	public static final String EVENT_IMAGE_CHANGED ="ImageChanged";
		 
	public static final String EVENT_UPDATE_DATA ="DataUpdate";
		 
	public static final String EVENT_UPDATE_DIALOG ="DilaogUpdate";
		 
	public static final String EVENT_LAYER_ADDED="LayerAdded";

	public static final String EVENT_LAYER_DELETED="LayerDeleted";
	
	public static final String EVENT_EFFECT_DELETED="EffectDeleted";
	
	public static final String EVENT_EFFECT_ADDED="EffectAdded";
	
	public static final String EFFECT_NAME="EffectName";
	
	public static final String ANIMATED_ELMENT="AnimatedElement";
	
	public static final String ATTR_VALUE_ACTUAL = "actual";
    
	public static final String ATTR_VALUE_DRAFT = "draft";
	
	public static final String CURRENT_S60_THEME = "currents60theme";
	
	public static final String ANIMATE_OR_NOT = "AnimateOrNot";
	
	public static final String MENUBAR_VIEW_MENU_ITEMS_STATE = "ViewMenuItemsState";
	 	
	public static final String SHIFT = "shift";
	
	public static final String EVENT_PREVIEW_IMAGES_UPDATE_END ="PreviewImagesUpdateEnd";
	
	public static final String EVENT_PREVIEW_IMAGES_UPDATE_START ="PreviewImagesUpdateStart";
	

	public static final String CONTINUOUS_MODE = "ContinuousMode";
    
    public static final String CYCLE_MODE = "Cycle";
    
    public static final String PLAY_MODE = "Play";
	 
	public static final String HALFCYCLE_MODE = "HalfcycleMode";
	
	public static final String ELEMENT_ID = "element_id";
	
	public static final String RENDERED_IMAGE = "planar_image";
	
	public static final String ANIMATIONWINDOW_SUBSYSTEM = "AnimationWindow";
	
    public static final String OFFSETX = "offsetX";
    
    public static final String OFFSETY = "offsetY";
    
    public static final String SHOW_EFFECTS_DIALOG="Show_Effects_Dialog";
	
    public static final String UPDATE_EDITINGAREA="update_editingarea";

    public static final String TEMP_DIR_NAME = "TempTheme";
    
    public static final String AGGREGATED_IMAGE = "Aggregated_image";
    
    public static final String REFRESH_DIALOG="Refresh_Dialog";
    
    public static final String MASK_IMAGE = "mask_image";

    public static final String SELECTED_IMAGE_RECT = "selImgRect";

    public static final String SCREEN_RECT = "screenRect";

    public static final String ICON_IMAGE_MAP = "Icon_Image_Map";

    public static final String ICON_MASK_MAP = "Icon_Mask_Map";
    
    public static final String DOUBLE_CLICKED_STRING = "DoubleClickedString";
    
    public static final String LAYER_MODEL = "LayerModel";
    
    public static final String RETURN_MASK_PATH = "return_mask_path";

    public static final String RETURN_FILE_PATH = "return_file_path";
	
	public static final String RETURN_FILE_INDEX = "return_file_index";
    
    public static final String PROPERTIES_LIST="propertiesList";
    
    public static final String PROPERTY_CHANGED="propertychanged";
	
	public static final int SCALE = 1;

    public static final int ROTATE = 2;

    public static final String OUTCODE = "outcode";

    public static final String MODE = "mode";

    public static final String MOUSEX = "mousex";

    public static final String MOUSEY = "mousey";

    public static final String SCALEEDGES = "scaleedges";

    public static final String ISALTDOWN = "isaltdown";

    public static final String ISSHIFTDOWN = "isshiftdown";

    public static final String IMAGEX = "imagex";

    public static final String IMAGEY = "imagey";

    public static final String IMAGE = "image";

    public static final String RETURNIMAGE = "returnimage";

    public static final String RETURNIMAGEX = "returnimagex";

    public static final String RETURNIMAGEY = "returnimagey";

    public static final String POLYGON = "polygon";
    
    public static final String TOOLBOX_MIRROR_ON = "ToolBoxMirrorOn";
    
    public static final String TOOLBOX_SECTIONS_ON = "ToolBoxSectionsOn";
    
    public static final String TOOLBOX_MASK_ON = "ToolBoxMaskOn";
    
    public static final String TOOLBOX_DESIGN_ON = "ToolBoxDesignOn";
    
    public static final String SHOW_DEBUGGING_DIALOG = "showDebuggingDialog";
    
    public static final String DONT_SHOW_THIS_MESSAGE = "DONT_SHOW_MESSAGE"; 
    
    public static final String MESSAGE  = "MESSAGE";
    
    public static final String BACKGROUND_IMAGE = "BACKGROUND_IMAGE";

    public static final String BACKGROUND_IMAGE_NAME = "BACKGROUND_IMAGE_NAME";
    
    public static final String GALLERY_S60THEME_CHANGED = "GALLERY_S60THEME_CHANGED";
    
    
	static {

		LOGGING_SUBSYSTEM_LIST[0] = THEMESTUDIO_ROOT_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[1] = EDITINGAREA_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[2] = TASKAREA_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[3] = COMPONENTLIST_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[4] = ELELMENTLIST_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[5] = ANIMATIONLIST_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[6] = BROWSERAREA_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[7] = PREVIEW_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[8] = LAYOUT_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[9] = EVENT_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[10] = CREATION_FUNC_ROW_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[11] = GALLERY_FUNC_ROW_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[12] = CREATION_TOOL_BOX_SUBSYSTEM;

		LOGGING_SUBSYSTEM_LIST[13] = MENU_BAR_SUBSYSTEM;

	}
	
	public static final String CL_TA_GET_POINTS = "getPoints";
	
	public static final String CL_EAC_GET_POINTS = "connectingPoints";
	
	public static final String SELECTED_TASK = "selectedTask";
	
	public static final String SELECTED_CG = "selectedCG";
	
	public static final String SELECTED_COMP ="selectedComp";

	public static final String SELECTED_ELEMENT ="selectedElement"; 
	
	public static final String LAYOUT_FILES_HASHMAP_DELIMITER = "X";
	
	public static final String LAYOUTS_DIR = "layouts";

	public static final String PREVIEW_TYPE_GALLERY = "Gallery";

	public static final String PREVIEW_TYPE_CREATION = "Creation"; 	
	
	
	public static final int ELEMENT_TYPE_GRAPHIC = 1;

	public static final int ELEMENT_TYPE_TEXT = 2;

	public static final int ELEMENT_TYPE_SOUND = 3;

	public static final int ELEMENT_TYPE_MEDIA = 4;

	public static final int ELEMENT_TYPE_IMAGEFILE = 5; 

	public static final String ELEMENT_TYPE = "element_type";
	/* for the preview subsystem - Uma */

	public static final String EVENT_PREVIEW_IMAGES_TOUPDATE ="PreviewImagesToUpdate";
	
	public static final String PREVIEW_GENERATION_SUBSYSTEM =
		"PreviewGeneration";

	public static final String EVENT_PREVIEW_ALL_IMAGES_TOGENERATE =
		"PreviewAllImagesToGenerate";
	public static final String EVENT_PREVIEW_ALL_IMAGES_GENERATED =
		"PreviewAllImagesGenerated";

	public static final String EVENT_PREVIEW_IMAGE_TOGENERATE =
		"PreviewImageToGenerate";
	public static final String EVENT_PREVIEW_IMAGE_GENERATED =
		"PreviewImageGenerated";

	public static final String EVENT_PREVIEW_IMAGE_TOUPDATE =
		"PreviewImageToUpdate";
	public static final String EVENT_PREVIEW_IMAGE_UPDATED =
		"PreviewImageUpdated";

	public static final String EVENT_PREVIEW_FOREGROUNDIMAGE_TOGENERATE =
		"PreviewForegroundImageToGenerate";

	public static final Object EVENT_PARAM_PREVIEW_UPDATE_REGENERATION = "PreviewUpdateRegeneration";
	public static final String EVENT_PREVIEW_FOREGROUNDIMAGE_GENERATED =
		"PreviewForegroundImageGenerated";

	public static final String EVENT_PREVIEW_BACKGROUNDIMAGE_TOGENERATE =
		"PreviewBackgroundImageToGenerate";
	public static final String EVENT_PREVIEW_BACKGROUNDIMAGE_GENERATED =
		"PreviewBackgroundImageGenerated";

	public static final String EVENT_PREVIEW_EDITINGAREAIMAGE_TOGENERATE =
		"PreviewEditingAreaImageToGenerate";
	public static final String EVENT_PREVIEW_EDITINGAREAIMAGE_GENERATED =
		"PreviewEditingAreaImageGenerated";


	public static final String EVENT_PARAM_SCREEN_NAME = "screenName";
	public static final String EVENT_PARAM_FORCE_GENERATE = "forceGenerate";
	public static final String EVENT_PARAM_RESOLUTION = "resolution";
	public static final String EVENT_PARAM_ORIENTATION = "orientation";	
	public static final String EVENT_PARAM_PHONE_MODEL = "phoneModel";
	public static final String EVENT_PARAM_ORIGINATOR = "originator";
	public static final String EVENT_PARAM_ORIGINATOR_DRAFT = "draft";	
	public static final String EVENT_PARAM_ORIGINATOR_SAVE = "save";
	public static final String EVENT_PARAM_ORIGINATOR_SAVE_ENABLED = "saveEnabled";

	public static final String EVENT_PARAM_SKIN_ELEMENT_TOSKIP = "skinElementToSkip";
	public static final String EVENT_PARAM_SKIN_ELEMENT_UPDATED = "skinElementUpdated";
	public static final String EVENT_PARAM_ONLY_GALLERY_SCREENS = "onlyGalleryScreens";
	
	public static final String EVENT_PARAM_GENERATED_IMAGE = "generatedImage";
	public static final String EVENT_PARAM_ALL_GENERATED_IMAGES_LIST = "allGeneratedImages";
	public static final String EVENT_PARAM_ORIGINATING_EVENT_TYPE = "eventType";	

	//for constructing skinable entity vector in editing area
	public static final String SKINNABLE_ENTITY = "skinnableEntity"; 
	
	
	public static final int MULTI_ELEMENT_START_X = 20;
	public static final int MULTI_ELEMENT_START_Y = 20;
	public static final int MULTI_ELEMENT_RIGHT_SPACE = 20;
	public static final int MULTI_ELEMENT_HV_SPACE =35;
	public static final int MULTI_ELEMENT_BOTTOM_SPACE = 20;  
	
	public static final String EVENT_PARAM_ORIGINATOR_PREVIEW = "preview";
	public static final String EVENT_PARAM_ORIGINATOR_CHECKOUT = "checkout";
	
	public static final int BOUNDBOXLEN = 7;	
	
	public static final String IDMAPPINGS_CONFIG_DIR = "IDMAPPINGS_CONFIG_DIR";
	public static final String LAYOUT_CONFIG_DIR = "LAYOUT_CONFIG_DIR";	
    
	public static final String CTRL = "ctrl";
    public static final String ALT= "alt";
    public static final String SPACE = "space";
    public static final String UP = "up";
    public static final String FILE_NAME = "file_name";
    public static final String COMP_FILE_NAME = "comp_file_name";

	public static final int ELEMENT_IMAGE_PARAM_PREVIEW_IMAGE = 3;
	
	public static final String SIGNAL="signal";
	
	public static final String BATTERY="battery";
	
	public static final String TRUE="true";
	
	public static final String DEFAULT_PREVIEW_SCREEN="defaultPreviewScreens";
	
	public static final String ANIM_PREVIEW_SCREEN="animationPreviewScreens";
	
	public static final String MODEL_MODE="model_mode";
	
	public static final String IS_SVG_IMAGE="isSVGImage";

	public static final String PREVIEW_CACHE_NAME = "previewCache";

    public static final String IMAGE_CACHE_NAME = "imageCache";

    public static final String SENTITY_CACHE_NAME = "sEntityCache";

	 public static final String BACKGROUND_LAYER_CACHE_NAME = "backgroundLayerCache";

    public static final String STARTS_P = "p";
    
    public static final String VALID_CHANGE = "validChange";
     
    public static final String SCROLL_VALUE = "SCROLL_VALUE";
    
    public static final String PREVIEW_SCROLL_DELAY = "PREVIEW_SCROLL_DELAY";
    
    public static final String SMOOTH_SCROLL_DELAY = "SMOOTH_SCROLL_DELAY";
    
    public static final String TILE="Tile";
    
    public static final String TILE_X="Tile_X";
    
    public static final String TILE_Y="Tile_Y";
    
    public static final String RESETMASK = "reset_mask";
    
    //public static final String COLORMARK = "colormark";
    
    public static final String TOOLBOX_TILE_STRING = "toolboxTileString";
    
    public static final String CHANGE_IN_RESOLUTION = "changeInResolution";
    
    public static final String EDIT_IMAGE_APP = "editImageApplication";
    
    public static final String EDIT_VECTOR = "Vector";
    
    public static final String EDIT_BMP = "Bitmap";
    
    public static final String COLORIMAGE = "colorimage";
    
    public static final String BOOLEAN = "boolean";
    
    public static final String CHANGE_AGGREGATE_IMAGE = "changeAggregateImage";
    
    public static final String STATE_INDICATOR = "stateIndicator";
    
    public static final String ORIGINAL_STATE = "originalState";
    
    public static final String CURRENT_STATE = "currentState";
    
    public static final String SHOW_SVGT_CONVERTER_POPUP = "SHOW_SVG_TO_SVGT_CONVERSION_DIALOG";
    
    public static final String LAYERS_STATE = "layersState";
	
	public static final String PROPERTIES_BITMAP = "Single Bitmap";
	
	public static final String PROPERTIES_3_PIECE = "3 Piece Bitmap";
	
	public static final String PROPERTIES_9_PIECE = "9 Piece Bitmap";
	
	public static final String PROPERTIES_11_PIECE = "11 Piece Bitmap";
	
	public static final String RESET_TOOLBOX = "resetToolBox";	
	
	public static final String RESET_ELEMENT_LIST = "resetElementList";
	
	public static final String RESET_ANIMATION_LIST = "resetAnimationList";
	
	public static final String RESET_FUNC_ROW = "resetFuncRow";
	
	public static final String RESET_ALL = "resetAll";
	
	public static final String EDITINGAREA_MAX_SIZE = "EditingAreaMaxSize";
	
	public static final String EDITINGAREA_MID_SIZE = "EditingAreaMidSize";
	
	public static final String COMPONENTLIST_SUBSYSTEM_MAX = "ComponentListSubsystemMax";
	
	public static final String TOPLEFT="Top Left";
    
    public static final String TOPRIGHT="Top Right";
    
    public static final String BOTTOMLEFT="Bottom Left";
    
    public static final String BOTTOMRIGHT="Bottom Right";
    
    public static final String CENTER="Center";
	
	public static final String ANIMATION_CYCLE="Cycle";
	
	public static final String ANIMATION_PLAY="Play";
	
	public static final String EDIT_SOUND="Sound";
	
	public static final String MENU_BAR_HELP = "MENU_BAR";
	
	public static final String GALLERY_VIEW_TAB_HELP = "GALARY_VIEW_TAB";
	
	public static final String GALARY_VIEW_FUNCTION_ROW_HELP = "GALARY_VIEW_FUNCTION_ROW";
	
	public static final String BROWSER_FRAME_HELP = "BROWSER_FRAME";
	
	public static final String BLANK_HELP = "blank";
	
	public static final String BROWSER_HELP = "BROWSER_TAB";
	
	public static final String MYWORKS_HELP = "MYWORKS_TAB";
	
	public static final String LIBRARY_HELP = "LIBRARY_TAB";
	
	public static final String CREATION_VIEW_TASK_AREA_HELP = "CREATION_VIEW_TASK_AREA";
	
	public static final String CREATION_VIEW_COMPONENT_AREA_HELP = "CREATION_VIEW_COMPONENT_AREA";
	
	public static final String CREATION_VIEW_ELEMENT_AREA_HELP = "CREATION_VIEW_ELEMENT_AREA";
	
	public static final String CREATION_VIEW_EDITING_AREA_HELP = "CREATION_VIEW_EDITING_AREA";
	
	public static final String CREATION_VIEW_FUNCTION_AREA_HELP = "CREATION_VIEW_FUNCTION_AREA";
	
	public static final String CREATION_VIEW_TOOL_BOX_AREA_HELP = "CREATION_VIEW_TOOL_BOX_AREA";
	
	public static final String FIXTOOLBAR_ADD_TO_FIX_LIST_HELP = "FIXTOOLBAR_SEE_COMP";
	
	public static final String FIXTOOLBAR_EDIT_FIX_LIST_HELP = "FIXTOOLBAR_TO_FIX";
	
	public static final String FIXTOOLBAR_CONTINUE_HELP = "FIXTOOLBAR_CONTINUE";
	
	public static final String FIXTOOLBAR_CANCEL_HELP = "FIXTOOLBAR_CANCEL";
	
	public static final String CREATION_VIEW_PREVIEW_HELP = "CREATION_VIEW_PREVIEW";
	
	public static final String CURRENT_THEME = "CURRENT_THEME";
	
	public static final String REPLACE_WIDTH = "##WIDTH##";
	
	public static final String REPLACE_HEIGHT = "##HEIGHT##";
	
	public static final String TOOLBOX_SOFT_MASK_STATE = "SoftMaskState";
    
    public static final String DELAYS = "delays";
    
    public static final String MENUBAR_RENAME_STATE = "MENUBAR_RENAME_STATE";
    
    public static final String SHOW_DIALOG = "ShowDialog";
    
    public static final String PARAMETER_ANIMATED = "parameterAnimated";
	
	public static final String ALLOWED_ENTITIES_PATH_50 = "ALLOWED_ENTITIES_PATH_50";
		
	public static final String COLORDATA_OBJECT = "COLORDATA_OBJECT";
    
    public static final String IS_MORPHED = "IS_MORPHED";

    public static final String ATTR_USE_LOCID = "USE_LOC_ID_WHILE_RENDERING";
}
