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
package com.nokia.tools.media.utils.svg2svgt;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nokia.svg2svgt.SVG2SVGTConstants;
import com.nokia.svg2svgt.ServiceRegistry;
import com.nokia.svg2svgt.converter.ConversionConstants;
import com.nokia.svg2svgt.converter.Conversions;
import com.nokia.svg2svgt.converter.NameSpaceAnalyzer;
import com.nokia.svg2svgt.log.Logger;



/**
 * Converts the "opacity" attribute into a pair of "full-opacity" and
 * "stroke-opacity" attributes.
 */
public class OpacityConverter implements Conversions, ConversionConstants
{
   /**
    * Logger instance.
    */
   private Logger myLogger = null;


// IMPLEMENTATION METHOD

    /**
     * Converts the "opacity" attribute into a pair of "full-opacity" and
     * "stroke-opacity" attributes.
     * 
     * @param svgNode	Node for which the conversion is desired.
     * @param svgDoc	SVG Document containing this node.
     * @param svgtNode	SVGT element node.
     * @param svgtDoc	SVGT document reference.
     */
   public void doConversion( Node svgNode, Document svgDoc, Node svgtNode, Document svgtDoc, Logger logger, String nsURL  ) throws DOMException
   {
        myLogger = logger;
        if ( Node.ATTRIBUTE_NODE != svgNode.getNodeType() )
        {
        	
        	return;
        }
        else
        {
			if ( Node.ELEMENT_NODE != svgtNode.getNodeType() )
			{
				return;
			}
			Element svgtParentElement = ( Element ) svgtNode;
			String elemName = svgNode.getNodeName();
			String nsName = null;
			int index = elemName.indexOf( ":" );
			if ( -1 != index )
			{
				nsName = elemName.substring( 0, index -1 );
				elemName = elemName.substring( index + 1, elemName.length() );
			}
        	// get the value of the attribute
        	String attrValue = svgNode.getNodeValue();
        	Attr fillOpacity = null;
			Attr strokeOpacity = null;
        	if ( null != nsName )
        	{
				fillOpacity = svgtDoc.createAttribute( nsName + ":" + FILL_OPACITY_ATTRIBUTE );
				strokeOpacity = svgtDoc.createAttribute( nsName + ":" + STROKE_OPACITY_ATTRIBUTE );
        	}
        	else
        	{
				fillOpacity = svgtDoc.createAttribute( FILL_OPACITY_ATTRIBUTE );
				strokeOpacity = svgtDoc.createAttribute( STROKE_OPACITY_ATTRIBUTE );
        	}
        	
        	// don't override if exists and the value is smaller than the master opacity value
        	Element svgElement = ((Attr) svgNode).getOwnerElement();
        	
        	double masterValue = 0, fillValue = 0, strokeValue = 0;
        	try {
        		masterValue = Double.parseDouble(attrValue);
        	} catch(Exception e) {}
        	try {
        		fillValue = Double.parseDouble(svgElement.getAttribute(FILL_OPACITY_ATTRIBUTE));
        	} catch(Exception e) {}
        	try {
        		strokeValue = Double.parseDouble(svgElement.getAttribute(STROKE_OPACITY_ATTRIBUTE));
        	} catch(Exception e) {}
        	
        	if (svgElement.hasAttribute(FILL_OPACITY_ATTRIBUTE) && fillValue < masterValue) {
        		fillOpacity.setNodeValue(svgElement.getAttribute(FILL_OPACITY_ATTRIBUTE));
        	} else {
        		fillOpacity.setNodeValue( attrValue );
        	}
			if ( true == retainNode( nsName, nsURL, FILL_OPACITY_ATTRIBUTE ) )
			{
				svgtParentElement.setAttributeNode( fillOpacity );
			}
			
			if (svgElement.hasAttribute(STROKE_OPACITY_ATTRIBUTE) && strokeValue < masterValue) {
				strokeOpacity.setNodeValue(svgElement.getAttribute(STROKE_OPACITY_ATTRIBUTE));
			} else {
				strokeOpacity.setNodeValue( attrValue );
			}
			if ( true == retainNode( nsName, nsURL, STROKE_OPACITY_ATTRIBUTE ) )
			{
				svgtParentElement.setAttributeNode( strokeOpacity );
			}
        }
   }


// PRIVATE METHODS

   /**
    * Checks if the converted not has to be retained in the output SVGT
    * document or not.
    * @param node	Node to be checked.	
    * @return		True if node is to be retained, else false.
    */
   private boolean retainNode( String nsName, String nsURL, String nodeName )
   {
   	   if ( true == isTagAllowed( nsName, nsURL, nodeName ) )
   	   {
   	   	   //logEvent( SVG2SVGTConstants.BLACK_TAG_REMOVED, 
   	   	   //          new String[]{ nodeName } );
		   isWarningRequired( nsName, nsURL, nodeName );
   	   	   return true;
   	   }
   	   
   	   return true;
   }


   /**
    * Checks if this node is present in black list or not.
    * @param node	Node to be searched.
    * @return		True if found, else false.
    */
   private boolean isTagAllowed( String nsName, String nsURL, String nodeName )
   {
	   NameSpaceAnalyzer nameSpaceA = ( NameSpaceAnalyzer ) ServiceRegistry.getService("com.nokia.svg2svgt.converter.NameSpaceAnalyzer");
	   if ( null == nameSpaceA )
	   {
	   	   return false;
	   }
	   
	   return nameSpaceA.isNodeAllowed( nsName, nsURL, nodeName ,Node.ATTRIBUTE_NODE );
   }


   /**
    * Checks if this node is present in grey list or not.
    * @param node	Node to be searched.
    * @return		True if found, else false.
    */

   private boolean isWarningRequired( String nsName, String nsURL, String nodeName )
   {
	  NameSpaceAnalyzer nameSpaceA = ( NameSpaceAnalyzer ) ServiceRegistry.getService("com.nokia.svg2svgt.converter.NameSpaceAnalyzer");
	  if ( null == nameSpaceA )
	  {
	  	   return false;
	  }
	  if ( true == nameSpaceA.isWarningTag( nsName, nsURL , nodeName, Node.ATTRIBUTE_NODE ) )
	  {
	  	  logWarning( SVG2SVGTConstants.GREY_TAG_FOUND, 
	  	              new String[]{ nodeName } );
	  }
	  return true;
   }



   /**
	* Logs an log event.
	* @param errorCode 	Message code for the log message.
	* @param params		Parameters for the message.
	*/
   private void logEvent( long msgCode, Object[] params )
   {
   	  if ( null != myLogger )
   	  {
   	  	 myLogger.logEvent( msgCode, params );
   	  }
   }


   /**
	* Logs a warning.
	* @param msgCode	Message code for the log message.
	* @param params		Parameters for the message.	
	*/
   private void logWarning( long msgCode, Object[] params )
   {
	  if ( null != myLogger )
	  {
	     myLogger.logEvent( msgCode, params );
	  }
   }

}
