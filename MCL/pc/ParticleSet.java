
import java.awt.*;

/**
 * Represents a particle set for the particle filtering algorithm.
 * 
 * @author Lawrie Griffiths
 *
 */
public class ParticleSet {
	static short NUM_PARTICLES = 1000;
	Particle[] particles = new Particle[NUM_PARTICLES];
	Map map;
	
	/**
	 * Minimum distance from a wall where the particle is placed.
	 */
	public static final int BORDER = 20;
	
	/**
	 * Create a set of NUM_PARTICLES particles
	 * randomly distributed with the given map.
	 * 
	 * @param map the map of the enclosed environment
	 */
	public ParticleSet(Map map) {
		this.map = map;
		for(int i=0;i<NUM_PARTICLES;i++) {
			particles[i] = generateParticle();
		}
	}
	
	/**
	 * Generate a random particle within the maopped area.
	 *  
	 * @return the particle
	 */
	private Particle generateParticle() {
		float x,y, angle;
		Rectangle bound = map.getBoundingRect();
		Rectangle innerRect = new Rectangle(bound.x+BORDER, bound.y+BORDER, 
				bound.width-BORDER*2, bound.height-BORDER*2);
		
		// Generate x, y values in bounding recangle	
		for(;;) {
			x = innerRect.x + (((float) Math.random()) * innerRect.width);
			y = innerRect.y + (((float) Math.random()) * innerRect.height);
			Point p = new Point(x, y);
			
			if (map.inside(p)) {
				break;
			}
		}
		
		// Pick a random angle
		angle = ((float) Math.random()) * 360;
	
		return new Particle(new Pose(x,y,angle));
	}
	
	/**
	 * Return the number of particles in the set
	 * 
	 * @return the number of particles
	 */
	public int numParticles() {
		return NUM_PARTICLES;
	}
	
	/**
	 * Get a specific particle
	 * 
	 * @param i the index of the particle
	 * @return the particle
	 */
	public Particle getParticile(int i) {
		return particles[i];
	}
	
	/**
	 * Resample the set picking those with higher weights.
	 * 
	 * Note that the new set has multiple instances of the particles
	 * with higher weights.
	 *
	 */
	public void resample() {
		Particle[] oldParticles = new Particle[NUM_PARTICLES];
		//Make of a copy of the particles
		for(int i=0;i<NUM_PARTICLES;i++) {
			oldParticles[i] = particles[i];
		}
		
		// Continually pick a random number and select the particles with
		// weights greater than or equal to it until we have a full
		// set of particles.
		int count = 0;	
		while(count < NUM_PARTICLES) {
			float rand = (float) Math.random();
			for(int i=0;i<NUM_PARTICLES && count<NUM_PARTICLES;i++) {
				if (oldParticles[i].getWeight() >= rand) {
					// Create a new instance of the particle and set its weitht to zero
					particles[count] = new Particle(new Pose(oldParticles[i].getPose().x,oldParticles[i].getPose().y, oldParticles[i].getPose().angle));
					particles[count++].setWeight(0);
				}
			}
		}
	}
	
	/**
	 * Take range reeadings for each particle
	 *
	 */
	public void takeReadings() {
		for(int i=0;i<NUM_PARTICLES;i++) {
			particles[i].takeReadings(map);
		}
	}
	
	/**
	 * Calculate the weight for each particle
	 * 
	 * @param rr the robot range readings
	 */
	public void calculateWeights(Readings rr) {
		for(int i=0;i<NUM_PARTICLES;i++) {
			particles[i].calculateWeight(rr);
		}
	}
	
	/**
	 * Apply a move to each particle
	 * 
	 * @param move the move to apply
	 */
	public void applyMove(Move move) {
		for(int i=0;i<NUM_PARTICLES;i++) {
			particles[i].applyMove(move);
		}
	}
}
