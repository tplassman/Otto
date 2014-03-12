
import java.awt.*;
import java.awt.geom.*;

/**
 * Represents a pose of the robot.
 * 
 * The pose is its x,y position and its heading angle.
 * 
 * @author Lawrie Griffiths
 *
 */
public class Pose {
	private static float MAX_RANGE_READING = 254f;
	private static final float MAXIMUM_RANGE = MAX_RANGE_READING * Map.PIXELS_PER_CM;
	private static final float ARROW_LENGTH = 10f;
	public float x, y, angle;
	
	public Pose(float x, float y, float angle) {
		this.x = x;
		this.y = y;
		this.angle = angle;
	}
	
	/**
	 * Generate a line representing the range reading
	 * 
	 * @return the line
	 */
	public Line getRangeLine() {
		return new Line(x, y, x + MAXIMUM_RANGE * (float) Math.cos(Math.toRadians(angle)),
				y + MAXIMUM_RANGE * (float) Math.sin(Math.toRadians(angle)));
	}
	
	private Line getArrowLine() {
		return new Line(x, y, x + ARROW_LENGTH * (float) Math.cos(Math.toRadians(angle)),
				y + ARROW_LENGTH * (float) Math.sin(Math.toRadians(angle)));
	}
	
	/**
	 * Paint the pose using Ellipse2D
	 * 
	 * @param g2d the Graphics2D object
	 */
	public void paint(Graphics2D g2d) {
		Ellipse2D c = new Ellipse2D.Float(x-1,y-1,2,2);
		Line rl = getArrowLine();
		Line2D l2d = new Line2D.Float(rl.x1,rl.y1,rl.x2,rl.y2);
		g2d.draw(l2d);
		g2d.draw(c);

	}
	
	/**
	 * Get the position of the robot as a Point
	 * 
	 * @return the Point
	 */
	public Point getPoint() {
		return new Point(x,y);
	}
	
	/**
	 * Get the heading angle of the robot
	 * 
	 * @return  the angle
	 */
	public float getAngle() {
		return angle;
	}
	
	/**
	 * Set the angle
	 * 
	 * @param angle the angle
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}
	
	/**
	 * Apply a move to the pose to calculate the new pose
	 * 
	 * @param move the move to apply
	 * 
	 */
	public void applyMove(Move move) {
		//System.out.println("Angle is " + angle);
		float ym = (move.distance * ((float) Math.sin(Math.toRadians(angle))));
		float xm = (move.distance * ((float) Math.cos(Math.toRadians(angle))));
		//System.out.println("Moving x = " + xm);
		//System.out.println("Moving y = " + ym);

		x += xm;
		y += ym;
		angle += move.angle;
		angle = angle % 360;
		//System.out.println("New angle is " + angle);
		
	}
}
