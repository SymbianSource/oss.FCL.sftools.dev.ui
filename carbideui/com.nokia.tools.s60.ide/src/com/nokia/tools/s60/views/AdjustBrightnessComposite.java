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
package com.nokia.tools.s60.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

import com.nokia.tools.screen.ui.propertysheet.color.ColorChangedLabelWrapper;
import com.nokia.tools.screen.ui.propertysheet.color.IColorPickerListener;
import com.nokia.tools.ui.color.ColorBox;
import com.nokia.tools.ui.color.ColorDescriptor;

public class AdjustBrightnessComposite  extends Composite{
	private static final int MIN_VALUE = 0;

	private static final int MAX_VALUE = 100;
	
	private ColorBox referenceBox= null;
	
	private RGB inputColor=new RGB(0,0,0);
	private RGB leadColor = new RGB(0,0,0);
	
	private RGB outputColor;
	protected int colorBoxSize = 16;
	
	private Text brightnessText;
	
	protected IColorPickerListener dialogClose;
	private ColorChangedLabelWrapper colorLabel;
	
	public AdjustBrightnessComposite(Composite parent, int style,ColorChangedLabelWrapper label, IColorPickerListener listener){
		super(parent, style);
		colorLabel=label;
		dialogClose=listener;
		GridLayout layout= new GridLayout();	
		if(label.getColorDescriptor().getRGB()!=null){
			this.inputColor=label.getColorDescriptor().getRGB();
		}		
		if( null != colorLabel && null != colorLabel.getParentColorGroup() ){
			leadColor = colorLabel.getParentColorGroup().getGroupColor();
			//if( leadColor.getHSB()[2]>0.9 )
			//	leadColor = new RGB( leadColor.getHSB()[0],leadColor.getHSB()[1], 0.9f); 
		}
		this.outputColor=inputColor;
		layout.numColumns = brightnessColors.length;;
		layout.marginWidth = 9;
		layout.marginHeight = 9;
	
		this.setLayout(layout);
	
		GridData gd;
		brightnessColors= getBrightness(leadColor);
		brightnessColorBoxes =new ColorBox[brightnessColors.length];
			
		for(int i=0;i<brightnessColors.length ;i++){
			if(i%2==0){
				Label label1= new Label(this, SWT.NONE);
				label1.setText((i+1)*10+"%");
				gd= new GridData();
				if(i!=8){
					gd.horizontalSpan=2;
				}else{
					gd.horizontalSpan=1;
				}
				label1.setLayoutData(gd);
			}			
		}
		
		for(int i=0;i<brightnessColors.length ;i++){
			
			brightnessColorBoxes[i] = new ColorBox(this, SWT.NONE,
					brightnessColors[i], i);
			
			gd = new GridData();
			gd.verticalIndent=0;
			gd.horizontalIndent=0;				
			gd.heightHint = colorBoxSize;
			gd.widthHint = colorBoxSize;
			gd.horizontalAlignment=SWT.BEGINNING;	
			brightnessColorBoxes[i].setLayoutData(gd);	
			brightnessColorBoxes[i].addMouseListener(new MouseAdapter() {
				public void mouseDown(MouseEvent e) {
					ColorBox colorBox = (ColorBox) e.widget;	
					int percentage = AdjustBrightnessComposite
							.getBrightnessPercentage(leadColor, colorBox
									.getColorBoxColorDescriptor().getRGB());
					brightnessText.setText(Integer.toString(percentage));
				}		
			});

			brightnessColorBoxes[i].getColorBox().addMouseListener(
					new MouseAdapter() {
						public void mouseDown(MouseEvent e) {
							Composite comp=(Composite) e.widget;
							ColorBox colorBox = (ColorBox) comp.getParent();			
							float brightnessRatio = AdjustBrightnessComposite
							.getBrightnessRatio(leadColor, colorBox
									.getColorBoxColorDescriptor().getRGB());
							int percentage = Math.round(brightnessRatio*100f);
							brightnessText.setText(Integer.toString(percentage));
							
							RGB brighter = AdjustBrightnessComposite.getBrighterColor(leadColor, brightnessRatio );
							updatePreview(brighter);

						}	
						
						@Override
						public void mouseDoubleClick(MouseEvent e) {
							Composite comp=(Composite) e.widget;
							ColorBox colorBox = (ColorBox) comp.getParent();			
							float brightnessRatio = AdjustBrightnessComposite
							.getBrightnessRatio(leadColor, colorBox
									.getColorBoxColorDescriptor().getRGB());
							int percentage = Math.round(brightnessRatio*100f);
							brightnessText.setText(Integer.toString(percentage));
							RGB brighter = AdjustBrightnessComposite.getBrighterColor(leadColor, brightnessRatio );
							updatePreview(brighter);
							dialogClose.okCloseDialog();
						}

			});
			
		}
		
		
		brightnessText= new Text(this, SWT.BORDER);
		brightnessText.setTextLimit(3);
		int percentage = AdjustBrightnessComposite
		.getBrightnessPercentage(leadColor, inputColor);
		brightnessText.setText(Integer.toString(percentage));
		gd= new GridData();
		gd.widthHint=2*colorBoxSize-6;
		gd.horizontalSpan=2;
		brightnessText.setLayoutData(gd);
		
		final Slider brightnessSlider= new Slider(this,SWT.HORIZONTAL);
		gd= new GridData();
		gd.horizontalSpan=7;
		gd.widthHint=9*colorBoxSize;
		int range = MAX_VALUE - MIN_VALUE;

		brightnessSlider.setMinimum(0);
		brightnessSlider.setMaximum(range + brightnessSlider.getThumb());
		brightnessSlider.setLayoutData(gd);
			
		brightnessSlider.setSelection(Integer.valueOf(brightnessText.getText()));			
			
			brightnessSlider.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int newPropertyValue = brightnessSlider.getSelection()
							- Math.abs(MIN_VALUE);

					String newSliderValue = new Integer(newPropertyValue)
							.toString();

					if (!newSliderValue.equals(brightnessText.getText())) {
						brightnessText.setText(newSliderValue);
						RGB brighter = AdjustBrightnessComposite.getBrighterColor(leadColor, newPropertyValue/100.0f );
						updatePreview(brighter);
					}
				}

			});

			brightnessText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					String newSliderValue = brightnessText.getText();
					try {
						int newSliderIntValue = Integer.parseInt(newSliderValue);

						// range control
						if (newSliderIntValue < MIN_VALUE) {
							brightnessText.setText(Integer.toString(MIN_VALUE));
						}
						if (newSliderIntValue > MAX_VALUE) {
							brightnessText.setText(Integer.toString(MAX_VALUE));
						}

						if (newSliderIntValue != brightnessSlider.getSelection()) {
							brightnessSlider.setSelection(newSliderIntValue
									+ Math.abs(MIN_VALUE));
							RGB brighter = AdjustBrightnessComposite.getBrighterColor(leadColor, newSliderIntValue/100.0f );
							updatePreview(brighter);
						}
					} catch (Exception ex) {
						brightnessText.setText("0");
					}
				}
			});	
		
		
		ColorBox oldColor= new ColorBox(this,SWT.NONE, new ColorDescriptor(inputColor,""),0);
		gd= new GridData();
		gd.widthHint=4*colorBoxSize;
		gd.heightHint=2*colorBoxSize;
		gd.horizontalAlignment=GridData.END;
		
		gd.horizontalSpan=4;
		oldColor.setLayoutData(gd);
		

		referenceBox= new ColorBox(this,SWT.NONE, new ColorDescriptor(inputColor,""),0);
		gd= new GridData();
		gd.widthHint=4*colorBoxSize;
		gd.heightHint=2*colorBoxSize;
		gd.horizontalSpan=4;
		gd.horizontalIndent=0;
		referenceBox.setLayoutData(gd);
		
		Button okButton= new Button(this, SWT.PUSH);
		okButton.setText("OK");
		okButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent event){
				dialogClose.okCloseDialog();
			}
		});
	}
	
	
	protected void updatePreview(RGB rgb) {
		outputColor=rgb;
		if(colorLabel!=null){
			colorLabel.getColorDescriptor().setRed(outputColor.red);
			colorLabel.getColorDescriptor().setGreen(outputColor.green);
			colorLabel.getColorDescriptor().setBlue(outputColor.blue);
		}
		referenceBox.updateColor(rgb.red, rgb.green, rgb.blue);
		
	}

	private ColorDescriptor[] getBrightness(RGB color){
		ColorDescriptor[] descs= new ColorDescriptor[9];
		float hue = color.getHSB()[0];
		float saturation = color.getHSB()[1];
		float brightness = color.getHSB()[2];
		
		for(int i=0; i<descs.length;i++){
			RGB rgb= new RGB(hue, saturation * (1+i)/10.0f , brightness + (1-brightness)* (9-i)/10.0f);
			descs[i]= new ColorDescriptor(rgb, Float.toString(hue));
		}
		return descs;
	}
	
	private ColorBox[] brightnessColorBoxes;

	private ColorDescriptor[] brightnessColors= new ColorDescriptor[9];
	


	public RGB getInputColor() {
		return inputColor;
	}

	public void setInputColor(RGB inputColor) {
		this.inputColor = inputColor;
	}

	public RGB getOutputColor() {
		return outputColor;
	}

	public void setOutputColor(RGB outputColor) {
		this.outputColor = outputColor;
	}
	
	
	public static float getBrightnessRatio(RGB leadColor, RGB derivedColor){
/*		float leadB = leadColor.getHSB()[2];
		float derB = derivedColor.getHSB()[2];
		return Math.round((1-((derB - leadB)/(1-leadB)))*100f);
*/
    	float leadB = leadColor.getHSB()[1];
		float derB = derivedColor.getHSB()[1];
		if( 0 != leadB)
			return (((derB / leadB)));
		else{
			//use brightness in this case nothing comes from saturation
			leadB = leadColor.getHSB()[2];
			derB = derivedColor.getHSB()[2];
			return (1-((derB - leadB)/(1-leadB)));
		}
		
		
	}

	public static int getBrightnessPercentage(RGB leadColor, RGB derivedColor) {
		return Math.round(getBrightnessRatio(leadColor, derivedColor) * 100f);

	}

	
	public static RGB getBrighterColor( RGB color, float howMuchBrighter ){
		float brightness=color.getHSB()[2] + (1-color.getHSB()[2])* (1-howMuchBrighter);
		float saturation = color.getHSB()[1] * (howMuchBrighter);
		saturation = Math.min(saturation, 1f);
		brightness = Math.min(brightness, 1f);
		brightness = Math.max(brightness, 0);
		return new RGB(color.getHSB()[0],saturation, brightness);
	}

}
