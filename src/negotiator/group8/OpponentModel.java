package negotiator.group8;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;


/**
 * Class that models opponent preference profile
 *
 */
public class OpponentModel {
	
	//opponent agent
	public AbstractNegotiationParty agent;
	
	//list of issues and their associated value preferences in descending order
	private Map<Issue, List<String>> issuePreferences;
	
	//list of issues and their associated weights
	private Map<Issue, Double> issueWeights; 
	
	//list of bids made by agent
	private List<Bid> bidsMadeByAgent;
	
	//mapping of Issues to their values and number of bids made for each value
	private Map<Issue, Map<Value, Integer>> issueValueFrequencies;
	
	//mapping of Issues to their values and number of bids made for each value
	Map<Issue, Map<Value, Integer>> sortedIssueValueFrequencies;
			
	/**
	 * class constructor
	 * @param opponentAgent agent whose preference is being modeled
	 */
	public OpponentModel(AbstractNegotiationParty opponentAgent)
	{
		this.agent = opponentAgent;
		
		List<Issue> issuesInDomain = this.agent.getUtilitySpace().getDomain().getIssues();
		
		//initialize issue with equal weights assigned to all issues 
		//such that their sum equals 1
		int issueCount = issuesInDomain.size();
		double initialWeights = 1.0 / issueCount;
		
		this.issuePreferences = new HashMap<Issue, List<String>>();
		this.issueWeights = new HashMap<Issue, Double>();
		
		this.issueValueFrequencies = new HashMap<Issue, Map<Value, Integer>>();
		
		//for each issue in domain, add an entry in list of preferences 
		//initialize each issue with the initial weights
		for(Issue issue : issuesInDomain)
		{
			this.issuePreferences.put(issue, new ArrayList<String>());
			
			this.issueWeights.put(issue, initialWeights);
			
			this.issueValueFrequencies.put(issue, new HashMap<Value, Integer>());
		}
		
	}
	
	//store bids made by this opponent in private list
	public void AddBid(Bid bid)
	{
		if (this.bidsMadeByAgent == null)
		{
			this.bidsMadeByAgent = new ArrayList<Bid>();
		}
		
		if (bid != null)
		{
			this.bidsMadeByAgent.add(bid);
		}
		
		try {
			this.UpdateIssueValueFrequencies(bid);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if (this.bidsMadeByAgent.size() > 10)
		{
			this.AnalyzeIssueValuePreferences();
		}
		
	}
	
	class ValueComparator implements Comparator<Value> {

	    Map<Value, Integer> base;
	    public ValueComparator(Map<Value, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(Value a, Value b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	
	public void AnalyzeIssueValuePreferences()
	{
		Iterator it = this.issueValueFrequencies.entrySet().iterator();
		
		
		sortedIssueValueFrequencies = new HashMap<Issue, Map<Value,Integer>>();
	
		
		while(it.hasNext()){
			Map.Entry<Issue, Map<Value, Integer>> pair = (Map.Entry<Issue, Map<Value,Integer>>)it.next();
			Issue issue = pair.getKey();
			Map<Value,Integer> valueFrequecies = pair.getValue();
			
			ValueComparator bvc =  new ValueComparator(valueFrequecies);
	        TreeMap<Value,Integer> sorted_map = new TreeMap<Value,Integer>(bvc);
	        sorted_map.putAll(valueFrequecies);
	        
	        System.out.println(sorted_map); 
	        
	        sortedIssueValueFrequencies.put(issue, sorted_map);
	        
		}
		
	}
	
	public void UpdateIssueValueFrequencies(Bid newBid) throws Exception
	{
//		for (Bid bid : this.bidsMadeByAgent)
//		{
			for (int i = 0; i < newBid.getIssues().size(); i++)
			{
				Issue issue = newBid.getIssues().get(i);
				Value issueValue = newBid.getValue(i+1);
				
				Map<Value, Integer> valueMapping = issueValueFrequencies.get(issue);
				
				int currentFrequency = 0;
				if (valueMapping.containsKey(issueValue))
				{
					currentFrequency = valueMapping.get(issueValue);
					currentFrequency++;
					
					valueMapping.replace(issueValue, currentFrequency);
				}
				else
				{
					currentFrequency++;
					valueMapping.put(issueValue, currentFrequency);
				}
			}
//		}
//		}
		
	}
	
	/**
	 * overridden equals method to compare two OpponentModel objects
	 * they are equal if they encapsulate the same agent, determined by agentID
	 * @param object object being compared
	 */
//	@Override
//	public boolean equals(Object object)
//	{
//		boolean result = false;
//		
//		if (object == null) 
//		{
//		    result = false;
//		} 
//		else 
//		{
//			AbstractNegotiationParty party = (AbstractNegotiationParty)object;
//			
//			if (this.agent.getPartyId() == party.getPartyId())
//			{
//				result = true;
//			}
//		}
//		
//		return result;
//	}
	
	@Override
	public boolean equals(Object object)
	{
		boolean result = false;
		
		if (object instanceof OpponentModel)
		{
			OpponentModel toCompare = (OpponentModel)object;
			
			if (this.agent.getPartyId().equals(toCompare.agent.getPartyId()))
			{
				result = true;
			}
		}
		
		return result;
	}
}
