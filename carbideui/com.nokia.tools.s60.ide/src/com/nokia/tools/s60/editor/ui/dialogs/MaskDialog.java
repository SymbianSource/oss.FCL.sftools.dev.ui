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

package com.nokia.tools.s60.editor.ui.dialogs;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.nokia.tools.media.utils.MaskUtils;
import com.nokia.tools.s60.ide.S60WorkspacePlugin;
import com.nokia.tools.screen.ui.branding.ISharedImageDescriptor;
import com.nokia.tools.ui.branding.util.BrandedTitleAreaDialog;

public class MaskDialog extends BrandedTitleAreaDialog {

	// public static final String MASK_DIALOG_CONTEXT =
	// "com.nokia.tools.s60.ide.keyPairsDialog_context"; 
	private Image inputImage, penImage, wandImage, eraseImage, clearImage,
	    infoImage;

	private ImageData maskImageData = null;

	private Composite canvasComposite, sizerElementsComposite;

	private Button penButton, wandButton, eraseButton, clearMaskButton;

	private Canvas canvas = null;

	private Canvas canvasMask = null;

	private Canvas canvasWithoutMask = null;

	private Canvas canvasResult = null;

	private static int maskPixelBlackValue = 0;

	private int noMaskPixelColorValue = 255;

	private float zoomFactor = 1;

	private int maxCanvasSize = 300;

	private int maxPreviewCanvasSize = 50;

	private float previewZoomFactor;

	private boolean invertMask = false;

	private Cursor arrowCursor, handCursor, penCursor, wandCursor,
	    rubberCursor;

	private boolean mousePressed = false;

	private boolean isDirty;

	private int sizerSize = 1; // Default value can be 1, 3, 5, 7, 9, 11 or 13

	private CustomCanvas sizer1x1, sizer3x3, sizer5x5, sizer7x7, sizer9x9,
	    sizer11x11, sizer13x13;

	private Color unselectedColor;

	private static final Color selectedColor = ColorConstants.menuBackgroundSelected;

	private Composite sizeInfoComposite;

	private Label lblSize;

