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
	
	//agent being modeled
	public AbstractNegotiationParty agent;
	
	//list of bids made by agent
	private List<Bid> bidsMadeByAgent;
	
	//list of issues and their associated weights
	private Map<Issue, Double> issueWeights; 
	
	//mapping of Issues to their values and number of bids made for each value
	private Map<Issue, Map<Value, Integer>> issueValueFrequencies;
	
	//mapping of Issues to their values and number of bids made for each value
	private Map<Issue, Map<Value, Integer>> sortedIssueValueFrequencies;
	
	//mapping of Issues to their values and number of bids made for each value
	private Map<Issue, Map<Value, Double>> issueEvaluationValues;
		
	/**
	 * class constructor
	 * @param opponentAgent agent whose preference is being modeled
	 */
	public OpponentModel(AbstractNegotiationParty opponentAgent) {
		
		this.agent = opponentAgent;
		
		this.bidsMadeByAgent = new ArrayList<Bid>();
		
		//get the issues that are in the domain
		List<Issue> issuesInDomain = this.agent.getUtilitySpace().getDomain().getIssues();
		
		this.issueWeights = new HashMap<Issue, Double>();
		this.issueValueFrequencies = new HashMap<Issue, Map<Value, Integer>>();
		this.issueEvaluationValues = new HashMap<Issue, Map<Value,Double>>();
		
		//for each issue in domain, add an entry in list of preferences 
		//initialize each issue with the initial weights
		for(Issue issue : issuesInDomain) {	
			this.issueWeights.put(issue, 0.0);
			
			this.issueValueFrequencies.put(issue, new HashMap<Value, Integer>());
			
			this.issueEvaluationValues.put(issue, new HashMap<Value, Double>());
		}
	}
	
	/**
	 * store bids made by this opponent for preference analysis
	 * @param bid
	 */
	public void AddBid(Bid bid) {
		if (bid != null) {
			this.bidsMadeByAgent.add(bid);
			
			this.UpdateIssueValueFrequencies(bid);
		}
		
		this.AnalyzeIssueValuePreferences();
	}
	
	/**
	 * update the count of bids made for every issue value
	 * @param newBid the bid based on which to update frequencies
	 */
	public void UpdateIssueValueFrequencies(Bid newBid)
	{
		for (int i = 0; i < newBid.getIssues().size(); i++) {
			Issue issue = newBid.getIssues().get(i);
			Value issueValue = null;
			
			try {
				issueValue = newBid.getValue(i+1);
			}
			catch(Exception e){
				e.printStackTrace();
				return;
			}
			
			Map<Value, Integer> valueMapping = issueValueFrequencies.get(issue);
			
			int currentFrequency = 0;
			if (valueMapping.containsKey(issueValue)) {
				currentFrequency = valueMapping.get(issueValue);
				currentFrequency++;
				
				valueMapping.put(issueValue, currentFrequency);
			}
			else {
				currentFrequency++;
				valueMapping.put(issueValue, currentFrequency);
			}
		}
	}
	
	/**
	 * method to analyze agent issue value preferences by bid frequency analysis
	 */
	public void AnalyzeIssueValuePreferences() {
		Iterator it = this.issueValueFrequencies.entrySet().iterator();
		
		sortedIssueValueFrequencies = new HashMap<Issue, Map<Value,Integer>>();
	
		while(it.hasNext()) {
			Map.Entry<Issue, Map<Value, Integer>> pair = (Map.Entry<Issue, Map<Value,Integer>>)it.next();
			Issue issue = pair.getKey();
			Map<Value,Integer> valueFrequencies = pair.getValue();
			
			//assign issue weights as inverse of number of different choices made for the issue
			double issueWeight = 1 /(double)valueFrequencies.size();
			this.issueWeights.put(issue, issueWeight);
			
			//sort the issue values by frequency of bids that target the particular value
			ValueComparator bvc =  new ValueComparator(valueFrequencies);
	        TreeMap<Value,Integer> sorted_map = new TreeMap<Value,Integer>(bvc);
	        sorted_map.putAll(valueFrequencies);
	        
	        int highestFrequency = sorted_map.firstEntry().getValue();
	        Iterator valueIterator = sorted_map.entrySet().iterator();
	        Map<Value, Double> evals = new HashMap<Value, Double>();
	        
	        //evaluation value of an issue value is the number of times the value was bid for
	        //divided by the number of times the highest bid value was bid for
	        while(valueIterator.hasNext()){
	        	Map.Entry<Value, Integer> valuePair = (Map.Entry<Value, Integer>)valueIterator.next();
	        	
	        	int valueFrequency = valuePair.getValue();
	        	
	        	double evaluationValue = valueFrequency / (double)highestFrequency;
	        	
	        	evals.put(valuePair.getKey(), evaluationValue);
	        }
	        
	        this.issueEvaluationValues.put(issue, evals);
	        this.sortedIssueValueFrequencies.put(issue, sorted_map);
		}
		
		this.UpdateIssueWeights();
	}
	
	/**
	 * method to estimate issue weights
	 */
	public void UpdateIssueWeights() {
		Iterator it = this.issueWeights.entrySet().iterator();
		double weightSum = 0.0;
		
		while(it.hasNext()) {
			Map.Entry<Value, Double> weightPair = (Map.Entry<Value, Double>)it.next();
        	
			weightSum += weightPair.getValue();
		}
		
		it = this.issueWeights.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry<Value, Double> weightPair = (Map.Entry<Value, Double>)it.next();
        	
			weightPair.setValue(weightPair.getValue() / weightSum);
		}
	}
	
	/**
	 * method to evaluate the utility of a bid for this agent
	 * @param bidToEvaluate bid whose utility to evaluate
	 * @return utility of bid for this agent
	 */
	public double EvaluateBidUtility(Bid bidToEvaluate) {
		double utilityValue = 0.0;
		
		// utility value = u1 * w1 + u2 * w2 + ...
		for (int i = 0; i < bidToEvaluate.getIssues().size(); i++) {
			Issue issue = bidToEvaluate.getIssues().get(i);
			Value issueValue = null;
			
			try {
				issueValue = bidToEvaluate.getValue(i + 1);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			
			if (issueValue != null) {
				double issueWeight = this.issueWeights.get(issue);
				
				Map<Value, Double> evaluationValues = this.issueEvaluationValues.get(issue);
				double evaluationValue = 0.0;
				
				if (evaluationValues.isEmpty() == false && evaluationValues.containsKey(issueValue)){
					evaluationValue = evaluationValues.get(issueValue);
				}
				
				utilityValue += issueWeight * evaluationValue;
			}
		}
		
		return utilityValue;
	}
	
	/**
	 * overridden equals method to compare two OpponentModel objects
	 * they are equal if they model the same agent, determined by agentID
	 * @param object object being compared
	 */
	@Override
	public boolean equals(Object object)
	{
		boolean result = false;
		
		if (object instanceof OpponentModel) {
			OpponentModel toCompare = (OpponentModel)object;
			
			if (this.agent.getPartyId().equals(toCompare.agent.getPartyId())) {
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * Class that is used to compare issue values
	 *
	 */
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
	        }
	    }
	}
}
