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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.fge.FGEModelFactory;
import org.openflexo.fge.FGEModelFactoryImpl;
import org.openflexo.fge.ShapeGraphicalRepresentation;
import org.openflexo.fge.ShapeGraphicalRepresentation.LocationConstraints;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * A {@link DrawingPalette} is the abstraction of a palette associated to a drawing<br>
 * A {@link DrawingPalette} is composed of {@link PaletteElement}
 * 
 * @author sylvain
 * 
 */
public class DrawingPalette implements HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(DrawingPalette.class.getPackage().getName());

	protected List<PaletteElement> elements;

	private final int width;
	private final int height;
	private final String title;

	private boolean drawWorkingArea = false;

	private final PropertyChangeSupport pcSupport;

	/**
	 * This factory is the one used to build palettes, NOT THE ONE which is used in the related drawing editor
	 */
	public static FGEModelFactory FACTORY;

	static {
		try {
			FACTORY = new FGEModelFactoryImpl();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}

	public DrawingPalette(int width, int height, String title) {
		try {
			FACTORY = new FGEModelFactoryImpl();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		pcSupport = new PropertyChangeSupport(this);
		this.width = width;
		this.height = height;
		this.title = title;
		elements = new ArrayList<PaletteElement>();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Build palette " + title + " " + Integer.toHexString(hashCode()) + " of " + getClass().getName());
		}
	}

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	public void delete() {
		for (PaletteElement element : elements) {
			element.delete();
		}
		elements = null;
	}

	public String getTitle() {
		return title;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public List<PaletteElement> getElements() {
		return elements;
	}

	public void addElement(PaletteElement element) {
		if (element != null) {
			elements.add(element);
			ShapeGraphicalRepresentation gr = element.getGraphicalRepresentation();
			if (gr != null) {
				// Try to perform some checks and initialization of
				// expecting behaviour for a PaletteElement
				element.getGraphicalRepresentation().setIsFocusable(false);
				element.getGraphicalRepresentation().setIsSelectable(false);
				element.getGraphicalRepresentation().setIsReadOnly(true);
				element.getGraphicalRepresentation().setLocationConstraints(LocationConstraints.UNMOVABLE);
				// element.getGraphicalRepresentation().addToMouseDragControls(mouseDragControl)
			}
			else {
				logger.warning("Adding an element with empty GR ! in palette:  " + this.getTitle());
			}
			pcSupport.firePropertyChange("elements", null, element);
		}
		else {

			logger.warning("Adding a null element in palette:  " + this.getTitle());
		}
	}

	public void removeElement(PaletteElement element) {
		elements.remove(element);
		pcSupport.firePropertyChange("elements", element, null);
	}

	public boolean getDrawWorkingArea() {
		return drawWorkingArea;
	}

	public void setDrawWorkingArea(boolean drawWorkingArea) {
		this.drawWorkingArea = drawWorkingArea;
	}

}
