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

package org.openflexo.fge.control;

import org.openflexo.fge.control.actions.DrawConnectorAction;
import org.openflexo.fge.control.actions.DrawShapeAction;
import org.openflexo.fge.control.tools.DianaInspectors;
import org.openflexo.fge.control.tools.DianaLayoutWidget;
import org.openflexo.fge.control.tools.DianaPalette;
import org.openflexo.fge.control.tools.DianaScaleSelector;
import org.openflexo.fge.control.tools.DianaStyles;
import org.openflexo.fge.control.tools.DianaToolSelector;
import org.openflexo.fge.control.tools.DrawComplexCurveToolController;
import org.openflexo.fge.control.tools.DrawConnectorToolController;
import org.openflexo.fge.control.tools.DrawPolygonToolController;
import org.openflexo.fge.control.tools.DrawShapeToolController;
import org.openflexo.fge.control.tools.DrawTextToolController;

/**
 * Represent the tool factory for a given technology (eg. Swing)
 * 
 * @author sylvain
 * 
 * @param <C>
 *            base minimal class of components build by this tool factory (eg JComponent for Swing)
 */
public interface DianaToolFactory<C> {

	public DianaToolSelector<? extends C, ?> makeDianaToolSelector(AbstractDianaEditor<?, ?, ?> editor);

	public DianaScaleSelector<? extends C, ?> makeDianaScaleSelector(AbstractDianaEditor<?, ?, ?> editor);

	public DianaStyles<? extends C, ?> makeDianaStyles();

	public DianaInspectors<?, ?> makeDianaDialogInspectors();

	public DianaInspectors<?, ?> makeDianaInspectors();

	public DianaLayoutWidget<? extends C, ?> makeDianaLayoutWidget();

	public DianaPalette<? extends C, ?> makeDianaPalette(DrawingPalette palette);

	public DrawPolygonToolController<?> makeDrawPolygonToolController(DianaInteractiveEditor<?, ?, ?> controller, DrawShapeAction control);

	public DrawComplexCurveToolController<?> makeDrawClosedCurveToolController(DianaInteractiveEditor<?, ?, ?> controller,
			DrawShapeAction control, boolean isClosedCurve);

	public DrawShapeToolController<?> makeDrawShapeToolController(DianaInteractiveEditor<?, ?, ?> controller, DrawShapeAction control);

	public DrawConnectorToolController<?> makeDrawConnectorToolController(DianaInteractiveEditor<?, ?, ?> controller,
			DrawConnectorAction control);

	public DrawTextToolController<?> makeDrawTextToolController(DianaInteractiveEditor<?, ?, ?> controller, DrawShapeAction control);

}
