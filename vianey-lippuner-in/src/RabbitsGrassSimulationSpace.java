import java.util.ArrayList;

import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace 
{
	private Object2DGrid grassSpace;
	private Object2DGrid agentSpace;
	private int grassEnergy;
	private ArrayList<Integer> emptyGrassList;

	/**
	 * Constructor of the Simulation Space with a fixed size
	 * @param xSize
	 * @param ySize
	 */	
	public RabbitsGrassSimulationSpace(int xSize, int ySize, int grassEnergy)
	{
		// Get the param for the grass energy
		this.grassEnergy = grassEnergy;
		
		// Create a space to store where is grass (init with 0=no-grass)
		grassSpace = new Object2DGrid(xSize, ySize);
	    for(int i = 0; i < xSize; i++)
	    {
	    	for(int j = 0; j < ySize; j++)
	    	{
	    		grassSpace.putObjectAt(i,j,new Integer(0));
	    	}
	    }
	  
	    // Empty grass list
	    emptyGrassList = new ArrayList<Integer>();

	    // Create a space to store where are agents
	    agentSpace = new Object2DGrid(xSize, ySize);

	}
	
	/**
	 * Initialize the space with grass
	 * @param grass The number of grass to distribute
	 */	
	public void spreadGrass(int grass)
	{	
		int maxGrass = grassSpace.getSizeX() * grassSpace.getSizeY();
    	// CARE FOR TOO MUCH GRASS
    	if (grass > maxGrass)
    	{
    		System.out.println("Capped the grass at max cell " + maxGrass + "!");
    		grass = maxGrass;
    	}    	
    	
    	// CARE FOR NOT ENOUGH GRASS
    	if (grass < 0)
    	{
    		System.out.println("Capped the grass at min cell 0!");
    		grass = 0;
    	}
    	
    	// More grass than no-grass
    	if (grass > maxGrass / 2)
    	{
    		// Overwrite the space to store where is grass (init with 1=grass)
    		for(int i = 0; i < grassSpace.getSizeX(); i++)
    	    {
    	    	for(int j = 0; j < grassSpace.getSizeY(); j++)
    	    	{
    	    		grassSpace.putObjectAt(i,j,new Integer(grassEnergy));
    	    	}
    	    }
    		
    	    // Randomly remove grass in grassSpace
    	    for (int i = 0; i < maxGrass - grass; i++)
    	    {	    	
    			// Choose coordinates (verify that there is no double assignment)
    	    	int x, y;
    	    	do
    	    	{
    	    		x = (int)(Math.random()*(grassSpace.getSizeX()));
    			 	y = (int)(Math.random()*(grassSpace.getSizeY()));
    	    	} while (((Integer)grassSpace.getObjectAt(x,y)).intValue() < 1);

    			
    	    	// Set to 0=no-grass
    	    	grassSpace.putObjectAt(x, y, new Integer(0));
    	    }    		

    	}
    	else
    	{
    	    // Randomly place grass in grassSpace
    	    for (int i = 0; i < grass; i++)
    	    {	    	
    			// Choose coordinates (verify that there is no double assignment)
    	    	int x, y;
    	    	do
    	    	{
    	    		x = (int)(Math.random()*(grassSpace.getSizeX()));
    			 	y = (int)(Math.random()*(grassSpace.getSizeY()));
    	    	} while (((Integer)grassSpace.getObjectAt(x,y)).intValue() > 0);

    			
    	    	// Set to 1=grass
    	    	grassSpace.putObjectAt(x, y, new Integer(grassEnergy));
    			
    	    }    		
    	}
    	
	    // Empty grass list
	    emptyGrassList = new ArrayList<Integer>();

    	// Get references of empty grass
    	for(int i = 0; i < grassSpace.getSizeX(); i++)
	    {
	    	for(int j = 0; j < grassSpace.getSizeY(); j++)
	    	{
	    		if (((Integer)grassSpace.getObjectAt(i,j)).intValue() == 0)
	    		{
	    			emptyGrassList.add(new Integer(i * grassSpace.getSizeY() + j));
	    		}
	    	}
	    }
	}
	
	/**
	 * Grow grass
	 * @param grass The number of grass to grow
	 */	
	public void growGrass(int grass)
	{
		SimUtilities.shuffle(emptyGrassList);

		for (int i = 0; i<grass; i++)
		{
			// No empty space left
			if (emptyGrassList.isEmpty()) break;
			
			// Get random space to add grass
			Integer index = emptyGrassList.remove(0);
			int x = index / grassSpace.getSizeY();
			int y = index % grassSpace.getSizeY();
			
	    	grassSpace.putObjectAt(x, y, new Integer(grassEnergy));
		}
	}
	
	/**
	 * Check if there is grass at position x,y
	 * @param x
	 * @param y
	 * @return Whether there is grass
	 */
	public boolean hasGrassAt(int x, int y)
	{
		return ((Integer)grassSpace.getObjectAt(x,y)).intValue() != 0;
	}
	
	/**
	 * Check how much grass is at position x,y
	 * @param x
	 * @param y
	 * @return The energy of the grass
	 */
	public int takeGrassAt(int x, int y)
	{
		return ((Integer)grassSpace.getObjectAt(x,y)).intValue();
	}
	
	/**
	 * Get the current grass space
	 * @return The current grass space
	 */
	public Object2DGrid getCurrentGrassSpace()
	{
	    return grassSpace;
	}
	
	/**
	 * Get the current agent space
	 * @return The current agent space
	 */
	public Object2DGrid getCurrentAgentSpace()
	{
	    return agentSpace;
	}
	
	/**
	 * Check if there is already an agent at cell x,y
	 * @param x
	 * @param y
	 * @return Whether there is an agent
	 */
	public boolean isCellOccupied(int x, int y)
	{
	    return agentSpace.getObjectAt(x, y) != null;
	}

	/**
	 * Add an agent to the space
	 * @param agent The new agent which has not yet been added
	 * @return Whether it could be placed
	 */
	public boolean addAgent(RabbitsGrassSimulationAgent agent)
	{
	    boolean retVal = false;
	    int count = 0;
	    int countLimit = 10 * agentSpace.getSizeX() * agentSpace.getSizeY(); // Define a max try limit

	    while ((retVal == false) && (count < countLimit))
	    {
	    	int x = (int)(Math.random()*(agentSpace.getSizeX()));
	    	int y = (int)(Math.random()*(agentSpace.getSizeY()));
	    	if (isCellOccupied(x,y) == false)
	    	{
	    		agentSpace.putObjectAt(x,y,agent);
	    		agent.setXY(x,y);
	            agent.setGrassSpace(this);
	    		retVal = true;
	    	}
	    	count++;
	    }
	    
	    if (count == countLimit)
	    {
	    	System.out.println("New agent could be be placed as no free space has been found");
	    }

	    return retVal;
	}
	
	/**
	 * Remove an agent from the space
	 * @param x 
	 * @param y
	 */
	public void removeAgentAt(int x, int y)
	{
	    agentSpace.putObjectAt(x, y, null);
	}
	
	/**
	 * Eat grass at space
	 * @param x 
	 * @param y
	 * @return 0 or 1, whether grass was there or not
	 */
	public int eatGrassAt(int x, int y)
	{
	    int grass = takeGrassAt(x, y);
	    grassSpace.putObjectAt(x, y, new Integer(0));
	    emptyGrassList.add(new Integer(x * grassSpace.getSizeY() + y));
	    return grass;
	}
	
	/**
	 * Counts the grass
	 * @return The number of grass cells
	 */
	public int getTotalGrass()
	{
	    int totalGrass = 0;
	    for(int i = 0; i < grassSpace.getSizeX(); i++)
	    {
	    	for(int j = 0; j < grassSpace.getSizeY(); j++)
	      	{
	    		if (hasGrassAt(i, j)) totalGrass++;
	      	}
	    }
	    return totalGrass;
	}
	
	/**
	 * Try to move the agent from x,y to proposedX,proposedY
	 * @param x 
	 * @param y
	 * @param proposedX 
	 * @param proposedY
	 * @return If it worked
	 */
	public boolean moveAgentAt(int x, int y, int proposedX, int proposedY)
	{
	    boolean retVal = false;
	    if(!isCellOccupied(proposedX, proposedY))
	    {
			RabbitsGrassSimulationAgent el = (RabbitsGrassSimulationAgent)agentSpace.getObjectAt(x, y);
			removeAgentAt(x,y);
			el.setXY(proposedX, proposedY);
			agentSpace.putObjectAt(proposedX, proposedY, el);
			retVal = true;
	    }
	    return retVal;
	}
}
