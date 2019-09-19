import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace 
{
	private Object2DGrid grassSpace;

	/**
	 * Constructor of the Simulation Space with a fixed size
	 * @param xSize
	 * @param ySize
	 */	
	public RabbitsGrassSimulationSpace(int xSize, int ySize)
	{
		// Create a space to store where is grass (init with 0=no-grass)
		grassSpace = new Object2DGrid(xSize, ySize);
	    for(int i = 0; i < xSize; i++)
	    {
	    	for(int j = 0; j < ySize; j++)
	    	{
	    		grassSpace.putObjectAt(i,j,new Integer(0));
	    	}
	    }
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
    	    		grassSpace.putObjectAt(i,j,new Integer(1));
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
    	    	grassSpace.putObjectAt(x, y, new Integer(1));
    			
    	    }    		
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
		return ((Integer)grassSpace.getObjectAt(x,y)).intValue() == 1;
	}
	
	/**
	 * Get the current grass space
	 * @return The current grass space
	 */
	public Object2DGrid getCurrentGrassSpace()
	{
	    return grassSpace;
	}
}
