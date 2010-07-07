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

package com.nokia.tools.media.utils.timeline;


public interface ITimeLineRow {

	public ITimeLineNode[] getNodes();

	public void setNodes(ITimeLineNode[] nodes);

	public void addNode(ITimeLineNode node);

	public void removeNode(ITimeLineNode node);

	public void removeAllNodes();

	public String getLabel();

	public void setLabel(String label);

	public boolean isVisible();

	public void setVisible(boolean visible);

	public void addTimeListener(ITimeListener listener);

	public void removeTimeListener(ITimeListener listener);

	public void addDoubleClickListener(ITimeLineDoubleClickListener listener);

	public void removeDoubleClickListener(ITimeLineDoubleClickListener listener);
	
	public Object getSource();

	public void setSource(Object source);
	
	public ITimeLine getTimeLine();

}
