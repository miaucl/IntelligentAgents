package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution {

	static final int maxPermutationTries = 10;

	static public TaskAction[] taskActions = null; // All task actions available
	static public Vehicle[] vehicles = null; // All vehicles available
	
	static public HashMap<Integer,Integer> taskActionIndex; // Generic object id to index
	static public HashMap<Integer,Integer> vehicleIndex; // Generic object id to index
	
	
	private LinkedList<TaskAction>[] chainMap; // chain of vehicle
	private Integer[] timeMap; // time of action
	private Vehicle[] vehicleMap; // vehicle of action

	Random rand = new Random();
	
	
	Solution(LinkedList<TaskAction>[] chainMap)
	{
	  	Integer timeMap[] = new Integer[chainMap.length];
    	Vehicle vehicleMap[] = new Vehicle[chainMap.length];
  
		this.chainMap = chainMap;
		this.timeMap = timeMap;
		this.vehicleMap = vehicleMap;
		
    	updateTime();
    	updateVehicle();

	}
		
	Solution(
			LinkedList<TaskAction>[] chainMap, 
			Integer[] timeMap, 
			Vehicle[] vehicleMap)
	{
		this.chainMap = chainMap;
		this.timeMap = timeMap;
		this.vehicleMap = vehicleMap;
	}
		
	
	public Solution[] getPermutations(int n)
	{
		Solution[] permutations = new Solution[n];
		for (int i = 0; i<n; i++)
		{
			// Duplicate
			permutations[i] = new Solution(
					chainMap.clone(),
					timeMap.clone(),
					vehicleMap.clone());
			permutations[i].permute(); // Permute the solution
		}
		return permutations;
	}
	
	public void permute()
	{
		permute(rand.nextInt(2)); // Choose random permutation mode
	}
	
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
				LinkedList<TaskAction> list = chainMap[vehicleIndex.get(vehicle.id())];
				
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
				LinkedList<TaskAction> list = chainMap[vehicleIndex.get(vehicle.id())];

				
				int r3 = rand.nextInt(vehicles.length); // Choose random vehicle action

				Vehicle appendToVehicle = vehicles[r3];
				LinkedList<TaskAction> appendToList = chainMap[vehicleIndex.get(appendToVehicle.id())];

				TaskAction pickupTaskAction = (taskAction.getType() == TaskActionType.Pickup) ? taskAction : linkedTaskAction;
				TaskAction deliveryTaskAction = (taskAction.getType() == TaskActionType.Delivery) ? taskAction : linkedTaskAction;
				
				list.remove(time(pickupTaskAction).intValue());
				list.remove(time(deliveryTaskAction).intValue());
				
				appendToList.add(pickupTaskAction);
				appendToList.add(deliveryTaskAction);
				
				// Update the time table
				appendTime(appendToVehicle, pickupTaskAction);
				appendTime(appendToVehicle, deliveryTaskAction);
				updateTime(vehicle);
				
				return;
			}	
		}
		
		System.out.println("No permutation found!");
	}

	
	public boolean validateConstraints()
	{
		boolean valid = true;
		
		for (Vehicle v : vehicles) // Check all vehicle paths
		{
			valid &= validateConstraints(v);
		}
		
		return valid;
	}

	public boolean validateConstraints(Vehicle v)
	{
		int remainingCapacity = v.capacity();
		ArrayList<Task> vehicleTasks = new ArrayList<Task>();
		
		for(TaskAction currentTaskAction : chainMap[vehicleIndex.get(v.id())])
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

	public void updateVehicle()
	{
		
		for (Vehicle v : vehicles) // Update all vehicle paths
		{
			updateVehicle(v);
		}
	}

	private void updateVehicle(Vehicle v)
	{				
		for(TaskAction currentTaskAction : chainMap[vehicleIndex.get(v.id())])
		{
			vehicleMap[taskActionIndex.get(currentTaskAction.getId())] = v; // Set vehicle of task action
		}
	}

	public void updateTime()
	{
		
		for (Vehicle v : vehicles) // Update all vehicle paths
		{
			updateTime(v);
		}
	}

	private void updateTime(Vehicle v)
	{		
		int i = 0;
		
		for(TaskAction currentTaskAction : chainMap[vehicleIndex.get(v.id())])
		{
			timeMap[taskActionIndex.get(currentTaskAction.getId())] = Integer.valueOf(i++); // Set time of task action
		}
	}

	private void swapTime(TaskAction t1, TaskAction t2)
	{
		int t1Time = time(t1);
		int t2Time = time(t2);
		timeMap[taskActionIndex.get(t1.getId())] = t2Time;
		timeMap[taskActionIndex.get(t2.getId())] = t1Time;
	}

	private void appendTime(Vehicle v, TaskAction t)
	{
		timeMap[taskActionIndex.get(t.getId())] = chainMap[vehicleIndex.get(v.id())].size();
	}
	
	
	public double cost()
	{
		double accumulatedCost = 0.0;
		
		for (Vehicle v : vehicles) // Accumulate all vehicle paths
		{
			accumulatedCost += dist(v) * cost(v);
		}
		
		return accumulatedCost;
	}

	public double cost(Vehicle v)
	{
		return v.costPerKm();
	}
	
	public double dist(Vehicle v)
	{
		double accumulatedDist = 0.0;

		City currentCity = v.homeCity();
		for(TaskAction nextTaskAction : chainMap[vehicleIndex.get(v.id())])
		{
			City nextCity = nextTaskAction.getCity();
			accumulatedDist += currentCity.distanceTo(nextCity);
			
			currentCity = nextCity;
		}

		return accumulatedDist;
	}
	
	public double dist(TaskAction t1, TaskAction t2)
	{
		return t2 == null ? 0 : t1.getCity().distanceTo(t2.getCity());
	}

	public double dist(Vehicle v, TaskAction t)
	{
		return t == null ? 0 : v.homeCity().distanceTo(t.getCity());
	}

	
	
	public TaskAction nextTaskAction(TaskAction taskAction)
	{
		Vehicle vehicle = vehicle(taskAction);
		LinkedList<TaskAction> list = chainMap[vehicleIndex.get(vehicle.id())];
		Integer t = time(taskAction) + 1;
		
		return t >= list.size() ? null : list.get(t);
	}
	public TaskAction nextTaskAction(Vehicle vehicle)
	{
		LinkedList<TaskAction> list = chainMap[vehicleIndex.get(vehicle.id())];
		
		return list.isEmpty() ? null : list.get(0);
	}
	public Integer time(TaskAction taskAction)
	{
		return timeMap[taskActionIndex.get(taskAction.getId())];
	}
	public Vehicle vehicle(TaskAction taskAction)
	{
		return vehicleMap[taskActionIndex.get(taskAction.getId())];
	}

}
