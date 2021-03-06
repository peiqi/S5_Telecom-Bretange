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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;

import org.openflexo.fge.Drawing.DrawingTreeNode;
import org.openflexo.fge.FGEModelFactory;
import org.openflexo.fge.control.DianaInteractiveEditor;
import org.openflexo.fge.control.actions.ToolAction;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.fge.graphics.FGEGraphics;
import org.openflexo.fge.view.DrawingView;
import org.openflexo.model.undo.CompoundEdit;
import org.openflexo.toolbox.HasPropertyChangeSupport;

/**
 * Abstract implementation of a Tool controller
 * 
 * @author sylvain
 * 
 * @param <ME>
 *            technology-specific controlling events type
 */
public abstract class ToolController<ME> implements PropertyChangeListener, HasPropertyChangeSupport {

	private static final Logger logger = Logger.getLogger(ToolController.class.getPackage().getName());

	private final DianaInteractiveEditor<?, ?, ?> controller;

	private boolean editionHasBeenStarted = false;

	private final PropertyChangeSupport pcSupport;

	private final ToolAction toolAction;

	public ToolController(DianaInteractiveEditor<?, ?, ?> controller, ToolAction toolAction) {
		super();
		pcSupport = new PropertyChangeSupport(this);
		this.controller = controller;
		this.toolAction = toolAction;
		editionHasBeenStarted = false;

	}

	public abstract FGEGraphics getGraphics();

	/**
	 * Return the DrawingView of the controller this tool is associated to
	 * 
	 * @return
	 */
	public DrawingView<?, ?> getDrawingView() {
		if (getController() != null) {
			return getController().getDrawingView();
		}
		return null;
	}

	public ToolAction getToolAction() {
		return toolAction;
	}

	protected void startMouseEdition(ME e) {
		editionHasBeenStarted = true;
	}

	protected void stopMouseEdition() {
		editionHasBeenStarted = false;
	}

	public boolean editionHasBeenStarted() {
		return editionHasBeenStarted;
	}

	public DianaInteractiveEditor<?, ?, ?> getController() {
		return controller;
	}

	public FGEModelFactory getFactory() {
		return controller.getFactory();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		logger.info("propertyChange in DrawCustomShapeToolController with " + evt);
	}

	/**
	 * Return point where event occurs, relative to DrawingView
	 */
	public abstract FGEPoint getPoint(ME e);

	/**
	 * Process mouse cliked event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mouseClicked(ME e) {
		// System.out.println("mouseClicked() on " + getPoint(e));
		return false;
	}

	/**
	 * Process mouse pressed event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mousePressed(ME e) {
		// System.out.println("mousePressed() on " + getPoint(e));
		return false;
	}

	/**
	 * Process mouse released event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mouseReleased(ME e) {
		// System.out.println("mouseReleased() on " + getPoint(e));
		return false;
	}

	/**
	 * Process mouse dragged event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mouseDragged(ME e) {
		// System.out.println("mouseDragged() on " + getPoint(e));
		return false;
	}

	/**
	 * Process mouse moved event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mouseMoved(ME e) {
		// System.out.println("mouseMoved() on " + getPoint(e));
		return false;
	}

	/**
	 * Process mouse entered event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mouseEntered(ME e) {
		// System.out.println("mouseEntered() on " + getPoint(e));
		return false;
	}

	/**
	 * Process mouse exited event for current controller
	 * 
	 * @param e
	 * @return true if the event has been consumed (means that the event processing should stop)
	 */
	public boolean mouseExited(ME e) {
		// System.out.println("mouseEntered() on " + getPoint(e));
		return false;
	}

	public abstract DrawingTreeNode<?, ?> getFocusedObject(ME e);

	@Override
	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcSupport;
	}

	@Override
	public String getDeletedProperty() {
		return null;
	}

	protected CompoundEdit startRecordEdit(String editName) {
		if (controller.getUndoManager() != null && !controller.getUndoManager().isUndoInProgress()
				&& !controller.getUndoManager().isRedoInProgress()) {
			return controller.getUndoManager().startRecording(editName);
		}
		return null;
	}

	protected void stopRecordEdit(CompoundEdit edit) {
		if (edit != null && controller.getUndoManager() != null) {
			controller.getUndoManager().stopRecording(edit);
		}
	}

	public void delete() {
		// TODO
		logger.warning("Please implement deletion for ToolController");
	}

}
