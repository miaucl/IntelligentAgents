package template;

import logist.plan.Action;
import logist.task.Task;
import logist.topology.Topology.City;

public class TaskAction 
{
	private static int count = 0;
	
	private Task task;
	private TaskAction linkedTaskAction;
	private TaskActionType type;
	private int id;
	
	TaskAction(Task task, TaskActionType type)
	{
		this.task = task;
		this.type = type;
		id = count;
		count++;
	}

	public Task getTask() 
	{
		return task;
	}

	public TaskActionType getType() 
	{
		return type;
	}
	
	
	public int getId() 
	{
		return this.id;
	}

	public TaskAction getLinkedTaskAction() 
	{
		return linkedTaskAction;
	}

	public void setLinkedTaskAction(TaskAction linkedTaskAction) 
	{
		this.linkedTaskAction = linkedTaskAction;
	}

	public City getCity() 
	{
		return (type == TaskActionType.Pickup) ? this.task.pickupCity : this.task.deliveryCity;
	}
	
}
