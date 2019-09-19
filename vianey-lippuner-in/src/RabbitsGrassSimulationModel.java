import java.awt.Color;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Value2DDisplay;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author A. Vianey, C. Lippuner
 */


public class RabbitsGrassSimulationModel extends SimModelImpl 
{		

	private static final int GRID_SIZE = 20;
	private static final int NUM_INIT_RABBITS = 4;
	private static final int NUM_INIT_GRASS = 12;
	private static final double GRASS_GROWTH_RATE = 0.1;
	private static final int BIRTH_THRESHOLD = 5;

	private Schedule schedule;
  
	private RabbitsGrassSimulationSpace grassSpace;

	private DisplaySurface displaySurf;

  	private int gridSize = GRID_SIZE;
  	private int numInitRabbits = NUM_INIT_RABBITS;
  	private int numInitGrass = NUM_INIT_GRASS;
  	private double grassGrowthRate = GRASS_GROWTH_RATE;
  	private int birthThreshold = BIRTH_THRESHOLD;

	public static void main(String[] args) 
	{
		
		System.out.println("Rabbit skeleton");

		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		// Do "not" modify the following lines of parsing arguments
		if (args.length == 0) // by default, you don't use parameter file nor batch mode 
			init.loadModel(model, "", false);
		else
			init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
		
	}
	
	/**
	 * The setup routine to clean all remaining objects and left-overs
	 */	
	@Override
	public void setup() 
	{			
		System.out.println("Running setup");
		grassSpace = null;
		
		if (displaySurf != null)
		{
			displaySurf.dispose();
		}
	    displaySurf = null;

	    displaySurf = new DisplaySurface(this, "Rabbits Grass Model Window 1");

	    registerDisplaySurface("Rabbits Grass Model Window 1", displaySurf);
	}


	/**
	 * Begin the simulation and setup all objects and configurations
	 */	
	@Override
	public void begin()
	{
	    buildModel();
	    buildSchedule();
	    buildDisplay();
	    
	    displaySurf.display();
	}

	/**
	 * Build the model
	 * Add the simulation space
	 */	
	private void buildModel()
	{
		System.out.println("Build model");
	    grassSpace = new RabbitsGrassSimulationSpace(gridSize, gridSize);
	    grassSpace.spreadGrass(numInitGrass);

	}
	
	/**
	 * Build the schedule
	 */	
	private void buildSchedule()
	{
		System.out.println("Build schedule");
	}
	
	/**
	 * Build the display
	 * Create a window to display the space
	 */	
	private void buildDisplay()
	{
		System.out.println("Build display");
		
		ColorMap map = new ColorMap();

	    map.mapColor(0, Color.white);
	    map.mapColor(1, Color.green);

	    Value2DDisplay displayGrass = new Value2DDisplay(grassSpace.getCurrentGrassSpace(), map);

	    displaySurf.addDisplayable(displayGrass, "Grass");
	}

	/**
	 * Get all available parameters of the model
	 * @return A string list of the parameters
	 */	
	@Override
	public String[] getInitParam() 
	{
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold" };
		return params;
	}

	/**
	 * Get the name of the simulation model
	 * @return The model name
	 */	
	@Override
	public String getName() 
	{
		return "Rabbit Grass Simulation";
	}

	/**
	 * Get the schedule of the simulation
	 * @return A schedule object
	 */	
	@Override
	public Schedule getSchedule() 
	{
		return schedule;
	}


	/**
	 * Getter for 'gridSize'
	 * @return The size of the space
	 */	
	public int getGridSize() 
	{
		return gridSize;
	}

	/**
	 * Setter for 'gridSize'
	 * @param gridSize
	 */	
	public void setGridSize(int gridSize) 
	{
		this.gridSize = gridSize;
	}

	/**
	 * Getter for 'numInitRabbits'
	 * @return Initial rabbits placed in the space
	 */	
	public int getNumInitRabbits() 
	{
		return numInitRabbits;
	}

	/**
	 * Setter for 'numInitRabbits'
	 * @param numInitRabbits
	 */	
	public void setNumInitRabbits(int numInitRabbits) 
	{
		this.numInitRabbits = numInitRabbits;
	}

	/**
	 * Getter for 'numInitGrass'
	 * @return Initial grass placed in the space
	 */	
	public int getNumInitGrass() 
	{
		return numInitGrass;
	}

	/**
	 * Setter for 'numInitGrass'
	 * @param numInitGrass
	 */	
	public void setNumInitGrass(int numInitGrass) 
	{
		this.numInitGrass = numInitGrass;
	}

	/**
	 * Getter for 'grassGrowthRate'
	 * @return Rate the grass should grow
	 */	
	public double getGrassGrowthRate() 
	{
		return grassGrowthRate;
	}

	/**
	 * Setter for 'grassGrowthRate'
	 * @param grassGrowthRate
	 */	
	public void setGrassGrowthRate(double grassGrowthRate) 
	{
		this.grassGrowthRate = grassGrowthRate;
	}

	/**
	 * Getter for 'birthThreshold'
	 * @return Grass threshold when reproduction should happen
	 */	
	public int getBirthThreshold() 
	{
		return birthThreshold;
	}

	/**
	 * Setter for 'birthThreshold'
	 * @param birthThreshold
	 */	
	public void setBirthThreshold(int birthThreshold) 
	{
		this.birthThreshold = birthThreshold;
	}
	
	
	
}
