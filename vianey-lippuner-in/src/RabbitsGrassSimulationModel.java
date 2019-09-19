import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.gui.DisplaySurface;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author Vianey, Lippuner
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

	@Override
	public void begin()
	{
	    buildModel();
	    buildSchedule();
	    buildDisplay();
	}

	public void buildModel()
	{
		System.out.println("Build model");
	    grassSpace = new RabbitsGrassSimulationSpace(gridSize, gridSize);
	    grassSpace.spreadGrass(numInitGrass);

	}
	
	public void buildSchedule()
	{
		System.out.println("Build schedule");
	}
	
	public void buildDisplay()
	{
		System.out.println("Build display");
	}

	@Override
	public String[] getInitParam() 
	{
		// TODO Auto-generated method stub
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold" };
		return params;
	}

	@Override
	public String getName() 
	{
		return "Rabbit Grass Simulation";
	}

	@Override
	public Schedule getSchedule() 
	{
		return schedule;
	}

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

	public int getGridSize() 
	{
		return gridSize;
	}

	public void setGridSize(int gridSize) 
	{
		this.gridSize = gridSize;
	}

	public int getNumInitRabbits() 
	{
		return numInitRabbits;
	}

	public void setNumInitRabbits(int numInitRabbits) 
	{
		this.numInitRabbits = numInitRabbits;
	}

	public int getNumInitGrass() 
	{
		return numInitGrass;
	}

	public void setNumInitGrass(int numInitGrass) 
	{
		this.numInitGrass = numInitGrass;
	}

	public double getGrassGrowthRate() 
	{
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(double grassGrowthRate) 
	{
		this.grassGrowthRate = grassGrowthRate;
	}

	public int getBirthThreshold() 
	{
		return birthThreshold;
	}

	public void setBirthThreshold(int birthThreshold) 
	{
		this.birthThreshold = birthThreshold;
	}
	
	
	
}
