package otto;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lejos.pc.comm.NXTConnector;


public class otto extends JFrame implements KeyListener, MouseListener
{
	private static final long serialVersionUID = 8L;
	Container con = new Container();
	JPanel main = new JPanel();
	Image img;
	static int width = 44;
	static int length = 155;
	static int maxScreenW = 1400;
	static int maxScreenH = 630;
	int angle = 0;
	static int [][] map;
	static cell [][] env;
	boolean drawRobot = false;
	
	double radius1 = width/2;
	double radius2 = length;
	
	public static int startX;
	public static int startY;
	public static int endX;
	public static int endY;
	public static boolean goal = false;
	public static ArrayList <cell> open = new ArrayList <cell>();
	public static ArrayList <cell> close = new ArrayList <cell>();
	public static ArrayList <cell> path = new ArrayList <cell>();
	
	static NXTConnector conn;
	static DataOutputStream dos;
	static DataInputStream dis;
	
	static Point bearing;
	static int bear;
	static Point [] bearings = new Point[8];

	Point2D center = new Point2D.Double(120,200);
	Point2D p1 = new Point2D.Double(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
	Point2D p2 = new Point2D.Double(radius1*Math.cos((angle*Math.PI/4)+(Math.PI))+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
	Point2D p3 = new Point2D.Double(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
	
	Point2D temp1 = new Point2D.Double(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
	Point2D temp2 = new Point2D.Double(radius1*Math.cos((angle*Math.PI/4)+(Math.PI))+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
	Point2D temp3 = new Point2D.Double(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
	
	int numParticles = 50000;
	ArrayList <Pose> particles = new ArrayList <Pose>();
	//ArrayList <Pose> particles2 = new ArrayList <Pose>();
	//ArrayList <Pose> particles3 = new ArrayList <Pose>();
	int reading;
	Random rand = new Random();
	boolean drawParticles = false;
	//int[] robotReading1 = {0,0,0,0};
	//int[] robotReading2 = {0,0,0,0};
	//int[] robotReading3 = {0,0,0,0};
	
	Graphics2D g2;


	public static void main (String[] args) throws IOException
	{
		conn = new NXTConnector();
		boolean connected = conn.connectTo("btspp://");
		
		if (!connected) {
			System.err.println("Failed to connect to any NXT");
		}
		else{
		dos = conn.getDataOut();
		dis = conn.getDataIn();
		}
		
		bearings[0] = new Point (1,-1);
		bearings[1] = new Point (1,0);
		bearings[2] = new Point (1,1);
		bearings[3] = new Point (0,1);
		bearings[4] = new Point (-1,1);
		bearings[5] = new Point (-1,0);
		bearings[6] = new Point (-1,-1);
		bearings[7] = new Point (0,-1);
			
		bearing = bearings[7];
		bear = 7;
		
		map=environment.getMap();
		env = new cell [map.length][map[0].length];
		setEnv(1);
		new otto();
	}
	
	public static void setEnv(int run)
	{
		for(int i=0; i<map.length; i++)
			for(int j=0; j<map[0].length; j++)
			{
				if(run==1)
					env[i][j]=new cell(i,j,(map[i][j]==2?true:false),(map[i][j]==3?true:false),(map[i][j]==1?true:false));
				
				if(env[i][j].isStart())
				{
					startX = i;
					startY = j;
				}
				if(env[i][j].isEnd())
				{
					endX = i;
					endY = j;
				}
			}
	}
	
	public otto()
	{
		super("Otto");
		setSize(new Dimension(1430,700));
		con=getContentPane();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBackground(Color.BLACK);
		addKeyListener(this);
		main=new JPanel();
		main.setSize(new Dimension(1430,700));
		con.add(main);
		con.addMouseListener(this);
		setVisible(true);
	}

	public void paint(Graphics g)
	{
		img=main.createImage(main.getWidth(),main.getHeight());
		g2 = (Graphics2D)img.getGraphics();
		
		g2.setColor(Color.DARK_GRAY);
		for (int i=20; i<1410; i+=10)
			g2.draw(new Line2D.Double(i,10,i,640));
		
		for (int i=20; i<640; i+=10)
			g2.draw(new Line2D.Double(10,i,1400,i));

		for(int i=0; i<env.length; i++)
			for(int j=0; j<env[i].length; j++)
			{
				g2.setColor(Color.RED);
				if(env[i][j].isWall() && env[i][j].isVisible())
					g2.fillRect(i*10,j*10,10,10);
				g2.setColor(Color.ORANGE);
				if(env[i][j].isEnd())
					g2.fillRect(i*10,j*10,10,10);
			}
		
		for(int i=0; i<path.size(); i++)
		{
			g2.setColor(Color.YELLOW);
			g2.fillRect(path.get(i).getX()*10,path.get(i).getY()*10,10,10);
		}

	if (drawParticles)
			for (int k = 0; k < particles.size(); k++) 
			{
				double x = particles.get(k).getPoint().getX();
				double y = particles.get(k).getPoint().getY();
				Line2D r = new Line2D.Double(x, y, x + (20*Math.cos(particles.get(k).getAngle())), y - (20*Math.sin(particles.get(k).getAngle())));
				Ellipse2D c = new Ellipse2D.Double(x-5, y-5, 10, 10);
				g2.setColor(Color.green);
				g2.draw(c);
				g2.setColor(Color.blue);
				g2.draw(r);
			}
	if(drawRobot)
	{
		g2.setColor(Color.RED);
		g2.draw(new Line2D.Double(p1,p2));
		g2.draw(new Line2D.Double(p2,p3));
		g2.draw(new Line2D.Double(p3,p1));
	}

		main.getGraphics().drawImage(img,0,0,this);
	}

	public void generateParticles()
	{
		// Create poses
		//int[] w = new int[env.length];
		//int[] h = new int[env[0].length];
		//for (int i = 0; i < env.length ; i++) 
		//	w[i] = i;
		//for (int j = 0; j < env[0].length; j++)
		//	h[j] = j;		
		double cellX;
		double cellY;
		double head;
		int north, south, east, west;
		
		for (int i = 0; i < env.length; i++)
		{
			for (int j = 0; j < env[0].length; j++)
			{
				if(!env[i][j].isWall())
				{
					cellX = (env[i][j].getX()*10)+5;
					cellY = (env[i][j].getY()*10)+5;
					head = 0;
					north = env[i][j].getNorth();
					south = env[i][j].getSouth();
					east = env[i][j].getEast();
					west = env[i][j].getWest();
					particles.add(new Pose(cellX, cellY, head, north, south, east, west));
					
					cellX = (env[i][j].getX()*10)+5;
					cellY = (env[i][j].getY()*10)+5;
					head = Math.PI/2;
					north = env[i][j].getNorth();
					south = env[i][j].getSouth();
					east = env[i][j].getEast();
					west = env[i][j].getWest();
					particles.add( new Pose(cellX, cellY, head, north, south, east, west));
					
					cellX = (env[i][j].getX()*10)+5;
					cellY = (env[i][j].getY()*10)+5;
					head = Math.PI;
					north = env[i][j].getNorth();
					south = env[i][j].getSouth();
					east = env[i][j].getEast();
					west = env[i][j].getWest();
					particles.add(new Pose(cellX, cellY, head, north, south, east, west));
					
					cellX = (env[i][j].getX()*10)+5;
					cellY = (env[i][j].getY()*10)+5;
					head = 3*Math.PI/2;
					north = env[i][j].getNorth();
					south = env[i][j].getSouth();
					east = env[i][j].getEast();
					west = env[i][j].getWest();
					particles.add(new Pose(cellX, cellY, head, north, south, east, west));
				}
			}
		}
		/*while (ii < particles.length)
		{
			int wTemp = w[rand.nextInt(env.length)];
			int hTemp = h[rand.nextInt(env[0].length)];
			if(!env[wTemp][hTemp].isWall())
			{
				double cellX = (env[wTemp][hTemp].getX()*10)+5;
				double cellY = (env[wTemp][hTemp].getY()*10)+5;
				double head = card[rand.nextInt(card.length)];
				int north = env[wTemp][hTemp].getNorth();
				int south = env[wTemp][hTemp].getSouth();
				int east = env[wTemp][hTemp].getEast();
				int west = env[wTemp][hTemp].getWest();
				particles[ii] = new Pose(cellX, cellY, head, north, south, east, west);
				ii++;
			}
		}*/
	}

	public void fixedMove()
	{
		int randMove = 3;
		
		if(randMove == 3 || randMove == 4)
		{
			try {
				dos.writeInt(randMove);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 2");
				e1.printStackTrace();
			}
		}
		try {
			dos.writeInt(randMove);
			dos.flush();
		} catch (IOException e1) {
			System.out.println("Error 2");
			e1.printStackTrace();
		}
		
		if(randMove == 3 | randMove == 4)
			for (int i=0; i<particles.size(); i++)
				particles.get(i).applyMove(randMove);

		for (int i=0; i<particles.size(); i++)
			particles.get(i).applyMove(randMove);
		
		repaint();
		update(g2);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {e.printStackTrace();}
	}

	public void fixedMove2()
	{
		int randMove = 1;
		
		if(randMove == 3 || randMove == 4)
		{
			try {
				dos.writeInt(randMove);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 2");
				e1.printStackTrace();
			}
		}
		try {
			dos.writeInt(randMove);
			dos.flush();
		} catch (IOException e1) {
			System.out.println("Error 2");
			e1.printStackTrace();
		}
		
		if(randMove == 3 | randMove == 4)
			for (int i=0; i<particles.size(); i++)
				particles.get(i).applyMove(randMove);

		for (int i=0; i<particles.size(); i++)
			particles.get(i).applyMove(randMove);
		
		repaint();
		update(g2);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {e.printStackTrace();}
	}
	
	public void applyMove()
	{
	/*	
	int move = 4;
	
	for (int i=0; i<4; i++)
	{
		try {
			dos.writeInt(5);
			dos.flush();
			reading = dis.readInt(); //this is an int not a float
			System.out.println("Distance is: "+reading);
		} catch (IOException e1) {
			System.out.println("Distance");
			e1.printStackTrace();
		}
		i--;
		if (reading != 259){
			robotReading1[i] = reading;

			try {
				dos.writeInt(move);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 2");
				e1.printStackTrace();
			}
			i++;
		}*/
	
	
	
	
		int[] move = {1, 2, 3, 4};
		int randMove = move[rand.nextInt(move.length)];
		System.out.println("move is: " +randMove);
		
		if(randMove == 3 || randMove == 4 ||randMove == 1 ||randMove == 2)
		{
			try {
				dos.writeInt(randMove);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 2");
				e1.printStackTrace();
			}
		}
		try {
			dos.writeInt(randMove);
			dos.flush();
		} catch (IOException e1) {
			System.out.println("Error 2");
			e1.printStackTrace();
		}
		
		if(randMove == 3 | randMove == 4 ||randMove == 1 ||randMove == 2)
			for (int i=0; i<particles.size(); i++)
				particles.get(i).applyMove(randMove);

		for (int i=0; i<particles.size(); i++)
			particles.get(i).applyMove(randMove);
		
		repaint();
		update(g2);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {e.printStackTrace();}
		
	}

	public void takeReading()
	{
		try {
			dos.writeInt(5);
			dos.flush();
			reading = dis.readInt(); //this is an int not a float
			System.out.println(reading);
		} catch (IOException e1) {
			System.out.println("Distance");
			e1.printStackTrace();
		}
	}
	
	public void calculateWeight()
	{
		for (int i=0; i<particles.size(); i++)
			particles.get(i).calculateWeight((double)reading);
	}

	public void resample()
	{
		Pose[] oldParticles = new Pose[particles.size()];
		//Make of a copy of the particles
		for(int i=0;i<particles.size();i++) {
			oldParticles[i] = particles.get(i);
		}
		// Continually pick a random number and select the particles with
		// weights greater than or equal to it until we have a full
		// set of particles.
		int count = 0;
		particles.clear();
		while(count < oldParticles.length) {
			//double randNum = Math.random();
			System.out.println(oldParticles[0].getWeight());
			double randNum =  0.10 + Math.random() * 0.90;
			for(int i=0;i<oldParticles.length && count<oldParticles.length;i++) {
				if (oldParticles[i].getNorth() > 0 && oldParticles[i].getEast() > 0 && oldParticles[i].getSouth() > 0 && oldParticles[i].getWest() > 0 && oldParticles[i].getWeight() >= randNum) 
				{
					System.out.println("Particle created");
					// Create a new instance of the particle and set its weight to zero
					particles.add(new Pose(oldParticles[i].getPoint().getX(),oldParticles[i].getPoint().getY(), oldParticles[i].getAngle(), oldParticles[i].getNorth(), oldParticles[i].getSouth(), oldParticles[i].getEast(), oldParticles[i].getWest()));
					particles.get(count).setWeight(0);
					count++;
				}
			}
		}
	}
	
	/*public void compareParticles()
	{
		double lowError = 0.8;
		double highError = 1.2;
		for(int i=0;i<particles.size();i++) {
			if (robotReading1[0]>lowError*particles.get(i).getNorth() && robotReading1[0]<highError*particles.get(i).getNorth()) 
				if (robotReading1[1]>lowError*particles.get(i).getEast() && robotReading1[1]<highError*particles.get(i).getEast()) 
					if (robotReading1[2]>lowError*particles.get(i).getSouth() && robotReading1[2]<highError*particles.get(i).getSouth()) 
						if (robotReading1[3]>lowError*particles.get(i).getWest() && robotReading1[3]<highError*particles.get(i).getWest())
							particles2.add(new Pose(particles.get(i).getPoint().getX(),particles.get(i).getPoint().getY(), particles.get(i).getAngle(), particles.get(i).getNorth(), particles.get(i).getSouth(), particles.get(i).getEast(), particles.get(i).getWest()));
		}
	}
	
	public void compareParticles2()
	{
		double lowError = 0.8;
		double highError = 1.2;
		for(int i=0;i<particles2.size();i++) {
			if (robotReading1[0]>lowError*particles2.get(i).getNorth() && robotReading1[0]<highError*particles2.get(i).getNorth()) 
				if (robotReading1[1]>lowError*particles2.get(i).getEast() && robotReading1[1]<highError*particles2.get(i).getEast()) 
					if (robotReading1[2]>lowError*particles2.get(i).getSouth() && robotReading1[2]<highError*particles2.get(i).getSouth()) 
						if (robotReading1[3]>lowError*particles2.get(i).getWest() && robotReading1[3]<highError*particles2.get(i).getWest())
							particles3.add(new Pose(particles2.get(i).getPoint().getX(),particles2.get(i).getPoint().getY(), particles2.get(i).getAngle(), particles2.get(i).getNorth(), particles2.get(i).getSouth(), particles2.get(i).getEast(), particles2.get(i).getWest()));
		}
	}
	
	
	public static int getMaxValue(int[] numbers){   
		int maxValue = numbers[0];   
		for(int i=0;i < numbers.length-1;i++){   
		   if(numbers[i] > maxValue){   
		     maxValue = numbers[i];   
		   }   
		}   
		return maxValue;   
	}
	
	public static int getMaxLoc(int[] numbers, int value){   
		int maxLoc = 0;   
		for(int i=0;i < numbers.length-1;i++){   
		   if(numbers[i] == value){   
		     maxLoc = i;;   
		   }   
		}   
		return maxLoc;   
	}
	
	public void driveForward()
	{
		int maxDist = getMaxValue(robotReading1);
		int maxLoc = getMaxLoc(robotReading1, maxDist);
		
		if (maxLoc == 1)
		{
			for(int i=0;i<maxDist-10;i++)
			{
				up();
			}
		}
		else if (maxLoc == 2)
		{
			right();
			right();
			for(int i=0;i<maxDist-10;i++)
			{
				up();
			}
		}
		else if (maxLoc == 3)
			for(int i=0;i<maxDist-10;i++)
			{
				down();
			}
		else 
		{
			left();
			left();
			for(int i=0;i<maxDist-10;i++)
			{
				up();
			}
		}
	}*/
		
	public boolean sameParticles()
	{
		int count = 1;
        for(int j = 1; j < particles.size(); j++) {
        	if(particles.get(0).getPoint().getX() == particles.get(j).getPoint().getX() && particles.get(0).getPoint().getY() == particles.get(j).getPoint().getY() && particles.get(0).getAngle() == particles.get(j).getAngle())
        		count++;
	    }
	    
        System.out.println("# of same particles: "+count);
        System.out.println("# of particles: "+particles.size());

	    if(count == particles.size())
	    	return true;
	    return false;
	}

	public void solve()
	{	
		setEnv(2);
		setBlock();
		
		for(int i=0; i<env.length; i++)
			for(int j=0; j<env[i].length; j++)
				env[i][j].setDistances(env);

		takeReading();
		System.out.println("reading is: " +reading);

		generateParticles();
		drawParticles = true;
		repaint();
		
		int turnCount = 0;
		while (turnCount < 4)
		{
			fixedMove();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {e.printStackTrace();}
			takeReading();
			while (reading > 256)
			{
				takeReading();
				System.out.println("reading is: " +reading);
			}
			calculateWeight();
			resample();
			repaint();
			turnCount++;
		}
		System.out.println("Apply Move");
		
		while (!sameParticles())
		{
			applyMove();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {e.printStackTrace();}
			takeReading();
			System.out.println("reading is: " +reading);
			System.out.println("Angle is: " +particles.get(0).getAngle());
			System.out.println("North is: " +particles.get(0).getNorth());
			System.out.println("East is: " +particles.get(0).getEast());
			System.out.println("South is: " +particles.get(0).getSouth());
			System.out.println("West is: " +particles.get(0).getWest());
			while (reading > 256)
			{
				takeReading();
				System.out.println("reading is: " +reading);
			}
			if (reading < 257)
			{
				calculateWeight();
				System.out.println("Weight is: " +particles.get(0).getWeight());
				System.out.println("particleReading is: " +particles.get(0).particleReading);
				resample();
				repaint();
			}
		}
		System.out.println("End location");
		startX = (int)particles.get(0).getPoint().getX()/10;
		startY = (int)particles.get(0).getPoint().getY()/10;
		
		System.out.println("X: "+startX+" Y: "+startY);
		
		center.setLocation(startX,startY);
		System.out.println(center);
		p1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
		p2.setLocation(radius1*Math.cos((angle*Math.PI/4)+(Math.PI))+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
		p3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());

		drawRobot = true;

		env[startX][startY].setStart();

		env[startX][startY].calculateAdjacencies(env);
		env[startX][startY].setF(env[startX][startY],env[endX][endY]);
		env[startX][startY].setParent(env[startX][startY]);
		env[endX][endY].setParent(env[endX][endY]);
		
		double lowF;
		int lowPos=0;
		int currentX = startX;
		int currentY = startY;
		open.add(env[startX][startY]);
		
		while(!goal)
		{
			lowF=open.get(0).getF();
			
			for(int i=0;i<open.size();i++)
			{
				if(open.get(i).getF()<=lowF)
				{
					lowPos = i;
					lowF = open.get(i).getF();
					currentX = open.get(i).getX();
					currentY = open.get(i).getY();
				}
			}

			if(open.get(lowPos).isEnd())
				goal = true;
			else
			{
				open.remove(env[currentX][currentY]);
				close.add(env[currentX][currentY]);
				
				for(int i=0; i<env[currentX][currentY].getAdjacencies().size(); i++)
				{
					if(!close.contains(env[currentX][currentY].getAdjacencies().get(i)))
					{
						if(open.contains(env[currentX][currentY].getAdjacencies().get(i)))
						{
							if(env[currentX][currentY].getAdjacencies().get(i).getG()>(env[currentX][currentY].getG()+env[currentX][currentY].getAdjacencies().get(i).calcG(env[currentX][currentY])))
							{
								env[currentX][currentY].getAdjacencies().get(i).setG(env[currentX][currentY].getG()+env[currentX][currentY].getAdjacencies().get(i).calcG(env[currentX][currentY]));
								env[currentX][currentY].getAdjacencies().get(i).setParent(env[currentX][currentY]);
								env[currentX][currentY].getAdjacencies().get(i).setF();
							}
						}
						else
						{
							env[currentX][currentY].getAdjacencies().get(i).calculateAdjacencies(env);
							open.add(env[currentX][currentY].getAdjacencies().get(i));
							env[currentX][currentY].getAdjacencies().get(i).setG(env[currentX][currentY].getG()+env[currentX][currentY].getAdjacencies().get(i).calcG(env[currentX][currentY]));
							env[currentX][currentY].getAdjacencies().get(i).setH(env[endX][endY]);
							env[currentX][currentY].getAdjacencies().get(i).setF();
							env[currentX][currentY].getAdjacencies().get(i).setParent(env[currentX][currentY]);
						}
					}
				}
			}
		}
		
		cell nextParent = open.get(lowPos).getParent();
		path.add(env[endX][endY]);
		while(!nextParent.isStart())
		{
			path.add(nextParent);
			nextParent = nextParent.getParent();
		}
		//path.add(env[startX][startY]);
		
		boolean done = false;
		boolean left = true;
		boolean right = true;
		while(!done)
		{
			left = true;
			right = true;
			while(!correctHeading(path.get(path.size()-1)))
			{
				System.out.println("Current: "+(int)center.getX()+" "+(int)center.getY());
				System.out.println("Next: "+path.get(path.size()-1));
				System.out.println("Bearing: "+bearing);
				if(left)
					if(left())
					{
						repaint();
						update(g2);
					}
					else
						left = false;
				if(!left)
					if(right())
					{
						repaint();
						update(g2);
					}
					else
						right = false;
				if(!left && !right)
					System.out.println("Stuck");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {e.printStackTrace();}
			}
			up();
			repaint();
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {e.printStackTrace();}
			
			path.remove(path.size()-1);
			System.out.println(path);
			
			if(path.size() == 0)
				done = true;
		}		
	}
	
	public boolean correctHeading(cell next)
	{
		if(next.getX()==(int)center.getX()+bearing.getX() && next.getY()==(int)center.getY()+bearing.getY())
			return true;
		return false;
	}

	public void setBlock()
	{
		for(int i=0; i<env.length; i++)
		{
			for(int j=0; j<env[i].length; j++)
			{
				if(env[i][j].isWall())
				{
					for(int l=1; l<(width/10)/2+1; l++)
						try{env[i][j+l].setBlock(true);}
						catch(IndexOutOfBoundsException Exception){}
			
					for(int l=1; l<(width/10)/2+1; l++)
						try{env[i][j-l].setBlock(true);}
						catch(IndexOutOfBoundsException Exception){}

					for(int l=1; l<(length/10)/2+1; l++)
						try{env[i+l][j].setBlock(true);}
						catch(IndexOutOfBoundsException Exception){}

					for(int l=1; l<(length/10)/2+1; l++)
						try{env[i-l][j].setBlock(true);}
						catch(IndexOutOfBoundsException Exception){}
				}
			}
		}
	}
	
	public void antiWallCheck(int x, int y)
	{
		for(int i=1; i<width/10+1; i++)
		{
			try{if(!env[x][y+i].isVisible())
				env[x][y+i].setWall(true);
			else
				break;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		
		for(int i=1; i<width/10+1; i++)
		{
			try{if(!env[x][y-i].isVisible())
				env[x][y-i].setWall(true);
			else
				break;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		
		for(int i=1; i<length/10+1; i++)
		{
			try{if(!env[x+i][y].isVisible())
				env[x+i][y].setWall(true);
			else
				break;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		
		for(int i=1; i<length/10+1; i++)
		{
			try{if(!env[x-i][y].isVisible())
				env[x-i][y].setWall(true);
			else
				break;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
	}
	
	public void wallCheck(int x, int y)
	{
		int temp = 0;
		for(int i=1; i<width/10+1; i++)
		{
			try{
				if(env[x][y+i].isWall())
					temp+=1;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		if(temp>0)
		{
			for(int i=1; i<width/10+1; i++)
			{
				try{if(!env[x][y+i].isWall())
					env[x][y+i].setWall(false);
				else
					break;
				}
				catch(IndexOutOfBoundsException Exception){}
			}
		}
		
		temp = 0;
		for(int i=1; i<width/10+1; i++)
		{
			try{
				if(env[x][y-i].isWall())
					temp+=1;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		if(temp>0)
		{
			for(int i=1; i<width/10+1; i++)
			{
				try{if(!env[x][y-i].isWall())
					env[x][y-i].setWall(false);
				else
					break;
				}
				catch(IndexOutOfBoundsException Exception){}
			}
		}
		
		temp = 0;
		for(int i=1; i<length/10+1; i++)
		{
			try{
				if(env[x+i][y].isWall())
					temp+=1;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		if(temp>0)
		{
			for(int i=1; i<length/10+1; i++)
			{
				try{if(!env[x+i][y].isWall())
					env[x+i][y].setWall(false);
				else
					break;
				}
				catch(IndexOutOfBoundsException Exception){}
			}
		}
		
		temp = 0;
		for(int i=1; i<length/10+1; i++)
		{
			try{
				if(env[x-i][y].isWall())
					temp+=1;
			}
			catch(IndexOutOfBoundsException Exception){}
		}
		if(temp>0)
		{
			for(int i=1; i<length/10+1; i++)
			{
				try{if(!env[x-i][y].isWall())
					env[x-i][y].setWall(false);
				else
					break;
				}
				catch(IndexOutOfBoundsException Exception){}
			}
		}
	}
	
	public boolean left()
	{
		angle += 1;
		center.setLocation(Math.abs(((p1.getX()-p2.getX())/2))+Math.min(p1.getX(),p2.getX()),Math.abs((p1.getY()-p2.getY()))/2+Math.min(p1.getY(),p2.getY()));
		
		temp1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
		temp2.setLocation(radius1*Math.cos((angle*Math.PI/4)+Math.PI)+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
		temp3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
		
		if(canMove(center)&&canMove(temp1)&&canMove(temp2)&&canMove(temp3))
		{
			p1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
			p2.setLocation(radius1*Math.cos((angle*Math.PI/4)+Math.PI)+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
			p3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
			
			try {
				dos.writeInt(3);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 3");
				e1.printStackTrace();
			}
			changeBearing(1);
			repaint();
			update(g2);
			return true;
		}
		else
		{
			angle -= 1;
			return false;
		}
	}
	
	public boolean right()
	{
		angle -= 1;
		center.setLocation(Math.abs(((p1.getX()-p2.getX())/2))+Math.min(p1.getX(),p2.getX()),Math.abs((p1.getY()-p2.getY()))/2+Math.min(p1.getY(),p2.getY()));
		
		temp1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
		temp2.setLocation(radius1*Math.cos((angle*Math.PI/4)+Math.PI)+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
		temp3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());	
		
		if(canMove(center)&&canMove(temp1)&&canMove(temp2)&&canMove(temp3))
		{
			p1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
			p2.setLocation(radius1*Math.cos((angle*Math.PI/4)+Math.PI)+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
			p3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());	
		
			try {
				dos.writeInt(4);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 4");
				e1.printStackTrace();
			}
			changeBearing(-1);
			repaint();
			update(g2);
			return true;
		}
		else
		{
			angle += 1;
			return false;
		}
	}
	
	public boolean up()
	{
		temp1.setLocation(p1.getX()-10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p1.getY()-10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		temp2.setLocation(p2.getX()-10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p2.getY()-10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		temp3.setLocation(p3.getX()-10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p3.getY()-10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		center.setLocation(Math.abs(((temp1.getX()-temp2.getX())/2))+Math.min(temp1.getX(),temp2.getX()),Math.abs((temp1.getY()-temp2.getY()))/2+Math.min(temp1.getY(),temp2.getY()));
		
		if(canMove(center)&&canMove(temp1)&&canMove(temp2)&&canMove(temp3))
		{
			p1.setLocation(p1.getX()-10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p1.getY()-10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
			p2.setLocation(p2.getX()-10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p2.getY()-10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
			p3.setLocation(p3.getX()-10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p3.getY()-10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
			
			try {
				dos.writeInt(1);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 1");
				e1.printStackTrace();
			}
			repaint();
			update(g2);
			return true;
		}
		else
			return false;
	}
	
	public boolean down()
	{
		temp1.setLocation(p1.getX()+10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p1.getY()+10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		temp2.setLocation(p2.getX()+10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p2.getY()+10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		temp3.setLocation(p3.getX()+10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p3.getY()+10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		center.setLocation(Math.abs(((temp1.getX()-temp2.getX())/2))+Math.min(temp1.getX(),temp2.getX()),Math.abs((temp1.getY()-temp2.getY()))/2+Math.min(temp1.getY(),temp2.getY()));
		
		if(canMove(center)&&canMove(temp1)&&canMove(temp2)&&canMove(temp3))
		{
			p1.setLocation(p1.getX()+10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p1.getY()+10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
			p2.setLocation(p2.getX()+10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p2.getY()+10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
			p3.setLocation(p3.getX()+10*Math.cos((angle*Math.PI/4)+(Math.PI/2)),p3.getY()+10*Math.sin((angle*Math.PI/4)+(Math.PI/2)));
		
			try {
				dos.writeInt(2);
				dos.flush();
			} catch (IOException e1) {
				System.out.println("Error 2");
				e1.printStackTrace();
			}
			repaint();
			update(g2);
			return true;
		}
		else
			return false;
	}
	
	public static void changeBearing(int dir)
	{
		if(bear+dir<0)
			bear = 7;
		else if(bear+dir>7)
			bear = 0;
		else
			bear = bear+dir;
		bearing = bearings[bear];
	}
	
	public void keyPressed(KeyEvent e) 
	{	
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
		{
			left();
		}
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
		{
			right();
		}
		if(e.getKeyCode()==KeyEvent.VK_UP)
		{
			up();
		}
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
		{
			down();
		}
		
		if(e.getKeyCode()== KeyEvent.VK_ENTER)
		{
			solve();
		}
		
		if(e.getKeyCode() == KeyEvent.VK_END)
		{
			try {
				dos.writeInt(6);
				dos.flush();
				Thread.sleep(100);
				dis.close();
				dos.close();
				Thread.sleep(100);
				conn.close();
			} catch (IOException ioe) {
				System.out.println("IOException closing connection:");
				System.out.println(ioe.getMessage());
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			try {
				dos.writeInt(5);
				dos.flush();
				int temp = dis.readInt();
				System.out.println(temp);
			} catch (IOException e1) {
				System.out.println("Distance");
				e1.printStackTrace();
			}
		}

		if(e.getKeyCode() == KeyEvent.VK_ALT)
		{
			try {
				dos.writeInt(7);
				dos.flush();
				int tempX = dis.readInt();
				int tempY = dis.readInt();
				System.out.println(tempX + " "+ tempY);
			} catch (IOException e1) {
				System.out.println("Distance");
				e1.printStackTrace();
			}
		}
		
		repaint();
	}
	public boolean canMove(Point2D future)
	{
		double fX = future.getX();
		double fY = future.getY();
		
		if((int)((fX)/10)<0 || (int)((fY)/10)<0 || fY<0 || fX<0 || (int)((fY)/10)>64 || (int)((fX)/10)>140)
			return false;
		if(map[(int)((fX)/10)][(int)((fY)/10)]>0)
			return false;
		
		return true;
	}
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	public void mouseClicked(MouseEvent e) 
	{
		int x = e.getX()/10;
		int y = e.getY()/10;
		
		/*System.out.println(x+" "+y);
		System.out.println("North "+env[x][y].getNorth());
		System.out.println("South "+env[x][y].getSouth());
		System.out.println("East "+env[x][y].getEast());
		System.out.println("West "+env[x][y].getWest());*/
			
		try{if(env[x][y].isWall())
			{
				antiWallCheck(x,y);
				env[x][y].setWall(false);
				env[x][y].setEnd();
			}
			else if(env[x][y].isEnd())
				env[x][y].setEnd();
			else
			{
				env[x][y].setWall(true);
				wallCheck(x,y);
			}
				
			}
		catch(IndexOutOfBoundsException Exception){}
		
		repaint();
	}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
