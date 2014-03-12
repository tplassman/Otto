package otto;

import java.awt.geom.*;

public class Pose {
	public double x, y, angle, weight, reading;
	public int north, south, east, west;
	int particleReading = 0;
	
	public Pose(double x, double y, double angle, int north, int south, int east, int west) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.north = north;
		this.south = south;
		this.east = east;
		this.west = west;
	}
	
	public Point2D getPoint() {
		return new Point2D.Double(x,y);
	}
	
	public void setPoint(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getAngle() {
		return angle;
	}
	
	public void setAngle(double angle) {
		this.angle = angle;
	}
	
	public int getNorth() {
		return north;
	}
	
	public void setNorth(int north) {
		this.north = north;
	}
	
	public int getSouth() {
		return south;
	}
	
	public void setSouth(int south) {
		this.south = south;
	}
	
	public int getEast() {
		return east;
	}
	
	public void setEast(int east) {
		this.east = east;
	}
	
	public int getWest() {
		return west;
	}
	
	public void setWest(int west) {
		this.west = west;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public int getParticleReading() {
		return particleReading;
	}
	
	public void calculateWeight(double reading) 
	{
		if ((int)Math.toDegrees(angle) % 360 == 0){
			particleReading = east;
		}
		else if ((int)Math.toDegrees(angle) % 360 == 90 || (int)Math.toDegrees(angle) % 360 == -270){
			particleReading = north;
		}
		else if ((int)Math.toDegrees(angle) % 360 == 180 || (int)Math.toDegrees(angle) % 360 == -180){
			particleReading = west;
		}
		else if ((int)Math.toDegrees(angle) % 360 == 270 || (int)Math.toDegrees(angle) % 360 == -90)
		{
			particleReading = south;
		}
		else
			System.out.println("No weight.");
		double robotReading = reading*10;
		//double diff = Math.abs(robotReading-particleReading)/robotReading;
		//weight = 1/(1+diff);
		double diff = Math.sqrt(Math.abs((robotReading*robotReading)-(particleReading*particleReading)));
		weight = 1/(1+(diff/robotReading));
	}
	
	public void applyMove(int move)
	{
		double dist = 10;
		if (move == 1){
			setPoint(x+(dist*Math.cos(angle)), y-(dist*Math.sin(angle)));
			setNorth(north - (int)(dist*Math.sin(angle)));			
			setSouth(south + (int)(dist*Math.sin(angle)));
			setEast(east - (int)(dist*Math.cos(angle)));
			setWest(west + (int)(dist*Math.cos(angle)));
			}
		if (move == 2){
			setPoint(x-(dist*Math.cos(angle)), y+(dist*Math.sin(angle)));
			setNorth(north + (int)(dist*Math.sin(angle)));			
			setSouth(south - (int)(dist*Math.sin(angle)));
			setEast(east + (int)(dist*Math.cos(angle)));
			setWest(west - (int)(dist*Math.cos(angle)));
			}
		if (move == 3)
			setAngle(angle - Math.PI/4);
		if (move == 4)
			setAngle(angle + Math.PI/4);
	}
}
