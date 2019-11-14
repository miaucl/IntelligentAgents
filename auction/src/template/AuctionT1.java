package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class AuctionT1 implements AuctionBehavior  
{
	private String name = "T1";

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private long timeout_bid;
    
	private Random random;
	private double lastCost;
	private double lastCostProposed;
	
	private ArrayList<Task> myAcceptedTasks;
	private ArrayList<Task> hisAcceptedTasks;
	
	private long myTaskRewards;
	private long hisTaskRewards;
	
	private ArrayList<Long> myBids;
	private ArrayList<Long> hisBids;
	
    
    private static final double P = 0.8; // Probability to pick old solution instead of new permutation
    private static final int N = 10; // Number of solution space permutations calculated per iteration
    private static final int M = 1000; // The averaging filter applied to the cost of the solutions
    private static final double K = 2000; // Max tries to find best solution	

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) 
	{
		
		LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        // the bid method cannot execute more than timeout_bid milliseconds
        timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		
		this.lastCost = 0;
		this.lastCostProposed = 0;
		this.myAcceptedTasks = new ArrayList<Task>();
		this.hisAcceptedTasks = new ArrayList<Task>();
		this.myBids = new ArrayList<Long>();
		this.hisBids = new ArrayList<Long>();
		this.myTaskRewards = 0;
		this.hisTaskRewards = 0;
		
		
		
		
		long seed = -9019554669489983951L * agent.id();
		this.random = new Random(seed);
	}
	
	public void auctionResult(Task previous, int winner, Long[] bids) 
	{
		myBids.add(bids[agent.id()]);
		hisBids.add(bids[agent.id() == 0 ? 1 : 0]); // Assuming only 2 agents play
		if (winner == agent.id()) 
		{
			myTaskRewards += previous.reward;
			myAcceptedTasks.add(previous); // add the task definitively
			lastCost = lastCostProposed;
	
			
		}
		else
		{
			hisTaskRewards += previous.reward;
			hisAcceptedTasks.add(previous);
		}
		
		//System.out.println("agentID= "+agent.id()+ " cost= "+lastBestSolution.cost()+" "+System.currentTimeMillis());
		
	}
	
	@Override
	public Long askPrice(Task task) 
	{
		boolean canCarry = false;
		for (Vehicle vehicle : agent.vehicles())
		{
			canCarry |= (vehicle.capacity() >= task.weight);
		}
		if (!canCarry)
		{
			System.out.println("Task " + task.toString() + " too heavy!");
			return null;		
		}
		

		myAcceptedTasks.add(task); //add the task to test
		Solution bestSolution = findBestSolution(agent.vehicles(),myAcceptedTasks,timeout_bid);
		myAcceptedTasks.remove(task); //remove the task
		double cost = bestSolution.cost();
		double marginalCost = cost - lastCost;
	        
		lastCostProposed = cost;

		System.out.println(name + " - " + agent.id() + "\tLast cost: " + lastCost + "\t cost: " + cost);
		double ratio = 1.05 + (random.nextDouble() * 0.1);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}
	
	public Solution findBestSolution(List<Vehicle> vehicles, ArrayList<Task> tasks, long timeout)
	{
        //System.out.println("Find best solution for " + tasks.size() + " tasks");
		timeout = Math.min(1000 * tasks.size(), timeout);// TODO REMOVE AS IT IS ONLY FOR TEST
				
		long time_start = System.currentTimeMillis(); // Get start timestamp of the planning method
        
        Solution intialSolution = createInitialSolution(vehicles, tasks); // Create the initial solution (same for all tries)            
		if (tasks.size() == 1) return intialSolution;
        
        ArrayList<Solution> solutions = new ArrayList<Solution>(); // Keep track of solutions found
        Solution  minSolution = intialSolution; // Keep the best solution over all tries
        int k = 0; 
        long oberheadTime = 1000; // 1s to finish the plan
        long maxRunTime = 0; // Keep the longest run as an estimation to stop before reaching the max time limit
        long plannedLastRun = time_start + timeout; // Timeout horizon
        while (k < K && (10 * maxRunTime + oberheadTime) < plannedLastRun - System.currentTimeMillis()) // Stop due to max k or out of time
        {
        	long k_start = System.currentTimeMillis(); // Get start timestamp of try k
        	
        	Solution solution = intialSolution; // Start from the initial solution
        	//System.out.println("k = "+ k + ", d = " + (plannedLastRun - System.currentTimeMillis()) + "ms remaining");
        
        
	        ArrayList<Double> costs = new ArrayList<Double>(); // List of the cost evolution
	        ArrayList<Double> m1s = new ArrayList<Double>(); // List of the first averaged buffer
	        ArrayList<Double> m2s = new ArrayList<Double>(); // List of the second averaged buffer
	        ArrayList<Double> mTots = new ArrayList<Double>(); // List of the average change in value over the last 2M iterations
	        
	        costs.add(solution.cost()); // Add cost of initial solution
	        int i = 0;
	        double m1 = 0;
	        double m2 = 0;
	        double mTotal = 0;
	        do
	        {
	      
	        	solution = findBestNeighborSolution(solution, solution.getPermutations(N)); // Get best permutation of current solution
	        	double cost = solution.cost(); // Calculate the cost
	        	costs.add(cost); // Add cost of current solution
	        	if (i < M) // Start filling buffer 2
	        	{
	        		m2+=cost;
	        	}
	        	else if (i < M * 2) // Also start filling buffer 1
	        	{
	        		m1+=costs.get(i-M);
	        		m2=m2-costs.get(i-M)+costs.get(i); // Update buffer 1
	        	}
	        	else
	        	{
	        		mTotal=(m1-m2)/M; // Calculate the difference
	        		m1s.add(m1);
	        		m2s.add(m2);
	        		mTots.add(mTotal);
	        		
	        		m1=m1-costs.get(i-2*M)+costs.get(i-M); // Update buffer 1
	        		m2=m2-costs.get(i-M)+costs.get(i); // Update buffer 2
	        		
	        		
	        	}
	        	i++;
		        if (solution.cost() < minSolution.cost()) // Check if current solution is better
		        {
		        	minSolution=solution;
		        }
	        } while ((mTotal >= 0 || i <= M * 2) && (10 * maxRunTime + oberheadTime) < plannedLastRun - System.currentTimeMillis()); // local minimum found and after transition phase
	        //System.out.println("m1s="+m1s.toString());
			//System.out.println("m2s="+m2s.toString());
			//System.out.println("mTots="+mTots.toString());
	        
	        //System.out.println("costs= "+costs.toString());
	        

	        solutions.add(solution); // Keep last solution (is not necessarily the best of the try)
	        
	        long k_end = System.currentTimeMillis();
	        long k_duration = k_end - k_start;
	        maxRunTime = Math.max(maxRunTime, k_duration);
	        
	        k++;
        }
        
        //System.out.println("min_cost="+minSolution.cost()); // Best solution found
        System.out.println("my_bids="+myBids);
        System.out.println("his_bids="+hisBids);
        return minSolution;
	}

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) 
    {
		Solution bestSolution = findBestSolution(agent.vehicles(),myAcceptedTasks,timeout_plan);
		
		long time_start = System.currentTimeMillis(); // Get start timestamp of the planning method

        List<Plan> plans = new ArrayList<Plan>(); // New empty plan
        
        for (Vehicle vehicle : vehicles)
        {
        	plans.add(extractPlan(bestSolution, vehicle)); // Create plans for each vehicle
        }
        
        double cost = bestSolution.cost();

        System.out.println("Plan for " + agent.vehicles().size() + " vehicles and " + myAcceptedTasks.size() + " tasks costs " + (myTaskRewards - cost));

        System.out.println(name + "\tCOST: " + cost + " REWARD: " + myTaskRewards + " => " + (myTaskRewards - cost));
        

       
        long time_end = System.currentTimeMillis();
        long time_duration = time_end - time_start;
        System.out.println("Create plan: " + time_duration + "ms");

  
        return plans;
    }
    
    /**
     * Create an initial feasible solution
     * 
     * @param vehicles The available vehicles
     * @param tasks The available tasks
     * @return The initial solution
     */
    private Solution createInitialSolution(List<Vehicle> vehicles, ArrayList<Task> tasks)
    {
    	Solution.rand = new Random(); // Class wide random generator
    	
    	Solution.taskActions = new TaskAction[tasks.size() * 2]; // Class wide list of all available tasks
    	Solution.vehicles = new Vehicle[vehicles.size()]; // Class wide list of all available vehicles
    	Solution.taskActionIndex = new HashMap<Integer, Integer>(); // Class wide map of all available task ids to the index
    	Solution.vehicleIndex = new HashMap<Integer, Integer>(); // Class wide map of all available vehicle ids to the index
    	
    	ArrayList<LinkedList<TaskAction>> chains = new ArrayList<LinkedList<TaskAction>>(); // TaskAction chains for the vehicles	
    
    	for (int i = 0; i<vehicles.size(); i++)
    	{
    		Solution.vehicles[i] = vehicles.get(i);
        	Solution.vehicleIndex.put(vehicles.get(i).id(), i);
    		
    		chains.add(new LinkedList<TaskAction>());
    	}
    	
    	
    	int i = 0;
    	int taskActionIndex = 0;
    	
    	for (Task task : tasks)
    	{
    		TaskAction pickup = new TaskAction(task, TaskActionType.Pickup); // Create the task pickup action
    		TaskAction delivery = new TaskAction(task, TaskActionType.Delivery); // Create the task delivery action
    		
    		pickup.setLinkedTaskAction(delivery); // Connect the two actions
    		delivery.setLinkedTaskAction(pickup);
   		
    		Solution.taskActions[taskActionIndex] = pickup; // Register the actions class wide
    		Solution.taskActions[taskActionIndex + 1] = delivery;
        	Solution.taskActionIndex.put(pickup.getId(), taskActionIndex);
        	Solution.taskActionIndex.put(delivery.getId(), taskActionIndex + 1);
    				
    		taskActionIndex += 2;
    		
    		boolean assigned = false;
    		int count = 0;
    		do // Find a vehicle which can carry the task
    		{
    			if (task.weight <= vehicles.get(i).capacity()) // If it is able to carry the task, it gets it
    			{
    	    		chains.get(i).add(pickup); // Directly pickup and deliver the task
    	    		chains.get(i).add(delivery);
    	    		assigned = true;
    			}
	    		i = (++i) % vehicles.size();
	    		count++;
    		}
    		while (!assigned && count < vehicles.size());
    		
    		
    		if (!assigned) // Nobody can do the job!
    		{
    			System.out.println("Task too heavy for all vehicles!");
    		}   		
    	}
    
    	
    	return new Solution(chains);
    }
    
    /**
     * Find the best permutation from the current solution
     * @param previousSolution Current solution
     * @param solutions Permutations of the current solution
     * @return The next solution
     */
    private Solution findBestNeighborSolution(Solution previousSolution, Solution[] solutions)
    {
    	Solution bestSolution = null;
    	double bestSolutionCost = 0;
    	
    	
    	for (Solution solution : solutions)
    	{
    		if (bestSolution == null || bestSolutionCost > solution.cost())  // Check if solution is better
    		{
    			bestSolution = solution;
    			bestSolutionCost = solution.cost();
    		}
       	}
    	
		return random.nextDouble() > P ? previousSolution : bestSolution; // Decide of old solution is kept or the new solution
    }
    
    /**
     * Extract the plan for a vehicle of a solution
     * 
     * @param solution The solution
     * @param vehicle The vehicle
     * @return The plan for the vehicle
     */
    private Plan extractPlan(Solution solution, Vehicle vehicle)
    {
        City current = vehicle.getCurrentCity(); // Starting city for the vehicle
        Plan plan = new Plan(current);
        
        TaskAction nextTaskAction = solution.nextTaskAction(vehicle); // Get first action for the vehicle
        
        while (nextTaskAction != null)
        {
        	for (City city : current.pathTo(nextTaskAction.getCity())) // Move to next action
        	{
                plan.appendMove(city);
            }
        	if (nextTaskAction.getType() == TaskActionType.Pickup) // Perform pickup
        	{
            	plan.appendPickup(nextTaskAction.getTask());        		
        	}
        	else // Perform delivery
        	{
            	plan.appendDelivery(nextTaskAction.getTask());        		        		
        	}
        	current = nextTaskAction.getCity(); // Update current city
        	nextTaskAction = solution.nextTaskAction(nextTaskAction); // Get next task
        }
        
        return plan;
    }
}
