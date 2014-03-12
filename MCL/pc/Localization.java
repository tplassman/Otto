
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import lejos.pc.comm.*;
import java.io.*;

public class Localization extends JPanel implements ActionListener {
	private Map map;
	private ParticleSet particles;
	private Readings readings = new Readings();
	private JButton weightButton, moveButton, stopButton;
	private NXTComm nxtComm;
	private DataOutputStream dos;
	private DataInputStream dis;
	
	// The amount the robot sticks out from its centre of rotation in centimetres
	private static final float ROBOT_SIZE = 12 * Map.PIXELS_PER_CM;

	// Command send to the NXT
	
	private static final byte ROTATE = 0;
	private static final byte FORWARD = 1;
	private static final byte RANGE = 2;
	private static final byte STOP = 3;
	
	// GUI Window size
	private static final int FRAME_WIDTH = 400;
	private static final int FRAME_HEIGHT = 800;
	
	/**
	 * Create a frame to display the panel in
	 * 
	 */
	private static JFrame openInJFrame(Container content,
	                                  int width,
	                                  int height,
	                                  String title,
	                                  Color bgColor) {
		JFrame frame = new JFrame(title);
	    frame.setBackground(bgColor);
	    content.setBackground(bgColor);
	    frame.setSize(width, height);
	    frame.setContentPane(content);
	    frame.addWindowListener(new WindowAdapter() {
	    	public void windowClosing(WindowEvent event) {
	    		System.exit(0);
	    	}	
	    });
	    frame.setVisible(true);
	    return(frame);
	}

	/**
	 * Paint the map and particles
	 */
	public void paintComponent(Graphics g) {
		clear(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.black);
		map.paint(g2d);
		g2d.setColor(Color.red);
		for(int i=0;i<particles.numParticles();i++) {
			particles.getParticile(i).getPose().paint(g2d);
		}
	}

	/**
	 * Clear the panel
	 */
	protected void clear(Graphics g) {
		super.paintComponent(g);
	}
	
	/**
	 * Create the GUI elements the map and the particle set and connect
	 * to the NXT.
	 */
	public Localization() {	
		//Create a map of the environment
		Line[] lines = {
				new Line(130,56,130,224),
				new Line(130,224,56,224),
				new Line(56,224,56,736),
				new Line(56,736,260,736),
				new Line(260,736,260,640),
				new Line(260,640,334,640),
				new Line(334,640,334,56),
				new Line(334,56,130,56)};
		
		map = new Map(lines, new Rectangle(56,56,278,686));
		
		// Create the particles
		particles = new ParticleSet(map);
		
		// Create some buttons
		weightButton = new JButton("Weight");
		add(weightButton);	
		weightButton.addActionListener(this);
		
		moveButton = new JButton("Move");
		add(moveButton);	
		moveButton.addActionListener(this);
		
		stopButton = new JButton("Stop");
		add(stopButton);	
		stopButton.addActionListener(this);
		
		//Connect to NXT
		connect();
	}
	
	/**
	 * Process buttons
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == weightButton) {
			// Get a set of robot range readings and use them
			// to apply weights to the particles.
			
			getReadings();
			readings.showReadings();
			
			// Ignore readings if one of them is out of range
			if (!readings.incomplete()) {
				particles.takeReadings();
				particles.calculateWeights(readings);
				particles.resample();
				repaint();
			}
		} else if (e.getSource() == moveButton) {		
			// Generate a random move
			Move move = Move.randomMove();
			
			float forwardRange = readings.getRange(1);
			
			//System.out.println("Forward range is " + forwardRange);
			//System.out.println("Move distance is " + move.distance);
			
			// Don't move forward if we are near the wall
			if (forwardRange > 0 && 
					move.distance + ParticleSet.BORDER + ROBOT_SIZE > forwardRange) {
				//System.out.println("Setting distance to zero");
				move.distance = 0f;
			}
			
			// Apply it to the robot
			applyMove(move);

			// Apply the move to the particles
			particles.applyMove(move);
			
			// repaint the panel
			repaint();
		} else if (e.getSource() == stopButton) {
			close();
		}
	}
	
	/**
	 * Connect to the NXT
	 *
	 */
	private void connect() {
		try {
			NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			
			NXTInfo[] nxtInfo = nxtComm.search("NOISY",NXTCommFactory.BLUETOOTH);
			
			if (nxtInfo.length == 0) {
				System.err.println("NO NXT found");
				System.exit(1);
			}
			
			if (!nxtComm.open(nxtInfo[0])) {
				System.err.println("Failed to open NXT");
				System.exit(1);
			}
			
			dis = new DataInputStream(nxtComm.getInputStream());
			dos = new DataOutputStream(nxtComm.getOutputStream());
			
		} catch (NXTCommException e) {
			System.err.println("NXTComm Exception: "  + e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Get range readings from the robot
	 *
	 */
	private void getReadings() {
		int rangeByte;
		float range;
		
		// Take forward reading
		rangeByte = (sendCommand(RANGE, 0f) & 0xFF);		
		if (rangeByte == 255) range = -1f;
		else range = ((float) rangeByte) * Map.PIXELS_PER_CM;	
		readings.setRange(1, range);
		
		// Move to the left
		sendCommand(ROTATE, -Readings.ANGLE);
		
		// Take left reading
		rangeByte = (sendCommand(RANGE, 0f) & 0xFF);		
		if (rangeByte == 255) range = -1f;
		else range = ((float) rangeByte) * Map.PIXELS_PER_CM;		
		readings.setRange(0, range);
		
		// Move to the right
		sendCommand(ROTATE, Readings.ANGLE*2);
		
		// Take right reading
		rangeByte = (sendCommand(RANGE, 0f) & 0xFF);		
		if (rangeByte == 255) range = -1f;
		else range = ((float) rangeByte) * Map.PIXELS_PER_CM;	
		readings.setRange(2, range);
		
		// Move back straight
		sendCommand(ROTATE, -Readings.ANGLE);
	}
	
	/**
	 * Apply the move to the robot
	 * 
	 */
	private void applyMove(Move move) {
		if (move.distance > 0f) {
			sendCommand(FORWARD, move.distance/Map.PIXELS_PER_CM);			
		}
		sendCommand(ROTATE,move.angle);
	}
	
	private byte sendCommand(byte command, float param) {
		try {
			dos.writeByte(command);
			dos.writeFloat(param);
			dos.flush();
			return dis.readByte();
		} catch (IOException ioe) {
			System.err.println("IO Exception");
			System.exit(1);
			return -1;
		}
	}
	/**
	 * Close down the program and the NXT
	 *
	 */
	private void close() {
		try {
			dos.writeByte(STOP);
			dos.writeFloat(0f);
			dos.flush();
			Thread.sleep(200);
			System.exit(0);
		} catch (Exception ioe) {
			System.err.println("IO Exception");
		}
	}

	public static void main(String[] args) {
		openInJFrame(new Localization(), FRAME_WIDTH, FRAME_HEIGHT, "Monte Carlo Localization", Color.white);
	}
}
