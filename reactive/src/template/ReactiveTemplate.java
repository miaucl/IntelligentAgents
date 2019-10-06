package src.template;

import java.util.ArrayList;
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
	private int nbActions;
	private int numActions;
	private int numStates;
	private Agent myAgent;


	private double[][] R;
	private double[][][] T;
	
	private double[][] Q;
	private double[] V;
	private int[] B;
	
	private ArrayList<City> cityList;
	
	private final int MAX_ITERATION = 1000000;
	private final double EPSILON = 0;
	private final double DEFAULT_DISCOUNT = 0.95;

	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) 
	{
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				DEFAULT_DISCOUNT);
		

		this.nbActions = 0;
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
		cityList = new ArrayList<City>(topology.cities());
		
		double maxReward = Double.NEGATIVE_INFINITY;
				
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
				else if (!actionPick) // Move action
				{
					City dest = topology.cities().get(actionIndex); // Get the destination of the delivery
					
					if(source.hasNeighbor(dest) && source != dest) // Valid edge (not virtual)
					{
						R[i][j]= 0;	// Moving is free, should it be?
					}
				}
				
				if (R[i][j] > maxReward)
				{
					maxReward = R[i][j];
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
						// WARNING: The move row does not add up to =1, as it is the sum of the product of p1(task A to B) * p2(new task at B) for all cells does not necessarily equal to 1 (as sum(p1(*) < 1)) --> TODO Normalization if needed
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
		System.out.println("Tn=" + Arrays.deepToString(T[1])); // Possible actions and future states for current state n

		// Init V
		for (int i = 0; i < V.length; i++) 
		{
			V[i] = random.nextDouble() * maxReward; // Get random value, scaling between 0 and maxReward
		}

		System.out.println("V=" + Arrays.toString(V));

		// Init B
		for (int i = 0; i < B.length; i++) 
		{
			B[i] = random.nextInt(numActions); // Get random action
		}

		System.out.println("B=" + Arrays.toString(B));
		
		
		
		
		// Start offboard algorithm
		int N = 0;
		boolean BhasChanged = true;
		boolean Vconverged = false;
		
		do
		{
			int Bsame = 0;
			int Vconverge = 0;
			
			for (int i = 0; i<numStates; i++)
			{
				double VmaxTemp = Double.NEGATIVE_INFINITY;
				int BmaxTemp = 0;
				
				
				for (int j = 0; j<numActions; j++)
				{
					Q[i][j] = R[i][j];
					for (int k = 0; k<numStates; k++)		 // TODO optimize???
					{
						Q[i][j] += discount * T[i][j][k] * V[k];
					}
					if (VmaxTemp < Q[i][j])
					{
						VmaxTemp = Q[i][j];
						BmaxTemp = j;
					}
				}
				
				
				if (Math.abs(V[i]- VmaxTemp) <= EPSILON)
				{	
					Vconverge++;
				}
						
				V[i] = VmaxTemp;
				
				if (B[i] != BmaxTemp)
				{
					B[i] = BmaxTemp;
				}
				else
				{
					Bsame++;
				}
				
			}
			
			System.out.println("Vconverge=" +Vconverge);
	
			if (Bsame == numStates)
			{
				BhasChanged = false;
			}
			
			if (Vconverge == numStates)
			{
				Vconverged = true;
			}
			
			if (N++ > MAX_ITERATION) // Ultimate stop criteria
			{
				System.out.printf("Stopped algorithm after MAX iterations: %d\n", MAX_ITERATION);
				break;
			}

			
		} while (BhasChanged || !Vconverged);
		
		System.out.println("N=" + N);
		System.out.println("V=" + Arrays.toString(V));
		System.out.println("B=" + Arrays.toString(B));
	

	}
	

	@Override
	public Action act(Vehicle vehicle, Task availableTask) 
	{
		nbActions++;
		
		Action action = null;

		if (availableTask == null)
		{
			int actionIndex = B[cityList.indexOf(vehicle.getCurrentCity())];
			if (actionIndex == numCities) 
			{
				System.out.print("Should never get here!");
				action = new Pickup(availableTask);
				System.out.printf("\n");
			}
			else
			{
				City nextCity = cityList.get(actionIndex);
				action = new Move(nextCity);
				System.out.printf("=> Move(%d) [reward=%d]\n", actionIndex, 0);
			}
		}
		else
		{
			int actionIndex = B[numCities + cityList.indexOf(vehicle.getCurrentCity())];
			if (actionIndex == numCities) 
			{
				action = new Pickup(availableTask);
				System.out.printf("=> Pick [reward=%d]\n", availableTask.reward);
			}
			else
			{
				City nextCity = cityList.get(actionIndex);
				action = new Move(nextCity);
				System.out.printf("=> Move(%d) [reward=%d]\n", actionIndex, 0);
			}
			
		}
		
		if (nbActions % 1000 == 0) 
		{
			System.out.println("The total profit after "+nbActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)nbActions)+")");
		}

		return action;
	}

}
