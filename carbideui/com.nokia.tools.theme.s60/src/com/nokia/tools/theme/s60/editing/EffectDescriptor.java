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
package com.nokia.tools.theme.s60.editing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.nokia.tools.media.utils.layers.EffectTypes;
import com.nokia.tools.media.utils.layers.IEffectDescriptor;
import com.nokia.tools.media.utils.layers.IEffectParameterDescriptor;
import com.nokia.tools.platform.theme.EffectConstants;
import com.nokia.tools.theme.s60.effects.EffectObject;


/**
 * Metadata about EffectObject. Parsed from effect object properties.
 *
 *
 */
public class EffectDescriptor implements IEffectDescriptor {

	private EffectObject effect;
	
	private String name;
	
	private String className;
	
	private String uid;
	
	private boolean input;
	
	private int type;
	
	ArrayList<IEffectParameterDescriptor> parameters = new ArrayList<IEffectParameterDescriptor>();
	
	public EffectDescriptor(EffectObject effect) {
		this.effect = effect;		
		Map attr = effect.getAttributeMap();
		name = (String) attr.get(EffectConstants.ATTR_NAME);		
		className = (String) attr.get("className");
		uid = (String) attr.get(EffectConstants.ATTR_UID);
		input = Boolean.valueOf((String)attr.get(EffectConstants.INPUT)).booleanValue();
		type = Integer.parseInt((String) attr.get(EffectConstants.ATTR_TYPE));
		//parse params info
		ArrayList uiParams = (ArrayList) attr.get(EffectConstants.UI_PARAMS);
		for (int i=0;i<uiParams.size();i++) {
			HashMap param = (HashMap) uiParams.get(i);
			EffectParameterDescriptor x = parseParam(param);
			parameters.add(x);
		}		
	}

	private EffectParameterDescriptor parseParam(HashMap params) {
		EffectParameterDescriptor param = new EffectParameterDescriptor();
		param.setCaption((String) params.get(EffectConstants.ATTR_CAPTION));
		param.setDefaultVal((String) params.get(EffectConstants.ATTR_DEFAULTVALUE));
		param.setMaxVal((String) params.get(EffectConstants.ATTR_MAXVAL));
		param.setMinVal((String) params.get(EffectConstants.ATTR_MINVAL));
		param.setUiType((String) params.get(EffectConstants.ATTR_UITYPE));
		param.setUiName((String) params.get(EffectConstants.ATTR_UINAME));			
		try {
			String options = (String) params.get(EffectConstants.ATTR_OPTIONS);
			if (options != null) {
				StringTokenizer stok = new StringTokenizer(options, ",");
				ArrayList<String> opt=  new ArrayList<String>();
				while(stok.hasMoreTokens()) {
					opt.add(stok.nextToken());
				}
				param.setOptions((String[])opt.toArray(new String[opt.size()]));				
				param.setPos(Integer.parseInt((String) params.get("pos")));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return param;
	}

	public String getClassName() {
		return className;
	}

	public EffectObject getEffectObject() {
		return effect;
	}

	public boolean isInput() {
		return input;
	}

	public String getName() {
		return name;
	}

	public List<IEffectParameterDescriptor> getParameters() {
		return (List<IEffectParameterDescriptor>) parameters.clone();
	}
	
	public IEffectParameterDescriptor getParameterDescriptor(String paramName) {
		for (IEffectParameterDescriptor ep: parameters) {
			if (ep.getUiName().equals(paramName))
				return ep;
		}
		return null;
	}
	
	public IEffectParameterDescriptor getParameterDescriptor(int pos) {
		return parameters.get(pos);
	}
	
	public int getParameterLiteralValueNumber(String paramName, String literalValue) {
		EffectParameterDescriptor epm = (EffectParameterDescriptor) getParameterDescriptor(paramName);
		return epm.getLiteralValueNumber(literalValue);			
	}
	
	public String getParameterLiteralValue(String paramName, int number) {
		EffectParameterDescriptor epm = (EffectParameterDescriptor) getParameterDescriptor(paramName);
		if (epm != null) {
			return epm.getLiteralValue(number);
		}
		return null;
	}

	/**
	 * 0 = onLayer
	 * 1 = betweenLayerEffects 
	 * @return
	 */
	public EffectTypes getType() {
		return EffectTypes.values()[type];
	}

	public String getUid() {
		return uid;
	}
		
}
