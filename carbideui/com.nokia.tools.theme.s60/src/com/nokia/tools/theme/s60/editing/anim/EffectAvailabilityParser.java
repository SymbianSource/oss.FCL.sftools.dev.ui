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
package com.nokia.tools.theme.s60.editing.anim;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.nokia.tools.media.utils.layers.TimingModel;
import com.nokia.tools.platform.theme.SkinnableEntity;
import com.nokia.tools.platform.theme.ThemeTag;
import com.nokia.tools.theme.s60.S60ThemePlugin;

public class EffectAvailabilityParser {

	public static final String WILDCARD_CHAR = "*";

	public static final String GLOBAL_ENTITY_ID = "*";

	public static final String ELEMENT_GROUP_DEF = "group-def";

	public static final String ELEMENT_ENTITY_DEF = "entity-def";

	public static final String ELEMENT_EFFECT = "effect";

	public static final String ELEMENT_ENABLE = "enable";

	public static final String ELEMENT_DISABLE = "disable";

	public static final String ATTRIBUTE_ID = "id";

	public static final String ATTRIBUTE_GROUP = "group";

	public static final String ATTRIBUTE_CAN_BE_ANIMATED = "canBeAnimated";

	public static final String ELEMENT_TimingConstraints = "timingConstraints";

	public static final String ELEMENT_TimingDef = "entity-timing-def";

	public static final String ATTRIBUTE_TimingModel = "timing";

	public static final String ELEMENT_LayerConstraints = "layerConstraints";

	public static final String ELEMENT_EntityType = "entity-type";

	public static final String ELEMENT_Constraint = "constraint";

	public static final String ATTRIBUTE_Name = "name";

	public static final String ATTRIBUTE_Type = "type";

	public static final String ATTRIBUTE_Value = "value";

	public static final EffectAvailabilityParser INSTANCE = new EffectAvailabilityParser();

	protected Map<String, GroupDefinition> groupDefs = new HashMap<String, GroupDefinition>();

	protected Map<String, EntityDefinition> entityDefs = new HashMap<String, EntityDefinition>();

	// cannot be map because we could want to store elementId1=RealTime and also
	// elementId1=Relative at the same time
	protected List<Map.Entry<String, String>> entityTimingDefs = new ArrayList<Map.Entry<String, String>>();

	protected List<LayerConstraints> layerConstraints = new ArrayList<LayerConstraints>();

