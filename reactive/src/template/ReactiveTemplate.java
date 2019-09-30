package template;

import java.util.Arrays;
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

public class ReactiveTemplate implements ReactiveBehavior 
{

	private Random random;
	private double pPickup;
	private int numActions;
	private int numStates;
	private Agent myAgent;

	private double[][] R;
	private double[][][] T;
	
	private double[][] Q;
	private double[] V;
	private int[] B;
	

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		int sizeCities = topology.size();

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = sizeCities * 2;
		this.numStates = sizeCities * 2;
		this.myAgent = agent;	
		
		R = new double[sizeCities*2][sizeCities*2]; // Init with rewards
		T = new double[sizeCities*2][sizeCities*2][sizeCities*2]; // Init with probabilities
		Q = new double[sizeCities*2][sizeCities*2]; // Init at zero
		V = new double[sizeCities*2]; // Init with random values
		B = new int[sizeCities*2]; // Init with random actions
		
		// Init R
		for (int i = 0; i < R.length; i++)
		{
			int cityIndex = i%sizeCities;
			boolean availablePacket = i>=sizeCities;
			City source = topology.cities().get(cityIndex);
			
			for (int j = 0; j < R[i].length; j++)
			{
				int actionIndex = j%sizeCities;
				boolean actionPick = j>=sizeCities;
				
				City dest = topology.cities().get(actionIndex); // Get the destination of the delivery
				
				R[i][j]= Double.NEGATIVE_INFINITY;
				
				if (availablePacket && actionPick) //if packet available and pickAndDeliver action
				{
					R[i][j]=td.reward(source, dest);
				}
				else if(source.hasNeighbor(dest)) //Moving is free
				{
					R[i][j]=0;	//-1?
				}
			} 
		} 
		
		
		
		System.out.println("R=" + Arrays.deepToString(R));
		
		// Init T
		for (int fromState = 0; fromState < T.length; fromState++)
		{			
			int fromStateCityIndex = fromState % sizeCities;
			City fromStateCity = topology.cities().get(fromStateCityIndex);
			//boolean availablePacketFrom = fromState>=sizeCities;
			
			for (int action = 0; action < T[fromState].length; action++)
			{
				int actionCityIndex = action % sizeCities;
				
				for (int toState = 0; toState < T[fromState][action].length; toState++)
				{
					int toStateCityIndex = toState % sizeCities;
					City toStateCity = topology.cities().get(toStateCityIndex);
					boolean availablePacketTo = toState>=sizeCities;
					
					// The destination of an action is 100%, so 0% for every other city
					if (!fromStateCity.hasNeighbor(toStateCity) || toStateCityIndex != actionCityIndex) 
					{
						T[fromState][action][toState] = 0;
					}
					else // Valid move
					{
						if (availablePacketTo) // Probability picking there is a package
						{
							T[fromState][action][toState] = 1 - td.probability(toStateCity, null);
						}
						else // Probability picking and there is no package
						{
							T[fromState][action][toState] = td.probability(toStateCity, null);
						}
					}			
				}
			}
		}
		
		System.out.println("T=" + Arrays.deepToString(T));
		
		// Init Q
		
		
		System.out.println("Q=" + Arrays.deepToString(Q));

		// Init V
		for (int i = 0; i < V.length; i++) 
		{
			V[i] = random.nextDouble() * 1; // TODO Need a scaling factor
		}

		System.out.println("V=" + Arrays.toString(V));

		// Init B
		for (int i = 0; i < B.length; i++) 
		{
			B[i] = random.nextInt(numActions);
		}

		System.out.println("B=" + Arrays.toString(B));
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
