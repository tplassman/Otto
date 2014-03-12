package otto;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import lejos.pc.comm.NXTConnector;

import otto.Pose;
import otto.cell;
import otto.environment;

public class otto extends JFrame implements KeyListener, MouseListener
{	
	private static final long serialVersionUID = 8L;
	Container con = new Container();
	JPanel main = new JPanel();
	Image img;
	static int width = 40;
	static int length = 80;
	static int maxScreenW = 1280;
	static int maxScreenH = 800;
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

	Point2D center = new Point2D.Double(80,200);
	Point2D p1 = new Point2D.Double(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
	Point2D p2 = new Point2D.Double(radius1*Math.cos((angle*Math.PI/4)+(Math.PI))+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
	Point2D p3 = new Point2D.Double(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
	
	Point2D temp1 = new Point2D.Double(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
	Point2D temp2 = new Point2D.Double(radius1*Math.cos((angle*Math.PI/4)+(Math.PI))+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
	Point2D temp3 = new Point2D.Double(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
	
	Pose[] particles = new Pose[7434];
	float reading;
	Random rand = new Random();
	
	public static void main(String[] args) 
	{
		map = environment.getMap();
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
		
		// Create poses
		int ii = 0;
		for (int i = 0; i < env.length ; i++) 
		{
			for (int j = 0; j < env[0].length; j++)
			{
				if(!env[i][j].isWall())
				{
					float cellX = (float)(env[i][j].getX()*10)+5;
					float cellY = (float)(env[i][j].getY()*10)+5;
					float head = (float)(Math.random()*359);
					particles[ii] = new Pose(cellX, cellY, head);
					ii++;
				}
			}
		}
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
			float rand = (float) Math.random();
			for(int i=0;i<particles.length && count<particles.length;i++) {
				if (oldParticles[i].getWeight() >= rand) {
					// Create a new instance of the particle and set its weight to zero
					particles[count] = new Pose((float)oldParticles[i].getPoint().getX(),(float)oldParticles[i].getPoint().getY(), oldParticles[i].angle);
					particles[count++].setWeight(0);
				}
			}
		}
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
		
		g2.setColor(Color.red);
		g2.draw(new Line2D.Double(p1,p2));
		g2.draw(new Line2D.Double(p2,p3));
		g2.draw(new Line2D.Double(p3,p1));	

		// Paint poses
		for (int k = 0; k < particles.length; k++) 
		{
			float x = (float) particles[k].getPoint().getX();
			float y = (float) particles[k].getPoint().getY();
			Line2D r = new Line2D.Float(x, y, x + 20*(float)Math.cos(particles[k].getAngle()), y+20*(float)Math.sin(particles[k].getAngle()));
			Ellipse2D c = new Ellipse2D.Float(x-5, y-5, 10, 10);
			g2.setColor(Color.green);
			g2.draw(c);
			g2.setColor(Color.blue);
			g2.draw(r);
			
		}
		main.getGraphics().drawImage(img,0,0,this);
	}

	public void keyPressed(KeyEvent e) 
	{	
		if(e.getKeyCode()==KeyEvent.VK_LEFT)
		{
			angle += 1;
			center.setLocation(Math.abs(((p1.getX()-p2.getX())/2))+Math.min(p1.getX(),p2.getX()),Math.abs((p1.getY()-p2.getY()))/2+Math.min(p1.getY(),p2.getY()));
			
			temp1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
			temp2.setLocation(radius1*Math.cos((angle*Math.PI/4)+Math.PI)+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
			temp3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
			
			System.out.println(temp3);
			
			if(canMove(center)&&canMove(temp1)&&canMove(temp2)&&canMove(temp3))
			{
				p1.setLocation(radius1*Math.cos(angle*Math.PI/4)+center.getX(),radius1*Math.sin(angle*Math.PI/4)+center.getY());
				p2.setLocation(radius1*Math.cos((angle*Math.PI/4)+Math.PI)+center.getX(),radius1*Math.sin((angle*Math.PI/4)+(Math.PI))+center.getY());
				p3.setLocation(radius2*Math.cos((angle*Math.PI/4)+(Math.PI/2))+center.getX(),radius2*Math.sin((angle*Math.PI/4)+(Math.PI/2))+center.getY());
			
				for (int k = 0; k < particles.length; k++) 
				{
					particles[k].setAngle(particles[k].getAngle()+45);
				}
			}
			else
				angle -= 1;
		}
		if(e.getKeyCode()==KeyEvent.VK_RIGHT)
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
				for (int k = 0; k < particles.length; k++) 
				{
					particles[k].setAngle(particles[k].getAngle()-45);
				}
			}
			else
				angle += 1;
		}
		if(e.getKeyCode()==KeyEvent.VK_UP)
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
				for (int k = 0; k < particles.length; k++) 
				{
					particles[k].setPoint((float)particles[k].getPoint().getX()-(10*(float)Math.cos(particles[k].getAngle())), (float)particles[k].getPoint().getY()-(10*(float)Math.sin(particles[k].getAngle())));
				}
			}
		}
		if(e.getKeyCode()==KeyEvent.VK_DOWN)
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
				for (int k = 0; k < particles.length; k++) 
				{
					particles[k].setPoint((float)particles[k].getPoint().getX()+(10*(float)Math.cos(particles[k].getAngle())), (float)particles[k].getPoint().getY()+(10*(float)Math.sin(particles[k].getAngle())));
				}
			}
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
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

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
