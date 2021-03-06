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
package org.openflexo.fge.swing.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openflexo.fge.ConnectorGraphicalRepresentation;
import org.openflexo.fge.Drawing.ConnectorNode;
import org.openflexo.fge.Drawing.DrawingTreeNode;
import org.openflexo.fge.FGEConstants;
import org.openflexo.fge.GraphicalRepresentation;
import org.openflexo.fge.control.AbstractDianaEditor;
import org.openflexo.fge.control.DianaInteractiveViewer;
import org.openflexo.fge.control.tools.DianaPalette;
import org.openflexo.fge.notifications.ConnectorModified;
import org.openflexo.fge.notifications.NodeDeleted;
import org.openflexo.fge.notifications.ObjectHasMoved;
import org.openflexo.fge.notifications.ObjectHasResized;
import org.openflexo.fge.notifications.ObjectMove;
import org.openflexo.fge.notifications.ObjectResized;
import org.openflexo.fge.notifications.ObjectWillMove;
import org.openflexo.fge.notifications.ObjectWillResize;
import org.openflexo.fge.swing.SwingViewFactory;
import org.openflexo.fge.swing.control.tools.JDianaPalette;
import org.openflexo.fge.swing.graphics.DrawUtils;
import org.openflexo.fge.swing.graphics.JFGEConnectorGraphics;
import org.openflexo.fge.swing.paint.FGEPaintManager;
import org.openflexo.fge.view.ConnectorView;

/**
 * The JConnectorView is the SWING implementation of a panel showing a {@link ConnectorNode}
 * 
 * @author sylvain
 * 
 * @param <O>
 */
@SuppressWarnings("serial")
public class JConnectorView<O> extends JPanel implements ConnectorView<O, JPanel>, JFGEView<O, JPanel> {

	private static final Logger logger = Logger.getLogger(JConnectorView.class.getPackage().getName());

	private ConnectorNode<O> connectorNode;
	private FGEViewMouseListener mouseListener;
	private AbstractDianaEditor<?, SwingViewFactory, JComponent> controller;

	private JLabelView<O> labelView;

	protected JFGEConnectorGraphics graphics;

	public JConnectorView(ConnectorNode<O> node, AbstractDianaEditor<?, SwingViewFactory, JComponent> controller) {
		super();
		this.controller = controller;
		this.connectorNode = node;
		updateLabelView();
		relocateAndResizeView();
		mouseListener = controller.getDianaFactory().makeViewMouseListener(connectorNode, this, controller);
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		connectorNode.getPropertyChangeSupport().addPropertyChangeListener(this);
		setOpaque(false);

		updateVisibility();

		graphics = new JFGEConnectorGraphics(node, this);

	}

	private boolean isDeleted = false;

	@Override
	public boolean isDeleted() {
		return isDeleted;
	}

