package negotiator.group8;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import negotiator.Bid;
import negotiator.DeadlineType;
import negotiator.Timeline;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Inform;
import negotiator.actions.Offer;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.utility.UtilitySpace;

import java.lang.Math;

/**
 * This is your negotiation party.
 */
public class Group8 extends AbstractNegotiationParty {

	//utility value above which bids will be accepted
	private double acceptanceValue;
	
	//number of rounds available for negotiation
	private double totalRounds;
	
	//number of rounds of negotiation completed
	private int roundCounter;
	
	//most recent bid
	private Bid mostRecentBid;
	
	//most recent bidder
	private AbstractNegotiationParty mostRecentBidder;
	
	//list of opponents modeled by opponentModel class
	private List<OpponentModel> opponents;
	
	/**
	 * Please keep this constructor. This is called by genius.
	 *
	 * @param utilitySpace Your utility space.
	 * @param deadlines The deadlines set for this negotiation.
	 * @param timeline Value counting from 0 (start) to 1 (end).
	 * @param randomSeed If you use any randomization, use this seed for it.
	 */
	public Group8(UtilitySpace utilitySpace,
				  Map<DeadlineType, Object> deadlines,
				  Timeline timeline,
				  long randomSeed) {
		// Make sure that this constructor calls it's parent.
		super(utilitySpace, deadlines, timeline, randomSeed);
		
		//get total number of rounds available from the environment
		this.totalRounds = timeline.getTotalTime() - 1;
		
		this.roundCounter = 0;
		
		this.acceptanceValue = 0.5;
	}
	
	/**
	 * Each round this method gets called and ask you to accept or offer. The first party in
	 * the first round is a bit different, it can only propose an offer.
	 *
	 * @param validActions Either a list containing both accept and offer or only offer.
	 * @return The chosen action.
	 */
	@Override
	public Action chooseAction(List<Class> validActions) {
		this.roundCounter++;
		
		//double tempAcceptanceValue = this.acceptanceValue;
		this.acceptanceValue = 0.9 + 1 - Math.pow(Math.pow(1.9, 1/this.totalRounds), Math.pow(this.acceptanceValue,(this.totalRounds) / this.roundCounter ) * (this.roundCounter-1));
		
		//first bid - offer a bid with high utility value for self
		if (!validActions.contains(Accept.class)) {
			Bid firstBid = this.generateHigherUtilityBid(this.acceptanceValue).get(0);
			
			return new Offer(firstBid);
		}
		//choosing action after a bid has been made
		else
		{
			//evaluate the utility of most recent bid
			double utilityOfMostRecentBid;
			
			try {
				utilityOfMostRecentBid = this.utilitySpace.getUtility(this.mostRecentBid);
			} 
			catch (Exception e) {
				utilityOfMostRecentBid = 0;
				e.printStackTrace();
			}
			
			//accept offer if utility is higher than our acceptance value
			if(utilityOfMostRecentBid >= this.acceptanceValue) {
				return new Accept();
			}
			//make counter offer 
			else {
				//generate some high utility bids
				List<Bid> possibleBids = generateHigherUtilityBid(this.acceptanceValue);
				
				//when too few rounds have passed, offer a random high utility bid
				if (this.roundCounter < 5) {
					return new Offer(possibleBids.get(0));
				}
				//among high utility bids choose one that has best utility for next agent
				else {
					//find next agent among list of opponents
					OpponentModel nextAgent = null;
					
					for (OpponentModel opponent : this.opponents) {
						if (opponent.agent.getPartyId().equals(this.mostRecentBidder.getPartyId())) {
							int indexOfPreviousBidder = this.opponents.indexOf(mostRecentBidder);
							
							int indexOfNextAgent = (indexOfPreviousBidder + 1)%this.opponents.size();
							
							nextAgent = this.opponents.get(indexOfNextAgent);
							break;
						}
					}
					
					//find bid with highest utility for next agent
					double maxUtilityForOpponent = 0.0;
					Bid bestBidForNextAgent = null;
					
					for (Bid bid : possibleBids) {
						double bidUtility = nextAgent.EvaluateBidUtility(bid);
						
						if (bidUtility > maxUtilityForOpponent) {
							bestBidForNextAgent = bid;
							maxUtilityForOpponent = bidUtility;
						}
					}
					
					return new Offer(bestBidForNextAgent);
				}
			}
		}
	}


	/**
	 * All offers proposed by the other parties will be received as a message.
	 * You can use this information to your advantage, for example to predict their utility.
	 *
	 * @param sender The party that did the action.
	 * @param action The action that party did.
	 */
	@Override
	public void receiveMessage(Object sender, Action action) {
		
		if (sender.equals("Protocol")) {
			super.receiveMessage(sender, action);
			
			//get number of agents in the negotiation
			Inform agentsInformation = (Inform)action;
			int numberOfAgents = (int)agentsInformation.getValue();
			
			//initialize list of other parties
			if (numberOfAgents > 1)
			{
				this.opponents = new ArrayList<OpponentModel>();
			}
			return;
		}
		
		AbstractNegotiationParty senderAgent = (AbstractNegotiationParty)sender;
		this.mostRecentBidder = senderAgent;
		
		//add sender agent to list of other parties if not present
		if (this.opponents.contains(new OpponentModel(senderAgent)) == false) {
			OpponentModel newOpponent = new OpponentModel(senderAgent);
			this.opponents.add(newOpponent);
		}
		
		
		if ((action instanceof Offer)) {
			mostRecentBid = ((Offer)action).getBid();
			
			OpponentModel senderModel = null;
			
			for (OpponentModel opponent : this.opponents)
			{
				if (opponent.agent.getPartyId() == senderAgent.getPartyId())
				{
					senderModel = opponent;
					break;
				}
			}
			
			if (senderModel != null)
			{
				try {
					senderModel.AddBid(mostRecentBid);
				}
				catch(Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}	
	}
	
	/*
	* generates random bids which have higher utility than your reservation value
	*/
	private List<Bid> generateHigherUtilityBid(double utilityValue)
    {
        Bid randomBid;
        List<Bid> randomBidsList = new ArrayList<Bid>();
        
        int counter = 0;
        
        double util;
        do
        {
        	counter++;
        	randomBid = generateRandomBid();
        try
        {
            util = utilitySpace.getUtility(randomBid);
        } catch (Exception e)
        {
            util = 0.0;
        }
        
        if (util > utilityValue && util < (utilityValue + 0.05))
        {
        	randomBidsList.add(randomBid);
        }
        
        }
        while(counter < 100);
        
        System.out.println(randomBidsList.size());
        return randomBidsList;
    }



	



}
