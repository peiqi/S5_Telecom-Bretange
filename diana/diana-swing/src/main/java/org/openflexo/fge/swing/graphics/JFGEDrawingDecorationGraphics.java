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
package org.openflexo.fge.swing.graphics;

import java.awt.Point;
import java.util.logging.Logger;

import org.openflexo.fge.Drawing.RootNode;
import org.openflexo.fge.DrawingGraphicalRepresentation;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.fge.graphics.FGEDrawingDecorationGraphics;
import org.openflexo.fge.swing.view.JDrawingView;

public class JFGEDrawingDecorationGraphics extends JFGEGraphics implements FGEDrawingDecorationGraphics {

	private static final Logger logger = Logger.getLogger(JFGEDrawingDecorationGraphics.class.getPackage().getName());

	public <O> JFGEDrawingDecorationGraphics(RootNode<O> rootNode, JDrawingView<O> view) {
		super(rootNode, view);
	}

	@Override
	public RootNode<?> getNode() {
		return (RootNode<?>) super.getNode();
	}

	@Override
	public DrawingGraphicalRepresentation getGraphicalRepresentation() {
		return (DrawingGraphicalRepresentation) super.getGraphicalRepresentation();
	}

	public double getWidth() {
		return getNode().getWidth();
	}

	public double getHeight() {
		return getNode().getHeight();
	}

	// Decoration graphics doesn't use normalized coordinates system
	@Override
	public Point convertNormalizedPointToViewCoordinates(double x, double y) {
		return new Point((int) Math.round(x * getScale()), (int) Math.round(y * getScale()));
	}

	// Decoration graphics doesn't use normalized coordinates system
	@Override
	public FGEPoint convertViewCoordinatesToNormalizedPoint(int x, int y) {
		return new FGEPoint(x / getScale(), y / getScale());
	}

}
