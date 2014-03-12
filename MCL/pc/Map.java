
import java.awt.*;
import java.awt.geom.*;

/**
 * A map of a room or other closed environment, represented
 * by line segments
 * 
 * @author Lawrie Griffiths
 *
 */
public class Map {
	private Line[] lines;
	private Rectangle boundingRect;
	
	public static float PIXELS_PER_CM = 2f;
	
	/**
	 * Caculate the range of a robot to the nearest wall
	 * 
	 * @param pose the pose of the robot
	 * @return the range or -1 if not in range
	 */
	public float range(Pose pose) {
		Line l = pose.getRangeLine();
		Line rl = null;
		
		for(int i=0;i<lines.length;i++) {
			Point p = lines[i].intersectsAt(l);
			if (p == null) continue; // Does not intersect
			Line tl = new Line(pose.x, pose.y, p.x, p.y);
			
			// If the range line intersects more than one map line
			// then take the shortest distance.
			if (rl == null || tl.length() < rl.length()) rl = tl;
		}
		return (rl == null ? -1 : rl.length());
	}
	
	/**
	 * Create a map from an array of line segments and a bounding rectangle
	 * 
	 * @param lines the line segments
	 * @param boundingRect the bounding rectangle
	 */
	public Map(Line[] lines, Rectangle boundingRect) {
		this.lines = lines;
		this.boundingRect = boundingRect;
	}
	
	/**
	 * Draw the map using Line2D objects
	 * 
	 * @param g2d the Graphics2D object
	 */
	public void paint(Graphics2D g2d) {
		
		for(int i=0;i<lines.length;i++) {
			Line2D line = new Line2D.Float(lines[i].x1, lines[i].y1,
					                 lines[i].x2, lines[i].y2);
			g2d.draw(line);
		}
	}
	
	/**
	 * Check if a point is within the mapped area
	 * 
	 * @param p the Point
	 * @return true iff the point is with the mapped area
	 */
	boolean inside(Point p) {
		if (p.x < boundingRect.x || p.y < boundingRect.y) return false;
		if (p.x > boundingRect.x + boundingRect.width || 
		    p.y > boundingRect.y + boundingRect.height) return false;
		
		// Create a line from the point to the left
		Line l = new Line(p.x,p.y, p.x - boundingRect.width, p.y);
		
		// Count intersections
		int count= 0;		
		for(int i=0;i<lines.length;i++ ) {
			if (lines[i].intersectsAt(l) != null) count++;
		}
        // We are inside if the number of intersections is odd
		return count%2 == 1;
	}
	
	/**
	 * Return the bounding rectangle of the mapped area
	 * 
	 * @return the bounding rectangle
	 */
	public Rectangle getBoundingRect() {
		return boundingRect;
	}
}
