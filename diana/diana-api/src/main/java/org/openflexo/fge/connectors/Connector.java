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

import java.beans.PropertyChangeListener;
import java.util.List;

import org.openflexo.fge.Drawing.ConnectorNode;
import org.openflexo.fge.Drawing.ShapeNode;
import org.openflexo.fge.GRProperty;
import org.openflexo.fge.connectors.ConnectorSpecification.ConnectorType;
import org.openflexo.fge.cp.ControlArea;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.fge.geom.FGERectangle;
import org.openflexo.fge.graphics.FGEConnectorGraphics;

public interface Connector<CS extends ConnectorSpecification> extends PropertyChangeListener {

	@SuppressWarnings("unchecked")
	public abstract CS getConnectorSpecification();

	public abstract ConnectorNode<?> getConnectorNode();

	public abstract ShapeNode<?> getStartNode();

	public abstract ShapeNode<?> getEndNode();

	public abstract ConnectorType getConnectorType();

	public abstract double getStartAngle();

	public abstract double getEndAngle();

	public abstract double distanceToConnector(FGEPoint aPoint, double scale);

	public abstract void drawConnector(FGEConnectorGraphics g);

	/**
	 * Retrieve all control area used to manage this connector
	 * 
	 * @return
	 */
	public abstract List<? extends ControlArea<?>> getControlAreas();

	public abstract FGEPoint getMiddleSymbolLocation();

	/**
	 * Return bounds of actually required area to fully display current connector (which might require to be paint outside normalized
	 * bounds)
	 * 
	 * @return
	 */
	public abstract FGERectangle getConnectorUsedBounds();

	/**
	 * Return start point, relative to start object
	 * 
	 * @return
	 */
	public abstract FGEPoint getStartLocation();

	/**
	 * Return end point, relative to end object
	 * 
	 * @return
	 */
	public abstract FGEPoint getEndLocation();

	public abstract void paintConnector(FGEConnectorGraphics g);

	public abstract void delete();

	public boolean isDeleted();

/**
	 * Returns the property value for supplied parameter<br>
	 * If many Connectors share same ConnectorSpecification (as indicated by {@link Drawing#getPersistenceMode()), do not store value in ConnectorSpecification, but store it in the Connector itself.<br>
	 * This implies that this value is not persistent (not serializable)
	 * 
	 * @param parameter
	 * @return
	 */
	public <T> T getPropertyValue(GRProperty<T> parameter);

/**
	 * Sets the property value for supplied parameter<br>
	 * If many Connectors share same ConnectorSpecification (as indicated by {@link Drawing#getPersistenceMode()), do not store value in ConnectorSpecification, but store it in the Connector itself.<br>
	 * This implies that this value is not persistent (not serializable)
	 * 
	 * @param parameter
	 * @return
	 */
	public <T> void setPropertyValue(GRProperty<T> parameter, T value);

}