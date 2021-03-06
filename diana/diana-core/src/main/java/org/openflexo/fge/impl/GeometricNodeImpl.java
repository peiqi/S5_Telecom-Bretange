package org.openflexo.fge.impl;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.openflexo.fge.BackgroundStyle;
import org.openflexo.fge.Drawing.GeometricNode;
import org.openflexo.fge.ForegroundStyle;
import org.openflexo.fge.GRBinding;
import org.openflexo.fge.GeometricGraphicalRepresentation;
import org.openflexo.fge.ShapeGraphicalRepresentation;
import org.openflexo.fge.control.DianaEditor;
import org.openflexo.fge.cp.ControlArea;
import org.openflexo.fge.cp.ControlPoint;
import org.openflexo.fge.cp.GeometryAdjustingControlPoint;
import org.openflexo.fge.geom.FGEAbstractLine;
import org.openflexo.fge.geom.FGECircle;
import org.openflexo.fge.geom.FGECubicCurve;
import org.openflexo.fge.geom.FGEDimension;
import org.openflexo.fge.geom.FGEEllips;
import org.openflexo.fge.geom.FGEGeneralShape;
import org.openflexo.fge.geom.FGEGeneralShape.GeneralShapePathElement;
import org.openflexo.fge.geom.FGEGeometricObject.CardinalQuadrant;
import org.openflexo.fge.geom.FGEGeometricObject.Filling;
import org.openflexo.fge.geom.FGELine;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.fge.geom.FGEPolygon;
import org.openflexo.fge.geom.FGEPolylin;
import org.openflexo.fge.geom.FGEQuadCurve;
import org.openflexo.fge.geom.FGERectangle;
import org.openflexo.fge.geom.FGERoundRectangle;
import org.openflexo.fge.geom.FGESegment;
import org.openflexo.fge.geom.FGEShape;
import org.openflexo.fge.geom.area.FGEArea;
import org.openflexo.fge.geom.area.FGEPlane;
import org.openflexo.fge.geom.area.FGEQuarterPlane;
import org.openflexo.fge.graphics.FGEGeometricGraphics;
import org.openflexo.fge.notifications.GeometryModified;
import org.openflexo.toolbox.ConcatenedList;

public class GeometricNodeImpl<O> extends DrawingTreeNodeImpl<O, GeometricGraphicalRepresentation> implements GeometricNode<O> {

	private static final Logger logger = Logger.getLogger(GeometricNodeImpl.class.getPackage().getName());

	// protected FGEGeometricGraphicsImpl graphics;

	protected List<ControlPoint> controlPoints;

	// TODO: change to protected
	public GeometricNodeImpl(DrawingImpl<?> drawingImpl, O drawable, GRBinding<O, GeometricGraphicalRepresentation> grBinding,
			ContainerNodeImpl<?, ?> parentNode) {
		super(drawingImpl, drawable, grBinding, parentNode);
		// graphics = new FGEGeometricGraphicsImpl(this);
		startDrawableObserving();
	}

	@Override
	public boolean delete() {
		if (!isDeleted()) {
			stopDrawableObserving();
			super.delete();
			finalizeDeletion();
			return true;
		}
		return false;
	}

	private List<? extends ControlArea<?>> controlAreas = null;

	@Override
	public List<? extends ControlArea<?>> getControlAreas() {
		if (controlAreas == null) {
			List<ControlArea<?>> customControlAreas = getGRBinding().makeControlAreasFor(this);
			if (customControlAreas == null) {
				controlAreas = getControlPoints();
			} else {
				ConcatenedList<ControlArea<?>> concatenedControlAreas = new ConcatenedList<ControlArea<?>>();
				concatenedControlAreas.addElementList(getControlPoints());
				concatenedControlAreas.addElementList(customControlAreas);
				controlAreas = concatenedControlAreas;
			}
		}
		return controlAreas;
		// return getConnector().getControlAreas();
	}

	@Override
	public List<ControlPoint> getControlPoints() {
		if (controlPoints == null) {
			rebuildControlPoints();
		}
		return controlPoints;
	}

	/*@Override
	public FGEGeometricGraphicsImpl getGraphics() {
		return graphics;
	}*/

	@Override
	public int getViewX(double scale) {
		return getViewBounds(scale).x;
	}

	@Override
	public int getViewY(double scale) {
		return getViewBounds(scale).y;
	}

	@Override
	public int getViewWidth(double scale) {
		return getViewBounds(scale).width;
	}

