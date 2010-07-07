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
 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

  
/**
 * The class defines the details of the image associated with the skinnable element/part
 *
 */
public class LayerEffect implements Cloneable {
    
    private Map<Object, Object> attributes;
	protected List<Object> pmList;
    private boolean isSelected=true;
	private ThemeGraphic tg = null;
    
    /**
     * Constructor
     */
    public LayerEffect (ThemeGraphic tg) {
        attributes = new HashMap<Object, Object>();
		pmList = new ArrayList<Object>();
		this.tg = tg;
		//parameterModel = new ArrayList();
    }
    
	public ThemeGraphic getThemeGraphic () {
        return this.tg;      
    }

    /**
     * Method to set the given attribute
     * @param name The name of the attribute
     * @param value The value of the attribute
     */
    public void setAttribute (String name, String value) {
        synchronized (this) {
			if(name.equalsIgnoreCase(ThemeTag.ATTR_NAME)) {
				attributes.put(name, value);
			}	
    	}
    }
    
    public String toString() {
    	return this.attributes.toString();
    }
    
    /**
     * Method to get the attribute list for a node
     */
    public synchronized Map<Object, Object> getAttributes () {
    	//mirror parameters froms PM list - needed ahen element was edited by EditableEntityImage
    	if (pmList != null) {
    		for (Object p:pmList) {
    			ParameterModel model = (ParameterModel) p;
    			attributes.put(model.getAttribute(ThemeTag.ATTR_NAME),model);
    		}
    	}
        return attributes;
    }
	
	public String getEffetName() {
		return this.attributes.get(ThemeTag.ATTR_NAME).toString();
	}
	
	public void setEffectName(String name) {
		this.attributes.put(ThemeTag.ATTR_NAME,name);
	}

	
    /**
     * Method to get the value of an attribute.
     * @param attrName The name of the attribute whose value is required.
     * @return A string containing the value of the attrName attribute. If the attribute is not found then it returns a null
     */
    public String getAttribute (String attrName) {
		if(attrName.equalsIgnoreCase(ThemeTag.ATTR_NAME)) {
            return (String) attributes.get(attrName);
		}else
        {
            ParameterModel param=getParameterModel(attrName);
			if(param!=null)
	            return param.getValue(attrName);
			else
				return null;

        }
    }
    
    public void setParameterModel(ParameterModel pm) {
		if(pm == null)
			return;
		else {
			pmList.add(pm);  
        }
    }
    
	public List<Object> getParameterModels () throws ThemeException {
        return this.pmList;
    }
	
    public ParameterModel getParameterModel(String name)
    {
    	for(int i=0;i<pmList.size();i++)
    	{
    		ParameterModel pm=(ParameterModel)pmList.get(i);
    		if(pm.getAttribute(ThemeTag.ATTR_NAME).equalsIgnoreCase(name))
    			return pm;
    	}
		return null;
    }	
		
    /**
     * Method to compare two objects
     * @param obj Object to be compared 
     * @return boolean true if both are equal else false
     */
    
    public boolean equals(Object obj) {
    	
    	if(!isSame(this.getEffetName(),((LayerEffect)obj).getEffetName())) {
    		return false;
    	}	
   		return true;
     }
     
    
    /**
     * Method to compare two Strings
     * @return boolean true if both are same else false
     */
    protected boolean isSame(String value1,String value2) {
    	
    	if((value1 == null && value2 !=null) || (value1 != null && value2 == null)) {
    		return false;
    	}
    	else if(value1 == null && value2 == null) {
    		return true;
    	}
    	else {
    		if(value1.equalsIgnoreCase(value2))
    			return true;
    	}
    	
    	return false;
    }	
    		
    public void clearLayerEffect() {
		if(this.attributes != null) {
			attributes.clear();
		}		
	}
     
     
     /**
     * Method to clone the LayerEffect object
     * @return Object object of the clone SkinImage
     */
    public Object clone1 (ThemeGraphic obj1) throws CloneNotSupportedException {
		
        LayerEffect obj = null;		
        obj = (LayerEffect)super.clone();
        obj.tg = obj1;
		 // clone the attributes Map
        HashMap m = new HashMap<Object, Object>(this.attributes);
        m = (HashMap) m.clone();
		obj.attributes = (Map) m;
		
		List<Object> les = this.pmList;
	    ArrayList<Object> al = new ArrayList<Object>(les);
	    al = (ArrayList) al.clone();
	    for (int j = 0; j < al.size(); j++) {
	    	ParameterModel pm = (ParameterModel) al.get(j); //(ThemeGraphic) al.get(j);
	    	pm = (ParameterModel) pm.clone1(obj1);		
	 	    al.set(j, pm);
        }
        obj.pmList = (List<Object>)al;  
        return (Object)obj;
    }
	/**
	 * @return Returns the isSelected.
	 */
	public boolean isSelected() {
		return isSelected;
	}
	/**
	 * @param isSelected The isSelected to set.
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected; 
	}
    
 


}
