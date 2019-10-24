package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.task.Task;

public class Solution {

	static public TaskAction[] taskActions = null; // All task actions available
	static public Vehicle[] vehicles = null; // All vehicles available
	
	static private HashMap<Integer,Integer> taskActionIndex; // Generic object id to index
	static private HashMap<Integer,Integer> vehicleIndex; // Generic object id to index
	
	
	private TaskAction[] nextMap; // next action
	private TaskAction[] previousMap; // previous action
	private TaskAction[] firstMap; // first action of vehicle
	private Integer[] timeMap; // time of action
	private Vehicle[] vehicleMap; // vehicle of action

	Random rand = new Random();
	
	
	Solution(
			TaskAction[] nextMap, 
			TaskAction[] previousMap, 
			TaskAction[] firstMap, 
			Integer[] timeMap, 
			Vehicle[] vehicleMap)
	{
		this.nextMap = nextMap;
		this.previousMap = previousMap;
		this.timeMap = timeMap;
		this.vehicleMap = vehicleMap;
		this.firstMap = firstMap;
	}
		
	
	public Solution[] getPermutations(int n)
	{
		Solution[] permutations = new Solution[n];
		for (int i = 0; i<n; i++)
		{
			// Duplicate
			permutations[i] = new Solution(
					nextMap.clone(), 
					previousMap.clone(), 
					firstMap.clone(),
					timeMap.clone(),
					vehicleMap.clone());
			permutations[i].permute(i); // Permute the solution
		}
		return permutations;
	}
	
	public void permute(int i)
	{
		while (true!=false) // :)
		{
			int r1 = rand.nextInt(2);
			if (r1 == 1) // Switch order
			{
				int r2 = rand.nextInt(taskActions.length); // Choose random task action

				int taRandIndex = r2;
				TaskAction taRand = taskActions[taRandIndex];
				TaskAction taRandNext = nextMap[taRandIndex];
				int taRandNextIndex = taskActionIndex.get(taRandNext.getId());
				
				if (taRandNext == null || taRand.getTask() == taRandNext.getTask()) continue; // Not allowed
				
				// Flip the order [... N M ...] => [... M N ...]
				nextMap[taRandIndex] = nextMap[taRandNextIndex];
				nextMap[taRandNextIndex] = taRand;
				nextMap[taRandNextIndex] = previousMap[taRandIndex];
				nextMap[taRandIndex] = taRandNext;
				
				// Special case if it is the first action task
				if (previousMap[taRandIndex] == null) firstMap[taRandIndex] = taRandNext;
				
				if (validateConstraints(vehicleMap[taRandIndex])) 
				{
					updateTime(vehicleMap[taRandIndex]); // Update the time
					return; // Everything went fine!
				}
				else // Switch back
				{
					// Flip the order [... M N ...] => [... N M ...]
					nextMap[taRandNextIndex] = nextMap[taRandIndex];
					nextMap[taRandIndex] = taRandNext;
					nextMap[taRandIndex] = previousMap[taRandNextIndex];
					nextMap[taRandNextIndex] = taRand;
					
					// Special case if it is the first action task
					if (previousMap[taRandIndex] == null) firstMap[taRandIndex] = taRand;
				}
			}
			else // Switch vehicle
			{
				// TODO Implement permutation 2
			}
			
		}
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
		
		TaskAction currentTaskAction = nextTaskAction(v);
		while (currentTaskAction != null) // Go until no further action
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
		
		return true;
	}

	public void updateTime()
	{
		
		for (Vehicle v : vehicles) // Update all vehicle paths
		{
			updateTime(v);
		}
	}

	public void updateTime(Vehicle v)
	{
		int i = 0;
		
		TaskAction currentTaskAction = nextTaskAction(v);
		while (currentTaskAction != null) // Go until no further action
		{
			timeMap[taskActionIndex.get(currentTaskAction.getId())] = Integer.valueOf(i++); // Set time of task action
			
			currentTaskAction = nextMap[taskActionIndex.get(currentTaskAction.getId())];  // Next task action
		}
	}

	
	
	public TaskAction nextTaskAction(TaskAction taskAction)
	{
		return nextMap[taskActionIndex.get(taskAction.getId())];
	}
	public TaskAction nextTaskAction(Vehicle vehicle)
	{
		return nextMap[vehicleIndex.get(vehicle.id())];
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
