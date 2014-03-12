
/**
 * Represents a robot move consisting of a turn and a 
 * drive forward
 * 
 * Supports generating of a random move.
 * 
 * @author Lawrie Griffiths
 *
 */
public class Move {
	private static final short MAX_DISTANCE = 80;
	public float angle, distance;
	
	/**
	 * Create the move from an angle and a distance
	 * 
	 * @param angle the angle to turn
	 * @param distance the distance to travel
	 */
	public Move(float angle, float distance) {
		this.angle = angle;
		this.distance = distance;
	}
	
	/**
	 * Set the angle
	 * 
	 * @param angle the angle to turn
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
	 * Set the distance
	 * 
	 * @param distance
	 */
	public void setDistance(float distance) {
		this.distance = distance;
	}
	
	/**
	 * Generate a random move.
	 * 
	 * @return the generated move
	 */
	public static Move randomMove() {
		float a = (float) Math.random() * 360;
		float d = (float) Math.random() * MAX_DISTANCE;
		return new Move(a,d);
	}
}
