
/**
 * Represents a set of range readings.
 * 
 * Currently three range readings are taken: one forward and one each at
 * 45 degrees to the left and right.
 * 
 * @author Lawrie Griffiths
 *
 */
public class Readings {
	public static final short NUM_READINGS = 3;
	public static final float ANGLE = 45f;
	private float[] ranges = new float[NUM_READINGS];
	
	/**
	 * Set a reading to a range
	 * 
	 * @param i the reading index
	 * @param range the rsange value
	 */
	public void setRange(int i, float range) {
		ranges[i] = range;
	}
	
	/**
	 * Get a specific range reading
	 * 
	 * @param i the reading index
	 * @return the range value
	 */
	public float getRange(int i) {
		return ranges[i];
	}
	
	/**
	 * Display the reading on System.out
	 *
	 */
	public void showReadings() {
		for(int i=0;i<NUM_READINGS;i++) {
			System.out.println("Reading " + i + " = " + ranges[i]);
		}
	}
	
	/**
	 * Return true if the readings are incomplete
	 * 
	 * @return true iff one of the readings is not valid
	 */
	public boolean incomplete() {
		for(int i=0;i<NUM_READINGS;i++) {
			if (ranges[i] < 0) return true;
		}
		return false;
	}
}