	public MaskDialog(Shell parentShell, String imageFilePath,
	    String maskFilePath, boolean invertMask) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
		this.invertMask = invertMask;
		initImages(imageFilePath, maskFilePath);
	}

	public void initImages(String imageFilePath, String maskFilePath) {
		inputImage = new Image(this.getParentShell().getDisplay(),
		    imageFilePath);
		int imageWidth = inputImage.getImageData().width;
		int imageHeight = inputImage.getImageData().height;
		computeZoomFactor(imageWidth, imageHeight);
		Image maskImage = null;
		if (maskFilePath != null) {
			ImageDescriptor desc = ImageDescriptor.createFromFile(null,
			    maskFilePath);
			if (desc != null) {
				ImageData id = desc.getImageData();
				if (id != null) {
					if (id.width != imageWidth || id.height != imageHeight) {
						id = id.scaledTo(imageWidth, imageHeight);
					}
					maskImage = new Image(this.getParentShell().getDisplay(),
					    id);
				}
			}
		}

		if (maskImage == null) {
			PaletteData palette = createGrayscalePaletteData();

			maskImageData = new ImageData(imageWidth, imageHeight, 8, palette);
			fillImageDataWithWhite(maskImageData);
		} else {

			maskImageData = maskImage.getImageData();
			
			if (maskImageData.depth == 8) {
				if (invertMask) {
					changeColor(maskImageData, 4, 255);
					changeColor(maskImageData, 252, 0);
				} else {
					changeColor(maskImageData, 4, 0); // black
					changeColor(maskImageData, 252, 255); // white
					changeAllButColor(maskImageData, 0, 255); // set soft mask
					// pixels either to
					// the black or
					// white
				}
			} else {
				changeColor(maskImageData, 4, 0); // black
				changeColor(maskImageData, 252, 255); // white
			}
		}

		// inputImage.dispose();
		if (maskImage != null)
			maskImage.dispose();
	}

	private void computeZoomFactor(int imageWidth, int imageHeight) {
		float widthZoomFactor = maxCanvasSize / (float) imageWidth;
		float heightZoomFactor = maxCanvasSize / (float) imageHeight;
		zoomFactor = widthZoomFactor < heightZoomFactor ? widthZoomFactor
		    : heightZoomFactor;

		widthZoomFactor = maxPreviewCanvasSize / (float) imageWidth;
		heightZoomFactor = maxPreviewCanvasSize / (float) imageHeight;

		previewZoomFactor = widthZoomFactor < heightZoomFactor ? widthZoomFactor
		    : heightZoomFactor;
	}

	private void clearMaskImageData(ImageData imgData) {
		int pixel = imgData.palette.getPixel(Display.getDefault()
		    .getSystemColor(SWT.COLOR_WHITE).getRGB());
		for (int i = 0; i < imgData.width; i++) {
			for (int j = 0; j < imgData.height; j++) {
				imgData.setPixel(i, j, pixel);
			}
		}
		isDirty = true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(parent.getParent(),
		// MaskDialog.MASK_DIALOG_CONTEXT);

		setTitle(Messages.MaskDialog_Banner_Title);
		setMessage(Messages.MaskDialog_Message);

		Display display = getShell().getDisplay();
		arrowCursor = new Cursor(display, SWT.CURSOR_ARROW);
		handCursor = new Cursor(display, SWT.CURSOR_HAND);
		penCursor = new Cursor(display, S60WorkspacePlugin.getImageDescriptor(
		    "icons/cursors/cursor_pointer.gif").getImageData(), 10, 21);
		wandCursor = new Cursor(display, S60WorkspacePlugin.getImageDescriptor(
		    "icons/cursors/cursor_bucket.gif").getImageData(), 13, 14);
		rubberCursor = new Cursor(display, S60WorkspacePlugin
		    .getImageDescriptor("icons/cursors/cursor_remove.gif")
		    .getImageData(), 9, 20);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 13;
		layout.marginWidth = 13;
		layout.verticalSpacing = 7;
		composite.setLayout(layout);

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 7;
		container.setLayout(layout);

		Composite canvasMainComposite = new Composite(container, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		canvasMainComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 2;
		canvasMainComposite.setLayout(layout);

		Composite ButtonComposite = new Composite(canvasMainComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_VERTICAL);
		ButtonComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		ButtonComposite.setLayout(layout);

		wandButton = new Button(ButtonComposite, SWT.FLAT | SWT.TOGGLE);
		wandButton.setToolTipText(Messages.MaskDialog_Wand_Tooltip);
		wandImage = S60WorkspacePlugin.getImageDescriptor(
		    "icons/bucket16x16.png").createImage();
		wandButton.setImage(wandImage);
		wandButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				wandButton.setSelection(true);
				penButton.setSelection(false);
				eraseButton.setSelection(false);
				sizerElementsComposite.setVisible(false);
				sizerElementsComposite.setCursor(arrowCursor);
				canvasComposite.setCursor(wandCursor);
				lblSize.setVisible(false);
			}
		});

		penButton = new Button(ButtonComposite, SWT.FLAT | SWT.TOGGLE);
		penButton.setToolTipText(Messages.MaskDialog_Pen_Tooltip);
		penImage = S60WorkspacePlugin.getImageDescriptor(
		    "icons/pointer16x16.png").createImage();
		penButton.setImage(penImage);
		penButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				wandButton.setSelection(false);
				penButton.setSelection(true);
				eraseButton.setSelection(false);
				sizerElementsComposite.setVisible(true);
				sizerElementsComposite.setCursor(handCursor);
				canvasComposite.setCursor(penCursor);
				lblSize.setVisible(true);
			}
		});

		eraseButton = new Button(ButtonComposite, SWT.FLAT | SWT.TOGGLE);
		eraseButton.setToolTipText(Messages.MaskDialog_Eraser_Tooltip);
		eraseImage = S60WorkspacePlugin.getImageDescriptor(
		    "icons/remove16x16.png").createImage();
		eraseButton.setImage(eraseImage);
		eraseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				wandButton.setSelection(false);
				penButton.setSelection(false);
				eraseButton.setSelection(true);
				sizerElementsComposite.setVisible(true);
				sizerElementsComposite.setCursor(handCursor);
				canvasComposite.setCursor(rubberCursor);
				lblSize.setVisible(true);
			}
		});

		clearMaskButton = new Button(ButtonComposite, SWT.FLAT | SWT.PUSH);
		clearMaskButton.setToolTipText(Messages.MaskDialog_Clear_Tooltip);
		clearImage = S60WorkspacePlugin.getImageDescriptor(
		    "icons/etool16/clear_co.gif").createImage();
		clearMaskButton.setImage(clearImage);
		clearMaskButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (MessageDialog.openQuestion(getShell(),
				    Messages.MaskDialog_ClearConfirmation_Title,
				    Messages.MaskDialog_ClearConfirmation_Message)) {
					clearMaskImageData(maskImageData);
					repaintCanvas();
					repaintMaskCanvas();
					repaintResultCanvas();
				}
			}
		});

		Composite sizersComposite = new Composite(ButtonComposite, SWT.BORDER);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.widthHint = 40;
		gd.heightHint = 219;
		gd.horizontalIndent = 6;
		gd.verticalIndent = 3;
		sizersComposite.setLayoutData(gd);
		layout = new GridLayout();
		sizersComposite.setLayout(layout);

		sizerElementsComposite = new Composite(sizersComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		sizerElementsComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		gd.horizontalAlignment = SWT.CENTER;
		sizerElementsComposite.setLayout(layout);
		sizerElementsComposite.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent pe) {
				unselectedColor = sizerElementsComposite.getBackground();
				updateSizer();
			}
		});

		gd = new GridData();
		gd.widthHint = 21;
		gd.heightHint = 21;

		sizer1x1 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 1, 1);
		sizer1x1.setLayoutData(gd);
		sizer1x1.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 1;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizer3x3 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 3, 3);
		sizer3x3.setLayoutData(gd);
		sizer3x3.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 3;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizer5x5 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 5, 5);
		sizer5x5.setLayoutData(gd);
		sizer5x5.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 5;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizer7x7 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 7, 7);
		sizer7x7.setLayoutData(gd);
		sizer7x7.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 7;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizer9x9 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 9, 9);
		sizer9x9.setLayoutData(gd);
		sizer9x9.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 9;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizer11x11 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 11, 11);
		sizer11x11.setLayoutData(gd);
		sizer11x11.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 11;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizer13x13 = new CustomCanvas(sizerElementsComposite, SWT.NONE, 13, 13);
		sizer13x13.setLayoutData(gd);
		sizer13x13.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					sizerSize = 13;
					updateSizer();
				}
			}

			public void mouseUp(MouseEvent e) {
			}
		});

		sizeInfoComposite = new Composite(sizersComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.verticalIndent = 2;
		sizeInfoComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		sizeInfoComposite.setLayout(layout);

		lblSize = new Label(sizeInfoComposite, SWT.CENTER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 40;
		lblSize.setLayoutData(gd);

		ScrolledComposite canvasScrolledComposite = new ScrolledComposite(
		    canvasMainComposite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		gd = new GridData(GridData.FILL_BOTH);
		canvasScrolledComposite.setLayoutData(gd);
		canvasScrolledComposite.setLayout(new FillLayout());
		canvasScrolledComposite.setExpandHorizontal(true);
		canvasScrolledComposite.setExpandVertical(true);
		canvasScrolledComposite.getHorizontalBar().setIncrement(13);
		canvasScrolledComposite.getHorizontalBar().setPageIncrement(
		    2 * canvasScrolledComposite.getHorizontalBar().getIncrement());
		canvasScrolledComposite.getVerticalBar().setIncrement(13);
		canvasScrolledComposite.getVerticalBar().setPageIncrement(
		    2 * canvasScrolledComposite.getVerticalBar().getIncrement());

		canvasComposite = new Composite(canvasScrolledComposite, SWT.NONE);
		canvasScrolledComposite.setContent(canvasComposite);
		gd = new GridData(GridData.FILL_BOTH);
		gd.minimumWidth = inputImage.getBounds().width;
		gd.minimumHeight = inputImage.getBounds().height;
		canvasComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		canvasComposite.setLayout(layout);
		canvasComposite.setBackground(ColorConstants.white);
		canvasComposite.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				int w = canvasComposite.getBounds().width;
				int h = canvasComposite.getBounds().height;
				int imageW = inputImage.getBounds().width;
				int imageH = inputImage.getBounds().height;
				float wFactor = (float) imageW / imageH;
				float hFactor = (float) imageH / imageW;
				float wGrowth = ((h - imageH) * wFactor) + imageW;
				float hGrowth = ((w - imageW) * hFactor) + imageH;
				if ((h - hGrowth) < (w - wGrowth))
					zoomFactor = h / (float) imageH;
				else
					zoomFactor = w / (float) imageW;
				canvas.computeSize((int) (imageW * zoomFactor),
				    (int) (imageH * zoomFactor), false);
				canvas.layout();
				repaintCanvas();
			}
		});

		canvas = new Canvas(canvasComposite, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = (int) (inputImage.getBounds().width * zoomFactor);
		gd.heightHint = (int) (inputImage.getBounds().height * zoomFactor);
		canvas.setLayoutData(gd);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent pe) {
				GC gc = pe.gc;
				repaintCanvas();
				gc.dispose();
			}
		});

		canvas.setBackground(ColorConstants.white);
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					mousePressed = true;
					int posX = (int) Math.floor(e.x / zoomFactor);
					int posY = (int) Math.floor(e.y / zoomFactor);
					int p = maskImageData.getPixel(posX, posY);
					if (penButton.getSelection()) {
						if (sizerSize == 1) {
							if (p != maskPixelBlackValue)
								addMaskPixel(posX, posY, sizerSize);
						} else
							addMaskPixel(posX, posY, sizerSize);
					}
					if (wandButton.getSelection()) {
						if (p != maskPixelBlackValue)
							drawWandMask(posX, posY);
					} else if (eraseButton.getSelection()) {
						if (sizerSize == 1) {
							if (p != noMaskPixelColorValue)
								removeMaskPixel(posX, posY, sizerSize);
						} else
							removeMaskPixel(posX, posY, sizerSize);
					}
				}
			}

			public void mouseUp(MouseEvent e) {
				mousePressed = false;
			}
		});

		canvas.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (mousePressed) {
					int posX = (int) Math.floor(e.x / zoomFactor);
					int posY = (int) Math.floor(e.y / zoomFactor);
					if (posX >= inputImage.getBounds().width
					    || posY >= inputImage.getBounds().height || posX < 0
					    || posY < 0) {
						return;
					}
					int p = maskImageData.getPixel(posX, posY);
					if (penButton.getSelection()) {
						if (sizerSize == 1) {
							if (p != maskPixelBlackValue)
								addMaskPixel(posX, posY, sizerSize);
						} else
							addMaskPixel(posX, posY, sizerSize);
					}
					if (wandButton.getSelection()) {
						if (p != maskPixelBlackValue)
							drawWandMask(posX, posY);
					} else if (eraseButton.getSelection()) {
						if (sizerSize == 1) {
							if (p != noMaskPixelColorValue)
								removeMaskPixel(posX, posY, sizerSize);
						} else
							removeMaskPixel(posX, posY, sizerSize);
					}
				}
			}
		});

		canvasScrolledComposite.setMinSize(inputImage.getBounds().width + 2,
		    inputImage.getBounds().height + 2);

		Group canvasPreviewGroup = new Group(container, SWT.NONE);
		canvasPreviewGroup.setText(Messages.MaskDialog_Preview_Label);
		gd = new GridData(SWT.FILL, SWT.TOP, false, false);
		canvasPreviewGroup.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginLeft = 9;
		layout.marginRight = 0;
		layout.marginTop = 9;
		layout.marginBottom = 0;
		layout.verticalSpacing = 7;
		canvasPreviewGroup.setLayout(layout);

		Composite canvasTopPreviewComposite = new Composite(canvasPreviewGroup,
		    SWT.NONE);
		canvasTopPreviewComposite.setLayoutData(new GridData(
		    GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		canvasTopPreviewComposite.setLayout(layout);

		Composite canvasMaskPreviewComposite = new Composite(
		    canvasTopPreviewComposite, SWT.NONE);
		canvasMaskPreviewComposite.setLayoutData(new GridData());
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		canvasMaskPreviewComposite.setLayout(layout);

		Label canvasMaskLabel = new Label(canvasMaskPreviewComposite, SWT.NONE);
		canvasMaskLabel.setText(Messages.MaskDialog_Mask_Label);

		canvasMask = new Canvas(canvasMaskPreviewComposite, SWT.NONE);
		canvasMask.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent pe) {
				repaintMaskCanvas();
			}
		});

		Composite canvasInputPreviewComposite = new Composite(
		    canvasTopPreviewComposite, SWT.NONE);
		canvasInputPreviewComposite.setLayoutData(new GridData());
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		canvasInputPreviewComposite.setLayout(layout);

		Label canvasWithoutMaskLabel = new Label(canvasInputPreviewComposite,
		    SWT.NONE);
		canvasWithoutMaskLabel.setText(Messages.MaskDialog_Input_Label);

		canvasWithoutMask = new Canvas(canvasInputPreviewComposite, SWT.NONE);
		canvasWithoutMask.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent pe) {
				GC gc = pe.gc;
				int widthZoomed = (int) (inputImage.getBounds().width * previewZoomFactor);
				int heightZoomed = (int) (inputImage.getBounds().height * previewZoomFactor);
				if (widthZoomed == 0) {
					widthZoomed = 1;
				}
				if (heightZoomed == 0) {
					heightZoomed = 1;
				}
				Image image = new Image(canvasWithoutMask.getDisplay(),
				    inputImage.getImageData().scaledTo(widthZoomed,
				        heightZoomed));
				gc.drawImage(image, 0, 0);
				image.dispose();
			}
		});

		Composite canvasBottomPreviewComposite = new Composite(
		    canvasPreviewGroup, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		canvasBottomPreviewComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		canvasBottomPreviewComposite.setLayout(layout);

		Composite canvasResultPreviewComposite = new Composite(
		    canvasBottomPreviewComposite, SWT.NONE);
		gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		gd.grabExcessHorizontalSpace = true;
		gd.verticalAlignment = SWT.CENTER;
		gd.grabExcessVerticalSpace = true;
		canvasResultPreviewComposite.setLayoutData(gd);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 7;
		canvasResultPreviewComposite.setLayout(layout);

		Label canvasResultLabel = new Label(canvasResultPreviewComposite,
		    SWT.NONE);
		canvasResultLabel.setText(Messages.MaskDialog_Result_Label);

		canvasResult = new Canvas(canvasResultPreviewComposite, SWT.NONE);

		canvasResult.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent pe) {
				repaintResultCanvas();
			}
		});

		Composite infoComposite = new Composite(container, SWT.NULL);
		layout = new GridLayout();
		infoComposite.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		infoComposite.setLayoutData(gd);
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 8;

		Label lblInfoImage = new Label(infoComposite, SWT.NONE);
		gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
		lblInfoImage.setLayoutData(gd);
		infoImage = ISharedImageDescriptor.ICON16_INFO.createImage();
		lblInfoImage.setImage(infoImage);

		Label lblInfo = new Label(infoComposite, SWT.WRAP);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		lblInfo.setLayoutData(gd);
		lblInfo.setText(Messages.MaskDialog_Info_Label);

		Composite container2 = new Composite(area, SWT.NONE);
		container2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout = new GridLayout();
		container2.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		final Label separator = new Label(container2, SWT.SEPARATOR
		    | SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		separator.setLayoutData(gd);

		// Initial states
		wandButton.setSelection(true);
		canvasComposite.setCursor(wandCursor);
		sizerElementsComposite.setVisible(false);
		lblSize.setVisible(false);
		updateSizer();

		return area;
	}

	private void updateSizer() {
		sizer1x1.setSelected(false);
		sizer3x3.setSelected(false);
		sizer5x5.setSelected(false);
		sizer7x7.setSelected(false);
		sizer9x9.setSelected(false);
		sizer11x11.setSelected(false);
		sizer13x13.setSelected(false);
		sizer1x1.setBackground(unselectedColor);
		sizer3x3.setBackground(unselectedColor);
		sizer5x5.setBackground(unselectedColor);
		sizer7x7.setBackground(unselectedColor);
		sizer9x9.setBackground(unselectedColor);
		sizer11x11.setBackground(unselectedColor);
		sizer13x13.setBackground(unselectedColor);

		switch (sizerSize) {
			case 1:
				sizer1x1.setSelected(true);
				sizer1x1.setBackground(selectedColor);
				break;
			case 3:
				sizer3x3.setSelected(true);
				sizer3x3.setBackground(selectedColor);
				break;
			case 5:
				sizer5x5.setSelected(true);
				sizer5x5.setBackground(selectedColor);
				break;
			case 7:
				sizer7x7.setSelected(true);
				sizer7x7.setBackground(selectedColor);
				break;
			case 9:
				sizer9x9.setSelected(true);
				sizer9x9.setBackground(selectedColor);
				break;
			case 11:
				sizer11x11.setSelected(true);
				sizer11x11.setBackground(selectedColor);
				break;
			case 13:
				sizer13x13.setSelected(true);
				sizer13x13.setBackground(selectedColor);
				break;
			default:
				sizer1x1.setSelected(true);
				sizer1x1.setBackground(selectedColor);
				break;
		}
		lblSize.setText(sizerSize + "x" + sizerSize);
	}

	private void drawWandMask(int posX, int posY) {
		int width = inputImage.getBounds().width;
		int height = inputImage.getBounds().height;
		int value = inputImage.getImageData().getPixel(posX, posY);
		paintBucket(new Point(posX, posY), value, width, height, inputImage
		    .getImageData(), maskImageData);
		isDirty = true;
		refreshAll();
	}

	private void removeMaskPixel(int posX, int posY, int size) {
		if (size == 1)
			maskImageData.setPixel(posX, posY, noMaskPixelColorValue);
		else {
			int width = inputImage.getBounds().width;
			int height = inputImage.getBounds().height;
			int pAround = (size - 1) / 2;
			int x, y;
			for (long i = posX - pAround; i < posX + pAround + 1; i++)
				for (long j = posY - pAround; j < posY + pAround + 1; j++) {
					if (i < 0)
						x = 0;
					else if (i > width - 2)
						x = width - 1;
					else
						x = (int) i;
					if (j < 0)
						y = 0;
					else if (j > height - 2)
						y = height - 1;
					else
						y = (int) j;
					maskImageData.setPixel(x, y, noMaskPixelColorValue);
				}
		}
		isDirty = true;
		refreshAll();
	}

	private void addMaskPixel(int posX, int posY, int size) {
		if (size == 1)
			maskImageData.setPixel(posX, posY, maskPixelBlackValue);
		else {
			int width = inputImage.getBounds().width;
			int height = inputImage.getBounds().height;
			int pAround = (size - 1) / 2;
			int x, y;
			for (long i = posX - pAround; i < posX + pAround + 1; i++)
				for (long j = posY - pAround; j < posY + pAround + 1; j++) {
					if (i < 0)
						x = 0;
					else if (i > width - 2)
						x = width - 1;
					else
						x = (int) i;
					if (j < 0)
						y = 0;
					else if (j > height - 2)
						y = height - 1;
					else
						y = (int) j;
					maskImageData.setPixel(x, y, maskPixelBlackValue);
				}
		}
		isDirty = true;
		refreshAll();
	}

	private void refreshAll() {
		repaintCanvas();
		repaintMaskCanvas();
		repaintResultCanvas();
	}

	private void changeColor(ImageData imgData, int colorToBeChanged,
	    int replacingColor) {
		for (int i = 0; i < imgData.width; i++) {
			for (int j = 0; j < imgData.height; j++) {
				if (imgData.getPixel(i, j) == colorToBeChanged) {
					imgData.setPixel(i, j, replacingColor);
				}
			}
		}
	}

	private void changeAllButColor(ImageData imgData, int colorToBeChanged,
	    int replacingColor) {
		for (int i = 0; i < imgData.width; i++) {
			for (int j = 0; j < imgData.height; j++) {
				if (imgData.getPixel(i, j) != colorToBeChanged
				    && imgData.getPixel(i, j) != replacingColor) {
					if (imgData.getPixel(i, j) < 128) {
						imgData.setPixel(i, j, colorToBeChanged);
					} else {
						imgData.setPixel(i, j, replacingColor);
					}
				}
			}
		}
	}

	private static void fillImageDataWithWhite(ImageData maskImageData) {
		for (int i = 0; i < maskImageData.width; i++) {
			for (int j = 0; j < maskImageData.height; j++) {
				maskImageData.setPixel(i, j, 255);
			}
		}
	}

	private void repaintCanvas() {

		int width = inputImage.getBounds().width;
		int height = inputImage.getBounds().height;

		ImageData maskForDraw = (ImageData) maskImageData.clone();
		maskForDraw = changeBlackToEmphColor(maskForDraw);
		maskForDraw.transparentPixel = 0;
		GC gcMain = new GC(canvas);

		Image image = new Image(canvasResult.getDisplay(), inputImage
		    .getImageData());
		Image maskImg = new Image(canvas.getDisplay(), maskForDraw);
		GC gc = new GC(image);
		gc.drawImage(maskImg, 0, 0);
		gc.dispose();
		maskImg.dispose();

		Image result = new Image(canvasResult.getDisplay(), image
		    .getImageData().scaledTo((int) (zoomFactor * width),
		        (int) (zoomFactor * height)));
		gcMain.drawImage(result, 0, 0);
		result.dispose();
		image.dispose();
		gcMain.dispose();
	}

	private void repaintMaskCanvas() {
		GC gc = new GC(canvasMask);
		int widthZoomed = (int) (maskImageData.width * previewZoomFactor);
		int heightZoomed = (int) (maskImageData.height * previewZoomFactor);
		if (widthZoomed == 0) {
			widthZoomed = 1;
		}
		if (heightZoomed == 0) {
			heightZoomed = 1;
		}

		Image maskImg = new Image(canvasMask.getDisplay(), maskImageData
		    .scaledTo(widthZoomed, heightZoomed));

		gc.drawImage(maskImg, 0, 0);
		gc.dispose();
		maskImg.dispose();
	}

	private void repaintResultCanvas() {
		GC gcResult = new GC(canvasResult);
		int widthZoomed = (int) (inputImage.getBounds().width * previewZoomFactor);
		int heightZoomed = (int) (inputImage.getBounds().height * previewZoomFactor);
		if (widthZoomed == 0) {
			widthZoomed = 1;
		}
		if (heightZoomed == 0) {
			heightZoomed = 1;
		}

		Image image = new Image(canvasResult.getDisplay(), inputImage
		    .getImageData());
		ImageData maskForDrawResult = (ImageData) maskImageData.clone();
		maskForDrawResult.transparentPixel = 255;
		Image mask = new Image(canvasResult.getDisplay(),
		    createTransparencyGridForMask(maskForDrawResult));
		GC gc = new GC(image);
		gc.drawImage(mask, 0, 0);
		gc.dispose();
		mask.dispose();

		Image result = new Image(canvasResult.getDisplay(), image
		    .getImageData().scaledTo(widthZoomed, heightZoomed));
		gcResult.drawImage(result, 0, 0);
		image.dispose();
		result.dispose();
		gcResult.dispose();
	}

	private ImageData createTransparencyGridForMask(ImageData data) {
		ImageData processedData = (ImageData) data.clone();
		if (!processedData.palette.isDirect) {
			for (int i = 0; i < processedData.width; i++) {
				for (int j = 0; j < processedData.height; j++) {
					if (processedData.getPixel(i, j) == maskPixelBlackValue) {
						if ((((i % 10) > 4) && ((j % 10) > 4))
						    || ((i % 10) < 4 && (j % 10) < 4)) {
							processedData.setPixel(i, j, 140);
						} else {
							processedData.setPixel(i, j, 180);
						}
					} else {

					}
				}
			}
		}
		return processedData;
	}

	private ImageData changeBlackToEmphColor(ImageData maskForDraw) {

		for (int i = 0; i < maskForDraw.width; i++) {
			for (int j = 0; j < maskForDraw.height; j++) {
				if (maskForDraw.getPixel(i, j) == 0) {
					maskForDraw.setPixel(i, j, 160);
				} else {
					maskForDraw.setPixel(i, j, 0);
				}
			}
		}
		return maskForDraw;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "&OK", true);
		createButton(parent, IDialogConstants.CANCEL_ID,
		    IDialogConstants.CANCEL_LABEL, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#close()
	 */
	@Override
	public boolean close() {
		if (isDirty) {
			if (!MessageDialog.openQuestion(getShell(),
			    Messages.MaskDialog_CloseConfirmation_Title,
			    Messages.MaskDialog_CloseConfirmation_Message)) {
				return false;
			}
		}
		if (inputImage != null) {
			inputImage.dispose();
		}
		if (penImage != null) {
			penImage.dispose();
		}
		if (wandImage != null) {
			wandImage.dispose();
		}
		if (eraseImage != null) {
			eraseImage.dispose();
		}
		if (clearImage != null) {
			clearImage.dispose();
		}
		if (infoImage != null) {
			infoImage.dispose();
		}
		if (arrowCursor != null) {
			arrowCursor.dispose();
		}
		if (handCursor != null) {
			handCursor.dispose();
		}
		if (penCursor != null) {
			penCursor.dispose();
		}
		if (wandCursor != null) {
			wandCursor.dispose();
		}
		if (rubberCursor != null) {
			rubberCursor.dispose();
		}
		return super.close();
	}

	/**
	 * Algorithm to create the paint bucket Algorithm Paint-Bucket(row, col,
	 * old_color, new_color) if (row,col) is on the grid and the color of
	 * (row,col) is old_color, then: Change the color of (row,col) to new_color.
	 * Paint-Bucket(row,col-1,old_color,new_color)
	 * Paint-Bucket(row,col+1,old_color,new_color)
	 * Paint-Bucket(row-1,col,old_color,new_color)
	 * Paint-Bucket(row+1,col,old_color,new_color) end of if
	 */
	private static void paintBucket(Point currPoint, int pickedColour,
	    int imageWidth, int imageHeight, ImageData copyImageData,
	    ImageData maskImageData) {

		MaskUtils.paintBucket(currPoint, pickedColour, imageWidth, imageHeight,
		    copyImageData, maskImageData);

		

	}

	public ImageData getMaskImageData() {
		return maskImageData;
	}

	private static PaletteData createGrayscalePaletteData() {
		RGB[] eightBitGreyscale = new RGB[256];
		for (int i = 0; i < 256; i++) {
			eightBitGreyscale[i] = new RGB(i, i, i);
		}
		return new PaletteData(eightBitGreyscale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getBannerIconDescriptor()
	 */
	@Override
	protected ImageDescriptor getBannerIconDescriptor() {
		return S60WorkspacePlugin
		    .getImageDescriptor("icons/wizban/extract_mask.png");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.s60.wizards.BrandedTitleAreaDialog#getTitle()
	 */
	@Override
	protected String getTitle() {
		return Messages.MaskDialog_Title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (IDialogConstants.OK_ID == buttonId
		    || IDialogConstants.CANCEL_ID == buttonId) {
			isDirty = false;
		}
		super.buttonPressed(buttonId);
	}
}
