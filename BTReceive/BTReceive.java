
import lejos.nxt.*;
import lejos.nxt.addon.CompassSensor;
import lejos.nxt.comm.*;
import lejos.robotics.navigation.CompassPilot;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;

import java.io.*;

public class BTReceive {
	
	//static Pilot robot = new TachoPilot (5.6f, 12.0f, Motor.A, Motor.C);
	static UltrasonicSensor uss = new UltrasonicSensor(SensorPort.S1);
	static CompassSensor comp = new CompassSensor(SensorPort.S2);
	static CompassPilot robot = new CompassPilot (comp, 44f, 108f, Motor.A, Motor.C);
	static boolean run = true;
	static BTConnection btc;
	static DataInputStream dis;
	static DataOutputStream dos;
	static float heading;
	
	public static void main(String [] args)  throws Exception 
	{   
	    btc = Bluetooth.waitForConnection();    
	    
	    dis = btc.openDataInputStream();
		dos = btc.openDataOutputStream();
		
		int n;
		
		while(run)
		{
			heading = comp.getDegrees();
			n = dis.readInt();
			switch(n)
			{
			case 1:forward(); break;
			case 2:backward(); break;
			case 3:left(); break;
			case 4:right(); break;
			case 5:distance(); break;
			case 6:stop(); break;
			}
			//dos.writeInt(-n);
			//dos.flush();
		}
	}
	
	public static void distance() throws IOException
	{
		int temp = uss.getDistance();
		int temp1 = (int) comp.getDegrees();
		LCD.drawInt(temp1, 0, 0);
		LCD.drawInt(temp,0, 1);
		//LCD.drawInt((int)(temp1+(heading-45<0?360:0)>360+45?temp1+(heading-45<0?360:0)-360:temp1+(heading-45<0?360:0)), 0, 1);
		dos.writeInt(temp);
		dos.flush();
	}
	
	public static void left()
	{
		robot.rotate(-45);
		//robot.rotateTo((heading+45>359?heading+45-359:heading+45),true);
		//LCD.drawString("Left", 0, 0);
		//LCD.drawInt((int) heading, 0, 1);
		//float temp = comp.getDegrees();
		//while((heading+45<359?heading+45:heading+45-359)>(temp-(heading+45>359?359:0)<0-45?temp-(heading+45>359?359:0)+359:temp-(heading+45>359?359:0)))
		//{
			//LCD.drawInt((int)comp.getDegrees(), 0, 2);
			//robot.rotate(5);
			//temp = comp.getDegrees();
			//Thread.sleep(1000);
		//}
		//LCD.clear();
	}
	
	public static void right()
	{
		robot.rotate(45);
		//robot.rotateTo((heading-45<0?heading-45+359:heading-45),true);
		//LCD.drawString("Right", 0, 0);
		//LCD.drawInt((int) heading, 0, 1);
		//float temp = comp.getDegrees();
		//while((heading-45<0?heading-45+359:heading-45)<(temp+(heading-45<0?359:0)>360+45?temp+(heading-45<0?359:0)-359:temp+(heading-45<0?359:0)))
		//{
			//LCD.drawInt((int)temp+(heading-45<0?360:0), 0, 2);
			//robot.rotate(-5);
			//temp = comp.getDegrees();
			//Thread.sleep(1000);
		//}
		//LCD.clear();
	}
	
	public static void forward() throws InterruptedException
	{
		//LCD.drawString("Forward", 0, 0);
		robot.forward();
		Thread.sleep(500);
		robot.stop();
		//LCD.clear();
	}
	
	public static void backward() throws InterruptedException
	{
		//LCD.drawString("Backward", 0, 0);
		robot.backward();
		Thread.sleep(500);
		robot.stop();
		//LCD.clear();
	}
	
	public static void stop() throws IOException, InterruptedException
	{
		dis.close();
		dos.close();
		Thread.sleep(100); // wait for data to drain
		btc.close();
		run = false;
	}
}
