import lejos.navigation.*;
import lejos.nxt.*;
import lejos.nxt.comm.*;
import java.io.*;

/***
 * Respond responds to commmands for the localization robot
 * from the PC.
 * 
 * @author Lawrie Griffiths
 *
 */
public class Respond {
	private static final byte ROTATE = 0;
	private static final byte FORWARD = 1;
	private static final byte RANGE = 2;
	private static final byte STOP = 3;
	
	public static void main(String[] args) throws Exception {
		BTConnection btc = Bluetooth.waitForConnection();
		DataInputStream dis = btc.openDataInputStream();
		DataOutputStream dos = btc.openDataOutputStream();
		Pilot robot = new Pilot(5.6f,16.0f,Motor.A, Motor.C,true);
		UltrasonicSensor sonic = new UltrasonicSensor(SensorPort.S1);
		
		while (true) {
			try {
				byte cmd = dis.readByte();
				
				LCD.drawInt(cmd,1,0,0);
				float param = dis.readFloat();
				LCD.drawInt((int) (param + 0.5f),4,0,1);
				
				switch (cmd) {
				case ROTATE: 
					robot.rotate((int) (param + 0.5f));
					dos.writeByte(0);
					break;
				case FORWARD: 
					robot.travel(param);
					dos.writeByte(0);
					break;
				case RANGE:
					dos.writeByte(sonic.getDistance());
					break;
				case STOP:
					System.exit(1);
				}
				dos.flush();
				
			} catch (IOException ioe) {
				System.err.println("IO Exception");
				Thread.sleep(2000);
				System.exit(1);
			}
		}
	}
}
