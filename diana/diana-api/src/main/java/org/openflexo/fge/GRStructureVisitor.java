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
package org.openflexo.fge;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.fge.Drawing.ConnectorNode;
import org.openflexo.fge.Drawing.ContainerNode;
import org.openflexo.fge.Drawing.DrawingTreeNode;
import org.openflexo.fge.Drawing.DrawingTreeNodeIdentifier;
import org.openflexo.fge.Drawing.GeometricNode;
import org.openflexo.fge.Drawing.GraphNode;
import org.openflexo.fge.Drawing.PendingConnector;
import org.openflexo.fge.Drawing.ShapeNode;
import org.openflexo.fge.GRBinding.ConnectorGRBinding;
import org.openflexo.fge.GRBinding.ContainerGRBinding;
import org.openflexo.fge.GRBinding.GeometricGRBinding;
import org.openflexo.fge.GRBinding.GraphGRBinding;
import org.openflexo.fge.GRBinding.ShapeGRBinding;
import org.openflexo.fge.graph.FGEGraph;

/**
 * Represents a dynamic structure allowing to explore represented graph of objects<br>
 * A {@link GRStructureVisitor} is attached to a {@link GRBinding}, and allows to explore from a {@link DrawingTreeNode} defined as a
 * starting point
 * 
 * @author sylvain
 * 
 * @param <R>
 */
public abstract class GRStructureVisitor<R> {

	static final Logger LOGGER = Logger.getLogger(GRStructureVisitor.class.getPackage().getName());

	private DrawingTreeNode<R, ?> node;
	private Drawing<?> drawing;

	private List<DrawingTreeNode<?, ?>> createdNodes;
	private List<DrawingTreeNode<?, ?>> deletedNodes;
	private List<DrawingTreeNode<?, ?>> updatedNodes;
	private List<PendingConnector<?>> pendingConnectors;

	/**
	 * Exploring method
	 * 
	 * @param drawable
	 */
	public abstract void visit(R drawable);

	/**
	 * Internal method called when starting exploring from supplied {@link DrawingTreeNode}
	 * 
	 * @param node
	 *            : the node from which starts the exploration
	 */
	public void startVisiting(DrawingTreeNode<R, ?> node) {
		this.node = node;
		drawing = node.getDrawing();
		createdNodes = new ArrayList<Drawing.DrawingTreeNode<?, ?>>();
		deletedNodes = new ArrayList<Drawing.DrawingTreeNode<?, ?>>();
		updatedNodes = new ArrayList<Drawing.DrawingTreeNode<?, ?>>();
		pendingConnectors = new ArrayList<PendingConnector<?>>();
	}

	/**
	 * Internal method called when stopping exploring form supplied {@link DrawingTreeNode}
	 */
	public void stopVisiting() {
	}

