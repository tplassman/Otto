package otto;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import lejos.pc.comm.NXTConnector;

import otto.Pose;
import otto.cell;
import otto.environment;

public class ottoRandom extends JFrame implements KeyListener, MouseListener
{	
	private static final long serialVersionUID = 8L;
	Container con = new Container();
	JPanel main = new JPanel();
	Image img;
	static int width = 40;
	static int length = 80;
	static int maxScreenW = 1280;
	static int maxScreenH = 700;
	int angle = 0;
	static int [][] map;
	static cell [][] env;
	
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

	int numParticles = 5000;
	Pose[] particles = new Pose[numParticles];
	double reading;
	Random rand = new Random();
	boolean drawParticles = false;
	
	public static void main(String[] args) 
	{
		map = environment.getMap();
		env = new cell [map.length][map[0].length];
		setEnv(1);
		new ottoRandom();
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
	
	public ottoRandom()
	{
		super("MCL");
		addKeyListener(this);		
		setSize(new Dimension(maxScreenW,maxScreenH));
		con=getContentPane();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBackground(Color.black);
		main=new JPanel();
		main.setSize(new Dimension(maxScreenW,maxScreenH));
		con.add(main);
		con.addMouseListener(this);
		setVisible(true);
		setResizable(true);
	}
	
	public void generateParticles()
	{
		// Create poses
		int[] w = new int[env.length];
		int[] h = new int[env[0].length];
		for (int i = 0; i < env.length ; i++) 
			w[i] = i;
		for (int j = 0; j < env[0].length; j++)
			h[j] = j;
		double[] card = {0, Math.PI/2, Math.PI, 3*Math.PI/2};
		int ii = 0;
		while (ii < particles.length)
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
		}
	}

	public void applyMove()
	{
		int[] move = {1, 2, 3, 4};
		int randMove = move[rand.nextInt(move.length)];
		System.out.println(randMove);
		
		try {
			dos.writeInt(randMove);
			dos.flush();
		} catch (IOException e1) {
			System.out.println("Error 2");
			e1.printStackTrace();
		}
		
		if(randMove == 3 || randMove == 4)
			for (int i=0; i<particles.length; i++)
				particles[i].applyMove(randMove);

		for (int i=0; i<particles.length; i++)
			particles[i].applyMove(randMove);
	}
	
	public void calculateWeight()
	{
		try {
			dos.writeInt(5);
			dos.flush();
			reading = dis.readFloat();
			System.out.println(reading);
		} catch (IOException e1) {
			System.out.println("Distance");
			e1.printStackTrace();
		}
		if (reading < 255)
			for (int i=0; i<particles.length; i++)
				particles[i].calculateWeight((double)reading);
	}

	public void resample()
	{
		Pose[] oldParticles = new Pose[particles.length];
		//Make of a copy of the particles
		for(int i=0;i<particles.length;i++) {
			oldParticles[i] = particles[i];
		}
		// Continually pick a random number and select the particles with
		// weights greater than or equal to it until we have a full
		// set of particles.
		int count = 0;	
		while(count < particles.length) {
			double randNum = Math.random();
			for(int i=0;i<particles.length && count<particles.length;i++) {
				if (oldParticles[i].getWeight() >= randNum && oldParticles[i].getWeight() <= 1+randNum) {
					// Create a new instance of the particle and set its weight to zero
					particles[count] = new Pose(oldParticles[i].getPoint().getX(),oldParticles[i].getPoint().getY(), oldParticles[i].getAngle(), oldParticles[i].getNorth(), oldParticles[i].getSouth(), oldParticles[i].getEast(), oldParticles[i].getWest());
					particles[count].setWeight(0);
					count++;
				}
			}
		}
	}
	
	public boolean sameParticles()
	{
		int count = 1;
        for(int j = 1; j < particles.length; j++) {
        	if(particles[0].getPoint().getX() == particles[j].getPoint().getX() && particles[0].getPoint().getY() == particles[j].getPoint().getY() && particles[0].getAngle() == particles[j].getAngle())
        		count++;
	    }
	    
	    if(count == particles.length)
	    	return true;
	    return false;
	}
	        	
	public void paint(Graphics g)
	{
		img=main.createImage(main.getWidth(),main.getHeight());
		Graphics2D g2 = (Graphics2D)img.getGraphics();
		
		g2.setColor(Color.DARK_GRAY);
		for (int i=10; i<1270; i+=10)
			g2.draw(new Line2D.Double(i,10,i,640));
		
		for (int i=10; i<640; i+=10)
			g2.draw(new Line2D.Double(10,i,1270,i));

		for(int i=0; i<env.length; i++)
			for(int j=0; j<env[i].length; j++)
			{
				g2.setColor(Color.RED);
				if(env[i][j].isWall() && env[i][j].isVisible())
					g2.fillRect(i*10,j*10,10,10);
				g2.setColor(Color.GREEN);
				if(env[i][j].isStart())
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
			for (int k = 0; k < particles.length; k++) 
			{
				double x = particles[k].getPoint().getX();
				double y = particles[k].getPoint().getY();
				Line2D r = new Line2D.Double(x, y, x + (20*Math.cos(particles[k].getAngle())), y - (20*Math.sin(particles[k].getAngle())));
				Ellipse2D c = new Ellipse2D.Double(x-5, y-5, 10, 10);
				g2.setColor(Color.green);
				g2.draw(c);
				g2.setColor(Color.blue);
				g2.draw(r);
			}
		main.getGraphics().drawImage(img,0,0,this);
	}

	public void solve() throws InterruptedException
	{
		setEnv(2);
		setBlock();

		for(int i=0; i<env.length; i++)
		{
			for(int j=0; j<env[i].length; j++)
			{
				env[i][j].setDistances(env);
			}
		}
		
		generateParticles();
		drawParticles = true;
		
		while (!sameParticles())
		{
			applyMove();
			Thread.sleep(3000);
			calculateWeight();
			if (reading < 255)
			{
				resample();
				repaint();
			}
		}
		
		startX = (int)particles[0].getPoint().getX();
		startY = (int)particles[0].getPoint().getY();
		
		
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
		path.add(env[startX][startY]);
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
	
	public void keyPressed(KeyEvent e) 
	{	
		if(e.getKeyCode()== KeyEvent.VK_ENTER)
			solve();
	}

	public void keyReleased(KeyEvent arg0) {}
	
	public void keyTyped(KeyEvent arg0) {}
	
	public void mouseClicked(MouseEvent e) 
	{
		int x = e.getX()/10;
		int y = e.getY()/10;
		
		System.out.println(x+" "+y);
			
		try{if(env[x][y].isWall())
			{
				antiWallCheck(x,y);
				env[x][y].setWall(true);
				env[x][y].setStart();
			} 
			else if(env[x][y].isStart())
			{
				env[x][y].setStart();
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
