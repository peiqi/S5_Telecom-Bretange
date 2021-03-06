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

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fge.Drawing.ConnectorNode;
import org.openflexo.fge.Drawing.DrawingTreeNode;
import org.openflexo.fge.Drawing.ShapeNode;
import org.openflexo.fge.control.AbstractDianaEditor;
import org.openflexo.fge.control.DianaInteractiveViewer;
import org.openflexo.fge.view.DianaViewFactory;
import org.openflexo.model.undo.CompoundEdit;

public abstract class DianaToolImpl<C, F extends DianaViewFactory<F, ?>> implements DianaTool<C, F>, PropertyChangeListener {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DianaToolImpl.class.getPackage().getName());

	private AbstractDianaEditor<?, F, ?> editor;

	/**
	 * Return the editor this tool is associated to
	 * 
	 * @return
	 */
	@Override
	public AbstractDianaEditor<?, F, ?> getEditor() {
		return editor;
	}

	/**
	 * Sets the editor this tool is associated to
	 * 
	 * @param editor
	 */
	@Override
	public void attachToEditor(AbstractDianaEditor<?, F, ?> editor) {
		if (this.editor != editor) {
			if (this.editor != null) {
				// This tool was associated to another editor, disconnect it
				this.editor.getPropertyChangeSupport().removePropertyChangeListener(this);
			}
			if (editor != null) {
				// Connect this tool to the new editor
				editor.getPropertyChangeSupport().addPropertyChangeListener(this);
			}
			this.editor = editor;
		}
	}

	public List<DrawingTreeNode<?, ?>> getSelection() {
		if (getEditor() instanceof DianaInteractiveViewer) {
			return ((DianaInteractiveViewer<?, F, ?>) getEditor()).getSelectedObjects();
		}
		return null;
	}

	public List<ShapeNode<?>> getSelectedShapes() {
		if (getEditor() instanceof DianaInteractiveViewer) {
			return ((DianaInteractiveViewer<?, F, ?>) getEditor()).getSelectedShapes();
		}
		return null;
	}

	public List<ConnectorNode<?>> getSelectedConnectors() {
		if (getEditor() instanceof DianaInteractiveViewer) {
			return ((DianaInteractiveViewer<?, F, ?>) getEditor()).getSelectedConnectors();
		}
		return null;
	}

	protected CompoundEdit startRecordEdit(String editName) {
		if (getEditor().getUndoManager() != null && !getEditor().getUndoManager().isUndoInProgress()
				&& !getEditor().getUndoManager().isRedoInProgress()) {
			return getEditor().getUndoManager().startRecording(editName);
		}
		return null;
	}

	protected void stopRecordEdit(CompoundEdit edit) {
		if (edit != null && getEditor().getUndoManager() != null) {
			getEditor().getUndoManager().stopRecording(edit);
		}
	}

}
