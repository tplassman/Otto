
import java.util.*;

/**
 * Represents a particle for the particle filtering algorithm.
 * The state of the particle is the pose, which represents
 * a possible pose of the robot.
 * 
 * The weight for a particle is set by taking a set of range readings using a map of the environment,
 * and comparing these ranges with those taken by the robot. The weight represents the relative probability
 * that the robot has this pose. Weights are from 0 to 1.
 *  
 * @author Lawrie Griffiths
 *
 */
public class Particle {
	private Pose pose;
	private float weight;
	private Readings readings = new Readings();
	private Random rand = new Random();
	
	public Particle(Pose pose) {
		this.pose = pose;
		weight = 0;
	}
	
	/**
	 * Set the weight for this particle
	 * 
	 * @param weight the weight of this particle
	 */
	public void setWeight(float weight) {
		this.weight = weight;
	}
	
	/**
	 * Return the weight of this particle
	 * 
	 * @return the weight
	 */
	public float getWeight() {
		return weight;
	}
	
	/**
	 * Return the pose of this particle
	 * 
	 * @return the pose
	 */
	public Pose getPose() {
		return pose;
	}
	
	/**
	 * Take range readings for this particle
	 * 
	 * @param map the map of the environment
	 */
	public void takeReadings(Map map) {
		// Take some readings		
		readings.setRange(1,  map.range(pose));
		pose.setAngle(pose.getAngle() - Readings.ANGLE);
		readings.setRange(0,  map.range(pose));
		pose.setAngle(pose.getAngle() + Readings.ANGLE*2);
		readings.setRange(2,  map.range(pose));
		pose.setAngle(pose.getAngle() - Readings.ANGLE);	
	}
	
	/**
	 * Calculate the weight for this particle by comparing its 
	 * readings with the robot's readings
	 * 
	 * @param rr Robot readings
	 */
	public void calculateWeight(Readings rr) {
		weight = 1;
		for(int i=0;i<Readings.NUM_READINGS;i++) {
			float myReading = readings.getRange(i);
			float robotReading = rr.getRange(i);
			float diff = Math.abs((robotReading - myReading) / 5);
			float inverse = 1/(diff+1);
			weight *= inverse;
		}
	}
	
	/**
	 * Apply the robot's move to the particle with a bit of random
	 * noise
	 * 
	 * @param move the robot's move
	 */
	public void applyMove(Move move) {
		pose.angle += (float) 4*rand.nextGaussian();
		pose.x += (float) rand.nextGaussian();
		pose.y += (float) rand.nextGaussian();
		pose.applyMove(move);
	}
}
