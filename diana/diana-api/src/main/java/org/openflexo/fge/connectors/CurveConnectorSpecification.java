/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2012-2013 Openflexo
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.fge.connectors;

import org.openflexo.fge.GRProperty;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;

@ModelEntity
@XMLElement(xmlTag = "ArcConnector")
public interface CurveConnectorSpecification extends ConnectorSpecification {

	// Property keys

	@PropertyIdentifier(type = FGEPoint.class)
	public static final String CP_POSITION_KEY = "cpPosition";
	@PropertyIdentifier(type = FGEPoint.class)
	public static final String CP1_RELATIVE_TO_START_OBJECT_KEY = "cp1RelativeToStartObject";
	@PropertyIdentifier(type = FGEPoint.class)
	public static final String CP2_RELATIVE_TO_END_OBJECT_KEY = "cp2RelativeToEndObject";
	@PropertyIdentifier(type = Boolean.class)
	public static final String ARE_BOUNDS_ADJUSTABLE_KEY = "areBoundsAdjustable";

	/*public static enum CurveConnectorParameters implements GRProperty {
		cpPosition, cp1RelativeToStartObject, cp2RelativeToEndObject, areBoundsAdjustable;
	}*/

	public static GRProperty<FGEPoint> CP_POSITION = GRProperty.getGRParameter(CurveConnectorSpecification.class, CP_POSITION_KEY,
			FGEPoint.class);
	public static GRProperty<FGEPoint> CP1_RELATIVE_TO_START_OBJECT = GRProperty.getGRParameter(CurveConnectorSpecification.class,
			CP1_RELATIVE_TO_START_OBJECT_KEY, FGEPoint.class);
	public static GRProperty<FGEPoint> CP2_RELATIVE_TO_END_OBJECT = GRProperty.getGRParameter(CurveConnectorSpecification.class,
			CP2_RELATIVE_TO_END_OBJECT_KEY, FGEPoint.class);
	public static GRProperty<Boolean> ARE_BOUNDS_ADJUSTABLE = GRProperty.getGRParameter(CurveConnectorSpecification.class,
			ARE_BOUNDS_ADJUSTABLE_KEY, Boolean.class);

	// *******************************************************************************
	// * Properties
	// *******************************************************************************

	@Getter(value = CP1_RELATIVE_TO_START_OBJECT_KEY, isStringConvertable = true)
	@XMLAttribute
	public FGEPoint getCp1RelativeToStartObject();

	@Setter(value = CP1_RELATIVE_TO_START_OBJECT_KEY)
	public void setCp1RelativeToStartObject(FGEPoint aPoint);

	@Getter(value = CP2_RELATIVE_TO_END_OBJECT_KEY, isStringConvertable = true)
	@XMLAttribute
	public FGEPoint getCp2RelativeToEndObject();

	@Setter(value = CP2_RELATIVE_TO_END_OBJECT_KEY)
	public void setCp2RelativeToEndObject(FGEPoint aPoint);

	@Getter(value = CP_POSITION_KEY, isStringConvertable = true)
	@XMLAttribute
	public FGEPoint getCpPosition();

	@Setter(value = CP_POSITION_KEY)
	public void setCpPosition(FGEPoint cpPosition);

	@Getter(value = ARE_BOUNDS_ADJUSTABLE_KEY, defaultValue = "true")
	@XMLAttribute
	public boolean getAreBoundsAdjustable();

	@Setter(value = ARE_BOUNDS_ADJUSTABLE_KEY)
	public void setAreBoundsAdjustable(boolean aFlag);

}
