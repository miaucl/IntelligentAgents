import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable 
{
	private int x;
	private int y;
	private int proposedX;
	private int proposedY;
	private int energy;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace grassSpace;
 
	public RabbitsGrassSimulationAgent(int energy)
	{
	    x = -1;
	    y = -1;
	    this.energy = energy; // Give it an initial energy
	    IDNumber++; // Keep track of all agents count
	    ID = IDNumber; // Remember its ID
	}

	
	/**
	 * Setter for 'x' and 'y'
	 * @param x
	 * @param y
	 */	
	public void setXY(int newX, int newY)
	{
	    x = newX;
	    y = newY;
	}

	/**
	 * Getter for 'x'
	 * @return The x coordinate
	 */	
	@Override
	public int getX() 
	{
		return x;
	}

	/**
	 * Getter for 'y'
	 * @return The y coordinate
	 */	
	@Override
	public int getY() 
	{
		return y;
	}

	/**
	 * Setter for 'grassSpace'
	 * @param grassSpace 
	 */	
	public void setGrassSpace(RabbitsGrassSimulationSpace s)
	{
	    grassSpace = s;
	}
	
	/**
	 * Getter for 'ID'
	 * @return Identifier string of the agent
	 */	
	public String getID()
	{
	    return "A-" + ID;
	}

	/**
	 * Setter for 'energy'
	 * @param The new energy of the agent
	 */	
	public void setEnergy(int e)
	{
	    energy = e;
	}
	
	/**
	 * Getter for 'energy'
	 * @return The current energy of the agent
	 */	
	public int getEnergy()
	{
	    return energy;
	}
	
	/**
	 * Draw function with instructions of how to draw the agent
	 * @param The graphics element of the simulation
	 */	
	@Override
	public void draw(SimGraphics G)
	{
		if(energy > 5)
		    G.drawFastRoundRect(Color.blue);
		else
		    G.drawFastRoundRect(Color.red);
	}


	/**
	 * Report to the console
	 */	
	public void report()
	{
	    System.out.println(getID() + 
	                       " at " + 
	                       x + ", " + y + 
	                       " has " + 
	                       getEnergy() + " energy");
	}
	
	/**
	 * Move one step and get new x and y position
	 */	
	public void proposeNewXY()
	{
		// Reset 0
		proposedX = x;
		proposedY = y;
		
		// Random direction
	    switch ((int)Math.floor(Math.random() * 4)) 
	    {
			case 0: // Right
				proposedX++;
				break;
			case 1: // Left
				proposedX--;
				break;
			case 2: // Top
				proposedY--;
				break;
			case 3: // Bottom
				proposedY++;
				break;
	
			default:
				System.out.println("Should never get here");
				break;
		}
	    
	    // Wrap around
	    if (proposedX < 0) proposedX = grassSpace.getCurrentAgentSpace().getSizeX() - 1;
	    else if (proposedX >= grassSpace.getCurrentAgentSpace().getSizeX()) proposedX = 0;
	    if (proposedY < 0) proposedY = grassSpace.getCurrentAgentSpace().getSizeY() - 1;
	    else if (proposedY >= grassSpace.getCurrentAgentSpace().getSizeY()) proposedY = 0;
	}
	
	/**
	 * Ask the space to move the agent
	 */	
	private boolean tryMove()
	{
		 return grassSpace.moveAgentAt(x, y, proposedX, proposedY);
	}
	
	/**
	 * Execute a step of the simulation
	 */	
	public void step()
	{
		proposeNewXY();
		
		if(tryMove())
		{
		      energy--;
		}
		
		energy += grassSpace.eatGrassAt(x, y);
	}

}
