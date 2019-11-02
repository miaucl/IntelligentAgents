package template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution {

	static final int maxPermutationTries = 20; // The max tries to permute a solution before is abandoned
	static final double T = 0.5; // The repartition of permutation type 1 and 2

	static public TaskAction[] taskActions = null; // All task actions available
	static public Vehicle[] vehicles = null; // All vehicles available
	
	static public HashMap<Integer,Integer> taskActionIndex; // Generic object id to index
	static public HashMap<Integer,Integer> vehicleIndex; // Generic object id to index
	
	
	private ArrayList<LinkedList<TaskAction>> chainMap; // chain of vehicle
	private Integer[] timeMap; // time of action
	private Vehicle[] vehicleMap; // vehicle of action

	static public Random rand;
	
	
	/**
	 * Create a solution from lists of tasks, all other lists are generated internally
	 * 
	 * @param chainMap The list of task lists
	 */
	Solution(ArrayList<LinkedList<TaskAction>> chainMap)
	{
	  	Integer timeMap[] = new Integer[taskActions.length];
    	Vehicle vehicleMap[] = new Vehicle[taskActions.length];
    	
  
		this.chainMap = chainMap;
		this.timeMap = timeMap;
		this.vehicleMap = vehicleMap;
				
    	updateTime(); // Create the time array map
    	updateVehicle(); // Create vehicle array map

	}
		
	/**
	 * Create a solution from an existing solution
	 * 
	 * @param chainMap The list of task lists
	 * @param timeMap The time mapping
	 * @param vehicleMap The vehicle mapping
	 */
	Solution(
			ArrayList<LinkedList<TaskAction>> chainMap, 
			Integer[] timeMap, 
			Vehicle[] vehicleMap)
	{
		this.chainMap = chainMap;
		this.timeMap = timeMap;
		this.vehicleMap = vehicleMap;
	}
		
	
	/**
	 * Get n permutations of the current solution
	 * 
	 * @param n
	 * @return The permutations
	 */
	public Solution[] getPermutations(int n)
	{
		Solution[] permutations = new Solution[n];
		for (int i = 0; i<n; i++)
		{
			// Duplicate the chain list
			ArrayList<LinkedList<TaskAction>> duplicatedChainMap = new ArrayList<LinkedList<TaskAction>>();
			
			// Duplicate the chain lists
			for (LinkedList list : chainMap)
			{
				duplicatedChainMap.add((LinkedList)list.clone());
			}
			
			// Create the permutation from current solution
			permutations[i] = new Solution(
					duplicatedChainMap,
					timeMap.clone(),
					vehicleMap.clone());
			permutations[i].permute(); // Permute the solution
		}
		return permutations;
	}
	
	/**
	 * Create a random permutation
	 */
	public void permute()
	{
		permute(rand.nextDouble() > T ? 0 : 1); // Choose random permutation mode
	}
	
	/**
	 * Create a specific permutation
	 * 
	 * @param i
	 */
	public void permute(int i)
	{
		for (int tries = 0; tries<maxPermutationTries; tries++)
		{
			if (i % 2 == 0) // Switch order
			{
				int r2 = rand.nextInt(taskActions.length); // Choose random task action
				
				TaskAction currentTaskAction = taskActions[r2];
				TaskAction nextTaskAction = this.nextTaskAction(currentTaskAction); // Take NEXT task action
				
				Vehicle vehicle = vehicle(currentTaskAction);
				LinkedList<TaskAction> list = chainMap.get(vehicleIndex.get(vehicle.id()));
				
				 // Not allowed as it is the last task OR the actions are on the same task (pickup->delivery)
				if (nextTaskAction == null || nextTaskAction.getTask() == currentTaskAction.getTask()) continue;				
				
				
				// Switch the order of the task actions
				list.set(time(nextTaskAction), currentTaskAction);
				list.set(time(currentTaskAction), nextTaskAction);
				
				
				// Picking task EARLIER is critical, as it may overload the vehicle, thus it has to be checked!
				if (currentTaskAction.getType() == TaskActionType.Delivery && 
					nextTaskAction.getType() == TaskActionType.Pickup)
				{
					// Validate the vehicle task chain in term of capacity
					if (!validateConstraints(vehicle)) 
					{
						// Reset the order of the task actions
						list.set(time(currentTaskAction), currentTaskAction);
						list.set(time(nextTaskAction), nextTaskAction);	
						continue;
					}

				}
				
				// Update the time table
				swapTime(currentTaskAction, nextTaskAction);
				
				return;
			}
			else // Append to vehicle
			{
				int r2 = rand.nextInt(taskActions.length); // Choose random task action
				
				TaskAction taskAction = taskActions[r2];
				TaskAction linkedTaskAction = taskAction.getLinkedTaskAction(); // Take LINKED task action
				Vehicle vehicle = vehicle(taskAction);
				LinkedList<TaskAction> list = chainMap.get(vehicleIndex.get(vehicle.id()));

				
				int r3 = rand.nextInt(vehicles.length); // Choose random vehicle action

				Vehicle appendToVehicle = vehicles[r3];
				LinkedList<TaskAction> appendToList = chainMap.get(vehicleIndex.get(appendToVehicle.id()));

				TaskAction pickupTaskAction = (taskAction.getType() == TaskActionType.Pickup) ? taskAction : linkedTaskAction;
				TaskAction deliveryTaskAction = (taskAction.getType() == TaskActionType.Delivery) ? taskAction : linkedTaskAction;
				
				list.remove(time(deliveryTaskAction).intValue()); // First delivery, as this does not shift the index
				list.remove(time(pickupTaskAction).intValue());
				
				// Append pickup to vehicle and update lists
				appendToList.add(pickupTaskAction);
				updateVehicle(appendToVehicle, pickupTaskAction);
				appendTime(appendToVehicle, pickupTaskAction);
				
				// Append delivery to vehicle and update lists
				appendToList.add(deliveryTaskAction);
				updateVehicle(appendToVehicle, deliveryTaskAction);
				appendTime(appendToVehicle, deliveryTaskAction);
				
				// Update time of old vehicle
				updateTime(vehicle);
				
				return;
			}	
		}
		
		System.out.println("No permutation found!");
	}

	/**
	 * Validate the constraints
	 * 
	 * @return
	 */
	public boolean validateConstraints()
	{
		boolean valid = true;
		
		for (Vehicle v : vehicles) // Check all vehicle paths
		{
			valid &= validateConstraints(v);
		}
		
		return valid;
	}

	/**
	 * Validate the constraints in respect to a vehicle
	 * 
	 * @param v The vehicle
	 * @return
	 */
	public boolean validateConstraints(Vehicle v)
	{
		int remainingCapacity = v.capacity();
		ArrayList<Task> vehicleTasks = new ArrayList<Task>();
		
		for (TaskAction currentTaskAction : chainMap.get(vehicleIndex.get(v.id())))
		{
			if (currentTaskAction.getType() == TaskActionType.Pickup && // Is it a pick up
				remainingCapacity >= currentTaskAction.getTask().weight) // Still capacity left
			{
				vehicleTasks.add(currentTaskAction.getTask());
				remainingCapacity -= currentTaskAction.getTask().weight;
			}
			else if (currentTaskAction.getType() == TaskActionType.Delivery && // Is it a delivery
					 vehicleTasks.contains(currentTaskAction.getTask())) // Carrying task already
			{
				vehicleTasks.remove(currentTaskAction.getTask());
				remainingCapacity += currentTaskAction.getTask().weight;
			}
			else // Something is off
			{
				return false;
			}
		}
		
		return vehicleTasks.isEmpty();
	}

	/**
	 * Update the vehicle list
	 */
	public void updateVehicle()
	{
		
		for (Vehicle v : vehicles) // Update all vehicle paths
		{
			updateVehicle(v);
		}
	}

	/**
	 * Update the vehicle list for a vehicle
	 * 
	 * @param v The vehicle
	 */
	private void updateVehicle(Vehicle v)
	{				
		for (TaskAction currentTaskAction : chainMap.get(vehicleIndex.get(v.id())))
		{
			vehicleMap[taskActionIndex.get(currentTaskAction.getId())] = v; // Set vehicle of task action
		}
	}

	/**
	 * Update the vehicle list for a task of a vehicle
	 * 
	 * @param v The vehicle
	 * @param t The task
	 */
	public void updateVehicle(Vehicle v, TaskAction t)
	{
		vehicleMap[taskActionIndex.get(t.getId())] = v;
	}

	/**
	 * Update the time list
	 */
	public void updateTime()
	{
		
		for (Vehicle v : vehicles) // Update all vehicle paths
		{
			updateTime(v);
		}
	}

	/**
	 * Update the time list of a vehicle
	 * 
	 * @param v The vehicle
	 */
	private void updateTime(Vehicle v)
	{		
		int i = 0;
		
		for (TaskAction currentTaskAction : chainMap.get(vehicleIndex.get(v.id())))
		{
			timeMap[taskActionIndex.get(currentTaskAction.getId())] = Integer.valueOf(i); // Set time of task action
			i++;
		}
	}

	/**
	 * Swap the timing in the list of two tasks
	 * 
	 * @param t1 The task 1
	 * @param t2 The task 2
	 */
	private void swapTime(TaskAction t1, TaskAction t2)
	{
		int t1Time = time(t1);
		int t2Time = time(t2);
		timeMap[taskActionIndex.get(t1.getId())] = t2Time;
		timeMap[taskActionIndex.get(t2.getId())] = t1Time;
	}

	/**
	 * Append the task timing to the list for a task of a vehicle
	 * 
	 * @param v The vehicle
	 * @param t The task
	 */
	private void appendTime(Vehicle v, TaskAction t)
	{
		timeMap[taskActionIndex.get(t.getId())] = chainMap.get(vehicleIndex.get(v.id())).size() - 1;
	}
	
	
	/**
	 * Calculate the cost of a solution
	 * 
	 * @return
	 */
	public double cost()
	{
		double accumulatedCost = 0.0;
		
		for (Vehicle v : vehicles) // Accumulate all vehicle paths
		{
			accumulatedCost += dist(v) * cost(v);
		}
		
		return accumulatedCost;
	}

	/**
	 * Calculate the cost of a vehicle
	 * 
	 * @param v The vehicle
	 * @return
	 */
	public double cost(Vehicle v)
	{
		return v.costPerKm();
	}
	
	/**
	 * Calculate the distance of a vehicle
	 * 
	 * @param v The vehicle
	 * @return
	 */
	public double dist(Vehicle v)
	{
		double accumulatedDist = 0.0;

		City currentCity = v.homeCity();
		for(TaskAction nextTaskAction : chainMap.get(vehicleIndex.get(v.id())))
		{
			City nextCity = nextTaskAction.getCity();
			accumulatedDist += currentCity.distanceTo(nextCity);
			
			currentCity = nextCity;
		}

		return accumulatedDist;
	}
	
	/**
	 * Calculate the distance between to task actions
	 * 
	 * @param t1 The task action 1
	 * @param t2 The task action 2
	 * @return
	 */
	public double dist(TaskAction t1, TaskAction t2)
	{
		return t2 == null ? 0 : t1.getCity().distanceTo(t2.getCity());
	}

	/**
	 * Calculate the distance between a vehicle start location and a task action
	 * 
	 * @param v The vehicle
	 * @param t The task action
	 * @return
	 */
	public double dist(Vehicle v, TaskAction t)
	{
		return t == null ? 0 : v.homeCity().distanceTo(t.getCity());
	}

	
	/**
	 * Get the next task action for a task action
	 * 
	 * @param taskAction The task action
	 * @return
	 */
	public TaskAction nextTaskAction(TaskAction taskAction)
	{
		Vehicle vehicle = vehicle(taskAction);
		LinkedList<TaskAction> list = chainMap.get(vehicleIndex.get(vehicle.id()));
		Integer t = time(taskAction) + 1;
		
		return t >= list.size() ? null : list.get(t);
	}
	
	/**
	 * Get the first task action for a vehicle
	 * 
	 * @param vehicle The vehicle
	 * @return
	 */
	public TaskAction nextTaskAction(Vehicle vehicle)
	{
		LinkedList<TaskAction> list = chainMap.get(vehicleIndex.get(vehicle.id()));
		
		return list.isEmpty() ? null : list.get(0);
	}
	
	/**
	 * Fet the time of a task action
	 * 
	 * @param taskAction The task action
	 * @return
	 */
	public Integer time(TaskAction taskAction)
	{
		return timeMap[taskActionIndex.get(taskAction.getId())];
	}
	
	/**
	 * Get the vehicle of a task action
	 * 
	 * @param taskAction The task action
	 * @return
	 */
	public Vehicle vehicle(TaskAction taskAction)
	{
		return vehicleMap[taskActionIndex.get(taskAction.getId())];
	}
	
	@Override
	public String toString()
	{
		String s = "Solution: " + this.cost() + "\n"; 
		for (LinkedList<TaskAction> list : chainMap)
		{
			for (TaskAction taskAction : list)
			{
				s += taskAction.toShortString() + "\t";
			}

			s += "\n";
		}
		return s;
	}

}