	@Override
	public synchronized void delete() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Delete JConnectorView for " + connectorNode);
		}
		if (getParentView() != null) {
			JDianaLayeredView<?> parentView = getParentView();
			// logger.warning("Unexpected not null parent, proceeding anyway");
			parentView.remove(this);
			parentView.revalidate();
			if (getPaintManager() != null) {
				getPaintManager().invalidate(connectorNode);
				getPaintManager().repaint(parentView);
			}
		}
		if (connectorNode != null) {
			connectorNode.getPropertyChangeSupport().removePropertyChangeListener(this);
		}
		getController().unreferenceViewForDrawingTreeNode(connectorNode);
		setDropTarget(null);
		removeMouseListener(mouseListener);
		removeMouseMotionListener(mouseListener);
		if (labelView != null) {
			labelView.delete();
		}
		labelView = null;
		controller = null;
		mouseListener = null;
		connectorNode = null;
		isDeleted = true;
	}

	@Override
	public O getDrawable() {
		return connectorNode.getDrawable();
	}

	@Override
	public ConnectorNode<O> getNode() {
		return connectorNode;
	}

	@Override
	public JFGEConnectorGraphics getFGEGraphics() {
		return graphics;
	}

	@Override
	public JDrawingView<?> getDrawingView() {
		if (getController() != null) {
			return (JDrawingView<?>) getController().getDrawingView();
		}
		return null;
	}

	@Override
	public JDianaLayeredView<?> getParent() {
		return (JDianaLayeredView<?>) super.getParent();
	}

	@Override
	public JDianaLayeredView<?> getParentView() {
		return getParent();
	}

	@Override
	public double getScale() {
		return getController().getScale();
	}

	@Override
	public void rescale() {
		relocateAndResizeView();
	}

	private void relocateAndResizeView() {
		relocateView();
		resizeView();
	}

	private void relocateView() {
		/*logger.info("relocateView to ("
				+getGraphicalRepresentation().getViewX(getScale())+","+
				getGraphicalRepresentation().getViewY(getScale())+")");*/
		if (labelView != null) {
			labelView.updateBounds();
		}
		int newX, newY;
		newX = connectorNode.getViewX(getScale());
		newY = connectorNode.getViewY(getScale());
		if (newX != getX() || newY != getY()) {
			setLocation(newX, newY);
		}
	}

	private void resizeView() {
		/*logger.info("resizeView to ("
				+getGraphicalRepresentation().getViewWidth(getScale())+","+
				getGraphicalRepresentation().getViewHeight(getScale())+")");*/
		if (labelView != null) {
			labelView.updateBounds();
		}
		int newWidth, newHeight;
		newWidth = connectorNode.getViewWidth(getScale());
		newHeight = connectorNode.getViewHeight(getScale());
		if (newWidth != getWidth() || newHeight != getHeight()) {
			setSize(newWidth, newHeight);
			if (getDrawingView().isBuffering()) {
				/* Something very bad happened here:
				 * the view is resizing while drawing view is beeing buffered:
				 * all the things we were buffering may be wrong now, we have to
				 * start buffering again
				 */
				getDrawingView().startBufferingAgain();
			}
		}
	}

	/*private void updateLayer()
	{
		//logger.info("GR: "+getGraphicalRepresentation()+" update layer to "+getLayer());
		if (getParent() instanceof JLayeredPane) {
			if (labelView!=null)
				((JLayeredPane)getParent()).setLayer(labelView, getLayer());
			((JLayeredPane)getParent()).setLayer(this, getLayer());
		}
	}*/

	private void updateLayer() {
		if (getParent() != null) {
			if (labelView != null) {
				getParent().setLayer((Component) labelView, getLayer());
				getParent().setPosition(labelView, connectorNode.getIndex() * 2);
			}
			getParent().setLayer((Component) this, getLayer());
			getParent().setPosition(this, connectorNode.getIndex() * 2 + 1);
		}
	}

	private void updateVisibility() {
		if (labelView != null) {
			labelView.setVisible(connectorNode.shouldBeDisplayed());
		}
		setVisible(connectorNode.shouldBeDisplayed());
	}

	private void updateLabelView() {
		if (!connectorNode.hasText() && labelView != null) {
			labelView.delete();
			labelView = null;
		} else if (connectorNode.hasText() && labelView == null) {
			labelView = new JLabelView<O>(getNode(), getController(), this);
			if (getParentView() != null) {
				getParentView().add(getLabelView());
			}
		}
	}

	public Integer getLayer() {
		return FGEConstants.INITIAL_LAYER + connectorNode.getGraphicalRepresentation().getLayer();
	}

	@Override
	public void paint(Graphics g) {
		if (isDeleted()) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g;
		DrawUtils.turnOnAntiAlising(g2);
		DrawUtils.setRenderQuality(g2);
		DrawUtils.setColorRenderQuality(g2);
		graphics.createGraphics(g2/*, controller*/);

		if (getPaintManager().isPaintingCacheEnabled()) {
			if (getDrawingView().isBuffering()) {
				// Buffering painting
				if (getPaintManager().isTemporaryObject(connectorNode)) {
					// This object is declared to be a temporary object, to be redrawn
					// continuously, so we need to ignore it: do nothing
					if (FGEPaintManager.paintPrimitiveLogger.isLoggable(Level.FINE)) {
						FGEPaintManager.paintPrimitiveLogger.fine("JConnectorView: buffering paint, ignore: " + connectorNode);
					}
				} else {
					if (FGEPaintManager.paintPrimitiveLogger.isLoggable(Level.FINE)) {
						FGEPaintManager.paintPrimitiveLogger.fine("JConnectorView: buffering paint, draw: " + connectorNode + " clip="
								+ g.getClip());
					}
					getNode().paint(graphics);
					super.paint(g);
				}
			} else {
				if (!getPaintManager().renderUsingBuffer((Graphics2D) g, g.getClipBounds(), connectorNode, getScale())) {
					getNode().paint(graphics);
					super.paint(g);
				}

				/*
				// Use buffer
				Image buffer = getPaintManager().getPaintBuffer();
				Rectangle localViewBounds = g.getClipBounds();
				Rectangle viewBoundsInDrawingView = GraphicalRepresentation.convertRectangle(getGraphicalRepresentation(), localViewBounds, getDrawingGraphicalRepresentation(), getScale());
				Point dp1 = localViewBounds.getLocation();
				Point dp2 = new Point(localViewBounds.x+localViewBounds.width-1,localViewBounds.y+localViewBounds.height-1);
				Point sp1 = viewBoundsInDrawingView.getLocation();
				Point sp2 = new Point(viewBoundsInDrawingView.x+viewBoundsInDrawingView.width-1,viewBoundsInDrawingView.y+viewBoundsInDrawingView.height-1);
				if (FGEPaintManager.paintPrimitiveLogger.isLoggable(Level.FINE))
					FGEPaintManager.paintPrimitiveLogger.fine("JConnectorView: use image buffer, copy area from "+sp1+"x"+sp2+" to "+dp1+"x"+dp2);
				g.drawImage(buffer,
						dp1.x,dp1.y,dp2.x,dp2.y,
						sp1.x,sp1.y,sp2.x,sp2.y,
						null);
				 */
			}
		} else {
			// Normal painting
			getNode().paint(graphics);
			super.paint(g);
		}

		// super.paint(g);
		// getGraphicalRepresentation().paint(g,getController());

		graphics.releaseGraphics();
	}

	@Override
	public AbstractDianaEditor<?, SwingViewFactory, JComponent> getController() {
		return controller;
	}

	/*	protected void handleNodeDeleted(NodeDeleted notification) {
			DrawingTreeNode<?, ?> deletedNode = notification.getDeletedNode();
			if (deletedNode == getNode()) {
				// If was not removed, try to do it now
				// TODO: is this necessary ???
				if (deletedNode != null && deletedNode.getParentNode() != null
						&& deletedNode.getParentNode().getChildNodes().contains(deletedNode)) {
					deletedNode.getParentNode().removeChild(deletedNode);
				}
				if (getController() instanceof DianaInteractiveViewer) {
					if (getNode() != null
							&& ((DianaInteractiveViewer<?, SwingViewFactory, JComponent>) getController()).getFocusedObjects().contains(
									getNode())) {
						((DianaInteractiveViewer<?, SwingViewFactory, JComponent>) getController()).removeFromFocusedObjects(getNode());
					}
					if (getNode() != null
							&& ((DianaInteractiveViewer<?, SwingViewFactory, JComponent>) getController()).getSelectedObjects().contains(
									getNode())) {
						((DianaInteractiveViewer<?, SwingViewFactory, JComponent>) getController()).removeFromSelectedObjects(getNode());
					}
				}
				// Now delete the view
				delete();
			}
		}*/

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (isDeleted) {
			logger.warning("Received notifications for deleted view " + evt);
			return;
		}

		if ((!evt.getPropertyName().equals(NodeDeleted.EVENT_NAME)) && getNode().isDeleted()) {
			logger.warning("Received notifications for deleted ConnectorNode " + evt);
			return;
		}

		// System.out.println("JConnectorView, received: "+aNotification);
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					propertyChange(evt);
				}
			});
		} else {
			if (evt.getPropertyName().equals(NodeDeleted.EVENT_NAME)) {
				delete();
			} else if (evt.getPropertyName().equals(ConnectorModified.EVENT_NAME)) {
				if (!getPaintManager().isTemporaryObjectOrParentIsTemporaryObject(connectorNode)) {
					getPaintManager().invalidate(connectorNode);
				}
				relocateAndResizeView();
				revalidate();
				getPaintManager().repaint(this);
			} /*else if (notification instanceof NodeDeleted) {
				handleNodeDeleted((NodeDeleted) notification);
				}*/else if (evt.getPropertyName().equals(GraphicalRepresentation.LAYER.getName())) {
				updateLayer();
				if (!getPaintManager().isTemporaryObjectOrParentIsTemporaryObject(connectorNode)) {
					getPaintManager().invalidate(connectorNode);
				}
				getPaintManager().repaint(this);
				/*if (getParentView() != null) {
					getParentView().revalidate();
					getPaintManager().repaint(this);
				}*/
			} else if (evt.getPropertyName().equals(DrawingTreeNode.IS_FOCUSED.getName())) {
				getPaintManager().repaint(this);
			} else if (evt.getPropertyName().equals(DrawingTreeNode.IS_SELECTED.getName())) {
				// TODO: ugly hack, please fix this, implement a ForceRepaint in FGEPaintManager
				if (connectorNode.getIsSelected()) {
					requestFocusInWindow();
				}
			} else if (evt.getPropertyName().equals(GraphicalRepresentation.TEXT.getName())) {
				updateLabelView();
			} else if (evt.getPropertyName().equals(GraphicalRepresentation.IS_VISIBLE.getName())) {
				updateVisibility();
				if (getPaintManager().isPaintingCacheEnabled()) {
					if (!getPaintManager().isTemporaryObjectOrParentIsTemporaryObject(connectorNode)) {
						getPaintManager().invalidate(connectorNode);
					}
				}
				getPaintManager().repaint(this);
			} else if (evt.getPropertyName().equals(ConnectorGraphicalRepresentation.APPLY_FOREGROUND_TO_SYMBOLS.getName())) {
				getPaintManager().repaint(this);
			} else if (evt.getPropertyName().equals(ObjectWillMove.EVENT_NAME)) {
				if (getPaintManager().isPaintingCacheEnabled()) {
					getPaintManager().addToTemporaryObjects(connectorNode);
					getPaintManager().invalidate(connectorNode);
				}
			} else if (evt.getPropertyName().equals(ObjectMove.PROPERTY_NAME)) {
				relocateView();
				if (getParentView() != null) {
					// getParentView().revalidate();
					getPaintManager().repaint(this);
				}
			} else if (evt.getPropertyName().equals(ObjectHasMoved.EVENT_NAME)) {
				if (getPaintManager().isPaintingCacheEnabled()) {
					getPaintManager().removeFromTemporaryObjects(connectorNode);
					getPaintManager().invalidate(connectorNode);
					getPaintManager().repaint(getParentView());
				}
			} else if (evt.getPropertyName().equals(ObjectWillResize.EVENT_NAME)) {
				if (getPaintManager().isPaintingCacheEnabled()) {
					getPaintManager().addToTemporaryObjects(connectorNode);
					getPaintManager().invalidate(connectorNode);
				}
			} else if (evt.getPropertyName().equals(ObjectResized.PROPERTY_NAME)) {
				relocateView();
				if (getParentView() != null) {
					// getParentView().revalidate();
					getPaintManager().repaint(this);
				}
			} else if (evt.getPropertyName().equals(ObjectHasResized.EVENT_NAME)) {
				if (getPaintManager().isPaintingCacheEnabled()) {
					getPaintManager().removeFromTemporaryObjects(connectorNode);
					getPaintManager().invalidate(connectorNode);
					getPaintManager().repaint(getParentView());
				}
			} else {
				// revalidate();
				if (!getPaintManager().isTemporaryObjectOrParentIsTemporaryObject(connectorNode)) {
					getPaintManager().invalidate(connectorNode);
				}
				getPaintManager().repaint(this);
			}
		}
	}

	@Override
	public JLabelView<O> getLabelView() {
		return labelView;
	}

	@Override
	public void activatePalette(DianaPalette<?, ?> aPalette) {
		if (aPalette instanceof JDianaPalette) {
			// A palette is registered, listen to drag'n'drop events
			setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, ((JDianaPalette) aPalette).buildPaletteDropListener(this,
					controller), true));
		}

	}

	@Override
	public FGEPaintManager getPaintManager() {
		return getDrawingView().getPaintManager();
	}

	@Override
	public String getToolTipText(MouseEvent event) {
		if (getController() instanceof DianaInteractiveViewer) {
			return ((DianaInteractiveViewer<?, SwingViewFactory, JComponent>) getController()).getToolTipText();
		}
		return super.getToolTipText(event);
	}

	@Override
	public void stopLabelEdition() {
		getLabelView().stopEdition();
	}

}
