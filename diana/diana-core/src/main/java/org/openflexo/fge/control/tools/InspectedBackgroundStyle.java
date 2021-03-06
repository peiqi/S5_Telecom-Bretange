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
package org.openflexo.fge.control.tools;

import java.util.List;

import org.openflexo.fge.BackgroundStyle;
import org.openflexo.fge.BackgroundStyle.BackgroundStyleType;
import org.openflexo.fge.Drawing.DrawingTreeNode;
import org.openflexo.fge.Drawing.ShapeNode;
import org.openflexo.fge.ShapeGraphicalRepresentation;
import org.openflexo.fge.control.DianaInteractiveViewer;
import org.openflexo.model.undo.CompoundEdit;

/**
 * Implementation of {@link BackgroundStyle}, as a container of graphical properties synchronized with and reflecting a selection<br>
 * This is the object beeing represented in tool inspectors
 * 
 * @author sylvain
 * 
 */
public class InspectedBackgroundStyle extends InspectedStyleUsingFactory<BackgroundStyleFactory, BackgroundStyle, BackgroundStyleType> {

	public InspectedBackgroundStyle(DianaInteractiveViewer<?, ?, ?> controller) {
		super(controller, new BackgroundStyleFactory(controller));
	}

	@Override
	public List<ShapeNode<?>> getSelection() {
		return getController().getSelectedShapes();
	}

	@Override
	public BackgroundStyle getStyle(DrawingTreeNode<?, ?> node) {
		if (node instanceof ShapeNode) {
			return ((ShapeNode<?>) node).getBackgroundStyle();
		}
		return null;
	}

	@Override
	protected BackgroundStyleType getStyleType(BackgroundStyle style) {
		if (style != null) {
			return style.getBackgroundStyleType();
		}
		return null;
	}

	@Override
	protected void applyNewStyle(BackgroundStyleType aStyleType, DrawingTreeNode<?, ?> node) {
		ShapeNode<?> n = (ShapeNode<?>) node;
		BackgroundStyle oldStyle = n.getBackgroundStyle();
		CompoundEdit setValueEdit = startRecordEdit("Set BackgroundStyleType to " + aStyleType);
		n.setBackgroundStyle(getStyleFactory().makeNewStyle(oldStyle));
		if (oldStyle != null) {
			n.getPropertyChangeSupport().firePropertyChange(ShapeGraphicalRepresentation.BACKGROUND_STYLE_TYPE_KEY,
					oldStyle.getBackgroundStyleType(), aStyleType);
		}
		stopRecordEdit(setValueEdit);
	}
}
