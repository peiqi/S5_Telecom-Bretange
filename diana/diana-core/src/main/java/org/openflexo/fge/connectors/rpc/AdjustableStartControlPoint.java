/*
 * (c) Copyright 2010-2011 AgileBirds
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
package org.openflexo.fge.connectors.rpc;

import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import org.openflexo.fge.FGEUtils;
import org.openflexo.fge.connectors.impl.RectPolylinConnector;
import org.openflexo.fge.control.DianaEditor;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.fge.geom.FGERectPolylin;
import org.openflexo.fge.geom.area.FGEArea;

public class AdjustableStartControlPoint extends RectPolylinAdjustableControlPoint {
	static final Logger LOGGER = Logger.getLogger(AdjustableStartControlPoint.class.getPackage().getName());

	public AdjustableStartControlPoint(FGEPoint point, RectPolylinConnector connector) {
		super(point, connector);
	}

	@Override
	public FGEArea getDraggingAuthorizedArea() {
		return getConnector().retrieveAllowedStartArea(false);
	}

	@Override
	public void startDragging(DianaEditor<?> controller, FGEPoint startPoint) {
		super.startDragging(controller, startPoint);
		getConnectorSpecification().setIsStartingLocationFixed(true);
	}

	@Override
	public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
			FGEPoint initialPoint, MouseEvent event) {
		// System.out.println("newRelativePoint="+newRelativePoint);
		FGEPoint pt = getNearestPointOnAuthorizedArea(newRelativePoint);
		// System.out.println("pt="+pt);
		if (pt == null) {
			LOGGER.warning("Cannot find nearest point for point " + newRelativePoint + " and area " + getDraggingAuthorizedArea());
			return false;
		}
		setPoint(pt);
		FGEPoint ptRelativeToStartObject = FGEUtils.convertNormalizedPoint(getNode(), pt, getNode().getStartNode());
		getConnector().setFixedStartLocation(ptRelativeToStartObject);
		switch (getConnectorSpecification().getAdjustability()) {
		case AUTO_LAYOUT:
			// Nothing special to do
			break;
		case BASICALLY_ADJUSTABLE:
			// Nothing special to do
			break;
		case FULLY_ADJUSTABLE:
			if (initialPolylin.getSegmentNb() == 1 && getConnector()._updateAsFullyAdjustableForUniqueSegment(pt)
					&& !getConnectorSpecification().getIsEndingLocationFixed()) {
				// OK this is still a unique segment, nice !
			} else {
				FGERectPolylin newPolylin = initialPolylin.clone();
				newPolylin.updatePointAt(0, pt);
				getConnector().updateWithNewPolylin(newPolylin, true);
			}
			break;
		default:
			break;
		}
		getConnector()._connectorChanged(true);
		getNode().notifyConnectorModified();
		return true;
	}
}