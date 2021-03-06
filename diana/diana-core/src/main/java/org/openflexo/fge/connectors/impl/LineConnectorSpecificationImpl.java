package org.openflexo.fge.connectors.impl;

import java.util.logging.Logger;

import org.openflexo.fge.Drawing.ConnectorNode;
import org.openflexo.fge.connectors.LineConnectorSpecification;
import org.openflexo.fge.geom.FGEPoint;
import org.openflexo.fge.notifications.FGEAttributeNotification;

public abstract class LineConnectorSpecificationImpl extends ConnectorSpecificationImpl implements LineConnectorSpecification {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(LineConnectorSpecification.class.getPackage().getName());

	private FGEPoint cp1RelativeToStartObject;
	private FGEPoint cp2RelativeToEndObject;
	private LineConnectorType lineConnectorType = LineConnectorType.MINIMAL_LENGTH;

	// Used for deserialization
	public LineConnectorSpecificationImpl() {
		super();
	}

	@Override
	public LineConnectorType getLineConnectorType() {
		return lineConnectorType;
	}

	@Override
	public void setLineConnectorType(LineConnectorType aLineConnectorType) {
		FGEAttributeNotification notification = requireChange(LINE_CONNECTOR_TYPE, aLineConnectorType);
		if (notification != null) {
			lineConnectorType = aLineConnectorType;
			hasChanged(notification);
		}
	}

	@Override
	public FGEPoint getCp1RelativeToStartObject() {
		return cp1RelativeToStartObject;
	}

	@Override
	public void setCp1RelativeToStartObject(FGEPoint aPoint) {
		FGEAttributeNotification notification = requireChange(CP1_RELATIVE_TO_START_OBJECT, aPoint);
		if (notification != null) {
			this.cp1RelativeToStartObject = aPoint;
			hasChanged(notification);
		}
	}

	@Override
	public FGEPoint getCp2RelativeToEndObject() {
		return cp2RelativeToEndObject;
	}

	@Override
	public void setCp2RelativeToEndObject(FGEPoint aPoint) {
		FGEAttributeNotification notification = requireChange(CP2_RELATIVE_TO_END_OBJECT, aPoint);
		if (notification != null) {
			this.cp2RelativeToEndObject = aPoint;
			hasChanged(notification);
		}
	}

	@Override
	public ConnectorType getConnectorType() {
		return ConnectorType.LINE;
	}

	/*@Override
	public LineConnectorSpecification clone() {
		LineConnectorSpecification returned = (LineConnectorSpecification) cloneObject();
		returned.setLineConnectorType(getLineConnectorType());
		returned.setCp1RelativeToStartObject(getCp1RelativeToStartObject());
		returned.setCp2RelativeToEndObject(getCp2RelativeToEndObject());
		return returned;
	}*/

	@Override
	public LineConnector makeConnector(ConnectorNode<?> connectorNode) {
		return new LineConnector(connectorNode);
	}

}
