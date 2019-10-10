package template;

import java.util.ArrayList;

import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.plan.Action.Delivery;

public class State 
{
	// State variables
	private City city;
	private int remainingCapacity;
	private TaskSet todoTasks;
	private TaskSet carriedTasks;
	
	
	// Measurements
	private int cost;
	
	// History
	private ArrayList<Action> history;
	
	public State(City city, int remainingCapacity, TaskSet todoTasks, TaskSet carriedTasks, int cost)
	{
		this(city, remainingCapacity, todoTasks, carriedTasks, cost, new ArrayList<Action>());
	}
	
	public State(City city, int remainingCapacity, TaskSet todoTasks, TaskSet carriedTasks, int cost, ArrayList<Action> history)
	{
		this.cost = cost;
		
		this.city = city;
		this.remainingCapacity = remainingCapacity;
		this.todoTasks = todoTasks;
		this.carriedTasks = carriedTasks;
		
		this.history = history;
	}
	
	public boolean isGoal()
	{
		return todoTasks.isEmpty() && carriedTasks.isEmpty();
	}
	
	public ArrayList<State> possibleNextStates()
	{
		ArrayList<State> nextStates = new ArrayList<State>();
		
						
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
				nextCost -= task.reward;
				
				nextStates.add(new State(	nextCity, 
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
			nextCost += city.distanceTo(possibleNextCity);
			
			nextStates.add(new State(	possibleNextCity, 
										nextRemainingCapacity, 
										nextTodoTasks, 
										nextCarriedTasks, 
										nextCost, 
										nextHistory));
		}

		
		return nextStates;
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
	
	public ArrayList<Action> getHistory() 
	{
		return history;
	}

}
