package src.template;

import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class SemiRandomAgent implements ReactiveBehavior 
{

	private Random random;
	private int nbActions;
	private Agent myAgent;


	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) 
	{
		

	}
	

	@Override
	public Action act(Vehicle vehicle, Task availableTask) 
	{
		nbActions++;
		
		Action action = null;

		
		if (availableTask == null || (nbActions > 10 && availableTask.reward < vehicle.getReward() / (nbActions+1)   ) )
		{
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));		
		} 
		else 
		{
			action = new Pickup(availableTask);
		}
		
			
		if (nbActions % 1000 == 0) 
		{
			System.out.println("The total profit after "+nbActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)nbActions)+")");
		}

		return action;
	}

}
