package template;

import logist.task.Task;
import logist.topology.Topology.City;

public class TaskAction 
{
	private static int count = 0; // The id
	
	private Task task;
	private TaskAction linkedTaskAction;
	private TaskActionType type;
	private int id;
	
	/**
	 * Initialize an action with the corresponding task and type
	 * 
	 * @param task
	 * @param type
	 */
	TaskAction(Task task, TaskActionType type)
	{
		this.task = task;
		this.type = type;
		id = count;
		count++;
	}

	/**
	 * Get the task
	 * 
	 * @return The task
	 */
	public Task getTask() 
	{
		return task;
	}

	/**
	 * Get the task type
	 * 
	 * @return The task type
	 */
	public TaskActionType getType() 
	{
		return type;
	}
	
	
	/**
	 * Get the action id
	 * 
	 * @return The id
	 */
	public int getId() 
	{
		return this.id;
	}

	/**
	 * Get the link task action
	 * 
	 * @return The linked task action
	 */
	public TaskAction getLinkedTaskAction() 
	{
		return linkedTaskAction;
	}

	/**
	 * Set the linked task action
	 * 
	 * @param linkedTaskAction The linked task action
	 */
	public void setLinkedTaskAction(TaskAction linkedTaskAction) 
	{
		this.linkedTaskAction = linkedTaskAction;
	}

	/**
	 * Get the city of the task
	 * 
	 * @return The city
	 */
	public City getCity() 
	{
		return (type == TaskActionType.Pickup) ? this.task.pickupCity : this.task.deliveryCity;
	}
	
	@Override
	public String toString()
	{
		return type.toString() + "_" + task.toString();
	}
	
	/**
	 * Create a short string representation
	 * 
	 * @return The string
	 */
	public String toShortString()
	{
		return (type == TaskActionType.Pickup ? "P" : "D") + "_" + task.id;
	}
	
}
