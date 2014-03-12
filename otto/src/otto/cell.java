package otto;

import java.util.ArrayList;

public class cell {

	private int x;
	private int y;
	private boolean start;
	private boolean end;
	private boolean wall;
	private boolean block;
	private boolean visible;

	private double f; // cost of getting from this square to goal
	private double h; // cost of getting from parent square to this node
	private double g;
	private int north;
	private int south;
	private int east;
	private int west;
	
	private ArrayList <cell> adjacencies = new ArrayList <cell> ();
	private cell parent;

	public cell(int x, int y, boolean start, boolean end, boolean wall) 
	{
		this.x = x;
		this.y = y;
		this.start = start;
		this.end = end;
		this.wall = wall;
		block = false;
		visible = true;
		north = 0;
		south = 0;
		east = 0;
		west = 0;
	}
	
	public void setDistances(cell[][] env)
	{
		if(isWall() && isVisible())
		{
			return;
		}
		else{
			cell north = env[x][y-1];
			cell south = env[x][y+1];
			cell west = env[x-1][y];
			cell east = env[x+1][y];
			int count = 1;
			
			while(!north.isWall() && north.isVisible())
			{
				count++;
				north = env[x][y-count];
			}
			setNorth(count*10);
		
			count = 1;
			while(!south.isWall() && south.isVisible())
			{
				count++;
				south = env[x][y+count];
			}
			setSouth(count*10);
		
			count = 1;
			while(!east.isWall() && east.isVisible())
			{
				count++;
				east = env[x+count][y];
			}
			setEast(count*10);
			
			count = 1;
			while(!west.isWall() && west.isVisible())
			{
				count++;
				west = env[x-count][y];
			}
			setWest(count*10);
		}
	}
	
	public void setNorth(int i)
	{
		north = i;
	}
	
	public void setSouth(int i)
	{
		south = i;
	}
	
	public void setEast(int i)
	{
		east = i;
	}
	
	public void setWest(int i)
	{
		west = i;
	}
	
	public int getNorth()
	{
		return north;
	}
	
	public int getSouth()
	{
		return south;
	}
	
	public int getEast()
	{
		return east;
	}
	
	public int getWest()
	{
		return west;
	}

	public void setBlock(boolean block)
	{
		this.block = block;
	}
	
	public boolean isBlock()
	{
		return block;
	}
	
	public int getX() 
	{
		return x;
	}

	public int getY() 
	{
		return y;
	}
	
	public void setWall(boolean visible)
	{
		wall = !wall;
		this.visible = visible;
	}
	public boolean isVisible()
	{
		return visible;
	}
	public boolean isWall()
	{
		return wall;
	}

	public boolean isStart() 
	{
		return start;
	}

	public void setStart() 
	{
		start = !start;
	}

	public boolean isEnd() 
	{
		return end;
	}

	public void setEnd() 
	{
		end = !end;
	}

	public ArrayList <cell> getAdjacencies() 
	{
		return adjacencies;
	}

	public cell getParent() 
	{
		return parent;
	}

	public void setParent(cell parent) 
	{
		this.parent = parent;
	}

	public void calculateAdjacencies(cell[][] env) 
	{
		int top = x - 1;
		int bottom = x + 1;
		int left = y - 1;
		int right = y + 1;	
		
		if(!isWall() && !isBlock())
		{
			try{
				if(!env[top][y].isWall() && !env[top][y].isBlock())
					adjacencies.add(env[top][y]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[top][right].isWall() && !env[top][y].isWall() && !env[x][right].isWall() && !env[top][right].isBlock() && !env[top][y].isBlock() && !env[x][right].isBlock())
					adjacencies.add(env[top][right]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[x][right].isWall() && !env[x][right].isBlock())
					adjacencies.add(env[x][right]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[bottom][right].isWall() && !env[x][right].isWall() && !env[bottom][y].isWall() && !env[bottom][right].isBlock() && !env[x][right].isBlock() && !env[bottom][y].isBlock())
					adjacencies.add(env[bottom][right]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[bottom][y].isWall() && !env[bottom][y].isBlock())
					adjacencies.add(env[bottom][y]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[bottom][left].isWall() && !env[bottom][y].isWall() && !env[x][left].isWall() && !env[bottom][left].isBlock() && !env[bottom][y].isBlock() && !env[x][left].isBlock())
					adjacencies.add(env[bottom][left]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[x][left].isWall() && !env[x][left].isBlock())
					adjacencies.add(env[x][left]);}
			catch(IndexOutOfBoundsException Exception){}
			try{
				if(!env[top][left].isWall() && !env[top][y].isWall() && !env[x][left].isWall() && !env[top][left].isBlock() && !env[top][y].isBlock() && !env[x][left].isBlock())
					adjacencies.add(env[top][left]);}
			catch(IndexOutOfBoundsException Exception){}
		}
	}

	public void setH(cell goal)
	{
		if(isEnd())
			h=0.0;
		else
		h = Math.sqrt(Math.pow((x-goal.getX())*10, 2) + Math.pow((y-goal.getY())*10, 2));
	}
	
	public double getH() 
	{	
		return h;
	}

	public double calcG(cell start)
	{
		if(!isWall())
		{
			int hor = 0;
			int dia = 0;
			int xTemp = Math.abs(start.getX()-getX());
			int yTemp = Math.abs(start.getY()-getY());
		
			while(xTemp>0 && yTemp>0)
			{
				xTemp -= 1;
				yTemp -= 1;
				dia++;
			}
			hor = xTemp + yTemp;
		
			return (hor*10) + (dia*14);
		}
		return 0;
	}
	
	public void setG(double g)
	{
		this.g = g;
	}
	
	public double getG()
	{
		return g;
	}
	
	public void setF(cell start, cell goal) 
	{
		setH(goal);
		f = getH() + calcG(start);
	}
	
	public void setF() 
	{
		f = getH() + getG();
	}
	
	public double getF()
	{
		return f;
	}
	
	public String toString()
	{
		return "X: "+x+" Y: "+y+"\n";
	}
}