	protected EffectAvailabilityParser() {
		try {
			initialize(getDefinitionFile());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected File getDefinitionFile() {
		String filePath = "data/config/availableEffects.xml";

		URL fullPathString = Platform.find(S60ThemePlugin.getDefault()
				.getBundle(), new Path(filePath));

		if (fullPathString == null) {
			try {
				fullPathString = new URL(filePath);
			} catch (MalformedURLException e) {
				return null;
			}
		}

		if (fullPathString == null)
			return null;

		File file = new File(fullPathString.getFile());
		if (!file.exists()) {
			try {
				fullPathString = Platform.asLocalURL(fullPathString);
				file = new File(fullPathString.getFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file.exists() ? file : null;
	}

	public void initialize(File file) throws SAXException, IOException,
			ParserConfigurationException {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		EffectAvailabilityParserHandler handler = new EffectAvailabilityParserHandler();
		parser.parse(file, handler);
	}

	public List<String> getAvailableEffects(String entityId,
			IEffectMatcher matcher) {
		EntityDefinition entityDef = entityDefs.get(entityId);

		if (entityDef == null) {
			entityDef = entityDefs.get(GLOBAL_ENTITY_ID);
		}

		return entityDef != null ? entityDef.getAvailableEffects(matcher)
				: new ArrayList<String>();
	}

	public boolean supportsAnimationTiming(SkinnableEntity entity, TimingModel timingModel) {
		
		String entityType = entity.isEntityType();
		if (ThemeTag.ELEMENT_MORPHING.equalsIgnoreCase(entityType)) {
			return TimingModel.Relative == timingModel;
		} else if (ThemeTag.ELEMENT_FASTANIMATION.equalsIgnoreCase(entityType)) {
			return TimingModel.RealTime == timingModel;
		}
		return false;
		
	}

	public boolean canBeAnimated(SkinnableEntity entity) {
		EntityDefinition entityDef = entityDefs.get(entity.getId());
		if (entityDef == null) {
			entityDef = entityDefs.get(GLOBAL_ENTITY_ID);
		}
		
		if (entityDef.canBeAnimated()) {
			//check also timing models
			return supportsAnimationTiming(entity, TimingModel.RealTime) ||
				supportsAnimationTiming(entity, TimingModel.Relative);
		} else
			return false;

	}

	class EffectAvailabilityParserHandler extends DefaultHandler {

		GroupDefinition lastGroupDef;

		EntityDefinition lastEntityDef;

		boolean enable, disable;

		boolean inTimingConstraintsNode;

		boolean inLayerConstraintsNode;

		LayerConstraints lastLayerConstraints;

		@Override
		public void startDocument() throws SAXException {
			lastGroupDef = null;
			lastEntityDef = null;
			lastLayerConstraints = null;
		}

		@Override
		public void endDocument() throws SAXException {
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (ELEMENT_GROUP_DEF.equalsIgnoreCase(qName)) {
				lastGroupDef = new GroupDefinition();
				lastGroupDef.id = attributes.getValue(ATTRIBUTE_ID);
				if (attributes.getValue(ATTRIBUTE_CAN_BE_ANIMATED) != null) {
					lastGroupDef.canBeAnimated = Boolean
							.parseBoolean(attributes
									.getValue(ATTRIBUTE_CAN_BE_ANIMATED));
				}
			}
			if (ELEMENT_ENTITY_DEF.equalsIgnoreCase(qName)) {
				lastEntityDef = new EntityDefinition();
				lastEntityDef.id = attributes.getValue(ATTRIBUTE_ID);
				lastEntityDef.groupId = attributes.getValue(ATTRIBUTE_GROUP);
				if (attributes.getValue(ATTRIBUTE_CAN_BE_ANIMATED) != null) {
					lastEntityDef.canBeAnimated = Boolean
							.parseBoolean(attributes
									.getValue(ATTRIBUTE_CAN_BE_ANIMATED));
				}
			}
			if (ELEMENT_ENABLE.equalsIgnoreCase(qName)) {
				enable = true;
			}
			if (ELEMENT_DISABLE.equalsIgnoreCase(qName)) {
				disable = true;
			}
			if (ELEMENT_EFFECT.equalsIgnoreCase(qName)) {
				if (lastGroupDef != null) {
					EffectDefinition effectDef = new EffectDefinition();
					effectDef.id = attributes.getValue(ATTRIBUTE_ID);
					for (int i = 0; i < attributes.getLength(); i++) {
						effectDef.params.put(attributes.getQName(i), attributes
								.getValue(i));
					}
					lastGroupDef.effects.add(effectDef);
				}
				if (lastEntityDef != null) {
					if (enable) {
						EffectDefinition effectDef = new EffectDefinition();
						effectDef.id = attributes.getValue(ATTRIBUTE_ID);
						for (int i = 0; i < attributes.getLength(); i++) {
							effectDef.params.put(attributes.getQName(i),
									attributes.getValue(i));
						}
						lastEntityDef.enabledEffects.add(effectDef);
					}
					if (disable) {
						lastEntityDef.disabledEffects.add(attributes
								.getValue(ATTRIBUTE_ID));
					}
				}
			}
			if (ELEMENT_TimingDef.equalsIgnoreCase(qName)
					&& inTimingConstraintsNode) {
				final String elementId = attributes.getValue(ATTRIBUTE_ID);
				final String timing = attributes
						.getValue(ATTRIBUTE_TimingModel);
				entityTimingDefs.add(new Map.Entry<String, String>() {
					public String getKey() {
						return elementId;
					}

					public String getValue() {
						return timing;
					}

					public String setValue(String value) {
						throw new RuntimeException();
					}
				});
			}
			if (ELEMENT_TimingConstraints.equalsIgnoreCase(qName)) {
				inTimingConstraintsNode = true;
			}

			if (ELEMENT_EntityType.equalsIgnoreCase(qName)
					&& inLayerConstraintsNode) {
				lastLayerConstraints = new LayerConstraints();
				lastLayerConstraints.entityId = attributes
						.getValue(ATTRIBUTE_ID);
				lastLayerConstraints.type = attributes.getValue(ATTRIBUTE_Type);
				layerConstraints.add(lastLayerConstraints);
			}
			if (ELEMENT_Constraint.equalsIgnoreCase(qName)
					&& inLayerConstraintsNode && lastLayerConstraints != null) {
				String name = attributes.getValue(ATTRIBUTE_Name);
				String value = attributes.getValue(ATTRIBUTE_Value);
				lastLayerConstraints.constraints.put(name, value);
			}
			if (ELEMENT_LayerConstraints.equalsIgnoreCase(qName)) {
				inLayerConstraintsNode = true;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (ELEMENT_GROUP_DEF.equalsIgnoreCase(qName)) {
				groupDefs.put(lastGroupDef.id, lastGroupDef);
				lastGroupDef = null;
			}
			if (ELEMENT_ENABLE.equalsIgnoreCase(qName)) {
				enable = false;
			}
			if (ELEMENT_DISABLE.equalsIgnoreCase(qName)) {
				disable = false;
			}
			if (ELEMENT_ENTITY_DEF.equalsIgnoreCase(qName)) {
				entityDefs.put(lastEntityDef.id, lastEntityDef);
				lastEntityDef = null;
			}
			if (ELEMENT_TimingConstraints.equalsIgnoreCase(qName)) {
				inTimingConstraintsNode = false;
			}
			if (ELEMENT_EntityType.equalsIgnoreCase(qName)) {
				lastLayerConstraints = null;
			}
			if (ELEMENT_LayerConstraints.equalsIgnoreCase(qName)) {
				inLayerConstraintsNode = false;
			}
		}
	}

	class GroupDefinition {
		String id;

		Boolean canBeAnimated;

		Set<EffectDefinition> effects = new HashSet<EffectDefinition>();

		public List<String> getAvailableEffects(IEffectMatcher matcher) {
			List<String> toRet = new ArrayList<String>();
			for (EffectDefinition effect : effects) {
				if (matcher != null) {
					if (matcher.match(effect.id, effect.params)) {
						toRet.add(effect.id);
					}
				} else {
					toRet.add(effect.id);
				}
			}
			return toRet;
		}

		public boolean canBeAnimated() {
			if (canBeAnimated != null) {
				return canBeAnimated;
			}

			return true;
		}
	}

	public class LayerConstraints {

		String entityId;

		String type;

		Map<String, String> constraints = new HashMap<String, String>();

		public Map<String, String> getConstraints() {
			return constraints;
		}

		public int getMaxLayerCount() {
			String value = constraints.get("max-layers-count");
			if (value != null) {
				return Integer.parseInt(value);
			} else {
				return Integer.MAX_VALUE;
			}
		}

		@Override
		public int hashCode() {
			return (entityId + ";" + type).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LayerConstraints) {
				LayerConstraints cons = (LayerConstraints) obj;
				return (entityId + ";" + type).equals(cons.entityId + ";"
						+ cons.type);

			}

			if (obj instanceof String) {
				return (entityId + ";" + type).equals(obj);
			}

			return super.equals(obj);
		}
	}

	public interface IEffectMatcher {
		boolean match(String effect, Map properties);
	}

	class EffectDefinition {
		String id;

		Map<String, String> params = new HashMap<String, String>();

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof EffectDefinition) {
				return ((EffectDefinition) obj).id.equals(id);
			}

			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			if (id != null) {
				return id.hashCode();
			}

			return super.hashCode();
		}
	}

	class EntityDefinition {
		String id;

		String groupId;

		Boolean canBeAnimated;

		Set<EffectDefinition> enabledEffects = new HashSet<EffectDefinition>();

		List<String> disabledEffects = new ArrayList<String>();

		public List<String> getAvailableEffects(IEffectMatcher matcher) {
			List<String> toRet = new ArrayList<String>();
			if (groupId != null) {
				GroupDefinition groupDef = groupDefs.get(groupId);
				if (groupDef != null) {
					toRet.addAll(groupDef.getAvailableEffects(matcher));
				}
			}
			for (EffectDefinition effect : enabledEffects) {
				if (toRet.contains(effect)) {
					toRet.remove(effect);
				}

				if (matcher != null) {
					if (matcher.match(effect.id, effect.params)) {
						toRet.add(effect.id);
					}
				} else {
					toRet.add(effect.id);
				}
			}

			for (String effect : disabledEffects) {
				toRet.remove(effect);
			}

			return toRet;
		}

		public boolean canBeAnimated() {
			if (canBeAnimated != null) {
				return canBeAnimated;
			}

			if (groupId != null) {
				GroupDefinition groupDef = groupDefs.get(groupId);
				if (groupDef != null) {
					return groupDef.canBeAnimated();
				}
			}

			return true;
		}
	}

	public LayerConstraints getLayerConstraintsForEntityType(String entityType) {
		return getLayerConstraints(null, entityType);
	}

	public LayerConstraints getLayerConstraintsForEntity(String entityId) {
		return getLayerConstraints(entityId, null);
	}

	protected LayerConstraints getLayerConstraints(String entityId,
			String entityType) {
		String key = entityId + ";" + entityType;
		for (LayerConstraints layerContraint : layerConstraints) {
			if (layerContraint.equals(key)) {
				return layerContraint;
			}
		}
		return null;
	}

}
