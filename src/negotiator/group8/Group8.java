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

	private double acceptanceValue = 0.9;
	private double totalRounds;
	private int roundCounter = 0;
	private Bid lastBid;
	private List<OpponentModel> otherParties;
	private AbstractNegotiationParty lastBidder;
	
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
		this.totalRounds = timeline.getTotalTime() - 1;
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
		double tempAcceptanceValue = this.acceptanceValue;
		tempAcceptanceValue = 0.9 + 1 - Math.pow(Math.pow(1.9, 1/this.totalRounds), Math.pow(this.acceptanceValue,(this.totalRounds) / this.roundCounter ) * (this.roundCounter-1));
		this.roundCounter++;
		
		double lastBidUtility;
		if (!validActions.contains(Accept.class)) {
			return new Offer(generateHigherUtilityBid(tempAcceptanceValue).get(0));
		}
		else
		{
			try {
				lastBidUtility = this.utilitySpace.getUtility(this.lastBid);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				lastBidUtility = 0;
				e.printStackTrace();
			}
			if(lastBidUtility >= tempAcceptanceValue)
				return new Accept();
			else 
			{
				List<Bid> possibleBids = generateHigherUtilityBid(tempAcceptanceValue);
				
				if (this.roundCounter < 5)
				{
					return new Offer(possibleBids.get(0));
				}
				else
				{
					OpponentModel senderModel = null;
					
					for (OpponentModel opponent : this.otherParties)
					{
						if (opponent.agent.getPartyId().equals(this.lastBidder.getPartyId()))
						{
							int index = this.otherParties.indexOf(lastBidder);
							//senderModel = opponent;
							senderModel = this.otherParties.get((index + 1)%this.otherParties.size());
							break;
						}
					}
					
					double maxUtilityForOpponent = 0.0;
					Bid bestBid = null;
					
					for (Bid bid : possibleBids)
					{
						double bidUtility = senderModel.EvaluateBidUtility(bid);
						
						if (bidUtility > maxUtilityForOpponent)
						{
							bestBid = bid;
							maxUtilityForOpponent = bidUtility;
						}
					}
					
					return new Offer(bestBid);
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
		
		if (sender.equals("Protocol"))
		{
			super.receiveMessage(sender, action);
			
			//get number of agents in the negotiation
			Inform agentsInformation = (Inform)action;
			int numberOfAgents = (int)agentsInformation.getValue();
			
			//initialize list of other parties
			if (numberOfAgents > 1)
			{
				this.otherParties = new ArrayList<OpponentModel>();
			}
			
			return;
		}
		
		AbstractNegotiationParty senderAgent = (AbstractNegotiationParty)sender;
		this.lastBidder = senderAgent;
		
		//add sender agent to list of other parties if not present
		if (this.otherParties.contains(new OpponentModel(senderAgent)) == false)
		{
			OpponentModel newOpponent = new OpponentModel(senderAgent);
			this.otherParties.add(newOpponent);
		}
		
		if ((action instanceof Offer)) {
			 lastBid = ((Offer)action).getBid();
			 
			//Optional<OpponentModel> senderModel = this.otherParties.stream().filter(o -> o.agent.getPartyId() == senderAgent.getPartyId()).findFirst();
			 
			OpponentModel senderModel = null;
			
			for (OpponentModel opponent : this.otherParties)
			{
				if (opponent.agent.getPartyId() == senderAgent.getPartyId())
				{
					senderModel = opponent;
					break;
				}
			}
			
			if (senderModel != null)
			{
				try
				{
					
				//senderModel.get().AddBid(lastBid);
					senderModel.AddBid(lastBid);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					
					return;
				}
			}
			}
		
		// Here you can listen to other parties' messages		
	}
	/*
	* generates random bids which have higher utility than your reservation value
	*/
	private List<Bid> generateHigherUtilityBid(double utilityValue)
    {
        Bid randomBid;
        List<Bid> randomBidsList = new ArrayList<Bid>();
        
        double util;
        do
        {
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
        while(randomBidsList.size() < 5);
        return randomBidsList;
    }



	



}