	/**
	 * Called to specify the drawing of a shape representing supplied drawable using supplied {@link GRBinding}
	 * 
	 * @param binding
	 * @param drawable
	 * @return
	 */
	public <O> ShapeNode<O> drawShape(ShapeGRBinding<O> binding, O drawable) {
		if (node instanceof ContainerNode) {
			return drawShape((ContainerNode<O, ?>) node, binding, drawable);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Called to specify the drawing of a shape representing supplied drawable using supplied {@link GRBinding}, under the graphical node
	 * representing supplied parentDrawable<br>
	 * This method should not be used if parentDrawable is represented using many GRBinding
	 * 
	 * @param binding
	 * @param drawable
	 * @param parentDrawable
	 * @return
	 */
	public <O> ShapeNode<O> drawShape(ShapeGRBinding<O> binding, O drawable, Object parentDrawable) {
		Drawing<?> drawing = node.getDrawing();
		DrawingTreeNode<?, ?> parentNode = drawing.getDrawingTreeNode(parentDrawable);
		if (parentNode == null) {
			LOGGER.warning("Cannot add shape: null container node");
			return null;
		} else if (parentNode instanceof ContainerNode) {
			return drawShape((ContainerNode<O, ?>) parentNode, binding, drawable);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Called to specify the drawing of a shape representing supplied drawable using supplied {@link GRBinding}, under the graphical node
	 * representing supplied parentDrawable and parentBinding
	 * 
	 * @param binding
	 * @param drawable
	 * @param parentBinding
	 * @param parentDrawable
	 * @return
	 */
	public <O, P> ShapeNode<O> drawShape(ShapeGRBinding<O> binding, O drawable, ContainerGRBinding<P, ?> parentBinding, P parentDrawable) {
		Drawing<?> drawing = node.getDrawing();
		DrawingTreeNode<?, ?> parentNode = drawing.getDrawingTreeNode(parentDrawable, parentBinding);
		if (parentNode == null) {
			LOGGER.warning("Cannot add shape: null container node");
			return null;
		} else if (parentNode instanceof ContainerNode) {
			return drawShape((ContainerNode<O, ?>) parentNode, binding, drawable);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Internally used to draw or retrieve a ShapeNode in the graphical object hierarchy
	 * 
	 * @param parent
	 * @param binding
	 * @param drawable
	 * @return
	 */
	private <O> ShapeNode<O> drawShape(ContainerNode<?, ?> parent, ShapeGRBinding<O> binding, O drawable) {
		Drawing<?> drawing = node.getDrawing();

		if (parent.hasShapeFor(binding, drawable)) {
			// Already existing
			// System.out.println("% Found already existing node for " + drawable);
			ShapeNode<O> returned = parent.getShapeFor(binding, drawable);
			updatedNodes.add(returned);
			// deletedNodes.remove(returned);
			return returned;
		} else {
			// System.out.println("% Creating new node for " + drawable);
			ShapeNode<O> returned = drawing.createNewShapeNode(parent, binding, drawable);
			// New node
			createdNodes.add(returned);
			return returned;
		}
	}

	/**
	 * Called to specify the drawing of a geometric representing supplied drawable using supplied {@link GRBinding}
	 * 
	 * @param binding
	 * @param drawable
	 * @return
	 */
	public <O> GeometricNode<O> drawGeometricObject(GeometricGRBinding<O> binding, O drawable) {
		if (node instanceof ContainerNode) {
			return drawGeometricObject((ContainerNode<O, ?>) node, binding, drawable);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Internally used to draw or retrieve a ShapeNode in the graphical object hierarchy
	 * 
	 * @param parent
	 * @param binding
	 * @param drawable
	 * @return
	 */
	private <O> GeometricNode<O> drawGeometricObject(ContainerNode<?, ?> parent, GeometricGRBinding<O> binding, O drawable) {
		Drawing<?> drawing = node.getDrawing();

		if (parent.hasGeometricObjectFor(binding, drawable)) {
			// Already existing
			// System.out.println("% Found already existing node for " + drawable);
			GeometricNode<O> returned = parent.getGeometricObjectFor(binding, drawable);
			updatedNodes.add(returned);
			// deletedNodes.remove(returned);
			return returned;
		} else {
			// System.out.println("% Creating new node for " + drawable);
			GeometricNode<O> returned = drawing.createNewGeometricNode(parent, binding, drawable);
			// New node
			createdNodes.add(returned);
			return returned;
		}
	}

	/**
	 * Called to specify the drawing of a connector representing supplied drawable using supplied {@link GRBinding}<br>
	 * This method should not be used if fromDrawable and/or toDrawable is/are represented using many GRBinding. In this case, use
	 * {@link #drawConnector(ConnectorGRBinding, Object, ShapeGRBinding, Object, ShapeGRBinding, Object)} instead
	 * 
	 * @param binding
	 * @param drawable
	 * @param fromDrawable
	 * @param toDrawable
	 * @return
	 */
	public <O> ConnectorNode<O> drawConnector(ConnectorGRBinding<O> binding, O drawable, Object fromDrawable, Object toDrawable) {
		Drawing<?> drawing = node.getDrawing();
		DrawingTreeNode<?, ?> fromNode = drawing.getDrawingTreeNode(fromDrawable);
		DrawingTreeNode<?, ?> toNode = drawing.getDrawingTreeNode(toDrawable);
		if (fromNode == null || toNode == null || node == null) {
			// Not fully resolved now, declare as pending
			DrawingTreeNodeIdentifier<?> parentNodeIdentifier = new DrawingTreeNodeIdentifier(node.getDrawable(), node.getGRBinding());
			DrawingTreeNodeIdentifier<?> startNodeIdentifier = new DrawingTreeNodeIdentifier(fromDrawable, null);
			DrawingTreeNodeIdentifier<?> endNodeIdentifier = new DrawingTreeNodeIdentifier(toDrawable, null);
			return drawPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier, endNodeIdentifier).getConnectorNode();
		}
		if (!(fromNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector from non-shape node");
			return null;
		}
		if (!(toNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector to non-shape node");
			return null;
		}
		if (node instanceof ContainerNode) {
			return drawConnector((ContainerNode<O, ?>) node, binding, drawable, (ShapeNode<?>) fromNode, (ShapeNode<?>) toNode);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

/**
	 * Called to specify the drawing of a connector representing supplied drawable using supplied {@link GRBinding} This method should not
	 * be used if fromDrawable and/or toDrawable and/or parentDrawable is/are represented using many GRBinding. In this case, use
	 * {@link #drawConnector(ConnectorGRBinding, Object, ShapeGRBinding, Object, ShapeGRBinding, Object, ContainerGRBinding, Object) instead
	 * 
	 * @param binding
	 * @param drawable
	 * @param fromDrawable
	 * @param toDrawable
	 * @return
	 */
	public <O> ConnectorNode<O> drawConnector(ConnectorGRBinding<O> binding, O drawable, Object fromDrawable, Object toDrawable,
			Object parentDrawable) {
		Drawing<?> drawing = node.getDrawing();
		DrawingTreeNode<?, ?> fromNode = drawing.getDrawingTreeNode(fromDrawable);
		DrawingTreeNode<?, ?> toNode = drawing.getDrawingTreeNode(toDrawable);
		if (fromNode == null || toNode == null || node == null) {
			// Not fully resolved now, declare as pending
			DrawingTreeNodeIdentifier<?> parentNodeIdentifier = new DrawingTreeNodeIdentifier(parentDrawable, null);
			DrawingTreeNodeIdentifier<?> startNodeIdentifier = new DrawingTreeNodeIdentifier(fromDrawable, null);
			DrawingTreeNodeIdentifier<?> endNodeIdentifier = new DrawingTreeNodeIdentifier(toDrawable, null);
			return drawPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier, endNodeIdentifier).getConnectorNode();
		}
		if (!(fromNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector from non-shape node");
			return null;
		}
		if (!(toNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector to non-shape node");
			return null;
		}
		DrawingTreeNode<?, ?> parentNode = drawing.getDrawingTreeNode(parentDrawable);
		if (parentNode == null) {
			LOGGER.warning("Cannot add shape: null container node");
			return null;
		} else if (parentNode instanceof ContainerNode) {
			return drawConnector((ContainerNode<O, ?>) parentNode, binding, drawable, (ShapeNode<?>) fromNode, (ShapeNode<?>) toNode);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Called to specify the drawing of a connector representing supplied drawable using supplied {@link GRBinding}<br>
	 * 
	 * @param binding
	 * @param drawable
	 * @param fromBinding
	 * @param fromDrawable
	 * @param toBinding
	 * @param toDrawable
	 * @return
	 */
	public <O, F, T> ConnectorNode<O> drawConnector(ConnectorGRBinding<O> binding, O drawable, ShapeGRBinding<F> fromBinding,
			F fromDrawable, ShapeGRBinding<T> toBinding, T toDrawable) {
		Drawing<?> drawing = node.getDrawing();
		DrawingTreeNode<?, ?> fromNode = drawing.getDrawingTreeNode(fromDrawable, fromBinding);
		DrawingTreeNode<?, ?> toNode = drawing.getDrawingTreeNode(toDrawable, toBinding);
		if (fromNode == null || toNode == null || node == null) {
			// Not fully resolved now, declare as pending
			DrawingTreeNodeIdentifier<?> parentNodeIdentifier = new DrawingTreeNodeIdentifier(node.getDrawable(), node.getGRBinding());
			DrawingTreeNodeIdentifier<?> startNodeIdentifier = new DrawingTreeNodeIdentifier(fromDrawable, null);
			DrawingTreeNodeIdentifier<?> endNodeIdentifier = new DrawingTreeNodeIdentifier(toDrawable, null);
			return drawPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier, endNodeIdentifier).getConnectorNode();
		}
		if (!(fromNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector from non-shape node");
			return null;
		}
		if (!(toNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector to non-shape node");
			return null;
		}
		if (node instanceof ContainerNode) {
			return drawConnector((ContainerNode<O, ?>) node, binding, drawable, (ShapeNode<?>) fromNode, (ShapeNode<?>) toNode);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Called to specify the drawing of a connector representing supplied drawable using supplied {@link GRBinding}<br>
	 * 
	 * @param binding
	 * @param drawable
	 * @param fromBinding
	 * @param fromDrawable
	 * @param toBinding
	 * @param toDrawable
	 * @param parentBinding
	 * @param parentDrawable
	 * @return
	 */
	public <O, F, T, P> ConnectorNode<O> drawConnector(ConnectorGRBinding<O> binding, O drawable, ShapeGRBinding<F> fromBinding,
			F fromDrawable, ShapeGRBinding<T> toBinding, T toDrawable, ContainerGRBinding<P, ?> parentBinding, P parentDrawable) {
		Drawing<?> drawing = node.getDrawing();
		DrawingTreeNode<?, ?> fromNode = drawing.getDrawingTreeNode(fromDrawable, fromBinding);
		DrawingTreeNode<?, ?> toNode = drawing.getDrawingTreeNode(toDrawable, toBinding);
		if (!(fromNode instanceof ShapeNode)) {
			if (fromNode == null || toNode == null || node == null) {
				// Not fully resolved now, declare as pending
				DrawingTreeNodeIdentifier<?> parentNodeIdentifier = new DrawingTreeNodeIdentifier(parentDrawable, parentBinding);
				DrawingTreeNodeIdentifier<?> startNodeIdentifier = new DrawingTreeNodeIdentifier(fromDrawable, null);
				DrawingTreeNodeIdentifier<?> endNodeIdentifier = new DrawingTreeNodeIdentifier(toDrawable, null);
				return drawPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier, endNodeIdentifier)
						.getConnectorNode();
			}
			LOGGER.warning("Cannot add connector from non-shape node");
			return null;
		}
		if (!(toNode instanceof ShapeNode)) {
			LOGGER.warning("Cannot add connector to non-shape node");
			return null;
		}
		DrawingTreeNode<?, ?> parentNode = drawing.getDrawingTreeNode(parentDrawable, parentBinding);
		if (parentNode == null) {
			LOGGER.warning("Cannot add shape: null container node");
			return null;
		} else if (parentNode instanceof ContainerNode) {
			return drawConnector((ContainerNode<O, ?>) parentNode, binding, drawable, (ShapeNode<?>) fromNode, (ShapeNode<?>) toNode);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Internally used to draw or retrieve a ShapeNode in the graphical object hierarchy
	 * 
	 * @param parent
	 * @param binding
	 * @param drawable
	 * @return
	 */
	private <O> ConnectorNode<O> drawConnector(ContainerNode<?, ?> parent, ConnectorGRBinding<O> binding, O drawable,
			ShapeNode<?> fromNode, ShapeNode<?> toNode) {
		Drawing<?> drawing = node.getDrawing();

		if (parent.hasConnectorFor(binding, drawable)) {
			// System.out.println("Ca existe deja");
			ConnectorNode<O> returned = parent.getConnectorFor(binding, drawable);
			if (returned.getStartNode() != fromNode || returned.getEndNode() != toNode) {
				// the structure is incorrect
				deletedNodes.add(returned);
				returned = drawing.createNewConnectorNode(parent, binding, drawable, fromNode, toNode);
				createdNodes.add(returned);
			} else {
				updatedNodes.add(returned);
			}
			return returned;
		} else {
			// System.out.println("***** New ConnectorNode !!!! " + parent.getChildNodes());
			ConnectorNode<O> returned = drawing.createNewConnectorNode(parent, binding, drawable, fromNode, toNode);
			createdNodes.add(returned);
			return returned;
		}
	}

	/**
	 * Internally used to manage a pending connector in the graphical object hierarchy
	 * 
	 * @param parent
	 * @param binding
	 * @param drawable
	 * @return
	 */
	private <O> PendingConnector<O> drawPendingConnector(ConnectorGRBinding<O> binding, O drawable,
			DrawingTreeNodeIdentifier<?> parentNodeIdentifier, DrawingTreeNodeIdentifier<?> startNodeIdentifier,
			DrawingTreeNodeIdentifier<?> endNodeIdentifier) {
		Drawing<?> drawing = node.getDrawing();

		if (drawing.hasPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier, endNodeIdentifier)) {
			return drawing.getPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier, endNodeIdentifier);
		} else {
			PendingConnector<O> returned = drawing.createPendingConnector(binding, drawable, parentNodeIdentifier, startNodeIdentifier,
					endNodeIdentifier);
			pendingConnectors.add(returned);
			return returned;
		}
	}

	/**
	 * Called to specify the drawing of a graph
	 * 
	 * @param binding
	 * @param drawable
	 * @return
	 */
	public <G extends FGEGraph> GraphNode<G> drawGraph(GraphGRBinding<G> binding, G drawable) {
		if (node instanceof ContainerNode) {
			return drawGraph((ContainerNode<G, ?>) node, binding, drawable);
		} else {
			LOGGER.warning("Cannot add shape in non-container node");
			return null;
		}
	}

	/**
	 * Internally used to draw or retrieve a ShapeNode in the graphical object hierarchy
	 * 
	 * @param parent
	 * @param binding
	 * @param drawable
	 * @return
	 */
	private <G extends FGEGraph> GraphNode<G> drawGraph(ContainerNode<?, ?> parent, GraphGRBinding<G> binding, G drawable) {
		Drawing<?> drawing = node.getDrawing();

		if (parent.hasGraphFor(binding, drawable)) {
			// Already existing
			// System.out.println("% Found already existing node for " + drawable);
			GraphNode<G> returned = parent.getGraphFor(binding, drawable);
			updatedNodes.add(returned);
			// deletedNodes.remove(returned);
			return returned;
		} else {
			// System.out.println("% Creating new node for " + drawable);
			GraphNode<G> returned = drawing.createNewGraphNode(parent, binding, drawable);
			// New node
			createdNodes.add(returned);
			return returned;
		}
	}

	public List<DrawingTreeNode<?, ?>> getCreatedNodes() {
		return createdNodes;
	}

	public List<DrawingTreeNode<?, ?>> getDeletedNodes() {
		return deletedNodes;
	}

	public List<DrawingTreeNode<?, ?>> getUpdatedNodes() {
		return updatedNodes;
	}

	public List<PendingConnector<?>> getPendingConnectors() {
		return pendingConnectors;
	}
}
