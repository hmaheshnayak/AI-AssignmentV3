package negotiator.group8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;

public class OpponentBid {
	public OpponentBid(ArrayList<Issue> issue,AgentID id){
		this.bids = new ArrayList<Bid>();
		this.issues = issue;
		this.id = id;
	}
	
	public List<Issue> issues ;
			//this.bids.get(0).getIssues();
	public Map<Issue, Double> issueWeight =  new HashMap<Issue, Double>();
	public Map<Issue, List<Value>> issueValue =  new HashMap<Issue, List<Value>>();
	public List<Map<Value, Double>> issueEvaValue = new ArrayList<Map<Value, Double>>();
	public AgentID id;
	public List<Bid> bids ;
	
	public void initializeIssueList(Bid bid){
		for (int i = 0;i < issues.size();i++){
			this.issueWeight.put(bid.getIssues().get(i), 0.0);
			this.issueValue.put(bid.getIssues().get(i), null);
		}
	}
	public void setProfile(){
		for (Bid bid : bids){
			for (int i = 0;i<this.issues.size();i++){
				try {
					this.issueValue.get(issues.get(i)).add(bid.getValue(i));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void analyzeProfile(){
		
	}
}
