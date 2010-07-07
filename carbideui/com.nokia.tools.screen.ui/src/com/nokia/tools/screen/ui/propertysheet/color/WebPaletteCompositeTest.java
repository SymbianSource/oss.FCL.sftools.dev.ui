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
package com.nokia.tools.screen.ui.propertysheet.color;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class WebPaletteCompositeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
			Display display= new Display();
			final Shell shell = new Shell(display);
			shell.setText("Color Picker");		
			shell.setBounds(100,100,320,410);
					
			GridLayout layout= new GridLayout();
			layout.numColumns=2;
			shell.setLayout(layout);
			
			Button x= new Button(shell,SWT.PUSH);
			x.setText("shaa");
			
			
			
			x.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e)
				{
			    String rgbString="#fef";
				CssColorDialog dialog =	new CssColorDialog(shell);
				dialog.setRGBString(rgbString);				
				dialog.open();
				rgbString=dialog.getRGBString();
				System.out.println(rgbString);
				}
				public void widgetDefaultSelected(SelectionEvent e){}
				});
			
			shell.open();
			while(!shell.isDisposed()){
				if(!display.readAndDispatch()) display.sleep();
			}
			display.dispose();
	}

}
