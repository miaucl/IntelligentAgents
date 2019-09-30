package template;

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

public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;

	private double[][] R;
	private double [][][] T;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;	
		
		int sizeCities = topology.size();
		R = new double[sizeCities*2][sizeCities+1];
		T = new double[sizeCities*2][sizeCities+1][sizeCities*2];
		
		for (int i = 0; i < R.length; i++)
		{
			int index = i%sizeCities;
			boolean availablePacket = i>=sizeCities;
			City source = topology.cities().get(index);
			
			for (int j = 0; j < R[i].length; j++)
			{
				City dest = topology.cities().get(j+1); //index 0 is pickAndDeliver action
				
				R[i][j]= Double.NEGATIVE_INFINITY;
				
				if (availablePacket && j==0) //if packet available and pickAndDeliver action
				{
					R[i][j]=td.reward(source, dest);
				}
				else if(source.hasNeighbor(dest))
				{
					R[i][j]=0;	//-1?
				}
			} 
		} 
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
}
