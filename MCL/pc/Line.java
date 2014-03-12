
import java.awt.geom.*;
import java.awt.*;

/**
 * Represents a line and supports calculating the point of 
 * intersection of two line segments.
 * 
 * @author Lawrie Griffiths
 *
 */
public class Line {
	public float x1, y1, x2, y2;
	
	public Line(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	/**
	 * Calculate the point of interscection of two lines.
	 * There must be a better way to do this calculation.
	 * 
	 * @param l the second line
	 * 
	 * @return the point of intersetion
	 */
	public Point intersectsAt(Line l) {	
		float x, y;
		if (y2 == y1 && l.y2 == l.y1) return null; // parallel
		if (x2 == x1 && l.x2 == l.x1) return null; // parallel	
		
		if (x1 == x2 && l.y1 == l.y2) {
			x = x1;
			y = l.y2;
		} else if (y1 == y2 && l.x1 == l.x2) {
			x = l.x1;
			y = y2;
		} else if (y2 == y1 || l.y2 == l.y1) {
			float a1 = (y2 - y1) / (x2 - x1);
			float b1 = y1 - a1*x1;

			float a2 = (l.y2 - l.y1) / (l.x2 - l.x1);
			float b2 = l.y1 - a2*l.x1;
			
			if (a1 == a2) return null; //parallel

			x = (b2 - b1) / (a1 - a2);
			y = a1 * x + b1; 

		} else {
			float a1 = (x2 - x1) / (y2 - y1);
			float b1 = x1 - a1*y1;

			float a2 = (l.x2 - l.x1) / (l.y2 - l.y1);
			float b2 = l.x1 - a2*l.y1;
			
			if (a1 == a2) return null; //parallel

			y = (b2 - b1) / (a1 - a2);
			x = a1 * y + b1; 
		}
		
		if (x1 <= x2) {
			if (x < x1 || x > x2) return null;
		} else {
			if (x < x2 || x > x1) return null;
		}
		if (y1 <= y2) {
			if (y < y1 || y > y2) return null;
		} else {
			if (y < y2 || y > y1) return null;
		}
		if (l.x1 <= l.x2) {
			if (x < l.x1 || x > l.x2) return null;
		} else {
			if (x < l.x2 || x > l.x1) return null;
		}
		if (l.y1 <= l.y2) {
			if (y < l.y1 || y > l.y2) return null;
		} else {
			if (y < l.y2 || y > l.y1) return null;
		}
		return new Point(x, y);
	}
	
	/**
	 * Return the length of the line
	 * 
	 * @return the length of the line
	 */
	public float length() {
		return (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	}
	
	/**
	 * Converts to a Line2D object and paints it on the
	 * panel.
	 * 
	 * @param g2d the Graphics2D object
	 */
	public void paint(Graphics2D g2d) {
		Line2D line = new Line2D.Float(x1, y1, x2,y2);
		g2d.draw(line);
	}
}