	@Override
	public int getViewHeight(double scale) {
		return getViewBounds(scale).height;
	}

	@Override
	public Rectangle getViewBounds(double scale) {
		return getBounds(scale);
	}

	@Override
	public Rectangle getBounds(double scale) {
		return getParentNode().getViewBounds(scale);
		// return new Rectangle(0,0,1,1);
	}

	@Override
	public AffineTransform convertNormalizedPointToViewCoordinatesAT(double scale) {
		return AffineTransform.getScaleInstance(scale, scale);

		/*double width = getContainerGraphicalRepresentation().getViewWidth(scale);
		double height = getContainerGraphicalRepresentation().getViewHeight(scale);
		AffineTransform returned = AffineTransform.getScaleInstance(width,height);
		if (scale != 1) {
			returned.preConcatenate(AffineTransform.getScaleInstance(scale,scale));
		}
		return returned;*/
	}

	@Override
	public AffineTransform convertViewCoordinatesToNormalizedPointAT(double scale) {
		return AffineTransform.getScaleInstance(1 / scale, 1 / scale);

		/*double width = getContainerGraphicalRepresentation().getViewWidth(scale);
		double height = getContainerGraphicalRepresentation().getViewHeight(scale);
		AffineTransform returned = new AffineTransform();
		if (scale != 1) returned = AffineTransform.getScaleInstance(1/scale, 1/scale);
		returned.preConcatenate(AffineTransform.getScaleInstance(1/width,1/height));
		return returned;*/
	}

	@Override
	public FGERectangle getNormalizedBounds() {
		Rectangle bounds = getBounds(1);
		return new FGERectangle(0, 0, bounds.width, bounds.height, Filling.FILLED);
	}

	@Override
	public boolean isContainedInSelection(Rectangle drawingViewSelection, double scale) {
		FGERectangle drawingViewBounds = new FGERectangle(drawingViewSelection.getX(), drawingViewSelection.getY(),
				drawingViewSelection.getWidth(), drawingViewSelection.getHeight(), Filling.FILLED);
		boolean isFullyContained = true;
		/*	for (ControlPoint cp : getConnector().getControlPoints()) {
			Point cpInContainerView = convertLocalNormalizedPointToRemoteViewCoordinates(
					cp.getPoint(), 
					getDrawingGraphicalRepresentation(), 
					scale);
			FGEPoint preciseCPInContainerView = new FGEPoint(cpInContainerView.x,cpInContainerView.y);
			if (!drawingViewBounds.containsPoint(preciseCPInContainerView)) {
				//System.out.println("Going outside: point="+preciseCPInContainerView+" bounds="+containerViewBounds);
				isFullyContained = false;
			}
		}*/
		return isFullyContained;
	}

	@Override
	public void paint(FGEGeometricGraphics g) {
		/*if (!isRegistered()) {
			setRegistered(true);
		}*/

		// super.paint(g, controller);

		/*Graphics2D g2 = (Graphics2D) g;
		if (controller instanceof AbstractDianaEditor<?, ?, ?>) {
			graphics.createGraphics(g2, (AbstractDianaEditor<?, ?, ?>) controller);
		} else {
			logger.warning("Unsupported controller: " + controller);
		}*/

		g.setDefaultBackground(getGraphicalRepresentation().getBackground());
		g.setDefaultForeground(getGraphicalRepresentation().getForeground());
		g.setDefaultTextStyle(getGraphicalRepresentation().getTextStyle());

		if (getIsSelected() || getIsFocused()) {
			ForegroundStyle style = (ForegroundStyle) getGraphicalRepresentation().getForeground().clone();
			if (getIsSelected()) {
				style.setColorNoNotification(getDrawing().getRoot().getGraphicalRepresentation().getSelectionColor());
			} else if (getIsFocused()) {
				style.setColorNoNotification(getDrawing().getRoot().getGraphicalRepresentation().getFocusColor());
			}
			g.setDefaultForeground(style);
		}

		// System.out.println("Attempt to paint " + getGraphicalRepresentation().getGeometricObject() + " with " + g + " dtn=" +
		// g.getNode());

		paintGeometricObject(g);

		if (getIsSelected() || getIsFocused()) {
			Color color = null;
			if (getIsSelected()) {
				color = getDrawing().getRoot().getGraphicalRepresentation().getSelectionColor();
			} else if (getIsFocused()) {
				color = getDrawing().getRoot().getGraphicalRepresentation().getFocusColor();
			}
			for (ControlPoint cp : getControlPoints()) {
				cp.paint(g);
			}
		}

		if (hasFloatingLabel()) {
			g.useTextStyle(getGraphicalRepresentation().getTextStyle());
			g.drawString(getGraphicalRepresentation().getText(), new FGEPoint(getLabelRelativePosition().x
					+ getGraphicalRepresentation().getAbsoluteTextX(), getLabelRelativePosition().y
					+ getGraphicalRepresentation().getAbsoluteTextY()), getGraphicalRepresentation().getHorizontalTextAlignment());
		}

		// g.releaseGraphics();
	}

