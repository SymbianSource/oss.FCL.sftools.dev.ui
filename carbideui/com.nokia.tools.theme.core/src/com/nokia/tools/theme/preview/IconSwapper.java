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
package com.nokia.tools.theme.preview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;

import com.nokia.tools.editing.core.EditingUtil;
import com.nokia.tools.editing.core.TypedAdapter;
import com.nokia.tools.editing.model.ModelPackage;
import com.nokia.tools.platform.layout.ComponentInfo;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.Theme;
import com.nokia.tools.platform.theme.ThemeBasicData;
import com.nokia.tools.platform.theme.ThemeConstants;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.platform.theme.preview.PreviewElement;
import com.nokia.tools.platform.theme.preview.PreviewTagConstants;
import com.nokia.tools.theme.content.ThemeData;
import com.nokia.tools.theme.content.ThemeScreenData;
import com.nokia.tools.theme.content.ThemeScreenElementData;

public class IconSwapper implements IEntityPreviewer {
	// some simple mechanism not to swap everytime the same
	private static int lastSwapped = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.nokia.tools.theme.preview.IEntityPreviewer#preview(com.nokia.tools.theme.content.ThemeScreenData,
	 *      com.nokia.tools.theme.content.ThemeData)
	 */
	public ThemeScreenData preview(ThemeScreenData preview, ThemeData data) {
		SkinnableEntity entity = (SkinnableEntity) data.getData();
		if (!isIcon(entity)) {
			return null;
		}

		List<ThemeScreenElementData> candidates = new ArrayList<ThemeScreenElementData>();
		List<ThemeScreenElementData> allElements = preview.findAllElements();
		for (ThemeScreenElementData elementData : allElements) {
			PreviewElement element = (PreviewElement) elementData.getData();
			if (isSwapCandidate(entity, element)) {
				candidates.add(elementData);
			}
		}
		if (candidates.isEmpty()) {
			return null;
		}
		if (lastSwapped >= candidates.size()) {
			lastSwapped = 0;
		}
		final ThemeScreenElementData iconCandidate = candidates
				.get(lastSwapped);
		ThemeScreenElementData textCandidate = null;
		int index = allElements.indexOf(iconCandidate);
		if (++index < allElements.size()) {
			ThemeScreenElementData textData = allElements.get(index);
			PreviewElement text = (PreviewElement) textData.getData();
			if (ThemeConstants.ELEMENT_TYPE_TEXT == text
					.getPreviewElementType()) {
				textCandidate = textData;
			}
		}

		final String originalId = iconCandidate.getSkinnableEntity()
				.getIdentifier();
		EditingUtil.setFeatureValue(iconCandidate.getResource(), "elementId",
				entity.getIdentifier());

		final String originalText = textCandidate == null ? null
				: getText(textCandidate.getData());
		if (textCandidate != null) {
			String text = getText(entity);
			EditingUtil.setFeatureValue(textCandidate.getResource(), "text",
					text);
		}
		final ThemeScreenElementData textDataToSwap = textCandidate;
		// listens for the data changes, if the swapped object has been removed,
		// restore the original value
		Adapter adapter = new TypedAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
			 */
			public void notifyChanged(Notification notification) {
				if (((EObject) notification.getNotifier())
						.eClass()
						.getEStructuralFeature(ModelPackage.EDIT_OBJECT__PARENT) == notification
						.getFeature()
						&& notification.getNewValue() == null) {
					EditingUtil.setFeatureValue(iconCandidate.getResource(),
							"elementId", originalId);
					if (textDataToSwap != null) {
						EditingUtil.setFeatureValue(textDataToSwap
								.getResource(), "text", originalText);
					}
				}

			}
		};
		iconCandidate.getResource().eAdapters().add(adapter);

		lastSwapped++;
		return preview;
	}

	private String getText(ThemeBasicData data) {
		if (data instanceof PreviewElement) {
			return ((PreviewElement) data).getText();
		}
		if (data instanceof SkinnableEntity) {
			String text = null;
			SkinnableEntity entity = (SkinnableEntity) data;
			text = entity
					.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_TEXT);
			if (null == text) {
				text = entity
						.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_NAME);
			}
			return text;
		}
		return null;
	}

	protected boolean isSwapCandidate(SkinnableEntity elementToShow,
			PreviewElement swap) {
		if (elementToShow == null || elementToShow.getComponentInfo() == null) {
			return false;
		}
		String swapSkinID = swap
				.getAttributeValue(PreviewTagConstants.ATTR_ELEMENT_ID);
		if (null == swapSkinID) {
			return false;
		}
		if (!swap.supportsDisplay(elementToShow.getRoot().getDisplay())) {
			// hit a candidate that doesn't support the current display
			// this can happen when an element is only visible in a bigger
			// display
			return false;
		}
		SkinnableEntity swapEntity = ((Theme) swap.getRoot())
				.getSkinnableEntity(swapSkinID);
		if (swapEntity == null) {
			return false;
		}
		ComponentInfo swapCandidateInfo = swapEntity.getComponentInfo();
		if (swapCandidateInfo == null || swapCandidateInfo.getName() == null) {
			return false;
		}

		ComponentInfo info = elementToShow.getComponentInfo();
		if (swapCandidateInfo.getName().equals(info.getName())) {

			return Boolean.valueOf(
					elementToShow.getAttributeValue(ThemeTag.ATTR_ANIMATE))
					.equals(
							Boolean.valueOf(swapEntity
									.getAttributeValue(ThemeTag.ATTR_ANIMATE)));
		}
		return false;
	}

	private boolean isIcon(SkinnableEntity element) {
		if (element == null) {
			return false;
		}
		return ThemeTag.PREVIEW_HINT_SWAP.equalsIgnoreCase(element
				.getPreviewHint());
	}
}
