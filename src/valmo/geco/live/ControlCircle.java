/**
 * Copyright (c) 2010 Simon Denier
 * Released under the MIT License (see LICENSE file)
 */
package valmo.geco.live;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * @author Simon Denier
 * @since Aug 27, 2010
 *
 */
public class ControlCircle {

	public static final int StrokeWidth = 3;

	public static final int ControlDiameter = 30;

	private String code;

	private Point position;
	
	private Point drawPosition;

	private String status;

	private StringBuilder labelBuffer;
	
	
	ControlCircle(String code, Point position) {
		this.code = code;
		this.position = position;
		this.drawPosition = new Point(position.x - ControlDiameter / 2, position.y - ControlDiameter / 2);
		resetStatus();
	}
	
	public String getCode() {
		return code;
	}

	public Point getPosition() {
		return position;
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void translate(int dx, int dy) {
		this.position.translate(dx, dy);
		this.drawPosition.translate(dx, dy);
	}

	public void drawOn(Graphics2D g2) {
		g2.setStroke(new BasicStroke(StrokeWidth));
		g2.setColor(getStatusColor());
		g2.drawOval(drawPosition.x, drawPosition.y, ControlDiameter, ControlDiameter);
		g2.drawString(getLabel(), drawPosition.x + ControlDiameter, drawPosition.y + ControlDiameter + 10);
	}

	private String getLabel() {
		if( labelBuffer==null ) {
			return code;
		} else {
			return labelBuffer + "-" + code;
		}
	}
	
	public Color getStatusColor() {
		if (status == "missed") {
			return Color.red;
		}
		if (status == "ok") {
			return Color.magenta;
		}
		if (status == "added") {
			return Color.blue;
		}
		return Color.magenta;
	}

	public void resetStatus() {
		this.status = "default";
		this.labelBuffer = null;
	}
	
	public void beOkControl(String order) {
		// default < added < ok < missed
		if( this.status!="missed" ) {
			this.status = "ok";
		}
		addLabel(order);
	}
	
	public void beMissedControl(String order) {
		// default < added < ok < missed
		this.status = "missed";
		addLabel(order);
	}

	public void beAddedControl() {
		// default < added < ok < missed
		if( this.status=="default" ) {
			this.status = "added";
		}
	}
	
	private void addLabel(String order) {
		if( labelBuffer==null ) {
			labelBuffer = new StringBuilder(order);
		} else {
			labelBuffer.append("/").append(order);
		}
	}

	
}
