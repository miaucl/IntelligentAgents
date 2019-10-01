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
	private int numCities;
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

		this.random = new Random();
		this.pPickup = discount;
		this.numCities = topology.size();
		this.numActions = numCities + 1; // Last action is pick
		this.numStates = numCities * 2;
		this.myAgent = agent;	
		
		R = new double[numStates][numActions]; // Init with rewards
		T = new double[numStates][numActions][numStates]; // Init with probabilities
		Q = new double[numStates][numActions]; // Init at zero
		V = new double[numStates]; // Init with random values
		B = new int[numStates]; // Init with random actions
		
		// Init R
		for (int i = 0; i < R.length; i++) // Loop all states
		{
			int cityIndex = i % numCities; // The source city in the state
			boolean availablePacket = (i >= numCities); // The flag for a package in the state
			City source = topology.cities().get(cityIndex); // The city reference of the state
			
			for (int j = 0; j < R[i].length; j++) // Loop all actions
			{
				int actionIndex = j % numCities; // The destination city of the action
				boolean actionPick = (j >= numCities); // Whether the action is pick instead of move
				
				R[i][j]= Double.NEGATIVE_INFINITY; // Init the reward with -inf as this represents all the virtual edges which are forbidden

				
				if (availablePacket && actionPick) // Use the average reward for the pick action with packet
				{
					R[i][j] = 0; // Init the average reward for a pick action at the source
					
					for (int k = 0; k<numCities; k++) // Loop all destination cities
					{
						R[i][j] += td.reward(source, topology.cities().get(k)); // Add the reward for a package from source to dest
					}
					R[i][j] /= numCities; // Make the average of all rewards assuming no non-empty city list
				}
				else // Move action
				{
					City dest = topology.cities().get(actionIndex); // Get the destination of the delivery
					
					if(source.hasNeighbor(dest)) // Valid edge (not virtual)
					{
						R[i][j]=0;	//Moving is free, should it be? TODO Maybe set to -1
					}
				}
			} 
		} 
		
		
		
		System.out.println("R=" + Arrays.deepToString(R));
		
		// Init T
		for (int fromState = 0; fromState < T.length; fromState++) // Loop all source cities from the state
		{			
			int fromStateCityIndex = fromState % numCities; // The source city in the state
			boolean availablePacketFrom = (fromState >= numCities); // The flag for a package in the state
			City fromStateCity = topology.cities().get(fromStateCityIndex); // The city reference of the state
			
			for (int action = 0; action < T[fromState].length; action++) // Loop all the actions
			{
				int actionCityIndex = action % numCities; // The source city of the action
				boolean actionPick = (action >= numCities); // Whether the action is pick

				for (int toState = 0; toState < T[fromState][action].length; toState++) // Loop all destination cities
				{
					int toStateCityIndex = toState % numCities; // The destination city in the state
					boolean availablePacketTo = (toState >= numCities); // The flag for a package in the state
					City toStateCity = topology.cities().get(toStateCityIndex); // The city reference of the state
					
					if (availablePacketFrom && actionPick) // Pick action, where the probability of arriving at a given point depends on the probabilities
					{						
						if (availablePacketTo) // Probability that there is a package at the destination and the current package goes there
						{
							T[fromState][action][toState] = td.probability(fromStateCity, toStateCity) * (1 - td.probability(toStateCity, null)); // Get the probability of arriving in that exact state
						}
						else // Probability that there is no package at the destination and the current package goes there
						{
							T[fromState][action][toState] = td.probability(fromStateCity, toStateCity) * td.probability(toStateCity, null); // Get the probability of arriving in that exact state
						}	
						// WARNING: The move row does not add up to =1, as it is the sum of the product of p(task A to B) * p(new task there) for all cells does not necessarily equal to 1 --> TODO Normalization if needed
					}
					else // Move action
					{
						// The destination of a move action is 100%, so 0% for every other city
						if (fromStateCity.hasNeighbor(toStateCity) && actionCityIndex == toStateCityIndex)  // Valid move
						{
							if (availablePacketTo) // Probability that there is a package at the destination
							{
								T[fromState][action][toState] = 1 * (1 - td.probability(toStateCity, null)); // Get the probability of arriving in that exact state
							}
							else // Probability that there is no package at the destination
							{
								T[fromState][action][toState] = 1 * td.probability(toStateCity, null); // Get the probability of arriving in that exact state
							}
						}		
					}					
				}
			}
		}
		
		System.out.println("T=" + Arrays.deepToString(T));
		System.out.println("Tn=" + Arrays.deepToString(T[11])); // Possible actions and future states for current state n
		
		// Init Q
		
		
		System.out.println("Q=" + Arrays.deepToString(Q));

		// Init V
		for (int i = 0; i < V.length; i++) 
		{
			V[i] = random.nextDouble() * 1; // Get random value // TODO Need a scaling factor
		}

		System.out.println("V=" + Arrays.toString(V));

		// Init B
		for (int i = 0; i < B.length; i++) 
		{
			B[i] = random.nextInt(numActions); // Get random action
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