	@Override
	public void paintGeometricObject(FGEGeometricGraphics graphics) {
		getGraphicalRepresentation().getGeometricObject().paint(graphics);
	}

	protected FGEPoint getLabelRelativePosition() {
		if (getGraphicalRepresentation().getGeometricObject() instanceof FGEPoint) {
			return (FGEPoint) getGraphicalRepresentation().getGeometricObject();
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEAbstractLine) {
			FGEAbstractLine line = (FGEAbstractLine) getGraphicalRepresentation().getGeometricObject();
			return new FGESegment(line.getP1(), line.getP2()).getMiddle();
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEShape) {
			return ((FGEShape) getGraphicalRepresentation().getGeometricObject()).getCenter();
		}
		return new FGEPoint(0, 0);
	}

	protected List<ControlPoint> buildControlPointsForPoint(FGEPoint point) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();
		returned.add(new GeometryAdjustingControlPoint<FGEPoint>(this, "point", point.clone()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGEPoint) getGraphicalRepresentation().getGeometricObject()).x = newAbsolutePoint.x;
				((FGEPoint) getGraphicalRepresentation().getGeometricObject()).y = newAbsolutePoint.y;
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGEPoint geometricObject) {
				setPoint(geometricObject);
			}
		});
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForLine(FGEAbstractLine line) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();
		returned.add(new GeometryAdjustingControlPoint<FGELine>(this, "p1", line.getP1()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGELine) getGraphicalRepresentation().getGeometricObject()).setP1(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGELine geometricObject) {
				setPoint(geometricObject.getP1());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGELine>(this, "p2", line.getP2()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGELine) getGraphicalRepresentation().getGeometricObject()).setP2(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGELine geometricObject) {
				setPoint(geometricObject.getP2());
			}
		});
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForCurve(FGEQuadCurve curve) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();
		returned.add(new GeometryAdjustingControlPoint<FGEQuadCurve>(this, "p1", curve.getP1()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGEQuadCurve) getGraphicalRepresentation().getGeometricObject()).setP1(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGEQuadCurve geometricObject) {
				setPoint(geometricObject.getP1());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGEQuadCurve>(this, "cp", curve.getCtrlPoint()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGEQuadCurve) getGraphicalRepresentation().getGeometricObject()).setCtrlPoint(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGEQuadCurve geometricObject) {
				setPoint(geometricObject.getCtrlPoint());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGEQuadCurve>(this, "p3", curve.getP3()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGEQuadCurve) getGraphicalRepresentation().getGeometricObject()).setP3(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGEQuadCurve geometricObject) {
				setPoint(geometricObject.getP3());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGEQuadCurve>(this, "p2", curve.getP2()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGEQuadCurve) getGraphicalRepresentation().getGeometricObject()).setP2(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGEQuadCurve geometricObject) {
				setPoint(geometricObject.getP2());
			}
		});
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForCurve(FGECubicCurve curve) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();
		returned.add(new GeometryAdjustingControlPoint<FGECubicCurve>(this, "p1", curve.getP1()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGECubicCurve) getGraphicalRepresentation().getGeometricObject()).setP1(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGECubicCurve geometricObject) {
				setPoint(geometricObject.getP1());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGECubicCurve>(this, "cp1", curve.getCtrlP1()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGECubicCurve) getGraphicalRepresentation().getGeometricObject()).setCtrlP1(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGECubicCurve geometricObject) {
				setPoint(geometricObject.getCtrlP1());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGECubicCurve>(this, "cp2", curve.getCtrlP2()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGECubicCurve) getGraphicalRepresentation().getGeometricObject()).setCtrlP2(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGECubicCurve geometricObject) {
				setPoint(geometricObject.getCtrlP2());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGECubicCurve>(this, "p2", curve.getP2()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				((FGECubicCurve) getGraphicalRepresentation().getGeometricObject()).setP2(newAbsolutePoint);
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGECubicCurve geometricObject) {
				setPoint(geometricObject.getP2());
			}
		});
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForPolygon(FGEPolygon polygon) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();

		for (int i = 0; i < polygon.getPointsNb(); i++) {
			final int index = i;
			returned.add(new GeometryAdjustingControlPoint<FGEPolygon>(this, "p" + i, polygon.getPointAt(i)) {
				@Override
				public FGEArea getDraggingAuthorizedArea() {
					return new FGEPlane();
				}

				@Override
				public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration,
						FGEPoint newAbsolutePoint, FGEPoint initialPoint, MouseEvent event) {
					FGEPoint p = ((FGEPolygon) getGraphicalRepresentation().getGeometricObject()).getPointAt(index);
					p.x = newAbsolutePoint.x;
					p.y = newAbsolutePoint.y;
					setPoint(newAbsolutePoint);
					notifyGeometryChanged();
					return true;
				}

				@Override
				public void update(FGEPolygon geometricObject) {
					if (geometricObject == null) {
						return;
					}
					setPoint(geometricObject.getPointAt(index));
				}
			});
		}
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForPolylin(FGEPolylin polylin) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();

		for (int i = 0; i < polylin.getPointsNb(); i++) {
			final int index = i;
			returned.add(new GeometryAdjustingControlPoint<FGEPolylin>(this, "p" + i, polylin.getPointAt(i)) {
				@Override
				public FGEArea getDraggingAuthorizedArea() {
					return new FGEPlane();
				}

				@Override
				public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration,
						FGEPoint newAbsolutePoint, FGEPoint initialPoint, MouseEvent event) {
					FGEPoint p = ((FGEPolylin) getGraphicalRepresentation().getGeometricObject()).getPointAt(index);
					p.x = newAbsolutePoint.x;
					p.y = newAbsolutePoint.y;
					setPoint(newAbsolutePoint);
					notifyGeometryChanged();
					return true;
				}

				@Override
				public void update(FGEPolylin geometricObject) {
					setPoint(geometricObject.getPointAt(index));
				}
			});
		}
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForEllips(FGEEllips ellips) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();
		// TODO
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForRectangle(FGERectangle rectangle) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();
		returned.add(new GeometryAdjustingControlPoint<FGERectangle>(this, "northWest", rectangle.getNorthWestPt()) {
			private double initialWidth;
			private double initialHeight;

			@Override
			public void startDragging(DianaEditor<?> controller, FGEPoint startPoint) {
				initialWidth = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width;
				initialHeight = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height;
				setDraggingAuthorizedArea(FGEQuarterPlane.makeFGEQuarterPlane(((FGERectangle) getGraphicalRepresentation()
						.getGeometricObject()).getSouthEastPt(), CardinalQuadrant.NORTH_WEST));
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				FGEPoint pt = getNearestPointOnAuthorizedArea(newAbsolutePoint);
				setPoint(pt);

				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).x = pt.x;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).y = pt.y;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width = -pt.x + initialPoint.x + initialWidth;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height = -pt.y + initialPoint.y + initialHeight;

				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGERectangle geometricObject) {
				setPoint(geometricObject.getNorthWestPt());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGERectangle>(this, "northEast", rectangle.getNorthEastPt()) {
			private double initialWidth;
			private double initialHeight;

			@Override
			public void startDragging(DianaEditor<?> controller, FGEPoint startPoint) {
				initialWidth = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width;
				initialHeight = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height;
				setDraggingAuthorizedArea(FGEQuarterPlane.makeFGEQuarterPlane(((FGERectangle) getGraphicalRepresentation()
						.getGeometricObject()).getSouthWestPt(), CardinalQuadrant.NORTH_EAST));
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				FGEPoint pt = getNearestPointOnAuthorizedArea(newAbsolutePoint);
				setPoint(pt);

				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).y = pt.y;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width = pt.x - initialPoint.x + initialWidth;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height = -pt.y + initialPoint.y + initialHeight;

				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGERectangle geometricObject) {
				setPoint(geometricObject.getNorthEastPt());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGERectangle>(this, "southWest", rectangle.getSouthWestPt()) {
			private double initialWidth;
			private double initialHeight;

			@Override
			public void startDragging(DianaEditor<?> controller, FGEPoint startPoint) {
				initialWidth = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width;
				initialHeight = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height;
				setDraggingAuthorizedArea(FGEQuarterPlane.makeFGEQuarterPlane(((FGERectangle) getGraphicalRepresentation()
						.getGeometricObject()).getNorthEastPt(), CardinalQuadrant.SOUTH_WEST));
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				FGEPoint pt = getNearestPointOnAuthorizedArea(newAbsolutePoint);
				setPoint(pt);

				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).x = pt.x;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width = -pt.x + initialPoint.x + initialWidth;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height = pt.y - initialPoint.y + initialHeight;

				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGERectangle geometricObject) {
				setPoint(geometricObject.getSouthWestPt());
			}
		});
		returned.add(new GeometryAdjustingControlPoint<FGERectangle>(this, "southEast", rectangle.getSouthEastPt()) {
			private double initialWidth;
			private double initialHeight;

			@Override
			public void startDragging(DianaEditor<?> controller, FGEPoint startPoint) {
				initialWidth = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width;
				initialHeight = ((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height;
				setDraggingAuthorizedArea(FGEQuarterPlane.makeFGEQuarterPlane(((FGERectangle) getGraphicalRepresentation()
						.getGeometricObject()).getNorthWestPt(), CardinalQuadrant.SOUTH_EAST));
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				FGEPoint pt = getNearestPointOnAuthorizedArea(newAbsolutePoint);
				setPoint(pt);

				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).width = pt.x - initialPoint.x + initialWidth;
				((FGERectangle) getGraphicalRepresentation().getGeometricObject()).height = pt.y - initialPoint.y + initialHeight;

				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGERectangle geometricObject) {
				setPoint(geometricObject.getSouthEastPt());
			}
		});
		return returned;
	}

	protected List<ControlPoint> buildControlPointsForGeneralShape(FGEGeneralShape<?> shape) {
		Vector<ControlPoint> returned = new Vector<ControlPoint>();

		returned.add(new GeometryAdjustingControlPoint<FGEGeneralShape<?>>(this, "p0", shape.getPathElements().firstElement().getP1()) {
			@Override
			public FGEArea getDraggingAuthorizedArea() {
				return new FGEPlane();
			}

			@Override
			public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration, FGEPoint newAbsolutePoint,
					FGEPoint initialPoint, MouseEvent event) {
				FGEPoint p = ((FGEGeneralShape<?>) getGraphicalRepresentation().getGeometricObject()).getPathElements().firstElement()
						.getP1();
				p.x = newAbsolutePoint.x;
				p.y = newAbsolutePoint.y;
				setPoint(newAbsolutePoint);
				notifyGeometryChanged();
				return true;
			}

			@Override
			public void update(FGEGeneralShape<?> geometricObject) {
				if (geometricObject != null && geometricObject.getPathElements() != null && geometricObject.getPathElements().size() > 0) {
					setPoint(geometricObject.getPathElements().firstElement().getP1());
				}
			}
		});
		for (int i = 0; i < shape.getPathElements().size(); i++) {
			final int index = i;
			GeneralShapePathElement<?> element = shape.getPathElements().get(i);
			returned.add(new GeometryAdjustingControlPoint<FGEGeneralShape<?>>(this, "p" + i, element.getP2()) {
				@Override
				public FGEArea getDraggingAuthorizedArea() {
					return new FGEPlane();
				}

				@Override
				public boolean dragToPoint(FGEPoint newRelativePoint, FGEPoint pointRelativeToInitialConfiguration,
						FGEPoint newAbsolutePoint, FGEPoint initialPoint, MouseEvent event) {
					FGEPoint p = ((FGEGeneralShape<?>) getGraphicalRepresentation().getGeometricObject()).getPathElements().get(index)
							.getP2();
					p.x = newAbsolutePoint.x;
					p.y = newAbsolutePoint.y;
					((FGEGeneralShape<?>) getGraphicalRepresentation().getGeometricObject()).refresh();
					setPoint(newAbsolutePoint);
					notifyGeometryChanged();
					return true;
				}

				@Override
				public void update(FGEGeneralShape<?> geometricObject) {
					if (geometricObject != null && geometricObject.getPathElements() != null
							&& geometricObject.getPathElements().size() > 0) {
						setPoint(geometricObject.getPathElements().get(index).getP2());
					}
				}
			});

		}
		return returned;
	}

	private void updateControlPoints() {
		boolean cpNeedsToBeRebuilt = false;
		for (ControlPoint cp : controlPoints) {
			if (cp instanceof GeometryAdjustingControlPoint) {
				((GeometryAdjustingControlPoint) cp).update(getGraphicalRepresentation().getGeometricObject());
			} else {
				cpNeedsToBeRebuilt = true;
			}
		}
		if (cpNeedsToBeRebuilt) {
			rebuildControlPoints();
		}
	}

	@Override
	public List<ControlPoint> rebuildControlPoints() {
		if (controlPoints == null) {
			controlPoints = new Vector<ControlPoint>();
		}
		controlPoints.clear();

		if (getGraphicalRepresentation().getGeometricObject() == null) {
			return controlPoints;
		}

		if (getGraphicalRepresentation().getGeometricObject() instanceof FGEPoint) {
			controlPoints.addAll(buildControlPointsForPoint((FGEPoint) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEAbstractLine) {
			controlPoints.addAll(buildControlPointsForLine((FGEAbstractLine) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGERectangle) {
			controlPoints.addAll(buildControlPointsForRectangle((FGERectangle) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGERoundRectangle) {
			controlPoints.addAll(buildControlPointsForRectangle(((FGERoundRectangle) getGraphicalRepresentation().getGeometricObject())
					.getBoundingBox()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEEllips) {
			controlPoints.addAll(buildControlPointsForEllips((FGEEllips) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEPolygon) {
			controlPoints.addAll(buildControlPointsForPolygon((FGEPolygon) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEPolylin) {
			controlPoints.addAll(buildControlPointsForPolylin((FGEPolylin) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEQuadCurve) {
			controlPoints.addAll(buildControlPointsForCurve((FGEQuadCurve) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGECubicCurve) {
			controlPoints.addAll(buildControlPointsForCurve((FGECubicCurve) getGraphicalRepresentation().getGeometricObject()));
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEGeneralShape) {
			controlPoints.addAll(buildControlPointsForGeneralShape((FGEGeneralShape) getGraphicalRepresentation().getGeometricObject()));
		}

		// controlPoints.addAll(index, c)

		controlAreas = null;

		return controlPoints;
	}

	@Override
	public void notifyGeometryChanged() {
		updateControlPoints();
		setChanged();
		notifyObservers(new GeometryModified());
		// Hack: for the inspector !!!
		if (getGraphicalRepresentation().getGeometricObject() instanceof FGEPoint) {
			notifyChange("drawable.x");
			notifyChange("drawable.y");
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGELine) {
			notifyChange("drawable.x1");
			notifyChange("drawable.y1");
			notifyChange("drawable.x2");
			notifyChange("drawable.y2");
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGERectangle) {
			notifyChange("drawable.x");
			notifyChange("drawable.y");
			notifyChange("drawable.width");
			notifyChange("drawable.height");
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGECircle) {
			notifyChange("drawable.centerX");
			notifyChange("drawable.centerY");
			notifyChange("drawable.radius");
		} else if (getGraphicalRepresentation().getGeometricObject() instanceof FGEEllips) {
			notifyChange("drawable.x");
			notifyChange("drawable.y");
			notifyChange("drawable.width");
			notifyChange("drawable.height");
		}
	}

	// Hack: for the inspector !!!
	private void notifyChange(final String string) {
		// setChanged();
		// notifyObservers(new FGEAttributeNotification(string, null, null));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (temporaryIgnoredObservables.contains(evt.getSource())) {
			// System.out.println("IGORE NOTIFICATION " + notification);
			return;
		}

		super.propertyChange(evt);

		if (evt.getSource() == getGraphicalRepresentation()) {
			if (evt.getPropertyName().equals(GeometricGraphicalRepresentation.GEOMETRIC_OBJECT.getName())) {
				notifyGeometryChanged();
			}
		}

		if (evt.getSource() instanceof BackgroundStyle) {
			notifyAttributeChanged(GeometricGraphicalRepresentation.BACKGROUND, null, getGraphicalRepresentation().getBackground());
		}
		if (evt.getSource() instanceof ForegroundStyle) {
			notifyAttributeChanged(GeometricGraphicalRepresentation.FOREGROUND, null, getGraphicalRepresentation().getForeground());
		}

	}

	@Override
	public boolean hasContainedLabel() {
		return false;
	}

	@Override
	public boolean hasFloatingLabel() {
		return hasText();
	}

	@Override
	public FGEDimension getRequiredLabelSize() {
		return null;
	}

	@Override
	public ForegroundStyle getForegroundStyle() {
		return getPropertyValue(ShapeGraphicalRepresentation.FOREGROUND);
	}

	@Override
	public void setForegroundStyle(ForegroundStyle aValue) {
		setPropertyValue(ShapeGraphicalRepresentation.FOREGROUND, aValue);
	}

}
