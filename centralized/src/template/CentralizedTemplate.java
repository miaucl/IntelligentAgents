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
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    
    private static final double P = 0.7;
    private static final int N = 10;
    private static final int M = 1000;
    private static final double K = 1000;
    
	private Random rand = new Random();
    
    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        
        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        
        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        //Solution.taskActions=new TaskAction[3];

        long time_start = System.currentTimeMillis();
        
		/*System.out.println("Agent " + agent.id() + " has tasks " + tasks);
        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }*/
        
        Solution intialSolution = createInitialSolution(vehicles, tasks);
        ArrayList<Solution> solutions = new ArrayList<Solution>();
        Solution  minSolution = intialSolution;
        for (int k = 0; k < K; k++)
        {
        	Solution solution = intialSolution;
        	System.out.println("k="+k);
        
        
	        ArrayList<Double> costs = new ArrayList<Double>();
	        ArrayList<Double> m1s = new ArrayList<Double>();
	        ArrayList<Double> m2s = new ArrayList<Double>();
	        ArrayList<Double> mTots = new ArrayList<Double>();
	        costs.add(solution.cost());
	        int i = 0;
	        double m1 = 0;
	        double m2 = 0;
	        double mTotal = 0;
	        do
	        {
	      
	        	solution = findBestSolution(solution, solution.getPermutations(N));
	        	double cost = solution.cost();
	        	costs.add(cost);
	        	if (i < M)
	        	{
	        		m2+=cost;
	        	}
	        	else if (i < M * 2)
	        	{
	        		m1+=costs.get(i-M);
	        		m2=m2-costs.get(i-M)+costs.get(i);
	        	}
	        	else
	        	{
	        		mTotal=(m1-m2)/M;
	        		m1s.add(m1);
	        		m2s.add(m2);
	        		mTots.add(mTotal);
	        		
	        		m1=m1-costs.get(i-2*M)+costs.get(i-M);
	        		m2=m2-costs.get(i-M)+costs.get(i);
	        		
	        		
	        	}
	        	/*
	        	if(i >= 2*M) 
	        	{ 
		        	m1 = 0;
		        	m2 = 0;
		        	for (int j = 0; j < M; j++)
		        	{
		        		
		        		m1+=costs.get(i-2*M+j);
		        		m2+=costs.get(i-M+j);
		        	}
		        	m1/=M;
		        	m2/=M;
		        	m1s.add(m1);
		        	m2s.add(m2);
		        	mTots.add(m1-m2);
	        	}*/
	        	i++;
		        if (solution.cost()<minSolution.cost())
		        {
		        	minSolution=solution;
		        }
	        } while (mTotal >= 0 || i <= M * 2); //local min found and after transition phase
//	        System.out.println("m1s="+m1s.toString());
//			System.out.println("m2s="+m2s.toString());
//			System.out.println("mTots="+mTots.toString());
//	        
//	        System.out.println("costs= "+costs.toString());
	        

	        solutions.add(solution);
	        
        }
        System.out.println("min cost="+minSolution.cost());
        System.out.print("solution=[");
        for (Solution sol : solutions)
        {
        	System.out.print(sol.cost() + " ");
        }
        System.out.println("];");
        List<Plan> plans = new ArrayList<Plan>();
        
        for (Vehicle vehicle : vehicles)
        {
        	plans.add(extractPlan(minSolution, vehicle));
        }
        
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        return plans;
    }
    
    private Solution createInitialSolution(List<Vehicle> vehicles, TaskSet tasks)
    {
    	Solution.rand = new Random();
    	
    	Solution.taskActions = new TaskAction[tasks.size() * 2];
    	Solution.vehicles = new Vehicle[vehicles.size()];
    	Solution.taskActionIndex = new HashMap<Integer, Integer>();
    	Solution.vehicleIndex = new HashMap<Integer, Integer>();
    	
    	ArrayList<LinkedList<TaskAction>> chains = new ArrayList<LinkedList<TaskAction>>();    	
    
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
    		//System.out.println(task.toString());
    		TaskAction pickup = new TaskAction(task, TaskActionType.Pickup);
    		TaskAction delivery = new TaskAction(task, TaskActionType.Delivery);
    		
    		pickup.setLinkedTaskAction(delivery);
    		delivery.setLinkedTaskAction(pickup);
   		
    		Solution.taskActions[taskActionIndex] = pickup;
    		Solution.taskActions[taskActionIndex + 1] = delivery;
        	Solution.taskActionIndex.put(pickup.getId(), taskActionIndex);
        	Solution.taskActionIndex.put(delivery.getId(), taskActionIndex + 1);
    				
    		taskActionIndex += 2;
    		
    		boolean assigned = false;
    		int count = 0;
    		do
    		{
    			if (task.weight <= vehicles.get(i).capacity())
    			{
    	    		chains.get(i).add(pickup);
    	    		chains.get(i).add(delivery);
    	    		assigned = true;
    			}
	    		i = (++i) % vehicles.size();
	    		count++;
    		}
    		while (!assigned && count < vehicles.size());
    		
    		
    		if (!assigned)
    		{
    			System.out.println("Task too heavy for all vehicles!");
    		}   		
    	}
    
    	
    	return new Solution(chains);
    }
    
    private Solution findBestSolution(Solution previousSolution, Solution[] solutions)
    {
    	Solution bestSolution = null;
    	double bestSolutionCost = 0;
    	
    	
    	// TODO pick at random
    	for (Solution solution : solutions)
    	{
    		//System.out.println("Sol cost:" + solution.cost());
    		if (bestSolution == null || bestSolutionCost > solution.cost()) 
    		{
    			bestSolution = solution;
    			bestSolutionCost = solution.cost();
    		}
       	}
    	
		return rand.nextDouble() > P ? previousSolution : bestSolution;
    }
    
    private Plan extractPlan(Solution solution, Vehicle vehicle)
    {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);
        
        TaskAction nextTaskAction = solution.nextTaskAction(vehicle);
        
        while (nextTaskAction != null)
        {
        	for (City city : current.pathTo(nextTaskAction.getCity())) 
        	{
                plan.appendMove(city);
            }
        	if (nextTaskAction.getType() == TaskActionType.Pickup)
        	{
            	plan.appendPickup(nextTaskAction.getTask());        		
        	}
        	else
        	{
            	plan.appendDelivery(nextTaskAction.getTask());        		        		
        	}
        	current = nextTaskAction.getCity();
        	nextTaskAction = solution.nextTaskAction(nextTaskAction);
        }
        
        return plan;
    }


    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}
