import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace 
{
	private Object2DGrid grassSpace;

	public RabbitsGrassSimulationSpace(int xSize, int ySize)
	{
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
	    // Randomly place grass in grassSpace
	    for(int i = 0; i < grass; i++)
	    {
	    	// TODO CARE FOR TOO MUCH GRASS
	    	
			// Choose coordinates
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
}
