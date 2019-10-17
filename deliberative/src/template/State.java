package template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.plan.Action.Delivery;

public class State implements Comparable<State>
{
	// State variables
	private City city;
	private Vehicle vehicle;
	private int remainingCapacity;
	private TaskSet todoTasks;
	private TaskSet carriedTasks;
	
	
	// Measurements
	private int cost;
	
	// Heuristics
	private double heuristic;
	
	// History
	private ArrayList<Action> history;
	
	public State(City city, Vehicle vehicle, int remainingCapacity, TaskSet todoTasks, TaskSet carriedTasks, int cost)
	{
		this(city, vehicle, remainingCapacity, todoTasks, carriedTasks, cost, new ArrayList<Action>());
	}
	
	public State(City city, Vehicle vehicle, int remainingCapacity, TaskSet todoTasks, TaskSet carriedTasks, int cost, ArrayList<Action> history)
	{
		this.cost = cost;
		
		this.city = city;
		this.vehicle = vehicle;
		this.remainingCapacity = remainingCapacity;
		this.todoTasks = todoTasks;
		this.carriedTasks = carriedTasks;
		
		this.calculateHeuristic();
		
		this.history = history;
	}
	
	public boolean isGoal()
	{
		return todoTasks.isEmpty() && carriedTasks.isEmpty();
	}
	
	public LinkedList<State> possibleNextStates()
	{
		LinkedList<State> nextStates = new LinkedList<State>();
		
						
		for (Task task : todoTasks)
		{
			if (task.pickupCity == city && remainingCapacity >= task.weight)
			{
				ArrayList<Action> nextHistory = (ArrayList<Action>) history.clone();
				City nextCity = city;
				int nextRemainingCapacity = remainingCapacity;
				TaskSet nextTodoTasks = TaskSet.copyOf(todoTasks);
				TaskSet nextCarriedTasks = TaskSet.copyOf(carriedTasks);
				int nextCost = cost;

				nextHistory.add(new Pickup(task));
				nextRemainingCapacity -= task.weight;
				nextTodoTasks.remove(task);
				nextCarriedTasks.add(task);
				
				nextStates.add(new State(	nextCity, 
											vehicle,
											nextRemainingCapacity, 
											nextTodoTasks, 
											nextCarriedTasks, 
											nextCost, 
											nextHistory));
			}
		}
		
		for (Task task : carriedTasks)
		{
			if (task.deliveryCity == city)
			{
				ArrayList<Action> nextHistory = (ArrayList<Action>) history.clone();
				City nextCity = city;
				int nextRemainingCapacity = remainingCapacity;
				TaskSet nextTodoTasks = TaskSet.copyOf(todoTasks);
				TaskSet nextCarriedTasks = TaskSet.copyOf(carriedTasks);
				int nextCost = cost;

				nextHistory.add(new Delivery(task));
				nextRemainingCapacity += task.weight;
				nextCarriedTasks.remove(task);
				
				nextStates.add(new State(	nextCity, 
											vehicle,
											nextRemainingCapacity, 
											nextTodoTasks, 
											nextCarriedTasks, 
											nextCost, 
											nextHistory));
			}
		}
		
		for (City possibleNextCity : city.neighbors())
		{
			ArrayList<Action> nextHistory = (ArrayList<Action>) history.clone();
			int nextRemainingCapacity = remainingCapacity;
			TaskSet nextTodoTasks = TaskSet.copyOf(todoTasks);
			TaskSet nextCarriedTasks = TaskSet.copyOf(carriedTasks);
			int nextCost = cost;

			nextHistory.add(new Move(possibleNextCity));
			nextCost += city.distanceTo(possibleNextCity) * vehicle.costPerKm();
			
			nextStates.add(new State(	possibleNextCity, 
										vehicle,
										nextRemainingCapacity, 
										nextTodoTasks, 
										nextCarriedTasks, 
										nextCost, 
										nextHistory));
		}

		
		return nextStates;
	}
	
	public void calculateHeuristic()
	{
		
		heuristic = 0;
		
		int NUM_HEURISTIC = 3;

		if (NUM_HEURISTIC == 1) {
			for (Task task : todoTasks)
			{
				heuristic = Math.max(heuristic, city.distanceTo(task.deliveryCity));
			}
		}
		else if (NUM_HEURISTIC == 2) {
			for (Task task : todoTasks)
			{
				heuristic = Math.max(heuristic, city.distanceTo(task.pickupCity) + task.pathLength()) ;
			}
		}
		else if (NUM_HEURISTIC == 3) {
			for (Task task : todoTasks)
			{
				heuristic = Math.max(heuristic, city.distanceTo(task.pickupCity) + task.pathLength());	
			}
			
			for (Task task : carriedTasks)
			{
				heuristic = Math.max(heuristic, city.distanceTo(task.deliveryCity)) ;	
			}
			
		}
		
		
		
		heuristic*= vehicle.costPerKm();
		
	}
	
	public City getCity() 
	{
		return city;
	}

	public int getRemainingCapacity()
	{
		return remainingCapacity;
	}

	public TaskSet getTodoTasks() 
	{
		return todoTasks;
	}

	public TaskSet getCarriedTasks() 
	{
		return carriedTasks;
	}

	public int getCost() 
	{
		return cost;
	}
	
	public double getHeuristic() 
	{
		return heuristic;
	}

	
	public ArrayList<Action> getHistory() 
	{
		return history;
	}

	@Override
	public int compareTo(State o) 
	{
		return (int)Math.floor(cost + heuristic - (o.getCost() + o.getHeuristic()));
	}

}
