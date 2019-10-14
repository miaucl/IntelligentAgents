package template;

/* import table */
import logist.simulation.Vehicle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeTemplate implements DeliberativeBehavior 
{

	enum Algorithm { BFS, ASTAR }
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) 
	{
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "BFS");
		
		// Throws IllegalArgumentException if algorithm is unknown
		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// ...
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) 
	{
		Plan plan;

		// Compute the plan with the selected algorithm.
		switch (algorithm) 
		{
		case ASTAR:
			// ...
			plan = astarPlan(vehicle, tasks);
			break;
		case BFS:
			// ...
			plan = bfsPlan(vehicle, tasks);
			break;
		default:
			throw new AssertionError("Should not happen.");
		}		
		return plan;
	}
	
	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) 
	{
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) 
		{
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}

	private Plan bfsPlan(Vehicle vehicle, TaskSet tasks) 
	{
		City current = vehicle.getCurrentCity(); // Start at current city
		Plan plan = new Plan(current); // Create a plan
		
        Deque<State> remainingStateDeque = new LinkedList<State>(); // The queue of remaining states to process (FIFO)
        
        remainingStateDeque.add(new State(current, vehicle, vehicle.capacity(), TaskSet.copyOf(tasks), TaskSet.noneOf(tasks), 0)); // Add the initial state
		
		State optimalState = null; // Keep track of the optimal solution found now
		
		State state;

		int i = 0;
		while (!remainingStateDeque.isEmpty()) // Loop until all states processed
		{
			state = remainingStateDeque.poll(); // Get next state
			
			System.out.println("i=" + ++i + ", l=" + remainingStateDeque.size() + ", c=" + state.getCost());
			if (optimalState != null && optimalState.getCost() <= state.getCost()) // Ignore if already too expensive and a better solution found
			{
			}
			else if (state.isGoal()) // Pick the new best solution and keep it
			{
				System.out.println("Solution found with cost: " + state.getCost());
				optimalState = state;
			}
			else // Get all successors
			{
				remainingStateDeque.addAll(state.possibleNextStates()); // Add the successors to the FIFO
			}
		}
		
		for (State s : remainingStateDeque)
		{
			System.out.println(s.getTodoTasks().size() + ", " + s.getCarriedTasks().size());
		}
		
		System.out.println("Plan:");
		for (Action action : optimalState.getHistory())
		{
			plan.append(action);
			System.out.println(action.toString());
		}
		
		
		return plan;
	}

	private Plan astarPlan(Vehicle vehicle, TaskSet tasks) 
	{
		City current = vehicle.getCurrentCity(); // Start at current city
		Plan plan = new Plan(current); // Create a plan
		
		LinkedList<State> remainingStateDeque = new LinkedList<State>(); // The queue of remaining states to process (FIFO)
        
        remainingStateDeque.add(new State(current, vehicle, vehicle.capacity(), TaskSet.copyOf(tasks), TaskSet.noneOf(tasks), 0)); // Add the initial state
		
		State optimalState = null; // Keep track of the optimal solution found now
		
		State state;

		int i = 0;
		while (!remainingStateDeque.isEmpty()) // Loop until all states processed
		{
			state = remainingStateDeque.poll(); // Get next state
			
			System.out.println("i=" + ++i + ", l=" + remainingStateDeque.size() + ", c=" + state.getCost()+ ",h=" + state.getHeuristic());
			if (optimalState != null && optimalState.getCost() <= state.getCost()) // Ignore if already too expensive and a better solution found
			{
			}
			else if (state.isGoal()) // Pick the new best solution and keep it
			{
				System.out.println("Solution found with cost: " + state.getCost());
				optimalState = state;
			}
			else // Get all successors
			{
				LinkedList<State> sortedSuccessorStates = state.possibleNextStates();
				Collections.sort(sortedSuccessorStates); // Sort the successor states based on the cost and the heuristic
				insertSortedSuccessorStates(remainingStateDeque, sortedSuccessorStates); // Insert the sorted successor states into the remain states queue
			}
			
			//if (i == 1000) break;
		}
		
		for (State s : remainingStateDeque)
		{
			System.out.println(s.getTodoTasks().size() + ", " + s.getCarriedTasks().size());
		}
		
		System.out.println("Plan:");
		for (Action action : optimalState.getHistory())
		{
			plan.append(action);
			System.out.println(action.toString());
		}
		
		
		return plan;
	}
	
	private void insertSortedSuccessorStates(LinkedList<State> remainingStateDeque, LinkedList<State> sortedSuccessorStates)
	{
		State state;
		int i = 0;
		while (!sortedSuccessorStates.isEmpty())
		{
			state = sortedSuccessorStates.poll(); // Get next state to insert
			while (i < remainingStateDeque.size() && remainingStateDeque.get(i).compareTo(state) < 0) i++; // Compare the state with each element in the list
			remainingStateDeque.add(i, state); // Insert the state at the given index
		}
	}

	@Override
	public void planCancelled(TaskSet carriedTasks) 
	{
		
		if (!carriedTasks.isEmpty()) 
		{
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
