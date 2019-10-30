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
        
        Solution solution = createInitialSolution(vehicles, tasks);
        
        int i = 0;
        do
        {
        	solution = findBestSolution(solution, solution.getPermutations(N));
        	System.out.println("ITERATION: " + ++i);
        } while (i < 1000);
        
        List<Plan> plans = new ArrayList<Plan>();
        
        for (Vehicle vehicle : vehicles)
        {
        	plans.add(extractPlan(solution, vehicle));
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        return plans;
    }
    
    private Solution createInitialSolution(List<Vehicle> vehicles, TaskSet tasks)
    {
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
    		System.out.println(task.toString());
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
    		if (bestSolution == null || bestSolutionCost > solution.cost()) bestSolution = solution;
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
