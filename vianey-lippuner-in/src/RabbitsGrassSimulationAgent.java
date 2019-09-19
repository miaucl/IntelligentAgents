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
	private int energy;
	private static int IDNumber = 0;
	private int ID;
	  
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
	 * Getter for 'ID'
	 * @return Identifier string of the agent
	 */	
	public String getID()
	{
	    return "A-" + ID;
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
	    G.drawFastRect(Color.blue);
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

}
